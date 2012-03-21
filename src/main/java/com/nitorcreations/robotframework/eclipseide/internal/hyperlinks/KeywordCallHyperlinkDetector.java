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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.internal.rules.RFTArgumentUtils;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

/**
 * This hyperlink detector creates hyperlinks for keyword calls, e.g.
 * "  SomeKeyword FooArgument" --> "SomeKeyword" is linked.
 * 
 * @author xkr47
 */
public class KeywordCallHyperlinkDetector extends HyperlinkDetector {

    @Override
    protected IHyperlink[] getLinks(IDocument document, RFELine rfeLine, ParsedString argument, int offset) {
        if (argument.getType() != ArgumentType.KEYWORD_CALL) {
            return null;
        }
        String linkString = argument.getUnescapedValue();
        IRegion linkRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        IHyperlink[] links = getLinks(document, linkString, linkRegion, LineType.KEYWORD_TABLE_KEYWORD_BEGIN);
        if (links == null) {
            // try without possible BDD prefix
            String alternateValue = argument.getAlternateValue();
            if (alternateValue != null) {
                int origLength = linkString.length();
                linkString = RFTArgumentUtils.unescapeArgument(alternateValue, 0, alternateValue.length());
                int lengthDiff = origLength - linkString.length();
                linkRegion = new Region(argument.getArgCharPos() + lengthDiff, argument.getValue().length() - lengthDiff);
                links = getLinks(document, linkString, linkRegion, LineType.KEYWORD_TABLE_KEYWORD_BEGIN);
            }
        }
        return links;
    }
}
