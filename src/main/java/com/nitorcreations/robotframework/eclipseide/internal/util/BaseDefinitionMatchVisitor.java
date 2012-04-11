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
package com.nitorcreations.robotframework.eclipseide.internal.util;

import org.eclipse.core.resources.IFile;

public abstract class BaseDefinitionMatchVisitor implements DefinitionMatchVisitor {

    protected final IFile file;

    public BaseDefinitionMatchVisitor(IFile file) {
        this.file = file;
    }

    protected String getFilePrefix(FileWithType location) {
        if (location.getFile() == file) {
            return "";
        }
        String name = getNameWithoutTxtPostfix(location.getName());
        return '[' + name + "] ";
    }

    protected String getNameWithoutTxtPostfix(String name) {
        if (name.toLowerCase().endsWith(".txt")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }
}