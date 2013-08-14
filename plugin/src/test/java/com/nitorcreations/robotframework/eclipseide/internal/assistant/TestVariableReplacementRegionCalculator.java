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

import static junit.framework.Assert.assertEquals;

import org.eclipse.jface.text.IRegion;
import org.junit.Test;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class TestVariableReplacementRegionCalculator {

    private static final int ARG_START_POS = 42;
    private static final ParsedString ARGUMENT = new ParsedString("foo${foo}hello@{list}${bar${hello", ARG_START_POS);
    private static final ParsedString ARGUMENT_2 = new ParsedString("${foo}hello", ARG_START_POS);
    private static final ParsedString PARTIAL_VARIABLE_AT_THE_END = new ParsedString("${foo}${bar", ARG_START_POS);
    private static final ParsedString COMPLETE_VARIABLE_AT_THE_END = new ParsedString("${foo}${bar}", ARG_START_POS);

    private static final int COMPLETE_VARIABLE_LENGTH = 6;
    private static final int COMPLETE_VARIABLE_START_POS = ARG_START_POS + 3;
    private static final int INSIDE_COMPLETE_VARIABLE_POS = COMPLETE_VARIABLE_START_POS + 3;

    private static final int PARTIAL_VARIABLE_LENGTH = 5;
    private static final int PARTIAL_VARIABLE_START_POS = ARG_START_POS + 21;
    private static final int INSIDE_PARTIAL_VARIABLE_POS = PARTIAL_VARIABLE_START_POS + 3;

    private static final int COMPLETE_VARIABLE_AT_THE_END_LENGTH = 6;
    private static final int COMPLETE_VARIABLE_AT_THE_END_START_POS = ARG_START_POS + 6;
    private static final int INSIDE_COMPLETE_VARIABLE_AT_THE_END_POS = COMPLETE_VARIABLE_AT_THE_END_START_POS + 3;

    private static final int PARTIAL_VARIABLE_AT_THE_END_LENGTH = 5;
    private static final int PARTIAL_VARIABLE_AT_THE_END_START_POS = ARG_START_POS + 6;
    private static final int INSIDE_PARTIAL_VARIABLE_AT_THE_END_POS = PARTIAL_VARIABLE_AT_THE_END_START_POS + 3;

    private static final int AFTER_CLOSED_VARIABLE_POS = ARG_START_POS + 8;

    private static final VariableReplacementRegionCalculator c = new VariableReplacementRegionCalculator();

    @Test
    public void partialVariableIsReplacedWhenCursonIsInsidePartialVariable() {
        IRegion region = c.calculate(ARGUMENT, INSIDE_PARTIAL_VARIABLE_POS);
        assertEquals(PARTIAL_VARIABLE_START_POS, region.getOffset());
        assertEquals(PARTIAL_VARIABLE_LENGTH, region.getLength());
    }

    // #43
    @Test
    public void partialVariableIsReplacedWhenCursonIsAfterPartialVariable() {
        IRegion region = c.calculate(ARGUMENT, PARTIAL_VARIABLE_START_POS + PARTIAL_VARIABLE_LENGTH);
        assertEquals(PARTIAL_VARIABLE_START_POS, region.getOffset());
        assertEquals(PARTIAL_VARIABLE_LENGTH, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenCursorIsBeforePartialVariable() {
        IRegion region = c.calculate(ARGUMENT, PARTIAL_VARIABLE_START_POS);
        assertEquals(PARTIAL_VARIABLE_START_POS, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void partialVariableAtTheEndIsReplacedWhenCursonIsInsidePartialVariable() {
        IRegion region = c.calculate(PARTIAL_VARIABLE_AT_THE_END, INSIDE_PARTIAL_VARIABLE_AT_THE_END_POS);
        assertEquals(PARTIAL_VARIABLE_AT_THE_END_START_POS, region.getOffset());
        assertEquals(PARTIAL_VARIABLE_AT_THE_END_LENGTH, region.getLength());
    }

    // #43
    @Test
    public void partialVariableAtTheEndIsReplacedWhenCursonIsAfterPartialVariable() {
        IRegion region = c.calculate(PARTIAL_VARIABLE_AT_THE_END, PARTIAL_VARIABLE_AT_THE_END_START_POS + PARTIAL_VARIABLE_AT_THE_END_LENGTH);
        assertEquals(PARTIAL_VARIABLE_AT_THE_END_START_POS, region.getOffset());
        assertEquals(PARTIAL_VARIABLE_AT_THE_END_LENGTH, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenCursorIsBeforePartialVariableAtTheEnd() {
        IRegion region = c.calculate(PARTIAL_VARIABLE_AT_THE_END, PARTIAL_VARIABLE_AT_THE_END_START_POS);
        assertEquals(PARTIAL_VARIABLE_AT_THE_END_START_POS, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void completeVariableAtTheEndIsReplacedWhenCursonIsInsideCompleteVariable() {
        IRegion region = c.calculate(COMPLETE_VARIABLE_AT_THE_END, INSIDE_COMPLETE_VARIABLE_AT_THE_END_POS);
        assertEquals(COMPLETE_VARIABLE_AT_THE_END_START_POS, region.getOffset());
        assertEquals(COMPLETE_VARIABLE_AT_THE_END_LENGTH, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenCursonIsAfterCompleteVariableAtTheEnd() {
        IRegion region = c.calculate(COMPLETE_VARIABLE_AT_THE_END, COMPLETE_VARIABLE_AT_THE_END_START_POS + COMPLETE_VARIABLE_AT_THE_END_LENGTH);
        assertEquals(COMPLETE_VARIABLE_AT_THE_END_START_POS + COMPLETE_VARIABLE_AT_THE_END_LENGTH, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenCursorIsBeforeCompleteVariableAtTheEnd() {
        IRegion region = c.calculate(COMPLETE_VARIABLE_AT_THE_END, COMPLETE_VARIABLE_AT_THE_END_START_POS);
        assertEquals(COMPLETE_VARIABLE_AT_THE_END_START_POS, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void completeVariableIsReplacedWhenCursorIsInsideVariable() {
        IRegion region = c.calculate(ARGUMENT, INSIDE_COMPLETE_VARIABLE_POS);
        assertEquals(COMPLETE_VARIABLE_START_POS, region.getOffset());
        assertEquals(COMPLETE_VARIABLE_LENGTH, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenCursorIsBeforeVariable() {
        IRegion region = c.calculate(ARGUMENT, COMPLETE_VARIABLE_START_POS);
        assertEquals(COMPLETE_VARIABLE_START_POS, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenCursorIsAfterVariable() {
        IRegion region = c.calculate(ARGUMENT, COMPLETE_VARIABLE_START_POS + COMPLETE_VARIABLE_LENGTH);
        assertEquals(COMPLETE_VARIABLE_START_POS + COMPLETE_VARIABLE_LENGTH, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenCursorIsInsideTextAfterClosedVariable() {
        IRegion region = c.calculate(ARGUMENT_2, AFTER_CLOSED_VARIABLE_POS);
        assertEquals(AFTER_CLOSED_VARIABLE_POS, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void variableStartIsReplacedWhenCursorIsInPartialVariableStart() {
        IRegion region = c.calculate(ARGUMENT, PARTIAL_VARIABLE_START_POS + 1);
        assertEquals(PARTIAL_VARIABLE_START_POS, region.getOffset());
        assertEquals(2, region.getLength());
    }

    @Test
    public void variableStartIsReplacedWhenCursorIsAfterPartialVariableStart() {
        IRegion region = c.calculate(ARGUMENT, PARTIAL_VARIABLE_START_POS + 2);
        assertEquals(PARTIAL_VARIABLE_START_POS, region.getOffset());
        assertEquals(2, region.getLength());
    }

    @Test
    public void variableIsReplacedWhenCursorIsInCompleteVariableStart() {
        IRegion region = c.calculate(ARGUMENT, COMPLETE_VARIABLE_START_POS + 1);
        assertEquals(COMPLETE_VARIABLE_START_POS, region.getOffset());
        assertEquals(COMPLETE_VARIABLE_LENGTH, region.getLength());
    }

    @Test
    public void variableIsReplacedWhenCursorIsAfterCompleteVariableStart() {
        IRegion region = c.calculate(ARGUMENT, COMPLETE_VARIABLE_START_POS + 2);
        assertEquals(COMPLETE_VARIABLE_START_POS, region.getOffset());
        assertEquals(COMPLETE_VARIABLE_LENGTH, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingForEmptyArgument() {
        IRegion region = c.calculate(new ParsedString("", ARG_START_POS), ARG_START_POS);
        assertEquals(ARG_START_POS, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenAtBeginningOfArgument() {
        IRegion region = c.calculate(ARGUMENT, ARG_START_POS);
        assertEquals(ARG_START_POS, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void doesNotReplaceAnythingWhenThereIsNotVariableInArgument() {
        IRegion region = c.calculate(new ParsedString("foobar{foobar}", ARG_START_POS), ARG_START_POS + 8);
        assertEquals(ARG_START_POS + 8, region.getOffset());
        assertEquals(0, region.getLength());
    }

    @Test
    public void replacesStartCharacterBeforeCursorIfNotFollowedByCurlyBrace() {
        IRegion region = c.calculate(new ParsedString("foobar$foobar", ARG_START_POS), ARG_START_POS + 7);
        assertEquals(ARG_START_POS + 6, region.getOffset());
        assertEquals(1, region.getLength());
    }

    @Test
    public void replacesStartCharacterBeforeCursorIfItIsTheLastCharacter() {
        IRegion region = c.calculate(new ParsedString("foobar$", ARG_START_POS), ARG_START_POS + 7);
        assertEquals(ARG_START_POS + 6, region.getOffset());
        assertEquals(1, region.getLength());
    }

    @Test
    public void replacesStartCharacterBeforeCursorIfItIsTheOnlyCharacter() {
        IRegion region = c.calculate(new ParsedString("@", ARG_START_POS), ARG_START_POS + 1);
        assertEquals(ARG_START_POS, region.getOffset());
        assertEquals(1, region.getLength());
    }
}
