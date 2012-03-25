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

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

/**
 * This hyperlink detector creates hyperlinks for resource references, e.g.
 * <ul>
 * <li><tt>Resource foo.txt</tt> - "foo.txt" is linked</li>
 * <li><tt>Variables &nbsp;foo.py &nbsp;argument</tt> - "foo.py" is linked</li>
 * </ul>
 */
public class ResourceHyperlinkDetector extends HyperlinkDetector {

    @Override
    protected void getLinks(IFile file, RobotLine rfeLine, ParsedString argument, int offset, List<IHyperlink> links) {
        boolean isResourceSetting = rfeLine.isResourceSetting();
        boolean isVariableSetting = rfeLine.isVariableSetting();
        if (!isResourceSetting && !isVariableSetting) {
            return;
        }
        ParsedString secondArgument = rfeLine.arguments.get(1);
        if (argument != secondArgument) {
            return;
        }
        String linkString = argument.getUnescapedValue();
        IFile targetFile = ResourceManager.getRelativeFile(file, linkString);
        if (!targetFile.exists()) {
            return;
        }
        IRegion linkRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        links.add(new Hyperlink(linkRegion, linkString, null, targetFile, isResourceSetting));
    }
}
