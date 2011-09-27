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

import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.otherone.robotframework.eclipse.editor.internal.coloring.RFTActionColoringScanner;
import org.otherone.robotframework.eclipse.editor.internal.coloring.RFTCommentColoringScanner;
import org.otherone.robotframework.eclipse.editor.internal.coloring.RFTKeywordColoringScanner;
import org.otherone.robotframework.eclipse.editor.internal.coloring.RFTTableColoringScanner;
import org.otherone.robotframework.eclipse.editor.internal.coloring.RFTVariableColoringScanner;

public class RFTPresentationReconciler extends PresentationReconciler {

  private Map<Class<? extends ITokenScanner>, ITokenScanner> coloringScanners;
  private ColorManager colorManager;

  public RFTPresentationReconciler(Map<Class<? extends ITokenScanner>, ITokenScanner> coloringScanners, ColorManager colorManager) {
    this.coloringScanners = coloringScanners;
    this.colorManager = colorManager;
    // TODO setDocumentPartitioning(partitioning);
    addColoringScanner(RFTPartitionScanner.RFT_TABLE, RFTTableColoringScanner.class);
    addColoringScanner(RFTPartitionScanner.RFT_COMMENT, RFTCommentColoringScanner.class);
    addColoringScanner(RFTPartitionScanner.RFT_VARIABLE, RFTVariableColoringScanner.class);
    addColoringScanner(RFTPartitionScanner.RFT_KEYWORD, RFTKeywordColoringScanner.class);
    addColoringScanner(RFTPartitionScanner.RFT_ACTION, RFTActionColoringScanner.class);
    addColoringScanner(IDocument.DEFAULT_CONTENT_TYPE, RFTKeywordColoringScanner.class);

    // NonRuleBasedDamagerRepairer ndr =
    // new NonRuleBasedDamagerRepairer(
    // new TextAttribute(
    // colorManager.getColor(IRFTColorConstants.RFT_COMMENT)));
    // reconciler.setDamager(ndr, RFTPartitionScanner.RFT_COMMENT);
    // reconciler.setRepairer(ndr, RFTPartitionScanner.RFT_COMMENT);

    this.coloringScanners = null;
    this.colorManager = null;
  }

  private void addColoringScanner(String partitionToken, Class<? extends ITokenScanner> coloringScannerClass) {
    ITokenScanner coloringScanner = coloringScanners.get(coloringScannerClass);
    if (coloringScanner == null) {
      try {
        coloringScanner = coloringScannerClass.getConstructor(ColorManager.class).newInstance(colorManager);
      } catch (Exception e) {
        throw new RuntimeException("Failed to construct coloring scanner " + coloringScannerClass, e);
      }
      coloringScanners.put(coloringScannerClass, coloringScanner);
    }
    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(coloringScanner);
    setDamager(dr, partitionToken);
    setRepairer(dr, partitionToken);
  }

}
