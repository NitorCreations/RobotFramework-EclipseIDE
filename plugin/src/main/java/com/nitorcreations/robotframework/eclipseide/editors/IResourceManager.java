/**
 * Copyright 2012, 2014 Nitor Creations Oy, SmallGreenET
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
package com.nitorcreations.robotframework.eclipseide.editors;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

public interface IResourceManager {

    IEditorPart openOrReuseEditorFor(IFile file, boolean isRobotFile);

    IFile getRelativeFile(IFile originalFile, String pathRelativeToOriginalFile);

    Map<IFile, IPath> getJavaFiles(String fullyQualifiedName);

    IDocument resolveDocumentFor(IFile file);

    IFile resolveFileFor(IDocument document);

    void unregisterEditor(RobotFrameworkTextfileEditor editor);

    void registerEditor(RobotFrameworkTextfileEditor editor);

    Resource getResource(File path);

}
