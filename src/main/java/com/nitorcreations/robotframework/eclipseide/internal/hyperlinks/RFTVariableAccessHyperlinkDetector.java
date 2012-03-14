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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.Type;
import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTArgumentUtils;
import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTVariableUtils;

/**
 * This hyperlink detector creates hyperlinks for variable accesses, e.g.
 * "  SomeKeyword ${variable}" --> "${variable}" is linked, but
 * "Some ${arg} testcase" is not, and "  [Arguments] ${foo}" isn't either.
 * 
 * @author xkr47
 */
public class RFTVariableAccessHyperlinkDetector extends HyperlinkDetector {

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

        IRegion lineInfo;
        String line;
        try {
            lineInfo = document.getLineInformationOfOffset(offset);
            line = document.get(lineInfo.getOffset(), lineInfo.getLength());
        } catch (BadLocationException ex) {
            return null;
        }

        // this is a hack - it doesn't detect properly if [Arguments] is INSIDE
        // a variable or if [ is
        // quoted with \[
        int argumentsIdx = line.indexOf("[Arguments]");
        if (argumentsIdx != -1) {
            // strip the rest of the line
            line = line.substring(0, argumentsIdx);
        }

        // TODO do variables work in the SETTINGS table?

        int start = RFTArgumentUtils.findNextArgumentStart(line, 0);
        if (start == -1) {
            if (line.startsWith("${") || line.startsWith(" ${")) {
                start = RFTArgumentUtils.calculateArgumentLength(line, 0);
            } else {
                // testcase & keyword definitions fit into this category..
                // nothing interesting on those
                // lines
                return null;
            }
        }

        final int offsetInLine = offset - lineInfo.getOffset();
        while (true) {
            int linkOffsetInLine = RFTVariableUtils.findNextVariableStart(line, start);
            if (linkOffsetInLine == -1) {
                // after last variable
                return null;
            }
            if (offsetInLine < linkOffsetInLine) {
                // first variable is further down the line, so no more variables
                // are reachable
                return null;
            }

            int linkLength = RFTVariableUtils.calculateVariableLength(line, linkOffsetInLine);
            if (offsetInLine < linkOffsetInLine + linkLength) {
                // pointing at variable access!
                String linkString = line.substring(linkOffsetInLine, linkOffsetInLine + linkLength);
                IRegion linkRegion = new Region(lineInfo.getOffset() + linkOffsetInLine, linkLength);
                IHyperlink[] links = getLinks(document, linkString, linkRegion, Type.VARIABLE_TABLE_LINE);
                if (links != null) {
                    return links;
                }
            }
            start = linkOffsetInLine + linkLength;
        }
    }
}
