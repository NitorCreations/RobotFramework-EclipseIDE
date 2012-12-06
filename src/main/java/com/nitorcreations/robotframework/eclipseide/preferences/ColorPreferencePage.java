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

import org.eclipse.jface.preference.ColorFieldEditor;
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
public class ColorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ColorPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Syntax coloring preferences for the Robot Framework editor");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
     * types of preferences. Each field editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        addField(new ColorFieldEditor(PreferenceConstants.P_COMMENT, "Comments", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_TABLE, "Table declaration", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_SETTING, "Setting name", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_SETTING_VALUE, "Setting value, string", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_SETTING_FILE, "Setting value, filename", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_SETTING_FILE_ARG, "Setting value, argument for library/variables", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_SETTING_FILE_WITH_NAME, "'WITH NAME' in library imports", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_VARIABLE, "Variable definition: name", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_VARIABLE_VALUE, "Variable definition: value", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_TESTCASE_NEW, "Test case declaration", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_KEYWORD_NEW, "Keyword declaration", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_KEYWORD_LVALUE, "Keyword return value variable assignment", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_KEYWORD, "Keyword call", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_KEYWORD_ARG, "Keyword argument", getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.P_FOR_PART, "FOR loop constructs", getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {}

}
