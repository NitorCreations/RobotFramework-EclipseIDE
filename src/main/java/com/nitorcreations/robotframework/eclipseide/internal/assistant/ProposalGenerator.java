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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class ProposalGenerator implements IProposalGenerator {
    @Override
    public void addKeywordProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposal> proposals) {
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        KeywordCompletionMatchVisitorProvider visitorProvider = new KeywordCompletionMatchVisitorProvider(file, replacementRegion);
        proposals.addAll(computeCompletionProposals(file, documentOffset, argument, visitorProvider));
    }

    @Override
    public void addVariableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposal> proposals, boolean allowOnlyLocalVariables) {
        IRegion replacementRegion = VariableReplacementRegionCalculator.calculate(argument, documentOffset);
        VariableCompletionMatchVisitorProvider visitorProvider = new VariableCompletionMatchVisitorProvider(file, replacementRegion, allowOnlyLocalVariables);
        List<RobotCompletionProposal> variableProposals = computeCompletionProposals(file, documentOffset, argument, visitorProvider);
        if (replacementRegion.getLength() > 0) {
            // the cursor is positioned for replacing a variable, so put the variable proposals first
            proposals.addAll(0, variableProposals);
        } else {
            // default positioning of proposals
            proposals.addAll(variableProposals);
        }
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

}
