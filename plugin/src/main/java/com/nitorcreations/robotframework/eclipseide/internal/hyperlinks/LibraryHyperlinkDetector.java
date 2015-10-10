/**
 * Copyright 2014 Nitor Creations Oy, SmallGreenET
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
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileType;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

/**
 * This hyperlink detector creates hyperlinks for library references in any projects of the workspace, e.g.
 * <ul>
 * <li><tt>Library com.company.TestLib</tt> - "TestLib.java" is linked</li>
 * </ul>
 */
public class LibraryHyperlinkDetector extends HyperlinkDetector {

    @Override
    protected void getLinks(IFile file, RobotLine rfeLine, ParsedString argument, int offset, List<IHyperlink> links) {
        if (isLibraryLineWithFileArgument(rfeLine, argument)) {
            String fullyQualifiedName = argument.getUnescapedValue();
            Map<IFile, IPath> targetJavaFiles = PluginContext.getResourceManager().getJavaFiles(fullyQualifiedName);
            for (Entry<IFile, IPath> targetJavaFile : targetJavaFiles.entrySet()) {
                String linkText = fullyQualifiedName + " in " + targetJavaFile.getValue().toString();
                links.add(createLinkForArgument(argument, targetJavaFile.getKey(), linkText));
            }
        }
    }

    private boolean isLibraryLineWithFileArgument(RobotLine rfeLine, ParsedString argument) {
        return rfeLine.isLibrarySetting() && argument.getType() == ArgumentType.SETTING_FILE;
    }

    private Hyperlink createLinkForArgument(ParsedString argument, IFile targetFile, String linkText) {
        IRegion linkRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        FileWithType targetFileWithType = new FileWithType(FileType.LIBRARY, targetFile);
        return new Hyperlink(linkRegion, linkText, null, targetFileWithType);
    }
}
