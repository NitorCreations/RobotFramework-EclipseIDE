/**
 * Copyright 2012 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.builder.parser.state;

import org.eclipse.core.runtime.CoreException;

import com.nitorcreations.robotframework.eclipseide.builder.parser.SeverityConfig;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RFEParser.ParsedLineInfo;
import com.nitorcreations.robotframework.eclipseide.structure.TestCaseDefinition;

public class TestcaseTableInitial extends State {

    public static final State STATE = new TestcaseTableInitial();

    @Override
    public void parse(ParsedLineInfo info) throws CoreException {
        if (tryParseTableSwitch(info)) {
            return;
        }
        if (info.arguments.get(0).getValue().isEmpty()) {
            warnIgnoredLine(info, SeverityConfig.IGNORED_LINE_OUTSIDE_RECOGNIZED_TESTCASE_OR_KEYWORD);
            return;
        }
        if (!tryParseArgument(info, 0, "test case name")) {
            // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
            return;
        }
        // start new testcase
        TestCaseDefinition tc = new TestCaseDefinition(info.fc());
        tc.setSequenceName(info.arguments.get(0));
        info.fc().addTestCase(tc);
        info.setState(TestcaseTableActive.STATE, tc);
        if (info.arguments.size() == 1) {
            return;
        }
        parseTestcaseLine(info);
    }

}
