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

import org.nightcode.common.util.props.Properties;
import org.nightcode.javacard.channel.scp.ScpVersion;

public class CardProperties {

  private final Properties properties;

  public CardProperties(Properties properties) {
    this.properties = properties;
  }

  public Aid getAid() {
    return Aid.parse(properties.getStringValue("aid"));
  }

  public byte getBaseKeyIdentifier() {
    return properties.getByteValue("base_key_identifier");
  }

  public byte getDekKeyIdentifier() {
    return properties.getByteValue("dek_key_identifier");
  }

  public byte getEncKeyIdentifier() {
    return properties.getByteValue("enc_key_identifier");
  }

  public byte getKeyVersionNumber() {
    return properties.getByteValue("key_version_number");
  }

  public byte getMacKeyIdentifier() {
    return properties.getByteValue("mac_key_identifier");
  }

  public ScpVersion getScpVersion() {
    return ScpVersion.of(properties.getIntValue("scp_version", 2));
  }
}
