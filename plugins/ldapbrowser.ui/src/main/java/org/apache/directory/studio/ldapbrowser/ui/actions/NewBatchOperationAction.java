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
import org.apache.directory.studio.ldapbrowser.ui.wizards.BatchOperationWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;


// ── CLASS: NewBatchOperationAction — CONSTRUCTION OF THE SECOND DEATH STAR ───
// The Emperor's engineers didn't build the second Death Star in one shot — they
// orchestrated a massive, multi-step construction project involving thousands of
// workers, complex sequencing, and a final operational target.  NewBatchOperation-
// Action is the kick-off button for that kind of operation: it opens the Batch
// Operation Wizard, which walks the user through assembling a complex set of LDAP
// modifications to apply in one coordinated sweep.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Batch Operation Wizard, which lets users define and run a set of
 * LDAP modifications against multiple entries in one go.
 * It's the entry point for bulk changes — think mass attribute updates or
 * applying a template operation across an entire search result.
 * Think of this class as the Imperial construction coordinator: it marshals
 * the wizard resources and hands control to the multi-step build process.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewBatchOperationAction extends BrowserAction
{

    // ── Laying The Foundation Superstructure ────────────────────────────────────
    // Before construction began, the Emperor's engineers cleared the site and set
    // up their command post.  Our no-arg constructor simply exists — BrowserAction's
    // own init machinery wires everything else up via the Eclipse action registry.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new NewBatchOperationAction, ready to be registered with Eclipse.
     * All the real wiring (image, enablement, selection listening) happens in the
     * parent BrowserAction initialiser, not here.
     */
    public NewBatchOperationAction()
    {
    }


    // ── Project Name On The Briefing Board ──────────────────────────────────────
    // Every Imperial briefing started with the project title on the holo-display:
    // "Second Death Star — Phase 1."  Our getText() does the same: returns the
    // localised label so Eclipse can display it in menus and toolbar tooltips.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action as it appears in menus.
     * The string is looked up from our NLS bundle so it can be translated.
     *
     * @return the localised action label, e.g. "New Batch Operation…"
     */
    public String getText()
    {
        return Messages.getString( "NewBatchOperationAction.NewBatch" ); //$NON-NLS-1$
    }


    // ── The Imperial Crest On Every Blueprint ───────────────────────────────────
    // Every Death Star schematic carried the Imperial crest — a visual badge that
    // identified the project at a glance.  Our icon does the same: it's the batch
    // operation image that appears next to the menu item and on toolbar buttons.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's toolbar/menu icon.
     * We pull the batch-operation icon from the plugin's image registry using the
     * {@link BrowserUIConstants#IMG_BATCH} constant.
     *
     * @return the image descriptor for the batch icon, never null if the plugin loaded correctly
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_BATCH );
    }


    // ── No Standard Command Code Assigned ───────────────────────────────────────
    // Not every Imperial project had a Galaxy-wide broadcast command code — some
    // operations were initiated locally, on demand.  This action has no global
    // Eclipse command ID, so keybinding is handled at the menu/toolbar level only.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding purposes.
     * We return null here because the Batch Operation action doesn't have a
     * registered workbench command ID — it's only accessible via the menu.
     *
     * @return null — no global command ID is registered for this action
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Enough Raw Material To Start Building ───────────────────────────────────
    // Construction only begins once the Emperor confirms sufficient resources are
    // on-site: enough workers, enough raw material, a target to work on.  We check
    // that the user has a meaningful selection — either a completed search or at
    // least one selected entry, result, bookmark, attribute, or value to operate on.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Decides whether the Batch Operation action should be available right now.
     * We enable it when either a single search with results is selected, or when
     * the user has any entries, search results, bookmarks, attributes, or values
     * selected — basically, when there's something concrete to operate on.
     *
     * @return true if there's a valid selection to batch-operate on
     */
    public boolean isEnabled()
    {
        return getSelectedSearches().length == 1
            && getSelectedSearches()[0].getSearchResults() != null
            || getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length
                + getSelectedAttributes().length + getSelectedValues().length > 0;
    }


    // ── Construction Teams Mobilise ─────────────────────────────────────────────
    // When the Emperor gave the order, construction teams immediately mobilised:
    // prefab modules were loaded, supervisors briefed, and the wizard opened its
    // first page.  run() does the same: it instantiates the BatchOperationWizard,
    // wraps it in a WizardDialog, and opens it blocking so we wait for the user.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Batch Operation Wizard in a blocking dialog.
     * The wizard walks the user through choosing an operation type, configuring its
     * parameters, and then executing against the current selection.
     * We block on open so the action is effectively synchronous from Eclipse's
     * perspective — the menu item stays "in use" until the dialog closes.
     */
    public void run()
    {
        BatchOperationWizard wizard = new BatchOperationWizard();
        WizardDialog dialog = new WizardDialog( getShell(), wizard );
        dialog.setBlockOnOpen( true );
        dialog.create();
        dialog.open();
    }
}
