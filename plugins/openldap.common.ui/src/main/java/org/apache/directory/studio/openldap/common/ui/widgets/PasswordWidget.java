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
package org.apache.directory.studio.openldap.common.ui.widgets;


import org.apache.directory.studio.common.ui.widgets.AbstractWidget;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.common.ui.dialogs.PasswordDialog;


// ── CLASS: PasswordWidget — REBEL VAULT KEEPER GUARDING SECRET ACCESS CODES ──
// Picture the Rebel vault keeper standing guard over the encrypted access codes
// for the Hoth base. The vault display shows the codes as bullet dots by default
// so passing Imperials cannot read them. The "Show Password" checkbox lifts the
// veil for the vault keeper alone. The "Edit Password..." button opens a
// {@link PasswordDialog} so the keeper can change the codes. An optional "None"
// checkbox allows the keeper to signal that no code is currently stored.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We provide a password display-and-edit widget consisting of a read-only text
 * field (masked by default), an "Edit Password..." button, and a "Show Password"
 * checkbox. An optional "None" checkbox signals a null (absent) password. We
 * extend {@link AbstractWidget} so change listeners are notified when the
 * password is updated.
 *
 * <p>The PasswordWidget provides a label to display the password, an edit button
 * and a 'Show Password' button to show/hide the password.</p>
 */
public class PasswordWidget extends AbstractWidget
{
    /** The password */
    private byte[] password;

    /** The flag to show the "None" checkbox or not */
    private boolean showNoneCheckbox;

    /** The flag indicating if the password should be shown or not */
    private boolean showPassword;

    // UI widgets
    private Composite composite;
    private Button noneCheckbox;
    private Text passwordText;
    private Button editButton;
    private Button showPasswordCheckbox;


    // ── CONSTRUCTOR: PasswordWidget() — BLANK VAULT SETUP ────────────────────
    // We create a vault keeper widget with no password and no None checkbox.
    // Use this when every invocation of the parent page will always have a
    // password (i.e., it is never optional).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link PasswordWidget} with no initial password and
     * without a "None" checkbox.
     */
    public PasswordWidget()
    {
    }


    // ── CONSTRUCTOR: PasswordWidget(boolean) — CONFIGURING THE VAULT ──────────
    // We create a vault keeper widget that optionally shows a "None" checkbox.
    // When the checkbox is present the user can signal that no password is stored.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create a new {@link PasswordWidget} that shows or hides the "None"
     * checkbox according to the supplied flag.
     *
     * @param showNoneCheckbox  {@code true} to show a "None" checkbox
     */
    public PasswordWidget( boolean showNoneCheckbox )
    {
        this.showNoneCheckbox = showNoneCheckbox;
    }


