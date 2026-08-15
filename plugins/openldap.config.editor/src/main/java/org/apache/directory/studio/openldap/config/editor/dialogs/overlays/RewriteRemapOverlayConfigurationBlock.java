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
import org.apache.directory.studio.openldap.config.editor.dialogs.RwmMappingDialog;
import org.apache.directory.studio.openldap.config.model.overlay.OlcRwmConfig;


// Like the Imperial construction crews assembling the rewrite-and-remap
// translation module onto the second Death Star — wiring up the mappings
// table where each row redirects one DN or attribute path to another, and
// installing the Add, Edit, and Delete controls so the operator can maintain
// the routing table at will — we build the RewriteRemap overlay configuration
// block that governs how the RWM overlay rewrites directory operations.
/**
 * This class implements the configuration block for the Rewrite/Remap (RWM)
 * overlay. We present a table of mapping strings with Add, Edit, and Delete
 * buttons, and we read/write that list to and from the {@link OlcRwmConfig}
 * model object on refresh and save.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RewriteRemapOverlayConfigurationBlock extends AbstractOverlayDialogConfigurationBlock<OlcRwmConfig>
{
    /** The mappings list */
    private List<String> mappings = new ArrayList<>();

    // UI widgets
    private TableViewer mappingsTableViewer;
    private Button addMappingButton;
    private Button editMappingButton;
    private Button deleteMappingButton;

    // Listeners
    private ISelectionChangedListener mappingsTableViewerSelectionChangedListener =  event ->
            deleteMappingButton.setEnabled( !mappingsTableViewer.getSelection().isEmpty() );

    private IDoubleClickListener mappingsTableViewerDoubleClickListener =  event -> editMappingButtonAction();

    private SelectionListener addMappingButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            RwmMappingDialog dialog = new RwmMappingDialog( addMappingButton.getShell(), browserConnection, "" );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String value = dialog.getValue();

                mappings.add( value );
                mappingsTableViewer.refresh();
                mappingsTableViewer.setSelection( new StructuredSelection( value ) );
            }
        }
    };
    private SelectionListener editMappingButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            editMappingButtonAction();
        }
    };
    private SelectionListener deleteMappingButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            StructuredSelection selection = ( StructuredSelection ) mappingsTableViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                String selectedAttribute = ( String ) selection.getFirstElement();

                mappings.remove( selectedAttribute );
                mappingsTableViewer.refresh();
            }
        }
    };


    // Like the construction crew initializing the RWM routing module with
    // a fresh empty mapping table — no translations yet, everything passes
    // through unchanged until the operator adds entries — we create the block
    // with a new empty OlcRwmConfig and an empty local mappings list.
    /**
     * Creates a new RewriteRemapOverlayConfigurationBlock with a fresh, empty
     * {@link OlcRwmConfig} as the backing model and an empty mappings list.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param connection the browser connection used by child dialogs for schema lookups
     */
    public RewriteRemapOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection connection )
    {
        super( dialog, connection );
        setOverlay( new OlcRwmConfig() );
    }


    // Like the crew installing a pre-configured RWM module that already has
    // a routing table from a previous deployment, we accept an existing
    // OlcRwmConfig and store it — falling back to a fresh empty one if null.
    /**
     * Creates a new RewriteRemapOverlayConfigurationBlock backed by the given
     * {@link OlcRwmConfig}. If {@code overlay} is {@code null} we create a
     * fresh default config instead.
     *
     * @param dialog the parent OverlayDialog that hosts this block
     * @param connection the browser connection used by child dialogs for schema lookups
     * @param overlay the existing RWM overlay config to edit, or {@code null}
     */
    public RewriteRemapOverlayConfigurationBlock( OverlayDialog dialog, IBrowserConnection connection,
        OlcRwmConfig overlay )
    {
        super( dialog, connection );

        if ( overlay == null )
        {
            setOverlay( new OlcRwmConfig() );
        }
        else
        {
            setOverlay( overlay );
        }
    }


    // Like the construction crew building the RWM module's control panel —
    // a scrollable mappings roster backed by a TableViewer and three action
    // buttons so the operator can add new translations, edit existing ones,
    // or remove ones that are no longer needed — we create the block content.
    /**
     * Creates the block content area with a "Mappings:" label, a
     * {@link TableViewer} showing the current mapping strings, and Add,
     * Edit, and Delete buttons wired to their respective listeners.
     *
     * @param parent the parent composite to attach our content to
     */
    public void createBlockContent( Composite parent )
    {
        Composite composite = BaseWidgetUtils.createColumnContainer( parent, 1, 1 );

        // Mappings
        BaseWidgetUtils.createLabel( composite, "Mappings:", 1 );
        Composite mappingsComposite = BaseWidgetUtils.createColumnContainer( composite, 2, 1 );

        // Mappings TableViewer
        mappingsTableViewer = new TableViewer( mappingsComposite );
        GridData tableViewerGridData = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 );
        tableViewerGridData.heightHint = 20;
        tableViewerGridData.widthHint = 100;
        mappingsTableViewer.getControl().setLayoutData( tableViewerGridData );
        mappingsTableViewer.setContentProvider( new ArrayContentProvider() );
        mappingsTableViewer.setInput( mappings );
        mappingsTableViewer.addSelectionChangedListener( mappingsTableViewerSelectionChangedListener );
        mappingsTableViewer.addDoubleClickListener( mappingsTableViewerDoubleClickListener );

        // Mapping Add Button
        addMappingButton = BaseWidgetUtils.createButton( mappingsComposite, "Add...", 1 );
        addMappingButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        addMappingButton.addSelectionListener( addMappingButtonSelectionListener );

        // Mapping Add Button
        editMappingButton = BaseWidgetUtils.createButton( mappingsComposite, "Edit...", 1 );
        editMappingButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        editMappingButton.addSelectionListener( editMappingButtonSelectionListener );

        // Mapping Delete Button
        deleteMappingButton = BaseWidgetUtils.createButton( mappingsComposite, "Delete", 1 );
        deleteMappingButton.setEnabled( false );
        deleteMappingButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        deleteMappingButton.addSelectionListener( deleteMappingButtonSelectionListener );
    }


    // Like the crew operating the module's routing-table editor — pulling
    // up the selected mapping entry in an RwmMappingDialog, letting the
    // operator change the translation rule, and then splicing the updated
    // value back into the same position in the list — we perform the edit.
    /**
     * Opens an {@link RwmMappingDialog} pre-populated with the currently
     * selected mapping string. If the dialog is confirmed, replaces the old
     * entry in the list with the new value and refreshes the table.
     */
    private void editMappingButtonAction()
    {
        StructuredSelection selection = ( StructuredSelection ) mappingsTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            String selectedMapping = ( String ) selection.getFirstElement();

            RwmMappingDialog dialog = new RwmMappingDialog( addMappingButton.getShell(),
                browserConnection, selectedMapping );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String value = dialog.getValue();

                int index = mappings.indexOf( selectedMapping );
                mappings.remove( selectedMapping );
                mappings.add( index, value );
                mappingsTableViewer.refresh();
                mappingsTableViewer.setSelection( new StructuredSelection( value ) );
            }
        }
    }


    // Like the crew pulling the current routing table out of the station's
    // configuration record and loading every translation rule into the
    // control panel's mappings list so the operator can see what's active,
    // we copy each overlay mapping value into our local list.
    /**
     * Refreshes the mappings list from the current {@link OlcRwmConfig},
     * clearing the local list and repopulating it from the overlay's
     * {@code olcRwmMap} values, then refreshing the table viewer.
     */
    public void refresh()
    {
        if ( overlay != null )
        {
            mappings.clear();

            List<String> olcRwmMap = overlay.getOlcRwmMap();

            if ( olcRwmMap != null )
            {
                for ( String value : olcRwmMap )
                {
                    mappings.add( value );
                }
            }

            mappingsTableViewer.refresh();
        }
    }


    // Like the crew writing the updated routing table back into the station's
    // configuration record — first clearing the old entries, then adding each
    // current mapping in order so the RWM module applies the translations in
    // the sequence the operator intended — we save all mappings to the overlay.
    /**
     * Saves the current mappings list back into the {@link OlcRwmConfig},
     * clearing any previous {@code olcRwmMap} values and adding each entry
     * from our local list in order.
     */
    public void save()
    {
        if ( overlay != null )
        {
            overlay.clearOlcRwmMap();

            for ( String mapping : mappings )
            {
                overlay.addOlcRwmMap( mapping );
            }
        }
    }
}
