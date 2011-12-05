/**
 * Copyright 2011 Nitor Creations Oy
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    settingTypes.put("Test Template", SettingType.KEYWORD_ARGS);
    settingTypes.put("Test Timeout", SettingType.STRING);
  }

  private final ColorManager manager;
  private final TokenQueue tokenQueue = new TokenQueue();
  private final IToken tokTABLE;
  private final IToken tokSETTING_KEY;
  private final IToken tokSETTING_VAL;
  private final IToken tokSETTING_KEYWORD_CALL;
  private final IToken tokSETTING_KEYWORD_ARG;
  private final IToken tokSETTING_FILE;
  private final IToken tokSETTING_FILE_ARG;
  private final IToken tokVARIABLE_KEY;
  private final IToken tokVARIABLE_VAL;
  private final IToken tokCOMMENT;
  private final IToken tokNEW_KEYWORD;
  private final IToken tokKEYWORD_CALL;
  private final IToken tokKEYWORD_ARG;

  // private IDocument document;
  private Iterator<RFELine> lineIterator;
  private RFELine line;
  private int argOff;
  private int argLen;
  private boolean lineEndsWithComment;
  private RFEPreParser.Type lastRealType;

  // private RFELine lastParsedLine;
  private boolean keywordSequence_isSetting;
  private SettingType setting_type;
  private boolean setting_gotFirstArg;

  public RFTColoringScanner(ColorManager colorManager) {
    this.manager = colorManager;
    // TODO dynamically fetched colors
    tokTABLE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.TABLE)));
    tokCOMMENT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.COMMENT)));
    tokSETTING_KEY = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING)));
    tokSETTING_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING_VALUE)));
    tokSETTING_FILE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING_FILE)));
    tokSETTING_FILE_ARG = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING_FILE_ARG)));
    tokVARIABLE_KEY = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE)));
    tokVARIABLE_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE_VALUE)));
    tokNEW_KEYWORD = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD_NEW)));
    tokKEYWORD_CALL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
    tokKEYWORD_ARG = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD_ARG)));
    tokSETTING_KEYWORD_CALL = tokKEYWORD_CALL;
    tokSETTING_KEYWORD_ARG = tokKEYWORD_ARG;
    //IToken tokARGUMENT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT)));
    //IToken tokARGUMENT_SEPARATOR = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT_SEPARATOR), null, TextAttribute.UNDERLINE));
  }

  @Override
  public void setRange(IDocument document, int offset, int length) {
    try {
      // this.document = document;
      tokenQueue.reset();
      List<RFELine> lines = new RFELexer(document).lex();
      new RFEPreParser(document, lines).preParse();
      lineIterator = lines.iterator();
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
      case TESTCASE_TABLE_TESTCASE_LINE:
      case KEYWORD_TABLE_KEYWORD_BEGIN:
      case KEYWORD_TABLE_KEYWORD_LINE: {
        switch (argOff) {
          case 0: {
            ParsedString newName = line.arguments.get(0);
            if (!newName.getValue().isEmpty()) {
              tokenQueue.add(newName, tokNEW_KEYWORD);
            }
            prepareNextToken();
            return;
          }
          case 1: {
            ParsedString newName = line.arguments.get(1);
            keywordSequence_isSetting = newName.getValue().startsWith("[");
            if (keywordSequence_isSetting) {
              tokenQueue.add(newName, tokSETTING_KEY);
            } else {
              tokenQueue.add(newName, tokKEYWORD_CALL); // TODO template
            }
            prepareNextToken();
            return;
          }
          default: {
            parseKeywordSequenceArgs();
            return;
          }
        }
      }
      case CONTINUATION_LINE: {
        if (argOff == 0) {
          argOff = line.arguments.get(0).getValue().isEmpty() ? 2 : 1;
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
            parseKeywordSequenceArgs();
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

  private void parseSettingArgs() {
    if (!setting_gotFirstArg) {
      setting_gotFirstArg = true;
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
          ParsedString file = line.arguments.get(argOff);
          tokenQueue.add(file, tokSETTING_FILE);
          prepareNextToken();
          return;
        }
        case KEYWORD_ARGS: {
          ParsedString file = line.arguments.get(argOff);
          tokenQueue.add(file, tokSETTING_KEYWORD_CALL);
          prepareNextToken();
          return;
        }
      }
      throw new RuntimeException();
    } else {
      switch (setting_type) {
        case FILE_ARGS: {
          ParsedString first = line.arguments.get(argOff);
          ParsedString last = line.arguments.get(argLen - 1);
          tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokSETTING_FILE_ARG);
          prepareNextLine();
          return;
        }
        case KEYWORD_ARGS: {
          ParsedString first = line.arguments.get(argOff);
          ParsedString last = line.arguments.get(argLen - 1);
          tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokSETTING_KEYWORD_ARG);
          prepareNextLine();
          return;
        }
      }
      throw new RuntimeException();
    }
  }

  private void parseVariableArgs() {
    ParsedString first = line.arguments.get(argOff);
    ParsedString last = line.arguments.get(argLen - 1);
    tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokVARIABLE_VAL);
    prepareNextLine();
  }

  private void parseKeywordSequenceArgs() {
    ParsedString first = line.arguments.get(argOff);
    ParsedString last = line.arguments.get(argLen - 1);
    tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), keywordSequence_isSetting ? tokSETTING_VAL : tokKEYWORD_ARG);
    prepareNextLine();
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
