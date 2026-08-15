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
import org.apache.directory.studio.schemaeditor.view.wizards.ExportSchemasAsOpenLdapWizard;
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


// ── CLASS: ExportSchemasAsOpenLdapAction — Clone Troopers Transmit Intel ─────
// After Order 66, Imperial Intelligence encodes captured Jedi records into the
// standard Imperial format and dispatches them to remote outposts via secure
// transmission — the data leaves in a form the recipients can use directly.
// This action does the same: it takes selected schemas and opens the wizard that
// writes them out as OpenLDAP .schema files, the native format those servers expect.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Opens the ExportSchemasAsOpenLdapWizard to write selected schemas as OpenLDAP .schema files.
 * OpenLDAP has its own schema file format — this action gets the user into the wizard
 * that handles that conversion and saves the results to disk.
 * Think of this as Imperial Intelligence encoding records into the standard
 * outpost format and queueing the transmission.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasAsOpenLdapAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TreeViewer viewer;


    // ── Intel Officer Takes The Assignment ────────────────────────────────────
    // The Imperial intelligence officer accepts the encoding assignment and notes
    // which data terminal holds the source records — ready to pull them at a moment's notice.
    // We store the viewer reference so we can read the selection when the action fires.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportSchemasAsOpenLdapAction wired to the given viewer.
     * We store the viewer to read the selected schemas at run-time; no actual
     * selection reading happens in the constructor.
     *
     * @param viewer  the TreeViewer showing schemas; we read its selection when the
     *                action fires to pre-populate the export wizard
     */
    public ExportSchemasAsOpenLdapAction( TreeViewer viewer )
    {
        super( Messages.getString( "ExportSchemasAsOpenLdapAction.SchemaAsOpenLDAPFilesAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_EXPORT ) );
        setEnabled( true );
        this.viewer = viewer;
    }


    // ── Encode And Transmit The Records ───────────────────────────────────────
    // The officer pulls the selected Jedi records, encodes them into the outpost
    // format, and opens the transmission channel so the operator can confirm the
    // destination before sending.
    // We collect selected Schema objects (filtering out non-schema tree nodes),
    // pass them to the wizard, and open the dialog.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Launches the ExportSchemasAsOpenLdapWizard pre-loaded with the current schema selection.
     * We filter the tree selection down to Schema nodes only — other selected nodes
     * (attribute types, object classes) are not exportable at the schema-file level
     * and are silently skipped.
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
        ExportSchemasAsOpenLdapWizard wizard = new ExportSchemasAsOpenLdapWizard();
        wizard.setSelectedSchemas( selectedSchemas.toArray( new Schema[0] ) );
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── Order Relayed Through Imperial Comms ──────────────────────────────────
    // The transmission order arrives through the standard Imperial relay — same
    // encoding run, same wizard opened, regardless of which channel carried the signal.
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


    // ── Transmission Complete: Nothing Left To Release ─────────────────────────
    // The channel is closed, the officer stands down — nothing to hand back.
    // We hold no resources that need explicit cleanup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Officer Reports In: No Window-Specific Orders ─────────────────────────
    // The officer checks in at the comm station but receives no additional instructions
    // tied to this workbench window — standing orders suffice.
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
    // Background traffic on the Imperial comm network doesn't affect which schemas
    // get encoded — we read the viewer selection at run-time, not here.
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
