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

import java.io.IOException;
import java.io.Reader;

/**
 * Tested indirectly through {@link TestLexer}.
 */
public class CountingLineReader {

    private final Reader reader;

    private int charsConsumed;

    private boolean eof;

    private final StringBuilder sb = new StringBuilder();

    public CountingLineReader(Reader reader) {
        this.reader = reader;
    }

    public void close() throws IOException {
        reader.close();
    }

    public int getCharPos() {
        return charsConsumed;
    }

    public String readLine() throws IOException {
        if (eof) {
            return null;
        }
        int lf;
        do {
            lf = sb.indexOf("\n");
        } while (lf == -1 && fill());
        String ret;
        if (lf == -1) {
            eof = true;
            ret = sb.toString();
            sb.setLength(0);
            charsConsumed += ret.length();
        } else {
            ret = sb.substring(0, lf);
            sb.delete(0, lf + 1);
            charsConsumed += lf + 1;
        }
        return ret.replace("\r", "");
    }

    private boolean fill() throws IOException {
        char[] buf = new char[1024];
        int read = reader.read(buf);
        if (read == -1) {
            return false;
        }
        sb.append(buf, 0, read);
        return true;
    }

}
