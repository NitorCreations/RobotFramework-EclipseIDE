/**
 * Copyright 2013 Nitor Creations Oy
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Region;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.nitorcreations.junit.runners.NicelyParameterized;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.Content;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

@RunWith(Enclosed.class)
public class TestKeywordDefinitionAttemptVisitor {

    public static final String PREAMBLE = "Called from the following testcases/keywords:\n";

    private static final Pattern CONVERT_TO_DASH_RE = Pattern.compile("<li>");
    private static final Pattern CONVERT_TO_LINEFEED_RE = Pattern.compile("<(?:/li|ul)>");
    private static final Pattern CONVERT_TO_SPACE_RE = Pattern.compile("\\s*(?:<.*?>\\s*+)++");

    public static String htmlToText(String additionalProposalInfo) {
        String text = CONVERT_TO_DASH_RE.matcher(additionalProposalInfo).replaceAll("-");
        text = CONVERT_TO_LINEFEED_RE.matcher(text).replaceAll("\n");
        text = CONVERT_TO_SPACE_RE.matcher(text).replaceAll(" ");
        text = text.trim();
        return text;
    }

    @RunWith(NicelyParameterized.class)
    public static class Regular_keywords extends BaseTestAttemptVisitor {
        KeywordDefinitionAttemptVisitor visitor;

        private final String table;
        private final String callHostIdentifier;

        @Parameters
        public static List<Object[]> parameters() {
            return Arrays.asList(new Object[][] { { "Test cases", "TEST CASE" }, { "Keywords", "KEYWORD" } });
        }

        public Regular_keywords(String table, String callHostIdentifier) {
            this.table = table;
            this.callHostIdentifier = callHostIdentifier;
        }

        @Test
        public void should_propose_missing_keywords_in_single_file() throws Exception {
            IFile origFile = addFile("orig.txt", "*" + table + "\nTestCaseOrKeyword\n  Existing keyword\n  Missing keyword 1  argument1\n*Keywords\nExisting keyword\n  Missing keyword 2  argument2");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 2, proposals.size());
            assertEquals("Missing keyword 1", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
            assertEquals("Missing keyword 2", proposals.get(1).getMatchArgument());
            assertThat(htmlToText(proposals.get(1).getAdditionalProposalInfo()), is(PREAMBLE + "- KEYWORD Existing keyword"));
        }

        @Test
        public void should_propose_missing_keyword_used_multiple_times_only_once() throws Exception {
            IFile origFile = addFile("orig.txt", "*" + table + "\nTestCaseOrKeyword1\n  Missing keyword\nTestCaseOrKeyword2\n  Missing keyword\n");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 1, proposals.size());
            assertEquals("Missing keyword", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword1\n- " + callHostIdentifier + " TestCaseOrKeyword2"));
        }

        @Test
        public void should_propose_keyword_at_cursor_as_missing() throws Exception {
            Content origContents = new Content("*" + table + "\nTestCaseOrKeyword\n  Existing keyword\n  Missing keyword 1  argument1\n*Keywords\n<arg>Existing keyword<argend>");
            IFile origFile = addFile("orig.txt", origContents.c());
            visitor = new KeywordDefinitionAttemptVisitor(origFile, origContents.ps("arg-argend", 0, ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 2, proposals.size());
            assertEquals("Existing keyword", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
            assertEquals("Missing keyword 1", proposals.get(1).getMatchArgument());
            assertThat(htmlToText(proposals.get(1).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
        }

        @Test
        public void should_not_propose_locally_missing_keywords_actually_found_in_included_file() throws Exception {
            IFile origFile = addFile("orig.txt", "*Settings\nResource  other.txt\n*" + table + "\nTestCaseOrKeyword\n  Existing keyword\n  Missing keyword 1  argument1");
            IFile otherFile = addFile("other.txt", "*Keywords\nExisting keyword\n");
            when(resourceManager.getRelativeFile(origFile, "other.txt")).thenReturn(otherFile);
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 1, proposals.size());
            assertEquals("Missing keyword 1", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
        }

        @Test
        public void should_not_propose_locally_missing_keywords_actually_found_in_included_library() throws Exception {
            IFile origFile = addFile("orig.txt", "*Settings\nLibrary  MyLibrary\n*" + table + "\nTestCaseOrKeyword\n  Existing keyword\n  Missing keyword 1  argument1");
            addLibraryIndex("MyLibrary", "Existing keyword\n");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 1, proposals.size());
            assertEquals("Missing keyword 1", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
        }

        @Test
        public void should_not_propose_keywords_missing_from_included_file() throws Exception {
            IFile origFile = addFile("orig.txt", "*Settings\nResource  other.txt\n*" + table + "\nTestCaseOrKeyword\n  Existing keyword\n  Missing keyword 1  argument1");
            IFile otherFile = addFile("other.txt", "*Keywords\nExisting keyword\n  Missing keyword 2  argument2\n");
            when(resourceManager.getRelativeFile(origFile, "other.txt")).thenReturn(otherFile);
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 1, proposals.size());
            assertEquals("Missing keyword 1", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
        }

        @Test
        public void should_not_propose_builtin_keyword() throws Exception {
            IFile origFile = addFile("orig.txt", "*" + table + "\nTestCaseOrKeyword\n  " + BUILTIN_KEYWORD + "\n  Missing keyword 1  argument1");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 1, proposals.size());
            assertEquals("Missing keyword 1", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
        }

        @Test
        public void should_filter_missing_keywords_by_attempted_prefix() throws Exception {
            Content origContents = new Content("*" + table + "\nTestCaseOrKeyword\n  Existing keyword\n  Keyword missing 1  argument1\n  Keyword missing 2\n  Keyword existing\n  Non-existent but mismatching prefix\n*Keywords\nKeyword existing\n<arg>Keyword <cursor>missing 1<argend>");
            IFile origFile = addFile("orig.txt", origContents.c());
            visitor = new KeywordDefinitionAttemptVisitor(origFile, origContents.ps("arg-argend", 0, ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(origContents.s("arg-cursor").toLowerCase(), new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 2, proposals.size());
            assertEquals("Keyword missing 1", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
            assertEquals("Keyword missing 2", proposals.get(1).getMatchArgument());
            assertThat(htmlToText(proposals.get(1).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
        }

        @Test
        public void should_not_propose_keyword_definition_for_premature_keyword_calls() throws Exception {
            IFile origFile = addFile("orig.txt", "*" + table + "\n  Premature keyword");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 0, proposals.size());
        }
    }

    @RunWith(NicelyParameterized.class)
    public static class Wildcard_keywords extends BaseTestAttemptVisitor {
        KeywordDefinitionAttemptVisitor visitor;

        private final String table;
        private final String callHostIdentifier;

        @Parameters
        public static List<Object[]> parameters() {
            return Arrays.asList(new Object[][] { { "Test cases", "TEST CASE" }, { "Keywords", "KEYWORD" } });
        }

        public Wildcard_keywords(String table, String callHostIdentifier) {
            this.table = table;
            this.callHostIdentifier = callHostIdentifier;
        }

        @Test
        public void should_not_propose_regular_call_matched_by_wildcard_keyword_definition() throws Exception {
            IFile origFile = addFile("orig.txt", "*" + table + "\nTestCaseOrKeyword\n  Say hello\n*Keywords\nSay ${something}");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 0, proposals.size());
        }

        @Test
        public void should_not_propose_regular_calls_matched_by_wildcard_keyword_definitions_mixed_with_regular() throws Exception {
            IFile origFile = addFile("orig.txt", "*" + table + "\nTestCaseOrKeyword\n  Say hello\n  Missing keyword\n  Existing keyword\n*Keywords\nSay ${something}\n${action} database\nExisting keyword\n");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 1, proposals.size());
            assertEquals("Missing keyword", proposals.get(0).getMatchArgument());
            assertThat(htmlToText(proposals.get(0).getAdditionalProposalInfo()), is(PREAMBLE + "- " + callHostIdentifier + " TestCaseOrKeyword"));
        }

        @Test
        public void should_not_propose_wildcard_call_exactly_matched_by_wildcard_keyword_definition() throws Exception {
            IFile origFile = addFile("orig.txt", "*" + table + "\nTestCaseOrKeyword\n  Log in as ${user} with password ${password}\n*Keywords\nLog in as ${u} with password ${p}");
            visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("", 0).setType(ArgumentType.NEW_KEYWORD));
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
            List<RobotCompletionProposal> proposals = proposalSet.getProposals();
            assertEquals(proposals.toString(), 0, proposals.size());
        }

        // TODO
        // @Test
        // public void should_gently_propose_wildcard_calls_matched_by_regular_keyword_definitions() throws Exception {
        // IFile origFile = addFile("orig.txt", "*" + table +
        // "\nTestCaseOrKeyword\n  Say ${key}\n*Keywords\nSay hello");
        // visitor = new KeywordDefinitionAttemptVisitor(origFile, new ParsedString("",
        // 0).setType(ArgumentType.NEW_KEYWORD));
        // RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", new Region(0, 0));
        // List<RobotCompletionProposal> proposals = proposalSet.getProposals();
        // assertEquals(proposals.toString(), 0, proposals.size());
        // }
    }
}
