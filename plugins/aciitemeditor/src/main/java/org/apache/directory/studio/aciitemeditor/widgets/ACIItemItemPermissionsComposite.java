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
import org.apache.directory.api.ldap.aci.ItemPermission;
import org.apache.directory.api.ldap.aci.UserClass;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.dialogs.ItemPermissionDialog;
import org.apache.directory.studio.aciitemeditor.model.UserClassWrapper;
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


// ── CLASS: ACIItemItemPermissionsComposite — GRAND MOFF'S ITEM-PERMISSION LIST ─
// In an itemFirst ACI directive the Grand Moff specifies a list of item
// permissions — each one names a set of user classes and a set of
// grants-and-denials that apply to them.
// This composite is the table that holds those item-permission rows with
// Add, Edit, and Delete buttons.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} presenting an editable list of {@link ItemPermission}
 * rows used in itemFirst ACI items.
 * Each row is managed through an {@link ItemPermissionDialog}.
 * Think of this as the Grand Moff's item-permission table: add rows, edit
 * them in the dialog, delete ones you no longer need.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemItemPermissionsComposite extends Composite
{
    /** The context. */
    private ACIItemValueWithContext context;

    /** The inner composite for all the content */
    private Composite composite = null;

    /** The table viewer containing all item classes */
    private TableViewer tableViewer = null;

    /** The edit button */
    private Button editButton = null;

    /** The delete button */
    private Button deleteButton = null;

    /** The selected item permissions, input of the table viewer */
    private List<ItemPermissionWrapper> itemPermissionWrappers = new ArrayList<ItemPermissionWrapper>();

    // ── CLASS: ItemPermissionWrapper — TABLE ROW DTO ──────────────────────────
    // Each row in the table view wraps one ItemPermission bean.
    // The toString() produces a compact summary for the table cell.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * DTO wrapping one {@link ItemPermission} bean for display in the table viewer.
     * {@link #toString()} returns a compact summary truncated to 50 characters.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class ItemPermissionWrapper
    {
        /** The item permission bean. */
        private ItemPermission itemPermission;


        /**
         * Creates a new {@code ItemPermissionWrapper}.
         *
         * @param itemPermission  the item permission bean to wrap
         */
        private ItemPermissionWrapper( ItemPermission itemPermission )
        {
            this.itemPermission = itemPermission;
        }


        /**
         * Returns a user-friendly summary of the item permission for display
         * in the table, truncated to 50 characters if necessary.
         *
         * @return the summary string
         */
        public String toString()
        {
            if ( itemPermission == null )
            {
                return "<UNKNOWN>"; //$NON-NLS-1$
            }
            else
            {
                StringBuilder buffer = new StringBuilder();

                if ( ( itemPermission.getPrecedence() != null ) && ( itemPermission.getPrecedence() > -1 ) )
                {
                    buffer.append( '(' );
                    buffer.append( itemPermission.getPrecedence() );
                    buffer.append( ") " );
                }

                boolean isFirst = true;

                for ( UserClass userClass : itemPermission.getUserClasses() )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        buffer.append( ',' );
                    }

                    String s = UserClassWrapper.CLASS_TO_DISPLAY_MAP.get( userClass.getClass() );
                    buffer.append( s );
                }

                buffer.append( ": " );

                isFirst = true;

                for ( GrantAndDenial grantAndDenial : itemPermission.getGrantsAndDenials() )
                {
                    if ( isFirst )
                    {
                        isFirst = false;
                    }
                    else
                    {
                        buffer.append( ',' );
                    }

                    if ( grantAndDenial.isGrant() )
                    {
                        buffer.append( '+' );
                    }
                    else
                    {
                        buffer.append( '-' );
                    }

                    buffer.append( grantAndDenial.getMicroOperation().getName() );
                }

                String s = buffer.toString();
                s = s.replace( '\r', ' ' );
                s = s.replace( '\n', ' ' );

                if ( s.length() > 50 )
                {
                    String temp = s;
                    s = temp.substring( 0, 25 );
                    s = s + "..."; //$NON-NLS-1$
                    s = s + temp.substring( temp.length() - 25, temp.length() );
                }

                return s;
            }
        }
    }


    // ── CONSTRUCT THE ITEM-PERMISSIONS TABLE ──────────────────────────────────
    /**
     * Creates a new {@code ACIItemItemPermissionsComposite}.
     * Builds the two-column inner composite, the table viewer, and the
     * Add / Edit / Delete button panel.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemItemPermissionsComposite( Composite parent, int style )
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
     * Creates the inner two-column composite, label, table viewer, and button panel.
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
        label.setText( Messages.getString( "ACIItemItemPermissionsComposite.description" ) ); //$NON-NLS-1$
        label.setLayoutData( labelGridData );

        createTable();
        createButtonComposite();
    }


    // ── BUILD THE TABLE VIEWER ────────────────────────────────────────────────
    /**
     * Creates and configures the {@link TableViewer} displaying the item
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
        tableViewer.setInput( itemPermissionWrappers );

        tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                itemPermissionSelected();
            }
        } );

        tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                editItemPermission();
            }
        } );
    }


    // ── BUILD THE BUTTON PANEL ────────────────────────────────────────────────
    /**
     * Creates the Add, Edit, and Delete buttons in a vertical button panel.
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
        addButton.setText( Messages.getString( "ACIItemItemPermissionsComposite.add.button" ) ); //$NON-NLS-1$
        addButton.setLayoutData( addButtonGridData );
        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addItemPermission();
            }
        } );

        editButton = new Button( buttonComposite, SWT.NONE );
        editButton.setText( Messages.getString( "ACIItemItemPermissionsComposite.edit.button" ) ); //$NON-NLS-1$
        editButton.setLayoutData( editButtonGridData );
        editButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                editItemPermission();
            }
        } );
        editButton.setEnabled( false );

        deleteButton = new Button( buttonComposite, SWT.NONE );
        deleteButton.setText( Messages.getString( "ACIItemItemPermissionsComposite.delete.button" ) ); //$NON-NLS-1$
        deleteButton.setLayoutData( deleteButtonGridData );
        deleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                deleteItemPermission();
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
        ( ( GridData ) getLayoutData() ).heightHint = visible ? -1 : 0;
    }


    // ── INJECT THE CONNECTION CONTEXT ─────────────────────────────────────────
    /**
     * Stores the connection context for use by {@link ItemPermissionDialog}.
     *
     * @param context  the value context
     */
    public void setContext( ACIItemValueWithContext context )
    {
        this.context = context;
    }


    // ── POPULATE THE TABLE ────────────────────────────────────────────────────
    /**
     * Replaces the current list with {@code itemPermissions} and refreshes
     * the table viewer.
     *
     * @param itemPermissions  the item permissions to display
     */
    public void setItemPermissions( Collection<ItemPermission> itemPermissions )
    {
        itemPermissionWrappers.clear();

        for ( ItemPermission itemPermission : itemPermissions )
        {
            ItemPermissionWrapper itemPermissionWrapper = new ItemPermissionWrapper( itemPermission );

            itemPermissionWrappers.add( itemPermissionWrapper );
        }

        tableViewer.refresh();
    }


    // ── COLLECT THE CURRENT LIST ──────────────────────────────────────────────
    /**
     * Returns the item permissions currently in the list.
     *
     * @return the collection of item permissions
     */
    public Collection<ItemPermission> getItemPermissions()
    {
        Collection<ItemPermission> itemPermissions = new ArrayList<ItemPermission>();

        for ( ItemPermissionWrapper itemPermissionWrapper : itemPermissionWrappers )
        {
            itemPermissions.add( itemPermissionWrapper.itemPermission );
        }

        return itemPermissions;
    }


    // ── GET THE SELECTED WRAPPER ──────────────────────────────────────────────
    /**
     * Returns the {@link ItemPermissionWrapper} currently selected in the table
     * viewer, or {@code null} if nothing is selected.
     *
     * @return the selected wrapper, or {@code null}
     */
    private ItemPermissionWrapper getSelectedItemPermissionWrapper()
    {
        ItemPermissionWrapper itemPermissionWrapper = null;

        IStructuredSelection selection = ( IStructuredSelection ) tableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            Object element = selection.getFirstElement();
            if ( element instanceof ItemPermissionWrapper )
            {
                itemPermissionWrapper = ( ItemPermissionWrapper ) element;
            }
        }

        return itemPermissionWrapper;
    }


    // ── ADD AN ITEM PERMISSION ────────────────────────────────────────────────
    /**
     * Opens an {@link ItemPermissionDialog} to compose a new item permission
     * and adds it to the list if the user confirms.
     */
    private void addItemPermission()
    {
        ItemPermissionDialog dialog = new ItemPermissionDialog( getShell(), null, context );
        if ( dialog.open() == ItemPermissionDialog.OK && dialog.getItemPermission() != null )
        {
            ItemPermissionWrapper itemPermissionWrapper = new ItemPermissionWrapper( dialog.getItemPermission() );
            itemPermissionWrappers.add( itemPermissionWrapper );

            tableViewer.refresh();
        }
    }


    // ── EDIT THE SELECTED ITEM PERMISSION ─────────────────────────────────────
    /**
     * Opens an {@link ItemPermissionDialog} pre-filled with the selected item
     * permission and replaces the old wrapper with the updated one on confirmation.
     */
    private void editItemPermission()
    {
        ItemPermissionWrapper oldItemPermissionWrapper = getSelectedItemPermissionWrapper();
        if ( oldItemPermissionWrapper != null )
        {
            ItemPermissionDialog dialog = new ItemPermissionDialog( getShell(),
                oldItemPermissionWrapper.itemPermission, context );
            if ( dialog.open() == ItemPermissionDialog.OK )
            {
                oldItemPermissionWrapper.itemPermission = dialog.getItemPermission();
                tableViewer.refresh();
            }
        }
    }


    // ── DELETE THE SELECTED ITEM PERMISSION ───────────────────────────────────
    /**
     * Removes the selected item permission wrapper from the list without
     * prompting for confirmation.
     */
    private void deleteItemPermission()
    {
        ItemPermissionWrapper itemPermissionWrapper = getSelectedItemPermissionWrapper();
        if ( itemPermissionWrapper != null )
        {
            itemPermissionWrappers.remove( itemPermissionWrapper );
            tableViewer.refresh();
        }
    }


    // ── REACT TO SELECTION CHANGES ────────────────────────────────────────────
    /**
     * Updates the enabled state of the Edit and Delete buttons based on whether
     * a row is selected in the table viewer.
     */
    private void itemPermissionSelected()
    {
        ItemPermissionWrapper itemPermissionWrapper = getSelectedItemPermissionWrapper();

        if ( itemPermissionWrapper == null )
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
