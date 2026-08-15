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
package org.apache.directory.studio.ldapservers.actions;


import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: RenameAction — LANDO CHANGES CLOUD CITY'S CALL SIGN ───────────────────────────────
// After joining the Rebellion and evacuating Cloud City, Lando Calrissian sits down at the
// Millennium Falcon's comm panel and reprograms Cloud City's transponder beacon with a new
// call sign — because the Empire is already scanning for the old one.
// He proposes a new name, checks the Alliance registry to make sure no other ship is already
// using it, and commits the change once everything looks clean.
// That is exactly what we do here: prompt the user for a new server name, validate it
// against the existing server list, and write it to the server object if it passes.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse Action that renames the currently selected LDAP server.
 * We pop an {@link InputDialog} pre-filled with the server's current name, validate the
 * proposed new name (it must be non-empty and not already in use), and — if the user
 * confirms — update the server object in place via {@link LdapServer#setName}.
 * Think of this class as Lando reprogramming Cloud City's transponder: you propose a
 * call sign, the system checks the registry, and if the name is free you commit it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private ServersView view;


    // ── LANDO SITS DOWN AT THE COMM PANEL ────────────────────────────────────────────────────
    // Lando drops into the pilot's seat aboard the Millennium Falcon after the evacuation,
    // pulls up the transponder configuration screen, and wires the renaming controls to the
    // specific server list he just inherited from Han.
    // We set up the action's labels, tooltip, and command IDs, then store the view reference
    // so run() knows exactly which list to pull the selected server from.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code RenameAction} wired to the given servers view.
     * This is the only constructor — renaming always requires a specific view to know which
     * server the user has selected.  We set the display text, tooltip, and Eclipse command
     * IDs (so the keyboard shortcut declared in {@code plugin.xml} works).
     *
     * <p>For example — Lando powers up the transponder console with the fleet roster open:</p>
     * <pre>
     *   RenameAction action = new RenameAction( serversView );
     *   // action is labelled "Rename", tooltip set, command ID wired — ready to fire
     * </pre>
     *
     * @param view  The {@link ServersView} whose selection we will rename —
     *              Lando's roster of currently tracked Cloud City frequencies.
     */
    public RenameAction( ServersView view )
    {
        this.view = view;
        setText( Messages.getString( "RenameAction.Rename" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "RenameAction.RenameToolTip" ) ); //$NON-NLS-1$
        setId( LdapServersPluginConstants.CMD_RENAME );
        setActionDefinitionId( LdapServersPluginConstants.CMD_RENAME );
    }


    // ── LANDO PROPOSES THE NEW CALL SIGN AND CHECKS THE REGISTRY ─────────────────────────────
    // Lando types a proposed call sign into the Alliance comm registry, waits for the
    // validator to confirm it isn't already in use by another ship, then commits the change.
    // If the proposed name is identical to the current one the validator waves it through;
    // if another ship already has that name the validator displays an error and blocks the save.
    // We open an {@link InputDialog} with exactly that inline validator, and update the server
    // object if the user clicks OK with a valid new name.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the rename action: reads the selected server, opens an input dialog
     * pre-filled with the current name, validates the proposed new name, and — if
     * valid and confirmed — applies it to the server object.
     * The inline validator allows the original name (no-op rename is fine) but rejects
     * any name already claimed by another server in the {@link LdapServersManager}.
     *
     * <p>For example — Lando proposes "Ghost Station Alpha" for the Cloud City beacon:</p>
     * <pre>
     *   InputDialog dialog = new InputDialog( shell, "Rename Server",
     *       "New name:", server.getName(), validator );
     *   dialog.open();
     *   String newName = dialog.getValue();
     *   if ( newName != null ) {
     *       server.setName( newName );   // beacon reprogrammed
     *   }
     * </pre>
     */
    public void run()
    {
        if ( view != null )
        {
            // Getting the selected server
            StructuredSelection selection = ( StructuredSelection ) view.getViewer().getSelection();
            final LdapServer server = ( LdapServer ) selection.getFirstElement();

            if ( server != null )
            {
                IInputValidator validator = new IInputValidator()
                {
                    public String isValid( String newName )
                    {
                        if ( server.getName().equals( newName ) )
                        {
                            return null;
                        }
                        else if ( !LdapServersManager.getDefault().isNameAvailable( newName ) )
                        {
                            return Messages.getString( "RenameAction.ErrorNameInUse" ); //$NON-NLS-1$
                        }
                        else
                        {
                            return null;
                        }
                    }
                };

                // Opening a dialog to ask the user a new name for the server
                InputDialog dialog = new InputDialog( view.getSite().getShell(),
                    Messages.getString( "RenameAction.RenameServer" ), //$NON-NLS-1$
                    Messages.getString( "RenameAction.NewName" ), //$NON-NLS-1$
                    server.getName(), validator );
                dialog.open();

                String newName = dialog.getValue();
                if ( newName != null )
                {
                    server.setName( newName );
                }
            }
        }
    }


    // ── THE FLEET RELAY PASSES LANDO'S COMMAND THROUGH ───────────────────────────────────────
    // A communications officer on the Rebel cruiser receives Lando's renaming order over
    // the fleet relay and forwards it straight to the transponder console unchanged.
    // Eclipse sometimes drives us through the {@link IWorkbenchWindowActionDelegate}
    // interface; this overload simply forwards to our primary {@link #run()}.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@link IWorkbenchWindowActionDelegate} entry point that delegates to {@link #run()}.
     * Eclipse calls this when the action is triggered via a menu or keyboard shortcut
     * registered through the workbench action extension point.
     *
     * @param action  The Eclipse proxy {@link IAction} — we ignore it and call {@link #run()}.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── LANDO STEPS AWAY FROM THE COMM PANEL ─────────────────────────────────────────────────
    // Lando pushes back from the console once the call sign is committed — nothing to
    // power down, no connections to close.
    // We implement this because {@link IWorkbenchWindowActionDelegate} requires it,
    // but we hold no resources of our own.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being torn down.
     * We hold no resources, so this is intentionally empty.
     *
     * <p>For example — Lando pushes back from the console, nothing to stow:</p>
     * <pre>
     *   // Nothing to do
     * </pre>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── THE LIAISON OFFICER CHECKS IN, GETS DISMISSED ────────────────────────────────────────
    // A liaison officer arrives at Lando's side to announce the workbench window reference,
    // but Lando already has everything he needs from the view passed at construction time.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is first associated with a workbench window.
     * We get everything we need from the {@link ServersView} supplied at construction time,
     * so this is a no-op.
     *
     * @param window  The workbench window we are attached to — reported in, not needed.
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── THE FLEET SCANNER REPORTS NO CHANGE TO THE COMM PANEL ────────────────────────────────
    // The Rebel fleet's scanners continuously sweep the selection picture and report back;
    // when the selection changes it doesn't affect Lando's transponder reprogramming workflow.
    // Eclipse fires this to let us toggle our enabled state; the view handles that for us.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes.
     * The {@link ServersView} manages our enabled state, so we don't need to react here.
     *
     * @param action     The proxy action we could enable or disable — not needed.
     * @param selection  The new workbench selection — we ignore it.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
