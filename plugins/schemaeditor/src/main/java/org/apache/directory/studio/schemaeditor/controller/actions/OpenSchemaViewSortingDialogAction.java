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
import org.apache.directory.studio.schemaeditor.view.views.SchemaViewSortingDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenSchemaViewSortingDialogAction — Leia's Hologram Message ────────
// Hidden inside R2-D2, Princess Leia's hologram flickers to life: a small but
// critical message pops up, delivering exactly the information needed at that
// moment, then vanishes when acknowledged.
// Here, clicking "Sort..." pops up the SchemaViewSortingDialog — a compact,
// focused dialog that delivers sort-order configuration and disappears once the
// user confirms their choice.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the sorting configuration dialog for the Schema View.
 * This action lives in the Schema View's toolbar so the user can quickly choose
 * how schemas and their child elements are ordered in the tree.
 * Think of this as Leia's hologram: a small, purpose-built popup that delivers
 * its message and closes once you've acknowledged it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSchemaViewSortingDialogAction extends Action implements IWorkbenchWindowActionDelegate
{
    // ── R2-D2 Receives And Stores The Hologram ────────────────────────────────────
    // R2 is handed Leia's message in the corridors of Tantive IV: he stores it,
    // labels it, and readies the projector. Our constructor stores the label,
    // tooltip, and icon, and starts the action disabled (needs a project open).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenSchemaViewSortingDialogAction with label, tooltip, and icon set.
     * We start disabled; the Schema View controller enables us once there's something to sort.
     */
    public OpenSchemaViewSortingDialogAction()
    {
        super( Messages.getString( "OpenSchemaViewSortingDialogAction.SortingAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenSchemaViewSortingDialogAction.SortingToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SORTING ) );
        setEnabled( false );
    }


    // ── Leia's Hologram Flickers To Life ──────────────────────────────────────────
    // R2 projects the hologram into the dimly lit corridor — Leia appears, delivers
    // her message, and the moment the recipient acknowledges it, the projection ends.
    // We grab the display's active shell and open the SchemaViewSortingDialog; once
    // the user picks their sort order and clicks OK, the dialog closes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link SchemaViewSortingDialog} so the user can choose how to order
     * elements in the Schema View tree.
     * The dialog is modal — execution blocks here until the user closes it.
     */
    public void run()
    {
        SchemaViewSortingDialog svsd = new SchemaViewSortingDialog( PlatformUI.getWorkbench().getDisplay()
            .getActiveShell() );
        svsd.open();
    }


    // ── R2 Replays The Message On Request ─────────────────────────────────────────
    // A second officer asks to see the message — R2 plays it again without modification.
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
    // Once the mission is complete, R2 powers down the hologram projector — nothing
    // to release here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── R2 Is Assigned To This Corridor ───────────────────────────────────────────
    // R2 is rolled into corridor 7 of Tantive IV — no special per-corridor setup needed.
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


    // ── Corridor Occupancy Changes, Message Delivery Unaffected ──────────────────
    // Whether the corridor is empty or full of troopers doesn't change whether R2
    // can project the hologram — sorting is always available when enabled.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the workbench selection changes. This action doesn't gate on selection,
     * so we do nothing here.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
