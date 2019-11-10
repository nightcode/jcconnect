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

import org.nightcode.javacard.channel.scp.ScpVersion;

import javax.annotation.Nullable;

public final class CardProperties {

  public static final class Builder {
    private Aid aid;
    private byte keyVersionNumber;
    private ScpVersion scpVersion = ScpVersion.SCP_02;

    private Builder() {
      // do nothing
    }

    public Builder aid(Aid val) {
      aid = val;
      return this;
    }

    public CardProperties build() {
      return new CardProperties(this);
    }

    public Builder keyVersionNumber(byte val) {
      keyVersionNumber = val;
      return this;
    }

    public Builder scpVersion(ScpVersion val) {
      scpVersion = val;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Aid aid;
  private final byte keyVersionNumber;
  private final ScpVersion scpVersion;

  private CardProperties(Builder builder) {
    aid = builder.aid;
    keyVersionNumber = builder.keyVersionNumber;
    scpVersion = builder.scpVersion;
  }

  public @Nullable Aid aid() {
    return aid;
  }

  public byte keyVersionNumber() {
    return keyVersionNumber;
  }

  public ScpVersion scpVersion() {
    return scpVersion;
  }
}
