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

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * https://robotframework.googlecode.com/hg/doc/userguide/RobotFrameworkUserGuide.html?r=2.6.1
 * http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/text/source/package-summary.html
 *
 * @author xkr47
 */
public class RobotFrameworkTextfileEditor extends TextEditor {

  private ColorManager colorManager;

  public RobotFrameworkTextfileEditor() {
    super();
    colorManager = new ColorManager();
    setSourceViewerConfiguration(new RFTConfiguration(colorManager));
    setDocumentProvider(new RFTDocumentProvider());
  }

  public void dispose() {
    colorManager.dispose();
    super.dispose();
  }

  @Override
  protected void initializeViewerColors(ISourceViewer viewer) {
    super.initializeViewerColors(viewer);
    viewer.getTextWidget().setForeground(colorManager.getColor(IRFTColorConstants.FG));
    viewer.getTextWidget().setBackground(colorManager.getColor(IRFTColorConstants.BG));
    viewer.getTextWidget().setSelectionForeground(colorManager.getColor(IRFTColorConstants.FG_SELECTION));
    viewer.getTextWidget().setSelectionBackground(colorManager.getColor(IRFTColorConstants.BG_SELECTION));
  }

}
