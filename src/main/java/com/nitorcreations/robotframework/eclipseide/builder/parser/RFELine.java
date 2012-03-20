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

import java.util.List;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class RFELine {
    public final int lineNo;
    public final int lineCharPos;
    public final List<ParsedString> arguments;
    public LineType type;

    public enum LineType {
        IGNORE, IGNORE_TABLE, SETTING_TABLE_BEGIN, SETTING_TABLE_LINE, VARIABLE_TABLE_BEGIN, VARIABLE_TABLE_LINE, TESTCASE_TABLE_BEGIN, TESTCASE_TABLE_IGNORE, TESTCASE_TABLE_TESTCASE_BEGIN, TESTCASE_TABLE_TESTCASE_LINE, KEYWORD_TABLE_BEGIN, KEYWORD_TABLE_IGNORE, KEYWORD_TABLE_KEYWORD_BEGIN, KEYWORD_TABLE_KEYWORD_LINE, CONTINUATION_LINE, COMMENT_LINE,
    }

    public RFELine(int lineNo, int lineCharPos, List<ParsedString> arguments) {
        this.lineNo = lineNo;
        this.lineCharPos = lineCharPos;
        this.arguments = arguments;
    }

    public boolean isType(LineType type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        return "<#" + lineNo + " " + type + "> " + arguments;
    }

    public ParsedString getArgumentAt(int offset) {
        for (ParsedString argument : arguments) {
            if (offset >= argument.getArgCharPos() && offset <= argument.getArgEndCharPos()) {
                return argument;
            }
        }
        return null;
    }
}
