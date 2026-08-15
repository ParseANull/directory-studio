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

package org.apache.directory.studio.connection.ui.preferences;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyStoreException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.directory.api.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionManager;
import org.apache.directory.studio.connection.core.PasswordsKeyStoreManager;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.dialogs.PasswordDialog;
import org.apache.directory.studio.connection.ui.dialogs.ResetPasswordDialog;
import org.apache.directory.studio.connection.ui.dialogs.SetupPasswordDialog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


// ── CLASS: PasswordsKeystorePreferencePage — THE REBEL BASE VAULT MANAGER ────────
// The Rebel Alliance stores connection bind passwords in an encrypted JKS keystore
// protected by a master password.  This preference page is the vault management
// terminal.  You can:
//   • Enable the vault for the first time (SetupPasswordDialog)
//   • Disable it — choose to either discard stored passwords or keep them by
//     re-hydrating them into the connection bean (after verifying the master
//     password via PasswordDialog)
//   • Change the master password (ResetPasswordDialog)
//
// All of this happens against a TEMPORARY copy of the keystore so that pressing
// "Cancel" leaves the live keystore untouched.  On performOk() we atomically
// overwrite the live keystore with the temp copy.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Preference page for the passwords keystore — an encrypted JKS file that stores
 * connection bind passwords keyed by connection ID.
 *
 * <p><b>How it works</b>: on {@link #init} we copy the live keystore to a temporary
 * file.  All enable/disable/change-password operations in the page run against that
 * temporary copy.  On {@link #performOk} the temporary file is saved and copied over
 * the live keystore; on {@link #performCancel} the temporary file is simply deleted.</p>
 *
 * <p>User flows:</p>
 * <ul>
 *   <li><b>Enable</b>: opens {@link SetupPasswordDialog}, loads the temp keystore
 *       with the chosen master password, migrates all existing bind passwords into it,
 *       clears the bind-password field on each connection bean.</li>
 *   <li><b>Disable (keep passwords)</b>: verifies master password via
 *       {@link PasswordDialog}, reads passwords back from the keystore into a backup
 *       map, then deletes the temp keystore file.</li>
 *   <li><b>Disable (discard passwords)</b>: deletes the temp keystore file; backup
 *       map stays empty.</li>
 *   <li><b>Change master password</b>: opens {@link ResetPasswordDialog}, verifies
 *       the current password, sets the new one, and saves the temp keystore.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordsKeystorePreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    // ── CONSTANTS ─────────────────────────────────────────────────────────────────

    /** Filename of the temporary keystore used for in-progress preference changes. */
    private static final String TEMPORARY_KEYSTORE_FILENAME = "passwords-prefs-temp.jks"; //$NON-NLS-1$


    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** Temporary keystore manager — all changes happen here until OK is pressed. */
    private PasswordsKeyStoreManager passwordsKeyStoreManager;

    /**
     * Backup of connection bind passwords loaded from the keystore when the user
     * chooses to disable the keystore but keep the passwords.  Applied back to the
     * connection beans on {@link #performOk}.
     */
    private Map<String, String> connectionsPasswordsBackup = new ConcurrentHashMap<>();

    /** The connection manager — needed to iterate connections for password migration. */
    private ConnectionManager connectionManager;


    // ── UI WIDGETS ────────────────────────────────────────────────────────────────

    /** Master "Enable passwords keystore" checkbox. */
    private Button enableKeystoreCheckbox;

    /** "Change Master Password…" button — enabled only when the keystore is enabled. */
    private Button changeMasterPasswordButton;


    // ── LISTENERS ─────────────────────────────────────────────────────────────────

    /**
     * Listener for the enable/disable checkbox.  Calls either
     * {@link #enablePasswordsKeystore()} or {@link #disablePasswordsKeystore()} and
     * rolls back the checkbox state if the operation fails or is cancelled.
     */
    private SelectionListener enableKeystoreCheckboxListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            Boolean selected = enableKeystoreCheckbox.getSelection();

            try
            {
                if ( selected )
                {
                    if ( !enablePasswordsKeystore() )
                    {
                        enableKeystoreCheckbox.setSelection( !selected );
                    }
                }
                else
                {
                    if ( !disablePasswordsKeystore() )
                    {
                        enableKeystoreCheckbox.setSelection( !selected );
                    }
                }
            }
            catch ( KeyStoreException kse )
            {
                CommonUIUtils.openErrorDialog( Messages
                    .getString( "PasswordsKeystorePreferencePage.AnErrorOccurredWhenEnablingDisablingTheKeystore" ) //$NON-NLS-1$
                    + kse.getMessage() );

                enableKeystoreCheckbox.setSelection( !selected );
            }

            updateButtonsEnabledState();
        }
    };

    /**
     * Listener for the "Change Master Password" button.  Delegates to
     * {@link #changeMasterPassword()}.
     */
    private SelectionListener changeMasterPasswordButtonListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected( SelectionEvent event )
        {
            changeMasterPassword();
        }
    };


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link PasswordsKeystorePreferencePage}.
     *
     * <p>Initialises the temporary {@link PasswordsKeyStoreManager} and obtains
     * a reference to the global {@link ConnectionManager}.</p>
     */
    public PasswordsKeystorePreferencePage()
    {
        super( Messages.getString( "PasswordsKeystorePreferencePage.PasswordsKeystore" ) ); //$NON-NLS-1$
        super.setDescription( Messages
            .getString( "PasswordsKeystorePreferencePage.GeneralSettingsForPasswordsKeystore" ) ); //$NON-NLS-1$
        super.noDefaultAndApplyButton();

        passwordsKeyStoreManager = new PasswordsKeyStoreManager( TEMPORARY_KEYSTORE_FILENAME );
        connectionManager = ConnectionCorePlugin.getDefault().getConnectionManager();
    }


    // ── INIT ──────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>If the live keystore file already exists, we copy it to the temporary
     * location so all subsequent changes are made against the copy.</p>
     */
    public void init( IWorkbench workbench )
    {
        // ── COPY LIVE KEYSTORE TO TEMP ────────────────────────────────────────────
        // We operate on the copy; if the user cancels, the live file is untouched.
        // ──────────────────────────────────────────────────────────────────────────
        File keystoreFile = ConnectionCorePlugin.getDefault().getPasswordsKeyStoreManager().getKeyStoreFile();

        if ( keystoreFile.exists() )
        {
            try
            {
                Files.copy( keystoreFile.toPath(), getTemporaryKeystoreFile().toPath() );
            }
            catch ( IOException e )
            {
                ConnectionUIPlugin
                    .getDefault()
                    .getLog()
                    .log(
                        new Status( Status.ERROR, ConnectionUIConstants.PLUGIN_ID, Status.ERROR,
                            "Couldn't duplicate the global keystore file.", e ) ); //$NON-NLS-1$
            }
        }
    }


    // ── GET TEMPORARY KEYSTORE FILE ───────────────────────────────────────────────
    /**
     * Returns the {@link File} handle for the temporary keystore file located in
     * the plugin's state-location directory.
     *
     * @return The temporary keystore {@link File}.
     */
    private File getTemporaryKeystoreFile()
    {
        return ConnectionCorePlugin.getDefault().getStateLocation().append( TEMPORARY_KEYSTORE_FILENAME ).toFile();
    }


    // ── CONTRIBUTE BUTTONS ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Adds the "Change Master Password…" button to the button bar, then updates
     * its enabled state to match the current checkbox selection.</p>
     */
    @Override
    protected void contributeButtons( Composite parent )
    {
        // ── ADD "CHANGE MASTER PASSWORD" BUTTON ───────────────────────────────────
        // We need an extra column on the parent's GridLayout to hold our button.
        // ──────────────────────────────────────────────────────────────────────────
        ( ( GridLayout ) parent.getLayout() ).numColumns++;

        changeMasterPasswordButton = BaseWidgetUtils.createButton( parent,
            Messages.getString( "PasswordsKeystorePreferencePage.ChangeMasterPasswordEllipsis" ), 1 ); //$NON-NLS-1$
        changeMasterPasswordButton.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        changeMasterPasswordButton.addSelectionListener( changeMasterPasswordButtonListener );

        updateButtonsEnabledState();
    }


    // ── CREATE CONTENTS ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Builds the preference page body: an enable/disable checkbox with a
     * warning label below it.  The checkbox is wired via {@link #addListeners()}.</p>
     */
    protected Control createContents( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 2, 1 );

        // ── ENABLE KEYSTORE CHECKBOX ──────────────────────────────────────────────
        enableKeystoreCheckbox = new Button( composite, SWT.CHECK | SWT.WRAP );
        enableKeystoreCheckbox
            .setText( Messages
                .getString( "PasswordsKeystorePreferencePage.StoreConnectionsPasswordsInPasswordProtectedKeystore" ) ); //$NON-NLS-1$
        enableKeystoreCheckbox.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false, 2, 1 ) );

        // ── WARNING LABEL ─────────────────────────────────────────────────────────
        // The keystore requires a master password — forgetting it means no passwords.
        // ──────────────────────────────────────────────────────────────────────────
        BaseWidgetUtils.createRadioIndent( composite, 1 );
        BaseWidgetUtils
            .createWrappedLabel(
                composite,
                Messages.getString( "PasswordsKeystorePreferencePage.WarningPasswordsKeystoreRequiresMasterPassword" ), //$NON-NLS-1$
                1 );

        initUI();
        addListeners();

        return composite;
    }


    // ── INIT UI ───────────────────────────────────────────────────────────────────
    /**
     * Reads the current keystore preference value and sets the checkbox accordingly.
     */
    private void initUI()
    {
        int connectionsPasswordsKeystore = ConnectionCorePlugin.getDefault().getPluginPreferences()
            .getInt( ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE );

        if ( connectionsPasswordsKeystore == ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE_OFF )
        {
            enableKeystoreCheckbox.setSelection( false );
        }
        else if ( connectionsPasswordsKeystore == ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE_ON )
        {
            enableKeystoreCheckbox.setSelection( true );
        }
    }


    // ── ADD LISTENERS ─────────────────────────────────────────────────────────────
    /**
     * Adds the enable/disable checkbox listener.
     */
    private void addListeners()
    {
        enableKeystoreCheckbox.addSelectionListener( enableKeystoreCheckboxListener );
    }


    // ── REMOVE LISTENERS ──────────────────────────────────────────────────────────
    /**
     * Removes the enable/disable checkbox listener (needed before programmatic
     * changes so we don't fire the listener mid-operation).
     */
    private void removeListeners()
    {
        enableKeystoreCheckbox.removeSelectionListener( enableKeystoreCheckboxListener );
    }


    // ── UPDATE BUTTONS ENABLED STATE ──────────────────────────────────────────────
    /**
     * Enables or disables the "Change Master Password" button based on whether
     * the keystore is currently enabled (checkbox checked).
     */
    private void updateButtonsEnabledState()
    {
        changeMasterPasswordButton.setEnabled( enableKeystoreCheckbox.getSelection() );
    }


    // ── PERFORM DEFAULTS ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Resets the keystore enable/disable setting to its platform default.
     * Listeners are temporarily removed to avoid triggering enable/disable flows
     * during the programmatic checkbox change.</p>
     */
    @Override
    protected void performDefaults()
    {
        removeListeners();

        int enablePasswordsKeystore = ConnectionCorePlugin.getDefault().getPluginPreferences()
            .getDefaultInt( ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE );

        if ( enablePasswordsKeystore == ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE_OFF )
        {
            enableKeystoreCheckbox.setSelection( false );
        }
        else if ( enablePasswordsKeystore == ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE_ON )
        {
            enableKeystoreCheckbox.setSelection( true );
        }

        updateButtonsEnabledState();
        addListeners();

        ConnectionCorePlugin.getDefault().savePluginPreferences();
        super.performDefaults();
    }


    // ── PERFORM OK ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Atomically commits all in-progress changes to the live keystore:</p>
     * <ul>
     *   <li>If <em>enabling</em>: saves the temp keystore, copies it over the live
     *       file, reloads the global manager, clears bind-password fields on all
     *       connections, saves connections, and records the ON preference.</li>
     *   <li>If <em>disabling</em>: resets the global manager, restores any backed-up
     *       passwords to connection beans, saves connections, and records the OFF
     *       preference.</li>
     * </ul>
     * Always deletes the temporary keystore file when done.
     *
     * @return Always {@code true}.
     */
    public boolean performOk()
    {
        PasswordsKeyStoreManager globalPasswordsKeyStoreManager = ConnectionCorePlugin.getDefault()
            .getPasswordsKeyStoreManager();

        if ( enableKeystoreCheckbox.getSelection() )
        {
            if ( passwordsKeyStoreManager.isLoaded() )
            {
                // ── SAVE TEMP KEYSTORE ────────────────────────────────────────────
                try
                {
                    passwordsKeyStoreManager.save();
                }
                catch ( KeyStoreException e )
                {
                    ConnectionUIPlugin
                        .getDefault()
                        .getLog()
                        .log(
                            new Status( Status.ERROR, ConnectionUIConstants.PLUGIN_ID, Status.ERROR,
                                "Couldn't save the temporary password keystore.", e ) ); //$NON-NLS-1$
                }

                // ── COPY TEMP → LIVE ──────────────────────────────────────────────
                try
                {
                    Files.copy( getTemporaryKeystoreFile().toPath(), ConnectionCorePlugin.getDefault()
                        .getPasswordsKeyStoreManager().getKeyStoreFile().toPath() );
                }
                catch ( IOException e )
                {
                    ConnectionUIPlugin
                        .getDefault()
                        .getLog()
                        .log(
                            new Status( Status.ERROR, ConnectionUIConstants.PLUGIN_ID, Status.ERROR,
                                "Couldn't copy the temporary keystore as the global keystore.", e ) ); //$NON-NLS-1$
                }

                // ── RELOAD GLOBAL KEYSTORE ────────────────────────────────────────
                try
                {
                    globalPasswordsKeyStoreManager.reload( passwordsKeyStoreManager.getMasterPassword() );
                }
                catch ( KeyStoreException e )
                {
                    ConnectionUIPlugin
                        .getDefault()
                        .getLog()
                        .log(
                            new Status( Status.ERROR, ConnectionUIConstants.PLUGIN_ID, Status.ERROR,
                                "Couldn't reload the global keystore file.", e ) ); //$NON-NLS-1$
                }

                // ── CLEAR PLAIN-TEXT PASSWORDS FROM CONNECTION BEANS ──────────────
                // Now that they are safe in the keystore, we remove them from the
                // serialised connection parameters.
                // ──────────────────────────────────────────────────────────────────
                for ( Connection connection : connectionManager.getConnections() )
                {
                    connection.getConnectionParameter().setBindPassword( null );
                }

                ConnectionCorePlugin.getDefault().getConnectionManager().saveConnections();

                ConnectionCorePlugin.getDefault().getPluginPreferences()
                    .setValue( ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE,
                        ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE_ON );
            }
        }
        else
        {
            // ── DISABLE: RESET GLOBAL KEYSTORE ────────────────────────────────────
            globalPasswordsKeyStoreManager.reset();

            // ── RESTORE BACKED-UP PASSWORDS ───────────────────────────────────────
            if ( !connectionsPasswordsBackup.isEmpty() )
            {
                for ( Map.Entry<String, String> entry : connectionsPasswordsBackup.entrySet() )
                {
                    Connection connection = connectionManager.getConnectionById( entry.getKey() );

                    if ( connection != null )
                    {
                        connection.getConnectionParameter().setBindPassword( entry.getValue() );
                    }
                }

                ConnectionCorePlugin.getDefault().getConnectionManager().saveConnections();
            }

            ConnectionCorePlugin.getDefault().getPluginPreferences()
                .setValue( ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE,
                    ConnectionCoreConstants.PREFERENCE_CONNECTIONS_PASSWORDS_KEYSTORE_OFF );
        }

        ConnectionCorePlugin.getDefault().savePluginPreferences();

        deleteTemporaryKeystore();
        return true;
    }


    // ── PERFORM CANCEL ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Deletes the temporary keystore file so no partial changes linger on disk.</p>
     *
     * @return Always {@code true}.
     */
    public boolean performCancel()
    {
        deleteTemporaryKeystore();
        return true;
    }


    // ── DELETE TEMPORARY KEYSTORE ─────────────────────────────────────────────────
    /**
     * Deletes the temporary keystore file if it exists.
     *
     * <p>Called from both {@link #performOk} and {@link #performCancel} to ensure
     * the temp file is cleaned up regardless of how the page is closed.</p>
     */
    private void deleteTemporaryKeystore()
    {
        File temporaryKeystoreFile = getTemporaryKeystoreFile();

        if ( temporaryKeystoreFile.exists() )
        {
            FileUtils.deleteQuietly( temporaryKeystoreFile );
        }
    }


    // ── ENABLE PASSWORDS KEYSTORE ─────────────────────────────────────────────────
    /**
     * Runs the enable-keystore flow:
     * <ol>
     *   <li>Opens {@link SetupPasswordDialog} to get a new master password.</li>
     *   <li>Loads the temporary keystore with that password.</li>
     *   <li>Migrates all existing bind passwords from connection beans into the keystore.</li>
     *   <li>Saves the temp keystore to disk.</li>
     * </ol>
     *
     * @return {@code true} if the keystore was successfully enabled;
     *         {@code false} if the user cancelled.
     * @throws KeyStoreException If the keystore could not be loaded or saved.
     */
    private boolean enablePasswordsKeystore() throws KeyStoreException
    {
        // ── PROMPT FOR MASTER PASSWORD ────────────────────────────────────────────
        SetupPasswordDialog setupPasswordDialog = new SetupPasswordDialog(
            enableKeystoreCheckbox.getShell(),
            Messages.getString( "PasswordsKeystorePreferencePage.SetupMasterPassword" ), //$NON-NLS-1$
            Messages.getString( "PasswordsKeystorePreferencePage.PleaseEnterMasterPasswordToSecurePasswordsKeystore" ), //$NON-NLS-1$
            null );

        if ( setupPasswordDialog.open() == SetupPasswordDialog.OK )
        {
            String masterPassword = setupPasswordDialog.getPassword();

            // Load the temp keystore with the new master password
            passwordsKeyStoreManager.load( masterPassword );

            // ── MIGRATE EXISTING PASSWORDS ────────────────────────────────────────
            for ( Connection connection : connectionManager.getConnections() )
            {
                String connectionPassword = connection.getBindPassword();

                if ( connectionPassword != null )
                {
                    passwordsKeyStoreManager.storeConnectionPassword( connection, connectionPassword, false );
                }
            }

            passwordsKeyStoreManager.save();

            return true;
        }

        return false;
    }


    // ── DISABLE PASSWORDS KEYSTORE ────────────────────────────────────────────────
    /**
     * Runs the disable-keystore flow:
     * <ol>
     *   <li>Asks the user whether to keep their stored passwords.</li>
     *   <li>If keeping: prompts for the master password (with retry loop) and
     *       populates {@link #connectionsPasswordsBackup}.</li>
     *   <li>Deletes the temp keystore file regardless.</li>
     * </ol>
     *
     * @return {@code true} if the keystore was successfully disabled;
     *         {@code false} if the user cancelled.
     */
    private boolean disablePasswordsKeystore()
    {
        // ── ASK: KEEP OR DISCARD STORED PASSWORDS? ────────────────────────────────
        MessageDialog keepConnectionsPasswordsDialog = new MessageDialog(
            enableKeystoreCheckbox.getShell(),
            Messages.getString( "PasswordsKeystorePreferencePage.KeepConnectionsPasswords" ), //$NON-NLS-1$
            null,
            Messages.getString( "PasswordsKeystorePreferencePage.DoYouWantToKeepYourConnectionsPasswords" ), //$NON-NLS-1$
            MessageDialog.QUESTION, new String[]
                { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
                    IDialogConstants.CANCEL_LABEL }, 0 );
        int keepConnectionsPasswordsValue = keepConnectionsPasswordsDialog.open();

        if ( keepConnectionsPasswordsValue == 1 )
        {
            // ── DISCARD: delete the keystore without extracting passwords ─────────
            connectionsPasswordsBackup.clear();
            passwordsKeyStoreManager.deleteKeystoreFile();
            return true;
        }
        else if ( keepConnectionsPasswordsValue == 0 )
        {
            // ── KEEP: verify master password then extract passwords ────────────────
            connectionsPasswordsBackup.clear();

            while ( true )
            {
                PasswordDialog passwordDialog = new PasswordDialog(
                    enableKeystoreCheckbox.getShell(),
                    Messages.getString( "PasswordsKeystorePreferencePage.VerifyMasterPassword" ), //$NON-NLS-1$
                    Messages.getString( "PasswordsKeystorePreferencePage.PleaseEnterYourMasterPassword" ), //$NON-NLS-1$
                    null );

                if ( passwordDialog.open() == PasswordDialog.CANCEL )
                {
                    return false;
                }

                String password = passwordDialog.getPassword();
                Exception checkPasswordException = null;

                try
                {
                    if ( passwordsKeyStoreManager.checkMasterPassword( password ) )
                    {
                        break;
                    }
                }
                catch ( KeyStoreException e )
                {
                    checkPasswordException = e;
                }

                // ── WRONG PASSWORD: offer retry or cancel ─────────────────────────
                String message = ( checkPasswordException == null )
                    ? Messages.getString( "PasswordsKeystorePreferencePage.MasterPasswordVerificationFailed" ) //$NON-NLS-1$
                    : Messages.getString( "PasswordsKeystorePreferencePage.MasterPasswordVerificationFailedWithException" ) //$NON-NLS-1$
                        + checkPasswordException.getMessage();

                MessageDialog errorDialog = new MessageDialog(
                    enableKeystoreCheckbox.getShell(),
                    Messages.getString( "PasswordsKeystorePreferencePage.VerifyMasterPasswordFailed" ), //$NON-NLS-1$
                    null, message, MessageDialog.ERROR, new String[]
                        { IDialogConstants.RETRY_LABEL, IDialogConstants.CANCEL_LABEL }, 0 );

                if ( errorDialog.open() == MessageDialog.CANCEL )
                {
                    return false;
                }
            }

            // ── POPULATE BACKUP MAP ───────────────────────────────────────────────
            String[] connectionIds = passwordsKeyStoreManager.getConnectionIds();

            if ( connectionIds != null )
            {
                for ( String connectionId : connectionIds )
                {
                    String password = passwordsKeyStoreManager.getConnectionPassword( connectionId );

                    if ( password != null )
                    {
                        connectionsPasswordsBackup.put( connectionId, password );
                    }
                }
            }

            passwordsKeyStoreManager.deleteKeystoreFile();

            return true;
        }
        else
        {
            // ── CANCELLED ────────────────────────────────────────────────────────
            return false;
        }
    }


    // ── CHANGE MASTER PASSWORD ────────────────────────────────────────────────────
    /**
     * Runs the change-master-password flow:
     * <ol>
     *   <li>Opens {@link ResetPasswordDialog} for current + new password.</li>
     *   <li>Verifies the current password against the temp keystore (with retry loop).</li>
     *   <li>Sets the new master password on the temp keystore and saves it.</li>
     * </ol>
     *
     * <p>Returns silently (no return value) — success is implied if the dialog
     * completes without cancellation or error.</p>
     */
    private void changeMasterPassword()
    {
        String newMasterPassword = null;

        while ( true )
        {
            // ── PROMPT FOR CURRENT + NEW PASSWORD ─────────────────────────────────
            ResetPasswordDialog resetPasswordDialog = new ResetPasswordDialog(
                changeMasterPasswordButton.getShell(),
                StringUtils.EMPTY, null, null );

            if ( resetPasswordDialog.open() != ResetPasswordDialog.OK )
            {
                return;
            }

            // ── VERIFY CURRENT PASSWORD ───────────────────────────────────────────
            Exception checkPasswordException = null;

            try
            {
                if ( passwordsKeyStoreManager.checkMasterPassword( resetPasswordDialog.getCurrentPassword() ) )
                {
                    newMasterPassword = resetPasswordDialog.getNewPassword();
                    break;
                }
            }
            catch ( KeyStoreException e )
            {
                checkPasswordException = e;
            }

            // ── WRONG PASSWORD: offer retry or cancel ─────────────────────────────
            String message = ( checkPasswordException == null )
                ? Messages.getString( "PasswordsKeystorePreferencePage.MasterPasswordVerificationFailed" ) //$NON-NLS-1$
                : Messages.getString( "PasswordsKeystorePreferencePage.MasterPasswordVerificationFailedWithException" ) //$NON-NLS-1$
                    + checkPasswordException.getMessage();

            MessageDialog errorDialog = new MessageDialog(
                enableKeystoreCheckbox.getShell(),
                Messages.getString( "PasswordsKeystorePreferencePage.VerifyMasterPasswordFailed" ), //$NON-NLS-1$
                null, message, MessageDialog.ERROR, new String[]
                    { IDialogConstants.RETRY_LABEL, IDialogConstants.CANCEL_LABEL }, 0 );

            if ( errorDialog.open() == MessageDialog.CANCEL )
            {
                return;
            }
        }

        if ( newMasterPassword != null )
        {
            try
            {
                passwordsKeyStoreManager.setMasterPassword( newMasterPassword );
                passwordsKeyStoreManager.save();
            }
            catch ( KeyStoreException e )
            {
                ConnectionUIPlugin
                    .getDefault()
                    .getLog()
                    .log(
                        new Status( Status.ERROR, ConnectionUIConstants.PLUGIN_ID, Status.ERROR,
                            "Couldn't save the keystore file.", e ) ); //$NON-NLS-1$
            }
        }
    }
}
