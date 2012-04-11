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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.nitorcreations.robotframework.eclipseide.internal.util.BaseDefinitionMatchVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public abstract class CompletionMatchVisitor extends BaseDefinitionMatchVisitor {
    protected final ParsedString userInput;
    private final List<RobotCompletionProposal> proposals;
    private final IRegion replacementRegion;

    public CompletionMatchVisitor(IFile file, ParsedString userInput, List<RobotCompletionProposal> proposals, IRegion replacementRegion) {
        super(file);
        this.userInput = userInput;
        this.proposals = proposals;
        this.replacementRegion = replacementRegion;
    }

    protected void addProposal(ParsedString proposal, FileWithType proposalLocation) {
        Image image = null;
        String displayString = getFilePrefix(proposalLocation) + proposal.getValue();
        String additionalProposalInfo = "I recommend: " + proposal.getValue();
        String informationDisplayString = "You chose: " + proposal.getValue();
        String replacementString = proposal.getValue();
        int cursorPosition = replacementString.length();
        proposals.add(new RobotCompletionProposal(proposal, proposalLocation, replacementString, replacementRegion, cursorPosition, image, displayString, informationDisplayString, additionalProposalInfo));
    }
}
