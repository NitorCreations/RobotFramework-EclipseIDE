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
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTArgumentUtils;

/**
 * This hyperlink detector creates hyperlinks for resource references, e.g.
 * "Resource foo.txt" --> "foo.txt" is linked.
 * 
 * @author xkr47
 */
public class RFTResourceHyperlinkDetector implements IHyperlinkDetector {

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

        // TODO detect if we are in the SETTINGS table
        int linkOffsetInLine;
        if (line.startsWith("Resource")) {
            linkOffsetInLine = 8;
        } else if (line.startsWith(" Resource")) {
            linkOffsetInLine = 9;
        } else {
            return null;
        }

        linkOffsetInLine = RFTArgumentUtils.findNextArgumentStart(line, linkOffsetInLine);
        if (linkOffsetInLine == -1) {
            return null;
        }

        int linkPos = linkOffsetInLine;
        int linkEnd;
        do {
            linkEnd = linkPos + RFTArgumentUtils.calculateArgumentLength(line, linkPos);
            linkPos = RFTArgumentUtils.findNextArgumentStart(line, linkEnd);
        } while (linkPos != -1);
        int linkLength = linkEnd - linkOffsetInLine;

        int offsetInLine = offset - lineInfo.getOffset();
        if (offsetInLine < linkOffsetInLine || offsetInLine >= linkOffsetInLine + linkLength) {
            // outside
            return null;
        }

        String linkString = RFTArgumentUtils.unescapeArgument(line, linkOffsetInLine, linkLength);
        IRegion linkRegion = new Region(lineInfo.getOffset() + linkOffsetInLine, linkLength);
        return new IHyperlink[] { new RFTResourceHyperlink(linkRegion, linkString) };
    }

}
