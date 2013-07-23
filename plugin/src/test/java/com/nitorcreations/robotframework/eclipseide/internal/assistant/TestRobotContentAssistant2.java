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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.IResourceManager;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.TestRobotContentAssistant2.MockProposalAdder.AddStyle;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

@RunWith(Enclosed.class)
public class TestRobotContentAssistant2 {
    @Ignore
    public abstract static class Base {
        static final String BUILTIN_KEYWORD = "BuiltIn Keyword";
        static final String BUILTIN_VARIABLE = "${BUILTIN_VARIABLE}";
        static final String BUILTIN_PREFIX = "[BuiltIn] ";
        static final String BUILTIN_INDEX_FILE = "BuiltIn.index";

        IProposalGenerator proposalGenerator;
        IRobotContentAssistant2 assistant;

        final IProject project = mock(IProject.class, "project");
        final IResourceManager resourceManager = mock(IResourceManager.class, "resourceManager");

        static final MockProposalAdder PROPOSAL_ADDER_FOR_TABLE_PROPOSALS = new MockProposalAdder();
        static final MockProposalAdder PROPOSAL_ADDER_FOR_SETTING_TABLE_PROPOSALS = new MockProposalAdder();
        static final MockProposalAdder PROPOSAL_ADDER_FOR_VARIABLE_PROPOSALS = new MockProposalAdder();
        static final MockProposalAdder PROPOSAL_ADDER_FOR_KEYWORD_CALL_PROPOSALS = new MockProposalAdder();
        static final MockProposalAdder PROPOSAL_ADDER_FOR_KEYWORD_DEFINITION_PROPOSALS = new MockProposalAdder();

        @Before
        public void setup() throws Exception {
            proposalGenerator = mock(IProposalGenerator.class, "proposalGenerator");
            assistant = new RobotContentAssistant2(proposalGenerator);

            PluginContext.setResourceManager(resourceManager);

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

            doAnswer(PROPOSAL_ADDER_FOR_TABLE_PROPOSALS).when(proposalGenerator).addTableProposals(any(IFile.class), any(ParsedString.class), anyInt(), anyListOf(RobotCompletionProposalSet.class));
            doAnswer(PROPOSAL_ADDER_FOR_SETTING_TABLE_PROPOSALS).when(proposalGenerator).addSettingTableProposals(any(IFile.class), any(ParsedString.class), anyInt(), anyListOf(RobotCompletionProposalSet.class));
            doAnswer(PROPOSAL_ADDER_FOR_VARIABLE_PROPOSALS).when(proposalGenerator).addVariableProposals(any(IFile.class), any(ParsedString.class), anyInt(), anyListOf(RobotCompletionProposalSet.class), anyInt(), anyInt());
            doAnswer(PROPOSAL_ADDER_FOR_KEYWORD_CALL_PROPOSALS).when(proposalGenerator).addKeywordCallProposals(any(IFile.class), any(ParsedString.class), anyInt(), anyListOf(RobotCompletionProposalSet.class));
            doAnswer(PROPOSAL_ADDER_FOR_KEYWORD_DEFINITION_PROPOSALS).when(proposalGenerator).addKeywordDefinitionProposals(any(IFile.class), any(ParsedString.class), anyInt(), anyListOf(RobotCompletionProposalSet.class));
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
    }

    @RunWith(Enclosed.class)
    public static class VariableReferences {

        public static class when_partially_entered extends Base {

            static final String LINKED_PREFIX = "[linked] ";
            static final String LINKED_FILENAME = "linked.txt";
            static final String FOO_VARIABLE = "${FOO}";
            static final String LINKED_VARIABLE = "${LINKEDVAR}";

