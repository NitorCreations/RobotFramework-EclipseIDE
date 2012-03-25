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
import org.eclipse.swt.graphics.Image;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordInlineArgumentMatcher;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordMatchResult;
import com.nitorcreations.robotframework.eclipseide.internal.util.BaseDefinitionMatchVisitor;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class KeywordCompletionMatchVisitor extends BaseDefinitionMatchVisitor {
    private final ParsedString substring;
    private final int leftPos;
    private final List<RobotCompletionProposal> proposals;
    private final int rightPos;
    private final RobotLine rfeLine;
    private final int replacePos;

    KeywordCompletionMatchVisitor(IFile file, ParsedString substring, int leftPos, List<RobotCompletionProposal> proposals, int rightPos, RobotLine rfeLine, int replacePos) {
        super(file);
        this.substring = substring;
        this.leftPos = leftPos;
        this.proposals = proposals;
        this.rightPos = rightPos;
        this.rfeLine = rfeLine;
        this.replacePos = replacePos;
    }

    @Override
    public VisitorInterest visitMatch(ParsedString match, IFile location) {
        if (substring != null) {
            // TODO this approach makes substring match any keyword with an
            // inline variable
            String lookFor = "${_}" + substring.getValue().toLowerCase() + "${_}";
            if (KeywordMatchResult.DIFFERENT == KeywordInlineArgumentMatcher.match(match.getValue().toLowerCase(), lookFor)) {
                // if (!match.getValue().contains(substring.getValue())) {
                return VisitorInterest.CONTINUE;
            }
        }
        Image image = null;
        String displayString = getFilePrefix(location) + match.getValue();
        String additionalProposalInfo = "I recommend: " + match.getValue();

        String informationDisplayString = "You chose: " + match.getValue();

        String replacementString = match.getValue();
        int replacementOffset = rfeLine.lineCharPos + replacePos;
        int replacementLength = rightPos - leftPos;
        int cursorPosition = replacementString.length();

        proposals.add(new RobotCompletionProposal(match, location, replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, informationDisplayString, additionalProposalInfo));
        return VisitorInterest.CONTINUE;
    }
}