/**
 * Copyright 2012 Nitor Creations Oy
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Region;
import org.junit.Test;

import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionMatchVisitor.VisitorInterest;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType.Type;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class TestVariableCompletionMatchVisitor {
    private static final ParsedString PROPOSAL = new ParsedString("${foo}", 0);
    private static final FileWithType DUMMY_LOCATION = new FileWithType(Type.LIBRARY, "TestLibrary");

    @Test
    public void testVisitMatchAddsProposalIfMatchIsFound() throws Exception {
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        VariableCompletionMatchVisitor visitor = getVisitor(proposals, new ParsedString("${fo", 0));
        assertEquals(visitor.visitMatch(PROPOSAL, DUMMY_LOCATION), VisitorInterest.CONTINUE);
        assertEquals(proposals.size(), 1);
        assertEquals(proposals.get(0).getMatchArgument(), PROPOSAL);
    }

    @Test
    public void testVisitMatchAddsProposalIfMatchArgumentIsNull() throws Exception {
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        VariableCompletionMatchVisitor visitor = getVisitor(proposals, null);
        assertEquals(visitor.visitMatch(PROPOSAL, DUMMY_LOCATION), VisitorInterest.CONTINUE);
        assertEquals(proposals.size(), 1);
        assertEquals(proposals.get(0).getMatchArgument(), PROPOSAL);
    }

    @Test
    public void testVisitMatchDoesNotAddProposalIfMatchIsNotFound() throws Exception {
        List<RobotCompletionProposal> proposals = new ArrayList<RobotCompletionProposal>();
        VariableCompletionMatchVisitor visitor = getVisitor(proposals, new ParsedString("${for", 0));
        assertEquals(visitor.visitMatch(PROPOSAL, DUMMY_LOCATION), VisitorInterest.CONTINUE);
        assertTrue(proposals.isEmpty());
    }

    private VariableCompletionMatchVisitor getVisitor(List<RobotCompletionProposal> proposals, ParsedString userInput) {
        return new VariableCompletionMatchVisitor(null, userInput, proposals, new Region(0, 0));
    }
}
