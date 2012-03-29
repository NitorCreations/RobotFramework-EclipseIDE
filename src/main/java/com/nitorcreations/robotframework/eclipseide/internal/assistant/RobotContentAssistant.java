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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;
import com.nitorcreations.robotframework.eclipseide.internal.rules.RobotWhitespace;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class RobotContentAssistant implements IContentAssistProcessor {

    // ctrl-space completion proposals
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        // find info about current line
        IRegion lineInfo;
        String line;
        int lineNo;
        IDocument document = viewer.getDocument();
        try {
            lineInfo = document.getLineInformationOfOffset(documentOffset);
            lineNo = document.getLineOfOffset(documentOffset);
            line = document.get(lineInfo.getOffset(), lineInfo.getLength());
        } catch (BadLocationException ex) {
            return null;
        }

        List<RobotLine> lines = RobotFile.get(document).getLines();
        RobotLine robotLine = lines.get(lineNo);
        ParsedString argument = robotLine.getArgumentAt(documentOffset);

        if (argument.getType() == ArgumentType.KEYWORD_CALL) {
            // find the cursor location range inside the current line where keyword
            // completion proposals make sense
            // TODO this only works for basic keyword calls, [Setup], FOR-indented,
            // etc unsupported atm
            int leftPos = findLeftmostKeywordPosition(lineInfo, line, robotLine);
            int rightPos = findRightmostKeywordPosition(lineInfo, line, robotLine);
            int replacePos = robotLine.arguments.size() >= 2 ? robotLine.arguments.get(1).getArgCharPos() - lineInfo.getOffset() : leftPos;
            int cursorPos = documentOffset - lineInfo.getOffset();
            // if inside range, return keyword proposals
            if (leftPos <= cursorPos && cursorPos <= rightPos) {
                argument = robotLine.arguments.size() >= 2 ? robotLine.arguments.get(1) : null;
                return computeCompletionProposals(viewer, document, documentOffset, robotLine, argument, leftPos, rightPos, replacePos);
            }
        } else if (argument.getUnescapedValue().startsWith("$")) {
            int leftPos = argument.getArgCharPos() - lineInfo.getOffset();
            int rightPos = argument.getArgEndCharPos() - lineInfo.getOffset();
            return computeCompletionProposals(viewer, document, documentOffset, robotLine, argument, leftPos, rightPos, leftPos);
        }
        return null;
    }

    int findLeftmostKeywordPosition(IRegion lineInfo, String line, RobotLine robotLine) {
        int startPos = 0;
        if (!robotLine.arguments.isEmpty()) {
            startPos = robotLine.arguments.get(0).getArgEndCharPos() - lineInfo.getOffset();
        }
        return RobotWhitespace.skipMinimumRobotWhitespace(line, startPos);
    }

    int findRightmostKeywordPosition(IRegion lineInfo, String line, RobotLine robotLine) {
        if (robotLine.arguments.size() >= 3) {
            return robotLine.arguments.get(1).getArgEndCharPos() - lineInfo.getOffset();
        }
        return line.length();
    }

    private ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, IDocument document, int documentOffset, final RobotLine robotLine, ParsedString argument, final int leftPos, final int rightPos, int replacePos) {
        IFile file = ResourceManager.resolveFileFor(document);
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        // first find matches that use the whole input as search string
        findMatches(robotLine, leftPos, rightPos, replacePos, argument, file, proposals);
        if (argument != null && proposalsIsEmptyOrContainsOnly(proposals, argument)) {
            proposals.clear();
            int lineOffset = documentOffset - robotLine.lineCharPos;
            if (leftPos < lineOffset && lineOffset < rightPos) {
                // try again, but only up to cursor
                int argumentOff = lineOffset - leftPos;
                ParsedString argumentleftPart = new ParsedString(argument.getValue().substring(0, argumentOff), argument.getArgCharPos());
                findMatches(robotLine, leftPos, rightPos, replacePos, argumentleftPart, file, proposals);
            }
            if (proposalsIsEmptyOrContainsOnly(proposals, argument)) {
                // try again, ignoring user input, i.e. show all possible keywords
                proposals.clear();
                findMatches(robotLine, leftPos, rightPos, replacePos, null, file, proposals);
            }
        }
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    private void findMatches(final RobotLine robotLine, final int leftPos, final int rightPos, int replacePos, final ParsedString argument, IFile file, final List<RobotCompletionProposal> proposals) {
        IRegion replacementRegion = new Region(robotLine.lineCharPos + replacePos, rightPos - leftPos);
        if (argument.getUnescapedValue().startsWith("$")) {
            DefinitionFinder.acceptMatches(file, LineType.VARIABLE_TABLE_LINE, new VariableCompletionMatchVisitor(file, argument, proposals, replacementRegion));
        } else {
            DefinitionFinder.acceptMatches(file, LineType.KEYWORD_TABLE_KEYWORD_BEGIN, new KeywordCompletionMatchVisitor(file, argument, proposals, replacementRegion));
        }
    }

    private boolean proposalsIsEmptyOrContainsOnly(List<RobotCompletionProposal> proposals, ParsedString argument) {
        if (proposals.size() != 1) {
            return proposals.isEmpty();
        }
        return proposals.get(0).getMatchArgument().getValue().equals(argument.getValue());
    }

    // ctrl-shift-space information popups
    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
        // TODO replace with real implementation
        IContextInformation[] result = new IContextInformation[5];
        for (int i = 0; i < result.length; i++) {
            String contextDisplayString = "contextDisplayString " + i;
            String informationDisplayString = "informationDisplayString " + i;
            result[i] = new ContextInformation(contextDisplayString, informationDisplayString);
        }
        return result;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new IContextInformationValidator() {

            private IContextInformation info;
            private ITextViewer viewer;
            private int offset;

            @Override
            public void install(IContextInformation info, ITextViewer viewer, int offset) {
                this.info = info;
                this.viewer = viewer;
                this.offset = offset;
            }

            @Override
            public boolean isContextInformationValid(int offset) {
                // TODO return false when cursor goes out of context for the
                // IContextInformation given to install()
                // see ContextInformationValidator.isContextInformationValid()

                // the user can always close a shown IContextInformation
                // instance by hitting Esc or moving the focus out of eclipse

                // if the previous IContextInformation is not closed before the
                // next is shown, it is temporarily hidden until the next one is
                // closed. This might confuse the user.
                return true;
            }

        };
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        // TODO perhaps '$' or '{'? test it to see how it works..
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

}
