package com.nitorcreations.robotframework.eclipseide.internal.hyperlinks;

import java.util.List;

import org.eclipse.core.resources.IFile;
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
            IFile targetJavaFile = PluginContext.getResourceManager().getJavaFile(fullyQualifiedName);
            if (targetJavaFile != null) {
                links.add(createLinkForArgument(argument, targetJavaFile, fullyQualifiedName));
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
