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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Image;

import com.nitorcreations.robotframework.eclipseide.builder.parser.ArgumentPreParser;
import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.util.ParserUtil;
import com.nitorcreations.robotframework.eclipseide.internal.util.DefinitionFinder;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.internal.util.LineFinder;
import com.nitorcreations.robotframework.eclipseide.internal.util.LineMatchVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest;
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
        List<String> attempts = generateAttempts(argument, documentOffset, argumentValue);

        for (String attempt : attempts) {
            Map<String, RobotCompletionProposal> ourProposals = new LinkedHashMap<String, RobotCompletionProposal>();
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
            RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
            ourProposalSet.getProposals().addAll(ourProposals.values());

            if (ourProposalSet.getProposals().size() == 1 && proposalsIsEmptyOrContainsOnly(ourProposalSet.getProposals(), argument)) {
                // Found a single exact hit - probably means it was content-assisted earlier and the user now wants to
                // change it to something else
                continue;
            }
            if (!ourProposalSet.getProposals().isEmpty()) {
                ourProposalSet.setBasedOnInput(!attempt.isEmpty());
                proposalSets.add(ourProposalSet);
                return;
            }
        }
    }

    @Override
    public void addSettingTableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        String argumentValue = argument.getValue();
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argumentValue.length());
        List<String> attempts = generateAttempts(argument, documentOffset, argumentValue.toLowerCase());

        List<String> settingKeys = new ArrayList<String>(ArgumentPreParser.getSettingKeys());
        Collections.sort(settingKeys);
        for (String attempt : attempts) {
            RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
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
                continue;
            }
            if (!ourProposalSet.getProposals().isEmpty()) {
                ourProposalSet.setBasedOnInput(!attempt.isEmpty());
                proposalSets.add(ourProposalSet);
                return;
            }
        }
    }

    private static final Set<LineType> KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES = new HashSet<LineType>();

    static {
        // all LineTypes that might have KEYWORD_CALL arguments or keyword definitions
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.TESTCASE_TABLE_TESTCASE_BEGIN);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.TESTCASE_TABLE_TESTCASE_LINE);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.KEYWORD_TABLE_KEYWORD_BEGIN);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.KEYWORD_TABLE_KEYWORD_LINE);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.CONTINUATION_LINE);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.SETTING_TABLE_LINE);
    }

    @Override
    public void addKeywordDefinitionProposals(final IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        String argumentValue = argument.getValue();
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argumentValue.length());
        List<String> attempts = generateAttempts(argument, documentOffset, argumentValue.toLowerCase());

        final Map<String, List<KeywordNeed>> undefinedKeywords = collectUndefinedKeywords(file, argument);
        for (String attempt : attempts) {
            RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
            for (Entry<String, List<KeywordNeed>> e : undefinedKeywords.entrySet()) {
                String key = e.getKey();
                if (key.toLowerCase().startsWith(attempt)) {
                    ParsedString proposal = new ParsedString(key, 0);
                    proposal.setType(ArgumentType.SETTING_KEY);

                    Image image = null;
                    String displayString = key;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Called from the following testcases/keywords:<ul>");
                    for (KeywordNeed need : e.getValue()) {
                        String callerType = need.callingTestcaseOrKeyword.getType() == ArgumentType.NEW_TESTCASE ? "TEST CASE" : "KEYWORD";
                        String callerName = need.callingTestcaseOrKeyword.getValue();
                        sb.append("<li><b>" + callerType + "</b> " + callerName + "</li>");
                    }
                    String additionalProposalInfo = sb.toString();
                    String informationDisplayString = null;
                    ourProposalSet.getProposals().add(new RobotCompletionProposal(proposal, null, replacementRegion, image, displayString, informationDisplayString, additionalProposalInfo));
                }
            }
            if (ourProposalSet.getProposals().size() == 1 && proposalsIsEmptyOrContainsOnly(ourProposalSet.getProposals(), argument)) {
                // Found a single exact hit - probably means it was content-assisted earlier and the user now wants to
                // change it to something else
                continue;
            }
            if (!ourProposalSet.getProposals().isEmpty()) {
                ourProposalSet.setBasedOnInput(!attempt.isEmpty());
                proposalSets.add(ourProposalSet);
                return;
            }
        }
    }

    private List<String> generateAttempts(ParsedString argument, int documentOffset, String lookFor) {
        List<String> attempts = new ArrayList<String>(3);
        attempts.add(lookFor);
        int argumentOffset = documentOffset - argument.getArgCharPos();
        if (lookFor.length() > argumentOffset) {
            attempts.add(lookFor.substring(0, argumentOffset));
        }
        attempts.add("");
        return attempts;
    }

    private static class KeywordNeed {
        public final ParsedString callingTestcaseOrKeyword;
        public final ParsedString calledKeyword;

        KeywordNeed(ParsedString callingTestcaseOrKeyword, ParsedString calledKeyword) {
            this.callingTestcaseOrKeyword = callingTestcaseOrKeyword;
            this.calledKeyword = calledKeyword;
        }
    }

    /**
     * Collect a list of keywords that are called in this file, but not defined in the file or in imported
     * resources/libraries. If assumeThisKeywordIsUndefined is not null, then it will not be considered as defined. This
     * is used to include the keyword already defined at the line where the cursor already is.
     */
    private Map<String, List<KeywordNeed>> collectUndefinedKeywords(final IFile file, final ParsedString assumeThisKeywordIsUndefined) {
        final Map<String, List<KeywordNeed>> neededKeywords = new LinkedHashMap<String, List<KeywordNeed>>();
        final List<String> definedKeywords = new ArrayList<String>();
        LineFinder.acceptMatches(file, new LineMatchVisitor() {

            @Override
            public VisitorInterest visitMatch(RobotLine line, FileWithType lineLocation) {
                if (lineLocation.getFile() == file) {
                    visitKeywordCalls(line);
                }
                if (line.type == LineType.KEYWORD_TABLE_KEYWORD_BEGIN) {
                    visitKeywordDefinition(line, lineLocation);
                }
                return VisitorInterest.CONTINUE;
            }

            private ParsedString lastDefinedTestcaseOrKeyword;

            private void visitKeywordCalls(RobotLine line) {
                for (ParsedString argument : line.arguments) {
                    switch (argument.getType()) {
                        case NEW_TESTCASE:
                        case NEW_KEYWORD:
                            lastDefinedTestcaseOrKeyword = argument;
                            break;
                        case KEYWORD_CALL:
                        case KEYWORD_CALL_DYNAMIC:
                            String argumentStr = argument.getValue();
                            List<KeywordNeed> list = neededKeywords.get(argumentStr);
                            if (list == null) {
                                list = new ArrayList<KeywordNeed>();
                                neededKeywords.put(argumentStr, list);
                            }
                            list.add(new KeywordNeed(lastDefinedTestcaseOrKeyword, argument));
                            break;
                    }
                }
            }

            private void visitKeywordDefinition(RobotLine line, FileWithType lineLocation) {
                ParsedString definedKeyword = line.arguments.get(0);
                if (definedKeyword != assumeThisKeywordIsUndefined) {
                    definedKeywords.add(definedKeyword.getValue());
                }
            }

            @Override
            public boolean visitImport(IFile currentFile, RobotLine line) {
                return true;
            }

            @Override
            public Set<LineType> getWantedLineTypes() {
                return KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES;
            }

            @Override
            public boolean wantsLibraryVariables() {
                return false;
            }

            @Override
            public boolean wantsLibraryKeywords() {
                return true;
            }
        });
        neededKeywords.keySet().removeAll(definedKeywords);
        return neededKeywords;
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
        ParsedString subArgument = argument.extractRegion(replacementRegion);
        VariableCompletionMatchVisitorProvider visitorProvider = new VariableCompletionMatchVisitorProvider(file, replacementRegion, maxVariableCharPos, maxSettingCharPos);
        RobotCompletionProposalSet variableProposals = computeCompletionProposals(file, documentOffset, subArgument, visitorProvider);
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
                ParsedString argumentleftPart = argument.extractRegion(new Region(argument.getArgCharPos(), argumentOff));
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
