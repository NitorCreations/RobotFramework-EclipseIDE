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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELexer;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFEPreParser;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public abstract class HyperlinkDetector implements IHyperlinkDetector {
    protected IHyperlink[] getLinks(IDocument document, String linkString, IRegion linkRegion, LineType type) {
        try {
            List<RFELine> lines = new RFELexer(document).lex();
            new RFEPreParser(document, lines).preParse();
            for (RFELine rfeLine : lines) {
                if (rfeLine.isType(type)) {
                    ParsedString firstArgument = rfeLine.arguments.get(0);
                    if (firstArgument.equals(linkString)) {
                        IRegion targetRegion = new Region(firstArgument.getArgEndCharPos(), 0);
                        return new IHyperlink[] { new RFTHyperlink(linkRegion, linkString, targetRegion, document) };
                    }
                }
            }
        } catch (CoreException e) {
            // ignored
        }
        return null;
    }

}
