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


import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;


// ── CLASS: ExtensionUtils — Checking the Falcon Has the Right Navigation Unit ─
// Before Han Solo starts the pre-flight sequence he checks that the ship in the
// docking bay really is the Millennium Falcon and not some other freighter.
// If the extension ID on the selected server doesn't match the known ApacheDS
// 2.0.0 extension point ID, we bail out early with an error dialog — just as
// Han would refuse to fly a different ship.
// ExtensionUtils is that identity check: a static utility with one public method
// that verifies a server is an ApacheDS 2.0.0 instance.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Static utility helpers shared between the action classes in this plugin.
 * Currently provides one method to verify that a selected {@link LdapServer}
 * is really an ApacheDS 2.0.0 server before attempting to operate on it.
 * If it isn't, a message dialog is shown and the caller should abort.
 * Think of this as Han Solo checking that the ship in the bay is actually
 * the Falcon before starting the pre-flight sequence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExtensionUtils
{
    private static final String EXTENSION_ID = "org.apache.directory.server.2.0.0"; //$NON-NLS-1$


    // ── Verify This Is the Right Ship Before Boarding ─────────────────────────
    // Compare the server's adapter extension ID to the known ApacheDS 2.0.0 ID.
    // If they don't match, show an error dialog and return false so the caller
    // can bail out immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Verifies that the given server uses the ApacheDS 2.0.0 adapter extension,
     * and shows an error dialog if it doesn't.
     *
     * <p>For example — Han checks before boarding:</p>
     * <pre>
     *   if (!ExtensionUtils.verifyApacheDs200OrPrintError(server, view)) {
     *       return; // wrong ship — abort
     *   }
     * </pre>
     *
     * @param server  the server to verify.
     * @param view    the Servers view, used as the parent shell for the error dialog.
     * @return        {@code true} if the server is an ApacheDS 2.0.0 server;
     *                {@code false} if not (an error dialog is also shown).
     */
    public static boolean verifyApacheDs200OrPrintError( LdapServer server, ServersView view )
    {
        // Checking that the server is really an ApacheDS 2.0.0 server
        if ( !EXTENSION_ID.equalsIgnoreCase( server.getLdapServerAdapterExtension().getId() ) )
        {
            String message = Messages.getString( "CreateConnectionAction.UnableReadServerConfiguration" ) //$NON-NLS-1$
                + "\n\n" //$NON-NLS-1$
                + Messages.getString( "CreateConnectionAction.NotA200Server" ); //$NON-NLS-1$

            reportErrorReadingServerConfiguration( view, message );
            return false;
        }
        else
        {
            return true;
        }
    }


    // ── Tell the Crew This Isn't the Right Ship ───────────────────────────────
    // Opens a simple modal error dialog using the Servers view shell as parent,
    // so the dialog stays on top of the correct window.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shows a modal error dialog reporting that the server configuration could
     * not be read (or that the server type doesn't match).
     *
     * @param view     the Servers view, used as the parent shell.
     * @param message  the error message to display.
     */
    private static void reportErrorReadingServerConfiguration( ServersView view, String message )
    {
        MessageDialog dialog = new MessageDialog( view.getSite().getShell(),
            Messages.getString( "CreateConnectionAction.UnableReadServerConfiguration" ), //$NON-NLS-1$
            null, message, MessageDialog.ERROR, new String[]
            { IDialogConstants.OK_LABEL }, MessageDialog.OK );
        dialog.open();
    }
}
