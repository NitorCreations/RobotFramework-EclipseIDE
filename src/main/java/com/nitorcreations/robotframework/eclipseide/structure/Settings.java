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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nitorcreations.robotframework.eclipseide.structure.api.IDynamicParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IKeywordCall;
import com.nitorcreations.robotframework.eclipseide.structure.api.ILibraryFile;
import com.nitorcreations.robotframework.eclipseide.structure.api.IParsedKeywordString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.ISettings;

public class Settings implements ISettings {

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

  // immutable versions of above returned by getters
  private List<IDynamicParsedString> resourceFilesIMM;
  private Map<IDynamicParsedString, List<IDynamicParsedString>> variableFilesIMM;
  private Map<IDynamicParsedString, ILibraryFile> libraryFilesIMM;
  private Map<IParsedString, List<IDynamicParsedString>> metadataIMM;

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

}
