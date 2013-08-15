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
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Region;
import org.junit.Test;

public class TestKeywordCallAttemptVisitor extends BaseTestAttemptVisitor {
    static final String LINKED_PREFIX = "[linked] ";
    static final String LINKED_FILENAME = "linked.txt";
    static final String LINKED_KEYWORD = "Say Hello";

    @Test
    public void should_propose_keyword_from_included_resource_file() throws Exception {
        IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\n");
        IFile linkedFile = addFile(LINKED_FILENAME, "*Keywords\n" + LINKED_KEYWORD + "\n");
        when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

        RobotCompletionProposalSet proposalSet = new KeywordCallAttemptVisitor(origFile).visitAttempt("", new Region(0, 0));

        assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
        verifyProposal(proposalSet, 0, LINKED_PREFIX + LINKED_KEYWORD, LINKED_KEYWORD);
        verifyProposal(proposalSet, 1, BUILTIN_PREFIX + BUILTIN_KEYWORD, BUILTIN_KEYWORD);
        assertFalse("Should be false always for keyword call attempts", proposalSet.isPriorityProposal());
    }

    @Test
    // #35
    public void should_propose_keyword_only_once_from_resource_file_included_twice() throws Exception {
        IFile origFile = addFile("orig.txt", "*Settings\nResource  " + LINKED_FILENAME + "\nResource  " + LINKED_FILENAME + "\n");
        IFile linkedFile = addFile(LINKED_FILENAME, "*Keywords\n" + LINKED_KEYWORD + "\n");
        when(resourceManager.getRelativeFile(origFile, LINKED_FILENAME)).thenReturn(linkedFile);

        RobotCompletionProposalSet proposalSet = new KeywordCallAttemptVisitor(origFile).visitAttempt("", new Region(0, 0));

        assertEquals("Got wrong amount of proposals: " + proposalSet.getProposals(), 2, proposalSet.getProposals().size());
        verifyProposal(proposalSet, 0, LINKED_PREFIX + LINKED_KEYWORD, LINKED_KEYWORD);
        verifyProposal(proposalSet, 1, BUILTIN_PREFIX + BUILTIN_KEYWORD, BUILTIN_KEYWORD);
        assertFalse("Should be false always for keyword call attempts", proposalSet.isPriorityProposal());
    }
}
