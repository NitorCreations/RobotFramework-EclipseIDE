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

public class RFEFileContents implements IRFEFileContents {

  public static class PositionedString {
    private final String value;
    private final int argCharPos;

    public PositionedString(String value, int argCharPos) {
      this.value = value;
      this.argCharPos = argCharPos;
    }

    public String getValue() {
      return value;
    }

    public int argCharPos() {
      return argCharPos;
    }

    /**
     * End position, exclusively.
     */
    public int argEndCharPos() {
      return argCharPos + value.length();
    }

    @Override
    public String toString() {
      return '"' + value + '"';
    }

    public String debugString() {
      return '"' + value + "\" @" + argCharPos + "-" + (argEndCharPos() - 1);
    }

  }
  
  public static class KeywordSequence {
    private final ;
  }

  List<PositionedString> resourceFiles;
  List<PositionedString> variableFiles;
  PositionedString suiteSetup;
  PositionedString suiteTeardown;
  PositionedString defaultTestSetup;
  PositionedString defaultTestTeardown;
  List<PositionedString> forcedTags;
  List<PositionedString> defaultTags;

  Map<PositionedString, PositionedString> variables;

  Map<PositionedString, KeywordSequence> testCases;
  Map<PositionedString, KeywordSequence> keywords;

}
