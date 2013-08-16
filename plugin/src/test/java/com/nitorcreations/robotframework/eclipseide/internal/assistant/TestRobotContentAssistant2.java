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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
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
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.IResourceManager;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.AttemptVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IAttemptGenerator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IProposalGeneratorFactory;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IRelevantProposalsFilter;
import com.nitorcreations.robotframework.eclipseide.internal.util.ArrayPriorityDeque;
import com.nitorcreations.robotframework.eclipseide.internal.util.PriorityDeque;
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

        static final Region VARIABLE_REGION = new Region(50, 1);

        IProposalGeneratorFactory proposalGeneratorFactory;
        IAttemptGenerator attemptGenerator;
        IVariableReplacementRegionCalculator variableReplacementRegionCalculator;
        IRelevantProposalsFilter relevantProposalsFilter;
        IRobotContentAssistant2 assistant;

        final IProject project = mock(IProject.class, "project");
        final IResourceManager resourceManager = mock(IResourceManager.class, "resourceManager");

        final AttemptVisitor mockTableAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockSettingTableAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockVariableAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockKeywordCallAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockKeywordDefinitionAttemptVisitor = mock(AttemptVisitor.class);

        @Before
        public void setup() throws Exception {
            proposalGeneratorFactory = mock(IProposalGeneratorFactory.class, "proposalGenerator");
            attemptGenerator = mock(IAttemptGenerator.class, "attemptGenerator");
            variableReplacementRegionCalculator = mock(IVariableReplacementRegionCalculator.class, "variableReplacementRegionCalculator");
            relevantProposalsFilter = mock(IRelevantProposalsFilter.class, "relevantProposalsFilter");
            assistant = new RobotContentAssistant2(proposalGeneratorFactory, attemptGenerator, variableReplacementRegionCalculator, relevantProposalsFilter);

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

            when(proposalGeneratorFactory.createTableAttemptVisitor()).thenReturn(mockTableAttemptVisitor);
            when(proposalGeneratorFactory.createSettingTableAttemptVisitor()).thenReturn(mockSettingTableAttemptVisitor);
            when(proposalGeneratorFactory.createVariableAttemptVisitor(any(IFile.class), anyInt(), anyInt())).thenReturn(mockVariableAttemptVisitor);
            when(proposalGeneratorFactory.createKeywordCallAttemptVisitor(any(IFile.class))).thenReturn(mockKeywordCallAttemptVisitor);
            when(proposalGeneratorFactory.createKeywordDefinitionAttemptVisitor(any(IFile.class), any(ParsedString.class))).thenReturn(mockKeywordDefinitionAttemptVisitor);

            when(variableReplacementRegionCalculator.calculate(any(ParsedString.class), anyInt())).thenReturn(VARIABLE_REGION);
        }

        @After
        public void checks() {
            verifyNoMoreInteractions(proposalGeneratorFactory, attemptGenerator, variableReplacementRegionCalculator, relevantProposalsFilter);
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
                // TODO mockVariableAttemptVisitor.setBasedOnInput(true);
                // TODO mockVARIABLEAttemptVisitor.setAddStyle(AddStyle.APPEND);

                ICompletionProposal[] proposals = assistant.generateProposals(origFile, documentOffset, origContents, lines, lineNo);

                assertEquals(1, proposals.length);
                // TODO assertSame(mockVariableAttemptVisitor.addedProposal, proposals[0]);
                ParsedString expectedArgument = new ParsedString(origContents2, origContents1.length(), 2);
                expectedArgument.setHasSpaceAfter(false);
                expectedArgument.setType(ArgumentType.KEYWORD_ARG);
                verify(proposalGeneratorFactory).createVariableAttemptVisitor(same(origFile), eq(Integer.MAX_VALUE), eq(Integer.MAX_VALUE));
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
                public static class produces_new extends Base {
                    public void at_empty_line() throws Exception {}

                    public void at_beginning_of_line_with_onespace_before_first_nonempty_argument() throws Exception {}

                    public void between_arguments_at_twospaces_after_previous() throws Exception {}

                    public void between_arguments_at_tab_after_previous() throws Exception {}

                    public void twospaces_after_last_argument() throws Exception {}

                    public void at_empty_argument() throws Exception {}

                    public void onespace_after_empty_argument() throws Exception {} // ??
                }

                public static class extends_old extends Base {}
            }

            public static class not_synthesized extends Base {
                public void at_start_of_argument() throws Exception {}

                public void in_middle_of_argment() throws Exception {}

                public void at_end_of_argument() throws Exception {}

                public void onespace_after_argument() throws Exception {}
            }
        }
    }

    static <T> PriorityDeque<T> anyPriorityDequeOf(Class<T> clazz) {
        anyList();
        return new ArrayPriorityDeque<T>(1);
    }
}
