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

public interface Apdu {

  byte CLA_ISO7816 =          (byte) 0x00;
  byte CLA_COMMAND_CHAINING = (byte) 0x10;
  byte CLA_GP =               (byte) 0x80;

  byte INS_GET_DATA = (byte) 0xCA;
  byte INS_SELECT =   (byte) 0xA4;
  byte INS_PUT_DATA = (byte) 0xDA;

  byte INS_DELETE =                (byte) 0xE4;
  byte INS_EXTERNAL_AUTHENTICATE = (byte) 0x82;
  byte INS_GET_STATUS =            (byte) 0xF2;
  byte INS_INITIALIZE_UPDATE =     (byte) 0x50;
  byte INS_INSTALL =               (byte) 0xE6;
  byte INS_LOAD =                  (byte) 0xE8;
  byte INS_PUT_KEY =               (byte) 0xD8;
  byte INS_SET_STATUS =            (byte) 0xF0;
  byte INS_STORE_DATA =            (byte) 0xE2;

  byte P1_INSTALL_AND_MAKE_SELECTABLE = (byte) 0x0C;
  byte P1_INSTALL_FOR_INSTALL =         (byte) 0x04;
  byte P1_INSTALL_FOR_LOAD =            (byte) 0x02;
  byte P1_LAST_BLOCK =                  (byte) 0x80;
  byte P1_MORE_BLOCKS =                 (byte) 0x00;
  byte P1_SELECT_BY_NAME =              (byte) 0x04;

  byte P2_EXTERNAL_AUTHENTICATE    = (byte) 0x00;
  byte P2_INITIALIZE_UPDATE        = (byte) 0x00;
  byte P2_KEY_INFORMATION_TEMPLATE = (byte) 0xE0;
  byte P2_SELECT_FIRST_OR_ONLY     = (byte) 0x00;
  byte P2_SELECT_NEXT_OCCURRENCE   = (byte) 0x02;
  byte P2_GET_DATA_CARD_DATA       = (byte) 0x66;

  byte GP_SECURED_MASK = (byte) 0x04;

  int SW_NO_ERROR                                 = 0x9000;
  int SW_SELECT_CARD_LOCKED                       = 0x6283;
  int SW_AUTHENTICATION_OF_HOST_CRYPTOGRAM_FAILED = 0x6300;
  int SW_SECURITY_STATUS_NOT_SATISFIED            = 0x6982;
  int SW_FILE_INVALID                             = 0x6983;
  int SW_FILE_NOT_FOUND                           = 0x6A82;
  int SW_RECORD_NOT_FOUND                         = 0x6A83;
  int SW_INCONSISTENT_P3                          = 0x6A87;
  int SW_REFERENCED_DATA_NOT_FOUND                = 0x6A88;
  int SW_INCORRECT_CLA_PARAMETER                  = 0x6E00;

  Aid DEFAULT_ISD_AID = Aid.parse("A000000151000000");
}
