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

package org.nightcode.javacard.channel.key;

import org.nightcode.common.base.Hexs;
import org.nightcode.javacard.AbstractJcTest;
import org.nightcode.javacard.channel.CardChannelContext;
import org.nightcode.javacard.util.JcCryptoUtils;

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Test;

public class Scp02KeySetTest extends AbstractJcTest {

  private static class TestKeyProvider implements KeyProvider {
    private static final byte[] ENC_KEY_BYTES = Hexs.hex().toByteArray("0123456789ABCDEFFEDCBA9876543210");
    private static final byte[] MAC_KEY_BYTES = Hexs.hex().toByteArray("0123456789ABCDEFFEDCBA9876543211");
    private static final byte[] DEK_KEY_BYTES = Hexs.hex().toByteArray("0123456789ABCDEFFEDCBA9876543212");

    private static final Key ENC_BASE_KEY = new SecretKeySpec(JcCryptoUtils.toKey24(ENC_KEY_BYTES), "DESede");
    private static final Key MAC_BASE_KEY = new SecretKeySpec(JcCryptoUtils.toKey24(MAC_KEY_BYTES), "DESede");
    private static final Key DEK_BASE_KEY = new SecretKeySpec(JcCryptoUtils.toKey24(DEK_KEY_BYTES), "DESede");

    @Override public Key deriveSessionKey(KeyContext context, KeyUsage usage) {
      if (DerivationType.SCP_02.equals(context.derivationType())) {
        Key baseKey = getKey(getKeyIdentifier(context, usage), context.cardProperties().getKeyVersionNumber());
        return deriveScp02SessionKey(baseKey, usage.keyConstant(), context.sequenceCounter());
      }
      throw new IllegalArgumentException("unsupported derivation type " + context.derivationType());
    }

    private Key deriveScp02SessionKey(Key baseKey, byte[] deriveKeyConstant, byte[] sequenceCounter) {
      byte[] derivationData = new byte[16];
      System.arraycopy(deriveKeyConstant, 0, derivationData, 0, 2);
      System.arraycopy(sequenceCounter, 0, derivationData, 2, 2);
      try {
        Cipher cipher = Cipher.getInstance(JcCryptoUtils.DES_EDE_CBC_NO_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, baseKey, JcCryptoUtils.ZERO_IV_PARAMETER_SPEC);
        byte[] result = cipher.doFinal(derivationData);
        return new SecretKeySpec(JcCryptoUtils.toKey24(result), "DESede");
      } catch (GeneralSecurityException ex) {
        throw new RuntimeException("session keys calculation failed", ex);
      }
    }

    private Key getKey(byte keyIdentifier, byte keyVersionNumber) {
      Key key;
      switch (keyIdentifier) {
        case 0x01:
          key = ENC_BASE_KEY;
          break;
        case 0x02:
          key = MAC_BASE_KEY;
          break;
        case 0x03:
          key = DEK_BASE_KEY;
          break;
        default:
          throw new IllegalArgumentException("wrong key identifier value: " + keyIdentifier);
      }
      return key;
    }
  }
  
  @Test public void testDeriveSessionKeys() {
    CardChannelContext context = createContext(request -> null);

    KeySet keySet = new Scp02KeySet(new TestKeyProvider());

    byte[] sequenceCounter = new byte[] {0x00, 0x1C};

    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);

    Key enc = sessionKeys.getDesEde(KeyUsage.ENC);
    Key mac = sessionKeys.getDesEde(KeyUsage.MAC);
    Key dek = sessionKeys.getDesEde(KeyUsage.DEK);
    Key rMac = sessionKeys.getDesEde(KeyUsage.R_MAC);

    Assert.assertEquals("48B8C246FD202D2965CCB8D8FF35CDC148B8C246FD202D29", HEX.fromByteArray(enc.getEncoded()));
    Assert.assertEquals("7BDAEAA601EC18885053EDAF4F2081107BDAEAA601EC1888", HEX.fromByteArray(mac.getEncoded()));
    Assert.assertEquals("1959DCECE0CC551E1A824096FEC209451959DCECE0CC551E", HEX.fromByteArray(dek.getEncoded()));
    Assert.assertEquals("CF98C95DF537D8AC05E16B8BEBC50DABCF98C95DF537D8AC", HEX.fromByteArray(rMac.getEncoded()));
  }
}
