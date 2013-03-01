package com.nitorcreations.robotframework.eclipseide.editors.outline;

import java.util.Collections;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

final class RobotOutlineContentProvider implements IContentProvider, ITreeContentProvider {

    @Override
    public void dispose() {

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // System.out.println("InputChanged V " + viewer + " DP " + documentProvider + " EI " +
        // robotContentOutlinePage.editorInput);
        System.out.println("OLD: " + oldInput);
        System.out.println("NEW: " + newInput);
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return new Object[] {//
        new RootCategoryEntry(inputElement, "Test cases", Collections.singleton(new ParsedString("User should be able to log in", 1))), //
                new RootCategoryEntry(inputElement, "Keywords", Collections.singleton(new ParsedString("Go to login page", 1))),//
                new RootCategoryEntry(inputElement, "Variables", Collections.singleton(new ParsedString("${FOO}", 1))),//
        };
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof RootCategoryEntry) {
            return ((RootCategoryEntry) parentElement).getEntries().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof ParsedStringEntry) {
            return ((ParsedStringEntry) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof RootCategoryEntry;
    }
}