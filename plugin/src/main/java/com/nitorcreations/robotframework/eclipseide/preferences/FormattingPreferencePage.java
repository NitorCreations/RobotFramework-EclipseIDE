/**
 * Copyright 2014 Nitor Creations Oy
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

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.nitorcreations.robotframework.eclipseide.Activator;

public class FormattingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private IntegerFieldEditor tabSizeEditor;

    public FormattingPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Preferred formatting of text for the Robot Framework editor");
    }

    @Override
    public void createFieldEditors() {
        String[][] tabPolicyChoices = new String[][] { { "Use Spaces", "true" }, { "Use Tabs", "false" }, { "Use Global Editor Defaults", "" } };
        ComboFieldEditor tabPolicyEditor = new ComboFieldEditor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, "Tab policy", tabPolicyChoices, getFieldEditorParent()) {
            @Override
            protected void fireValueChanged(String property, Object oldValue, Object newValue) {
                super.fireValueChanged(property, oldValue, newValue);
                updateTabSizeEditor(newValue);
            }

            @Override
            protected void doLoad() {
                super.doLoad();
                updateTabSizeEditor(getPreferenceStore().getString(getPreferenceName()));
            }

            @Override
            protected void doLoadDefault() {
                super.doLoadDefault();
                updateTabSizeEditor(getPreferenceStore().getDefaultString(getPreferenceName()));
            }

            private void updateTabSizeEditor(Object newValue) {
                boolean shouldTabSizeEditorBeEnabled = ((String) newValue).length() > 0;
                tabSizeEditor.setEnabled(shouldTabSizeEditorBeEnabled, getFieldEditorParent());
            }
        };
        addField(tabPolicyEditor);

        tabSizeEditor = new IntegerFieldEditor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, "&Tab size", getFieldEditorParent()) {
            @Override
            protected boolean checkState() {
                if (!isEnabled()) {
                    clearErrorMessage();
                    return true;
                }
                return super.checkState();
            }

            @Override
            protected void doStore() {
                if (!isEnabled()) {
                    getPreferenceStore().setToDefault(getPreferenceName());
                } else {
                    super.doStore();
                }
            }

            private boolean isEnabled() {
                return getLabelControl(getFieldEditorParent()).getEnabled();
            }

            @Override
            public void setEnabled(boolean enabled, Composite parent) {
                super.setEnabled(enabled, parent);
                valueChanged();
            }
        };
        tabSizeEditor.setValidRange(1, 20); // same as Aptana Studio
        addField(tabSizeEditor);
    }

    @Override
    public void dispose() {
        super.dispose();
        tabSizeEditor = null;
    }

    @Override
    public void init(IWorkbench workbench) {}
}
