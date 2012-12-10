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

public enum LineType {
    IGNORE(TableType.UNKNOWN), //
    IGNORE_TABLE(TableType.IGNORE), //
    SETTING_TABLE_BEGIN(TableType.SETTING), //
    SETTING_TABLE_LINE(TableType.SETTING), //
    VARIABLE_TABLE_BEGIN(TableType.VARIABLE), //
    VARIABLE_TABLE_LINE(TableType.VARIABLE), //
    TESTCASE_TABLE_BEGIN(TableType.TESTCASE), //
    TESTCASE_TABLE_IGNORE(TableType.TESTCASE), //
    TESTCASE_TABLE_TESTCASE_BEGIN(TableType.TESTCASE), //
    TESTCASE_TABLE_TESTCASE_LINE(TableType.TESTCASE), //
    KEYWORD_TABLE_BEGIN(TableType.KEYWORD), //
    KEYWORD_TABLE_IGNORE(TableType.KEYWORD), //
    KEYWORD_TABLE_KEYWORD_BEGIN(TableType.KEYWORD), //
    KEYWORD_TABLE_KEYWORD_LINE(TableType.KEYWORD), //
    CONTINUATION_LINE(TableType.UNKNOWN), //
    COMMENT_LINE(TableType.UNKNOWN), //
    ;
    public final TableType tableType;

    LineType(TableType tableType) {
        this.tableType = tableType;
    }
}
