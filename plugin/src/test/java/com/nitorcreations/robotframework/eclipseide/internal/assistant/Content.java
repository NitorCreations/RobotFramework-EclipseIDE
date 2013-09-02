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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString.ArgumentType;

/**
 * This class accepts text annotated with &lt;text&gt; which results in a pointer with the name "text" to be remembered
 * at the specfieid point.
 */
public class Content {
    private static final Pattern POINTER_RE = Pattern.compile("<([^>]+)>");
    private final Map<String, Integer> pointers = new LinkedHashMap<String, Integer>();
    private final String content;

    public Content(String contentWithPointers) {
        Matcher m = POINTER_RE.matcher(contentWithPointers);
        StringBuffer sb = new StringBuffer();
        pointers.put("start", 0);
        while (m.find()) {
            m.appendReplacement(sb, "");
            String pointerName = m.group(1);
            int pointerTarget = sb.length();
            Integer old = pointers.put(pointerName, pointerTarget);
            if (old != null) {
                throw new IllegalStateException("Duplicate pointer " + pointerName);
            }
        }
        pointers.put("end", sb.length());
        m.appendTail(sb);
        content = sb.toString();
    }

    public int o(String pointerName) {
        if (!pointers.containsKey(pointerName)) {
            throw new NoSuchElementException(pointerName);
        }
        return pointers.get(pointerName);
    }

    public int l(String pointerRange) {
        String[] pointers = pointerRange.split("-", 2);
        return o(pointers[1]) - o(pointers[0]);
    }

    public IRegion r(String pointerRange) {
        String[] pointers = pointerRange.split("-", 2);
        int p0 = o(pointers[0]);
        int p1 = o(pointers[1]);
        return new Region(p0, p1 - p0);
    }

    public ParsedString ps(String pointerRange, int argIndex, ArgumentType argType) {
        return ps(r(pointerRange), argIndex, argType);
    }

    public ParsedString ps(IRegion region, int argIndex, ArgumentType argType) {
        return new ParsedString(s(region), region.getOffset(), argIndex).setType(argType);
    }

    public String s(String pointerRange) {
        return s(r(pointerRange));
    }

    public String s(IRegion region) {
        return content.substring(region.getOffset(), region.getOffset() + region.getLength());
    }

    public String c() {
        return content;
    }
}
