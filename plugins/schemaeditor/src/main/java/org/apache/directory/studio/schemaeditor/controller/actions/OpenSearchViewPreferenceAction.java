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


import org.apache.directory.studio.schemaeditor.view.preferences.SearchViewPreferencePage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenSearchViewPreferenceAction — Palpatine Issues Order 66 ─────────
// In the Chancellor's office, Palpatine reaches across to the control panel and
// transmits the encrypted directive that reshapes how every unit in the galaxy
// operates — one centralized command, immediate system-wide effect.
// Here, clicking "Preferences..." on the Search View opens the dedicated
// preference page that controls how search results are displayed and filtered,
// reconfiguring the Search View's behavior from that moment on.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse preference page for the Search View.
 * This action is wired to the "Preferences..." entry in the Search View's toolbar menu
 * so the user can adjust search display settings directly, without hunting through
 * the global Preferences dialog.
 * Think of this as Palpatine's directive: one click opens the command center that
 * reconfigures how the Search View behaves going forward.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSearchViewPreferenceAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── Emperor's Comm System Is Activated ───────────────────────────────────────
    // Before the order goes out, the comm system is powered on and set to the right
    // frequency. We set the label and tooltip, and enable immediately — search
    // preferences are accessible even without a project loaded.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenSearchViewPreferenceAction with its label and tooltip configured.
     * We start enabled immediately — preferences can be opened regardless of project state.
     */
    public OpenSearchViewPreferenceAction()
    {
        super( Messages.getString( "OpenSearchViewPreferenceAction.PreferencesAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenSearchViewPreferenceAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── Order Transmitted, Configuration Locked In ────────────────────────────────
    // Palpatine speaks and the signal propagates instantly — we find the active shell
    // and open the Search View preference page directly using Eclipse's PreferencesUtil.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Search View preference dialog, navigating straight to our page.
     * We use {@link PreferencesUtil#createPreferenceDialogOn} so the user sees only
     * the Search View settings, not the full preferences tree.
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        PreferencesUtil.createPreferenceDialogOn( shell, SearchViewPreferencePage.ID, new String[]
            { SearchViewPreferencePage.ID }, null ).open();
    }


    // ── Officer Relays The Command ────────────────────────────────────────────────
    // A subordinate officer repeats Palpatine's message down the chain of command —
    // no changes, just faithful delegation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} so Eclipse's command framework can invoke us.
     *
     * @param action  the IAction proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Comm System Powers Down After Transmission ────────────────────────────────
    // The order has been sent; the comm system shuts down — nothing to release here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Emperor Assigned His Senate Office ───────────────────────────────────────
    // Palpatine is shown to the Chancellor's suite — no per-window configuration needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when this action is bound to a workbench window. No per-window setup needed.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Senate Votes Shift, Order Stands ─────────────────────────────────────────
    // The Senate reacts, but Palpatine's directive is independent of their vote —
    // selection changes don't affect whether this action is available.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the workbench selection changes. We don't gate on selection, so
     * this is intentionally empty.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
