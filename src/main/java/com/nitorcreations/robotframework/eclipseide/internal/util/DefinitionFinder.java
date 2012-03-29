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
package com.nitorcreations.robotframework.eclipseide.internal.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionMatchVisitor.VisitorInterest;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class DefinitionFinder {

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
        Set<IFile> unprocessedFiles = new HashSet<IFile>();
        Set<IFile> processedFiles = new HashSet<IFile>();
        unprocessedFiles.add(file);
        while (!unprocessedFiles.isEmpty()) {
            IFile targetFile = unprocessedFiles.iterator().next();
            RobotFile robotFile = RobotFile.get(targetFile, true);
            if (robotFile != null) {
                List<RobotLine> lines = robotFile.getLines();
                VisitorInterest interest = VisitorInterest.CONTINUE;
                for (RobotLine line : lines) {
                    if (line.isType(visitor.getWantedLineType())) {
                        ParsedString firstArgument = line.arguments.get(0);
                        interest = visitor.visitMatch(firstArgument, targetFile);
                        if (interest == VisitorInterest.STOP) {
                            return;
                        }
                    }
                }
                if (interest == VisitorInterest.CONTINUE_TO_END_OF_CURRENT_FILE) {
                    return;
                }
                for (RobotLine line : lines) {
                    if (line.isResourceSetting()) {
                        ParsedString secondArgument = line.arguments.get(1);
                        IFile resourceFile = ResourceManager.getRelativeFile(targetFile, secondArgument.getUnescapedValue());
                        if (resourceFile.exists() && !processedFiles.contains(resourceFile)) {
                            unprocessedFiles.add(resourceFile);
                        }
                    }
                }
            }
            processedFiles.add(targetFile);
            unprocessedFiles.remove(targetFile);
        }
    }

}
