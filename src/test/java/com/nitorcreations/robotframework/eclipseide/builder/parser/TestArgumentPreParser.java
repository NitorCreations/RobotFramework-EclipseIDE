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

import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.COMMENT;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.IGNORED;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.KEYWORD_ARG;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.KEYWORD_CALL;
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
        public void keyword_args_settings() throws Exception {
            for (String key : new String[] { "Test Setup", "Test Teardown", "Test Template", "Suite Setup", "Suite Teardown" }) {
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
        public void when_global_template_is_set_then_regular_lines_should_emit_KEYWORD_ARG_only() throws Exception {
            t("*Settings\nTest Template  Log\n*Test cases\nTC1  Hello", TABLE, SETTING_KEY, KEYWORD_CALL, TABLE, NEW_TESTCASE, KEYWORD_ARG);
            t("*Test cases\nTC1\n  Hello\n*Settings\nTest Template  Log", TABLE, NEW_TESTCASE, IGNORED, KEYWORD_ARG, TABLE, SETTING_KEY, KEYWORD_CALL);
        }

        @Test
        public void global_template_is_set_but_local_template_cancels_it_should_emit_KEYWORD_CALL() throws Exception {
            t("*Settings\nTest Template  Log\n*Test cases\nTC1  [Template]  NONE\n  Hello", TABLE, SETTING_KEY, KEYWORD_CALL, TABLE, NEW_TESTCASE, SETTING_KEY, KEYWORD_CALL, IGNORED, KEYWORD_CALL);
            t("*Test cases\nTC1  [Template]  NONE\n  Hello\n*Settings\nTest Template  Log", TABLE, NEW_TESTCASE, SETTING_KEY, KEYWORD_CALL, IGNORED, KEYWORD_CALL, TABLE, SETTING_KEY, KEYWORD_CALL);
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
    }

    static void t(String input, ArgumentType... expected) throws Exception {
        List<RFELine> lines = RobotFile.getLines(input).getLines();
        int p = 0;
        for (RFELine rfeLine : lines) {
            for (ParsedString arg : rfeLine.arguments) {
                if (arg.getType() != expected[p++]) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Error on line #" + rfeLine.lineNo + ": Expected types ").append(Arrays.toString(expected)).append(" but got:");
                    for (RFELine rfeLine2 : lines) {
                        sb.append('\n').append(rfeLine2);
                    }
                    throw new AssertionFailedError(sb.toString());
                }
            }
        }
        if (p != expected.length) {
            throw new ArrayIndexOutOfBoundsException(p);
        }
    }
}
