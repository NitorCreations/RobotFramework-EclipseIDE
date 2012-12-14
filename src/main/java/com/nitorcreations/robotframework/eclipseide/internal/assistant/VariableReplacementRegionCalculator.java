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
    public static Region calculate(ParsedString argument, int cursorOffsetInDocument) {
        String arg = argument.getValue();
        int cursorOffsetInArgument = cursorOffsetInDocument - argument.getArgCharPos();
        // System.out.println("== '" + arg + "' @ " + cursorOffsetInArgument);
        if (cursorOffsetInArgument == 0) {
            // System.out.println("@@ start");
            return new Region(cursorOffsetInDocument, 0);
        }
        int dollarPos = arg.lastIndexOf('$', cursorOffsetInArgument - 1);
        int atPos = arg.lastIndexOf('@', cursorOffsetInArgument - 1);
        int startPosInArgument = Math.max(dollarPos, atPos);
        int startPosInDocument = argument.getArgCharPos() + startPosInArgument;
        if (startPosInArgument == -1) {
            // System.out.println("@@ no $/@");
            return new Region(cursorOffsetInDocument, 0);
        }
        // System.out.println("== startPos " + startPosInArgument);
        if (!condCharAt(arg, startPosInArgument + 1, '{')) {
            if (cursorOffsetInArgument == startPosInArgument + 1) {
                // include $ or @
                // System.out.println("@@ at $/@");
                return new Region(startPosInDocument, 1);
            }
            // System.out.println("@@ after $/@");
            return new Region(cursorOffsetInDocument, 0);
        }
        int closePos = arg.indexOf('}', startPosInArgument + 2);
        int nextDollarPos = arg.indexOf('$', startPosInArgument + 2);
        int nextAtPos = arg.indexOf('@', startPosInArgument + 2);
        if (closePos != -1 && closePos < cursorOffsetInArgument) {
            // System.out.println("@@ after");
            return new Region(cursorOffsetInDocument, 0);
        }
        if (closePos == -1 && nextDollarPos == -1 && nextAtPos == -1) {
            // System.out.println("@@ after ${/@{");
            return new Region(startPosInDocument, argument.getArgEndCharPos() - startPosInDocument);
        }
        int nextStartPosInArgument = getNextStartPosInArgument(closePos, nextDollarPos, nextAtPos);
        boolean cursorAtVariableStart = cursorOffsetInArgument == startPosInArgument + 1 || cursorOffsetInArgument == startPosInArgument + 2;
        boolean insidePartialVariable = closePos != nextStartPosInArgument - 1;
        if (cursorAtVariableStart && insidePartialVariable) {
            // replace only ${ or @{
            // System.out.println("@@ at ${/@{");
            return new Region(startPosInDocument, 2);
        }
        // replace complete variable
        // System.out.println("@@ inside");
        return new Region(startPosInDocument, nextStartPosInArgument - startPosInArgument);
    }

    private static int getNextStartPosInArgument(int closePos, int nextDollarPos, int nextAtPos) {
        int minPositive = minPositive(closePos, minPositive(nextDollarPos, nextAtPos));
        if (closePos == minPositive) {
            return closePos + 1;
        }
        return minPositive;
    }

    private static int minPositive(int a, int b) {
        if (a < 0) {
            return b;
        }
        if (b < 0) {
            return a;
        }
        return Math.min(a, b);
    }

    private static boolean condCharAt(String arg, int pos, char ch) {
        return arg.length() > pos && arg.charAt(pos) == ch;
    }
}
