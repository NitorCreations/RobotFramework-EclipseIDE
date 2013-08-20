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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.IResourceManager;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.AttemptVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IAttemptGenerator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IProposalGeneratorFactory;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IRelevantProposalsFilter;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.RobotCompletionProposalSet;
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

        static final ICompletionProposal[] PROPOSALS = new ICompletionProposal[0];

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

            when(relevantProposalsFilter.extractMostRelevantProposals(anyListOf(RobotCompletionProposalSet.class))).thenReturn(PROPOSALS);
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

            /**
             * This class accepts text annotated with &lt;text&gt; which results in a pointer with the name "text" to be
             * remembered at the specfieid point.
             */
            public static class Content {
                private static final Pattern POINTER_RE = Pattern.compile("<([^>]+)>");
                private final Map<String, Integer> pointers = new LinkedHashMap<String, Integer>();
                private final String content;

                public Content(String contentWithPointers) {
                    Matcher m = POINTER_RE.matcher(contentWithPointers);
                    StringBuffer sb = new StringBuffer();
                    pointers.put("start", 0);
                    while (m.find()) {
                        m.appendReplacement(sb, "");
                        String pointerName = m.group(1);
                        int pointerTarget = sb.length();
                        pointers.put(pointerName, pointerTarget);
                    }
                    pointers.put("end", sb.length());
                    m.appendTail(sb);
                    content = sb.toString();
                }

                public int o(String pointerName) {
                    if (!pointers.containsKey(pointerName)) {
                        throw new NoSuchElementException(pointerName);
                    }
                    return pointers.get(pointerName);
                }

                public int l(String pointerRange) {
                    String[] pointers = pointerRange.split("-", 2);
                    return o(pointers[1]) - o(pointers[0]);
                }

                public IRegion r(String pointerRange) {
                    String[] pointers = pointerRange.split("-", 2);
                    int p0 = o(pointers[0]);
                    int p1 = o(pointers[1]);
                    return new Region(p0, p1 - p0);
                }

                public ParsedString ps(String pointerRange, int argIndex, ArgumentType argType) {
                    return ps(r(pointerRange), argIndex, argType);
                }

                public ParsedString ps(IRegion region, int argIndex, ArgumentType argType) {
                    return new ParsedString(content.substring(region.getOffset(), region.getOffset() + region.getLength()), region.getOffset(), argIndex).setType(argType);
                }

                public String c() {
                    return content;
                }
            }

            static final String LINKED_PREFIX = "[linked] ";
            static final String LINKED_FILENAME = "linked.txt";
            static final String FOO_VARIABLE = "${FOO}";
            static final String LINKED_VARIABLE = "${LINKEDVAR}";

            // return document.get().substring(0, (Integer) invocation.getArguments()[0]).replaceAll("[^\n]+",
            // "").length();
            @Test
            public void should_suggest_replacing_entered_variable() throws Exception {
                Content content = new Content("*Testcases\nTestcase\n  Log  <arg>${F<cursor>");
                int documentOffset = content.o("cursor");

                IFile origFile = mock(IFile.class);
                List<RobotLine> lines = RobotFile.parse(content.c()).getLines();
                int lineNo = lines.size() - 1;

                ParsedString variableSubArgument = content.ps("arg-end", 2, ArgumentType.KEYWORD_ARG);
                IRegion variableRegion = content.r("arg-end");
                when(variableReplacementRegionCalculator.calculate(variableSubArgument, documentOffset)).thenReturn(variableRegion);

                ICompletionProposal[] proposals = assistant.generateProposals(origFile, documentOffset, content.c(), lines, lineNo);

                verify(variableReplacementRegionCalculator).calculate(variableSubArgument, documentOffset);
                verify(proposalGeneratorFactory).createVariableAttemptVisitor(same(origFile), eq(Integer.MAX_VALUE), eq(Integer.MAX_VALUE));
                verifyBase(proposals, documentOffset, variableSubArgument, mockVariableAttemptVisitor);
            }

            private void verifyBase(ICompletionProposal[] proposals, int documentOffset, ParsedString variableSubArgument, AttemptVisitor visitor) {
                verifyBase(proposals, documentOffset, new ParsedString[] { variableSubArgument }, new AttemptVisitor[] { visitor });
            }

            @SuppressWarnings({ "rawtypes", "unchecked" })
            private void verifyBase(ICompletionProposal[] proposals, int documentOffset, ParsedString[] variableSubArguments, AttemptVisitor[] visitors) {

                assertSame(PROPOSALS, proposals);
                assertEquals(variableSubArguments.length, visitors.length);

                ArgumentCaptor<List> proposalSetsCaptor = ArgumentCaptor.forClass(List.class);
                for (int i = 0; i < variableSubArguments.length; ++i) {
                    ParsedString variableSubArgument = variableSubArguments[i];
                    AttemptVisitor visitor = visitors[i];
                    verify(attemptGenerator).acceptAttempts(eq(variableSubArgument), eq(documentOffset), proposalSetsCaptor.capture(), same(visitor));
                }
                if (visitors.length >= 1) {
                    List<List> listOfProposalSets = proposalSetsCaptor.getAllValues();
                    for (List proposalSets : listOfProposalSets) {
                        assertThat(proposalSets, is(instanceOf(List.class)));
                    }
                    verify(relevantProposalsFilter).extractMostRelevantProposals(proposalSetsCaptor.getValue());
                } else {
                    verify(relevantProposalsFilter).extractMostRelevantProposals(anyListOf(RobotCompletionProposalSet.class));
                }
            }
        }
    }

    @Ignore
    @RunWith(Enclosed.class)
    public static class FeatureTests {
        static final String LINKED_PREFIX = "[linked] ";
        static final String LINKED_FILENAME = "linked.txt";
        static final String FOO_VARIABLE = "${FOO}";
        static final String LINKED_VARIABLE = "${LINKEDVAR}";

        @RunWith(Enclosed.class)
        public static class Argument_synthesis {
            @RunWith(Enclosed.class)
            public static class synthesized {
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
}
