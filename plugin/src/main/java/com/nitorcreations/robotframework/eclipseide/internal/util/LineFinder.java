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
package com.nitorcreations.robotframework.eclipseide.internal.util;

import static com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest.CONTINUE;
import static com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest.CONTINUE_TO_END_OF_CURRENT_FILE;
import static com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest.STOP;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.IndexFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class LineFinder {

    /**
     * This iterates the given resource file and recursively included resource files to locate definitions of keywords
     * and global variables. It passes the matches to the given {@link LineMatchVisitor} instance.
     * 
     * @param file
     *            the starting file
     * @param visitor
     *            the visitor of the matches found
     */
    public static void acceptMatches(IFile file, LineMatchVisitor visitor) {
        /*
         * Priority level 0: built-in variables. Priority level 1: definitions from the local file. Priority level 2:
         * definitions from included resource and variable files, recursively. Priority level 3: explicitly loaded
         * libraries. Priority level 4: built-in library.
         */
        PriorityDeque<FileWithType> unprocessedFiles = new ArrayPriorityDeque<FileWithType>(5, new Prioritizer<FileWithType>() {
            @Override
            public int prioritize(FileWithType fileWithType) {
                return fileWithType.getType() == FileType.LIBRARY ? 3 : 2;
            }
        });
        unprocessedFiles.add(0, new FileWithType(FileType.BUILTIN_VARIABLE, "BuiltIn", file.getProject()));
        unprocessedFiles.add(1, new FileWithType(FileType.RESOURCE, file));
        unprocessedFiles.add(4, new FileWithType(FileType.LIBRARY, "BuiltIn", file.getProject()));

        Set<FileWithType> allFiles = new HashSet<FileWithType>();
        allFiles.addAll(unprocessedFiles);
        int currentPriorityLevel = 0;
        while (!unprocessedFiles.isEmpty()) {
            FileWithType currentFileWithType = unprocessedFiles.removeFirst();

            VisitorInterest interest;
            switch (currentFileWithType.getType()) {
                case RESOURCE:
                    interest = acceptResourceFile(currentFileWithType, visitor, unprocessedFiles, allFiles);
                    break;
                case LIBRARY:
                case VARIABLE:
                case BUILTIN_VARIABLE:
                    interest = acceptVariableOrLibraryFile(currentFileWithType, visitor);
                    break;
                default:
                    throw new RuntimeException("Unhandled " + currentFileWithType);
            }
            int nextPriorityLevel = unprocessedFiles.peekLowestPriority();
            switch (interest) {
                case STOP:
                case CONTINUE_TO_END_OF_CURRENT_FILE:
                    return;
                case CONTINUE_TO_END_OF_CURRENT_PRIORITY_LEVEL:
                    if (nextPriorityLevel != currentPriorityLevel) {
                        return;
                    }
                    break;
            }
            currentPriorityLevel = nextPriorityLevel;
        }
    }

    private static VisitorInterest acceptResourceFile(FileWithType currentFileWithType, LineMatchVisitor visitor, Collection<FileWithType> unprocessedFiles, Set<FileWithType> allFiles) {
        IFile currentFile = currentFileWithType.getFile();
        RobotFile currentRobotFile = RobotFile.get(currentFile, true);
        List<RobotLine> lines;
        if (currentRobotFile == null) {
            return CONTINUE;
        }
        lines = currentRobotFile.getLines();
        VisitorInterest interest = CONTINUE;
        for (RobotLine line : lines) {
            if (visitor.getWantedLineTypes().contains(line.type)) {
                interest = visitor.visitMatch(line, currentFileWithType);
                if (interest == STOP) {
                    return STOP;
                }
            }
        }
        if (interest != CONTINUE_TO_END_OF_CURRENT_FILE) {
            for (RobotLine line : lines) {
                if (line.isResourceSetting()) {
                    if (visitor.visitImport(currentFile, line)) {
                        processLinkableFile(unprocessedFiles, allFiles, currentFile, line, FileType.RESOURCE);
                    }
                } else if (line.isVariableSetting()) {
                    if (visitor.visitImport(currentFile, line)) {
                        processLinkableFile(unprocessedFiles, allFiles, currentFile, line, FileType.VARIABLE);
                    }
                } else if (line.isLibrarySetting()) {
                    if (visitor.visitImport(currentFile, line)) {
                        processUnlinkableFile(unprocessedFiles, allFiles, line, FileType.LIBRARY, currentFileWithType.getProject());
                    }
                }
            }
        }
        return interest;
    }

    private static void processLinkableFile(Collection<FileWithType> unprocessedFiles, Set<FileWithType> allFiles, IFile currentFile, RobotLine line, FileType type) {
        ParsedString secondArgument = line.arguments.get(1);
        String secondArgumentUnescaped = secondArgument.getUnescapedValue();
        IFile resourceFile = PluginContext.getResourceManager().getRelativeFile(currentFile, secondArgumentUnescaped);
        if (resourceFile == null) {
            throw new IllegalStateException("Could not get relative path from \"" + currentFile + "\" to \"" + secondArgumentUnescaped + '"');
        }
        FileWithType fileWithType = new FileWithType(type, resourceFile);
        if (resourceFile.exists()) {
            addIfNew(unprocessedFiles, allFiles, fileWithType);
        }
    }

    private static void processUnlinkableFile(Collection<FileWithType> unprocessedFiles, Set<FileWithType> allFiles, RobotLine line, FileType type, IProject project) {
        ParsedString secondArgument = line.arguments.get(1);
        FileWithType fileWithType = new FileWithType(type, secondArgument.getValue(), project);
        if (!secondArgument.isEmpty()) {
            addIfNew(unprocessedFiles, allFiles, fileWithType);
        }
    }

    private static void addIfNew(Collection<FileWithType> unprocessedFiles, Set<FileWithType> allFiles, FileWithType fileWithType) {
        if (allFiles.add(fileWithType)) {
            unprocessedFiles.add(fileWithType);
        }
    }

    private static VisitorInterest acceptVariableOrLibraryFile(FileWithType currentFileWithType, LineMatchVisitor visitor) {
        if (visitor.wantsLibraryKeywords()) {
            List<String> keywords = IndexFile.getKeywords(currentFileWithType);
            if (!keywords.isEmpty()) {
                return acceptList(keywords, LineType.KEYWORD_TABLE_KEYWORD_BEGIN, ArgumentType.NEW_KEYWORD, visitor, currentFileWithType);
            }
        }
        if (visitor.wantsLibraryVariables()) {
            List<String> variables = IndexFile.getVariables(currentFileWithType);
            return acceptList(variables, LineType.VARIABLE_TABLE_LINE, ArgumentType.VARIABLE_KEY, visitor, currentFileWithType);
        }
        return CONTINUE;
    }

    static VisitorInterest acceptList(List<String> proposals, LineType lineType, ArgumentType type, LineMatchVisitor visitor, FileWithType fileWithType) {
        VisitorInterest interest = CONTINUE;
        for (String proposalStr : proposals) {
            ParsedString proposal = new ParsedString(proposalStr, 0); // offset 0 = "located" at beginning of file
            proposal.setType(type);
            RobotLine line = new RobotLine(-1, -1, Collections.singletonList(proposal));
            line.type = lineType;
            interest = visitor.visitMatch(line, fileWithType);
            if (interest == STOP) {
                return STOP;
            }
        }
        return interest;
    }

}
