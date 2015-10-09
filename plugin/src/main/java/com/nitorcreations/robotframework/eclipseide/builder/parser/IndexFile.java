/**
 * Copyright 2012, 2015 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import static com.nitorcreations.robotframework.eclipseide.internal.util.Reporter.report;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;

public class IndexFile {

    public static List<String> getVariables(FileWithType fileWithType) {
        switch (fileWithType.getType()) {
            case VARIABLE:
                return getVariableFileVariables(fileWithType.getFile());
            case BUILTIN_VARIABLE:
                return getBuiltinVariables(fileWithType);
        }
        return Collections.emptyList();
    }

    public static List<String> getKeywords(FileWithType fileWithType) {
        switch (fileWithType.getType()) {
            case LIBRARY:
                return getLibraryKeywords(fileWithType);
        }
        return Collections.emptyList();
    }

    private static List<String> getVariableFileVariables(IFile file) {
        IFile indexFile = getIndexFileForVariableFile(file);
        return getMatches(indexFile, true);
    }

    private static List<String> getBuiltinVariables(FileWithType fileWithType) {
        IFile indexFile = getIndexFileForLibrary(fileWithType);
        return getMatches(indexFile, true);
    }

    private static List<String> getLibraryKeywords(FileWithType fileWithType) {
        IFile indexFile = getIndexFileForLibrary(fileWithType);
        return getMatches(indexFile, false);
    }

    private static IFile getIndexFileForVariableFile(IFile file) {
        IPath filePath = file.getFullPath();
        IPath indexFilePath = filePath.addFileExtension("index");
        IFile indexFile = file.getWorkspace().getRoot().getFile(indexFilePath);
        return indexFile;
    }

    private static IFile getIndexFileForLibrary(FileWithType fileWithType) {
        IProject project = fileWithType.getProject();
        IPath projectPath = project.getFullPath();
        IPath indexFilePath = projectPath.append("robot-indices/" + fileWithType.getName() + ".index");
        IFile indexFile = project.getWorkspace().getRoot().getFile(indexFilePath);
        return indexFile;
    }

    private static List<String> getMatches(IFile indexFile, boolean wantVariables) {
        if (indexFile == null) {
            return Collections.emptyList();
        }
        if (!indexFile.exists()) {
            if (indexFile.isSynchronized(IFile.DEPTH_ZERO)) {
                report("Warning: index file " + formatForLog(indexFile) + " not found, not able to do proper error checking / hyperlinking / code completion");
            } else {
                report("Warning: index file " + formatForLog(indexFile) + " is out of sync. Please refresh the workspace.");
            }
            return Collections.emptyList();
        }
        List<String> contents = load(indexFile);
        filter(contents, wantVariables);
        return contents;
    }

    private static List<String> load(IFile indexFile) {
        InputStream stream = null;
        try {
            stream = indexFile.getContents(true);
            Reader r = new InputStreamReader(stream, "UTF-8");
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(r);
            List<String> lines = new ArrayList<String>();
            String line;
            while (null != (line = br.readLine())) {
                lines.add(line);
            }
            return lines;
        } catch (CoreException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                report("Workspace out of sync for file " + formatForLog(indexFile) + " - it no longer exists in the file system. Please refresh the workspace.");
            } else {
                e.printStackTrace();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static void filter(List<String> contents, boolean wantVariables) {
        Iterator<String> it = contents.iterator();
        while (it.hasNext()) {
            String l = it.next();
            if (l.isEmpty()) {
                continue;
            }
            boolean isVariable = false;
            switch (l.charAt(0)) {
                case '#':
                    continue;
                case '$':
                case '@':
                    isVariable = true;
                    break;
            }
            if (isVariable != wantVariables) {
                it.remove();
            }
        }
    }

    private static String formatForLog(IResource resource) {
        if (resource == null) {
            return null;
        }
        IPath location = resource.getLocation();
        if (location == null) {
            return resource.toString();
        }
        return location.toOSString() + " (" + resource + ')';
    }

}
