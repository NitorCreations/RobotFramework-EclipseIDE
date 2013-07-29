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

    @Override
    public void addTableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        acceptAttempts(argument, documentOffset, argument.getValue(), proposalSets, new TableAttemptVisitor());
    }

    public static class TableAttemptVisitor implements AttemptVisitor {
        private final Map<String, String> tableNameToFull = new LinkedHashMap<String, String>();

        public TableAttemptVisitor() {
            tableNameToFull.put("variables", "* Variables");
            tableNameToFull.put("settings", "* Settings");
            tableNameToFull.put("metadata", "* Settings");
            tableNameToFull.put("testcases", "* Test Cases");
            tableNameToFull.put("keywords", "* Keywords");
            tableNameToFull.put("userkeywords", "* Keywords");
        }

        @Override
        public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
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
            return ourProposalSet;
        }
    }

    @Override
    public void addSettingTableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        acceptAttempts(argument, documentOffset, argument.getValue().toLowerCase(), proposalSets, new SettingTableAttemptVisitor());
    }

    public static class SettingTableAttemptVisitor implements AttemptVisitor {

        private final List<String> settingKeys;

        public SettingTableAttemptVisitor() {
            settingKeys = new ArrayList<String>(ArgumentPreParser.getSettingKeys());
            Collections.sort(settingKeys);
        }

        @Override
        public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
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
            return ourProposalSet;
        }
    }

    @Override
    public void addKeywordDefinitionProposals(final IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        acceptAttempts(argument, documentOffset, argument.getValue().toLowerCase(), proposalSets, new KeywordDefinitionAttemptVisitor(file, argument));
    }

    public static class KeywordDefinitionAttemptVisitor implements AttemptVisitor {
        final Map<String, List<KeywordNeed>> undefinedKeywords;

        public KeywordDefinitionAttemptVisitor(IFile file, ParsedString argument) {
            undefinedKeywords = collectUndefinedKeywords(file, argument);
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
        public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
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
            return ourProposalSet;
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
         * resources/libraries. If assumeThisKeywordIsUndefined is not null, then it will not be considered as defined.
         * This is used to include the keyword already defined at the line where the cursor already is.
         */
        private static Map<String, List<KeywordNeed>> collectUndefinedKeywords(final IFile file, final ParsedString assumeThisKeywordIsUndefined) {
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
    }

    @Override
    public void addKeywordCallProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets) {
        // toLowerCase?
        acceptAttempts(argument, documentOffset, argument.getValue().toLowerCase(), proposalSets, new KeywordCallAttemptVisitor(file));
    }

    public static class KeywordCallAttemptVisitor implements AttemptVisitor {
        private final IFile file;

        public KeywordCallAttemptVisitor(IFile file) {
            this.file = file;
        }

        @Override
        public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
            final CompletionMatchVisitorProvider visitorProvider = new KeywordCompletionMatchVisitorProvider(file, replacementRegion);
            RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
            // first find matches that use the whole input as search string
            DefinitionFinder.acceptMatches(file, visitorProvider.get(attempt, ourProposalSet.getProposals()));
            return ourProposalSet;
        }
    }

    @Override
    public void addVariableProposals(IFile file, ParsedString argument, int documentOffset, List<RobotCompletionProposalSet> proposalSets, int maxVariableCharPos, int maxSettingCharPos) {
        IRegion replacementRegion = VariableReplacementRegionCalculator.calculate(argument, documentOffset);
        ParsedString subArgument = argument.extractRegion(replacementRegion);

        // toLowerCase?
        acceptAttempts(subArgument, documentOffset, subArgument.getValue().toLowerCase(), proposalSets, new VariableAttemptVisitor(file, maxVariableCharPos, maxSettingCharPos));
    }

    public static class VariableAttemptVisitor implements AttemptVisitor {
        private final IFile file;
        private final int maxVariableCharPos;
        private final int maxSettingCharPos;

        public VariableAttemptVisitor(IFile file, int maxVariableCharPos, int maxSettingCharPos) {
            this.file = file;
            this.maxVariableCharPos = maxVariableCharPos;
            this.maxSettingCharPos = maxSettingCharPos;
        }

        @Override
        public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
            final CompletionMatchVisitorProvider visitorProvider = new VariableCompletionMatchVisitorProvider(file, replacementRegion, maxVariableCharPos, maxSettingCharPos);
            RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
            // first find matches that use the whole input as search string
            DefinitionFinder.acceptMatches(file, visitorProvider.get(attempt, ourProposalSet.getProposals()));
            return ourProposalSet;

            // TODO
            // if (replacementRegion.getLength() > 0) {
            // // the cursor is positioned for replacing a variable, so put the variable proposals first
            // proposalSets.add(0, variableProposals);
            // } else {
            // // default positioning of proposals
            // proposalSets.add(variableProposals);
            // }
        }
    }

    public interface AttemptVisitor {
        RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion);
    }

    private void acceptAttempts(ParsedString argument, int documentOffset, String lookFor, List<RobotCompletionProposalSet> proposalSets, AttemptVisitor attemptVisitor) {
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        List<String> attempts = generateAttempts(argument, documentOffset, lookFor);
        for (String attempt : attempts) {
            RobotCompletionProposalSet ourProposalSet = attemptVisitor.visitAttempt(attempt, replacementRegion);
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

    private boolean proposalsIsEmptyOrContainsOnly(List<RobotCompletionProposal> proposals, ParsedString argument) {
        if (proposals.size() != 1) {
            return proposals.isEmpty();
        }
        return proposals.get(0).getMatchArgument().getValue().equals(argument.getValue());
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
}
