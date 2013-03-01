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