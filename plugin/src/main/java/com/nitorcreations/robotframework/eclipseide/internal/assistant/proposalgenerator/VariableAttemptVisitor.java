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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;

import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;

public class VariableAttemptVisitor implements AttemptVisitor {
    private final IFile file;
    private final int maxVariableCharPos;
    private final int maxSettingCharPos;

    public VariableAttemptVisitor(IFile file, int maxVariableCharPos, int maxSettingCharPos) {
        this.file = file;
        this.maxVariableCharPos = maxVariableCharPos;
        this.maxSettingCharPos = maxSettingCharPos;
    }

    @Override
    public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
        assert attempt.equals(attempt.toLowerCase());
        RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
        CompletionMatchVisitor visitor = new VariableCompletionMatchVisitor(file, attempt, ourProposalSet.getProposals(), replacementRegion, maxVariableCharPos, maxSettingCharPos);
        DefinitionFinder.acceptMatches(file, visitor);
        if (replacementRegion.getLength() > 0) {
            // the cursor is positioned for replacing a variable, so mark the variable proposals high priority
            ourProposalSet.setPriorityProposal();
        }
        return ourProposalSet;

        // TODO
        // if (replacementRegion.getLength() > 0) {
        // // the cursor is positioned for replacing a variable, so put the variable proposals first
        // proposalSets.add(0, variableProposals);
        // } else {
        // // default positioning of proposals
        // proposalSets.add(variableProposals);
        // }
    }
}
