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

package org.nightcode.javacard.util;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.macs.ISO9797Alg3Mac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public final class JcCryptoUtils {

  public static final String DES_ECB_NO_PADDING = "DES/ECB/NoPadding";
  public static final String DES_EDE_ECB_NO_PADDING = "DESede/ECB/NoPadding";
  public static final String DES_EDE_CBC_NO_PADDING = "DESede/CBC/NoPadding";

  public static final int DEFAULT_MAC_SIZE_BITS = 64;

  public static final byte[] ZERO_ARRAY_8 = new byte[8];
  public static final IvParameterSpec ZERO_IV_PARAMETER_SPEC = new IvParameterSpec(ZERO_ARRAY_8);

  public static byte[] desKcv(Key key) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(DES_EDE_ECB_NO_PADDING);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return Arrays.copyOf(cipher.doFinal(ZERO_ARRAY_8), 3);
  }

  public static byte[] macAlgorithm3(Key key, byte[] iv, byte[] input) {
    CipherParameters cipherParameters = new KeyParameter(key.getEncoded());
    if (iv != null) {
      cipherParameters = new ParametersWithIV(cipherParameters, iv);
    }
    BlockCipher cipher = new DESEngine();
    Mac mac = new ISO9797Alg3Mac(cipher, DEFAULT_MAC_SIZE_BITS);
    mac.init(cipherParameters);

    mac.update(input, 0, input.length);
    byte[] result = new byte[mac.getMacSize()];
    mac.doFinal(result, 0);
    return result;
  }

  public static byte[] toKey24(byte[] key) {
    if (key == null) {
      throw new NullPointerException("key");
    }
    if (key.length == 24) {
      return key;
    } else if (key.length == 16) {
      byte[] key24 = new byte[24];
      System.arraycopy(key, 0, key24, 0, 16);
      System.arraycopy(key, 0, key24, 16, 8);
      return key24;
    }
    throw new IllegalArgumentException("Wrong key length [" + key.length + "]");
  }

  private JcCryptoUtils() {
    // do nothing
  }
}
