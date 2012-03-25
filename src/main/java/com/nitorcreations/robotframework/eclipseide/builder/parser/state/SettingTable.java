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

import java.util.List;

import org.eclipse.core.runtime.CoreException;

import com.nitorcreations.robotframework.eclipseide.builder.parser.ParsedLineInfo;
import com.nitorcreations.robotframework.eclipseide.builder.parser.SeverityConfig;
import com.nitorcreations.robotframework.eclipseide.structure.DynamicParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.KeywordCall;
import com.nitorcreations.robotframework.eclipseide.structure.LibraryFile;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IDynamicParsedKeywordString;

public class SettingTable extends State {

    public static final State STATE = new SettingTable();

    @Override
    public void parse(ParsedLineInfo info) throws CoreException {
        if (tryParseTableSwitch(info)) {
            return;
        }
        ParsedString cmdArg = info.arguments.get(0);
        String cmd = cmdArg.getValue();
        if (cmd.equals("...")) {
            // TODO
        } else {
            info.clearContinuationList();
        }
        if (cmd.endsWith(":")) {
            cmd = cmd.substring(0, cmd.length() - 1);
        }
        if (cmd.equals("Resource")) {
            parseResourceFile(info, cmdArg);
        } else if (cmd.equals("Variables")) {
            parseVariableFile(info, cmdArg);
        } else if (cmd.equals("Library")) {
            parseLibraryFile(info, cmdArg);
        } else if (cmd.equals("Suite Setup")) {
            parseSuiteSetup(info, cmdArg);
        } else if (cmd.equals("Suite Teardown")) {
            parseSuiteTeardown(info, cmdArg);
        } else if (cmd.equals("Documentation")) {
            parseDocumentation(info, cmdArg);
        } else if (cmd.equals("Metadata")) {
            parseMetadata(info, cmdArg);
        } else if (cmd.equals("Force Tags")) {
            parseForceTags(info, cmdArg);
        } else if (cmd.equals("Default Tags")) {
            parseDefaultTags(info, cmdArg);
        } else if (cmd.equals("Test Setup")) {
            parseTestSetup(info, cmdArg);
        } else if (cmd.equals("Test Teardown")) {
            parseTestTeardown(info, cmdArg);
        } else if (cmd.equals("Test Template")) {
            parseTestTemplate(info, cmdArg);
        } else if (cmd.equals("Test Timeout")) {
            parseTestTimeout(info, cmdArg);
        } else {
            warnIgnoredLine(info, SeverityConfig.IGNORED_LINE_IN_SETTING_TABLE);
        }
    }

    private void parseResourceFile(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which resource file to load", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        ParsedString resource = info.arguments.get(1);
        System.out.println("Load resource file " + resource);
        boolean success = info.fc().getSettingsInt().addResourceFile(resource.splitRegularArgument());
        if (!success) {
            addWarning(info, "Duplicate resource file", resource.getArgCharPos(), resource.getArgEndCharPos());
        }
        warnIgnoreUnusedArgs(info, 2);
        return;
    }

    private void parseVariableFile(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which variable file to load", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        ParsedString varFile = info.arguments.get(1);
        System.out.println("Load variable file " + varFile);
        List<DynamicParsedString> arguments = splitRegularArguments(info, 2, 0);
        boolean success = info.fc().getSettingsInt().addVariableFile(varFile.splitRegularArgument(), arguments);
        if (!success) {
            addWarning(info, "Duplicate variable file", varFile.getArgCharPos(), varFile.getArgEndCharPos());
        }
        info.setContinuationList(arguments);
    }

    private void parseLibraryFile(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which library to load", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        ParsedString library = info.arguments.get(1);
        System.out.println("Load library " + library);
        LibraryFile libraryFile = new LibraryFile();
        libraryFile.setRealName(library.splitRegularArgument());
        boolean hasCustomName = info.arguments.size() >= 4 && info.arguments.get(info.arguments.size() - 2).getValue().equalsIgnoreCase("WITH NAME");
        if (hasCustomName) {
            libraryFile.setCustomName(info.arguments.get(info.arguments.size() - 1).splitRegularArgument());
        } else {
            libraryFile.setCustomName(libraryFile.getRealName());
        }
        List<DynamicParsedString> arguments = splitRegularArguments(info, 2, hasCustomName ? 2 : 0);
        libraryFile.setArguments(arguments);
        boolean success = info.fc().getSettingsInt().addLibraryFile(libraryFile);
        if (!success) {
            IDynamicParsedKeywordString customName = libraryFile.getCustomName();
            addWarning(info, "Duplicate library file", customName.getArgCharPos(), customName.getArgEndCharPos());
        }
        info.setContinuationList(arguments);
    }

    private void parseSuiteSetup(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
            info.fc().getSettingsInt().setSuiteSetup(call);
            info.setContinuationList(call.getArguments());
        }
    }

    private void parseSuiteTeardown(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
            info.fc().getSettingsInt().setSuiteTeardown(call);
            info.setContinuationList(call.getArguments());
        }
    }

    private void parseDocumentation(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which tag(s) to force to all test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        List<DynamicParsedString> documentation = splitRegularArguments(info, 1, 0);
        info.fc().getSettingsInt().setDocumentation(documentation);
        info.setContinuationList(documentation);
    }

    private void parseMetadata(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 3) {
            if (info.arguments.size() < 2) {
                addError(info, "Missing argument(s), e.g. the metadata key and value(s)", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            } else {
                addError(info, "Missing argument, e.g. the metadata value(s)", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            }
            return;
        }
        List<DynamicParsedString> values = splitRegularArguments(info, 2, 0);
        ParsedString key = info.arguments.get(1);
        boolean success = info.fc().getSettingsInt().addMetadata(key, values);
        if (!success) {
            addWarning(info, "Duplicate metadata key", key.getArgCharPos(), key.getArgEndCharPos());
        }
        info.setContinuationList(values);
    }

    private void parseForceTags(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which tag(s) to force to all test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        List<DynamicParsedString> tags = splitRegularArguments(info, 1, 0);
        info.fc().getSettingsInt().setForcedTestTags(tags);
        info.setContinuationList(tags);
    }

    private void parseDefaultTags(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which tag(s) to use as default for test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        List<DynamicParsedString> tags = splitRegularArguments(info, 1, 0);
        info.fc().getSettingsInt().setDefaultTestTags(tags);
        info.setContinuationList(tags);
    }

    private void parseTestSetup(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
            info.fc().getSettingsInt().setDefaultTestSetup(call);
            info.setContinuationList(call.getArguments());
        }
    }

    private void parseTestTeardown(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
            info.fc().getSettingsInt().setDefaultTestTeardown(call);
            info.setContinuationList(call.getArguments());
        }
    }

    private void parseTestTemplate(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which template to use as default for test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        ParsedString template = info.arguments.get(1);
        info.fc().getSettingsInt().setTemplate(template);
        warnIgnoreUnusedArgs(info, 2);
    }

    private void parseTestTimeout(ParsedLineInfo info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. the default test case timeout", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
            return;
        }
        ParsedString timeout = info.arguments.get(1);
        info.fc().getSettingsInt().setDefaultTestTimeout(timeout.splitRegularArgument());
        if (info.arguments.size() >= 3) {
            ParsedString message = info.arguments.get(2);
            info.fc().getSettingsInt().setDefaultTestTimeoutMessage(message);
        }
        warnIgnoreUnusedArgs(info, 3);
    }

}
