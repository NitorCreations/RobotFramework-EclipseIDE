/**
 * Copyright 2011 Nitor Creations Oy
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
package org.otherone.robotframework.eclipse.editor.builder;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ToggleNatureAction implements IObjectActionDelegate {

  private ISelection selection;

  @Override
  public void run(IAction action) {
    if (selection instanceof IStructuredSelection) {
      for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
        Object element = it.next();
        IProject project = null;
        if (element instanceof IProject) {
          project = (IProject) element;
        } else if (element instanceof IAdaptable) {
          project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
        }
        if (project != null) {
          toggleNature(project);
        }
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    this.selection = selection;
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  private void toggleNature(IProject project) {
    try {
      IProjectDescription description = project.getDescription();
      String[] natures = description.getNatureIds();

      for (int i = 0; i < natures.length; ++i) {
        if (RFENature.NATURE_ID.equals(natures[i])) {
          // Remove the nature
          String[] newNatures = new String[natures.length - 1];
          System.arraycopy(natures, 0, newNatures, 0, i);
          System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
          description.setNatureIds(newNatures);
          project.setDescription(description, null);
          return;
        }
      }

      // Add the nature
      String[] newNatures = new String[natures.length + 1];
      System.arraycopy(natures, 0, newNatures, 0, natures.length);
      newNatures[natures.length] = RFENature.NATURE_ID;
      description.setNatureIds(newNatures);
      project.setDescription(description, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

}
