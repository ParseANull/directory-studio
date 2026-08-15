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


import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import org.apache.directory.studio.openldap.config.editor.DirectoryServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;


// ── CLASS: OpenDirectoryConfigurationAction — Han Plots Coordinates From A Map ─
// In Episode IV, Han Solo doesn't always jump from a connection he already has —
// sometimes he's working from a star chart, picking coordinates off a physical
// map. He asks "where do you want to go?" (a directory dialog), the crew points
// to a location on the chart (the slapd.d folder), and then he makes the jump.
// This action is the filesystem-based cousin of OpenConfigurationAction: instead
// of opening the editor from a live LDAP connection, we ask the user to pick their
// slapd.d directory on disk, then open the editor backed by that directory.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse window-level action that opens the OpenLDAP configuration editor
 * from a local filesystem directory (the slapd.d directory). The user picks a
 * directory via a dialog, and we open the editor with a
 * {@link DirectoryServerConfigurationInput} pointing at it.
 * Unlike {@link OpenConfigurationAction} (which works with a live LDAP connection),
 * this action works with the config files already on disk — useful when you have
 * a copy of the slapd.d directory locally and don't need a live server.
 * Think of us as Han picking coordinates off a star chart: the destination is
 * chosen by browsing, not by an active connection.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenDirectoryConfigurationAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The active workbench window — we need its shell to parent dialogs. */
    private IWorkbenchWindow window;


    // ── Han Takes The Helm For This Window ────────────────────────────────────
    // The Millennium Falcon's controls are connected to a specific cockpit window.
    // Eclipse calls init() to tell us which workbench window we're attached to,
    // so we can parent dialogs correctly and open editors in the right page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse to bind this action to a specific workbench window.
     * We store the window so we can access its shell for dialog parenting and
     * its active page for opening the editor.
     *
     * @param window  the workbench window this action is running within
     */
    public void init( IWorkbenchWindow window )
    {
        this.window = window;
    }


    // ── Han Asks "Where Are We Going?" Then Makes The Jump ────────────────────
    // Han opens the star charts (a directory chooser dialog), asks the crew to
    // point out their destination (the slapd.d folder), and the moment they confirm
    // he punches it — opening the configuration editor backed by that directory.
    // If the user cancels the dialog we do nothing; if the editor can't open we
    // silently swallow the exception (it's practically impossible to hit).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fires the action: opens a directory chooser dialog so the user can browse to
     * their slapd.d folder, then opens the OpenLDAP configuration editor backed by
     * that directory. If the user cancels the dialog we exit quietly. The
     * {@link PartInitException} is swallowed because the editor ID is always valid
     * and this case can't realistically occur.
     *
     * @param action  the Eclipse action proxy (we don't use it directly)
     */
    public void run( IAction action )
    {
        // Creating a directory dialog for the user
        // to select the "slapd.d" directory
        DirectoryDialog dialog = new DirectoryDialog( window.getShell(), SWT.OPEN );
        dialog.setText( "Choose 'slapd.d' folder..." );
        dialog.setFilterPath( System.getProperty( "user.home" ) );

        // Getting the directory selected by the user
        String selectedDirectory = dialog.open();

        if ( selectedDirectory != null )
        {
            try
            {
                window.getActivePage().openEditor( new DirectoryServerConfigurationInput( new File(
                    selectedDirectory ) ), OpenLdapServerConfigurationEditor.ID );
            }
            catch ( PartInitException e )
            {
                // Should never happen
            }
        }
    }


    // ── Han Doesn't Care What The Galaxy Is Doing While He Flies ─────────────
    // Han Solo tunes out the background chatter — he's focused on flying. Eclipse
    // notifies us when the selection changes, but we don't need to track it for
    // this action (unlike OpenConfigurationAction, we don't depend on a selection).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes. We don't depend on
     * any selection for this action — the user picks the directory from a dialog —
     * so this is a no-op.
     *
     * @param action     the Eclipse action proxy
     * @param selection  the current selection (ignored)
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // TODO Auto-generated method stub

    }


    // ── Han Parks The Ship When The Mission Is Done ────────────────────────────
    // When the action is decommissioned by Eclipse, we could release resources.
    // We hold no resources, so this is a no-op — Han just walks away from the
    // cockpit when the flight is over.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being disposed. We hold no
     * resources that need releasing, so this is a no-op.
     */
    public void dispose()
    {
        // TODO Auto-generated method stub

    }
}
