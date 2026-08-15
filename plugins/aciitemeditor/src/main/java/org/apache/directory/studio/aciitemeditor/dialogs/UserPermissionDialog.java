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
package org.apache.directory.studio.aciitemeditor.dialogs;


import java.util.Collection;

import org.apache.directory.api.ldap.aci.GrantAndDenial;
import org.apache.directory.api.ldap.aci.ProtectedItem;
import org.apache.directory.api.ldap.aci.UserPermission;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.widgets.ACIItemGrantsAndDenialsComposite;
import org.apache.directory.studio.aciitemeditor.widgets.ACIItemProtectedItemsComposite;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;


// ── CLASS: UserPermissionDialog — GRAND MOFF COMPOSING A USER-FIRST DIRECTIVE ─
// In a userFirst ACI directive, Grand Moff Tarkin specifies which resources a
// particular user class may access, and the exact grants and denials that apply.
// This dialog is the form he fills in: precedence at the top, protected-items
// table in the middle, grants-and-denials tree at the bottom.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Dialog} for composing or editing a single {@link UserPermission}.
 * The dialog contains:
 * <ul>
 *   <li>An optional precedence spinner (enabled by a checkbox)</li>
 *   <li>An {@link ACIItemProtectedItemsComposite} for selecting protected items</li>
 *   <li>An {@link ACIItemGrantsAndDenialsComposite} for specifying grants and denials</li>
 * </ul>
 * Pressing OK assembles the three parts into a new {@link UserPermission}.
 * Think of this as Grand Moff composing a user-first directive: which resources
 * this user class may touch, and under what permissions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UserPermissionDialog extends Dialog
{

    /** The context */
    private ACIItemValueWithContext context;

    /** The initial value, passed by the constructor */
    private UserPermission initialUserPermission;

    /** The resulting value returned by getUserPermission() */
    private UserPermission returnUserPermission;

    /** The precedence checkbox to enable/disable spinner */
    private Button precedenceCheckbox = null;

    /** The precedence spinner */
    private Spinner precedenceSpinner = null;

    /** The widget with protected items table */
    private ACIItemProtectedItemsComposite protectedItemsComposite;

    /** The widget with grants and denials table */
    private ACIItemGrantsAndDenialsComposite grantsAndDenialsComposite;


    // ── OPEN THE USER-PERMISSION FORM ─────────────────────────────────────────
    // Grand Moff opens the user-first form, pre-populated with an existing
    // permission (if editing) or blank (if adding).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code UserPermissionDialog}.
     *
     * <p>For example — adding a new user permission from the table composite:</p>
     * <pre>
     *   UserPermissionDialog dlg = new UserPermissionDialog(shell, null, context);
     *   if (dlg.open() == Dialog.OK) {
     *     UserPermission perm = dlg.getUserPermission();
     *   }
     * </pre>
     *
     * @param parentShell            the parent SWT shell
     * @param initialUserPermission  the permission to pre-populate, or {@code null} for a blank form
     * @param context                the DTO carrying connection and entry for sub-dialogs
     */
    public UserPermissionDialog( Shell parentShell, UserPermission initialUserPermission,
        ACIItemValueWithContext context )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.initialUserPermission = initialUserPermission;
        this.context = context;
        this.returnUserPermission = null;
    }


    // ── SET TITLE AND ICON ────────────────────────────────────────────────────
    // The orderly labels the dialog window so the officer knows which directive
    // section he is completing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog title and icon.
     *
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "UserPermissionDialog.dialog.text" ) ); //$NON-NLS-1$
        shell.setImage( Activator.getDefault().getImage( Messages.getString( "UserPermissionDialog.dialog.icon" ) ) ); //$NON-NLS-1$
    }


    // ── VALIDATE AND COMMIT ───────────────────────────────────────────────────
    // Grand Moff signs the user-first form: precedence, protected items, and
    // grants/denials are assembled into a UserPermission and stored.
    // If assembly fails, an error dialog appears and the form stays open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Assembles the filled-in form into a new {@link UserPermission} and closes
     * the dialog.  Shows a {@link MessageDialog} if any field is invalid.
     */
    protected void okPressed()
    {
        try
        {
            Integer precedence = precedenceCheckbox.getSelection() ? precedenceSpinner.getSelection() : null;
            Collection<ProtectedItem> protectedItems = protectedItemsComposite.getProtectedItems();
            Collection<GrantAndDenial> grantsAndDenials = grantsAndDenialsComposite.getGrantsAndDenials();
            returnUserPermission = new UserPermission( precedence, grantsAndDenials, protectedItems );
            super.okPressed();
        }
        catch ( Exception e )
        {
            MessageDialog.openError( getShell(), Messages
                .getString( "UserPermissionDialog.error.invalidUserPermission" ), e.getMessage() ); //$NON-NLS-1$
        }
    }


    // ── BUILD THE FORM LAYOUT ─────────────────────────────────────────────────
    // The orderly assembles the three-section form: precedence at the top,
    // protected-items table in the middle, grants-and-denials tree at the bottom.
    // Pre-fills from the initial permission if one was provided.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd.heightHint = convertVerticalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH ) * 4 / 3;
        composite.setLayoutData( gd );

        // precedence
        Composite spinnerComposite = new Composite( composite, SWT.NONE );
        spinnerComposite.setLayout( new GridLayout( 2, false ) );
        spinnerComposite.setLayoutData( new GridData() );
        precedenceCheckbox = new Button( spinnerComposite, SWT.CHECK );
        precedenceCheckbox.setText( Messages.getString( "UserPermissionDialog.precedence.label" ) ); //$NON-NLS-1$
        precedenceCheckbox.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                precedenceSpinner.setEnabled( precedenceCheckbox.getSelection() );
            }
        } );
        precedenceSpinner = new Spinner( spinnerComposite, SWT.BORDER );
        precedenceSpinner.setMinimum( 0 );
        precedenceSpinner.setMaximum( 255 );
        precedenceSpinner.setDigits( 0 );
        precedenceSpinner.setIncrement( 1 );
        precedenceSpinner.setPageIncrement( 10 );
        precedenceSpinner.setSelection( 0 );
        precedenceSpinner.setEnabled( false );
        GridData precedenceGridData = new GridData();
        precedenceGridData.grabExcessHorizontalSpace = true;
        precedenceGridData.verticalAlignment = GridData.CENTER;
        precedenceGridData.horizontalAlignment = GridData.BEGINNING;
        precedenceGridData.widthHint = 3 * 12;
        precedenceSpinner.setLayoutData( precedenceGridData );

        // protected items
        protectedItemsComposite = new ACIItemProtectedItemsComposite( composite, SWT.NONE );
        protectedItemsComposite.setContext( context );

        // grants and denials
        grantsAndDenialsComposite = new ACIItemGrantsAndDenialsComposite( composite, SWT.NONE );

        // set initial values
        if ( initialUserPermission != null )
        {
            if ( ( initialUserPermission.getPrecedence() != null ) && ( initialUserPermission.getPrecedence() > -1 ) )
            {
                precedenceCheckbox.setSelection( true );
                precedenceSpinner.setEnabled( true );
                precedenceSpinner.setSelection( initialUserPermission.getPrecedence() );
            }
            protectedItemsComposite.setProtectedItems( initialUserPermission.getProtectedItems() );
            grantsAndDenialsComposite.setGrantsAndDenials( initialUserPermission.getGrantsAndDenials() );
        }

        applyDialogFont( composite );
        return composite;
    }


    // ── RETURN THE COMPOSED PERMISSION ────────────────────────────────────────
    // After Grand Moff seals the form, the caller retrieves the assembled
    // UserPermission to add to or update the user-permissions table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link UserPermission} assembled by the dialog after OK was pressed.
     * Returns {@code null} if the dialog was cancelled.
     *
     * @return the composed user permission, or {@code null}
     */
    public UserPermission getUserPermission()
    {
        return returnUserPermission;
    }

}
