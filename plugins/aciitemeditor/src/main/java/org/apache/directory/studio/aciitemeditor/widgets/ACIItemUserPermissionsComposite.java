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
package org.apache.directory.studio.aciitemeditor.widgets;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.directory.api.ldap.aci.GrantAndDenial;
import org.apache.directory.api.ldap.aci.ProtectedItem;
import org.apache.directory.api.ldap.aci.UserPermission;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.dialogs.UserPermissionDialog;
import org.apache.directory.studio.aciitemeditor.model.ProtectedItemWrapper;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;


// ── CLASS: ACIItemUserPermissionsComposite — GRAND MOFF'S USER-PERMISSION LIST ─
// In a userFirst ACI directive the Grand Moff specifies a list of user
// permissions — each one names a set of protected items and a set of
// grants-and-denials.
// This composite is the table holding those rows with Add, Edit, and Delete
// buttons.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} presenting an editable list of {@link UserPermission}
 * rows used in userFirst ACI items.
 * Each row is managed through a {@link UserPermissionDialog}.
 * Think of this as the Grand Moff's user-permission table: add rows, edit
 * them in the dialog, delete ones you no longer need.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemUserPermissionsComposite extends Composite
{
    /** The context. */
    private ACIItemValueWithContext context;

    /** The inner composite for all the content */
    private Composite composite = null;

    /** The table viewer containing all user classes */
    private TableViewer tableViewer = null;

    /** The edit button */
    private Button editButton = null;

    /** The delete button */
    private Button deleteButton = null;

    /** The selected user permissions, also input of the table viewer */
    private List<UserPermissionWrapper> userPermissionWrappers = new ArrayList<UserPermissionWrapper>();

    // ── CLASS: UserPermissionWrapper — TABLE ROW DTO ──────────────────────────
    // Each row wraps one UserPermission bean.  The toString() produces a compact
    // summary for the table cell, truncated to 50 characters if necessary.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * DTO wrapping one {@link UserPermission} bean for display in the table viewer.
     * {@link #toString()} returns a compact summary truncated to 50 characters.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class UserPermissionWrapper
    {
        /** The user permission bean. */
        private UserPermission userPermission;


        /**
         * Creates a new {@code UserPermissionWrapper}.
         *
         * @param userPermission  the user permission bean to wrap
         */
        public UserPermissionWrapper( UserPermission userPermission )
        {
            this.userPermission = userPermission;
        }


        /**
         * Returns a user-friendly summary of the user permission for display
         * in the table, truncated to 50 characters if necessary.
         *
         * @return the summary string
         */
        public String toString()
        {
            if ( userPermission == null )
            {
                return "<UNKNOWN>"; //$NON-NLS-1$
            }
            else
            {
                StringBuilder buffer = new StringBuilder();

                if ( ( userPermission.getPrecedence() != null ) && ( userPermission.getPrecedence() > -1 ) )
                {
                    buffer.append( '(' );
                    buffer.append( userPermission.getPrecedence() );
                    buffer.append( ") " );
                }

                boolean isFirst = true;

                for ( ProtectedItem item : userPermission.getProtectedItems() )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        buffer.append( ',' );
                    }

                    buffer.append( ProtectedItemWrapper.CLASS_TO_DISPLAY_MAP.get( item.getClass() ) );
                }

                buffer.append( ": " );

                isFirst = true;

                for ( GrantAndDenial gd : userPermission.getGrantsAndDenials() )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        buffer.append( ',' );
                    }

                    if ( gd.isGrant() )
                    {
                        buffer.append( '+' );
                    }
                    else
                    {
                        buffer.append( '-' );
                    }

                    buffer.append( gd.getMicroOperation().getName() );
                }

                String result = buffer.toString();
                result = result.replace( '\r', ' ' );
                result = result.replace( '\n', ' ' );

                if ( buffer.length() > 50 )
                {
                    buffer.setLength( 0 );

                    buffer.append( result.substring( 0, 25 ) );
                    buffer.append( "..." );
                    buffer.append( result.substring( result.length() - 25, result.length() ) );

                    return buffer.toString();
                }
                else
                {
                    return result;
                }
            }
        }
    }


    // ── CONSTRUCT THE USER-PERMISSIONS TABLE ──────────────────────────────────
    /**
     * Creates a new {@code ACIItemUserPermissionsComposite}.
     * Builds the table viewer and the Add / Edit / Delete button panel.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemUserPermissionsComposite( Composite parent, int style )
    {
        super( parent, style );

        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout( layout );

        GridData layoutData = new GridData();
        layoutData.horizontalAlignment = GridData.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.verticalAlignment = GridData.CENTER;
        setLayoutData( layoutData );

        createComposite();
    }


    // ── BUILD THE INNER COMPOSITE ─────────────────────────────────────────────
    /**
     * Creates the two-column inner composite, label, table viewer, and button panel.
     */
    private void createComposite()
    {
        GridData labelGridData = new GridData();
        labelGridData.horizontalSpan = 2;
        labelGridData.verticalAlignment = GridData.CENTER;
        labelGridData.grabExcessHorizontalSpace = true;
        labelGridData.horizontalAlignment = GridData.FILL;

        GridLayout gridLayout = new GridLayout();
        gridLayout.makeColumnsEqualWidth = false;
        gridLayout.numColumns = 2;

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalSpan = 1;
        gridData.verticalAlignment = GridData.BEGINNING;

        composite = new Composite( this, SWT.NONE );
        composite.setLayoutData( gridData );
        composite.setLayout( gridLayout );

        Label label = new Label( composite, SWT.NONE );
        label.setText( Messages.getString( "ACIItemUserPermissionsComposite.descripton" ) ); //$NON-NLS-1$
        label.setLayoutData( labelGridData );

        createTable();
        createButtonComposite();
    }


    // ── BUILD THE TABLE VIEWER ────────────────────────────────────────────────
    /**
     * Creates and configures the {@link TableViewer} displaying the user
     * permission list, and wires selection and double-click listeners.
     */
    private void createTable()
    {
        GridData tableGridData = new GridData();
        tableGridData.grabExcessHorizontalSpace = true;
        tableGridData.verticalAlignment = GridData.FILL;
        tableGridData.horizontalAlignment = GridData.FILL;
        //tableGridData.heightHint = 100;

        Table table = new Table( composite, SWT.BORDER );
        table.setHeaderVisible( false );
        table.setLayoutData( tableGridData );
        table.setLinesVisible( false );
        tableViewer = new TableViewer( table );
        tableViewer.setContentProvider( new ArrayContentProvider() );
        tableViewer.setLabelProvider( new LabelProvider() );
        tableViewer.setInput( userPermissionWrappers );

        tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged( SelectionChangedEvent event )
            {
                userPermissionSelected();
            }
        } );

        tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            @Override
            public void doubleClick( DoubleClickEvent event )
            {
                editUserPermission();
            }
        } );
    }


    // ── BUILD THE BUTTON PANEL ────────────────────────────────────────────────
    /**
     * Creates the Add, Edit, and Delete buttons.
     * Edit and Delete are disabled until a row is selected.
     */
    private void createButtonComposite()
    {
        GridData deleteButtonGridData = new GridData();
        deleteButtonGridData.horizontalAlignment = GridData.FILL;
        deleteButtonGridData.grabExcessHorizontalSpace = false;
        deleteButtonGridData.verticalAlignment = GridData.BEGINNING;
        deleteButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData editButtonGridData = new GridData();
        editButtonGridData.horizontalAlignment = GridData.FILL;
        editButtonGridData.grabExcessHorizontalSpace = false;
        editButtonGridData.verticalAlignment = GridData.BEGINNING;
        editButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData addButtonGridData = new GridData();
        addButtonGridData.horizontalAlignment = GridData.FILL;
        addButtonGridData.grabExcessHorizontalSpace = false;
        addButtonGridData.verticalAlignment = GridData.BEGINNING;
        addButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.CENTER;
        gridData.grabExcessHorizontalSpace = false;
        gridData.grabExcessVerticalSpace = false;
        gridData.verticalAlignment = GridData.FILL;

        Composite buttonComposite = new Composite( composite, SWT.NONE );
        buttonComposite.setLayoutData( gridData );
        buttonComposite.setLayout( gridLayout );

        Button addButton = new Button( buttonComposite, SWT.NONE );
        addButton.setText( Messages.getString( "ACIItemUserPermissionsComposite.add.button" ) ); //$NON-NLS-1$
        addButton.setLayoutData( addButtonGridData );

        addButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                addUserPermission();
            }
        } );

        editButton = new Button( buttonComposite, SWT.NONE );
        editButton.setText( Messages.getString( "ACIItemUserPermissionsComposite.edit.button" ) ); //$NON-NLS-1$
        editButton.setLayoutData( editButtonGridData );

        editButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                editUserPermission();
            }
        } );

        editButton.setEnabled( false );

        deleteButton = new Button( buttonComposite, SWT.NONE );
        deleteButton.setText( Messages.getString( "ACIItemUserPermissionsComposite.delete.button" ) ); //$NON-NLS-1$
        deleteButton.setLayoutData( deleteButtonGridData );

        deleteButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                deleteUserPermission();
            }
        } );

        deleteButton.setEnabled( false );

    }


    // ── SHOW / HIDE THIS COMPOSITE ────────────────────────────────────────────
    /**
     * Shows or hides this composite by adjusting its {@code GridData.heightHint}.
     *
     * @param visible  {@code true} to show, {@code false} to hide
     */
    public void setVisible( boolean visible )
    {
        super.setVisible( visible );

        if ( visible )
        {
            ( ( GridData ) getLayoutData() ).heightHint = -1;
        }
        else
        {
            ( ( GridData ) getLayoutData() ).heightHint = 0;
        }
    }


    // ── INJECT THE CONNECTION CONTEXT ─────────────────────────────────────────
    /**
     * Stores the connection context for use by {@link UserPermissionDialog}.
     *
     * @param context  the value context
     */
    public void setContext( ACIItemValueWithContext context )
    {
        this.context = context;
    }


    // ── POPULATE THE TABLE ────────────────────────────────────────────────────
    /**
     * Replaces the current list with {@code userPermissions} and refreshes
     * the table viewer.
     *
     * @param userPermissions  the user permissions to display
     */
    public void setUserPermissions( Collection<UserPermission> userPermissions )
    {
        userPermissionWrappers.clear();

        for ( UserPermission userPermission : userPermissions )
        {
            UserPermissionWrapper userPermissionWrapper = new UserPermissionWrapper( userPermission );

            userPermissionWrappers.add( userPermissionWrapper );
        }

        tableViewer.refresh();
    }


    // ── COLLECT THE CURRENT LIST ──────────────────────────────────────────────
    /**
     * Returns the user permissions currently in the list.
     *
     * @return the collection of user permissions
     */
    public Collection<UserPermission> getUserPermissions()
    {
        Collection<UserPermission> userPermissions = new ArrayList<UserPermission>();

        for ( UserPermissionWrapper userPermissionWrapper : userPermissionWrappers )
        {
            userPermissions.add( userPermissionWrapper.userPermission );
        }

        return userPermissions;
    }


    // ── GET THE SELECTED WRAPPER ──────────────────────────────────────────────
    /**
     * Returns the {@link UserPermissionWrapper} currently selected in the table
     * viewer, or {@code null} if nothing is selected.
     *
     * @return the selected wrapper, or {@code null}
     */
    private UserPermissionWrapper getSelectedUserPermissionWrapper()
    {
        IStructuredSelection selection = ( IStructuredSelection ) tableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            Object element = selection.getFirstElement();

            if ( element instanceof UserPermissionWrapper )
            {
                return ( UserPermissionWrapper ) element;
            }
        }

        return null;
    }


    // ── ADD A USER PERMISSION ─────────────────────────────────────────────────
    /**
     * Opens a {@link UserPermissionDialog} to compose a new user permission and
     * adds it to the list if the user confirms.
     */
    private void addUserPermission()
    {
        UserPermissionDialog dialog = new UserPermissionDialog( getShell(), null, context );

        if ( ( dialog.open() == UserPermissionDialog.OK ) && ( dialog.getUserPermission() != null ) )
        {
            UserPermissionWrapper userPermissionWrapper = new UserPermissionWrapper( dialog.getUserPermission() );
            userPermissionWrappers.add( userPermissionWrapper );

            tableViewer.refresh();
        }
    }


    // ── EDIT THE SELECTED USER PERMISSION ────────────────────────────────────
    /**
     * Opens a {@link UserPermissionDialog} pre-filled with the selected user
     * permission and replaces the old wrapper with the updated one on confirmation.
     */
    private void editUserPermission()
    {
        UserPermissionWrapper oldUserPermissionWrapper = getSelectedUserPermissionWrapper();

        if ( oldUserPermissionWrapper != null )
        {
            UserPermissionDialog dialog = new UserPermissionDialog( getShell(),
                oldUserPermissionWrapper.userPermission, context );

            if ( dialog.open() == UserPermissionDialog.OK )
            {
                oldUserPermissionWrapper.userPermission = dialog.getUserPermission();
                tableViewer.refresh();
            }
        }
    }


    // ── DELETE THE SELECTED USER PERMISSION ───────────────────────────────────
    /**
     * Removes the selected user permission wrapper from the list without
     * prompting for confirmation.
     */
    private void deleteUserPermission()
    {
        UserPermissionWrapper userPermissionWrapper = getSelectedUserPermissionWrapper();

        if ( userPermissionWrapper != null )
        {
            userPermissionWrappers.remove( userPermissionWrapper );
            tableViewer.refresh();
        }
    }


    // ── REACT TO SELECTION CHANGES ────────────────────────────────────────────
    /**
     * Updates the enabled state of the Edit and Delete buttons based on whether
     * a row is selected in the table viewer.
     */
    private void userPermissionSelected()
    {
        UserPermissionWrapper userPermissionWrapper = getSelectedUserPermissionWrapper();

        if ( userPermissionWrapper == null )
        {
            editButton.setEnabled( false );
            deleteButton.setEnabled( false );
        }
        else
        {
            editButton.setEnabled( true );
            deleteButton.setEnabled( true );
        }
    }
}
