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
package org.otherone.robotframework.eclipse.editor.editors;

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
import org.otherone.robotframework.eclipse.editor.builder.parser.RFELexer;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFELine;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFEPreParser;
import org.otherone.robotframework.eclipse.editor.structure.ParsedString;

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
    settingTypes.put("Test Template", SettingType.KEYWORD_ARGS); // or just keyword ?
    settingTypes.put("Test Timeout", SettingType.STRING);
  }

  static final Map<String, SettingType> keywordSequenceSettingTypes = new HashMap<String, SettingType>();
  static {
    keywordSequenceSettingTypes.put("[Documentation]", SettingType.STRING);
    keywordSequenceSettingTypes.put("[Tags]", SettingType.STRING);
    keywordSequenceSettingTypes.put("[Setup]", SettingType.KEYWORD_ARGS);
    keywordSequenceSettingTypes.put("[Teardown]", SettingType.KEYWORD_ARGS);
    keywordSequenceSettingTypes.put("[Template]", SettingType.KEYWORD_ARGS); // or just keyword ?
    keywordSequenceSettingTypes.put("[Timeout]", SettingType.STRING);
    keywordSequenceSettingTypes.put("[Arguments]", SettingType.STRING);
    keywordSequenceSettingTypes.put("[Return]", SettingType.STRING);
  }

  private final ColorManager manager;
  private final TokenQueue tokenQueue = new TokenQueue();
  private final IToken tokTABLE;
  private final IToken tokSETTING_KEY;
  private final IToken tokSETTING_VAL;
  private final IToken tokSETTING_FILE;
  private final IToken tokSETTING_FILE_ARG;
  private final IToken tokSETTING_FILE_WITH_NAME_KEY;
  private final IToken tokSETTING_FILE_WITH_NAME_VALUE;
  private final IToken tokVARIABLE_KEY; // TODO consider combining with tokKEYWORD_LVALUE
  private final IToken tokVARIABLE_VAL;
  private final IToken tokCOMMENT;
  private final IToken tokNEW_TESTCASE;
  private final IToken tokNEW_KEYWORD;
  private final IToken tokKEYWORD_LVALUE;
  private final IToken tokKEYWORD_CALL;
  private final IToken tokKEYWORD_ARG;
  private final IToken tokFOR_PART;

  //  private IDocument document;
  private List<RFELine> lines;
  private ListIterator<RFELine> lineIterator;
  private RFELine line;
  private int argOff;
  private int argLen;
  private boolean lineEndsWithComment;
  private RFEPreParser.Type lastRealType;

  //  private RFELine lastParsedLine;

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
    //IToken tokARGUMENT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT)));
    //IToken tokARGUMENT_SEPARATOR = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT_SEPARATOR), null, TextAttribute.UNDERLINE));
  }

  @Override
  public void setRange(IDocument document, int offset, int length) {
    try {
      //      this.document = document;
      tokenQueue.reset();
      lines = new RFELexer(document).lex();
      new RFEPreParser(document, lines).preParse();
      lineIterator = lines.listIterator();
      lastRealType = RFEPreParser.Type.IGNORE;
      prepareNextLine();
      // fileContents = new RFEParser(document, lines).parse();
      // this.fileContentsVariableIt = fileContents.getVariables().entrySet().iterator();
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

  @Override
  public IToken nextToken() {
    while (!tokenQueue.hasPending()) {
      // lastParsedLine = line;
      parseMoreTokens();
    }
    IToken t = tokenQueue.take();
    //    int tokenOff = getTokenOffset();
    //    int tokenLen = getTokenLength();
    //    System.out.print("TOK: " + (lastParsedLine != null ? "[" + lastParsedLine.lineNo + ":" + lastParsedLine.lineCharPos + "] " : "") + t + " off " + tokenOff
    //        + " end " + (tokenOff + tokenLen) + " len " + tokenLen);
    //    if (t instanceof Token) {
    //      Token tt = (Token) t;
    //      if (tt.getData() instanceof TextAttribute) {
    //        TextAttribute ta = (TextAttribute) tt.getData();
    //        System.out.print(" " + ta.getForeground());
    //      }
    //    }
    //    System.out.println(" txt \"" + document.get().substring(tokenOff, tokenOff + tokenLen).replace("\n", "\\n") + "\"");
    return t;
  }

  void parseMoreTokens() {
    if (line == null) {
      tokenQueue.addEof();
      return;
    }
    RFEPreParser.Type type = (RFEPreParser.Type) line.info.get(RFEPreParser.Type.class);
    if (type != RFEPreParser.Type.COMMENT_LINE && type != RFEPreParser.Type.CONTINUATION_LINE) {
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
        tokenQueue.add(table, tokTABLE);
        prepareNextLine();
        return;
      }
      case SETTING_TABLE_LINE: {
        switch (argOff) {
          case 0: {
            ParsedString setting = line.arguments.get(0);
            tokenQueue.add(setting, tokSETTING_KEY);
            setting_type = settingTypes.get(setting.getValue());
            if (setting_type == null) {
              setting_type = SettingType.UNKNOWN;
            }
            setting_gotFirstArg = false;
            keywordSequence_keywordCallState = KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT; // TODO possibly should be KEYWORD_NOT_FOR_NOINDENT
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
            ParsedString setting = line.arguments.get(0);
            tokenQueue.add(setting, tokVARIABLE_KEY);
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
          ParsedString newName = line.arguments.get(0);
          if (!newName.isEmpty()) {
            tokenQueue.add(newName, type == RFEPreParser.Type.TESTCASE_TABLE_TESTCASE_BEGIN ? tokNEW_TESTCASE : tokNEW_KEYWORD);
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
              keywordSequence_keywordCallState = KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT; // TODO possibly should be KEYWORD_NOT_FOR_NOINDENT
              keywordSequence_settingType = keywordSequenceSettingTypes.get(keywordOrSetting.getValue());
              if (keywordSequence_settingType == null) {
                keywordSequence_settingType = SettingType.UNKNOWN;
              }
              tokenQueue.add(keywordOrSetting, tokSETTING_KEY);
              prepareNextToken();
            } else {
              keywordSequence_keywordCallState = KeywordCallState.UNDETERMINED;
              parseKeywordCall();
            }
            return;
          }
          default: {
            if (keywordSequence_isSetting) {
              parseKeywordSequenceSetting();
            } else {
              parseKeywordCall();
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
              parseKeywordCall();
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
        ParsedString first = line.arguments.get(argOff);
        ParsedString last = line.arguments.get(argLen - 1);
        tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokSETTING_VAL);
        prepareNextLine();
        return;
      }
      case FILE: {
        ParsedString file = line.arguments.get(argOff);
        tokenQueue.add(file, tokSETTING_FILE);
        prepareNextLine();
        return;
      }
      case FILE_ARGS: {
        if (!setting_gotFirstArg) {
          ParsedString file = line.arguments.get(argOff);
          tokenQueue.add(file, tokSETTING_FILE);
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
                tokenQueue.add(arg, tokSETTING_FILE_WITH_NAME_KEY);
              } else {
                tokenQueue.add(arg, tokSETTING_FILE_ARG);
              }
              prepareNextToken();
              return;
            case GOT_KEY:
              ParsedString name = line.arguments.get(argOff);
              tokenQueue.add(name, tokSETTING_FILE_WITH_NAME_VALUE);
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
        parseKeywordCall();
        return;
      }
    }
    throw new RuntimeException();
  }

  private void parseVariableArgs() {
    ParsedString first = line.arguments.get(argOff);
    ParsedString last = line.arguments.get(argLen - 1);
    tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokVARIABLE_VAL);
    prepareNextLine();
  }

  private void parseKeywordSequenceSetting() {
    switch (keywordSequence_settingType) {
      case UNKNOWN: {
        prepareNextLine();
        return;
      }
      case STRING: {
        ParsedString first = line.arguments.get(argOff);
        ParsedString last = line.arguments.get(argLen - 1);
        tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokSETTING_VAL);
        prepareNextLine();
        return;
      }
      case KEYWORD_ARGS: {
        parseKeywordCall();
        return;
      }
    }
    throw new RuntimeException();
  }

  /**
   * Before this is called the first time, keywordSequence_keywordCallState must be initialized to
   * either UNDETERMINED, UNDETERMINED_NOINDENT, KEYWORD_NOINDENT, KEYWORD_NOT_FOR_NOINDENT
   */
  private void parseKeywordCall() {
    if (keywordSequence_keywordCallState.isUndetermined()) {
      keywordSequence_keywordCallState = determineInitialKeywordCallState(keywordSequence_keywordCallState);
    }
    switch (keywordSequence_keywordCallState) {
      case LVALUE_NOINDENT:
      case LVALUE: {
        ParsedString variable = line.arguments.get(argOff);
        if (!variable.isEmpty() || keywordSequence_keywordCallState == KeywordCallState.LVALUE_NOINDENT) {
          tokenQueue.add(variable, tokKEYWORD_LVALUE);
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
            tokenQueue.add(keyword, tokFOR_PART);
            keywordSequence_keywordCallState = KeywordCallState.FOR_ARGS;
          } else {
            tokenQueue.add(keyword, tokKEYWORD_CALL);
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
          tokenQueue.add(arg, tokFOR_PART);
          keywordSequence_keywordCallState = KeywordCallState.ARGS;
          prepareNextToken();
          return;
        }
        tokenQueue.add(arg, tokKEYWORD_LVALUE);
        prepareNextToken();
        return;
      }
      case ARGS: {
        ParsedString first = line.arguments.get(argOff);
        ParsedString last = line.arguments.get(argLen - 1);
        tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokKEYWORD_ARG);
        prepareNextLine();
        return;
      }
    }
    throw new RuntimeException();
  }

  KeywordCallState determineInitialKeywordCallState(KeywordCallState initialKeywordCallState) {
    /* in this particular case, we need to do lookahead to see if we have
     * zero or more direct variable references, followed by a variable
     * reference suffixed with an equal sign. If this is the case, those
     * variables will be considered as lvalues and the following argument
     * as a keyword. 
     */
    // TODO if template then go directly to ARGS state

    KeywordCallState keywordCallState = scanLine(initialKeywordCallState, line, argOff);
    if (!keywordCallState.isUndetermined()) {
      return keywordCallState;
    }

    outer:
    for (int line = lineIterator.nextIndex(); line < lines.size(); ++line) {
      RFELine nextLine = lines.get(line);
      RFEPreParser.Type type = (RFEPreParser.Type) nextLine.info.get(RFEPreParser.Type.class);
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
          return initialKeywordCallState == KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT ? KeywordCallState.KEYWORD_NOT_FOR_NOINDENT
              : KeywordCallState.KEYWORD;
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
          return initialKeywordCallState == KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT ? KeywordCallState.KEYWORD_NOT_FOR_NOINDENT
              : KeywordCallState.KEYWORD;
      }
      if (arg.endsWith("=")) {
        return initialKeywordCallState == KeywordCallState.UNDETERMINED_NOT_FOR_NOINDENT ? KeywordCallState.LVALUE_NOINDENT : KeywordCallState.LVALUE;
      }
    }
    return initialKeywordCallState;
  }

  static class TokenQueue {
    private static class PendingToken {
      final IToken token;
      final int len;

      public PendingToken(IToken token, int len) {
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
