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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine.LineType;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class ArgumentPreParser {

    enum SettingType {
        UNKNOWN, STRING, FILE, FILE_ARGS, KEYWORD_ARGS,
    }

    enum KeywordCallState {
        UNDETERMINED, UNDETERMINED_NOT_FOR_NOINDENT, UNDETERMINED_GOTVARIABLE, LVALUE_NOINDENT, LVALUE, KEYWORD, KEYWORD_NOT_FOR_NOINDENT, FOR_ARGS, ARGS, ;
        public boolean isUndetermined() {
            return name().startsWith("UNDETERMINED");
        }
    }

    static final Map<String, SettingType> settingTypes = new HashMap<String, SettingType>();
    static {
        settingTypes.put("Resource", SettingType.FILE_ARGS);
        settingTypes.put("Variables", SettingType.FILE);
        settingTypes.put("Library", SettingType.FILE_ARGS);
        settingTypes.put("Suite Setup", SettingType.KEYWORD_ARGS);
        settingTypes.put("Suite Teardown", SettingType.KEYWORD_ARGS);
        settingTypes.put("Documentation", SettingType.STRING);
        settingTypes.put("Metadata", SettingType.STRING);
        settingTypes.put("Force Tags", SettingType.STRING);
        settingTypes.put("Default Tags", SettingType.STRING);
        settingTypes.put("Test Setup", SettingType.KEYWORD_ARGS);
        settingTypes.put("Test Teardown", SettingType.KEYWORD_ARGS);
        settingTypes.put("Test Template", SettingType.KEYWORD_ARGS); // or just
                                                                     // keyword
                                                                     // ?
        settingTypes.put("Test Timeout", SettingType.STRING);
    }

    static final Map<String, SettingType> keywordSequenceSettingTypes = new HashMap<String, SettingType>();
    static {
        keywordSequenceSettingTypes.put("[Documentation]", SettingType.STRING);
        keywordSequenceSettingTypes.put("[Tags]", SettingType.STRING);
        keywordSequenceSettingTypes.put("[Setup]", SettingType.KEYWORD_ARGS);
        keywordSequenceSettingTypes.put("[Teardown]", SettingType.KEYWORD_ARGS);
        keywordSequenceSettingTypes.put("[Template]", SettingType.KEYWORD_ARGS); // or
                                                                                 // just
                                                                                 // keyword
                                                                                 // ?
        keywordSequenceSettingTypes.put("[Timeout]", SettingType.STRING);
        keywordSequenceSettingTypes.put("[Arguments]", SettingType.STRING);
        keywordSequenceSettingTypes.put("[Return]", SettingType.STRING);
    }

    private List<RFELine> lines;
    private ListIterator<RFELine> lineIterator;
    private RFELine line;
    private int argOff;
    private int argLen;
    private boolean lineEndsWithComment;
    private LineType lastRealType;

    private boolean keywordSequence_isSetting;
    private SettingType keywordSequence_settingType;
    private KeywordCallState keywordSequence_keywordCallState;

    private SettingType setting_type;
    private boolean setting_gotFirstArg;
    private WithNameState setting_withNameState;

    private static final int NO_TEMPLATE = -1;
    private int globalTemplateAtLine;
    private int localTemplateAtLine;

    enum WithNameState {
        NONE, GOT_KEY, GOT_VALUE
    }

    public ArgumentPreParser() {}

    public void setRange(List<RFELine> lines) {
        this.lines = lines;
        lineIterator = lines.listIterator();
        lastRealType = LineType.IGNORE;
        globalTemplateAtLine = NO_TEMPLATE;
        prepareNextLine();
    }

    void prepareNextToken() {
        assert argOff >= 0;
        assert argOff < argLen;
        if (++argOff == argLen) {
            prepareNextLine();
        }
    }

    void prepareNextLine() {
        assert argOff >= 0;
        assert argOff <= argLen;
        // if previous line ended with comment, add it to queue now
        if (lineEndsWithComment) {
            ParsedString comment = line.arguments.get(argLen);
            if (comment.getValue().startsWith("#")) {
                comment.setType(ArgumentType.COMMENT);
            }
        }
        // next line
        if (lineIterator.hasNext()) {
            line = lineIterator.next();
            argLen = line.arguments.size();
            lineEndsWithComment = line.arguments.get(argLen - 1).getValue().startsWith("#");
            if (lineEndsWithComment) {
                --argLen; // exclude now, deal with it later (see top of method)
            }
        } else {
            lines = null;
            lineIterator = null;
            line = null;
            argLen = 0;
            lineEndsWithComment = false;
        }
        argOff = 0;
    }

    public void parseAll() {
        lookForGlobalTestTemplate();
        while (lineIterator != null) {
            parseMoreTokens();
        }
    }

    void parseMoreTokens() {
        if (line == null) {
            return;
        }
        LineType type = line.type;
        if (type != LineType.COMMENT_LINE && type != LineType.CONTINUATION_LINE) {
            lastRealType = type;
        }
        switch (type) {
        case IGNORE_TABLE:
        case SETTING_TABLE_BEGIN:
        case VARIABLE_TABLE_BEGIN:
        case TESTCASE_TABLE_BEGIN:
        case KEYWORD_TABLE_BEGIN: {
            assert argOff == 0;
            ParsedString table = line.arguments.get(0);
            table.setType(ArgumentType.TABLE);
            prepareNextLine();
            return;
        }
        case SETTING_TABLE_LINE: {
            switch (argOff) {
            case 0: {
                ParsedString setting = line.arguments.get(0);
                setting.setType(ArgumentType.SETTING_KEY);
                setting_type = settingTypes.get(setting.getValue());
                if (setting_type == null) {
                    setting_type = SettingType.UNKNOWN;
                }
                setting_gotFirstArg = false;
                keywordSequence_keywordCallState = KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT; // TODO
                                                                                                   // possibly
                                                                                                   // should
                                                                                                   // be
                                                                                                   // KEYWORD_NOT_FOR_NOINDENT
                prepareNextToken();
                return;
            }
            default: {
                parseSettingArgs();
                return;
            }
            }
        }
        case VARIABLE_TABLE_LINE: {
            switch (argOff) {
            case 0:
                ParsedString variable = line.arguments.get(0);
                variable.setType(ArgumentType.VARIABLE_KEY);
                prepareNextToken();
                return;
            default:
                parseVariableArgs();
                return;
            }
        }
        case COMMENT_LINE: // prepareNextLine handles the comments
        case IGNORE:
        case TESTCASE_TABLE_IGNORE:
        case KEYWORD_TABLE_IGNORE: {
            prepareNextLine();
            return;
        }
        case TESTCASE_TABLE_TESTCASE_BEGIN:
        case KEYWORD_TABLE_KEYWORD_BEGIN:
            if (argOff == 0) {
                lookForLocalTestTemplate();
                ParsedString newName = line.arguments.get(0);
                if (!newName.isEmpty()) {
                    boolean isTestCase = type == LineType.TESTCASE_TABLE_TESTCASE_BEGIN;
                    newName.setType(isTestCase ? ArgumentType.NEW_TESTCASE : ArgumentType.NEW_KEYWORD);
                }
                prepareNextToken();
                return;
            }

            // FALL THROUGH

        case TESTCASE_TABLE_TESTCASE_LINE:
        case KEYWORD_TABLE_KEYWORD_LINE: {
            switch (argOff) {
            case 0: {
                prepareNextToken();
                return;
            }
            case 1: {
                ParsedString keywordOrSetting = line.arguments.get(1);
                keywordSequence_isSetting = keywordOrSetting.getValue().startsWith("[");
                if (keywordSequence_isSetting) {
                    keywordSequence_keywordCallState = KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT; // TODO
                                                                                                       // possibly
                                                                                                       // should
                                                                                                       // be
                                                                                                       // KEYWORD_NOT_FOR_NOINDENT
                    keywordSequence_settingType = keywordSequenceSettingTypes.get(keywordOrSetting.getValue());
                    if (keywordSequence_settingType == null) {
                        keywordSequence_settingType = SettingType.UNKNOWN;
                    }
                    keywordOrSetting.setType(ArgumentType.SETTING_KEY);
                    prepareNextToken();
                } else {
                    keywordSequence_keywordCallState = KeywordCallState.UNDETERMINED;
                    parseKeywordCall(lastRealType == LineType.TESTCASE_TABLE_TESTCASE_BEGIN || lastRealType == LineType.TESTCASE_TABLE_TESTCASE_LINE);
                }
                return;
            }
            default: {
                if (keywordSequence_isSetting) {
                    parseKeywordSequenceSetting();
                } else {
                    parseKeywordCall(lastRealType == LineType.TESTCASE_TABLE_TESTCASE_BEGIN || lastRealType == LineType.TESTCASE_TABLE_TESTCASE_LINE);
                }
                return;
            }
            }
        }
        case CONTINUATION_LINE: {
            if (argOff == 0) {
                argOff = determineContinuationLineArgOff(line);
                if (argOff >= argLen) {
                    prepareNextLine();
                    return;
                }
            }
            switch (lastRealType) {
            case COMMENT_LINE:
            case CONTINUATION_LINE:
                throw new RuntimeException();
            case IGNORE:
            case TESTCASE_TABLE_IGNORE:
            case KEYWORD_TABLE_IGNORE: {
                // continue ignoring
                prepareNextLine();
                return;
            }
            case IGNORE_TABLE:
            case SETTING_TABLE_BEGIN:
            case VARIABLE_TABLE_BEGIN:
            case TESTCASE_TABLE_BEGIN:
            case KEYWORD_TABLE_BEGIN: {
                // all arguments ignored
                prepareNextLine();
                return;
            }
            case SETTING_TABLE_LINE: {
                parseSettingArgs();
                return;
            }
            case VARIABLE_TABLE_LINE: {
                parseVariableArgs();
                return;
            }
            case TESTCASE_TABLE_TESTCASE_BEGIN:
            case TESTCASE_TABLE_TESTCASE_LINE:
            case KEYWORD_TABLE_KEYWORD_BEGIN:
            case KEYWORD_TABLE_KEYWORD_LINE: {
                if (keywordSequence_isSetting) {
                    parseKeywordSequenceSetting();
                } else {
                    parseKeywordCall(lastRealType == LineType.TESTCASE_TABLE_TESTCASE_BEGIN || lastRealType == LineType.TESTCASE_TABLE_TESTCASE_LINE);
                }
                return;
            }
            default: {
                prepareNextLine();
                return;
            }
            }
        }
        }
    }

    int determineContinuationLineArgOff(RFELine theLine) {
        return theLine.arguments.get(0).getValue().equals(RFEPreParser.CONTINUATION_STR) ? 1 : 2;
    }

    private void parseSettingArgs() {
        switch (setting_type) {
        case UNKNOWN: {
            prepareNextLine();
            return;
        }
        case STRING: {
            setArgTypesToEol(ArgumentType.SETTING_VAL);
            prepareNextLine();
            return;
        }
        case FILE: {
            ParsedString file = line.arguments.get(argOff);
            file.setType(ArgumentType.SETTING_FILE);
            prepareNextLine();
            return;
        }
        case FILE_ARGS: {
            if (!setting_gotFirstArg) {
                ParsedString file = line.arguments.get(argOff);
                file.setType(ArgumentType.SETTING_FILE);
                prepareNextToken();
                setting_gotFirstArg = true;
                setting_withNameState = WithNameState.NONE;
                return;
            } else {
                switch (setting_withNameState) {
                case NONE:
                    ParsedString arg = line.arguments.get(argOff);
                    if (arg.getValue().equals("WITH NAME")) {
                        setting_withNameState = WithNameState.GOT_KEY;
                        arg.setType(ArgumentType.SETTING_FILE_WITH_NAME_KEY);
                    } else {
                        arg.setType(ArgumentType.SETTING_FILE_ARG);
                    }
                    prepareNextToken();
                    return;
                case GOT_KEY:
                    ParsedString name = line.arguments.get(argOff);
                    name.setType(ArgumentType.SETTING_FILE_WITH_NAME_VALUE);
                    setting_withNameState = WithNameState.GOT_VALUE;
                    prepareNextLine();
                    return;
                case GOT_VALUE:
                    prepareNextLine();
                    return;
                }
            }
            throw new RuntimeException();
        }
        case KEYWORD_ARGS: {
            parseKeywordCall(false);
            return;
        }
        }
        throw new RuntimeException();
    }

    private void parseVariableArgs() {
        setArgTypesToEol(ArgumentType.VARIABLE_VAL);
        prepareNextLine();
    }

    private void parseKeywordSequenceSetting() {
        switch (keywordSequence_settingType) {
        case UNKNOWN: {
            prepareNextLine();
            return;
        }
        case STRING: {
            setArgTypesToEol(ArgumentType.SETTING_VAL);
            prepareNextLine();
            return;
        }
        case KEYWORD_ARGS: {
            parseKeywordCall(false);
            return;
        }
        }
        throw new RuntimeException();
    }

    /**
     * Before this is called the first time, keywordSequence_keywordCallState
     * must be initialized to either UNDETERMINED, UNDETERMINED_NOINDENT,
     * KEYWORD_NOINDENT, KEYWORD_NOT_FOR_NOINDENT
     * @param templatesEnabled whether the template flags {@link #globalTemplateAtLine} and {@link #localTemplateAtLine} affect keyword calls during this invocation
     */
    private void parseKeywordCall(boolean templatesEnabled) {
        if (keywordSequence_keywordCallState.isUndetermined()) {
            keywordSequence_keywordCallState = determineInitialKeywordCallState(keywordSequence_keywordCallState);
        }
        switch (keywordSequence_keywordCallState) {
        case LVALUE_NOINDENT:
        case LVALUE: {
            ParsedString variable = line.arguments.get(argOff);
            if (!variable.isEmpty() || keywordSequence_keywordCallState == KeywordCallState.LVALUE_NOINDENT) {
                variable.setType(ArgumentType.KEYWORD_LVALUE);
                if (variable.getValue().endsWith("=")) {
                    keywordSequence_keywordCallState = KeywordCallState.KEYWORD_NOT_FOR_NOINDENT;
                }
            }
            prepareNextToken();
            return;
        }
        case KEYWORD_NOT_FOR_NOINDENT:
        case KEYWORD: {
            ParsedString keyword = line.arguments.get(argOff);
            if (!keyword.isEmpty() || keywordSequence_keywordCallState == KeywordCallState.KEYWORD_NOT_FOR_NOINDENT) {
                if (keyword.getValue().equals(":FOR") && keywordSequence_keywordCallState != KeywordCallState.KEYWORD_NOT_FOR_NOINDENT) {
                    keyword.setType(ArgumentType.FOR_PART);
                    keywordSequence_keywordCallState = KeywordCallState.FOR_ARGS;
                } else {
                    if (templatesEnabled && (globalTemplateAtLine != NO_TEMPLATE || localTemplateAtLine != NO_TEMPLATE)) {
                        keyword.setType(ArgumentType.KEYWORD_ARG);
                    } else {
                        keyword.setType(ArgumentType.KEYWORD_CALL);
                    }
                    keywordSequence_keywordCallState = KeywordCallState.ARGS;
                }
            }
            prepareNextToken();
            return;
        }
        case FOR_ARGS: {
            ParsedString arg = line.arguments.get(argOff);
            String argVal = arg.getValue();
            if (argVal.equals("IN") || argVal.equals("IN RANGE")) {
                arg.setType(ArgumentType.FOR_PART);
                keywordSequence_keywordCallState = KeywordCallState.ARGS;
                prepareNextToken();
                return;
            }
            arg.setType(ArgumentType.KEYWORD_LVALUE);
            prepareNextToken();
            return;
        }
        case ARGS: {
            setArgTypesToEol(ArgumentType.KEYWORD_ARG);
            prepareNextLine();
            return;
        }
        }
        throw new RuntimeException();
    }

    KeywordCallState determineInitialKeywordCallState(KeywordCallState initialKeywordCallState) {
        /*
         * in this particular case, we need to do lookahead to see if we have
         * zero or more direct variable references, followed by a variable
         * reference suffixed with an equal sign. If this is the case, those
         * variables will be considered as lvalues and the following argument as
         * a keyword.
         */
        // TODO if template then go directly to ARGS state

        KeywordCallState keywordCallState = scanLine(initialKeywordCallState, line, argOff);
        if (!keywordCallState.isUndetermined()) {
            return keywordCallState;
        }

        outer: for (int line = lineIterator.nextIndex(); line < lines.size(); ++line) {
            RFELine nextLine = lines.get(line);
            LineType type = nextLine.type;
            switch (type) {
            case COMMENT_LINE:
                continue;
            case CONTINUATION_LINE: {
                int nextLineArgOff = determineContinuationLineArgOff(nextLine);
                keywordCallState = scanLine(keywordCallState, nextLine, nextLineArgOff);
                if (!keywordCallState.isUndetermined()) {
                    return keywordCallState;
                }
                break;
            }
            default:
                break outer;
            }
        }
        // no equal sign found so..
        return initialKeywordCallState == KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT ? KeywordCallState.KEYWORD_NOT_FOR_NOINDENT : KeywordCallState.KEYWORD;
    }

    private KeywordCallState scanLine(KeywordCallState initialKeywordCallState, RFELine scanLine, int scanOff) {
        assert initialKeywordCallState.isUndetermined();
        for (; scanOff < scanLine.arguments.size(); ++scanOff) {
            ParsedString parsedString = scanLine.arguments.get(scanOff);
            if (parsedString.isEmpty()) {
                if (initialKeywordCallState == KeywordCallState.UNDETERMINED) {
                    // no variables yet
                    continue;
                } else {
                    // no equal sign found before first non-variable parameter
                    return initialKeywordCallState == KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT ? KeywordCallState.KEYWORD_NOT_FOR_NOINDENT : KeywordCallState.KEYWORD;
                }
            }
            String arg = parsedString.getValue();
            switch (arg.charAt(0)) {
            case '$':
            case '@':
                // TODO ensure it's a proper lvalue
                initialKeywordCallState = KeywordCallState.UNDETERMINED_GOTVARIABLE;
                break;
            default:
                // non-variable and no prior lvalue indication, so..
                return initialKeywordCallState == KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT ? KeywordCallState.KEYWORD_NOT_FOR_NOINDENT : KeywordCallState.KEYWORD;
            }
            if (arg.endsWith("=")) {
                return initialKeywordCallState == KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT ? KeywordCallState.LVALUE_NOINDENT : KeywordCallState.LVALUE;
            }
        }
        return initialKeywordCallState;
    }

    private void setArgTypesToEol(ArgumentType settingVal) {
        for (int i = argOff; i < argLen; ++i) {
            line.arguments.get(i).setType(settingVal);
        }
    }

    private void lookForGlobalTestTemplate() {
        for (RFELine line : lines) {
            if (line.isType(LineType.SETTING_TABLE_LINE)) {
                if (line.arguments.get(0).equals("Test Template")) {
                    globalTemplateAtLine = line.lineNo;
                    // continue searching; last hit remains in effect
                }
            }
        }
    }

    private void lookForLocalTestTemplate() {
        localTemplateAtLine = NO_TEMPLATE;
        outer: for (int lineNo = lineIterator.nextIndex() - 1; lineNo < lines.size(); ++lineNo) {
            RFELine line = lines.get(lineNo);
            assert line.lineNo - 1 == lineNo;
            switch (line.type) {
            case TESTCASE_TABLE_TESTCASE_BEGIN:
            case TESTCASE_TABLE_TESTCASE_LINE:
                break;
            case CONTINUATION_LINE:
            case COMMENT_LINE:
                continue;
            default:
                // testcase ended, do not look further
                break outer;
            }
            if (line.arguments.size() >= 2 && line.arguments.get(1).equals("[Template]")) {
                localTemplateAtLine = line.lineNo;
                // continue searching; last hit remains in effect
            }
        }
    }

}
