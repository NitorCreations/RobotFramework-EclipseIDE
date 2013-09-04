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
package com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.nitorcreations.robotframework.eclipseide.internal.util.ArrayPriorityDeque;
import com.nitorcreations.robotframework.eclipseide.internal.util.Prioritizer;
import com.nitorcreations.robotframework.eclipseide.internal.util.PriorityDeque;

public class RelevantProposalsFilter implements IRelevantProposalsFilter {

    private static final int NOT_PRIORITY_PROPOSAL_MASK = 1 << 0;
    private static final int NOT_BASED_ON_INPUT_MASK = 1 << 1;

    @Override
    public ICompletionProposal[] extractMostRelevantProposals(List<RobotCompletionProposalSet> proposalSets2) {
        PriorityDeque<RobotCompletionProposalSet> proposalSets = removeEmptyProposalSets(proposalSets2);

        int lowestPriority = proposalSets.peekLowestPriority();
        boolean isEmpty = lowestPriority == -1;
        if (isEmpty) {
            return null;
        }
        if (lowestPriority < NOT_BASED_ON_INPUT_MASK) {
            // we have got proposals based on input, so remove proposals not based on input
            proposalSets.clear(NOT_BASED_ON_INPUT_MASK, proposalSets.getNumberOfPriorityLevels() - 1);
        }

        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        for (RobotCompletionProposalSet proposalSet : proposalSets) {
            proposals.addAll(proposalSet.getProposals());
        }
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    private PriorityDeque<RobotCompletionProposalSet> removeEmptyProposalSets(Collection<RobotCompletionProposalSet> proposalSets) {
        PriorityDeque<RobotCompletionProposalSet> proposalSets2 = createProposalSets();
        for (Iterator<RobotCompletionProposalSet> proposalSetIt = proposalSets.iterator(); proposalSetIt.hasNext();) {
            RobotCompletionProposalSet proposalSet = proposalSetIt.next();
            if (!proposalSet.getProposals().isEmpty()) {
                proposalSets2.add(proposalSet);
            }
        }
        return proposalSets2;
    }

    private PriorityDeque<RobotCompletionProposalSet> createProposalSets() {
        return new ArrayPriorityDeque<RobotCompletionProposalSet>(4, new Prioritizer<RobotCompletionProposalSet>() {
            @Override
            public int prioritize(RobotCompletionProposalSet t) {
                return (t.isPriorityProposal() ? 0 : NOT_PRIORITY_PROPOSAL_MASK) + (t.isBasedOnInput() ? 0 : NOT_BASED_ON_INPUT_MASK);
            }
        });
    }
}
