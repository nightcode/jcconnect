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

public enum KeyUsage {

  ENC("enc", new byte[] {(byte) 0x01, (byte) 0x82}),
  MAC("mac", new byte[] {(byte) 0x01, (byte) 0x01}),
  DEK("dek", new byte[] {(byte) 0x01, (byte) 0x81}),
  R_MAC("rmac", new byte[] {(byte) 0x01, (byte) 0x02});

  private final String alias;
  private final byte[] keyConstant;

  KeyUsage(String alias, byte[] keyConstant) {
    this.alias = alias;
    this.keyConstant = keyConstant;
  }

  public String alias() {
    return alias;
  }

  public byte[] keyConstant() {
    return keyConstant;
  }
}
