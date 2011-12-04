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
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFELexer;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFELexer.LexLine;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFEParser;
import org.otherone.robotframework.eclipse.editor.structure.api.IDynamicParsedString;
import org.otherone.robotframework.eclipse.editor.structure.api.IParsedString;
import org.otherone.robotframework.eclipse.editor.structure.api.IRFEFileContents;
import org.otherone.robotframework.eclipse.editor.structure.api.IVariableDefinition;

public class RFTColoringScanner implements ITokenScanner {

  private IRFEFileContents fileContents;
  private Iterator<Entry<IParsedString, IVariableDefinition>> fileContentsVariableIt;
  private Entry<IParsedString, IVariableDefinition> entry;
  boolean value = true;

  private final ColorManager manager;
  private final Token tokVARIABLE;
  private final Token tokVARIABLE_VAL;

  public RFTColoringScanner(ColorManager colorManager) {
    this.manager = colorManager;
    // TODO dynamically fetched colors
    tokVARIABLE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE)));
    tokVARIABLE_VAL = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.KEYWORD)));
  }

  @Override
  public void setRange(IDocument document, int offset, int length) {
    try {
      List<LexLine> lines = new RFELexer(document).lex();
      fileContents = new RFEParser(document, lines).parse();
      this.fileContentsVariableIt = fileContents.getVariables().entrySet().iterator();
    } catch (CoreException e) {
      throw new RuntimeException("Error parsing", e);
    }
  }

  @Override
  public IToken nextToken() {
    if (value) {
      if (!fileContentsVariableIt.hasNext()) {
        entry = null;
        return Token.EOF;
      }
      entry = fileContentsVariableIt.next();
      value = false;
    } else {
      value = true;
    }
    return value ? tokVARIABLE_VAL : tokVARIABLE;
  }

  @Override
  public int getTokenOffset() {
    int i = getTokenOffset2();
    System.out.println("getTokenOffset " + i);
    return i;
  }

  public int getTokenOffset2() {
    if (value) {
      return entry.getValue().getValues().get(0).getArgCharPos();
    } else {
      return entry.getKey().getArgCharPos();
    }
  }

  @Override
  public int getTokenLength() {
    int i = getTokenLength2();
    System.out.println("getTokenLength " + i);
    return i;
  }

  public int getTokenLength2() {
    if (value) {
      List<IDynamicParsedString> values = entry.getValue().getValues();
      IDynamicParsedString val0 = values.get(0);
      IDynamicParsedString valn = values.get(values.size() - 1);
      return valn.getArgEndCharPos() - val0.getArgCharPos();
    } else {
      IParsedString key = entry.getKey();
      return key.getArgEndCharPos() - key.getArgCharPos();
    }
  }

}
