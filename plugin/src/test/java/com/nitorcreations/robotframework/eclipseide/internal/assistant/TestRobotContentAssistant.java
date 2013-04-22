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
package com.nitorcreations.robotframework.eclipseide.internal.assistant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.PluginContext;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotFile;
import com.nitorcreations.robotframework.eclipseide.builder.parser.RobotLine;
import com.nitorcreations.robotframework.eclipseide.editors.IResourceManager;

@RunWith(Enclosed.class)
public class TestRobotContentAssistant {
    @Ignore
    public abstract static class Base {
        ITextViewer textViewer;
        IRobotContentAssistant2 rca2;
        RobotContentAssistant assistant;
        IDocument document;

        final IProject project = mock(IProject.class, "project");
        final IResourceManager resourceManager = mock(IResourceManager.class, "resourceManager");

        @Before
        public void setup() throws Exception {
            textViewer = mock(ITextViewer.class, "textViewer");
            rca2 = mock(IRobotContentAssistant2.class, "rca2");
            assistant = new RobotContentAssistant(rca2);
            document = mock(IDocument.class, "document");
            when(textViewer.getDocument()).thenReturn(document);

            PluginContext.setResourceManager(resourceManager);
        }

        @SuppressWarnings("unchecked")
        protected IFile addFile(String fileName, String origContents) throws Exception {
            final IFile file = mock(IFile.class, fileName);
            ByteArrayInputStream contentStream = new ByteArrayInputStream(origContents.getBytes("UTF-8"));
            when(file.getContents()).thenReturn(contentStream).thenThrow(ArrayIndexOutOfBoundsException.class);
            when(file.getContents(anyBoolean())).thenReturn(contentStream).thenThrow(ArrayIndexOutOfBoundsException.class);
            when(file.getCharset()).thenReturn("UTF-8");
            when(file.getProject()).thenReturn(project);
            when(file.getName()).thenReturn(fileName);
            when(file.exists()).thenReturn(true);
            return file;
        }
    }

    public static class computeCompletionProposals extends Base {

        static final String FILE_CONTENTS = "*Variables\n${FOO}  bar\n*Testcases\nTest logging\n  Log  Hello";
        static final int FAKE_DOCUMENT_OFFSET = 12345;
        static final int FAKE_LINE_NO = 49;
        static final ICompletionProposal[] EXPECTED_PROPOSALS = new ICompletionProposal[3];

        @Test
        public void should_suggest_replacing_entered_variable() throws Exception {
            IFile origFile = addFile("orig.txt", FILE_CONTENTS);
            when(resourceManager.resolveFileFor(document)).thenReturn(origFile);
            when(document.get()).thenReturn(FILE_CONTENTS);
            when(document.getLineOfOffset(FAKE_DOCUMENT_OFFSET)).thenReturn(FAKE_LINE_NO);
            when(rca2.generateProposals(eq(origFile), eq(FAKE_DOCUMENT_OFFSET), eq(FILE_CONTENTS), anyListOf(RobotLine.class), eq(FAKE_LINE_NO))).thenReturn(EXPECTED_PROPOSALS);

            ICompletionProposal[] proposals = assistant.computeCompletionProposals(textViewer, FAKE_DOCUMENT_OFFSET);

            assertThat(proposals, is(sameInstance(EXPECTED_PROPOSALS)));
            List<RobotLine> lines = RobotFile.parse(FILE_CONTENTS).getLines();
            verify(rca2).generateProposals(origFile, FAKE_DOCUMENT_OFFSET, FILE_CONTENTS, lines, FAKE_LINE_NO);
            verifyNoMoreInteractions(rca2);
        }
    }
}
