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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


// ── CLASS: PasswordDialog — HAN AT THE IMPERIAL SECURITY CHECKPOINT ───────────────
// Han Solo needs to enter the correct code to pass the Imperial checkpoint, but he
// doesn't want the stormtroopers to see what he's typing.  He also has an option to
// reveal what he typed — just to double-check he didn't fat-finger it.
// PasswordDialog is that checkpoint: a single masked text field with an optional
// "Show password" checkbox that toggles between bullet characters (U+2022) and
// plain text.  Used by UIAuthHandler and PasswordsKeyStoreManagerUtils when they
// need to prompt the user for a bind password or keystore master password.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Single-field password prompt dialog.
 *
 * <p>Shows a title, an optional explanatory message, a password text field
 * (masked with the bullet character U+2022 by default), and a "Show password"
 * checkbox that toggles between masked and plain-text display.</p>
 *
 * <p>On OK the typed text is stored and retrievable via {@link #getPassword()}.
 * On Cancel (or Escape) {@link #getPassword()} returns {@code null}.</p>
 *
 * <p>Layout sketch:</p>
 * <pre>
 * .--------------------------------------------------.
 * |            Enter password for "xxxxx"            |
 * +--------------------------------------------------+
 * | Please enter password of user "yyyyyyyyyyyyy"    |
 * | [----------------------------------------------] |
 * | [ ] Show password                                |
 * |                                                  |
 * |                                (Cancel) (  OK  ) |
 * .__________________________________________________. </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordDialog extends Dialog
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The title shown in the dialog's title bar. */
    private String title;

    /** Optional message shown above the password field; may be {@code null}. */
    private String message;

    /**
     * The value typed by the user.  Starts as empty string (or the initialValue
     * passed to the constructor).  Set to {@code null} on Cancel.
     */
    private String value = StringUtils.EMPTY;

    // ── UI WIDGETS ────────────────────────────────────────────────────────────────

    /** The masked password text widget. */
    private Text passwordText;

    /** The checkbox that toggles between masked and plain-text display. */
    private Button showPasswordCheckbox;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link PasswordDialog}.
     *
     * @param parentShell  The parent SWT shell.
     * @param title        The title shown in the dialog's title bar.
     * @param message      Explanatory text shown above the password field;
     *                     may be {@code null} to omit the label.
     * @param initialValue The text pre-filled into the password field (usually
     *                     empty); pass {@code null} to start with an empty field.
     */
    public PasswordDialog( Shell parentShell, String title, String message, String initialValue )
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
     * Sets the shell title, treating {@code null} as empty via
     * {@link CommonUIUtils#getTextValue}.
     */
    @Override
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( CommonUIUtils.getTextValue( title ) );
    }


    // ── BUTTON PRESSED ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * On OK, captures the current text from the password field.
     * On Cancel (or any other button), sets the value to {@code null} so
     * callers can distinguish "user entered nothing" from "user cancelled".
     */
    @Override
    protected void buttonPressed( int buttonId )
    {
        // ── CAPTURE OR NULLIFY ────────────────────────────────────────────────────
        // OK → store whatever is in the text field.
        // anything else → null signals cancellation to the caller.
        // ──────────────────────────────────────────────────────────────────────────
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


    // ── SHOW PASSWORD CHECKBOX LISTENER ───────────────────────────────────────────
    /**
     * Toggles the password text field between masked (bullet U+2022) and
     * plain-text display when the "Show password" checkbox is clicked.
     */
    private SelectionAdapter showPasswordCheckboxListener = new SelectionAdapter()
    {
        /**
         * {@inheritDoc}
         *
         * {@code '\0'} disables echo masking (shows plain text).
         * {@code '•'} (bullet) re-enables masking.
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
    };


    // ── CREATE DIALOG AREA ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the dialog body: an optional message label, a masked password text
     * field, and a "Show password" checkbox.
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

        // ── OPTIONAL MESSAGE LABEL ────────────────────────────────────────────────
        // Only render the label if the caller provided a non-null message string.
        // ──────────────────────────────────────────────────────────────────────────
        if ( message != null )
        {
            Label messageLabel = BaseWidgetUtils.createWrappedLabel( composite, message, 1 );
            GridData messageLabelGridData = new GridData( SWT.FILL, SWT.CENTER, true, true );
            messageLabelGridData.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
            messageLabel.setLayoutData( messageLabelGridData );
        }

        // ── PASSWORD TEXT FIELD ───────────────────────────────────────────────────
        // Bullet character (U+2022) masks the typed password.
        // ──────────────────────────────────────────────────────────────────────────
        passwordText = BaseWidgetUtils.createText( composite, value, 1 );
        passwordText.setEchoChar( '•' );

        // ── SHOW PASSWORD CHECKBOX ────────────────────────────────────────────────
        showPasswordCheckbox = BaseWidgetUtils.createCheckbox( composite,
            Messages.getString( "PasswordDialog.ShowPassword" ), 1 ); //$NON-NLS-1$
        showPasswordCheckbox.addSelectionListener( showPasswordCheckboxListener );

        // ── FOCUS ─────────────────────────────────────────────────────────────────
        passwordText.setFocus();
        applyDialogFont( composite );

        return composite;
    }


    // ── GET PASSWORD ──────────────────────────────────────────────────────────────
    /**
     * Returns the password typed by the user.
     *
     * <p>Returns {@code null} if the user cancelled.  Returns an empty string if
     * the user clicked OK without typing anything.</p>
     *
     * @return The typed password, or {@code null} if cancelled.
     */
    public String getPassword()
    {
        return value;
    }
}
