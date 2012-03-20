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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;

public class RobotFile {

    static class FileInfo {
        RobotFile onDisk;
        RobotFile inEditor;
    }

    private static final Map<IFile, FileInfo> FILES = Collections.synchronizedMap(new HashMap<IFile, FileInfo>());

    private final List<RFELine> lines;

    private RobotFile(List<RFELine> lines) {
        this.lines = lines;
    }

    public List<RFELine> getLines() {
        return lines;
    }

    public static RobotFile get(IDocument document) {
        return get(document, true);
    }

    public static RobotFile parse(IDocument document) {
        return get(document, false);
    }

    private static RobotFile get(IDocument document, boolean useCached) {
        IFile file = ResourceManager.resolveFileFor(document);
        FileInfo fileInfo = FILES.get(file);
        if (useCached && fileInfo != null && fileInfo.inEditor != null) {
            return fileInfo.inEditor;
        }
        RobotFile parsed = parse(new RFELexer(document));
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            FILES.put(file, fileInfo);
        }
        fileInfo.inEditor = parsed;
        return parsed;
    }

    public static RobotFile parse(String fileContents) {
        try {
            return parse(new RFELexer(fileContents));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new RobotFile(Collections.<RFELine> emptyList());
    }

    private static RobotFile parse(RFELexer lexer) {
        try {
            List<RFELine> lines = lexer.lex();
            new RFEPreParser(null, lines).preParse();
            ArgumentPreParser app = new ArgumentPreParser();
            app.setRange(lines);
            app.parseAll();
            return new RobotFile(lines);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return new RobotFile(Collections.<RFELine> emptyList());
    }
}
