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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.otherone.robotframework.eclipse.editor.builder.info.IDynamicParsedString;
import org.otherone.robotframework.eclipse.editor.builder.info.IKeywordCall;
import org.otherone.robotframework.eclipse.editor.builder.info.IUserKeywordDefinition;

public class UserKeywordDefinition extends KeywordSequence implements IUserKeywordDefinition {

  private Set<String> argumentsKeys;
  private List<IDynamicParsedString> arguments;
  private List<IDynamicParsedString> returnValues;
  private IKeywordCall keywordTeardown;

  private List<IDynamicParsedString> argumentsIMM;
  private List<IDynamicParsedString> returnValuesIMM;

  // singles

  public void setKeywordTeardown(IKeywordCall keywordTeardown) {
    this.keywordTeardown = keywordTeardown;
  }

  // lists

  public boolean addArgument(IDynamicParsedString argument) {
    if (this.arguments == null) {
      this.argumentsKeys = new HashSet<String>();
      this.arguments = new ArrayList<IDynamicParsedString>();
      this.argumentsIMM = Collections.unmodifiableList(this.arguments);
    }
    if (argumentsKeys.add(argument.getParts().get(0).getValue())) {
      return false;
    }
    return this.arguments.add(argument);
  }

  public void addReturnValue(IDynamicParsedString returnValue) {
    if (this.returnValues == null) {
      this.returnValues = new ArrayList<IDynamicParsedString>();
      this.returnValuesIMM = Collections.unmodifiableList(this.returnValues);
    }
    this.returnValues.add(returnValue);
  }

  // getters

  @Override
  public List<IDynamicParsedString> getArguments() {
    return argumentsIMM;
  }

  @Override
  public List<IDynamicParsedString> getReturnValues() {
    return returnValuesIMM;
  }

  @Override
  public IKeywordCall getKeywordTeardown() {
    return keywordTeardown;
  }

}
