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
        static final String BUILTIN_PREFIX = "[BuiltIn] ";
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

    public static class KeywordCalls extends Base {
        static final String LINKED_PREFIX = "[linked] ";
        static final String LINKED_FILENAME = "linked.txt";
        static final String LINKED_KEYWORD = "Say Hello";

        @Test
        public void should_propose_keyword_from_included_resource_file() throws Exception {
            List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\n");
            IFile linkedFile = addFile(LINKED_FILENAME, "*Keywords\n" + LINKED_KEYWORD + "\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

            ParsedString argument = new ParsedString("", 0);
            proposalGenerator.addKeywordCallProposals(origFile, argument, 0, proposalSets);

            assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
            RobotCompletionProposalSet proposalSet = proposalSets.get(0);
            assertEquals(false, proposalSet.isBasedOnInput());
            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, LINKED_PREFIX + LINKED_KEYWORD, LINKED_KEYWORD);
            verifyProposal(proposalSet, 1, BUILTIN_PREFIX + BUILTIN_KEYWORD, BUILTIN_KEYWORD);
        }

        @Test
        // #35
        public void should_propose_keyword_only_once_from_resource_file_included_twice() throws Exception {
            List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\nResource  " + LINKED_FILENAME + "\n");
            IFile linkedFile = addFile(LINKED_FILENAME, "*Keywords\n" + LINKED_KEYWORD + "\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

            ParsedString argument = new ParsedString("", 0);
            proposalGenerator.addKeywordCallProposals(origFile, argument, 0, proposalSets);

            assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
            RobotCompletionProposalSet proposalSet = proposalSets.get(0);
            assertEquals(false, proposalSet.isBasedOnInput());
            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, LINKED_PREFIX + LINKED_KEYWORD, LINKED_KEYWORD);
            verifyProposal(proposalSet, 1, BUILTIN_PREFIX + BUILTIN_KEYWORD, BUILTIN_KEYWORD);
        }
    }

    @RunWith(Enclosed.class)
    public static class VariableReferences {

        public static class when_all extends Base {
            static final String LINKED_PREFIX = "[linked] ";
            static final String LINKED_FILENAME = "linked.txt";
            static final String FOO_VARIABLE = "${FOO}";
            static final String LINKED_VARIABLE = "${LINKEDVAR}";

            @Test
            public void should_propose_all_variables() throws Exception {
                List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
                IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n");
                IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n" + LINKED_VARIABLE + "  value\n");
                when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposalSets, Integer.MAX_VALUE, Integer.MAX_VALUE);

                assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
                RobotCompletionProposalSet proposalSet = proposalSets.get(0);
                assertEquals(false, proposalSet.isBasedOnInput());
                assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 3, proposalSet.getProposals().size());
                verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
                verifyProposal(proposalSet, 2, LINKED_PREFIX + LINKED_VARIABLE, LINKED_VARIABLE);
            }

            @Test
            // #35
            public void should_propose_variable_only_once_from_resource_file_included_twice() throws Exception {
                List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
                IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n");
                IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n" + LINKED_VARIABLE + "  value\n");
                when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposalSets, Integer.MAX_VALUE, Integer.MAX_VALUE);

                assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
                RobotCompletionProposalSet proposalSet = proposalSets.get(0);
                assertEquals(false, proposalSet.isBasedOnInput());
                assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 3, proposalSet.getProposals().size());
                verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
                verifyProposal(proposalSet, 2, LINKED_PREFIX + LINKED_VARIABLE, LINKED_VARIABLE);
                // ${LINKEDVAR} not included twice
            }

            @Test
            // #43
            public void should_replace_partially_typed_variable() throws Exception {
                List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
                String origContents1 = "*Variables\n" + FOO_VARIABLE + "  bar\n*Testcase\n  Log  ";
                String origContents2 = "${F";
                String origContents = origContents1 + origContents2;
                IFile origFile = addFile("orig.txt", origContents);

                ParsedString argument = new ParsedString(origContents2, origContents1.length());
                proposalGenerator.addVariableProposals(origFile, argument, origContents.length(), proposalSets, Integer.MAX_VALUE, Integer.MAX_VALUE);

                assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
                RobotCompletionProposalSet proposalSet = proposalSets.get(0);
                assertEquals(true, proposalSet.isBasedOnInput());
                assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 1, proposalSet.getProposals().size());
                verifyProposal(proposalSet, 0, FOO_VARIABLE, FOO_VARIABLE, origContents1.length(), origContents2.length());
            }
        }

        // #23
        public static class when_only_local extends Base {
            static final String LINKED_FILENAME = "linked.txt";
            static final String FOO_VARIABLE = "${FOO}";

            @Test
            public void should_only_propose_BuiltIn_and_local_variables() throws Exception {
                List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
                IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n");
                IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n${LINKEDVAR}  value\n");
                when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposalSets, Integer.MAX_VALUE, -1);

                assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
                RobotCompletionProposalSet proposalSet = proposalSets.get(0);
                assertEquals(false, proposalSet.isBasedOnInput());
                assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
                verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
                // ${LINKEDVAR} excluded
            }

            @Test
            public void should_only_propose_BuiltIn_variables_when_no_local_variables_present() throws Exception {
                List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
                IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\nResource  " + LINKED_FILENAME);
                IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n${LINKEDVAR}  value\n");
                when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposalSets, Integer.MAX_VALUE, -1);

                assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
                RobotCompletionProposalSet proposalSet = proposalSets.get(0);
                assertEquals(false, proposalSet.isBasedOnInput());
                assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 1, proposalSet.getProposals().size());
                verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                // ${LINKEDVAR} excluded
            }

            @Test
            public void should_propose_subset_when_maxVariableCharPos_set() throws Exception {
                List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
                String origContents1 = "*Settings\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n";
                String origContents2 = "${BAR}  bar\n${ZOT}  zot\n";
                IFile origFile = addFile("orig.txt", origContents1 + origContents2);
                IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n${LINKEDVAR}  value\n");
                when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposalSets, origContents1.length() - 1, -1);

                assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
                RobotCompletionProposalSet proposalSet = proposalSets.get(0);
                assertEquals(false, proposalSet.isBasedOnInput());
                assertEquals(false, proposalSet.isBasedOnInput());
                assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
                verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
                // ${BAR} and ${LINKEDVAR} excluded
            }

        }

        // #23
        public static class when_partially_imported extends Base {
            static final String LINKED_PREFIX_1 = "[linked1] ";
            static final String LINKED_FILENAME_1 = "linked1.txt";
            static final String LINKED_FILENAME_2 = "linked2.txt";
            static final String FOO_VARIABLE = "${FOO}";
            static final String LINKED_VARIABLE_1 = "${LINKEDVAR1}";
            static final String LINKED_VARIABLE_2 = "${LINKEDVAR2}";

            @Test
            public void should_propose_subset_when_maxSettingsCharPos_set() throws Exception {
                List<RobotCompletionProposalSet> proposalSets = new ArrayList<RobotCompletionProposalSet>();
                String origContents1 = "*Settings\nResource  " + LINKED_FILENAME_1 + "\n";
                String origContents2 = "Resource  " + LINKED_FILENAME_2 + "\n*Variables\n" + FOO_VARIABLE + "  bar\n";
                IFile origFile = addFile("orig.txt", origContents1 + origContents2);
                IFile linkedFile1 = addFile(LINKED_FILENAME_1, "*Variables\n" + LINKED_VARIABLE_1 + "  value\n");
                IFile linkedFile2 = addFile(LINKED_FILENAME_2, "*Variables\n" + LINKED_VARIABLE_2 + "  value\n");
                when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME_1)).thenReturn(linkedFile1);
                when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME_2)).thenReturn(linkedFile2);

                ParsedString argument = new ParsedString("", 0);
                proposalGenerator.addVariableProposals(origFile, argument, 0, proposalSets, Integer.MAX_VALUE, origContents1.length() - 1);

                assertEquals("Got wrong amount of proposal sets: " + proposalSets, 1, proposalSets.size());
                RobotCompletionProposalSet proposalSet = proposalSets.get(0);
                assertEquals(false, proposalSet.isBasedOnInput());
                assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 3, proposalSet.getProposals().size());
                verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
                verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
                verifyProposal(proposalSet, 2, LINKED_PREFIX_1 + LINKED_VARIABLE_1, LINKED_VARIABLE_1);
                // ${LINKED_VARIABLE_2} excluded
            }
        }
    }
}
