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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import java.util.Collections;
import java.util.List;

import com.nitorcreations.robotframework.eclipseide.builder.parser.state.State;
import com.nitorcreations.robotframework.eclipseide.builder.util.MarkerManager;
import com.nitorcreations.robotframework.eclipseide.structure.KeywordSequence;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.RobotFileContents;
import com.nitorcreations.robotframework.eclipseide.structure.api.IDynamicParsedString;

public class ParsedLineInfo {
    private final RobotParser parser;
    public final List<ParsedString> arguments;
    public final int lineNo;
    public final int lineCharPos;

    public ParsedLineInfo(RobotParser parser, List<ParsedString> arguments, int lineNo, int charPos) {
        this.parser = parser;
        this.arguments = Collections.unmodifiableList(arguments);
        this.lineNo = lineNo;
        this.lineCharPos = charPos;
    }

    public RobotFileContents fc() {
        return parser.fc;
    }

    public void setState(State state, KeywordSequence testcaseOrKeywordBeingParsed) {
        parser.setState(state, testcaseOrKeywordBeingParsed);
    }

    public void clearContinuationList() {
        parser.clearContinuationList();
    }

    public void setContinuationList(List<? extends IDynamicParsedString> listToContinue) {
        parser.setContinuationList(listToContinue);
    }

    public MarkerManager markerManager() {
        return parser.getMarkerManager();
    }

}