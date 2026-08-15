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
import org.apache.directory.studio.schemaeditor.view.views.SearchViewSortingDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenSearchViewSortingDialogAction — Leia's Hologram Message ────────
// Deep in the Tantive IV's corridors, R2-D2 projects Princess Leia's hologram:
// a small, urgent popup that appears, delivers its precise message — "Help me,
// Obi-Wan" — and disappears once acknowledged.
// Here, clicking "Sort..." pops up the SearchViewSortingDialog — a compact,
// focused popup that lets the user configure result ordering, then vanishes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the sorting configuration dialog for the Search View.
 * This action lives in the Search View's toolbar so the user can quickly choose
 * how search results are ordered in the results list.
 * Think of this as Leia's hologram: a small, purpose-built popup that delivers
 * its message and closes once the user has made their choice.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSearchViewSortingDialogAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── R2 Stores The Hologram And Readies The Projector ──────────────────────────
    // R2 receives the message in the Tantive IV corridor, tucks it away, and has
    // the projector lens ready for when it's needed. We set up label, tooltip,
    // and icon, and start enabled — sorting is available as soon as the view loads.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenSearchViewSortingDialogAction with label, tooltip, and icon set.
     * We start enabled immediately since sorting can be configured without a project open.
     */
    public OpenSearchViewSortingDialogAction()
    {
        super( Messages.getString( "OpenSearchViewSortingDialogAction.SortingAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenSearchViewSortingDialogAction.SortingToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SORTING ) );
        setEnabled( true );
    }


    // ── Leia's Message Flickers Into The Corridor ─────────────────────────────────
    // R2 projects the hologram: Leia's image appears, speaks her message about the
    // plans, and waits for Obi-Wan (the user) to absorb the information and confirm.
    // We open the SearchViewSortingDialog on the active shell; it blocks until the
    // user selects their sort order and closes it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link SearchViewSortingDialog} so the user can configure how search
     * results are ordered.
     * The dialog is modal — execution waits here until the user closes it.
     */
    public void run()
    {
        SearchViewSortingDialog svsd = new SearchViewSortingDialog( PlatformUI.getWorkbench().getDisplay()
            .getActiveShell() );
        svsd.open();
    }


    // ── R2 Replays On Request ─────────────────────────────────────────────────────
    // A second officer asks to see the message — R2 plays it again faithfully.
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


    // ── Projector Powers Down ─────────────────────────────────────────────────────
    // Once the mission is delivered, R2 powers down the hologram projector — nothing
    // to release.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── R2 Is Assigned To The Escape Pod Bay ─────────────────────────────────────
    // R2 is rolled to his assigned bay — no special per-bay setup needed.
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


    // ── Corridor Occupancy Irrelevant To Projection ───────────────────────────────
    // Whether the corridor is busy or empty doesn't affect R2's ability to project
    // the hologram — sort configuration is always available when enabled.
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
