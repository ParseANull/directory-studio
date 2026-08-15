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
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: PropertiesAction — MON MOTHMA REVIEWS A STARFIGHTER'S MISSION BRIEFING ───────────
// Mon Mothma sits in the Rebel Alliance briefing room with a full dossier open in front
// of her — the X-wing's registration, its pilot record, its last maintenance date, its
// current assignment.  She can read every detail but the fighter stays in the hangar;
// nothing is changed by the review itself.
// That is what we do here: open the server's Eclipse Properties dialog so the operator
// can read every detail about the selected LDAP server without triggering any lifecycle
// changes.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse Action that opens the Properties dialog for the selected LDAP server.
 * The dialog is a standard Eclipse {@link PreferenceDialog} scoped to the server object;
 * adapter plug-ins contribute their own property pages to it.
 * Nothing about the server is started, stopped, or deleted — this is purely a read
 * (or light-edit) view of the server's settings.
 * Think of this class as Mon Mothma reviewing a starfighter's mission briefing dossier —
 * full visibility, no launch order.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PropertiesAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private ServersView view;


    // ── MON MOTHMA OPENS THE BRIEFING ROOM ───────────────────────────────────────────────────
    // Mon Mothma arranges the briefing room before the specific starfighter's dossier
    // has been placed on the table — the room is ready but the file hasn't arrived yet.
    // We create the action here with no view reference; the view context will arrive
    // later through the {@link IWorkbenchWindowActionDelegate} lifecycle.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a standalone {@code PropertiesAction} with no associated view.
     * Eclipse uses this no-arg constructor when wiring us as a global workbench action;
     * the view gets injected later through {@link #init(IWorkbenchWindow)}.
     *
     * <p>For example — Mon Mothma opens the briefing room before the dossier arrives:</p>
     * <pre>
     *   PropertiesAction action = new PropertiesAction();
     *   // view is null — Eclipse will supply context when the delegate is initialised
     * </pre>
     */
    public PropertiesAction()
    {
        super( Messages.getString( "PropertiesAction.Properties" ) ); //$NON-NLS-1$
        init();
    }


    // ── MON MOTHMA ENTERS WITH THE DOSSIER IN HAND ───────────────────────────────────────────
    // Mon Mothma walks into the briefing room already carrying the specific starfighter's
    // dossier — she knows exactly which pilot record she wants to review.
    // We receive the {@link ServersView} here so run() can pull the selected server
    // and open its Properties dialog immediately.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@code PropertiesAction} pre-wired to the given servers view.
     * This is the constructor used when the action is contributed directly to the
     * {@link ServersView} context menu — we already know where to find the selected server.
     *
     * <p>For example — Mon Mothma arrives with the dossier ready:</p>
     * <pre>
     *   PropertiesAction action = new PropertiesAction( serversView );
     *   // action knows which server to look up — no hunting needed
     * </pre>
     *
     * @param view  The {@link ServersView} whose selection we will show properties for —
     *              the specific starfighter dossier Mon Mothma came to review.
     */
    public PropertiesAction( ServersView view )
    {
        super( Messages.getString( "PropertiesAction.Properties" ) ); //$NON-NLS-1$
        this.view = view;
        init();
    }


    // ── THE BRIEFING ROOM IS LABELLED AND READY ───────────────────────────────────────────────
    // Mon Mothma's aide posts the command ID, tooltip sign, and room number so every
    // officer knows which door to knock on and what keyboard shortcut opens it.
    // We wire up the Eclipse action identifiers and tooltip text, shared between both
    // constructors to avoid duplication.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Wires up the action's command identifiers and tooltip text.
     * Called from both constructors.  The command ID must match the declaration in
     * {@code plugin.xml} for the keyboard shortcut to work.
     *
     * <p>For example — the aide posts the room sign:</p>
     * <pre>
     *   action.setId( CMD_PROPERTIES );
     *   action.setTooltip( "Server Properties" );
     * </pre>
     */
    private void init()
    {
        setId( LdapServersPluginConstants.CMD_PROPERTIES );
        setActionDefinitionId( LdapServersPluginConstants.CMD_PROPERTIES );
        setToolTipText( Messages.getString( "PropertiesAction.PropertiesToolTip" ) ); //$NON-NLS-1$
    }


    // ── MON MOTHMA OPENS THE DOSSIER ON THE TABLE ────────────────────────────────────────────
    // Mon Mothma reaches across the table, opens the starfighter's dossier to the first
    // page, and invites the operator to read through every spec and note.
    // We open a {@link PreferenceDialog} scoped to the selected server; the dialog's
    // title bar is set to "Properties for <server name>", truncated if the name is long.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse Properties dialog for the currently selected server.
     * We use {@link PreferencesUtil#createPropertyDialogOn} to build a dialog populated
     * with all property pages contributed for the server object (typically by the
     * server's adapter plug-in).  The dialog title is set to "Properties for &lt;name&gt;",
     * with the name truncated to 30 characters so it fits comfortably in the title bar.
     * If nothing is selected we do nothing.
     *
     * <p>For example — Mon Mothma reads the dossier aloud to the briefing room:</p>
     * <pre>
     *   LdapServer server = view.getSelection();
     *   PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(
     *       shell, server, PROP_SERVER_PROPERTY_PAGE, null, null );
     *   dialog.getShell().setText( "Properties for " + shorten( server.getName(), 30 ) );
     *   dialog.open();
     * </pre>
     */
    public void run()
    {
        if ( view != null )
        {
            StructuredSelection selection = ( StructuredSelection ) view.getViewer().getSelection();
            if ( !selection.isEmpty() )
            {
                LdapServer server = ( LdapServer ) selection.getFirstElement();
                PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn( view.getViewSite().getShell(),
                    server, LdapServersPluginConstants.PROP_SERVER_PROPERTY_PAGE, null, null );
                dialog.getShell().setText( NLS.bind( Messages.getString( "PropertiesAction.PropertiesFor" ), //$NON-NLS-1$
                    shorten( server.getName(), 30 ) ) );
                dialog.open();
            }
        }
    }


    // ── MON MOTHMA TRIMS THE DOSSIER COVER TO FIT THE DISPLAY ────────────────────────────────
    // The briefing room's status board can only show 30 characters for a starfighter's
    // call sign before the display clips.  Mon Mothma's aide trims long names from both
    // ends — keeping the beginning and the tail visible — and replaces control characters
    // with dots so nothing garbled appears on screen.
    // We do the same to server names before placing them in dialog title bars.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Shortens a label to at most {@code maxLength} characters and replaces any
     * non-printable (ISO control) characters with dots.
     * We use this to keep server names from overflowing dialog title bars.  Long names
     * are trimmed from the middle — we keep the start and end visible — and joined with
     * an ellipsis, which is the least surprising way to signal that text was cut.
     *
     * <p>For example — Mon Mothma's aide trims a long call sign for the display board:</p>
     * <pre>
     *   shorten( "MyVeryLongServerName-Production-EU", 30 )
     *   // returns "MyVeryLong...on-EU" — beginning and end visible
     *   shorten( null, 30 )   // returns null
     *   shorten( "abc", 2 )   // returns "..." (maxLength too small to be useful)
     * </pre>
     *
     * @param label      The string to shorten — typically a server name.  If {@code null},
     *                   we return {@code null} immediately.
     * @param maxLength  The maximum number of characters to allow in the result.
     *                   Must be at least 3 for a meaningful output; below that we just return
     *                   {@code "..."}.
     * @return           The (possibly truncated and sanitised) label, or {@code null} if
     *                   {@code label} was {@code null}.
     */
    public static String shorten( String label, int maxLength )
    {
        if ( label == null )
        {
            return null;
        }

        // shorten label
        if ( maxLength < 3 )
        {
            return "..."; //$NON-NLS-1$
        }
        if ( label.length() > maxLength )
        {
            label = label.substring( 0, maxLength / 2 ) + "..." //$NON-NLS-1$
                + label.substring( label.length() - maxLength / 2, label.length() );

        }

        // filter non-printable characters
        StringBuffer sb = new StringBuffer( maxLength + 3 );
        for ( int i = 0; i < label.length(); i++ )
        {
            char c = label.charAt( i );
            if ( Character.isISOControl( c ) )
            {
                sb.append( '.' );
            }
            else
            {
                sb.append( c );
            }
        }

        return sb.toString();
    }


    // ── THE RELAY OFFICER PASSES MON MOTHMA'S REVIEW REQUEST ─────────────────────────────────
    // A communications officer receives Mon Mothma's "open the dossier" order via the
    // fleet relay and passes it through to the briefing room unchanged.
    // Eclipse calls this variant when we are triggered through an extension-point
    // action; we simply forward to our primary {@link #run()} method.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * {@link IWorkbenchWindowActionDelegate} entry point that delegates to {@link #run()}.
     * Eclipse uses this variant when we are triggered through a global menu or keyboard
     * shortcut registered via the workbench extension point.
     *
     * @param action  The Eclipse proxy {@link IAction} — we ignore it and call {@link #run()}.
     */
    public void run( IAction action )
    {
        run();
    }


    // ── THE BRIEFING ROOM IS CLEARED AFTER THE REVIEW ────────────────────────────────────────
    // Mon Mothma's aide tidies the briefing room after she leaves — but there is nothing
    // to put away; the dossier was always read-only.
    // We implement this because the interface requires it, but we hold no resources.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action delegate is being torn down.
     * We hold no resources of our own, so this is intentionally empty.
     *
     * <p>For example — the aide straightens the chairs; nothing else to stow:</p>
     * <pre>
     *   // Nothing to do
     * </pre>
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── MON MOTHMA'S AIDE CHECKS IN, GETS WAVED OFF ──────────────────────────────────────────
    // An aide arrives at the briefing room entrance to announce the workbench window
    // reference, but Mon Mothma already has everything she needs from the view.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this action is first associated with a workbench window.
     * We don't need the window reference — everything we use comes from the
     * {@link ServersView} supplied at construction time — so this is a no-op.
     *
     * @param window  The workbench window we are attached to — reported in, not needed.
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── THE REBEL SENSOR SWEEPS, BRIEFING ROOM UNAFFECTED ────────────────────────────────────
    // The Alliance's tactical display continuously refreshes as selections change,
    // but Mon Mothma's briefing room availability doesn't depend on what's selected.
    // Eclipse fires this to let us update our enabled state; we leave that to the view.
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
