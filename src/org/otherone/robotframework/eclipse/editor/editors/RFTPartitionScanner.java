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
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.graphics.RGB;

public class RFTPartitionScanner extends RuleBasedPartitionScanner {
  public final static String RFT_TABLE = "__RFT_TABLE";
  public final static String RFT_TABLE_SETTING = "__RFT_TABLE_S";
  public final static String RFT_TABLE_VARIABLE = "__RFT_TABLE_V";
  public final static String RFT_TABLE_TESTCASE = "__RFT_TABLE_T";
  public final static String RFT_TABLE_KEYWORD = "__RFT_TABLE_K";
  public final static String RFT_COMMENT = "__RFT_COMMENT";
  public final static String RFT_SETTING = "__RFT_SETTING";
  public final static String RFT_SETTING_VALUE = "__RFT_SETTING_VALUE";
  public final static String RFT_VARIABLE = "__RFT_VARIABLE";
  public final static String RFT_VARIABLE_VALUE = "__RFT_VARIABLE_VALUE";
  public final static String RFT_TESTCASE = "__RFT_TESTCASE";
  public final static String RFT_KEYWORD = "__RFT_KEYWORD";
  public final static String RFT_ACTION = "__RFT_ACTION";
  public final static String RFT_DEFAULT = "__RFT_DEFAULT";

  private final static String[] CONTENT_TYPES = {
    IDocument.DEFAULT_CONTENT_TYPE,
    RFT_TABLE,
    RFT_TABLE_SETTING,
    RFT_TABLE_VARIABLE,
    RFT_TABLE_TESTCASE,
    RFT_TABLE_KEYWORD,
    RFT_COMMENT,
    RFT_SETTING,
    RFT_SETTING_VALUE,
    RFT_VARIABLE,
    RFT_VARIABLE_VALUE,
    RFT_TESTCASE,
    RFT_KEYWORD,
    RFT_ACTION,
    RFT_DEFAULT,
  };

  public static String[] getContentTypes() {
    return CONTENT_TYPES.clone();
  }

  private final static String[] CONTENT_TYPES2 = {
    RFT_TABLE,
    RFT_TABLE_SETTING,
    RFT_TABLE_VARIABLE,
    RFT_TABLE_TESTCASE,
    RFT_TABLE_KEYWORD,
    RFT_COMMENT,
    RFT_SETTING,
    RFT_SETTING_VALUE,
    RFT_VARIABLE,
    RFT_VARIABLE_VALUE,
    RFT_TESTCASE,
    RFT_KEYWORD,
    RFT_ACTION,
    RFT_DEFAULT,
  };

  public static String[] getContentTypes2() {
    return CONTENT_TYPES2.clone();
  }

  public RFTPartitionScanner() {
    IToken tokTABLE = new Token(RFT_TABLE);
    IToken tokVARIABLE = new Token(RFT_VARIABLE);
    IPredicateRule[] rules = {
        new SingleLineRule("***","", tokTABLE, '\0', true) {{ setColumnConstraint(0); }},
        new SingleLineRule("${","}", tokVARIABLE, '\0', true),
//        new SingleLineRule("***Setting", "***", tokTABLE_SETTING),
//        new SingleLineRule("***Settings", "***", tokTABLE_SETTING),
//        new SingleLineRule("***Metadata", "***", tokTABLE_SETTING),
    };

    //rules[0] = new MultiLineRule("<!--", "-->", xmlComment);
    //rules[1] = new TagRule(tag);

setPredicateRules(rules);

    //  IToken tokTABLE_SETTING = new Token(RFT_TABLE_SETTING);
//    IToken tokTABLE_SETTING = new Token(RFT_TABLE_SETTING);
//    IToken tokTABLE_VARIABLE = new Token(RFT_TABLE_VARIABLE);
//    IToken tokTABLE_TESTCASE = new Token(RFT_TABLE_TESTCASE);
//    IToken tokTABLE_KEYWORD = new Token(RFT_TABLE_KEYWORD);
//    IToken tokCOMMENT = new Token(RFT_COMMENT);
//    IToken tokSETTING = new Token(RFT_SETTING);
//    IToken tokSETTING_VALUE = new Token(RFT_SETTING_VALUE);
//    IToken tokVARIABLE = new Token(RFT_VARIABLE);
//    IToken tokVARIABLE_VALUE = new Token(RFT_VARIABLE_VALUE);
//    IToken tokTESTCASE = new Token(RFT_TESTCASE);
//    IToken tokKEYWORD = new Token(RFT_KEYWORD);
//    IToken tokACTION = new Token(RFT_ACTION);
//    IToken tokDEFAULT = new Token(RFT_DEFAULT);
//    
//    IPredicateRule[] rules = {
//        new SingleLineRule("***Setting table", "", tokTABLE_SETTING),
//        new SingleLineRule("***Setting", "***", tokTABLE_SETTING),
//        new SingleLineRule("***Settings", "***", tokTABLE_SETTING),
//        new SingleLineRule("***Metadata", "***", tokTABLE_SETTING),
//    };
//
//    //rules[0] = new MultiLineRule("<!--", "-->", xmlComment);
//    //rules[1] = new TagRule(tag);
//
//    setPredicateRules(rules);
  }
}
