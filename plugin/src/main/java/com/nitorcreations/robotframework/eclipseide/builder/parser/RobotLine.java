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

import java.util.List;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

public class RobotLine {
    public final int lineNo;
    public final int lineCharPos;
    public final List<ParsedString> arguments;
    public LineType type;

    public RobotLine(int lineNo, int lineCharPos, List<ParsedString> arguments) {
        this.lineNo = lineNo;
        this.lineCharPos = lineCharPos;
        this.arguments = arguments;
    }

    public boolean isType(LineType type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        return "<#" + lineNo + " " + type + "> " + arguments;
    }

    public ParsedString getArgumentAt(int offset) {
        if (offset < lineCharPos) { // TODO verify upper boundary too
            throw new IndexOutOfBoundsException(offset + "<" + lineCharPos);
        }
        for (ParsedString argument : arguments) {
            if (offset >= argument.getArgCharPos() && offset <= argument.getExtendedArgEndCharPos()) {
                return argument;
            }
        }
        return null;
    }

    public boolean isResourceSetting() {
        return isSettingFor("Resource");
    }

    public boolean isVariableSetting() {
        return isSettingFor("Variables");
    }

    public boolean isLibrarySetting() {
        return isSettingFor("Library");
    }

    private boolean isSettingFor(String setting) {
        if (!isType(LineType.SETTING_TABLE_LINE)) {
            return false;
        }
        if (arguments.size() < 2) {
            return false;
        }
        ParsedString firstArgument = arguments.get(0);
        if (firstArgument.getType() != ArgumentType.SETTING_KEY) {
            return false;
        }
        return firstArgument.getValue().equals(setting);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result + lineCharPos;
        result = prime * result + lineNo;
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
        RobotLine other = (RobotLine) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (lineCharPos != other.lineCharPos)
            return false;
        if (lineNo != other.lineNo)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
