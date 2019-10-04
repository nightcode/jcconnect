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

import org.nightcode.javacard.common.CardProperties;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class Scp02KeySet extends KeySet {

  private final KeyProvider keyProvider;

  Scp02KeySet(KeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Override public SessionKeys deriveSessionKeys(CardProperties cardProperties, byte[] sequenceCounter) {
    KeyContext context = KeyContext.builder()
        .derivationType(DerivationType.SCP_02)
        .cardProperties(cardProperties)
        .sequenceCounter(sequenceCounter)
        .build();

    Map<KeyUsage, Key> keys = new HashMap<>();

    keys.put(KeyUsage.ENC, keyProvider.deriveSessionKey(context, KeyUsage.ENC));
    keys.put(KeyUsage.MAC, keyProvider.deriveSessionKey(context, KeyUsage.MAC));
    keys.put(KeyUsage.DEK, keyProvider.deriveSessionKey(context, KeyUsage.DEK));
    keys.put(KeyUsage.R_MAC, keyProvider.deriveSessionKey(context, KeyUsage.R_MAC));

    return new SessionKeysImpl(keys);
  }
}
