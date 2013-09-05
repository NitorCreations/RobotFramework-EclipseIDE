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

        // syntax coloring preferences
        // commented colors are for dark background
        store.setDefault(PreferenceConstants.P_COMMENT, "128,128,128"); // 128,128,128
        store.setDefault(PreferenceConstants.P_TABLE, "192,0,192"); // 192,0,192
        store.setDefault(PreferenceConstants.P_SETTING, "0,128,0"); // 0,192,0
        store.setDefault(PreferenceConstants.P_SETTING_VALUE, "0,192,0"); // 0,255,0
        store.setDefault(PreferenceConstants.P_SETTING_FILE, "0,0,0"); // 255,255,255
        store.setDefault(PreferenceConstants.P_SETTING_FILE_ARG, "0,192,64"); // 0,255,64
        store.setDefault(PreferenceConstants.P_SETTING_FILE_WITH_NAME, "192,192,192"); // 192,192,192
        store.setDefault(PreferenceConstants.P_VARIABLE, "0,170,180"); // 0,170,180
        store.setDefault(PreferenceConstants.P_VARIABLE_VALUE, "115,124,133"); // 180,192,202
        store.setDefault(PreferenceConstants.P_TESTCASE_NEW, "222,0,0"); // 222,0,0
        store.setDefault(PreferenceConstants.P_KEYWORD_NEW, "128,128,50"); // 255,255,50
        store.setDefault(PreferenceConstants.P_KEYWORD_LVALUE, "225,0,159"); // 255,0,180
        store.setDefault(PreferenceConstants.P_KEYWORD, "212,149,0"); // 255,180,0
        store.setDefault(PreferenceConstants.P_KEYWORD_ARG, "255,100,0"); // 255,100,0
        store.setDefault(PreferenceConstants.P_FOR_PART, "0,128,128"); // 0,255,255

        // table naming preferences
        store.setDefault(PreferenceConstants.P_VARIABLE_TABLE_FORMAT, "* Variables");
        store.setDefault(PreferenceConstants.P_SETTING_TABLE_FORMAT, "* Settings");
        store.setDefault(PreferenceConstants.P_TESTCASE_TABLE_FORMAT, "* Test Cases");
        store.setDefault(PreferenceConstants.P_KEYWORD_TABLE_FORMAT, "* Keywords");
    }

}
