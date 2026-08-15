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

package org.apache.directory.studio.connection.ui.dialogs;


import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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


// ── CLASS: ResetPasswordDialog — CHANGING THE FALCON'S HYPERDRIVE ACCESS CODE ────
// When the crew decides the old hyperdrive access code has been compromised, someone
// has to prove they know the old code before they are allowed to set a new one —
// and the new code must be typed twice to confirm it didn't get fat-fingered.
// ResetPasswordDialog enforces that three-field flow: current password, new password,
// verify new password.  The OK button stays disabled until all three are non-empty,
// the two new-password fields match, and the current password differs from the new one.
// Used by PasswordsKeystorePreferencePage to let the user change the keystore master
// password without leaving it in an unknown state.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Three-field password-reset dialog: current password, new password, verify new.
 *
 * <p>The OK button is enabled only when:</p>
 * <ul>
 *   <li>The current-password field is non-empty.</li>
 *   <li>The new-password field is non-empty.</li>
 *   <li>The verify-new-password field matches the new-password field.</li>
 *   <li>The current password differs from the new password.</li>
 * </ul>
 *
 * <p>Each password field has a "Show password" checkbox to toggle masking.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ResetPasswordDialog extends Dialog
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The window title. */
    private String title;

    /** Optional explanatory message shown above the groups; may be {@code null}. */
    private String message;

    /**
     * The current password, initialised from {@code initialValue}.
     * Set to {@code null} on Cancel.
     */
    private String currentPassword = StringUtils.EMPTY;

    /**
     * The new password typed by the user.
     * Set to {@code null} on Cancel.
     */
    private String newPassword = StringUtils.EMPTY;

    // ── UI WIDGETS ────────────────────────────────────────────────────────────────

    /** The OK button — kept as a field so validate() can enable/disable it. */
    private Button okButton;

    /** Text field for the current (existing) password. */
    private Text currentPasswordText;

    /** Checkbox to toggle masking of the current-password field. */
    private Button showCurrentPasswordCheckbox;

    /** Text field for the new password. */
    private Text newPasswordText;

    /** Checkbox to toggle masking of the new-password field. */
    private Button showNewPasswordCheckbox;

    /** Text field for confirming the new password. */
    private Text verifyNewPasswordText;

    /** Checkbox to toggle masking of the verify-new-password field. */
    private Button showVerifyNewPasswordCheckbox;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ResetPasswordDialog}.
     *
     * @param parentShell  The parent SWT shell.
     * @param title        Title shown in the dialog's title bar.
     * @param message      Explanatory text above the groups; may be {@code null}.
     * @param initialValue Pre-filled current password; pass {@code null} for empty.
     */
    public ResetPasswordDialog( Shell parentShell, String title, String message, String initialValue )
    {
        super( parentShell );
        this.title = title;
        this.message = message;

        if ( initialValue == null )
        {
            currentPassword = StringUtils.EMPTY;
        }
        else
        {
            currentPassword = initialValue;
        }
    }


    // ── CONFIGURE SHELL ───────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Sets the shell title.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( CommonUIUtils.getTextValue( title ) );
    }


    // ── CREATE BUTTONS FOR BUTTON BAR ─────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Creates OK and Cancel buttons.  We keep a reference to the OK button so
     * {@link #validate()} can enable/disable it, and immediately run validation
     * so the initial state is consistent with the empty fields.
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        // Initial validation — fields are empty, so OK should start disabled.
        validate();
    }


    // ── BUTTON PRESSED ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * On OK, captures the current and new passwords from the text fields.
     * On Cancel, nullifies both fields to signal cancellation.
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            currentPassword = currentPasswordText.getText();
            newPassword = newPasswordText.getText();
        }
        else
        {
            currentPassword = null;
            newPassword = null;
        }

        super.buttonPressed( buttonId );
    }


    // ── CREATE DIALOG AREA ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the dialog with two group boxes:
     * <ol>
     *   <li>"Current Password" — one masked text field + "Show password" checkbox.</li>
     *   <li>"New Password" — two masked text fields (new + verify) each with their
     *       own "Show password" checkbox.</li>
     * </ol>
     * All three text fields call {@link #validate()} via a ModifyListener on every
     * keystroke.
     */
    @Override
    protected Control createDialogArea( Composite parent )
    {
        // ── OUTER COMPOSITE ───────────────────────────────────────────────────────
        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
        layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
        layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
        layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
        composite.setLayout( layout );
        GridData compositeGridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        compositeGridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        composite.setLayoutData( compositeGridData );

        // ── OPTIONAL MESSAGE ──────────────────────────────────────────────────────
        if ( message != null )
        {
            Label messageLabel = BaseWidgetUtils.createWrappedLabel( composite, message, 1 );
            GridData messageLabelGridData = new GridData( SWT.FILL, SWT.CENTER, true, true, 2, 1 );
            messageLabelGridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
            messageLabel.setLayoutData( messageLabelGridData );
        }

        // ── CURRENT PASSWORD GROUP ────────────────────────────────────────────────
        // The user must prove they know the existing password before we allow a change.
        // ──────────────────────────────────────────────────────────────────────────
        Group currentPasswordGroup = BaseWidgetUtils.createGroup( composite,
            Messages.getString( "ResetPasswordDialog.CurrentPassword" ), 1 ); //$NON-NLS-1$
        currentPasswordGroup.setLayout( new GridLayout( 2, false ) );

        BaseWidgetUtils.createLabel( currentPasswordGroup,
            Messages.getString( "ResetPasswordDialog.CurrentPasswordColon" ), 1 ); //$NON-NLS-1$
        currentPasswordText = BaseWidgetUtils.createText( currentPasswordGroup, StringUtils.EMPTY, 1 );
        currentPasswordText.setEchoChar( '•' );
        currentPasswordText.addModifyListener( event -> validate() );

        // "Show password" toggles masking on currentPasswordText
        BaseWidgetUtils.createLabel( currentPasswordGroup, StringUtils.EMPTY, 1 );
        showCurrentPasswordCheckbox = BaseWidgetUtils.createCheckbox( currentPasswordGroup,
            Messages.getString( "ResetPasswordDialog.ShowPassword" ), 1 ); //$NON-NLS-1$
        showCurrentPasswordCheckbox.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                if ( showCurrentPasswordCheckbox.getSelection() )
                {
                    currentPasswordText.setEchoChar( '\0' );
                }
                else
                {
                    currentPasswordText.setEchoChar( '•' );
                }
            }
        } );

        // ── NEW PASSWORD GROUP ────────────────────────────────────────────────────
        // Two fields: the new password and a confirmation copy.
        // ──────────────────────────────────────────────────────────────────────────
        Group newPasswordGroup = BaseWidgetUtils.createGroup( composite,
            Messages.getString( "ResetPasswordDialog.NewPassword" ), 1 ); //$NON-NLS-1$
        newPasswordGroup.setLayout( new GridLayout( 2, false ) );

        BaseWidgetUtils.createLabel( newPasswordGroup, Messages.getString( "ResetPasswordDialog.NewPasswordColon" ), 1 ); //$NON-NLS-1$
        newPasswordText = BaseWidgetUtils.createText( newPasswordGroup, StringUtils.EMPTY, 1 );
        newPasswordText.setEchoChar( '•' );
        newPasswordText.addModifyListener( event -> validate() );

        BaseWidgetUtils.createLabel( newPasswordGroup, StringUtils.EMPTY, 1 );
        showNewPasswordCheckbox = BaseWidgetUtils.createCheckbox( newPasswordGroup,
            Messages.getString( "ResetPasswordDialog.ShowPassword" ), 1 ); //$NON-NLS-1$
        showNewPasswordCheckbox.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                if ( showNewPasswordCheckbox.getSelection() )
                {
                    newPasswordText.setEchoChar( '\0' );
                }
                else
                {
                    newPasswordText.setEchoChar( '•' );
                }
            }
        } );

        // Verify field
        BaseWidgetUtils.createLabel( newPasswordGroup,
            Messages.getString( "ResetPasswordDialog.VerifyNewPasswordColon" ), 1 ); //$NON-NLS-1$
        verifyNewPasswordText = BaseWidgetUtils.createText( newPasswordGroup, StringUtils.EMPTY, 1 );
        verifyNewPasswordText.setEchoChar( '•' );
        verifyNewPasswordText.addModifyListener( event -> validate() );

        BaseWidgetUtils.createLabel( newPasswordGroup, StringUtils.EMPTY, 1 );
        showVerifyNewPasswordCheckbox = BaseWidgetUtils.createCheckbox( newPasswordGroup,
            Messages.getString( "ResetPasswordDialog.ShowPassword" ), 1 ); //$NON-NLS-1$
        showVerifyNewPasswordCheckbox.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                if ( showVerifyNewPasswordCheckbox.getSelection() )
                {
                    verifyNewPasswordText.setEchoChar( '\0' );
                }
                else
                {
                    verifyNewPasswordText.setEchoChar( '•' );
                }
            }
        } );

        currentPasswordGroup.setFocus();

        applyDialogFont( composite );
        return composite;
    }


    // ── GET CURRENT PASSWORD ──────────────────────────────────────────────────────
    /**
     * Returns the current password typed by the user.
     *
     * <p>Returns {@code null} if the dialog was cancelled.</p>
     *
     * @return The current password string, or {@code null} if cancelled.
     */
    public String getCurrentPassword()
    {
        return currentPassword;
    }


    // ── GET NEW PASSWORD ──────────────────────────────────────────────────────────
    /**
     * Returns the new password typed by the user.
     *
     * <p>Returns {@code null} if the dialog was cancelled.</p>
     *
     * @return The new password string, or {@code null} if cancelled.
     */
    public String getNewPassword()
    {
        return newPassword;
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────
    /**
     * Enables the OK button only when all validation conditions are met:
     * current password is non-empty, new password is non-empty, the two
     * new-password fields match, and the new password differs from the current one.
     *
     * <p>Called on every ModifyEvent from all three text fields.</p>
     */
    private void validate()
    {
        // ── FOUR CONDITIONS FOR OK ─────────────────────────────────────────────────
        // 1. Current password is not empty.
        // 2. New password is not empty.
        // 3. Verify field matches new password.
        // 4. New password differs from current password (can't "change" to the same).
        // ──────────────────────────────────────────────────────────────────────────
        String currentPwd = currentPasswordText.getText();
        String newPwd = newPasswordText.getText();
        String verifyNewPwd = verifyNewPasswordText.getText();

        okButton.setEnabled( !Strings.isEmpty( currentPwd ) && !Strings.isEmpty( newPwd )
            && newPwd.equals( verifyNewPwd ) && !currentPwd.equals( newPwd ) );
    }
}
