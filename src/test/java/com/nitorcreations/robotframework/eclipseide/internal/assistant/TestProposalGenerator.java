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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.junit.Before;
import org.junit.Test;

import com.nitorcreations.robotframework.eclipseide.editors.IResourceManager;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManagerProvider;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class TestProposalGenerator {

    final ProposalGenerator proposalGenerator = new ProposalGenerator();
    final IProject project = mock(IProject.class, "project");
    final IResourceManager resourceManager = mock(IResourceManager.class, "resourceManager");

    @Before
    public void setup() {
        ResourceManagerProvider.set(resourceManager);

        final IWorkspace workspace = mock(IWorkspace.class, "workspace");
        final IWorkspaceRoot workspaceRoot = mock(IWorkspaceRoot.class, "workspaceRoot");
        final IPath projectFullPath = mock(IPath.class, "projectFullPath");
        final IPath dummyPath = mock(IPath.class, "dummyPath");
        final IFile dummyFile = mock(IFile.class, "dummyFile");

        when(project.getFullPath()).thenReturn(projectFullPath);
        when(projectFullPath.append("robot-indices/BuiltIn.index")).thenReturn(dummyPath);
        when(project.getWorkspace()).thenReturn(workspace);
        when(workspace.getRoot()).thenReturn(workspaceRoot);
        when(workspaceRoot.getFile(dummyPath)).thenReturn(dummyFile);
    }

    @SuppressWarnings("unchecked")
    private IFile addFile(String fileName, String origContents) throws Exception {
        final IFile file = mock(IFile.class, fileName);
        when(file.getContents()).thenReturn(new ByteArrayInputStream(origContents.getBytes("UTF-8"))).thenThrow(ArrayIndexOutOfBoundsException.class);
        when(file.getCharset()).thenReturn("UTF-8");
        when(file.getProject()).thenReturn(project);
        when(file.getName()).thenReturn(fileName);
        when(file.exists()).thenReturn(true);
        return file;
    }

    @Test
    public void should_propose_keyword_from_included_resource_file() throws Exception {
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        final String linkedFileName = "linked.txt";
        IFile origFile = addFile("orig.txt", "*Settings\nResource  " + linkedFileName + "\n");
        IFile linkedFile = addFile(linkedFileName, "*Keywords\nSay Hello\n");
        when(resourceManager.getRelativeFile(origFile, linkedFileName)).thenReturn(linkedFile);

        ParsedString argument = new ParsedString("", 0);
        argument.setType(ArgumentType.KEYWORD_CALL);
        proposalGenerator.addKeywordProposals(origFile, argument, 0, proposals);

        assertEquals("Got wrong amount of proposals: " + proposals, 1, proposals.size());
        RobotCompletionProposal proposal = proposals.get(0);
        assertEquals("[linked] Say Hello", proposal.getDisplayString());
        final IDocument document = mock(IDocument.class, "document");
        proposal.apply(document);
        verify(document).replace(anyInt(), anyInt(), eq("Say Hello"));
    }

    @Test
    public void should_propose_keyword_only_once_from_resource_file_included_twice() throws Exception {
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        final String linkedFileName = "linked.txt";
        IFile origFile = addFile("orig.txt", "*Settings\nResource  " + linkedFileName + "\nResource  " + linkedFileName + "\n");
        IFile linkedFile = addFile(linkedFileName, "*Keywords\nSay Hello\n");
        when(resourceManager.getRelativeFile(origFile, linkedFileName)).thenReturn(linkedFile);

        ParsedString argument = new ParsedString("", 0);
        argument.setType(ArgumentType.KEYWORD_CALL);
        proposalGenerator.addKeywordProposals(origFile, argument, 0, proposals);

        assertEquals("Got wrong amount of proposals: " + proposals, 1, proposals.size());
        RobotCompletionProposal proposal = proposals.get(0);
        assertEquals("[linked] Say Hello", proposal.getDisplayString());
        final IDocument document = mock(IDocument.class, "document");
        proposal.apply(document);
        verify(document).replace(anyInt(), anyInt(), eq("Say Hello"));
    }

}
