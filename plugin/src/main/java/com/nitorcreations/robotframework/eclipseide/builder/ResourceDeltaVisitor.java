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
package com.nitorcreations.robotframework.eclipseide.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;

public class ResourceDeltaVisitor extends BaseResourceVisitor implements IResourceDeltaVisitor {

    public ResourceDeltaVisitor(RobotBuilder robotBuilder, IProgressMonitor monitor) {
        super(robotBuilder, monitor);
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        switch (delta.getKind()) {
        case IResourceDelta.ADDED:
            // handle added resource
            this.robotBuilder.parse(visitedFiles, resource, monitor);
            break;
        case IResourceDelta.REMOVED:
            // handle removed resource
            if (resource instanceof IFile) {
                RobotFile.erase((IFile) resource);
            }
            break;
        case IResourceDelta.CHANGED:
            // handle changed resource
            this.robotBuilder.parse(visitedFiles, resource, monitor);
            break;
        }
        // return true to continue visiting children.
        return true;
    }

}