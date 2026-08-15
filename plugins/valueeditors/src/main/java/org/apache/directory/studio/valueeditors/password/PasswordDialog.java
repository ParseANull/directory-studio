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

package org.apache.directory.studio.valueeditors.password;


import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionParameter.AuthenticationMethod;
import org.apache.directory.studio.connection.core.jobs.CheckBindRunnable;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.Password;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;


// ── CLASS: PasswordDialog — VADER'S CREDENTIAL VERIFICATION TERMINAL ─────────
// Deep in the Death Star's command corridor, Vader's security terminal offers
// two tabs: "Current Password" (inspect the stored hash, test the plain-text
// credential against it, or attempt a live bind) and "New Password" (choose a
// new secret, pick a hash algorithm, preview the result before committing).
// The OK button only lights up when the two "new password" fields match — no
// partial credentials are ever committed to the Imperial database.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Modal dialog for viewing, verifying, and replacing an LDAP {@code userPassword}
 * attribute value.
 * The dialog has two tabs:
 * <ul>
 *   <li><strong>Current Password</strong> — shows the stored hash algorithm, the
 *       hashed bytes and salt in hex, and lets the user test a plain-text
 *       password against the stored hash (local verify) or against the live
 *       directory (LDAP bind).</li>
 *   <li><strong>New Password</strong> — lets the user enter and confirm a new
 *       password, choose a hash algorithm (SHA, SHA256, SSHA, MD5, crypt, …),
 *       and preview the encoded result before committing.</li>
 * </ul>
 * OK is disabled until the two new-password fields match; it is also unavailable
 * on the "Current Password" tab so the user must deliberately switch to the
 * "New Password" tab to overwrite the credential.
 * Think of this as Vader's credential verification terminal — inspect the
 * stored secret on one side, provision a replacement on the other.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordDialog extends Dialog
{
    /** The constant for no hash method */
    private static final String NO_HASH_METHOD = "NO-HASH-METHOD";

    /** The supported hash methods */
    private static final Object[] HASH_METHODS =
        {
            LdapSecurityConstants.HASH_METHOD_SHA,
            LdapSecurityConstants.HASH_METHOD_SHA256,
            LdapSecurityConstants.HASH_METHOD_SHA384,
            LdapSecurityConstants.HASH_METHOD_SHA512,
            LdapSecurityConstants.HASH_METHOD_SSHA,
            LdapSecurityConstants.HASH_METHOD_SSHA256,
            LdapSecurityConstants.HASH_METHOD_SSHA384,
            LdapSecurityConstants.HASH_METHOD_SSHA512,
            LdapSecurityConstants.HASH_METHOD_MD5,
            LdapSecurityConstants.HASH_METHOD_SMD5,
            LdapSecurityConstants.HASH_METHOD_PKCS5S2,
            LdapSecurityConstants.HASH_METHOD_CRYPT,
            LdapSecurityConstants.HASH_METHOD_CRYPT_MD5,
            LdapSecurityConstants.HASH_METHOD_CRYPT_SHA256,
            LdapSecurityConstants.HASH_METHOD_CRYPT_SHA512,
            NO_HASH_METHOD };

    /** Constant for the Current Password tab */
    private static final int CURRENT_TAB = 0;

    /** Constant for the New Password tab */
    private static final int NEW_TAB = 1;

    /** Constant for the selected tab dialog settings key */
    private static final String SELECTED_TAB_DIALOGSETTINGS_KEY = PasswordDialog.class.getName() + ".tab"; //$NON-NLS-1$

    /** Constant for the selected hash method dialog settings key */
    private static final String SELECTED_HASH_METHOD_DIALOGSETTINGS_KEY = PasswordDialog.class.getName()
        + ".hashMethod"; //$NON-NLS-1$

    /** The display mode */
    private DisplayMode displayMode;

    /** The associated entry for binding */
    private IEntry entry;

    /** The current password */
    private Password currentPassword;

    /** The new password */
    private Password newPassword;

    /** The return password */
    private byte[] returnPassword;

    // UI widgets
    private Button okButton;
    private TabFolder tabFolder;
    private TabItem currentPasswordTab;
    private Composite currentPasswordComposite;
    private Text currentPasswordText;
    private Text currentPasswordHashMethodText;
    private Text currentPasswordValueHexText;
    private Text currentPasswordSaltHexText;
    private Button showCurrentPasswordDetailsButton;
    private Text testPasswordText;
    private Text testBindDnText;
    private Button showTestPasswordDetailsButton;
    private Button verifyPasswordButton;
    private Button bindPasswordButton;
    private TabItem newPasswordTab;
    private Composite newPasswordComposite;
    private Text newPasswordText;
    private Text confirmNewPasswordText;
    private ComboViewer newPasswordHashMethodComboViewer;
    private Text newPasswordPreviewText;
    private Text newPasswordPreviewValueHexText;
    private Text newPasswordPreviewSaltHexText;
    private Button newSaltButton;
    private Button showNewPasswordDetailsButton;


    // ── Vader's Terminal Opens With the Stored Credential ────────────────────
    // The terminal is initialised with the current hashed credential (raw bytes)
    // and the entry whose DN will be used for the live-bind test.
    // If the raw bytes can't be parsed (malformed hash prefix), we quietly set
    // currentPassword to null so the "Current Password" tab is suppressed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new PasswordDialog.
     * Parses the current password bytes into a {@link Password} model.
     * If the bytes are null or fail to parse, we start in "new password only"
     * mode (no "Current Password" tab).
     *
     * <p>For example — the terminal opens with the stored credential:</p>
     * <pre>
     *   PasswordDialog dialog = new PasswordDialog(shell, entry.getPasswordBytes(), entry);
     *   dialog.open();
     * </pre>
     *
     * @param parentShell     The SWT shell that owns this dialog.
     * @param currentPassword The current hashed password as raw bytes, or {@code null}.
     * @param entry           The LDAP entry whose DN will be used for live bind tests.
     */
    public PasswordDialog( Shell parentShell, byte[] currentPassword, IEntry entry )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );

        try
        {
            this.currentPassword = currentPassword != null ? new Password( currentPassword ) : null;
        }
        catch ( IllegalArgumentException e )
        {
        }

        this.entry = entry;

        this.returnPassword = null;
    }


    // ── Vader Labels the Terminal ─────────────────────────────────────────────
    // The terminal is labelled "Password Editor" and receives the Imperial lock
    // icon so it is unmistakably identified as the credential management console.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the dialog shell — sets the window title and icon.
     *
     * @param shell  The SWT Shell to configure.
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "PasswordDialog.PasswordEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_PASSWORDEDITOR ) );
    }


    // ── Vader Commits the New Credential ─────────────────────────────────────
    // When OK is pressed, the newly composed password is serialised to bytes and
    // stored as the return value.  We also persist the user's choice of hash
    // method to dialog settings so the same algorithm is pre-selected next time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks OK — serialises the new password to bytes and
     * saves the selected hash algorithm to dialog settings for next time.
     * If no new password was entered (the user left "New Password" blank), we
     * set the return password to {@code null}.
     */
    protected void okPressed()
    {
        // create password
        if ( newPassword != null )
        {
            returnPassword = newPassword.toBytes();
        }
        else
        {
            returnPassword = null;
        }

        // save selected hash method to dialog settings, selected tab will be
        // saved on close()
        LdapSecurityConstants selectedHashMethod = getSelectedNewPasswordHashMethod();

        if ( selectedHashMethod == null )
        {
            ValueEditorsActivator.getDefault().getDialogSettings().put( SELECTED_HASH_METHOD_DIALOGSETTINGS_KEY,
                NO_HASH_METHOD );
        }
        else
        {
            ValueEditorsActivator.getDefault().getDialogSettings().put( SELECTED_HASH_METHOD_DIALOGSETTINGS_KEY,
                selectedHashMethod.getName() );
        }

        super.okPressed();
    }


    // ── Vader's Terminal Saves Its State on Exit ──────────────────────────────
    // When the terminal closes (whether OK or Cancel), we remember which tab the
    // user was on so it reopens to the same view next time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the currently active tab index to dialog settings before closing so
     * the same tab is shown when the dialog reopens.
     *
     * @return {@code true} if the shell was successfully closed.
     */
    public boolean close()
    {
        // save selected tab to dialog settings
        ValueEditorsActivator.getDefault().getDialogSettings().put( SELECTED_TAB_DIALOGSETTINGS_KEY,
            tabFolder.getSelectionIndex() );

        return super.close();
    }


    // ── Vader's Terminal Restores Its Previous Settings ───────────────────────
    // The OK and Cancel buttons are created first.  Then we restore the last
    // selected tab and hash algorithm from dialog settings.  If no current
    // password exists we force the New Password tab.  Finally we trigger an
    // initial update so the UI reflects the loaded state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the OK and Cancel buttons, then restores previously saved dialog
     * settings (last active tab, last chosen hash algorithm).
     * If there is no current password we force the "New Password" tab.
     * Triggers an initial UI refresh via {@link #updateTabFolder()}.
     *
     * @param parent  The composite that hosts the button bar.
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        // load dialog settings
        try
        {
            int tabIndex = ValueEditorsActivator.getDefault().getDialogSettings().getInt(
                SELECTED_TAB_DIALOGSETTINGS_KEY );
            if ( currentPassword == null || currentPassword.toBytes().length == 0 )
            {
                tabIndex = NEW_TAB;
            }
            tabFolder.setSelection( tabIndex );
        }
        catch ( Exception e )
        {
        }

        try
        {
            String hashMethodName = ValueEditorsActivator.getDefault().getDialogSettings().get(
                SELECTED_HASH_METHOD_DIALOGSETTINGS_KEY );

            LdapSecurityConstants hashMethod = LdapSecurityConstants.getAlgorithm( hashMethodName );

            if ( ( hashMethod == null ) || NO_HASH_METHOD.equals( hashMethodName ) )
            {
                newPasswordHashMethodComboViewer.setSelection( new StructuredSelection( NO_HASH_METHOD ) );
            }
            else
            {
                newPasswordHashMethodComboViewer.setSelection( new StructuredSelection( hashMethod ) );
            }
        }
        catch ( Exception e )
        {
        }

        // update on load
        updateTabFolder();
    }


    // ── Vader's Terminal Builds the Two-Tab Console ───────────────────────────
    // The main panel is a tab folder.  If a current password exists, we build
    // the "Current Password" inspection tab first; then we always build the
    // "New Password" provisioning tab.  Listeners are attached after both tabs
    // are ready so no premature update fires during construction.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content — a tab folder with a "Current Password"
     * inspection tab (if a password exists) and a "New Password" provisioning tab.
     * Returns the top-level composite.
     *
     * <p>For example — the two-tab console layout:</p>
     * <pre>
     *   [ Current Password | New Password ]
     *   ┌─────────────────────────────────┐
     *   │ Current Password: ••••••••       │
     *   │ Hash Method: SSHA               │
     *   │ Verify: [ _________ ] [Verify]  │
     *   └─────────────────────────────────┘
     * </pre>
     *
     * @param parent  The parent composite provided by JFace.
     * @return        The top-level composite containing the tab folder.
     */
    protected Control createDialogArea( Composite parent )
    {
        // Composite
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 3 / 2;
        gd.heightHint = convertVerticalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 2 / 3;
        composite.setLayoutData( gd );

        // Tab folder
        tabFolder = new TabFolder( composite, SWT.TOP );
        tabFolder.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        tabFolder.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                updateTabFolder();
            }
        } );

        // Checking the current password
        if ( currentPassword != null && currentPassword.toBytes().length > 0 )
        {
            // Setting the display mode
            displayMode = DisplayMode.CURRENT_AND_NEW_PASSWORD;

            // Creating the current password tab
            createCurrentPasswordTab();
        }
        else
        {
            // Setting the display mode
            displayMode = DisplayMode.NEW_PASSWORD_ONLY;
        }

        // Creating the new password tab
        createNewPasswordTab();

        addListeners();

        applyDialogFont( composite );
        return composite;
    }


    // ── Vader's Terminal Builds the Inspection Tab ────────────────────────────
    // The "Current Password" tab shows the stored hash in masked form, its
    // algorithm, the hex-encoded hash bytes and salt, and a verify field where
    // the user can type a test password.  "Show details" checkboxes unmask the
    // fields.  "Verify" runs a local hash comparison; "Bind" executes a live
    // LDAP bind.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and populates the "Current Password" tab — shows the stored hash,
     * hash algorithm, hex values, and provides Verify and Bind buttons for
     * testing a plain-text password against the stored credential.
     */
    private void createCurrentPasswordTab()
    {
        // Current password composite
        currentPasswordComposite = new Composite( tabFolder, SWT.NONE );
        GridLayout currentLayout = new GridLayout( 2, false );
        currentLayout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
        currentLayout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
        currentLayout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
        currentLayout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
        currentPasswordComposite.setLayout( currentLayout );
        currentPasswordComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Current password text
        BaseWidgetUtils.createLabel( currentPasswordComposite, Messages
            .getString( "PasswordDialog.CurrentPassword" ) + ":", 1 ); //$NON-NLS-1$//$NON-NLS-2$
        currentPasswordText = BaseWidgetUtils.createReadonlyText( currentPasswordComposite, "", 1 ); //$NON-NLS-1$

        // Current password details composite
        new Label( currentPasswordComposite, SWT.NONE );
        Composite currentPasswordDetailsComposite = BaseWidgetUtils.createColumnContainer( currentPasswordComposite,
            2, 1 );

        // Current password hash method label
        BaseWidgetUtils.createLabel( currentPasswordDetailsComposite,
            Messages.getString( "PasswordDialog.HashMethod" ), 1 ); //$NON-NLS-1$
        currentPasswordHashMethodText = BaseWidgetUtils.createLabeledText( currentPasswordDetailsComposite, "", 1 ); //$NON-NLS-1$

        // Current password hex label
        BaseWidgetUtils.createLabel( currentPasswordDetailsComposite, Messages
            .getString( "PasswordDialog.PasswordHex" ), 1 ); //$NON-NLS-1$
        currentPasswordValueHexText = BaseWidgetUtils.createLabeledText( currentPasswordDetailsComposite, "", 1 ); //$NON-NLS-1$

        // Current password salt hex label
        BaseWidgetUtils.createLabel( currentPasswordDetailsComposite,
            Messages.getString( "PasswordDialog.SaltHex" ), 1 ); //$NON-NLS-1$
        currentPasswordSaltHexText = BaseWidgetUtils.createLabeledText( currentPasswordDetailsComposite, "", 1 ); //$NON-NLS-1$

        // Show current password details button
        showCurrentPasswordDetailsButton = BaseWidgetUtils.createCheckbox( currentPasswordDetailsComposite, Messages
            .getString( "PasswordDialog.ShowCurrentPasswordDetails" ), 2 ); //$NON-NLS-1$

        // Verify password text
        BaseWidgetUtils
            .createLabel( currentPasswordComposite, Messages.getString( "PasswordDialog.VerifyPassword" ), 1 ); //$NON-NLS-1$
        testPasswordText = BaseWidgetUtils.createText( currentPasswordComposite, "", 1 ); //$NON-NLS-1$

        // Verify password details composite
        new Label( currentPasswordComposite, SWT.NONE );
        Composite testPasswordDetailsComposite = BaseWidgetUtils.createColumnContainer( currentPasswordComposite, 2,
            1 );

        // Bind DN label
        BaseWidgetUtils.createLabel( testPasswordDetailsComposite, Messages.getString( "PasswordDialog.BindDn" ), 1 ); //$NON-NLS-1$
        testBindDnText = BaseWidgetUtils.createLabeledText( testPasswordDetailsComposite, "", 1 ); //$NON-NLS-1$

        // Show verify password details button
        showTestPasswordDetailsButton = BaseWidgetUtils.createCheckbox( testPasswordDetailsComposite, Messages
            .getString( "PasswordDialog.ShowTestPasswordDetails" ), 2 ); //$NON-NLS-1$

        // Verify password buttons composite
        new Label( currentPasswordComposite, SWT.NONE );
        Composite verifyPasswordButtonsComposite = BaseWidgetUtils.createColumnContainer( currentPasswordComposite,
            2, 1 );

        // Verify button
        verifyPasswordButton = BaseWidgetUtils.createButton( verifyPasswordButtonsComposite, Messages
            .getString( "PasswordDialog.Verify" ), 1 ); //$NON-NLS-1$
        verifyPasswordButton.setEnabled( false );

        // Bind button
        bindPasswordButton = BaseWidgetUtils.createButton( verifyPasswordButtonsComposite, Messages
            .getString( "PasswordDialog.Bind" ), 1 ); //$NON-NLS-1$
        bindPasswordButton.setEnabled( false );

        // Current password tab
        currentPasswordTab = new TabItem( tabFolder, SWT.NONE );
        currentPasswordTab.setText( Messages.getString( "PasswordDialog.CurrentPassword" ) ); //$NON-NLS-1$
        currentPasswordTab.setControl( currentPasswordComposite );
    }


    // ── Vader's Terminal Builds the Provisioning Tab ──────────────────────────
    // The "New Password" tab provides entry/confirm fields, a hash algorithm
    // picker (populated from HASH_METHODS), a read-only preview of the encoded
    // result, and a "New Salt" button (enabled for salted algorithms).  OK is
    // disabled until the two text fields match.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates and populates the "New Password" tab — two text fields for the new
     * password and its confirmation, a hash algorithm combo, a preview of the
     * encoded result, and a "New Salt" button for salted algorithms.
     */
    private void createNewPasswordTab()
    {
        // New password composite
        newPasswordComposite = new Composite( tabFolder, SWT.NONE );
        GridLayout newLayout = new GridLayout( 2, false );
        newLayout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
        newLayout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
        newLayout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
        newLayout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
        newPasswordComposite.setLayout( newLayout );

        // New password text
        BaseWidgetUtils.createLabel( newPasswordComposite, Messages.getString( "PasswordDialog.EnterNewPassword" ), 1 ); //$NON-NLS-1$
        newPasswordText = BaseWidgetUtils.createText( newPasswordComposite, "", 1 ); //$NON-NLS-1$

        // Confirm new password text
        BaseWidgetUtils
            .createLabel( newPasswordComposite, Messages.getString( "PasswordDialog.ConfirmNewPassword" ), 1 ); //$NON-NLS-1$
        confirmNewPasswordText = BaseWidgetUtils.createText( newPasswordComposite, "", 1 ); //$NON-NLS-1$

        // New password hashing method combo
        BaseWidgetUtils.createLabel( newPasswordComposite, Messages.getString( "PasswordDialog.SelectHashMethod" ), 1 ); //$NON-NLS-1$
        newPasswordHashMethodComboViewer = new ComboViewer( newPasswordComposite );
        newPasswordHashMethodComboViewer.setContentProvider( new ArrayContentProvider() );
        newPasswordHashMethodComboViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                String hashMethod = getHashMethodName( element );

                if ( !"".equals( hashMethod ) )
                {
                    return hashMethod;
                }

                return super.getText( element );
            }
        } );
        newPasswordHashMethodComboViewer.setInput( HASH_METHODS );
        newPasswordHashMethodComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // New password preview text
        BaseWidgetUtils.createLabel( newPasswordComposite, Messages.getString( "PasswordDialog.PasswordPreview" ), 1 ); //$NON-NLS-1$
        newPasswordPreviewText = BaseWidgetUtils.createReadonlyText( newPasswordComposite, "", 1 ); //$NON-NLS-1$

        // New salt button
        newSaltButton = BaseWidgetUtils.createButton( newPasswordComposite, Messages
            .getString( "PasswordDialog.NewSalt" ), 1 ); //$NON-NLS-1$
        newSaltButton.setLayoutData( new GridData() );
        newSaltButton.setEnabled( false );

        // New password preview details composite
        Composite newPasswordPreviewDetailsComposite = BaseWidgetUtils.createColumnContainer( newPasswordComposite, 2,
            1 );

        // New password preview hex label
        BaseWidgetUtils.createLabel( newPasswordPreviewDetailsComposite,
            Messages.getString( "PasswordDialog.PasswordHex" ), 1 ); //$NON-NLS-1$
        newPasswordPreviewValueHexText = BaseWidgetUtils.createLabeledText( newPasswordPreviewDetailsComposite, ":", 1 ); //$NON-NLS-1$

        // New password preview salt hex label
        BaseWidgetUtils.createLabel( newPasswordPreviewDetailsComposite,
            Messages.getString( "PasswordDialog.SaltHex" ), 1 ); //$NON-NLS-1$
        newPasswordPreviewSaltHexText = BaseWidgetUtils.createLabeledText( newPasswordPreviewDetailsComposite, "", 1 ); //$NON-NLS-1$

        // Show new password details button
        showNewPasswordDetailsButton = BaseWidgetUtils.createCheckbox( newPasswordPreviewDetailsComposite, Messages
            .getString( "PasswordDialog.ShowNewPasswordDetails" ), 2 ); //$NON-NLS-1$

        // New password tab
        newPasswordTab = new TabItem( tabFolder, SWT.NONE );
        newPasswordTab.setText( Messages.getString( "PasswordDialog.NewPassword" ) ); //$NON-NLS-1$
        newPasswordTab.setControl( newPasswordComposite );
    }


    // ── Vader's Terminal Wires Up Its Sensors ────────────────────────────────
    // After both tabs are built, we attach listeners that trigger a UI refresh
    // whenever the user types, changes the hash method, or toggles a checkbox.
    // The "Verify" and "Bind" listeners are attached only in dual-tab mode.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches SWT listeners to all interactive widgets so the UI refreshes
     * whenever the user types, toggles a checkbox, or changes the hash combo.
     * "Verify" and "Bind" listeners are attached only when both tabs are shown
     * (i.e. there is a current password to inspect).
     */
    private void addListeners()
    {
        if ( displayMode == DisplayMode.CURRENT_AND_NEW_PASSWORD )
        {
            showCurrentPasswordDetailsButton.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent arg0 )
                {
                    updateCurrentPasswordGroup();
                }
            } );

            testPasswordText.addModifyListener( new ModifyListener()
            {
                public void modifyText( ModifyEvent e )
                {
                    updateCurrentPasswordGroup();
                }
            } );

            showTestPasswordDetailsButton.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent arg0 )
                {
                    updateCurrentPasswordGroup();
                }
            } );

            verifyPasswordButton.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent event )
                {
                    verifyCurrentPassword();
                }
            } );

            bindPasswordButton.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent event )
                {
                    bindCurrentPassword();
                }
            } );
        }

        newPasswordText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                updateNewPasswordGroup();
            }
        } );

        confirmNewPasswordText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                updateNewPasswordGroup();
            }
        } );

        newPasswordHashMethodComboViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                updateNewPasswordGroup();
            }
        } );

        newSaltButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                updateNewPasswordGroup();
            }
        } );

        showNewPasswordDetailsButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent arg0 )
            {
                updateNewPasswordGroup();
            }
        } );
    }


    // ── Vader's Terminal Refreshes the Inspection Tab ─────────────────────────
    // On the "Current Password" tab we show the stored hash, algorithm, and hex
    // bytes, masked behind bullet characters unless "Show details" is ticked.
    // The "Verify" and "Bind" buttons become clickable only when the test field
    // has text (and, for Bind, when a live connection exists).  While this tab
    // is active, OK is disabled — the user must switch to "New Password" to commit.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the "Current Password" tab widgets to reflect the current
     * password model and the state of the "Show details" and test-password fields.
     * Enables or disables the Verify/Bind buttons based on whether there is a
     * testable password and a live connection.
     * Disables OK while this tab is active.
     */
    private void updateCurrentPasswordGroup()
    {
        // set current password to the UI widgets
        if ( currentPassword != null )
        {
            currentPasswordHashMethodText.setText( getCurrentPasswordHashMethodName() );
            currentPasswordValueHexText.setText( Utils
                .getNonNullString( currentPassword.getHashedPasswordAsHexString() ) );
            currentPasswordSaltHexText.setText( Utils.getNonNullString( currentPassword.getSaltAsHexString() ) );
            currentPasswordText.setText( currentPassword.toString() );
        }

        // show password details?
        if ( showCurrentPasswordDetailsButton.getSelection() )
        {
            currentPasswordText.setEchoChar( '\0' );
            currentPasswordValueHexText.setEchoChar( '\0' );
            currentPasswordSaltHexText.setEchoChar( '\0' );
        }
        else
        {
            currentPasswordText.setEchoChar( '•' );
            currentPasswordValueHexText.setEchoChar( '•' );
            currentPasswordSaltHexText.setEchoChar( currentPasswordSaltHexText.getText().equals(
                Utils.getNonNullString( null ) ) ? '\0' : '•' );
        }

        // enable/disable test field and buttons
        testPasswordText.setEnabled( currentPassword != null && currentPassword.getHashedPassword() != null
            && currentPassword.toBytes().length > 0 );
        testBindDnText.setText( entry != null ? entry.getDn().getName() : Utils.getNonNullString( null ) );
        if ( showTestPasswordDetailsButton.getSelection() )
        {
            testPasswordText.setEchoChar( '\0' );
        }
        else
        {
            testPasswordText.setEchoChar( '•' );
        }
        verifyPasswordButton.setEnabled( testPasswordText.isEnabled() && !"".equals( testPasswordText.getText() ) ); //$NON-NLS-1$
        bindPasswordButton.setEnabled( testPasswordText.isEnabled() && !"".equals( testPasswordText.getText() ) //$NON-NLS-1$
            && entry != null && entry.getBrowserConnection().getConnection() != null );

        // default dialog button
        if ( verifyPasswordButton.isEnabled() )
        {
            getShell().setDefaultButton( verifyPasswordButton );
        }
        else
        {
            getShell().setDefaultButton( okButton );
        }

        okButton.setEnabled( false );
    }


    // ── Vader's Terminal Runs a Local Hash Comparison ─────────────────────────
    // The "Verify" button triggers a local verification: we hash the test
    // password with the same algorithm and salt stored in the directory and
    // compare byte-by-byte.  A success produces an info dialog; a failure
    // calls the exception handler so the error is reported consistently.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Verifies the test password against the stored hash locally (no network call).
     * Opens an information dialog on success or an error dialog on failure.
     */
    private void verifyCurrentPassword()
    {
        String testPassword = testPasswordText.getText();
        if ( currentPassword != null )
        {
            if ( currentPassword.verify( testPassword ) )
            {
                MessageDialog dialog = new MessageDialog(
                    getShell(),
                    Messages.getString( "PasswordDialog.PasswordVerification" ), getShell().getImage(), //$NON-NLS-1$
                    Messages.getString( "PasswordDialog.PasswordVerifiedSuccessfully" ), MessageDialog.INFORMATION, new String[] //$NON-NLS-1$
                        { IDialogConstants.OK_LABEL }, 0 );
                dialog.open();
            }
            else
            {
                IStatus status = new Status( IStatus.ERROR, ValueEditorsConstants.PLUGIN_ID, 1,
                    Messages.getString( "PasswordDialog.PasswordVerificationFailed" ), null ); //$NON-NLS-1$
                ConnectionUIPlugin.getDefault().getExceptionHandler().handleException( status );
            }
        }
    }


    // ── Vader's Terminal Attempts a Live LDAP Bind ────────────────────────────
    // The "Bind" button performs a real LDAP BIND operation using the entry's DN
    // and the test password.  We clone the connection (so we don't disturb the
    // live one), set the credentials, and run CheckBindRunnable.  A success
    // dialog confirms access; failures are surfaced via the normal status mechanism.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Attempts a live LDAP BIND using the entry's DN and the typed test password.
     * Clones the current connection to avoid disturbing the active session.
     * Shows an information dialog on success; errors are reported by the
     * connection UI exception handler.
     */
    private void bindCurrentPassword()
    {
        if ( !"".equals( testPasswordText.getText() ) && entry != null //$NON-NLS-1$
            && entry.getBrowserConnection().getConnection() != null )
        {
            Connection connection = ( Connection ) entry.getBrowserConnection().getConnection().clone();
            connection.getConnectionParameter().setName( null );
            connection.getConnectionParameter().setBindPrincipal( entry.getDn().getName() );
            connection.getConnectionParameter().setBindPassword( testPasswordText.getText() );
            connection.getConnectionParameter().setAuthMethod( AuthenticationMethod.SIMPLE );

            CheckBindRunnable runnable = new CheckBindRunnable( connection );
            IStatus status = RunnableContextRunner.execute( runnable, null, true );
            if ( status.isOK() )
            {
                MessageDialog.openInformation( Display.getDefault().getActiveShell(), Messages
                    .getString( "PasswordDialog.CheckAuthentication" ), //$NON-NLS-1$
                    Messages.getString( "PasswordDialog.AuthenticationSuccessful" ) ); //$NON-NLS-1$
            }
        }
    }


    // ── Vader's Terminal Refreshes the Provisioning Tab ───────────────────────
    // While the user types on the "New Password" tab we continuously re-hash the
    // input with the chosen algorithm and update the preview.  If the confirm
    // field matches we enable OK; if they diverge or either field is blank, OK
    // stays disabled and the preview is cleared.  The "New Salt" button is
    // enabled only for salted algorithms.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the "New Password" tab — re-hashes the new password with the
     * currently selected algorithm, updates the preview fields, and enables or
     * disables the OK button and "New Salt" button based on whether the two
     * password fields match.
     */
    private void updateNewPasswordGroup()
    {
        // set new password to the UI widgets
        newPassword = new Password( getSelectedNewPasswordHashMethod(), newPasswordText.getText() );
        if ( !"".equals( newPasswordText.getText() ) //$NON-NLS-1$
            && newPasswordText.getText().equals( confirmNewPasswordText.getText() ) )
        {
            newPasswordPreviewValueHexText
                .setText( Utils.getNonNullString( newPassword.getHashedPasswordAsHexString() ) );
            newPasswordPreviewSaltHexText.setText( Utils.getNonNullString( newPassword.getSaltAsHexString() ) );
            newPasswordPreviewText.setText( newPassword.toString() );
            newSaltButton.setEnabled( newPassword.getSalt() != null );
            okButton.setEnabled( true );
            getShell().setDefaultButton( okButton );
        }
        else
        {
            newPassword = null;
            newPasswordPreviewValueHexText.setText( Utils.getNonNullString( null ) );
            newPasswordPreviewSaltHexText.setText( Utils.getNonNullString( null ) );
            newPasswordPreviewText.setText( Utils.getNonNullString( null ) );
            newSaltButton.setEnabled( false );
            okButton.setEnabled( false );
        }

        // show password details?
        if ( showNewPasswordDetailsButton.getSelection() )
        {
            newPasswordText.setEchoChar( '\0' );
            confirmNewPasswordText.setEchoChar( '\0' );
            newPasswordPreviewText.setEchoChar( '\0' );
            newPasswordPreviewValueHexText.setEchoChar( '\0' );
            newPasswordPreviewSaltHexText.setEchoChar( '\0' );
        }
        else
        {
            newPasswordText.setEchoChar( '•' );
            confirmNewPasswordText.setEchoChar( '•' );
            newPasswordPreviewText.setEchoChar( newPasswordPreviewText.getText()
                .equals( Utils.getNonNullString( null ) ) ? '\0' : '•' );
            newPasswordPreviewValueHexText.setEchoChar( newPasswordPreviewValueHexText.getText().equals(
                Utils.getNonNullString( null ) ) ? '\0' : '•' );
            newPasswordPreviewSaltHexText.setEchoChar( newPasswordPreviewSaltHexText.getText().equals(
                Utils.getNonNullString( null ) ) ? '\0' : '•' );
        }
    }


    // ── Vader's Terminal Refreshes the Active Tab ─────────────────────────────
    // When the user switches tabs, we redirect focus and trigger the appropriate
    // group-update method so the newly visible tab reflects the current state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the active tab changes.
     * Triggers the appropriate group update ({@link #updateCurrentPasswordGroup()}
     * or {@link #updateNewPasswordGroup()}) and sets focus to the primary field
     * on the newly selected tab.
     */
    private void updateTabFolder()
    {
        if ( testPasswordText != null && newPasswordText != null )
        {
            if ( tabFolder.getSelectionIndex() == CURRENT_TAB )
            {
                updateCurrentPasswordGroup();
                testPasswordText.setFocus();
            }
            else if ( tabFolder.getSelectionIndex() == NEW_TAB )
            {
                updateNewPasswordGroup();
                newPasswordText.setFocus();
            }
        }
    }


    // ── Vader's Terminal Reads the Chosen Algorithm ───────────────────────────
    // The "New Password" tab has a combo where the user picks the hash algorithm.
    // We read the current selection and return the enum constant, or null for
    // the "no hash" option (plain-text storage).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link LdapSecurityConstants} hash algorithm currently selected
     * in the "New Password" tab combo, or {@code null} if "No Hash" is chosen
     * (meaning the password will be stored in plain text).
     *
     * @return  The selected hash algorithm, or {@code null} for no hashing.
     */
    private LdapSecurityConstants getSelectedNewPasswordHashMethod()
    {
        StructuredSelection selection = ( StructuredSelection ) newPasswordHashMethodComboViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            Object selectedObject = selection.getFirstElement();

            if ( selectedObject instanceof LdapSecurityConstants )
            {
                return ( LdapSecurityConstants ) selectedObject;
            }
        }

        return null;
    }


    // ── Vader's Terminal Resolves an Algorithm to Its Display Name ────────────
    // Given a hash method object (either an enum constant or the NO_HASH_METHOD
    // sentinel string), we return its human-readable name for the combo label
    // provider.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for a hash method object.
     * If the object is a {@link LdapSecurityConstants} constant we return
     * {@code hashMethod.getName()}; if it is the {@code NO_HASH_METHOD} sentinel
     * string we return the "no hash" browser-core message; otherwise we return
     * {@code null}.
     *
     * @param o  The hash method object (enum constant or sentinel string).
     * @return   The display name, or {@code null} if the type is unrecognised.
     */
    private String getHashMethodName( Object o )
    {
        if ( o instanceof LdapSecurityConstants )
        {
            LdapSecurityConstants hashMethod = ( LdapSecurityConstants ) o;

            return hashMethod.getName();
        }
        else if ( ( o instanceof String ) && NO_HASH_METHOD.equals( o ) )
        {
            return BrowserCoreMessages.model__no_hash;
        }

        return null;
    }


    // ── Vader's Terminal Reads the Current Hash Algorithm Name ────────────────
    // We read the hash algorithm recorded in the stored password model and look
    // up its display name.  If no algorithm is recorded (plain-text password),
    // we return the "no hash" label so the field is never left blank.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of the hash algorithm recorded in the current
     * stored password.  Returns the "no hash" label if the password is plain text.
     *
     * @return  The hash algorithm name string for display.
     */
    private String getCurrentPasswordHashMethodName()
    {
        LdapSecurityConstants hashMethod = currentPassword.getHashMethod();

        if ( hashMethod != null )
        {
            return Utils.getNonNullString( getHashMethodName( hashMethod ) );
        }
        else
        {
            return Utils.getNonNullString( getHashMethodName( NO_HASH_METHOD ) );
        }
    }


    // ── Vader's Terminal Hands Over the New Credential ───────────────────────
    // After OK is pressed, callers retrieve the new password bytes here.
    // Returns null if the dialog was cancelled or the user set no new password.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new password as raw bytes (hashed or plain text depending on
     * the selected algorithm) after the dialog has been closed with OK.
     * Returns {@code null} if the dialog was cancelled or no new password was set.
     *
     * <p>For example — the caller retrieves the encoded credential:</p>
     * <pre>
     *   if (dialog.open() == Dialog.OK) {
     *       byte[] pw = dialog.getNewPassword();
     *       // write pw to the userPassword attribute
     *   }
     * </pre>
     *
     * @return  The new encoded password bytes, or {@code null}.
     */
    public byte[] getNewPassword()
    {
        return returnPassword;
    }

    // ── ENUM: DisplayMode — VADER'S TERMINAL OPERATING MODE ──────────────────
    // The terminal operates in one of two modes: showing both the current and new
    // password tabs when a credential already exists, or showing only the new
    // password tab when no credential is stored yet.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Indicates whether the dialog shows only the "New Password" tab or both
     * the "Current Password" and "New Password" tabs.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private enum DisplayMode
    {
        CURRENT_AND_NEW_PASSWORD,
        NEW_PASSWORD_ONLY
    }
}
