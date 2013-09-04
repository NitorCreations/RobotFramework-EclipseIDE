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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.junit.Before;
import org.junit.Test;

import com.nitorcreations.robotframework.eclipseide.preferences.PreferenceConstants;

public class TestTableAttemptVisitor {

    public static final String VARIABLES_PROPOSAL = "* Variables";
    public static final String SETTINGS_PROPOSAL = "* Settings";
    public static final String TEST_CASES_PROPOSAL = "* Test Cases";
    public static final String KEYWORDS_PROPOSAL = "* Keywords";

    TableAttemptVisitor visitor;

    final IRegion replacementRegion = mock(IRegion.class);

    @Before
    public void setupVisitor() {
        IPreferenceStore preferenceStore = mock(IPreferenceStore.class, "preferenceStore");
        when(preferenceStore.getString(PreferenceConstants.P_VARIABLE_TABLE_FORMAT)).thenReturn(VARIABLES_PROPOSAL);
        when(preferenceStore.getString(PreferenceConstants.P_SETTING_TABLE_FORMAT)).thenReturn(SETTINGS_PROPOSAL);
        when(preferenceStore.getString(PreferenceConstants.P_TESTCASE_TABLE_FORMAT)).thenReturn(TEST_CASES_PROPOSAL);
        when(preferenceStore.getString(PreferenceConstants.P_KEYWORD_TABLE_FORMAT)).thenReturn(KEYWORDS_PROPOSAL);
        visitor = new TableAttemptVisitor(preferenceStore);
    }

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
        verifyExpectedProposals(attempt, proposalSet, VARIABLES_PROPOSAL, SETTINGS_PROPOSAL, TEST_CASES_PROPOSAL, KEYWORDS_PROPOSAL);
    }

    @Test
    public void should_return_all_for_purely_ignored_input() {
        String attempt = "*   *   *";
        RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
        verifyExpectedProposals(attempt, proposalSet, VARIABLES_PROPOSAL, SETTINGS_PROPOSAL, TEST_CASES_PROPOSAL, KEYWORDS_PROPOSAL);
    }

    @Test
    public void should_return_Variables_proposal_for_relevant_prefixes() {
        for (String attempt : new String[] { "v", "* vari", "*** variables ***", VARIABLES_PROPOSAL.toLowerCase() }) {
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet, VARIABLES_PROPOSAL);
        }
    }

    @Test
    public void should_return_Settings_proposal_for_relevant_prefixes() {
        for (String attempt : new String[] { "s", "* sett", "*** settings ***", "m", "* meta", "*** metadata ***", SETTINGS_PROPOSAL.toLowerCase() }) {
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet, SETTINGS_PROPOSAL);
        }
    }

    @Test
    public void should_return_Test_Cases_proposal_for_relevant_prefixes() {
        for (String attempt : new String[] { "t", "* test", "*** test cases ***", TEST_CASES_PROPOSAL.toLowerCase() }) {
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet, TEST_CASES_PROPOSAL);
        }
    }

    @Test
    public void should_return_Keywords_proposal_for_relevant_prefixes() {
        for (String attempt : new String[] { "k", "* key", "*** keywords ***", "u", "* use", "*** user keywords ***", KEYWORDS_PROPOSAL.toLowerCase() }) {
            RobotCompletionProposalSet proposalSet = visitor.visitAttempt(attempt, replacementRegion);
            verifyExpectedProposals(attempt, proposalSet, KEYWORDS_PROPOSAL);
        }
    }

    @Test
    public void should_not_return_any_proposals_for_irrelevant_prefixes() {
        for (char ch = 33; ch <= 126; ++ch) {
            if ("vsmtku*".indexOf(ch) != -1 || Character.isUpperCase(ch)) {
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
        assertTrue("Unexpected proposals encountered: " + proposals + " for attempt'" + attempt + "'", proposals.isEmpty());
    }
}
