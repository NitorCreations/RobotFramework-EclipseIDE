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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

public class RFTColoringScanner implements ITokenScanner {

  private final ColorManager manager;
  private final TokenQueue tokenQueue = new TokenQueue();
  private final IToken tokTABLE;
  private final IToken tokSETTING_KEY;
  private final IToken tokSETTING_VAL;
  private final IToken tokVARIABLE_KEY;
  private final IToken tokVARIABLE_VAL;
  private final IToken tokCOMMENT;
  private final IToken tokNEW_KEYWORD;
  private final IToken tokKEYWORD_CALL;
  private final IToken tokKEYWORD_ARG;

  private IDocument document;
  private Iterator<RFELine> lineIterator;
  private RFELine line;
  private int argOff;
  private int argLen;
  private boolean lineEndsWithComment;
  private RFEPreParser.Type lastRealType;

  private RFELine lastParsedLine;

  public RFTColoringScanner(ColorManager colorManager) {
    this.manager = colorManager;
    // TODO dynamically fetched colors
    tokTABLE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.TABLE)));
    tokCOMMENT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.COMMENT)));
    tokSETTING_KEY = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING)));
    tokSETTING_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
    tokVARIABLE_KEY = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE)));
    tokVARIABLE_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
    tokNEW_KEYWORD = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
    tokKEYWORD_CALL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
    tokKEYWORD_ARG = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.DEFAULT)));
    //IToken tokARGUMENT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT)));
    //IToken tokARGUMENT_SEPARATOR = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT_SEPARATOR), null, TextAttribute.UNDERLINE));

  }

  @Override
  public void setRange(IDocument document, int offset, int length) {
    try {
      this.document = document;
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
      lastParsedLine = line;
      parseMoreTokens();
    }
    IToken t = tokenQueue.take();
    int tokenOff = getTokenOffset();
    int tokenLen = getTokenLength();
    System.out.print("TOK: " + (lastParsedLine != null ? "[" + lastParsedLine.lineNo + ":" + lastParsedLine.lineCharPos + "] " : "") + t + " off " + tokenOff
        + " end " + (tokenOff + tokenLen) + " len " + tokenLen);
    System.out.println(" txt \"" + document.get().substring(tokenOff, tokenOff + tokenLen).replace("\n", "\\n") + "\"");
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
            prepareNextToken();
            return;
          }
          default: {
            ParsedString first = line.arguments.get(1);
            ParsedString last = line.arguments.get(argLen - 1);
            tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokSETTING_VAL);
            prepareNextLine();
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
            ParsedString first = line.arguments.get(1);
            ParsedString last = line.arguments.get(argLen - 1);
            tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokVARIABLE_VAL);
            prepareNextLine();
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
            if (newName.getValue().startsWith("[")) {
              tokenQueue.add(newName, tokSETTING_KEY); // TODO remember
            } else {
              tokenQueue.add(newName, tokKEYWORD_CALL); // TODO template
            }
            prepareNextToken();
            return;
          }
          default: {
            ParsedString first = line.arguments.get(2);
            ParsedString last = line.arguments.get(argLen - 1);
            tokenQueue.add(first.getArgCharPos(), last.getArgEndCharPos(), tokKEYWORD_ARG);
            prepareNextLine();
            return;
          }
        }
      }
      case CONTINUATION_LINE: {
        switch (lastRealType) {
          case COMMENT_LINE:
          case CONTINUATION_LINE:
            throw new RuntimeException();
          case IGNORE:
          case TESTCASE_TABLE_IGNORE:
          case KEYWORD_TABLE_IGNORE: {
            prepareNextLine();
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
