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
import com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class KeywordCompletionMatchVisitor extends CompletionMatchVisitor {

    public KeywordCompletionMatchVisitor(IFile file, ParsedString argument, List<RobotCompletionProposal> proposals, IRegion replacementRegion) {
        super(file, argument, proposals, replacementRegion);
    }

    @Override
    public VisitorInterest visitMatch(ParsedString proposal, FileWithType proposalLocation) {
        if (userInput == null) {
            addProposal(proposal, proposalLocation);
        } else {
            String userInputString = userInput.getValue().toLowerCase();
            String proposalString = proposal.getValue().toLowerCase();
            if (proposalString.contains(userInputString) || matchesWithoutPrefix(userInputString, proposalString, proposalLocation)) {
                addProposal(proposal, proposalLocation);
            }
            // if (KeywordMatchResult.DIFFERENT == match(proposalString, lookFor(userInputString))) {
            // if (!prefixesMatch(userInputString, proposalLocation)) {
            // return VisitorInterest.CONTINUE;
            // }
            // if (KeywordMatchResult.DIFFERENT == match(proposalString, lookFor(valueWithoutPrefix(userInputString))))
            // {
            // return VisitorInterest.CONTINUE;
            // }
            // }
        }
        return VisitorInterest.CONTINUE;
    }

    @Override
    protected void addProposal(ParsedString proposal, FileWithType proposalLocation) {
        String proposalString = proposal.getValue().toLowerCase();
        boolean proposalExisted = addedProposals.contains(proposalString);
        super.addProposal(proposal, proposalLocation);
        if (proposalExisted) {
            for (RobotCompletionProposal robotCompletionProposal : proposals) {
                setPrefixRequiredIfNeeded(proposalString, robotCompletionProposal);
            }
        }
    }

    private void setPrefixRequiredIfNeeded(String proposalString, RobotCompletionProposal robotCompletionProposal) {
        if (file == robotCompletionProposal.getMatchLocation().getFile()) {
            return;
        }
        if (robotCompletionProposal.getMatchArgument().getValue().toLowerCase().equals(proposalString)) {
            robotCompletionProposal.setPrefixRequired();
        }
    }

    private boolean matchesWithoutPrefix(String userInputString, String proposalString, FileWithType proposalLocation) {
        if (!prefixesMatch(userInputString, proposalLocation)) {
            return false;
        }
        String valueWithoutPrefix = userInputString.substring(userInputString.indexOf('.') + 1);
        return proposalString.contains(valueWithoutPrefix);
    }

    private boolean prefixesMatch(String userInputString, FileWithType proposalLocation) {
        int indexOfDot = userInputString.indexOf('.');
        if (indexOfDot == -1) {
            return false;
        }
        String userInputPrefix = userInputString.substring(0, indexOfDot);
        return proposalLocation.getName().toLowerCase().equals(userInputPrefix);
    }

    // private String lookFor(String value) {
    // // TODO this approach makes substring match any keyword with an inline variable
    // return "${_}" + value + "${_}";
    // }

    @Override
    public LineType getWantedLineType() {
        return LineType.KEYWORD_TABLE_KEYWORD_BEGIN;
    }

    @Override
    public boolean visitImport(IFile sourceFile, RobotLine line) {
        return true;
    }
}
