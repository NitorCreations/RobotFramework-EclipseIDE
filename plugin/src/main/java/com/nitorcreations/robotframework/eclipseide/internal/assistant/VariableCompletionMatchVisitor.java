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

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class VariableCompletionMatchVisitor extends CompletionMatchVisitor {

    private final int maxVariableCharPos;
    private final int maxSettingCharPos;

    public VariableCompletionMatchVisitor(IFile file, ParsedString userInput, List<RobotCompletionProposal> proposals, IRegion replacementRegion, int maxVariableCharPos, int maxSettingCharPos) {
        super(file, userInput, proposals, replacementRegion);
        this.maxVariableCharPos = maxVariableCharPos;
        this.maxSettingCharPos = maxSettingCharPos;
    }

    @Override
    public VisitorInterest visitMatch(ParsedString proposal, FileWithType proposalLocation) {
        if (proposal.getArgCharPos() > maxVariableCharPos) {
            return VisitorInterest.STOP;
        }

        if (userInput == null || proposal.getUnescapedValue().toLowerCase().contains(userInput.getUnescapedValue().toLowerCase())) {
            if (!addedProposals.contains(proposal.getValue().toLowerCase())) {
                addProposal(proposal, proposalLocation);
            }
        }

        return VisitorInterest.CONTINUE;
    }

    @Override
    public LineType getWantedLineType() {
        return LineType.VARIABLE_TABLE_LINE;
    }

    @Override
    public boolean visitImport(IFile sourceFile, RobotLine line) {
        return line.lineCharPos <= maxSettingCharPos;
    }
}
