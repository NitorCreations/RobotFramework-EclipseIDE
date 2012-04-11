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

import static com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionMatchVisitor.VisitorInterest.CONTINUE;
import static com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionMatchVisitor.VisitorInterest.CONTINUE_TO_END_OF_CURRENT_FILE;
import static com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionMatchVisitor.VisitorInterest.STOP;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.nitorcreations.robotframework.eclipseide.builder.parser.IndexFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionMatchVisitor.VisitorInterest;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

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
        PriorityDeque<FileWithType> unprocessedFiles = new LinkedPriorityDeque<FileWithType>(3, new Prioritizer<FileWithType>() {
            @Override
            public int prioritize(FileWithType fileWithType) {
                return fileWithType.getType() == FileType.LIBRARY ? 1 : 0;
            }
        });
        // first add builtin variables so they are processed first
        unprocessedFiles.add(0, new FileWithType(FileType.BUILTIN_VARIABLE, "BuiltIn", file.getProject()));
        unprocessedFiles.add(0, new FileWithType(FileType.RESOURCE, file));
        // add the builtin keywords last
        unprocessedFiles.add(2, new FileWithType(FileType.LIBRARY, "BuiltIn", file.getProject()));

        Set<FileWithType> processedFiles = new HashSet<FileWithType>();
        while (!unprocessedFiles.isEmpty()) {
            FileWithType currentFileWithType = unprocessedFiles.removeFirst();
            processedFiles.add(currentFileWithType);

            VisitorInterest interest;
            switch (currentFileWithType.getType()) {
                case RESOURCE:
                    interest = acceptResourceFile(currentFileWithType, visitor, unprocessedFiles, processedFiles);
                    break;
                case LIBRARY:
                case VARIABLE:
                case BUILTIN_VARIABLE:
                    interest = acceptVariableOrLibraryFile(currentFileWithType, visitor);
                    break;
                default:
                    throw new RuntimeException("Unhandled " + currentFileWithType);
            }
            if (interest == CONTINUE_TO_END_OF_CURRENT_FILE) {
                return;
            }
        }
    }

    private static VisitorInterest acceptResourceFile(FileWithType currentFileWithType, DefinitionMatchVisitor visitor, Collection<FileWithType> unprocessedFiles, Set<FileWithType> processedFiles) {
        IFile currentFile = currentFileWithType.getFile();
        RobotFile currentRobotFile = RobotFile.get(currentFile, true);
        List<RobotLine> lines;
        if (currentRobotFile == null) {
            return CONTINUE;
        }
        lines = currentRobotFile.getLines();
        VisitorInterest interest = CONTINUE;
        for (RobotLine line : lines) {
            if (line.isType(visitor.getWantedLineType())) {
                ParsedString proposal = line.arguments.get(0);
                interest = visitor.visitMatch(proposal, currentFileWithType);
                if (interest == STOP) {
                    return STOP;
                }
            }
        }
        if (interest != CONTINUE_TO_END_OF_CURRENT_FILE) {
            for (RobotLine line : lines) {
                if (line.isResourceSetting()) {
                    processLinkableFile(unprocessedFiles, processedFiles, currentFile, line, FileType.RESOURCE);
                } else if (line.isVariableSetting()) {
                    processLinkableFile(unprocessedFiles, processedFiles, currentFile, line, FileType.VARIABLE);
                } else if (line.isLibrarySetting()) {
                    processUnlinkableFile(unprocessedFiles, processedFiles, line, FileType.LIBRARY, currentFileWithType.getProject());
                }
            }
        }
        return interest;
    }

    private static void processLinkableFile(Collection<FileWithType> unprocessedFiles, Set<FileWithType> processedFiles, IFile currentFile, RobotLine line, FileType type) {
        ParsedString secondArgument = line.arguments.get(1);
        IFile resourceFile = ResourceManager.getRelativeFile(currentFile, secondArgument.getUnescapedValue());
        FileWithType fileWithType = new FileWithType(type, resourceFile);
        if (resourceFile.exists()) {
            addIfNew(unprocessedFiles, processedFiles, fileWithType);
        }
    }

    private static void processUnlinkableFile(Collection<FileWithType> unprocessedFiles, Set<FileWithType> processedFiles, RobotLine line, FileType type, IProject project) {
        ParsedString secondArgument = line.arguments.get(1);
        FileWithType fileWithType = new FileWithType(type, secondArgument.getValue(), project);
        if (!secondArgument.isEmpty()) {
            addIfNew(unprocessedFiles, processedFiles, fileWithType);
        }
    }

    private static void addIfNew(Collection<FileWithType> unprocessedFiles, Set<FileWithType> processedFiles, FileWithType fileWithType) {
        if (!processedFiles.contains(fileWithType)) {
            unprocessedFiles.add(fileWithType);
        }
    }

    private static VisitorInterest acceptVariableOrLibraryFile(FileWithType currentFileWithType, DefinitionMatchVisitor visitor) {
        switch (visitor.getWantedLineType()) {
            case KEYWORD_TABLE_KEYWORD_BEGIN: {
                List<String> keywords = IndexFile.getKeywords(currentFileWithType);
                return acceptList(keywords, ArgumentType.NEW_KEYWORD, visitor, currentFileWithType);
            }
            case VARIABLE_TABLE_LINE: {
                List<String> variables = IndexFile.getVariables(currentFileWithType);
                return acceptList(variables, ArgumentType.VARIABLE_KEY, visitor, currentFileWithType);
            }
        }
        return CONTINUE;
    }

    static VisitorInterest acceptList(List<String> proposals, ArgumentType type, DefinitionMatchVisitor visitor, FileWithType fileWithType) {
        VisitorInterest interest = CONTINUE;
        for (String proposalStr : proposals) {
            ParsedString proposal = new ParsedString(proposalStr, 0); // offset 0 = "located" at beginning of file
            proposal.setType(type);
            interest = visitor.visitMatch(proposal, fileWithType);
            if (interest == STOP) {
                return STOP;
            }
        }
        return interest;
    }

}
