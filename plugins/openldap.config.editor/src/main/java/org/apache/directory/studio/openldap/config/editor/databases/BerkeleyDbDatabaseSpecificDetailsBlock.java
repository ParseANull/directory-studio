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
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.apache.directory.studio.openldap.common.ui.widgets.BooleanWithDefaultWidget;
import org.apache.directory.studio.openldap.common.ui.widgets.DirectoryBrowserWidget;
import org.apache.directory.studio.openldap.common.ui.widgets.FileBrowserWidget;
import org.apache.directory.studio.openldap.common.ui.widgets.UnixPermissionsWidget;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginUtils;
import org.apache.directory.studio.openldap.config.editor.dialogs.DbConfigurationDialog;
import org.apache.directory.studio.openldap.config.model.database.OlcBdbConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcBdbConfigLockDetectEnum;
import org.apache.directory.studio.openldap.config.model.widgets.IndicesWidget;
import org.apache.directory.studio.openldap.config.model.widgets.LockDetectWidget;


// ── CLASS: BerkeleyDbDatabaseSpecificDetailsBlock — Palpatine's Imperial Archives ──
// The Empire kept its most critical records in the massive Berkeley vaults:
// indexed, encrypted, precisely tuned for performance, and locked down with
// sophisticated concurrency controls. Berkeley DB (BDB/HDB) is OpenLDAP's
// most feature-rich storage backend, giving administrators control over
// caching, indexing, encryption, checkpointing, and deadlock handling.
// This block exposes all of those controls in five collapsible form sections.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Database-specific UI block for the OpenLDAP Berkeley DB backends
 * ({@code olcBdbConfig} / {@code olcHdbConfig}).
 * BDB is the original high-performance storage backend for OpenLDAP, offering
 * fine-grained control over caching, indexing strategies, file encryption,
 * shared-memory keys, checkpoint intervals, page sizes, and deadlock detection.
 * We organise all those knobs into five collapsible sections:
 * Database Configuration, Database Indices, Database Cache,
 * Database Limits, and Database Options.
 * Think of it as Palpatine's Imperial archives — enormous, meticulously indexed,
 * encrypted, and tuned to serve the galaxy at scale.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BerkeleyDbDatabaseSpecificDetailsBlock<BDB extends OlcBdbConfig> extends
    AbstractDatabaseSpecificDetailsBlock<BDB>
{
    // UI Widgets
    private DirectoryBrowserWidget directoryBrowserWidget;
    private UnixPermissionsWidget modeUnixPermissionsWidget;
    private Button editConfigurationButton;
    private FileBrowserWidget cryptFileBrowserWidget;
    private Text cryptKeyText;
    private Text sharedMemoryKeyText;
    private IndicesWidget indicesWidget;
    private BooleanWithDefaultWidget linearIndexBooleanWithDefaultWidget;
    private Text cacheSizeText;
    private Text cacheFreeText;
    private Text dnCacheSizeText;
    private Text idlCacheSizeText;
    private Text searchStackDepthText;
    private Text pageSizeText;
    private Text checkpointText;
    private BooleanWithDefaultWidget disableSynchronousDatabaseWritesBooleanWithDefaultWidget;
    private BooleanWithDefaultWidget allowReadsOfUncommitedDataBooleanWithDefaultWidget;
    private LockDetectWidget lockDetectWidget;

    // Listeners
    private SelectionListener editConfigurationButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            DbConfigurationDialog dialog = new DbConfigurationDialog( editConfigurationButton.getShell(),
                database.getOlcDbConfig().toArray( new String[0] ) );

            if ( dialog.open() == DbConfigurationDialog.OK )
            {
                List<String> newConfiguration = new ArrayList<>();

                String[] configurationFromDialog = dialog.getConfiguration();

                if ( configurationFromDialog.length > 0 )
                {
                    for ( String configurationLineFromDialog : configurationFromDialog )
                    {
                        newConfiguration.add( configurationLineFromDialog );
                    }
                }

                database.setOlcDbConfig( newConfiguration );
                detailsPage.setEditorDirty();
            }
        }
    };


    // ── Commission the Imperial Archive ──────────────────────────────────────
    // The Imperial archivist receives their assignment: manage this particular
    // Berkeley DB vault. We bind the block to its parent details page, the
    // OlcBdbConfig model, and the live LDAP connection used for DN pickers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new Berkeley DB block, linking it to the parent details page,
     * the BDB model object, and the live browser connection.
     * A browser connection is needed here because BDB config includes DN-based
     * settings that require an entry picker.
     *
     * @param detailsPage       the parent details page that receives dirty signals
     * @param database          the {@link OlcBdbConfig} (or subtype) model we are editing
     * @param browserConnection the live LDAP connection for DN picker widgets
     */
    public BerkeleyDbDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, BDB database,
        IBrowserConnection browserConnection )
    {
        super( detailsPage, database, browserConnection );
    }


    // ── Build the Five-Section Control Console ────────────────────────────────
    // The archivist lays out the five vault-management consoles side by side:
    // configuration, indexes, cache, limits, and options. Each section can be
    // collapsed so the operator can focus on one subsystem at a time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the complete block UI — five collapsible {@link Section} panels
     * covering every configurable aspect of a Berkeley DB database.
     *
     * @param parent   the parent composite to attach our content to
     * @param toolkit  the JFace Forms toolkit for styled widget creation
     * @return         the top-level composite that wraps all five sections
     */
    public Composite createBlockContent( Composite parent, FormToolkit toolkit )
    {
        // Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        createDatabaseConfigurationSection( composite, toolkit );
        createDatabaseIndexesSection( composite, toolkit );
        createDatabaseCacheSection( composite, toolkit );
        createDatabaseLimitsSection( composite, toolkit );
        createDatabaseOptionsSection( composite, toolkit );

        return composite;
    }


    // ── Build the Configuration Console ──────────────────────────────────────
    // The main vault-management panel: where files are stored, what permissions
    // they have, the raw BDB config directives, the encryption file and key,
    // and the shared-memory segment key used for inter-process communication.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Configuration" section — the primary set of controls
     * covering filesystem location, file permissions, raw {@code olcDbConfig} directives,
     * encryption file, encryption key, and shared-memory key.
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createDatabaseConfigurationSection( Composite parent, FormToolkit toolkit )
    {
        // Database Configuration Section
        Section databaseConfigurationSection = toolkit.createSection( parent, Section.TWISTIE );
        databaseConfigurationSection.setText( "Database Configuration" );
        databaseConfigurationSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite databaseConfigurationComposite = toolkit.createComposite( databaseConfigurationSection );
        toolkit.paintBordersFor( databaseConfigurationComposite );
        databaseConfigurationComposite.setLayout( new GridLayout( 2, false ) );
        databaseConfigurationSection.setClient( databaseConfigurationComposite );

        // Directory Text
        toolkit.createLabel( databaseConfigurationComposite, "Directory:" );
        Composite directoryComposite = toolkit.createComposite( databaseConfigurationComposite );
        GridLayout directoryCompositeGridLayout = new GridLayout( 2, false );
        directoryCompositeGridLayout.marginHeight = directoryCompositeGridLayout.marginWidth = 0;
        directoryCompositeGridLayout.verticalSpacing = 0;
        directoryComposite.setLayout( directoryCompositeGridLayout );
        directoryComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        directoryBrowserWidget = new DirectoryBrowserWidget( "" );
        directoryBrowserWidget.createWidget( directoryComposite, toolkit );

        // Mode Text
        toolkit.createLabel( databaseConfigurationComposite, "Mode:" );
        modeUnixPermissionsWidget = new UnixPermissionsWidget();
        modeUnixPermissionsWidget.create( databaseConfigurationComposite, toolkit );
        modeUnixPermissionsWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Configuration Text
        toolkit.createLabel( databaseConfigurationComposite, "Configuration:" );
        editConfigurationButton = toolkit.createButton( databaseConfigurationComposite, "Edit Configuration...",
            SWT.PUSH );
        editConfigurationButton.addSelectionListener( editConfigurationButtonSelectionListener );

        // Crypt File Text
        toolkit.createLabel( databaseConfigurationComposite, "Crypt File:" );
        Composite cryptFileComposite = toolkit.createComposite( databaseConfigurationComposite );
        GridLayout cryptFileCompositeGridLayout = new GridLayout( 2, false );
        cryptFileCompositeGridLayout.marginHeight = cryptFileCompositeGridLayout.marginWidth = 0;
        cryptFileCompositeGridLayout.verticalSpacing = 0;
        cryptFileComposite.setLayout( cryptFileCompositeGridLayout );
        cryptFileComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        cryptFileBrowserWidget = new FileBrowserWidget( "", new String[0],
            FileBrowserWidget.TYPE_OPEN );
        cryptFileBrowserWidget.createWidget( cryptFileComposite, toolkit );

        // Crypt Key Text
        toolkit.createLabel( databaseConfigurationComposite, "Crypt Key:" );
        cryptKeyText = toolkit.createText( databaseConfigurationComposite, "" );
        cryptKeyText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Shared Memory Key Text
        toolkit.createLabel( databaseConfigurationComposite, "Shared Memory Key:" );
        sharedMemoryKeyText = BaseWidgetUtils.createIntegerText( toolkit, databaseConfigurationComposite );
        sharedMemoryKeyText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Build the Indexing Console ────────────────────────────────────────────
    // The index management panel: the multi-value indices widget lets the
    // operator define which attributes get BDB indices and what kind (eq, sub,
    // approx, pres). The linear-index toggle switches between the default
    // two-pass indexing and a single-pass linear index (faster for writes).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Indices" section — an {@link IndicesWidget} for multi-value
     * {@code olcDbIndex} entries plus a {@link BooleanWithDefaultWidget} for
     * {@code olcDbLinearIndex}.
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createDatabaseIndexesSection( Composite parent, FormToolkit toolkit )
    {
        // Database Indices Section
        Section databaseIndexesSection = toolkit.createSection( parent, Section.TWISTIE );
        databaseIndexesSection.setText( "Database Indices" );
        databaseIndexesSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite databaseIndexesComposite = toolkit.createComposite( databaseIndexesSection );
        toolkit.paintBordersFor( databaseIndexesComposite );
        databaseIndexesComposite.setLayout( new GridLayout( 2, false ) );
        databaseIndexesSection.setClient( databaseIndexesComposite );

        // Indices Widget
        indicesWidget = new IndicesWidget( browserConnection );
        indicesWidget.createWidgetWithEdit( databaseIndexesComposite, toolkit );
        indicesWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Linear Indexes Widget
        toolkit.createLabel( databaseIndexesComposite, "Linear Index:" );
        linearIndexBooleanWithDefaultWidget = new BooleanWithDefaultWidget( false );
        linearIndexBooleanWithDefaultWidget.create( databaseIndexesComposite, toolkit );
        linearIndexBooleanWithDefaultWidget.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Build the Cache Console ───────────────────────────────────────────────
    // BDB uses an in-memory cache to speed up reads. The Imperial archivist
    // tunes four cache dimensions: main cache size, how many entries to free
    // when the cache is full, DN cache size (for quick DN lookups), and IDL
    // cache size (for index data lists). More cache = faster reads, bigger RAM.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Cache" section — four numeric inputs covering
     * {@code olcDbCacheSize}, {@code olcDbCacheFree}, {@code olcDbDNcacheSize},
     * and {@code olcDbIDLcacheSize}.
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createDatabaseCacheSection( Composite parent, FormToolkit toolkit )
    {
        // Database Cache Section
        Section databaseCacheSection = toolkit.createSection( parent, Section.TWISTIE );
        databaseCacheSection.setText( "Database Cache" );
        databaseCacheSection.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        Composite databaseCacheComposite = toolkit.createComposite( databaseCacheSection );
        toolkit.paintBordersFor( databaseCacheComposite );
        databaseCacheComposite.setLayout( new GridLayout( 2, false ) );
        databaseCacheSection.setClient( databaseCacheComposite );

        // Cache Size Text
        toolkit.createLabel( databaseCacheComposite, "Cache Size:" );
        cacheSizeText = BaseWidgetUtils.createIntegerText( toolkit, databaseCacheComposite );
        cacheSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Cache Free Text
        toolkit.createLabel( databaseCacheComposite, "Cache Free:" );
        cacheFreeText = BaseWidgetUtils.createIntegerText( toolkit, databaseCacheComposite );
        cacheFreeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // DN Cache Size Text
        toolkit.createLabel( databaseCacheComposite, "DN Cache Size:" );
        dnCacheSizeText = BaseWidgetUtils.createIntegerText( toolkit, databaseCacheComposite );
        dnCacheSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // IDL Cache Size Text
        toolkit.createLabel( databaseCacheComposite, "IDL Cache Size:" );
        idlCacheSizeText = BaseWidgetUtils.createIntegerText( toolkit, databaseCacheComposite );
        idlCacheSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Build the Limits Console ──────────────────────────────────────────────
    // Operational ceilings for the Berkeley vault: how deep the search stack
    // can grow, how large each BDB page can be (affects storage efficiency),
    // and at what interval BDB should flush (checkpoint) its transaction log.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Limits" section — three controls for
     * {@code olcDbSearchStack} (search recursion depth),
     * {@code olcDbPageSize} (BDB page size), and
     * {@code olcDbCheckpoint} (checkpoint interval).
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

        // Search Stack Depth Text
        toolkit.createLabel( databaseLimitsComposite, "Search Stack Depth:" );
        searchStackDepthText = BaseWidgetUtils.createIntegerText( toolkit, databaseLimitsComposite );
        searchStackDepthText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Page Size Text
        toolkit.createLabel( databaseLimitsComposite, "Page Size:" );
        pageSizeText = toolkit.createText( databaseLimitsComposite, "" );
        pageSizeText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Checkpoint Text
        toolkit.createLabel( databaseLimitsComposite, "Checkpoint Interval:" );
        checkpointText = toolkit.createText( databaseLimitsComposite, "" );
        checkpointText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Build the Options Console ─────────────────────────────────────────────
    // Advanced behavioral switches: whether BDB should flush writes synchronously
    // (safer but slower), whether dirty reads are allowed (faster but can return
    // stale data), and which deadlock-detection algorithm the BDB environment
    // should use when multiple transactions collide.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the "Database Options" section — two {@link BooleanWithDefaultWidget}
     * controls for {@code olcDbNoSync} and dirty-read mode, plus a
     * {@link LockDetectWidget} for the {@code olcDbLockDetect} deadlock algorithm.
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

        // Allow Reads Of Uncommited Data Widget
        toolkit.createLabel( databaseOptionsComposite, "Allow Reads Of Uncommited Data:" );
        allowReadsOfUncommitedDataBooleanWithDefaultWidget = new BooleanWithDefaultWidget( false );
        allowReadsOfUncommitedDataBooleanWithDefaultWidget.create( databaseOptionsComposite, toolkit );
        allowReadsOfUncommitedDataBooleanWithDefaultWidget.getControl().setLayoutData(
            new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Deadlock Detection Algorithm Widget
        toolkit.createLabel( databaseOptionsComposite, "Deadlock Detection Algorithm:" );
        lockDetectWidget = new LockDetectWidget();
        lockDetectWidget.createWidget( databaseOptionsComposite );
        lockDetectWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
    }


    // ── Re-read All Vault Settings from the Model ─────────────────────────────
    // The archivist steps through every control panel and updates it to reflect
    // the current state of the OlcBdbConfig model — directory, mode, encryption,
    // indexes, caches, limits, and options. Listeners are paused during this
    // operation so we don't generate spurious dirty signals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all UI controls from the current {@link OlcBdbConfig} model.
     * Every widget in all five sections is updated to match the model's current values.
     * Null model values are converted to safe defaults (empty strings, false, etc.)
     * Listeners are removed before the update and re-attached after.
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

            // Crypt File Text
            String cryptFile = database.getOlcDbCryptFile();
            cryptFileBrowserWidget.setFilename( ( cryptFile == null ) ? "" : cryptFile );

            // Crypt Key Text
            byte[] cryptKey = database.getOlcDbCryptKey();
            cryptKeyText.setText( ( cryptKey == null ) ? "" : new String( cryptKey ) ); //$NON-NLS-1$

            // Shared Memory Key Text
            Integer sharedMemoryKey = database.getOlcDbShmKey();
            sharedMemoryKeyText.setText( ( sharedMemoryKey == null ) ? "" : "" + sharedMemoryKey ); //$NON-NLS-1$

            // Indices Text
            //indicesWidget.setIndices( database.getOlcDbIndex() );

            // Linear Index Widget
            linearIndexBooleanWithDefaultWidget.setValue( database.getOlcDbLinearIndex() );

            // Cache Size Text
            Integer cacheSize = database.getOlcDbCacheSize();
            cacheSizeText.setText( ( cacheSize == null ) ? "" : "" + cacheSize ); //$NON-NLS-1$

            // Cache Free Text
            Integer cacheFree = database.getOlcDbCacheFree();
            cacheFreeText.setText( ( cacheFree == null ) ? "" : "" + cacheFree ); //$NON-NLS-1$

            // DN Cache Size Text
            Integer dnCacheSize = database.getOlcDbDNcacheSize();
            dnCacheSizeText.setText( ( dnCacheSize == null ) ? "" : "" + dnCacheSize ); //$NON-NLS-1$

            // IDL Cache Size Text
            Integer idlCacheSize = database.getOlcDbIDLcacheSize();
            idlCacheSizeText.setText( ( idlCacheSize == null ) ? "" : "" + idlCacheSize ); //$NON-NLS-1$

            // Search Stack Depth Text
            Integer searchStackDepth = database.getOlcDbSearchStack();
            searchStackDepthText.setText( ( searchStackDepth == null ) ? "" : "" + searchStackDepth ); //$NON-NLS-1$

            // Page Size Text
            List<String> pageSize = database.getOlcDbPageSize();
            pageSizeText.setText( ( pageSize == null ) ? "" : OpenLdapConfigurationPluginUtils.concatenate( pageSize ) ); //$NON-NLS-1$

            // Checkpoint Text
            String checkpoint = database.getOlcDbCheckpoint();
            checkpointText.setText( ( checkpoint == null ) ? "" : checkpoint ); //$NON-NLS-1$

            // Disable Synchronous Database Writes Widget
            disableSynchronousDatabaseWritesBooleanWithDefaultWidget.setValue( database.getOlcDbNoSync() );

            // Allow Reads Of Uncommited Data Widget
            allowReadsOfUncommitedDataBooleanWithDefaultWidget.setValue( database.getOlcDbDirtyRead() );

            // Deadlock Detection Algorithm Text
            lockDetectWidget.setValue( OlcBdbConfigLockDetectEnum.fromString( database.getOlcDbLockDetect() ) );
        }

        addListeners();
    }


    // ── Start Watching All Controls ───────────────────────────────────────────
    // The vault's change-monitoring system activates: every text field, widget,
    // and button now reports user changes upward as "editor is dirty."
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches dirty listeners to every widget in all five sections.
     * Any user interaction with any control will propagate a dirty signal
     * to the parent editor, enabling the Save button.
     */
    private void addListeners()
    {
        directoryBrowserWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
        modeUnixPermissionsWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
        editConfigurationButton.addSelectionListener( editConfigurationButtonSelectionListener );
        cryptFileBrowserWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
        cryptKeyText.addModifyListener( dirtyModifyListener );
        sharedMemoryKeyText.addModifyListener( dirtyModifyListener );

        indicesWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
        linearIndexBooleanWithDefaultWidget.addWidgetModifyListener( dirtyWidgetModifyListener );

        cacheSizeText.addModifyListener( dirtyModifyListener );
        cacheFreeText.addModifyListener( dirtyModifyListener );
        dnCacheSizeText.addModifyListener( dirtyModifyListener );
        idlCacheSizeText.addModifyListener( dirtyModifyListener );

        searchStackDepthText.addModifyListener( dirtyModifyListener );
        pageSizeText.addModifyListener( dirtyModifyListener );
        checkpointText.addModifyListener( dirtyModifyListener );

        disableSynchronousDatabaseWritesBooleanWithDefaultWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
        allowReadsOfUncommitedDataBooleanWithDefaultWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
        lockDetectWidget.addWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Stop Watching All Controls ────────────────────────────────────────────
    // The vault's change-monitoring system deactivates before we update
    // the controls programmatically, preventing false dirty signals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches all dirty listeners from every widget in all five sections.
     * Called before populating controls from the model so that programmatic
     * updates do not trigger spurious "editor is dirty" events.
     */
    private void removeListeners()
    {
        directoryBrowserWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
        modeUnixPermissionsWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
        editConfigurationButton.removeSelectionListener( editConfigurationButtonSelectionListener );
        cryptFileBrowserWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
        cryptKeyText.removeModifyListener( dirtyModifyListener );
        sharedMemoryKeyText.removeModifyListener( dirtyModifyListener );

        indicesWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
        linearIndexBooleanWithDefaultWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );

        cacheSizeText.removeModifyListener( dirtyModifyListener );
        cacheFreeText.removeModifyListener( dirtyModifyListener );
        dnCacheSizeText.removeModifyListener( dirtyModifyListener );
        idlCacheSizeText.removeModifyListener( dirtyModifyListener );

        searchStackDepthText.removeModifyListener( dirtyModifyListener );
        pageSizeText.removeModifyListener( dirtyModifyListener );
        checkpointText.removeModifyListener( dirtyModifyListener );

        disableSynchronousDatabaseWritesBooleanWithDefaultWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
        allowReadsOfUncommitedDataBooleanWithDefaultWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
        lockDetectWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );
    }


    // ── Commit All Vault Settings to the Model ────────────────────────────────
    // The archivist reads every control and stamps its current value into the
    // official OlcBdbConfig record. Empty/unparseable numeric fields are
    // committed as null (no value). The directory and crypt-file browsers
    // also save their dialog history at commit time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes the current state of every UI control back into the {@link OlcBdbConfig} model.
     * Empty numeric fields are stored as null. The directory and crypt-file widgets
     * additionally save their dialog settings (browse history) on commit.
     * Note: page-size commit is currently a TODO — the field is not yet wired up.
     *
     * @param onSave  {@code true} when triggered by a full editor save;
     *                {@code false} for page-change commits
     */
    public void commit( boolean onSave )
    {
        // Directory Text
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

        // Mode Text
        database.setOlcDbMode( modeUnixPermissionsWidget.getValue() );

        // Crypt File Text
        String cryptFile = cryptFileBrowserWidget.getFilename();

        if ( Strings.isEmpty( cryptFile ) )
        {
            database.setOlcDbCryptFile( null );
        }
        else
        {
            database.setOlcDbCryptFile( cryptFile );
        }

        cryptFileBrowserWidget.saveDialogSettings();

        // Crypt Key Text
        String cryptKey = cryptKeyText.getText();

        if ( Strings.isEmpty( cryptKey ) )
        {
            database.setOlcDbCryptKey( null );
        }
        else
        {
            database.setOlcDbCryptKey( cryptKey.getBytes() );
        }

        // Shared Memory Key Text
        try
        {
            database.setOlcDbShmKey( Integer.parseInt( sharedMemoryKeyText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbShmKey( null );
        }

        // Indices Widget
        database.clearOlcDbIndex();
        /*
        for ( String index : indicesWidget.getIndices() )
        {
            database.addOlcDbIndex( index );
        }
        */

        // Linear Index Widget
        database.setOlcDbLinearIndex( linearIndexBooleanWithDefaultWidget.getValue() );

        // Cache Size Text
        try
        {
            database.setOlcDbCacheSize( Integer.parseInt( cacheSizeText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbCacheSize( null );
        }

        // Cache Free Text
        try
        {
            database.setOlcDbCacheFree( Integer.parseInt( cacheFreeText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbCacheFree( null );
        }

        // DN Cache Size Text
        try
        {
            database.setOlcDbDNcacheSize( Integer.parseInt( dnCacheSizeText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbDNcacheSize( null );
        }

        // IDL Cache Size Text
        try
        {
            database.setOlcDbIDLcacheSize( Integer.parseInt( idlCacheSizeText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbIDLcacheSize( null );
        }

        // Search Stack Depth Text
        try
        {
            database.setOlcDbSearchStack( Integer.parseInt( searchStackDepthText.getText() ) );
        }
        catch ( NumberFormatException e )
        {
            database.setOlcDbSearchStack( null );
        }

        // Page Size Text
        //TODO

        // Checkpoint Text
        database.setOlcDbCheckpoint( checkpointText.getText() );

        // Disable Synchronous Database Writes Widget
        database.setOlcDbNoSync( disableSynchronousDatabaseWritesBooleanWithDefaultWidget.getValue() );

        // Allow Reads Of Uncommited Data Widget
        database.setOlcDbDirtyRead( allowReadsOfUncommitedDataBooleanWithDefaultWidget.getValue() );

        // Deadlock Detection Algorithm Text
        OlcBdbConfigLockDetectEnum lockDetect = lockDetectWidget.getValue();

        if ( lockDetect != null )
        {
            database.setOlcDbLockDetect( lockDetect.toString() );
        }
        else
        {
            database.setOlcDbLockDetect( null );
        }
    }
}
