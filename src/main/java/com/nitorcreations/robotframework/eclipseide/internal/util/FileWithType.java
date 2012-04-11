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
import org.eclipse.core.resources.IProject;

public class FileWithType {
    public enum Type {
        RESOURCE, LIBRARY, VARIABLE
    }

    private final Type type;
    private final IFile file;
    private final String name;
    private final IProject project;

    public FileWithType(FileWithType.Type type, IFile file) {
        if (type == null || file == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.file = file;
        this.name = getNameWithoutPostfix(file.getName());
        this.project = file.getProject();
    }

    public FileWithType(FileWithType.Type type, String name, IProject project) {
        if (type == null || name == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.file = null;
        this.name = name;
        this.project = project;
    }

    private String getNameWithoutPostfix(String name) {
        int dotLocation = name.indexOf(".");
        if (dotLocation == -1) {
            return name;
        }
        return name.substring(0, dotLocation);
    }

    public Type getType() {
        return type;
    }

    public IFile getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public IProject getProject() {
        return project;
    }

    public boolean isRobotFile() {
        return type == Type.RESOURCE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileWithType other = (FileWithType) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FileWithType [type=" + type + (file != null ? ", file=" + file : ", name=" + name) + "]";
    }

}
