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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.api.ldap.extras.extended.pwdModify.PasswordModifyRequest;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.ui.dialogs.PasswordModifyExtendedOperationDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;


// ── CLASS: PasswordModifyExtendedOperationAction — MACE WINDU CONFRONTS PALPATINE ──
// Mace Windu strides into Palpatine's office, lightsaber ready, and confronts him
// directly — "You're under arrest, Chancellor."  The outcome depends entirely on
// what Palpatine reveals: if he's truly a Sith, everything changes; if the server
// doesn't support the Password Modify extended operation, we can't proceed.
// This action opens the Password Modify dialog that lets users change an LDAP entry's
// password using RFC 3062 — the "extended operation" Mace Windu is here to invoke.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Password Modify Extended Operation dialog for the currently selected
 * LDAP entry or connection.
 * The LDAP Password Modify extended operation (RFC 3062) is the proper, server-side
 * way to change a password — it lets the server enforce its own password policy
 * rather than us just writing a new value to the userPassword attribute.
 * Think of this class as Mace Windu: it only acts when it knows the server supports
 * the operation (by checking the rootDSE's supported extensions list), and it goes
 * straight to the source.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordModifyExtendedOperationAction extends BrowserAction
{
    // ── Mace Assembles His Strike Team ──────────────────────────────────────────
    // Mace gathered his Jedi Masters before going to Palpatine — he came prepared.
    // Our constructor calls super() to initialise the BrowserAction machinery so
    // we're properly connected to the selection framework before run() fires.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new PasswordModifyExtendedOperationAction wired into the BrowserAction
     * framework.
     */
    public PasswordModifyExtendedOperationAction()
    {
        super();
    }


    // ── Mace Enters The Office ──────────────────────────────────────────────────
    // Mace strides into Palpatine's office and opens the confrontation dialog —
    // there's no turning back.  run() resolves the connection and entry for the
    // current selection, then opens the PasswordModifyExtendedOperationDialog so
    // the user can supply the old password, the new password, or let the server
    // generate one.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Password Modify Extended Operation dialog for the currently selected
     * entry or connection.
     * We resolve the target connection and entry first (entry may be null if only
     * a connection is selected — some servers allow password changes at that level),
     * then open the dialog to collect the password modification details from the user.
     */
    public void run()
    {
        ConnectionAndEntry connectionAndDn = getConnectionAndEntry();
        PasswordModifyExtendedOperationDialog passwordDialog = new PasswordModifyExtendedOperationDialog(
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell(),
            connectionAndDn.connection, connectionAndDn.entry );
        passwordDialog.open();
    }


    // ── Identifying The Right Target ─────────────────────────────────────────────
    // Mace needed to know exactly who he was confronting before he walked in.
    // getConnectionAndEntry() resolves — in priority order — which connection and
    // which entry to pass to the dialog, checking entries, search results, bookmarks,
    // direct connections, and the editor input in turn.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Resolves the browser connection and optional target entry from the current selection.
     * We check the selection in priority order: selected entries first, then search
     * results, bookmarks, raw connections, and finally the view's input if it's a
     * browser connection.  The entry may be null if only a connection is available.
     *
     * @return a {@link ConnectionAndEntry} holding the connection and (possibly null) entry,
     *         or null if no valid connection can be found in the current selection
     */
    private ConnectionAndEntry getConnectionAndEntry()
    {
        if ( getSelectedEntries().length > 0 )
        {
            return new ConnectionAndEntry( getSelectedEntries()[0].getBrowserConnection(),
                getSelectedEntries()[0] );
        }
        else if ( getSelectedSearchResults().length > 0 )
        {
            return new ConnectionAndEntry( getSelectedSearchResults()[0].getEntry().getBrowserConnection(),
                getSelectedSearchResults()[0].getEntry() );
        }
        else if ( getSelectedBookmarks().length > 0 )
        {
            return new ConnectionAndEntry( getSelectedBookmarks()[0].getEntry().getBrowserConnection(),
                getSelectedBookmarks()[0].getEntry() );
        }
        else if ( getSelectedConnections().length > 0 )
        {
            Connection connection = getSelectedConnections()[0];
            IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnection( connection );
            return new ConnectionAndEntry( browserConnection, null );
        }
        else if ( getInput() instanceof IBrowserConnection )
        {
            return new ConnectionAndEntry( ( IBrowserConnection ) getInput(), null );
        }

        return null;
    }

    /**
     * Simple value holder pairing an {@link IBrowserConnection} with an optional
     * {@link IEntry} — used internally to pass both pieces of context to the dialog.
     * Think of it as Mace Windu's briefing notes: the location (connection) and the
     * subject (entry, if known).
     */
    protected class ConnectionAndEntry
    {
        private IBrowserConnection connection;
        private IEntry entry;


        // ── Briefing Notes Are Written ───────────────────────────────────────────
        // Mace's aide jots down the target details before the confrontation begins.
        // This constructor simply stores the connection and entry for later use.
        // ────────────────────────────────────────────────────────────────────────────────
        /**
         * Creates a ConnectionAndEntry pairing the given connection with an optional entry.
         *
         * @param connection  the browser connection representing the LDAP server;
         *                    used to check extension support and send the operation
         * @param entry       the LDAP entry whose password we're changing, or null if
         *                    the operation targets the connection's own bind identity
         */
        protected ConnectionAndEntry( IBrowserConnection connection, IEntry entry )
        {
            this.connection = connection;
            this.entry = entry;
        }
    }


    // ── Mace Reads The Office Door Sign ──────────────────────────────────────────
    // Even Mace glanced at the sign on Palpatine's door — just to confirm the room.
    // getText() returns the localised label for this action so Eclipse can label the
    // menu item correctly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localised display label for this action.
     *
     * @return the menu-item text for the password modify operation
     */
    public String getText()
    {
        return Messages.getString( "PasswordModifyExtendedOperationAction.Text" ); //$NON-NLS-1$
    }


    // ── Mace Carries No Insignia On This Mission ─────────────────────────────────
    // Mace went in without his council robe's formal insignia — he was going as a
    // Jedi, not as a council member.  No dedicated icon is registered for this action.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's icon.
     * No dedicated icon is registered for the password modify action.
     *
     * @return always null
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── No Standard Alliance Code ────────────────────────────────────────────────
    // Not every Jedi mission had a broadcast code — some were strictly off-the-books.
    // This action has no registered Eclipse command ID.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * No global command is registered for this action.
     *
     * @return always null
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Mace Checks The Server Supports The Confrontation ────────────────────────
    // Mace wouldn't walk into just any room and declare an arrest — he verified the
    // situation first.  isEnabled() checks that we have a valid connection, that it
    // has a rootDSE, and that the rootDSE declares support for the Password Modify
    // extended operation OID.  If the server doesn't advertise it, we can't use it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether the Password Modify action is available for the current selection.
     * We check that a valid connection/entry combination exists, that the server has
     * a loaded rootDSE, and that the rootDSE lists the RFC 3062 Password Modify
     * extended operation OID in its {@code supportedExtension} attribute.
     * If the server doesn't advertise that OID, we stay greyed out.
     *
     * @return true if the server supports Password Modify and a valid target exists
     */
    public boolean isEnabled()
    {
        return getConnectionAndEntry() != null
            && getConnectionAndEntry().connection != null
            && getConnectionAndEntry().connection.getRootDSE() != null
            && getConnectionAndEntry().connection.getRootDSE()
                .isExtensionSupported( PasswordModifyRequest.EXTENSION_OID );
    }

}
