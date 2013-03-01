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
