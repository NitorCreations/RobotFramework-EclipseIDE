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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTVariableUtils;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

/**
 * This hyperlink detector creates hyperlinks for variable accesses, e.g.
 * "  SomeKeyword ${variable}" --> "${variable}" is linked, but
 * "Some ${arg} testcase" is not, and "  [Arguments] ${foo}" isn't either.
 * 
 * @author xkr47
 */
public class VariableAccessHyperlinkDetector extends HyperlinkDetector {

    /**
     * This detector assumes generated hyperlinks are static, i.e. the link
     * target is calculated at detection time and not changed even if the code
     * would update later.
     */
    @Override
    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        if (region == null || textViewer == null) {
            return null;
        }

        IDocument document = textViewer.getDocument();
        if (document == null) {
            return null;
        }

        int offset = region.getOffset();

        int lineNumber;
        try {
            lineNumber = document.getLineOfOffset(offset);
        } catch (BadLocationException ex) {
            return null;
        }

        List<RFELine> lines = RobotFile.getLines(document);
        if (lines.size() <= lineNumber) {
            return null;
        }

        RFELine rfeLine = lines.get(lineNumber);
        ParsedString argument = rfeLine.getArgumentAt(offset);
        // TODO: only check types that can contain variables
        if (argument == null) {
            return null;
        }

        String argumentValue = argument.getValue();
        int start = 0;
        while (true) {
            int linkOffsetInArgument = RFTVariableUtils.findNextVariableStart(argumentValue, start);
            if (linkOffsetInArgument == -1) {
                // after last variable
                return null;
            }
            if (offset < argument.getArgCharPos() + linkOffsetInArgument) {
                // before next variable
                return null;
            }
            int linkLength = RFTVariableUtils.calculateVariableLength(argumentValue, linkOffsetInArgument);
            if (offset < argument.getArgCharPos() + linkOffsetInArgument + linkLength) {
                // pointing at variable access!
                String linkString = argumentValue.substring(linkOffsetInArgument, linkOffsetInArgument + linkLength);
                IRegion linkRegion = new Region(argument.getArgCharPos() + linkOffsetInArgument, linkLength);
                IHyperlink[] links = getLinks(document, linkString, linkRegion, LineType.VARIABLE_TABLE_LINE);
                if (links != null) {
                    return links;
                }
            }
            start = linkOffsetInArgument + linkLength;
        }
    }
}
