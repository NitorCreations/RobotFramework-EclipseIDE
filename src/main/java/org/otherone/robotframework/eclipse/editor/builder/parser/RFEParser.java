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
package org.otherone.robotframework.eclipse.editor.builder.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.otherone.robotframework.eclipse.editor.builder.RFEBuilder;
import org.otherone.robotframework.eclipse.editor.structure.DynamicParsedString;
import org.otherone.robotframework.eclipse.editor.structure.KeywordCall;
import org.otherone.robotframework.eclipse.editor.structure.KeywordSequence;
import org.otherone.robotframework.eclipse.editor.structure.LibraryFile;
import org.otherone.robotframework.eclipse.editor.structure.ParsedString;
import org.otherone.robotframework.eclipse.editor.structure.RFEFileContents;
import org.otherone.robotframework.eclipse.editor.structure.TestCaseDefinition;
import org.otherone.robotframework.eclipse.editor.structure.UserKeywordDefinition;
import org.otherone.robotframework.eclipse.editor.structure.VariableDefinition;
import org.otherone.robotframework.eclipse.editor.structure.api.IDynamicParsedKeywordString;
import org.otherone.robotframework.eclipse.editor.structure.api.IDynamicParsedString;

/* TODO support the line continuation sequence "..." TODO support lists @{foo}, access @{foo}[0]
 * TODO support environment variables %{foo} TODO support builtin variables, section 2.5.4 TODO
 * since Robot Framework 2.6, support "number" variables ${123} ${0xFFF} ${0o777} ${0b111} TODO
 * since Robot Framework 2.5.5, all setting names can optionally include a colon at the end, for
 * example "Documentation:" */
public class RFEParser {

  static final int SEVERITY_IGNORE = -500;

  static final int SEVERITY_UNKNOWN_TABLE = IMarker.SEVERITY_ERROR;
  static final int SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TABLE = IMarker.SEVERITY_INFO;
  static final int SEVERITY_IGNORED_LINE_IN_SETTING_TABLE = IMarker.SEVERITY_WARNING;
  /**
   * As per documentation.
   */
  static final int SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TESTCASE_OR_KEYWORD = IMarker.SEVERITY_ERROR;

  private final String filename;
  private final Reader filestream;
  private final IProgressMonitor monitor;
  private final MarkerManager markerManager;

  private State state = State.IGNORE;
  final RFEFileContents fc = new RFEFileContents();
  KeywordSequence testcaseOrKeywordBeingParsed;
  List<? extends IDynamicParsedString> listToContinue;

  void setState(State newState, KeywordSequence testcaseOrKeywordBeingParsed) {
    state = newState;
    this.testcaseOrKeywordBeingParsed = testcaseOrKeywordBeingParsed;
  }

  void setContinuationList(List<? extends IDynamicParsedString> listToContinue) {
    assert listToContinue != null;
    this.listToContinue = listToContinue;
  }

  void clearContinuationList() {
    listToContinue = null;
  }

  public static interface MarkerManager {
    /**
     * @see IResource#createMarker(String)
     */
    public IMarker createMarker(String type) throws CoreException;

    public void eraseMarkers();
  }

  static class Info {
    final RFEParser parser;
    final List<ParsedString> arguments;
    final int lineNo;
    final int lineCharPos;

    public Info(RFEParser parser, List<ParsedString> arguments, int lineNo, int charPos) {
      this.parser = parser;
      this.arguments = Collections.unmodifiableList(arguments);
      this.lineNo = lineNo;
      this.lineCharPos = charPos;
    }

  }

  enum State {
    IGNORE {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        warnIgnoredLine(info, SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TABLE);
      }
    },
    SETTING_TABLE {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        ParsedString cmdArg = info.arguments.get(0);
        String cmd = cmdArg.getValue();
        if (cmd.equals("...")) {
          // TODO
        } else {
          info.parser.clearContinuationList();
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
          warnIgnoredLine(info, SEVERITY_IGNORED_LINE_IN_SETTING_TABLE);
        }
      }

