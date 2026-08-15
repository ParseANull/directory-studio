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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.view.wizards.ExportSchemasForADSWizard;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportSchemasForADSAction — Clone Troopers Supply The Fleet ────────
// After Order 66, Imperial supply corps package resources in the format each
// specific fleet installation requires — not the generic Imperial format, but the
// exact layout expected by Apache Directory Server's deployment pipeline.
// This action does the same: it opens the wizard that exports schemas in the
// format ADS (Apache Directory Server) needs to consume them directly.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Opens the ExportSchemasForADSWizard to write selected schemas in Apache Directory Server format.
 * ADS uses a specific layout for its schema partition files — this action gets the
 * user into the wizard that handles that specialised serialisation.
 * Think of this as clone troopers preparing supply packages in the exact configuration
 * that each front-line installation can plug in without modification.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasForADSAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── Supply Corps Accepts The Packaging Assignment ─────────────────────────
    // The supply trooper notes which depot holds the raw schema materials and
    // prepares the appropriate packaging template — everything is ready to go
    // the moment the order arrives.
    // We store the viewer reference so we can collect the selection at run-time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportSchemasForADSAction wired to the given viewer.
     * We store the viewer to read selected schemas when the action fires; no
     * selection reading happens in the constructor.
     *
     * @param viewer  the TreeViewer showing schemas; we read its selection when the
     *                action fires to pre-populate the ADS export wizard
     */
    public ExportSchemasForADSAction( TreeViewer viewer )
    {
        super( Messages.getString( "ExportSchemasForADSAction.SchemaForADSAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_EXPORT_FOR_ADS ) );
        setEnabled( true );
        this.viewer = viewer;
    }


    // ── Package And Dispatch To The ADS Installation ──────────────────────────
    // The supply corps pulls the selected crates from the depot, packages them
    // in the ADS-specific format, and opens the dispatch hatch so the operator
    // can confirm the destination before shipment.
    // We collect Schema nodes, pass them to the ADS wizard, and open the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Launches the ExportSchemasForADSWizard pre-loaded with the current schema selection.
     * We filter the tree selection to Schema nodes only; non-schema nodes are silently
     * skipped since the ADS export operates at the schema granularity.
     */
    public void run()
    {
        List<Schema> selectedSchemas = new ArrayList<Schema>();
        // Getting the selection
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() > 0 ) )
        {
            for ( Iterator<?> i = selection.iterator(); i.hasNext(); )
            {
                Object o = i.next();
                if ( o instanceof SchemaWrapper )
                {
                    selectedSchemas.add( ( ( SchemaWrapper ) o ).getSchema() );
                }
            }
        }

        // Instantiates and initializes the wizard
        ExportSchemasForADSWizard wizard = new ExportSchemasForADSWizard();
        wizard.setSelectedSchemas( selectedSchemas.toArray( new Schema[0] ) );
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── Order Relayed Through Supply Chain Comms ───────────────────────────────
    // The packaging order arrives via the standard Imperial supply-chain relay —
    // same packages assembled, same wizard opened, regardless of the channel.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when called via the workbench action delegate channel.
     *
     * @param action  the workbench action proxy; we ignore it and call our own run()
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Shipment Dispatched: Nothing Left To Release ───────────────────────────
    // The hatch closes, the supply corps stands down — no loose ends, no gear to
    // hand back. We hold no resources that need explicit cleanup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Supply Corps Reports In: No Window-Specific Orders ────────────────────
    // The trooper checks in at the depot command post but receives no additional
    // instructions tied to this workbench window — standing orders suffice.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op init — we don't need the workbench window reference.
     *
     * @param window  the workbench window; not used here
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Comms Idle: Workbench Selection Ignored ────────────────────────────────
    // Depot chatter doesn't change which schemas get packaged — we read the viewer
    // selection at run-time, not from this callback.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we read selection directly from the viewer when we run, not here.
     *
     * @param action     the workbench action proxy; not used
     * @param selection  the workbench selection; not used
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
