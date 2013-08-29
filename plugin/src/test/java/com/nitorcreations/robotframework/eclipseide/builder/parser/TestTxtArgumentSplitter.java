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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.nitorcreations.junit.runners.NicelyParameterized;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

@RunWith(Enclosed.class)
public class TestTxtArgumentSplitter {

    static List<ParsedString> s(String line) {
        return s(line, 0);
    }

    static List<ParsedString> s(String line, int charPos) {
        return TxtArgumentSplitter.splitLineIntoArguments(line, charPos);
    }

    static ParsedString single(String line) {
        List<ParsedString> l = s(line);
        assertEquals("Should parse to single argument only", 1, l.size());
        return l.get(0);
    }

    static ParsedString firstOfTwo(String line) {
        List<ParsedString> l = s(line);
        assertEquals("Should parse to two arguments", 2, l.size());
        return l.get(0);
    }

    @RunWith(NicelyParameterized.class)
    public static class Basic_tests {

        private final String input;
        private final String[] expected;

        static void a(String line, String... expectedArguments) {
            final int off = 27;
            List<ParsedString> l = s(line, off);
            assertArgumentCount(l, expectedArguments);
            for (int i = 0; i < expectedArguments.length; ++i) {
                String expected = expectedArguments[i];
                ParsedString actual = l.get(i);
                assertEquals("Argument", expected, actual.getValue());
                assertEquals("argCharPos", line.indexOf(expected) + off, actual.getArgCharPos());
                assertEquals("argEndCharPos", actual.getArgEndCharPos(), actual.getArgCharPos() + actual.getValue().length());
            }
        }

        static void assertArgumentCount(List<ParsedString> l, String... expectedArguments) {
            assertEquals("Wrong argument count for line, expected " + Arrays.toString(expectedArguments) + ", got " + l + "; count", expectedArguments.length, l.size());
        }

        public Basic_tests(String input, String... expected) {
            this.input = input;
            this.expected = expected;
        }

        @Test
        public void test() throws Exception {
            a(input, expected);
        }

        @Parameters
        public static List<Object[]> createTests() {
            List<Object[]> args = new ArrayList<Object[]>();
            add(args, "Hello world", "Hello world");
            add(args, " Hello world", "Hello world");
            add(args, "  Hello world", "", "Hello world");
            add(args, "    Hello world", "", "Hello world");
            add(args, "\tHello world", "", "Hello world");

            add(args, "\\  Hello world", "\\", "Hello world");
            add(args, "  \\  Hello world", "", "\\", "Hello world");

            add(args, "#lol", "#lol");
            add(args, "   #lol", "", "#lol");
            add(args, "Hello world   #lol", "Hello world", "#lol");
            add(args, "  Hello world   #lol", "", "Hello world", "#lol");

            add(args, "  Hello world #lol", "", "Hello world #lol");
            add(args, "  Hello world #lol  #lol2", "", "Hello world #lol", "#lol2");

            add(args, "#lol this is", "#lol this is");
            add(args, "#lol this   is", "#lol this   is");
            add(args, "  Hello world   #lol   this", "", "Hello world", "#lol   this");

            // whitespace
            add(args, "");
            add(args, "  ");
            add(args, "  Keyword  ", "", "Keyword");
            return args;
        }

        private static void add(List<Object[]> args, String input, String... expected) {
            args.add(new Object[] { input, expected });
        }
    }

    public static class Special_cases {
        @Test
        public void arg_at_eol_does_not_extend() {
            assertFalse(single("Hello").hasSpaceAfter());
        }

        @Test
        public void single_space_before_eol_extends() {
            assertTrue(single("Hello ").hasSpaceAfter());
        }

        @Test
        public void double_space_before_eol_extends() {
            assertTrue(single("Hello  ").hasSpaceAfter());
        }

        @Test
        public void single_tab_before_eol_does_not_extend() {
            assertFalse(single("Hello\t").hasSpaceAfter());
        }

        @Test
        public void double_tab_before_eol_does_not_extend() {
            assertFalse(single("Hello\t\t").hasSpaceAfter());
        }

        @Test
        public void double_space_between_args_extends() {
            assertTrue(firstOfTwo("Hello  World").hasSpaceAfter());
        }

        @Test
        public void single_tab_between_args_does_not_extend() {
            assertFalse(firstOfTwo("Hello\tWorld").hasSpaceAfter());
        }

        @Test
        public void double_tab_between_args_does_not_extend() {
            assertFalse(firstOfTwo("Hello\t\tWorld").hasSpaceAfter());
        }
    }

}
