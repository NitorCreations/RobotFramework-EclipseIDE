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
package com.nitorcreations.robotframework.eclipseide.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordInlineArgumentMatcher;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordInlineArgumentMatcher.KeywordMatchResult;
import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTWhitespace;
import com.nitorcreations.robotframework.eclipseide.internal.util.BaseDefinitionMatchVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class RobotContentAssistant implements IContentAssistProcessor {

    String[] fgProposals = { "test1", "test2" };

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

        List<RFELine> lines = RobotFile.get(document).getLines();
        RFELine rfeLine = lines.get(lineNo);

        // find the cursor location range inside the current line where keyword
        // completion proposals make sense
        // TODO this only works for basic keyword calls, [Setup], FOR-indented,
        // etc unsupported atm
        int leftPos = findLeftmostKeywordPosition(line, rfeLine);
        int rightPos = findRightmostKeywordPosition(lineInfo, line, rfeLine);
        int cursorPos = documentOffset - lineInfo.getOffset();
        // if inside range, return keyword proposals
        if (leftPos <= cursorPos && cursorPos <= rightPos) {
            return computeKeywordCompletionProposals(viewer, document, documentOffset, rfeLine, leftPos, rightPos);
        }

        return null;
    }

    int findLeftmostKeywordPosition(String line, RFELine rfeLine) {
        int startPos = 0;
        if (!rfeLine.arguments.isEmpty()) {
            startPos = rfeLine.arguments.get(0).getValue().length();
        }
        startPos = skipMinimumRobotWhitespace(line, startPos);
        return startPos;
    }

    int findRightmostKeywordPosition(IRegion lineInfo, String line, RFELine rfeLine) {
        int endPos = line.length();
        if (rfeLine.arguments.size() >= 3) {
            endPos = rfeLine.arguments.get(1).getArgEndCharPos() - lineInfo.getOffset();
        }
        return endPos;
    }

    private ICompletionProposal[] computeKeywordCompletionProposals(ITextViewer viewer, IDocument document, int documentOffset, final RFELine rfeLine, final int leftPos, final int rightPos) {
        final ParsedString arg1 = rfeLine.arguments.size() >= 2 ? rfeLine.arguments.get(1) : null;
        IFile file = ResourceManager.resolveFileFor(document);
        final List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        // first find matches that use the whole input as search string
        DefinitionFinder.acceptMatches(file, LineType.KEYWORD_TABLE_KEYWORD_BEGIN, new KeywordCompletionMatchVisitor(file, arg1, leftPos, proposals, rightPos, rfeLine));
        if (arg1 != null && (proposals.isEmpty() || proposalsContainOnly(proposals, arg1))) {
            proposals.clear();
            int lineOffset = documentOffset - rfeLine.lineCharPos;
            if (leftPos < lineOffset && lineOffset < rightPos) {
                // try again, but only up to cursor
                int argumentOff = lineOffset - leftPos;
                ParsedString arg1leftPart = new ParsedString(arg1.getValue().substring(0, argumentOff), arg1.getArgCharPos());
                DefinitionFinder.acceptMatches(file, LineType.KEYWORD_TABLE_KEYWORD_BEGIN, new KeywordCompletionMatchVisitor(file, arg1leftPart, leftPos, proposals, rightPos, rfeLine));
            }
            if (proposals.isEmpty() || proposalsContainOnly(proposals, arg1)) {
                // try again, ignoring user input, i.e. show all possible
                // keywords
                proposals.clear();
                DefinitionFinder.acceptMatches(file, LineType.KEYWORD_TABLE_KEYWORD_BEGIN, new KeywordCompletionMatchVisitor(file, null, leftPos, proposals, rightPos, rfeLine));
            }
        }
        ICompletionProposal[] proposalsArr = new ICompletionProposal[proposals.size()];
        proposals.toArray(proposalsArr);
        return proposalsArr;
    }

    private boolean proposalsContainOnly(List<RobotCompletionProposal> proposals, ParsedString arg1) {
        if (proposals.size() != 1) {
            return false;
        }
        return proposals.get(0).getMatchKeyword().getValue().equals(arg1.getValue());
    }

    private static final class RobotCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6 {

        private final ParsedString matchKeyword;
        private final IFile matchLocation;
        private final String replacementString;
        private final int replacementOffset;
        private final int replacementLength;
        private final int cursorPosition;
        private final Image image;
        private final String displayString;
        private final String informationDisplayString;
        private final String additionalProposalInfo;

        public RobotCompletionProposal(ParsedString matchKeyword, IFile matchLocation, String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, String informationDisplayString, String additionalProposalInfo) {
            this.matchKeyword = matchKeyword;
            this.matchLocation = matchLocation;
            this.replacementString = replacementString;
            this.replacementOffset = replacementOffset;
            this.replacementLength = replacementLength;
            this.cursorPosition = cursorPosition;
            this.image = image;
            this.displayString = displayString;
            this.informationDisplayString = informationDisplayString;
            this.additionalProposalInfo = additionalProposalInfo;
        }

        public ParsedString getMatchKeyword() {
            return matchKeyword;
        }

        public IFile getMatchLocation() {
            return matchLocation;
        }

        @Override
        public void apply(IDocument document) {
            try {
                document.replace(replacementOffset, replacementLength, replacementString);
            } catch (BadLocationException x) {
                // ignore
            }
        }

        @Override
        public Point getSelection(IDocument document) {
            return new Point(replacementOffset + cursorPosition, 0);
        }

        @Override
        public String getAdditionalProposalInfo() {
            return additionalProposalInfo;
        }

        @Override
        public String getDisplayString() {
            return displayString;
        }

        @Override
        public StyledString getStyledDisplayString() {
            StyledString ss = new StyledString();
            ss.append(displayString);
            return ss;
        }

        @Override
        public Image getImage() {
            return image;
        }

        @Override
        public IContextInformation getContextInformation() {
            return new ContextInformation(null, informationDisplayString);
        }

    }

    private static final class KeywordCompletionMatchVisitor extends BaseDefinitionMatchVisitor {
        private final ParsedString substring;
        private final int leftPos;
        private final List<RobotCompletionProposal> proposals;
        private final int rightPos;
        private final RFELine rfeLine;

        KeywordCompletionMatchVisitor(IFile file, ParsedString substring, int leftPos, List<RobotCompletionProposal> proposals, int rightPos, RFELine rfeLine) {
            super(file);
            this.substring = substring;
            this.leftPos = leftPos;
            this.proposals = proposals;
            this.rightPos = rightPos;
            this.rfeLine = rfeLine;
        }

        @Override
        public VisitorInterest visitMatch(ParsedString match, IFile location) {
            if (substring != null) {
                // TODO this approach makes substring match any keyword with an
                // inline variable
                String lookFor = "${_}" + substring.getValue().toLowerCase() + "${_}";
                if (KeywordMatchResult.DIFFERENT == KeywordInlineArgumentMatcher.match(match.getValue().toLowerCase(), lookFor)) {
                    // if (!match.getValue().contains(substring.getValue())) {
                    return VisitorInterest.CONTINUE;
                }
            }
            Image image = null;
            String displayString = getFilePrefix(location) + match.getValue();
            String additionalProposalInfo = "I recommend: " + match.getValue();

            String informationDisplayString = "You chose: " + match.getValue();

            String replacementString = match.getValue();
            int replacementOffset;
            int replacementLength;
            // if (substring != null) {
            // replacementOffset = substring.getArgCharPos();
            // } else {
            replacementOffset = rfeLine.lineCharPos + leftPos;
            // }
            replacementLength = rightPos - leftPos;
            int cursorPosition = replacementString.length();

            proposals.add(new RobotCompletionProposal(match, location, replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, informationDisplayString, additionalProposalInfo));
            return VisitorInterest.CONTINUE;
        }
    }

    private static int skipMinimumRobotWhitespace(String line, int startPos) {
        boolean gotOne = false;
        int i;
        for (i = startPos; i < line.length(); ++i) {
            char ch = line.charAt(i);
            if (!RFTWhitespace.isWhitespace(ch)) {
                // I don't think this should ever happen
                return line.length() + 1;
            }
            if (ch == '\t' || gotOne) {
                return i + 1;
            }
            gotOne = true;
        }
        return i;
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
