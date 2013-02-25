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
package com.nitorcreations.robotframework.eclipseide.structure;

import java.util.Collections;
import java.util.List;

import com.nitorcreations.robotframework.eclipseide.structure.api.IDynamicParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IKeywordCall;
import com.nitorcreations.robotframework.eclipseide.structure.api.IUserKeywordDefinition;

public class UserKeywordDefinition extends KeywordSequence implements IUserKeywordDefinition {

    private IDynamicParsedString sequenceName;
    private List<IDynamicParsedString> argumentsIMM;
    private List<IDynamicParsedString> returnValuesIMM;
    private IKeywordCall keywordTeardown;

    // singles

    public void setSequenceName(IDynamicParsedString sequenceName) {
        this.sequenceName = sequenceName;
    }

    public void setKeywordTeardown(IKeywordCall keywordTeardown) {
        this.keywordTeardown = keywordTeardown;
    }

    // lists

    public void setArguments(List<? extends IDynamicParsedString> arguments) {
        this.argumentsIMM = Collections.unmodifiableList(arguments);
    }

    public void setReturnValues(List<? extends IDynamicParsedString> returnValues) {
        this.returnValuesIMM = Collections.unmodifiableList(returnValues);
    }

    // getters

    @Override
    public IDynamicParsedString getSequenceName() {
        return sequenceName;
    }

    @Override
    public List<IDynamicParsedString> getArguments() {
        return argumentsIMM;
    }

    @Override
    public List<IDynamicParsedString> getReturnValues() {
        return returnValuesIMM;
    }

    @Override
    public IKeywordCall getKeywordTeardown() {
        return keywordTeardown;
    }

}
