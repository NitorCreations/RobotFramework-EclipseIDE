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

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.otherone.robotframework.eclipse.editor.Activator;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFEParser;

public class RFEBuilder extends IncrementalProjectBuilder {

  class ResourceVisitor implements IResourceVisitor {

    private final IProgressMonitor monitor;

    public ResourceVisitor(IProgressMonitor monitor) {
      this.monitor = monitor;
    }

    @Override
    public boolean visit(IResource resource) {
      validate(resource, monitor);
      // return true to continue visiting children.
      return true;
    }

  }

  class ResourceDeltaVisitor implements IResourceDeltaVisitor {

    private final IProgressMonitor monitor;

    public ResourceDeltaVisitor(IProgressMonitor monitor) {
      this.monitor = monitor;
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
      IResource resource = delta.getResource();
      switch (delta.getKind()) {
        case IResourceDelta.ADDED:
          // handle added resource
          validate(resource, monitor);
          break;
        case IResourceDelta.REMOVED:
          // handle removed resource
          break;
        case IResourceDelta.CHANGED:
          // handle changed resource
          validate(resource, monitor);
          break;
      }
      // return true to continue visiting children.
      return true;
    }

  }

  public static final String BUILDER_ID = Activator.PLUGIN_ID + ".rfeBuilder";

  public static final String MARKER_TYPE = Activator.PLUGIN_ID + ".testProblem";

  @Override
  protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
    if (kind == FULL_BUILD) {
      fullBuild(monitor);
    } else {
      IResourceDelta delta = getDelta(getProject());
      if (delta == null) {
        fullBuild(monitor);
      } else {
        incrementalBuild(delta, monitor);
      }
    }
    return null;
  }

  private void deleteMarkers(IFile file) {
    try {
      file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
    } catch (CoreException ce) {
    }
  }

  protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
    getProject().accept(new ResourceVisitor(monitor));
  }

  protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
    delta.accept(new ResourceDeltaVisitor(monitor));
  }

  void validate(IResource resource, IProgressMonitor monitor) {
    if (resource instanceof IFile && resource.getName().endsWith(".txt")) {
      IFile file = (IFile) resource;
      deleteMarkers(file);
      try {
        new RFEParser(file, monitor).parse();
      } catch (Exception e1) {
        // new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Internal error", e1)
        throw new RuntimeException("Validation problem", e1);
      }
    }
  }
}
