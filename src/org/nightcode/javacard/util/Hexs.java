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

import java.util.Objects;

/**
 * An object which convert hexadecimal string to byte array and vice versa.
 */
public final class Hexs {

  private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  /**
   * Returns a Hexs that uses upper case hex digits
   * and empty string as byte separator.
   *
   * @return a Hexs that uses upper case hex digits
   *         and empty string as byte separator
   */
  public static Hexs hex() {
    return new Hexs();
  }

  private Hexs() {
    // do nothing
  }

  /**
   * Returns a hexadecimal string representation of each bytes of {@code bytes}.
   *
   * @param bytes a bytes to convert
   * @return a hexadecimal string representation of each bytes of {@code bytes}
   */
  public String fromByteArray(byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    return fromByteArray(bytes, 0, bytes.length);
  }

  /**
   * Returns a hexadecimal string representation of {@code length} bytes of {@code bytes}
   * starting at offset {@code offset}.
   *
   * @param bytes a bytes to convert
   * @param offset start offset in the bytes
   * @param length maximum number of bytes to use
   * @return a hexadecimal string representation of each bytes of {@code bytes}
   */
  public String fromByteArray(byte[] bytes, int offset, int length) {
    Objects.requireNonNull(bytes, "bytes");
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be equal or greater than zero");
    }
    if (length < 0) {
      throw new IllegalArgumentException("length must be greater than zero");
    }
    if (offset + length > bytes.length) {
      throw new IllegalArgumentException(String.format("(offset + length) must be less than %s", bytes.length));
    }
    return fromByteArrayInternal(bytes, offset, length);
  }

  /**
   * Returns a byte array representation of hexadecimal string {@code hexString}.
   *
   * @param hexString  hexadecimal string to convert
   * @return a byte array representation of hexadecimal string {@code hexString}
   */
  public byte[] toByteArray(String hexString) {
    Objects.requireNonNull(hexString, "hexadecimal string");
    if ((hexString.length() & 0x1) != 0) {
      throw new IllegalArgumentException(String
          .format("hexadecimal string <%s> must have an even number of characters.", hexString));
    }
    int length = hexString.length();
    byte[] result = new byte[length >> 1];
    for (int i = 0; i < length; i += 2) {
      int hn = Character.digit(hexString.charAt(i), 16);
      int ln = Character.digit(hexString.charAt(i + 1), 16);
      result[i >> 1] = (byte) ((hn << 4) | ln);
    }
    return result;
  }

  private String fromByteArrayInternal(byte[] bytes, int offset, int length) {
    int capacity = length << 1;
    StringBuilder builder = new StringBuilder(capacity);
    int size = offset + length;
    for (int i = offset; i < size; i++) {
      builder.append(HEX_DIGITS[(bytes[i] & 0xF0) >> 4]);
      builder.append(HEX_DIGITS[bytes[i] & 0x0F]);
    }
    return builder.toString();
  }
}
