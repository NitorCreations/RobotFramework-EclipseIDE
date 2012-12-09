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
import static org.mockito.Matchers.anyBoolean;
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.editors.IResourceManager;
import com.nitorcreations.robotframework.eclipseide.editors.ResourceManagerProvider;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

@RunWith(Enclosed.class)
public class TestProposalGenerator {
    @Ignore
    public abstract static class Base {
        static final String BUILTIN_KEYWORD = "BuiltIn Keyword";
        static final String BUILTIN_VARIABLE = "${BUILTIN_VARIABLE}";
        static final String BUILTIN_INDEX_FILE = "BuiltIn.index";

        final ProposalGenerator proposalGenerator = new ProposalGenerator();
        final IProject project = mock(IProject.class, "project");
        final IResourceManager resourceManager = mock(IResourceManager.class, "resourceManager");

        @Before
        public void setup() throws Exception {
            ResourceManagerProvider.set(resourceManager);

            final IWorkspace workspace = mock(IWorkspace.class, "workspace");
            final IWorkspaceRoot workspaceRoot = mock(IWorkspaceRoot.class, "workspaceRoot");
            final IPath projectFullPath = mock(IPath.class, "projectFullPath");
            final IPath builtinIndexPath = mock(IPath.class, "builtinIndexPath");
            final IFile builtinIndexFile = addFile(BUILTIN_INDEX_FILE, BUILTIN_KEYWORD + '\n' + BUILTIN_VARIABLE + '\n');

            when(project.getFullPath()).thenReturn(projectFullPath);
            when(projectFullPath.append("robot-indices/" + BUILTIN_INDEX_FILE)).thenReturn(builtinIndexPath);
            when(project.getWorkspace()).thenReturn(workspace);
            when(workspace.getRoot()).thenReturn(workspaceRoot);
            when(workspaceRoot.getFile(builtinIndexPath)).thenReturn(builtinIndexFile);
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

        protected void verifyProposal(List<RobotCompletionProposal> proposals, int index, String expectedDisplayString, String expectedCompletion) throws BadLocationException {
            RobotCompletionProposal proposal = proposals.get(index);
            assertEquals(expectedDisplayString, proposal.getDisplayString());
            final IDocument document = mock(IDocument.class, "document");
            proposal.apply(document);
            verify(document).replace(anyInt(), anyInt(), eq(expectedCompletion));
        }
    }

    public static class Keywords extends Base {
        @Test
        public void should_propose_keyword_from_included_resource_file() throws Exception {
            List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
            final String linkedFileName = "linked.txt";
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + linkedFileName + "\n");
            IFile linkedFile = addFile(linkedFileName, "*Keywords\nSay Hello\n");
            when(resourceManager.getRelativeFile(origFile, linkedFileName)).thenReturn(linkedFile);

            ParsedString argument = new ParsedString("", 0);
            proposalGenerator.addKeywordProposals(origFile, argument, 0, proposals);

            assertEquals("Got wrong amount of proposals: " + proposals, 2, proposals.size());
            verifyProposal(proposals, 0, "[linked] Say Hello", "Say Hello");
            verifyProposal(proposals, 1, "[BuiltIn] " + BUILTIN_KEYWORD, BUILTIN_KEYWORD);
        }

        @Test
        // #35
        public void should_propose_keyword_only_once_from_resource_file_included_twice() throws Exception {
            List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
            final String linkedFileName = "linked.txt";
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + linkedFileName + "\nResource  " + linkedFileName + "\n");
            IFile linkedFile = addFile(linkedFileName, "*Keywords\nSay Hello\n");
            when(resourceManager.getRelativeFile(origFile, linkedFileName)).thenReturn(linkedFile);

            ParsedString argument = new ParsedString("", 0);
            proposalGenerator.addKeywordProposals(origFile, argument, 0, proposals);

            assertEquals("Got wrong amount of proposals: " + proposals, 2, proposals.size());
            verifyProposal(proposals, 0, "[linked] Say Hello", "Say Hello");
            verifyProposal(proposals, 1, "[BuiltIn] " + BUILTIN_KEYWORD, BUILTIN_KEYWORD);
        }
    }

    @RunWith(Enclosed.class)
    public static class Variables {

        public static class when_all extends Base {
            @Test
            public void should_propose_all_variables() throws Exception {
                List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
                final String linkedFileName = "linked.txt";
                IFile origFile = addFile("orig.txt", "*Settings\nResource  " + linkedFileName + "\nResource  " + linkedFileName + "\n*Variables\n${FOO}  bar\n");
                IFile linkedFile = addFile(linkedFileName, "*Variables\n${LINKEDVAR}  value\n");
                when(resourceManager.getRelativeFile(origFile, linkedFileName)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposals, false);

                assertEquals("Got wrong amount of proposals: " + proposals, 3, proposals.size());
                verifyProposal(proposals, 0, "[BuiltIn] " + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                verifyProposal(proposals, 1, "${FOO}", "${FOO}");
                verifyProposal(proposals, 2, "[linked] ${LINKEDVAR}", "${LINKEDVAR}");
            }

        }

        // #23
        public static class when_only_local extends Base {
            @Test
            public void should_only_propose_BuiltIn_and_local_variables() throws Exception {
                List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
                final String linkedFileName = "linked.txt";
                IFile origFile = addFile("orig.txt", "*Settings\nResource  " + linkedFileName + "\nResource  " + linkedFileName + "\n*Variables\n${FOO}  bar\n");
                IFile linkedFile = addFile(linkedFileName, "*Variables\n${LINKEDVAR}  value\n");
                when(resourceManager.getRelativeFile(origFile, linkedFileName)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposals, true);

                assertEquals("Got wrong amount of proposals: " + proposals, 2, proposals.size());
                verifyProposal(proposals, 0, "[BuiltIn] " + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                verifyProposal(proposals, 1, "${FOO}", "${FOO}");
            }

            @Test
            public void should_only_propose_BuiltIn_variables_when_no_local_variables_present() throws Exception {
                List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
                final String linkedFileName = "linked.txt";
                IFile origFile = addFile("orig.txt", "*Settings\nResource  " + linkedFileName + "\nResource  " + linkedFileName);
                IFile linkedFile = addFile(linkedFileName, "*Variables\n${LINKEDVAR}  value\n");
                when(resourceManager.getRelativeFile(origFile, linkedFileName)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposals, true);

                assertEquals("Got wrong amount of proposals: " + proposals, 1, proposals.size());
                verifyProposal(proposals, 0, "[BuiltIn] " + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
            }
        }
    }
}
