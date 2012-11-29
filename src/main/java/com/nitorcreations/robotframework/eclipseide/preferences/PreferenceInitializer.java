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
package com.nitorcreations.robotframework.eclipseide.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nitorcreations.robotframework.eclipseide.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        // store.setDefault(PreferenceConstants.P_BOOLEAN, true);
        // store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
        // store.setDefault(PreferenceConstants.P_STRING, "Default value");

        store.setDefault(PreferenceConstants.P_COMMENT, "128,128,128");
        store.setDefault(PreferenceConstants.P_TABLE, "192,0,192");
        store.setDefault(PreferenceConstants.P_SETTING, "0,192,0");
        store.setDefault(PreferenceConstants.P_SETTING_VALUE, "0,255,0");
        store.setDefault(PreferenceConstants.P_SETTING_FILE, "0,0,0"); // new RGB(255, 255, 255)
        store.setDefault(PreferenceConstants.P_SETTING_FILE_ARG, "0,255,64");
        store.setDefault(PreferenceConstants.P_VARIABLE, "0,170,180");
        store.setDefault(PreferenceConstants.P_VARIABLE_VALUE, "140,152,162"); // new RGB(180, 192, 202)
        store.setDefault(PreferenceConstants.P_TESTCASE_NEW, "222,0,0");
        store.setDefault(PreferenceConstants.P_KEYWORD_NEW, "128,128,50"); // new RGB(255, 255, 50)
        store.setDefault(PreferenceConstants.P_KEYWORD_LVALUE, "255,0,180");
        store.setDefault(PreferenceConstants.P_KEYWORD, "255,180,0");
        store.setDefault(PreferenceConstants.P_KEYWORD_ARG, "255,100,0");
        store.setDefault(PreferenceConstants.P_FOR_PART, "0,255,255");
        store.setDefault(PreferenceConstants.P_DEFAULT, "192,192,192");
        store.setDefault(PreferenceConstants.P_UNKNOWN, "255,140,0");
        store.setDefault(PreferenceConstants.P_FIELD_BG, "8,16,24");
        store.setDefault(PreferenceConstants.P_FG, "210,210,210");
        store.setDefault(PreferenceConstants.P_BG, "16,32,48");
        store.setDefault(PreferenceConstants.P_FG_SELECTION, "255,255,255");
        store.setDefault(PreferenceConstants.P_BG_SELECTION, "56,83,104");
        // store.setDefault(PreferenceConstants.P_ARGUMENT_SEPARATOR, "16,32,48");
    }

}
