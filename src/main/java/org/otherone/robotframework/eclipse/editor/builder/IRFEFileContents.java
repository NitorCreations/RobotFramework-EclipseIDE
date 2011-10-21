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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

/**
 * This interface provides bean-ish access to the parsed structure of a robot file.
 * 
 * @author xkr47
 */
public interface IRFEFileContents {

  /**
   * This is used to tag methods whose return value should always be null for resource files. I.e.
   * the resource file is syntactically broken if the annotated method returns a non-null value.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface NotAllowedInResourceFiles {
    /**
     * @return since which Robot Framework version is it not allowed? (i.e. it was allowed before
     *         this version)
     */
    String value() default "";
  }

  /**
   * This is used to tag methods whose return value should always be null for test suite
   * initialization files, e.g. "__init__.txt".
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface NotAllowedInTestSuiteInitializationFiles {
    /**
     * @return since which Robot Framework version is it not allowed? (i.e. it was allowed before
     *         this version)
     */
    String value() default "";
  }

  /**
   * This is used to tag methods whose return value should always be null for robot files prior to a
   * specific version.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface AvailableFrom {
    /**
     * @return since which Robot Framework version is it available?
     */
    String value();
  }

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

  List<IDynamicPositionedString> getResourceFiles();

  Map<IDynamicPositionedString, List<IDynamicPositionedString>> getVariableFiles();

  /**
   * Libraries to load, mapping to their arguments. TODO a library can be loaded many times with
   * different names (see section 2.4.2)
   */
  Map<IDynamicPositionedString, List<IDynamicPositionedString>> getLibraryFiles();

  @NotAllowedInResourceFiles
  IKeywordCall getSuiteSetup();

  @NotAllowedInResourceFiles
  IKeywordCall getSuiteTeardown();

  List<IDynamicPositionedString> getDocumentation();

  @NotAllowedInResourceFiles
  Map<IPositionedString, List<IDynamicPositionedString>> getMetadata();

  // Setting table, test case stuff

  @NotAllowedInResourceFiles
  List<IDynamicPositionedString> getForcedTestTags();

  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  List<IDynamicPositionedString> getDefaultTestTags();

  @NotAllowedInResourceFiles
  IKeywordCall getDefaultTestSetup();

  @NotAllowedInResourceFiles
  IKeywordCall getDefaultTestTeardown();

  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  IPositionedKeywordString getTemplate(); // not dynamic

  /**
   * Since 2.5.6, the special keyword "NONE" can be used.
   */
  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  IDynamicPositionedString getDefaultTestTimeout();

  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  IPositionedString getDefaultTestTimeoutMessage();

  // Other tables

  public interface ITestCaseDefinition extends IKeywordSequence {

    /**
     * @return may return the special keyword "NONE" to override suite-default tags declared with
     *         {@link IRFEFileContents#getDefaultTestTags()}.
     */
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
     * Since Robot Framework 2.5.6, the {@link IKeywordCall#getKeyword()} may exceptionally return
     * one of the special values "" or "NONE" when the user wants to override the file-default test
     * setup with a no-op. (It's unknown why the "No Operation" keyword wasn't reused).
     */
    IKeywordCall getTestSetup();

    /**
     * Same special circumstances as documented in {@link #getTestSetup()} apply.
     */
    IKeywordCall getTestTeardown();

    /**
     * Since Robot Framework 2.5.6, the special template "NONE" indicates no template should be
     * used.
     */
    IPositionedKeywordString getTemplate(); // not dynamic

  }

  public interface IUserKeywordDefinition extends IKeywordSequence {

    /**
     * When arguments have default values, {@link IDynamicPositionedString#getParts()} returns two
     * parts, "${argument}" and "=value".
     */
    List<IDynamicPositionedString> getArguments();

    List<IDynamicPositionedString> getReturnValues();

    @AvailableFrom("2.6")
    IKeywordCall getKeywordTeardown();

  }

  public interface IKeywordSequence {

    IDynamicPositionedString getSequenceName(); // TODO just IPositionedString for test cases?

    List<IDynamicPositionedString> getDocumentation();

    /**
     * Since 2.5.6, the special keyword "NONE" can be used.
     */
    IDynamicPositionedString getTimeout();

    IPositionedString getTimeoutMessage();

    List<IKeywordCall> getKeywordCalls();
  }

  public interface IKeywordCall {
    /**
     * Returns null if a template keyword is active, except if the keyword is ":FOR".
     */
    IDynamicPositionedKeywordString getKeyword();

    List<IDynamicPositionedString> getArguments();
  }

  Map<IPositionedString, IDynamicPositionedString> getVariables();

  Map<IPositionedString, ITestCaseDefinition> getTestCases();

  Map<IDynamicPositionedString, IUserKeywordDefinition> getKeywords();

}
