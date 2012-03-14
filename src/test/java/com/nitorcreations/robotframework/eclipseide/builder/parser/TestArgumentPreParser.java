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

import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.IGNORED;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.KEYWORD_ARG;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.KEYWORD_CALL;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.NEW_TESTCASE;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_FILE;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.SETTING_KEY;
import static com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType.TABLE;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

@RunWith(Enclosed.class)
public class TestArgumentPreParser {

    public static class Setting_table_parsing {
        @Test
        public void basic_settings() throws Exception {
            t("*Settings\nResource  foo.txt", TABLE, SETTING_KEY, SETTING_FILE);
            t("*Settings\nLibrary  OperatingSystem", TABLE, SETTING_KEY, SETTING_FILE);
        }
    }

    public static class Test_case_parsing {
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
    }

    static void t(String input, ArgumentType... expected) throws Exception {
        List<RFELine> lines = parse(input);
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

    private static List<RFELine> parse(String fileContents) throws UnsupportedEncodingException, FileNotFoundException, CoreException {
        RFELexer lexer = new RFELexer(fileContents);
        List<RFELine> lines = lexer.lex();
        new RFEPreParser(null, lines).preParse();
        ArgumentPreParser app = new ArgumentPreParser();
        app.setRange(lines);
        app.parseAll();
        return lines;
    }

}
