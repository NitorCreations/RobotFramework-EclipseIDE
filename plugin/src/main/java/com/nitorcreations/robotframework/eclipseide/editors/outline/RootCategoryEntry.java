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
package com.nitorcreations.robotframework.eclipseide.editors.outline;

import java.util.HashSet;
import java.util.Set;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class RootCategoryEntry {
    final Object input;
    final String category;
    final Set<ParsedStringEntry> entries;

    public RootCategoryEntry(Object input, String category, Set<ParsedString> parsedStrings) {
        this.input = input;
        this.category = category;
        this.entries = new HashSet<ParsedStringEntry>(parsedStrings.size());
        for (ParsedString parsedString : parsedStrings) {
            ParsedStringEntry entry = new ParsedStringEntry(this, parsedString);
            entries.add(entry);
        }
    }

    public Object getInput() {
        return input;
    }

    public Set<ParsedStringEntry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return category;
    }
}