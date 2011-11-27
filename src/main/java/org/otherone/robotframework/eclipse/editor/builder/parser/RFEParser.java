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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.otherone.robotframework.eclipse.editor.builder.RFEBuilder;
import org.otherone.robotframework.eclipse.editor.builder.parser.state.Ignore;
import org.otherone.robotframework.eclipse.editor.builder.parser.state.State;
import org.otherone.robotframework.eclipse.editor.builder.util.NullMarkerManager;
import org.otherone.robotframework.eclipse.editor.structure.KeywordSequence;
import org.otherone.robotframework.eclipse.editor.structure.ParsedString;
import org.otherone.robotframework.eclipse.editor.structure.RFEFileContents;
import org.otherone.robotframework.eclipse.editor.structure.api.IDynamicParsedString;
import org.otherone.robotframework.eclipse.editor.structure.api.IRFEFileContents;

/* TODO support the line continuation sequence "..." TODO support lists @{foo}, access @{foo}[0]
 * TODO support environment variables %{foo} TODO support builtin variables, section 2.5.4 TODO
 * since Robot Framework 2.6, support "number" variables ${123} ${0xFFF} ${0o777} ${0b111} TODO
 * since Robot Framework 2.5.5, all setting names can optionally include a colon at the end, for
 * example "Documentation:" */
public class RFEParser {

  private final String filename;
  private final Reader filestream;
  private final IProgressMonitor monitor;
  private final MarkerManager markerManager;

  private State state = Ignore.STATE;
  final RFEFileContents fc = new RFEFileContents();
  KeywordSequence testcaseOrKeywordBeingParsed;
  List<? extends IDynamicParsedString> listToContinue;

  public void setState(State newState, KeywordSequence testcaseOrKeywordBeingParsed) {
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

  public static class ParsedLineInfo {
    final RFEParser parser;
    public final List<ParsedString> arguments;
    public final int lineNo;
    public final int lineCharPos;

    public ParsedLineInfo(RFEParser parser, List<ParsedString> arguments, int lineNo, int charPos) {
      this.parser = parser;
      this.arguments = Collections.unmodifiableList(arguments);
      this.lineNo = lineNo;
      this.lineCharPos = charPos;
    }

    public RFEFileContents fc() {
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
      return parser.markerManager;
    }

  }

  /**
   * For files being "compiled" from disk.
   * 
   * @param file
   * @param monitor
   * @throws UnsupportedEncodingException
   * @throws CoreException
   */
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

  /**
   * For documents being edited.
   * 
   * @param document
   */
  public RFEParser(IDocument document) {
    this.filename = "<document being edited>";
    this.filestream = new StringReader(document.get());
    this.monitor = new NullProgressMonitor();
    this.markerManager = new NullMarkerManager();
  }

  public IRFEFileContents parse() throws CoreException {
    try {
      System.out.println("Parsing " + filename);
      markerManager.eraseMarkers();
      CountingLineReader contents = new CountingLineReader(filestream);
      String line;
      int lineNo = 1;
      int charPos = 0;
      while (null != (line = contents.readLine())) {
        if (monitor.isCanceled()) {
          return null;
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
    return fc;
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
    state.parse(new ParsedLineInfo(this, arguments, lineNo, charPos));
    if (oldState != state) {
      System.out.println("State " + oldState + " -> " + state);
    }
  }

}
