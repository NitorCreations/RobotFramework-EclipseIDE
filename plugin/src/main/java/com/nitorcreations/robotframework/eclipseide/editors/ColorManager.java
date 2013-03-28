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
package com.nitorcreations.robotframework.eclipseide.editors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.nitorcreations.robotframework.eclipseide.Activator;

public class ColorManager {

    protected Set<String> colorPreferences = new HashSet<String>();
    protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);
    private boolean listenerRegistered;

    public void dispose() {
        Iterator<Color> e = fColorTable.values().iterator();
        while (e.hasNext())
            e.next().dispose();
        fColorTable.clear();
    }

    public Color getColor(String preferenceId) {
        colorPreferences.add(preferenceId);
        IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        if (!listenerRegistered) {
            preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    dispose();
                }
            });
            listenerRegistered = true;
        }
        String rgbString = preferenceStore.getString(preferenceId);
        String[] rgbArr = rgbString.split(",");
        RGB irftColor = new RGB(Integer.parseInt(rgbArr[0]), Integer.parseInt(rgbArr[1]), Integer.parseInt(rgbArr[2]));
        Color color = fColorTable.get(irftColor);
        if (color == null) {
            color = new Color(Display.getCurrent(), irftColor);
            fColorTable.put(irftColor, color);
        }
        return color;
    }

    public boolean isColorPreference(String preferenceId) {
        return colorPreferences.contains(preferenceId);
    }
}
