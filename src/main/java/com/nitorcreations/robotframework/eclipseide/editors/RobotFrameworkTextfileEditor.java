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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;

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

    public static final String EDITOR_ID = RobotFrameworkTextfileEditor.class.getName();

    private final ColorManager colorManager;

    public RobotFrameworkTextfileEditor() {
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new RFTSourceViewerConfiguration(colorManager));
        setDocumentProvider(new RFTDocumentProvider(this));
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        handleClosePossiblyOpenDocument();
        super.doSetInput(input);
        if (input != null) {
            handleOpenDocument();
        }
    }

    @Override
    public void dispose() {
        handleClosePossiblyOpenDocument();
        colorManager.dispose();
        super.dispose();
    }

    private void handleClosePossiblyOpenDocument() {
        IEditorInput old = getEditorInput();
        if (old != null) {
            handleCloseDocument(old);
        }
    }

    private void handleOpenDocument() {
        IDocument document = getEditedDocument();
        System.out.println("Opened document " + getEditorInput() + " -> " + document);
        ResourceManager.registerEditor(this);
    }

    private void handleCloseDocument(IEditorInput old) {
        System.out.println("Closing document " + old);
        ResourceManager.unregisterEditor(this);
        RobotFile.erase(getEditedDocument());
    }

    public IDocument getEditedDocument() {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    public IFile getEditedFile() {
        return (IFile) getEditorInput().getAdapter(IFile.class);
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

// 190312 1720 xxxx
