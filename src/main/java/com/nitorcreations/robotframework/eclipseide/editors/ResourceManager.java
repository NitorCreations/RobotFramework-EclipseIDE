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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public final class ResourceManager {

    private static final Map<File, Resource> resources = new HashMap<File, Resource>();

    public static Resource getResource(File path) {
        try {
            Resource r;
            synchronized (ResourceManager.class) {
                File canonicalPath = path.getCanonicalFile();
                r = resources.get(canonicalPath);
                if (r == null) {
                    r = new Resource(canonicalPath);
                    r.loadFromDisk();
                    resources.put(canonicalPath, r);
                }
            }
            return r;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<IDocument, IFile> DOCUMENT_TO_FILE = Collections.synchronizedMap(new HashMap<IDocument, IFile>());
    private static final Map<IFile, IDocument> FILE_TO_DOCUMENT = Collections.synchronizedMap(new HashMap<IFile, IDocument>());

    public static void registerEditor(RobotFrameworkTextfileEditor editor) {
        IDocument editedDocument = editor.getEditedDocument();
        IFile editedFile = editor.getEditedFile();
        assert !DOCUMENT_TO_FILE.containsKey(editedDocument);
        assert !FILE_TO_DOCUMENT.containsKey(editedFile);
        DOCUMENT_TO_FILE.put(editedDocument, editedFile);
        FILE_TO_DOCUMENT.put(editedFile, editedDocument);
    }

    public static void unregisterEditor(RobotFrameworkTextfileEditor editor) {
        DOCUMENT_TO_FILE.remove(editor.getEditedDocument());
        FILE_TO_DOCUMENT.remove(editor.getEditedFile());
    }

    public static IFile resolveFileFor(IDocument document) {
        return DOCUMENT_TO_FILE.get(document);
    }

    public static IDocument resolveDocumentFor(IFile file) {
        return FILE_TO_DOCUMENT.get(file);
    }

    public static IFile getRelativeFile(IFile originalFile, String pathRelativeToOriginalFile) {
        IPath originalFolderPath = originalFile.getParent().getFullPath();
        IPath newPath = originalFolderPath.append(pathRelativeToOriginalFile);
        return originalFile.getWorkspace().getRoot().getFile(newPath);
    }

    public static RobotFrameworkTextfileEditor openOrReuseEditorFor(IFile file) {
        try {
            IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            FileEditorInput editorInput = new FileEditorInput(file);
            int matchFlags = IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT;
            IEditorPart editor = workbenchPage.openEditor(editorInput, RobotFrameworkTextfileEditor.EDITOR_ID, true, matchFlags);
            return (RobotFrameworkTextfileEditor) editor;
        } catch (PartInitException e) {
            throw new RuntimeException("Problem opening robot editor for " + file, e);
        }
    }

}
