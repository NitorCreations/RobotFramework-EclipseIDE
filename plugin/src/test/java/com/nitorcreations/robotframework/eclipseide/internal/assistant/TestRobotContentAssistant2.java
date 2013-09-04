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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
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
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.AttemptVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IAttemptGenerator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IProposalSuitabilityDeterminer;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.IRelevantProposalsFilter;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.RobotCompletionProposalSet;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.VisitorInfo;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

@RunWith(Enclosed.class)
public class TestRobotContentAssistant2 {
    @Ignore
    public abstract static class Base {
        IProposalSuitabilityDeterminer proposalSuitabilityDeterminer;
        IAttemptGenerator attemptGenerator;
        IRelevantProposalsFilter relevantProposalsFilter;
        IRobotContentAssistant2 assistant;

        final IFile dummyFile = mock(IFile.class);

        static final int dummyLineNo = 0;
        static final String dummyContent = "";
        static final int dummyLineCharPos = 33;
        static final int dummyDocumentOffset = dummyLineCharPos + 4;
        static final ParsedString dummyArgument = new ParsedString("foo", dummyLineCharPos + 1, 0).setType(ArgumentType.COMMENT);
        static final List<RobotLine> dummyLines = Collections.singletonList(new RobotLine(dummyLineNo, dummyLineCharPos, Collections.singletonList(dummyArgument)));
        static final List<VisitorInfo> dummyNoVisitorInfos = Collections.emptyList();

        @Before
        public void setupBase() throws Exception {
            proposalSuitabilityDeterminer = mock(IProposalSuitabilityDeterminer.class, "proposalSuitabilityDeterminer");
            attemptGenerator = mock(IAttemptGenerator.class, "attemptGenerator");
            relevantProposalsFilter = mock(IRelevantProposalsFilter.class, "relevantProposalsFilter");
            assistant = new RobotContentAssistant2(proposalSuitabilityDeterminer, attemptGenerator, relevantProposalsFilter);

            PluginContext.setResourceManager(null);
        }

        protected VisitorInfo createVisitorInfo(int i) {
            AttemptVisitor visitior = mock(AttemptVisitor.class, "attemptVisitor");
            ParsedString argument = new ParsedString("text" + i, i, i);
            return new VisitorInfo(argument, visitior);
        }
    }

    @RunWith(Enclosed.class)
    public static class Makes_sure_we_have_an_argument_for_the_current_cursor_position {
        @Ignore
        public static abstract class ArgumentBase extends Base {
            protected final void doTest(Content content, List<RobotLine> lines, int lineNo, ParsedString argument) {
                int documentOffset = content.o("cursor");
                when(proposalSuitabilityDeterminer.generateAttemptVisitors(dummyFile, argument, documentOffset, lines.get(lineNo).lineCharPos)).thenReturn(dummyNoVisitorInfos);

                assistant.generateProposals(dummyFile, documentOffset, content.c(), lines, lineNo);

                verify(proposalSuitabilityDeterminer).generateAttemptVisitors(dummyFile, argument, documentOffset, lines.get(lineNo).lineCharPos);
            }

            @After
            public final void checks() {
                verifyNoMoreInteractions(proposalSuitabilityDeterminer);
            }
        }

