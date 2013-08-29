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
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class AttemptGenerator implements IAttemptGenerator {

    @Override
    public void acceptAttempts(ParsedString argument, int documentOffset, Collection<RobotCompletionProposalSet> proposalSets, AttemptVisitor attemptVisitor) {
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        List<String> attempts = generateAttempts(argument, documentOffset);
        for (String attempt : attempts) {
            RobotCompletionProposalSet proposalSet = attemptVisitor.visitAttempt(attempt, replacementRegion);
            if (proposalsContainsOnly(proposalSet.getProposals(), argument)) {
                // Found a single exact hit - probably means it was content-assisted earlier and the user now wants to
                // change it to something else
                continue;
            }
            if (!proposalSet.getProposals().isEmpty()) {
                proposalSet.setBasedOnInput(!attempt.isEmpty());
                proposalSets.add(proposalSet);
                return;
            }
        }
    }

    private static boolean proposalsContainsOnly(List<RobotCompletionProposal> proposals, ParsedString argument) {
        return proposals.size() == 1 && proposals.get(0).getMatchArgument().equals(argument.getValue());
    }

    private static List<String> generateAttempts(ParsedString argument, int documentOffset) {
        String lookFor = argument.getValue().toLowerCase();
        List<String> attempts = new ArrayList<String>(3);
        attempts.add(lookFor);
        int argumentOffset = documentOffset - argument.getArgCharPos();
        if (argumentOffset > 0 && lookFor.length() > argumentOffset) {
            attempts.add(lookFor.substring(0, argumentOffset));
        }
        if (!lookFor.isEmpty()) {
            attempts.add("");
        }
        return attempts;
    }
}
