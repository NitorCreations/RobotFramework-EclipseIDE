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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.otherone.robotframework.eclipse.editor.coloring.RFTActionColoringScanner;
import org.otherone.robotframework.eclipse.editor.coloring.RFTCommentColoringScanner;
import org.otherone.robotframework.eclipse.editor.coloring.RFTDefaultColoringScanner;
import org.otherone.robotframework.eclipse.editor.coloring.RFTKeywordColoringScanner;
import org.otherone.robotframework.eclipse.editor.coloring.RFTTableColoringScanner;
import org.otherone.robotframework.eclipse.editor.coloring.RFTVariableColoringScanner;

public class RFTConfiguration extends SourceViewerConfiguration {
  private ColorManager colorManager;
  private ITokenScanner defaultColoringScanner;
  private ITokenScanner tableColoringScanner;

  public RFTConfiguration(ColorManager colorManager) {
    this.colorManager = colorManager;
  }
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return RFTPartitionScanner.getContentTypes();
  }

  //	public ITextDoubleClickStrategy getDoubleClickStrategy(
  //		ISourceViewer sourceViewer,
  //		String contentType) {
  //		if (doubleClickStrategy == null)
  //			doubleClickStrategy = new RFTDoubleClickStrategy();
  //		return doubleClickStrategy;
  //	}

  @Override
  public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
    return super.getAnnotationHover(sourceViewer);
  }

  @Override
  public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
    return super.getHyperlinkDetectors(sourceViewer);
  }

  private ITokenScanner getDefaultColoringScanner() {
    if (defaultColoringScanner == null) {
      defaultColoringScanner = new RFTDefaultColoringScanner(colorManager);
    }
    return defaultColoringScanner;
  }

  private ITokenScanner getTableColoringScanner() {
    if (tableColoringScanner == null) {
      tableColoringScanner = new RFTTableColoringScanner(colorManager);
    }
    return tableColoringScanner;
  }

  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();
    addColoringScanner(reconciler, RFTPartitionScanner.RFT_TABLE, RFTTableColoringScanner.class);
    addColoringScanner(reconciler, RFTPartitionScanner.RFT_COMMENT, RFTCommentColoringScanner.class);
    addColoringScanner(reconciler, RFTPartitionScanner.RFT_VARIABLE, RFTVariableColoringScanner.class);
    addColoringScanner(reconciler, RFTPartitionScanner.RFT_KEYWORD, RFTKeywordColoringScanner.class);
    addColoringScanner(reconciler, RFTPartitionScanner.RFT_ACTION, RFTActionColoringScanner.class);
    addColoringScanner(reconciler, IDocument.DEFAULT_CONTENT_TYPE, RFTKeywordColoringScanner.class);

//    NonRuleBasedDamagerRepairer ndr =
//                        new NonRuleBasedDamagerRepairer(
//                                            new TextAttribute(
//                                                                colorManager.getColor(IRFTColorConstants.RFT_COMMENT)));
//    reconciler.setDamager(ndr, RFTPartitionScanner.RFT_COMMENT);
//    reconciler.setRepairer(ndr, RFTPartitionScanner.RFT_COMMENT);

    return reconciler;
  }
  
  private final Map<Class<? extends ITokenScanner>, ITokenScanner> coloringScanners = new HashMap<Class<? extends ITokenScanner>, ITokenScanner>();

  private void addColoringScanner(PresentationReconciler reconciler, String partitionToken, Class<? extends ITokenScanner> coloringScannerClass) {
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
    reconciler.setDamager(dr, partitionToken);
    reconciler.setRepairer(dr, partitionToken);
  }

}
