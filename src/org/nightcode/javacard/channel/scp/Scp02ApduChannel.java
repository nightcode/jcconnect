/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nightcode.javacard.channel.scp;

import org.nightcode.common.base.Hexs;
import org.nightcode.javacard.JavaCardException;
import org.nightcode.javacard.channel.ApduChannel;
import org.nightcode.javacard.channel.CardChannelContext;
import org.nightcode.javacard.channel.key.KeyUsage;
import org.nightcode.javacard.channel.key.SessionKeys;
import org.nightcode.javacard.common.SecurityLevel;
import org.nightcode.javacard.util.Iso7816D4;
import org.nightcode.javacard.util.JcCryptoUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.EnumSet;

import javax.crypto.Cipher;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

final class Scp02ApduChannel implements ApduChannel {

  private static final int CMAC_LENGTH = 8;
  private static final int ENC_LENGTH = 8;

  private static final int SWS_LENGTH = 2;

  private static final Hexs HEX = Hexs.hex();

  private final ApduChannel channel;
  private final SessionKeys sessionKeys;
  private final EnumSet<SecurityLevel> securityLevel;
  private final Scp02ParameterI i;
  private final int maxDataLength;

  private volatile byte[] icv;
  private volatile byte[] rIcv;
  private volatile byte[] rMac;

  private final Cipher desCipher;
  private final Cipher desCbcCipher;

  Scp02ApduChannel(CardChannelContext context, Scp02ParameterI i, EnumSet<SecurityLevel> securityLevel) {
    this(context.channel(), context.getMaxLength(), i, securityLevel, context.getSessionKeys(), null);
  }

  Scp02ApduChannel(CardChannelContext context, Scp02ParameterI i, EnumSet<SecurityLevel> securityLevel, byte[] icv) {
    this(context.channel(), context.getMaxLength(), i, securityLevel, context.getSessionKeys(), icv);
  }

  Scp02ApduChannel(ApduChannel channel, int maxLength, Scp02ParameterI i, EnumSet<SecurityLevel> securityLevel,
      SessionKeys sessionKeys, byte[] icv) {
    this.channel = channel;
    this.i = i;
    this.securityLevel = securityLevel;
    this.sessionKeys = sessionKeys;
    this.icv = icv;

    int length = maxLength;
    if (securityLevel.contains(SecurityLevel.C_MAC)) {
      length -= CMAC_LENGTH;
      if (securityLevel.contains(SecurityLevel.C_DECRYPTION)) {
        length -= ENC_LENGTH;
      }
    }
    this.maxDataLength = length;

    try {
      desCipher = Cipher.getInstance(JcCryptoUtils.DES_ECB_NO_PADDING);
      desCbcCipher = Cipher.getInstance(JcCryptoUtils.DES_EDE_CBC_NO_PADDING);
    } catch (GeneralSecurityException ex) {
      throw new RuntimeException("unsupported cryptographic algorithm", ex);
    }
  }

  @Override public ResponseAPDU transmit(CommandAPDU request) throws IOException {
    try {
      ResponseAPDU response = channel.transmit(wrap(request));
      return unwrap(response);
    } catch (Exception ex) {
      throw new IOException(String.format("APDU [%s] transmit problem: %s"
          , HEX.fromByteArray(request.getBytes()), ex.getMessage()), ex);
    }
  }

  byte[] getIcv() {
    return icv;
  }

  void setRicv(byte[] rIcv) {
    this.rIcv = rIcv;
  }

  private byte[] commandEncryption(Key key, byte[] data) throws GeneralSecurityException {
    desCbcCipher.init(Cipher.ENCRYPT_MODE, key, JcCryptoUtils.ZERO_IV_PARAMETER_SPEC);
    return desCbcCipher.doFinal(data);
  }

  private byte[] icvEncryption(Key key, byte[] icv) throws GeneralSecurityException {
    desCipher.init(Cipher.ENCRYPT_MODE, key);
    return desCipher.doFinal(icv);
  }

