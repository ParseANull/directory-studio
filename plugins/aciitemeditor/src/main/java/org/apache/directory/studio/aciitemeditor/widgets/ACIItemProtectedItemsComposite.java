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
import org.apache.directory.studio.aciitemeditor.ACIItemValueWithContext;
import org.apache.directory.studio.aciitemeditor.Activator;
import org.apache.directory.studio.aciitemeditor.dialogs.MultiValuedDialog;
import org.apache.directory.studio.aciitemeditor.model.ProtectedItemWrapper;
import org.apache.directory.studio.aciitemeditor.model.ProtectedItemWrapperFactory;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.valueeditors.AbstractDialogStringValueEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
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


// ── CLASS: ACIItemProtectedItemsComposite — ISB RESOURCE-CATEGORY TABLE ───────
// The ISB itemFirst directive lists which resource categories are protected
// (entry, allUserAttributeTypes, attributeType, etc.).  The officer checks
// the applicable rows, then optionally clicks Edit to supply per-category values.
// ACIItemProtectedItemsComposite is that table: 12 possible rows, checkbox,
// and an Edit button for multi-valued categories.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * SWT {@link Composite} presenting a fixed list of twelve
 * {@link ProtectedItemWrapper} rows as a checkbox table.
 * Checked rows are included in the ACI item; editable rows open a
 * {@link MultiValuedDialog} (or a single-value cell editor) via the Edit button.
 * Think of this as the ISB resource-category table: check the categories,
 * fill in the values, the directive collects them all.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ACIItemProtectedItemsComposite extends Composite
{
    /** The context. */
    private ACIItemValueWithContext context;

    /** The inner composite for all the content */
    private Composite composite = null;

    /** The table viewer containing all protected items */
    private CheckboxTableViewer tableViewer = null;

    /** The edit button */
    private Button editButton = null;

    /** The possible protected items, used as input for the table viewer */
    private ProtectedItemWrapper[] protectedItemWrappers = ProtectedItemWrapperFactory.createProtectedItemWrappers();


    // ── CONSTRUCT THE PROTECTED-ITEMS TABLE ───────────────────────────────────
    /**
     * Creates a new {@code ACIItemProtectedItemsComposite}.
     * Builds the checkbox table and the Edit / Select All / Deselect All /
     * Reverse Selection button panel.
     *
     * @param parent  the parent composite
     * @param style   SWT style bits
     */
    public ACIItemProtectedItemsComposite( Composite parent, int style )
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
        label.setText( Messages.getString( "ACIItemProtectedItemsComposite.description" ) ); //$NON-NLS-1$
        label.setLayoutData( labelGridData );

        createTable();

        createButtonComposite();
    }


    // ── BUILD THE CHECKBOX TABLE VIEWER ───────────────────────────────────────
    /**
     * Creates and configures the {@link CheckboxTableViewer} displaying all
     * protected-item categories and wires selection, check-state, and
     * double-click listeners.
     */
    private void createTable()
    {
        GridData tableGridData = new GridData();
        tableGridData.grabExcessHorizontalSpace = true;
        tableGridData.verticalAlignment = GridData.FILL;
        tableGridData.horizontalAlignment = GridData.FILL;
        //tableGridData.heightHint = 100;

        Table table = new Table( composite, SWT.BORDER | SWT.CHECK );
        table.setHeaderVisible( false );
        table.setLayoutData( tableGridData );
        table.setLinesVisible( false );
        tableViewer = new CheckboxTableViewer( table );
        tableViewer.setContentProvider( new ArrayContentProvider() );
        tableViewer.setLabelProvider( new ProtectedItemsLabelProvider() );
        tableViewer.setInput( protectedItemWrappers );

        tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                protectedItemSelected();
            }
        } );
        tableViewer.addCheckStateListener( new ICheckStateListener()
        {
            public void checkStateChanged( CheckStateChangedEvent event )
            {
                protectedItemChecked();
            }
        } );
        tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                if ( editButton.isEnabled() )
                {
                    editProtectedItem();
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
        editButton.setText( Messages.getString( "ACIItemProtectedItemsComposite.edit.button" ) ); //$NON-NLS-1$
        editButton.setLayoutData( editButtonGridData );
        editButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                editProtectedItem();
            }
        } );
        editButton.setEnabled( false );

        Button selectAllButton = new Button( buttonComposite, SWT.NONE );
        selectAllButton.setText( Messages.getString( "ACIItemProtectedItemsComposite.selectAll.button" ) ); //$NON-NLS-1$
        selectAllButton.setLayoutData( selectAllButtonGridData );
        selectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                tableViewer.setCheckedElements( protectedItemWrappers );
                refreshTable();
            }
        } );

        Button deselectAllButton = new Button( buttonComposite, SWT.NONE );
        deselectAllButton.setText( Messages.getString( "ACIItemProtectedItemsComposite.deselectAll.button" ) ); //$NON-NLS-1$
        deselectAllButton.setLayoutData( deselectAllButtonGridData );
        deselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                tableViewer.setCheckedElements( new ProtectedItem[0] );
                refreshTable();
            }
        } );

        Button reverseSelectionButton = new Button( buttonComposite, SWT.NONE );
        reverseSelectionButton.setText( Messages.getString( "ACIItemProtectedItemsComposite.revers.button" ) ); //$NON-NLS-1$
        reverseSelectionButton.setLayoutData( reverseSelectionButtonGridData );
        reverseSelectionButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                List<Object> elements = new ArrayList<Object>();
                elements.addAll( Arrays.asList( protectedItemWrappers ) );
                elements.removeAll( Arrays.asList( tableViewer.getCheckedElements() ) );
                tableViewer.setCheckedElements( elements.toArray() );
                refreshTable();
            }
        } );

    }

    // ── CLASS: ProtectedItemsLabelProvider — ERROR-ICON LABEL PROVIDER ────────
    // If a row is checked but its value is invalid the label provider shows
    // an error icon so the officer knows which rows need attention.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@link LabelProvider} that shows an error icon for checked rows whose
     * protected item fails to parse, and no icon otherwise.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class ProtectedItemsLabelProvider extends LabelProvider
    {

        /**
         * Returns the error icon if the protected item is checked and invalid.
         *
         * @param element  the element
         * @return         the error icon, or {@code null}
         */
        public Image getImage( Object element )
        {
            if ( element instanceof ProtectedItemWrapper )
            {
                ProtectedItemWrapper wrapper = ( ProtectedItemWrapper ) element;
                if ( tableViewer.getChecked( wrapper ) )
                {
                    try
                    {
                        wrapper.getProtectedItem();
                    }
                    catch ( ParseException e )
                    {
                        return Activator.getDefault().getImage(
                            Messages.getString( "ACIItemProtectedItemsComposite.error.icon" ) ); //$NON-NLS-1$
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
     * {@code protectedItems} and populates their values.
     *
     * @param protectedItems  the protected items to display
     */
    public void setProtectedItems( Collection<ProtectedItem> protectedItems )
    {
        // reset first
        for ( ProtectedItemWrapper protectedItemWrapper : protectedItemWrappers )
        {
            tableViewer.setChecked( protectedItemWrapper, false );
        }

        for ( ProtectedItem item : protectedItems )
        {
            for ( ProtectedItemWrapper protectedItemWrapper : protectedItemWrappers )
            {
                if ( protectedItemWrapper.getClazz() == item.getClass() )
                {
                    protectedItemWrapper.setProtectedItem( item );
                    tableViewer.setChecked( protectedItemWrapper, true );
                }
            }
        }

        refreshTable();
    }


    // ── COLLECT CHECKED PROTECTED ITEMS ───────────────────────────────────────
    /**
     * Returns the collection of {@link ProtectedItem} objects corresponding to
     * the currently checked rows.
     *
     * @return the checked protected items
     * @throws ParseException  if any checked wrapper's value fails to parse
     */
    public Collection<ProtectedItem> getProtectedItems() throws ParseException
    {

        Collection<ProtectedItem> protectedItems = new ArrayList<ProtectedItem>();

        for ( ProtectedItemWrapper protectedItemWrapper : protectedItemWrappers )
        {
            if ( tableViewer.getChecked( protectedItemWrapper ) )
            {
                ProtectedItem protectedItem = protectedItemWrapper.getProtectedItem();
                protectedItems.add( protectedItem );
            }
        }

        return protectedItems;
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


    // ── GET THE SELECTED WRAPPER ──────────────────────────────────────────────
    /**
     * Returns the {@link ProtectedItemWrapper} currently selected in the table
     * viewer, or {@code null} if nothing is selected.
     *
     * @return the selected wrapper, or {@code null}
     */
    private ProtectedItemWrapper getSelectedProtectedItemWrapper()
    {
        ProtectedItemWrapper protectedItemWrapper = null;

        IStructuredSelection selection = ( IStructuredSelection ) tableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            Object element = selection.getFirstElement();
            if ( element instanceof ProtectedItemWrapper )
            {
                protectedItemWrapper = ( ProtectedItemWrapper ) element;
            }
        }

        return protectedItemWrapper;
    }


    // ── REACT TO SELECTION CHANGES ────────────────────────────────────────────
    /**
     * Enables or disables the Edit button based on whether the selected row
     * is editable.
     */
    private void protectedItemSelected()
    {
        ProtectedItemWrapper protectedItemWrapper = getSelectedProtectedItemWrapper();

        if ( protectedItemWrapper == null || !protectedItemWrapper.isEditable() )
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
    private void protectedItemChecked()
    {
        refreshTable();
    }


    // ── OPEN THE VALUE EDITOR ─────────────────────────────────────────────────
    /**
     * Opens the appropriate value editor (multi-valued dialog or single-value
     * cell editor) for the selected protected item wrapper, then refreshes the table.
     */
    private void editProtectedItem()
    {
        ProtectedItemWrapper protectedItemWrapper = getSelectedProtectedItemWrapper();

        AbstractDialogStringValueEditor valueEditor = protectedItemWrapper.getValueEditor();
        if ( valueEditor != null )
        {
            if ( protectedItemWrapper.isMultivalued() )
            {
                MultiValuedDialog dialog = new MultiValuedDialog( getShell(), protectedItemWrapper.getDisplayName(),
                    protectedItemWrapper.getValues(), context, valueEditor );
                dialog.open();
                refreshTable();
            }
            else
            {
                List<String> values = protectedItemWrapper.getValues();
                String oldValue = values.isEmpty() ? null : values.get( 0 );
                if ( oldValue == null )
                {
                    oldValue = ""; //$NON-NLS-1$
                }

                IAttribute attribute = new Attribute( context.getEntry(), "" ); //$NON-NLS-1$
                IValue value = new Value( attribute, oldValue ); //$NON-NLS-1$
                Object oldRawValue = valueEditor.getRawValue( value ); //$NON-NLS-1$

                CellEditor cellEditor = valueEditor.getCellEditor();
                cellEditor.setValue( oldRawValue );
                cellEditor.activate();
                Object newRawValue = cellEditor.getValue();

                if ( newRawValue != null )
                {
                    String newValue = ( String ) valueEditor.getStringOrBinaryValue( newRawValue );

                    values.clear();
                    values.add( newValue );
                    tableViewer.refresh();
                }
            }
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
