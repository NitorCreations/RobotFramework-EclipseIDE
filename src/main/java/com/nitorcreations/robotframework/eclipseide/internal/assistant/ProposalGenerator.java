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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Image;

import com.nitorcreations.robotframework.eclipseide.builder.parser.ArgumentPreParser;
import com.nitorcreations.robotframework.eclipseide.builder.parser.util.ParserUtil;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class ProposalGenerator implements IProposalGenerator {

    private static final Map<String, String> tableNameToFull = new LinkedHashMap<String, String>();

    static {
        tableNameToFull.put("variables", "* Variables");
        tableNameToFull.put("settings", "* Settings");
        tableNameToFull.put("metadata", "* Settings");
        tableNameToFull.put("testcases", "* Test Cases");
        tableNameToFull.put("keywords", "* Keywords");
        tableNameToFull.put("userkeywords", "* Keywords");
    }

    @Override
    public void addTableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        String argumentValue = argument.getValue();
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argumentValue.length());

        List<String> attempts = new ArrayList<String>(3);
        attempts.add(argumentValue);
        int argumentOffset = documentOffset - argument.getArgCharPos();
        if (argumentValue.length() > argumentOffset) {
            attempts.add(argumentValue.substring(0, argumentOffset));
        }
        attempts.add("");

        Map<String, RobotCompletionProposal> ourProposals = new LinkedHashMap<String, RobotCompletionProposal>();
        Boolean basedOnInput = null;
        for (String attempt : attempts) {
            String tableArgument = ParserUtil.parseTable(attempt);
            for (Entry<String, String> e : tableNameToFull.entrySet()) {
                if (e.getKey().startsWith(tableArgument)) {
                    ParsedString proposal = new ParsedString(e.getValue(), 0);
                    proposal.setType(ArgumentType.TABLE);

                    Image image = null;
                    String displayString = e.getValue();
                    String additionalProposalInfo = null;
                    String informationDisplayString = null;
                    RobotCompletionProposal rcp = new RobotCompletionProposal(proposal, null, replacementRegion, image, displayString, informationDisplayString, additionalProposalInfo);
                    rcp.setCursorPositionAdjustment(1);
                    ourProposals.put(e.getValue(), rcp);
                }
            }
            if (ourProposals.size() == 1 && ourProposals.values().iterator().next().getMatchArgument().getValue().equals(argumentValue)) {
                // Found a single exact hit - probably means it was content-assisted earlier and the user now wants to
                // change it to something else
                ourProposals.clear();
                continue;
            }
            if (!ourProposals.isEmpty()) {
                basedOnInput = !attempt.isEmpty();
                break;
            }
        }
        if (!ourProposals.isEmpty()) {
            RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
            ourProposalSet.getProposals().addAll(ourProposals.values());
            ourProposalSet.setBasedOnInput(basedOnInput);
            proposalSets.add(ourProposalSet);
        }
    }

    @Override
    public void addSettingTableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        String argumentValue = argument.getValue();
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argumentValue.length());

        List<String> attempts = new ArrayList<String>(3);
        attempts.add(argumentValue.toLowerCase());
        int argumentOffset = documentOffset - argument.getArgCharPos();
        if (argumentValue.length() > argumentOffset) {
            attempts.add(argumentValue.substring(0, argumentOffset));
        }
        attempts.add("");

        List<String> settingKeys = new ArrayList<String>(ArgumentPreParser.getSettingKeys());
        Collections.sort(settingKeys);
        RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
        for (String attempt : attempts) {
            for (String key : settingKeys) {
                if (key.toLowerCase().startsWith(attempt)) {
                    ParsedString proposal = new ParsedString(key, 0);
                    proposal.setType(ArgumentType.SETTING_KEY);

                    Image image = null;
                    String displayString = key;
                    String additionalProposalInfo = null;
                    String informationDisplayString = null;
                    ourProposalSet.getProposals().add(new RobotCompletionProposal(proposal, null, replacementRegion, image, displayString, informationDisplayString, additionalProposalInfo));
                }
            }
            if (ourProposalSet.getProposals().size() == 1 && ourProposalSet.getProposals().get(0).getMatchArgument().getValue().equals(argumentValue)) {
                // Found a single exact hit - probably means it was content-assisted earlier and the user now wants to
                // change it to something else
                ourProposalSet.getProposals().clear();
                continue;
            }
            if (!ourProposalSet.getProposals().isEmpty()) {
                ourProposalSet.setBasedOnInput(!attempt.isEmpty());
                break;
            }
        }
        if (!ourProposalSet.getProposals().isEmpty()) {
            proposalSets.add(ourProposalSet);
        }
    }

    @Override
    public void addKeywordCallProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        KeywordCompletionMatchVisitorProvider visitorProvider = new KeywordCompletionMatchVisitorProvider(file, replacementRegion);
        proposalSets.add(computeCompletionProposals(file, documentOffset, argument, visitorProvider));
    }

    @Override
    public void addVariableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets, int maxVariableCharPos, int maxSettingCharPos) {
        IRegion replacementRegion = VariableReplacementRegionCalculator.calculate(argument, documentOffset);
        VariableCompletionMatchVisitorProvider visitorProvider = new VariableCompletionMatchVisitorProvider(file, replacementRegion, maxVariableCharPos, maxSettingCharPos);
        RobotCompletionProposalSet variableProposals = computeCompletionProposals(file, documentOffset, argument, visitorProvider);
        if (replacementRegion.getLength() > 0) {
            // the cursor is positioned for replacing a variable, so put the variable proposals first
            proposalSets.add(0, variableProposals);
        } else {
            // default positioning of proposals
            proposalSets.add(variableProposals);
        }
    }

    private RobotCompletionProposalSet computeCompletionProposals(IFile file, int documentOffset, ParsedString argument, CompletionMatchVisitorProvider visitorProvider) {
        System.out.println("RobotContentAssistant.computeCompletionProposals() " + documentOffset + " " + argument);
        RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
        // first find matches that use the whole input as search string
        DefinitionFinder.acceptMatches(file, visitorProvider.get(argument, ourProposalSet.getProposals()));
        if (argument != null && proposalsIsEmptyOrContainsOnly(ourProposalSet.getProposals(), argument)) {
            ourProposalSet.getProposals().clear();
            // int lineOffset = documentOffset - lineCharPos;
            if (argument.getArgCharPos() < documentOffset && documentOffset < argument.getArgEndCharPos()) {
                // try again, but only up to cursor
                int argumentOff = documentOffset - argument.getArgCharPos();
                ParsedString argumentleftPart = new ParsedString(argument.getValue().substring(0, argumentOff), argument.getArgCharPos());
                DefinitionFinder.acceptMatches(file, visitorProvider.get(argumentleftPart, ourProposalSet.getProposals()));
            }
            if (proposalsIsEmptyOrContainsOnly(ourProposalSet.getProposals(), argument)) {
                // try again, ignoring user input, i.e. show all possible keywords
                ourProposalSet.getProposals().clear();
                DefinitionFinder.acceptMatches(file, visitorProvider.get(null, ourProposalSet.getProposals()));
                ourProposalSet.setBasedOnInput(false);
            } else {
                ourProposalSet.setBasedOnInput(true);
            }
        } else {
            ourProposalSet.setBasedOnInput(!argument.isEmpty());
        }
        return ourProposalSet;
    }

    private boolean proposalsIsEmptyOrContainsOnly(List<RobotCompletionProposal> proposals, ParsedString argument) {
        if (proposals.size() != 1) {
            return proposals.isEmpty();
        }
        return proposals.get(0).getMatchArgument().getValue().equals(argument.getValue());
    }

}
