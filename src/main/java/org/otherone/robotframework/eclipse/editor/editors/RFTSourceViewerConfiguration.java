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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.otherone.robotframework.eclipse.editor.internal.hyperlinks.RFTKeywordCallHyperlinkDetector;
import org.otherone.robotframework.eclipse.editor.internal.hyperlinks.RFTResourceHyperlinkDetector;
import org.otherone.robotframework.eclipse.editor.internal.hyperlinks.RFTVariableAccessHyperlinkDetector;

public class RFTSourceViewerConfiguration extends SourceViewerConfiguration {

  private final ColorManager colorManager;

  private final Map<Class<? extends ITokenScanner>, ITokenScanner> coloringScanners = new HashMap<Class<? extends ITokenScanner>, ITokenScanner>();

  public RFTSourceViewerConfiguration(ColorManager colorManager) {
    this.colorManager = colorManager;
  }

  // @Override
  // public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
  // return RFTPartitionScanner.getContentTypes();
  // }

  // public ITextDoubleClickStrategy getDoubleClickStrategy(
  // ISourceViewer sourceViewer,
  // String contentType) {
  // if (doubleClickStrategy == null)
  // doubleClickStrategy = new RFTDoubleClickStrategy();
  // return doubleClickStrategy;
  // }

  // @Override
  // public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
  // return super.getAnnotationHover(sourceViewer);
  // }

  /**
   * This creates links that can be followed by ctrl-mouseclick.
   */
  @Override
  public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
    List<IHyperlinkDetector> detectors = new ArrayList<IHyperlinkDetector>();
    detectors.addAll(Arrays.asList(super.getHyperlinkDetectors(sourceViewer)));
    detectors.add(new RFTResourceHyperlinkDetector());
    detectors.add(new RFTKeywordCallHyperlinkDetector());
    detectors.add(new RFTVariableAccessHyperlinkDetector());
    return detectors.toArray(new IHyperlinkDetector[detectors.size()]);
  }

  /**
   * This handles the syntax coloring of the code.
   */
  @Override
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    return new RFTPresentationReconciler(coloringScanners, colorManager);
  }

}
