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

package org.nightcode.javacard;

import org.nightcode.common.base.Hexs;
import org.nightcode.common.util.props.Properties;
import org.nightcode.common.util.props.PropertiesMapStorage;
import org.nightcode.javacard.channel.ApduChannel;
import org.nightcode.javacard.channel.CardChannelContext;
import org.nightcode.javacard.channel.key.KeyContext;
import org.nightcode.javacard.channel.key.KeyProvider;
import org.nightcode.javacard.channel.key.KeyUsage;
import org.nightcode.javacard.channel.scp.ScpVersion;
import org.nightcode.javacard.common.CardProperties;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractJcTest {

  protected static final Hexs HEX = Hexs.hex();
 
  protected static CardChannelContext createContext(ApduChannel channel) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("key_version_number", (byte) 0xFF);
    properties.put("enc_key_identifier", (byte) 0x01);
    properties.put("mac_key_identifier", (byte) 0x02);
    properties.put("dek_key_identifier", (byte) 0x03);
    CardProperties cardProperties = new CardProperties(Properties.of(new PropertiesMapStorage(properties))) ;

    CardChannelContext context = CardChannelContext.builder()
        .channel(channel)
        .cardProperties(cardProperties)
        .keyProvider(KeyProvider.DEFAULT)
        .maxLength(0xFF)
        .build();
    context.setScpVersion(ScpVersion.SCP_02);

    return context;
  }

  public static byte getKeyIdentifier(KeyContext context, KeyUsage usage) {
    switch (usage) {
      case ENC:
        return context.cardProperties().getEncKeyIdentifier();
      case MAC:
        return context.cardProperties().getMacKeyIdentifier();
      case DEK:
        return context.cardProperties().getDekKeyIdentifier();
      case R_MAC:
        return context.cardProperties().getMacKeyIdentifier();
      default:
        throw new IllegalArgumentException("unsupported key usage '" + usage + "'");
    }
  }
}
