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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

public class RobotContentAssistant implements IContentAssistProcessor {

    String[] fgProposals = { "test1", "test2" };

    // ctrl-space completion proposals
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        ICompletionProposal[] result = new ICompletionProposal[fgProposals.length];
        for (int i = 0; i < fgProposals.length; i++) {
            Image image = null;
            String displayString = "displayString " + fgProposals[i];
            String additionalProposalInfo = "additionalProposalInfo " + fgProposals[i];

            String informationDisplayString = "informationDisplayString " + fgProposals[i];

            String replacementString = "replacementString " + fgProposals[i];
            int replacementOffset = documentOffset;
            int replacementLength = i;
            int cursorPosition = replacementString.length() - i;

            IContextInformation info = new ContextInformation(null, informationDisplayString);
            result[i] = new CompletionProposal(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, info, additionalProposalInfo);
        }
        return result;
    }

    // ctrl-shift-space information popups
    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
        IContextInformation[] result = new IContextInformation[5];
        for (int i = 0; i < result.length; i++) {
            String contextDisplayString = "contextDisplayString " + i;
            String informationDisplayString = "informationDisplayString " + i;
            result[i] = new ContextInformation(contextDisplayString, informationDisplayString);
        }
        return result;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new IContextInformationValidator() {

            private IContextInformation info;
            private ITextViewer viewer;
            private int offset;

            @Override
            public void install(IContextInformation info, ITextViewer viewer, int offset) {
                this.info = info;
                this.viewer = viewer;
                this.offset = offset;
            }

            @Override
            public boolean isContextInformationValid(int offset) {
                // TODO return false when cursor goes out of context for the
                // IContextInformation given to install()
                // see ContextInformationValidator.isContextInformationValid()

                // the user can always close a shown IContextInformation
                // instance by hitting Esc or moving the focus out of eclipse

                // if the previous IContextInformation is not closed before the
                // next is shown, it is temporarily hidden until the next one is
                // closed. This might confuse the user.
                return true;
            }

        };
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

}
