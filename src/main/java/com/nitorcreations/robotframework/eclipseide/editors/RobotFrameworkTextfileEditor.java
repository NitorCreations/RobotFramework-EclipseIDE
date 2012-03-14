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
package com.nitorcreations.robotframework.eclipseide.editors;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * https://robotframework.googlecode.com/hg/doc/userguide/
 * RobotFrameworkUserGuide.html?r=2.6.1 http:/
 * /help.eclipse.org/helios/index.jsp
 * ?topic=/org.eclipse.platform.doc.isv/reference/api/org/ eclipse
 * /jface/text/source/package-summary.html
 * 
 * @author xkr47
 */
public class RobotFrameworkTextfileEditor extends TextEditor {

    private final ColorManager colorManager;

    public RobotFrameworkTextfileEditor() {
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new RFTSourceViewerConfiguration(colorManager));
        setDocumentProvider(new RFTDocumentProvider());
        EditorResolver.setLastOpenedEditor(this);
    }

    @Override
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

    @Override
    protected void initializeViewerColors(ISourceViewer viewer) {
        super.initializeViewerColors(viewer);
        colorManager.setDarkBackgroundScheme(isDarkBackground(viewer));
    }

    private boolean isDarkBackground(ISourceViewer viewer) {
        Color background = viewer.getTextWidget().getBackground();
        int lightness = background.getBlue() * 11 + background.getGreen() * 59 + background.getRed() * 30;
        return lightness < 12800;
    }
}