    // ── METHOD: createWidget(Composite) — OPENING THE VAULT (NO TOOLKIT) ──────
    // We delegate to the toolkit-aware overload with a {@code null} toolkit so
    // there is always one code path to maintain.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create the widget's SWT controls inside the given parent without a
     * {@link FormToolkit}. Delegates to
     * {@link #createWidget(Composite, FormToolkit)}.
     *
     * @param parent  the parent {@link Composite}
     */
    public void createWidget( Composite parent )
    {
        createWidget( parent, null );
    }


    // ── METHOD: createWidget(Composite, FormToolkit) — OPENING THE VAULT ──────
    // We build the composite holding the optional None checkbox, the masked
    // password text field, the Edit button, and the Show Password checkbox. We
    // wire up the None checkbox to disable editing when checked, and we call
    // noneCheckboxSelected() at the end to set the initial enabled state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create all SWT controls for this widget inside the given parent,
     * optionally adapting them with a {@link FormToolkit} for Eclipse Forms pages.
     *
     * @param parent   the parent {@link Composite}
     * @param toolkit  the form toolkit, or {@code null} for plain SWT
     */
    public void createWidget( Composite parent, FormToolkit toolkit )
    {
        // Composite
        if ( toolkit != null )
        {
            composite = toolkit.createComposite( parent );
        }
        else
        {
            composite = new Composite( parent, SWT.NONE );
        }
        GridLayout compositeGridLayout = new GridLayout( getNumberOfColumnsForComposite(), false );
        compositeGridLayout.marginHeight = compositeGridLayout.marginWidth = 0;
        compositeGridLayout.verticalSpacing = 0;
        composite.setLayout( compositeGridLayout );

        // None Checbox
        if ( showNoneCheckbox )
        {
            if ( toolkit != null )
            {
                noneCheckbox = toolkit.createButton( composite, "None", SWT.CHECK );
            }
            else
            {
                noneCheckbox = BaseWidgetUtils.createCheckbox( composite, "None", 1 );
            }
            noneCheckbox.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    noneCheckboxSelected( noneCheckbox.getSelection() );
                    notifyListeners();
                }
            } );
        }

        // Password Text
        if ( toolkit != null )
        {
            passwordText = toolkit.createText( composite, "", SWT.NONE );
        }
        else
        {
            passwordText = BaseWidgetUtils.createReadonlyText( composite, "", 1 );
        }
        passwordText.setEditable( false );
        GridData gd = new GridData( SWT.FILL, SWT.CENTER, true, false );
        gd.widthHint = 50;
        passwordText.setLayoutData( gd );

        // Setting the echo char for the password text
        if ( showPassword )
        {
            passwordText.setEchoChar( '\0' );
        }
        else
        {
            passwordText.setEchoChar( '•' );
        }

        // Edit Button
        if ( toolkit != null )
        {
            editButton = toolkit.createButton( composite, "Edit Password...", SWT.PUSH );
        }
        else
        {
            editButton = BaseWidgetUtils.createButton( composite, "Edit Password...", 1 );
            editButton.setLayoutData( new GridData() );
        }
        editButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                editButtonAction();
            }
        } );

        // Show Password Checkbox
        if ( toolkit != null )
        {
            if ( showNoneCheckbox )
            {
                toolkit.createLabel( composite, "" );
            }

            showPasswordCheckbox = toolkit.createButton( composite, "Show Password", SWT.CHECK );
        }
        else
        {
            if ( showNoneCheckbox )
            {
                BaseWidgetUtils.createLabel( composite, "", 1 );
            }

            showPasswordCheckbox = BaseWidgetUtils.createCheckbox( composite, "Show Password",
                getNumberOfColumnsForComposite() );
        }
        GridData showPasswordCheckboxGridData = new GridData();
        showPasswordCheckboxGridData.horizontalSpan = getNumberOfColumnsForComposite() - 1;
        showPasswordCheckbox.setLayoutData( showPasswordCheckboxGridData );
        showPasswordCheckbox.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent event )
            {
                showPasswordAction();
            }
        } );

        noneCheckboxSelected( showNoneCheckbox );
    }


    // ── METHOD: getNumberOfColumnsForComposite — COUNTING THE VAULT PANELS ────
    // We return 3 when the None checkbox is shown (checkbox + text + button),
    // or 2 when hidden (text + button).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the column count needed by the inner composite: 3 if the "None"
     * checkbox is visible, 2 otherwise.
     *
     * @return the number of columns for the inner {@link GridLayout}
     */
    private int getNumberOfColumnsForComposite()
    {
        if ( showNoneCheckbox )
        {
            return 3;
        }
        else
        {
            return 2;
        }
    }


    // ── METHOD: noneCheckboxSelected — LOCKING OR UNLOCKING THE VAULT ─────────
    // When the None checkbox is checked we disable the Edit button and the Show
    // Password checkbox because there is no code to view or change. When
    // unchecked we re-enable both controls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We enable or disable the Edit button and Show Password checkbox based on
     * the state of the None checkbox. When {@code state} is {@code true} both
     * controls are disabled.
     *
     * @param state  {@code true} if "None" is selected
     */
    private void noneCheckboxSelected( boolean state )
    {
        editButton.setEnabled( !state );
        showPasswordCheckbox.setEnabled( !state );
    }


    // ── METHOD: editButtonAction — OPENING THE VAULT DOOR ────────────────────
    // We open a {@link PasswordDialog} pre-seeded with the current password. If
    // the user confirms a new password we update our internal field, refresh the
    // text display, and fire change listeners.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We open a {@link PasswordDialog} seeded with the current password. If the
     * user confirms a new password we update our internal byte array, display
     * it in the text field, and notify change listeners.
     */
    private void editButtonAction()
    {
        // Creating and displaying a password dialog
        PasswordDialog passwordDialog = new PasswordDialog( editButton.getShell(), password );
        if ( passwordDialog.open() == Dialog.OK )
        {
            if ( passwordDialog.getNewPassword() != password )
            {
                byte[] password = passwordDialog.getNewPassword();
                if ( ( password != null ) && ( password.length > 0 ) )
                {
                    this.password = password;
                    passwordText.setText( new String( password ) );
                    notifyListeners();
                }
            }
        }
    }


    // ── METHOD: showPasswordAction — LIFTING OR LOWERING THE VEIL ────────────
    // We toggle the echo character on the password text field between the null
    // character (show plaintext) and the bullet character (mask as dots).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We toggle the password text field between plaintext mode (echo char
     * {@code '\0'}) and masked mode (echo char {@code '•'}) based on the
     * current state of the "Show Password" checkbox.
     */
    private void showPasswordAction()
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


    // ── METHOD: setPassword — LOADING THE ACCESS CODES ────────────────────────
    // We update our internal password field and synchronize the text display.
    // If a None checkbox is present we also update its selection state and
    // enable/disable the edit controls accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We set the widget's current password and refresh the display. If a "None"
     * checkbox is present we set its selection based on whether {@code password}
     * is {@code null}.
     *
     * @param password  the new password bytes, or {@code null} for none
     */
    public void setPassword( byte[] password )
    {
        this.password = password;

        if ( showNoneCheckbox )
        {
            boolean noneSelected = ( password == null );
            noneCheckbox.setSelection( noneSelected );
            noneCheckboxSelected( noneSelected );
        }

        // Updating the password text field
        if ( ( password != null ) && ( password.length > 0 ) )
        {
            passwordText.setText( new String( password ) );
        }
        else
        {
            passwordText.setText( "" ); //$NON-NLS-1$
        }
    }


    // ── METHOD: getPassword — READING THE ACCESS CODES ───────────────────────
    // We return the current password bytes, or {@code null} if None is selected
    // or the password array is empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the current password as a byte array, or {@code null} if the
     * "None" checkbox is checked or no password has been set.
     *
     * @return the password bytes, or {@code null}
     */
    public byte[] getPassword()
    {
        if ( showNoneCheckbox && noneCheckbox.getSelection() )
        {
            return null;
        }

        if ( ( password != null ) && ( password.length > 0 ) )
        {
            return password;
        }

        return null;
    }


    // ── METHOD: getPasswordAsString — READING THE ACCESS CODES AS TEXT ────────
    // We return the password decoded as a UTF-16 String, or {@code null} if
    // None is selected or the password is empty. Useful for text-based
    // configuration attributes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the current password decoded as a {@link String}, or
     * {@code null} if the "None" checkbox is checked or no password has been set.
     *
     * @return the password string, or {@code null}
     */
    public String getPasswordAsString()
    {
        if ( showNoneCheckbox && noneCheckbox.getSelection() )
        {
            return null;
        }

        if ( ( password != null ) && ( password.length > 0 ) )
        {
            return new String( password );
        }

        return null;
    }


    // ── METHOD: getControl — HANDING OVER THE VAULT DOOR HANDLE ──────────────
    // We return the top-level composite so the parent layout can size and
    // position the entire password widget as a unit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the top-level {@link Control} (a {@link Composite}) for this
     * widget so the parent layout can size and position it.
     *
     * @return the primary composite control
     */
    public Control getControl()
    {
        return composite;
    }


    // ── METHOD: setEnabled — LOCKING OR UNLOCKING THE VAULT EXTERNALLY ────────
    // We enable or disable the interactive controls depending on the overall
    // enabled flag, honouring the None checkbox state if present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We enable or disable the widget's interactive controls. When a "None"
     * checkbox is present, we also enable/disable it and respect its current
     * selection state when enabling the Edit and Show Password controls.
     *
     * @param enabled  {@code true} to enable the widget, {@code false} to disable it
     */
    public void setEnabled( boolean enabled )
    {
        if ( ( editButton != null ) && ( !editButton.isDisposed() ) )
        {
            if ( showNoneCheckbox )
            {
                noneCheckbox.setEnabled( enabled );
                noneCheckboxSelected( noneCheckbox.getSelection() && enabled );
            }
            else
            {
                editButton.setEnabled( enabled );
                showPasswordCheckbox.setEnabled( enabled );
            }
        }
    }
}
