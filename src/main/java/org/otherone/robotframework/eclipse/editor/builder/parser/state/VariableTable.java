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
package org.otherone.robotframework.eclipse.editor.builder.parser.state;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFEParser.ParsedLineInfo;
import org.otherone.robotframework.eclipse.editor.structure.DynamicParsedString;
import org.otherone.robotframework.eclipse.editor.structure.ParsedString;
import org.otherone.robotframework.eclipse.editor.structure.VariableDefinition;

public class VariableTable extends State {

  public static final State STATE = new VariableTable();

  @Override
  public void parse(ParsedLineInfo info) throws CoreException {
    if (tryParseTableSwitch(info)) {
      return;
    }
    if (!tryParseVariable(info, 0)) {
      return;
    }
    ParsedString varArg = info.arguments.get(0);
    // TODO tryParseArgument(info, 1, "variable content");
    List<DynamicParsedString> values = splitRegularArguments(info, 1, 0);
    VariableDefinition varDef = new VariableDefinition();
    varDef.setVariable(varArg);
    varDef.setValues(values);
    info.fc().addVariable(varDef);
    info.setContinuationList(values);
  }

}
