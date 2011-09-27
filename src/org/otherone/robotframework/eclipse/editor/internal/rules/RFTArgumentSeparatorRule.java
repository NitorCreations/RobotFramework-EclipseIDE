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

public class RFTArgumentSeparatorRule implements IRule {
  
  private final IToken token;

  public RFTArgumentSeparatorRule(IToken token) {
    this.token = token;
  }

  @Override
  public IToken evaluate(ICharacterScanner scanner) {
    int c = scanner.read();
    if (c == ICharacterScanner.EOF || !isWhitespace(c)) {
      scanner.unread();
      return Token.UNDEFINED;
    }
    if (c != '\t') {
      // need a second whitespace before we consider it an argument separator
      c = scanner.read();
      if (c == ICharacterScanner.EOF || !isWhitespace(c)) {
        scanner.unread();
        scanner.unread();
        return Token.UNDEFINED;
      }
    }
    do {
      c = scanner.read();
    } while (c != ICharacterScanner.EOF && isWhitespace(c));
    scanner.unread();
    return token;
  }

  private static boolean isWhitespace(int c) {
    return c == ' ' || c == '\t' || c == '\r' || c == '\n';
  }

}
