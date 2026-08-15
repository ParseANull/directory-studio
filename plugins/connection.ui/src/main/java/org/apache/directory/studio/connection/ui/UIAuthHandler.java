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

package org.apache.directory.studio.connection.ui;


import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionParameter;
import org.apache.directory.studio.connection.core.Credentials;
import org.apache.directory.studio.connection.core.IAuthHandler;
import org.apache.directory.studio.connection.core.ICredentials;
import org.apache.directory.studio.connection.core.PasswordsKeyStoreManager;
import org.apache.directory.studio.connection.ui.dialogs.PasswordDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;


// ── CLASS: UIAuthHandler — HAN AT THE IMPERIAL CHECKPOINT ─────────────────────────
// Every time the Falcon needs to pass an Imperial checkpoint, Han has to present
// the right access code.  UIAuthHandler is the person who decides what code to use:
//
//   Case 1: No bind principal (empty/null) → anonymous bind → return empty credentials.
//   Case 2: Passwords keystore is enabled:
//     2a. Keystore is not loaded → call PasswordsKeyStoreManagerUtils.askUserToLoadKeystore().
//         If the user fails → return null (cancel the bind).
//     2b. Keystore has a password for this connection → return it.
//     2c. Keystore has no stored password → fall through to ask the user.
//   Case 3: Passwords keystore is disabled:
//     3a. The connection parameter has a stored password → return it.
//     3b. No stored password → pop up a PasswordDialog.
//
// The PasswordDialog is always opened via syncExec so the LDAP background thread
// blocks until the user types in the password and clicks OK.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Production implementation of {@link IAuthHandler} that asks the user for the
 * bind password via a {@link PasswordDialog} when no stored password is available.
 *
 * <p>If the passwords keystore is enabled, we look there first.  If the keystore
 * is not yet unlocked, we call {@link PasswordsKeyStoreManagerUtils#askUserToLoadKeystore()}
 * to prompt the user for the master password before proceeding.</p>
 *
 * <p>Registered with {@link ConnectionCorePlugin} by {@link ConnectionUIPlugin#start}.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UIAuthHandler implements IAuthHandler
{
    // ── GET CREDENTIALS — THE FULL DECISION TREE ──────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns the credentials for the given connection parameter.
     *
     * <ul>
     *   <li>If the bind principal is empty, returns empty credentials (anonymous bind).</li>
     *   <li>If the keystore is enabled, looks up the password from the keystore.</li>
     *   <li>If the keystore is not loaded, prompts the user for the master password.</li>
     *   <li>If no stored password is found (keystore or connection parameter), opens
     *       a {@link PasswordDialog}.</li>
     * </ul>
     *
     * @param connectionParameter  The connection parameter to obtain credentials for.
     * @return  The {@link ICredentials} to use, or {@code null} if the user cancelled.
     */
    public ICredentials getCredentials( final ConnectionParameter connectionParameter )
    {
        // ── CASE 1: NO BIND PRINCIPAL — ANONYMOUS BIND ────────────────────────────
        if ( Strings.isEmpty( connectionParameter.getBindPrincipal() ) )
        {
            return new Credentials( StringUtils.EMPTY, StringUtils.EMPTY, connectionParameter );
        }
        else
        {
            // ── CASE 2: PASSWORDS KEYSTORE IS ENABLED ─────────────────────────────
            if ( PasswordsKeyStoreManagerUtils.isPasswordsKeystoreEnabled() )
            {
                PasswordsKeyStoreManager passwordsKeyStoreManager = ConnectionCorePlugin.getDefault()
                    .getPasswordsKeyStoreManager();

                // ── CASE 2a: KEYSTORE NOT LOADED — ASK FOR MASTER PASSWORD ─────────
                if ( !passwordsKeyStoreManager.isLoaded() && !PasswordsKeyStoreManagerUtils.askUserToLoadKeystore() )
                {
                    return null;
                }

                String password = passwordsKeyStoreManager.getConnectionPassword( connectionParameter.getId() );

                // ── CASE 2b: KEYSTORE HAS A STORED PASSWORD ───────────────────────
                if ( !Strings.isEmpty( password ) )
                {
                    return new Credentials( connectionParameter.getBindPrincipal(),
                        password, connectionParameter );
                }
                // ── CASE 2c: KEYSTORE HAS NO PASSWORD — ASK THE USER ──────────────
                else
                {
                    return askConnectionPassword( connectionParameter );
                }
            }
            // ── CASE 3: PASSWORDS KEYSTORE DISABLED ───────────────────────────────
            else
            {
                // ── CASE 3a: CONNECTION PARAMETER HAS A STORED PASSWORD ────────────
                if ( !Strings.isEmpty( connectionParameter.getBindPassword() ) )
                {
                    return new Credentials( connectionParameter.getBindPrincipal(),
                        connectionParameter.getBindPassword(), connectionParameter );
                }
                // ── CASE 3b: NO STORED PASSWORD — ASK THE USER ────────────────────
                else
                {
                    return askConnectionPassword( connectionParameter );
                }
            }
        }
    }


    // ── ASK CONNECTION PASSWORD — SHOW THE PASSWORD DIALOG ────────────────────────
    // We run the dialog on the SWT UI thread via syncExec.  The returned password
    // is captured in a one-element array (the standard lambda-capture workaround).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link PasswordDialog} on the SWT UI thread asking for the bind password.
     * Returns the typed credentials, or {@code null} if the user clicked Cancel.
     *
     * @param connectionParameter  The connection parameter (used for dialog title and label).
     * @return  A {@link Credentials} with the bind principal and entered password,
     *          or {@code null} if cancelled.
     */
    private Credentials askConnectionPassword( final ConnectionParameter connectionParameter )
    {
        final String[] password = new String[1];

        PlatformUI.getWorkbench().getDisplay().syncExec( () ->
        {
            PasswordDialog dialog = new PasswordDialog(
                PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                NLS.bind(
                    Messages.getString( "UIAuthHandler.EnterPasswordFor" ), new String[] //$NON-NLS-1$
                    { connectionParameter.getName() } ),
                NLS.bind(
                    Messages.getString( "UIAuthHandler.PleaseEnterPasswordOfUser" ),
                    connectionParameter.getBindPrincipal() ),
                StringUtils.EMPTY );

            if ( dialog.open() == PasswordDialog.OK )
            {
                password[0] = dialog.getPassword();
            }
        } );

        if ( password[0] != null )
        {
            return new Credentials( connectionParameter.getBindPrincipal(), password[0],
                connectionParameter );
        }

        return null;
    }
}
