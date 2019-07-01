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

package org.nightcode.javacard.common;

import org.nightcode.common.base.Hexs;

import java.util.Arrays;

public final class Aid {

  public static Aid of(byte[] buffer) {
    return new Aid(buffer, 0, buffer.length);
  }

  public static Aid of(byte[] buffer, int offset, int length) {
    return new Aid(buffer, offset, length);
  }

  public static Aid parse(String aid) {
    return new Aid(Hexs.hex().toByteArray(aid));
  }

  private final byte[] aid;

  private Aid(byte[] buffer) {
    this(buffer, 0, buffer.length);
  }

  private Aid(byte[] buffer, int offset, int length) {
    if ((offset + length) > buffer.length) {
      throw new IllegalArgumentException();
    } else if (5 > length || length > 16) {
      throw new IllegalArgumentException("AID length MUST be between 5 and 16 bytes");
    }
    aid = Arrays.copyOfRange(buffer, offset, offset + length);
  }

  public byte[] array() {
    return Arrays.copyOf(aid, aid.length);
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Aid)) {
      return false;
    }
    Aid other = (Aid) obj;
    return Arrays.equals(aid, other.aid);
  }

  @Override public int hashCode() {
    return Arrays.hashCode(aid);
  }

  public int length() {
    return aid.length;
  }

  @Override public String toString() {
    return Hexs.hex().fromByteArray(aid);
  }
}
