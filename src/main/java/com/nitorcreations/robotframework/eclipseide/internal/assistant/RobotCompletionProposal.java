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

import com.nitorcreations.robotframework.eclipseide.internal.util.FileWithType;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

class RobotCompletionProposal implements ICompletionProposal, ICompletionProposalExtension6 {

    private final ParsedString matchArgument;
    private final FileWithType matchLocation;
    private final IRegion replacementRegion;
    private final Image image;
    private final String displayString;
    private final String informationDisplayString;
    private final String additionalProposalInfo;
    private boolean prefixRequired = false;

    public RobotCompletionProposal(ParsedString matchArgument, FileWithType proposalLocation, IRegion replacementRegion, Image image, String displayString, String informationDisplayString, String additionalProposalInfo) {
        this.matchArgument = matchArgument;
        this.matchLocation = proposalLocation;
        this.replacementRegion = replacementRegion;
        this.image = image;
        this.displayString = displayString;
        this.informationDisplayString = informationDisplayString;
        this.additionalProposalInfo = additionalProposalInfo;
    }

    ParsedString getMatchArgument() {
        return matchArgument;
    }

    FileWithType getMatchLocation() {
        return matchLocation;
    }

    @Override
    public void apply(IDocument document) {
        try {
            document.replace(replacementRegion.getOffset(), replacementRegion.getLength(), getReplacementString());
        } catch (BadLocationException x) {
            // ignore
        }
    }

    private String getReplacementString() {
        if (prefixRequired) {
            return matchLocation.getName() + "." + matchArgument.getValue();
        }
        return matchArgument.getValue();
    }

    @Override
    public Point getSelection(IDocument document) {
        return new Point(replacementRegion.getOffset() + getReplacementString().length(), 0);
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

    public void setPrefixRequired() {
        prefixRequired = true;
    }
}
