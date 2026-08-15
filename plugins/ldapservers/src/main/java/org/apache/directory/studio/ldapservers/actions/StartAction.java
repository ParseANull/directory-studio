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


import org.apache.directory.studio.ldapservers.LdapServersPlugin;
import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.jobs.StartLdapServerRunnable;
import org.apache.directory.studio.ldapservers.jobs.StudioLdapServerJob;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapter;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: StartAction — THE DEATH STAR POWERS UP ITS SUPERLASER ─────────────────────────────
// Grand Moff Tarkin stands on the bridge of the Death Star and commands the weapons crew
// to charge the superlaser.  Before they commit to a full-power discharge, the targeting
// officer checks that the relevant firing ports are clear — if another weapon already has
// the same port locked, Tarkin is warned and given a chance to abort.
// Once the all-clear comes, the reactor energy surges through the focusing dish and
// the beam fires.  That is what we do here: check for port conflicts, warn the user if
// any are found, and then schedule the background job that brings the LDAP server online.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse Action that starts the currently selected LDAP server.
 * Before firing the start job we use the server's {@link LdapServerAdapter} to check
 * whether any of the server's network ports are already in use; if they are, we warn
 * the user and let them cancel.  If everything looks clear (or the user proceeds anyway),
 * we schedule a {@link StartLdapServerRunnable} background job that delegates the actual
 * startup to the adapter.
 * Think of this class as Grand Moff Tarkin charging the Death Star's superlaser — port
 * checks are the targeting clearance, and scheduling the job is pulling the trigger.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StartAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private ServersView view;


    // ── THE DEATH STAR COMES ONLINE, CREW AT BATTLE STATIONS ─────────────────────────────────
    // The Death Star's reactor hums to life and the weapons crew takes their positions —
    // but no specific target has been locked yet; the view reference arrives later.
    // We set up the action's label, command IDs, tooltip, and start icon, ready to be
    // placed on a toolbar and triggered by the user.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a standalone {@code StartAction} with no associated view.
     * Eclipse uses this no-arg constructor when wiring us as a global workbench action
     * contribution.  The view context arrives later through the
     * {@link IWorkbenchWindowActionDelegate} lifecycle.
     *
     * <p>For example — the Death Star's superlaser comes online before a target is picked:</p>
     * <pre>
     *   StartAction action = new StartAction();
     *   // view is null — Eclipse will supply context when the delegate is initialised
     * </pre>
     */
    public StartAction()
    {
        super( Messages.getString( "StartAction.Start" ) ); //$NON-NLS-1$
        init();
    }


    // ── TARKIN TAKES THE BRIDGE WITH A TARGET ALREADY IN MIND ────────────────────────────────
    // Tarkin strides onto the Death Star's bridge already knowing which planet he wants
    // targeted — the view reference tells us exactly which server list to consult.
    // We store the view so run() can pull the selected server without searching.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code StartAction} pre-wired to the given servers view.
     * This is the constructor used when the action is contributed directly to the
     * {@link ServersView} toolbar — we already know where to find the selected server.
     *
     * <p>For example — Tarkin arrives on the bridge with Alderaan already in his sights:</p>
     * <pre>
     *   StartAction action = new StartAction( serversView );
     *   // action knows which server list to target — no hunting needed
     * </pre>
     *
     * @param view  The {@link ServersView} whose selection we will start —
     *              Tarkin's targeting console pre-loaded with today's target.
     */
    public StartAction( ServersView view )
    {
        super( Messages.getString( "StartAction.Start" ) ); //$NON-NLS-1$
        this.view = view;
        init();
    }


    // ── THE WEAPONS CREW ARMS THE SUPERLASER ─────────────────────────────────────────────────
    // The Death Star's weapons crew runs the pre-fire checklist: command ID confirmed,
    // targeting computer enabled, start icon lit up on every console display.
    // Without this step the action can't be triggered from any menu or toolbar.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the action's command identifiers, tooltip, and icon.
     * Called from both constructors to avoid duplicating the wiring logic.
     * The command ID must match the declaration in {@code plugin.xml} for the keyboard
     * shortcut to work correctly.
     *
     * <p>For example — the weapons crew enables the firing console:</p>
     * <pre>
     *   console.setCommandId( CMD_START );
     *   console.setTooltip( "Start server" );
     *   console.setIcon( IMG_START );
     * </pre>
     */
    private void init()
    {
        setId( LdapServersPluginConstants.CMD_START );
        setActionDefinitionId( LdapServersPluginConstants.CMD_START );
        setToolTipText( Messages.getString( "StartAction.StartToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( LdapServersPlugin.getDefault().getImageDescriptor( LdapServersPluginConstants.IMG_START ) );
    }


    // ── TARKIN ORDERS PORT CLEARANCE, THEN FIRES ─────────────────────────────────────────────
    // Tarkin asks the targeting officer to confirm that the superlaser's firing ports
    // are unobstructed — if another weapon has the same port locked, he gets a warning
    // dialog and the option to abort or proceed anyway.
    // Once clearance is given, the energy surges and the beam fires: we schedule
    // {@link StartLdapServerRunnable} and the server comes online.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the start action: reads the selected server, checks for port conflicts via
     * the server's {@link LdapServerAdapter}, warns the user if any ports are already in
     * use, and — unless the user cancels — schedules a {@link StartLdapServerRunnable}
     * background job to bring the server online.
     * If no adapter is available for the selected server we show an error dialog and stop.
     *
     * <p>For example — Tarkin clears the ports and pulls the trigger:</p>
     * <pre>
     *   String[] blockedPorts = adapter.checkPortsBeforeServerStart( server );
     *   if ( blockedPorts.length > 0 ) {
     *       if ( warningDialog.open() == CANCEL ) { return; }
     *   }
     *   new StudioLdapServerJob( new StartLdapServerRunnable( server ) ).schedule();
     * </pre>
     */
    public void run()
    {
        if ( view != null )
        {
            // Getting the selection
            StructuredSelection selection = ( StructuredSelection ) view.getViewer().getSelection();
            if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
            {
                // Getting the server
                LdapServer server = ( LdapServer ) selection.getFirstElement();

                LdapServerAdapterExtension ldapServerAdapterExtension = server.getLdapServerAdapterExtension();
                if ( ( ldapServerAdapterExtension != null ) && ( ldapServerAdapterExtension.getInstance() != null ) )
                {
                    LdapServerAdapter ldapServerAdapter = ldapServerAdapterExtension.getInstance();

                    try
                    {

                        // Getting the ports already in use
                        String[] portsAlreadyInUse = ldapServerAdapter.checkPortsBeforeServerStart( server );
                        if ( ( portsAlreadyInUse == null ) || ( portsAlreadyInUse.length > 0 ) )
                        {
                            String title = null;
                            String message = null;

                            if ( portsAlreadyInUse.length == 1 )
                            {
                                title = Messages.getString( "StartAction.PortInUse" ); //$NON-NLS-1$
                                message = NLS
                                    .bind(
                                        Messages.getString( "StartAction.PortOfProtocolInUse" ), new String[] { portsAlreadyInUse[0] } ); //$NON-NLS-1$
                            }
                            else
                            {
                                title = Messages.getString( "StartAction.PortsInUse" ); //$NON-NLS-1$
                                message = Messages.getString( "StartAction.PortsOfProtocolsInUse" ); //$NON-NLS-1$
                                for ( String portAlreadyInUse : portsAlreadyInUse )
                                {
                                    message += "\n    - " + portAlreadyInUse; //$NON-NLS-1$
                                }
                            }

                            message += "\n\n" + Messages.getString( "StartAction.Continue" ); //$NON-NLS-1$ //$NON-NLS-2$

                            MessageDialog dialog = new MessageDialog( view.getSite().getShell(), title, null, message,
                                MessageDialog.WARNING, new String[]
                                    { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, MessageDialog.OK );
                            if ( dialog.open() == MessageDialog.CANCEL )
                            {
                                return;
                            }
                        }

                        // Creating and scheduling the job to start the server
                        StudioLdapServerJob job = new StudioLdapServerJob( new StartLdapServerRunnable( server ) );
                        job.schedule();
                    }
                    catch ( Exception e )
                    {
                        // Showing an error in case no LDAP Server Adapter can be found
                        MessageDialog
                            .openError( view.getSite().getShell(),
                                Messages.getString( "StartAction.ErrorStartingServer" ), //$NON-NLS-1$
                                NLS.bind(
                                    Messages.getString( "StartAction.ServerCanNotBeStarted" ) + "\n" + Messages.getString( "StartAction.Cause" ), server.getName(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    e.getMessage() ) );
                    }
                }
                else
                {
                    // Showing an error in case no LDAP Server Adapter can be found
                    MessageDialog.openError( view.getSite().getShell(),
                        Messages.getString( "StartAction.NoLdapServerAdapter" ), //$NON-NLS-1$
                        NLS.bind( Messages.getString( "StartAction.ServerCanNotBeStarted" ) + "\n" //$NON-NLS-1$ //$NON-NLS-2$
                            + Messages.getString( "StartAction.NoLdapServerAdapterCouldBeFound" ), server.getName() ) ); //$NON-NLS-1$
                }
            }
        }
    }


    // ── THE INTERCOM RELAYS TARKIN'S FIRE ORDER ───────────────────────────────────────────────
    // An intercom officer on the Death Star's bridge receives Tarkin's fire command
    // and relays it to the weapons console verbatim, adding nothing of his own.
    // Eclipse sometimes drives us through the {@link IWorkbenchWindowActionDelegate}
    // interface; this overload just forwards the call to our primary {@link #run()}.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@link IWorkbenchWindowActionDelegate} entry point that delegates to {@link #run()}.
     * Eclipse calls this variant when the action is triggered via a menu or keyboard
     * shortcut registered through the workbench extension point.
     *
     * @param action  The Eclipse proxy {@link IAction} — we ignore it and call {@link #run()}.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── THE DEATH STAR'S WEAPONS BAY POWERS DOWN ─────────────────────────────────────────────
    // After the shot the superlaser cools and the weapons bay quietly resets — there is
    // nothing to manually clean up; the station handles it automatically.
    // We implement this because {@link IWorkbenchWindowActionDelegate} requires it,
    // but we hold no resources of our own.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being torn down.
     * We hold no resources of our own, so this is intentionally empty.
     *
     * <p>For example — the superlaser cools; no manual reset needed:</p>
     * <pre>
     *   // Nothing to do
     * </pre>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── A BRIDGE OFFICER REPORTS IN, GETS WAVED OFF ──────────────────────────────────────────
    // A fresh officer arrives at Tarkin's bridge station to announce the workbench window
    // reference, but Tarkin already has everything he needs from the view.
    // We don't use the window reference here; all context comes from the view.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is first associated with a workbench window.
     * We don't use the window reference — everything we need comes from the
     * {@link ServersView} supplied at construction time — so this is a no-op.
     *
     * @param window  The workbench window we are attached to — reported in, not needed.
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── THE TARGETING SENSORS SWEEP, NOTHING CHANGES ─────────────────────────────────────────
    // The Death Star's targeting arrays continuously sweep for changes in the selection
    // picture; when nothing mission-critical shifts they simply log "no change" and wait.
    // Eclipse fires this so we can update our enabled state — the view handles that for us.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes.
     * The {@link ServersView} manages our enabled state (e.g. we should be disabled when
     * the server is already running), so we don't need to react here.
     *
     * @param action     The proxy action we could enable or disable — not needed.
     * @param selection  The new workbench selection — we ignore it.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
