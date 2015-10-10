/**
 * Copyright 2012-2014 Nitor Creations Oy, SmallGreenET
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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import com.nitorcreations.robotframework.eclipseide.internal.assistant.RobotContentAssistant;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.RobotContentAssistant2;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.VariableReplacementRegionCalculator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.AttemptGenerator;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.ProposalGeneratorFactory;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.ProposalSuitabilityDeterminer;
import com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator.RelevantProposalsFilter;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.KeywordCallHyperlinkDetector;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.LibraryHyperlinkDetector;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.ResourceHyperlinkDetector;
import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.VariableAccessHyperlinkDetector;

public class RobotSourceViewerConfiguration extends TextSourceViewerConfiguration {

    private final ColorManager colorManager;

    public RobotSourceViewerConfiguration(ColorManager colorManager, IPreferenceStore prefStore) {
        super(prefStore);
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
        detectors.add(new LibraryHyperlinkDetector());
        return detectors.toArray(new IHyperlinkDetector[detectors.size()]);
    }

    /**
     * This handles the syntax coloring of the code.
     */
    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        return new RobotPresentationReconciler(colorManager);
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant = new ContentAssistant();

        assistant.enableAutoInsert(true);
        assistant.enablePrefixCompletion(true);

        // this enables RobotContentAssistant.getCompletionProposalAutoActivationCharacters()
        assistant.enableAutoActivation(true);

        // assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
        // assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        // assistant.setStatusLineVisible(true);

        ProposalSuitabilityDeterminer proposalSuitabilityDeterminer = new ProposalSuitabilityDeterminer(new ProposalGeneratorFactory(), new VariableReplacementRegionCalculator());
        RobotContentAssistant2 assistant2 = new RobotContentAssistant2(proposalSuitabilityDeterminer, new AttemptGenerator(), new RelevantProposalsFilter());
        assistant.setContentAssistProcessor(new RobotContentAssistant(assistant2), IDocument.DEFAULT_CONTENT_TYPE);

        assistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {
            @Override
            protected IInformationControl doCreateInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, true);
            }
        });
        return assistant;
    }

}
