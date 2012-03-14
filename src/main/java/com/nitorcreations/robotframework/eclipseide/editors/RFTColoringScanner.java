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
package com.nitorcreations.robotframework.eclipseide.editors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.nitorcreations.robotframework.eclipseide.builder.parser.ArgumentPreParser;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELexer;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFELine;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFEPreParser;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

// TODO Variable references
public class RFTColoringScanner implements ITokenScanner {

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

    private final ColorManager manager;
    private final TokenQueue tokenQueue = new TokenQueue();
    private IToken tokTABLE;
    private IToken tokSETTING_KEY;
    private IToken tokSETTING_VAL;
    private IToken tokSETTING_FILE;
    private IToken tokSETTING_FILE_ARG;
    private IToken tokSETTING_FILE_WITH_NAME_KEY;
    private IToken tokSETTING_FILE_WITH_NAME_VALUE;
    private IToken tokVARIABLE_KEY; // TODO consider combining with
                                    // tokKEYWORD_LVALUE
    private IToken tokVARIABLE_VAL;
    private IToken tokCOMMENT;
    private IToken tokNEW_TESTCASE;
    private IToken tokNEW_KEYWORD;
    private IToken tokKEYWORD_LVALUE;
    private IToken tokKEYWORD_CALL;
    private IToken tokKEYWORD_ARG;
    private IToken tokFOR_PART;

    // private IDocument document;
    private List<RFELine> lines;
    private ListIterator<RFELine> lineIterator;
    private RFELine line;
    private int argOff;
    private int argLen;
    private boolean lineEndsWithComment;
    private RFEPreParser.Type lastRealType;

    // private RFELine lastParsedLine;

    private boolean keywordSequence_isSetting;
    private SettingType keywordSequence_settingType;
    private KeywordCallState keywordSequence_keywordCallState;

    private SettingType setting_type;
    private boolean setting_gotFirstArg;
    private WithNameState setting_withNameState;

    enum WithNameState {
        NONE, GOT_KEY, GOT_VALUE
    }

    public RFTColoringScanner(ColorManager colorManager) {
        this.manager = colorManager;
        // IToken tokARGUMENT = new Token(new
        // TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT)));
        // IToken tokARGUMENT_SEPARATOR = new Token(new
        // TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT_SEPARATOR),
        // null, TextAttribute.UNDERLINE));
    }

    private final Map<ArgumentType, IToken> argTypeToTokenMap = new HashMap<ArgumentType, IToken>();

