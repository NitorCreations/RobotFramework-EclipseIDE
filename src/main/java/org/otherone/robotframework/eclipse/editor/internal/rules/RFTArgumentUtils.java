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
package org.otherone.robotframework.eclipse.editor.internal.rules;

public final class RFTArgumentUtils {

  /**
   * Finds the start of the next argument, when scanning from the offset given. If only whitespace
   * is found, "no keyword" is returned. Also if the given start position does not start with a
   * whitespace separator, "no keyword" is returned.
   * 
   * @param line
   *          the line to scan
   * @param start
   *          the position to start scanning on. Must be either the start of line or the first
   *          whitespace detected after the previous argument.
   * @return the position at which the next keyword starts, or -1 if no more keywords are found.
   */
  public static int findNextArgumentStart(String line, int start) {
    if (start == line.length() || !RFTWhitespace.isWhitespace(line.charAt(start))) {
      return -1;
    }

    // first check that we fulfill argument separator requirements
    if (line.charAt(start) == '\t') {
      ++start;
    } else {
      // keyword call must be preceded by at least two whitespace when first is
      // not a tab
      if (start + 1 == line.length() || !RFTWhitespace.isWhitespace(line.charAt(start + 1))) {
        return -1;
      }
      start += 2;
    }

    // next skip any additional whitespace
    while (true) {
      if (start == line.length()) {
        return -1;
      }
      if (!RFTWhitespace.isWhitespace(line.charAt(start))) {
        break;
      }
      ++start;
    }

    // drop comments
    if (line.charAt(start) == '#') {
      return -1;
    }

    return start;
  }

  /**
   * Calculate the length of the argument at the given position.
   * 
   * @param line
   *          the line to scan
   * @param start
   *          the start position of the keyword
   * @return the number of characters in the keyword
   */
  public static int calculateArgumentLength(String line, int start) {
    int end = start;
    outer: while (true) {
      while (end < line.length() && !RFTWhitespace.isWhitespace(line.charAt(end))) {
        switch (line.charAt(end)) {
          case '#':
            break outer;
          case '\\':
            if (end + 1 == line.length()) {
              break outer; // exclude the \ from the keyword
            }
            end += 2;
            break;
          default:
            ++end;
            break;
        }
      }
      if (end == line.length()) {
        break;
      }
      if (line.charAt(end) == '\t') {
        break;
      }
      if (end + 1 == line.length()) {
        // break, retaining end at the whitespace position, excluding it from the keyword (Robot
        // framework trims all keywords)
        break;
      }
      if (RFTWhitespace.isWhitespace(line.charAt(end + 1))) {
        // at least two successive whitespace
        break;
      }
      ++end; // accept whitespace as part of keyword and continue
    }
    return end - start;
  }

  /**
   * Unescape an argument string according to Robot Framework escaping rules.
   * 
   * @param line
   *          the line containing the argument
   * @param start
   *          start position of the argument
   * @param length
   *          length of the argument
   * @return the argument, unescaped
   */
  public static String unescapeArgument(String line, int start, int length) {
    int nextEscape = line.indexOf('\\', start);
    int end = start + length;
    if (nextEscape == -1 || nextEscape >= end) {
      return line.substring(start, end);
    }
    StringBuilder sb = new StringBuilder();
    do {
      sb.append(line, start, nextEscape);
      if (nextEscape + 1 == line.length()) {
        // ignore single \
        break;
      }
      switch (line.charAt(nextEscape + 1)) {
        case 'n':
          sb.append('\n');
          break;
        case 'r':
          sb.append('\r');
          break;
        case 't':
          sb.append('\t');
          break;
        default:
          sb.append(line.charAt(nextEscape + 1));
          break;
      }
      start = nextEscape + 2;
      nextEscape = line.indexOf('\\', start);
    } while (nextEscape != -1 && nextEscape <= end);
    sb.append(line, start, end);
    return sb.toString();
  }

}
