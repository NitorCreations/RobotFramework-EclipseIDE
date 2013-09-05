/**
 * Copyright 2012-2013 Nitor Creations Oy
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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

import com.nitorcreations.robotframework.eclipseide.PluginContext;

public class RobotFile {

    private static final Map<IFile, FileInfo> FILES = Collections.synchronizedMap(new HashMap<IFile, FileInfo>());

    private final List<RobotLine> lines;

    private RobotFile(List<RobotLine> lines) {
        this.lines = lines;
    }

    public List<RobotLine> getLines() {
        return lines;
    }

    public static Collection<IFile> getAllFiles() {
        // TODO we need to restore this list on plugin startup. The parsed
        // contents need not be restored.
        synchronized (FILES) {
            return new ArrayList<IFile>(FILES.keySet());
        }
    }

    public static RobotFile get(IDocument document) {
        return get(document, true);
    }

    public static RobotFile get(IFile file, boolean useEditorVersion) {
        return get(file, useEditorVersion, true, new NullProgressMonitor());
    }

    public static RobotFile parse(IDocument document) {
        return get(document, false);
    }

    public static RobotFile parse(IFile file, IProgressMonitor monitor) {
        return get(file, false, false, monitor);
    }

    public static void erase(IFile file) {
        FileInfo fileInfo = FILES.get(file);
        if (fileInfo != null) {
            fileInfo.onDisk = null;
            if (fileInfo.isEmpty()) {
                FILES.remove(file);
            }
        }
    }

    public static void erase(IDocument document) {
        IFile file = PluginContext.getResourceManager().resolveFileFor(document);
        FileInfo fileInfo = FILES.get(file);
        if (fileInfo != null) {
            fileInfo.inEditor = null;
            if (fileInfo.isEmpty()) {
                FILES.remove(file);
            }
        }
    }

    private static RobotFile get(IFile file, boolean useEditorVersion, boolean useCached, IProgressMonitor monitor) {
        if (useEditorVersion) {
            IDocument document = PluginContext.getResourceManager().resolveDocumentFor(file);
            if (document != null) {
                return get(document, useCached);
            }
        }
        if (!file.exists()) {
            return null;
        }
        FileInfo fileInfo = FILES.get(file);
        if (useCached && fileInfo != null && fileInfo.onDisk != null) {
            return fileInfo.onDisk;
        }
        RobotFile parsed;
        try {
            parsed = parse(file.toString(), new Lexer(file, monitor));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            FILES.put(file, fileInfo);
        }
        fileInfo.onDisk = parsed;
        return parsed;
    }

    private static RobotFile get(IDocument document, boolean useCached) {
        IFile file = PluginContext.getResourceManager().resolveFileFor(document);
        FileInfo fileInfo = FILES.get(file);
        if (useCached && fileInfo != null && fileInfo.inEditor != null) {
            return fileInfo.inEditor;
        }
        RobotFile parsed = parse(file.toString(), new Lexer(document));
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            FILES.put(file, fileInfo);
        }
        fileInfo.inEditor = parsed;
        return parsed;
    }

    public static RobotFile parse(String fileContents) {
        try {
            return parse("<in-memory file>", new Lexer(fileContents));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new RobotFile(Collections.<RobotLine> emptyList());
    }

    private static RobotFile parse(String filename, Lexer lexer) {
        try {
            List<RobotLine> lines = lexer.lex(); // split input into lines & arguments
            new PreParser(filename, lines).preParse(); // determine line types
            ArgumentPreParser app = new ArgumentPreParser();
            app.setRange(lines);
            app.parseAll(); // determine argument types
            return new RobotFile(lines);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return new RobotFile(Collections.<RobotLine> emptyList());
    }
}
