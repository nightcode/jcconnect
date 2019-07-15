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

import org.nightcode.common.base.Hexs;
import org.nightcode.javacard.AbstractJcTest;
import org.nightcode.javacard.JavaCardException;
import org.nightcode.javacard.channel.ApduChannel;
import org.nightcode.javacard.channel.CardChannelContext;
import org.nightcode.javacard.channel.key.KeySet;
import org.nightcode.javacard.channel.key.SessionKeys;
import org.nightcode.javacard.common.SecurityLevel;
import org.nightcode.javacard.util.ByteArrayGenerator;

import java.io.IOException;
import java.util.EnumSet;

import javax.smartcardio.ResponseAPDU;

import org.junit.Assert;
import org.junit.Test;

public class Scp02SessionTest extends AbstractJcTest {

  @Test public void testInitializeUpdate() throws IOException, JavaCardException {
    ApduChannel channel = request -> {
      Assert.assertArrayEquals(Hexs.hex().toByteArray("8050000008E4C522735415CC5C00"), request.getBytes());
      return new ResponseAPDU(Hexs.hex().toByteArray("0000000000000000000001020002D9857D532F04EFA4524D0C2472659000"));
    };

    CardChannelContext context = createContext(channel);

    ByteArrayGenerator byteArrayGenerator = size -> Hexs.hex().toByteArray("E4C522735415CC5C");

    Scp02Session session = new Scp02Session(context, byteArrayGenerator);

    Scp02Context scp02Context = session.initializeUpdate((byte) 0);

    Assert.assertArrayEquals(Hexs.hex().toByteArray("0002"), scp02Context.getSequenceCounter());
    Assert.assertArrayEquals(Hexs.hex().toByteArray("D9857D532F04"), scp02Context.getCardChallenge());
    Assert.assertArrayEquals(Hexs.hex().toByteArray("E4C522735415CC5C"), scp02Context.getHostChallenge());
  }

  @Test public void testExternalAuthenticateCmac() throws JavaCardException, IOException {
    ApduChannel channel = request -> {
      Assert.assertArrayEquals(Hexs.hex().toByteArray("84820100100958CFB92281CADE0772395773758FE4"), request.getBytes());
      return new ResponseAPDU(Hexs.hex().toByteArray("9000"));
    };

    CardChannelContext context = createContext(channel);

    byte[] sequenceCounter =  Hexs.hex().toByteArray("0002");
    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);
    
    Scp02Context scp02Context = new Scp02Context(
        Hexs.hex().toByteArray("E4C522735415CC5C")
        , Hexs.hex().toByteArray("D9857D532F04")
        , sequenceCounter
    );

    Scp02Session session = new Scp02Session(context);
    session.externalAuthenticate(scp02Context, EnumSet.of(SecurityLevel.C_MAC));
  }

  @Test public void testExternalAuthenticateCmacEnc() throws JavaCardException, IOException {
    ApduChannel channel = request -> {
      Assert.assertArrayEquals(Hexs.hex().toByteArray("8482030010BFD9D136839B3BB9AD88229B0C0773C3"), request.getBytes());
      return new ResponseAPDU(Hexs.hex().toByteArray("9000"));
    };

    CardChannelContext context = createContext(channel);

    byte[] sequenceCounter =  Hexs.hex().toByteArray("0005");
    KeySet keySet = KeySet.of(ScpVersion.SCP_02, context.keyProvider());
    SessionKeys sessionKeys = keySet.deriveSessionKeys(context.getCardProperties(), sequenceCounter);
    context.setSessionKeys(sessionKeys);

    Scp02Context scp02Context = new Scp02Context(
        Hexs.hex().toByteArray("4534CEAB691E93FC")
        , Hexs.hex().toByteArray("BD1A6BE9D3D5")
        , sequenceCounter
    );

    Scp02Session session = new Scp02Session(context);
    session.externalAuthenticate(scp02Context, EnumSet.of(SecurityLevel.C_MAC, SecurityLevel.C_DECRYPTION));
  }

  @Test public void testOpenSecureChannel() {
    ApduChannel channel = request -> new ResponseAPDU(Hexs.hex().toByteArray("9000"));

    CardChannelContext context = createContext(channel);
    Scp02Session session = new Scp02Session(context);
    try {
      session.openSecureChannel(EnumSet.of(SecurityLevel.C_DECRYPTION));
      Assert.fail("must throw IllegalArgumentException");
    } catch (Exception ex) {
      Assert.assertEquals("C_DECRYPTION must be combined with C_MAC", ex.getMessage());
    }

    try {
      session.openSecureChannel(EnumSet.of(SecurityLevel.R_DECRYPTION));
      Assert.fail("must throw IllegalArgumentException");
    } catch (Exception ex) {
      Assert.assertEquals("R_DECRYPTION must be combined with R_MAC", ex.getMessage());
    }
  }
}
