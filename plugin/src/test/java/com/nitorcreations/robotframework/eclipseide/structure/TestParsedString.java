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
package com.nitorcreations.robotframework.eclipseide.structure;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.eclipse.jface.text.Region;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

@RunWith(Enclosed.class)
public class TestParsedString {

    public static class ArgumentIndex {
        @Test
        public void getter_should_throw_exception_when_not_set() {
            ParsedString parsedString = new ParsedString("Hello", 52);
            try {
                parsedString.getArgumentIndex();
                fail("Should throw IllegalStateException");
            } catch (IllegalStateException e) {
                // expected
            }
        }

        @Test
        public void getter_should_return_value_when_set() {
            final int ARGUMENT_INDEX = 4;
            ParsedString parsedString = new ParsedString("Hello", 52, ARGUMENT_INDEX);
            int argumentIndex = parsedString.getArgumentIndex();
            assertThat(argumentIndex, is(ARGUMENT_INDEX));
        }
    }

    public static class BasicOffsets {
        @Test
        public void testValues() throws Exception {
            final String TEXT = "Hello \\ World";
            final String BDDTEXT = "Given " + TEXT;
            final String UNESCAPED_BDDTEXT = "Given Hello  World";
            final int ARG_OFF = 57;
            final ParsedString parsedString = new ParsedString(BDDTEXT, ARG_OFF);
            assertEquals(BDDTEXT, parsedString.getValue());
            assertEquals(TEXT, parsedString.getAlternateValue());
            assertEquals(UNESCAPED_BDDTEXT, parsedString.getUnescapedValue());
        }

        @Test
        public void testOffsets() throws Exception {
            final String TEXT = "Hello";
            final int TEXT_LEN = TEXT.length();
            final int ARG_OFF = 57;
            final ParsedString parsedString = new ParsedString(TEXT, ARG_OFF);
            assertEquals(ARG_OFF, parsedString.getArgCharPos());
            assertEquals(ARG_OFF + TEXT_LEN, parsedString.getArgEndCharPos());
            assertEquals(ARG_OFF + TEXT_LEN, parsedString.getExtendedArgEndCharPos());
            assertEquals(false, parsedString.hasSpaceAfter());
            parsedString.setHasSpaceAfter(true);
            assertEquals(ARG_OFF + TEXT_LEN + 1, parsedString.getExtendedArgEndCharPos());
            assertEquals(true, parsedString.hasSpaceAfter());
        }

        @Test
        public void testType() throws Exception {
            final String TEXT = "Hello";
            final int ARG_OFF = 57;
            final ParsedString parsedString = new ParsedString(TEXT, ARG_OFF);
            ArgumentType ARG_TYPE = ArgumentType.KEYWORD_CALL;
            parsedString.setType(ARG_TYPE);
            assertEquals(ARG_TYPE, parsedString.getType());
        }
    }

    public static class extractRegion {
        final String TEXT = "Hello";
        final int TEXT_LEN = TEXT.length();
        final int ARG_OFF = 57;
        final int ARG_END_OFF = ARG_OFF + TEXT_LEN;
        final ParsedString parsedString = new ParsedString(TEXT, ARG_OFF);
        static final ArgumentType TYPE = ArgumentType.KEYWORD_CALL_DYNAMIC;

        @Before
        public void setupParsedString() {
            parsedString.setType(TYPE);
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void beginning_before_should_throw_exception() {
            final int REG_OFF = ARG_OFF - 1;
            final int REG_END_OFF = ARG_END_OFF - 1;
            parsedString.extractRegion(new Region(REG_OFF, REG_END_OFF - REG_OFF));
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void beginning_before_and_ending_after_should_throw_exception() {
            final int REG_OFF = ARG_OFF - 1;
            final int REG_END_OFF = ARG_END_OFF + 1;
            parsedString.extractRegion(new Region(REG_OFF, REG_END_OFF - REG_OFF));
        }

        @Test(expected = IndexOutOfBoundsException.class)
        public void ending_after_should_throw_exception() {
            final int REG_OFF = ARG_OFF + 1;
            final int REG_END_OFF = ARG_END_OFF + 1;
            parsedString.extractRegion(new Region(REG_OFF, REG_END_OFF - REG_OFF));
        }

        @Test
        public void beginning_at_should_work() {
            final int REG_OFF = ARG_OFF;
            final int REG_END_OFF = REG_OFF + 2;
            ParsedString r = parsedString.extractRegion(new Region(REG_OFF, REG_END_OFF - REG_OFF));
            assertEquals(REG_OFF, r.getArgCharPos());
            assertEquals("He", r.getValue());
            assertEquals(TYPE, r.getType());
            assertFalse(r.hasSpaceAfter());
        }

        @Test
        public void ending_at_should_work() {
            final int REG_OFF = ARG_END_OFF - 2;
            final int REG_END_OFF = ARG_END_OFF;
            ParsedString r = parsedString.extractRegion(new Region(REG_OFF, REG_END_OFF - REG_OFF));
            assertEquals(REG_OFF, r.getArgCharPos());
            assertEquals("lo", r.getValue());
            assertEquals(TYPE, r.getType());
            assertFalse(r.hasSpaceAfter());
        }

        @Test
        public void in_the_middle_should_work() {
            final int REG_OFF = ARG_OFF + 2;
            final int REG_END_OFF = ARG_END_OFF - 2;
            ParsedString r = parsedString.extractRegion(new Region(REG_OFF, REG_END_OFF - REG_OFF));
            assertEquals(REG_OFF, r.getArgCharPos());
            assertEquals("l", r.getValue());
            assertEquals(TYPE, r.getType());
            assertFalse(r.hasSpaceAfter());
        }
    }
}
