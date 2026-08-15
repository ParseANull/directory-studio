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


import org.apache.directory.server.config.beans.ConfigBean;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.ConnectionParameter.AuthenticationMethod;
import org.apache.directory.studio.connection.core.ConnectionParameter.EncryptionMethod;
import org.apache.directory.studio.connection.core.ConnectionServerType;
import org.apache.directory.studio.connection.core.DetectedConnectionProperties;
import org.apache.directory.studio.connection.core.PasswordsKeyStoreManager;
import org.apache.directory.studio.connection.ui.PasswordsKeyStoreManagerUtils;
import org.apache.directory.studio.ldapservers.actions.CreateConnectionActionHelper;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.views.ServersView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


// ── CLASS: CreateConnectionAction — Docking the Falcon at the Rebel Base ──────
// Once the Millennium Falcon has landed and the engines are running, Han wants
// to plug in to the Rebel base's communication network.  He checks the docking
// bay (Servers view selection), reads the Falcon's port manifest (config.ldif),
// and creates an LDAP Browser connection entry with the right host, port,
// encryption, and credentials so the Alliance computers can talk to the ship.
// CreateConnectionAction is that "plug in" step: it reads the running server's
// config, builds a ConnectionParameter object, and hands it to the connection
// framework via CreateConnectionActionHelper.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse action that creates an LDAP Browser connection to a selected
 * ApacheDS 2.0.0 server.
 * On {@link #run} we read the server's {@code config.ldif} to discover which
 * protocol (LDAP or LDAPS) is active, build a {@link ConnectionParameter} with
 * the correct host/port/encryption/credentials, and register it with the
 * LDAP Browser via {@link CreateConnectionActionHelper}.
 * Think of this as docking the Falcon and plugging into the Rebel base's
 * communication network — the ship (server) is running; we just need to hand
 * the connection parameters to the Alliance computers.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CreateConnectionAction implements IObjectActionDelegate
{
    /** The {@link ServersView} */
    private ServersView view;


    // ── Read the Port Manifest and Create the Docking Connection ─────────────
    // We verify the type, read the config, check that LDAP or LDAPS is enabled,
    // then call createConnection() to build and register the connection entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs the create-connection action.
     * <ol>
     *   <li>Gets the single selected server from the Servers view.</li>
     *   <li>Verifies it is an ApacheDS 2.0.0 server.</li>
     *   <li>Reads its {@code config.ldif} to get the active protocol and port.</li>
     *   <li>Checks that LDAP and/or LDAPS is enabled.</li>
     *   <li>Creates and registers an LDAP Browser connection entry.</li>
     * </ol>
     *
     * @param action  the triggering action (not used directly).
     */
    public void run( IAction action )
    {
        if ( view != null )
        {
            // Getting the selection
            StructuredSelection selection = ( StructuredSelection ) view.getViewer().getSelection();
            if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
            {
                // Getting the server
                LdapServer server = ( LdapServer ) selection.getFirstElement();

                // Checking that the server is really an ApacheDS 2.0.0 server
                if ( !ExtensionUtils.verifyApacheDs200OrPrintError( server, view ) )
                {
                    return;
                }

                // Parsing the 'config.ldif' file
                ConfigBean configuration = null;
                try
                {
                    configuration = ApacheDS200LdapServerAdapter.getServerConfiguration( server ).getConfigBean();
                }
                catch ( Exception e )
                {
                    String message = Messages.getString( "CreateConnectionAction.UnableReadServerConfiguration" ) //$NON-NLS-1$
                        + "\n\n" //$NON-NLS-1$
                        + Messages.getString( "CreateConnectionAction.FollowingErrorOccurred" ) + e.getMessage(); //$NON-NLS-1$

                    reportErrorReadingServerConfiguration( view, message );
                    return;
                }

                // Checking if we could read the 'server.xml' file
                if ( configuration == null )
                {
                    reportErrorReadingServerConfiguration( view,
                        Messages.getString( "CreateConnectionAction.UnableReadServerConfiguration" ) ); //$NON-NLS-1$
                    return;
                }

                // Checking is LDAP and/or LDAPS is/are enabled
                if ( ( ApacheDS200LdapServerAdapter.isEnableLdap( configuration ) )
                    || ( ApacheDS200LdapServerAdapter.isEnableLdaps( configuration ) ) )
                {
                    // Creating the connection using the helper class
                    createConnection( server, configuration );
                }
                else
                {
                    // LDAP and LDAPS protocols are disabled, we report this error to the user
                    MessageDialog dialog = new MessageDialog( view.getSite().getShell(),
                        Messages.getString( "CreateConnectionAction.UnableCreateConnection" ), null, //$NON-NLS-1$
                        Messages.getString( "CreateConnectionAction.LDAPAndLDAPSDisabled" ), MessageDialog.ERROR, //$NON-NLS-1$
                        new String[]
                            { IDialogConstants.OK_LABEL }, MessageDialog.OK );
                    dialog.open();
                }
            }
        }
    }


    // ── The Port Is Closed — Tell the Crew ────────────────────────────────────
    // Opens a modal error dialog when the config file can't be read, so the
    // operator knows why the connection couldn't be created.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Shows a modal error dialog reporting that the server configuration
     * could not be read.
     *
     * @param view     the Servers view, used as the parent shell.
     * @param message  the error message to display.
     */
    private void reportErrorReadingServerConfiguration( ServersView view, String message )
    {
        MessageDialog dialog = new MessageDialog( view.getSite().getShell(),
            Messages.getString( "CreateConnectionAction.UnableReadServerConfiguration" ), //$NON-NLS-1$
            null, message, MessageDialog.ERROR, new String[]
                { IDialogConstants.OK_LABEL }, MessageDialog.OK );
        dialog.open();
    }


    // ── Plug the Falcon into the Base Communication Network ───────────────────
    // We build a ConnectionParameter object with the right host, port, encryption,
    // credentials, and server-type metadata, then hand it to
    // CreateConnectionActionHelper so it appears in the LDAP Browser Connections view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a {@link ConnectionParameter} from the server's active configuration
     * and registers it as a new LDAP Browser connection.
     * Prefers LDAP over LDAPS when both are enabled.
     * Stores the password {@code "secret"} (the ApacheDS default admin password)
     * in the keystore if one is configured, otherwise stores it in clear text
     * on the parameter object.
     *
     * @param server         the server to connect to.
     * @param configuration  the parsed configuration bean from {@code config.ldif}.
     */
    private void createConnection( LdapServer server, ConfigBean configuration )
    {
        // Creating the connection parameter object
        ConnectionParameter connectionParameter = new ConnectionParameter();

        // Authentication method
        connectionParameter.setAuthMethod( AuthenticationMethod.SIMPLE );

        // LDAP or LDAPS?
        if ( ApacheDS200LdapServerAdapter.isEnableLdap( configuration ) )
        {
            connectionParameter.setEncryptionMethod( EncryptionMethod.NONE );
            connectionParameter.setPort( ApacheDS200LdapServerAdapter.getLdapPort( configuration ) );
        }
        else if ( ApacheDS200LdapServerAdapter.isEnableLdaps( configuration ) )
        {
            connectionParameter.setEncryptionMethod( EncryptionMethod.LDAPS );
            connectionParameter.setPort( ApacheDS200LdapServerAdapter.getLdapsPort( configuration ) );
        }

        // Bind password
        // Checking of the connection passwords keystore is enabled
        if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
        {
            // Getting the password keystore manager
            PasswordsKeyStoreManager passwordsKeyStoreManager = ConnectionCorePlugin.getDefault()
                .getPasswordsKeyStoreManager();

            // Checking if the keystore is loaded
            if ( passwordsKeyStoreManager.isLoaded() )
            {
                passwordsKeyStoreManager.storeConnectionPassword( connectionParameter.getId(), "secret" ); //$NON-NLS-1$
            }
            else
            {
                // Asking the user to load the keystore
                if ( PasswordsKeyStoreManagerUtils.askUserToLoadKeystore() )
                {
                    passwordsKeyStoreManager.storeConnectionPassword( connectionParameter.getId(), "secret" ); //$NON-NLS-1$
                }
            }
        }
        else
        {
            connectionParameter.setBindPassword( "secret" ); //$NON-NLS-1$
        }

        // Bind principal
        connectionParameter.setBindPrincipal( "uid=admin,ou=system" ); //$NON-NLS-1$

        // Host
        connectionParameter.setHost( "localhost" ); //$NON-NLS-1$

        // Name
        connectionParameter.setName( server.getName() );

        // Extended Properties
        connectionParameter.setExtendedProperty( DetectedConnectionProperties.CONNECTION_PARAMETER_SERVER_TYPE,
            ConnectionServerType.APACHEDS.toString() );
        connectionParameter.setExtendedProperty( DetectedConnectionProperties.CONNECTION_PARAMETER_VENDOR_NAME,
            "Apache Software Foundation" ); //$NON-NLS-1$
        connectionParameter.setExtendedProperty( DetectedConnectionProperties.CONNECTION_PARAMETER_VENDOR_VERSION,
            "2.0.0" ); //$NON-NLS-1$

        // Creating the connection
        CreateConnectionActionHelper.createLdapBrowserConnection( server, new Connection( connectionParameter ) );
    }


    // ── Nothing to Do on Selection Change ────────────────────────────────────
    // This action doesn't update its enabled state based on selection;
    // the action is always present in the context menu.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the selection in the Servers view changes.
     * This action does nothing on selection change — it is always enabled.
     *
     * @param action     the action.
     * @param selection  the new selection (unused).
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }


    // ── Connect This Action to the Docking Bay View ───────────────────────────
    // Eclipse calls this when the context menu is being built; we capture the
    // Servers view so run() can read the selection from it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Associates this action with the active {@link ServersView}.
     *
     * @param action      the action (unused).
     * @param targetPart  the workbench part; stored if it is a {@link ServersView}.
     */
    public void setActivePart( IAction action, IWorkbenchPart targetPart )
    {
        // Storing the Servers view
        if ( targetPart instanceof ServersView )
        {
            view = ( ServersView ) targetPart;
        }
    }
}
