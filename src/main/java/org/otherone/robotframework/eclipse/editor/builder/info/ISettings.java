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
import java.util.Map;

public interface ISettings {

  // suite stuff

  List<IDynamicParsedString> getResourceFiles();

  Map<IDynamicParsedString, List<IDynamicParsedString>> getVariableFiles();

  /**
   * Libraries to load, mapping to their arguments. TODO a library can be loaded many times with
   * different names (see section 2.4.2)
   */
  Map<IDynamicParsedString, ILibraryFile> getLibraryFiles();

  @NotAllowedInResourceFiles
  IKeywordCall getSuiteSetup();

  @NotAllowedInResourceFiles
  IKeywordCall getSuiteTeardown();

  List<IDynamicParsedString> getDocumentation();

  @NotAllowedInResourceFiles
  Map<IParsedString, List<IDynamicParsedString>> getMetadata();

  // test case stuff

  @NotAllowedInResourceFiles
  List<IDynamicParsedString> getForcedTestTags();

  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  List<IDynamicParsedString> getDefaultTestTags();

  @NotAllowedInResourceFiles
  IKeywordCall getDefaultTestSetup();

  @NotAllowedInResourceFiles
  IKeywordCall getDefaultTestTeardown();

  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  IParsedKeywordString getTemplate(); // not dynamic

  /**
   * Since 2.5.6, the special keyword "NONE" can be used.
   */
  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  IDynamicParsedString getDefaultTestTimeout();

  @NotAllowedInResourceFiles
  @NotAllowedInTestSuiteInitializationFiles("2.5")
  IParsedString getDefaultTestTimeoutMessage();

}
