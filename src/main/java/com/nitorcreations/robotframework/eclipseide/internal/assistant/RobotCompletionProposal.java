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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

class RobotCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6 {

    private final ParsedString matchArgument;
    private final IFile matchLocation;
    private final String replacementString;
    private final IRegion replacementRegion;
    private final int cursorPosition;
    private final Image image;
    private final String displayString;
    private final String informationDisplayString;
    private final String additionalProposalInfo;

    public RobotCompletionProposal(ParsedString matchArgument, IFile matchLocation, String replacementString, IRegion replacementRegion, int cursorPosition, Image image, String displayString, String informationDisplayString, String additionalProposalInfo) {
        this.matchArgument = matchArgument;
        this.matchLocation = matchLocation;
        this.replacementString = replacementString;
        this.replacementRegion = replacementRegion;
        this.cursorPosition = cursorPosition;
        this.image = image;
        this.displayString = displayString;
        this.informationDisplayString = informationDisplayString;
        this.additionalProposalInfo = additionalProposalInfo;
    }

    public ParsedString getMatchArgument() {
        return matchArgument;
    }

    public IFile getMatchLocation() {
        return matchLocation;
    }

    @Override
    public void apply(IDocument document) {
        try {
            document.replace(replacementRegion.getOffset(), replacementRegion.getLength(), replacementString);
        } catch (BadLocationException x) {
            // ignore
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        return new Point(replacementRegion.getOffset() + cursorPosition, 0);
    }

    @Override
    public String getAdditionalProposalInfo() {
        return additionalProposalInfo;
    }

    @Override
    public String getDisplayString() {
        return getStyledDisplayString().toString();
    }

    @Override
    public StyledString getStyledDisplayString() {
        StyledString ss = new StyledString();
        ss.append(displayString);
        // TODO use styles to improve readability
        return ss;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public IContextInformation getContextInformation() {
        return new ContextInformation(null, informationDisplayString);
    }

}
