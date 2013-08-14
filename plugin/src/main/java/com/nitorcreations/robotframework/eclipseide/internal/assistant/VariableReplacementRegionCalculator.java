/**
 * Copyright 2012-2013 Nitor Creations Oy
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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class VariableReplacementRegionCalculator implements IVariableReplacementRegionCalculator {
    @Override
    public IRegion calculate(ParsedString argument, int cursorOffsetInDocument) {
        String arg = argument.getValue();
        int cursorOffset = cursorOffsetInDocument - argument.getArgCharPos();
        if (cursorOffset == 0) {
            // at the beginning of the argument, nothing to replace
            return new Region(cursorOffsetInDocument, 0);
        }
        int startPos = getStartPos(arg, cursorOffset);
        if (startPos == -1) {
            // no variable in argument, nothing to replace
            return new Region(cursorOffsetInDocument, 0);
        }
        int startPosInDocument = argument.getArgCharPos() + startPos;
        if (!isCharAt(arg, startPos + 1, '{')) {
            if (cursorOffset == startPos + 1) {
                // "foo$<cursor is here>" or "foo$<cursor is here>bar" -> replace $ or @
                return new Region(startPosInDocument, 1);
            }
            // "foo$b<cursor is here>ar" -> nothing to replace
            return new Region(cursorOffsetInDocument, 0);
        }
        int closePos = arg.indexOf('}', startPos + 2);
        if (closePos != -1 && closePos < cursorOffset) {
            // "${foo}b<cursor is here>ar -> nothing to replace
            return new Region(cursorOffsetInDocument, 0);
        }
        int nextDollarPos = arg.indexOf('$', startPos + 2);
        int nextAtPos = arg.indexOf('@', startPos + 2);
        if (closePos == -1 && nextDollarPos == -1 && nextAtPos == -1) {
            // unclosed variable at the end of the argument, replace unclosed variable
            return new Region(startPosInDocument, argument.getArgEndCharPos() - startPosInDocument);
        }
        int endPos = getEndPos(closePos, nextDollarPos, nextAtPos);
        boolean cursorAtVariableStart = cursorOffset == startPos + 1 || cursorOffset == startPos + 2;
        boolean isClosedVariable = closePos == endPos - 1;
        if (cursorAtVariableStart && !isClosedVariable) {
            // "foo$<cursor is here>{bar" replace only ${ or @{
            return new Region(startPosInDocument, 2);
        }
        // replace variable (may be followed by other variables)
        return new Region(startPosInDocument, endPos - startPos);
    }

    private static int getStartPos(String arg, int cursorOffset) {
        int dollarPos = arg.lastIndexOf('$', cursorOffset - 1);
        int atPos = arg.lastIndexOf('@', cursorOffset - 1);
        return Math.max(dollarPos, atPos);
    }

    private static int getEndPos(int closePos, int nextDollarPos, int nextAtPos) {
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

    private static boolean isCharAt(String arg, int pos, char ch) {
        return arg.length() > pos && arg.charAt(pos) == ch;
    }
}
