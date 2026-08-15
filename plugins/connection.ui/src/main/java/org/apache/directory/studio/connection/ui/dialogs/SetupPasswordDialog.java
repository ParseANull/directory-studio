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


// ── CLASS: SetupPasswordDialog — SEALING THE VAULT FOR THE FIRST TIME ────────────
// When the Rebel Alliance first encrypts their data vault, they need to choose a
// master key — and to make sure they didn't mistype it, they have to enter it twice.
// SetupPasswordDialog is that first-time setup moment: two masked text fields
// (password + verify) inside a single "Password" group.  The OK button stays
// disabled until both fields are non-empty and identical.
// Used by PasswordsKeystorePreferencePage when the user first enables the keystore.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Two-field dialog for setting up a new password (no "current password" field).
 *
 * <p>Useful for first-time setup flows where we don't need to verify an existing
 * credential — just confirm the new one.  The OK button is enabled only when
 * both the password and verify fields are non-empty and identical.</p>
 *
 * <p>Each text field has a "Show password" checkbox to toggle masking.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SetupPasswordDialog extends Dialog
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The window title. */
    private String title;

    /** Optional message above the group; may be {@code null}. */
    private String message;

    /**
     * The password value.  Pre-filled from {@code initialValue}, then updated
     * from the text field on OK.  Set to {@code null} on Cancel.
     */
    private String value = StringUtils.EMPTY;

    // ── UI WIDGETS ────────────────────────────────────────────────────────────────

    /** OK button — kept as a field so {@link #validate()} can enable/disable it. */
    private Button okButton;

    /** The primary (new) password text field. */
    private Text passwordText;

    /** Checkbox to toggle masking on {@link #passwordText}. */
    private Button showPasswordCheckbox;

    /** Confirmation copy of the new password. */
    private Text verifyPasswordText;

    /** Checkbox to toggle masking on {@link #verifyPasswordText}. */
    private Button showVerifyPasswordCheckbox;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link SetupPasswordDialog}.
     *
     * @param parentShell  The parent SWT shell.
     * @param title        Title shown in the dialog's title bar.
     * @param message      Explanatory message above the password group; may be
     *                     {@code null} to omit.
     * @param initialValue Pre-filled password value; pass {@code null} for empty.
     */
    public SetupPasswordDialog( Shell parentShell, String title, String message, String initialValue )
    {
        super( parentShell );
        this.title = title;
        this.message = message;

        if ( initialValue == null )
        {
            value = StringUtils.EMPTY;
        }
        else
        {
            value = initialValue;
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
     * Creates OK and Cancel buttons, and immediately validates so the initial
     * disabled state is correct (both fields are empty at creation).
     */
    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );

        validate();
    }


    // ── BUTTON PRESSED ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * On OK, captures the password text.  On Cancel, sets value to {@code null}.
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == IDialogConstants.OK_ID )
        {
            value = passwordText.getText();
        }
        else
        {
            value = null;
        }

        super.buttonPressed( buttonId );
    }


    // ── CREATE DIALOG AREA ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds a single "Password" group with two masked text fields (password and
     * verify) each followed by a "Show password" checkbox.
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
            GridData messageLabelGridData = new GridData( SWT.FILL, SWT.CENTER, true, true );
            messageLabelGridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
            messageLabel.setLayoutData( messageLabelGridData );
        }

        // ── PASSWORD GROUP ────────────────────────────────────────────────────────
        // A two-column group: label | field, with "Show password" checkboxes.
        // ──────────────────────────────────────────────────────────────────────────
        Group passwordGroup = BaseWidgetUtils.createGroup( composite,
            Messages.getString( "SetupPasswordDialog.Password" ), 1 ); //$NON-NLS-1$
        passwordGroup.setLayout( new GridLayout( 2, false ) );

        // Password field
        BaseWidgetUtils.createLabel( passwordGroup, Messages.getString( "SetupPasswordDialog.PasswordColon" ), 1 ); //$NON-NLS-1$
        passwordText = BaseWidgetUtils.createText( passwordGroup, value, 1 );
        passwordText.setEchoChar( '•' );
        passwordText.addModifyListener( event -> validate() );

        // Show-password checkbox for the password field
        BaseWidgetUtils.createLabel( passwordGroup, StringUtils.EMPTY, 1 );
        showPasswordCheckbox = BaseWidgetUtils.createCheckbox( passwordGroup,
            Messages.getString( "SetupPasswordDialog.ShowPassword" ), 1 ); //$NON-NLS-1$
        showPasswordCheckbox.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                if ( showPasswordCheckbox.getSelection() )
                {
                    passwordText.setEchoChar( '\0' );
                }
                else
                {
                    passwordText.setEchoChar( '•' );
                }
            }
        } );

        // Verify field
        BaseWidgetUtils.createLabel( passwordGroup, Messages.getString( "SetupPasswordDialog.VerifyPasswordColon" ), 1 ); //$NON-NLS-1$
        verifyPasswordText = BaseWidgetUtils.createText( passwordGroup, value, 1 );
        verifyPasswordText.setEchoChar( '•' );
        verifyPasswordText.addModifyListener( event -> validate() );

        // Show-password checkbox for the verify field
        BaseWidgetUtils.createLabel( passwordGroup, StringUtils.EMPTY, 1 );
        showVerifyPasswordCheckbox = BaseWidgetUtils.createCheckbox( passwordGroup,
            Messages.getString( "SetupPasswordDialog.ShowPassword" ), 1 ); //$NON-NLS-1$
        showVerifyPasswordCheckbox.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                if ( showVerifyPasswordCheckbox.getSelection() )
                {
                    verifyPasswordText.setEchoChar( '\0' );
                }
                else
                {
                    verifyPasswordText.setEchoChar( '•' );
                }
            }
        } );

        passwordText.setFocus();

        applyDialogFont( composite );
        return composite;
    }


    // ── GET PASSWORD ──────────────────────────────────────────────────────────────
    /**
     * Returns the password typed by the user.
     *
     * <p>Returns {@code null} if the dialog was cancelled.</p>
     *
     * @return The typed password string, or {@code null} if cancelled.
     */
    public String getPassword()
    {
        return value;
    }


    // ── VALIDATE ──────────────────────────────────────────────────────────────────
    /**
     * Enables the OK button only when both fields are non-empty and identical.
     *
     * <p>Called on every ModifyEvent from both text fields.</p>
     */
    private void validate()
    {
        // ── OK ENABLED WHEN: non-empty AND matching ────────────────────────────────
        String password = passwordText.getText();
        String verifyPassword = verifyPasswordText.getText();

        okButton.setEnabled( !Strings.isEmpty( password ) && password.equals( verifyPassword ) );
    }
}
