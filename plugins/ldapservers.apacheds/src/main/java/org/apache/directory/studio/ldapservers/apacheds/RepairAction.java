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

package org.apache.directory.studio.ldapservers.apacheds;


import org.apache.directory.studio.ldapservers.jobs.StudioLdapServerJob;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


// ── CLASS: RepairAction — Han Calling for the Hyperdrive Repair Crew ──────────
// When the Falcon's partitions are corrupted and the ship won't fly, Han
// right-clicks the ship in the docking bay list and selects "Repair."  The
// Repair action checks that the selected ship is really the Falcon (ApacheDS
// 2.0.0), verifies it's stopped (you can't repair a running engine), then
// dispatches a RepairRunnable through a StudioLdapServerJob.
// RepairAction is that right-click context menu entry: an IObjectActionDelegate
// wired to the Servers view right-click menu for stopped ApacheDS 2.0.0 servers.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse action that triggers a partition repair for an ApacheDS 2.0.0 server.
 * Enabled only when the selected server is stopped.
 * On {@link #run}, it verifies the server type via {@link ExtensionUtils},
 * then schedules a {@link RepairRunnable} through a {@link StudioLdapServerJob}.
 * Think of this as Han Solo calling the repair crew for the Falcon's hyperdrive
 * after a bad shutdown corrupted the partition files.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RepairAction implements IObjectActionDelegate
{
    /** The {@link ServersView} */
    private ServersView view;


    // ── Han Gives the Order — Dispatch the Repair Crew ───────────────────────
    // We get the selected server, verify it's the right type, create a
    // RepairRunnable, and hand it to a StudioLdapServerJob for background
    // execution.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs the repair action.
     * Gets the selected server, verifies it is an ApacheDS 2.0.0 server, and
     * schedules a {@link RepairRunnable} via {@link StudioLdapServerJob}.
     *
     * @param action  the triggering action (not used directly).
     */
    @Override
    public void run( IAction action )
    {
        LdapServer server = getSelectedServer();
        if ( server != null )
        {
            // Checking that the server is really an ApacheDS 2.0.0 server
            if ( !ExtensionUtils.verifyApacheDs200OrPrintError( server, view ) )
            {
                return;
            }

            // Creating and scheduling the job to start the server
            StudioLdapServerJob job = new StudioLdapServerJob( new RepairRunnable( server ) );
            job.schedule();
        }
    }


    // ── Enable the Action Only When the Ship Is Docked ───────────────────────
    // We can only repair a stopped server; a running engine can't be opened up.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the enabled state of the action.
     * The action is enabled only when exactly one server is selected and its
     * status is {@link LdapServerStatus#STOPPED}.
     *
     * @param action     the action to enable/disable.
     * @param selection  the current selection (unused — we query the view directly).
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        LdapServer server = getSelectedServer();
        action.setEnabled( server != null && server.getStatus() == LdapServerStatus.STOPPED );
    }


    // ── Connect This Action to the Servers View ───────────────────────────────
    // Eclipse calls this when the context menu is being built; we capture the
    // Servers view reference so we can reach its viewer for the selection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores a reference to the {@link ServersView} when this action is
     * associated with it.
     *
     * @param action      the action (unused).
     * @param targetPart  the workbench part; stored if it is a {@link ServersView}.
     */
    public void setActivePart( IAction action, IWorkbenchPart targetPart )
    {
        // Storing the Servers view
        if ( targetPart instanceof ServersView )
        {
            view = ( ServersView ) targetPart;
        }
    }


    // ── Find Out Which Ship Is in the Docking Bay ─────────────────────────────
    // We query the Servers view's viewer for a single-element selection and
    // return the LdapServer it represents, or null if nothing is selected.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the single currently selected {@link LdapServer}, or {@code null}
     * if no server is selected or if more than one is selected.
     *
     * @return  the selected server, or {@code null}.
     */
    private LdapServer getSelectedServer()
    {
        if ( view != null )
        {
            // Getting the selection
            StructuredSelection selection = ( StructuredSelection ) view.getViewer().getSelection();
            if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
            {
                // Getting the server
                LdapServer server = ( LdapServer ) selection.getFirstElement();
                return server;
            }
        }

        return null;
    }

}
