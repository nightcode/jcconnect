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
import org.nightcode.javacard.channel.CardChannelContext;
import org.nightcode.javacard.channel.scp.Scp02ParameterI;
import org.nightcode.javacard.channel.scp.ScpVersion;
import org.nightcode.javacard.common.CardProperties;

public abstract class KeySet {

  public static KeySet of(CardChannelContext context) throws JavaCardException {
    if (ScpVersion.SCP_02.equals(context.getScpVersion())) {
      Scp02ParameterI i = Scp02ParameterI.of(context.getCardRecognitionData());
      return new Scp02KeySet(i.threeSecureChannelKeys(),  context.getCardProperties(), context.keyProvider());
    }
    throw new JavaCardException("can't create KeySet, unsupported SCP %s", context.getScpVersion());
  }

  final CardProperties cardProperties;

  KeySet(CardProperties cardProperties) {
    this.cardProperties = cardProperties;
  }

  public abstract SessionKeys deriveSessionKeys(byte[] sequenceCounter);
}
