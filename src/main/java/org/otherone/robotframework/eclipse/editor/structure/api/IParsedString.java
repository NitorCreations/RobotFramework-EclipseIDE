/**
 * Copyright 2011 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.otherone.robotframework.eclipse.editor.structure.api;

public interface IParsedString {

  int SYNTHESIZED_VALUE_CHAR_POS = -1;

  /**
   * @return the string value.
   */
  String getValue();

  /**
   * @return the character offset (from the beginning of the file) where {@link #getValue()} begins.
   *         If the value is synthesized, the value {@link #SYNTHESIZED_VALUE_CHAR_POS} is returned.
   */
  int getArgCharPos();

  /**
   * @return the character offset (from the beginning of the file) where {@link #getValue()} ends,
   *         exclusively.
   */
  int getArgEndCharPos();

  /**
   * @return a debug string representing this string.
   */
  String getDebugString();

  /**
   * @return the hash code of {@link #getValue()}.
   */
  @Override
  public int hashCode();

  /**
   * @return getValue().equals(obj.getValue())
   */
  @Override
  public boolean equals(Object obj);

}
