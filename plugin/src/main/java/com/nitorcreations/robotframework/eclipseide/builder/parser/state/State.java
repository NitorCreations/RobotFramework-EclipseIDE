/**
 * Copyright 2012-2013 Nitor Creations Oy
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import com.nitorcreations.robotframework.eclipseide.builder.RobotBuilder;
import com.nitorcreations.robotframework.eclipseide.builder.parser.ParsedLineInfo;
import com.nitorcreations.robotframework.eclipseide.builder.parser.SeverityConfig;
import com.nitorcreations.robotframework.eclipseide.builder.parser.util.ParserUtil;
import com.nitorcreations.robotframework.eclipseide.structure.DynamicParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.KeywordCall;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public abstract class State {

    public static final int SEVERITY_IGNORE = -500;

    public abstract void parse(ParsedLineInfo info) throws CoreException;

    protected void parseTestcaseLine(ParsedLineInfo info) throws CoreException {
        // TODO
        parseTestcaseOrUserKeywordLine(info);
    }

    protected void parseUserKeywordLine(ParsedLineInfo info) throws CoreException {
        // TODO
        parseTestcaseOrUserKeywordLine(info);
    }

    private void parseTestcaseOrUserKeywordLine(ParsedLineInfo info) throws CoreException {
        for (int i = 1; i < info.arguments.size(); ++i) {
            tryParseArgument(info, i, i == 1 ? "keyword name" : "keyword argument " + (i - 1));
        }
        // TODO Auto-generated method stub

    }

    List<DynamicParsedString> splitRegularArguments(ParsedLineInfo info, int startPos, int endSkip) {
        List<DynamicParsedString> arguments = new ArrayList<DynamicParsedString>();
        for (int i = startPos; i < info.arguments.size() - endSkip; ++i) {
            arguments.add(info.arguments.get(i).splitRegularArgument());
        }
        return arguments;
    }

    protected KeywordCall parseKeywordCall(ParsedLineInfo info, ParsedString cmdArg) {
        // TODO Auto-generated method stub
        return null;
    }

    protected boolean tryParseTableSwitch(ParsedLineInfo info) throws CoreException {
        ParsedString tableArgument = info.arguments.get(0);
        if (!tableArgument.getValue().startsWith("*")) {
            return false;
        }
        String table = ParserUtil.parseTable(tableArgument.getValue());
        State nextState = tableNameToState.get(table);
        if (nextState == null) {
            nextState = Ignore.STATE;
            // due to the replace above, we need to reverse engineer the exact
            // position
            int firstPos = tableArgument.getValue().indexOf(table.charAt(0));
            int lastPos = tableArgument.getValue().lastIndexOf(table.charAt(table.length() - 1)) + 1;
            addMarker(info, "Unknown table '" + table + "'", SeverityConfig.UNKNOWN_TABLE, tableArgument.getArgCharPos() + firstPos, tableArgument.getArgCharPos() + lastPos);
            return true;
        }
        info.setState(nextState, null);
        return true;
    }

    /**
     * Argument should be a single variable.
     */
    protected boolean tryParseVariable(ParsedLineInfo info, int arg) throws CoreException {
        ParsedString varArg = info.arguments.get(arg);
        String var = varArg.getValue();
        if (!var.startsWith("${") && !var.startsWith("@{")) {
            if (!var.endsWith("}")) {
                addError(info, "Variable must start with ${ or @{ and end with }", varArg.getArgCharPos(), varArg.getArgEndCharPos());
            } else {
                addError(info, "Variable must start with ${ or @{", varArg.getArgCharPos(), varArg.getArgEndCharPos());
            }
            return false;
        }
        if (!var.endsWith("}")) {
            addError(info, "Variable must end with }", varArg.getArgCharPos(), varArg.getArgEndCharPos());
            return false;
        }
        // int closingPos = var.indexOf('}', 2);
        // if (closingPos != var.length() - 1) {
        // // TODO this is wrong, recursion is actually allowed
        // addError(info, "Variable name must not contain }",
        // varArg.getArgCharPos() + closingPos, varArg.getArgCharPos() +
        // closingPos + 1);
        // return false;
        // }
        // TODO further checks?
        return true;
    }

    /**
     * A regular argument, which may contain embedded variables.
     * 
     * @param argumentDescription
     */
    protected boolean tryParseArgument(ParsedLineInfo info, int arg, String argumentDescription) throws CoreException {
        ParsedString varArg = info.arguments.get(arg);
        String var = varArg.getValue();
        // TODO
        return true;
    }

    void warnIgnoreUnusedArgs(ParsedLineInfo info, int usedArgs) throws CoreException {
        if (info.arguments.size() > usedArgs) {
            addWarning(info, "Extra argument(s) ignored", info.arguments.get(usedArgs).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
        }
    }

    void warnIgnoredLine(ParsedLineInfo info, int severity) throws CoreException {
        addMarker(info, "Unknown text ignored", severity, info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
    }

    static final Map<String, State> tableNameToState = new HashMap<String, State>();

    static {
        tableNameToState.put("setting", SettingTable.STATE);
        tableNameToState.put("settings", SettingTable.STATE);
        tableNameToState.put("metadata", SettingTable.STATE);
        tableNameToState.put("variable", VariableTable.STATE);
        tableNameToState.put("variables", VariableTable.STATE);
        tableNameToState.put("testcase", TestcaseTableInitial.STATE);
        tableNameToState.put("testcases", TestcaseTableInitial.STATE);
        tableNameToState.put("keyword", KeywordTableInitial.STATE);
        tableNameToState.put("keywords", KeywordTableInitial.STATE);
        tableNameToState.put("userkeyword", KeywordTableInitial.STATE);
        tableNameToState.put("userkeywords", KeywordTableInitial.STATE);
    }

    void addError(ParsedLineInfo info, String error, int startPos, int endPos) throws CoreException {
        addMarker(info, error, IMarker.SEVERITY_ERROR, startPos, endPos);
    }

    void addWarning(ParsedLineInfo info, String error, int startPos, int endPos) throws CoreException {
        addMarker(info, error, IMarker.SEVERITY_WARNING, startPos, endPos);
    }

    void addInfo(ParsedLineInfo info, String error, int startPos, int endPos) throws CoreException {
        addMarker(info, error, IMarker.SEVERITY_INFO, startPos, endPos);
    }

    private void addMarker(ParsedLineInfo info, String error, int severity, int startPos, int endPos) throws CoreException {
        if (severity == SEVERITY_IGNORE) {
            return;
        }
        IMarker marker = info.markerManager().createMarker(RobotBuilder.MARKER_TYPE);
        marker.setAttribute(IMarker.MESSAGE, error);
        marker.setAttribute(IMarker.SEVERITY, severity);
        marker.setAttribute(IMarker.LINE_NUMBER, info.lineNo + 1);
        marker.setAttribute(IMarker.CHAR_START, startPos);
        marker.setAttribute(IMarker.CHAR_END, endPos);
        // marker.setAttribute(IMarker.LOCATION, "Somewhere");
    }
}
