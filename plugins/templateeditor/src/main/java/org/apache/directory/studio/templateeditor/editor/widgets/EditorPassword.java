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
package org.apache.directory.studio.templateeditor.editor.widgets;


import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.valueeditors.password.PasswordDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.model.widgets.TemplatePassword;


// ── CLASS: EditorPassword — THE TANTIVE IV SECURITY CONSOLE ──────────────────────
// On the Tantive IV, access codes are displayed as dots — not plaintext — and
// only a security officer with the right clearance can change them by entering
// the new code through the secure console dialog. This class is that security
// console: it displays the current LDAP password attribute as dots (or plaintext
// if the "Show Password" checkbox is checked), and an optional "Edit..." button
// opens the Eclipse PasswordDialog so the officer can change the value using the
// proper password management UI (which handles hashing, etc.).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A password display and editor widget bound to a single binary LDAP attribute.
 * The password is always displayed as masked dots by default. An optional
 * "Edit…" button opens the Eclipse {@link PasswordDialog} for secure password
 * entry. An optional "Show Password" checkbox toggles the echo character between
 * dots and plaintext.
 * Think of this as the Tantive IV security console.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EditorPassword extends EditorWidget<TemplatePassword>
{
    /** The current password value */
    private byte[] currentPassword;

    /** The password text field */
    private Text passwordTextField;

    /** The "Edit..." button */
    private ToolItem editToolItem;

    /** The "Show Password" checkbox*/
    private Button showPasswordCheckbox;


    // ── CONSTRUCTOR: INSTALL THE SECURITY CONSOLE ─────────────────────────────────
    // The security technician installs the password panel. It binds to the binary
    // LDAP attribute declared in templatePassword and shows the password in the
    // configured mode (hidden or visible).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EditorPassword} bound to the given template password model.
     *
     * @param editor            the owning entry editor
     * @param templatePassword  the template model specifying attribute type, hidden flag, etc.
     * @param toolkit           the form toolkit
     */
    public EditorPassword( IEntryEditor editor, TemplatePassword templatePassword, FormToolkit toolkit )
    {
        super( templatePassword, editor, toolkit );
    }


    // ── CREATE WIDGET: POWER UP THE SECURITY CONSOLE ─────────────────────────────
    /**
     * Creates the password text field (with echo char), the optional "Edit…" button,
     * and the optional "Show Password" checkbox. Fills the text from the current
     * LDAP attribute value.
     *
     * @param parent  the parent composite
     * @return the widget composite
     */
    public Composite createWidget( Composite parent )
    {
        // Creating and initializing the widget UI
        Composite composite = initWidget( parent );

        // Updating the widget's content
        updateWidget();

        // Adding the listeners
        addListeners();

        return composite;
    }


    // ── INIT WIDGET: BUILD THE PASSWORD DISPLAY ───────────────────────────────────
    // We create a composite with 1 or 2 columns (text + optional toolbar), set the
    // echo character to the bullet symbol if the password should be hidden, and
    // optionally add the "Show Password" checkbox below.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the composite: a non-editable text field with configurable echo char,
     * an optional "Edit…" toolbar button, and an optional "Show Password" checkbox.
     *
     * @param parent  the parent composite
     * @return the password widget composite
     */
    private Composite initWidget( Composite parent )
    {
        // Creating the widget composite
        Composite composite = getToolkit().createComposite( parent );
        composite.setLayoutData( getGridata() );

        // Calculating the number of columns needed
        int numberOfColumns = 1;
        if ( getWidget().isShowEditButton() )
        {
            numberOfColumns++;
        }

        // Creating the layout
        GridLayout gl = new GridLayout( numberOfColumns, false );
        gl.marginHeight = gl.marginWidth = 0;
        gl.horizontalSpacing = gl.verticalSpacing = 0;
        composite.setLayout( gl );

        // Creating the password text field
        passwordTextField = getToolkit().createText( composite, null, SWT.BORDER );
        passwordTextField.setEditable( false );
        GridData gd = new GridData( SWT.FILL, SWT.CENTER, true, false );
        gd.widthHint = 50;
        passwordTextField.setLayoutData( gd );

        // Setting the echo char for the password text field
        if ( getWidget().isHidden() )
        {
            passwordTextField.setEchoChar( '•' );
        }
        else
        {
            passwordTextField.setEchoChar( '\0' );
        }

        // Creating the edit password button
        if ( getWidget().isShowEditButton() )
        {
            ToolBar toolbar = new ToolBar( composite, SWT.HORIZONTAL | SWT.FLAT );

            editToolItem = new ToolItem( toolbar, SWT.PUSH );
            editToolItem.setToolTipText( Messages.getString( "EditorPassword.EditPassword" ) ); //$NON-NLS-1$
            editToolItem.setImage( EntryTemplatePlugin.getDefault().getImage(
                EntryTemplatePluginConstants.IMG_TOOLBAR_EDIT_PASSWORD ) );
        }

        // Creating the show password checkbox
        if ( getWidget().isShowShowPasswordCheckbox() )
        {
            showPasswordCheckbox = getToolkit().createButton( composite,
                Messages.getString( "EditorPassword.ShowPassword" ), SWT.CHECK ); //$NON-NLS-1$
        }

        return composite;
    }


    // ── UPDATE WIDGET: REFRESH THE PASSWORD DISPLAY ───────────────────────────────
    // We re-read the binary LDAP attribute value and display it in the text field.
    // The echo character masking means the actual bytes aren't revealed unless the
    // "Show Password" checkbox is active.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Re-reads the binary LDAP attribute value and updates the text field.
     * The echo character is already set, so the display respects the hidden flag.
     */
    private void updateWidget()
    {
        // Getting the current password value in the attribute
        IAttribute attribute = getAttribute();
        if ( ( attribute != null ) && ( attribute.getValueSize() > 0 ) )
        {
            currentPassword = attribute.getValues()[0].getBinaryValue();
        }
        else
        {
            currentPassword = null;
        }

        // Updating the password text field
        if ( currentPassword != null )
        {
            passwordTextField.setText( new String( currentPassword ) );
        }
        else
        {
            passwordTextField.setText( "" ); //$NON-NLS-1$
        }
    }


    // ── ADD LISTENERS: WIRE THE EDIT AND SHOW-PASSWORD HANDLERS ─────────────────
    /**
     * Attaches selection listeners to the "Edit…" toolbar button and the "Show
     * Password" checkbox (if present).
     */
    private void addListeners()
    {
        // Edit Password toolbar item
        if ( ( editToolItem != null ) && ( !editToolItem.isDisposed() ) )
        {
            editToolItem.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    editToolItemAction();
                }
            } );
        }

        // Show Password checkbox
        if ( ( showPasswordCheckbox != null ) && ( !showPasswordCheckbox.isDisposed() ) )
        {
            showPasswordCheckbox.addSelectionListener( new SelectionAdapter()
            {
                public void widgetSelected( SelectionEvent e )
                {
                    showPasswordAction();
                }
            } );
        }
    }


    // ── EDIT TOOL ITEM ACTION: OPEN THE PASSWORD DIALOG ──────────────────────────
    // The security officer clicks "Edit..." and the standard Eclipse PasswordDialog
    // appears. If they enter a new password and click OK, we store the new bytes
    // and write them to the LDAP attribute.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens a {@link PasswordDialog} pre-populated with the current password bytes.
     * If the user confirms a new password, writes it to the LDAP attribute.
     */
    private void editToolItemAction()
    {
        // Creating and displaying a password dialog
        PasswordDialog passwordDialog = new PasswordDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getShell(), currentPassword, getEntry() );
        if ( passwordDialog.open() == Dialog.OK )
        {
            if ( passwordDialog.getNewPassword() != currentPassword )
            {
                currentPassword = passwordDialog.getNewPassword();
                passwordTextField.setText( new String( currentPassword ) );
                updateEntry();
            }
        }
    }


    // ── SHOW PASSWORD ACTION: TOGGLE ECHO CHARACTER ───────────────────────────────
    // The operator checks "Show Password" — the dots change to the actual characters
    // so they can verify what's stored.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the echo character between {@code '\0'} (plaintext) and the bullet
     * character {@code '•'} based on the "Show Password" checkbox state.
     */
    private void showPasswordAction()
    {
        if ( showPasswordCheckbox.getSelection() )
        {
            passwordTextField.setEchoChar( '\0' );
        }
        else
        {
            passwordTextField.setEchoChar( '•' );
        }
    }


    // ── UPDATE ENTRY: WRITE THE NEW PASSWORD TO THE ATTRIBUTE ────────────────────
    /**
     * Writes the current {@code currentPassword} bytes to the LDAP attribute.
     * Creates, modifies, or deletes the attribute depending on whether the
     * password is set.
     */
    private void updateEntry()
    {
        // Getting the  attribute
        IAttribute attribute = getAttribute();
        if ( attribute == null )
        {
            if ( ( currentPassword != null ) && ( currentPassword.length != 0 ) )
            {
                // Creating a new attribute with the value
                addNewAttribute( currentPassword );
            }
        }
        else
        {
            if ( ( currentPassword != null ) && ( currentPassword.length != 0 ) )
            {
                // Modifying the existing attribute
                modifyAttributeValue( currentPassword );
            }
            else
            {
                // Deleting the attribute
                deleteAttribute();
            }
        }
    }


    // ── UPDATE: REFRESH THE PASSWORD DISPLAY ─────────────────────────────────────
    /**
     * Refreshes the password text field from the current LDAP attribute value.
     */
    public void update()
    {
        updateWidget();
    }


    // ── DISPOSE: NOTHING EXTRA TO CLEAN UP ───────────────────────────────────────
    /**
     * No-op — SWT controls are owned by their parent composite.
     */
    public void dispose()
    {
        // Nothing to do
    }
}
