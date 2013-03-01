package com.nitorcreations.robotframework.eclipseide.editors.outline;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class ParsedStringEntry {
    final RootCategoryEntry parent;
    final ParsedString parsedString;

    public ParsedStringEntry(RootCategoryEntry parent, ParsedString parsedString) {
        this.parent = parent;
        this.parsedString = parsedString;
    }

    public RootCategoryEntry getParent() {
        assert parent != null;
        return parent;
    }

    @Override
    public String toString() {
        return parsedString.getValue();
    }
}