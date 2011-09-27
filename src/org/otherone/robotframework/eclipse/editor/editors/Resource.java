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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A representation of a robot framework file, which is here called a "resource".
 * 
 * @author xkr47
 */
public class Resource {

  /**
   * The resource.
   */
  public final File resourceFile;

  /**
   * The list of included resource files.
   */
  public final List<File> includedResources = new ArrayList<File>();

  /**
   * Map of defined variables, mapping to line numbers.
   */
  public final Map<String, Integer> definedVariables = new LinkedHashMap<String, Integer>();

  /**
   * Map of defined keywords, mapping to line numbers.
   */
  public final Map<String, Integer> definedKeywords = new LinkedHashMap<String, Integer>();

  public Resource(File resourceFile) {
    this.resourceFile = resourceFile;
  }

  public ResourceLocation findVariable(String name) {
    Integer line = definedVariables.get(name);
    if (line != null) {
      return new ResourceLocation(this, line);
    }
    for (File f : includedResources) {
      Resource r = ResourceManager.getResource(f);
      if (r == null) {
        continue;
      }
      ResourceLocation l = r.findVariable(name);
      if (l != null) {
        return l;
      }
    }
    return null;
  }

  public ResourceLocation findKeyword(String name) {
    Integer line = definedKeywords.get(name);
    if (line != null) {
      return new ResourceLocation(this, line);
    }
    for (File f : includedResources) {
      Resource r = ResourceManager.getResource(f);
      if (r == null) {
        continue;
      }
      ResourceLocation l = r.findKeyword(name);
      if (l != null) {
        return l;
      }
    }
    return null;
  }

  public void loadFromDisk() {
    // TODO
  }

}
