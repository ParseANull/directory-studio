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
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.wizards.NewBookmarkWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewBookmarkAction — PLACING A BEACON IN THE EMPIRE ────────────────
// When Imperial engineers finished a new section of the second Death Star, they
// planted a permanent marker — a beacon the construction crews could navigate back
// to reliably.  NewBookmarkAction does the same for LDAP entries: it opens the
// New Bookmark Wizard so the user can plant a named, permanent shortcut to any
// entry in the directory tree they want to return to quickly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the New Bookmark Wizard so the user can create a named shortcut to an
 * LDAP entry in the directory browser.
 * Bookmarks let users jump directly to frequently accessed entries without
 * navigating the full directory tree every time.
 * Think of this class as planting a permanent construction beacon — once set,
 * the crew can always find their way back to that exact spot.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewBookmarkAction extends BrowserAction
{
    // ── Beacon Construction Team On Standby ─────────────────────────────────────
    // The beacon crew doesn't do anything until they receive the order — they just
    // assemble and wait.  Our no-arg constructor follows the same pattern: the
    // parent BrowserAction sets up selection-listener plumbing; we just stand ready.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewBookmarkAction.
     * Nothing interesting happens here — Eclipse calls this when registering the
     * action, and the BrowserAction parent handles the selection-listener setup.
     */
    public NewBookmarkAction()
    {
    }


    // ── Crew Activates And Plants The Beacon ────────────────────────────────────
    // When the order comes in, the beacon crew springs into action: they grab the
    // current location readings, initialise the wizard with that context, and open
    // the dialog for the commander to name the beacon and confirm placement.
    // We do exactly that: instantiate the wizard, pass it the active workbench
    // window and selection, then open the blocking WizardDialog.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the New Bookmark Wizard in a blocking dialog.
     * We pass the current workbench selection to the wizard so it can pre-populate
     * the target entry from whatever the user has highlighted in the browser.
     * Blocking on open means the action waits until the user finishes or cancels
     * before returning control to Eclipse.
     */
    public void run()
    {
        NewBookmarkWizard wizard = new NewBookmarkWizard();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        wizard.init( window.getWorkbench(), ( IStructuredSelection ) window.getSelectionService().getSelection() );
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }


    // ── Beacon Has An Official Designation ──────────────────────────────────────
    // Every Imperial beacon carried an official designation on its label plate.
    // Our action's text is that designation — the localised string Eclipse uses to
    // label the menu item and any associated tooltip.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action as it appears in menus.
     * The string is looked up from our NLS bundle so it can be translated.
     *
     * @return the localised action label, e.g. "New Bookmark…"
     */
    public String getText()
    {
        return Messages.getString( "NewBookmarkAction.NewBookmark" ); //$NON-NLS-1$
    }


    // ── Beacon Carries The Add-Bookmark Icon ────────────────────────────────────
    // The beacon's visual indicator — a blinking light — told crews what it was
    // without needing to read the label.  Our icon does the same: the add-bookmark
    // image makes the menu item instantly recognisable.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's toolbar/menu icon.
     * We pull the bookmark-add icon from the plugin's image registry.
     *
     * @return the image descriptor for the bookmark-add icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_BOOKMARK_ADD );
    }


    // ── No Broadcast Command Code ───────────────────────────────────────────────
    // Not every beacon placement needed a Galaxy-wide command code — some were local
    // operations.  This action has no global Eclipse command ID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding purposes.
     * We don't have a registered command for "New Bookmark" so this returns null.
     *
     * @return null — no global command ID is registered for this action
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Beacons Can Always Be Placed ────────────────────────────────────────────
    // Unlike some operations that require specific preconditions, planting a beacon
    // is always possible — the crew is always available.  We always return true here
    // because the wizard will guide the user through picking an entry regardless of
    // what's currently selected in the browser.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether the New Bookmark action is available right now.
     * Creating a bookmark doesn't depend on the current selection — the wizard
     * handles entry picking — so we always return true.
     *
     * @return always true; this action is always enabled
     */
    public boolean isEnabled()
    {
        return true;
    }
}
