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
package org.otherone.robotframework.eclipse.editor.builder.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.otherone.robotframework.eclipse.editor.structure.ParsedString;

public class RFEPreParser {

  private final String filename;
  private final List<RFELine> lines;

  /**
   * For documents being edited.
   * 
   * @param document
   */
  public RFEPreParser(IDocument document, List<RFELine> lines) {
    this.filename = "<document being edited>";
    this.lines = lines;
  }

  public void preParse() throws CoreException {
    try {
      System.out.println("Preparsing " + filename);
      for (RFELine line : lines) {
        try {
          parseLine(line);
        } catch (CoreException e) {
          throw new RuntimeException("Error when preparsing line " + line.lineNo + ": '" + line.arguments + "'", e);
        } catch (RuntimeException e) {
          throw new RuntimeException("Internal error when preparsing line " + line.lineNo + ": '" + line.arguments + "'", e);
        }
      }

      // TODO store results
    } catch (Exception e) {
      throw new RuntimeException("Error preparsing robot file " + filename, e);
    }
  }

  //  enum State {
  //    IGNORE, SETTING_TABLE, VARIABLE_TABLE, TESTCASE_TABLE_INITIAL, TESTCASE_TABLE_ACTIVE, KEYWORD_TABLE_INITIAL, KEYWORD_TABLE_ACTIVE,
  //  }

  public enum Type {
    IGNORE, IGNORE_TABLE, SETTING_TABLE_BEGIN, SETTING_TABLE_LINE, VARIABLE_TABLE_BEGIN, VARIABLE_TABLE_LINE, TESTCASE_TABLE_BEGIN, TESTCASE_TABLE_IGNORE,
    TESTCASE_TABLE_TESTCASE_BEGIN, TESTCASE_TABLE_TESTCASE_LINE, KEYWORD_TABLE_BEGIN, KEYWORD_TABLE_IGNORE, KEYWORD_TABLE_KEYWORD_BEGIN,
    KEYWORD_TABLE_KEYWORD_LINE, CONTINUATION_LINE, COMMENT_LINE,
  }

  private Type prevLineType = Type.IGNORE;

  private void parseLine(RFELine line) throws CoreException {
    //System.out.println(line.arguments);
    Type tableType = tryParseTableSwitch(line);
    if (tableType != null) {
      line.info.put(Type.class, tableType);
      prevLineType = tableType;
      return;
    }
    if (tryParseContinuationLine(line)) {
      line.info.put(Type.class, Type.CONTINUATION_LINE);
      // prevLineType not updated
      return;
    }
    String first = line.arguments.get(0).getValue();
    boolean firstEmpty = first.isEmpty();
    String second = line.arguments.size() < 2 ? "" : line.arguments.get(1).getValue();
    if (first.startsWith("#") || firstEmpty && second.startsWith("#")) {
      line.info.put(Type.class, Type.COMMENT_LINE);
      // prevLineType not updated
      return;
    }
    Type lineType = determineLineTypeFromPrevious(firstEmpty);
    line.info.put(Type.class, lineType);
    prevLineType = lineType;
  }

  private Type tryParseTableSwitch(RFELine line) throws CoreException {
    ParsedString tableArgument = line.arguments.get(0);
    if (!tableArgument.getValue().startsWith("*")) {
      return null;
    }
    String table = tableArgument.getValue().replace("*", "");
    Type curType = tableNameToType.get(table);
    if (curType == null) {
      return Type.IGNORE_TABLE;
    }
    return curType;
  }

  private static final Map<String, Type> tableNameToType = new HashMap<String, Type>();

  static {
    tableNameToType.put("Setting", Type.SETTING_TABLE_BEGIN);
    tableNameToType.put("Settings", Type.SETTING_TABLE_BEGIN);
    tableNameToType.put("Metadata", Type.SETTING_TABLE_BEGIN);
    tableNameToType.put("Variable", Type.VARIABLE_TABLE_BEGIN);
    tableNameToType.put("Variables", Type.VARIABLE_TABLE_BEGIN);
    tableNameToType.put("Test Case", Type.TESTCASE_TABLE_BEGIN);
    tableNameToType.put("Test Cases", Type.TESTCASE_TABLE_BEGIN);
    tableNameToType.put("Keyword", Type.KEYWORD_TABLE_BEGIN);
    tableNameToType.put("Keywords", Type.KEYWORD_TABLE_BEGIN);
    tableNameToType.put("User Keyword", Type.KEYWORD_TABLE_BEGIN);
    tableNameToType.put("User Keywords", Type.KEYWORD_TABLE_BEGIN);
  }

  private boolean tryParseContinuationLine(RFELine line) throws CoreException {
    ParsedString arg = line.arguments.get(0);
    if (!arg.getValue().equals("\\")) {
      // first column not continuation, try second-column continuation
      if (!arg.getValue().isEmpty()) {
        // first column must be empty for second-column continuation
        return false;
      }
      if (line.arguments.size() < 2) {
        // must have two columns 
        return false;
      }
      if (!line.arguments.get(1).getValue().equals("\\")) {
        // second column not continuation either
        return false;
      }
    }
    return true;
  }

  private Type determineLineTypeFromPrevious(boolean firstEmpty) {
    switch (prevLineType) {
      case IGNORE:
        return Type.IGNORE;
      case SETTING_TABLE_BEGIN:
      case SETTING_TABLE_LINE:
        return Type.SETTING_TABLE_LINE;
      case VARIABLE_TABLE_BEGIN:
      case VARIABLE_TABLE_LINE:
        return Type.VARIABLE_TABLE_LINE;
      case TESTCASE_TABLE_BEGIN:
      case TESTCASE_TABLE_IGNORE:
        return !firstEmpty ? Type.TESTCASE_TABLE_TESTCASE_BEGIN : Type.TESTCASE_TABLE_IGNORE;
      case TESTCASE_TABLE_TESTCASE_BEGIN:
      case TESTCASE_TABLE_TESTCASE_LINE:
        return !firstEmpty ? Type.TESTCASE_TABLE_TESTCASE_BEGIN : Type.TESTCASE_TABLE_TESTCASE_LINE;
      case KEYWORD_TABLE_BEGIN:
      case KEYWORD_TABLE_IGNORE:
        return !firstEmpty ? Type.KEYWORD_TABLE_KEYWORD_BEGIN : Type.KEYWORD_TABLE_IGNORE;
      case KEYWORD_TABLE_KEYWORD_BEGIN:
      case KEYWORD_TABLE_KEYWORD_LINE:
        return !firstEmpty ? Type.KEYWORD_TABLE_KEYWORD_BEGIN : Type.KEYWORD_TABLE_KEYWORD_LINE;
      case IGNORE_TABLE:
        return Type.IGNORE;
    }
    throw new RuntimeException("Unhandled previous line type " + prevLineType);
  }

}
