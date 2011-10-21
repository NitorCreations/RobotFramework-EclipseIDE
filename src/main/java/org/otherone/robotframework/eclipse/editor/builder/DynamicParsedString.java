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

import java.util.Collections;
import java.util.List;

import org.otherone.robotframework.eclipse.editor.builder.info.IDynamicParsedKeywordString;
import org.otherone.robotframework.eclipse.editor.builder.info.IDynamicParsedString;

public class DynamicParsedString extends ParsedString implements IDynamicParsedKeywordString {

  private final List<IDynamicParsedString> parts;

  /**
   * @param parts
   *          automatically wrapped using {@link Collections#unmodifiableList(List)}
   */
  public DynamicParsedString(String value, int argCharPos, List<? extends IDynamicParsedString> parts) {
    super(value, argCharPos);
    if (parts == null) {
      throw new NullPointerException("parts");
    }
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("parts list is empty");
    }
    this.parts = Collections.unmodifiableList(parts);
  }

  @Override
  public List<IDynamicParsedString> getParts() {
    return parts;
  }

  @Override
  public String toString() {
    return parts.toString();
  }

}
