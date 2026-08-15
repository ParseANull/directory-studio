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


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.views.SearchView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: RunCurrentSearchAgainAction — R2-D2 Re-Queries The Death Star Terminal ────────────
// R2-D2 is already plugged into the Death Star's computer, interface arm extended.
// Sometimes the data he pulled the first time is stale, or the droids want a fresh pass
// over the same query — so he reissues the exact same search without unplugging.
// This action does the same: re-runs whatever search the SearchView last executed.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * An action that tells the {@link SearchView} to re-execute its most recent search query.
 * It's useful when the schema data may have changed since the last search and you want
 * fresh results without having to retype or reconfigure the search parameters.
 * Think of this class as R2-D2 reissuing his last query to the Death Star terminal —
 * same input, fresh data pull.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RunCurrentSearchAgainAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private SearchView view;


    // ── R2 Extends His Interface Arm And Connects ─────────────────────────────────────────────
    // R2-D2 rolls up to the Death Star terminal, extends his scomp link, and registers
    // as the re-query handler for this particular console (SearchView).
    // He sets himself to always-enabled — there's always something to re-run.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of RunCurrentSearchAgainAction, bound to the given SearchView.
     * We initialise the button label, tooltip, and icon from plugin constants, then store
     * a reference to the view so run() can delegate to it directly.
     *
     * <p>For example — R2 plugs in and readies the re-query channel:</p>
     * <pre>
     *   R2-D2: *bweep* (interface connected)
     *   "Search view registered. Ready to re-run on command."
     * </pre>
     *
     * @param view  the SearchView whose current search will be re-executed when the action fires
     */
    public RunCurrentSearchAgainAction( SearchView view )
    {
        super( Messages.getString( "RunCurrentSearchAgainAction.RerunSearchAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_RUN_CURRENT_SEARCH_AGAIN ) );
        setEnabled( true );
        this.view = view;
    }


    // ── R2 Re-Issues The Last Query To The Terminal ───────────────────────────────────────────
    // R2 taps the same query sequence he sent moments ago — no new input required.
    // The Death Star terminal responds with a fresh data pull using the same search criteria.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires the action, asking the SearchView to re-run whatever search it last performed.
     * We simply delegate to {@code view.runCurrentSearchAgain()} — the view owns the search
     * state and knows how to replay it.
     *
     * <p>For example — R2 reissues the terminal command:</p>
     * <pre>
     *   R2-D2: *chirp* → view.runCurrentSearchAgain()
     *   Terminal: "Re-running query... results refreshed."
     * </pre>
     */
    public void run()
    {
        view.runCurrentSearchAgain();
    }


    // ── R2 Responds To The Workbench Relay Signal ─────────────────────────────────────────────
    // When Eclipse routes the trigger through the IWorkbenchWindowActionDelegate path,
    // R2 treats it as the same re-query command and fires his main run() without complaint.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when this action fires via the workbench action delegate path.
     * The {@code action} parameter is Eclipse's proxy wrapper; we ignore it.
     *
     * @param action  the workbench action proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── R2 Retracts His Interface Arm, No Cleanup Needed ─────────────────────────────────────
    // R2 withdraws the scomp link cleanly — no dangling connections, no resources to release.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes this action when the workbench is done with it.
     * We hold no resources requiring explicit release, so this is intentionally empty.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── R2 Acknowledges Window Assignment With A Beep ─────────────────────────────────────────
    // The window tells R2 which terminal bay he's operating in, but R2 already has his
    // view reference from the constructor — no additional wiring needed.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is associated with a workbench window.
     * We don't need the window reference — the SearchView was passed in at construction time.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── R2 Ignores The Workbench Selection Broadcast ──────────────────────────────────────────
    // R2 doesn't adjust his re-query behaviour based on what's selected in other views —
    // he's always ready to re-run, regardless of selection state.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * This action is always enabled and doesn't respond to selection changes, so this is empty.
     *
     * @param action     the workbench action proxy; unused
     * @param selection  the current workbench selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
