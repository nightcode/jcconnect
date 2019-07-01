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

public enum SecurityLevel {

  NO_SECURITY_LEVEL(0x00),
  C_MAC(0x01),
  C_DECRYPTION(0x02),
  R_MAC(0x10),
  R_DECRYPTION(0x20)
  ;

  private final int bitMask;

  SecurityLevel(int bitMask) {
    this.bitMask = bitMask;
  }

  public int bitMask() {
    return bitMask;
  }
}
