/**
 * Copyright 2012-2014 Nitor Creations Oy, SmallGreenET
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
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public final class ResourceManager implements IResourceManager {

    private final Map<File, Resource> resources = new HashMap<File, Resource>();

    @Override
    public Resource getResource(File path) {
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

    private final Map<IDocument, IFile> DOCUMENT_TO_FILE = Collections.synchronizedMap(new HashMap<IDocument, IFile>());
    private final Map<IFile, IDocument> FILE_TO_DOCUMENT = Collections.synchronizedMap(new HashMap<IFile, IDocument>());

    @Override
    public void registerEditor(RobotFrameworkTextfileEditor editor) {
        IDocument editedDocument = editor.getEditedDocument();
        IFile editedFile = editor.getEditedFile();
        assert !DOCUMENT_TO_FILE.containsKey(editedDocument);
        assert !FILE_TO_DOCUMENT.containsKey(editedFile);
        DOCUMENT_TO_FILE.put(editedDocument, editedFile);
        FILE_TO_DOCUMENT.put(editedFile, editedDocument);
    }

    @Override
    public void unregisterEditor(RobotFrameworkTextfileEditor editor) {
        DOCUMENT_TO_FILE.remove(editor.getEditedDocument());
        FILE_TO_DOCUMENT.remove(editor.getEditedFile());
    }

    @Override
    public IFile resolveFileFor(IDocument document) {
        return DOCUMENT_TO_FILE.get(document);
    }

    @Override
    public IDocument resolveDocumentFor(IFile file) {
        return FILE_TO_DOCUMENT.get(file);
    }

    @Override
    public IFile getRelativeFile(IFile originalFile, String pathRelativeToOriginalFile) {
        IPath originalFolderPath = originalFile.getParent().getLocation();
        IPath newPath = originalFolderPath.append(pathRelativeToOriginalFile);
        IWorkspaceRoot root = originalFile.getWorkspace().getRoot();
        return getBestFileForLocation(root, newPath);
    }

    /**
     * When using nested Maven projects with M2E, it seems M2E does not properly indicate files belonging to subprojects
     * as filtered from the parent project. As a result, if we called {@link IWorkspaceRoot#getFileForLocation(IPath)}
     * to resolve the location of a resource, it would typically return a IFile referring to the file through the parent
     * project instead of the expected subproject. This would then end up opening files in Eclipse appearing to belong
     * to the "wrong" project (when "Link with editor" is active) when following a hyperlink.
     * <p>
     * To work around this, we instead fetch ALL possible IFile references for the given path and try to pick the best
     * one using heuristics.
     * <p>
     * Should M2E start filtering files in the expected way, we could switch back to
     * {@link IWorkspaceRoot#getFileForLocation()} after most users can be assumed to have updated their M2E to a
     * sufficiently new version.
     */
    private IFile getBestFileForLocation(IWorkspaceRoot root, IPath path) {
        URI pathUri = uriForPath(path);
        // The heuristics for picking the best match is currently the one with the shortest project-relative path. This
        // may fail (i.e. pick the wrong alternative) if modules are not strictly below each other in the file system.
        IFile bestFile = null;
        int bestSegments = Integer.MAX_VALUE;
        for (IFile file : root.findFilesForLocationURI(pathUri)) {
            int segments = file.getProjectRelativePath().segmentCount();
            if (segments < bestSegments) {
                bestFile = file;
                bestSegments = segments;
            }
        }
        return bestFile;
    }

    private URI uriForPath(IPath path) {
        return new File(path.toString()).toURI();
    }

    @Override
    public Map<IFile, IPath> getJavaFiles(String fullyQualifiedName) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        Map<IFile, IPath> files = new LinkedHashMap<IFile, IPath>();
        for (IProject project : root.getProjects()) {
            try {
                IJavaProject javaProject = JavaCore.create(project);
                IType type = javaProject.findType(fullyQualifiedName);
                if (type != null) {
                    IFile file = root.getFile(type.getPath());
                    if (file.exists()) {
                        IJavaElement ancestor = type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
                        IPath path = ancestor != null ? ancestor.getPath() : type.getPath();
                        files.put(file, path);
                    }
                }
            } catch (JavaModelException e) {
                // non-Java or unopened projects are simple skipped
            }
        }
        return files;
    }

    @Override
    public IEditorPart openOrReuseEditorFor(IFile file, boolean isRobotFile) {
        try {
            IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            FileEditorInput editorInput = new FileEditorInput(file);
            int matchFlags;
            String editorId;
            if (isRobotFile) {
                matchFlags = IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID;
                editorId = RobotFrameworkTextfileEditor.EDITOR_ID;
            } else {
                matchFlags = IWorkbenchPage.MATCH_INPUT;
                editorId = IDE.getEditorDescriptor(file).getId();
            }
            return workbenchPage.openEditor(editorInput, editorId, true, matchFlags);
        } catch (PartInitException e) {
            throw new RuntimeException("Problem opening robot editor for " + file, e);
        }
    }

}
