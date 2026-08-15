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


import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.jobs.OpenConfigurationLdapServerRunnable;
import org.apache.directory.studio.ldapservers.jobs.StudioLdapServerJob;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: OpenConfigurationAction — IMPERIAL ENGINEERS STUDY THE DEATH STAR SCHEMATICS ──────
// Deep inside the Death Star's engineering bay, Imperial technicians unroll the full
// technical readout of the station — every power coupling, every targeting array,
// every blast door relay laid out in exhaustive detail.
// That is what we do here: open the server-specific configuration editor so the
// operator can inspect and adjust every setting for the selected LDAP server.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse Action that opens the configuration editor for a selected LDAP server.
 * When triggered we schedule an {@link OpenConfigurationLdapServerRunnable} background
 * job; that job delegates to the server's adapter, which knows how to open the right
 * editor (e.g. an ApacheDS server opens its own config.xml editor).
 * Think of this class as an Imperial engineer rolling out the Death Star's technical
 * schematics — full access to every dial and switch in the system.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenConfigurationAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private ServersView view;


    // ── AN ENGINEER IS SUMMONED TO THE TECHNICAL READOUT ROOM ────────────────────────────────
    // A junior Imperial engineer is paged to the Death Star's briefing room without a
    // specific station assignment yet — they will be told which console to approach once
    // Tarkin decides which system needs inspecting.
    // We create the action here with no view reference; Eclipse will supply context later
    // through the {@link IWorkbenchWindowActionDelegate} lifecycle.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a standalone {@code OpenConfigurationAction} with no associated view.
     * Eclipse uses this no-arg constructor when wiring us up as a global workbench action
     * contribution; the view context arrives later via {@link #init(IWorkbenchWindow)}.
     *
     * <p>For example — the engineer reports for duty before getting a station assignment:</p>
     * <pre>
     *   OpenConfigurationAction action = new OpenConfigurationAction();
     *   // view is null — Eclipse will provide context when the delegate is initialised
     * </pre>
     */
    public OpenConfigurationAction()
    {
        super( Messages.getString( "OpenConfigurationAction.OpenConfiguration" ) ); //$NON-NLS-1$
        init();
    }


    // ── THE ENGINEER ARRIVES AT THE CORRECT CONSOLE WITH STATION IN HAND ─────────────────────
    // The senior engineering officer arrives already knowing which station she is headed to —
    // Tarkin pointed her at the Death Star's specific power conduit display before she left.
    // We receive the {@link ServersView} here so run() can immediately pull the selected
    // server rather than hunting for a selection from scratch.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an {@code OpenConfigurationAction} pre-wired to the given servers view.
     * This constructor is used when the action is contributed directly to the
     * {@link ServersView} toolbar or context menu — we already know where to look
     * for the selected server when {@link #run()} fires.
     *
     * <p>For example — the engineer walks straight to the assigned console:</p>
     * <pre>
     *   OpenConfigurationAction action = new OpenConfigurationAction( serversView );
     *   // action knows which panel to open — no hunting needed
     * </pre>
     *
     * @param view  The {@link ServersView} whose selection we will open the config for —
     *              the specific Death Star engineering console we are assigned to.
     */
    public OpenConfigurationAction( ServersView view )
    {
        super( Messages.getString( "OpenConfigurationAction.OpenConfiguration" ) ); //$NON-NLS-1$
        this.view = view;
        init();
    }


    // ── THE ENGINEERING BAY IS PREPPED AND LABELLED ──────────────────────────────────────────
    // The Death Star's engineering room is stencilled with its command codes, tooltip
    // signs, and station identifiers so every officer knows exactly where to go.
    // We wire up the Eclipse action's command ID (for keyboard bindings in plugin.xml)
    // and the tooltip text that appears when the user hovers over the button.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the action's command identifiers and tooltip.
     * Called from both constructors to avoid duplicating the wiring logic.
     * The command ID must match the declaration in {@code plugin.xml} for keyboard
     * shortcuts to work correctly.
     *
     * <p>For example — the engineering console is labelled and ready:</p>
     * <pre>
     *   console.setCommandId( CMD_OPEN_CONFIGURATION );
     *   console.setTooltip( "Open Configuration" );
     * </pre>
     */
    private void init()
    {
        setId( LdapServersPluginConstants.CMD_OPEN_CONFIGURATION );
        setActionDefinitionId( LdapServersPluginConstants.CMD_OPEN_CONFIGURATION );
        setToolTipText( Messages.getString( "OpenConfigurationAction.OpenConfigurationToolTip" ) ); //$NON-NLS-1$
    }


    // ── THE ENGINEER UNROLLS THE TECHNICAL SCHEMATICS ────────────────────────────────────────
    // The Imperial engineer reaches the console, verifies that exactly one station is
    // selected on the display, then unrolls the full technical readout for that station.
    // We check that the view has a single server selected, then schedule
    // {@link OpenConfigurationLdapServerRunnable} to open the configuration editor.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the open-configuration action: reads the currently selected server and
     * schedules an {@link OpenConfigurationLdapServerRunnable} background job to open
     * the server's configuration editor.
     * We only act if exactly one server is selected; if the selection is empty or contains
     * multiple items we do nothing.
     *
     * <p>For example — the engineer opens the schematics for the selected station:</p>
     * <pre>
     *   LdapServer server = view.getSingleSelection();   // exactly one, or bail
     *   new StudioLdapServerJob(
     *       new OpenConfigurationLdapServerRunnable( server ) ).schedule();
     *   // configuration editor opens in the Eclipse editor area
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

                // Creating and scheduling the job to start the server
                StudioLdapServerJob job = new StudioLdapServerJob( new OpenConfigurationLdapServerRunnable( server ) );
                job.schedule();
            }
        }
    }


    // ── THE RELAY OFFICER PASSES THE ORDER THROUGH ───────────────────────────────────────────
    // A communications officer on the Death Star's bridge receives the "open schematics"
    // order from Tarkin and relays it to the engineering bay without adding anything.
    // Eclipse invokes this variant when we are triggered through an extension-point
    // action; we simply forward the call to our primary {@link #run()} method.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@link IWorkbenchWindowActionDelegate} entry point that delegates to {@link #run()}.
     * Eclipse calls this when the action is triggered via a menu or keyboard shortcut
     * registered through the workbench action extension point.
     * We don't need anything from the {@code action} parameter.
     *
     * @param action  The Eclipse proxy {@link IAction} — we ignore it and forward to
     *                {@link #run()}.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── THE ENGINEERING BAY TIDIES UP AFTER THE REVIEW ───────────────────────────────────────
    // The Imperial engineers roll up the schematics and stow them back in the vault once
    // the review session is over — but there is nothing else to clean up.
    // We implement this because {@link IWorkbenchWindowActionDelegate} requires it,
    // but we hold no resources of our own.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being torn down.
     * We hold no resources of our own, so this is intentionally empty.
     *
     * <p>For example — the engineers roll up the schematics, nothing else to stow:</p>
     * <pre>
     *   // Nothing to do
     * </pre>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── A FRESH OFFICER REPORTS IN, GETS WAVED OFF ───────────────────────────────────────────
    // A junior officer arrives at the engineering bay to announce the workbench window
    // reference, but the senior engineer already has everything she needs.
    // We don't need the window reference here — we get what we need from the view.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is first associated with a workbench window.
     * We don't use the window reference — everything we need comes from the
     * {@link ServersView} supplied at construction time — so this is a no-op.
     *
     * @param window  The workbench window we are associated with — reported in, not needed.
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── TARGETING SENSORS SWEEP, NOTHING TO REPORT ───────────────────────────────────────────
    // The Death Star's sensor arrays sweep continuously; when the selection picture
    // changes they update their logs but don't alter the engineering bay's configuration.
    // Eclipse fires this to let us adjust our enabled state — but the view handles
    // enablement for us, so we ignore the notification.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes.
     * The {@link ServersView} manages our enabled state based on selection, so we
     * don't need to react here.
     *
     * @param action     The proxy action we could enable or disable — not needed.
     * @param selection  The new workbench selection — we ignore it.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
