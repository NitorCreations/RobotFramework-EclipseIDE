/**
 * Copyright 2013 Nitor Creations Oy
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.TableType;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IProposalGenerator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.RobotCompletionProposal;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.RobotCompletionProposalSet;
import com.nitorcreations.robotframework.eclipseide.internal.util.ArrayPriorityDeque;
import com.nitorcreations.robotframework.eclipseide.internal.util.Prioritizer;
import com.nitorcreations.robotframework.eclipseide.internal.util.PriorityDeque;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class RobotContentAssistant2 implements IRobotContentAssistant2 {

    private final IProposalGenerator proposalGenerator;

    public RobotContentAssistant2(IProposalGenerator proposalGenerator) {
        this.proposalGenerator = proposalGenerator;
    }

    @Override
    public ICompletionProposal[] generateProposals(IFile file, int documentOffset, String documentText, List<RobotLine> lines, int lineNo) {
        RobotLine robotLine = lines.get(lineNo);
        ParsedString argument = robotLine.getArgumentAt(documentOffset);
        if (argument == null) {
            argument = synthesizeArgument(documentText, documentOffset, lineNo);
        }

        return generateProposalsForArgument(file, argument, documentOffset, lines, lineNo, robotLine);
    }

    private ICompletionProposal[] generateProposalsForArgument(IFile file, ParsedString argument, int documentOffset, List<RobotLine> lines, int lineNo, RobotLine robotLine) {
        PriorityDeque<RobotCompletionProposalSet> proposalSets;
        if (argument.getArgumentIndex() == 0) {
            proposalSets = generateProposalsForFirstArgument(file, argument, documentOffset, lines, lineNo);
        } else {
            proposalSets = generateProposalsForRestOfArguments(file, argument, documentOffset, robotLine);
        }
        return extractMostRelevantProposals(proposalSets);
    }

    private PriorityDeque<RobotCompletionProposalSet> generateProposalsForFirstArgument(IFile file, ParsedString argument, int documentOffset, List<RobotLine> lines, int lineNo) {
        PriorityDeque<RobotCompletionProposalSet> proposalSets = createProposalSets();
        switch (determineTableTypeForLine(lines, lineNo)) {
            case KEYWORD:
                proposalGenerator.addKeywordDefinitionProposals(file, argument, documentOffset, proposalSets);
                break;
            case SETTING:
                proposalGenerator.addSettingTableProposals(file, argument, documentOffset, proposalSets);
                break;
            default:
                break;
        }
        proposalGenerator.addTableProposals(file, argument, documentOffset, proposalSets);
        // TODO we should only include either of setting/table proposals if either has exactly one match perhaps?
        return proposalSets;
    }

    private PriorityDeque<RobotCompletionProposalSet> generateProposalsForRestOfArguments(IFile file, ParsedString argument, int documentOffset, RobotLine robotLine) {
        boolean allowKeywords = false;
        boolean allowVariables = false;
        int maxVariableCharPos = Integer.MAX_VALUE;
        int maxSettingCharPos = Integer.MAX_VALUE;
        switch (argument.getType()) {
            case KEYWORD_CALL:
                allowKeywords = true;
                break;
            case KEYWORD_CALL_DYNAMIC:
                allowKeywords = true;
                allowVariables = true;
                break;
            case KEYWORD_ARG:
                allowVariables = true;
                break;
            case SETTING_FILE_ARG:
            case SETTING_VAL:
            case SETTING_FILE:
                allowVariables = true;
                // limit visible imported variables to those loaded before current line
                maxSettingCharPos = robotLine.lineCharPos - 1;
                break;
            case VARIABLE_VAL:
                allowVariables = true;
                // limit visible local variables to those declared before current line
                maxVariableCharPos = robotLine.lineCharPos - 1;
                maxSettingCharPos = -1;
                break;
        }
        PriorityDeque<RobotCompletionProposalSet> proposalSets = createProposalSets();
        if (allowKeywords) {
            proposalGenerator.addKeywordCallProposals(file, argument, documentOffset, proposalSets);
        }
        if (allowVariables) {
            proposalGenerator.addVariableProposals(file, argument, documentOffset, proposalSets, maxVariableCharPos, maxSettingCharPos);
        }
        return proposalSets;
    }

    private PriorityDeque<RobotCompletionProposalSet> createProposalSets() {
        return new ArrayPriorityDeque<RobotCompletionProposalSet>(4, new Prioritizer<RobotCompletionProposalSet>() {
            @Override
            public int prioritize(RobotCompletionProposalSet t) {
                return (t.isPriorityProposal() ? 0 : 1) + (t.isBasedOnInput() ? 0 : 2);
            }
        });
    }

    private ICompletionProposal[] extractMostRelevantProposals(PriorityDeque<RobotCompletionProposalSet> proposalSets) {
        for (Iterator<RobotCompletionProposalSet> proposalSetIt = proposalSets.iterator(); proposalSetIt.hasNext();) {
            RobotCompletionProposalSet proposalSet = proposalSetIt.next();
            if (proposalSet.getProposals().isEmpty()) {
                proposalSetIt.remove();
                continue;
            }
        }
        int lowestPriority = proposalSets.peekLowestPriority();
        boolean isEmpty = lowestPriority == -1;
        if (isEmpty) {
            return null;
        }
        if (lowestPriority < 2) {
            // we have got proposals based on input, so remove proposals not based on input
            proposalSets.clear(2, proposalSets.getNumberOfPriorityLevels() - 1);
        }

        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        for (RobotCompletionProposalSet proposalSet : proposalSets) {
            proposals.addAll(proposalSet.getProposals());
        }
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    private TableType determineTableTypeForLine(List<RobotLine> lines, int lineNo) {
        for (int i = lineNo; i >= 0; --i) {
            TableType tableType = lines.get(i).type.tableType;
            if (tableType != TableType.UNKNOWN) {
                return tableType;
            }
        }
        return TableType.UNKNOWN;
    }

    /**
     * Since there is no argument for the current cursor position (otherwise this method wouldn't have been called),
     * figure out which argument it would be by fake-inserting a dummy character at that position. After parsing the
     * file with the dummy character included, grab the argument that now resolves for the cursor position. Then undo
     * the added dummy character from that argument and return the resulting argument, which is possibly empty, but
     * which has a suitable {@link ArgumentType} assigned to it. This type thus indicates what type the argument would
     * be should the user choose to use any of the content assist suggestions, and lets us decide what content assist
     * suggestions to show in the first place.
     * 
     * @return the synthesized argument
     */
    private ParsedString synthesizeArgument(String documentText, int documentOffset, int lineNo) {
        StringBuilder newText = new StringBuilder(documentText.length() + 3);
        newText.append(documentText, 0, documentOffset);
        newText.append('x'); // dummy character
        newText.append(documentText, documentOffset, documentText.length());
        List<RobotLine> lines = RobotFile.parse(newText.toString()).getLines();
        RobotLine robotLine = lines.get(lineNo);
        ParsedString synthesizedArgument = robotLine.getArgumentAt(documentOffset);
        assert synthesizedArgument != null;
        assert synthesizedArgument.getArgCharPos() == documentOffset;
        String synthesizedArgumentWithoutDummyCharacter = synthesizedArgument.getValue().substring(1);
        ParsedString actualArgument = new ParsedString(synthesizedArgumentWithoutDummyCharacter, documentOffset, synthesizedArgument.getArgumentIndex());
        actualArgument.copyTypeVariablesFrom(synthesizedArgument);
        return actualArgument;
    }

}
