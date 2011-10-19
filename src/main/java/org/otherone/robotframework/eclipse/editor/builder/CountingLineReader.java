/**
 * Copyright 2011 Nitor Creations Oy
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
package org.otherone.robotframework.eclipse.editor.builder;

import java.io.IOException;
import java.io.Reader;

public class CountingLineReader {

  private final Reader reader;

  private int charsConsumed;

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
    int lf;
    do {
      lf = sb.indexOf("\n");
    } while (lf == -1 && fill());
    String ret;
    if (lf == -1) {
      ret = sb.toString();
      if (ret.length() == 0) {
        ret = null;
      }
      sb.setLength(0);
      charsConsumed += lf;
    } else {
      ret = sb.substring(0, lf);
      sb.delete(0, lf + 1);
      charsConsumed += lf + 1;
    }
    return ret;
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
