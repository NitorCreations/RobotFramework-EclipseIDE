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
import org.otherone.robotframework.eclipse.editor.builder.info.IKeywordCall;

public class KeywordCall implements IKeywordCall {

  private IDynamicParsedKeywordString keyword;
  private List<IDynamicParsedString> argumentsIMM;

  // singles

  public void setKeyword(IDynamicParsedKeywordString keyword) {
    this.keyword = keyword;
  }

  // lists

  public void setArguments(List<IDynamicParsedString> arguments) {
    this.argumentsIMM = Collections.unmodifiableList(arguments);
  }

  // getters

  @Override
  public IDynamicParsedKeywordString getKeyword() {
    return keyword;
  }

  @Override
  public List<IDynamicParsedString> getArguments() {
    return argumentsIMM;
  }

}
