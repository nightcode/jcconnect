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

import org.nightcode.javacard.JavaCardException;
import org.nightcode.javacard.channel.key.KeyProvider;
import org.nightcode.javacard.channel.scp.Scp02Session;
import org.nightcode.javacard.channel.scp.ScpVersion;
import org.nightcode.javacard.common.Aid;
import org.nightcode.javacard.common.CardProperties;
import org.nightcode.javacard.util.ApduPreconditions;
import org.nightcode.javacard.util.JcUtils;
import org.nightcode.tools.ber.BerFrame;

import java.io.IOException;
import java.util.logging.Logger;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import static org.nightcode.javacard.common.Apdu.CLA_ISO7816;
import static org.nightcode.javacard.common.Apdu.DEFAULT_ISD_AID;
import static org.nightcode.javacard.common.Apdu.INS_SELECT;
import static org.nightcode.javacard.common.Apdu.P1_SELECT_BY_NAME;
import static org.nightcode.javacard.common.Apdu.P2_SELECT_FIRST_OR_ONLY;
import static org.nightcode.javacard.common.Apdu.SW_INCONSISTENT_P3;
import static org.nightcode.javacard.common.Apdu.SW_NO_ERROR;
import static org.nightcode.javacard.common.Apdu.SW_SELECT_CARD_LOCKED;

public class CardChannelServiceImpl implements CardChannelService {

  private static final Logger LOGGER = Logger.getLogger(CardChannelServiceImpl.class.getName());

  private static final int DEF_MAX_RESPONSE_LENGTH = 256;

  private static final int TAG_6F_FCI_TEMPLATE = 0x6F;
  private static final int TAG_84_AID = 0x84;
  private static final int TAG_A5_PROPRIETARY_DATA = 0xA5;
  private static final int TAG_73_CARD_RECOGNITION_DATA = 0x73;
  private static final int TAG_MAX_LENGTH_OF_DATA_FIELD = 0x9F65;
  private static final int TAG_APP_LIFE_CYCLE_DATA = 0x9F6E;

  private final KeyProvider keyProvider;

  public CardChannelServiceImpl(KeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Override public CardChannelContext createCardChannelContext(CardProperties properties, ApduChannel channel)
      throws JavaCardException {
    CommandAPDU command = new CommandAPDU(CLA_ISO7816, INS_SELECT, P1_SELECT_BY_NAME, P2_SELECT_FIRST_OR_ONLY
        , DEF_MAX_RESPONSE_LENGTH);
    ResponseAPDU response;
    try {
      response = channel.transmit(command);
    } catch (IOException ex) {
      throw new JavaCardException("can't transmit APDU command", ex);
    }

    if (response.getSW() == SW_INCONSISTENT_P3) {
      return createCardChannelContext(properties, channel, DEFAULT_ISD_AID);
    }

    ApduPreconditions.checkSw("can't execute SELECT for DEFAULT", response.getSW(), SW_NO_ERROR, SW_SELECT_CARD_LOCKED);

    return createCardChannelContext(properties, channel, response.getData());
  }

  @Override public CardChannelContext createCardChannelContext(CardProperties properties, ApduChannel channel,
      Aid aid) throws JavaCardException {
    CommandAPDU command = new CommandAPDU(CLA_ISO7816, INS_SELECT, P1_SELECT_BY_NAME, P2_SELECT_FIRST_OR_ONLY
        , aid.array(), DEF_MAX_RESPONSE_LENGTH);
    ResponseAPDU response;
    try {
      response = channel.transmit(command);
    } catch (IOException ex) {
      throw new JavaCardException("can't transmit APDU command", ex);
    }

    ApduPreconditions.checkSw("can't execute SELECT for AID=" + aid, response.getSW()
        , SW_NO_ERROR, SW_SELECT_CARD_LOCKED);

    return createCardChannelContext(properties, channel, response.getData());
  }

  @Override public SecureChannelSession createSecureChannelSession(CardChannelContext context)
      throws JavaCardException {
    if (ScpVersion.SCP_02.equals(context.getScpVersion())) {
      return new Scp02Session(context);
    }
    throw new JavaCardException("unsupported SCP version %s", context.getScpVersion());
  }

  private CardChannelContext createCardChannelContext(CardProperties properties, ApduChannel channel,
      byte[] selectResponseData) {
    CardChannelContext.Builder builder = CardChannelContext.builder()
        .cardProperties(properties)
        .channel(channel)
        .keyProvider(keyProvider);
    parseSelectResponse(builder, selectResponseData);

    return builder.build();
  }

  private void parseSelectResponse(CardChannelContext.Builder builder, byte[] data) {
    BerFrame berFrame = BerFrame.parseFrom(data);
    JcUtils.logBerFrame(berFrame, LOGGER);

    BerFrame fciTemplate = berFrame.getTag(TAG_6F_FCI_TEMPLATE);
    if (fciTemplate == null) {
      LOGGER.info("response message doesn't contain FCI template");
      return;
    }

    byte[] aid = fciTemplate.getContent(TAG_84_AID);
    if (aid != null) {
      Aid sdAid = Aid.of(aid);
      builder.sdAid(sdAid);
    }

    BerFrame proprietaryData = fciTemplate.getTag(TAG_A5_PROPRIETARY_DATA);
    if (proprietaryData == null) {
      LOGGER.info("FCI template doesn't contain Proprietary Data");
      return;
    }

    int maxLength = 0;
    byte[] content = proprietaryData.getContent(TAG_MAX_LENGTH_OF_DATA_FIELD);
    if (content != null) {
      for (int i = 0; i < content.length; i++) {
        maxLength = (maxLength << (8 * i)) + (content[i] & 0xFF);
      }
    }
    builder.maxLength(maxLength);

    byte[] appLifeCycleData = proprietaryData.getContent(TAG_APP_LIFE_CYCLE_DATA);

    BerFrame cardRecognitionDataTag = proprietaryData.getTag(TAG_73_CARD_RECOGNITION_DATA);
    if (cardRecognitionDataTag == null) {
      LOGGER.info("Proprietary Data doesn't contain Card Recognition Data");
      return;
    }

    builder.cardRecognitionData(cardRecognitionDataTag);
  }
}
