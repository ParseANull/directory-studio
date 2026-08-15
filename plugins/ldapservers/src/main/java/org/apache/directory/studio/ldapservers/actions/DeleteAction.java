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
import org.apache.directory.studio.ldapservers.dialogs.DeleteServerDialog;
import org.apache.directory.studio.ldapservers.jobs.DeleteLdapServerRunnable;
import org.apache.directory.studio.ldapservers.jobs.StudioLdapServerJob;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


// ── CLASS: DeleteAction — THE DEATH STAR FIRES ON ALDERAAN ───────────────────────────────────
// Grand Moff Tarkin stands on the Death Star's bridge and orders the superlaser trained
// on Alderaan.  Princess Leia watches in horror as an entire planet — with its history,
// its people, everything — vanishes in a single, irreversible burst of green light.
// That is exactly what happens here: the user picks a server, we ask once for confirmation
// (Tarkin gives Leia one last chance to cooperate), and then we fire — scheduling a
// background job that wipes the server permanently from disk and memory.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse Action that permanently removes a selected LDAP server from Directory Studio.
 * When triggered, we confirm the destructive intent with a dialog and then schedule
 * a {@link DeleteLdapServerRunnable} background job to do the actual deletion.
 * Think of this class as Grand Moff Tarkin on the Death Star bridge — once the order
 * is given and the beam fires, Alderaan (your server) is gone for good.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private ServersView view;


    // ── THE DEATH STAR STATIONS ITSELF ABOVE THE TARGET ──────────────────────────────────────
    // The Death Star swings into position above Alderaan, target locked, crew at battle
    // stations — but no specific planet coordinates have been loaded yet.
    // Tarkin is on the bridge, weapon primed, waiting for a target to be supplied later.
    // We create a free-standing action instance here, wiring in the label and icon but
    // leaving the view reference null until Eclipse supplies it via init() or selectionChanged().
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a standalone {@code DeleteAction} with no associated view.
     * Eclipse uses this no-arg constructor when wiring us up as a global workbench action
     * (e.g. from a menu contribution); the view gets injected later via the
     * {@link IWorkbenchWindowActionDelegate} lifecycle.
     *
     * <p>For example — Tarkin initialises the weapon before a target is chosen:</p>
     * <pre>
     *   DeleteAction action = new DeleteAction();
     *   // view is null here — Eclipse will provide context via init()
     * </pre>
     */
    public DeleteAction()
    {
        super( Messages.getString( "DeleteAction.Delete" ) ); //$NON-NLS-1$
        init();
    }


    // ── TARKIN LOCKS IN THE TARGET COORDINATES ────────────────────────────────────────────────
    // Tarkin steps to the targeting console with the precise coordinates of Alderaan already
    // loaded, knowing exactly which world he wants destroyed.
    // We receive the specific {@link ServersView} here, so when run() fires we know exactly
    // which server list to pull the target from.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code DeleteAction} pre-wired to the given servers view.
     * This is the constructor used when the action is contributed directly to the
     * {@link ServersView} toolbar or context menu — we already know which list
     * to pull the selected server from when {@link #run()} is called.
     *
     * <p>For example — Tarkin arrives at the firing station with coordinates in hand:</p>
     * <pre>
     *   DeleteAction action = new DeleteAction( serversView );
     *   // action already knows where to look for the target
     * </pre>
     *
     * @param view  The {@link ServersView} whose current selection we will delete —
     *              Tarkin's targeting console pre-loaded with Alderaan's coordinates.
     */
    public DeleteAction( ServersView view )
    {
        super( Messages.getString( "DeleteAction.Delete" ) ); //$NON-NLS-1$
        this.view = view;
        init();
    }


    // ── WEAPONS OFFICERS ARM THE SUPERLASER ──────────────────────────────────────────────────
    // The Death Star's weapons crew runs through their pre-fire checklist: command ID set,
    // targeting computer armed, delete icon loaded onto the console display.
    // Without this step the action can't be triggered from any menu or toolbar.
    // We wire up the Eclipse action's identifier, keyboard binding ID, tooltip, and icon
    // so the UI knows how to display and invoke us.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the action's command identifiers, tooltip, and icon.
     * We call this from both constructors to avoid duplicating the wiring logic.
     * Eclipse needs these IDs to match keyboard shortcut definitions in plugin.xml,
     * and the icon makes it obvious to users that this action is destructive.
     *
     * <p>For example — the weapons crew arms the console:</p>
     * <pre>
     *   console.setCommandId( CMD_DELETE );
     *   console.setTooltip( "Delete selected server" );
     *   console.setIcon( IMG_TOOL_DELETE );
     * </pre>
     */
    private void init()
    {
        setId( LdapServersPluginConstants.CMD_DELETE );
        setActionDefinitionId( LdapServersPluginConstants.CMD_DELETE );
        setToolTipText( Messages.getString( "DeleteAction.DeleteToolTip" ) ); //$NON-NLS-1$
        setImageDescriptor( PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor( ISharedImages.IMG_TOOL_DELETE ) );
    }


    // ── TARKIN GIVES THE ORDER — THE BEAM FIRES ──────────────────────────────────────────────
    // Tarkin turns to the weapons officer and says "You may fire when ready."
    // The green beam erupts from the superlaser dish.  Alderaan screams and vanishes.
    // We grab the selected server from the view, pop a confirmation dialog (one last
    // chance to back out), and if the user clicks OK we schedule the delete job.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the delete action: reads the currently selected server, shows a
     * confirmation dialog, and — if the user confirms — schedules a
     * {@link DeleteLdapServerRunnable} to wipe the server from disk and memory.
     * If nothing is selected, or the user cancels the dialog, we do nothing.
     *
     * <p>For example — Tarkin gives the order and the beam fires:</p>
     * <pre>
     *   LdapServer target = view.getSelection();          // Alderaan, locked
     *   if ( confirmationDialog.open() == OK ) {
     *       new StudioLdapServerJob( new DeleteLdapServerRunnable( target ) ).schedule();
     *       // target is gone — no undo
     *   }
     * </pre>
     */
    public void run()
    {
        if ( view != null )
        {
            // What we get from the TableViewer is a StructuredSelection
            StructuredSelection selection = ( StructuredSelection ) view.getViewer().getSelection();

            // Here's the real object
            LdapServer server = ( LdapServer ) selection.getFirstElement();

            // Asking for confirmation
            DeleteServerDialog dsd = new DeleteServerDialog( view.getSite().getShell(), server );
            if ( dsd.open() == DeleteServerDialog.OK )
            {
                // Creating and scheduling the job to delete the server
                StudioLdapServerJob job = new StudioLdapServerJob( new DeleteLdapServerRunnable( server ) );
                job.schedule();
            }
        }
    }


    // ── THE DELEGATE RELAYS TARKIN'S ORDER ───────────────────────────────────────────────────
    // An Imperial officer on a nearby station receives Tarkin's command over the intercom
    // and passes it through to the weapons console without adding anything of his own.
    // Eclipse sometimes drives us through the {@link IWorkbenchWindowActionDelegate}
    // interface rather than calling run() directly; this overload just forwards the call.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@link IWorkbenchWindowActionDelegate} entry point that simply delegates to
     * {@link #run()}.  Eclipse calls this variant when the action is triggered via a
     * menu or keyboard shortcut registered through the workbench action extension point.
     * The {@code action} parameter is the proxy object Eclipse hands us — we don't need it.
     *
     * <p>For example — the intercom operator repeats the order verbatim:</p>
     * <pre>
     *   officer.receiveCommand( action );
     *   firingConsole.run();   // same outcome, one extra hop
     * </pre>
     *
     * @param action  The Eclipse proxy {@link IAction} — we ignore it and delegate straight
     *                to our own {@link #run()} method.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── THE STATION STANDS DOWN AFTER THE SHOT ────────────────────────────────────────────────
    // The Death Star's crew relaxes as the superlaser cools — nothing to clean up,
    // the weapon resets itself automatically.
    // We implement dispose() because the interface requires it, but we hold no resources
    // that need releasing, so this is intentionally empty.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up any resources held by this action when Eclipse is done with it.
     * We don't hold any resources ourselves, so this is a no-op — but the
     * {@link IWorkbenchWindowActionDelegate} interface demands we implement it.
     *
     * <p>For example — the weapons crew stands down, nothing to stow:</p>
     * <pre>
     *   // Nothing to do — the Death Star resets automatically
     * </pre>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── THE BRIDGE OFFICER REPORTS IN ─────────────────────────────────────────────────────────
    // A fresh officer arrives at Tarkin's bridge station and announces himself — but
    // Tarkin already has everything he needs and waves him off.
    // Eclipse calls this so we can grab a reference to the workbench window if we need it;
    // we don't, so we leave this empty.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is first associated with a workbench window.
     * We could grab the window reference here for later use, but we get everything we
     * need from the {@link ServersView} passed at construction time, so this is a no-op.
     *
     * @param window  The workbench window we are associated with — Tarkin's bridge, reported
     *                in but not needed.
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── TARGET TRACKING REPORTS NO CHANGE ────────────────────────────────────────────────────
    // The targeting sensors continuously sweep the battlefield; when nothing moves
    // they simply report "no change" and wait for the next scan.
    // Eclipse fires this whenever the workbench selection changes so we can toggle our
    // enabled state; we leave that to the view's own enablement logic, so this is a no-op.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse whenever the workbench selection changes.
     * We could use this to enable or disable the action based on what is selected,
     * but the {@link ServersView} handles enablement for us, so we do nothing here.
     *
     * @param action     The proxy action whose enabled state we could update — not needed.
     * @param selection  The new workbench selection — we ignore it.
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
