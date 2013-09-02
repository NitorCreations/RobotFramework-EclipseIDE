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
package com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.junit.Before;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.editors.IResourceManager;

public abstract class BaseTestAttemptVisitor {
    static final String BUILTIN_KEYWORD = "BuiltIn Keyword";
    static final String BUILTIN_VARIABLE = "${BUILTIN_VARIABLE}";
    static final String BUILTIN_PREFIX = "[BuiltIn] ";
    static final String BUILTIN_INDEX_FILE = "BuiltIn.index";

    final IProject project = mock(IProject.class, "project");
    final IResourceManager resourceManager = mock(IResourceManager.class, "resourceManager");

    private IPath projectFullPath;
    private IWorkspaceRoot workspaceRoot;

    @Before
    public void setup() throws Exception {
        PluginContext.setResourceManager(resourceManager);

        final IWorkspace workspace = mock(IWorkspace.class, "workspace");
        workspaceRoot = mock(IWorkspaceRoot.class, "workspaceRoot");
        projectFullPath = mock(IPath.class, "projectFullPath");
        final IPath builtinIndexPath = mock(IPath.class, "builtinIndexPath");
        final IFile builtinIndexFile = addFile(BUILTIN_INDEX_FILE, BUILTIN_KEYWORD + '\n' + BUILTIN_VARIABLE + '\n');

        when(project.getFullPath()).thenReturn(projectFullPath);
        when(projectFullPath.append("robot-indices/" + BUILTIN_INDEX_FILE)).thenReturn(builtinIndexPath);
        when(project.getWorkspace()).thenReturn(workspace);
        when(workspace.getRoot()).thenReturn(workspaceRoot);
        when(workspaceRoot.getFile(builtinIndexPath)).thenReturn(builtinIndexFile);
    }

    protected void addLibraryIndex(String libraryName, String contents) throws Exception {
        final IPath libraryIndexPath = mock(IPath.class, "libraryIndexPath for " + libraryName);
        final IFile libraryIndexFile = addFile(libraryName + ".index", contents);
        when(projectFullPath.append("robot-indices/" + BUILTIN_INDEX_FILE)).thenReturn(libraryIndexPath);
        when(workspaceRoot.getFile(libraryIndexPath)).thenReturn(libraryIndexFile);
    }

    @SuppressWarnings("unchecked")
    protected IFile addFile(String fileName, String origContents) throws Exception {
        final IFile file = mock(IFile.class, fileName);
        ByteArrayInputStream contentStream = new ByteArrayInputStream(origContents.getBytes("UTF-8"));
        when(file.getContents()).thenReturn(contentStream).thenThrow(ArrayIndexOutOfBoundsException.class);
        when(file.getContents(anyBoolean())).thenReturn(contentStream).thenThrow(ArrayIndexOutOfBoundsException.class);
        when(file.getCharset()).thenReturn("UTF-8");
        when(file.getProject()).thenReturn(project);
        when(file.getName()).thenReturn(fileName);
        when(file.exists()).thenReturn(true);
        return file;
    }

    protected void verifyProposal(RobotCompletionProposalSet proposalSet, int index, String expectedDisplayString, String expectedCompletion) throws BadLocationException {
        verifyProposal(proposalSet, index, expectedDisplayString, expectedCompletion, null, null);
    }

    protected void verifyProposal(RobotCompletionProposalSet proposalSet, int index, String expectedDisplayString, String expectedCompletion, Integer matchOffset, Integer matchLength) throws BadLocationException {
        RobotCompletionProposal proposal = proposalSet.getProposals().get(index);
        assertEquals(expectedDisplayString, proposal.getDisplayString());
        final IDocument document = mock(IDocument.class, "document");
        proposal.apply(document);
        verify(document).replace(matchOffset != null ? eq(matchOffset) : anyInt(), matchLength != null ? eq(matchLength) : anyInt(), eq(expectedCompletion));
    }
}
