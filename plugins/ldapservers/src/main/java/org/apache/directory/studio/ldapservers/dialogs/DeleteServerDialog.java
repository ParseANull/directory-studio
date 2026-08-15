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

package org.apache.directory.studio.ldapservers.dialogs;


import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: DeleteServerDialog — THE REBEL COUNCIL AUTHORISES DESTROYING ALDERAAN ──────────
// In Rogue One, the Rebel council debates a dangerous, irreversible action — destroying the
// Death Star plans — and demands explicit confirmation before anyone proceeds.
// This dialog mirrors that moment: before we wipe a server's data from disk, we show the user
// a confirmation dialog ("Are you sure?") so an accidental click doesn't nuke their config.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link MessageDialog} that asks the user to confirm deletion of an LDAP server.
 * Deletion is irreversible — it removes the server from the registry AND deletes its data
 * directory from disk — so we always demand explicit OK/Cancel before proceeding.
 * Think of this as the Rebel Council's confirmation vote: no action happens until someone
 * clicks OK.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteServerDialog extends MessageDialog
{
    /** The server */
    protected LdapServer server;


    // ── The Council Reads Out The Charges ───────────────────────────────────────────────────
    // Mon Mothma stands before the Rebel Council and reads the full name of the target:
    // "We are proposing to destroy Alderaan — are you certain?"
    // We build the dialog with the server's name interpolated into the confirmation message.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new deletion confirmation dialog for the given server.
     * The dialog title reads "Delete Server" and the body asks "Are you sure you want to
     * delete [serverName]?" — this is our last firewall against accidental data loss.
     *
     * <p>For example — Mon Mothma convenes the Council:</p>
     * <pre>
     *   Dialog pops up: "Delete Server"
     *   Body: "Are you sure you want to delete 'ApacheDS-Local'?"
     *   Buttons: OK  |  Cancel
     *   Default focus: OK (user must consciously choose).
     * </pre>
     *
     * @param parentShell  the SWT shell that owns this dialog — typically the workbench window
     * @param server       the server slated for deletion; must not be null
     * @throws IllegalArgumentException  if server is null
     */
    public DeleteServerDialog( Shell parentShell, LdapServer server )
    {
        super( parentShell, Messages.getString( "DeleteServerDialog.DeleteServer" ), null, null, QUESTION, new String[] //$NON-NLS-1$
            { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, OK );

        if ( server == null )
        {
            throw new IllegalArgumentException();
        }

        this.server = server;
        message = NLS.bind( Messages.getString( "DeleteServerDialog.SureToDelete" ), server.getName() ); //$NON-NLS-1$
    }
}