            // return document.get().substring(0, (Integer) invocation.getArguments()[0]).replaceAll("[^\n]+",
            // "").length();
            @Test
            public void should_suggest_replacing_entered_variable() throws Exception {
                final String origContents1 = "*Variables\n" + FOO_VARIABLE + "  bar\n*Testcases\nTestcase\n  Log  ";
                final String origContents2 = "${F";
                final String origContents = origContents1 + origContents2;
                IFile origFile = mock(IFile.class);
                List<RobotLine> lines = RobotFile.parse(origContents).getLines();
                int documentOffset = origContents.length();
                int lineNo = lines.size() - 1;
                PROPOSAL_ADDER_FOR_VARIABLE_PROPOSALS.setBasedOnInput(true);
                PROPOSAL_ADDER_FOR_VARIABLE_PROPOSALS.setAddStyle(AddStyle.APPEND);

                ICompletionProposal[] proposals = assistant.generateProposals(origFile, documentOffset, origContents, lines, lineNo);

                assertEquals(1, proposals.length);
                assertSame(PROPOSAL_ADDER_FOR_VARIABLE_PROPOSALS.addedProposal, proposals[0]);
                ParsedString expectedArgument = new ParsedString(origContents2, origContents1.length(), 2);
                expectedArgument.setHasSpaceAfter(false);
                expectedArgument.setType(ArgumentType.KEYWORD_ARG);
                verify(proposalGenerator).addVariableProposals(same(origFile), eq(expectedArgument), eq(documentOffset), anyListOf(RobotCompletionProposalSet.class), eq(Integer.MAX_VALUE), eq(Integer.MAX_VALUE));
                verifyNoMoreInteractions(proposalGenerator);
            }
        }
    }

    @RunWith(Enclosed.class)
    public static class FeatureTests {
        static final String LINKED_PREFIX = "[linked] ";
        static final String LINKED_FILENAME = "linked.txt";
        static final String FOO_VARIABLE = "${FOO}";
        static final String LINKED_VARIABLE = "${LINKEDVAR}";

        @RunWith(Enclosed.class)
        public static class Argument_synthesis {
            @RunWith(Enclosed.class)
            public static class synthesized extends Base {
                public static class produces_new  extends Base {
                    public void at_empty_line() throws Exception {}
                    public void at_beginning_of_line_with_onespace_before_first_nonempty_argument() throws Exception {}
                    public void between_arguments_at_twospaces_after_previous() throws Exception {}
                    public void between_arguments_at_tab_after_previous() throws Exception {}
                    public void twospaces_after_last_argument() throws Exception {}
                    public void at_empty_argument() throws Exception {}
                    public void onespace_after_empty_argument() throws Exception {} // ??
                }
                public static class extends_old extends Base {
                }
            }

            public static class not_synthesized extends Base {
                public void at_start_of_argument() throws Exception {}
                public void in_middle_of_argment() throws Exception {}
                public void at_end_of_argument() throws Exception {}
                public void onespace_after_argument() throws Exception {}
            }
        }
    }

    static final class MockProposalAdder implements Answer<Void> {
        private final RobotCompletionProposalSet addedProposalSet = new RobotCompletionProposalSet();
        public final RobotCompletionProposal addedProposal;
        private AddStyle addStyle;

        enum AddStyle {
            PREPEND, APPEND
        }

        MockProposalAdder() {
            addedProposal = new RobotCompletionProposal(null, null, null, null, null, null, null);
            addedProposalSet.getProposals().add(addedProposal);
        }

        public void setBasedOnInput(boolean basedOnInput) {
            addedProposalSet.setBasedOnInput(basedOnInput);
        }

        public void setAddStyle(AddStyle addStyle) {
            this.addStyle = addStyle;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            @SuppressWarnings("unchecked")
            List<RobotCompletionProposalSet> proposalSets = (List<RobotCompletionProposalSet>) invocation.getArguments()[3];
            if (addStyle == AddStyle.PREPEND) {
                proposalSets.add(0, addedProposalSet);
            } else {
                proposalSets.add(addedProposalSet);
            }
            return null;
        }
    }
}
