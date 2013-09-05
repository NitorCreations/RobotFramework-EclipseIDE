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
package com.nitorcreations.robotframework.eclipseide.internal.hyperlinks;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordInlineArgumentMatcher;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordMatchResult;
import com.nitorcreations.robotframework.eclipseide.internal.rules.ArgumentUtils;
import com.nitorcreations.robotframework.eclipseide.internal.util.BaseDefinitionMatchVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

/**
 * This hyperlink detector creates hyperlinks for keyword calls, e.g. *
 * <ul>
 * <li><tt>SomeKeyword &nbsp;${variable}</tt> - "SomeKeyword" is linked)</li>
 * <li><tt>Some ${inlinearg} testcase</tt> - "Some ${inlinearg} testcase" is linked</li>
 * <li><tt>[Arguments] &nbsp;${foo}</tt> - "${foo}" is linked</li>
 * </ul>
 */
public class KeywordCallHyperlinkDetector extends HyperlinkDetector {

    private static final class KeywordHyperlinkMatchVisitor extends BaseDefinitionMatchVisitor {
        private final IRegion linkRegion;
        private final String linkString;
        private final List<IHyperlink> links;

        KeywordHyperlinkMatchVisitor(String linkString, IRegion linkRegion, IFile file, List<IHyperlink> links) {
            super(file);
            this.linkRegion = linkRegion;
            this.linkString = linkString;
            this.links = links;
        }

        @Override
        public VisitorInterest visitMatch(ParsedString match, FileWithType location) {
            if (location.getFile() == null) {
                return VisitorInterest.CONTINUE;
            }
            String matchString = getMatchStringInFile(location, linkString);
            KeywordMatchResult matchResult = KeywordInlineArgumentMatcher.match(match.getValue().toLowerCase(), matchString);
            if (matchResult != KeywordMatchResult.DIFFERENT) {
                IRegion targetRegion = new Region(match.getArgEndCharPos(), 0);
                links.add(new Hyperlink(linkRegion, getDisplayString(match.getValue(), location), targetRegion, location));
                return VisitorInterest.CONTINUE_TO_END_OF_CURRENT_PRIORITY_LEVEL;
            }
            return VisitorInterest.CONTINUE;
        }

        private String getMatchStringInFile(FileWithType location, String linkString) {
            String filePrefix = location.getName() + ".";
            if (linkString.startsWith(filePrefix)) {
                return linkString.substring(filePrefix.length()).toLowerCase();
            }
            return linkString.toLowerCase();
        }

        @Override
        public LineType getWantedLineType() {
            return LineType.KEYWORD_TABLE_KEYWORD_BEGIN;
        }

        @Override
        public boolean visitImport(IFile sourceFile, RobotLine line) {
            return true;
        }
    }

    @Override
    protected void getLinks(IFile file, RobotLine rfeLine, ParsedString argument, int offset, List<IHyperlink> links) {
        if (argument.getType() != ArgumentType.KEYWORD_CALL && argument.getType() != ArgumentType.KEYWORD_CALL_DYNAMIC) {
            return;
        }
        String linkString = argument.getUnescapedValue();
        IRegion linkRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        DefinitionFinder.acceptMatches(file, new KeywordHyperlinkMatchVisitor(linkString, linkRegion, file, links));
        if (links.isEmpty()) {
            // try without possible BDD prefix
            String alternateValue = argument.getAlternateValue();
            if (alternateValue != null) {
                int origLength = linkString.length();
                linkString = ArgumentUtils.unescapeArgument(alternateValue, 0, alternateValue.length());
                int lengthDiff = origLength - linkString.length();
                linkRegion = new Region(argument.getArgCharPos() + lengthDiff, argument.getValue().length() - lengthDiff);
                DefinitionFinder.acceptMatches(file, new KeywordHyperlinkMatchVisitor(linkString, linkRegion, file, links));
            }
        }
    }
}
