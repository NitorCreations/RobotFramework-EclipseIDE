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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.junit.Test;

import com.nitorcreations.robotframework.eclipseide.builder.parser.ArgumentPreParser;

/**
 * Tests a subset of the setting proposals available.
 */
public class TestSettingTableAttemptVisitor {

    private static final String[] ALL_PROPOSALS = ArgumentPreParser.getSettingKeys().toArray(new String[0]);

    final SettingTableAttemptVisitor visitor = new SettingTableAttemptVisitor();

    final IRegion replacementRegion = mock(IRegion.class);

    @Test
    public void should_not_return_a_high_priority_proposalSet() {
        RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", replacementRegion);
        assertThat(proposalSet.isPriorityProposal(), is(false));
    }

    @Test
    public void should_not_set_basedOnInput_property() {
        RobotCompletionProposalSet proposalSet = visitor.visitAttempt("", replacementRegion);
        try {
            proposalSet.isBasedOnInput();
            fail("basedOnInput property was unexpectedly set");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void should_return_all_for_empty_input() {
        String attempt = "";
        RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
        verifyExpectedProposals(attempt, proposalSet, ALL_PROPOSALS);
    }

    @Test
    public void should_return_Documentation_proposal_for_relevant_prefixes() {
        for (String attempt : new String[] { "do", "documentation" }) {
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet, "Documentation");
        }
    }

    @Test
    public void should_return_Suite_Setup_proposal_for_relevant_prefixes() {
        for (String attempt : new String[] { "suite s", "suite setup" }) {
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet, "Suite Setup");
        }
    }

    @Test
    public void should_return_T_proposals_for_relevant_prefixes() {
        for (String attempt : new String[] { "t", "test" }) {
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet, "Test Setup", "Test Template", "Test Teardown", "Test Timeout");
        }
    }

    @Test
    public void should_not_return_any_proposals_for_irrelevant_prefixes() {
        for (char ch = 33; ch <= 126; ++ch) {
            if ("dflmprstv".indexOf(ch) != -1 || Character.isUpperCase(ch)) {
                continue;
            }
            String attempt = Character.toString(ch);
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet);
        }
    }

    private static void verifyExpectedProposals(String attempt, RobotCompletionProposalSet proposalSet, String... expectedProposals) {
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>(proposalSet.getProposals());
        next_proposal: for (String expectedProposal : expectedProposals) {
            Iterator<RobotCompletionProposal> it = proposals.iterator();
            while (it.hasNext()) {
                RobotCompletionProposal proposal = it.next();
                String matchArgument = proposal.getMatchArgument();
                if (matchArgument.equals(expectedProposal)) {
                    it.remove();
                    continue next_proposal;
                }
            }
            fail("Returned proposals did not contain '" + expectedProposal + "' for attempt'" + attempt + "'");
        }
        assertTrue("Unexpected proposals encountered: " + str(proposals) + " for attempt'" + attempt + "'", proposals.isEmpty());
    }

    private static String str(List<RobotCompletionProposal> proposals) {
        List<String> s = new ArrayList<String>();
        for (RobotCompletionProposal proposal : proposals) {
            s.add(proposal.getMatchArgument());
        }
        return s.toString();
    }
}
