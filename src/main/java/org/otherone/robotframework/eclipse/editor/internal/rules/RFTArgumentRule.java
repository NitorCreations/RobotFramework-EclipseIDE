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
package org.otherone.robotframework.eclipse.editor.internal.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class RFTArgumentRule implements IRule {
  
  private final IToken token;
  private final boolean isKeyword;

  /**
   * Scan test case names, keywords, arguments etc, separated by tabs or
   * multiple whitespace.
   * 
   * @param token
   * @param isKeyword whether we are scanning for a keyword. If yes, then we only match in the first column, and variables are considered a part of the keyword
   */
  public RFTArgumentRule(IToken token, boolean isKeyword) {
    this.token = token;
    this.isKeyword = isKeyword;
  }

  @Override
  public IToken evaluate(ICharacterScanner scanner) {
    StringBuilder sb = new StringBuilder();
    if (isKeyword && scanner.getColumn() > 1) {
      return Token.UNDEFINED;
    }
    int c = scanner.read();
    if (c == ICharacterScanner.EOF || isWhitespace(c) || c == '#' || (!isKeyword && c == '$')) {
      scanner.unread();
      return Token.UNDEFINED;
    }
    while(true) {
      if (isWhitespace(c)) {
        int c2 = scanner.read();
        if (isWhitespace(c2)) {
          scanner.unread();
          scanner.unread();
          return token;
        }
        sb.append(c);
        c = c2;
      }
      if (c == '#' || (!isKeyword && c == '$')) {
        scanner.unread();
        return token;
      }
      if (c == '\\') {
        c = scanner.read();
        if (c == ICharacterScanner.EOF) {
          scanner.unread();
          return token;
        }
        switch (c) {
        case 'n': sb.append('\n'); break;
        case 'r': sb.append('\r'); break;
        case 't': sb.append('\t'); break;
        default: sb.append(c); break;
        }
      } else {
        sb.append(c);
      }
      c = scanner.read();
      if (c == ICharacterScanner.EOF || c == '\t') {
        scanner.unread();
        return token;
      }
    }
  }

  private static boolean isWhitespace(int c) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n';
  }

}
