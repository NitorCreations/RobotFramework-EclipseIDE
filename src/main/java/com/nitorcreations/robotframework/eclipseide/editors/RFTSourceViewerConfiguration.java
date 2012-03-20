/**
 * Copyright 2012 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.KeywordCallHyperlinkDetector;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.ResourceHyperlinkDetector;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.VariableAccessHyperlinkDetector;

public class RFTSourceViewerConfiguration extends SourceViewerConfiguration {

    private final ColorManager colorManager;

    public RFTSourceViewerConfiguration(ColorManager colorManager) {
        this.colorManager = colorManager;

    }

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
        detectors.add(new ResourceHyperlinkDetector());
        detectors.add(new KeywordCallHyperlinkDetector());
        detectors.add(new VariableAccessHyperlinkDetector());
        return detectors.toArray(new IHyperlinkDetector[detectors.size()]);
    }

    /**
     * This handles the syntax coloring of the code.
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        return new RFTPresentationReconciler(colorManager);
    }

}
