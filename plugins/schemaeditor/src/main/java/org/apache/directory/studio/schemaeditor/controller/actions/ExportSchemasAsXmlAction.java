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
import org.apache.directory.studio.schemaeditor.view.wizards.ExportSchemasAsXmlWizard;
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


// ── CLASS: ExportSchemasAsXmlAction — Clone Troopers File The Imperial Report ─
// After securing a system, clone troopers compile the intelligence into the
// standard Imperial XML data format and transmit it to the central archive —
// structured, machine-readable, ready for import anywhere in the Empire.
// This action does the same: it opens the wizard that serialises selected schemas
// into Apache Directory Studio's XML format so they can be shared or backed up.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Opens the ExportSchemasAsXmlWizard to write selected schemas as ADS XML files.
 * The XML format is Directory Studio's own portable schema representation — richer
 * than OpenLDAP files and round-trippable back into the tool via the import wizard.
 * Think of this as clone troopers encoding the captured data into the Imperial
 * standard report format and queuing it for the central archive.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasAsXmlAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── Trooper Takes The Reporting Assignment ────────────────────────────────
    // The reporting trooper accepts the assignment, notes which terminal holds the
    // source data, and stands ready to compile the report on command.
    // We store the viewer reference so we can collect the selection when the action fires.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportSchemasAsXmlAction wired to the given viewer.
     * We store the viewer to read the selected schemas at run-time; no selection
     * reading happens in the constructor.
     *
     * @param viewer  the TreeViewer showing schemas; we read its selection when the
     *                action fires to pre-populate the export wizard
     */
    public ExportSchemasAsXmlAction( TreeViewer viewer )
    {
        super( Messages.getString( "ExportSchemasAsXmlAction.SchemaAsXMLFileAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_EXPORT ) );
        setEnabled( true );
        this.viewer = viewer;
    }


    // ── Compile And Dispatch The Report ───────────────────────────────────────
    // The trooper pulls the selected records, compiles them into the Imperial XML
    // report template, and opens the dispatch channel so the operator can confirm
    // the destination archive before the report is sent.
    // We collect Schema nodes from the tree selection, pass them to the wizard,
    // and open the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Launches the ExportSchemasAsXmlWizard pre-loaded with the current schema selection.
     * We filter the tree selection to Schema nodes only — non-schema nodes in a mixed
     * selection are silently skipped since the XML export works at the schema level.
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
        ExportSchemasAsXmlWizard wizard = new ExportSchemasAsXmlWizard();
        wizard.setSelectedSchemas( selectedSchemas.toArray( new Schema[0] ) );
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── Order Relayed Through Imperial Comms ──────────────────────────────────
    // The reporting order arrives through the standard Imperial relay — same
    // compilation run, same wizard opened, regardless of which channel brought the signal.
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


    // ── Report Filed: Nothing Left To Release ─────────────────────────────────
    // The report is dispatched, the trooper stands down — no loose ends.
    // We hold no resources that need explicit cleanup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Trooper Reports In: No Window-Specific Orders ─────────────────────────
    // The trooper checks in at the reporting station but receives no additional
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
    // Background traffic on the Imperial network doesn't affect which schemas get
    // compiled — we read the viewer selection at run-time, not from this callback.
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