      private void parseResourceFile(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which resource file to load", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        ParsedString resource = info.arguments.get(1);
        System.out.println("Load resource file " + resource);
        boolean success = info.parser.fc.getSettingsInt().addResourceFile(resource.splitRegularArgument());
        if (!success) {
          addWarning(info, "Duplicate resource file", resource.getArgCharPos(), resource.getArgEndCharPos());
        }
        warnIgnoreUnusedArgs(info, 2);
        return;
      }

      private void parseVariableFile(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which variable file to load", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        ParsedString varFile = info.arguments.get(1);
        System.out.println("Load variable file " + varFile);
        List<DynamicParsedString> arguments = splitRegularArguments(info, 2, 0);
        boolean success = info.parser.fc.getSettingsInt().addVariableFile(varFile.splitRegularArgument(), arguments);
        if (!success) {
          addWarning(info, "Duplicate variable file", varFile.getArgCharPos(), varFile.getArgEndCharPos());
        }
        info.parser.setContinuationList(arguments);
      }

      private void parseLibraryFile(Info info, ParsedString cmdArg) throws CoreException {
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
        boolean success = info.parser.fc.getSettingsInt().addLibraryFile(libraryFile);
        if (!success) {
          IDynamicParsedKeywordString customName = libraryFile.getCustomName();
          addWarning(info, "Duplicate library file", customName.getArgCharPos(), customName.getArgEndCharPos());
        }
        info.parser.setContinuationList(arguments);
      }

