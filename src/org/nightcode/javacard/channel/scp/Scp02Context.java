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

package org.nightcode.javacard.channel.scp;

class Scp02Context {

  private final byte[] hostChallenge;
  private final byte[] cardChallenge;
  private final byte[] sequenceCounter;

  Scp02Context(byte[] hostChallenge, byte[] cardChallenge, byte[] sequenceCounter) {
    this.hostChallenge = hostChallenge;
    this.cardChallenge = cardChallenge;
    this.sequenceCounter = sequenceCounter;
  }

  byte[] getCardChallenge() {
    return cardChallenge;
  }

  byte[] getHostChallenge() {
    return hostChallenge;
  }

  byte[] getSequenceCounter() {
    return sequenceCounter;
  }
}
