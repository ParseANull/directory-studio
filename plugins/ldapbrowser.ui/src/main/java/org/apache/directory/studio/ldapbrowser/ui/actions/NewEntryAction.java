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
import org.apache.directory.studio.ldapbrowser.common.wizards.NewEntryWizard;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;


// ── CLASS: NewEntryAction — LUKE'S JOURNEY FROM TATOOINE TO THE DEATH STAR ───
// Luke didn't leap from a dusty moisture farm directly to blowing up the Death
// Star — he took it step by step: meet Obi-Wan, leave Tatooine, rescue Leia,
// reach the Death Star, escape with the plans.  Creating a new LDAP entry works
// the same way: the user steps through a wizard — choose object classes, set the
// RDN, fill in attributes — and at the end a new entry exists in the directory.
// NewEntryAction is the "hop on the speeder" moment that kicks off that journey.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Launches the New Entry Wizard, which guides the user step-by-step through
 * creating a new LDAP entry in the connected directory.
 * Each wizard page is one leg of the journey: object class selection, RDN
 * definition, attribute population — culminating in the entry being written to
 * the server.
 * Think of this class as the moment Luke decides to leave Tatooine: it's the
 * action that starts the whole creation flow.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewEntryAction extends BrowserAction
{
    protected IWorkbenchWindow window;


    // ── Luke Packs His Bags With No Destination Yet ──────────────────────────────
    // Luke left home before he knew exactly where he was going — he just knew he
    // was leaving.  The no-arg constructor creates the action without a window
    // reference; Eclipse will inject the window later via init().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a NewEntryAction with no associated workbench window.
     * Eclipse uses this form when creating the action from a plugin.xml extension
     * point declaration; the window is bound later via {@code init(IWorkbenchWindow)}.
     */
    public NewEntryAction()
    {
    }


    // ── Luke Knows He's Headed To Alderaan ───────────────────────────────────────
    // When Obi-Wan told Luke "you're going to Alderaan," Luke had a destination.
    // This constructor gives us an explicit window reference so we know which
    // workbench context we're operating in from the start.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a NewEntryAction already bound to the given workbench window.
     * Use this when you know the target window at construction time — it means
     * we don't have to look it up later when run() is called.
     *
     * @param window  the workbench window this action operates within; we store it
     *                so we can access the shell and current selection in run()
     */
    public NewEntryAction( IWorkbenchWindow window )
    {
        super();
        this.window = window;
    }


    // ── Luke Leaves The Homestead ────────────────────────────────────────────────
    // When Luke finally left Tatooine for good he severed his ties — no going back.
    // dispose() does our equivalent cleanup: it calls super.dispose() to tear down
    // the BrowserAction machinery and nulls out our window reference so we don't
    // hold a stale reference to a closed workbench window.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases resources held by this action, including the workbench window reference.
     * Called by Eclipse when the action is being deregistered (e.g. on window close).
     * We null out the window to avoid memory leaks from stale UI references.
     */
    public void dispose()
    {
        super.dispose();
        this.window = null;
    }


    // ── Obi-Wan Briefs Luke On The Mission ───────────────────────────────────────
    // Obi-Wan handed Luke his mission context — the lightsaber, the hologram, the
    // destination — before they boarded the Millennium Falcon.  init() does the
    // same: Eclipse calls it to hand us the active workbench window so we can use
    // it in run() when the user triggers the action.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Initialises this action with the active workbench window.
     * Eclipse calls this on workbench-window-scoped actions (e.g. actions declared
     * in an {@code actionSets} extension).  We store the window so run() can reach
     * the shell and selection service.
     *
     * @param window  the active workbench window; stored for use in run()
     */
    public void init( IWorkbenchWindow window )
    {
        super.init( window );
        this.window = window;
    }


    // ── Falcon Jumps To Hyperspace ───────────────────────────────────────────────
    // Han pulls the hyperdrive lever and they're off — no looking back.  run()
    // fires the wizard: it grabs the current selection (so the wizard knows which
    // parent entry to start from), wraps the wizard in a blocking dialog, and
    // opens it.  The user navigates the steps; we wait.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the New Entry Wizard in a blocking dialog using the current workbench
     * selection as the starting context.
     * The wizard guides the user through every step of entry creation; we block
     * until the dialog is dismissed so the browser can refresh afterwards.
     */
    public void run()
    {
        INewWizard wizard = getWizard();

        wizard.init( window.getWorkbench(), ( IStructuredSelection ) window.getSelectionService().getSelection() );
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }


    // ── Choosing Which Ship To Fly ───────────────────────────────────────────────
    // Luke could have taken a speeder or a Y-wing — he took the Falcon because that's
    // what the mission called for.  getWizard() is the hook that lets subclasses (like
    // NewContextEntryAction) swap in a different wizard for a different kind of entry.
    // The default here is the standard NewEntryWizard for ordinary child entries.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the wizard to use for entry creation.
     * Subclasses override this to return a specialised wizard — for example,
     * {@link NewContextEntryAction} returns a {@code NewContextEntryWizard} for
     * creating naming-context root entries.  The default is the general-purpose
     * {@code NewEntryWizard}.
     *
     * @return the wizard to wrap in a dialog and present to the user
     */
    protected INewWizard getWizard()
    {
        return new NewEntryWizard();
    }


    // ── Mission Briefing Board: "New Entry" ──────────────────────────────────────
    // Every Rebel briefing board had a mission name at the top.  getText() returns
    // the localised menu-item label so Eclipse can display it clearly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display label for this action.
     *
     * @return the menu-item text, e.g. "New Entry…"
     */
    public String getText()
    {
        return Messages.getString( "NewEntryAction.NewEntry" ); //$NON-NLS-1$
    }


    // ── Rebel Starfighter Icon ───────────────────────────────────────────────────
    // Every mission slot on the briefing board had a ship icon next to it.  Our
    // entry-add icon plays that role: it makes the menu item visually distinct.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's toolbar/menu icon.
     * We use the entry-add icon from the plugin's image registry.
     *
     * @return the image descriptor for the entry-add icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_ENTRY_ADD );
    }


    // ── Mission Has No Pre-Assigned Command Code ─────────────────────────────────
    // Not every Rebel mission had a standard Alliance broadcast code — some were
    // ad-hoc.  This action has no global Eclipse command ID for keybinding.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action.
     * We don't register a global command for "New Entry," so this returns null.
     *
     * @return null — no global command ID is registered
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Luke Is Always Ready To Leave Tatooine ───────────────────────────────────
    // Luke was always eager — he'd have left the first day if he could.  Creating
    // a new entry has no prerequisites that we check here; the wizard handles its
    // own validation.  So we always return true.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether the New Entry action is available.
     * We always return true because the wizard handles its own context checking —
     * there's no pre-condition at the action level that would prevent the wizard
     * from opening.
     *
     * @return always true
     */
    public boolean isEnabled()
    {
        return true;
    }

}
