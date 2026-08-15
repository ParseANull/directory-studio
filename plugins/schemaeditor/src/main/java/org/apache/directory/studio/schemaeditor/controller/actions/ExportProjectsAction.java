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
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.view.wizards.ExportProjectsWizard;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportProjectsAction — Clone Troopers Dispatch Secure Cargo ───────
// After Order 66, Imperial forces move systematically to secure and ship important
// resources out of the field and into the Emperor's archives.
// This action does the same: it packages selected schema projects and opens the
// ExportProjectsWizard so the user can ship them out to a file on disk.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Opens the ExportProjectsWizard to export one or more schema projects to disk.
 * We collect whatever projects the user has selected in the viewer and pre-load
 * them into the wizard so the user doesn't have to re-select them on the first page.
 * Think of this as Imperial cargo troopers loading selected crates onto the shuttle
 * before the pilot even opens the ramp.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportProjectsAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TableViewer viewer;


    // ── Cargo Trooper Takes The Manifest ─────────────────────────────────────
    // The cargo trooper accepts the shipping manifest and notes which bay holds
    // the crates — he's ready to load them the moment the order comes through.
    // We store the viewer reference so we can inspect the selection at run-time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportProjectsAction wired to the given viewer.
     * We just store the viewer here; the actual selection read happens when the
     * user triggers the action via {@link #run()}.
     *
     * @param viewer  the TableViewer listing projects; we read its selection when
     *                the action fires to pre-populate the export wizard
     */
    public ExportProjectsAction( TableViewer viewer )
    {
        super( Messages.getString( "ExportProjectsAction.SchemaProjectsAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_EXPORT ) );
        setEnabled( true );
        this.viewer = viewer;
    }


    // ── Load The Crates And Open The Ramp ─────────────────────────────────────
    // The cargo trooper reads the manifest, pulls the selected crates from the bay,
    // and loads them onto the shuttle — then the pilot opens the ramp so the
    // operator can confirm the shipment before departure.
    // We collect the selected Project objects, hand them to the wizard, then open
    // the WizardDialog so the user can configure the export destination.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Launches the ExportProjectsWizard pre-loaded with the current selection.
     * Any projects selected in the viewer are passed to the wizard as the default
     * export set; the user can adjust them on the first wizard page.
     */
    public void run()
    {
        List<Project> selectedProjects = new ArrayList<Project>();
        // Getting the selection
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() > 0 ) )
        {
            for ( Iterator<?> i = selection.iterator(); i.hasNext(); )
            {
                selectedProjects.add( ( ( ProjectWrapper ) i.next() ).getProject() );
            }
        }

        // Instantiates and initializes the wizard
        ExportProjectsWizard wizard = new ExportProjectsWizard();
        wizard.setSelectedProjects( selectedProjects.toArray( new Project[0] ) );
        wizard.init( PlatformUI.getWorkbench(), StructuredSelection.EMPTY );
        // Instantiates the wizard container with the wizard and opens it
        WizardDialog dialog = new WizardDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard );
        dialog.create();
        dialog.open();
    }


    // ── Relay Through Imperial Comms ──────────────────────────────────────────
    // The shipping order arrives via the standard Imperial logistics channel — same
    // action, same crates loaded, regardless of which comm brought the signal.
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


    // ── Shuttle Departed: Nothing Left To Clean Up ─────────────────────────────
    // The cargo bay is empty, the shuttle is gone — nothing to hand back.
    // We hold no resources that need explicit cleanup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Trooper Reports To Bay: No Window-Specific Orders ─────────────────────
    // The cargo trooper checks in at the bay but receives no special instructions
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
    // The loading dock chatter doesn't affect the shipping manifest — we read the
    // viewer selection at run-time, not from the workbench selection event.
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
