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
package com.nitorcreations.robotframework.eclipseide.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

import com.nitorcreations.robotframework.eclipseide.Activator;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;

/**
 * https://robotframework.googlecode.com/hg/doc/userguide/ RobotFrameworkUserGuide.html?r=2.6.1 http:/
 * /help.eclipse.org/helios/index.jsp ?topic=/org.eclipse.platform.doc.isv/reference/api/org/ eclipse
 * /jface/text/source/package-summary.html
 * 
 * @author xkr47
 */
public class RobotFrameworkTextfileEditor extends TextEditor {

    public static final String EDITOR_ID = RobotFrameworkTextfileEditor.class.getName();

    private final ColorManager colorManager;

    public RobotFrameworkTextfileEditor() {
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new RobotSourceViewerConfiguration(colorManager));
        setDocumentProvider(new FileDocumentProvider());
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
        ResourceManagerProvider.get().registerEditor(this);
    }

    private void handleCloseDocument(IEditorInput old) {
        System.out.println("Closing document " + old);
        ResourceManagerProvider.get().unregisterEditor(this);
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
        /*
         * Workaround for issue mentioned in #36. Steps to repeat: 1. Comment out the line below 2. Change some robot
         * ide syntax color preference and apply the changes 3. change the default text editor foreground in
         * "General/Editors/Text Editors". As a result the editor colors are wrongly rendered. Making any change to the
         * file fixes the problem. The line below seems to work around this problem.
         */
        viewer.invalidateTextPresentation();
    }

    @Override
    protected boolean affectsTextPresentation(PropertyChangeEvent event) {
        return colorManager.isColorPreference(event.getProperty());
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        /*
         * Extend the base preferences of the editor (this class) with our own plugin preferences. The base editor
         * (superclass) listens to changes in the preference store and uses affectsTextPresentation() above to determine
         * whether to redraw the editor.
         * 
         * [ASSUMPTION] We need to include/extend the preferences of the base editor in order for base editor to operate
         * accoding to generic editor preferences chosen by the user (font, background etc).
         */
        IPreferenceStore baseEditorPreferenceStore = getPreferenceStore();
        IPreferenceStore ourPreferenceStore = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(new ChainedPreferenceStore(new IPreferenceStore[] { ourPreferenceStore, baseEditorPreferenceStore }));
    }
}

// 190312 1720 xxxx