        @RunWith(Enclosed.class)
        public static class synthesizes_argument_when_there_is_none_available {
            public static class produces_new extends ArgumentBase {
                @Test
                public void in_empty_file() throws Exception {
                    Content content = new Content("<arg><cursor><argend>");
                    ParsedString argument = content.ps("arg-argend", 0, ArgumentType.IGNORED);
                    int lineNo = 0;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void at_empty_line_in_beginning_of_file() throws Exception {
                    Content content = new Content("<arg><cursor><argend>\n*Settings");
                    ParsedString argument = content.ps("arg-argend", 0, ArgumentType.IGNORED);
                    int lineNo = 0;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void at_empty_line_in_middle_of_file() throws Exception {
                    Content content = new Content("*Settings\n<arg><cursor><argend>\nResource  foo.txt");
                    ParsedString argument = content.ps("arg-argend", 0, ArgumentType.SETTING_KEY);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void at_empty_line_at_end_of_file() throws Exception {
                    Content content = new Content("*Settings\n<arg><cursor><argend>");
                    ParsedString argument = content.ps("arg-argend", 0, ArgumentType.SETTING_KEY);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void at_empty_first_argument_separated_by_twospaces() throws Exception {
                    Content content = new Content("*Settings\n<arg><cursor><argend>  second");
                    ParsedString argument = content.ps("arg-argend", 0, ArgumentType.SETTING_KEY).setHasSpaceAfter(true);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void at_empty_first_argument_separated_by_tab() throws Exception {
                    Content content = new Content("*Settings\n<arg><cursor><argend>\tsecond");
                    ParsedString argument = content.ps("arg-argend", 0, ArgumentType.SETTING_KEY);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void at_empty_middle_argument_separated_by_twospaces() throws Exception {
                    Content content = new Content("*Settings\nDocumentation  <arg><cursor><argend>  next");
                    ParsedString argument = content.ps("arg-argend", 1, ArgumentType.SETTING_VAL).setHasSpaceAfter(true);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void at_empty_middle_argument_separated_by_tabs() throws Exception {
                    Content content = new Content("*Settings\nDocumentation\t<arg><cursor><argend>\tnext");
                    ParsedString argument = content.ps("arg-argend", 1, ArgumentType.SETTING_VAL);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void twospaces_after_last_argument() throws Exception {
                    Content content = new Content("*Settings\nDocumentation  <arg><cursor><argend>");
                    ParsedString argument = content.ps("arg-argend", 1, ArgumentType.SETTING_VAL);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void twospaces_after_last_argument_with_space_after() throws Exception {
                    Content content = new Content("*Settings\nDocumentation  <arg><cursor><argend> ");
                    ParsedString argument = content.ps("arg-argend", 1, ArgumentType.SETTING_VAL).setHasSpaceAfter(true);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void tab_after_last_argument() throws Exception {
                    Content content = new Content("*Settings\nDocumentation\t<arg><cursor><argend>");
                    ParsedString argument = content.ps("arg-argend", 1, ArgumentType.SETTING_VAL);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void tab_after_last_argument_with_space_after() throws Exception {
                    Content content = new Content("*Settings\nDocumentation\t<arg><cursor><argend> ");
                    ParsedString argument = content.ps("arg-argend", 1, ArgumentType.SETTING_VAL).setHasSpaceAfter(true);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                private void doTest(Content content, ParsedString argument, int lineNo) {
                    List<RobotLine> lines = RobotFile.parse(content.c()).getLines();
                    doTest(content, lines, lineNo, argument);
                }
            }

            public static class extends_old extends ArgumentBase {

                @Test
                public void at_beginning_of_line_with_onespace_before_first_nonempty_argument() throws Exception {
                    Content content = new Content("*Settings\n<cursor><arg> Documentation<argend>");
                    ParsedString argument = content.ps("arg-argend", 0, ArgumentType.SETTING_KEY);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void middle_of_line_with_onespace_before_first_nonempty_argument() throws Exception {
                    Content content = new Content("*Settings\n   <cursor><arg> Documentation<argend>");
                    ParsedString argument = content.ps("arg-argend", 1, ArgumentType.IGNORED);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void between_arguments_twospaces_after_previous_onespace_before_next() throws Exception {
                    Content content = new Content("*Settings\nDocumentation  previous  <arg><cursor> next<argend>");
                    ParsedString argument = content.ps("arg-argend", 2, ArgumentType.SETTING_VAL);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                @Test
                public void between_arguments_tab_after_previous_onespace_before_next() throws Exception {
                    Content content = new Content("*Settings\nDocumentation  previous\t<arg><cursor> next<argend>");
                    ParsedString argument = content.ps("arg-argend", 2, ArgumentType.SETTING_VAL);
                    int lineNo = 1;
                    doTest(content, argument, lineNo);
                }

                private void doTest(Content content, ParsedString argument, int lineNo) {
                    List<RobotLine> lines = RobotFile.parse(content.c()).getLines();
                    doTest(content, lines, lineNo, argument);
                }
            }
        }

        public static class uses_existing_argument_when_available extends ArgumentBase {
            @Test
            public void at_start_of_argument_in_beginning_of_document() throws Exception {
                doTest("<arg><cursor>ARGUMENT<argend>  NEXTARGUMENT");
            }

            @Test
            public void at_start_of_argument() throws Exception {
                doTest("TEST  <arg><cursor>ARGUMENT<argend>  NEXTARGUMENT");
            }

            @Test
            public void in_middle_of_argment() throws Exception {
                doTest("TEST  <arg>ARGU<cursor>MENT<argend>  NEXTARGUMENT");
            }

            @Test
            public void at_end_of_argument() throws Exception {
                doTest("TEST  <arg>ARGUMENT<cursor><argend>  NEXTARGUMENT");
            }

            @Test
            public void onespace_after_argument() throws Exception {
                doTest("TEST  <arg>ARGUMENT<argend> <cursor> NEXTARGUMENT");
            }

            private void doTest(String contentWithPointers) {
                Content content = new Content(contentWithPointers);
                List<RobotLine> lines = RobotFile.parse(content.c()).getLines();
                int lineNo = lines.size() - 1;

                ParsedString argument = lines.get(lineNo).getArgumentAt(content.o("arg"));

                doTest(content, lines, lineNo, argument);
            }
        }
    }

    public static class Iterates_produced_proposal_generators extends Base {

        @Test
        public void when_no_generator_produced() {
            testWith(Collections.<VisitorInfo> emptyList());
        }

        @Test
        public void when_single_generator_produced() {
            testWith(Collections.singletonList(createVisitorInfo(0)));
        }

        @Test
        public void when_multiple_generator_produced() {
            List<VisitorInfo> list = new ArrayList<VisitorInfo>();
            list.add(createVisitorInfo(0));
            list.add(createVisitorInfo(1));
            list.add(createVisitorInfo(2));
            testWith(list);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void testWith(List<VisitorInfo> visitorInfos) {
            when(proposalSuitabilityDeterminer.generateAttemptVisitors(dummyFile, dummyArgument, dummyDocumentOffset, dummyLineCharPos)).thenReturn(visitorInfos);

            assistant.generateProposals(dummyFile, dummyDocumentOffset, dummyContent, dummyLines, dummyLineNo);

            if (!visitorInfos.isEmpty()) {
                ArgumentCaptor<List> proposalSetsCaptor = ArgumentCaptor.forClass(List.class);
                for (VisitorInfo visitorInfo : visitorInfos) {
                    verify(attemptGenerator).acceptAttempts(same(visitorInfo.visitorArgument), eq(dummyDocumentOffset), proposalSetsCaptor.capture(), same(visitorInfo.visitior));
                }

                List<List> listOfProposalSets = proposalSetsCaptor.getAllValues();
                List lastProposalSets = proposalSetsCaptor.getValue();
                assertThat(lastProposalSets, is(instanceOf(List.class)));
                for (List proposalSets : listOfProposalSets) {
                    assertThat(proposalSets, is(sameInstance(lastProposalSets)));
                }
            }
        }

        @After
        public void checks() {
            verifyNoMoreInteractions(attemptGenerator);
        }
    }

    public static class Extracts_most_relevant_proposals_from_produced_proposals extends Base {

        @Before
        public void setup() {
            VisitorInfo visitorInfo = createVisitorInfo(0);
            List<VisitorInfo> visitorInfos = Collections.singletonList(visitorInfo);
            when(proposalSuitabilityDeterminer.generateAttemptVisitors(dummyFile, dummyArgument, dummyDocumentOffset, dummyLineCharPos)).thenReturn(visitorInfos);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        public void extractMostRelevantProposals_is_called_with_same_instance_of_proposalSets_as_acceptAttempts() {
            assistant.generateProposals(dummyFile, dummyDocumentOffset, dummyContent, dummyLines, dummyLineNo);

            ArgumentCaptor<List> proposalSetsCaptor = ArgumentCaptor.forClass(List.class);
            verify(attemptGenerator).acceptAttempts(any(ParsedString.class), anyInt(), proposalSetsCaptor.capture(), any(AttemptVisitor.class));
            verify(relevantProposalsFilter).extractMostRelevantProposals(same(proposalSetsCaptor.getValue()));
        }

        @After
        public void checks() {
            verifyNoMoreInteractions(relevantProposalsFilter);
        }
    }

    public static class Returns_extracted_proposals extends Base {
        static final ICompletionProposal[] PROPOSALS = new ICompletionProposal[0];

        @Before
        public void setup() {
            when(proposalSuitabilityDeterminer.generateAttemptVisitors(dummyFile, dummyArgument, dummyDocumentOffset, dummyLineCharPos)).thenReturn(dummyNoVisitorInfos);
            when(relevantProposalsFilter.extractMostRelevantProposals(anyListOf(RobotCompletionProposalSet.class))).thenReturn(PROPOSALS);
        }

        @Test
        public void test() {
            ICompletionProposal[] actualProposals = assistant.generateProposals(dummyFile, dummyDocumentOffset, dummyContent, dummyLines, dummyLineNo);
            assertSame(PROPOSALS, actualProposals);
        }
    }

}
