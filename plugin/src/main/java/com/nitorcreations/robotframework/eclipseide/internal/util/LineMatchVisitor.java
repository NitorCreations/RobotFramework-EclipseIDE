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

import java.util.Set;

import org.eclipse.core.resources.IFile;

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;

public interface LineMatchVisitor {

    /**
     * @param line
     *            the line
     * @param lineLocation
     *            where line is located - null if the proposal is located in a variable file or a library
     * @param keywordContext
     */
    VisitorInterest visitMatch(RobotLine line, FileWithType lineLocation, String keywordContext);

    VisitorInterest visitMatch(RobotLine line, FileWithType fileWithType);

    Set<LineType> getWantedLineTypes();

    boolean wantsLibraryKeywords();

    boolean wantsLibraryVariables();

    /**
     * @return true if {@link DefinitionFinder} should descend into the given import, false if not
     */
    boolean visitImport(IFile currentFile, RobotLine line);

}
