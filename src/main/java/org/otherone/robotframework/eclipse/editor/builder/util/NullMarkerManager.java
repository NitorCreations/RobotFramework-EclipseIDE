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
package org.otherone.robotframework.eclipse.editor.builder.util;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFEParser.MarkerManager;

public class NullMarkerManager implements MarkerManager {

  @Override
  public IMarker createMarker(String type) throws CoreException {
    return new IMarker() {

      @SuppressWarnings("rawtypes")
      @Override
      public Object getAdapter(Class adapter) {
        return null;
      }

      @Override
      public void delete() throws CoreException {}

      @Override
      public boolean exists() {
        return false;
      }

      @Override
      public Object getAttribute(String attributeName) throws CoreException {
        return null;
      }

      @Override
      public int getAttribute(String attributeName, int defaultValue) {
        return 0;
      }

      @Override
      public String getAttribute(String attributeName, String defaultValue) {
        return null;
      }

      @Override
      public boolean getAttribute(String attributeName, boolean defaultValue) {
        return false;
      }

      @Override
      public Map<String, Object> getAttributes() throws CoreException {
        return null;
      }

      @Override
      public Object[] getAttributes(String[] attributeNames) throws CoreException {
        return null;
      }

      @Override
      public long getCreationTime() throws CoreException {
        return 0;
      }

      @Override
      public long getId() {
        return 0;
      }

      @Override
      public IResource getResource() {
        return null;
      }

      @Override
      public String getType() throws CoreException {
        return null;
      }

      @Override
      public boolean isSubtypeOf(String superType) throws CoreException {
        return false;
      }

      @Override
      public void setAttribute(String attributeName, int value) throws CoreException {}

      @Override
      public void setAttribute(String attributeName, Object value) throws CoreException {}

      @Override
      public void setAttribute(String attributeName, boolean value) throws CoreException {}

      @Override
      public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {}

      @Override
      public void setAttributes(Map<String, ? extends Object> attributes) throws CoreException {}

    };
  }

  @Override
  public void eraseMarkers() {}

}
