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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public abstract class HyperlinkDetector implements IHyperlinkDetector {

    protected List<RFELine> lines;

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
        lines = RobotFile.get(document).getLines();
        if (lineNumber >= lines.size()) {
            return null;
        }
        RFELine rfeLine = lines.get(lineNumber);
        ParsedString argument = rfeLine.getArgumentAt(offset);
        if (argument == null) {
            return null;
        }
        return getLinks(document, rfeLine, argument, offset);
    }

    protected abstract IHyperlink[] getLinks(IDocument document, RFELine rfeLine, ParsedString argument, int offset);

    protected IHyperlink[] getLinks(IDocument document, String linkString, IRegion linkRegion, LineType lineType) {
        Set<IFile> unprocessedFiles = new HashSet<IFile>();
        Set<IFile> processedFiles = new HashSet<IFile>();
        unprocessedFiles.add(ResourceManager.resolveFileFor(document));
        while (!unprocessedFiles.isEmpty()) {
            IFile targetFile = unprocessedFiles.iterator().next();
            RobotFile robotFile = RobotFile.get(targetFile, true);
            if (robotFile == null) {
                return null;
            }
            List<RFELine> lines = robotFile.getLines();
            for (RFELine line : lines) {
                if (line.isType(lineType)) {
                    ParsedString firstArgument = line.arguments.get(0);
                    if (firstArgument.equals(linkString)) {
                        IRegion targetRegion = new Region(firstArgument.getArgEndCharPos(), 0);
                        return new IHyperlink[] { new Hyperlink(linkRegion, linkString, targetRegion, targetFile) };
                    }
                }
            }
            for (RFELine line : lines) {
                if (line.isResourceSetting()) {
                    ParsedString secondArgument = line.arguments.get(1);
                    IFile resourceFile = ResourceManager.getRelativeFile(targetFile, secondArgument.getUnescapedValue());
                    if (resourceFile.exists() && !processedFiles.contains(resourceFile)) {
                        unprocessedFiles.add(resourceFile);
                    }
                }
            }
            processedFiles.add(targetFile);
            unprocessedFiles.remove(targetFile);
        }
        return null;
    }

}
