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
package org.otherone.robotframework.eclipse.editor.builder;

import org.otherone.robotframework.eclipse.editor.builder.info.IParsedKeywordString;

/**
 * An immutable implementation of all the I*String interfaces in the ...builder.info package.
 * 
 * @author xkr47
 */
public class ParsedString implements IParsedKeywordString {

  private static String[] STRIPPABLE_PREFIXES = { "given ", "when ", "then ", "and " };

  private final String value;
  private final int argCharPos;

  public ParsedString(String value, int argCharPos) {
    if (value == null) {
      throw new NullPointerException("value");
    }
    if (value.isEmpty()) {
      throw new IllegalArgumentException("value is empty");
    }
    if (argCharPos < 0) {
      throw new IllegalArgumentException("argCharPos < 0");
    }
    this.value = value;
    this.argCharPos = argCharPos;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public int getArgCharPos() {
    return argCharPos;
  }

  @Override
  public int getArgEndCharPos() {
    return argCharPos + value.length();
  }

  @Override
  public String getAlternateValue() {
    String lcValue = value.toLowerCase();
    for (String strippablePrefix : STRIPPABLE_PREFIXES) {
      if (lcValue.startsWith(strippablePrefix)) {
        return value.substring(strippablePrefix.length());
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return '"' + value + '"';
  }

  @Override
  public String getDebugString() {
    return toString() + " @" + argCharPos + "-" + (getArgEndCharPos() - 1);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return value.equals(obj);
  }

}
