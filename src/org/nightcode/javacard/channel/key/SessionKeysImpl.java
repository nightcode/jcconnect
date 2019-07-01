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

import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

class SessionKeysImpl implements SessionKeys {

  private final Map<KeyUsage, Key> keys;

  SessionKeysImpl(Map<KeyUsage, Key> keys) {
    this.keys = new HashMap<>(keys);
  }

  @Override public Key getDes(KeyUsage usage) {
    Key desEde = getDesEde(usage);
    return new SecretKeySpec(Arrays.copyOf(desEde.getEncoded(), 8), "DES");
  }

  @Override public Key getDesEde(KeyUsage usage) {
    return keys.get(usage);
  }
}
