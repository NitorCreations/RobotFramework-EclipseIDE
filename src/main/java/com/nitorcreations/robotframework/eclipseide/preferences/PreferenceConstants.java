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
package com.nitorcreations.robotframework.eclipseide.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

    // public static final String P_PATH = "pathPreference";
    //
    // public static final String P_BOOLEAN = "booleanPreference";
    //
    // public static final String P_CHOICE = "choicePreference";
    //
    // public static final String P_STRING = "stringPreference";
    //
    // public static final String P_COLOR = "colorPreference";

    private static final String base = "com.nitorcreations.robotframework.eclipseide.preferences.";
    private static final String baseSyntaxColoring = base + "syntaxcoloring.";

    public static final String P_COMMENT = baseSyntaxColoring + "comment";
    public static final String P_TABLE = baseSyntaxColoring + "table";
    public static final String P_SETTING = baseSyntaxColoring + "setting";
    public static final String P_SETTING_VALUE = baseSyntaxColoring + "setting_value";
    public static final String P_SETTING_FILE = baseSyntaxColoring + "setting_file";
    public static final String P_SETTING_FILE_ARG = baseSyntaxColoring + "setting_file_arg";
    public static final String P_VARIABLE = baseSyntaxColoring + "variable";
    public static final String P_VARIABLE_VALUE = baseSyntaxColoring + "variable_value";
    public static final String P_TESTCASE_NEW = baseSyntaxColoring + "testcase_new";
    public static final String P_KEYWORD_NEW = baseSyntaxColoring + "keyword_new";
    public static final String P_KEYWORD_LVALUE = baseSyntaxColoring + "keyword_lvalue";
    public static final String P_KEYWORD = baseSyntaxColoring + "keyword";
    public static final String P_KEYWORD_ARG = baseSyntaxColoring + "keyword_arg";
    public static final String P_FOR_PART = baseSyntaxColoring + "for_part";
    public static final String P_DEFAULT = baseSyntaxColoring + "default";
    public static final String P_UNKNOWN = baseSyntaxColoring + "unknown";
    public static final String P_FIELD_BG = baseSyntaxColoring + "field_bg";
    public static final String P_FG = baseSyntaxColoring + "fg";
    public static final String P_BG = baseSyntaxColoring + "bg";
    public static final String P_FG_SELECTION = baseSyntaxColoring + "fg_selection";
    public static final String P_BG_SELECTION = baseSyntaxColoring + "bg_selection";
    public static final String P_ARGUMENT_SEPARATOR = baseSyntaxColoring + "argument_separator";
}
