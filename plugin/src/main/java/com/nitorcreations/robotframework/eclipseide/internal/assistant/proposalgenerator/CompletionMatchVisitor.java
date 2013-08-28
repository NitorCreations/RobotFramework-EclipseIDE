/**
 * Copyright 2012-2013 Nitor Creations Oy
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

import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.nitorcreations.robotframework.eclipseide.internal.util.BaseDefinitionMatchVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;

public abstract class CompletionMatchVisitor extends BaseDefinitionMatchVisitor {
    protected final String userInput;
    protected final List<RobotCompletionProposal> proposals;
    private final IRegion replacementRegion;
    protected final HashSet<String> addedProposals = new HashSet<String>();

    public CompletionMatchVisitor(IFile file, String userInput, List<RobotCompletionProposal> proposals, IRegion replacementRegion) {
        super(file);
        this.userInput = userInput;
        this.proposals = proposals;
        this.replacementRegion = replacementRegion;
    }

    protected void addProposal(String proposal, FileWithType proposalLocation) {
        Image image = null;
        String displayString = getDisplayString(proposal, proposalLocation);
        String replacementString = proposal;
        String additionalProposalInfo = "I recommend: " + replacementString;
        String informationDisplayString = "You chose: " + replacementString;
        proposals.add(new RobotCompletionProposal(proposal, proposalLocation, replacementRegion, image, displayString, informationDisplayString, additionalProposalInfo));
        addedProposals.add(proposal.toLowerCase());
    }
}
