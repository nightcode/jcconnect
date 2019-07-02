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

import org.nightcode.javacard.AbstractJcTest;
import org.nightcode.javacard.channel.ApduChannel;
import org.nightcode.javacard.channel.CardChannelContext;
import org.nightcode.javacard.channel.key.KeySet;
import org.nightcode.javacard.channel.key.SessionKeys;
import org.nightcode.javacard.common.SecurityLevel;

import java.util.EnumSet;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.junit.Assert;
import org.junit.Test;

public class Scp02ApduChannelTest extends AbstractJcTest {

  @Test public void testTransmitCmac() throws Exception {
    ApduChannel channel = request -> {
      Assert.assertEquals("84F280020A4F00517FECC3E4B00186", HEX.fromByteArray(request.getBytes()));
      return new ResponseAPDU(HEX.toByteArray("E3114F08A0000000030000009F700101C5019E9000"));
    };

    CardChannelContext context = createContext(channel);

    byte[] sequenceCounter =  HEX.toByteArray("0004");
    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);

    ApduChannel scpChannel
        = new Scp02ApduChannel(context, EnumSet.of(SecurityLevel.C_MAC), HEX.toByteArray("FB3A120021F7363C"));
    scpChannel.transmit(new CommandAPDU(HEX.toByteArray("80F28002024F00")));
  }

  @Test public void testTransmitCmacEnc() throws Exception {
    ApduChannel channel = request -> {
      Assert.assertEquals("84F280021082E63A0FE0BFAD7CE78AA5C6E038A340", HEX.fromByteArray(request.getBytes()));
      return new ResponseAPDU(HEX.toByteArray("E3114F08A0000000030000009F700101C5019E9000"));
    };

    CardChannelContext context = createContext(channel);

    byte[] sequenceCounter =  HEX.toByteArray("0005");
    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);

    ApduChannel scpChannel = new Scp02ApduChannel(context, EnumSet.of(SecurityLevel.C_MAC, SecurityLevel.C_DECRYPTION)
        , HEX.toByteArray("AD88229B0C0773C3"));
    scpChannel.transmit(new CommandAPDU(HEX.toByteArray("80F28002024F00")));
  }

  @Test public void testTransmitCmacZeroLc() throws Exception {
    ApduChannel channel = request -> {
      Assert.assertEquals("84DD0000088F37D75E12BA3CFB", HEX.fromByteArray(request.getBytes()));
      return new ResponseAPDU(HEX.toByteArray("9000"));
    };

    CardChannelContext context = createContext(channel);

    byte[] sequenceCounter =  HEX.toByteArray("002B");
    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);

    ApduChannel scpChannel
        = new Scp02ApduChannel(context, EnumSet.of(SecurityLevel.C_MAC), HEX.toByteArray("1FFFFA8673AC4B36"));
    scpChannel.transmit(new CommandAPDU(HEX.toByteArray("80DD0000")));
  }

  @Test public void testTransmitCmacRmac() throws Exception {
    ApduChannel channel = request -> {
      Assert.assertEquals("84F280020A4F00D3A881BA3923ADA0", HEX.fromByteArray(request.getBytes()));
      return new ResponseAPDU(HEX.toByteArray("E3134F08A0000001510000009F700107C5039EFE804490ADB965570A769000"));
    };

    CardChannelContext context = createContext(channel);

    byte[] sequenceCounter =  HEX.toByteArray("0064");
    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);

    Scp02ApduChannel scpChannel = new Scp02ApduChannel(context, EnumSet.of(SecurityLevel.C_MAC, SecurityLevel.R_MAC)
        , HEX.toByteArray("D548435F7981B2B4"));
    scpChannel.setRicv(HEX.toByteArray("D548435F7981B2B4"));
    
    ResponseAPDU response = scpChannel.transmit(new CommandAPDU(HEX.toByteArray("84F28002024F00")));
    Assert.assertEquals("E3134F08A0000001510000009F700107C5039EFE80", HEX.fromByteArray(response.getData()));
  }

  @Test public void testTransmitCmacEncRmac() throws Exception {
    ApduChannel channel = request -> {
      Assert.assertEquals("84F2400018FD4290F0AD4100E0A568E813960DF479AFA8F841D435A3E6", HEX.fromByteArray(request.getBytes()));
      return new ResponseAPDU(HEX.toByteArray("0799BA9C8ECCDE3B8E9000"));
    };

    CardChannelContext context = createContext(channel);

    byte[] sequenceCounter =  HEX.toByteArray("006D");
    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);

    Scp02ApduChannel scpChannel = new Scp02ApduChannel(context
        , EnumSet.of(SecurityLevel.C_MAC, SecurityLevel.C_DECRYPTION, SecurityLevel.R_MAC)
        , HEX.toByteArray("94373CCB68A4C173"));
    scpChannel.setRicv(HEX.toByteArray("94373CCB68A4C173"));

    ResponseAPDU response = scpChannel.transmit(new CommandAPDU(HEX.toByteArray("80F240000B4F09F00000008800010101")));
    Assert.assertEquals("07", HEX.fromByteArray(response.getData()));
  }
}
