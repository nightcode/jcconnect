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
import org.nightcode.javacard.util.JcCryptoUtils;

import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public interface KeyProvider {

  final class DefaultKeyProvider implements KeyProvider {

    private static final byte[] DEFAULT_KEY_BYTES = Hexs.hex().toByteArray("404142434445464748494A4B4C4D4E4F");

    private static final Key BASE_KEY = new SecretKeySpec(JcCryptoUtils.toKey24(DEFAULT_KEY_BYTES), "DESede");

    private DefaultKeyProvider() {
      // do nothing
    }

    @Override public Key deriveSessionKey(KeyContext context, byte keyIdentifier, KeyUsage usage) {
      if (DerivationType.SCP_02.equals(context.derivationType())) {
        return deriveScp02SessionKey(usage.keyConstant(), context.sequenceCounter());
      }
      throw new IllegalArgumentException("unsupported derivation type " + context.derivationType());
    }

    private Key deriveScp02SessionKey(byte[] deriveKeyConstant, byte[] sequenceCounter) {
      byte[] derivationData = new byte[16];
      System.arraycopy(deriveKeyConstant, 0, derivationData, 0, 2);
      System.arraycopy(sequenceCounter, 0, derivationData, 2, 2);
      try {
        Cipher cipher = Cipher.getInstance(JcCryptoUtils.DES_EDE_CBC_NO_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, BASE_KEY, JcCryptoUtils.ZERO_IV_PARAMETER_SPEC);
        byte[] result = cipher.doFinal(derivationData);
        return new SecretKeySpec(JcCryptoUtils.toKey24(result), "DESede");
      } catch (GeneralSecurityException ex) {
        throw new RuntimeException("session keys calculation failed", ex);
      }
    }
  }

  KeyProvider DEFAULT = new DefaultKeyProvider();

  Key deriveSessionKey(KeyContext context, byte keyIdentifier, KeyUsage usage);
}
