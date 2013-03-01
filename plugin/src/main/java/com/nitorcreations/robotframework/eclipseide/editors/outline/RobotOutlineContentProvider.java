package com.nitorcreations.robotframework.eclipseide.editors.outline;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

final class RobotOutlineContentProvider implements IContentProvider, ITreeContentProvider {

    private final IDocumentProvider documentProvider;

    public RobotOutlineContentProvider(IDocumentProvider documentProvider) {
        this.documentProvider = documentProvider;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // System.out.println("InputChanged V " + viewer + " DP " + documentProvider + " EI " +
        // robotContentOutlinePage.editorInput);
        System.out.println("V " + viewer);
        System.out.println("OLD: " + oldInput);
        System.out.println("NEW: " + newInput);
    }

    @Override
    public Object[] getElements(Object inputElement) {
        System.out.println("getElements() " + inputElement);
        IDocument document = documentProvider.getDocument(inputElement);
        RobotFile rf = RobotFile.get(document);
        Set<ParsedString> testCases = new LinkedHashSet<ParsedString>();
        Set<ParsedString> keywords = new LinkedHashSet<ParsedString>();
        Set<ParsedString> variables = new LinkedHashSet<ParsedString>();
        for (RobotLine line : rf.getLines()) {
            switch (line.type) {
                case TESTCASE_TABLE_TESTCASE_BEGIN:
                    testCases.add(line.arguments.get(0));
                    break;
                case KEYWORD_TABLE_KEYWORD_BEGIN:
                    keywords.add(line.arguments.get(0));
                    break;
                case VARIABLE_TABLE_LINE:
                    variables.add(line.arguments.get(0));
                    break;
            }
        }
        return new Object[] {//
        new RootCategoryEntry(inputElement, "Test cases", testCases), //
                new RootCategoryEntry(inputElement, "Keywords", keywords),//
                new RootCategoryEntry(inputElement, "Variables", variables),//
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