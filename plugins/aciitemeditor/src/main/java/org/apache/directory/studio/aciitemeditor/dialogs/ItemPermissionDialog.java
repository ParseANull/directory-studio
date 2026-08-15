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
import org.apache.directory.api.ldap.aci.ItemPermission;
import org.apache.directory.api.ldap.aci.UserClass;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.widgets.ACIItemGrantsAndDenialsComposite;
import org.apache.directory.studio.aciitemeditor.widgets.ACIItemUserClassesComposite;
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


// ── CLASS: ItemPermissionDialog — GRAND MOFF COMPOSING AN ITEM-FIRST DIRECTIVE
// In an itemFirst ACI directive, Grand Moff Tarkin specifies which user classes
// are allowed near a resource, and exactly which grants and denials apply.
// This dialog is the form he fills in: precedence spinner at the top,
// user-classes table in the middle, grants-and-denials tree at the bottom.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link Dialog} for composing or editing a single {@link ItemPermission}.
 * The dialog contains:
 * <ul>
 *   <li>An optional precedence spinner (enabled by a checkbox)</li>
 *   <li>An {@link ACIItemUserClassesComposite} for selecting user classes</li>
 *   <li>An {@link ACIItemGrantsAndDenialsComposite} for specifying grants and denials</li>
 * </ul>
 * Pressing OK assembles the three parts into a new {@link ItemPermission}.
 * Think of this as Grand Moff composing an item-first directive: who may access
 * the resource, and under what permissions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ItemPermissionDialog extends Dialog
{

    /** The context */
    private ACIItemValueWithContext context;

    /** The initial value, passed by the constructor */
    private ItemPermission initialItemPermission;

    /** The resulting value returned by getItemPermission() */
    private ItemPermission returnItemPermission;

    /** The precedence checkbox to enable/disable spinner */
    private Button precedenceCheckbox = null;

    /** The precedence spinner */
    private Spinner precedenceSpinner = null;

    /** The widget with user classes table */
    private ACIItemUserClassesComposite userClassesComposite;

    /** The widget with grants and denials table */
    private ACIItemGrantsAndDenialsComposite grantsAndDenialsComposite;


    // ── OPEN THE ITEM-PERMISSION FORM ─────────────────────────────────────────
    // Grand Moff opens the form pre-populated with the existing permission
    // (if editing) or blank (if adding).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code ItemPermissionDialog}.
     *
     * <p>For example — adding a new item permission from the table composite:</p>
     * <pre>
     *   ItemPermissionDialog dlg = new ItemPermissionDialog(shell, null, context);
     *   if (dlg.open() == Dialog.OK) {
     *     ItemPermission perm = dlg.getItemPermission();
     *   }
     * </pre>
     *
     * @param parentShell           the parent SWT shell
     * @param initialItemPermission the permission to pre-populate the form, or {@code null} for a blank form
     * @param context               the DTO carrying connection and entry for sub-dialogs
     */
    public ItemPermissionDialog( Shell parentShell, ItemPermission initialItemPermission,
        ACIItemValueWithContext context )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.context = context;
        this.initialItemPermission = initialItemPermission;
        this.returnItemPermission = null;
    }


    // ── SET TITLE AND ICON ────────────────────────────────────────────────────
    // The orderly labels the form window so the officer knows which directive
    // section he is filling in.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog title and icon.
     *
     * {@inheritDoc}
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( Messages.getString( "ItemPermissionDialog.dialog.text" ) ); //$NON-NLS-1$
        shell.setImage( Activator.getDefault().getImage( Messages.getString( "ItemPermissionDialog.dialog.icon" ) ) ); //$NON-NLS-1$
    }


    // ── VALIDATE AND COMMIT ───────────────────────────────────────────────────
    // Grand Moff signs the form: precedence, user classes, and grants/denials
    // are assembled into an ItemPermission and stored.  If assembly fails, an
    // error dialog appears and the form stays open.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Assembles the filled-in form into a new {@link ItemPermission} and closes
     * the dialog.  Shows a {@link MessageDialog} if any field is invalid.
     */
    protected void okPressed()
    {
        try
        {
            int precedence = precedenceCheckbox.getSelection() ? precedenceSpinner.getSelection() : -1;
            Collection<UserClass> userClasses = userClassesComposite.getUserClasses();
            Collection<GrantAndDenial> grantsAndDenials = grantsAndDenialsComposite.getGrantsAndDenials();
            returnItemPermission = new ItemPermission( precedence, grantsAndDenials, userClasses );
            super.okPressed();
        }
        catch ( Exception e )
        {
            MessageDialog.openError( getShell(), Messages
                .getString( "ItemPermissionDialog.error.invalidItemPermission" ), e.getMessage() ); //$NON-NLS-1$
        }
    }


    // ── BUILD THE FORM LAYOUT ─────────────────────────────────────────────────
    // The orderly prints and assembles the three-section form: precedence at the
    // top, user-classes table in the middle, grants-and-denials at the bottom.
    // If an initial permission was provided, all fields are pre-filled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the precedence controls, user-classes composite, and grants-and-denials
     * composite, then pre-fills them from {@code initialItemPermission} if provided.
     *
     * @param parent  the parent composite provided by the Dialog framework
     * @return        the fully constructed dialog content area
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
        precedenceCheckbox.setText( Messages.getString( "ItemPermissionDialog.precedence.label" ) ); //$NON-NLS-1$
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

        // user classes
        userClassesComposite = new ACIItemUserClassesComposite( composite, SWT.NONE );
        userClassesComposite.setContext( context );

        // grants and denial
        grantsAndDenialsComposite = new ACIItemGrantsAndDenialsComposite( composite, SWT.NONE );

        // set initial values
        if ( initialItemPermission != null )
        {
            if ( ( initialItemPermission.getPrecedence() != null ) && ( initialItemPermission.getPrecedence() > -1 ) )
            {
                precedenceCheckbox.setSelection( true );
                precedenceSpinner.setEnabled( true );
                precedenceSpinner.setSelection( initialItemPermission.getPrecedence() );
            }
            userClassesComposite.setUserClasses( initialItemPermission.getUserClasses() );
            grantsAndDenialsComposite.setGrantsAndDenials( initialItemPermission.getGrantsAndDenials() );
        }

        applyDialogFont( composite );
        return composite;
    }


    // ── RETURN THE COMPOSED PERMISSION ────────────────────────────────────────
    // After Grand Moff seals the form, the caller retrieves the assembled
    // ItemPermission to add to or update the item-permissions table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ItemPermission} assembled by the dialog after OK was pressed.
     * Returns {@code null} if the dialog was cancelled.
     *
     * @return the composed item permission, or {@code null}
     */
    public ItemPermission getItemPermission()
    {
        return returnItemPermission;
    }

}
