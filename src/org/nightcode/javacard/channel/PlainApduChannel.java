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

import org.nightcode.common.base.Hexs;
import org.nightcode.common.util.logging.LogManager;
import org.nightcode.common.util.logging.Logger;

import java.io.IOException;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class PlainApduChannel implements ApduChannel {

  private static final Logger LOGGER = LogManager.getLogger(PlainApduChannel.class);

  private static final Hexs HEX = Hexs.hex();

  private final CardChannel channel;

  public PlainApduChannel(CardChannel channel) {
    this.channel = channel;
  }

  @Override public ResponseAPDU transmit(CommandAPDU request) throws IOException {
    LOGGER.debug("  >>>> %s %s",  HEX.fromByteArray(request.getBytes(), 0, 4)
        , HEX.fromByteArray(request.getBytes(), 4, request.getBytes().length - 4));
    ResponseAPDU response;
    try {
      response = channel.transmit(request);
    } catch (CardException ex) {
      throw new IOException(ex);
    }
    LOGGER.debug("  <<<< %s %04X", HEX.fromByteArray(response.getData()), response.getSW());
    return response;
  }
}
