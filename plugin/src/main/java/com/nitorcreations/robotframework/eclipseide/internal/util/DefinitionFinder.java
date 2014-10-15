/**
 * Copyright 2012, 2014 Nitor Creations Oy, Dreamhunters-net
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
package com.nitorcreations.robotframework.eclipseide.internal.util;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;

public class DefinitionFinder {

    private static class LineMatchVisitorAdapter implements LineMatchVisitor {

        private final DefinitionMatchVisitor delegate;

        LineMatchVisitorAdapter(DefinitionMatchVisitor delegate) {
            this.delegate = delegate;
        }

        @Override
        public VisitorInterest visitMatch(RobotLine line, FileWithType lineLocation, String context) {
            return delegate.visitMatch(line.arguments.get(0), lineLocation, context);
        }

        @Override
        public VisitorInterest visitMatch(RobotLine line, FileWithType lineLocation) {
            return delegate.visitMatch(line.arguments.get(0), lineLocation, "");
        }

        @Override
        public Set<LineType> getWantedLineTypes() {
            return Collections.singleton(delegate.getWantedLineType());
        }

        @Override
        public boolean wantsLibraryKeywords() {
            return delegate.getWantedLineType() == LineType.KEYWORD_TABLE_KEYWORD_BEGIN;
        }

        @Override
        public boolean wantsLibraryVariables() {
            return delegate.getWantedLineType() == LineType.VARIABLE_TABLE_LINE;
        }

        @Override
        public boolean visitImport(IFile currentFile, RobotLine line) {
            return delegate.visitImport(currentFile, line);
        }

    }

    /**
     * This iterates the given resource file and recursively included resource files to locate definitions of keywords
     * and global variables. It passes the matches to the given {@link DefinitionMatchVisitor} instance.
     * 
     * @param file
     *            the starting file
     * @param visitor
     *            the visitor of the matches found
     */
    public static void acceptMatches(IFile file, DefinitionMatchVisitor visitor) {
        LineFinder.acceptMatches(file, new LineMatchVisitorAdapter(visitor));
    }

}
