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

import java.util.Arrays;

public final class KeyContext {

  public static final class Builder {
    private DerivationType derivationType;
    private CardProperties cardProperties;
    private byte[] sequenceCounter;

    private Builder() {
      // do nothing
    }

    public KeyContext build() {
      return new KeyContext(this);
    }

    public Builder cardProperties(CardProperties val) {
      cardProperties = val;
      return this;
    }

    public Builder derivationType(DerivationType val) {
      derivationType = val;
      return this;
    }

    public Builder sequenceCounter(byte[] val) {
      sequenceCounter = val;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final DerivationType derivationType;
  private final CardProperties cardProperties;
  private final byte[] sequenceCounter;

  private KeyContext(Builder builder) {
    derivationType = builder.derivationType;
    cardProperties = builder.cardProperties;
    sequenceCounter = Arrays.copyOf(builder.sequenceCounter, builder.sequenceCounter.length);
  }

  public DerivationType derivationType() {
    return derivationType;
  }

  public CardProperties cardProperties() {
    return cardProperties;
  }

  public byte[] sequenceCounter() {
    return sequenceCounter;
  }
}
