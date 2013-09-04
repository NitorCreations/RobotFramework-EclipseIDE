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
package com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nitorcreations.robotframework.eclipseide.Activator;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class ProposalGeneratorFactory implements IProposalGeneratorFactory {

    @Override
    public AttemptVisitor createTableAttemptVisitor() {
        IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        return new TableAttemptVisitor(preferenceStore);
    }

    @Override
    public AttemptVisitor createSettingTableAttemptVisitor() {
        return new SettingTableAttemptVisitor();
    }

    @Override
    public AttemptVisitor createKeywordDefinitionAttemptVisitor(final IFile file, ParsedString argument) {
        return new KeywordDefinitionAttemptVisitor(file, argument);
    }

    @Override
    public AttemptVisitor createKeywordCallAttemptVisitor(IFile file) {
        return new KeywordCallAttemptVisitor(file);
    }

    @Override
    public AttemptVisitor createVariableAttemptVisitor(IFile file, int maxVariableCharPos, int maxSettingCharPos) {
        return new VariableAttemptVisitor(file, maxVariableCharPos, maxSettingCharPos);
    }

}
