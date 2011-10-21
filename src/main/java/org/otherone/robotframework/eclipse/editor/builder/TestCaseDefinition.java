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
import java.util.List;

import org.otherone.robotframework.eclipse.editor.builder.info.IDynamicParsedString;
import org.otherone.robotframework.eclipse.editor.builder.info.IKeywordCall;
import org.otherone.robotframework.eclipse.editor.builder.info.IParsedKeywordString;
import org.otherone.robotframework.eclipse.editor.builder.info.ITestCaseDefinition;

public class TestCaseDefinition extends KeywordSequence implements ITestCaseDefinition {

  private List<IDynamicParsedString> declaredStaticTags;
  private List<IDynamicParsedString> declaredAndInheritedStaticTags;
  private IKeywordCall testSetup;
  private IKeywordCall testTeardown;
  private IParsedKeywordString template;

  private List<IDynamicParsedString> declaredStaticTagsIMM;
  private List<IDynamicParsedString> declaredAndInheritedStaticTagsIMM;

  // singles

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

  public void addDeclaredStaticTag(IDynamicParsedString declaredStaticTag) {
    if (this.declaredStaticTags == null) {
      this.declaredStaticTags = new ArrayList<IDynamicParsedString>();
      this.declaredStaticTagsIMM = Collections.unmodifiableList(this.declaredStaticTags);
    }
    this.declaredStaticTags.add(declaredStaticTag);
  }

  public void addDeclaredAndInheritedStaticTag(IDynamicParsedString declaredAndInheritedStaticTag) {
    if (this.declaredAndInheritedStaticTags == null) {
      this.declaredAndInheritedStaticTags = new ArrayList<IDynamicParsedString>();
      this.declaredAndInheritedStaticTagsIMM = Collections.unmodifiableList(this.declaredAndInheritedStaticTags);
    }
    this.declaredAndInheritedStaticTags.add(declaredAndInheritedStaticTag);
  }

  // getters

  @Override
  public List<IDynamicParsedString> getDeclaredStaticTags() {
    return declaredStaticTagsIMM;
  }

  @Override
  public List<IDynamicParsedString> getDeclaredAndInheritedStaticTags() {
    return declaredAndInheritedStaticTagsIMM;
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
