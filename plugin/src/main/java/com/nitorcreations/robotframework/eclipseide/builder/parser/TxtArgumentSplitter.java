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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class TxtArgumentSplitter {

    private static final Pattern SEPARATOR_RE = Pattern.compile("(?:\t| [ \t])[ \t]*");

    /**
     * Splits a line from a robot TXT file into arguments. Only supports the tab-or-multiple-whitespace separator right
     * now.
     * 
     * @param line
     * @param charPos
     *            the file character position of the first character of the line
     * @return
     */
    static List<ParsedString> splitLineIntoArguments(String line, int charPos) {
        String origLine = line;
        // remove trailing empty cells and whitespace
        line = rtrim(line);
        if (line == null) {
            return Collections.emptyList();
        }

        // split line by tab-or-multiwhitespace
        Matcher m = SEPARATOR_RE.matcher(line);
        List<ParsedString> arguments = new ArrayList<ParsedString>();
        int lastEnd = 0;
        for (int argumentIndex = 0;; ++argumentIndex) {
            // if next cell starts with #, the rest of the line is a comment
            boolean isComment = lastEnd < line.length() && line.charAt(lastEnd) == '#';
            boolean isLastArgument = isComment || !m.find();
            int nextStart = !isLastArgument ? m.start() : line.length();
            if (lastEnd == 0 && nextStart > 0 && line.charAt(0) == ' ') {
                /*
                 * spec says all arguments are trimmed - this is the only case when additional trimming is needed.
                 */
                ++lastEnd;
            }
            ParsedString parsedString = new ParsedString(line.substring(lastEnd, nextStart), charPos + lastEnd, argumentIndex);
            int nextLen = (!isLastArgument ? m.end() : origLine.length()) - nextStart;
            if (nextLen >= 1 && origLine.charAt(nextStart) == ' ') {
                parsedString.setHasSpaceAfter(true);
            }
            arguments.add(parsedString);
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
            --epos;
        }
        return null; // empty line
    }

}
