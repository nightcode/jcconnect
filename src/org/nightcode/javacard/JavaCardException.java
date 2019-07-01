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

public class JavaCardException extends Exception {

  public JavaCardException(String message) {
    super(message, null, true, false);
  }

  public JavaCardException(String format, Object... args) {
    super(String.format(format, args), null, true, false);
  }

  public JavaCardException(String message, Throwable cause) {
    super(message, cause, true, false);
  }
}
