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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class RobotContentAssistant implements IContentAssistProcessor {

    // ctrl-space completion proposals
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        // find info about current line
        int lineNo;
        IDocument document = viewer.getDocument();
        try {
            lineNo = document.getLineOfOffset(documentOffset);
        } catch (BadLocationException ex) {
            return null;
        }

        List<RobotLine> lines = RobotFile.get(document).getLines();
        RobotLine robotLine;
        if (lineNo < lines.size()) {
            robotLine = lines.get(lineNo);
        } else {
            robotLine = new RobotLine(lineNo, documentOffset, Collections.<ParsedString> emptyList());
        }
        ParsedString argument = robotLine.getArgumentAt(documentOffset);
        if (argument == null) {
            argument = synthesizeArgument(document, documentOffset, lineNo);
        }

        IFile file = ResourceManager.resolveFileFor(document);
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        boolean allowKeywords = false;
        boolean allowVariables = false;
        switch (argument.getType()) {
            case KEYWORD_CALL:
                allowKeywords = true;
                break;
            case KEYWORD_CALL_DYNAMIC:
                allowKeywords = true;
                allowVariables = true;
                break;
            case KEYWORD_ARG:
            case SETTING_FILE_ARG:
            case SETTING_VAL:
            case SETTING_FILE: // TODO verify
            case VARIABLE_VAL: // TODO only suggest local variables
                allowVariables = true;
                break;
        }
        if (allowKeywords) {
            IRegion replacementRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
            KeywordCompletionMatchVisitorProvider visitorProvider = new KeywordCompletionMatchVisitorProvider(file, replacementRegion);
            proposals.addAll(computeCompletionProposals(file, documentOffset, argument, visitorProvider));
        }
        if (allowVariables) {
            IRegion replacementRegion = VariableReplacementRegionCalculator.calculate(argument, documentOffset);
            VariableCompletionMatchVisitorProvider visitorProvider = new VariableCompletionMatchVisitorProvider(file, replacementRegion);
            List<RobotCompletionProposal> variableProposals = computeCompletionProposals(file, documentOffset, argument, visitorProvider);
            if (replacementRegion.getLength() > 0) {
                // the cursor is positioned for replacing a variable, so put the variable proposals first
                proposals.addAll(0, variableProposals);
            } else {
                // default positioning of proposals
                proposals.addAll(variableProposals);
            }
        }
        if (proposals.isEmpty()) {
            return null;
        }
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    private ParsedString synthesizeArgument(IDocument document, int documentOffset, int lineNo) {
        String documentText = document.get();
        // insert synthetic text at current position, and wrap with tabs to make sure it's treated as a separate
        // argument
        // TODO skip pre/post-tab if beginning/end of line
        StringBuilder newText = new StringBuilder(documentText.length() + 3);
        newText.append(documentText, 0, documentOffset);
        int syntheticDocumentOffset = documentOffset;
        if (documentOffset > 0 && !isCrLf(documentText.charAt(documentOffset - 1))) {
            newText.append('\t');
            ++syntheticDocumentOffset;
        }
        newText.append('x'); // synthetic argument
        if (documentOffset < documentText.length() && !isCrLf(documentText.charAt(documentOffset))) {
            newText.append('\t');
        }
        newText.append(documentText, documentOffset, documentText.length());
        List<RobotLine> lines = RobotFile.parse(newText.toString()).getLines();
        RobotLine robotLine = lines.get(lineNo);
        ParsedString synthesizedArgument = robotLine.getArgumentAt(syntheticDocumentOffset);
        assert synthesizedArgument != null;
        ParsedString argument = new ParsedString("", documentOffset);
        argument.copyTypeVariablesFrom(synthesizedArgument);
        return argument;
    }

    private static boolean isCrLf(char ch) {
        return ch == '\n' || ch == '\r';
    }

    private List<RobotCompletionProposal> computeCompletionProposals(IFile file, int documentOffset, ParsedString argument, CompletionMatchVisitorProvider visitorProvider) {
        System.out.println("RobotContentAssistant.computeCompletionProposals() " + documentOffset + " " + argument);
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        // first find matches that use the whole input as search string
        DefinitionFinder.acceptMatches(file, visitorProvider.get(argument, proposals));
        if (argument != null && proposalsIsEmptyOrContainsOnly(proposals, argument)) {
            proposals.clear();
            // int lineOffset = documentOffset - lineCharPos;
            if (argument.getArgCharPos() < documentOffset && documentOffset < argument.getArgEndCharPos()) {
                // try again, but only up to cursor
                int argumentOff = documentOffset - argument.getArgCharPos();
                ParsedString argumentleftPart = new ParsedString(argument.getValue().substring(0, argumentOff), argument.getArgCharPos());
                DefinitionFinder.acceptMatches(file, visitorProvider.get(argumentleftPart, proposals));
            }
            if (proposalsIsEmptyOrContainsOnly(proposals, argument)) {
                // try again, ignoring user input, i.e. show all possible keywords
                proposals.clear();
                DefinitionFinder.acceptMatches(file, visitorProvider.get(null, proposals));
            }
        }
        return proposals;
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
        return null;
        // TODO example code below
        // IContextInformation[] result = new IContextInformation[5];
        // for (int i = 0; i < result.length; i++) {
        // String contextDisplayString = "contextDisplayString " + i;
        // String informationDisplayString = "informationDisplayString " + i;
        // result[i] = new ContextInformation(contextDisplayString, informationDisplayString);
        // }
        // return result;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new IContextInformationValidator() {

            private int offset;

            @Override
            public void install(IContextInformation info, ITextViewer viewer, int offset) {
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
                return this.offset == offset;
            }
        };
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '$', '@' };
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
