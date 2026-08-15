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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldifeditor.LdifEditorActivator;
import org.apache.directory.studio.ldifeditor.LdifEditorConstants;
import org.apache.directory.studio.ldifeditor.editor.LdifEditor;
import org.apache.directory.studio.ldifeditor.editor.NonExistingLdifEditorInput;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewLdifFileAction — DRAFTING A NEW DEATH STAR BLUEPRINT ───────────
// Before any construction began on the second Death Star, the engineers sat down
// with blank datapads and started drafting new schematics.  LDIF (LDAP Data
// Interchange Format) is the text format used to describe directory entries and
// the changes to make to them — it's our blueprint format.  NewLdifFileAction
// opens the LDIF editor with a blank slate, giving the user an empty datapad to
// start drafting those blueprints.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens a new, empty LDIF editor tab in the Eclipse workbench.
 * LDIF is a plain-text format for describing LDAP entries and modifications;
 * this action gives the user a blank document to write LDIF by hand, which they
 * can later apply to a connected directory.
 * Think of this class as handing an engineer a blank datapad: ready to draft,
 * nothing pre-filled.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewLdifFileAction extends BrowserAction
{
    // ── Engineer Reports For Duty ────────────────────────────────────────────────
    // Before the engineer sits down to draft, they check in at the command post.
    // Our constructor calls super() to wire into the BrowserAction infrastructure —
    // selection listeners, image registry, all of it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewLdifFileAction and registers it with the BrowserAction framework.
     * The parent class wires up selection-state listeners; we just call super().
     */
    public NewLdifFileAction()
    {
        super();
    }


    // ── Engineer Opens A Blank Datapad ───────────────────────────────────────────
    // The engineer taps "New Document" on their Imperial datapad.  A blank screen
    // appears, cursor blinking, ready for input.  run() does exactly this: it
    // creates a NonExistingLdifEditorInput (our "blank document" signal) and asks
    // the workbench to open the LDIF editor with it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a blank LDIF editor in the active workbench page.
     * We create a {@code NonExistingLdifEditorInput} — which tells the editor there's
     * no backing file yet — and hand it to {@code IWorkbenchPage.openEditor()}.
     * The user can type LDIF freely; saving it will prompt for a file location.
     * If the editor tab can't be opened (rare), we silently swallow the
     * {@code PartInitException} — the user will simply notice nothing happened.
     */
    public void run()
    {
        IEditorInput input = new NonExistingLdifEditorInput();
        String editorId = LdifEditor.getId();
        try
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            page.openEditor( input, editorId );
        }
        catch ( PartInitException e )
        {
        }
    }


    // ── Blueprint File Is Labelled ───────────────────────────────────────────────
    // Every datapad document had a working title at the top.  getText() returns the
    // localised label that Eclipse puts in the File > New menu and the toolbar.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display label for this action.
     *
     * @return the menu-item text, e.g. "New LDIF File"
     */
    public String getText()
    {
        return Messages.getString( "NewLdifFileAction.NewLDIF" ); //$NON-NLS-1$
    }


    // ── Datapad Has The LDIF Editor Icon ────────────────────────────────────────
    // The datapad's app icon told you at a glance what you were opening.  We pull
    // the LDIF-editor-new icon from the LDIF editor plugin's own image registry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's toolbar/menu icon.
     * The icon comes from the LDIF editor plugin — we use the LDIF-new-file image
     * rather than the browser plugin's own icons.
     *
     * @return the image descriptor for the "new LDIF" icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return LdifEditorActivator.getDefault().getImageDescriptor( LdifEditorConstants.IMG_LDIFEDITOR_NEW );
    }


    // ── No Special Command Broadcast ────────────────────────────────────────────
    // Not all Imperial operations got a Galaxy-wide broadcast command code.
    // This action doesn't bind to a workbench command ID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * We don't register a global command here, so null is returned.
     *
     * @return null — no global command ID is registered for this action
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Datapads Are Always Available ───────────────────────────────────────────
    // Engineers could always grab a blank datapad — no prerequisite approvals needed.
    // Opening a new LDIF file doesn't depend on any selection state, so we always
    // return true.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether the New LDIF File action is available.
     * Opening a blank editor has no preconditions, so this always returns true.
     *
     * @return always true
     */
    public boolean isEnabled()
    {
        return true;
    }
}
