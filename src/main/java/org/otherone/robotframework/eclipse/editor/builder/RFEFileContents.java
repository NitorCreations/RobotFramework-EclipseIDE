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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.otherone.robotframework.eclipse.editor.builder.info.IDynamicParsedString;
import org.otherone.robotframework.eclipse.editor.builder.info.IKeywordCall;
import org.otherone.robotframework.eclipse.editor.builder.info.ILibraryFile;
import org.otherone.robotframework.eclipse.editor.builder.info.IParsedKeywordString;
import org.otherone.robotframework.eclipse.editor.builder.info.IParsedString;
import org.otherone.robotframework.eclipse.editor.builder.info.IRFEFileContents;
import org.otherone.robotframework.eclipse.editor.builder.info.ITestCaseDefinition;
import org.otherone.robotframework.eclipse.editor.builder.info.IUserKeywordDefinition;
import org.otherone.robotframework.eclipse.editor.builder.info.IVariableDefinition;

public class RFEFileContents implements IRFEFileContents {

  private List<IDynamicParsedString> resourceFiles;
  private Map<IDynamicParsedString, List<IDynamicParsedString>> variableFiles;
  private Map<IDynamicParsedString, ILibraryFile> libraryFiles;
  private IKeywordCall suiteSetup;
  private IKeywordCall suiteTeardown;
  private List<IDynamicParsedString> documentationIMM;
  private Map<IParsedString, List<IDynamicParsedString>> metadata;
  private List<IDynamicParsedString> forcedTestTagsIMM;
  private List<IDynamicParsedString> defaultTestTagsIMM;
  private IKeywordCall defaultTestSetup;
  private IKeywordCall defaultTestTeardown;
  private IParsedKeywordString template;
  private IDynamicParsedString defaultTestTimeout;
  private IParsedString defaultTestTimeoutMessage;
  private Map<IParsedString, IVariableDefinition> variables;
  private Map<IParsedString, ITestCaseDefinition> testCases;
  private Map<IDynamicParsedString, IUserKeywordDefinition> keywords;

  // immutable versions of above returned by getters
  private List<IDynamicParsedString> resourceFilesIMM;
  private Map<IDynamicParsedString, List<IDynamicParsedString>> variableFilesIMM;
  private Map<IDynamicParsedString, ILibraryFile> libraryFilesIMM;
  private Map<IParsedString, List<IDynamicParsedString>> metadataIMM;
  private Map<IParsedString, IVariableDefinition> variablesIMM;
  private Map<IParsedString, ITestCaseDefinition> testCasesIMM;
  private Map<IDynamicParsedString, IUserKeywordDefinition> keywordsIMM;

  // singles

  public void setSuiteSetup(IKeywordCall suiteSetup) {
    this.suiteSetup = suiteSetup;
  }

  public void setSuiteTeardown(IKeywordCall suiteTeardown) {
    this.suiteTeardown = suiteTeardown;
  }

  public void setDefaultTestSetup(IKeywordCall defaultTestSetup) {
    this.defaultTestSetup = defaultTestSetup;
  }

  public void setDefaultTestTeardown(IKeywordCall defaultTestTeardown) {
    this.defaultTestTeardown = defaultTestTeardown;
  }

  public void setTemplate(IParsedKeywordString template) {
    this.template = template;
  }

  public void setDefaultTestTimeout(IDynamicParsedString defaultTestTimeout) {
    this.defaultTestTimeout = defaultTestTimeout;
  }

  public void setDefaultTestTimeoutMessage(IParsedString defaultTestTimeoutMessage) {
    this.defaultTestTimeoutMessage = defaultTestTimeoutMessage;
  }

  // lists

  public boolean addResourceFile(IDynamicParsedString resourceFile) {
    if (this.resourceFiles == null) {
      this.resourceFiles = new ArrayList<IDynamicParsedString>();
      this.resourceFilesIMM = Collections.unmodifiableList(this.resourceFiles);
    }
    return this.resourceFiles.add(resourceFile);
  }

  public void setDocumentation(List<? extends IDynamicParsedString> documentation) {
    this.documentationIMM = Collections.unmodifiableList(documentation);
  }

