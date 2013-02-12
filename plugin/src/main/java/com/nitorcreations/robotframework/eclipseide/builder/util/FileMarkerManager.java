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
package com.nitorcreations.robotframework.eclipseide.builder.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.nitorcreations.robotframework.eclipseide.builder.RobotBuilder;

public class FileMarkerManager implements MarkerManager {
    private final IFile file;

    public FileMarkerManager(IFile file) {
        this.file = file;
    }

    @Override
    public IMarker createMarker(String type) throws CoreException {
        return file.createMarker(type);
    }

    @Override
    public void eraseMarkers() {
        try {
            file.deleteMarkers(RobotBuilder.MARKER_TYPE, false, IResource.DEPTH_ZERO);
        } catch (CoreException ce) {}
    }
}