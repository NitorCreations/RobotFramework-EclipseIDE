/**
 * Copyright 2012-2013 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;

public class RobotContentAssistant implements IContentAssistProcessor {

    private final IRobotContentAssistant2 robotContentAssistant2;

    public RobotContentAssistant(IRobotContentAssistant2 robotContentAssistant2) {
        this.robotContentAssistant2 = robotContentAssistant2;
    }

    // ctrl-space completion proposals
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        // find info about current line
        int lineNo;
        IDocument document = viewer.getDocument();
        try {
            lineNo = document.getLineOfOffset(documentOffset);
        } catch (BadLocationException ex) {
            return null;
        }

        List<RobotLine> lines = RobotFile.get(document).getLines();
        IFile file = PluginContext.getResourceManager().resolveFileFor(document);
        String documentText = document.get();

        return robotContentAssistant2.generateProposals(file, documentOffset, documentText, lines, lineNo);
    }

    // ctrl-shift-space information popups
    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
        return null;
        // TODO example code below
        // IContextInformation[] result = new IContextInformation[5];
        // for (int i = 0; i < result.length; i++) {
        // String contextDisplayString = "contextDisplayString " + i;
        // String informationDisplayString = "informationDisplayString " + i;
        // result[i] = new ContextInformation(contextDisplayString, informationDisplayString);
        // }
        // return result;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new IContextInformationValidator() {

            private int offset;

            @Override
            public void install(IContextInformation info, ITextViewer viewer, int offset) {
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
                return this.offset == offset;
            }
        };
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '$', '@' };
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
