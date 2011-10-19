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
package org.otherone.robotframework.eclipse.editor.editors;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ResourceManager {

  private static final Map<File, Resource> resources = new HashMap<File, Resource>();

  public static Resource getResource(File path) {
    try {
      Resource r;
      synchronized (ResourceManager.class) {
        File canonicalPath = path.getCanonicalFile();
        r = resources.get(canonicalPath);
        if (r == null) {
          r = new Resource(canonicalPath);
          r.loadFromDisk();
          resources.put(canonicalPath, r);
        }
      }
      return r;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
