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
package org.apache.directory.studio.schemaeditor.controller.actions;


import org.apache.directory.studio.schemaeditor.view.preferences.SchemaViewPreferencePage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenSchemaViewPreferenceAction — Palpatine Issues Order 66 ─────────
// Seated on the Senate throne, Palpatine broadcasts the encoded directive that
// reconfigures every aspect of Imperial operations in one sweep.
// Here, clicking "Preferences..." opens the Schema View's dedicated preference
// page — the control panel that reconfigures how schemas and their elements are
// displayed, grouped, and filtered from this point forward.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse preference page for the Schema View.
 * This action is wired to the "Preferences..." entry in the Schema View's toolbar
 * menu so the user can adjust Schema View display options without navigating the
 * full global preferences dialog.
 * Think of this as Palpatine transmitting his directive: one action opens the
 * central configuration page that shapes how the Schema View behaves.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSchemaViewPreferenceAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── Emperor's Comm Unit Stands Ready ──────────────────────────────────────────
    // Palpatine's holographic comm system is powered up and labeled before the
    // Senate session begins — all it needs is one activation to broadcast the order.
    // We set the label, tooltip, and start disabled (a project must be open before
    // schema view preferences are meaningful).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of OpenSchemaViewPreferenceAction with its label and tooltip set.
     * We start disabled; the controlling view enables us once a project is open.
     */
    public OpenSchemaViewPreferenceAction()
    {
        super( Messages.getString( "OpenSchemaViewPreferenceAction.PreferencesAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenSchemaViewPreferenceAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( false );
    }


    // ── The Order Goes Out Across The Holonet ────────────────────────────────────
    // Palpatine speaks: the encrypted signal travels through every relay station
    // and the configuration change takes effect galaxy-wide.
    // We find the current active shell and open the Schema View preference page
    // directly — no detour through the global preferences tree.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Schema View preference dialog, navigating straight to our page.
     * We use {@link PreferencesUtil#createPreferenceDialogOn} so the user lands
     * exactly on the Schema View settings without having to find them manually.
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        PreferencesUtil.createPreferenceDialogOn( shell, SchemaViewPreferencePage.ID, new String[]
            { SchemaViewPreferencePage.ID }, null ).open();
    }


    // ── Junior Officer Relays The Broadcast ──────────────────────────────────────
    // A communications officer re-transmits the Emperor's message down the chain
    // of command — no modification, just a faithful pass-through.
    // This IAction overload lets the Eclipse command framework invoke us; we simply
    // delegate to our no-arg run().
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} so Eclipse's workbench action framework can invoke us.
     *
     * @param action  the IAction proxy passed by the framework; we ignore it
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Comm Unit Powers Down ────────────────────────────────────────────────────
    // After Order 66 is complete, the Emperor's comm unit is powered down and stored —
    // no lingering connections to close here either.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Emperor Is Assigned His Senate Chamber ────────────────────────────────────
    // Palpatine is shown to his seat in the Senate — window-specific context handed
    // over, but he needs no special configuration per window.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when this action is bound to a workbench window. We need no window-specific
     * initialization.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Senate Vote Changes, Emperor Ignores It ───────────────────────────────────
    // The Senate votes shift, but Palpatine's order is already decided — the
     // selection in the UI doesn't affect whether preferences can be opened.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the workbench selection changes. We don't react to selection for
     * this action, so this is intentionally empty.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
