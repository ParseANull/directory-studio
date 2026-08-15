/*
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
package org.apache.directory.studio.openldap.common.ui.dialogs;


import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.Password;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.valueeditors.ValueEditorsActivator;
import org.apache.directory.studio.valueeditors.ValueEditorsConstants;
import org.apache.directory.studio.valueeditors.password.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: PasswordDialog — REBEL ALLIANCE VAULT FOR ENCRYPTED CODES ─────────
// Picture the scene where Mon Mothma opens the encrypted Rebel vault: she can
// see the current code stored inside, choose a new encryption algorithm for
// the replacement code, and preview the hashed result before committing. The
// vault shows bullets instead of clear text by default — only a "Show Details"
// checkbox reveals the underlying bytes. OK is disabled until a valid new
// password has been entered and previewed successfully.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We present the password editor dialog, which lets users view the current
 * password (if one exists) and enter a new one. The new password can be
 * encoded with any of the supported hash algorithms; a live preview shows the
 * hashed bytes and salt before the user confirms. We return the final encoded
 * password bytes via {@link #getNewPassword()} after the dialog closes with OK.
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
            LdapSecurityConstants.HASH_METHOD_CRYPT,
            NO_HASH_METHOD };

    /** The current password */
    private Password currentPassword;

    /** The new password */
    private Password newPassword;

    /** The return password*/
    private byte[] returnPassword;

    // UI Widgets
    private Button okButton;
    private Group currentPasswordGroup;
    private Text currentPasswordText;
    private Text currentPasswordHashMethodText;
    private Text currentPasswordValueHexText;
    private Text currentPasswordSaltHexText;
    private Button showCurrentPasswordDetailsButton;
    private Group newPasswordGroup;
    private Text newPasswordText;
    private ComboViewer newPasswordHashMethodComboViewer;
    private Text newPasswordPreviewText;
    private Button newSaltButton;
    private Text newPasswordPreviewValueHexText;
    private Text newPasswordPreviewSaltHexText;
    private Button showNewPasswordDetailsButton;


    // ── CONSTRUCTOR: PasswordDialog — OPENING THE VAULT DOOR ─────────────────
    // We receive the bytes of the current password (if any) and wrap them in a
    // Password object so we can decode the hash method and display it in the
    // "current password" section. A null argument means the vault is empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a PasswordDialog pre-loaded with the current password bytes.
     * If {@code currentPassword} is non-null we wrap it in a {@link Password}
     * object so the hash method and hex values can be displayed. Pass null if
     * there is no existing password.
     *
     * @param parentShell      the parent shell
     * @param currentPassword  the raw bytes of the existing password, or null if none
     */
    public PasswordDialog( Shell parentShell, byte[] currentPassword )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );

        if ( currentPassword != null )
        {
            this.currentPassword = new Password( currentPassword );
        }
    }


    // ── METHOD: configureShell — LABELLING THE VAULT ENTRANCE ────────────────
    // We inscribe the vault title and stamp it with the password-editor icon
    // so the operator knows at a glance which secure facility they have opened.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "PasswordDialog.PasswordEditor" ) ); //$NON-NLS-1$
        shell.setImage( ValueEditorsActivator.getDefault().getImage( ValueEditorsConstants.IMG_PASSWORDEDITOR ) );
    }


    // ── METHOD: okPressed — SEALING THE NEW CODE IN THE VAULT ────────────────
    // The operator has approved the new password: we convert the in-memory
    // Password object to its final byte representation and store it for the
    // caller to retrieve. If no new password was entered we store null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
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

        super.okPressed();
    }


    // ── METHOD: createButtonsForButtonBar — PLACING THE VAULT CONTROLS ────────
    // We wire up OK and Cancel, then immediately refresh both password groups
    // so the button states and field contents are consistent from the moment
    // the vault door swings open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        if ( hasCurrentPassword() )
        {
            updateCurrentPasswordGroup();
        }

        updateNewPasswordGroup();
    }


    // ── METHOD: createDialogArea — BUILDING THE VAULT INTERIOR ───────────────
    // We construct the two main sections: an optional read-only view of the
    // current password (shown only when one exists) and the new-password editor
    // with hash-method selector, preview fields, and show/hide controls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( gd );

        if ( hasCurrentPassword() )
        {
            createCurrentPasswordGroup( composite );
        }

        createNewPasswordGroup( composite );

        addListeners();

        applyDialogFont( composite );

        return composite;
    }


    // ── METHOD: createCurrentPasswordGroup — DISPLAYING THE EXISTING CODE ─────
    // We build the read-only section that reveals what is already in the vault:
    // the masked password string, its hash algorithm, and the hex-encoded bytes
    // and salt — all hidden behind bullet characters unless "Show Details" is
    // ticked.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build the "Current Password" group showing the existing password in
     * masked form along with its hash method, hex value, and salt hex value.
     * A "Show Current Password Details" checkbox controls visibility.
     *
     * @param parent  the parent composite to attach the group to
     */
    private void createCurrentPasswordGroup( Composite parent )
    {
        currentPasswordGroup = BaseWidgetUtils.createGroup( parent, "Current Password", 1 );
        currentPasswordGroup.setLayout( new GridLayout( 2, false ) );
        currentPasswordGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Current password text
        BaseWidgetUtils.createLabel( currentPasswordGroup, Messages
            .getString( "PasswordDialog.CurrentPassword" ) + ":", 1 ); //$NON-NLS-1$//$NON-NLS-2$
        currentPasswordText = BaseWidgetUtils.createReadonlyText( currentPasswordGroup, "", 1 ); //$NON-NLS-1$

        // Current password details composite
        new Label( currentPasswordGroup, SWT.NONE );
        Composite currentPasswordDetailsComposite = BaseWidgetUtils.createColumnContainer( currentPasswordGroup,
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
    }


    // ── METHOD: createNewPasswordGroup — SETTING UP THE NEW CODE ENTRY ────────
    // This is the active side of the vault: the operator types a new password,
    // picks a hashing algorithm from the dropdown, and sees the preview update
    // in real time. A "New Salt" button re-rolls the random salt for algorithms
    // that use one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We build the "New Password" group where the user types a new password,
     * selects a hash algorithm, and sees a live preview of the encoded result
     * including hex values and salt.
     *
     * @param parent  the parent composite to attach the group to
     */
    private void createNewPasswordGroup( Composite parent )
    {
        newPasswordGroup = BaseWidgetUtils.createGroup( parent, "New Password", 1 );
        newPasswordGroup.setLayout( new GridLayout( 2, false ) );
        newPasswordGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // New password text
        BaseWidgetUtils.createLabel( newPasswordGroup, Messages.getString( "PasswordDialog.EnterNewPassword" ), 1 ); //$NON-NLS-1$
        newPasswordText = BaseWidgetUtils.createText( newPasswordGroup, "", 1 ); //$NON-NLS-1$

        // New password hashing method combo
        BaseWidgetUtils.createLabel( newPasswordGroup, Messages.getString( "PasswordDialog.SelectHashMethod" ), 1 ); //$NON-NLS-1$
        newPasswordHashMethodComboViewer = new ComboViewer( newPasswordGroup );
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
        newPasswordHashMethodComboViewer.setSelection( new StructuredSelection( NO_HASH_METHOD ) );
        newPasswordHashMethodComboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        // New password preview text
        BaseWidgetUtils.createLabel( newPasswordGroup, Messages.getString( "PasswordDialog.PasswordPreview" ), 1 ); //$NON-NLS-1$
        newPasswordPreviewText = BaseWidgetUtils.createReadonlyText( newPasswordGroup, "", 1 ); //$NON-NLS-1$

        // New salt button
        newSaltButton = BaseWidgetUtils.createButton( newPasswordGroup, Messages
            .getString( "PasswordDialog.NewSalt" ), 1 ); //$NON-NLS-1$
        newSaltButton.setLayoutData( new GridData() );
        newSaltButton.setEnabled( false );

        // New password preview details composite
        Composite newPasswordPreviewDetailsComposite = BaseWidgetUtils.createColumnContainer( newPasswordGroup, 2,
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
    }


    // ── METHOD: updateCurrentPasswordGroup — REFRESHING THE EXISTING CODE DISPLAY
    // Whenever visibility is toggled we update echo characters to either show
    // or mask the stored bytes. We also push the hash method, hex value, and
    // salt into their respective fields so the display is always in sync with
    // the in-memory Password object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We refresh the current-password section to reflect the latest state of
     * the {@code currentPassword} object and the "Show Details" checkbox. We
     * toggle echo characters between bullet (•) and null (visible) based
     * on the checkbox state.
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
    }


    // ── METHOD: updateNewPasswordGroup — PREVIEWING THE NEW ENCRYPTED CODE ────
    // As the operator types and adjusts the algorithm, we rebuild the Password
    // object on the fly and push the preview bytes into the display fields.
    // We enable OK only when a non-empty new password is confirmed; otherwise
    // we null out newPassword and disable OK so an incomplete entry cannot slip
    // through.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We rebuild the new-password preview every time the input text or hash
     * algorithm changes. We enable the OK button and New Salt button only when
     * a valid non-empty password is entered; we clear the preview and disable
     * OK when the field is empty.
     */
    private void updateNewPasswordGroup()
    {
        // set new password to the UI widgets
        newPassword = new Password( getSelectedNewPasswordHashMethod(), newPasswordText.getText() );
        if ( !"".equals( newPasswordText.getText() ) || newPassword.getHashMethod() == null ) //$NON-NLS-1$
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
            newPasswordPreviewText.setEchoChar( '\0' );
            newPasswordPreviewValueHexText.setEchoChar( '\0' );
            newPasswordPreviewSaltHexText.setEchoChar( '\0' );
        }
        else
        {
            newPasswordText.setEchoChar( '•' );
            newPasswordPreviewText.setEchoChar( newPasswordPreviewText.getText()
                .equals( Utils.getNonNullString( null ) ) ? '\0' : '•' );
            newPasswordPreviewValueHexText.setEchoChar( newPasswordPreviewValueHexText.getText().equals(
                Utils.getNonNullString( null ) ) ? '\0' : '•' );
            newPasswordPreviewSaltHexText.setEchoChar( newPasswordPreviewSaltHexText.getText().equals(
                Utils.getNonNullString( null ) ) ? '\0' : '•' );
        }
    }


    // ── METHOD: addListeners — WIRING THE VAULT CONTROLS ─────────────────────
    // Each interactive control gets a listener so changes propagate immediately:
    // the show-details checkbox toggles visibility, the text field and algorithm
    // combo refresh the preview, and the New Salt button regenerates randomness.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We attach change listeners to every interactive widget in the dialog.
     * Listeners call the appropriate update method so the preview and button
     * states stay in sync with the user's input at all times.
     */
    private void addListeners()
    {
        if ( hasCurrentPassword() )
        {
            showCurrentPasswordDetailsButton.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent arg0 )
                {
                    updateCurrentPasswordGroup();
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


    // ── METHOD: hasCurrentPassword — CHECKING IF THE VAULT ALREADY HAS A CODE ─
    // Before we show the "Current Password" section we verify that an actual
    // password was supplied and that it contains at least one byte. An empty
    // byte array is treated the same as null — the vault is effectively empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if a non-null, non-empty current password was
     * supplied at construction time. This controls whether the current-password
     * group is displayed at all.
     *
     * @return {@code true} if the dialog has a current password, {@code false} if not
     */
    private boolean hasCurrentPassword()
    {
        return ( ( currentPassword != null ) && ( currentPassword.toBytes().length > 0 ) );
    }


    // ── METHOD: getSelectedNewPasswordHashMethod — READING THE ALGORITHM DIAL ─
    // We look at which entry the operator selected in the algorithm combo and
    // return the corresponding LdapSecurityConstants value. If the selection is
    // the "no hash" sentinel we return null to indicate plain-text storage.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We read the currently selected hash algorithm from the combo viewer and
     * return it as an {@link LdapSecurityConstants} value. We return {@code null}
     * if the "no hash" option is selected or if the selection is empty.
     *
     * @return the selected hash method, or {@code null} for plain text
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


    // ── METHOD: getHashMethodName — TRANSLATING THE ALGORITHM CODE ────────────
    // The combo entries are either LdapSecurityConstants enum values or our
    // NO_HASH_METHOD sentinel string. We translate each into a human-readable
    // label so the combo shows friendly names instead of raw enum identifiers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We translate a hash method object into its display name. If {@code o} is
     * an {@link LdapSecurityConstants} we call {@code getName()}; if it is the
     * {@code NO_HASH_METHOD} sentinel string we return the localized "no hash"
     * label. We return {@code null} for any unrecognized object.
     *
     * @param o  the hash method object from the combo input array
     * @return   the display name for the hash method, or {@code null}
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


    // ── METHOD: getCurrentPasswordHashMethodName — IDENTIFYING THE VAULT LOCK ─
    // We inspect the existing Password object to find out which algorithm was
    // used to encode it. If no algorithm is present (plain text) we fall back
    // to the "no hash" label so the display is never blank.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We retrieve the display name of the hash algorithm that was used to store
     * the current password. If the current password has no hash method (plain
     * text) we return the "no hash" label via {@link #getHashMethodName(Object)}.
     *
     * @return the current password's hash method display name, never null
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


    // ── METHOD: getNewPassword — RETRIEVING THE SEALED CODE ──────────────────
    // After OK is pressed the caller fetches the result here: the encoded bytes
    // of the new password ready for storage. If OK was not pressed or the
    // field was left empty, this returns null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the byte array of the new password after the dialog is confirmed.
     * The bytes are either encrypted with the selected hash algorithm or stored
     * as plain text if no algorithm was chosen. We return {@code null} if the
     * dialog was cancelled or no new password was entered.
     *
     * @return  the password bytes (possibly hashed), or {@code null}
     */
    public byte[] getNewPassword()
    {
        return returnPassword;
    }
}
