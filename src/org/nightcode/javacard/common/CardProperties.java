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

import javax.annotation.Nullable;

public class CardProperties {

  private final Properties properties;

  public CardProperties(Properties properties) {
    this.properties = properties;
  }

  public @Nullable Aid getAid() {
    String aid = properties.getStringValue("aid");
    if (aid == null) {
      return null;
    }
    return Aid.parse(aid);
  }

  public byte getKeyVersionNumber() {
    return properties.getByteValue("key_version_number");
  }

  public ScpVersion getScpVersion() {
    return ScpVersion.of(properties.getIntValue("scp_version", 2));
  }
}
