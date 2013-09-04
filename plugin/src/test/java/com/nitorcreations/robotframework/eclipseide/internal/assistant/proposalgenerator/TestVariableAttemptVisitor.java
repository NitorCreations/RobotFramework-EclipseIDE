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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Region;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class TestVariableAttemptVisitor {

    public static class when_all extends BaseTestAttemptVisitor {
        static final String LINKED_PREFIX = "[linked] ";
        static final String LINKED_FILENAME = "linked.txt";
        static final String FOO_VARIABLE = "${FOO}";
        static final String LINKED_VARIABLE = "${LINKEDVAR}";

        @Test
        public void should_propose_all_variables() throws Exception {
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n");
            IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n" + LINKED_VARIABLE + "  value\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

            RobotCompletionProposalSet proposalSet = new VariableAttemptVisitor(origFile, Integer.MAX_VALUE, Integer.MAX_VALUE).visitAttempt("", new Region(0, 0));

            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 3, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
            verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
            verifyProposal(proposalSet, 2, LINKED_PREFIX + LINKED_VARIABLE, LINKED_VARIABLE);
            assertFalse("Should be false when attempt is empty", proposalSet.isPriorityProposal());
        }

        @Test
        // #35
        public void should_propose_variable_only_once_from_resource_file_included_twice() throws Exception {
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n");
            IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n" + LINKED_VARIABLE + "  value\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

            RobotCompletionProposalSet proposalSet = new VariableAttemptVisitor(origFile, Integer.MAX_VALUE, Integer.MAX_VALUE).visitAttempt("", new Region(0, 0));

            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 3, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
            verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
            verifyProposal(proposalSet, 2, LINKED_PREFIX + LINKED_VARIABLE, LINKED_VARIABLE);
            // ${LINKEDVAR} not included twice
            assertFalse("Should be false when attempt is empty", proposalSet.isPriorityProposal());
        }

        @Test
        // #43
        public void should_replace_partially_typed_variable() throws Exception {
            String origContents1 = "*Variables\n" + FOO_VARIABLE + "  bar\n*Testcase\n  Log  ";
            String origContents2 = "${f";
            String origContents = origContents1 + origContents2;
            IFile origFile = addFile("orig.txt", origContents);

            RobotCompletionProposalSet proposalSet = new VariableAttemptVisitor(origFile, Integer.MAX_VALUE, Integer.MAX_VALUE).visitAttempt(origContents2, new Region(origContents1.length(), origContents2.length()));

            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 1, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, FOO_VARIABLE, FOO_VARIABLE, origContents1.length(), origContents2.length());
            assertTrue("Should be true when attempt is non-empty", proposalSet.isPriorityProposal());
        }
    }

    // #23
    public static class when_only_local extends BaseTestAttemptVisitor {
        static final String LINKED_FILENAME = "linked.txt";
        static final String FOO_VARIABLE = "${FOO}";

        @Test
        public void should_only_propose_BuiltIn_and_local_variables() throws Exception {
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n");
            IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n${LINKEDVAR}  value\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

            RobotCompletionProposalSet proposalSet = new VariableAttemptVisitor(origFile, Integer.MAX_VALUE, -1).visitAttempt("", new Region(0, 0));

            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
            verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
            // ${LINKEDVAR} excluded
            assertFalse("Should be false when attempt is empty", proposalSet.isPriorityProposal());
        }

        @Test
        public void should_only_propose_BuiltIn_variables_when_no_local_variables_present() throws Exception {
            IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\nResource  " + LINKED_FILENAME);
            IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n${LINKEDVAR}  value\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

            RobotCompletionProposalSet proposalSet = new VariableAttemptVisitor(origFile, Integer.MAX_VALUE, -1).visitAttempt("", new Region(0, 0));

            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 1, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
            // ${LINKEDVAR} excluded
            assertFalse("Should be false when attempt is empty", proposalSet.isPriorityProposal());
        }

        @Test
        public void should_propose_subset_when_maxVariableCharPos_set() throws Exception {
            String origContents1 = "*Settings\nResource  " + LINKED_FILENAME + "\n*Variables\n" + FOO_VARIABLE + "  bar\n";
            String origContents2 = "${BAR}  bar\n${ZOT}  zot\n";
            IFile origFile = addFile("orig.txt", origContents1 + origContents2);
            IFile linkedFile = addFile(LINKED_FILENAME, "*Variables\n${LINKEDVAR}  value\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

            RobotCompletionProposalSet proposalSet = new VariableAttemptVisitor(origFile, origContents1.length() - 1, -1).visitAttempt("", new Region(0, 0));

            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
            verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
            // ${BAR} and ${LINKEDVAR} excluded
            assertFalse("Should be false when attempt is empty", proposalSet.isPriorityProposal());
        }

    }

    // #23
    public static class when_partially_imported extends BaseTestAttemptVisitor {
        static final String LINKED_PREFIX_1 = "[linked1] ";
        static final String LINKED_FILENAME_1 = "linked1.txt";
        static final String LINKED_FILENAME_2 = "linked2.txt";
        static final String FOO_VARIABLE = "${FOO}";
        static final String LINKED_VARIABLE_1 = "${LINKEDVAR1}";
        static final String LINKED_VARIABLE_2 = "${LINKEDVAR2}";

        @Test
        public void should_propose_subset_when_maxSettingsCharPos_set() throws Exception {
            String origContents1 = "*Settings\nResource  " + LINKED_FILENAME_1 + "\n";
            String origContents2 = "Resource  " + LINKED_FILENAME_2 + "\n*Variables\n" + FOO_VARIABLE + "  bar\n";
            IFile origFile = addFile("orig.txt", origContents1 + origContents2);
            IFile linkedFile1 = addFile(LINKED_FILENAME_1, "*Variables\n" + LINKED_VARIABLE_1 + "  value\n");
            IFile linkedFile2 = addFile(LINKED_FILENAME_2, "*Variables\n" + LINKED_VARIABLE_2 + "  value\n");
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME_1)).thenReturn(linkedFile1);
            when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME_2)).thenReturn(linkedFile2);

            RobotCompletionProposalSet proposalSet = new VariableAttemptVisitor(origFile, Integer.MAX_VALUE, origContents1.length() - 1).visitAttempt("", new Region(0, 0));

            assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 3, proposalSet.getProposals().size());
            verifyProposal(proposalSet, 0, BUILTIN_PREFIX + BUILTIN_VARIABLE, BUILTIN_VARIABLE);
            verifyProposal(proposalSet, 1, FOO_VARIABLE, FOO_VARIABLE);
            verifyProposal(proposalSet, 2, LINKED_PREFIX_1 + LINKED_VARIABLE_1, LINKED_VARIABLE_1);
            // ${LINKED_VARIABLE_2} excluded
            assertFalse("Should be false when attempt is empty", proposalSet.isPriorityProposal());
        }
    }
}
