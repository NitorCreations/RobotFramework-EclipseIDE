/**
 * Copyright 2013 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.editors.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.nitorcreations.robotframework.eclipseide.editors.RobotFrameworkTextfileEditor;

public class RobotContentOutlinePage extends ContentOutlinePage {

    // private final RobotFrameworkTextfileEditor robotFrameworkTextfileEditor;
    // private final IDocumentProvider documentProvider;
    private IEditorInput editorInput;

    public RobotContentOutlinePage(IDocumentProvider documentProvider, RobotFrameworkTextfileEditor robotFrameworkTextfileEditor) {
        // this.documentProvider = documentProvider;
        // this.robotFrameworkTextfileEditor = robotFrameworkTextfileEditor;
    }

    public void setInput(IEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new RobotOutlineContentProvider());
        viewer.setLabelProvider(new LabelProvider());
        viewer.addSelectionChangedListener(this);

        if (editorInput != null) {
            viewer.setInput(editorInput);
        }
    }

}
