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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;

public class TestRelevantProposalsFilter {

    final RelevantProposalsFilter filter = new RelevantProposalsFilter();

    @Test
    public void should_return_null_when_no_proposalSets_given() throws Exception {
        List<RobotCompletionProposalSet> sets = new ArrayList<RobotCompletionProposalSet>();

        ICompletionProposal[] actual = filter.extractMostRelevantProposals(sets);

        assertNull(actual);
    }

    @Test
    public void should_ignore_empty_proposalSets_when_nothing_else() throws Exception {
        List<RobotCompletionProposalSet> sets = new ArrayList<RobotCompletionProposalSet>();
        sets.add(new RobotCompletionProposalSet());
        sets.add(new RobotCompletionProposalSet());

        ICompletionProposal[] actual = filter.extractMostRelevantProposals(sets);

        assertNull(actual);
    }

    @Test
    public void should_ignore_empty_proposalSets_when_something_else() throws Exception {
        RobotCompletionProposalSet empty = new RobotCompletionProposalSet();
        RobotCompletionProposalSet notEmpty = new RobotCompletionProposalSet();
        notEmpty.setBasedOnInput(false);

        ICompletionProposal[] expected = addProposalsTo(notEmpty, 2, "notEmpty");

        List<RobotCompletionProposalSet> sets = new ArrayList<RobotCompletionProposalSet>();
        sets.add(empty);
        sets.add(notEmpty);

        ICompletionProposal[] actual = filter.extractMostRelevantProposals(sets);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void should_return_single_proposalSet_in_different_states() throws Exception {
        RobotCompletionProposalSet set1 = new RobotCompletionProposalSet();
        set1.setBasedOnInput(false);
        RobotCompletionProposalSet set2 = new RobotCompletionProposalSet();
        set2.setBasedOnInput(false);
        set2.setPriorityProposal();
        RobotCompletionProposalSet set3 = new RobotCompletionProposalSet();
        set3.setBasedOnInput(true);
        RobotCompletionProposalSet set4 = new RobotCompletionProposalSet();
        set4.setBasedOnInput(true);
        set4.setPriorityProposal();

        for (RobotCompletionProposalSet set : new RobotCompletionProposalSet[] { set1, set2, set3, set4 }) {
            ICompletionProposal[] expected = addProposalsTo(set, 3, "set");

            List<RobotCompletionProposalSet> sets = new ArrayList<RobotCompletionProposalSet>();
            sets.add(set);

            ICompletionProposal[] actual = filter.extractMostRelevantProposals(sets);

            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void should_only_return_proposals_based_on_input__normal_priority() throws Exception {
        should_only_return_proposals_based_on_input(false);
    }

    @Test
    public void should_only_return_proposals_based_on_input__high_priority() throws Exception {
        should_only_return_proposals_based_on_input(true);
    }

    private void should_only_return_proposals_based_on_input(boolean highPriority) throws Exception {
        RobotCompletionProposalSet setNotBasedOnInput1 = new RobotCompletionProposalSet();
        setNotBasedOnInput1.setBasedOnInput(false);
        RobotCompletionProposalSet setNotBasedOnInput2 = new RobotCompletionProposalSet();
        setNotBasedOnInput2.setBasedOnInput(false);
        RobotCompletionProposalSet setBasedOnInput1 = new RobotCompletionProposalSet();
        setBasedOnInput1.setBasedOnInput(true);
        RobotCompletionProposalSet setBasedOnInput2 = new RobotCompletionProposalSet();
        setBasedOnInput2.setBasedOnInput(true);
        if (highPriority) {
            setNotBasedOnInput1.setPriorityProposal();
            setNotBasedOnInput2.setPriorityProposal();
            setBasedOnInput1.setPriorityProposal();
            setBasedOnInput2.setPriorityProposal();
        }

        addProposalsTo(setNotBasedOnInput1, 3, "setNotBasedOnInput1");
        addProposalsTo(setNotBasedOnInput2, 3, "setNotBasedOnInput2");
        ICompletionProposal[] expectedBasedOnInput1 = addProposalsTo(setBasedOnInput1, 3, "setBasedOnInput1");
        ICompletionProposal[] expectedBasedOnInput2 = addProposalsTo(setBasedOnInput2, 3, "setBasedOnInput2");

        List<RobotCompletionProposalSet> sets = new ArrayList<RobotCompletionProposalSet>();
        sets.add(setNotBasedOnInput1);
        sets.add(setBasedOnInput1);
        sets.add(setNotBasedOnInput2);
        sets.add(setBasedOnInput2);

        ICompletionProposal[] actual = filter.extractMostRelevantProposals(sets);

        // expected order is important here
        assertArrayEquals(concat(expectedBasedOnInput1, expectedBasedOnInput2), actual);
    }

    @Test
    public void should_prioritize_proposals__based_on_input() throws Exception {
        should_prioritize_proposals(true);
    }

    @Test
    public void should_prioritize_proposals__not_based_on_input() throws Exception {
        should_prioritize_proposals(false);
    }

    private void should_prioritize_proposals(boolean basedOnInput) {
        RobotCompletionProposalSet setNormalPriority1 = new RobotCompletionProposalSet();
        setNormalPriority1.setBasedOnInput(basedOnInput);
        RobotCompletionProposalSet setNormalPriority2 = new RobotCompletionProposalSet();
        setNormalPriority2.setBasedOnInput(basedOnInput);
        RobotCompletionProposalSet setHighPriority1 = new RobotCompletionProposalSet();
        setHighPriority1.setBasedOnInput(basedOnInput);
        setHighPriority1.setPriorityProposal();
        RobotCompletionProposalSet setHighPriority2 = new RobotCompletionProposalSet();
        setHighPriority2.setBasedOnInput(basedOnInput);
        setHighPriority2.setPriorityProposal();

        ICompletionProposal[] expectedNormalPriority1 = addProposalsTo(setNormalPriority1, 3, "setNormalPriority1");
        ICompletionProposal[] expectedNormalPriority2 = addProposalsTo(setNormalPriority2, 3, "setNormalPriority2");
        ICompletionProposal[] expectedHighPriority1 = addProposalsTo(setHighPriority1, 3, "setHighPriority1");
        ICompletionProposal[] expectedHighPriority2 = addProposalsTo(setHighPriority2, 3, "setHighPriority2");

        List<RobotCompletionProposalSet> sets = new ArrayList<RobotCompletionProposalSet>();
        sets.add(setNormalPriority1);
        sets.add(setHighPriority1);
        sets.add(setNormalPriority2);
        sets.add(setHighPriority2);

        ICompletionProposal[] actual = filter.extractMostRelevantProposals(sets);

        // expected order is important here
        assertArrayEquals(concat(expectedHighPriority1, expectedHighPriority2, expectedNormalPriority1, expectedNormalPriority2), actual);
    }

    @Test
    public void should_only_return_proposals_based_on_input_regardless_of_high_priority() throws Exception {
        RobotCompletionProposalSet setHighPriorityNotBasedOnInput = new RobotCompletionProposalSet();
        setHighPriorityNotBasedOnInput.setBasedOnInput(false);
        setHighPriorityNotBasedOnInput.setPriorityProposal();
        RobotCompletionProposalSet setNormalPriorityBasedOnInput = new RobotCompletionProposalSet();
        setNormalPriorityBasedOnInput.setBasedOnInput(true);

        addProposalsTo(setHighPriorityNotBasedOnInput, 3, "setHighPriorityNotBasedOnInput");
        ICompletionProposal[] expectedNormalPriorityBasedOnInput = addProposalsTo(setNormalPriorityBasedOnInput, 3, "setNormalPriorityBasedOnInput");

        List<RobotCompletionProposalSet> sets = new ArrayList<RobotCompletionProposalSet>();
        sets.add(setHighPriorityNotBasedOnInput);
        sets.add(setNormalPriorityBasedOnInput);

        ICompletionProposal[] actual = filter.extractMostRelevantProposals(sets);

        assertArrayEquals(expectedNormalPriorityBasedOnInput, actual);
    }

    /**
     * Add [n] proposals to given [set] and also return an array containing the same proposals.
     * 
     * @param pref
     *            toString prefix to use for the proposals, identifying them in error conditions
     * @return array containing the proposals
     */
    private ICompletionProposal[] addProposalsTo(RobotCompletionProposalSet set, int n, String pref) {
        List<RobotCompletionProposal> proposals = set.getProposals();
        for (int i = 0; i < n; ++i) {
            proposals.add(new MockRobotCompletionProposal(pref, i));
        }
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    private static class MockRobotCompletionProposal extends RobotCompletionProposal {
        private final String pref;
        private final int i;

        private MockRobotCompletionProposal(String pref, int i) {
            super(null, null, null, null, null, null, null);
            this.pref = pref;
            this.i = i;
        }

        @Override
        public String toString() {
            return pref + '[' + i + ']';
        }
    }

    private static <T> T[] concat(T[]... a) {
        int len = 0;
        for (T[] t : a) {
            len += t.length;
        }
        T[] result = Arrays.copyOf(a[0], len);
        int off = a[0].length;
        for (int i = 1; i < a.length; ++i) {
            System.arraycopy(a[i], 0, result, off, a[i].length);
            off += a[i].length;
        }
        return result;
    }

}
