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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.AliasDerefMode;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.server.config.beans.ReplConsumerBean;
import org.apache.directory.studio.common.ui.dialogs.AttributeDialog;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.EntryWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.FilterWidget;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.utils.SchemaObjectLoader;
import org.apache.directory.studio.common.ui.wrappers.StringValueWrapper;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


// ── CLASS: ReplicationDetailsPage — THE REBEL SIGNALS OFFICER'S EDITING STATION ─────────
// A Rebel signals officer sits at her console on Yavin 4 and opens the briefing file for
// one replication consumer — the outpost that pulls intelligence from a remote Imperial
// server.  She configures the remote host, the bind credentials, the search scope, and
// exactly which attributes to sync, then commits her changes so the consumer bean reflects
// her orders.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * The details panel shown on the right side of the Replication master/details view.
 * It renders every configurable field of a single {@link ReplConsumerBean} — identity,
 * connection parameters, and search configuration — and commits user edits back to the
 * bean on every interaction.
 * Think of this class as the Rebel signals officer's editing station: she picks a consumer
 * from the roster on the left and this panel opens its full briefing file for inspection
 * and amendment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReplicationDetailsPage implements IDetailsPage
{
    /** The associated Master Details Block */
    private ReplicationMasterDetailsBlock masterDetailsBlock;

    /** The Managed Form */
    private IManagedForm mform;

    /** The input consumer */
    private ReplConsumerBean input;

    /** The browser connection */
    private IBrowserConnection browserConnection;

    /** The Attribute list loader */
    private SchemaObjectLoader attributeLoader;

    /** The list of attributes */
    private List<String> attributesList = new ArrayList<String>();

    // UI Widgets
    private Button enabledCheckbox;
    private Text idText;
    private Text descriptionText;
    private Button refreshAndPersistModeButton;
    private Button refreshOnlyModeButton;
    private Text refreshIntervalText;
    private Text remoteHostText;
    private Text remotePortText;
    private Text bindDnText;
    private Text bindPasswordText;
    private Button showPasswordCheckbox;
    private Button useStartTlsCheckbox;
    private Text sizeLimitText;
    private Text timeLimitText;
    private EntryWidget entryWidget;
    private FilterWidget filterWidget;
    private Button subtreeScopeButton;
    private Button oneLevelScopeButton;
    private Button objectScopeButton;
    private Button allAttributesCheckbox;
    private TableViewer attributesTableViewer;
    private Button addAttributeButton;
    private Button editAttributeButton;
    private Button deleteAttributeButton;
    private Button findingBaseDnAliasesDereferencingButton;
    private Button searchAliasesDereferencingButton;

    // Listeners
    /** The Text Modify Listener */
    private ModifyListener textModifyListener = new ModifyListener()
    {
        public void modifyText( ModifyEvent e )
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        }
    };

    /** The button Selection Listener */
    private SelectionListener buttonSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        }
    };

    /** The widget Modify Listener */
    private WidgetModifyListener widgetModifyListener = new WidgetModifyListener()
    {
        public void widgetModified( WidgetModifyEvent event )
        {
            commit( true );
            masterDetailsBlock.setEditorDirty();
        }
    };

    private VerifyListener integerVerifyListener = new VerifyListener()
    {
        public void verifyText( VerifyEvent e )
        {
            if ( !e.text.matches( "[0-9]*" ) ) //$NON-NLS-1$
            {
                e.doit = false;
            }
        }
    };

    private SelectionListener showPasswordCheckboxSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            if ( showPasswordCheckbox.getSelection() )
            {
                bindPasswordText.setEchoChar( '\0' );
            }
            else
            {
                bindPasswordText.setEchoChar( '•' );
            }
        }
    };

    private ISelectionChangedListener attributesTableViewerSelectionListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            updateAttributesButtonsEnableState();
        }
    };

    /** The Double Click Listener for the Indexed Attributes Table Viewer */
    private IDoubleClickListener attributesTableViewerDoubleClickListener = new IDoubleClickListener()
    {
        public void doubleClick( DoubleClickEvent event )
        {
            editSelectedAttribute();
        }
    };

    private SelectionListener addAttributeButtonSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            addNewAttribute();
        }
    };

    private SelectionListener editAttributeButtonSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            editSelectedAttribute();
        }
    };

    private SelectionListener deleteAttributeButtonSelectionListener = new SelectionAdapter()
    {
        public void widgetSelected( SelectionEvent e )
        {
            deleteSelectedAttribute();
        }
    };


    // ── Reporting For Duty At The Signals Station ──────────────────────────────────────────
    // The Rebel signals officer arrives at her editing station, grabs a reference to the
    // master block (so she can signal "dirty" edits), fires up the schema object loader,
    // and looks up the browser connection tied to the current server config.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ReplicationDetailsPage and wires it to the given master/details block.
     * Also initialises the schema object loader (used to suggest attribute names in the
     * attributes dialog) and resolves the browser connection for the entry and filter widgets.
     *
     * <p>For example — the Rebel signals officer reports for duty:</p>
     * <pre>
     *   She grabs a radio link to the master roster block so she can mark changes as dirty.
     *   She spins up the attribute loader so auto-complete works in the attribute dialog.
     *   She resolves the LDAP browser connection tied to this server's configuration.
     * </pre>
     *
     * @param pmdb  the {@link ReplicationMasterDetailsBlock} that owns this details page
     */
    public ReplicationDetailsPage( ReplicationMasterDetailsBlock pmdb )
    {
        masterDetailsBlock = pmdb;
        attributeLoader = new SchemaObjectLoader();

        // Getting the browser connection associated with the connection in the configuration
        browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( masterDetailsBlock.getPage().getConnection() );
    }


    // ── Assembling The Full Briefing Station ──────────────────────────────────────────────
    // The Rebel officer lays out three specialised sub-panels on her editing station:
    // the consumer's identity, its connection parameters, and its search configuration.
    // Each sub-panel maps to a different dimension of the replication consumer dossier.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds all three visual sections of the details panel inside the given parent composite.
     * Delegates each section to its own {@code create*Section} method — details, connection,
     * and configuration.
     *
     * <p>For example — the officer lays out her briefing station with three dedicated zones:</p>
     * <pre>
     *   Zone 1: identity (ID, description, enabled flag).
     *   Zone 2: connection (replication mode, remote host/port, bind credentials, TLS).
     *   Zone 3: configuration (base DN, filter, scope, attributes, alias dereferencing).
     * </pre>
     *
     * @param parent  the SWT composite to build the sections inside
     */
    public void createContents( Composite parent )
    {
        FormToolkit toolkit = mform.getToolkit();
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 5;
        layout.leftMargin = 5;
        layout.rightMargin = 2;
        layout.bottomMargin = 2;
        parent.setLayout( layout );

        createDetailsSection( parent, toolkit );
        createConnectionSection( parent, toolkit );
        createConfigurationSection( parent, toolkit );
    }


    // ── Building The Consumer Identity Zone ───────────────────────────────────────────────
    // The top zone of the briefing station shows who this replication consumer is: its
    // unique ID, a plain-English description, and whether it is currently active.
    // These are the fields a Rebel commander checks first to identify an intelligence outpost.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Replication Consumer Details" section — the top block of the details
     * panel — containing the Enabled checkbox, the consumer ID field, and the Description.
     *
     * <p>For example — a Rebel commander reads the identity panel of an outpost dossier:</p>
     * <pre>
     *   Enabled = checked, ID = "consumer1", Description = "Syncs from Coruscant node".
     * </pre>
     *
     * @param parent   the parent composite to attach this section to
     * @param toolkit  the Eclipse Forms toolkit used to create styled widgets
     */
    private void createDetailsSection( Composite parent, FormToolkit toolkit )
    {
        Section section = toolkit.createSection( parent, Section.DESCRIPTION | Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Replication Consumer Details" );
        section.setDescription( "Set the properties of the replication consumer." );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite client = toolkit.createComposite( section );
        toolkit.paintBordersFor( client );
        GridLayout glayout = new GridLayout( 2, false );
        client.setLayout( glayout );
        section.setClient( client );

        // Enabled Checkbox
        enabledCheckbox = toolkit.createButton( client, "Enabled", SWT.CHECK );
        enabledCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // ID Text
        toolkit.createLabel( client, "ID:" );
        idText = toolkit.createText( client, "" );
        idText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Description Text
        toolkit.createLabel( client, "Description:" );
        descriptionText = toolkit.createText( client, "" );
        descriptionText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Building The Comms Link Zone ──────────────────────────────────────────────────────
    // This zone is the radio room: it configures how the consumer connects to the remote
    // LDAP server — replication mode, host, port, bind DN, password, size/time limits,
    // and whether to encrypt with StartTLS.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Connection" section covering replication mode (refresh-and-persist vs
     * refresh-only), refresh interval, remote host and port, bind credentials, size and
     * time limits, and the StartTLS toggle.
     *
     * <p>For example — a Rebel signals officer configures the comms link to a remote node:</p>
     * <pre>
     *   She selects "Refresh And Persist" mode so the consumer stays connected continuously.
     *   She enters "coruscant.empire.gov" as the remote host and 10389 as the port.
     *   She fills in the bind DN and password, then enables StartTLS for encrypted comms.
     * </pre>
     *
     * @param parent   the parent composite
     * @param toolkit  the Eclipse Forms toolkit
     */
    private void createConnectionSection( Composite parent, FormToolkit toolkit )
    {
        Section section = toolkit.createSection( parent, Section.DESCRIPTION | Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Connection" );
        section.setDescription( "Set the properties of the connection." );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        composite.setLayout( new GridLayout( 2, false ) );
        section.setClient( composite );

        // Replication Mode
        toolkit.createLabel( composite, "Replication Mode:" );

        // Refresh And Persist Mode Button
        refreshAndPersistModeButton = toolkit.createButton( composite, "Refresh And Persist", SWT.RADIO );
        refreshAndPersistModeButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );

        // Refresh Only Mode Button
        toolkit.createLabel( composite, "" );
        refreshOnlyModeButton = toolkit.createButton( composite, "Refresh Only", SWT.RADIO );
        refreshOnlyModeButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );

        // Refresh Interval
        toolkit.createLabel( composite, "" );
        Composite refreshIntervalComposite = toolkit.createComposite( composite );
        refreshIntervalComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        refreshIntervalComposite.setLayout( new GridLayout( 3, false ) );
        toolkit.createLabel( refreshIntervalComposite, "  " );
        toolkit.createLabel( refreshIntervalComposite, "Refresh Interval (ms):" );
        refreshIntervalText = toolkit.createText( refreshIntervalComposite, "" );
        refreshIntervalText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Remote Host Text
        toolkit.createLabel( composite, "Remote Host:" );
        remoteHostText = toolkit.createText( composite, "" );
        remoteHostText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Remote Port Text
        toolkit.createLabel( composite, "Remote Port:" );
        remotePortText = toolkit.createText( composite, "" );
        remotePortText.addVerifyListener( integerVerifyListener );
        remotePortText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Bind DN Text
        toolkit.createLabel( composite, "Bind DN:" );
        bindDnText = toolkit.createText( composite, "" );
        bindDnText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Bind Password Text
        toolkit.createLabel( composite, "Bind Password:" );
        bindPasswordText = toolkit.createText( composite, "" );
        bindPasswordText.setEchoChar( '•' );
        bindPasswordText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Show Password Checkbox
        toolkit.createLabel( composite, "" ); //$NON-NLS-1$
        showPasswordCheckbox = toolkit.createButton( composite, "Show password", SWT.CHECK );
        showPasswordCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        showPasswordCheckbox.setSelection( false );

        // Size Limit Text
        toolkit.createLabel( composite, "Size Limit:" );
        sizeLimitText = toolkit.createText( composite, "" );
        sizeLimitText.addVerifyListener( integerVerifyListener );
        sizeLimitText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Time Limit Text
        toolkit.createLabel( composite, "Time Limit:" );
        timeLimitText = toolkit.createText( composite, "" );
        timeLimitText.addVerifyListener( integerVerifyListener );
        timeLimitText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Use Start TLS
        toolkit.createLabel( composite, "" ); //$NON-NLS-1$
        useStartTlsCheckbox = toolkit.createButton( composite, "Use Start TLS", SWT.CHECK );
        useStartTlsCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        useStartTlsCheckbox.setSelection( false );
    }


    // ── Building The Intelligence Gathering Zone ───────────────────────────────────────────
    // This zone defines what the consumer actually syncs: the base DN it starts from, an
    // LDAP filter for which entries to include, the search scope (subtree / one level /
    // object), which attributes to replicate, and how to handle LDAP aliases.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Configuration" section covering the base DN, LDAP search filter, search
     * scope (subtree / one-level / object), the list of attributes to replicate, and the
     * alias-dereferencing mode.
     * This section defines the shape of the data the consumer will pull from the remote server.
     *
     * <p>For example — a Rebel analyst configures what intelligence to collect:</p>
     * <pre>
     *   She sets base DN to "dc=example,dc=com" to start from the root of the tree.
     *   She applies filter "(objectClass=*)" to pull everything.
     *   She picks "Subtree" scope and adds "cn", "sn", "mail" to the attributes list.
     * </pre>
     *
     * @param parent   the parent composite
     * @param toolkit  the Eclipse Forms toolkit
     */
    private void createConfigurationSection( Composite parent, FormToolkit toolkit )
    {
        Section section = toolkit.createSection( parent, Section.DESCRIPTION | Section.TITLE_BAR );
        section.marginWidth = 10;
        section.setText( "Configuration" );
        section.setDescription( "Set the properties of the configuration." );
        TableWrapData td = new TableWrapData( TableWrapData.FILL, TableWrapData.TOP );
        td.grabHorizontal = true;
        section.setLayoutData( td );
        Composite composite = toolkit.createComposite( section );
        toolkit.paintBordersFor( composite );
        GridLayout glayout = new GridLayout( 3, false );
        composite.setLayout( glayout );
        section.setClient( composite );

        // Base DN Text
        toolkit.createLabel( composite, "Base DN:" );
        entryWidget = new EntryWidget( browserConnection, Dn.EMPTY_DN );
        entryWidget.createWidget( composite );

        // Filter Text
        toolkit.createLabel( composite, "Filter:" );
        filterWidget = new FilterWidget();
        filterWidget.setBrowserConnection( browserConnection );
        filterWidget.createWidget( composite );

        // Scope
        Label scopeLabel = toolkit.createLabel( composite, "Scope:" );
        scopeLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.TOP, false, false, 1, 3 ) );

        // Subtree Scope Button
        subtreeScopeButton = toolkit.createButton( composite, "Subtree", SWT.RADIO );
        subtreeScopeButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1 ) );

        // One Level Scope Button
        oneLevelScopeButton = toolkit.createButton( composite, "One Level", SWT.RADIO );
        oneLevelScopeButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1 ) );

        // Object Scope Button
        objectScopeButton = toolkit.createButton( composite, "Object", SWT.RADIO );
        objectScopeButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1 ) );

        // Attributes Label
        Label attributesLabel = toolkit.createLabel( composite, "Attributes:" );
        attributesLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );

        // All Attributes Checkbox
        allAttributesCheckbox = toolkit.createButton( composite, "All Attributes", SWT.CHECK );
        allAttributesCheckbox.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

        // Attributes Table Viewer
        toolkit.createLabel( composite, "" ); //$NON-NLS-1$
        Composite attributesTableComposite = toolkit.createComposite( composite );
        GridLayout gl = new GridLayout( 2, false );
        gl.marginWidth = gl.marginHeight = 0;
        attributesTableComposite.setLayout( gl );
        attributesTableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
        Table attributesTable = toolkit.createTable( attributesTableComposite, SWT.BORDER );
        attributesTable.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 ) );
        attributesTableViewer = new TableViewer( attributesTable );
        attributesTableViewer.setContentProvider( new ArrayContentProvider() );
        attributesTableViewer.setInput( attributesList );

        addAttributeButton = toolkit.createButton( attributesTableComposite, "Add...", SWT.PUSH );
        addAttributeButton.setLayoutData( createNewButtonGridData() );

        editAttributeButton = toolkit.createButton( attributesTableComposite, "Edit...", SWT.PUSH );
        editAttributeButton.setEnabled( false );
        editAttributeButton.setLayoutData( createNewButtonGridData() );

        deleteAttributeButton = toolkit.createButton( attributesTableComposite, "Delete", SWT.PUSH );
        deleteAttributeButton.setEnabled( false );
        deleteAttributeButton.setLayoutData( createNewButtonGridData() );

        // Aliases Dereferencing Text
        Label aliasesDereferencingLable = toolkit.createLabel( composite, "Aliases\nDereferencing:" );
        aliasesDereferencingLable.setLayoutData( new GridData( SWT.BEGINNING, SWT.TOP, false, false, 1, 2 ) );

        // Finding Base DN Aliases Dereferencing Button
        findingBaseDnAliasesDereferencingButton = toolkit.createButton( composite, "Finding Base DN", SWT.CHECK );
        findingBaseDnAliasesDereferencingButton
            .setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1 ) );

        // Search Aliases Dereferencing Button
        searchAliasesDereferencingButton = toolkit.createButton( composite, "Search", SWT.CHECK );
        searchAliasesDereferencingButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1 ) );
    }


    // ── Keeping The Attribute Buttons In Sync ─────────────────────────────────────────────
    // The Edit and Delete buttons on the attributes table only make sense when something is
    // selected; we enable or disable them based on the current table selection state.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the Edit and Delete attribute buttons based on whether the
     * attributes table has a current selection.
     * Called every time the table selection changes so the buttons always reflect reality.
     *
     * <p>For example — the analyst's table controls mirror what she has highlighted:</p>
     * <pre>
     *   She clicks "cn" in the attributes list — Edit and Delete light up.
     *   She clicks away to deselect — they grey out again.
     * </pre>
     */
    private void updateAttributesButtonsEnableState()
    {
        ISelection selection = attributesTableViewer.getSelection();

        editAttributeButton.setEnabled( !selection.isEmpty() );
        deleteAttributeButton.setEnabled( !selection.isEmpty() );
    }


    // ── Adding A New Attribute To Replicate ───────────────────────────────────────────────
    // The Rebel analyst decides she wants to sync a new attribute and opens the attribute
    // picker dialog.  If she confirms a choice, we add it to the list (deduplicating) and
    // select it in the table so she can see the result.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the attribute-picker dialog, and if the user confirms, adds the chosen attribute
     * to the replication attribute list (if not already present) and refreshes the table.
     * We deduplicate because replicating the same attribute twice would be pointless.
     *
     * <p>For example — the analyst adds "mail" to the sync list:</p>
     * <pre>
     *   She clicks Add; the attribute dialog opens with schema-aware auto-complete.
     *   She picks "mail" and confirms — the table gains a new "mail" row.
     *   If "mail" was already in the list we silently skip the duplicate.
     * </pre>
     */
    private void addNewAttribute()
    {
        AttributeDialog dialog = new AttributeDialog( addAttributeButton.getShell() );
        dialog.addNewElement();
        dialog.setAttributeNamesAndOids( attributeLoader.getAttributeNamesAndOids() );

        if ( AttributeDialog.OK == dialog.open() )
        {
            String newAttribute = dialog.getEditedElement().getValue();

            if ( !attributesList.contains( newAttribute ) )
            {
                attributesList.add( newAttribute );
            }

            attributesTableViewer.refresh();
            attributesTableViewer.setSelection( new StructuredSelection( newAttribute ) );
            masterDetailsBlock.setEditorDirty();
        }
    }


    // ── Editing An Existing Attribute ─────────────────────────────────────────────────────
    // The analyst realises she typed "cnn" instead of "cn" and needs to correct the
    // selected row.  We open the attribute dialog pre-populated with the current value;
    // if she confirms a new name we remove the old one and insert the corrected attribute.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the attribute-picker dialog pre-populated with the currently selected attribute,
     * and if the user confirms, replaces the old attribute with the new one in the list.
     * Also accessible via double-clicking a row in the attributes table.
     *
     * <p>For example — the analyst corrects a typo in the sync list:</p>
     * <pre>
     *   She double-clicks "cnn" in the table; the dialog opens with "cnn" pre-filled.
     *   She corrects it to "cn" and confirms — "cnn" is removed and "cn" takes its place.
     * </pre>
     */
    private void editSelectedAttribute()
    {
        StructuredSelection selection = ( StructuredSelection ) attributesTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            String attribute = ( String ) selection.getFirstElement();

            AttributeDialog dialog = new AttributeDialog( addAttributeButton.getShell() );
            dialog.setEditedElement( new StringValueWrapper( attribute, false ) );
            dialog.setAttributeNamesAndOids( attributeLoader.getAttributeNamesAndOids() );

            if ( AttributeDialog.OK == dialog.open() )
            {
                attributesList.remove( attribute );

                String newAttribute = dialog.getEditedElement().getValue();

                if ( !attributesList.contains( newAttribute ) )
                {
                    attributesList.add( newAttribute );
                }

                attributesTableViewer.refresh();
                attributesTableViewer.setSelection( new StructuredSelection( newAttribute ) );
                masterDetailsBlock.setEditorDirty();
            }
        }
    }


    // ── Dropping An Attribute From The Sync List ──────────────────────────────────────────
    // The analyst decides an attribute is no longer needed in the sync list and removes it
    // without a confirmation dialog — the change is still reversible via the editor's undo.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected attribute from the replication attribute list and
     * refreshes the table viewer.
     * No confirmation dialog — this is a lightweight edit that can be undone by reverting
     * the editor.
     *
     * <p>For example — the analyst drops "telephoneNumber" from the sync list:</p>
     * <pre>
     *   She selects "telephoneNumber" and presses Delete.
     *   The entry vanishes from the table and the editor is marked dirty.
     * </pre>
     */
    private void deleteSelectedAttribute()
    {
        StructuredSelection selection = ( StructuredSelection ) attributesTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            String attribute = ( String ) selection.getFirstElement();

            attributesList.remove( attribute );
            attributesTableViewer.refresh();
            masterDetailsBlock.setEditorDirty();
        }
    }


    // ── Sizing The Attribute Table Buttons ────────────────────────────────────────────────
    // The Add, Edit, and Delete buttons next to the attributes table all need to be the
    // same standard width — we create a shared GridData with the Eclipse-standard button
    // width so the button strip looks tidy regardless of label length.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a {@link GridData} instance sized to the Eclipse standard button width for
     * use on the Add, Edit, and Delete buttons beside the attributes table.
     * Using the same factory for all three keeps the button strip visually consistent.
     *
     * @return  a new {@link GridData} with {@code widthHint} set to
     *          {@link IDialogConstants#BUTTON_WIDTH}
     */
    private GridData createNewButtonGridData()
    {
        GridData gd = new GridData( SWT.FILL, SWT.BEGINNING, false, false );
        gd.widthHint = IDialogConstants.BUTTON_WIDTH;
        return gd;
    }


    // ── Arming Every Control On The Station ───────────────────────────────────────────────
    // The Rebel technician runs a signal cable from every widget on the editing station to
    // the central commit-and-dirty pipeline so that any change flows back into the model
    // and lights up the editor's save indicator.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches all the event listeners (modify, selection, verify, double-click) to every
     * widget on the details panel.
     * Called at the end of {@link #refresh()} after we've populated the widgets from the
     * model, so listeners don't fire spuriously during the population phase.
     *
     * <p>For example — the technician wires every panel control to the commit pipeline:</p>
     * <pre>
     *   Every text field gets a ModifyListener that commits and marks dirty on each keystroke.
     *   Every radio and checkbox gets a SelectionListener that does the same on toggle.
     *   The attributes table gets a double-click listener that opens the edit dialog.
     * </pre>
     */
    private void addListeners()
    {
        enabledCheckbox.addSelectionListener( buttonSelectionListener );
        idText.addModifyListener( textModifyListener );
        descriptionText.addModifyListener( textModifyListener );
        refreshAndPersistModeButton.addSelectionListener( buttonSelectionListener );
        refreshOnlyModeButton.addSelectionListener( buttonSelectionListener );
        refreshIntervalText.addModifyListener( textModifyListener );
        remoteHostText.addModifyListener( textModifyListener );
        remotePortText.addModifyListener( textModifyListener );
        bindDnText.addModifyListener( textModifyListener );
        bindPasswordText.addModifyListener( textModifyListener );
        showPasswordCheckbox.addSelectionListener( showPasswordCheckboxSelectionListener );
        sizeLimitText.addModifyListener( textModifyListener );
        timeLimitText.addModifyListener( textModifyListener );
        useStartTlsCheckbox.addSelectionListener( buttonSelectionListener );
        entryWidget.addWidgetModifyListener( widgetModifyListener );
        filterWidget.addWidgetModifyListener( widgetModifyListener );
        subtreeScopeButton.addSelectionListener( buttonSelectionListener );
        oneLevelScopeButton.addSelectionListener( buttonSelectionListener );
        objectScopeButton.addSelectionListener( buttonSelectionListener );
        allAttributesCheckbox.addSelectionListener( buttonSelectionListener );
        attributesTableViewer.addDoubleClickListener( attributesTableViewerDoubleClickListener );
        attributesTableViewer.addSelectionChangedListener( attributesTableViewerSelectionListener );
        addAttributeButton.addSelectionListener( addAttributeButtonSelectionListener );
        editAttributeButton.addSelectionListener( editAttributeButtonSelectionListener );
        deleteAttributeButton.addSelectionListener( deleteAttributeButtonSelectionListener );
        findingBaseDnAliasesDereferencingButton.addSelectionListener( buttonSelectionListener );
        searchAliasesDereferencingButton.addSelectionListener( buttonSelectionListener );
    }


    // ── Disarming Every Control Before A Data Reload ──────────────────────────────────────
    // Before we repopulate the widgets from a newly selected consumer bean, we cut all the
    // listener cables — otherwise every setText() and setSelection() would trigger a commit
    // that writes half-formed data back into the model mid-load.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all event listeners from every widget on the panel.
     * Must be called at the start of {@link #refresh()} before we push new model data into
     * the widgets — otherwise listeners would commit partial data on every {@code setText()}.
     *
     * <p>For example — the technician cuts the cables before swapping the active dossier:</p>
     * <pre>
     *   She disconnects everything so loading new values doesn't trigger spurious commits.
     *   Once the load is done, addListeners() re-arms all the connections.
     * </pre>
     */
    private void removeListeners()
    {
        enabledCheckbox.removeSelectionListener( buttonSelectionListener );
        idText.removeModifyListener( textModifyListener );
        descriptionText.removeModifyListener( textModifyListener );
        refreshAndPersistModeButton.removeSelectionListener( buttonSelectionListener );
        refreshOnlyModeButton.removeSelectionListener( buttonSelectionListener );
        refreshIntervalText.removeModifyListener( textModifyListener );
        remoteHostText.removeModifyListener( textModifyListener );
        remotePortText.removeModifyListener( textModifyListener );
        bindDnText.removeModifyListener( textModifyListener );
        bindPasswordText.removeModifyListener( textModifyListener );
        showPasswordCheckbox.removeSelectionListener( showPasswordCheckboxSelectionListener );
        sizeLimitText.removeModifyListener( textModifyListener );
        timeLimitText.removeModifyListener( textModifyListener );
        useStartTlsCheckbox.removeSelectionListener( buttonSelectionListener );
        entryWidget.removeWidgetModifyListener( widgetModifyListener );
        filterWidget.removeWidgetModifyListener( widgetModifyListener );
        subtreeScopeButton.removeSelectionListener( buttonSelectionListener );
        oneLevelScopeButton.removeSelectionListener( buttonSelectionListener );
        objectScopeButton.removeSelectionListener( buttonSelectionListener );
        allAttributesCheckbox.removeSelectionListener( buttonSelectionListener );
        attributesTableViewer.removeDoubleClickListener( attributesTableViewerDoubleClickListener );
        attributesTableViewer.removeSelectionChangedListener( attributesTableViewerSelectionListener );
        addAttributeButton.removeSelectionListener( addAttributeButtonSelectionListener );
        editAttributeButton.removeSelectionListener( editAttributeButtonSelectionListener );
        deleteAttributeButton.removeSelectionListener( deleteAttributeButtonSelectionListener );
        findingBaseDnAliasesDereferencingButton.removeSelectionListener( buttonSelectionListener );
        searchAliasesDereferencingButton.removeSelectionListener( buttonSelectionListener );
    }


    // ── Receiving A New Consumer Selection ────────────────────────────────────────────────
    // A Rebel commander taps a different consumer row on the master roster and the editing
    // station gets the news: "here is the new dossier — display it."
    // We extract the bean from the selection and call refresh() to repaint the panel.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the Eclipse forms framework when the master-list selection changes.
     * We extract the newly selected {@link ReplConsumerBean} (or set it to {@code null}
     * for an empty/multi-selection) then call {@link #refresh()} to repopulate the panel.
     *
     * <p>For example — the commander swaps the open dossier on the editing station:</p>
     * <pre>
     *   She clicks "consumer2" on the roster; the selection event fires.
     *   We pull out consumer2's bean and refresh the panel to show its settings.
     *   A multi-selection or no selection sets the bean to null and blanks the panel.
     * </pre>
     *
     * @param part       the form part that fired the event (unused here)
     * @param selection  the new selection from the master table viewer
     */
    public void selectionChanged( IFormPart part, ISelection selection )
    {
        IStructuredSelection ssel = ( IStructuredSelection ) selection;
        if ( ssel.size() == 1 )
        {
            input = ( ReplConsumerBean ) ssel.getFirstElement();
        }
        else
        {
            input = null;
        }
        refresh();
    }


    // ── Flushing All Panel Values Back Into The Model ─────────────────────────────────────
    // The Rebel officer finishes editing and presses Save — every value visible on screen
    // needs to be written back into the ReplConsumerBean so the config model reflects
    // exactly what she typed.  We read each widget and push its value, guarding against
    // parse failures with sensible zero-defaults.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads every widget on the panel and pushes its current value into the in-memory
     * {@link ReplConsumerBean}.
     * Called on every user interaction via the modify/selection listeners so the model
     * stays in sync at all times, not just at an explicit save.
     *
     * <p>For example — the officer logs every field from the briefing panel into the dossier:</p>
     * <pre>
     *   She reads enabled, ID, replication mode, remote host, bind DN, search scope...
     *   Each value goes straight into the corresponding setter on the consumer bean.
     *   Unparseable numeric fields (port, size limit, time limit) safely default to 0.
     * </pre>
     *
     * @param onSave  {@code true} when called as part of an explicit editor save, {@code false}
     *                for live incremental updates triggered by listener events
     */
    public void commit( boolean onSave )
    {
        if ( input != null )
        {
            // Enabled
            input.setEnabled( enabledCheckbox.getSelection() );

            // ID
            input.setReplConsumerId( ServerConfigurationEditorUtils.checkEmptyString( idText.getText() ) );

            // Description
            input.setDescription( ServerConfigurationEditorUtils.checkEmptyString( descriptionText.getText() ) );

            // Refresh Mode
            input.setReplRefreshNPersist( refreshAndPersistModeButton.getSelection() );

            // Refresh Interval
            try
            {
                input.setReplRefreshInterval( Long.parseLong( refreshIntervalText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                input.setReplRefreshInterval( 60000 );
            }

            // Remote Host
            input.setReplProvHostName( ServerConfigurationEditorUtils.checkEmptyString( remoteHostText.getText() ) );

            // Remote Port
            try
            {
                input.setReplProvPort( Integer.parseInt( remotePortText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                input.setReplProvPort( 0 );
            }

            // Bind DN
            input.setReplUserDn( ServerConfigurationEditorUtils.checkEmptyString( bindDnText.getText() ) );

            // Bind Password
            String password = ServerConfigurationEditorUtils.checkEmptyString( bindPasswordText.getText() );

            if ( password != null )
            {
                input.setReplUserPassword( password.getBytes() );
            }
            else
            {
                input.setReplUserPassword( null );
            }

            // Size Limit
            try
            {
                input.setReplSearchSizeLimit( Integer.parseInt( sizeLimitText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                input.setReplSearchSizeLimit( 0 );
            }

            // Time Limit
            try
            {
                input.setReplSearchTimeout( Integer.parseInt( timeLimitText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                input.setReplSearchTimeout( 0 );
            }

            // Use Start TLS
            input.setReplUseTls( useStartTlsCheckbox.getSelection() );

            // Search Base DN
            Dn baseDn = entryWidget.getDn();

            if ( baseDn != null )
            {
                input.setSearchBaseDn( ServerConfigurationEditorUtils.checkEmptyString( baseDn.toString() ) );
            }
            else
            {
                input.setSearchBaseDn( null );
            }

            // Search Filter
            input.setReplSearchFilter( ServerConfigurationEditorUtils.checkEmptyString( filterWidget.getFilter() ) );

            // Search Scope
            SearchScope scope = getSearchScope();

            if ( scope != null )
            {
                input.setReplSearchScope( scope.getLdapUrlValue() );
            }
            else
            {
                input.setReplSearchScope( null );
            }

            // Aliases Dereferencing
            input.setReplAliasDerefMode( getAliasDerefMode().getJndiValue() );

            // Attributes
            List<String> replAttributes = new ArrayList<String>();
            replAttributes.addAll( attributesList );

            // All (User) Attribute
            if ( allAttributesCheckbox.getSelection() )
            {
                replAttributes.add( SchemaConstants.ALL_USER_ATTRIBUTES );
            }

            input.setReplAttributes( replAttributes );
        }
    }


    // ── Reading The Search Scope Radio Buttons ────────────────────────────────────────────
    // Exactly one of three radio buttons (Subtree / One Level / Object) is selected at any
    // time.  We map whichever one is selected to the corresponding SearchScope enum constant.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the three scope radio buttons and returns the corresponding {@link SearchScope}
     * constant, or {@code null} if somehow none is selected (shouldn't happen in practice).
     *
     * <p>For example — the analyst reads the scope dial on the search configuration panel:</p>
     * <pre>
     *   Subtree selected → SearchScope.SUBTREE.
     *   One Level selected → SearchScope.ONELEVEL.
     *   Object selected → SearchScope.OBJECT.
     * </pre>
     *
     * @return  the selected {@link SearchScope}, or {@code null}
     */
    private SearchScope getSearchScope()
    {
        if ( subtreeScopeButton.getSelection() )
        {
            return SearchScope.SUBTREE;
        }
        else if ( oneLevelScopeButton.getSelection() )
        {
            return SearchScope.ONELEVEL;
        }
        else if ( objectScopeButton.getSelection() )
        {
            return SearchScope.OBJECT;
        }

        return null;
    }


    // ── Reading The Alias Dereferencing Checkboxes ────────────────────────────────────────
    // Two checkboxes — "Finding Base DN" and "Search" — combine into four possible
    // AliasDerefMode values.  We map each combination to the corresponding enum constant.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Maps the two alias-dereferencing checkboxes to an {@link AliasDerefMode} constant.
     * Both checked → DEREF_ALWAYS; only Search → DEREF_IN_SEARCHING; only Finding Base DN
     * → DEREF_FINDING_BASE_OBJ; neither → NEVER_DEREF_ALIASES.
     *
     * <p>For example — the analyst maps checkbox states to an LDAP deref mode:</p>
     * <pre>
     *   Both checked → always dereference aliases, wherever they appear.
     *   Neither checked → never follow alias entries during the search.
     * </pre>
     *
     * @return  the {@link AliasDerefMode} corresponding to the current checkbox state
     */
    private AliasDerefMode getAliasDerefMode()
    {
        if ( findingBaseDnAliasesDereferencingButton.getSelection() && searchAliasesDereferencingButton.getSelection() )
        {
            return AliasDerefMode.DEREF_ALWAYS;
        }
        else if ( !findingBaseDnAliasesDereferencingButton.getSelection()
            && searchAliasesDereferencingButton.getSelection() )
        {
            return AliasDerefMode.DEREF_IN_SEARCHING;
        }
        else if ( findingBaseDnAliasesDereferencingButton.getSelection()
            && !searchAliasesDereferencingButton.getSelection() )
        {
            return AliasDerefMode.DEREF_FINDING_BASE_OBJ;
        }
        else if ( !findingBaseDnAliasesDereferencingButton.getSelection()
            && !searchAliasesDereferencingButton.getSelection() )
        {
            return AliasDerefMode.NEVER_DEREF_ALIASES;
        }

        return AliasDerefMode.NEVER_DEREF_ALIASES;
    }


    // ── Cleaning Up When The Station Closes ───────────────────────────────────────────────
    // When the editing station is decommissioned we have nothing extra to release, but the
    // IDetailsPage interface requires this method to exist.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Lifecycle method called when this details page is disposed.
     * We have no resources to clean up here, so this is intentionally empty.
     *
     * @see IDetailsPage#dispose()
     */
    public void dispose()
    {
    }


    // ── Registering The Managed Form ──────────────────────────────────────────────────────
    // Before any widgets can be built, Eclipse hands us the managed form that provides the
    // toolkit and lifecycle.  We store it so createContents() can access the toolkit.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the {@link IManagedForm} reference so we can access the toolkit inside
     * {@link #createContents(Composite)}.
     * Eclipse guarantees this is called before {@code createContents}.
     *
     * @param form  the managed form that owns this details page
     */
    public void initialize( IManagedForm form )
    {
        this.mform = form;
    }


    // ── Reporting Dirty State ─────────────────────────────────────────────────────────────
    // Dirtiness is tracked at the editor level; this page does not maintain its own dirty
    // state.  We always return false and let setEditorDirty() handle the save indicator.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — dirtiness is tracked at the editor level via
     * {@code setEditorDirty()}, not by this details page.
     *
     * @return  {@code false} always
     */
    public boolean isDirty()
    {
        return false;
    }


    // ── Reporting Staleness ───────────────────────────────────────────────────────────────
    // Staleness is handled through the master-list selection change flow, not by this page
    // polling the model independently.  We always return false.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — staleness detection is driven by the selection-change
     * mechanism, not by this page tracking model versions.
     *
     * @return  {@code false} always
     */
    public boolean isStale()
    {
        return false;
    }


    // ── Repainting The Editing Station ────────────────────────────────────────────────────
    // The officer opens a new consumer dossier and the editing station repaints to show its
    // contents: cut listener cables, load every field from the bean, re-arm the cables.
    // We also handle the attributes list specially — stripping out the wildcard entries
    // before populating the table since those are represented by dedicated checkboxes.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all widgets from the currently selected {@link ReplConsumerBean}.
     * The sequence is always: remove listeners → populate widgets → add listeners, to
     * prevent spurious commits during population.
     * Special handling for the search scope and alias deref mode: we parse the stored string
     * values back into enums and set the matching radio/checkbox controls.
     *
     * <p>For example — the officer opens a freshly selected consumer dossier:</p>
     * <pre>
     *   She cuts the commit pipeline, loads each field from the bean, handles edge cases
     *   like an invalid scope string or null alias mode by defaulting to safe values.
     *   The attributes table is repopulated after removing the wildcard placeholders.
     *   Finally she re-arms the pipeline so future edits flow back to the bean.
     * </pre>
     */
    public void refresh()
    {
        removeListeners();

        if ( input != null )
        {
            // Enabled
            enabledCheckbox.setSelection( input.isEnabled() );

            // ID
            idText.setText( ServerConfigurationEditorUtils.checkNull( input.getReplConsumerId() ) );

            // Description
            descriptionText.setText( ServerConfigurationEditorUtils.checkNull( input.getDescription() ) );

            // Refresh And Persist
            refreshAndPersistModeButton.setSelection( input.isReplRefreshNPersist() );

            // Refresh Only
            refreshOnlyModeButton.setSelection( !input.isReplRefreshNPersist() );

            // Refresh Interval
            refreshIntervalText.setText( ServerConfigurationEditorUtils.checkNull( String.valueOf( input
                .getReplRefreshInterval() ) ) );

            // Remote Host
            remoteHostText.setText( ServerConfigurationEditorUtils.checkNull( input.getReplProvHostName() ) );

            // Remote Port
            remotePortText
                .setText( ServerConfigurationEditorUtils.checkNull( String.valueOf( input.getReplProvPort() ) ) );

            // Bind DN
            bindDnText.setText( ServerConfigurationEditorUtils.checkNull( input.getReplUserDn() ) );

            // Bind Password
            byte[] bindPassword = input.getReplUserPassword();

            if ( ( bindPassword != null ) && ( bindPassword.length > 0 ) )
            {
                bindPasswordText.setText( ServerConfigurationEditorUtils.checkNull( new String( input
                    .getReplUserPassword() ) ) );
            }
            else
            {
                bindPasswordText.setText( "" );
            }

            // Size Limit
            sizeLimitText.setText( ServerConfigurationEditorUtils.checkNull( String.valueOf( input
                .getReplSearchSizeLimit() ) ) );

            // Time Limit
            timeLimitText.setText( ServerConfigurationEditorUtils.checkNull( String.valueOf( input
                .getReplSearchTimeout() ) ) );

            // Use Start TLS
            useStartTlsCheckbox.setSelection( input.isReplUseTls() );

            // Search Base DN
            try
            {
                entryWidget.setInput( browserConnection, new Dn( input.getSearchBaseDn() ) );
            }
            catch ( LdapInvalidDnException e )
            {
                entryWidget.setInput( browserConnection, Dn.EMPTY_DN );
            }

            // Search Filter
            filterWidget.setFilter( ServerConfigurationEditorUtils.checkNull( input.getReplSearchFilter() ) );

            // Search Scope
            SearchScope scope = null;
            try
            {
                scope = SearchScope.getSearchScope( SearchScope.getSearchScope( input.getReplSearchScope() ) );
            }
            catch ( IllegalArgumentException e )
            {
                scope = null;
            }

            if ( scope != null )
            {
                switch ( scope )
                {
                    case SUBTREE:
                        subtreeScopeButton.setSelection( true );
                        oneLevelScopeButton.setSelection( false );
                        objectScopeButton.setSelection( false );
                        break;
                    case ONELEVEL:
                        subtreeScopeButton.setSelection( false );
                        oneLevelScopeButton.setSelection( true );
                        objectScopeButton.setSelection( false );
                        break;
                    case OBJECT:
                        subtreeScopeButton.setSelection( false );
                        oneLevelScopeButton.setSelection( false );
                        objectScopeButton.setSelection( true );
                        break;
                }
            }
            else
            {
                subtreeScopeButton.setSelection( true );
            }

            // Aliases Dereferencing
            AliasDerefMode aliasDerefMode = null;
            try
            {
                aliasDerefMode = AliasDerefMode.getDerefMode( input.getReplAliasDerefMode() );
            }
            catch ( IllegalArgumentException e )
            {
                aliasDerefMode = null;
            }

            if ( aliasDerefMode != null )
            {
                switch ( aliasDerefMode )
                {
                    case DEREF_ALWAYS:
                        findingBaseDnAliasesDereferencingButton.setSelection( true );
                        searchAliasesDereferencingButton.setSelection( true );
                        break;
                    case DEREF_FINDING_BASE_OBJ:
                        findingBaseDnAliasesDereferencingButton.setSelection( true );
                        searchAliasesDereferencingButton.setSelection( false );
                        break;
                    case DEREF_IN_SEARCHING:
                        findingBaseDnAliasesDereferencingButton.setSelection( false );
                        searchAliasesDereferencingButton.setSelection( true );
                        break;
                    case NEVER_DEREF_ALIASES:
                        findingBaseDnAliasesDereferencingButton.setSelection( false );
                        searchAliasesDereferencingButton.setSelection( false );
                        break;
                }
            }
            else
            {
                findingBaseDnAliasesDereferencingButton.setSelection( true );
                searchAliasesDereferencingButton.setSelection( true );
            }

            // Attributes
            attributesList.clear();
            attributesList.addAll( input.getReplAttributes() );

            // All Attributes Checkbox
            if ( attributesList.contains( SchemaConstants.ALL_USER_ATTRIBUTES ) )
            {
                attributesList.remove( SchemaConstants.ALL_USER_ATTRIBUTES );
                allAttributesCheckbox.setSelection( true );
            }
            else
            {
                allAttributesCheckbox.setSelection( false );
            }

            // All Operational Attributes
            if ( attributesList.contains( SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES ) )
            {
                attributesList.remove( SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES );
            }

            attributesTableViewer.refresh();
        }

        addListeners();
    }


    // ── Directing Keyboard Focus ───────────────────────────────────────────────────────────
    // When the editing station becomes active, we send keyboard focus to the ID field so
    // the analyst can start typing or tabbing right away without needing to click first.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets keyboard focus to the consumer ID text field when this details page becomes active.
     *
     * @see IDetailsPage#setFocus()
     */
    public void setFocus()
    {
        idText.setFocus();
    }


    // ── Refusing External Form Input ──────────────────────────────────────────────────────
    // If the forms framework tries to push an external object into this panel we decline —
    // we only respond to master-list selections, not programmatic input pushes.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} because this details page does not accept externally
     * pushed form input — it only reacts to master-list selection events.
     *
     * @param input  the input object being offered (ignored)
     * @return       {@code false} always
     */
    public boolean setFormInput( Object input )
    {
        return false;
    }
}