  public void setForcedTestTags(List<? extends IDynamicParsedString> forcedTestTags) {
    this.forcedTestTagsIMM = Collections.unmodifiableList(forcedTestTags);
  }

  public void setDefaultTestTags(List<? extends IDynamicParsedString> defaultTestTags) {
    this.defaultTestTagsIMM = Collections.unmodifiableList(defaultTestTags);
  }

  // maps with single values

  public boolean addLibraryFile(ILibraryFile libraryFile) {
    if (this.libraryFiles == null) {
      this.libraryFiles = new HashMap<IDynamicParsedString, ILibraryFile>();
      this.libraryFilesIMM = Collections.unmodifiableMap(this.libraryFiles);
    }
    if (this.libraryFiles.containsKey(libraryFile.getCustomName())) {
      return false;
    }
    this.libraryFiles.put(libraryFile.getCustomName(), libraryFile);
    return true;
  }

  public boolean addVariable(IVariableDefinition variable) {
    if (this.variables == null) {
      this.variables = new HashMap<IParsedString, IVariableDefinition>();
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
      this.testCases = new HashMap<IParsedString, ITestCaseDefinition>();
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
      this.keywords = new HashMap<IDynamicParsedString, IUserKeywordDefinition>();
      this.keywordsIMM = Collections.unmodifiableMap(this.keywords);
    }
    if (this.keywords.containsKey(keyword.getSequenceName())) {
      return false;
    }
    this.keywords.put(keyword.getSequenceName(), keyword);
    return true;
  }

  // maps with list values

  public boolean addVariableFile(IDynamicParsedString variableFile, List<? extends IDynamicParsedString> arguments) {
    if (this.variableFiles == null) {
      this.variableFiles = new HashMap<IDynamicParsedString, List<IDynamicParsedString>>();
      this.variableFilesIMM = Collections.unmodifiableMap(this.variableFiles);
    }
    if (this.variableFilesIMM.containsKey(variableFile)) {
      return false;
    }
    this.variableFiles.put(variableFile, Collections.unmodifiableList(arguments));
    return true;
  }

  public boolean addMetadata(IParsedString key, List<? extends IDynamicParsedString> values) {
    if (this.metadata == null) {
      this.metadata = new HashMap<IParsedString, List<IDynamicParsedString>>();
      this.metadataIMM = Collections.unmodifiableMap(this.metadata);
    }
    if (this.metadata.containsKey(key)) {
      return false;
    }
    this.metadata.put(key, Collections.unmodifiableList(values));
    return true;
  }

  // getters

  @Override
  public List<IDynamicParsedString> getResourceFiles() {
    return resourceFilesIMM;
  }

  @Override
  public Map<IDynamicParsedString, List<IDynamicParsedString>> getVariableFiles() {
    return variableFilesIMM;
  }

  @Override
  public Map<IDynamicParsedString, ILibraryFile> getLibraryFiles() {
    return libraryFilesIMM;
  }

  @Override
  public IKeywordCall getSuiteSetup() {
    return suiteSetup;
  }

  @Override
  public IKeywordCall getSuiteTeardown() {
    return suiteTeardown;
  }

  @Override
  public List<IDynamicParsedString> getDocumentation() {
    return documentationIMM;
  }

  @Override
  public Map<IParsedString, List<IDynamicParsedString>> getMetadata() {
    return metadataIMM;
  }

  @Override
  public List<IDynamicParsedString> getForcedTestTags() {
    return forcedTestTagsIMM;
  }

  @Override
  public List<IDynamicParsedString> getDefaultTestTags() {
    return defaultTestTagsIMM;
  }

  @Override
  public IKeywordCall getDefaultTestSetup() {
    return defaultTestSetup;
  }

  @Override
  public IKeywordCall getDefaultTestTeardown() {
    return defaultTestTeardown;
  }

  @Override
  public IParsedKeywordString getTemplate() {
    return template;
  }

  @Override
  public IDynamicParsedString getDefaultTestTimeout() {
    return defaultTestTimeout;
  }

  @Override
  public IParsedString getDefaultTestTimeoutMessage() {
    return defaultTestTimeoutMessage;
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