      private void parseSuiteSetup(Info info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
          info.parser.fc.getSettingsInt().setSuiteSetup(call);
          info.parser.setContinuationList(call.getArguments());
        }
      }

      private void parseSuiteTeardown(Info info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
          info.parser.fc.getSettingsInt().setSuiteTeardown(call);
          info.parser.setContinuationList(call.getArguments());
        }
      }

      private void parseDocumentation(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which tag(s) to force to all test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        List<DynamicParsedString> documentation = splitRegularArguments(info, 1, 0);
        info.parser.fc.getSettingsInt().setDocumentation(documentation);
        info.parser.setContinuationList(documentation);
      }

      private void parseMetadata(Info info, ParsedString cmdArg) throws CoreException {
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
        boolean success = info.parser.fc.getSettingsInt().addMetadata(key, values);
        if (!success) {
          addWarning(info, "Duplicate metadata key", key.getArgCharPos(), key.getArgEndCharPos());
        }
        info.parser.setContinuationList(values);
      }

      private void parseForceTags(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which tag(s) to force to all test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        List<DynamicParsedString> tags = splitRegularArguments(info, 1, 0);
        info.parser.fc.getSettingsInt().setForcedTestTags(tags);
        info.parser.setContinuationList(tags);
      }

      private void parseDefaultTags(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which tag(s) to use as default for test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        List<DynamicParsedString> tags = splitRegularArguments(info, 1, 0);
        info.parser.fc.getSettingsInt().setDefaultTestTags(tags);
        info.parser.setContinuationList(tags);
      }

      private void parseTestSetup(Info info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
          info.parser.fc.getSettingsInt().setDefaultTestSetup(call);
          info.parser.setContinuationList(call.getArguments());
        }
      }

      private void parseTestTeardown(Info info, ParsedString cmdArg) throws CoreException {
        KeywordCall call = parseKeywordCall(info, cmdArg);
        if (call != null) {
          info.parser.fc.getSettingsInt().setDefaultTestTeardown(call);
          info.parser.setContinuationList(call.getArguments());
        }
      }

      private void parseTestTemplate(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which template to use as default for test cases", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        ParsedString template = info.arguments.get(1);
        info.parser.fc.getSettingsInt().setTemplate(template);
        warnIgnoreUnusedArgs(info, 2);
      }

      private void parseTestTimeout(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. the default test case timeout", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        ParsedString timeout = info.arguments.get(1);
        info.parser.fc.getSettingsInt().setDefaultTestTimeout(timeout.splitRegularArgument());
        if (info.arguments.size() >= 3) {
          ParsedString message = info.arguments.get(2);
          info.parser.fc.getSettingsInt().setDefaultTestTimeoutMessage(message);
        }
        warnIgnoreUnusedArgs(info, 3);
      }

    },
    VARIABLE_TABLE {
      @Override
      void parse(Info info) throws CoreException {
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
        info.parser.fc.addVariable(varDef);
        info.parser.setContinuationList(values);
      }
    },
    TESTCASE_TABLE_INITIAL {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        if (info.arguments.get(0).getValue().isEmpty()) {
          warnIgnoredLine(info, SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TESTCASE_OR_KEYWORD);
          return;
        }
        if (!tryParseArgument(info, 0, "test case name")) {
          // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
          return;
        }
        // start new testcase
        TestCaseDefinition tc = new TestCaseDefinition(info.parser.fc);
        tc.setSequenceName(info.arguments.get(0));
        info.parser.fc.addTestCase(tc);
        info.parser.setState(TESTCASE_TABLE_ACTIVE, tc);
        if (info.arguments.size() == 1) {
          return;
        }
        parseTestcaseLine(info);
      }
    },
    TESTCASE_TABLE_ACTIVE {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        if (!info.arguments.get(0).getValue().isEmpty()) {
          // start new testcase
          if (!tryParseArgument(info, 0, "test case name")) {
            // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
            return;
          }
          TestCaseDefinition tc = new TestCaseDefinition(info.parser.fc);
          tc.setSequenceName(info.arguments.get(0));
          info.parser.fc.addTestCase(tc);
          info.parser.setState(TESTCASE_TABLE_ACTIVE, tc);
          if (info.arguments.size() == 1) {
            return;
          }
        }
        parseTestcaseLine(info);
      }
    },
    KEYWORD_TABLE_INITIAL {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        if (info.arguments.get(0).getValue().isEmpty()) {
          warnIgnoredLine(info, SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TESTCASE_OR_KEYWORD);
          return;
        }
        if (!tryParseArgument(info, 0, "user keyword name")) {
          // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
          return;
        }
        // start new user keyword
        UserKeywordDefinition ukw = new UserKeywordDefinition();
        ukw.setSequenceName(info.arguments.get(0).splitRegularArgument());
        info.parser.fc.addKeyword(ukw);
        info.parser.setState(KEYWORD_TABLE_ACTIVE, ukw);
        if (info.arguments.size() == 1) {
          return;
        }
        parseUserKeywordLine(info);
      }
    },
    KEYWORD_TABLE_ACTIVE {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        if (!info.arguments.get(0).getValue().isEmpty()) {
          // start new testcase
          if (!tryParseArgument(info, 0, "user keyword name")) {
            // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
            return;
          }
          UserKeywordDefinition ukw = new UserKeywordDefinition();
          ukw.setSequenceName(info.arguments.get(0).splitRegularArgument());
          info.parser.fc.addKeyword(ukw);
          info.parser.setState(KEYWORD_TABLE_ACTIVE, ukw);
          if (info.arguments.size() == 1) {
            return;
          }
        }
        parseUserKeywordLine(info);
      }
    },
    ;

    abstract void parse(Info info) throws CoreException;

    protected void parseTestcaseLine(Info info) throws CoreException {
      // TODO
      parseTestcaseOrUserKeywordLine(info);
    }

    protected void parseUserKeywordLine(Info info) throws CoreException {
      // TODO
      parseTestcaseOrUserKeywordLine(info);
    }

    private void parseTestcaseOrUserKeywordLine(Info info) throws CoreException {
      for (int i = 1; i < info.arguments.size(); ++i) {
        tryParseArgument(info, i, i == 1 ? "keyword name" : "keyword argument " + (i - 1));
      }
      // TODO Auto-generated method stub

    }

    List<DynamicParsedString> splitRegularArguments(Info info, int startPos, int endSkip) {
      List<DynamicParsedString> arguments = new ArrayList<DynamicParsedString>();
      for (int i = startPos; i < info.arguments.size() - endSkip; ++i) {
        arguments.add(info.arguments.get(i).splitRegularArgument());
      }
      return arguments;
    }

    protected KeywordCall parseKeywordCall(Info info, ParsedString cmdArg) {
      // TODO Auto-generated method stub
      return null;
    }

    protected boolean tryParseTableSwitch(Info info) throws CoreException {
      ParsedString tableArgument = info.arguments.get(0);
      if (!tableArgument.getValue().startsWith("*")) {
        return false;
      }
      String table = tableArgument.getValue().replace("*", "");
      State nextState = tableNameToState.get(table);
      if (nextState == null) {
        nextState = State.IGNORE;
        // due to the replace above, we need to reverse engineer the exact position
        int firstPos = tableArgument.getValue().indexOf(table.charAt(0));
        int lastPos = tableArgument.getValue().lastIndexOf(table.charAt(table.length() - 1)) + 1;
        addMarker(info, "Unknown table '" + table + "'", SEVERITY_UNKNOWN_TABLE, tableArgument.getArgCharPos() + firstPos, tableArgument.getArgCharPos()
            + lastPos);
        return true;
      }
      info.parser.setState(nextState, null);
      return true;
    }

    /**
     * Argument should be a single variable.
     */
    protected boolean tryParseVariable(Info info, int arg) throws CoreException {
      ParsedString varArg = info.arguments.get(arg);
      String var = varArg.getValue();
      if (!var.startsWith("${")) {
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
      int closingPos = var.indexOf('}', 2);
      if (closingPos != var.length() - 1) {
        // TODO this is wrong, recursion is actually allowed
        addError(info, "Variable name must not contain }", varArg.getArgCharPos() + closingPos, varArg.getArgCharPos() + closingPos + 1);
        return false;
      }
      // TODO further checks?
      return true;
    }

    /**
     * A regular argument, which may contain embedded variables.
     * 
     * @param argumentDescription
     */
    protected boolean tryParseArgument(Info info, int arg, String argumentDescription) throws CoreException {
      ParsedString varArg = info.arguments.get(arg);
      String var = varArg.getValue();
      // TODO
      return true;
    }

    void warnIgnoreUnusedArgs(Info info, int usedArgs) throws CoreException {
      if (info.arguments.size() > usedArgs) {
        addWarning(info, "Extra argument(s) ignored", info.arguments.get(usedArgs).getArgCharPos(), info.arguments.get(info.arguments.size() - 1)
            .getArgEndCharPos());
      }
    }

    void warnIgnoredLine(Info info, int severity) throws CoreException {
      addMarker(info, "Unknown text ignored", severity, info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
    }

    static final Pattern TABLE_RE = Pattern.compile("^\\s*\\*+\\s*([^*]+?)\\s*\\**\\s*$");

    static final Map<String, State> tableNameToState = new HashMap<String, State>();

    static {
      tableNameToState.put("Setting", State.SETTING_TABLE);
      tableNameToState.put("Settings", State.SETTING_TABLE);
      tableNameToState.put("Metadata", State.SETTING_TABLE);
      tableNameToState.put("Variable", State.VARIABLE_TABLE);
      tableNameToState.put("Variables", State.VARIABLE_TABLE);
      tableNameToState.put("Test Case", State.TESTCASE_TABLE_INITIAL);
      tableNameToState.put("Test Cases", State.TESTCASE_TABLE_INITIAL);
      tableNameToState.put("Keyword", State.KEYWORD_TABLE_INITIAL);
      tableNameToState.put("Keywords", State.KEYWORD_TABLE_INITIAL);
      tableNameToState.put("User Keyword", State.KEYWORD_TABLE_INITIAL);
      tableNameToState.put("User Keywords", State.KEYWORD_TABLE_INITIAL);
    }

    void addError(Info info, String error, int startPos, int endPos) throws CoreException {
      addMarker(info, error, IMarker.SEVERITY_ERROR, startPos, endPos);
    }

    void addWarning(Info info, String error, int startPos, int endPos) throws CoreException {
      addMarker(info, error, IMarker.SEVERITY_WARNING, startPos, endPos);
    }

    void addInfo(Info info, String error, int startPos, int endPos) throws CoreException {
      addMarker(info, error, IMarker.SEVERITY_INFO, startPos, endPos);
    }

    private void addMarker(Info info, String error, int severity, int startPos, int endPos) throws CoreException {
      if (severity == SEVERITY_IGNORE) {
        return;
      }
      IMarker marker = info.parser.markerManager.createMarker(RFEBuilder.MARKER_TYPE);
      marker.setAttribute(IMarker.MESSAGE, error);
      marker.setAttribute(IMarker.SEVERITY, severity);
      marker.setAttribute(IMarker.LINE_NUMBER, info.lineNo);
      marker.setAttribute(IMarker.CHAR_START, startPos);
      marker.setAttribute(IMarker.CHAR_END, endPos);
      // marker.setAttribute(IMarker.LOCATION, "Somewhere");
    }

  }

  public RFEParser(final IFile file, IProgressMonitor monitor) throws UnsupportedEncodingException, CoreException {
    this.filename = file.toString();
    this.filestream = new InputStreamReader(file.getContents(), file.getCharset());
    this.monitor = monitor == null ? new NullProgressMonitor() : monitor;
    this.markerManager = new MarkerManager() {
      @Override
      public IMarker createMarker(String type) throws CoreException {
        return file.createMarker(type);
      }

      @Override
      public void eraseMarkers() {
        try {
          file.deleteMarkers(RFEBuilder.MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException ce) {}
      }
    };
  }

  /**
   * For unit tests.
   * 
   * @param file the file path
   * @param charset the charset to read the file in
   * @param markerManager for managing markers
   * @throws UnsupportedEncodingException
   * @throws FileNotFoundException
   */
  public RFEParser(File file, String charset, MarkerManager markerManager) throws UnsupportedEncodingException, FileNotFoundException {
    this.filename = file.getName();
    this.filestream = new InputStreamReader(new FileInputStream(file), charset);
    this.monitor = new NullProgressMonitor();
    this.markerManager = markerManager;
  }

  public void parse() throws CoreException {
    try {
      System.out.println("Parsing " + filename);
      markerManager.eraseMarkers();
      CountingLineReader contents = new CountingLineReader(filestream);
      String line;
      int lineNo = 1;
      int charPos = 0;
      while (null != (line = contents.readLine())) {
        if (monitor.isCanceled()) {
          return;
        }
        try {
          parseLine(line, lineNo, charPos);
        } catch (CoreException e) {
          throw new RuntimeException("Error when parsing line " + lineNo + ": '" + line + "'", e);
        } catch (RuntimeException e) {
          throw new RuntimeException("Internal error when parsing line " + lineNo + ": '" + line + "'", e);
        }
        ++lineNo;
        charPos = contents.getCharPos();
      }

      // TODO store results
    } catch (Exception e) {
      throw new RuntimeException("Error parsing robot file " + filename, e);
    } finally {
      try {
        filestream.close();
      } catch (IOException e) {
        // ignore
      }
    }
  }

  // private static final Pattern LINE_RE = Pattern.compile("\\G(?:[^#\\\\]|#|\\.)");

  private void parseLine(String line, int lineNo, int charPos) throws CoreException {
    List<ParsedString> arguments = TxtArgumentSplitter.splitLineIntoArguments(line, charPos);
    if (arguments.isEmpty()) {
      return;
    }
    if (arguments.size() == 1 && arguments.get(0).getValue().isEmpty()) {
      return;
    }
    System.out.println(arguments);
    State oldState = state;
    state.parse(new Info(this, arguments, lineNo, charPos));
    if (oldState != state) {
      System.out.println("State " + oldState + " -> " + state);
    }
  }

}