    private void prepareTokens() {
        // TODO dynamically fetched colors
        tokTABLE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.TABLE)));
        tokCOMMENT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.COMMENT)));
        tokSETTING_KEY = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING)));
        tokSETTING_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING_VALUE)));
        tokSETTING_FILE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING_FILE)));
        tokSETTING_FILE_ARG = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING_FILE_ARG)));
        tokSETTING_FILE_WITH_NAME_KEY = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.DEFAULT)));
        tokSETTING_FILE_WITH_NAME_VALUE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING_FILE)));
        tokVARIABLE_KEY = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE)));
        tokVARIABLE_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE_VALUE)));
        tokNEW_TESTCASE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.TESTCASE_NEW)));
        tokNEW_KEYWORD = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD_NEW)));
        tokKEYWORD_LVALUE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD_LVALUE)));
        tokKEYWORD_CALL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
        tokKEYWORD_ARG = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD_ARG)));
        tokFOR_PART = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.FOR_PART)));
        argTypeToTokenMap.put(ArgumentType.COMMENT, tokCOMMENT);
        argTypeToTokenMap.put(ArgumentType.TABLE, tokTABLE);
        argTypeToTokenMap.put(ArgumentType.SETTING_KEY, tokSETTING_KEY);
        argTypeToTokenMap.put(ArgumentType.VARIABLE_KEY, tokVARIABLE_KEY);
        argTypeToTokenMap.put(ArgumentType.NEW_TESTCASE, tokNEW_TESTCASE);
        argTypeToTokenMap.put(ArgumentType.NEW_KEYWORD, tokNEW_KEYWORD);
        argTypeToTokenMap.put(ArgumentType.SETTING_VAL, tokSETTING_VAL);
        argTypeToTokenMap.put(ArgumentType.SETTING_FILE, tokSETTING_FILE);
        argTypeToTokenMap.put(ArgumentType.SETTING_FILE_WITH_NAME_KEY, tokSETTING_FILE_WITH_NAME_KEY);
        argTypeToTokenMap.put(ArgumentType.SETTING_FILE_ARG, tokSETTING_FILE_ARG);
        argTypeToTokenMap.put(ArgumentType.SETTING_FILE_WITH_NAME_VALUE, tokSETTING_FILE_WITH_NAME_VALUE);
        argTypeToTokenMap.put(ArgumentType.VARIABLE_VAL, tokVARIABLE_VAL);
        argTypeToTokenMap.put(ArgumentType.KEYWORD_LVALUE, tokKEYWORD_LVALUE);
        argTypeToTokenMap.put(ArgumentType.FOR_PART, tokFOR_PART);
        argTypeToTokenMap.put(ArgumentType.KEYWORD_CALL, tokKEYWORD_CALL);
        argTypeToTokenMap.put(ArgumentType.KEYWORD_ARG, tokKEYWORD_ARG);
    }

    @Override
    public void setRange(IDocument document, int offset, int length) {
        prepareTokens();
        try {
            // this.document = document;
            tokenQueue.reset();
            lines = new RFELexer(document).lex();
            new RFEPreParser(document, lines).preParse();
            ArgumentPreParser argumentPreParser = new ArgumentPreParser();
            argumentPreParser.setRange(lines);
            argumentPreParser.parseAll();
            lineIterator = lines.listIterator();
            lastRealType = RFEPreParser.Type.IGNORE;
            prepareNextLine();
            // fileContents = new RFEParser(document, lines).parse();
            // this.fileContentsVariableIt =
            // fileContents.getVariables().entrySet().iterator();
        } catch (CoreException e) {
            throw new RuntimeException("Error parsing", e);
        }
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
                tokenQueue.add(comment, tokCOMMENT);
            }
        }
        // next line
        if (lineIterator.hasNext()) {
            line = lineIterator.next();
            argLen = line.arguments.size();
            // lineEndsWithComment = line.arguments.get(argLen - 1).getType() ==
            // ArgumentType.COMMENT;
            // if (lineEndsWithComment) {
            // --argLen; // exclude now, deal with it later (see top of method)
            // }
        } else {
            lines = null;
            lineIterator = null;
            line = null;
            argLen = 0;
            lineEndsWithComment = false;
        }
        argOff = 0;
    }

    @Override
    public IToken nextToken() {
        while (!tokenQueue.hasPending()) {
            // lastParsedLine = line;
            parseMoreTokens();
        }
        IToken t = tokenQueue.take();
        // int tokenOff = getTokenOffset();
        // int tokenLen = getTokenLength();
        // System.out.print("TOK: " + (lastParsedLine != null ? "[" +
        // lastParsedLine.lineNo + ":" + lastParsedLine.lineCharPos + "] " : "")
        // + t + " off " + tokenOff
        // + " end " + (tokenOff + tokenLen) + " len " + tokenLen);
        // if (t instanceof Token) {
        // Token tt = (Token) t;
        // if (tt.getData() instanceof TextAttribute) {
        // TextAttribute ta = (TextAttribute) tt.getData();
        // System.out.print(" " + ta.getForeground());
        // }
        // }
        // System.out.println(" txt \"" + document.get().substring(tokenOff,
        // tokenOff + tokenLen).replace("\n", "\\n") + "\"");
        return t;
    }

    void parseMoreTokens() {
        if (line == null) {
            tokenQueue.addEof();
            return;
        }
        ParsedString arg = line.arguments.get(argOff);
        IToken token = argTypeToTokenMap.get(arg.getType());
        if (token != null) {
            tokenQueue.add(arg, token);
        }
        prepareNextToken();
    }

    static class TokenQueue {
        private static class PendingToken {
            final IToken token;
            final int len;

            public PendingToken(IToken token, int len) {
                assert token != null;
                this.token = token;
                this.len = len;
            }
        }

        private final List<PendingToken> pendingTokens = new LinkedList<PendingToken>();
        private int nextTokenStart = 0;
        private int curTokenOff, curTokenLen;

        public void reset() {
            nextTokenStart = 0;
            assert pendingTokens.isEmpty();
            pendingTokens.clear();
            curTokenOff = curTokenLen = 0;
        }

        public IToken take() {
            PendingToken removed = pendingTokens.remove(0);
            curTokenOff += curTokenLen;
            curTokenLen = removed.len;
            assert removed.token != null;
            return removed.token;
        }

        public void addEof() {
            addToken(0, Token.EOF);
        }

        public boolean hasPending() {
            return !pendingTokens.isEmpty();
        }

        public void add(ParsedString arg, IToken token) {
            add(arg.getArgCharPos(), arg.getArgEndCharPos(), token);
        }

        public void add(int off, int eoff, IToken token) {
            if (off > nextTokenStart) {
                addToken(off - nextTokenStart, Token.UNDEFINED);
            }
            addToken(eoff - off, token);
            nextTokenStart = eoff;
        }

        private void addToken(int len, IToken token) {
            pendingTokens.add(new PendingToken(token, len));
        }

        public int getLastTakenTokenOffset() {
            return curTokenOff;
        }

        public int getLastTakenTokenLength() {
            return curTokenLen;
        }

    }

    @Override
    public int getTokenOffset() {
        return tokenQueue.getLastTakenTokenOffset();
    }

    @Override
    public int getTokenLength() {
        return tokenQueue.getLastTakenTokenLength();
    }

}
