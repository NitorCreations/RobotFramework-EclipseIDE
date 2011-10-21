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

import org.otherone.robotframework.eclipse.editor.builder.info.IParsedString;

public class RFEFileContents /* implements IRFEFileContents */{

  public static class PositionedString implements IParsedString {
    private final String value;
    private final int argCharPos;

    public PositionedString(String value, int argCharPos) {
      this.value = value;
      this.argCharPos = argCharPos;
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public int getArgCharPos() {
      return argCharPos;
    }

    /**
     * End position, exclusively.
     */
    @Override
    public int getArgEndCharPos() {
      return argCharPos + value.length();
    }

    @Override
    public String toString() {
      return '"' + value + '"';
    }

    @Override
    public String getDebugString() {
      return '"' + value + "\" @" + argCharPos + "-" + (getArgEndCharPos() - 1);
    }

  }

  public static class KeywordSequence {
    private int x;
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
