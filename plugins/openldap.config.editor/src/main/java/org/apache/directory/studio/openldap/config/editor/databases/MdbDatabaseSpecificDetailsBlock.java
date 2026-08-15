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
package org.apache.directory.studio.openldap.config.editor.databases;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.ui.CommonUIConstants;
import org.apache.directory.studio.common.ui.CommonUIPlugin;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.common.ui.widgets.TableWidget;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.openldap.config.editor.wrappers.DbIndexDecorator;
import org.apache.directory.studio.openldap.config.editor.wrappers.DbIndexWrapper;
import org.apache.directory.studio.openldap.config.model.database.OlcMdbConfig;
import org.apache.directory.studio.openldap.common.ui.widgets.BooleanWithDefaultWidget;
import org.apache.directory.studio.openldap.common.ui.widgets.DirectoryBrowserWidget;
import org.apache.directory.studio.openldap.common.ui.widgets.UnixPermissionsWidget;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: MdbDatabaseSpecificDetailsBlock — Palpatine's Memory-Mapped Archives ──
// The Empire eventually upgraded from the older Berkeley vaults to a faster,
// simpler memory-mapped data store: MDB (Lightning Memory-Mapped Database).
// LMDB is leaner than BDB — no transaction log, no deadlock manager, just a
// single memory-mapped file that the OS keeps in RAM as long as possible.
// This block configures the MDB backend across four collapsible sections:
// configuration (directory + mode), indexes, limits, and options.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Database-specific UI block for the OpenLDAP MDB (Lightning Memory-Mapped Database)
 * backend ({@code olcMdbConfig}).
 * MDB is OpenLDAP's modern, high-performance storage engine. It uses a single
 * memory-mapped file — no separate transaction log, no BDB locking overhead.
 * We expose its configuration in four collapsible sections: MDB Configuration
 * (directory + file mode), Database Indices, Database Limits (readers, size,
 * entry size, stack depth, checkpoint), and Database Options (no-sync toggle
 * and environment flags).
 * The {@code olcDbDirectory} field is mandatory and therefore displayed in red bold.
 * Think of this as Palpatine's upgraded memory-mapped Imperial archives —
 * faster reads, simpler writes, same absolute authority over data.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MdbDatabaseSpecificDetailsBlock extends AbstractDatabaseSpecificDetailsBlock<OlcMdbConfig>
{
    // UI Widgets
    /** The olcDbDirectory attribute (String) */
    private DirectoryBrowserWidget directoryBrowserWidget;

    /** The olcDbCheckpoint attribute (String) */
    private Text checkpointText;

    /** The olcDbEnvFlags attribute (String, multi-values) */
    private Text envFlagsText;

    /** The olcDbIndex attribute (String, multi-values) */
    private TableWidget<DbIndexWrapper> indicesWidget;

    /** The olcMaxEntrySize attribute (Integer) No yet available (2.4.41) */
    private Text maxEntrySizeText;

    /** The olcDbMaxReaders attribute (Integer) */
    private Text maxReadersText;

    /** The olcMaxSize attribute (Long) */
    private Text maxSizeText;

    /** The olcDbMode attribute (String) */
    private UnixPermissionsWidget modeUnixPermissionsWidget;

    /** The olcDbNoSync attribute (Boolean) */
    private BooleanWithDefaultWidget disableSynchronousDatabaseWritesBooleanWithDefaultWidget;

    /** The olcDbSearchStack attribute( Integer) */
    private Text searchStackDepthText;


    // ── Listen for Index Table Changes ────────────────────────────────────────
    // When the user adds, edits, or removes an index row, we collect all
    // current rows and push the updated list directly into the model.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * The {@code olcAllows} listener — fires whenever the indices table changes.
     * Collects the current list of {@link DbIndexWrapper} elements and writes
     * them back to the model's {@code olcDbIndex} attribute.
     */
    private WidgetModifyListener indexesListener = event ->
        {
            List<String> indices = new ArrayList<>();

            for ( DbIndexWrapper dbIndex : indicesWidget.getElements() )
            {
                indices.add( dbIndex.toString() );
            }

            database.setOlcDbIndex( indices );
        };


    // ── Commission the Memory-Mapped Archive ─────────────────────────────────
    // The Imperial archivist for the MDB vault receives their assignment:
    // manage this particular MDB database. We bind to the parent details page,
    // the OlcMdbConfig model, and the live LDAP connection for any pickers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new MDB database block, linking it to the parent details page,
     * the MDB model object, and the live browser connection.
     *
     * @param detailsPage       the parent details page that receives dirty signals
     * @param database          the {@link OlcMdbConfig} model we are editing
     * @param browserConnection the live LDAP connection for entry-picker widgets and schema checks
     */
    public MdbDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, OlcMdbConfig database,
        IBrowserConnection browserConnection )
    {
        super( detailsPage, database, browserConnection );
    }


    // ── Build the Four-Section Control Console ────────────────────────────────
    // The archivist lays out four collapsible vault-management panels:
    // configuration, indexes, limits, and options.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the complete block UI — four collapsible {@link Section} panels
     * covering every configurable aspect of an MDB database.
     *
     * @param parent   the parent composite to attach our content to
     * @param toolkit  the JFace Forms toolkit for styled widget creation
     * @return         the top-level composite that wraps all four sections
     */
    public Composite createBlockContent( Composite parent, FormToolkit toolkit )
    {
        // Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        createDatabaseConfigurationSection( composite, toolkit );
        createDatabaseIndexesSection( composite, toolkit );
        createDatabaseLimitsSection( composite, toolkit );
        createDatabaseOptionsSection( composite, toolkit );

        return composite;
    }


    // ── Build the MDB Configuration Console ──────────────────────────────────
    // The primary configuration panel: directory (where the mdb file lives,
    // rendered in bold red because it is a required attribute) and file mode
    // (Unix permissions on the mdb file). These are the two mandatory knobs
    // before MDB can serve any data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "MDB Configuration" section covering {@code olcDbDirectory}
     * (required — shown in red bold) and {@code olcDbMode} (Unix permissions).
     * We manage the following configuration elements:
     * <ul>
     * <li>Directory : the directory on disk where the file will be stored</li>
     * <li>mode : the file mode for this directory</li>
     * </ul>
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createDatabaseConfigurationSection( Composite parent, FormToolkit toolkit )
    {
        // Database Configuration Section
        Section databaseConfigurationSection = toolkit.createSection( parent, Section.TWISTIE );
        databaseConfigurationSection.setText( Messages.getString( "OpenLDAPMDBConfiguration.Section" ) );
        databaseConfigurationSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite databaseConfigurationComposite = toolkit.createComposite( databaseConfigurationSection );
        toolkit.paintBordersFor( databaseConfigurationComposite );
        databaseConfigurationComposite.setLayout( new GridLayout( 2, false ) );
        databaseConfigurationSection.setClient( databaseConfigurationComposite );

        // Directory Text. This is a MUST attribute (it will be red and bold)
        Label olcDirectory = toolkit.createLabel( databaseConfigurationComposite, Messages.getString( "OpenLDAPMDBConfiguration.Directory" ) );
        olcDirectory.setForeground( CommonUIPlugin.getDefault().getColor( CommonUIConstants.ERROR_COLOR ) );
        FontDescriptor boldDescriptor = FontDescriptor.createFrom( olcDirectory.getFont() ).setStyle( SWT.BOLD );
        Font boldFont = boldDescriptor.createFont( olcDirectory.getDisplay() );
        olcDirectory.setFont( boldFont );
        Composite directoryComposite = toolkit.createComposite( databaseConfigurationComposite );
        GridLayout directoryCompositeGridLayout = new GridLayout( 2, false );
        directoryCompositeGridLayout.marginHeight = directoryCompositeGridLayout.marginWidth = 0;
        directoryCompositeGridLayout.verticalSpacing = 0;
        directoryComposite.setLayout( directoryCompositeGridLayout );
        directoryComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        directoryBrowserWidget = new DirectoryBrowserWidget( "" );
        directoryBrowserWidget.createWidget( directoryComposite, toolkit );

        // Mode Text
        toolkit.createLabel( databaseConfigurationComposite, Messages.getString( "OpenLDAPMDBConfiguration.Mode" ) );
        modeUnixPermissionsWidget = new UnixPermissionsWidget();
        modeUnixPermissionsWidget.create( databaseConfigurationComposite, toolkit );
        modeUnixPermissionsWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Build the Indexing Console ────────────────────────────────────────────
    // MDB can maintain secondary indexes (B-tree sorted values for specific
    // attributes) to speed up filtered searches. We show a table widget that
    // lets the operator add, edit, and remove index definitions. Each row maps
    // to an {@code olcDbIndex} entry in the config.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Indices" section — a {@link TableWidget} of
     * {@link DbIndexWrapper} items representing the {@code olcDbIndex} multi-value attribute.
     * Covers: {@code olcDbIndex}.
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createDatabaseIndexesSection( Composite parent, FormToolkit toolkit )
    {
        // Database Indices Section
        Section databaseIndexesSection = toolkit.createSection( parent, Section.TWISTIE );
        databaseIndexesSection.setText( Messages.getString( "OpenLDAPMDBConfiguration.IndicesSection" ) );
        databaseIndexesSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite databaseIndexesComposite = toolkit.createComposite( databaseIndexesSection );
        toolkit.paintBordersFor( databaseIndexesComposite );
        databaseIndexesComposite.setLayout( new GridLayout( 2, false ) );
        databaseIndexesSection.setClient( databaseIndexesComposite );

        // Indices Widget
        indicesWidget = new TableWidget<>( new DbIndexDecorator( null, browserConnection ) );
        indicesWidget.createWidgetWithEdit( databaseIndexesComposite, toolkit );
        indicesWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        indicesWidget.addWidgetModifyListener( indexesListener );
    }


    // ── Build the Limits Console ──────────────────────────────────────────────
    // Operational ceilings for the MDB store: max concurrent reader threads,
    // max file size (the memory-map reservation), optional max entry size
    // (only available if the schema declares olcDbMaxEntrySize — added in 2.4.41),
    // search recursion depth, and checkpoint interval.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Limits" section covering:
     * <ul>
     * <li>{@code olcDbCheckpoint} — checkpoint interval</li>
     * <li>{@code olcDbMaxEntrySize} — max per-entry size (OpenLDAP 2.4.41+ only,
     *     created only if the schema has the attribute type)</li>
     * <li>{@code olcDbMaxReaders} — maximum number of concurrent reader threads</li>
     * <li>{@code olcDbMaxSize} — maximum total memory-map size</li>
     * <li>{@code olcDbSearchStack} — maximum search recursion depth</li>
     * </ul>
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createDatabaseLimitsSection( Composite parent, FormToolkit toolkit )
    {
        // Database Limits Section
        Section databaseLimitsSection = toolkit.createSection( parent, Section.TWISTIE );
        databaseLimitsSection.setText( "Database Limits" );
        databaseLimitsSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite databaseLimitsComposite = toolkit.createComposite( databaseLimitsSection );
        toolkit.paintBordersFor( databaseLimitsComposite );
        databaseLimitsComposite.setLayout( new GridLayout( 2, false ) );
        databaseLimitsSection.setClient( databaseLimitsComposite );

        // Max Readers Text
        toolkit.createLabel( databaseLimitsComposite, "Maximum Readers:" );
        maxReadersText = BaseWidgetUtils.createIntegerText( toolkit, databaseLimitsComposite );
        maxReadersText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Max Size Text
        toolkit.createLabel( databaseLimitsComposite, "Maximum Size:" );
        maxSizeText = BaseWidgetUtils.createIntegerText( toolkit, databaseLimitsComposite );
        maxSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        if ( browserConnection.getSchema().hasAttributeTypeDescription( "olcDbMaxEntrySize" ) )
        {
            // Max Entry Size Text
            toolkit.createLabel( databaseLimitsComposite, "Maximum Entry Size:" );
            maxEntrySizeText = BaseWidgetUtils.createIntegerText( toolkit, databaseLimitsComposite );
            maxEntrySizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        }

        // Search Stack Depth Text
        toolkit.createLabel( databaseLimitsComposite, "Search Stack Depth:" );
        searchStackDepthText = BaseWidgetUtils.createIntegerText( toolkit, databaseLimitsComposite );
        searchStackDepthText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Checkpoint Text
        toolkit.createLabel( databaseLimitsComposite, "Checkpoint Interval:" );
        checkpointText = toolkit.createText( databaseLimitsComposite, "" );
        checkpointText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Build the Options Console ─────────────────────────────────────────────
    // The behavioral override panel: whether MDB should skip the OS sync after
    // each commit (olcDbNoSync — faster but riskier on crash), plus environment
    // flags added in OpenLDAP 2.4.33 (not yet wired up in the UI).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Options" section covering:
     * <ul>
     * <li>{@code olcDbNoSync} — disable synchronous database writes (faster but less durable)</li>
     * <li>{@code olcDbEnvFlags} — environment flags (OpenLDAP 2.4.33+, UI not yet wired)</li>
     * </ul>
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createDatabaseOptionsSection( Composite parent, FormToolkit toolkit )
    {
        // Database Options Section
        Section databaseOptionsSection = toolkit.createSection( parent, Section.TWISTIE );
        databaseOptionsSection.setText( "Database Options" );
        databaseOptionsSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite databaseOptionsComposite = toolkit.createComposite( databaseOptionsSection );
        toolkit.paintBordersFor( databaseOptionsComposite );
        databaseOptionsComposite.setLayout( new GridLayout( 2, false ) );
        databaseOptionsSection.setClient( databaseOptionsComposite );

        // Disable Synchronous Database Writes Widget
        toolkit.createLabel( databaseOptionsComposite, "Disable Synchronous Database Writes:" );
        disableSynchronousDatabaseWritesBooleanWithDefaultWidget = new BooleanWithDefaultWidget( false );
        disableSynchronousDatabaseWritesBooleanWithDefaultWidget.create( databaseOptionsComposite, toolkit );
        disableSynchronousDatabaseWritesBooleanWithDefaultWidget.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Env flags here...
    }


    // ── Re-read All Archive Settings from the Model ───────────────────────────
    // The archivist walks through every control panel and updates it to match
    // the current OlcMdbConfig model state. For the optional olcDbMaxEntrySize
    // field, we only touch it if the schema knows about that attribute.
    // Listeners are paused during this sweep to avoid spurious dirty signals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all UI controls from the current {@link OlcMdbConfig} model.
     * Every widget in all four sections is updated to match the model.
     * Null model values become safe display defaults (empty strings / false).
     * The {@code maxEntrySizeText} widget is only updated when the connected
     * schema advertises the {@code olcDbMaxEntrySize} attribute type.
     */
    public void refresh()
    {
        removeListeners();

        if ( database != null )
        {
            // Directory Text
            String directory = database.getOlcDbDirectory();
            directoryBrowserWidget.setDirectoryPath( ( directory == null ) ? "" : directory );

            // Mode Text
            String mode = database.getOlcDbMode();
            modeUnixPermissionsWidget.setValue( mode );

            // Indices Text
            List<DbIndexWrapper> dbIndexWrappers = new ArrayList<>();

            for ( String index : database.getOlcDbIndex() )
            {
                dbIndexWrappers.add( new DbIndexWrapper( index ) );
            }

            indicesWidget.setElements( dbIndexWrappers );

            // Max Readers Text
            Integer maxReaders = database.getOlcDbMaxReaders();
            maxReadersText.setText( ( maxReaders == null ) ? "" : maxReaders.toString() ); //$NON-NLS-1$

            // Max Size Text
            Long maxSize = database.getOlcDbMaxSize();
            maxSizeText.setText( ( maxSize == null ) ? "" : maxSize.toString() ); //$NON-NLS-1$

            // Search Stack Depth Text
            Integer searchStackDepth = database.getOlcDbSearchStack();
            searchStackDepthText.setText( ( searchStackDepth == null ) ? "" : searchStackDepth.toString() ); //$NON-NLS-1$

            // Checkpoint Text
            String checkpoint = database.getOlcDbCheckpoint();
            checkpointText.setText( ( checkpoint == null ) ? "" : checkpoint ); //$NON-NLS-1$

            // Disable Synchronous Database Writes Widget
            disableSynchronousDatabaseWritesBooleanWithDefaultWidget.setValue( database.getOlcDbNoSync() );

            // MaxEntrySize Text
            if ( browserConnection.getSchema().hasAttributeTypeDescription( "olcDbMaxEntrySize" ) )
            {
                // Max Entry Size Text
                Integer maxEntrySize = database.getOlcDbMaxEntrySize();

                if ( maxEntrySize != null )
                {
                    maxEntrySizeText.setText( maxEntrySize.toString() );
                }
            }
        }

        addListeners();
    }


    // ── Start Watching All Controls ───────────────────────────────────────────
    // Every widget in all four sections gets a dirty listener attached.
    // If the schema exposes olcDbMaxEntrySize, that field gets a listener too.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches dirty listeners to every widget in all four sections.
     * The {@code maxEntrySizeText} listener is only attached when the schema
     * advertises {@code olcDbMaxEntrySize}. Any user interaction with any
     * control propagates a "editor is dirty" signal to the parent.
     */
    private void addListeners()
    {
        directoryBrowserWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
        modeUnixPermissionsWidget.addWidgetModifyListener( dirtyWidgetModifyListener );

        indicesWidget.addWidgetModifyListener( dirtyWidgetModifyListener );

        maxReadersText.addModifyListener( dirtyModifyListener );
        maxSizeText.addModifyListener( dirtyModifyListener );

        if ( browserConnection.getSchema().hasAttributeTypeDescription( "olcDbMaxEntrySize" ) )
        {
            maxEntrySizeText.addModifyListener( dirtyModifyListener );
        }

        searchStackDepthText.addModifyListener( dirtyModifyListener );
        checkpointText.addModifyListener( dirtyModifyListener );

        disableSynchronousDatabaseWritesBooleanWithDefaultWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Stop Watching All Controls ────────────────────────────────────────────
    // All dirty listeners are detached before we update controls from the model,
    // preventing false dirty events. Schema-conditional listeners are also removed
    // only when the schema has the corresponding attribute type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all dirty listeners from every widget in all four sections.
     * Called before populating controls from the model so that programmatic
     * updates do not trigger spurious "editor is dirty" events.
     * The {@code maxEntrySizeText} listener is only removed when the schema
     * advertises {@code olcDbMaxEntrySize}.
     */
    private void removeListeners()
    {
        directoryBrowserWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
        modeUnixPermissionsWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );

        indicesWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );

        maxReadersText.removeModifyListener( dirtyModifyListener );
        maxSizeText.removeModifyListener( dirtyModifyListener );

        if ( browserConnection.getSchema().hasAttributeTypeDescription( "olcDbMaxEntrySize" ) )
        {
            maxEntrySizeText.removeModifyListener( dirtyModifyListener );
        }

        searchStackDepthText.removeModifyListener( dirtyModifyListener );
        checkpointText.removeModifyListener( dirtyModifyListener );

        disableSynchronousDatabaseWritesBooleanWithDefaultWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Commit All Archive Settings to the Model ──────────────────────────────
    // The archivist reads every control and stamps its value into the official
    // OlcMdbConfig record. Empty/unparseable numbers become null in the model.
    // The directory widget also saves its browse-dialog history on commit.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the current state of every UI control back into the {@link OlcMdbConfig} model.
     * Empty numeric fields are stored as null. The directory widget saves its dialog
     * settings (browse history) on commit. The index list is rebuilt from the
     * table widget's current rows. The optional {@code olcDbMaxEntrySize} field is
     * only committed when the schema advertises that attribute.
     *
     * @param onSave  {@code true} when triggered by a full editor save;
     *                {@code false} for page-change commits
     */
    public void commit( boolean onSave )
    {
        // Directory
        String directory = directoryBrowserWidget.getDirectoryPath();

        if ( Strings.isEmpty( directory ) )
        {
            database.setOlcDbDirectory( null );
        }
        else
        {
            database.setOlcDbDirectory( directory );
        }

        directoryBrowserWidget.saveDialogSettings();

        // Mode
        database.setOlcDbMode( modeUnixPermissionsWidget.getValue() );

        // Indices
        database.clearOlcDbIndex();

        for ( DbIndexWrapper dbIndexWrapper : indicesWidget.getElements() )
        {
            database.addOlcDbIndex( dbIndexWrapper.toString() );
        }

        // Max readers
        try
        {
            database.setOlcDbMaxReaders( Integer.parseInt( maxReadersText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbMaxReaders( null );
        }

        // Max Size
        try
        {
            database.setOlcDbMaxSize( Long.parseLong( maxSizeText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbMaxSize( null );
        }

        // Max Entry Size
        if ( browserConnection.getSchema().hasAttributeTypeDescription( "olcDbMaxEntrySize" ) )
        {
            try
            {
                database.setOlcDbMaxEntrySize( Integer.parseInt( maxEntrySizeText.getText() ) );
            }
            catch ( NumberFormatException e )
            {
                database.setOlcDbMaxEntrySize( null );
            }
        }

        // Search Stack Depth
        try
        {
            database.setOlcDbSearchStack( Integer.parseInt( searchStackDepthText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbSearchStack( null );
        }

        // Checkpoint Interval
        database.setOlcDbCheckpoint( checkpointText.getText() );

        // Disable Synchronous Database Writes
        database.setOlcDbNoSync( disableSynchronousDatabaseWritesBooleanWithDefaultWidget.getValue() );
    }
}
