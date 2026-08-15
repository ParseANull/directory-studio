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
package org.apache.directory.studio.openldap.config.editor.dialogs.overlays;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.apache.directory.studio.openldap.common.ui.dialogs.AttributeDialog;
import org.apache.directory.studio.openldap.config.editor.dialogs.AbstractOverlayDialogConfigurationBlock;
import org.apache.directory.studio.openldap.config.editor.dialogs.OverlayDialog;
import org.apache.directory.studio.openldap.config.editor.dialogs.ValueSortingValueDialog;
import org.apache.directory.studio.openldap.config.model.overlay.OlcValSortConfig;


// Like the Imperial construction crews assembling the value-sorting enforcement
// module onto the second Death Star — building the value-sorts roster where each
// row specifies how attribute values in search results should be ordered, and
// installing Add, Edit, and Delete controls so the operator can manage the
// sorting rules at will — we construct the ValueSorting overlay configuration block
// that governs how the ValSort overlay reorders multi-valued attribute results.
/**
 * This class implements the configuration block for the Value Sorting overlay.
 * We present a table of value-sort strings with Add, Edit, and Delete buttons,
 * and we read/write that list to and from the {@link OlcValSortConfig} model
 * object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ValueSortingOverlayConfigurationBlock extends
    AbstractOverlayDialogConfigurationBlock<OlcValSortConfig>
{
    /** The value sorts list */
    private List<String> valueSorts = new ArrayList<>();

    // UI widgets
    private TableViewer valueSortsTableViewer;
    private Button addValueSortButton;
    private Button editValueSortButton;
    private Button deleteValueSortButton;

    // Listeners
    private ISelectionChangedListener valueSortsTableViewerSelectionChangedListener =  event ->
        deleteValueSortButton.setEnabled( !valueSortsTableViewer.getSelection().isEmpty() );

    private IDoubleClickListener valueSortsTableViewerDoubleClickListener =  event -> editValueSortButtonAction();

    private SelectionListener addValueSortButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            ValueSortingValueDialog dialog = new ValueSortingValueDialog( addValueSortButton.getShell(),
                browserConnection, "" );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String value = dialog.getValue();

                valueSorts.add( value );
                valueSortsTableViewer.refresh();
                valueSortsTableViewer.setSelection( new StructuredSelection( value ) );
            }
        }
    };
    private SelectionListener editValueSortButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            editValueSortButtonAction();
        }
    };
    private SelectionListener deleteValueSortButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            StructuredSelection selection = ( StructuredSelection ) valueSortsTableViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                String selectedAttribute = ( String ) selection.getFirstElement();

                valueSorts.remove( selectedAttribute );
                valueSortsTableViewer.refresh();
            }
        }
    };


    // Like the construction crew initializing a fresh value-sorting module
    // with an empty sort-rules roster — no ordering specifications yet, all
    // attributes returned in their natural server order — we create the block
    // with a new empty OlcValSortConfig and an empty local valueSorts list.
    /**
     * Creates a new ValueSortingOverlayConfigurationBlock with a fresh, empty
     * {@link OlcValSortConfig} as the backing model and an empty value-sorts list.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param connection the browser connection used by child dialogs for schema lookups
     */
    public ValueSortingOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection connection )
    {
        super( dialog, connection );
        setOverlay( new OlcValSortConfig() );
    }


    // Like the crew installing a pre-configured value-sorting module that
    // already has sorting rules from a previous deployment, we accept an
    // existing OlcValSortConfig and store it — falling back to a fresh
    // empty one if null was passed.
    /**
     * Creates a new ValueSortingOverlayConfigurationBlock backed by the given
     * {@link OlcValSortConfig}. If {@code overlay} is {@code null} we create
     * a fresh default config instead.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param connection the browser connection used by child dialogs for schema lookups
     * @param overlay the existing value-sorting overlay config to edit, or {@code null}
     */
    public ValueSortingOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection connection,
        OlcValSortConfig overlay )
    {
        super( dialog, connection );

        if ( overlay == null )
        {
            setOverlay( new OlcValSortConfig() );
        }
        else
        {
            setOverlay( overlay );
        }
    }


    // Like the construction crew building the value-sorting module's control
    // panel — a scrollable value-sorts roster backed by a TableViewer and
    // three action buttons so the operator can add new sort rules, edit
    // existing ones, or remove rules that are no longer needed — we build
    // the block content widgets.
    /**
     * Creates the block content area with a "Value Sorts:" label, a
     * {@link TableViewer} showing the current value-sort strings, and Add,
     * Edit, and Delete buttons wired to their respective listeners.
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Value Sorts
        BaseWidgetUtils.createLabel( composite, "Value Sorts:", 1 );
        Composite valueSortsComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        // Value Sorts TableViewer
        valueSortsTableViewer = new TableViewer( valueSortsComposite );
        GridData tableViewerGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 );
        tableViewerGridData.heightHint = 20;
        tableViewerGridData.widthHint = 100;
        valueSortsTableViewer.getControl().setLayoutData( tableViewerGridData );
        valueSortsTableViewer.setContentProvider( new ArrayContentProvider() );
        valueSortsTableViewer.setInput( valueSorts );
        valueSortsTableViewer.addSelectionChangedListener( valueSortsTableViewerSelectionChangedListener );
        valueSortsTableViewer.addDoubleClickListener( valueSortsTableViewerDoubleClickListener );

        // Value Sort Add Button
        addValueSortButton = BaseWidgetUtils.createButton( valueSortsComposite, "Add...", 1 );
        addValueSortButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        addValueSortButton.addSelectionListener( addValueSortButtonSelectionListener );

        // Value Sort Add Button
        editValueSortButton = BaseWidgetUtils.createButton( valueSortsComposite, "Edit...", 1 );
        editValueSortButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        editValueSortButton.addSelectionListener( editValueSortButtonSelectionListener );

        // Value Sort Delete Button
        deleteValueSortButton = BaseWidgetUtils.createButton( valueSortsComposite, "Delete", 1 );
        deleteValueSortButton.setEnabled( false );
        deleteValueSortButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        deleteValueSortButton.addSelectionListener( deleteValueSortButtonSelectionListener );
    }


    // Like the crew operating the value-sorting module's sort-rule editor —
    // pulling the selected rule into a ValueSortingValueDialog, letting the
    // operator modify it, and then splicing the updated entry back into the
    // same position in the list so order is preserved — we perform the edit.
    /**
     * Opens a {@link ValueSortingValueDialog} pre-populated with the currently
     * selected value-sort string. If the dialog is confirmed, replaces the old
     * entry in the list at the same index with the new value and refreshes the table.
     */
    private void editValueSortButtonAction()
    {
        StructuredSelection selection = ( StructuredSelection ) valueSortsTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            String selectedValueSort = ( String ) selection.getFirstElement();

            ValueSortingValueDialog dialog = new ValueSortingValueDialog( addValueSortButton.getShell(),
                browserConnection, selectedValueSort );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String value = dialog.getValue();

                int index = valueSorts.indexOf( selectedValueSort );
                valueSorts.remove( selectedValueSort );
                valueSorts.add( index, value );
                valueSortsTableViewer.refresh();
                valueSortsTableViewer.setSelection( new StructuredSelection( value ) );
            }
        }
    }


    // Like the crew pulling the current value-sort rules out of the station's
    // configuration record and loading each sorting specification into the
    // control panel's roster so the operator can see what's currently active,
    // we copy each overlay value-sort entry into our local list.
    /**
     * Refreshes the value-sorts list from the current {@link OlcValSortConfig},
     * clearing the local list and repopulating it from the overlay's
     * {@code olcValSortAttr} values, then refreshing the table viewer.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            valueSorts.clear();

            List<String> olcValSortAttr = overlay.getOlcValSortAttr();

            if ( olcValSortAttr != null )
            {
                for ( String value : olcValSortAttr )
                {
                    valueSorts.add( value );
                }
            }

            valueSortsTableViewer.refresh();
        }
    }


    // Like the crew writing the updated sort rules back into the station's
    // configuration record — clearing the old entries first, then adding
    // each current value-sort rule in the order the operator arranged them
    // so the overlay applies sorts in the intended sequence — we save.
    /**
     * Saves the current value-sorts list back into the {@link OlcValSortConfig},
     * clearing any previous {@code olcValSortAttr} values and adding each entry
     * from our local list in order.
     */
    public void save()
    {
        if ( overlay != null )
        {
            overlay.clearOlcValSortAttr();

            for ( String valueSort : valueSorts )
            {
                overlay.addOlcValSortAttr( valueSort );
            }
        }
    }
}
