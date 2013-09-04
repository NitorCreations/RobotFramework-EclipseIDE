/**
 * Copyright 2012-2013 Nitor Creations Oy
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

import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.COMMENT;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.IGNORED;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.KEYWORD_ARG;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.KEYWORD_CALL;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.KEYWORD_CALL_DYNAMIC;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.NEW_KEYWORD;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.NEW_TESTCASE;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_FILE;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_FILE_ARG;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_FILE_WITH_NAME_KEY;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_FILE_WITH_NAME_VALUE;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_KEY;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_VAL;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.TABLE;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.VARIABLE_KEY;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.VARIABLE_VAL;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

@RunWith(Enclosed.class)
public class TestArgumentPreParser {

    public static class Empty_file_parsing {
        @Test
        public void at_start_of_line() throws Exception {
            t("");
        }
    }

    public static class Comment_parsing {
        @Test
        public void at_start_of_line() throws Exception {
            t("*Settings\n#Comment", TABLE, COMMENT);
        }

        @Test
        public void in_the_middle_of_the_line() throws Exception {
            t("*Settings\nResource  foo.txt  # Comment", TABLE, SETTING_KEY, SETTING_FILE, COMMENT);
        }

        @Test
        public void extending_to_end_of_line() throws Exception {
            t("*Settings\nResource  foo.txt  # Comment  separated\tstuff", TABLE, SETTING_KEY, SETTING_FILE, COMMENT);
        }

        @Test
        public void line_continuation_not_part_of_comment() throws Exception {
            t("*Settings\nResource  # Comment\n...  foo.txt", TABLE, SETTING_KEY, COMMENT, IGNORED, SETTING_FILE);
        }
    }

    public static class Before_table_parsing {
        @Test
        public void ignore_text() throws Exception {
            t("Hello", IGNORED);
        }

        @Test
        public void ignore_line_continuation() throws Exception {
            t("...  continues", IGNORED, IGNORED);
            t("crap\n...  continues", IGNORED, IGNORED, IGNORED);
        }
    }

    public static class Table_parsing {
        @Test
        public void basic_table_declarations() throws Exception {
            t("*Settings", TABLE);
            t("*** Settings ***", TABLE);
        }

        @Test
        public void ignore_text_after_unknown_table() throws Exception {
            t("*Unknown  ignored", TABLE, IGNORED);
        }

        @Test
        public void ignore_text_after_known_table() throws Exception {
            t("*Settings  ignored", TABLE, IGNORED);
        }
    }

    public static class Unknown_table_parsing {
        @Test
        public void ignore_text() throws Exception {
            t("*Unknown\nHello", TABLE, IGNORED);
        }

        @Test
        public void ignore_line_continuation() throws Exception {
            t("*Unknown\n...  continues", TABLE, IGNORED, IGNORED);
            t("*Unkwnon\ncrap\n...  continues", TABLE, IGNORED, IGNORED, IGNORED);
        }
    }

    public static class Setting_table_parsing {
        @Test
        public void basic_settings() throws Exception {
            t("*Settings\nResource  foo.txt", TABLE, SETTING_KEY, SETTING_FILE);
            t("*Settings\nLibrary  OperatingSystem", TABLE, SETTING_KEY, SETTING_FILE);
        }

        @Test
        public void ignore_broken_line_continuation() throws Exception {
            t("*Settings\n...  continues", TABLE, IGNORED, IGNORED);
        }

        @Test
        public void line_continuation_works() throws Exception {
            t("*Settings\nResource\n...  foo.txt", TABLE, SETTING_KEY, IGNORED, SETTING_FILE);
        }

        @Test
        public void string_settings() throws Exception {
            for (String key : new String[] { "Force Tags", "Default Tags", "Test Timeout", "Documentation", "Metadata" }) {
                t("*Settings\n" + key + "  val", TABLE, SETTING_KEY, SETTING_VAL);
            }
        }

        @Test
        public void dynamickeyword_args_settings() throws Exception {
            for (String key : new String[] { "Test Setup", "Test Teardown", "Suite Setup", "Suite Teardown" }) {
                t("*Settings\n" + key + "  Keyword  arg", TABLE, SETTING_KEY, KEYWORD_CALL_DYNAMIC, KEYWORD_ARG);
            }
        }

        @Test
        public void keyword_args_settings() throws Exception {
            for (String key : new String[] { "Test Template" }) {
                t("*Settings\n" + key + "  Keyword  arg", TABLE, SETTING_KEY, KEYWORD_CALL, KEYWORD_ARG);
            }
        }

        @Test
        public void library_setting() throws Exception {
            t("*Settings\nLibrary  SeleniumLibrary", TABLE, SETTING_KEY, SETTING_FILE);
            t("*Settings\nLibrary  SeleniumLibrary  arg1  arg2", TABLE, SETTING_KEY, SETTING_FILE, SETTING_FILE_ARG, SETTING_FILE_ARG);
            t("*Settings\nLibrary  SeleniumLibrary  arg  WITH NAME  SelLib", TABLE, SETTING_KEY, SETTING_FILE, SETTING_FILE_ARG, SETTING_FILE_WITH_NAME_KEY, SETTING_FILE_WITH_NAME_VALUE);
        }

        @Test
        public void resource_setting() throws Exception {
            t("*Settings\nResource  resource.txt  ignoredarg", TABLE, SETTING_KEY, SETTING_FILE, IGNORED);
        }

        @Test
        public void variables_setting() throws Exception {
            t("*Settings\nVariables  foo.py  arg1  arg2", TABLE, SETTING_KEY, SETTING_FILE, SETTING_FILE_ARG, SETTING_FILE_ARG);
        }
    }

    public static class Variable_table_parsing {
        @Test
        public void basic_variables() throws Exception {
            t("*Variables\n${FOO}  bar", TABLE, VARIABLE_KEY, VARIABLE_VAL);
            t("*Variables\n${FOO}  bar  zot", TABLE, VARIABLE_KEY, VARIABLE_VAL, VARIABLE_VAL);
            t("*Variables\n@{FOO}  bar  zot", TABLE, VARIABLE_KEY, VARIABLE_VAL, VARIABLE_VAL);
        }

        @Test
        public void recursive_variables() throws Exception {
            t("*Variables\n${FOO}  ${BAR}", TABLE, VARIABLE_KEY, VARIABLE_VAL);
            t("*Variables\n${FOO}  x${BAR}y", TABLE, VARIABLE_KEY, VARIABLE_VAL);
        }
    }

    public static class Test_case_table_parsing {
        @Test
        public void basic_testcases_should_convert_cleanly() throws Exception {
            t("*Test cases\nTC1  Log  Hello", TABLE, NEW_TESTCASE, KEYWORD_CALL, KEYWORD_ARG);
            t("*Test cases\nTC1\n  Log  Hello", TABLE, NEW_TESTCASE, IGNORED, KEYWORD_CALL, KEYWORD_ARG);
        }

        @Test
        public void when_testcase_template_is_set_then_regular_lines_should_emit_KEYWORD_ARG_only() throws Exception {
            t("*Test cases\nTC1  [Template]  Log\n  Hello", TABLE, NEW_TESTCASE, SETTING_KEY, KEYWORD_CALL, IGNORED, KEYWORD_ARG);
            t("*Test cases\nTC1\n  Hello\n  [Template]  Log", TABLE, NEW_TESTCASE, IGNORED, KEYWORD_ARG, IGNORED, SETTING_KEY, KEYWORD_CALL);
        }

        @Test
        public void setup_and_teardown_settings() throws Exception {
            for (String key : new String[] { "[Setup]", "[Teardown]" }) {
                t("*Test cases\nTC1  " + key + "  Keyword", TABLE, NEW_TESTCASE, SETTING_KEY, KEYWORD_CALL_DYNAMIC);
            }
        }

        @Test
        public void when_global_template_is_set_then_regular_lines_should_emit_KEYWORD_ARG_only() throws Exception {
            t("*Settings\nTest Template  Log\n*Test cases\nTC1  Hello", TABLE, SETTING_KEY, KEYWORD_CALL, TABLE, NEW_TESTCASE, KEYWORD_ARG);
            t("*Test cases\nTC1\n  Hello\n*Settings\nTest Template  Log", TABLE, NEW_TESTCASE, IGNORED, KEYWORD_ARG, TABLE, SETTING_KEY, KEYWORD_CALL);
        }

        @Test
        public void global_template_is_set_but_local_template_cancels_it_should_emit_KEYWORD_CALL() throws Exception {
            t("*Settings\nTest Template  Log\n*Test cases\nTC1  [Template]  NONE\n  Hello", TABLE, SETTING_KEY, KEYWORD_CALL, TABLE, NEW_TESTCASE, SETTING_KEY, SETTING_VAL, IGNORED, KEYWORD_CALL);
            t("*Test cases\nTC1  [Template]  NONE\n  Hello\n*Settings\nTest Template  Log", TABLE, NEW_TESTCASE, SETTING_KEY, SETTING_VAL, IGNORED, KEYWORD_CALL, TABLE, SETTING_KEY, KEYWORD_CALL);
        }

        @Test
        public void ignore_broken_line_continuation() throws Exception {
            t("*Test cases\n...  continues", TABLE, IGNORED, IGNORED);
        }

        @Test
        public void line_continuation_works() throws Exception {
            t("*Test cases\nTC1\n  Keyword\n  ...  argument", TABLE, NEW_TESTCASE, IGNORED, KEYWORD_CALL, IGNORED, IGNORED, KEYWORD_ARG);
            t("*Test cases\nTC1\n  [Documentation]\n  ...  text\n  ...  text2", TABLE, NEW_TESTCASE, IGNORED, SETTING_KEY, IGNORED, IGNORED, SETTING_VAL, IGNORED, IGNORED, SETTING_VAL);
            t("*Test cases\nTC1\n  [Documentation]\n  ...  text\n#comment\n  ...  text2", TABLE, NEW_TESTCASE, IGNORED, SETTING_KEY, IGNORED, IGNORED, SETTING_VAL, COMMENT, IGNORED, IGNORED, SETTING_VAL);
        }

        @Test
        public void whitespace_treatment() throws Exception {
            t("*Test cases\n", TABLE);
            t("*Test cases\n  ", TABLE);
            t("*Test cases\n  \n", TABLE);
        }

        @Test
        public void local_template_is_not_detected_from_next_testcase() throws Exception {
            t("*Test cases\nTC1\n  Keyword\nTC2\n  [Template]  Log", TABLE, NEW_TESTCASE, IGNORED, KEYWORD_CALL, NEW_TESTCASE, IGNORED, SETTING_KEY, KEYWORD_CALL);
        }

        @Test
        public void local_template_is_still_detected_when_empty_lines_in_front() throws Exception {
            t("*Test cases\nTC1\n  Keyword\n\n  [Template]  Log", TABLE, NEW_TESTCASE, IGNORED, KEYWORD_ARG, IGNORED, SETTING_KEY, KEYWORD_CALL);
        }
    }

    public static class Keyword_table_parsing {
        @Test
        public void basic_keywords_should_convert_cleanly() throws Exception {
            t("*Keywords\nKW1  Log  Hello", TABLE, NEW_KEYWORD, KEYWORD_CALL, KEYWORD_ARG);
            t("*Keywords\nKW1\n  Log  Hello", TABLE, NEW_KEYWORD, IGNORED, KEYWORD_CALL, KEYWORD_ARG);
        }

        @Test
        public void when_bogous_keyword_template_is_set_then_regular_lines_should_still_emit_KEYWORD_CALL() throws Exception {
            t("*Keywords\nKW1  [Template]  Log\n  Hello", TABLE, NEW_KEYWORD, SETTING_KEY, KEYWORD_CALL, IGNORED, KEYWORD_CALL);
            t("*Keywords\nKW1\n  Hello\n  [Template]  Log", TABLE, NEW_KEYWORD, IGNORED, KEYWORD_CALL, IGNORED, SETTING_KEY, KEYWORD_CALL);
        }

        @Test
        public void when_global_template_is_set_then_regular_lines_should_still_emit_KEYWORD_CALL() throws Exception {
            t("*Settings\nTest Template  Log\n*Keywords\nKW1  Hello", TABLE, SETTING_KEY, KEYWORD_CALL, TABLE, NEW_KEYWORD, KEYWORD_CALL);
            t("*Keywords\nKW1  Hello\n*Settings\nTest Template  Log", TABLE, NEW_KEYWORD, KEYWORD_CALL, TABLE, SETTING_KEY, KEYWORD_CALL);
        }

        @Test
        public void ignore_broken_line_continuation() throws Exception {
            t("*Keywords\n...  continues", TABLE, IGNORED, IGNORED);
        }

        @Test
        public void line_continuation_works() throws Exception {
            t("*Keywords\nKW1\n  Keyword\n  ...  argument", TABLE, NEW_KEYWORD, IGNORED, KEYWORD_CALL, IGNORED, IGNORED, KEYWORD_ARG);
            t("*Keywords\nKW1\n  [Arguments]\n  ...  arg1\n  ...  arg2", TABLE, NEW_KEYWORD, IGNORED, SETTING_KEY, IGNORED, IGNORED, SETTING_VAL, IGNORED, IGNORED, SETTING_VAL);
            t("*Keywords\nKW1\n  [Arguments]\n  #comment\n  ...  arg1\n  ...  arg2", TABLE, NEW_KEYWORD, IGNORED, SETTING_KEY, IGNORED, COMMENT, IGNORED, IGNORED, SETTING_VAL, IGNORED, IGNORED, SETTING_VAL);
        }

        @Test
        public void recursive_keyword_calls() throws Exception {
            t("*Keywords\nKW1\n  Run Keyword  Test", TABLE, NEW_KEYWORD, IGNORED, KEYWORD_CALL, KEYWORD_CALL_DYNAMIC);
            t("*Keywords\nKW1\n  Run Keyword If  1 < 2  Run Keyword  Log  Hello", TABLE, NEW_KEYWORD, IGNORED, KEYWORD_CALL, KEYWORD_ARG, KEYWORD_CALL_DYNAMIC, KEYWORD_CALL_DYNAMIC, KEYWORD_ARG);
        }

        @Test
        public void template_settings_are_ignored_in_keywords() throws Exception {
            t("*Keywords\nKW1\n  [Template]  Run Keyword\n  Test", TABLE, NEW_KEYWORD, IGNORED, SETTING_KEY, KEYWORD_CALL, IGNORED, KEYWORD_CALL);
            t("*Settings\nTest Template  Run Keyword\n*Keywords\nKW1\n  Test", TABLE, SETTING_KEY, KEYWORD_CALL, TABLE, NEW_KEYWORD, IGNORED, KEYWORD_CALL);
        }

        @Test
        public void recursive_keyword_calls_by_local_template_setting() throws Exception {
            t("*Test Cases\nTC1\n  [Template]  Run Keyword\n  Test", TABLE, NEW_TESTCASE, IGNORED, SETTING_KEY, KEYWORD_CALL, IGNORED, KEYWORD_CALL_DYNAMIC);
        }

        @Test
        public void recursive_keyword_calls_by_global_template_setting() throws Exception {
            t("*Settings\nTest Template  Run Keyword\n*Test Cases\nTC1\n  Test", TABLE, SETTING_KEY, KEYWORD_CALL, TABLE, NEW_KEYWORD, IGNORED, KEYWORD_CALL_DYNAMIC);
        }
    }

    static List<RobotLine> s(String input, ArgumentType... expected) throws AssertionFailedError {
        List<RobotLine> lines = RobotFile.parse(input).getLines();
        // for (int i = 0; i < 5; ++i) {
        // System.out.println();
        // }
        // System.out.println("------------------------------\n" + input + "\n----------------\n" + lines);
        assertCorrectLines(lines, expected);
        return lines;
    }

    static int c, ct, n;

    static void t(String input, ArgumentType... expected) throws Exception {
        c = 1;
        List<RobotLine> lines = s(input, expected);
        tRecurse(lines);
        ct += c;
        ++n;
        System.out.println(c + " mutations, " + ct + " total, " + (ct / (double) n) + " average");
    }

    private static void tRecurse(List<RobotLine> lines) throws AssertionFailedError {
        tRecurse(lines, 0, 0);
    }

    private static void tRecurse(List<RobotLine> lines, int startLine, int startArg) throws AssertionFailedError {
        ++c;
        try {
            for (int i = startLine; i < lines.size(); i++) {
                RobotLine robotLine = lines.get(i);
                final int nArgs = robotLine.arguments.size();
                final int contInd = determineContinuationIndex(lines, i);
                int arg = 1;
                if (nArgs > 0 && robotLine.arguments.get(0).getType() == ArgumentType.IGNORED) {
                    arg = 2;
                }
                if (startArg > 0) {
                    arg = Math.max(arg, startArg);
                    startArg = 0;
                }
                for (; arg < nArgs; ++arg) {
                    if (robotLine.arguments.get(arg).getType() == ArgumentType.IGNORED) {
                        continue;
                    }
                    RobotLine line1a = splice(robotLine, arg, nArgs - arg);
                    RobotLine line1b = splice(robotLine, arg - 1, nArgs - arg + 1, new ParsedString(robotLine.arguments.get(arg - 1).getValue() + "  ", 0).setType(robotLine.arguments.get(arg - 1).getType()));
                    // RobotLine line1b = splice(robotLine, arg, nArgs - arg, new ParsedString("",
                    // 0).setType(robotLine.arguments.get(arg).getType()));
                    RobotLine line2 = contInd == 1 ? splice(robotLine, 0, arg, new ParsedString("...", 0)) : splice(robotLine, 0, arg, new ParsedString("", 0), new ParsedString("...", 0));
                    List<RobotLine> lines2a = splice(lines, robotLine, line1a, line2);
                    List<RobotLine> lines2b = splice(lines, robotLine, line1b, line2);
                    t2(lines2a);
                    tRecurse(lines2a, i + 1, contInd + 1);
                    t2(lines2b);
                    tRecurse(lines2b, i + 1, contInd + 1);
                }
            }
        } catch (Throwable t) {
            throw wrapError(lines, new StringBuilder("Original spec:"), t);
        }
    }

    private static void t2(List<RobotLine> inputLines) {
        List<ArgumentType> expected = new ArrayList<ArgumentType>();
        StringBuilder input = new StringBuilder();
        for (RobotLine line : inputLines) {
            boolean firstArg = true;
            for (ParsedString arg : line.arguments) {
                expected.add(arg.getType());
                if (firstArg) {
                    firstArg = false;
                } else {
                    input.append("  ");
                }
                input.append(arg.getValue());
            }
            input.append('\n');
        }
        ArgumentType[] expectedArr = new ArgumentType[expected.size()];
        expected.toArray(expectedArr);

        String newInput = input.toString();
        List<RobotLine> parsedLines = RobotFile.parse(newInput).getLines();
        // System.out.println();
        // System.out.println("------------------------------\n" + newInput + "\n----------------\n" + parsedLines);
        assertCorrectLines(parsedLines, expectedArr);
    }

    private static List<RobotLine> splice(List<RobotLine> lines, RobotLine oldLine, RobotLine... newLine) {
        List<RobotLine> lines2 = new ArrayList<RobotLine>(lines);
        int pos = lines2.indexOf(oldLine);
        lines2.remove(pos);
        lines2.addAll(pos, Arrays.asList(newLine));
        return lines2;
    }

    private static RobotLine splice(RobotLine oldLine, int pos, int removeCount, ParsedString... newArgs) {
        RobotLine newLine = new RobotLine(oldLine.lineNo, oldLine.lineCharPos, new ArrayList<ParsedString>(oldLine.arguments));
        newLine.type = oldLine.type;
        for (int i = 0; i < removeCount; ++i) {
            newLine.arguments.remove(pos);
        }
        newLine.arguments.addAll(pos, Arrays.asList(newArgs));
        return newLine;
    }

    private static int determineContinuationIndex(List<RobotLine> lines, int i) {
        for (; i >= 0; --i) {
            RobotLine robotLine = lines.get(i);
            switch (robotLine.type) {
                case CONTINUATION_LINE:
                    continue;
                case KEYWORD_TABLE_KEYWORD_BEGIN:
                case KEYWORD_TABLE_KEYWORD_LINE:
                case TESTCASE_TABLE_TESTCASE_BEGIN:
                case TESTCASE_TABLE_TESTCASE_LINE:
                    return 2;
                default:
                    return 1;
            }
        }
        return 1;
    }

    private static void assertCorrectLines(List<RobotLine> lines, ArgumentType... expected) throws AssertionFailedError {
        RobotLine lastRfeLine = null;
        try {
            int p = 0;
            for (RobotLine rfeLine : lines) {
                lastRfeLine = rfeLine;
                for (ParsedString arg : rfeLine.arguments) {
                    if (p == expected.length) {
                        fail("Got more arguments than expected, first extra argument: " + arg);
                    }
                    assertEquals("Argument type mismatch for argument " + arg, expected[p++], arg.getType());
                }
            }
            if (p != expected.length) {
                fail("Got less arguments than expected, first missing argument: " + expected[p]);
            }
        } catch (Throwable e) {
            throw wrapErrorOnLine(lines, lastRfeLine, e, expected);
        }
    }

    private static AssertionFailedError wrapErrorOnLine(List<RobotLine> lines, RobotLine lastRfeLine, Throwable e, ArgumentType... expected) {
        StringBuilder descr = new StringBuilder();
        descr.append("Error on line #").append(lastRfeLine != null ? lastRfeLine.lineNo : -1);
        descr.append(": Expected types ").append(Arrays.toString(expected)).append(" but got:");
        return wrapError(lines, descr, e);
    }

    private static AssertionFailedError wrapError(List<RobotLine> lines, StringBuilder descr, Throwable e) {
        for (RobotLine rfeLine2 : lines) {
            descr.append('\n').append(rfeLine2);
        }
        AssertionFailedError afe = new AssertionFailedError(descr.toString());
        afe.initCause(e);
        return afe;
    }
}
