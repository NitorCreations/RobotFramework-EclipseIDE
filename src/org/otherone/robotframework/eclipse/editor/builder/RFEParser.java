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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class RFEParser {

  private final IFile file;
  private final IProgressMonitor monitor;

  public RFEParser(IFile file, IProgressMonitor monitor) {
    this.file = file;
    this.monitor = monitor;
  }

  public void parse() throws CoreException {
    // TODO
    System.out.println("Parsing " + file);

    IMarker marker = file.createMarker(RFEBuilder.MARKER_TYPE);
    marker.setAttribute(IMarker.MESSAGE, "Something is wrong");
    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
    marker.setAttribute(IMarker.LINE_NUMBER, 3);
    marker.setAttribute(IMarker.CHAR_START, 11); // from beginning of document
    marker.setAttribute(IMarker.CHAR_END, 19);
    marker.setAttribute(IMarker.LOCATION, "Somewhere");

    marker = file.createMarker(RFEBuilder.MARKER_TYPE);
    marker.setAttribute(IMarker.MESSAGE, "Something is wrong2");
    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
    marker.setAttribute(IMarker.LINE_NUMBER, 3);
    marker.setAttribute(IMarker.CHAR_START, 25); // from beginning of document
    marker.setAttribute(IMarker.CHAR_END, 27);
    marker.setAttribute(IMarker.LOCATION, "Somewhere2");
  }

  // private void addMarker(IFile file, String message, int lineNumber, int severity) {
  // try {
  // IMarker marker = file.createMarker(RFEBuilder.MARKER_TYPE);
  // marker.setAttribute(IMarker.MESSAGE, message);
  // marker.setAttribute(IMarker.SEVERITY, severity);
  // if (lineNumber == -1) {
  // lineNumber = 1;
  // }
  // marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
  // } catch (CoreException e) {
  // }
  // }
}
