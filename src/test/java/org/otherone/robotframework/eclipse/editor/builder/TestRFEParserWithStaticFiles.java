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

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFELexer;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFELexer.LexLine;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFEParser;
import org.otherone.robotframework.eclipse.editor.builder.parser.RFEParser.MarkerManager;

@RunWith(Parameterized.class)
public class TestRFEParserWithStaticFiles {

  public static final class NullMarkerParser implements MarkerManager {

    @Override
    public IMarker createMarker(String type) throws CoreException {
      System.out.println("  New marker");
      InvocationHandler handler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          if (method.getName().equals("setAttribute")) {
            System.out.println("    " + args[0] + " = \"" + args[1] + '"');
          }
          return null;
        }
      };
      return (IMarker) Proxy.newProxyInstance(NullMarkerParser.class.getClassLoader(), new Class[] { IMarker.class }, handler);
    }

    @Override
    public void eraseMarkers() {
      // when testing partial parsing perhaps
    }
  }

  @Parameters
  public static List<Object[]> files() {
    File dir = new File("src/test/resources/RFEParser");
    List<Object[]> files = new ArrayList<Object[]>();
    for (File entry : dir.listFiles()) {
      if (entry.isFile() && entry.getName().endsWith(".txt")) {
        files.add(new Object[] { entry });
      }
    }
    return files;
  }

  private final File file;

  public TestRFEParserWithStaticFiles(File file) {
    this.file = file;
  }

  @Test
  public void testFile() throws Exception {
    List<LexLine> lines = new RFELexer(file, "UTF-8").lex();
    new RFEParser(file, lines, new NullMarkerParser()).parse();
  }

}
