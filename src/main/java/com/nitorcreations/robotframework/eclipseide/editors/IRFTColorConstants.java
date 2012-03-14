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
package com.nitorcreations.robotframework.eclipseide.editors;

import org.eclipse.swt.graphics.RGB;

public enum IRFTColorConstants {
    COMMENT(new RGB(128, 128, 128)), //
    TABLE(new RGB(192, 0, 192)), //
    SETTING(new RGB(0, 192, 0)), //
    SETTING_VALUE(new RGB(0, 255, 0)), //
    SETTING_FILE(new RGB(0, 0, 0), new RGB(255, 255, 255)), //
    SETTING_FILE_ARG(new RGB(0, 255, 64)), //
    VARIABLE(new RGB(0, 170, 180)), //
    VARIABLE_VALUE(new RGB(140, 152, 162), new RGB(180, 192, 202)), //
    TESTCASE_NEW(new RGB(222, 0, 0)), //
    KEYWORD_NEW(new RGB(128, 128, 50), new RGB(255, 255, 50)), //
    KEYWORD_LVALUE(new RGB(255, 0, 180)), //
    KEYWORD(new RGB(255, 180, 0)), //
    KEYWORD_ARG(new RGB(255, 100, 0)), //
    FOR_PART(new RGB(0, 255, 255)), //
    DEFAULT(new RGB(192, 192, 192)), //
    UNKNOWN(new RGB(255, 140, 0)), //
    FIELD_BG(new RGB(8, 16, 24)), //
    FG(new RGB(210, 210, 210)), //
    BG(new RGB(16, 32, 48)), //
    FG_SELECTION(new RGB(255, 255, 255)), //
    BG_SELECTION(new RGB(56, 83, 104)), //
    ARGUMENT_SEPARATOR(new RGB(16, 32, 48));

    private final RGB lightSchemeColor;
    private final RGB darkSchemeColor;

    private IRFTColorConstants(RGB color) {
        this(color, color);
    }

    private IRFTColorConstants(RGB lightSchemeColor, RGB darkSchemeColor) {
        this.lightSchemeColor = lightSchemeColor;
        this.darkSchemeColor = darkSchemeColor;
    }

    public RGB getColor(boolean isDarkScheme) {
        if (isDarkScheme) {
            return darkSchemeColor;
        }
        return lightSchemeColor;
    }
}
