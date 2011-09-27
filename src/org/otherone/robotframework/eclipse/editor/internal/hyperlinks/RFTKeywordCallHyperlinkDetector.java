/**
 * Copyright 2011 Nitor Creations Oy
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
package org.otherone.robotframework.eclipse.editor.internal.hyperlinks;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.otherone.robotframework.eclipse.editor.internal.rules.RFTArgumentUtils;

/**
 * This hyperlink detector creates hyperlinks for keyword calls, e.g. "  SomeKeyword FooArgument"
 * --> "SomeKeyword" is linked.
 * 
 * @author xkr47
 */
public class RFTKeywordCallHyperlinkDetector implements IHyperlinkDetector {

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

    int linkOffsetInLine = RFTArgumentUtils.findNextArgumentStart(line, 0);
    if (linkOffsetInLine == -1) {
      // testcase & keyword definitions fit into this category
      return null;
    }

    if (line.charAt(linkOffsetInLine) == '[') {
      // it's a Setting
      return null;
    }

    int linkLength = RFTArgumentUtils.calculateArgumentLength(line, linkOffsetInLine);

    int offsetInLine = offset - lineInfo.getOffset();
    if (offsetInLine < linkOffsetInLine || offsetInLine >= linkOffsetInLine + linkLength) {
      // outside
      return null;
    }

    String linkString = RFTArgumentUtils.unescapeArgument(line, linkOffsetInLine, linkLength);

    IRegion linkRegion = new Region(lineInfo.getOffset() + linkOffsetInLine, linkLength);
    return new IHyperlink[] { new RFTKeywordCallHyperlink(linkRegion, linkString) };
  }

}
