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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nitorcreations.robotframework.eclipseide.Activator;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class MainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public MainPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("General preferences for the Robot Framework editor - see subpages.");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
     * types of preferences. Each field editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        // addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, "&Directory preference:",
        // getFieldEditorParent()));
        // addField(new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN, "&An example of a boolean preference",
        // getFieldEditorParent()));
        //
        // addField(new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE,
        // "An example of a multiple-choice preference", 1, new String[][] { { "&Choice 1", "choice1" }, { "C&hoice 2",
        // "choice2" } }, getFieldEditorParent()));
        // addField(new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
        // addField(new ColorFieldEditor(PreferenceConstants.P_COLOR, "&Color", getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {}

}