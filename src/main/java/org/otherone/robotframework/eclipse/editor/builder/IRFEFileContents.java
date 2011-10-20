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

import java.util.List;
import java.util.Map;

public interface IRFEFileContents {

  public interface IPositionedString {

    int SYNTHESIZED_VALUE_CHAR_POS = -1;

    /**
     * @return the string value.
     */
    String getValue();

    /**
     * @return the character offset (from the beginning of the file) where {@link #getValue()}
     *         begins. If the value is synthesized, the value {@link #SYNTHESIZED_VALUE_CHAR_POS} is
     *         returned.
     */
    int getArgCharPos();

    /**
     * @return the character offset (from the beginning of the file) where {@link #getValue()} ends,
     *         exclusively.
     */
    int getArgEndCharPos();

    /**
     * @return a debug string representing this string.
     */
    String getDebugString();

  }

  public interface IDynamicPositionedString extends IPositionedString {

    /**
     * Returns the static and dynamic parts, one by one.
     * 
     * @return when {@link #getValue()} returns "Get ${someVar} contents", this returns a list with
     *         [ "Get ", "${someVar}", " contents" ]
     */
    List<IPositionedString> getParts();

  }

  public interface IKeywordString {

    /**
     * @return If the keyword (returned by {@link IPositionedString#getValue()}) starts with one of
     *         the "Given", "When", "Then" or "And" prefixes, the alternate keyword with the prefix
     *         stripped is returned. Otherwise, null is returned.
     */
    String getAlternateValue();

  }

  public interface IPositionedKeywordString extends IPositionedString, IKeywordString {
  }

  public interface IDynamicPositionedKeywordString extends IDynamicPositionedString, IKeywordString {
  }

  // TODO check the IPositionedString:s below, could some of them be dynamic ?

  // Setting table, suite stuff

  List<IPositionedString> getResourceFiles();

  /**
   * Libraries to load, mapping to their arguments. TODO a library can be loaded many times with
   * different names (see section 2.4.2)
   */
  Map<IDynamicPositionedString, List<IDynamicPositionedString>> getLibraryFiles();

  IPositionedString getSuiteSetup();

  IPositionedString getSuiteTeardown();

  List<IDynamicPositionedString> getDocumentation();

  Map<IPositionedString, List<IDynamicPositionedString>> getMetadata();

  // Setting table, test case stuff

  List<IDynamicPositionedString> getForcedTestTags();

  List<IDynamicPositionedString> getDefaultTestTags();

  IKeywordCall getDefaultTestSetup();

  IKeywordCall getDefaultTestTeardown();

  IPositionedKeywordString getTemplate(); // not dynamic

  IPositionedString getDefaultTestTimeout();

  IPositionedString getDefaultTestTimeoutMessage();

  // Other tables

  public interface IKeywordSequence {

    IDynamicPositionedString getSequenceName(); // TODO just IPositionedString for test cases?

    List<IDynamicPositionedString> getDocumentation();

    List<IDynamicPositionedString> getDeclaredStaticTags();

    /**
     * Calculated by combining {@link IRFEFileContents#getForcedTestTags()} with either
     * {@link IRFEFileContents#getDefaultTestTags()} or {@link #getDeclaredStaticTags()}.
     * 
     * Please note that additional tags can be given from the command line, and the tag set can also
     * be manipulated dynamically when the test case executes. These are not included in the
     * returned set, only the "static" tags as defined above.
     * 
     * @return the "effective" set of static tags for this keyword.
     */
    List<IDynamicPositionedString> getDeclaredAndInheritedStaticTags();

    /**
     * The {@link IKeywordCall#getKeyword()} may exceptionally return one of the special values ""
     * or "NONE" when the user wants to override the file-default test setup with a no-op. (It's
     * unknown why the "No Operation" keyword wasn't reused).
     */
    IKeywordCall getTestSetup();

    /**
     * Same special circumstances as documented in {@link #getTestSetup()} apply.
     */
    IKeywordCall getTestTeardown();

    IPositionedKeywordString getTemplate(); // not dynamic

    IPositionedString getTestTimeout();

    IPositionedString getTestTimeoutMessage();

    List<IKeywordCall> getKeywordCalls();
  }

  public interface IKeywordCall {
    /**
     * Returns null if a template keyword is active
     * 
     * @return
     */
    IDynamicPositionedKeywordString getKeyword();

    List<IDynamicPositionedString> getArguments();
  }

  Map<IPositionedString, IDynamicPositionedString> getVariables();

  Map<IDynamicPositionedString, IKeywordSequence> getTestCases();

  Map<IDynamicPositionedString, IKeywordSequence> getKeywords();

}
