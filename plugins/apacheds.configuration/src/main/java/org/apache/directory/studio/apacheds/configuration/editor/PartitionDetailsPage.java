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
package org.apache.directory.studio.apacheds.configuration.editor;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.server.config.beans.IndexBean;
import org.apache.directory.server.config.beans.JdbmIndexBean;
import org.apache.directory.server.config.beans.JdbmPartitionBean;
import org.apache.directory.server.config.beans.MavibotIndexBean;
import org.apache.directory.server.config.beans.MavibotPartitionBean;
import org.apache.directory.server.config.beans.PartitionBean;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.apache.directory.studio.apacheds.configuration.dialogs.AttributeValueDialog;
import org.apache.directory.studio.apacheds.configuration.dialogs.JdbmIndexDialog;
import org.apache.directory.studio.apacheds.configuration.dialogs.MavibotIndexDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: PartitionDetailsPage — Imperial Vault Chief's Inspection Dossier ──
// When the registry master selects a vault from the left-hand roster, the
// vault chief opens up the full inspection dossier on the right: vault ID,
// suffix DN, root context entry, storage-engine specifics, and search indexes.
// This class is the right-hand details panel of the master/details split —
// it shows everything about whichever partition the user has selected.
// ──────────────────────────────────────────────────────────────────────────
/**
 * The details panel displayed on the right side of the Partitions master/details
 * layout when the user selects a partition in the master list.
 * Implements the Eclipse Forms {@link IDetailsPage} contract so the
 * {@link PartitionsMasterDetailsBlock} can wire it in automatically.
 * Think of this class as the Imperial vault chief's full inspection dossier:
 * every configurable aspect of one partition — ID, suffix, context entry,
 * storage type, and indexes — laid out in separate sections for easy review.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PartitionDetailsPage implements IDetailsPage
{
    /** The class instance */
    private PartitionDetailsPage instance;

    /** The associated Master Details Block */
    private PartitionsMasterDetailsBlock masterDetailsBlock;

    /** The partition wrapper */
    private PartitionWrapper partitionWrapper;

    /** The partition specific details block */
    private PartitionSpecificDetailsBlock partitionSpecificDetailsBlock;

    /** The Context Entry */
    private Entry contextEntry;

    /** The Indexes List */
    private List<IndexBean> indexesList;

    // UI fields
    private Composite parentComposite;
    private FormToolkit toolkit;
    private Composite partitionSpecificDetailsComposite;
    private Section specificSettingsSection;
    private Composite specificSettingsSectionComposite;
    private ComboViewer partitionTypeComboViewer;
    private Text idText;
    private Text suffixText;
    private Button synchOnWriteCheckbox;
    private Button autoGenerateContextEntryCheckbox;
    private TableViewer contextEntryTableViewer;
    private Button contextEntryAddButton;
    private Button contextEntryEditButton;
    private Button contextEntryDeleteButton;
    private TableViewer indexesTableViewer;
    private Button indexesAddButton;
    private Button indexesEditButton;
    private Button indexesDeleteButton;

    // Listeners
    /** The Text Modify Listener */
    private ModifyListener textModifyListener = event ->
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        };

    private ModifyListener suffixTextModifyListener = event -> autoGenerateContextEntry();

    /** The Checkbox Selection Listener */
    private SelectionListener checkboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        }
    };

    private SelectionListener autoGenerateContextEntryCheckboxSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            autoGenerateContextEntry();
            updateContextEntryEnableState();
        }
    };

    /** The Selection Changed Listener for the Context Entry Table Viewer */
    private ISelectionChangedListener contextEntryTableViewerSelectionListener = event -> updateContextEntryEnableState();

    /** The Double Click Listener for the Indexed Attributes Table Viewer */
    private IDoubleClickListener contextEntryTableViewerDoubleClickListener = event -> editSelectedContextEntry();

    /** The Listener for the Add button of the Context Entry Section */
    private SelectionListener contextEntryAddButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            AttributeValueDialog dialog = new AttributeValueDialog( new AttributeValueObject( "", "" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            if ( AttributeValueDialog.OK == dialog.open() && dialog.isDirty() )
            {
                AttributeValueObject newAttributeValueObject = dialog.getAttributeValueObject();
                Attribute attribute = contextEntry.get( newAttributeValueObject.getAttribute() );

                if ( attribute != null )
                {
                    try
                    {
                        attribute.add( newAttributeValueObject.getValue() );
                    }
                    catch ( LdapInvalidAttributeValueException liave )
                    {
                        // Will never occur
                    }
                }
                else
                {
                    try
                    {
                        contextEntry.put( new DefaultAttribute( newAttributeValueObject.getAttribute(),
                            newAttributeValueObject.getValue() ) );
                    }
                    catch ( LdapException e1 )
                    {
                        // Will never occur
                    }
                }

                contextEntryTableViewer.refresh();
                resizeContextEntryTableColumnsToFit();
                masterDetailsBlock.setEditorDirty();
                //                dirty = true; TODO
                commit( true );
            }
        }
    };

    private ISelectionChangedListener partitionTypeComboViewerSelectionChangedListener = event ->
        {
            PartitionType type = ( PartitionType ) ( ( StructuredSelection ) partitionTypeComboViewer.getSelection() )
                .getFirstElement();

            if ( ( partitionWrapper != null ) && ( partitionWrapper.getPartition() != null ) )
            {
                PartitionBean partition = partitionWrapper.getPartition();

                // Only change the type if it's a different one
                if ( type != PartitionType.fromPartition( partition ) )
                {
                    switch ( type )
                    {
                        case JDBM:
                            JdbmPartitionBean newJdbmPartition = new JdbmPartitionBean();
                            copyPartitionProperties( partition, newJdbmPartition );
                            partitionWrapper.setPartition( newJdbmPartition );
                            break;
                        case MAVIBOT:
                            MavibotPartitionBean newMavibotPartition = new MavibotPartitionBean();
                            copyPartitionProperties( partition, newMavibotPartition );
                            partitionWrapper.setPartition( newMavibotPartition );
                            break;
                        default:
                            break;
                    }

                    refresh();
                    setEditorDirty();
                }
            }
        };

    /** The Listener for the Edit button of the Context Entry Section */
    private SelectionListener contextEntryEditButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            editSelectedContextEntry();
        }
    };

    /** The Listener for the Delete button of the Context Entry Section */
    private SelectionListener contextEntryDeleteButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            StructuredSelection selection = ( StructuredSelection ) contextEntryTableViewer.getSelection();
            if ( !selection.isEmpty() )
            {
                AttributeValueObject attributeValueObject = ( AttributeValueObject ) selection.getFirstElement();

                Attribute attribute = contextEntry.get( attributeValueObject.getAttribute() );
                if ( attribute != null )
                {
                    attribute.remove( attributeValueObject.getValue() );
                    contextEntryTableViewer.refresh();
                    resizeContextEntryTableColumnsToFit();
                    masterDetailsBlock.setEditorDirty();
                    //                    dirty = true; TODO
                    commit( true );
                }
            }
        }
    };

    /** The Selection Changed Listener for the Indexed Attributes Table Viewer */
    private ISelectionChangedListener indexedAttributesTableViewerListener = event ->
        {
            indexesEditButton.setEnabled( !event.getSelection().isEmpty() );
            indexesDeleteButton.setEnabled( !event.getSelection().isEmpty() );
        };

    /** The Double Click Listener for the Indexed Attributes Table Viewer */
    private IDoubleClickListener indexedAttributesTableViewerDoubleClickListener = event -> editSelectedIndex();

    /** The Listener for the Add button of the Indexed Attributes Section */
    private SelectionListener indexedAttributeAddButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            addNewIndex();
        }


        /**
         * Adds a new index and opens the index dialog.
         */
        private void addNewIndex()
        {
            PartitionType partitionType = ( PartitionType ) ( ( StructuredSelection ) partitionTypeComboViewer
                .getSelection() ).getFirstElement();

            if ( partitionType != null )
            {
                IndexBean newIndex = null;

                // JDBM partition
                if ( partitionType == PartitionType.JDBM )
                {
                    JdbmIndexBean newJdbmIndex = new JdbmIndexBean();
                    newJdbmIndex.setIndexAttributeId( "" ); //$NON-NLS-1$
                    newJdbmIndex.setIndexCacheSize( 100 );

                    JdbmIndexDialog dialog = new JdbmIndexDialog( newJdbmIndex );

                    if ( JdbmIndexDialog.OK == dialog.open() )
                    {
                        newIndex = dialog.getIndex();
                    }
                    else
                    {
                        // Cancel
                        return;
                    }
                }
                // Mavibot Partition
                else if ( partitionType == PartitionType.MAVIBOT )
                {
                    MavibotIndexBean newMavibotIndex = new MavibotIndexBean();
                    newMavibotIndex.setIndexAttributeId( "" ); //$NON-NLS-1$

                    MavibotIndexDialog dialog = new MavibotIndexDialog( newMavibotIndex );

                    if ( MavibotIndexDialog.OK == dialog.open() )
                    {
                        newIndex = dialog.getIndex();
                    }
                    else
                    {
                        // Cancel
                        return;
                    }
                }

                // Checking the new index
                if ( newIndex != null )
                {
                    indexesList.add( newIndex );
                    indexesTableViewer.refresh();
                    indexesTableViewer.setSelection( new StructuredSelection( newIndex ) );
                    masterDetailsBlock.setEditorDirty();
                }
            }
        }
    };

    /** The Listener for the Edit button of the Indexed Attributes Section */
    private SelectionListener indexedAttributeEditButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            editSelectedIndex();
        }
    };

    /** The Listener for the Delete button of the Indexed Attributes Section */
    private SelectionListener indexedAttributeDeleteButtonListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            deleteSelectedIndex();
        }


        /**
         * Deletes the selected index in the indexes table viewer
         */
        private void deleteSelectedIndex()
        {
            StructuredSelection selection = ( StructuredSelection ) indexesTableViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                IndexBean selectedIndex = ( IndexBean ) selection.getFirstElement();

                if ( MessageDialog
                    .openConfirm( indexesDeleteButton.getShell(),
                        Messages.getString( "PartitionDetailsPage.ConfirmDelete" ), //$NON-NLS-1$
                        NLS.bind(
                            Messages.getString( "PartitionDetailsPage.AreYouSureDeleteIndex" ), selectedIndex.getIndexAttributeId() ) ) ) //$NON-NLS-1$
                {
                    indexesList.remove( selectedIndex );
                    indexesTableViewer.refresh();
                    masterDetailsBlock.setEditorDirty();
                }
            }
        }
    };


    // ── Vault Chief Receives Assignment from the Registry Master ─────────────
    // The registry master hands the vault chief their badge and a reference to
    // the master list they'll report back to whenever the vault record changes.
    // We store the self-reference (used by specific details blocks to call back
    // to us) and the master-details block reference for dirty-marking.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PartitionDetailsPage} and stores a reference
     * to the owning {@link PartitionsMasterDetailsBlock}.
     * We also capture a self-reference so that concrete
     * {@link PartitionSpecificDetailsBlock} implementations can call back
     * to this page when they need to flag a change.
     *
     * <p>For example — the vault chief gets their briefing:</p>
     * <pre>
     *   The registry master walks the new chief to their desk, hands them
     *   a comms link back to the master registry, and assigns them to stand
     *   by until a vault is selected from the list on the left.
     * </pre>
     *
     * @param pmdb  the {@link PartitionsMasterDetailsBlock} that owns this page
     */
    public PartitionDetailsPage( PartitionsMasterDetailsBlock pmdb )
    {
        instance = this;
        masterDetailsBlock = pmdb;
    }


    // ── Vault Chief Lays Out the Full Inspection Dossier ────────────────────
    // The chief spreads four documents across the desk in order: the general
    // ID sheet, the root context entry record, the storage-engine spec sheet,
    // and the search index registry — everything needed to fully inspect a vault.
    // We call four section-creation helpers in sequence, each building one
    // visual section inside the provided parent composite.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates the full UI content for this details page inside the given composite.
     * Calls four helper methods in sequence to build the General Details, Context Entry,
     * Partition-Specific Settings, and Indexes sections that together describe one partition.
     *
     * <p>For example — the full dossier laid out:</p>
     * <pre>
     *   Section 1 : General Details (ID, suffix, type, sync-on-write)
     *   Section 2 : Context Entry   (root LDAP entry attribute/value pairs)
     *   Section 3 : Specific Settings (JDBM or Mavibot knobs)
     *   Section 4 : Indexed Attributes (search index list with Add/Edit/Delete)
     * </pre>
     *
     * @param parent  the composite provided by the Eclipse Forms framework
     */
    public void createContents( Composite parent )
    {
        this.parentComposite = parent;
        parent.setLayout( new GridLayout() );

        createGeneralDetailsSection( parent, toolkit );
        createContextEntrySection( parent, toolkit );
        createPartitionSpecificSettingsSection( parent, toolkit );
        createIndexesSection( parent, toolkit );
    }


    // ── Vault ID and Classification Panel Built ──────────────────────────────
    // The first page of the dossier covers the basics: what type of storage
    // engine powers this vault, its short name, its LDAP root DN, and whether
    // writes are synced to disk immediately.
    // We create a two-column grid section containing a type combo, ID text field,
    // suffix text field, and a sync-on-write checkbox.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "General Details" section containing the core partition properties.
     * This section lets the user choose the storage type (JDBM or Mavibot),
     * set the partition ID and suffix DN, and toggle sync-on-write behavior.
     *
     * <p>For example — the ID sheet filled in:</p>
     * <pre>
     *   Partition Type : [JDBM v]
     *   Id             : [example          ]
     *   Suffix         : [dc=example,dc=com]
     *   [X] Synchronization on Write
     * </pre>
     *
     * @param parent   the parent composite (the details page body)
     * @param toolkit  the form toolkit for creating styled widgets
     */
    private void createGeneralDetailsSection( Composite parent, FormToolkit toolkit )
    {
        Section section = toolkit.createSection( parent, Section.DESCRIPTION | Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( Messages.getString( "PartitionDetailsPage.PartitionsGeneralDetails" ) ); //$NON-NLS-1$
        section.setDescription( Messages.getString( "PartitionDetailsPage.SetPropertiesOfPartition" ) ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite client = toolkit.createComposite( section );
        toolkit.paintBordersFor( client );
        GridLayout glayout = new GridLayout( 2, false );
        client.setLayout( glayout );
        section.setClient( client );

        // Type
        toolkit.createLabel( client, "Partition Type:" );
        Combo partitionTypeCombo = new Combo( client, SWT.READ_ONLY | SWT.SINGLE );
        partitionTypeCombo.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        partitionTypeComboViewer = new ComboViewer( partitionTypeCombo );
        partitionTypeComboViewer.setContentProvider( new ArrayContentProvider() );
        partitionTypeComboViewer.setInput( new Object[]
            { PartitionType.JDBM, PartitionType.MAVIBOT } );

        // ID
        toolkit.createLabel( client, Messages.getString( "PartitionDetailsPage.Id" ) ); //$NON-NLS-1$
        idText = toolkit.createText( client, "" ); //$NON-NLS-1$
        idText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Suffix
        toolkit.createLabel( client, "Suffix:" ); //$NON-NLS-1$
        suffixText = toolkit.createText( client, "" ); //$NON-NLS-1$
        suffixText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Synchronisation On Write
        synchOnWriteCheckbox = toolkit.createButton( client,
            Messages.getString( "PartitionDetailsPage.SynchronizationOnWrite" ), SWT.CHECK ); //$NON-NLS-1$
        synchOnWriteCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
    }


    // ── Root Context Entry Record Panel Constructed ───────────────────────────
    // Every vault needs a root entry — the top-level LDAP object that anchors
    // the whole tree. This section lets the chief view and edit those root
    // attribute/value pairs, or auto-generate them from the suffix DN.
    // We create a table viewer with Attribute and Value columns, plus Add/Edit/Delete
    // buttons and an auto-generate checkbox that drives the whole thing.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Context Entry" section for viewing and editing the partition's
     * root LDAP entry attribute/value pairs.
     * The "Auto Generate" checkbox, when ticked, derives the context entry from
     * the suffix DN automatically, greying out the table and buttons.
     *
     * <p>For example — the vault's founding document drawn up:</p>
     * <pre>
     *   [X] Auto-generate context entry from suffix DN
     *   | Attribute  | Value         |   [Add]
     *   | objectClass| domain        |   [Edit]
     *   | dc         | example       |   [Delete]
     * </pre>
     *
     * @param parent   the parent composite (the details page body)
     * @param toolkit  the form toolkit for creating styled widgets
     */
    private void createContextEntrySection( Composite parent, FormToolkit toolkit )
    {
        Section section = toolkit.createSection( parent, Section.DESCRIPTION | Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Context Entry" ); //$NON-NLS-1$
        section.setDescription( "Set the attribute/value pairs for the Context Entry of the partition." ); //$NON-NLS-1$
        section.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite client = toolkit.createComposite( section );
        toolkit.paintBordersFor( client );
        client.setLayout( new GridLayout( 2, false ) );
        section.setClient( client );

        // Auto Generate Context Entry Checkbox
        autoGenerateContextEntryCheckbox = toolkit.createButton( client,
            Messages.getString( "PartitionDetailsPage.AutoGenerateContextEntryFromSuffixDn" ), //$NON-NLS-1$
            SWT.CHECK );
        autoGenerateContextEntryCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 3, 1 ) );

        // Context Entry Table Viewer
        Table contextEntryTable = toolkit.createTable( client, SWT.NONE );
        GridData gd = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 3 );
        gd.heightHint = 62;
        gd.widthHint = 50;
        contextEntryTable.setLayoutData( gd );
        TableColumn idColumn = new TableColumn( contextEntryTable, SWT.LEFT, 0 );
        idColumn.setText( Messages.getString( "PartitionDetailsPage.Attribute" ) ); //$NON-NLS-1$
        idColumn.setWidth( 100 );
        TableColumn valueColumn = new TableColumn( contextEntryTable, SWT.LEFT, 1 );
        valueColumn.setText( Messages.getString( "PartitionDetailsPage.Value" ) ); //$NON-NLS-1$
        valueColumn.setWidth( 100 );
        contextEntryTable.setHeaderVisible( true );
        contextEntryTableViewer = new TableViewer( contextEntryTable );
        contextEntryTableViewer.setContentProvider( new IStructuredContentProvider()
        {
            public Object[] getElements( Object inputElement )
            {
                List<AttributeValueObject> elements = new ArrayList<>();
                Entry entry = ( Entry ) inputElement;

                Iterator<Attribute> attributes = entry.iterator();
                while ( attributes.hasNext() )
                {
                    Attribute attribute = attributes.next();

                    Iterator<Value> values = attribute.iterator();
                    while ( values.hasNext() )
                    {
                        Value value = values.next();
                        elements.add( new AttributeValueObject( attribute.getId(), value.getString() ) );
                    }
                }

                return elements.toArray();
            }


            @Override
            public void dispose()
            {
            }


            @Override
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
            {
            }
        } );
        contextEntryTableViewer.setLabelProvider( new ITableLabelProvider()
        {
            public String getColumnText( Object element, int columnIndex )
            {
                if ( element != null )
                {
                    switch ( columnIndex )
                    {
                        case 0:
                            return ( ( AttributeValueObject ) element ).getAttribute();
                        case 1:
                            return ( ( AttributeValueObject ) element ).getValue();
                        default:
                            break;
                    }
                }

                return null;
            }


            public Image getColumnImage( Object element, int columnIndex )
            {
                return null;
            }


            public void addListener( ILabelProviderListener listener )
            {
            }


            public void dispose()
            {
            }


            public boolean isLabelProperty( Object element, String property )
            {
                return false;
            }


            public void removeListener( ILabelProviderListener listener )
            {
            }
        } );

        GridData buttonsGD = new GridData( SWT.FILL, SWT.BEGINNING, false, false );
        buttonsGD.widthHint = IDialogConstants.BUTTON_WIDTH;

        // Context Entry Add Button
        contextEntryAddButton = toolkit.createButton( client,
            Messages.getString( "PartitionDetailsPage.Add" ), SWT.PUSH ); //$NON-NLS-1$
        contextEntryAddButton.setLayoutData( buttonsGD );

        // Context Entry Edit Button
        contextEntryEditButton = toolkit.createButton( client,
            Messages.getString( "PartitionDetailsPage.Edit" ), SWT.PUSH ); //$NON-NLS-1$
        contextEntryEditButton.setEnabled( false );
        contextEntryEditButton.setLayoutData( buttonsGD );

        // Context Entry Delete Button
        contextEntryDeleteButton = toolkit.createButton( client,
            Messages.getString( "PartitionDetailsPage.Delete" ), SWT.PUSH ); //$NON-NLS-1$
        contextEntryDeleteButton.setEnabled( false );
        contextEntryDeleteButton.setLayoutData( buttonsGD );
    }


    // ── Context Entry Controls Enabled or Greyed Out ─────────────────────────
    // If the "auto-generate" order is in effect, the vault chief can't manually
    // edit the root record — those controls are locked. If the order is lifted,
    // the buttons re-enable, but only the Delete and Edit become active if
    // something is actually selected in the table.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Updates the enabled/disabled state of the context entry table and its buttons
     * based on whether auto-generation is active and whether a row is selected.
     * When "Auto Generate" is checked, everything is locked; when unchecked,
     * Add is always enabled and Edit/Delete only light up when a row is selected.
     */
    private void updateContextEntryEnableState()
    {
        contextEntryTableViewer.getTable().setEnabled( !autoGenerateContextEntryCheckbox.getSelection() );
        contextEntryAddButton.setEnabled( !autoGenerateContextEntryCheckbox.getSelection() );
        contextEntryEditButton.setEnabled( ( !autoGenerateContextEntryCheckbox.getSelection() )
            && ( !contextEntryTableViewer.getSelection().isEmpty() ) );
        contextEntryDeleteButton.setEnabled( ( !autoGenerateContextEntryCheckbox.getSelection() )
            && ( !contextEntryTableViewer.getSelection().isEmpty() ) );
    }


    // ── Storage Engine Spec Sheet Section Scaffolded ─────────────────────────
    // The vault's tech spec section is a collapsible frame that will be filled
    // by whichever specialist panel matches the selected storage engine type.
    // Right now we just erect the frame; the actual specialist content is
    // swapped in later by updatePartitionSpecificSettingsSection().
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the collapsible "Partition Specific Settings" section shell.
     * The section starts empty; its inner composite is populated (and replaced
     * when the partition type changes) by {@link #updatePartitionSpecificSettingsSection()}.
     * The section hides itself entirely if no type-specific block is available.
     *
     * <p>For example — the specialist panel frame goes up:</p>
     * <pre>
     *   [v] Partition Specific Settings   (collapsible section header)
     *   +--------------------------------+
     *   |  (content injected later by    |
     *   |   the JDBM or Mavibot block)   |
     *   +--------------------------------+
     * </pre>
     *
     * @param parent   the parent composite (the details page body)
     * @param toolkit  the form toolkit for creating styled widgets
     */
    private void createPartitionSpecificSettingsSection( Composite parent, FormToolkit toolkit )
    {
        // Creating the Section
        specificSettingsSection = toolkit.createSection( parent, Section.TWISTIE | Section.EXPANDED
            | Section.TITLE_BAR );
        specificSettingsSection.marginWidth = 10;
        specificSettingsSection.setText( "Partition Specific Settings" );
        specificSettingsSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Creating the Composite
        specificSettingsSectionComposite = toolkit.createComposite( specificSettingsSection );
        toolkit.paintBordersFor( specificSettingsSectionComposite );
        GridLayout gd = new GridLayout();
        gd.marginHeight = gd.marginWidth = 0;
        gd.verticalSpacing = gd.horizontalSpacing = 0;
        specificSettingsSectionComposite.setLayout( gd );
        specificSettingsSection.setClient( specificSettingsSectionComposite );
    }


    // ── Old Specialist Panel Cleared from the Frame ───────────────────────────
    // The vault type just changed — out with the old specialist panel, before
    // the new one can be installed. We dispose the existing inner composite
    // so the SWT widget tree doesn't accumulate stale controls.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Disposes the currently displayed type-specific settings composite, if any.
     * We call this whenever the partition type changes so the old JDBM or Mavibot
     * panel is fully cleaned up before the new one is constructed in its place.
     */
    private void disposeSpecificSettingsComposite()
    {
        if ( ( partitionSpecificDetailsComposite != null ) && !( partitionSpecificDetailsComposite.isDisposed() ) )
        {
            partitionSpecificDetailsComposite.dispose();
        }

        partitionSpecificDetailsComposite = null;
    }


    // ── New Specialist Panel Installed and Refreshed ─────────────────────────
    // The new specialist strides in, disposes the previous panel, bolts their
    // own instrument cluster into the frame, and reads out the current vault
    // values so all their dials show the right readings immediately.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the type-specific settings composite with a freshly built one
     * from the current {@link PartitionSpecificDetailsBlock}.
     * Disposes the old composite first, then asks the new block to build its
     * content, triggers a layout pass so sizes re-flow, and hides the whole section
     * if there is no type-specific block (e.g., unknown partition type).
     */
    private void updatePartitionSpecificSettingsSection()
    {
        // Disposing existing specific settings composite
        disposeSpecificSettingsComposite();

        // Create the specific settings block content
        if ( partitionSpecificDetailsBlock != null )
        {
            partitionSpecificDetailsComposite = partitionSpecificDetailsBlock.createBlockContent(
                specificSettingsSectionComposite,
                toolkit );
            partitionSpecificDetailsBlock.refresh();
        }

        parentComposite.layout( true, true );

        // Making the section visible or not
        specificSettingsSection.setVisible( partitionSpecificDetailsBlock != null );
    }


    // ── Search Index Registry Panel Constructed ───────────────────────────────
    // The last page of the dossier is the vault's index ledger: a scrollable
    // list of every LDAP attribute that has a dedicated B-tree index to speed
    // up searches, with buttons to add, edit, or remove individual entries.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the "Indexed Attributes" section for managing the partition's search indexes.
     * Shows a list of current indexes with attribute ID and (for JDBM) cache size,
     * plus Add, Edit, and Delete buttons. Edit and Delete are disabled until a row
     * is selected in the table.
     *
     * <p>For example — the index ledger pinned to the wall:</p>
     * <pre>
     *   | objectClass [100] |   [Add]
     *   | uid [100]         |   [Edit]
     *   | entryUUID [100]   |   [Delete]
     *   | ...               |
     * </pre>
     *
     * @param parent   the parent composite (the details page body)
     * @param toolkit  the form toolkit for creating styled widgets
     */
    private void createIndexesSection( Composite parent, FormToolkit toolkit )
    {
        // Section
        Section indexedAttributesSection = toolkit.createSection( parent, Section.DESCRIPTION | Section.TITLE_BAR );
        indexedAttributesSection.marginWidth = 10;
        indexedAttributesSection.setText( Messages.getString( "PartitionDetailsPage.IndexedAttributes" ) ); //$NON-NLS-1$
        indexedAttributesSection.setDescription( Messages
            .getString( "PartitionDetailsPage.SetIndexedAttributesOfPartition" ) ); //$NON-NLS-1$
        indexedAttributesSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite indexedAttributesClient = toolkit.createComposite( indexedAttributesSection );
        toolkit.paintBordersFor( indexedAttributesClient );
        indexedAttributesClient.setLayout( new GridLayout( 2, false ) );
        indexedAttributesSection.setClient( indexedAttributesClient );

        // TableViewer
        Table indexedAttributesTable = toolkit.createTable( indexedAttributesClient, SWT.NONE );
        GridData gd = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 3 );
        gd.heightHint = 80;
        indexedAttributesTable.setLayoutData( gd );
        indexesTableViewer = new TableViewer( indexedAttributesTable );
        indexesTableViewer.setContentProvider( new ArrayContentProvider() );
        indexesTableViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof JdbmIndexBean )
                {
                    JdbmIndexBean jdbmIndexBean = ( JdbmIndexBean ) element;

                    return NLS.bind( "{0} [{1}]", jdbmIndexBean.getIndexAttributeId(), //$NON-NLS-1$
                        jdbmIndexBean.getIndexCacheSize() );
                }
                else if ( element instanceof MavibotIndexBean )
                {
                    MavibotIndexBean mavibotIndexBean = ( MavibotIndexBean ) element;

                    return mavibotIndexBean.getIndexAttributeId();
                }

                return super.getText( element );
            }


            @Override
            public Image getImage( Object element )
            {
                if ( element instanceof IndexBean )
                {
                    return ApacheDS2ConfigurationPlugin.getDefault().getImage(
                        ApacheDS2ConfigurationPluginConstants.IMG_INDEX );
                }

                return super.getImage( element );
            }
        } );

        // Add button
        indexesAddButton = toolkit.createButton( indexedAttributesClient,
            Messages.getString( "PartitionDetailsPage.Add" ), SWT.PUSH ); //$NON-NLS-1$
        indexesAddButton.setLayoutData( createNewButtonGridData() );

        // Edit button
        indexesEditButton = toolkit.createButton( indexedAttributesClient,
            Messages.getString( "PartitionDetailsPage.Edit" ), SWT.PUSH ); //$NON-NLS-1$
        indexesEditButton.setEnabled( false );
        indexesEditButton.setLayoutData( createNewButtonGridData() );

        // Delete button
        indexesDeleteButton = toolkit.createButton( indexedAttributesClient,
            Messages.getString( "PartitionDetailsPage.Delete" ), SWT.PUSH ); //$NON-NLS-1$
        indexesDeleteButton.setEnabled( false );
        indexesDeleteButton.setLayoutData( createNewButtonGridData() );
    }


    // ── Standard Button Sized to Imperial Spec ───────────────────────────────
    // Every button in the Imperial fleet is built to the same standardized
    // dimensions — no bespoke widths that make the UI look like a cantina menu.
    // We produce a GridData object sized to the standard dialog button width.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link GridData} sized to the standard dialog button width.
     * Re-use this whenever you need a button that aligns with Add/Edit/Delete
     * button columns elsewhere in the form.
     *
     * @return  a new {@link GridData} set to fill horizontally at the standard button width
     */
    private GridData createNewButtonGridData()
    {
        GridData gd = new GridData( SWT.FILL, SWT.BEGINNING, false, false );
        gd.widthHint = IDialogConstants.BUTTON_WIDTH;
        return gd;
    }


    // ── Guards Posted at Every Dossier Field ─────────────────────────────────
    // The vault chief posts a guard at each form field so that any user edit
    // is immediately captured: text changes commit to the model, checkbox toggles
    // commit and mark the editor dirty, and table selections enable the right buttons.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Attaches all pre-built listener instances to their matching UI controls.
     * Called after loading data into the form so that subsequent user edits
     * are captured. Always pair with a prior or subsequent call to
     * {@link #removeListeners()} to bracket programmatic updates.
     */
    private void addListeners()
    {
        partitionTypeComboViewer.addSelectionChangedListener( partitionTypeComboViewerSelectionChangedListener );

        idText.addModifyListener( textModifyListener );
        suffixText.addModifyListener( textModifyListener );
        suffixText.addModifyListener( suffixTextModifyListener );
        synchOnWriteCheckbox.addSelectionListener( checkboxSelectionListener );

        autoGenerateContextEntryCheckbox.addSelectionListener( autoGenerateContextEntryCheckboxSelectionListener );
        contextEntryTableViewer.addDoubleClickListener( contextEntryTableViewerDoubleClickListener );
        contextEntryTableViewer.addSelectionChangedListener( contextEntryTableViewerSelectionListener );
        contextEntryAddButton.addSelectionListener( contextEntryAddButtonListener );
        contextEntryEditButton.addSelectionListener( contextEntryEditButtonListener );
        contextEntryDeleteButton.addSelectionListener( contextEntryDeleteButtonListener );

        indexesTableViewer.addSelectionChangedListener( indexedAttributesTableViewerListener );
        indexesTableViewer.addDoubleClickListener( indexedAttributesTableViewerDoubleClickListener );
        indexesAddButton.addSelectionListener( indexedAttributeAddButtonListener );
        indexesEditButton.addSelectionListener( indexedAttributeEditButtonListener );
        indexesDeleteButton.addSelectionListener( indexedAttributeDeleteButtonListener );
    }


    // ── Guards Stood Down Before Data Reload ─────────────────────────────────
    // Before the refresh cycle rewrites every field from the model, the chief
    // stands all the guards down — otherwise every programmatic text update
    // would trigger a commit event and falsely dirty the editor.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all listener instances from their matching UI controls.
     * We always call this before programmatically loading data into widgets
     * so that the load doesn't fire change events. Always follow up with
     * a call to {@link #addListeners()}.
     */
    private void removeListeners()
    {
        partitionTypeComboViewer.removeSelectionChangedListener( partitionTypeComboViewerSelectionChangedListener );

        idText.removeModifyListener( textModifyListener );
        suffixText.removeModifyListener( textModifyListener );
        suffixText.removeModifyListener( suffixTextModifyListener );
        synchOnWriteCheckbox.removeSelectionListener( checkboxSelectionListener );

        autoGenerateContextEntryCheckbox.removeSelectionListener( autoGenerateContextEntryCheckboxSelectionListener );
        contextEntryTableViewer.removeDoubleClickListener( contextEntryTableViewerDoubleClickListener );
        contextEntryTableViewer.removeSelectionChangedListener( contextEntryTableViewerSelectionListener );
        contextEntryAddButton.removeSelectionListener( contextEntryAddButtonListener );
        contextEntryEditButton.removeSelectionListener( contextEntryEditButtonListener );
        contextEntryDeleteButton.removeSelectionListener( contextEntryDeleteButtonListener );

        indexesTableViewer.removeSelectionChangedListener( indexedAttributesTableViewerListener );
        indexesTableViewer.removeDoubleClickListener( indexedAttributesTableViewerDoubleClickListener );
        indexesAddButton.removeSelectionListener( indexedAttributeAddButtonListener );
        indexesEditButton.removeSelectionListener( indexedAttributeEditButtonListener );
        indexesDeleteButton.removeSelectionListener( indexedAttributeDeleteButtonListener );
    }


    // ── Vault Chief Reassigned to a Different Vault ───────────────────────────
    // The registry master taps a different vault on the list; the chief picks up
    // their dossier, walks over to the newly selected vault, and immediately starts
    // reading its particulars into the form panels on the right.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a selection change in the master list.
     * Extracts the newly selected {@link PartitionWrapper} from the structured
     * selection (or clears it if nothing is selected), then triggers a full
     * {@link #refresh()} so all panels update to show the new partition's data.
     *
     * @param part       the form part that fired the selection event
     * @param selection  the new selection from the master list table viewer
     */
    public void selectionChanged( IFormPart part, ISelection selection )
    {
        IStructuredSelection ssel = ( IStructuredSelection ) selection;
        if ( ssel.size() == 1 )
        {
            partitionWrapper = ( PartitionWrapper ) ssel.getFirstElement();
        }
        else
        {
            partitionWrapper = null;
        }
        refresh();
    }


    // ── Chief Files the Updated Vault Record Back into the Model ─────────────
    // Inspection complete — the chief reads every dial and checkbox, writes the
    // values onto the official partition bean, and hands the dossier back to the
    // registry. The specific-settings specialist does the same for their section.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Writes the current widget values back into the underlying {@link PartitionBean}.
     * Pushes the ID, suffix DN, context entry LDIF, and sync-on-write flag from
     * the form controls into the model. Also delegates to the
     * {@link PartitionSpecificDetailsBlock} so storage-engine settings are saved too.
     * Does nothing if no partition is currently selected.
     *
     * @param onSave  {@code true} when called during an explicit Save; {@code false}
     *                when committing for other reasons such as a page switch
     */
    public void commit( boolean onSave )
    {
        if ( ( partitionWrapper != null ) && ( partitionWrapper.getPartition() != null ) )
        {
            PartitionBean partition = partitionWrapper.getPartition();

            // ID
            partition.setPartitionId( idText.getText() );

            // Suffix
            try
            {
                partition.setPartitionSuffix( new Dn( suffixText.getText() ) );
            }
            catch ( LdapInvalidDnException e )
            {
                // Stay silent
            }

            // Context Entry
            if ( contextEntry.size() > 0 )
            {
                LdifEntry ldifEntry = new LdifEntry( contextEntry );
                ldifEntry.setDn( partition.getPartitionSuffix() );
                partition.setContextEntry( ldifEntry.toString() );
            }
            else
            {
                partition.setContextEntry( null );
            }

            // Synchronization on write
            partition.setPartitionSyncOnWrite( synchOnWriteCheckbox.getSelection() );

            //
            // Specific Settings
            //
            if ( partitionSpecificDetailsBlock != null )
            {
                partitionSpecificDetailsBlock.commit( onSave );
            }
        }
    }


    // ── Vault Chief Dismissed, Desk Cleared ──────────────────────────────────
    // The inspection is over; the vault chief packs up and leaves the room.
    // There is nothing to explicitly release here, but the method satisfies
    // the IDetailsPage contract.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this details page.
     * Currently a no-op — the Eclipse Forms framework cleans up widget disposal
     * automatically. Exists to satisfy the {@link IDetailsPage} contract.
     */
    public void dispose()
    {
    }


    // ── Chief Receives the Master Form Toolkit Briefing ───────────────────────
    // Before the chief can draw any widgets, they need the toolkit — the
    // Imperial-spec tool kit that ensures every control looks consistently styled.
    // We pull it from the managed form and stash it for later use.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Stores the {@link FormToolkit} from the managed form for later use when
     * building widget sections.
     * The toolkit is needed during {@link #createContents(Composite)} and the
     * section-creation helpers, which may be called after this method.
     *
     * @param form  the managed form that owns this details page
     */
    public void initialize( IManagedForm form )
    {
        toolkit = form.getToolkit();
    }


    // ── Chief Checks Whether the Dossier Needs Filing ────────────────────────
    // A quick check: has anything in this panel been changed since the last save?
    // We always return false here because dirty state is managed by the master
    // block and the editor itself — not by individual detail pages.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Indicates whether this details page has unsaved changes.
     * Always returns {@code false} because dirty tracking for partitions is
     * managed at the {@link PartitionsMasterDetailsBlock} level, not here.
     *
     * @return  {@code false} always
     */
    public boolean isDirty()
    {
        return false;
    }


    // ── Chief Checks Whether the Displayed Data Is Stale ─────────────────────
    // Is the dossier out of date? Do we need to re-fetch from the master record?
    // We always say no here — the refresh is driven externally by selection events.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Indicates whether the displayed data is stale and needs to be refreshed.
     * Always returns {@code false} — refresh is triggered by external selection
     * events in the master list, not by an internal staleness check.
     *
     * @return  {@code false} always
     */
    public boolean isStale()
    {
        return false;
    }


    // ── Chief Reads the Fresh Vault Dossier Top to Bottom ────────────────────
    // A new vault has been selected — the chief stands down the guards, reads
    // the entire dossier from scratch, populates all four sections, swaps in
    // the correct specialist panel, then posts the guards back.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all UI sections from the currently selected {@link PartitionWrapper}.
     * Detaches listeners first to prevent spurious change events during loading.
     * Populates type, ID, suffix, auto-generate flag, context entry, index list,
     * sync-on-write, and the type-specific settings block, then re-attaches listeners.
     * If no partition is selected, the form is left in its previous state.
     */
    public void refresh()
    {
        removeListeners();

        if ( ( partitionWrapper != null ) && ( partitionWrapper.getPartition() != null ) )
        {
            PartitionBean partition = partitionWrapper.getPartition();

            // Checking if the selected partition is the system partition
            boolean isSystemPartition = PartitionsPage.isSystemPartition( partition );

            // Type
            PartitionType partitionType = PartitionType.fromPartition( partition );
            partitionTypeComboViewer.setSelection( new StructuredSelection( partitionType ) );
            partitionTypeComboViewer.getCombo().setEnabled( !isSystemPartition );

            // ID
            String id = partition.getPartitionId();
            idText.setText( ( id == null ) ? "" : id ); //$NON-NLS-1$
            idText.setEnabled( !isSystemPartition );

            // Suffix
            Dn suffix = partition.getPartitionSuffix();
            suffixText.setText( ( suffix == null ) ? "" : suffix.toString() ); //$NON-NLS-1$
            suffixText.setEnabled( !isSystemPartition );

            // Auto Generate Context Entry
            autoGenerateContextEntryCheckbox.setSelection( true ); // TODO review this

            // Context Entry
            refreshContextEntry();

            // Indexed Attributes
            indexesList = partition.getIndexes();
            indexesTableViewer.setInput( indexesList );

            // Synchronization on write
            synchOnWriteCheckbox.setSelection( partition.isPartitionSyncOnWrite() );

            //
            // Specific Settings
            //

            // JdbmPartitionBean Type
            if ( partition instanceof JdbmPartitionBean )
            {
                partitionTypeComboViewer.setSelection( new StructuredSelection( PartitionType.JDBM ) );
                partitionSpecificDetailsBlock = new JdbmPartitionSpecificDetailsBlock( instance,
                    ( JdbmPartitionBean ) partition );
            }
            // MavibotPartitionBean Type
            else if ( partition instanceof MavibotPartitionBean )
            {
                partitionTypeComboViewer.setSelection( new StructuredSelection( PartitionType.MAVIBOT ) );
                partitionSpecificDetailsBlock = new MavibotPartitionSpecificDetailsBlock( instance,
                    ( MavibotPartitionBean ) partition );
            }
            else
            {
                partitionTypeComboViewer.setSelection( null );
                partitionSpecificDetailsBlock = null;
            }

            updatePartitionSpecificSettingsSection();
        }

        addListeners();
    }


    // ── Context Entry Table Reloaded from the Partition Record ───────────────
    // The chief flips to the root-entry page of the dossier and reads the LDIF
    // string stored on the partition bean, parses it back into an Entry object,
    // and feeds it into the table viewer so the rows update.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the context entry table viewer from the partition bean's stored LDIF string.
     * Parses the LDIF, wraps it in a {@link DefaultEntry}, feeds it to the table,
     * resizes the columns to fit, and updates the enable state of the buttons.
     * Falls back to an empty entry if the stored string is missing or unparseable.
     */
    private void refreshContextEntry()
    {
        if ( ( partitionWrapper != null ) && ( partitionWrapper.getPartition() != null ) )
        {
            PartitionBean partition = partitionWrapper.getPartition();

            String contextEntryString = partition.getContextEntry();

            if ( ( contextEntryString != null ) && ( !"".equals( contextEntryString ) ) ) //$NON-NLS-1$
            {
                try
                {
                    // Replace '\n' to real LF
                    contextEntryString = contextEntryString.replaceAll( "\\\\n", "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

                    LdifReader reader = new LdifReader( new StringReader( contextEntryString ) );
                    contextEntry = reader.next().getEntry();
                    reader.close();
                }
                catch ( Exception e )
                {
                    contextEntry = new DefaultEntry();
                }
            }
            else
            {
                contextEntry = new DefaultEntry();
            }

            contextEntryTableViewer.setInput( contextEntry );
            resizeContextEntryTableColumnsToFit();

            // TODO Verify this

            boolean enabled = !autoGenerateContextEntryCheckbox.getSelection();
            contextEntryTableViewer.getTable().setEnabled( enabled );
            contextEntryAddButton.setEnabled( enabled );
            contextEntryEditButton.setEnabled( enabled );
            contextEntryDeleteButton.setEnabled( enabled );
        }
    }


    // ── Vault Root Entry Generated from Suffix DN ─────────────────────────────
    // When the "auto-generate" order is active, the vault's root entry is derived
    // automatically from the suffix DN — the chief doesn't have to write it by hand.
    // We parse the current suffix text into a Dn, ask the master block to generate
    // the standard LDIF, push it onto the partition, and refresh the table.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Auto-generates the context entry LDIF from the current suffix DN text field.
     * Only runs when the "Auto Generate" checkbox is ticked and a partition is selected.
     * Parses the suffix text as a {@link Dn}, delegates LDIF generation to
     * {@link PartitionsMasterDetailsBlock#getContextEntryLdif(Dn)}, stores it on the
     * partition bean, and refreshes the context entry table.
     * Silently ignores an invalid suffix DN.
     */
    private void autoGenerateContextEntry()
    {
        if ( ( partitionWrapper != null ) && ( partitionWrapper.getPartition() != null ) )
        {
            PartitionBean partition = partitionWrapper.getPartition();

            if ( autoGenerateContextEntryCheckbox.getSelection() )
            {
                try
                {
                    Dn suffixDn = new Dn( suffixText.getText() );
                    partition.setContextEntry( PartitionsMasterDetailsBlock.getContextEntryLdif( suffixDn ) );
                    refreshContextEntry();
                }
                catch ( LdapInvalidDnException e1 )
                {
                    // Silent
                }
            }
        }
    }


    // ── Context Entry Table Columns Packed to Fit Contents ───────────────────
    // After data is loaded into the table, the chief calls the quartermaster
    // to tighten up the column widths so nothing is clipped and nothing wastes
    // empty space — with a little extra breathing room on the first column.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Packs the context entry table columns so their width matches their content.
     * Adds a small padding to the Attribute column so the text doesn't sit flush
     * against the column separator. Call this after updating the table's input.
     */
    private void resizeContextEntryTableColumnsToFit()
    {
        // Resizing the first column
        contextEntryTableViewer.getTable().getColumn( 0 ).pack();
        // Adding a little space to the first column
        contextEntryTableViewer.getTable().getColumn( 0 )
            .setWidth( contextEntryTableViewer.getTable().getColumn( 0 ).getWidth() + 5 );
        // Resizing the second column
        contextEntryTableViewer.getTable().getColumn( 1 ).pack();
    }


    // ── Cursor Directed to the ID Field ──────────────────────────────────────
    // When the vault chief first sits down at the dossier, we direct their
    // attention to the most important field: the partition ID text box.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Moves keyboard focus to the partition ID text field.
     * Called by the Eclipse Forms framework when this details page becomes active,
     * so the user can start typing immediately without clicking first.
     */
    public void setFocus()
    {
        idText.setFocus();
    }


    // ── Form Input Object Offered but Declined ───────────────────────────────
    // The forms framework occasionally offers us an input object directly;
    // we don't use that mechanism — our input comes from selectionChanged().
    // So we politely decline and return false every time.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Handles a direct form input offer from the Eclipse Forms framework.
     * We always return {@code false} because we receive our input through
     * {@link #selectionChanged(IFormPart, ISelection)} rather than this route.
     *
     * @param input  the proposed input object (ignored)
     * @return       {@code false} always
     */
    public boolean setFormInput( Object input )
    {
        return false;
    }


    // ── Selected Index Record Opened for Editing ─────────────────────────────
    // The chief picks the highlighted index entry off the ledger and opens
    // the appropriate dialog — JDBM or Mavibot — so the attribute ID and cache
    // size (for JDBM) can be modified before being filed back.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Opens the appropriate index dialog for the currently selected index in the table.
     * Detects whether the selected index is a {@link JdbmIndexBean} or
     * {@link MavibotIndexBean}, opens the matching dialog, and if the user confirms
     * and made changes, refreshes the table and marks the editor dirty.
     * Does nothing if the selection is empty or the partition type is unknown.
     */
    private void editSelectedIndex()
    {
        StructuredSelection selection = ( StructuredSelection ) indexesTableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            PartitionType partitionType = ( PartitionType ) ( ( StructuredSelection ) partitionTypeComboViewer
                .getSelection() ).getFirstElement();

            if ( partitionType != null )
            {
                IndexBean editedIndex = null;

                // JDBM partition
                if ( partitionType == PartitionType.JDBM )
                {
                    // Getting the selected JDBM index
                    JdbmIndexBean index = ( JdbmIndexBean ) selection.getFirstElement();

                    // Creating a JDBM dialog
                    JdbmIndexDialog dialog = new JdbmIndexDialog( index );

                    if ( JdbmIndexDialog.OK == dialog.open() && dialog.isDirty() )
                    {
                        editedIndex = index;
                    }
                }
                // Mavibot Partition
                else if ( partitionType == PartitionType.MAVIBOT )
                {
                    // Getting the selected Mavibot index
                    MavibotIndexBean index = ( MavibotIndexBean ) selection.getFirstElement();

                    // Creating a Mavibot dialog
                    MavibotIndexDialog dialog = new MavibotIndexDialog( index );

                    if ( MavibotIndexDialog.OK == dialog.open() && dialog.isDirty() )
                    {
                        editedIndex = index;
                    }
                }

                // Checking the new index
                if ( editedIndex != null )
                {
                    indexesTableViewer.refresh();
                    masterDetailsBlock.setEditorDirty();
                }
            }
        }
    }


    // ── Selected Context Entry Attribute Opened for Editing ───────────────────
    // The chief picks a row from the root-entry table and opens the attribute
    // editor dialog. If the user changes anything, the old value is removed
    // from the entry and the new one inserted before the table is refreshed.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Opens the attribute/value editor dialog for the currently selected row in
     * the context entry table.
     * Removes the old attribute value from the entry and inserts the updated one.
     * Refreshes the table, resizes columns, and marks the editor dirty if the user
     * confirmed the dialog and made a change.
     * Does nothing if the selection is empty.
     */
    private void editSelectedContextEntry()
    {
        StructuredSelection selection = ( StructuredSelection ) contextEntryTableViewer.getSelection();
        if ( !selection.isEmpty() )
        {
            AttributeValueObject attributeValueObject = ( AttributeValueObject ) selection.getFirstElement();

            String oldId = attributeValueObject.getAttribute();
            String oldValue = attributeValueObject.getValue();

            AttributeValueDialog dialog = new AttributeValueDialog( attributeValueObject );
            if ( AttributeValueDialog.OK == dialog.open() && dialog.isDirty() )
            {
                Attribute attribute = contextEntry.get( oldId );
                if ( attribute != null )
                {
                    attribute.remove( oldValue );
                }

                AttributeValueObject newAttributeValueObject = dialog.getAttributeValueObject();
                attribute = contextEntry.get( newAttributeValueObject.getAttribute() );

                if ( attribute != null )
                {
                    try
                    {
                        attribute.add( newAttributeValueObject.getValue() );
                    }
                    catch ( LdapInvalidAttributeValueException liave )
                    {
                        // Will never occur
                    }
                }
                else
                {
                    try
                    {
                        contextEntry.put( new DefaultAttribute( newAttributeValueObject.getAttribute(),
                            newAttributeValueObject.getValue() ) );
                    }
                    catch ( LdapException e )
                    {
                        // Will never occur
                    }
                }

                contextEntryTableViewer.refresh();
                resizeContextEntryTableColumnsToFit();
                masterDetailsBlock.setEditorDirty();
                //                dirty = true; TODO
                commit( true );
            }
        }
    }


    // ── Editor Flagged as Having Unsaved Changes ──────────────────────────────
    // The vault record has been modified; we signal the master registry block
    // so it can in turn notify the Eclipse editor framework that there are
    // pending changes and the Save button should light up.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Propagates a dirty notification up to the {@link PartitionsMasterDetailsBlock}.
     * Call this from type-specific detail blocks (e.g., JDBM or Mavibot) when their
     * controls change, so the top-level editor Save button activates.
     */
    public void setEditorDirty()
    {
        masterDetailsBlock.setEditorDirty();
    }


    // ── Vault Properties Transferred to a New Storage Engine Bean ────────────
    // The user just changed the partition type — JDBM to Mavibot or vice versa.
    // We need to carry over all the common properties (ID, suffix, context entry,
    // indexes…) to the new bean, converting index types along the way.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Copies all common {@link PartitionBean} properties from {@code original} to
     * {@code destination}, converting indexes to the appropriate target type.
     * Called when the user switches partition type in the combo so we don't lose
     * the ID, suffix, context entry, and other settings that carry across types.
     * Silently ignores {@code null} arguments.
     *
     * @param original     the source partition bean to copy from
     * @param destination  the target partition bean to copy into
     */
    private void copyPartitionProperties( PartitionBean original, PartitionBean destination )
    {
        if ( ( original != null ) && ( destination != null ) )
        {
            // Simple properties
            destination.setContextEntry( original.getContextEntry() );
            destination.setDescription( original.getDescription() );
            destination.setDn( original.getDn() );
            destination.setEnabled( original.isEnabled() );
            destination.setPartitionId( original.getPartitionId() );
            destination.setPartitionSuffix( original.getPartitionSuffix() );
            destination.setPartitionSyncOnWrite( original.isPartitionSyncOnWrite() );

            // Indexes
            List<IndexBean> originalIndexes = original.getIndexes();
            List<IndexBean> destinationIndexes = new ArrayList<>();

            if ( originalIndexes != null )
            {
                for ( IndexBean originalIndexBean : originalIndexes )
                {
                    if ( destination instanceof JdbmPartitionBean )
                    {
                        JdbmIndexBean destinationIndexBean = new JdbmIndexBean();

                        destinationIndexBean.setIndexAttributeId( originalIndexBean.getIndexAttributeId() );
                        destinationIndexBean.setIndexHasReverse( originalIndexBean.getIndexHasReverse() );

                        destinationIndexes.add( destinationIndexBean );
                    }
                    else if ( destination instanceof MavibotPartitionBean )
                    {
                        MavibotIndexBean destinationIndexBean = new MavibotIndexBean();

                        destinationIndexBean.setIndexAttributeId( originalIndexBean.getIndexAttributeId() );
                        destinationIndexBean.setIndexHasReverse( originalIndexBean.getIndexHasReverse() );

                        destinationIndexes.add( destinationIndexBean );
                    }
                }
            }

            destination.setIndexes( destinationIndexes );
        }
    }
}
