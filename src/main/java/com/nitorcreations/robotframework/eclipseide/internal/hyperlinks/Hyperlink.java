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
package com.nitorcreations.robotframework.eclipseide.internal.hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.nitorcreations.robotframework.eclipseide.editors.ResourceManager;
import com.nitorcreations.robotframework.eclipseide.editors.RobotFrameworkTextfileEditor;

public class Hyperlink implements IHyperlink {

    protected final IRegion linkRegion;
    protected final String linkText;
    private final IRegion targetRegion;
    private final IFile targetFile;

    public Hyperlink(IRegion linkRegion, String linkText, IRegion targetRegion, IFile targetFile) {
        this.linkRegion = linkRegion;
        this.linkText = linkText;
        this.targetRegion = targetRegion;
        this.targetFile = targetFile;

    }

    @Override
    public IRegion getHyperlinkRegion() {
        return linkRegion;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return linkText;
    }

    @Override
    public void open() {
        RobotFrameworkTextfileEditor editor = ResourceManager.openOrReuseEditorFor(targetFile, true);
        if (targetRegion != null) {
            editor.selectAndReveal(targetRegion.getOffset(), targetRegion.getLength());
        }
    }

}
