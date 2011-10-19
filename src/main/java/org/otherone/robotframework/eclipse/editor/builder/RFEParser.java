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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.otherone.robotframework.eclipse.editor.builder.RFEParser.Info.Argument;

public class RFEParser {

  private final IFile file;
  private final IProgressMonitor monitor;

  private State state;

  // abstract class State {
  // void parse(String line);
  // }

  static class Info {
    static class Argument {
      final String value;
      final int argCharPos;

      public Argument(String value, int argCharPos) {
        this.value = value;
        this.argCharPos = argCharPos;
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
      }
    },
    SETTING_TABLE {
      @Override
      void parse(Info info) throws CoreException {

      }
    },
    VARIABLE_TABLE {
      @Override
      void parse(Info info) throws CoreException {

      }
    },
    TESTCASE_TABLE_INITIAL {
      @Override
      void parse(Info info) throws CoreException {

      }
    },
    TESTCASE_TABLE_ACTIVE {
      @Override
      void parse(Info info) throws CoreException {

      }
    },
    KEYWORD_TABLE_INITIAL {
      @Override
      void parse(Info info) throws CoreException {

      }
    },
    KEYWORD_TABLE_ACTIVE {
      @Override
      void parse(Info info) throws CoreException {

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
        int lastPos = tableArgument.value.lastIndexOf(table.charAt(table.length() - 1));
        addError(info, "Unknown table '" + table + "'", firstPos, lastPos);
        return true;
      }
      info.parser.state = nextState;
      return true;
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
      IMarker marker = info.parser.file.createMarker(RFEBuilder.MARKER_TYPE);
      marker.setAttribute(IMarker.MESSAGE, error);
      marker.setAttribute(IMarker.SEVERITY, severity);
      marker.setAttribute(IMarker.LINE_NUMBER, info.lineNo);
      marker.setAttribute(IMarker.CHAR_START, info.lineCharPos + startPos);
      marker.setAttribute(IMarker.CHAR_END, info.lineCharPos + endPos);
      // marker.setAttribute(IMarker.LOCATION, "Somewhere");
    }
  }

  public RFEParser(IFile file, IProgressMonitor monitor) {
    this.file = file;
    this.monitor = monitor;
    state = State.IGNORE;
  }

  public void parse() throws CoreException {
    try {

      String charset = file.getCharset();
      System.out.println("Parsing " + file + " in charset " + charset);
      InputStream contentsRaw = file.getContents();
      try {
        CountingLineReader contents = new CountingLineReader(new InputStreamReader(contentsRaw, charset));
        String line;
        int lineNo = 1;
        int charPos = 0;
        while (null != (line = contents.readLine())) {
          if (monitor.isCanceled()) {
            return;
          }
          parseLine(line, lineNo, charPos);
          ++lineNo;
          charPos = contents.getCharPos();
        }
      } finally {
        contentsRaw.close();
      }

      // TODO store results
    } catch (Exception e) {
      throw new RuntimeException("Error parsing robot file", e);
    }
  }

  // private static final Pattern LINE_RE = Pattern.compile("\\G(?:[^#\\\\]|#|\\.)");

  private void parseLine(String line, int lineNo, int charPos) throws CoreException {
    List<Argument> arguments = TxtArgumentSplitter.splitLineIntoArguments(line, charPos);

    state.parse(new Info(this, arguments, lineNo, charPos));
  }

}
