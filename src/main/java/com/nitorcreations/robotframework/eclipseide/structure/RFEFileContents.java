/**
 * Copyright 2012 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.structure;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.nitorcreations.robotframework.eclipseide.structure.api.IDynamicParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IRFEFileContents;
import com.nitorcreations.robotframework.eclipseide.structure.api.ISettings;
import com.nitorcreations.robotframework.eclipseide.structure.api.ITestCaseDefinition;
import com.nitorcreations.robotframework.eclipseide.structure.api.IUserKeywordDefinition;
import com.nitorcreations.robotframework.eclipseide.structure.api.IVariableDefinition;

public class RFEFileContents implements IRFEFileContents {

  private static final class IParsedStringOffsetComparator implements Comparator<IParsedString> {
    @Override
    public int compare(IParsedString o1, IParsedString o2) {
      return o1.getArgCharPos() - o2.getArgCharPos();
    }
  }

  private final Settings settings = new Settings();
  private Map<IParsedString, IVariableDefinition> variables;
  private Map<IParsedString, ITestCaseDefinition> testCases;
  private Map<IDynamicParsedString, IUserKeywordDefinition> keywords;

  // immutable versions of above returned by getters
  private Map<IParsedString, IVariableDefinition> variablesIMM;
  private Map<IParsedString, ITestCaseDefinition> testCasesIMM;
  private Map<IDynamicParsedString, IUserKeywordDefinition> keywordsIMM;

  // maps with single values

  public boolean addVariable(IVariableDefinition variable) {
    if (this.variables == null) {
      this.variables = new TreeMap<IParsedString, IVariableDefinition>(new IParsedStringOffsetComparator());
      this.variablesIMM = Collections.unmodifiableMap(this.variables);
    }
    if (this.variables.containsKey(variable.getVariable())) {
      return false;
    }
    this.variables.put(variable.getVariable(), variable);
    return true;
  }

  public boolean addTestCase(ITestCaseDefinition testCase) {
    if (this.testCases == null) {
      this.testCases = new TreeMap<IParsedString, ITestCaseDefinition>(new IParsedStringOffsetComparator());
      this.testCasesIMM = Collections.unmodifiableMap(this.testCases);
    }
    if (this.testCases.containsKey(testCase.getSequenceName())) {
      return false;
    }
    this.testCases.put(testCase.getSequenceName(), testCase);
    return true;
  }

  public boolean addKeyword(IUserKeywordDefinition keyword) {
    if (this.keywords == null) {
      this.keywords = new TreeMap<IDynamicParsedString, IUserKeywordDefinition>(new IParsedStringOffsetComparator());
      this.keywordsIMM = Collections.unmodifiableMap(this.keywords);
    }
    if (this.keywords.containsKey(keyword.getSequenceName())) {
      return false;
    }
    this.keywords.put(keyword.getSequenceName(), keyword);
    return true;
  }

  public Settings getSettingsInt() {
    return settings;
  }

  // getters

  @Override
  public ISettings getSettings() {
    return settings;
  }

  @Override
  public Map<IParsedString, IVariableDefinition> getVariables() {
    return variablesIMM;
  }

  @Override
  public Map<IParsedString, ITestCaseDefinition> getTestCases() {
    return testCasesIMM;
  }

  @Override
  public Map<IDynamicParsedString, IUserKeywordDefinition> getKeywords() {
    return keywordsIMM;
  }

}
