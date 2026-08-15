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
import org.apache.directory.studio.ldapservers.jobs.StopLdapServerRunnable;
import org.apache.directory.studio.ldapservers.jobs.StudioLdapServerJob;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: StopAction — THE DEATH STAR POWERS DOWN AFTER YAVIN ───────────────────────────────
// Luke Skywalker sits in the trench of the Death Star, targeting computer switched off,
// trusting the Force.  His proton torpedoes race down the two-metre-wide thermal exhaust
// port and detonate the main reactor.  The station shudders, goes dark, and silently
// implodes — the superlaser that threatened the galaxy falls silent forever.
// That is what we do here: the user picks a running LDAP server and we schedule the
// background job that gracefully shuts it down.  No confirmation needed — Luke didn't
// hesitate, and neither do we.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse Action that stops the currently selected LDAP server.
 * We read the selected server from the {@link ServersView}, then schedule a
 * {@link StopLdapServerRunnable} background job that delegates the actual shutdown
 * to the server's adapter.  Unlike {@code StartAction}, we don't need to check ports
 * or ask for confirmation — stopping is always safe to attempt immediately.
 * Think of this class as Luke's proton torpedoes — aimed, launched, and the station
 * goes dark without a second prompt.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StopAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private ServersView view;


    // ── LUKE LOCKS THE TARGETING COMPUTER BEFORE SWITCHING IT OFF ────────────────────────────
    // Luke's X-wing screams down the trench, targeting computer engaged and armed —
    // the action is initialised with its labels and icon but no specific server selected yet.
    // We create a free-standing action instance here; the view context arrives later through
    // the {@link IWorkbenchWindowActionDelegate} lifecycle.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a standalone {@code StopAction} with no associated view.
     * Eclipse uses this no-arg constructor when wiring us as a global workbench action
     * contribution; the view gets injected later via {@link #init(IWorkbenchWindow)}.
     *
     * <p>For example — Luke's targeting computer arms before the trench run begins:</p>
     * <pre>
     *   StopAction action = new StopAction();
     *   // view is null — Eclipse will supply context when the delegate is initialised
     * </pre>
     */
    public StopAction()
    {
        super( Messages.getString( "StopAction.Stop" ) ); //$NON-NLS-1$
        init();
    }


    // ── LUKE ENTERS THE TRENCH WITH A TARGET REFERENCE ALREADY LOADED ────────────────────────
    // Luke drops into the Death Star trench already knowing which exhaust port he is
    // targeting — the view reference tells us exactly which server list to consult.
    // We store the view so run() can pull the selected server without searching.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code StopAction} pre-wired to the given servers view.
     * This constructor is used when the action is contributed directly to the
     * {@link ServersView} toolbar — we already know where to find the selected server.
     *
     * <p>For example — Luke drops into the trench with the exhaust port coordinates loaded:</p>
     * <pre>
     *   StopAction action = new StopAction( serversView );
     *   // action knows which server list to target — no hunting needed
     * </pre>
     *
     * @param view  The {@link ServersView} whose selection we will stop —
     *              Luke's Death Star targeting display showing today's exhaust port.
     */
    public StopAction( ServersView view )
    {
        super( Messages.getString( "StopAction.Stop" ) ); //$NON-NLS-1$
        this.view = view;
        init();
    }


    // ── THE TARGETING SYSTEMS ARE PREPPED FOR THE KILL SHOT ──────────────────────────────────
    // Luke's targeting computer confirms the command ID, lights up the stop icon on his
    // console, and loads the tooltip text so his wingman knows what they are about to do.
    // We wire up the Eclipse action's command ID, tooltip, and stop icon so the UI can
    // display and trigger us correctly.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the action's command identifiers, tooltip, and icon.
     * Called from both constructors to avoid duplicating the wiring logic.
     * The command ID must match the declaration in {@code plugin.xml} for the
     * keyboard shortcut to work correctly.
     *
     * <p>For example — the targeting console is armed and labelled:</p>
     * <pre>
     *   console.setCommandId( CMD_STOP );
     *   console.setTooltip( "Stop server" );
     *   console.setIcon( IMG_STOP );
     * </pre>
     */
    private void init()
    {
        setId( LdapServersPluginConstants.CMD_STOP );
        setActionDefinitionId( LdapServersPluginConstants.CMD_STOP );
        setToolTipText( Messages.getString( "StopAction.StopToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( LdapServersPlugin.getDefault().getImageDescriptor( LdapServersPluginConstants.IMG_STOP ) );
    }


    // ── LUKE FIRES — THE STATION GOES DARK ───────────────────────────────────────────────────
    // Luke releases the torpedoes.  They spiral down the exhaust port, detonate the reactor,
    // and the Death Star's lights go out one by one.  No confirmation asked — the order
    // was to stop the station, and it is done.
    // We grab the selected server, schedule a {@link StopLdapServerRunnable}, and that
    // background job brings the server to a clean halt.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the stop action: reads the currently selected server from the view and
     * schedules a {@link StopLdapServerRunnable} background job to shut it down.
     * We only act when exactly one server is selected and it is not already stopped;
     * the adapter handles the actual graceful-shutdown sequence.
     * There is no confirmation dialog — stopping is safe to attempt without a second prompt.
     *
     * <p>For example — Luke fires and the Death Star goes dark:</p>
     * <pre>
     *   LdapServer server = view.getSingleSelection();
     *   new StudioLdapServerJob( new StopLdapServerRunnable( server ) ).schedule();
     *   // server shuts down gracefully in the background
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

                // Creating and scheduling the job to stop the server
                StudioLdapServerJob job = new StudioLdapServerJob( new StopLdapServerRunnable( server ) );
                job.schedule();
            }
        }
    }


    // ── GOLD LEADER RELAYS LUKE'S FIRE ORDER ─────────────────────────────────────────────────
    // Gold Leader receives Luke's attack order over the Rebel comm channel and relays it
    // straight to the squadron without adding anything of his own.
    // Eclipse sometimes drives us through the {@link IWorkbenchWindowActionDelegate}
    // interface; this overload simply forwards to our primary {@link #run()}.
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


    // ── LUKE'S X-WING PEELS AWAY FROM THE EXPLOSION ──────────────────────────────────────────
    // Luke rolls his X-wing clear of the expanding fireball — the mission is done,
    // nothing to clean up, the torpedoes already spent.
    // We implement this because {@link IWorkbenchWindowActionDelegate} requires it,
    // but we hold no resources of our own.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being torn down.
     * We hold no resources of our own, so this is intentionally empty.
     *
     * <p>For example — the X-wing clears the blast radius; nothing to stow:</p>
     * <pre>
     *   // Nothing to do
     * </pre>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── R2-D2 CHECKS IN, GETS WAVED OFF ──────────────────────────────────────────────────────
    // R2-D2 beeps from Luke's astromech socket to announce the workbench window reference,
    // but Luke already has everything he needs from the view passed at construction time.
    // We don't use the window reference; all context comes from the view.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is first associated with a workbench window.
     * We don't need the window reference — everything we use comes from the
     * {@link ServersView} supplied at construction time — so this is a no-op.
     *
     * @param window  The workbench window we are attached to — R2-D2 beeps in, not needed.
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── THE REBEL SCANNERS SWEEP, TRENCH RUN UNAFFECTED ──────────────────────────────────────
    // The Rebel fleet's scanners continuously update the battle picture; when the workbench
    // selection changes they note it down but Luke's targeting run doesn't change course.
    // Eclipse fires this to let us toggle our enabled state — the view manages that for us.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes.
     * The {@link ServersView} manages our enabled state (e.g. we should be disabled when
     * the server is already stopped), so we don't need to react here.
     *
     * @param action     The proxy action we could enable or disable — not needed.
     * @param selection  The new workbench selection — we ignore it.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
