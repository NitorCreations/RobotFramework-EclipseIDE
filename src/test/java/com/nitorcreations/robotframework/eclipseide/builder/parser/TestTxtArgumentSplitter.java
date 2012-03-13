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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.nitorcreations.robotframework.eclipseide.builder.parser.TxtArgumentSplitter;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

@RunWith(Parameterized.class)
public class TestTxtArgumentSplitter {

  private final String input;
  private final String[] expected;

  static List<ParsedString> s(String line) {
    return s(line, 0);
  }

  static List<ParsedString> s(String line, int charPos) {
    return TxtArgumentSplitter.splitLineIntoArguments(line, charPos);
  }

  static void a(String line, String... expectedArguments) {
    final int off = 27;
    List<ParsedString> l = s(line, off);
    assertArgumentCount(l, expectedArguments);
    for (int i = 0; i < expectedArguments.length; ++i) {
      String expected = expectedArguments[i];
      ParsedString actual = l.get(i);
      Assert.assertEquals("Argument", expected, actual.getValue());
      Assert.assertEquals("argCharPos", line.indexOf(expected) + off, actual.getArgCharPos());
      Assert.assertEquals("argEndCharPos", actual.getArgEndCharPos(), actual.getArgCharPos() + actual.getValue().length());
    }
  }

  static void assertArgumentCount(List<ParsedString> l, String... expectedArguments) {
    Assert.assertEquals("Wrong argument count for line, expected " + Arrays.toString(expectedArguments) + ", got " + l + "; count", expectedArguments.length,
                        l.size());
  }

  public TestTxtArgumentSplitter(String input, String... expected) {
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
    return args;
  }

  private static void add(List<Object[]> args, String input, String... expected) {
    args.add(new Object[] { input, expected });
  }
}
