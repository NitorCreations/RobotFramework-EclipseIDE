/**
 * Copyright 2013 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class VisitorInfo {
    public final AttemptVisitor visitior;
    public final ParsedString visitorArgument;

    public VisitorInfo(ParsedString visitorArgument, AttemptVisitor visitor) {
        if (visitorArgument == null || visitor == null) {
            throw new IllegalArgumentException("visitorArgument=" + visitorArgument + " visitor=" + visitor);
        }
        this.visitior = visitor;
        this.visitorArgument = visitorArgument;
    }

    @Override
    public String toString() {
        return "VisitorInfo [visitior=" + visitior + ", visitorArgument=" + visitorArgument + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((visitior == null) ? 0 : visitior.hashCode());
        result = prime * result + ((visitorArgument == null) ? 0 : visitorArgument.hashCode());
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
        VisitorInfo other = (VisitorInfo) obj;
        if (visitior == null) {
            if (other.visitior != null)
                return false;
        } else if (!visitior.equals(other.visitior))
            return false;
        if (visitorArgument == null) {
            if (other.visitorArgument != null)
                return false;
        } else if (!visitorArgument.equals(other.visitorArgument))
            return false;
        return true;
    }
}
