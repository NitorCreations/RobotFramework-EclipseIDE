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
package org.otherone.robotframework.eclipse.editor.builder.info;

import java.util.List;

public interface ITestCaseDefinition extends IKeywordSequence {

  /**
   * @return may return the special keyword "NONE" to override suite-default tags declared with
   *         {@link IRFEFileContents#getDefaultTestTags()}.
   */
  List<IDynamicParsedString> getDeclaredStaticTags();

  /**
   * Calculated by combining {@link IRFEFileContents#getForcedTestTags()} with either
   * {@link IRFEFileContents#getDefaultTestTags()} or {@link #getDeclaredStaticTags()}.
   * 
   * Please note that additional tags can be given from the command line, and the tag set can also
   * be manipulated dynamically when the test case executes. These are not included in the returned
   * set, only the "static" tags as defined above.
   * 
   * @return the "effective" set of static tags for this keyword.
   */
  List<IDynamicParsedString> getDeclaredAndInheritedStaticTags();

  /**
   * Since Robot Framework 2.5.6, the {@link IKeywordCall#getKeyword()} may exceptionally return one
   * of the special values "" or "NONE" when the user wants to override the file-default test setup
   * with a no-op. (It's unknown why the "No Operation" keyword wasn't reused).
   */
  IKeywordCall getTestSetup();

  /**
   * Same special circumstances as documented in {@link #getTestSetup()} apply.
   */
  IKeywordCall getTestTeardown();

  /**
   * Since Robot Framework 2.5.6, the special template "NONE" indicates no template should be used.
   */
  IParsedKeywordString getTemplate(); // not dynamic

}