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

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;

public class RFTDefaultColoringScanner extends RuleBasedScanner {

  public RFTDefaultColoringScanner(ColorManager manager) {
    Token tokDEFAULT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.DEFAULT)));
    IToken tokVARIABLE = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.VARIABLE)));
    IToken tokSETTING = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.SETTING)));
    IToken tokACTION = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ACTION)));
    IToken tokARGUMENT = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT)));
    IToken tokARGUMENT_SEPARATOR = new Token(new TextAttribute(manager.getColor(IRFTColorConstants.ARGUMENT), null, TextAttribute.UNDERLINE));

    IRule[] rules = {
        new SingleLineRule("${", "}", tokVARIABLE, '\\', true),
        new SingleLineRule("[", "]", tokSETTING, '\\', true),
//        new SingleLineRule("  ", "", tokACTION, '\\', true) {{ setColumnConstraint(0); }},
//        new SingleLineRule("\t", "", tokACTION, '\\', true) {{ setColumnConstraint(0); }},
//        new SingleLineRule("  ", " ", tokARGUMENT, '\\', true),
//        new SingleLineRule("\t", "", tokARGUMENT, '\\', true),
        new RFTArgumentSeparatorRule(tokARGUMENT_SEPARATOR),
//        new WhitespaceRule(new RFTWhitespaceDetector()),
    };

    setRules(rules);
    setDefaultReturnToken(tokDEFAULT);
  }
}
