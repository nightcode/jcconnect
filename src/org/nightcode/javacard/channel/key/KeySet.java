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

import org.nightcode.javacard.JavaCardException;
import org.nightcode.javacard.channel.scp.ScpVersion;
import org.nightcode.javacard.common.CardProperties;

public abstract class KeySet {

  public static KeySet of(ScpVersion scpVersion, KeyProvider keyProvider) throws JavaCardException {
    if (ScpVersion.SCP_02.equals(scpVersion)) {
      return new Scp02KeySet(keyProvider);
    }
    throw new JavaCardException("can't create KeySet, unsupported SCP %s", scpVersion);
  }

  public abstract SessionKeys deriveSessionKeys(CardProperties cardProperties, byte[] sequenceCounter);
}
