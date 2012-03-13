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
import java.util.List;

import com.nitorcreations.robotframework.eclipseide.structure.api.IDynamicParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IKeywordCall;
import com.nitorcreations.robotframework.eclipseide.structure.api.IParsedKeywordString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IRFEFileContents;
import com.nitorcreations.robotframework.eclipseide.structure.api.ITestCaseDefinition;

public class TestCaseDefinition extends KeywordSequence implements ITestCaseDefinition {

  private IParsedString sequenceName;
  private List<IDynamicParsedString> declaredStaticTagsIMM;
  private IKeywordCall testSetup;
  private IKeywordCall testTeardown;
  private IParsedKeywordString template;

  private final IRFEFileContents fileContents;

  public TestCaseDefinition(IRFEFileContents fileContents) {
    this.fileContents = fileContents;
  }

  // singles

  public void setSequenceName(IParsedString sequenceName) {
    this.sequenceName = sequenceName;
  }

  public void setTestSetup(IKeywordCall testSetup) {
    this.testSetup = testSetup;
  }

  public void setTestTeardown(IKeywordCall testTeardown) {
    this.testTeardown = testTeardown;
  }

  public void setTemplate(IParsedKeywordString template) {
    this.template = template;
  }

  // lists

  public void setDeclaredStaticTags(List<? extends IDynamicParsedString> declaredStaticTags) {
    this.declaredStaticTagsIMM = Collections.unmodifiableList(declaredStaticTags);
  }

  // getters

  @Override
  public IParsedString getSequenceName() {
    return sequenceName;
  }

  @Override
  public List<IDynamicParsedString> getDeclaredStaticTags() {
    return declaredStaticTagsIMM;
  }

  @Override
  public List<IDynamicParsedString> getDeclaredAndInheritedStaticTags() {
    if (fileContents.getSettings().getForcedTestTags() == null) {
      if (declaredStaticTagsIMM != null) {
        return declaredStaticTagsIMM;
      }
      return fileContents.getSettings().getDefaultTestTags();
    }
    if (declaredStaticTagsIMM != null) {
      List<IDynamicParsedString> declaredAndInheritedStaticTags = new ArrayList<IDynamicParsedString>();
      declaredAndInheritedStaticTags.addAll(fileContents.getSettings().getForcedTestTags());
      declaredAndInheritedStaticTags.addAll(declaredStaticTagsIMM);
      return declaredAndInheritedStaticTags;
    }
    if (fileContents.getSettings().getDefaultTestTags() != null) {
      List<IDynamicParsedString> declaredAndInheritedStaticTags = new ArrayList<IDynamicParsedString>();
      declaredAndInheritedStaticTags.addAll(fileContents.getSettings().getForcedTestTags());
      declaredStaticTagsIMM.addAll(fileContents.getSettings().getDefaultTestTags());
      return declaredAndInheritedStaticTags;
    }
    return fileContents.getSettings().getForcedTestTags();
  }

  @Override
  public IKeywordCall getTestSetup() {
    return testSetup;
  }

  @Override
  public IKeywordCall getTestTeardown() {
    return testTeardown;
  }

  @Override
  public IParsedKeywordString getTemplate() {
    return template;
  }

}
