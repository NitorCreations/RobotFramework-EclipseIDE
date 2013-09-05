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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.Content;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.IVariableReplacementRegionCalculator;
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

        final AttemptVisitor mockTableAttemptVisitor = mock(AttemptVisitor.class, "tableAttemptVisitor");
        final AttemptVisitor mockSettingTableAttemptVisitor = mock(AttemptVisitor.class, "settingTableAttemptVisitor");
        final AttemptVisitor mockVariableAttemptVisitor = mock(AttemptVisitor.class, "variableAttemptVisitor");
        final AttemptVisitor mockKeywordCallAttemptVisitor = mock(AttemptVisitor.class, "keywordCallAttemptVisitor");
        final AttemptVisitor mockKeywordDefinitionAttemptVisitor = mock(AttemptVisitor.class, "keywordDefinitionAttemptVisitor");

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

        Matcher<List<VisitorInfo>> contaisVisitorInfo(AttemptVisitor visitor, ParsedString argument) {
            final VisitorInfo expectedVisitorInfo = new VisitorInfo(argument, visitor);
            return new BaseMatcher<List<VisitorInfo>>() {
                @Override
                public void describeTo(Description description) {
                    description.appendText("contain ").appendValue(expectedVisitorInfo);
                }

                @Override
                public boolean matches(Object item) {
                    if (!(item instanceof Collection)) {
                        throw new IllegalArgumentException("item is not a Collection");
                    }
                    return ((Collection<?>) item).contains(expectedVisitorInfo);
                }
            };
        }

        Matcher<List<VisitorInfo>> isLastVisitorInfo(AttemptVisitor visitor, ParsedString argument) {
            final VisitorInfo expectedVisitorInfo = new VisitorInfo(argument, visitor);
            return new BaseMatcher<List<VisitorInfo>>() {
                @Override
                public void describeTo(Description description) {
                    description.appendText("contain ").appendValue(expectedVisitorInfo);
                }

                @Override
                public boolean matches(Object item) {
                    if (!(item instanceof List)) {
                        throw new IllegalArgumentException("item is not a List");
                    }
                    List<?> list = (List<?>) item;
                    return list.indexOf(expectedVisitorInfo) == list.size() - 1;
                }
            };
        }
    }

    static ArgumentType getArgumentTypeFrom(Class<?> c) {
        String className = c.getName();
        int dollarIdx = className.lastIndexOf('$');
        String innerClassName = className.substring(dollarIdx + 1);
        String argumentTypeName = innerClassName.replace("_proposals", "");
        return ArgumentType.valueOf(argumentTypeName);
    }

    public static class ArgumentTypeTestsTest {
        @Test
        public void should_have_test_classes_for_all_argument_types() throws Exception {
            Set<ArgumentType> unencounteredArgumentTypes = new HashSet<ArgumentType>(Arrays.asList(ArgumentType.values()));
            for (Class<?> c : ArgumentTypeTests.class.getClasses()) {
                try {
                    ArgumentType argumentType = getArgumentTypeFrom(c);
                    unencounteredArgumentTypes.remove(argumentType);
                } catch (IllegalArgumentException e) {
                    // not an ArgumentType test class, ignore
                }
            }
            assertTrue("Missing test classes for: " + unencounteredArgumentTypes, unencounteredArgumentTypes.isEmpty());
        }
    }

    @RunWith(Enclosed.class)
    public static class ArgumentTypeTests {
        @Ignore
        public static abstract class ArgumentTypeBase extends Base {

            protected ArgumentType getArgumentType() {
                return getArgumentTypeFrom(getClass());
            }

            protected Region setupVariableReplacementRegionCalculator(int start, int len) {
                Region variableRegion = new Region(start, len);
                when(variableReplacementRegionCalculator.calculate(any(ParsedString.class), anyInt())).thenReturn(variableRegion);
                return variableRegion;
            }

            @Test
            public void basetest__should_not_fail() throws Exception {
                setupVariableReplacementRegionCalculator(12, 1);
                for (int argumentIndex = 0; argumentIndex < 5; ++argumentIndex) {
                    try {
                        ParsedString argument = new ParsedString("Hello", 11, argumentIndex).setType(getArgumentType());
                        int documentOffset = argument.getArgCharPos();
                        int lineCharPos = 10;
                        List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                        assertNotNull(attemptVisitors);
                    } catch (Exception e) {
                        throw new RuntimeException("For argument index " + argumentIndex, e);
                    }
                }
            }

            @Test
            public void basetest__table_proposals_should_be_present_for_argument_index_0() throws Exception {
                setupVariableReplacementRegionCalculator(12, 1);
                int argumentIndex = 0;
                ParsedString argument = new ParsedString("Hello", 11, argumentIndex).setType(getArgumentType());
                int documentOffset = argument.getArgCharPos();
                int lineCharPos = 10;
                List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                assertThat(attemptVisitors, contaisVisitorInfo(mockTableAttemptVisitor, argument));
                verify(proposalGeneratorFactory).createTableAttemptVisitor();
            }

            @Test
            public void basetest__table_proposals_should_be_last_proposal() throws Exception {
                setupVariableReplacementRegionCalculator(12, 1);
                int argumentIndex = 0;
                ParsedString argument = new ParsedString("Hello", 11, argumentIndex).setType(getArgumentType());
                int documentOffset = argument.getArgCharPos();
                int lineCharPos = 10;
                List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                assertThat(attemptVisitors, isLastVisitorInfo(mockTableAttemptVisitor, argument));
            }

            @Test
            public void basetest__table_proposals_should_not_be_present_for_nonzero_argument_indexes() throws Exception {
                setupVariableReplacementRegionCalculator(12, 1);
                for (int argumentIndex = 1; argumentIndex < 5; ++argumentIndex) {
                    try {
                        ParsedString argument = new ParsedString("Hello", 11, argumentIndex).setType(getArgumentType());
                        int documentOffset = argument.getArgCharPos();
                        int lineCharPos = 10;
                        List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                        assertThat(attemptVisitors, not(contaisVisitorInfo(mockTableAttemptVisitor, argument)));
                        verify(proposalGeneratorFactory, never()).createTableAttemptVisitor();
                    } catch (Exception e) {
                        throw new RuntimeException("For argument index " + argumentIndex, e);
                    }
                }
            }

            protected void template__should_propose_keyword_proposals() throws Exception {
                InOrder inOrder = Mockito.inOrder(proposalGeneratorFactory);
                for (int argumentIndex = 1; argumentIndex < 5; ++argumentIndex) {
                    try {
                        ParsedString argument = new ParsedString("Hello", 11, argumentIndex).setType(getArgumentType());
                        int documentOffset = argument.getArgCharPos();
                        int lineCharPos = 10;
                        List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                        assertThat(attemptVisitors, contaisVisitorInfo(mockKeywordCallAttemptVisitor, argument));
                        inOrder.verify(proposalGeneratorFactory).createKeywordCallAttemptVisitor(same(file));
                    } catch (Exception e) {
                        throw new RuntimeException("For argument index " + argumentIndex, e);
                    }
                }
            }

            protected void template__should_propose_variable_proposals() throws Exception {
                should_propose_variable_proposals_with_maxs(PositionBounds.ALL, PositionBounds.ALL);
            }

            protected void template__should_propose_variable_proposals_up_to_previous_setting_line() throws Exception {
                should_propose_variable_proposals_with_maxs(PositionBounds.ALL, PositionBounds.LINE_CHAR_POS_MINUS_ONE);
            }

            protected void template__should_propose_variable_proposals_up_to_previous_variable_line() throws Exception {
                should_propose_variable_proposals_with_maxs(PositionBounds.LINE_CHAR_POS_MINUS_ONE, PositionBounds.NONE);
            }

            private enum PositionBounds {
                NONE, LINE_CHAR_POS_MINUS_ONE, ALL
            }

            private void should_propose_variable_proposals_with_maxs(PositionBounds variableBounds, PositionBounds settingBounds) {
                Region variableRegion = setupVariableReplacementRegionCalculator(12, 1);
                InOrder inOrder = Mockito.inOrder(proposalGeneratorFactory);
                for (int argumentIndex = 1; argumentIndex < 5; ++argumentIndex) {
                    try {
                        ParsedString argument = new ParsedString("Hello", 11, argumentIndex).setType(getArgumentType());
                        int documentOffset = argument.getArgCharPos();
                        int lineCharPos = 10;
                        List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                        assertThat(attemptVisitors, contaisVisitorInfo(mockVariableAttemptVisitor, argument.extractRegion(variableRegion)));
                        inOrder.verify(proposalGeneratorFactory).createVariableAttemptVisitor(same(file), eq(getCharPosFor(variableBounds, lineCharPos)), eq(getCharPosFor(settingBounds, lineCharPos)));
                    } catch (Exception e) {
                        throw new RuntimeException("For argument index " + argumentIndex, e);
                    }
                }
            }

            private int getCharPosFor(PositionBounds settingBounds, int lineCharPos) {
                switch (settingBounds) {
                    case ALL:
                        return Integer.MAX_VALUE;
                    case LINE_CHAR_POS_MINUS_ONE:
                        return lineCharPos - 1;
                    case NONE:
                        return -1;
                    default:
                        throw new IllegalArgumentException("Unsupported bounds " + settingBounds);
                }
            }

        }

        public static class IGNORED_proposals extends ArgumentTypeBase {}

        public static class COMMENT_proposals extends ArgumentTypeBase {}

        public static class TABLE_proposals extends ArgumentTypeBase {}

        public static class SETTING_KEY_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_setting_table_proposals() throws Exception {
                int argumentIndex = 0;
                ParsedString argument = new ParsedString("Test Setup", 11, argumentIndex).setType(getArgumentType());
                int documentOffset = argument.getArgCharPos();
                int lineCharPos = 10;
                List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                assertThat(attemptVisitors, contaisVisitorInfo(mockSettingTableAttemptVisitor, argument));
                verify(proposalGeneratorFactory).createSettingTableAttemptVisitor();
            }
        }

        public static class VARIABLE_KEY_proposals extends ArgumentTypeBase {}

        public static class NEW_TESTCASE_proposals extends ArgumentTypeBase {}

        public static class NEW_KEYWORD_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_keyword_definition_proposals() throws Exception {
                int argumentIndex = 0;
                ParsedString argument = new ParsedString("Hello", 11, argumentIndex).setType(getArgumentType());
                int documentOffset = argument.getArgCharPos();
                int lineCharPos = 10;
                List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, argument, documentOffset, lineCharPos);
                assertThat(attemptVisitors, contaisVisitorInfo(mockKeywordDefinitionAttemptVisitor, argument));
                verify(proposalGeneratorFactory).createKeywordDefinitionAttemptVisitor(same(file), same(argument));
            }
        }

        public static class SETTING_VAL_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_variable_proposals_up_to_previous_setting_line() throws Exception {
                template__should_propose_variable_proposals_up_to_previous_setting_line();
            }
        }

        public static class SETTING_FILE_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_variable_proposals_up_to_previous_setting_line() throws Exception {
                template__should_propose_variable_proposals_up_to_previous_setting_line();
            }
        }

        public static class SETTING_FILE_WITH_NAME_KEY_proposals extends ArgumentTypeBase {}

        public static class SETTING_FILE_ARG_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_variable_proposals_up_to_previous_setting_line() throws Exception {
                template__should_propose_variable_proposals_up_to_previous_setting_line();
            }
        }

        public static class SETTING_FILE_WITH_NAME_VALUE_proposals extends ArgumentTypeBase {}

        public static class VARIABLE_VAL_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_variable_proposals_up_to_previous_setting_line() throws Exception {
                template__should_propose_variable_proposals_up_to_previous_variable_line();
            }
        }

        public static class KEYWORD_LVALUE_proposals extends ArgumentTypeBase {}

        public static class FOR_PART_proposals extends ArgumentTypeBase {}

        public static class KEYWORD_CALL_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_keyword_proposals() throws Exception {
                template__should_propose_keyword_proposals();
            }
        }

        public static class KEYWORD_CALL_DYNAMIC_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_keyword_proposals() throws Exception {
                setupVariableReplacementRegionCalculator(12, 1);
                template__should_propose_keyword_proposals();
            }

            @Test
            public void should_propose_variable_proposals() throws Exception {
                template__should_propose_variable_proposals();
            }
        }

        public static class KEYWORD_ARG_proposals extends ArgumentTypeBase {
            @Test
            public void should_propose_variable_proposals() throws Exception {
                template__should_propose_variable_proposals();
            }
        }
    }

    @RunWith(Enclosed.class)
    public static class VariableReferences {

        public static class when_partially_entered extends Base {
            @Test
            public void should_suggest_replacing_entered_variable() throws Exception {
                Content content = new Content("*Testcases\nTestcase\n  Log  <arg>${F<cursor>");
                int documentOffset = content.o("cursor");

                List<RobotLine> lines = RobotFile.parse(content.c()).getLines();
                int lineNo = lines.size() - 1;

                ParsedString variableSubArgument = content.ps("arg-end", 2, ArgumentType.KEYWORD_ARG);
                IRegion variableRegion = content.r("arg-end");
                when(variableReplacementRegionCalculator.calculate(variableSubArgument, documentOffset)).thenReturn(variableRegion);

                List<VisitorInfo> attemptVisitors = proposalSuitabilityDeterminer.generateAttemptVisitors(file, variableSubArgument, documentOffset, lines.get(lineNo).lineCharPos);

                verify(variableReplacementRegionCalculator).calculate(variableSubArgument, documentOffset);
                verify(proposalGeneratorFactory).createVariableAttemptVisitor(same(file), eq(Integer.MAX_VALUE), eq(Integer.MAX_VALUE));
                assertThat(attemptVisitors, is(equalTo(Arrays.asList(new VisitorInfo(variableSubArgument, mockVariableAttemptVisitor)))));

            }
        }
    }
}
