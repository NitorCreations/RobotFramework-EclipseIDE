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
package com.nitorcreations.robotframework.eclipseide.internal.rules;

public class RobotWhitespace {

    public static boolean isWhitespace(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
    }

    public static int skipMinimumRobotWhitespace(String line, int startPos) {
        boolean gotOne = false;
        int i;
        for (i = startPos; i < line.length(); ++i) {
            char ch = line.charAt(i);
            if (!RobotWhitespace.isWhitespace(ch)) {
                // I don't think this should ever happen
                return line.length() + 1;
            }
            if (ch == '\t' || gotOne) {
                return i + 1;
            }
            gotOne = true;
        }
        return i;
    }

}
