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
package org.otherone.robotframework.eclipse.editor.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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

/*
 * TODO support the line continuation sequence "..."
 * TODO support lists @{foo}, access @{foo}[0]
 * TODO support environment variables %{foo}
 * TODO support builtin variables, section 2.5.4 
 * TODO since Robot Framework 2.6, support "number" variables ${123} ${0xFFF} ${0o777} ${0b111}
 * TODO since Robot Framework 2.5.5, all setting names can optionally include a colon at the end, for example "Documentation:" 
 */
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
  private final MarkerCreator markerCreator;

  private State state = State.IGNORE;
  final RFEFileContents fc = new RFEFileContents();
  KeywordSequence testcaseOrKeywordBeingParsed;
  List<DynamicParsedString> listToContinue;

  void setState(State newState, KeywordSequence testcaseOrKeywordBeingParsed, List<DynamicParsedString> listToContinue) {
    state = newState;
    this.testcaseOrKeywordBeingParsed = testcaseOrKeywordBeingParsed;
    this.listToContinue = listToContinue;
  }

  public static interface MarkerCreator {
    /**
     * @see IResource#createMarker(String)
     */
    public IMarker createMarker(String type) throws CoreException;
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
        System.out.println("Load resource " + resource);
        boolean dupe = info.parser.fc.getSettingsInt().addResourceFile(resource.splitRegularArgument());
        if (dupe) {
          addWarning(info, "Duplicate resource load", resource.getArgCharPos(), resource.getArgEndCharPos());
        }
        warnIgnoreUnusedArgs(info, 2);
        return;
      }

      private void parseVariableFile(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which variable file to load", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        ParsedString library = info.arguments.get(1);
        System.out.println("Load library " + library);
        return;
      }

      private void parseLibraryFile(Info info, ParsedString cmdArg) throws CoreException {
        if (info.arguments.size() < 2) {
          addError(info, "Missing argument, e.g. which library to load", cmdArg.getArgCharPos(), cmdArg.getArgEndCharPos());
          return;
        }
        ParsedString library = info.arguments.get(1);
        System.out.println("Load library " + library);
        return;
      }

      private void parseSuiteSetup(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseSuiteTeardown(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseDocumentation(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseMetadata(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseForceTags(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseDefaultTags(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseTestSetup(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseTestTeardown(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseTestTemplate(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
      }

      private void parseTestTimeout(Info info, ParsedString cmdArg) throws CoreException {
        addError(info, "Not implemented", info.arguments.get(0).getArgCharPos(), info.arguments.get(info.arguments.size() - 1).getArgEndCharPos());
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
        // TODO store location of variable definition
        tryParseArgument(info, 1, "variable content");
        warnIgnoreUnusedArgs(info, 2);
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
        if (!tryParseArgument(info, 0, "testcase name")) {
          // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
          return;
        }
        info.parser.setState(TESTCASE_TABLE_ACTIVE, null, null);
      }
    },
    TESTCASE_TABLE_ACTIVE {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        if (!info.arguments.get(0).getValue().isEmpty()) {
          if (!tryParseArgument(info, 0, "testcase name")) {
            // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
            return;
          }
          info.parser.setState(TESTCASE_TABLE_ACTIVE, null, null);
          return;
        }
        for (int i = 1; i < info.arguments.size(); ++i) {
          tryParseArgument(info, i, i == 1 ? "keyword name" : "keyword argument " + (i - 1));
        }
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
        if (!tryParseArgument(info, 0, "keyword name")) {
          // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
          return;
        }
        info.parser.setState(KEYWORD_TABLE_ACTIVE, null, null);
      }
    },
    KEYWORD_TABLE_ACTIVE {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }

      }
    },
    ;
    abstract void parse(Info info) throws CoreException;

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
      info.parser.setState(nextState, null, null);
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
      IMarker marker = info.parser.markerCreator.createMarker(RFEBuilder.MARKER_TYPE);
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
    this.markerCreator = new MarkerCreator() {
      @Override
      public IMarker createMarker(String type) throws CoreException {
        return file.createMarker(type);
      }
    };
  }

  /**
   * For unit tests.
   * 
   * @param file
   *          the file path
   * @param charset
   *          the charset to read the file in
   * @param markerCreator
   *          for tracking marker creation
   * @throws UnsupportedEncodingException
   * @throws FileNotFoundException
   */
  public RFEParser(File file, String charset, MarkerCreator markerCreator) throws UnsupportedEncodingException, FileNotFoundException {
    this.filename = file.getName();
    this.filestream = new InputStreamReader(new FileInputStream(file), charset);
    this.monitor = new NullProgressMonitor();
    this.markerCreator = markerCreator;
  }

  public void parse() throws CoreException {
    try {
      System.out.println("Parsing " + filename);
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
          throw new RuntimeException("Internal parser error on line " + lineNo, e);
        } catch (RuntimeException e) {
          throw new RuntimeException("Internal parser error on line " + lineNo, e);
        }
        ++lineNo;
        charPos = contents.getCharPos();
      }

      // TODO store results
    } catch (Exception e) {
      throw new RuntimeException("Error parsing robot file", e);
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