  private ResponseAPDU unwrap(ResponseAPDU origin) throws JavaCardException {
    if (securityLevel.contains(SecurityLevel.R_MAC)) {
      byte[] data = origin.getData();
      if (data.length < 8) {
        throw new JavaCardException("received invalid APDU response %s", Hexs.hex().fromByteArray(data));
      }
      int rDataLength = data.length - 8;
      byte[] buffer = new byte[Iso7816D4.paddedLength(rMac.length + 1 + rDataLength + SWS_LENGTH)];
      int offset = 0;
      System.arraycopy(rMac, 0, buffer, offset, rMac.length);
      offset += rMac.length;
      buffer[offset++] = (byte) rDataLength;
      System.arraycopy(data, 0, buffer, offset, rDataLength);
      offset += rDataLength;
      buffer[offset++] = (byte) origin.getSW1();
      buffer[offset++] = (byte) origin.getSW2();
      buffer[offset] = (byte) 0x80;

      Key sessionMacKey = sessionKeys.getDesEde(KeyUsage.R_MAC);

      byte[] rmac = JcCryptoUtils.macAlgorithm3(sessionMacKey, rIcv, buffer);
      rIcv = rmac;

      byte[] cardRmac = new byte[8];
      System.arraycopy(data, rDataLength, cardRmac, 0, cardRmac.length);
      if (!Arrays.equals(rmac, cardRmac)) {
        throw new JavaCardException("invalid RMAC value %s, expected %s"
            , HEX.fromByteArray(cardRmac), HEX.fromByteArray(rmac));
      }

      return new ResponseAPDU(Arrays.copyOfRange(buffer, rMac.length + 1, offset));
    }

    return origin;
  }

  private CommandAPDU wrap(CommandAPDU origin) throws IOException, GeneralSecurityException {
    byte[] apdu = origin.getBytes();
    int nc = origin.getNc();
    if (securityLevel.contains(SecurityLevel.R_MAC)) {
      if (nc == 0) {
        rMac = Arrays.copyOf(apdu, 4);
      } else {
        rMac = Arrays.copyOf(apdu, 5 + nc);
      }
      rMac[0] &= (byte) 0xF8;
    }

    if (!securityLevel.contains(SecurityLevel.C_MAC)) {
      return origin;
    }

    if (nc > maxDataLength) {
      throw new IOException(String.format("APDU command DATA length %d MUST be <= %d", nc, maxDataLength));
    }

    byte[] buffer = new byte[Iso7816D4.paddedLength(5 + nc)];
    System.arraycopy(apdu, 0, buffer, 0, 4);
    buffer[4] = (byte) nc;
    if (nc > 0) {
      System.arraycopy(apdu, 5, buffer, 5, nc);
    }
    buffer[5 + nc] = (byte) 0x80;

    if (i.cMacOnModifiedApdu()) {
      buffer[0] |= 0x04;
      buffer[4] += CMAC_LENGTH;
    }



    if (icv == null) {
      icv = new byte[8];
    } else if (i.icvEncryptionForCMacSession()) {
      Key key = sessionKeys.getDes(KeyUsage.MAC);
      icv = icvEncryption(key, icv);
    }

    Key sessionMacKey = sessionKeys.getDesEde(KeyUsage.MAC);
    byte[] cmac = JcCryptoUtils.macAlgorithm3(sessionMacKey, icv, buffer);
    icv = cmac;

    if (i.cMacOnUnmodifiedApdu()) {
      buffer[0] |= 0x04;
      buffer[4] += CMAC_LENGTH;
    }

    byte[] commandData;
    if (nc > 0 && securityLevel.contains(SecurityLevel.C_DECRYPTION)) {
      Key sessionEncKey = sessionKeys.getDesEde(KeyUsage.ENC);
      byte[] paddedData = Iso7816D4.pad(apdu, 5, nc);
      commandData = commandEncryption(sessionEncKey, paddedData);
      buffer[4] += paddedData.length - nc;
    } else {
      commandData = origin.getData();
    }

    int offset = 0;
    byte[] command = new byte[5 + commandData.length + CMAC_LENGTH + ((origin.getNe() > 0) ? 1 : 0)];
    System.arraycopy(buffer, 0, command, offset, 5);
    offset += 5;
    if (commandData.length > 0) {
      System.arraycopy(commandData, 0, command, offset, commandData.length);
      offset += commandData.length;
    }
    System.arraycopy(cmac, 0, command, offset, cmac.length);
    if (origin.getNe() > 0) {
      command[offset + cmac.length] = (byte) origin.getNe();
    }

    return new CommandAPDU(command);
  }
}
