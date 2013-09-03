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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.nitorcreations.robotframework.eclipseide.builder.parser.LineType;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.internal.util.LineFinder;
import com.nitorcreations.robotframework.eclipseide.internal.util.LineMatchVisitor;
import com.nitorcreations.robotframework.eclipseide.internal.util.VisitorInterest;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class KeywordDefinitionAttemptVisitor implements AttemptVisitor {
    final Map<String, List<KeywordNeed>> undefinedKeywords;

    public KeywordDefinitionAttemptVisitor(IFile file, ParsedString argument) {
        undefinedKeywords = collectUndefinedKeywords(file, argument);
    }

    private static final Set<LineType> KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES = new HashSet<LineType>();

    static {
        // all LineTypes that might have KEYWORD_CALL arguments or keyword definitions
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.TESTCASE_TABLE_BEGIN);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.TESTCASE_TABLE_TESTCASE_BEGIN);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.TESTCASE_TABLE_TESTCASE_LINE);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.KEYWORD_TABLE_BEGIN);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.KEYWORD_TABLE_KEYWORD_BEGIN);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.KEYWORD_TABLE_KEYWORD_LINE);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.CONTINUATION_LINE);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.SETTING_TABLE_BEGIN);
        KEYWORD_CALLS_AND_DEFINITIONS_LINETYPES.add(LineType.SETTING_TABLE_LINE);
    }

    @Override
    public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
        assert attempt.equals(attempt.toLowerCase());
        RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
        for (Entry<String, List<KeywordNeed>> e : undefinedKeywords.entrySet()) {
            String key = e.getKey();
            if (key.toLowerCase().startsWith(attempt)) {
                String proposal = key;
                Image image = null;
                String displayString = key;
                StringBuilder sb = new StringBuilder();
                sb.append("Called from the following locations:<ul>");
                for (KeywordNeed need : e.getValue()) {
                    if (need.callingTestcaseOrKeyword != null) {
                        ArgumentType type = need.callingTestcaseOrKeyword.getType();
                        String callerType = type == ArgumentType.NEW_TESTCASE ? "TEST CASE" : type == ArgumentType.NEW_KEYWORD ? "KEYWORD" : "UNKNOWN";
                        String callerName = need.callingTestcaseOrKeyword.getValue();
                        sb.append("<li><b>").append(callerType).append("</b> ").append(callerName);
                        if (need.callingSetting != null) {
                            String settingType = need.callingSetting.getType() == ArgumentType.SETTING_KEY ? "SETTING" : "UNKNOWN";
                            String settingName = need.callingSetting.getValue();
                            sb.append(", via <b>").append(settingType).append("</b> ").append(settingName);
                        }
                        sb.append("</li>");
                    } else {
                        String settingType = need.callingSetting.getType() == ArgumentType.SETTING_KEY ? "SETTING" : "UNKNOWN";
                        String settingName = need.callingSetting.getValue();
                        sb.append("<li><b>").append(settingType).append("</b> ").append(settingName).append("</li>");
                    }
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
        public final ParsedString callingSetting;
        public final ParsedString calledKeyword;

        KeywordNeed(ParsedString callingTestcaseOrKeyword, ParsedString callingSetting, ParsedString calledKeyword) {
            assert callingTestcaseOrKeyword != null || callingSetting != null;
            this.callingTestcaseOrKeyword = callingTestcaseOrKeyword;
            this.callingSetting = callingSetting;
            this.calledKeyword = calledKeyword;
        }
    }

    /**
     * Collect a list of keywords that are called in this file, but not defined in the file or in imported
     * resources/libraries. If assumeThisKeywordIsUndefined is not null, then it will not be considered as defined. This
     * is used to include the keyword already defined at the line where the cursor already is.
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
            private ParsedString lastDefinedSetting;

            private void visitKeywordCalls(RobotLine line) {
                if (line.type != LineType.CONTINUATION_LINE) {
                    lastDefinedSetting = null;
                }
                for (ParsedString argument : line.arguments) {
                    switch (argument.getType()) {
                        case TABLE:
                            lastDefinedTestcaseOrKeyword = null;
                            break;
                        case SETTING_KEY:
                            lastDefinedSetting = argument;
                            break;
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
                            list.add(new KeywordNeed(lastDefinedTestcaseOrKeyword, lastDefinedSetting, argument));
                            break;
                    }
                }
            }

            private void visitKeywordDefinition(RobotLine line, FileWithType lineLocation) {
                ParsedString definedKeyword = line.arguments.get(0);
                definedKeywords.add(definedKeyword.getValue());
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
        definedKeywords.remove(assumeThisKeywordIsUndefined.getValue());
        if (!definedKeywords.isEmpty()) {
            Pattern definedKeywordsRE = regexpify(definedKeywords);
            removeKeywordsMatchingRegexp(neededKeywords, definedKeywordsRE);
        }
        return neededKeywords;
    }

    static final Pattern INLINE_PARAMETER_RE = Pattern.compile("\\$\\{[^}]+\\}");

    private static Pattern regexpify(List<String> definedKeywords) {
        StringBuilder regexpSb = new StringBuilder(4000);
        for (String definedKeyword : definedKeywords) {
            if (regexpSb.length() > 0) {
                regexpSb.append('|');
            }
            regexpify(regexpSb, definedKeyword);
        }
        return Pattern.compile(regexpSb.toString());
    }

    /**
     * Converts keywords to regular expressions by changing parameters embedded in keyword to ".*" and quoting
     * everything else as necessary, appending the regular expression to the given target. Highly hypothetical example:
     * "Should replace ** with ${variable}" appends the regexp "Should replace \*\* with .*" (or an equivalent form) to
     * the target StringBuilder.
     */
    private static void regexpify(StringBuilder target, String definedKeyword) {
        Matcher parameterMatcher = INLINE_PARAMETER_RE.matcher(definedKeyword);
        int nextStart = 0;
        while (parameterMatcher.find()) {
            target.append(Pattern.quote(definedKeyword.substring(nextStart, parameterMatcher.start()))).append(".*");
            nextStart = parameterMatcher.end();
        }
        target.append(Pattern.quote(definedKeyword.substring(nextStart)));
    }

    private static void removeKeywordsMatchingRegexp(final Map<String, List<KeywordNeed>> keywords, Pattern regexp) {
        Iterator<String> it = keywords.keySet().iterator();
        while (it.hasNext()) {
            if (regexp.matcher(it.next()).matches()) {
                it.remove();
            }
        }
    }

}
