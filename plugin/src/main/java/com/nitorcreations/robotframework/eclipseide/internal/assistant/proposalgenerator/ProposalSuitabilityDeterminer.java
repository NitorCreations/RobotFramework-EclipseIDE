/**
 * Copyright 2013 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;

import com.nitorcreations.robotframework.eclipseide.internal.assistant.IVariableReplacementRegionCalculator;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

/**
 * This class determines what TYPES of proposals that are relevant for the current cursor position, e.g. "Keyword",
 * "Variable reference", "Table start" or similar. It then uses the given ProposalGenerator to generate proposals for
 * those types.
 */
public class ProposalSuitabilityDeterminer implements IProposalSuitabilityDeterminer {

    private final IProposalGeneratorFactory proposalGeneratorFactory;
    private final IVariableReplacementRegionCalculator variableReplacementRegionCalculator;

    public ProposalSuitabilityDeterminer(IProposalGeneratorFactory proposalGeneratorFactory, IVariableReplacementRegionCalculator variableReplacementRegionCalculator) {
        this.proposalGeneratorFactory = proposalGeneratorFactory;
        this.variableReplacementRegionCalculator = variableReplacementRegionCalculator;
    }

    @Override
    public List<VisitorInfo> generateAttemptVisitors(IFile file, ParsedString argument, int documentOffset, int lineCharPos) {
        List<VisitorInfo> visitors;
        if (argument.getArgumentIndex() == 0) {
            visitors = createProposalGeneratorsForFirstArgument(file, argument);
        } else {
            visitors = createProposalGeneratorsForRestOfArguments(file, argument, documentOffset, lineCharPos);
        }
        return visitors;
    }

    private List<VisitorInfo> createProposalGeneratorsForFirstArgument(IFile file, ParsedString argument) {
        List<VisitorInfo> visitorInfos = new ArrayList<VisitorInfo>();
        switch (argument.getType()) {
            case NEW_KEYWORD:
                visitorInfos.add(new VisitorInfo(argument, proposalGeneratorFactory.createKeywordDefinitionAttemptVisitor(file, argument)));
                break;
            case SETTING_KEY:
                visitorInfos.add(new VisitorInfo(argument, proposalGeneratorFactory.createSettingTableAttemptVisitor()));
                break;
            default:
                break;
        }
        visitorInfos.add(new VisitorInfo(argument, proposalGeneratorFactory.createTableAttemptVisitor()));
        // TODO we should only include either of setting/table proposals if either has exactly one match perhaps?
        return visitorInfos;
    }

    private List<VisitorInfo> createProposalGeneratorsForRestOfArguments(IFile file, ParsedString argument, int documentOffset, int lineCharPos) {
        boolean allowKeywords = false;
        boolean allowVariables = false;
        int maxVariableCharPos = Integer.MAX_VALUE;
        int maxSettingCharPos = Integer.MAX_VALUE;
        switch (argument.getType()) {
            case KEYWORD_CALL:
                allowKeywords = true;
                break;
            case KEYWORD_CALL_DYNAMIC:
                allowKeywords = true;
                allowVariables = true;
                break;
            case KEYWORD_ARG:
                allowVariables = true;
                break;
            case SETTING_FILE_ARG:
            case SETTING_VAL:
            case SETTING_FILE:
                allowVariables = true;
                // limit visible imported variables to those loaded before current line
                maxSettingCharPos = lineCharPos - 1;
                break;
            case VARIABLE_VAL:
                allowVariables = true;
                // limit visible local variables to those declared before current line
                maxVariableCharPos = lineCharPos - 1;
                maxSettingCharPos = -1;
                break;
        }
        List<VisitorInfo> visitorInfos = new ArrayList<VisitorInfo>();
        if (allowKeywords) {
            visitorInfos.add(new VisitorInfo(argument, proposalGeneratorFactory.createKeywordCallAttemptVisitor(file)));
        }
        if (allowVariables) {
            IRegion variableReplacementRegion = variableReplacementRegionCalculator.calculate(argument, documentOffset);
            ParsedString variableInsideArgument = argument.extractRegion(variableReplacementRegion);
            visitorInfos.add(new VisitorInfo(variableInsideArgument, proposalGeneratorFactory.createVariableAttemptVisitor(file, maxVariableCharPos, maxSettingCharPos)));
        }
        return visitorInfos;
    }
}
