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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.util.ParserUtil;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

// TODO case sensitivity
public class RFEPreParser {

    public static final String CONTINUATION_STR = "...";
    private final String filename;
    private final List<RFELine> lines;

    /**
     * For documents being edited.
     * 
     * @param document
     */
    public RFEPreParser(String filename, List<RFELine> lines) {
        this.filename = filename;
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

    // enum State {
    // IGNORE, SETTING_TABLE, VARIABLE_TABLE, TESTCASE_TABLE_INITIAL,
    // TESTCASE_TABLE_ACTIVE, KEYWORD_TABLE_INITIAL, KEYWORD_TABLE_ACTIVE,
    // }

    private LineType prevLineType = LineType.IGNORE;

    private void parseLine(RFELine line) throws CoreException {
        if (line.arguments.isEmpty()) {
            line.type = LineType.IGNORE;
            return;
        }
        // System.out.println(line.arguments);
        LineType tableType = tryParseTableSwitch(line);
        if (tableType != null) {
            line.type = tableType;
            prevLineType = tableType;
            return;
        }
        if (tryParseContinuationLine(line)) {
            line.type = LineType.CONTINUATION_LINE;
            // prevLineType not updated
            return;
        }
        ParsedString firstArg = line.arguments.get(0);
        boolean firstEmpty = firstArg.isEmpty();
        String first = firstArg.getValue();
        String second = line.arguments.size() < 2 ? "" : line.arguments.get(1).getValue();
        if (first.startsWith("#") || firstEmpty && second.startsWith("#")) {
            line.type = LineType.COMMENT_LINE;
            // prevLineType not updated
            return;
        }
        LineType lineType = determineLineTypeFromPrevious(firstEmpty);
        line.type = lineType;
        prevLineType = lineType;
    }

    private LineType tryParseTableSwitch(RFELine line) throws CoreException {
        ParsedString tableArgument = line.arguments.get(0);
        if (!tableArgument.getValue().startsWith("*")) {
            return null;
        }
        String table = ParserUtil.parseTable(tableArgument.getValue());
        LineType curType = tableNameToType.get(table);
        if (curType == null) {
            return LineType.IGNORE_TABLE;
        }
        return curType;
    }

    private static final Map<String, LineType> tableNameToType = new HashMap<String, LineType>();

    static {
        tableNameToType.put("setting", LineType.SETTING_TABLE_BEGIN);
        tableNameToType.put("settings", LineType.SETTING_TABLE_BEGIN);
        tableNameToType.put("metadata", LineType.SETTING_TABLE_BEGIN);
        tableNameToType.put("variable", LineType.VARIABLE_TABLE_BEGIN);
        tableNameToType.put("variables", LineType.VARIABLE_TABLE_BEGIN);
        tableNameToType.put("testcase", LineType.TESTCASE_TABLE_BEGIN);
        tableNameToType.put("testcases", LineType.TESTCASE_TABLE_BEGIN);
        tableNameToType.put("keyword", LineType.KEYWORD_TABLE_BEGIN);
        tableNameToType.put("keywords", LineType.KEYWORD_TABLE_BEGIN);
        tableNameToType.put("userkeyword", LineType.KEYWORD_TABLE_BEGIN);
        tableNameToType.put("userkeywords", LineType.KEYWORD_TABLE_BEGIN);
    }

    private boolean tryParseContinuationLine(RFELine line) throws CoreException {
        ParsedString arg = line.arguments.get(0);
        if (!arg.getValue().equals(CONTINUATION_STR)) {
            // first column not continuation, try second-column continuation
            if (!arg.isEmpty()) { // "  ..." or "\ ...", documentation does not
                                  // clearly state either, and both are
                                  // semantically valid
                // first column must be empty for second-column continuation
                return false;
            }
            if (line.arguments.size() < 2) {
                // must have two columns
                return false;
            }
            if (!line.arguments.get(1).getValue().equals(CONTINUATION_STR)) {
                // second column not continuation either
                return false;
            }
        }
        return true;
    }

    private LineType determineLineTypeFromPrevious(boolean firstEmpty) {
        switch (prevLineType) {
        case IGNORE:
            return LineType.IGNORE;
        case SETTING_TABLE_BEGIN:
        case SETTING_TABLE_LINE:
            return LineType.SETTING_TABLE_LINE;
        case VARIABLE_TABLE_BEGIN:
        case VARIABLE_TABLE_LINE:
            return LineType.VARIABLE_TABLE_LINE;
        case TESTCASE_TABLE_BEGIN:
        case TESTCASE_TABLE_IGNORE:
            return !firstEmpty ? LineType.TESTCASE_TABLE_TESTCASE_BEGIN : LineType.TESTCASE_TABLE_IGNORE;
        case TESTCASE_TABLE_TESTCASE_BEGIN:
        case TESTCASE_TABLE_TESTCASE_LINE:
            return !firstEmpty ? LineType.TESTCASE_TABLE_TESTCASE_BEGIN : LineType.TESTCASE_TABLE_TESTCASE_LINE;
        case KEYWORD_TABLE_BEGIN:
        case KEYWORD_TABLE_IGNORE:
            return !firstEmpty ? LineType.KEYWORD_TABLE_KEYWORD_BEGIN : LineType.KEYWORD_TABLE_IGNORE;
        case KEYWORD_TABLE_KEYWORD_BEGIN:
        case KEYWORD_TABLE_KEYWORD_LINE:
            return !firstEmpty ? LineType.KEYWORD_TABLE_KEYWORD_BEGIN : LineType.KEYWORD_TABLE_KEYWORD_LINE;
        case IGNORE_TABLE:
            return LineType.IGNORE;
        }
        throw new RuntimeException("Unhandled previous line type " + prevLineType);
    }

}
