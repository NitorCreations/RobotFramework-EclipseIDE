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

import static com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordInlineArgumentMatcher.match;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordMatchResult;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class KeywordCompletionMatchVisitor extends CompletionMatchVisitor {

    public KeywordCompletionMatchVisitor(IFile file, ParsedString argument, List<RobotCompletionProposal> proposals, IRegion replacementRegion) {
        super(file, argument, proposals, replacementRegion);
    }

    @Override
    public VisitorInterest visitMatch(ParsedString match, IFile location) {
        if (substring != null) {
            String substringValue = substring.getValue().toLowerCase();
            String matchValue = match.getValue().toLowerCase();
            if (KeywordMatchResult.DIFFERENT == match(matchValue, lookFor(substringValue))) {
                if (!prefixesMatch(substringValue, location)) {
                    return VisitorInterest.CONTINUE;
                }
                if (KeywordMatchResult.DIFFERENT == match(matchValue, lookFor(valueWithoutPrefix(substringValue)))) {
                    return VisitorInterest.CONTINUE;
                }
            }
        }
        addProposal(match, location);
        return VisitorInterest.CONTINUE;
    }

    private String valueWithoutPrefix(String value) {
        return value.substring(value.indexOf('.') + 1);
    }

    private boolean prefixesMatch(String substringValue, IFile location) {
        int indexOfDot = substringValue.indexOf('.');
        if (indexOfDot == -1) {
            return false;
        }
        String substringPrefix = substringValue.substring(0, indexOfDot + 1);
        return location.getName().startsWith(substringPrefix);
    }

    private String lookFor(String value) {
        // TODO this approach makes substring match any keyword with an inline variable
        return "${_}" + value + "${_}";
    }

    @Override
    public LineType getWantedLineType() {
        return LineType.KEYWORD_TABLE_KEYWORD_BEGIN;
    }
}
