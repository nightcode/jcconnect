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

package org.nightcode.javacard.util;

import org.nightcode.common.base.Hexs;
import org.nightcode.common.util.logging.LogManager;
import org.nightcode.common.util.logging.Logger;
import org.nightcode.javacard.channel.scp.ScpVersion;
import org.nightcode.tools.ber.BerFrame;
import org.nightcode.tools.ber.StreamBerPrinter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;

public final class JcUtils {

  private static final Logger LOGGER = LogManager.getLogger(JcUtils.class);

  private static final int OID_TAG   = 0x06;
  private static final int APP_TAG_0 = 0x60;
  private static final int APP_TAG_3 = 0x63;
  private static final int APP_TAG_4 = 0x64;
  private static final int APP_TAG_5 = 0x65;
  private static final int APP_TAG_6 = 0x66;
  private static final int APP_TAG_7 = 0x67;
  private static final int APP_TAG_8 = 0x68;

  private static final Hexs HEX = Hexs.hex();

  public static void logBerFrame(BerFrame berFrame, Logger logger) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      new StreamBerPrinter(out).print(berFrame);
      logger.debug(new String(out.toByteArray(), StandardCharsets.UTF_8));
    } catch (IOException ex) {
      LOGGER.trace("can't print BER frame", ex);
    }
  }

  public static boolean isGlobalPlatform(BerFrame cardRecognitionData) {
    byte[] content = cardRecognitionData.getTagAsByteArray(OID_TAG);
    try {
      String oid = ASN1ObjectIdentifier.fromByteArray(content).toString();
      return "1.2.840.114283.1".equals(oid);
    } catch (Exception ex) {
      throw illegalArgumentException("Card Recognition Data", content);
    }
  }

  public static String getGlobalPlatformVersion(BerFrame cardRecognitionData) {
    byte[] content = cardRecognitionData.getContent(APP_TAG_0);
    try {
      String oid = ASN1ObjectIdentifier.fromByteArray(content).toString();
      if (!oid.startsWith("1.2.840.114283.2")) {
        return "unknown";
      }
      return oid.substring("1.2.840.114283.2.".length());
    } catch (Exception ex) {
      throw illegalArgumentException("Card Management Type and Version", content);
    }
  }

  public static String getJavaCardVersion(BerFrame cardRecognitionData) {
    byte[] content = cardRecognitionData.getContent(APP_TAG_6);
    try {
      String oid = ASN1ObjectIdentifier.fromByteArray(content).toString();
      if (!oid.startsWith("1.3.6.1.4.1.42.2.110.1")) {
        return "unknown";
      }
      return oid.substring("1.3.6.1.4.1.42.2.110.1.".length());
    } catch (Exception ex) {
      throw illegalArgumentException("Card/Chip details", content);
    }
  }

  public static String getOidValue(byte[] content) {
    try {
      ASN1Primitive oid = ASN1ObjectIdentifier.fromByteArray(content);
      return oid.toString();
    } catch (Exception ex) {
      throw new IllegalArgumentException("can't get OID value from byte array" + toHexSafe(content));
    }
  }

  public static ScpVersion getScpVersion(BerFrame cardRecognitionData) {
    byte[] content = cardRecognitionData.getContent(APP_TAG_4);
    if (content == null) {
      return ScpVersion.UNKNOWN;
    }
    try {
      String oid = ASN1ObjectIdentifier.fromByteArray(content).toString();
      if (!oid.startsWith("1.2.840.114283.4")) {
        return ScpVersion.UNKNOWN;
      }
      String[] p = oid.substring("1.2.840.114283.4.".length()).split("\\.");
      return ScpVersion.of(Integer.parseInt(p[0]));
    } catch (Exception ex) {
      throw illegalArgumentException("Secure Channel Protocol", content);
    }
  }

  @Nullable public static Byte getScpImplementationOptions(BerFrame cardRecognitionData) {
    byte[] content = cardRecognitionData.getContent(APP_TAG_4);
    if (content == null) {
      return null;
    }
    try {
      String oid = ASN1ObjectIdentifier.fromByteArray(content).toString();
      if (!oid.startsWith("1.2.840.114283.4")) {
        return null;
      }
      String[] p = oid.substring("1.2.840.114283.4.".length()).split("\\.");
      return Byte.parseByte(p[1]);
    } catch (Exception ex) {
      throw illegalArgumentException("Secure Channel Protocol", content);
    }
  }

  public static byte[] joinArrays(byte[]... src) {
    int totalLength = 0;
    for (byte[] array : src) {
      totalLength += array.length;
    }
    byte[] dst = new byte[totalLength];
    int offset = 0;
    for (byte[] array : src) {
      System.arraycopy(array, 0, dst, offset, array.length);
      offset += array.length;
    }
    return dst;
  }

  public static void printCardRecognitionData(BerFrame cardRecognitionData, PrintStream out) {
    StringBuilder sb = new StringBuilder("CardRecognitionData:")
        .append("\n    GlobalPlatform card:     ").append(isGlobalPlatform(cardRecognitionData) ? "yes" : "no")
        .append("\n    GlobalPlatform version:  ").append(getGlobalPlatformVersion(cardRecognitionData))
        .append("\n    Secure Channel Protocol: ")
        .append(String.format("%s i=%02x"
            , getScpVersion(cardRecognitionData), getScpImplementationOptions(cardRecognitionData)))
        .append("\n    JavaCard version:        ").append(getJavaCardVersion(cardRecognitionData));

    BerFrame tag5 = cardRecognitionData.getTag(APP_TAG_5);
    BerFrame tag7 = cardRecognitionData.getTag(APP_TAG_7);
    BerFrame tag8 = cardRecognitionData.getTag(APP_TAG_8);
    sb.append("\n    Tag 3:                   ").append(JcUtils.getOidValue(cardRecognitionData.getContent(APP_TAG_3)));
    if (tag5 != null) {
      sb.append("\n    Tag 5:                   ").append(JcUtils.toHexSafe(tag5));
    }
    if (tag7 != null) {
      sb.append("\n    Tag 7:                   ").append(JcUtils.toHexSafe(tag7));
    }
    if (tag8 != null) {
      sb.append("\n    Tag 8:                   ").append(JcUtils.toHexSafe(tag8));
    }
    out.println(sb.toString());
  }

  public static String swToString(int sw) {
    return String.format("0x%04X", sw);
  }

  public static String toHexSafe(@Nullable byte[] src) {
    return (src != null) ? HEX.fromByteArray(src) : "NULL";
  }

  public static String toHexSafe(@Nullable BerFrame src) {
    return (src != null) ? HEX.fromByteArray(src.toByteArray()) : "NULL";
  }

  private static IllegalArgumentException illegalArgumentException(String message, byte[] content) {
    return new IllegalArgumentException(String.format("invalid %s tag value [%s]", message, toHexSafe(content)));
  }

  private JcUtils() {
    // do nothing
  }
}
