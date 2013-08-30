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
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

import com.nitorcreations.robotframework.eclipseide.builder.parser.ArgumentPreParser;

public class SettingTableAttemptVisitor implements AttemptVisitor {

    private final List<String> settingKeys;

    public SettingTableAttemptVisitor() {
        settingKeys = new ArrayList<String>(ArgumentPreParser.getSettingKeys());
        Collections.sort(settingKeys);
    }

    @Override
    public RobotCompletionProposalSet visitAttempt(String attempt, IRegion replacementRegion) {
        assert attempt.equals(attempt.toLowerCase());
        RobotCompletionProposalSet ourProposalSet = new RobotCompletionProposalSet();
        for (String key : settingKeys) {
            if (key.toLowerCase().startsWith(attempt)) {
                String proposal = key;
                Image image = null;
                String displayString = key;
                String additionalProposalInfo = null;
                String informationDisplayString = null;
                ourProposalSet.getProposals().add(new RobotCompletionProposal(proposal, null, replacementRegion, image, displayString, informationDisplayString, additionalProposalInfo));
            }
        }
        return ourProposalSet;
    }
}
