/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.apache.directory.studio.ldifeditor.wizards;


import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.ldifeditor.editor.NonExistingLdifEditorInput;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;


// ── CLASS: NewLdifFileWizard — REBEL OPERATOR OPENS A BLANK TRANSMISSION ─────
// The Rebel operator needs to draft a new communiqué from scratch, so they
// pick "New LDIF File" from the File menu — and instead of a file-picker
// wizard, the console simply opens a blank editor pane ready for them to type.
// NewLdifFileWizard is that shortcut: a one-step wizard whose only job is to
// open a new LdifEditor with a NonExistingLdifEditorInput so the operator
// can start drafting without choosing a filename up front.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse {@link INewWizard} that adds a "New LDIF File" entry to the
 * platform's "New..." menu.
 * When {@link #performFinish()} is called, it opens a {@link LdifEditor} with
 * a fresh {@link NonExistingLdifEditorInput} on the active workbench page —
 * no wizard pages are shown; the wizard completes instantly.
 * Think of this as the Rebel operator's one-click shortcut to open a blank
 * transmission console.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */

public class NewLdifFileWizard extends Wizard implements INewWizard
{

    /** The active workbench window, captured in {@link #init}. */
    private IWorkbenchWindow window;


    // ── CONSTRUCT ─────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of {@code NewLdifFileWizard}.
     */
    public NewLdifFileWizard()
    {
    }


    // ── INIT ──────────────────────────────────────────────────────────────────
    // Capture the active window reference so performFinish() can open the editor.
    /**
     * {@inheritDoc}
     *
     * <p>Stores the active {@link IWorkbenchWindow} for later use by
     * {@link #performFinish()}.</p>
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        window = workbench.getActiveWorkbenchWindow();
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Clears the window reference.</p>
     */
    public void dispose()
    {
        window = null;
    }


    // ── GET WIZARD ID ─────────────────────────────────────────────────────────
    /**
     * Returns the plug-in-registered ID of this wizard.
     *
     * @return {@link LdifEditorConstants#NEW_WIZARD_NEW_LDIF_FILE}
     */
    public static String getId()
    {
        return LdifEditorConstants.NEW_WIZARD_NEW_LDIF_FILE;
    }


    // ── OPEN A BLANK EDITOR ───────────────────────────────────────────────────
    // The whole "wizard" is just: open a new LDIF editor with a dummy input.
    /**
     * {@inheritDoc}
     *
     * <p>Opens a new {@link LdifEditor} on the active workbench page using a
     * {@link NonExistingLdifEditorInput}.  Returns {@code false} if
     * {@link IWorkbenchPage#openEditor} throws a {@link PartInitException},
     * {@code true} otherwise.</p>
     */
    public boolean performFinish()
    {
        IEditorInput input = new NonExistingLdifEditorInput();
        String editorId = LdifEditor.getId();

        try
        {
            IWorkbenchPage page = window.getActivePage();
            page.openEditor( input, editorId );
        }
        catch ( PartInitException e )
        {
            return false;
        }
        return true;
    }

}
