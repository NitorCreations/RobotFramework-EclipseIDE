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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public abstract class HyperlinkDetector implements IHyperlinkDetector {

    protected List<RobotLine> lines;

    /**
     * This detector assumes generated hyperlinks are static, i.e. the link target is calculated at detection time and
     * not changed even if the code would update later.
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
        lines = RobotFile.get(document).getLines();
        List<IHyperlink> links;
        try {
            RobotLine rfeLine = lines.get(lineNumber);
            ParsedString argument = rfeLine.getArgumentAt(offset);
            if (argument == null) {
                return null;
            }
            IFile file = PluginContext.getResourceManager().resolveFileFor(document);
            links = new ArrayList<IHyperlink>();
            getLinks(file, rfeLine, argument, offset, links);
        } finally {
            lines = null;
        }
        if (links.isEmpty()) {
            return null;
        }
        if (!canShowMultipleHyperlinks) {
            return new IHyperlink[] { links.get(0) };

        }
        return links.toArray(new IHyperlink[links.size()]);
    }

    protected abstract void getLinks(IFile file, RobotLine rfeLine, ParsedString argument, int offset, List<IHyperlink> links);

}
