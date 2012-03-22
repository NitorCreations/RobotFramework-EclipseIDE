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
package com.nitorcreations.robotframework.eclipseide.internal.hyperlinks;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTVariableUtils;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

/**
 * This hyperlink detector creates hyperlinks for variable accesses, e.g.
 * <ul>
 * <li><tt>SomeKeyword &nbsp;${variable}</tt> - "${variable}" is linked)</li>
 * <li><tt>Some ${inlinearg} testcase</tt> - "${inlinearg}" is linked</li>
 * <li><tt>[Arguments] &nbsp;${foo}</tt> - "${foo}" is linked</li>
 * </ul>
 */
public class VariableAccessHyperlinkDetector extends HyperlinkDetector {

    private static final class VariableMatchVisitor extends BaseMatchVisitor {
        private final IRegion linkRegion;
        private final String linkString;
        private final List<IHyperlink> links;

        VariableMatchVisitor(String linkString, IRegion linkRegion, IFile file, List<IHyperlink> links) {
            super(file);
            this.linkRegion = linkRegion;
            this.linkString = linkString;
            this.links = links;
        }

        @Override
        public boolean visitMatch(ParsedString match, IFile location) {
            if (match.getValue().equalsIgnoreCase(linkString)) {
                IRegion targetRegion = new Region(match.getArgEndCharPos(), 0);
                links.add(new Hyperlink(linkRegion, getFilePrefix(location) + match.getValue(), targetRegion, location));
            }
            return true;
        }
    }

    @Override
    protected void getLinks(IFile file, RFELine rfeLine, ParsedString argument, int offset, List<IHyperlink> links) {
        // TODO: only check types that can contain variables
        String argumentValue = argument.getValue();
        int start = 0;
        while (true) {
            int linkOffsetInArgument = RFTVariableUtils.findNextVariableStart(argumentValue, start);
            if (linkOffsetInArgument == -1) {
                // after last variable
                return;
            }
            if (offset < argument.getArgCharPos() + linkOffsetInArgument) {
                // before next variable
                return;
            }
            int linkLength = RFTVariableUtils.calculateVariableLength(argumentValue, linkOffsetInArgument);
            if (offset < argument.getArgCharPos() + linkOffsetInArgument + linkLength) {
                // pointing at variable access!
                String linkString = argumentValue.substring(linkOffsetInArgument, linkOffsetInArgument + linkLength);
                IRegion linkRegion = new Region(argument.getArgCharPos() + linkOffsetInArgument, linkLength);
                acceptMatches(file, LineType.VARIABLE_TABLE_LINE, new VariableMatchVisitor(linkString, linkRegion, file, links));
                return;
            }
            start = linkOffsetInArgument + linkLength;
        }
    }
}
