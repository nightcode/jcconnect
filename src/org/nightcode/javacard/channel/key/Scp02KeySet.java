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

  private static final DerivationType DERIVATION_TYPE = DerivationType.SCP_02;

  private final boolean threeKeys;
  private final KeyProvider keyProvider;

  Scp02KeySet(boolean threeKeys, CardProperties cardProperties, KeyProvider keyProvider) {
    super(cardProperties);
    this.threeKeys = threeKeys;
    this.keyProvider = keyProvider;
  }

  @Override public SessionKeys deriveSessionKeys(byte[] sequenceCounter) {
    if (threeKeys) {
      return threeKeysMode(sequenceCounter);
    } else {
      return singleKeyMode(sequenceCounter);
    }
  }

  private SessionKeys singleKeyMode(byte[] sequenceCounter) {
    byte keyIdentifier = cardProperties.getBaseKeyIdentifier();
    Key baseKey = keyProvider.getKey(keyIdentifier, cardProperties.getKeyVersionNumber());

    Map<KeyUsage, Key> keys = new HashMap<>();
    keys.put(KeyUsage.ENC, keyProvider.deriveSessionKey(baseKey, sequenceCounter, KeyUsage.ENC, DERIVATION_TYPE));
    keys.put(KeyUsage.MAC, keyProvider.deriveSessionKey(baseKey, sequenceCounter, KeyUsage.MAC, DERIVATION_TYPE));
    keys.put(KeyUsage.DEK, keyProvider.deriveSessionKey(baseKey, sequenceCounter, KeyUsage.DEK, DERIVATION_TYPE));
    keys.put(KeyUsage.R_MAC, keyProvider.deriveSessionKey(baseKey, sequenceCounter, KeyUsage.R_MAC, DERIVATION_TYPE));

    return new SessionKeysImpl(keys);
  }

  private SessionKeys threeKeysMode(byte[] sequenceCounter) {
    Map<KeyUsage, Key> keys = new HashMap<>();
    byte keyVersion = cardProperties.getKeyVersionNumber();

    Key key = keyProvider.getKey(cardProperties.getEncKeyIdentifier(), keyVersion);
    keys.put(KeyUsage.ENC, keyProvider.deriveSessionKey(key, sequenceCounter, KeyUsage.ENC, DERIVATION_TYPE));

    key = keyProvider.getKey(cardProperties.getMacKeyIdentifier(), keyVersion);
    keys.put(KeyUsage.MAC, keyProvider.deriveSessionKey(key, sequenceCounter, KeyUsage.MAC, DERIVATION_TYPE));

    key = keyProvider.getKey(cardProperties.getDekKeyIdentifier(), keyVersion);
    keys.put(KeyUsage.DEK, keyProvider.deriveSessionKey(key, sequenceCounter, KeyUsage.DEK, DERIVATION_TYPE));

    key = keyProvider.getKey(cardProperties.getDekKeyIdentifier(), keyVersion);
    keys.put(KeyUsage.R_MAC, keyProvider.deriveSessionKey(key, sequenceCounter, KeyUsage.R_MAC, DERIVATION_TYPE));

    return new SessionKeysImpl(keys);
  }
}
