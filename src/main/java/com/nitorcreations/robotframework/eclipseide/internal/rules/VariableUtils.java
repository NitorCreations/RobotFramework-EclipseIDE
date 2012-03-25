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

public final class VariableUtils {

    /**
     * Finds the start of the next variable, when scanning from the offset
     * given. The name of the variable must be at least one character for it to
     * be detected as a link.
     * 
     * @param line
     *            the line to scan
     * @param start
     *            the position to start scanning on
     * @return the position at which the next variable starts, or -1 if no more
     *         variables are found.
     */
    public static int findNextVariableStart(String line, int start) {
        for (; start < line.length() - 3; ++start) {
            if (line.charAt(start) == '$' && line.charAt(start + 1) == '{') {
                return start;
            }
            if (line.charAt(start) == '#') {
                return -1;
            }
            if (line.charAt(start) == '\\') {
                // skip next character, possibly $
                ++start;
            }
        }
        return -1;
    }

    /**
     * Calculate the length of the variable starting at the given position, or
     * -1 if the variable does not end with }.
     * 
     * @param line
     *            the line to scan
     * @param start
     *            the start position of the variable
     * @return the number of characters in the variable
     */
    public static int calculateVariableLength(String line, int start) {
        final int origStart = start;
        for (; start < line.length(); ++start) {
            if (line.charAt(start) == '}') {
                return start - origStart + 1;
            }
            if (line.charAt(start) == '\\') {
                // skip next character, possibly }
                ++start;
            }
        }
        // if we get here, variable did not end with }
        return -1;
    }

}
