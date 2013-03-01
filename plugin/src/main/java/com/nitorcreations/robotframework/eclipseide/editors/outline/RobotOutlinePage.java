package com.nitorcreations.robotframework.eclipseide.editors.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class RobotOutlinePage extends ContentOutlinePage {

    private final IDocumentProvider documentProvider;
    private IEditorInput editorInput;

    public RobotOutlinePage(IDocumentProvider documentProvider) {
        this.documentProvider = documentProvider;
    }

    public void setInput(IEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new RobotOutlineContentProvider(documentProvider));
        viewer.setLabelProvider(new LabelProvider()); // we could use custom label provider here to get fancy icons

        if (editorInput != null) {
            viewer.setInput(editorInput);
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        super.selectionChanged(event);

        // TODO Jump to selection
    }
}
