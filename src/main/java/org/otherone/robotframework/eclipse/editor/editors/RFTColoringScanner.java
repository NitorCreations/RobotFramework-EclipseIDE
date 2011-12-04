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
  private final Token tokVARIABLE;
  private final Token tokVARIABLE_VAL;

  private IDocument document;
  private Iterator<RFELine> lineIterator;
  private RFELine line;
  private int argOff;


  public RFTColoringScanner(ColorManager colorManager) {
    this.manager = colorManager;
    // TODO dynamically fetched colors
    tokVARIABLE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE)));
    tokVARIABLE_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
  }

  @Override
  public void setRange(IDocument document, int offset, int length) {
    try {
      this.document = document;
      tokenQueue.reset();
      List<RFELine> lines = new RFELexer(document).lex();
      new RFEPreParser(document, lines).preParse();
      lineIterator = lines.iterator();
      prepareNextLine();
      // fileContents = new RFEParser(document, lines).parse();
      // this.fileContentsVariableIt = fileContents.getVariables().entrySet().iterator();
    } catch (CoreException e) {
      throw new RuntimeException("Error parsing", e);
    }
  }

  void prepareNextToken() {
    ++argOff;
    if (argOff >= line.arguments.size()) {
      //prepareNextLine();
      prepareNextLine();
    }
  }

  void prepareNextLine() {
    if (lineIterator.hasNext()) {
      line = lineIterator.next();
    } else {
      line = null;
    }
    argOff = 0;
  }

  RFELine l;

  @Override
  public IToken nextToken() {
    if (!tokenQueue.hasPending()) {
      l = line;
      parseMoreTokens();
    }
    IToken t = tokenQueue.take();
    int tokenOff = getTokenOffset();
    int tokenLen = getTokenLength();
    System.out.println("TOK: " + (l != null ? "[" + l.lineNo + ":" + l.lineCharPos + "] " : "") + t + " off " + tokenOff + " end " + (tokenOff + tokenLen)
        + " len " + tokenLen + " txt \"" + document.get().substring(tokenOff, tokenOff + tokenLen) + "\"");
    return t;
  }

  void parseMoreTokens() {
    if (line == null) {
      tokenQueue.addEof();
      return;
    }
    switch ((RFEPreParser.Type) line.info.get(RFEPreParser.Type.class)) {
      case COMMENT_LINE: {
        argOff = line.arguments.size() - 1;
        ParsedString comment = line.arguments.get(argOff);
        tokenQueue.add(comment.getArgCharPos(), comment.getValue().length(), tokVARIABLE);
        prepareNextLine();
        return;
      }
      default: {
        ParsedString arg = line.arguments.get(argOff);
        tokenQueue.add(arg.getArgCharPos(), arg.getValue().length(), tokVARIABLE_VAL);
        prepareNextToken();
        return;
      }
    }
    //    if (value) {
    //      if (!fileContentsVariableIt.hasNext()) {
    //        entry = null;
    //        return Token.EOF;
    //      }
    //      entry = fileContentsVariableIt.next();
    //      value = false;
    //    } else {
    //      value = true;
    //    }
    //    return value ? tokVARIABLE_VAL : tokVARIABLE;
  }

  //  public int getTokenLength2() {
  //    if (value) {
  //      List<IDynamicParsedString> values = entry.getValue().getValues();
  //      IDynamicParsedString val0 = values.get(0);
  //      IDynamicPars7edString valn = values.get(values.size() - 1);
  //      return valn.getArgEndCharPos() - val0.getArgCharPos();
  //    } else {
  //      IParsedString key = entry.getKey();
  //      return key.getArgEndCharPos() - key.getArgCharPos();
  //    }
  //  }

  private final TokenQueue tokenQueue = new TokenQueue();

  static class TokenQueue {
    static class PendingToken {
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

    void reset() {
      nextTokenStart = 0;
      assert pendingTokens.isEmpty();
      pendingTokens.clear();
      curTokenOff = curTokenLen = 0;
    }

    IToken take() {
      PendingToken removed = pendingTokens.remove(0);
      curTokenOff += curTokenLen;
      curTokenLen = removed.len;
      return removed.token;
    }

    public void addEof() {
      addToken(0, Token.EOF);
    }

    boolean hasPending() {
      return !pendingTokens.isEmpty();
    }

    void add(int off, int len, IToken token) {
      if (off > nextTokenStart) {
        addToken(off - nextTokenStart, Token.UNDEFINED);
      }
      addToken(len, token);
      nextTokenStart = off + len;
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
