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


import java.security.KeyStoreException;

import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.ui.dialogs.PasswordDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


// ── CLASS: PasswordsKeyStoreManagerUtils — HAN'S VAULT ACCESS CHECK ───────────────
// The passwords keystore is the Falcon's secure vault: all saved connection
// passwords live there, protected by a master password.
// Before the vault can be used, Han has to unlock it with his vault code.
// This utility class handles the two UI-level questions about that vault:
//   1. isPasswordsKeystoreEnabled() — is the vault even switched on in preferences?
//   2. askUserToLoadKeystore()      — pop up a password dialog, verify it against
//      the keystore, retry on failure, and cancel on user request.
// These are UI utilities (they show SWT dialogs), so they live here in
// connection.ui rather than in the headless connection.core.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static UI utilities for the connection passwords keystore feature.
 *
 * <p>Provides two helpers:</p>
 * <ul>
 *   <li>{@link #isPasswordsKeystoreEnabled()} — reads the preference to check
 *       whether the passwords keystore is turned on.</li>
 *   <li>{@link #askUserToLoadKeystore()} — opens a password dialog on the SWT
 *       thread, verifies the entered master password, and retries on failure.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class PasswordsKeyStoreManagerUtils
{
    /**
     * Prevents instantiation.  All methods are static.
     */
    private PasswordsKeyStoreManagerUtils()
    {
    }


    // ── IS PASSWORDS KEYSTORE ENABLED — CHECK THE PREFERENCE ──────────────────────
    /**
     * Returns {@code true} if the connection passwords keystore is enabled in
     * the connection core preferences.
     *
     * @return  {@code true} if the keystore is switched on.
     */
    public static boolean isPasswordsKeystoreEnabled()
    {
        return ConnectionCorePlugin.getDefault().getPluginPreferences()
            .getInt(
                ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE ) == ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE_ON;
    }


    // ── ASK USER TO LOAD KEYSTORE — PROMPT, VERIFY, RETRY ─────────────────────────
    // We run the whole interaction inside syncExec so this method blocks on the
    // calling (background) thread until the user finishes.
    // The loop structure:
    //   1. Show PasswordDialog asking for the master password.
    //   2. If the user cancels → return false.
    //   3. Call checkMasterPassword().  If it passes → return true.
    //   4. If it fails → show an error + Retry/Cancel dialog.
    //      If the user retries → go back to step 1.
    //      If the user cancels → return false.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link PasswordDialog} on the SWT UI thread and verifies the entered
     * master password against the passwords keystore.
     * Retries until the user enters the correct password or cancels.
     *
     * @return  {@code true} if the keystore was successfully loaded (correct password
     *          entered), {@code false} if the user cancelled.
     */
    public static boolean askUserToLoadKeystore()
    {
        final boolean[] keystoreLoaded = new boolean[1];
        keystoreLoaded[0] = false;

        PlatformUI.getWorkbench().getDisplay().syncExec( () ->
        {
            while ( true )
            {
                Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

                // ── STEP 1: ASK FOR THE MASTER PASSWORD ───────────────────────────
                PasswordDialog passwordDialog = new PasswordDialog( shell, Messages
                    .getString( "PasswordsKeyStoreManagerUtils.VerifyMasterPassword" ), //$NON-NLS-1$
                    Messages.getString( "PasswordsKeyStoreManagerUtils.PleaseEnterMasterPassword" ), null ); //$NON-NLS-1$

                if ( passwordDialog.open() == PasswordDialog.CANCEL )
                {
                    keystoreLoaded[0] = false;
                    return;
                }

                String masterPassword = passwordDialog.getPassword();

                // ── STEP 2: VERIFY THE PASSWORD ───────────────────────────────────
                Exception checkPasswordException = null;
                try
                {
                    if ( ConnectionCorePlugin.getDefault().getPasswordsKeyStoreManager()
                        .checkMasterPassword( masterPassword ) )
                    {
                        keystoreLoaded[0] = true;
                        break;
                    }
                }
                catch ( KeyStoreException e )
                {
                    checkPasswordException = e;
                }

                // ── STEP 3: SHOW ERROR AND OFFER RETRY ────────────────────────────
                String message;

                if ( checkPasswordException == null )
                {
                    message = Messages
                        .getString( "PasswordsKeyStoreManagerUtils.MasterPasswordVerificationFailed" ); //$NON-NLS-1$
                }
                else
                {
                    message = Messages
                        .getString( "PasswordsKeyStoreManagerUtils.MasterPasswordVerificationFailedWithException" ) //$NON-NLS-1$
                        + checkPasswordException.getMessage();
                }

                MessageDialog errorDialog = new MessageDialog(
                    shell,
                    Messages.getString( "PasswordsKeyStoreManagerUtils.VerifyMasterPasswordFailed" ), null, message, //$NON-NLS-1$
                    MessageDialog.ERROR, new String[]
                    {
                        IDialogConstants.RETRY_LABEL,
                        IDialogConstants.CANCEL_LABEL },
                    0 );

                if ( errorDialog.open() == MessageDialog.CANCEL )
                {
                    keystoreLoaded[0] = false;
                    return;
                }
                // User chose Retry — loop back to step 1.
            }
        } );

        return keystoreLoaded[0];
    }
}
