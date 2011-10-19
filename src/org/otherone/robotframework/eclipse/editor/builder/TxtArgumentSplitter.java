/**
 * Copyright 2011 Nitor Creations Oy
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
package org.otherone.robotframework.eclipse.editor.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.otherone.robotframework.eclipse.editor.builder.RFEParser.Info.Argument;

public class TxtArgumentSplitter {

  private static final Pattern SEPARATOR_RE = Pattern.compile("(?:\t| [ \t])[ \t]*");

  /**
   * Splits a line from a robot TXT file into arguments. Only supports the
   * tab-or-multiple-whitespace separator right now.
   * 
   * @param line
   * @param charPos
   * @return
   */
  static List<Argument> splitLineIntoArguments(String line, int charPos) {
    // remove trailing empty cells and whitespace
    line = rtrim(line);

    // split line by tab-or-multiwhitespace
    Matcher m = SEPARATOR_RE.matcher(line);
    List<Argument> arguments = new ArrayList<Argument>();
    int lastEnd = 0;
    while (true) {
      if (lastEnd < line.length() && line.charAt(lastEnd) == '#') {
        // next cell starts with comment, so the rest of the line should be ignored
        break;
      }
      boolean isLastArgument = !m.find();
      int nextStart = !isLastArgument ? m.start() : line.length();
      arguments.add(new Argument(line.substring(lastEnd, nextStart), charPos + lastEnd));
      if (isLastArgument) {
        // last argument
        break;
      }
      lastEnd = m.end();
    }
    return arguments;
  }

  static String rtrim(String line) {
    int epos = line.length() - 1;
    while (epos >= 0) {
      switch (line.charAt(epos)) {
        case ' ':
        case '\t':
          break;
        default:
          return line.substring(0, epos + 1);
      }
    }
    return null; // empty line
  }

}
