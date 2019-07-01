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

package org.nightcode.javacard.channel;

import org.nightcode.javacard.channel.key.KeyProvider;
import org.nightcode.javacard.channel.key.SessionKeys;
import org.nightcode.javacard.channel.scp.ScpVersion;
import org.nightcode.javacard.common.Aid;
import org.nightcode.javacard.common.CardProperties;
import org.nightcode.javacard.util.JcUtils;
import org.nightcode.tools.ber.BerFrame;

import javax.annotation.Nullable;

public final class CardChannelContext {

  public static final class Builder {
    private ApduChannel channel;
    private KeyProvider keyProvider;

    private CardProperties cardProperties;
    private BerFrame cardRecognitionData;
    private int maxLength;
    private Aid sdAid;

    private Builder() {
      // do nothing
    }

    public CardChannelContext build() {
      return new CardChannelContext(this);
    }

    public Builder cardProperties(CardProperties val) {
      cardProperties = val;
      return this;
    }

    public Builder cardRecognitionData(BerFrame val) {
      cardRecognitionData = val;
      return this;
    }

    public Builder channel(ApduChannel val) {
      channel = val;
      return this;
    }

    public Builder keyProvider(KeyProvider val) {
      keyProvider = val;
      return this;
    }

    public Builder maxLength(int val) {
      maxLength = val;
      return this;
    }

    public Builder sdAid(Aid val) {
      sdAid = val;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final CardProperties cardProperties;
  private final BerFrame cardRecognitionData;
  private final int maxLength;
  private final Aid sdAid;

  private final ApduChannel channel;
  private final KeyProvider keyProvider;

  private volatile ScpVersion scpVersion;
  private volatile SessionKeys sessionKeys;

  private CardChannelContext(Builder builder) {
    channel = builder.channel;
    keyProvider = builder.keyProvider;

    cardProperties = builder.cardProperties;
    cardRecognitionData = builder.cardRecognitionData;
    maxLength = builder.maxLength;
    sdAid = builder.sdAid;

    if (cardRecognitionData != null) {
      scpVersion = JcUtils.getScpVersion(cardRecognitionData);
    } else {
      // fallback
      scpVersion = cardProperties.getScpVersion();
    }
  }

  public ApduChannel channel() {
    return channel;
  }

  public CardProperties getCardProperties() {
    return cardProperties;
  }

  @Nullable public BerFrame getCardRecognitionData() {
    return cardRecognitionData;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public ScpVersion getScpVersion() {
    return scpVersion;
  }

  public SessionKeys getSessionKeys() {
    return sessionKeys;
  }

  public Aid getSdAid() {
    return sdAid;
  }

  public KeyProvider keyProvider() {
    return keyProvider;
  }

  public void setScpVersion(ScpVersion scpVersion) {
    this.scpVersion = scpVersion;
  }

  public void setSessionKeys(SessionKeys sessionKeys) {
    this.sessionKeys = sessionKeys;
  }
}
