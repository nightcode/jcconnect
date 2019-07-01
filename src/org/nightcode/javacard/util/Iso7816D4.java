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

import java.util.Arrays;

import javax.crypto.BadPaddingException;

public final class Iso7816D4 {

  public static int paddedLength(int origin) {
    return ((origin + 1) + 7) & ~7;
  }

  public static byte[] pad(byte[] src) {
    return pad(src, 0, src.length);
  }

  public static byte[] pad(byte[] src, int offset, int length) {
    int total = paddedLength(length);
    byte[] dst = new byte[total];
    System.arraycopy(src, offset, dst, 0, Math.min(length, src.length));
    dst[length] = (byte) 0x80;
    return dst;
  }

  public static byte[] unPad(byte[] src) throws BadPaddingException {
    int i = src.length - 1;
    while (i > 0 && src[i] == 0x00 && (src.length - i) < 8) {
      i--;
    }
    if (src[i] != (byte) 0x80) {
      throw new BadPaddingException("invalid padding");
    }
    return Arrays.copyOf(src, i);
  }

  private Iso7816D4() {
    // do nothing
  }
}
