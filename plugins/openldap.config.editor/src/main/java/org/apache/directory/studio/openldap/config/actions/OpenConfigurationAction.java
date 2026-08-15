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

package org.apache.directory.studio.openldap.config.actions;


import org.apache.directory.studio.connection.core.Connection;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import org.apache.directory.studio.openldap.config.editor.ConnectionServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;


// ── CLASS: OpenConfigurationAction — Han Solo Jumps To Hyperspace ─────────────
// In Episode IV, Han Solo sits at the Millennium Falcon's controls, punches in
// the coordinates for Alderaan, and yanks the hyperspace lever. The ship makes
// the jump instantly — no lengthy preparation, just the connection from their
// current location to their destination, seamlessly bridged.
// This action does the same thing: the user has selected a Connection in the
// Directory Studio browser, they click "Open Configuration", and we launch the
// OpenLDAP configuration editor backed by that live connection — jumping from
// the LDAP browser view straight into the config editor in one move.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse object action (context-menu entry) that opens the OpenLDAP
 * configuration editor for the currently selected LDAP connection.
 * When the user right-clicks a connection in the Directory Studio connection view
 * and chooses "Open Configuration", we package that connection into a
 * {@link ConnectionServerConfigurationInput} and open the
 * {@link OpenLdapServerConfigurationEditor} with it.
 * Think of us as Han at the Falcon's controls: we bridge the gap between
 * "user has a connection" and "user is editing the server config" in one clean jump.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenConfigurationAction implements IObjectActionDelegate
{
    /** The connection the user has selected in the browser; null if nothing relevant is selected. */
    private Connection selectedConnection;


    // ── Han Pulls The Hyperspace Lever ────────────────────────────────────────
    // Han's got the coordinates locked in and the selected connection cached.
    // He pulls the lever — we open the OpenLDAP configuration editor backed by
    // the selected connection. If there's no valid connection selected, we do
    // nothing (Han doesn't jump without coordinates).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires the action: opens the OpenLDAP configuration editor for the selected
     * connection. If no connection is selected, this is a no-op. The exception
     * catch is intentional — the framework guarantees the editor ID is valid so
     * this should never throw in practice.
     *
     * @param action  the Eclipse action proxy (we don't use it directly)
     */
    public void run( IAction action )
    {
        if ( selectedConnection != null )
        {
            try
            {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .openEditor( new ConnectionServerConfigurationInput( selectedConnection ),
                        OpenLdapServerConfigurationEditor.ID );
            }
            catch ( Exception e )
            {
                // Will never occur.
            }
        }
    }


    // ── Han Confirms The Destination Before Jumping ───────────────────────────
    // Before Han jumps to hyperspace he checks what destination the crew has
    // actually selected — is there exactly one selection, and is it a valid
    // Connection object? We cache it if so, clear it if not, so run() knows
    // whether there's a valid target.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the user's selection changes in the view.
     * We accept the selection only when exactly one item is selected and it's
     * a {@link Connection} instance. Anything else clears the cached connection
     * so run() becomes a no-op.
     *
     * @param action     the Eclipse action proxy
     * @param selection  the current selection in the active view
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        StructuredSelection structuredSelection = ( StructuredSelection ) selection;

        if ( ( structuredSelection.size() == 1 ) && ( structuredSelection.getFirstElement() instanceof Connection ) )
        {
            selectedConnection = ( Connection ) structuredSelection.getFirstElement();
        }
        else
        {
            selectedConnection = null;
        }
    }


    // ── Han Doesn't Care Which Part Of The Ship He's In ──────────────────────
    // Han Solo doesn't need to know which cockpit seat he's using — he just flies.
    // We don't need a reference to the containing workbench part; this method
    // is part of the IObjectActionDelegate contract but we have nothing to do here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to give us a reference to the part containing the selection.
     * We don't need it — everything we need comes through run() and selectionChanged().
     *
     * @param action      the Eclipse action proxy
     * @param targetPart  the workbench part that contains the current selection
     */
    public void setActivePart( IAction action, IWorkbenchPart targetPart )
    {
        // Nothing to do
    }
}
