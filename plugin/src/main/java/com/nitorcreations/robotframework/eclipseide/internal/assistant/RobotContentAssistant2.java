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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IAttemptGenerator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IProposalSuitabilityDeterminer;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IRelevantProposalsFilter;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.RobotCompletionProposalSet;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.VisitorInfo;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

/**
 * This class first makes sure we have an <b>argument</b> that is the target to generate proposals for, then uses an
 * {@link IProposalSuitabilityDeterminer} to produce a <b>set of proposal generators</b> to be used, then an
 * {@link IAttemptGenerator} to produce <b>proposals</b> using the proposal generators, and finally an
 * {@link IRelevantProposalsFilter} to extract the <b>most relevant proposals</b> considering the different types of
 * proposals that were generated.
 */
public class RobotContentAssistant2 implements IRobotContentAssistant2 {

    private final IProposalSuitabilityDeterminer proposalSuitabilityDeterminer;
    private final IAttemptGenerator attemptGenerator;
    private final IRelevantProposalsFilter relevantProposalsFilter;

    public RobotContentAssistant2(IProposalSuitabilityDeterminer proposalSuitabilityDeterminer, IAttemptGenerator attemptGenerator, IRelevantProposalsFilter relevantProposalsFilter) {
        this.proposalSuitabilityDeterminer = proposalSuitabilityDeterminer;
        this.attemptGenerator = attemptGenerator;
        this.relevantProposalsFilter = relevantProposalsFilter;
    }

    @Override
    public ICompletionProposal[] generateProposals(IFile file, int documentOffset, String documentText, List<RobotLine> lines, int lineNo) {
        RobotLine robotLine = lines.get(lineNo);
        ParsedString argument = robotLine.getArgumentAt(documentOffset);
        if (argument == null) {
            argument = synthesizeArgument(documentText, documentOffset, lineNo);
        }

        return generateProposalsForArgument(file, argument, documentOffset, robotLine.lineCharPos);
    }

    private ICompletionProposal[] generateProposalsForArgument(IFile file, ParsedString argument, int documentOffset, int lineCharPos) {
        List<VisitorInfo> visitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
        List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
        for (VisitorInfo visitorInfo : visitors) {
            attemptGenerator.acceptAttempts(visitorInfo.visitorArgument, documentOffset, proposalSets, visitorInfo.visitior);
        }
        return relevantProposalsFilter.extractMostRelevantProposals(proposalSets);
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
        ParsedString actualArgument = new ParsedString(synthesizedArgumentWithoutDummyCharacter, documentOffset, synthesizedArgument.getArgumentIndex()).setHasSpaceAfter(synthesizedArgument.hasSpaceAfter());
        actualArgument.copyTypeVariablesFrom(synthesizedArgument);
        return actualArgument;
    }

}
