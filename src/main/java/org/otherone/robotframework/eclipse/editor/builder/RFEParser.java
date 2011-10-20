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
import org.otherone.robotframework.eclipse.editor.builder.RFEParser.Info.Argument;

public class RFEParser {

  static final int SEVERITY_IGNORE = -500;

  static final int SEVERITY_UNKNOWN_TABLE = IMarker.SEVERITY_ERROR;
  static final int SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TABLE = IMarker.SEVERITY_INFO;
  static final int SEVERITY_IGNORED_LINE_IN_SETTING_TABLE = IMarker.SEVERITY_WARNING;
  static final int SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TESTCASE_OR_KEYWORD = IMarker.SEVERITY_WARNING;

  private final String filename;
  private final Reader filestream;
  private final IProgressMonitor monitor;
  private final MarkerCreator markerCreator;

  private State state = State.IGNORE;
  String testcaseOrKeywordBeingParsed;

  void setState(State newState, String testcaseOrKeywordBeingParsed) {
    state = newState;
    this.testcaseOrKeywordBeingParsed = testcaseOrKeywordBeingParsed;
  }

  // abstract class State {
  // void parse(String line);
  // }

  public static interface MarkerCreator {
    /**
     * @see IResource#createMarker(String)
     */
    public IMarker createMarker(String type) throws CoreException;
  }

  static class Info {
    static class Argument {
      final String value;
      final int argCharPos;

      public Argument(String value, int argCharPos) {
        this.value = value;
        this.argCharPos = argCharPos;
      }

      @Override
      public String toString() {
        return '"' + value + '"';
        // return '"' + value + "\" @" + argCharPos + "-" + (argEndCharPos() - 1);
      }

      public int argEndCharPos() {
        return argCharPos + value.length();
      }

    }

    final RFEParser parser;
    final List<Argument> arguments;
    final int lineNo;
    final int lineCharPos;

    public Info(RFEParser parser, List<Argument> arguments, int lineNo, int charPos) {
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
        Argument cmdArg = info.arguments.get(0);
        String cmd = cmdArg.value;
        if (cmd.equals("Library")) {
          if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which library to load", cmdArg.argCharPos, cmdArg.argEndCharPos());
            return;
          }
          Argument library = info.arguments.get(1);
          System.out.println("Load library " + library);
          warnIgnoreUnusedArgs(info, 2);
          return;
        }
        if (cmd.equals("Resource")) {
          if (info.arguments.size() < 2) {
            addError(info, "Missing argument, e.g. which library to load", cmdArg.argCharPos, cmdArg.argEndCharPos());
            return;
          }
          Argument resource = info.arguments.get(1);
          System.out.println("Load resource " + resource);
          warnIgnoreUnusedArgs(info, 2);
          return;
        }
        warnIgnoredLine(info, SEVERITY_IGNORED_LINE_IN_SETTING_TABLE);
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
        Argument varArg = info.arguments.get(0);
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
        if (info.arguments.get(0).value.isEmpty()) {
          warnIgnoredLine(info, SEVERITY_IGNORED_LINE_OUTSIDE_RECOGNIZED_TESTCASE_OR_KEYWORD);
          return;
        }
        if (!tryParseArgument(info, 0, "testcase name")) {
          // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
          return;
        }
        info.parser.setState(TESTCASE_TABLE_ACTIVE, info.arguments.get(0).value);
      }
    },
    TESTCASE_TABLE_ACTIVE {
      @Override
      void parse(Info info) throws CoreException {
        if (tryParseTableSwitch(info)) {
          return;
        }
        if (!info.arguments.get(0).value.isEmpty()) {
          if (!tryParseArgument(info, 0, "testcase name")) {
            // warnIgnoredLine(info, IMarker.SEVERITY_ERROR);
            return;
          }
          info.parser.setState(TESTCASE_TABLE_ACTIVE, info.arguments.get(0).value);
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
      Argument tableArgument = info.arguments.get(0);
      if (!tableArgument.value.startsWith("*")) {
        return false;
      }
      String table = tableArgument.value.replace("*", "");
      State nextState = tableNameToState.get(table);
      if (nextState == null) {
        nextState = State.IGNORE;
        // due to the replace above, we need to reverse engineer the exact position
        int firstPos = tableArgument.value.indexOf(table.charAt(0));
        int lastPos = tableArgument.value.lastIndexOf(table.charAt(table.length() - 1)) + 1;
        addMarker(info, "Unknown table '" + table + "'", SEVERITY_UNKNOWN_TABLE, tableArgument.argCharPos + firstPos, tableArgument.argCharPos + lastPos);
        return true;
      }
      info.parser.setState(nextState, null);
      return true;
    }

    /**
     * Argument should be a single variable.
     */
    protected boolean tryParseVariable(Info info, int arg) throws CoreException {
      Argument varArg = info.arguments.get(arg);
      String var = varArg.value;
      if (!var.startsWith("${")) {
        if (!var.endsWith("}")) {
          addError(info, "Variable must start with ${ and end with }", varArg.argCharPos, varArg.argEndCharPos());
        } else {
          addError(info, "Variable must start with ${", varArg.argCharPos, varArg.argEndCharPos());
        }
        return false;
      }
      if (!var.endsWith("}")) {
        addError(info, "Variable must end with }", varArg.argCharPos, varArg.argEndCharPos());
        return false;
      }
      int closingPos = var.indexOf('}', 2);
      if (closingPos != var.length() - 1) {
        addError(info, "Variable name must not contain }", varArg.argCharPos + closingPos, varArg.argCharPos + closingPos + 1);
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
      Argument varArg = info.arguments.get(arg);
      String var = varArg.value;
      // TODO
      return true;
    }

    void warnIgnoreUnusedArgs(Info info, int usedArgs) throws CoreException {
      if (info.arguments.size() > usedArgs) {
        addWarning(info, "Extra argument(s) ignored", info.arguments.get(usedArgs).argCharPos, info.arguments.get(info.arguments.size() - 1).argEndCharPos());
      }
    }

    void warnIgnoredLine(Info info, int severity) throws CoreException {
      addMarker(info, "Unknown text ignored", severity, info.arguments.get(0).argCharPos, info.arguments.get(info.arguments.size() - 1).argEndCharPos());
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
    List<Argument> arguments = TxtArgumentSplitter.splitLineIntoArguments(line, charPos);
    if (arguments.isEmpty()) {
      return;
    }
    if (arguments.size() == 1 && arguments.get(0).value.isEmpty()) {
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
