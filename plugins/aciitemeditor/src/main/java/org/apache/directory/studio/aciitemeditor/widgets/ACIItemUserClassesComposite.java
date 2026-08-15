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


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.directory.api.ldap.aci.ProtectedItem;
import org.apache.directory.api.ldap.aci.UserClass;
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.dialogs.MultiValuedDialog;
import org.apache.directory.studio.aciitemeditor.model.UserClassWrapper;
import org.apache.directory.studio.aciitemeditor.model.UserClassWrapperFactory;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;


// ── CLASS: ACIItemUserClassesComposite — ISB PERSONNEL-CATEGORY TABLE ─────────
// In a userFirst ACI directive the Grand Moff selects which user categories
// the directive applies to: allUsers, thisEntry, subtree, etc.
// Each category can optionally carry values (e.g., a subtree specification).
// This composite is that personnel-category table: 6 possible rows, checkbox,
// and an Edit button for categories that need values.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} presenting a fixed list of six {@link UserClassWrapper}
 * rows as a checkbox table.
 * Checked rows are included in the ACI item; editable rows open a
 * {@link MultiValuedDialog} via the Edit button.
 * Think of this as the ISB personnel-category table: check the categories that
 * apply, fill in values where needed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemUserClassesComposite extends Composite
{
    /** The context. */
    private ACIItemValueWithContext context;

    /** The inner composite for all the content */
    private Composite composite = null;

    /** The table viewer containing all user classes */
    private CheckboxTableViewer tableViewer = null;

    /** The edit button */
    private Button editButton = null;

    /** The possible user classes, used as input for the table viewer */
    private UserClassWrapper[] userClassWrappers = UserClassWrapperFactory.createUserClassWrappers();


    // ── CONSTRUCT THE USER-CLASSES TABLE ──────────────────────────────────────
    /**
     * Creates a new {@code ACIItemUserClassesComposite}.
     * Builds the checkbox table and the Edit / Select All / Deselect All /
     * Reverse Selection button panel.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemUserClassesComposite( Composite parent, int style )
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
     * Creates the two-column inner composite, label, checkbox table viewer,
     * and button panel.
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
        label.setText( Messages.getString( "ACIItemUserClassesComposite.description" ) ); //$NON-NLS-1$
        label.setLayoutData( labelGridData );

        createTable();
        createButtonComposite();
    }


    // ── BUILD THE CHECKBOX TABLE VIEWER ───────────────────────────────────────
    /**
     * Creates and configures the {@link CheckboxTableViewer} displaying all
     * user class categories and wires selection, check-state, and double-click
     * listeners.
     */
    private void createTable()
    {
        GridData tableGridData = new GridData();
        tableGridData.grabExcessHorizontalSpace = true;
        tableGridData.verticalAlignment = GridData.FILL;
        tableGridData.horizontalAlignment = GridData.FILL;

        Table table = new Table( composite, SWT.BORDER | SWT.CHECK );
        table.setHeaderVisible( false );
        table.setLayoutData( tableGridData );
        table.setLinesVisible( false );
        tableViewer = new CheckboxTableViewer( table );
        tableViewer.setContentProvider( new ArrayContentProvider() );
        tableViewer.setLabelProvider( new UserClassesLabelProvider() );
        tableViewer.setInput( userClassWrappers );

        tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged( SelectionChangedEvent event )
            {
                userClassSelected();
            }
        } );

        tableViewer.addCheckStateListener( new ICheckStateListener()
        {
            @Override
            public void checkStateChanged( CheckStateChangedEvent event )
            {
                userClassChecked();
            }
        } );

        tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            @Override
            public void doubleClick( DoubleClickEvent event )
            {
                if ( editButton.isEnabled() )
                {
                    editUserClass();
                }
            }
        } );
    }


    // ── BUILD THE BUTTON PANEL ────────────────────────────────────────────────
    /**
     * Creates the Edit, Select All, Deselect All, and Reverse Selection buttons.
     * Edit is disabled until an editable row is selected.
     */
    private void createButtonComposite()
    {
        GridData reverseSelectionButtonGridData = new GridData();
        reverseSelectionButtonGridData.horizontalAlignment = GridData.FILL;
        reverseSelectionButtonGridData.grabExcessHorizontalSpace = false;
        reverseSelectionButtonGridData.verticalAlignment = GridData.BEGINNING;
        reverseSelectionButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData deselectAllButtonGridData = new GridData();
        deselectAllButtonGridData.horizontalAlignment = GridData.FILL;
        deselectAllButtonGridData.grabExcessHorizontalSpace = false;
        deselectAllButtonGridData.verticalAlignment = GridData.BEGINNING;
        deselectAllButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData selectAllButtonGridData = new GridData();
        selectAllButtonGridData.horizontalAlignment = GridData.FILL;
        selectAllButtonGridData.grabExcessHorizontalSpace = false;
        selectAllButtonGridData.verticalAlignment = GridData.BEGINNING;
        selectAllButtonGridData.widthHint = Activator.getButtonWidth( this );

        GridData editButtonGridData = new GridData();
        editButtonGridData.horizontalAlignment = GridData.FILL;
        editButtonGridData.grabExcessHorizontalSpace = false;
        editButtonGridData.verticalAlignment = GridData.BEGINNING;
        editButtonGridData.widthHint = Activator.getButtonWidth( this );

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

        editButton = new Button( buttonComposite, SWT.NONE );
        editButton.setText( Messages.getString( "ACIItemUserClassesComposite.edit.button" ) ); //$NON-NLS-1$
        editButton.setLayoutData( editButtonGridData );

        editButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                editUserClass();
            }
        } );

        editButton.setEnabled( false );

        Button selectAllButton = new Button( buttonComposite, SWT.NONE );
        selectAllButton.setText( Messages.getString( "ACIItemUserClassesComposite.selectAll.button" ) ); //$NON-NLS-1$
        selectAllButton.setLayoutData( selectAllButtonGridData );

        selectAllButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                tableViewer.setCheckedElements( userClassWrappers );
                refreshTable();
            }
        } );

        Button deselectAllButton = new Button( buttonComposite, SWT.NONE );
        deselectAllButton.setText( Messages.getString( "ACIItemUserClassesComposite.deselectAll.button" ) ); //$NON-NLS-1$
        deselectAllButton.setLayoutData( deselectAllButtonGridData );

        deselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                tableViewer.setCheckedElements( new ProtectedItem[0] );
                refreshTable();
            }
        } );

        Button reverseSelectionButton = new Button( buttonComposite, SWT.NONE );
        reverseSelectionButton.setText( Messages.getString( "ACIItemUserClassesComposite.revert.buton" ) ); //$NON-NLS-1$
        reverseSelectionButton.setLayoutData( reverseSelectionButtonGridData );

        reverseSelectionButton.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent event )
            {
                List<Object> elements = new ArrayList<Object>();
                elements.addAll( Arrays.asList( userClassWrappers ) );
                elements.removeAll( Arrays.asList( tableViewer.getCheckedElements() ) );
                tableViewer.setCheckedElements( elements.toArray() );
                refreshTable();
            }
        } );
    }


    // ── CLASS: UserClassesLabelProvider — ERROR-ICON LABEL PROVIDER ───────────
    // If a row is checked but its value fails to parse the label provider shows
    // an error icon to draw the officer's attention.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@link LabelProvider} that shows an error icon for checked rows whose
     * user class fails to parse, and no icon otherwise.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class UserClassesLabelProvider extends LabelProvider
    {
        /**
         * Returns the error icon if the user class is checked and invalid.
         *
         * @param element  the element
         * @return         the error icon, or {@code null}
         */
        public Image getImage( Object element )
        {
            if ( element instanceof UserClassWrapper )
            {
                UserClassWrapper wrapper = ( UserClassWrapper ) element;

                if ( tableViewer.getChecked( wrapper ) )
                {
                    try
                    {
                        wrapper.getUserClass();
                    }
                    catch ( ParseException e )
                    {
                        return Activator.getDefault().getImage(
                            Messages.getString( "ACIItemUserClassesComposite.error.icon" ) ); //$NON-NLS-1$
                    }
                }
            }

            return null;
        }
    }


    // ── INJECT THE CONNECTION CONTEXT ─────────────────────────────────────────
    /**
     * Stores the connection context for use by value editors opened via Edit.
     *
     * @param context  the value context
     */
    public void setContext( ACIItemValueWithContext context )
    {
        this.context = context;
    }


    // ── POPULATE THE TABLE ────────────────────────────────────────────────────
    /**
     * Resets all checkboxes, then checks the rows corresponding to
     * {@code userClasses} and populates their values.
     *
     * @param userClasses  the user classes to display
     */
    public void setUserClasses( Collection<UserClass> userClasses )
    {
        // reset first
        for ( UserClassWrapper userClassWrapper : userClassWrappers )
        {
            tableViewer.setChecked( userClassWrapper, false );
        }

        for ( UserClass userClass : userClasses )
        {
            for ( UserClassWrapper userClassWrapper : userClassWrappers )
            {
                if ( userClassWrapper.getClazz() == userClass.getClass() )
                {
                    userClassWrapper.setUserClass( userClass );
                    tableViewer.setChecked( userClassWrapper, true );
                }
            }
        }

        refreshTable();
    }


    // ── COLLECT CHECKED USER CLASSES ──────────────────────────────────────────
    /**
     * Returns the collection of {@link UserClass} objects corresponding to
     * the currently checked rows.
     *
     * @return the checked user classes
     * @throws ParseException  if any checked wrapper's value fails to parse
     */
    public Collection<UserClass> getUserClasses() throws ParseException
    {
        Collection<UserClass> userClasses = new ArrayList<UserClass>();

        for ( UserClassWrapper userClassWrapper : userClassWrappers )
        {
            if ( tableViewer.getChecked( userClassWrapper ) )
            {
                UserClass userClass = userClassWrapper.getUserClass();
                userClasses.add( userClass );
            }
        }

        return userClasses;
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


    // ── GET THE SELECTED WRAPPER ──────────────────────────────────────────────
    /**
     * Returns the {@link UserClassWrapper} currently selected in the table
     * viewer, or {@code null} if nothing is selected.
     *
     * @return the selected wrapper, or {@code null}
     */
    private UserClassWrapper getSelectedUserClassWrapper()
    {
        IStructuredSelection selection = ( IStructuredSelection ) tableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            Object element = selection.getFirstElement();

            if ( element instanceof UserClassWrapper )
            {
                return ( UserClassWrapper ) element;
            }
        }

        return null;
    }


    // ── REACT TO SELECTION CHANGES ────────────────────────────────────────────
    /**
     * Enables or disables the Edit button based on whether the selected row
     * is editable.
     */
    private void userClassSelected()
    {
        UserClassWrapper userClassWrapper = getSelectedUserClassWrapper();

        if ( ( userClassWrapper == null ) || !userClassWrapper.isEditable() )
        {
            editButton.setEnabled( false );
        }
        else
        {
            editButton.setEnabled( true );
        }
    }


    // ── REACT TO CHECK-STATE CHANGES ──────────────────────────────────────────
    /**
     * Refreshes the table when a checkbox is toggled (to update error icons).
     */
    private void userClassChecked()
    {
        refreshTable();
    }


    // ── OPEN THE VALUE EDITOR ─────────────────────────────────────────────────
    /**
     * Opens a {@link MultiValuedDialog} for the selected user class wrapper
     * if it has an associated value editor, then refreshes the table.
     */
    private void editUserClass()
    {
        UserClassWrapper userClassWrapper = getSelectedUserClassWrapper();

        AbstractDialogStringValueEditor editor = userClassWrapper.getValueEditor();

        if ( editor != null )
        {
            MultiValuedDialog dialog = new MultiValuedDialog( getShell(), userClassWrapper.getDisplayName(),
                userClassWrapper.getValues(), context, editor );
            dialog.open();
            refreshTable();
        }
    }


    // ── REFRESH THE TABLE ─────────────────────────────────────────────────────
    /**
     * Refreshes the table viewer, causing labels (including error icons) to
     * be recomputed.
     */
    private void refreshTable()
    {
        tableViewer.refresh();
    }
}
