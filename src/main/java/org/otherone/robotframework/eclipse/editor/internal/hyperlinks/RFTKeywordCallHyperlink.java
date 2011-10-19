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
package org.otherone.robotframework.eclipse.editor.internal.hyperlinks;

import java.io.File;

import org.eclipse.jface.text.IRegion;
import org.otherone.robotframework.eclipse.editor.editors.ResourceManager;

public class RFTKeywordCallHyperlink extends RFTHyperlink {

  private final String keyword;
  private final File currentFile;

  public RFTKeywordCallHyperlink(IRegion region, String linkText, String keyword, File currentFile) {
    super(region, linkText);
    this.keyword = keyword;
    this.currentFile = currentFile;
  }

  @Override
  public void open() {
    System.out.println("Open hyperlink RFT Keyword Call '" + text + "'");
    openLocation(ResourceManager.getResource(currentFile).findKeyword(keyword));
  }

}
