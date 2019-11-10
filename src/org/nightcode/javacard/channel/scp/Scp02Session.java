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

import org.nightcode.javacard.JavaCardException;
import org.nightcode.javacard.channel.CardChannelContext;
import org.nightcode.javacard.channel.SecureChannelSession;
import org.nightcode.javacard.channel.key.KeySet;
import org.nightcode.javacard.channel.key.KeyUsage;
import org.nightcode.javacard.channel.key.SessionKeys;
import org.nightcode.javacard.common.Apdu;
import org.nightcode.javacard.common.SecurityLevel;
import org.nightcode.javacard.util.ApduPreconditions;
import org.nightcode.javacard.util.ByteArrayGenerator;
import org.nightcode.javacard.util.Hexs;
import org.nightcode.javacard.util.Iso7816D4;
import org.nightcode.javacard.util.JcCryptoUtils;
import org.nightcode.javacard.util.JcUtils;
import org.nightcode.javacard.util.SecureRandomByteArrayGenerator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class Scp02Session implements SecureChannelSession {

  private static final Logger LOGGER = Logger.getLogger(Scp02Session.class.getName());

  private static final Hexs HEX = Hexs.hex();

  private final CardChannelContext context;
  private final ByteArrayGenerator byteArrayGenerator;

  private volatile Scp02ApduChannel channel;

  private final Cipher desEdeCipher;

  public Scp02Session(CardChannelContext context) {
    this(context, new SecureRandomByteArrayGenerator());
  }

  Scp02Session(CardChannelContext context, ByteArrayGenerator byteArrayGenerator) {
    this.context = context;
    this.byteArrayGenerator = byteArrayGenerator;

    try {
      desEdeCipher = Cipher.getInstance(JcCryptoUtils.DES_EDE_ECB_NO_PADDING);
    } catch (GeneralSecurityException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override public byte[] encryptData(byte[] data) throws GeneralSecurityException {
    Key dek = context.getSessionKeys().getDesEde(KeyUsage.DEK);
    desEdeCipher.init(Cipher.ENCRYPT_MODE, dek);
    return desEdeCipher.doFinal(data);
  }

  @Override public void openSecureChannel(EnumSet<SecurityLevel> securityLevel)
      throws IOException, JavaCardException {
    if (securityLevel.contains(SecurityLevel.C_DECRYPTION) && !securityLevel.contains(SecurityLevel.C_MAC)) {
      throw new IllegalArgumentException("C_DECRYPTION must be combined with C_MAC");
    }

    byte hostKeyVersionNumber = context.getCardProperties().keyVersionNumber();

    Scp02Context scp02Context = initializeUpdate(hostKeyVersionNumber);
    byte[] icv = externalAuthenticate(scp02Context, securityLevel);

    channel = new Scp02ApduChannel(context, securityLevel, icv);
    channel.setRicv(icv);
  }

  @Override public ResponseAPDU transmit(CommandAPDU request) throws IOException {
    return channel.transmit(request);
  }

  Scp02Context initializeUpdate(byte keyVersionNumber) throws IOException, JavaCardException {
    byte[] hostChallenge = byteArrayGenerator.generate(8);

    CommandAPDU request = new CommandAPDU(Apdu.CLA_GP, Apdu.INS_INITIALIZE_UPDATE
        , keyVersionNumber, Apdu.P2_INITIALIZE_UPDATE, hostChallenge, 256);
    ResponseAPDU response = context.channel().transmit(request);

    int sw = response.getSW();
    if ((sw == Apdu.SW_SECURITY_STATUS_NOT_SATISFIED) || (sw == Apdu.SW_FILE_INVALID)) {
      throw new JavaCardException("[SW=0x%04X] INITIALIZE UPDATE failed, card LOCKED?", sw);
    }
    if (sw == Apdu.SW_REFERENCED_DATA_NOT_FOUND) {
      throw new JavaCardException("[SW=0x%04X] INITIALIZE UPDATE failed, referenced data not found", sw);
    }
    ApduPreconditions.checkSw("INITIALIZE UPDATE failed", sw, 0x9000);

    byte[] data = response.getData();
    if (data.length != 28) {
      throw new JavaCardException("invalid INITIALIZE UPDATE response message length %dB", data.length);
    }

    int offset = 0;
    byte[] keyDiversificationData = Arrays.copyOfRange(data, offset, 10);

    offset += keyDiversificationData.length;
    byte[] keyInformation = Arrays.copyOfRange(data, offset, offset + 2);
    byte cardKeyVersionNumber = (byte) (keyInformation[0] & 0xFF);
    int scpVersion = keyInformation[1] & 0xFF;

    offset += keyInformation.length;
    byte[] sequenceCounter = Arrays.copyOfRange(data, offset, offset + 2);

    offset += sequenceCounter.length;
    byte[] cardChallenge = Arrays.copyOfRange(data, offset, offset + 6);

    offset += cardChallenge.length;
    byte[] cardCryptogram = Arrays.copyOfRange(data, offset, offset + 8);

    LOGGER.log(Level.FINER, String.format(
              "  Key diversification data: %s"
            + "\n  Key information:          keyVersionNumber=%s; SCP_0%s"
            + "\n  Sequence counter:         %s"
            + "\n  Card challenge:           %s"
            + "\n  Card cryptogram:          %s"
        , HEX.fromByteArray(keyDiversificationData)
        , cardKeyVersionNumber, scpVersion
        , HEX.fromByteArray(sequenceCounter)
        , HEX.fromByteArray(cardChallenge)
        , HEX.fromByteArray(cardCryptogram)
    ));

    if (ScpVersion.SCP_02.version() != scpVersion) {
      throw new JavaCardException("SCP version mismatch: SCP_02 != SCP_0%s", scpVersion);
    }

    if ((keyVersionNumber > 0) && (keyVersionNumber != cardKeyVersionNumber)) {
      throw new JavaCardException("key version mismatch: %s != %s", keyVersionNumber, cardKeyVersionNumber);
    }

    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);

    Key encKey = sessionKeys.getDesEde(KeyUsage.ENC);
    byte[] calculatedCardCryptogram;
    try {
      calculatedCardCryptogram = generateCryptogram(encKey, hostChallenge, sequenceCounter, cardChallenge);
    } catch (GeneralSecurityException ex) {
      throw new JavaCardException("can't calculate card cryptogram", ex);
    }
    if (!Arrays.equals(cardCryptogram, calculatedCardCryptogram)) {
      throw new JavaCardException("Card Cryptogram verification failed: "
          + "    \nCard cryptogram:       %s    \nCalculated cryptogram: %s"
          , HEX.fromByteArray(cardCryptogram), HEX.fromByteArray(calculatedCardCryptogram)
      );
    }
    LOGGER.log(Level.INFO, "verified Card Cryptogram: " + HEX.fromByteArray(cardCryptogram));

    return new Scp02Context(hostChallenge, cardChallenge, sequenceCounter);
  }

  byte[] externalAuthenticate(Scp02Context scp02Context, EnumSet<SecurityLevel> securityLevel)
      throws IOException, JavaCardException {
    int p1 = 0;
    for (SecurityLevel level : securityLevel) {
      p1 |= level.bitMask();
    }

    Scp02ApduChannel initialSecuredChannel = new Scp02ApduChannel(context, EnumSet.of(SecurityLevel.C_MAC));

    Key encKey = context.getSessionKeys().getDesEde(KeyUsage.ENC);
    byte[] hostCryptogram;
    try {
      hostCryptogram = generateCryptogram(encKey, scp02Context.getSequenceCounter(), scp02Context.getCardChallenge()
          , scp02Context.getHostChallenge());
    } catch (GeneralSecurityException ex) {
      throw new JavaCardException("can't generate host cryptogram", ex);
    }

    CommandAPDU request = new CommandAPDU(Apdu.CLA_GP, Apdu.INS_EXTERNAL_AUTHENTICATE
        , p1, Apdu.P2_EXTERNAL_AUTHENTICATE, hostCryptogram);
    ResponseAPDU response = initialSecuredChannel.transmit(request);

    int sw = response.getSW();
    if (response.getSW() == Apdu.SW_AUTHENTICATION_OF_HOST_CRYPTOGRAM_FAILED) {
      throw new JavaCardException("[SW=0x%04X] EXTERNAL AUTHENTICATE authentication of host cryptogram failed", sw);
    }

    ApduPreconditions.checkSw("EXTERNAL AUTHENTICATE failed", sw, 0x9000);

    return initialSecuredChannel.getIcv();
  }

  private byte[] generateCryptogram(Key key, byte[]... blocks) throws GeneralSecurityException {
    byte[] buffer = JcUtils.joinArrays(blocks);
    buffer = Iso7816D4.pad(buffer);

    Cipher cipher = Cipher.getInstance(JcCryptoUtils.DES_EDE_CBC_NO_PADDING);
    cipher.init(Cipher.ENCRYPT_MODE, key, JcCryptoUtils.ZERO_IV_PARAMETER_SPEC);

    byte[] result = cipher.doFinal(buffer, 0, buffer.length);
    return Arrays.copyOfRange(result, result.length - 8, result.length);
  }
}
