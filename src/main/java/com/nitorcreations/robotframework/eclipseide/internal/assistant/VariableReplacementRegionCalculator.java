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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import org.eclipse.jface.text.Region;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class VariableReplacementRegionCalculator {
    public static Region calculate(ParsedString argument, int documentOffset) {
        String arg = argument.getValue();
        int argOffset = documentOffset - argument.getArgCharPos();
        // System.out.println("== '" + arg + "' @ " + argOffset);
        if (argOffset == 0) {
            // System.out.println("@@ start");
            return new Region(documentOffset, 0);
        }
        int dollarPos = arg.lastIndexOf('$', argOffset - 1);
        int atPos = arg.lastIndexOf('@', argOffset - 1);
        int startPos = Math.max(dollarPos, atPos);
        if (startPos == -1) {
            // System.out.println("@@ no $/@");
            return new Region(documentOffset, 0);
        }
        // System.out.println("== startPos " + startPos);
        if (!condCharAt(arg, startPos + 1, '{')) {
            if (argOffset == startPos + 1) {
                // include $ or @
                // System.out.println("@@ at $/@");
                return new Region(argument.getArgCharPos() + startPos, 1);
            }
            // System.out.println("@@ after $/@");
            return new Region(documentOffset, 0);
        }
        int closePos = arg.indexOf('}', startPos + 2);
        if (closePos == -1) {
            if (argOffset == startPos + 1 || argOffset == startPos + 2) {
                // include ${ or @{
                // System.out.println("@@ at ${/@{");
                return new Region(argument.getArgCharPos() + startPos, 2);
            }
            // System.out.println("@@ after ${/@{");
            return new Region(documentOffset, 0);
        }
        if (closePos < argOffset) {
            // System.out.println("@@ after");
            return new Region(documentOffset, 0);
        }
        // System.out.println("@@ inside");
        return new Region(argument.getArgCharPos() + startPos, closePos - startPos + 1);
    }

    static boolean condCharAt(String arg, int pos, char ch) {
        return arg.length() > pos && arg.charAt(pos) == ch;
    }

}
