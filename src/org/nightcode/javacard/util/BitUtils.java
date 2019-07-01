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

public final class BitUtils {

  private static final String ERROR_MESSAGE = "index value should be grate or equals 0 and less then 8: ";

  public static boolean checkBit(int index, byte value) {
    if (0 > index || index >= 8) {
      throw new IllegalArgumentException(ERROR_MESSAGE + index);
    }
    long mask = 1L << index;
    return (value & mask) != 0;
  }

  public static byte resetBit(int index, byte value) {
    if (0 > index || index >= 8) {
      throw new IllegalArgumentException(ERROR_MESSAGE + index);
    }
    byte mask = (byte) (1 << index);
    return (byte) (value ^ mask);
  }

  public static long setBit(int index, byte value) {
    if (0 > index || index >= 8) {
      throw new IllegalArgumentException(ERROR_MESSAGE + index);
    }
    long mask = 1L << index;
    return value | mask;
  }

  private BitUtils() {
    // do nothing
  }
}
