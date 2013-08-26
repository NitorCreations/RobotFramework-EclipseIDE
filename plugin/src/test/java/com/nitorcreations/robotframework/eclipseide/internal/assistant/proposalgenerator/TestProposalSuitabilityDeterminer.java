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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.Content;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.IVariableReplacementRegionCalculator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.ProposalSuitabilityDeterminer.VisitorInfo;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

@RunWith(Enclosed.class)
public class TestProposalSuitabilityDeterminer {
    @Ignore
    public abstract static class Base {
        static final String BUILTIN_KEYWORD = "BuiltIn Keyword";
        static final String BUILTIN_VARIABLE = "${BUILTIN_VARIABLE}";
        static final String BUILTIN_PREFIX = "[BuiltIn] ";
        static final String BUILTIN_INDEX_FILE = "BuiltIn.index";

        IProposalGeneratorFactory proposalGeneratorFactory;
        IVariableReplacementRegionCalculator variableReplacementRegionCalculator;
        IProposalSuitabilityDeterminer proposalSuitabilityDeterminer;

        final IFile file = mock(IFile.class, "file");

        final AttemptVisitor mockTableAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockSettingTableAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockVariableAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockKeywordCallAttemptVisitor = mock(AttemptVisitor.class);
        final AttemptVisitor mockKeywordDefinitionAttemptVisitor = mock(AttemptVisitor.class);

        @Before
        public void setup() throws Exception {
            proposalGeneratorFactory = mock(IProposalGeneratorFactory.class, "proposalGenerator");
            variableReplacementRegionCalculator = mock(IVariableReplacementRegionCalculator.class, "variableReplacementRegionCalculator");
            proposalSuitabilityDeterminer = new ProposalSuitabilityDeterminer(proposalGeneratorFactory, variableReplacementRegionCalculator);

            when(proposalGeneratorFactory.createTableAttemptVisitor()).thenReturn(mockTableAttemptVisitor);
            when(proposalGeneratorFactory.createSettingTableAttemptVisitor()).thenReturn(mockSettingTableAttemptVisitor);
            when(proposalGeneratorFactory.createVariableAttemptVisitor(any(IFile.class), anyInt(), anyInt())).thenReturn(mockVariableAttemptVisitor);
            when(proposalGeneratorFactory.createKeywordCallAttemptVisitor(any(IFile.class))).thenReturn(mockKeywordCallAttemptVisitor);
            when(proposalGeneratorFactory.createKeywordDefinitionAttemptVisitor(any(IFile.class), any(ParsedString.class))).thenReturn(mockKeywordDefinitionAttemptVisitor);

        }

        @After
        public void checks() {
            verifyNoMoreInteractions(proposalGeneratorFactory, variableReplacementRegionCalculator);
        }
    }

    @RunWith(Enclosed.class)
    public static class VariableReferences {

        public static class when_partially_entered extends Base {

            static final String LINKED_PREFIX = "[linked] ";
            static final String LINKED_FILENAME = "linked.txt";
            static final String FOO_VARIABLE = "${FOO}";
            static final String LINKED_VARIABLE = "${LINKEDVAR}";

            @Test
            public void should_suggest_replacing_entered_variable() throws Exception {
                Content content = new Content("*Testcases\nTestcase\n  Log  <arg>${F<cursor>");
                int documentOffset = content.o("cursor");

                List<RobotLine> lines = RobotFile.parse(content.c()).getLines();
                int lineNo = lines.size() - 1;

                ParsedString variableSubArgument = content.ps("arg-end", 2, ArgumentType.KEYWORD_ARG);
                IRegion variableRegion = content.r("arg-end");
                when(variableReplacementRegionCalculator.calculate(variableSubArgument, documentOffset)).thenReturn(variableRegion);

                List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, variableSubArgument, documentOffset, lines.get(lineNo));

                verify(variableReplacementRegionCalculator).calculate(variableSubArgument, documentOffset);
                verify(proposalGeneratorFactory).createVariableAttemptVisitor(same(file), eq(Integer.MAX_VALUE), eq(Integer.MAX_VALUE));
                assertThat(attemptVisitors, is(equalTo(Arrays.asList(new VisitorInfo(variableSubArgument, mockVariableAttemptVisitor)))));

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
