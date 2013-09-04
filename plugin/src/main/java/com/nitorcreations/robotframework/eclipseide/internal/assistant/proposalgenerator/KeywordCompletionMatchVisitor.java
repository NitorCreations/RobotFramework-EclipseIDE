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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class KeywordCompletionMatchVisitor extends CompletionMatchVisitor {

    public KeywordCompletionMatchVisitor(IFile file, String argument, List<RobotCompletionProposal> proposals, IRegion replacementRegion) {
        super(file, argument, proposals, replacementRegion);
    }

    @Override
    public VisitorInterest visitMatch(ParsedString match, FileWithType matchLocation) {
        if (userInput == null) {
            addProposal(match.getValue(), matchLocation);
        } else {
            String userInputStringLower = userInput.toLowerCase();
            String matchStringLower = match.getValue().toLowerCase();
            if (matchStringLower.contains(userInputStringLower) || matchesWithoutPrefix(userInputStringLower, matchStringLower, matchLocation)) {
                addProposal(match.getValue(), matchLocation);
            }
            // if (KeywordMatchResult.DIFFERENT == match(matchStringLower, lookFor(userInputStringLower))) {
            // if (!prefixesMatch(userInputStringLower, matchLocation)) {
            // return VisitorInterest.CONTINUE;
            // }
            // if (KeywordMatchResult.DIFFERENT == match(matchStringLower,
            // lookFor(valueWithoutPrefix(userInputStringLower))))
            // {
            // return VisitorInterest.CONTINUE;
            // }
            // }
        }
        return VisitorInterest.CONTINUE;
    }

    @Override
    protected void addProposal(String proposal, FileWithType proposalLocation) {
        String proposalStringLower = proposal.toLowerCase();
        boolean proposalExisted = addedProposals.contains(proposalStringLower);
        super.addProposal(proposal, proposalLocation);
        if (proposalExisted) {
            for (RobotCompletionProposal robotCompletionProposal : proposals) {
                setPrefixRequiredIfNeeded(proposalStringLower, robotCompletionProposal);
            }
        }
    }

    private void setPrefixRequiredIfNeeded(String proposalStringLower, RobotCompletionProposal robotCompletionProposal) {
        if (file == robotCompletionProposal.getMatchLocation().getFile()) {
            return;
        }
        if (robotCompletionProposal.getMatchArgument().toLowerCase().equals(proposalStringLower)) {
            robotCompletionProposal.setPrefixRequired();
        }
    }

    private boolean matchesWithoutPrefix(String userInputStringLower, String matchStringLower, FileWithType matchLocation) {
        if (!prefixesMatch(userInputStringLower, matchLocation)) {
            return false;
        }
        String valueWithoutPrefixLower = userInputStringLower.substring(userInputStringLower.indexOf('.') + 1);
        return matchStringLower.contains(valueWithoutPrefixLower);
    }

    private boolean prefixesMatch(String userInputStringLower, FileWithType location) {
        int indexOfDot = userInputStringLower.indexOf('.');
        if (indexOfDot == -1) {
            return false;
        }
        String userInputPrefixLower = userInputStringLower.substring(0, indexOfDot);
        return location.getName().toLowerCase().startsWith(userInputPrefixLower);
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
