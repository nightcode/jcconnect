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

import org.nightcode.javacard.util.JcUtils;
import org.nightcode.tools.ber.BerFrame;

import javax.annotation.Nullable;

public final class Scp02ParameterI {

  private static final Scp02ParameterI DEFAULT = Scp02ParameterI.parse((byte) 0x15);

  public static Scp02ParameterI of(@Nullable BerFrame cardRecognitionData) {
    if (cardRecognitionData == null) {
      return DEFAULT;
    }

    Byte i = JcUtils.getScpImplementationOptions(cardRecognitionData);
    if (i == null) {
      return DEFAULT;
    }

    return Scp02ParameterI.parse(i);
  }

  public static Scp02ParameterI parse(byte i) {
    return new Scp02ParameterI(i);
  }

  private final byte i;

  private Scp02ParameterI(byte i) {
    this.i = i;
  }

  public boolean threeSecureChannelKeys() {
    return (i & 0x01) == 0x01;
  }

  public boolean oneSecureChannelBaseKey() {
    return (i & 0x01) == 0x00;
  }

  boolean cMacOnUnmodifiedApdu() {
    return (i & 0x02) == 0x02;
  }

  boolean cMacOnModifiedApdu() {
    return (i & 0x02) == 0x00;
  }

  boolean initiationModeExplicit() {
    return (i & 0x04) == 0x04;
  }

  boolean initiationModeImplicit() {
    return (i & 0x04) == 0x00;
  }

  boolean icvSetToMacOverAid() {
    return (i & 0x08) == 0x08;
  }

  boolean icvSetToZero() {
    return (i & 0x08) == 0x00;
  }

  boolean icvEncryptionForCMacSession() {
    return (i & 0x10) == 0x10;
  }

  boolean noIcvEncryption() {
    return (i & 0x10) == 0x00;
  }

  boolean rMacSupport() {
    return (i & 0x20) == 0x20;
  }

  boolean noRMacSupport() {
    return (i & 0x20) == 0x00;
  }

  boolean wellKnownPseudoRandomAlgorithm() {
    return (i & 0x40) == 0x40;
  }

  boolean unspecifiedCardChallengeGenerationMethod() {
    return (i & 0x40) == 0x00;
  }
}
