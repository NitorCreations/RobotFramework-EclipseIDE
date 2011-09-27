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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class RFTPartitionScanner extends RuleBasedPartitionScanner {
  public final static String RFT_TABLE = "__RFT_TABLE";
  public final static String RFT_COMMENT = "__RFT_COMMENT";
  public final static String RFT_VARIABLE = "__RFT_VARIABLE";
  public final static String RFT_KEYWORD = "__RFT_KEYWORD";
  public final static String RFT_ACTION = "__RFT_ACTION";

  private final static String[] CONTENT_TYPES = {
    IDocument.DEFAULT_CONTENT_TYPE,
    RFT_TABLE,
    RFT_COMMENT,
    RFT_VARIABLE,
    RFT_KEYWORD,
    RFT_ACTION,
  };

  public static String[] getContentTypes() {
    return CONTENT_TYPES.clone();
  }

  private final static String[] CONTENT_TYPES2 = {
    IDocument.DEFAULT_CONTENT_TYPE,
    RFT_TABLE,
    RFT_COMMENT,
    RFT_VARIABLE,
    RFT_KEYWORD,
    RFT_ACTION,
  };

  public static String[] getContentTypes2() {
    return CONTENT_TYPES2.clone();
  }

  public RFTPartitionScanner() {
    IToken tokTABLE = new Token(RFT_TABLE);
    IToken tokCOMMENT = new Token(RFT_COMMENT);
    IToken tokVARIABLE = new Token(RFT_VARIABLE);
    IToken tokKEYWORD = new Token(RFT_KEYWORD);
    IToken tokACTION = new Token(RFT_ACTION);
    IPredicateRule[] rules = {
        new SingleLineRule("*","", tokTABLE, '\\', true) {{ setColumnConstraint(0); }},
        new SingleLineRule(" *","", tokTABLE, '\\', true) {{ setColumnConstraint(0); }},
        new SingleLineRule("#","", tokCOMMENT, '\\', true),
        new SingleLineRule("$","", tokVARIABLE, '\\', true) {{ setColumnConstraint(0); }},
        
        new SingleLineRule("  ","", tokACTION, '\\', true) {{ setColumnConstraint(0); }},
        new SingleLineRule(" \r","", tokACTION, '\\', true) {{ setColumnConstraint(0); }},
        new SingleLineRule("\r ","", tokACTION, '\\', true) {{ setColumnConstraint(0); }},
        new SingleLineRule("\t","", tokACTION, '\\', true) {{ setColumnConstraint(0); }},

        new SingleLineRule(" ","", tokKEYWORD, '\\', true) {{ setColumnConstraint(0); }},
    };
    setPredicateRules(rules);
  }
}
