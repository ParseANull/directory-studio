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

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.openldap.common.ui.model.DatabaseTypeEnum;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginUtils;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.editor.dialogs.DatabaseTypeDialog;
import org.apache.directory.studio.openldap.config.editor.pages.DatabasesPage;
import org.apache.directory.studio.openldap.config.editor.wrappers.DatabaseWrapper;
import org.apache.directory.studio.openldap.config.editor.wrappers.DatabaseWrapperLabelProvider;
import org.apache.directory.studio.openldap.config.editor.wrappers.DatabaseWrapperViewerComparator;
import org.apache.directory.studio.openldap.config.model.database.OlcBdbConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcDatabaseConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcDbPerlConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcDbSocketConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcHdbConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcLDAPConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcLdifConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcMdbConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcMetaConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcMonitorConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcNdbConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcNullConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcPasswdConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcRelayConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcShellConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcSqlConfig;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: DatabasesMasterDetailsBlock — Lando Running Cloud City ─────────────
// Lando Calrissian didn't manage every function of Cloud City himself —
// he maintained a master list of all city systems, and when you selected one,
// the right control panel appeared on the right side. That's exactly what this
// class does for OpenLDAP databases: the left pane is the master list
// (Add, Delete, Up, Down), and the right pane is the detail editor for
// whatever database is selected.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The master/details block for the Databases page of the OpenLDAP Server Configuration Editor.
 * The left (master) side shows the list of all configured databases in a {@link TableViewer},
 * with Add, Delete, Up, and Down buttons to manage the ordering of databases.
 * The right (details) side shows a {@link DatabasesDetailsPage} whose content changes
 * to match whichever database is selected in the list.
 * Think of Lando running Cloud City: he sees all the systems at a glance, and
 * when he selects one, the detailed controls for that system appear.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DatabasesMasterDetailsBlock extends MasterDetailsBlock
{
    private static final String NEW_ID = "database";

    /** The associated page */
    private DatabasesPage page;

    /** The Managed form */
    private IManagedForm managedForm;

    /** The details page */
    private DatabasesDetailsPage detailsPage;

    /** The database wrappers */
    private List<DatabaseWrapper> databaseWrappers = new ArrayList<>();

    /** The currently selected object */
    private Object currentSelection;

    // UI Fields
    /** The table listing all the existing databases */
    private TableViewer databaseTableViewer;

    /** The button used to add a new Database */
    private Button addButton;
    private DatabaseTypeDialog databaseTypeDialog;

    /** The button used to delete an existing Database */
    private Button deleteButton;

    /** The button used to move up Database in the list */
    private Button upButton;

    /** The button used to move down Database in the list */
    private Button downButton;

    // Listeners
    /**
     * A listener called when the Database table content has changed. It will enable
     * or disabled button accordingly to the changes.
     */
    private ISelectionChangedListener viewerSelectionChangedListener = event ->
        {
            if ( !event.getSelection().isEmpty() )
            {
                Object newSelection = ( ( StructuredSelection ) event.getSelection() ).getFirstElement();

                if ( newSelection != currentSelection )
                {
                    currentSelection = newSelection;

                    // Only show the details if the database is enabled
                    // 2.5 feature...
                    // TODO : check with the SchemaManager is the olcDisabled AT is present.
                    /*
                    OlcDatabaseConfig database = ((DatabaseWrapper)currentSelection).getDatabase();
                    Boolean disabled = database.getOlcDisabled();

                    if ( ( disabled == null ) || ( disabled == false ) )
                    {
                        detailsPart.commit( false );
                        managedForm.fireSelectionChanged( managedForm.getParts()[0], event.getSelection() );
                        databseViewer.refresh();
                        refreshButtonStates();
                    }
                    */
                    detailsPart.commit( false );
                    managedForm.fireSelectionChanged( managedForm.getParts()[0], event.getSelection() );
                    databaseTableViewer.refresh();
                    refreshButtonStates();
                }
            }
        };


    /**
     * A listener called when the Add button is clicked
     */
    private SelectionAdapter addButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            addNewDatabase();
        }
    };

    /**
     * A listener called when the Delete button is clicked
     */
    private SelectionAdapter deleteButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            deleteSelectedDatabase();
        }
    };

    /**
     * A listener called when the Up button is clicked
     */
    private SelectionAdapter upButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            moveSelectedDatabaseUp();
        }
    };

    /**
     * A listener called when the Down button is clicked
     */
    private SelectionAdapter downButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            moveSelectedDatabaseDown();
        }
    };


    // ── Open the Cloud City Control Room ─────────────────────────────────────
    // Lando sets up his management headquarters. We receive a reference to the
    // parent DatabasesPage so we can later get the configuration model and the
    // parent editor from it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@link DatabasesMasterDetailsBlock} and links it to the
     * given databases page. The page provides access to the configuration model
     * and the parent editor (for setting the dirty flag).
     *
     * @param page  the parent {@link DatabasesPage} that owns this block
     */
    public DatabasesMasterDetailsBlock( DatabasesPage page )
    {
        super();
        this.page = page;
    }


    // ── Lay Out the Split-Panel View ──────────────────────────────────────────
    // Cloud City's control room has two sides: the master list on the left takes
    // up one-third of the space, and the detail panel on the right takes two-thirds.
    // We override createContent to set that 1:2 sash ratio.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the split-panel layout and sets the sash weight ratio to 1:2
     * (master list takes one third, details panel takes two thirds).
     *
     * @param managedForm  the Eclipse Forms managed form driving this block
     */
    @Override
    public void createContent( IManagedForm managedForm )
    {
        super.createContent( managedForm );

        // Giving the weights of both parts of the SashForm.
        sashForm.setWeights( new int[]
            { 1, 2 } );
    }


    // ── Build the Master List Panel ───────────────────────────────────────────
    // The left side of Cloud City's control room: a scrollable list of all
    // databases, flanked by Add, Delete, separator, Up, and Down buttons.
    // Clicking any button fires the corresponding action through its listener.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the master (left) pane: an "All Databases" section containing a
     * {@link TableViewer} backed by {@code databaseWrappers}, plus five buttons:
     * Add, Delete, separator, Up, Down.
     * The viewer is initialised with {@code initFromInput()} and all button listeners
     * are attached with {@code addListeners()}.
     *
     * @param managedForm  the Eclipse Forms managed form
     * @param parent       the parent composite to place the master pane into
     */
    protected void createMasterPart( IManagedForm managedForm, Composite parent )
    {
        FormToolkit toolkit = managedForm.getToolkit();

        // Creating the Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout() );

        // Creating the Section
        Section section = toolkit.createSection( composite, Section.TITLE_BAR );
        section.setText( "All Databases" );
        Composite client = toolkit.createComposite( section );
        client.setLayout( new GridLayout( 2, false ) );
        toolkit.paintBordersFor( client );
        section.setClient( client );
        section.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        // Creating the Table and Table Viewer
        Table table = toolkit.createTable( client, SWT.NONE );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 5 );
        gd.heightHint = 20;
        gd.widthHint = 100;
        table.setLayoutData( gd );
        SectionPart sectionPart = new SectionPart( section );
        this.managedForm = managedForm;
        managedForm.addPart( sectionPart );
        databaseTableViewer = new TableViewer( table );

        databaseTableViewer.setContentProvider( new ArrayContentProvider() );
        databaseTableViewer.setLabelProvider( new DatabaseWrapperLabelProvider() );
        databaseTableViewer.setComparator( new DatabaseWrapperViewerComparator() );

        // Add a contextual menu to enable/disable a Database. This is a 2.5 feature.
        // TODO : check with the schemaManager
        /*
        databseViewer.getTable().addMouseListener ( new MouseListener()
            {
                public void mouseUp( MouseEvent e )
                {
                    // Nothing to do
                }

                public void mouseDown( MouseEvent event )
                {
                    if ( event.button == 3 )
                    {
                        Table table = (Table)event.getSource();
                        int selectedItem = table.getSelectionIndex();
                        DatabaseWrapper database = databaseWrappers.get( selectedItem );

                        Menu menu = new Menu( databseViewer.getTable().getShell(), SWT.POP_UP );
                        MenuItem enabled = new MenuItem ( menu, SWT.PUSH );

                        Boolean disabled = database.getDatabase().getOlcDisabled();

                        if ( ( disabled != null ) && ( disabled == true ) )
                        {
                            enabled.setText ( "Enable" );
                        }
                        else
                        {
                            enabled.setText ( "Disable" );
                        }

                        // Add a listener on the menu
                        enabled.addListener( SWT.Selection, new Listener()
                        {
                            @Override
                            public void handleEvent( Event event )
                            {
                                // Switch the flag from disabled to enabled, and from enabled to disabled
                                database.getDatabase().setOlcDisabled( ( disabled == null ) || !disabled );
                                databseViewer.refresh();
                            }
                        });

                        // draws pop up menu:
                        Point pt = new Point( event.x, event.y );
                        pt = table.toDisplay( pt );
                        menu.setLocation( pt.x, pt.y );
                        menu.setVisible ( true );
                    }
                }

                public void mouseDoubleClick( MouseEvent e )
                {
                    // Nothing to do
                }
            }
        );
        */

        // Creating the button(s)
        addButton = toolkit.createButton( client, "Add", SWT.PUSH );
        addButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        databaseTypeDialog = new DatabaseTypeDialog( addButton.getShell() );

        deleteButton = toolkit.createButton( client, "Delete", SWT.PUSH );
        deleteButton.setEnabled( false );
        deleteButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        Label separator = BaseWidgetUtils.createSeparator( client, 1 );
        separator.setLayoutData( new GridData( SWT.NONE, SWT.BEGINNING, false, false ) );

        upButton = toolkit.createButton( client, "Up", SWT.PUSH );
        upButton.setEnabled( false );
        upButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        downButton = toolkit.createButton( client, "Down", SWT.PUSH );
        downButton.setEnabled( false );
        downButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        initFromInput();
        addListeners();
    }


    // ── Load the Current Database Roster ─────────────────────────────────────
    // Lando asks his system: "What databases do we have?" We clear the local
    // list, re-read from the configuration model, wrap each database, and
    // hand the list to the table viewer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the database table viewer from the current configuration model.
     * Clears the existing wrapper list, wraps every {@link OlcDatabaseConfig} from
     * the configuration into a {@link DatabaseWrapper}, and sets the result as
     * the viewer's input.
     */
    private void initFromInput()
    {
        databaseWrappers.clear();

        for ( OlcDatabaseConfig database : page.getConfiguration().getDatabases() )
        {
            databaseWrappers.add( new DatabaseWrapper( database ) );
        }

        databaseTableViewer.setInput( databaseWrappers );
    }


    // ── Refresh the Roster Display ────────────────────────────────────────────
    // Lando calls for a full status update: reload from the model, then
    // tell the viewer to repaint itself.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the master list UI by re-initialising from the input model
     * and then asking the table viewer to repaint.
     */
    public void refreshUI()
    {
        initFromInput();
        databaseTableViewer.refresh();
    }


    // ── Hook Up All Button and Table Listeners ────────────────────────────────
    // Every interactive control in the master panel gets its corresponding
    // listener wired up: table selection drives the details pane, and each
    // button fires its action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches all event listeners to the master panel's interactive controls:
     * the table viewer's selection listener (drives the detail pane)
     * and the selection listeners for Add, Delete, Up, and Down buttons.
     */
    private void addListeners()
    {
        databaseTableViewer.addSelectionChangedListener( viewerSelectionChangedListener );
        addButton.addSelectionListener( addButtonSelectionListener );
        deleteButton.addSelectionListener( deleteButtonSelectionListener );
        upButton.addSelectionListener( upButtonSelectionListener );
        downButton.addSelectionListener( downButtonSelectionListener );
    }


    // ── Add a New Database to the Roster ─────────────────────────────────────
    // Lando opens the "new system" dialog, picks a system type from the
    // available backends, creates the right model object for it, assigns
    // it a unique ID and ordering prefix, gives it a placeholder suffix DN,
    // and adds it to the list. The viewer and dirty flag are updated.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link DatabaseTypeDialog}, creates a new database model object
     * of the selected type, assigns it an {@code olcDatabase} value with a new
     * ordering prefix and a placeholder suffix DN (dc=database{n},dc=com),
     * wraps it in a {@link DatabaseWrapper}, adds it to the list, selects it,
     * and marks the editor dirty.
     */
    private void addNewDatabase()
    {
        String newId = getNewId();

        OlcDatabaseConfig database = null;
        DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.NONE;

        // Open the Dialog, and process the addition if it went fine
        if ( databaseTypeDialog.open() == Dialog.OK )
        {
            databaseTypeEnum = databaseTypeDialog.getDatabaseType();

            switch ( databaseTypeEnum )
            {
                case BDB :
                    database = new OlcBdbConfig();
                    break;

                case CONFIG :
                    //database = new OlcConfig();
                    break;


                case DB_PERL :
                    database = new OlcDbPerlConfig();
                    break;

                case DB_SOCKET :
                    database = new OlcDbSocketConfig();
                    break;

                case FRONTEND :
                    //database = new OlcFrontendConfig();
                    break;

                case HDB :
                    database = new OlcHdbConfig();
                    break;

                case LDAP :
                    database = new OlcLDAPConfig();
                    break;

                case LDIF :
                    database = new OlcLdifConfig();
                    break;

                case MDB :
                    database = new OlcMdbConfig();
                    break;

                case META :
                    database = new OlcMetaConfig();
                    break;

                case MONITOR :
                    database = new OlcMonitorConfig();
                    break;

                case NDB :
                    database = new OlcNdbConfig();
                    break;

                case NULL :
                    database = new OlcNullConfig();
                    break;

                case PASSWD :
                    database = new OlcPasswdConfig();
                    break;

                case RELAY :
                    database = new OlcRelayConfig();
                    break;

                case SHELL :
                    database = new OlcShellConfig();
                    break;

                case SQL :
                    database = new OlcSqlConfig();
                    break;

                default :
                    break;
            }
        }

        database.setOlcDatabase( "{" + getNewOrderingValue() + "}" + databaseTypeEnum.name() );

        try
        {
            database.addOlcSuffix( new Dn( "dc=" + newId + ",dc=com" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch ( LdapInvalidDnException e1 )
        {
            // Will never happen
        }

        DatabaseWrapper databaseWrapper = new DatabaseWrapper( database );
        databaseWrappers.add( databaseWrapper );
        databaseTableViewer.refresh();
        databaseTableViewer.setSelection( new StructuredSelection( databaseWrapper ) );
        setEditorDirty();
    }


    // ── Remove the Selected Database from the Roster ──────────────────────────
    // Lando asks for confirmation before decommissioning a system module —
    // "Are you sure you want to delete database X?" If yes, we remove the
    // wrapper and mark the editor dirty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected {@link DatabaseWrapper} from the list
     * after prompting the user to confirm. The editor is marked dirty on confirmation.
     * If nothing is selected, the method does nothing.
     */
    private void deleteSelectedDatabase()
    {
        StructuredSelection selection = ( StructuredSelection ) databaseTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            DatabaseWrapper databaseWrapper = ( DatabaseWrapper ) selection.getFirstElement();
            OlcDatabaseConfig database = databaseWrapper.getDatabase();

            if ( MessageDialog.openConfirm( page.getManagedForm().getForm().getShell(), "Confirm Delete",
                NLS.bind( "Are you sure you want to delete database ''{0} ({1})''?",
                    OpenLdapConfigurationPluginUtils.stripOrderingPrefix( database.getOlcDatabase() ),
                    getSuffixValue( database ) ) ) )
            {
                databaseWrappers.remove( databaseWrapper );
                setEditorDirty();
            }
        }
    }


    // ── Get the Primary Suffix for Display ────────────────────────────────────
    // Lando needs a human-readable label for the "confirm delete" message.
    // We return the first suffix DN of the database, or "none" if it has none.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first {@code olcSuffix} DN of the given database as a string,
     * or {@code "none"} if the database has no suffix values.
     * Used to build the "confirm delete" message.
     *
     * @param database  the database whose suffix we want
     * @return          the first suffix DN string, or {@code "none"}
     */
    private String getSuffixValue( OlcDatabaseConfig database )
    {
        String suffix = OpenLdapConfigurationPluginUtils.getFirstValueDn( database.getOlcSuffix() );

        if ( suffix != null )
        {
            return suffix;
        }
        else
        {
            return "none";
        }
    }


    // ── Generate a Unique ID for a New Database ───────────────────────────────
    // Lando needs a unique system name (like "database1", "database2") so each
    // new DB gets a distinct placeholder suffix. We increment a counter until
    // we find one not already in use.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Generates a unique string ID for a newly created database by appending
     * an incrementing counter to the prefix {@code "database"} until the name
     * does not collide with any existing database's {@code olcDatabase} value.
     * The ID is used as the suffix hostname segment in the placeholder DN.
     *
     * @return  a unique database ID string (e.g. {@code "database1"}, {@code "database2"})
     */
    private String getNewId()
    {
        int counter = 1;
        String name = NEW_ID;
        boolean ok = false;

        while ( !ok )
        {
            ok = true;
            name = NEW_ID + counter;

            for ( DatabaseWrapper databaseWrapper : databaseWrappers )
            {
                if ( name.equalsIgnoreCase( OpenLdapConfigurationPluginUtils.stripOrderingPrefix( databaseWrapper
                    .getDatabase().getOlcDatabase() ) ) )
                {
                    ok = false;
                }
            }

            counter++;
        }

        return name;
    }


    // ── Pick the Next Available Ordering Value ────────────────────────────────
    // Lando assigns a system priority slot. The next new database gets the
    // slot one above the current maximum.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the ordering prefix to assign to a newly created database.
     * It is always {@code getMaxOrderingValue() + 1}, placing the new database
     * after all existing ones.
     *
     * @return  the new ordering integer to embed in {@code {n}database}
     */
    private int getNewOrderingValue()
    {
        return getMaxOrderingValue() + 1;
    }


    // ── Find the Lowest Priority Slot in Use ─────────────────────────────────
    // Lando checks which system occupies the lowest slot number (i.e., appears
    // first in the ordered list). Used to decide whether the Up button should
    // be enabled for the selected database.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the smallest ordering prefix currently in use across all database wrappers.
     * Used to determine whether the selected database is already first, and thus
     * whether the Up button should be disabled.
     *
     * @return  the minimum ordering prefix value, or {@link Integer#MAX_VALUE} if none
     */
    private int getMinOrderingValue()
    {
        int minOrderingValue = Integer.MAX_VALUE;

        for ( DatabaseWrapper databaseWrapper : databaseWrappers )
        {
            if ( OpenLdapConfigurationPluginUtils.hasOrderingPrefix( databaseWrapper.getDatabase().getOlcDatabase() ) )
            {
                int databaseOrderingValue = OpenLdapConfigurationPluginUtils.getOrderingPrefix( databaseWrapper
                    .getDatabase().getOlcDatabase() );

                if ( databaseOrderingValue < minOrderingValue )
                {
                    minOrderingValue = databaseOrderingValue;
                }
            }
        }

        return minOrderingValue;
    }


    // ── Find the Highest Priority Slot in Use ─────────────────────────────────
    // Lando checks which system occupies the highest slot number (i.e., appears
    // last in the ordered list). Used to pick the next ordering value and to
    // decide whether the Down button should be enabled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the largest ordering prefix currently in use across all database wrappers.
     * Used to compute the next ordering value for new databases and to determine
     * whether the Down button should be disabled for the currently selected database.
     *
     * @return  the maximum ordering prefix value, or {@code -1} if none
     */
    private int getMaxOrderingValue()
    {
        int maxOrderingValue = -1;

        for ( DatabaseWrapper databaseWrapper : databaseWrappers )
        {
            String database = databaseWrapper.getDatabase().getOlcDatabase();

            int databaseOrderingValue = OpenLdapConfigurationPluginUtils.getOrderingPrefix( database );

            if ( databaseOrderingValue > maxOrderingValue )
            {
                maxOrderingValue = databaseOrderingValue;
            }
        }

        return maxOrderingValue;
    }


    // ── Promote the Selected Database Up One Slot ─────────────────────────────
    // Lando moves a system one step higher in priority by swapping its ordering
    // prefix with the system that currently occupies the slot just above it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Moves the currently selected database one position earlier in the ordering
     * by swapping its {@code {n}} ordering prefix with the database that has
     * the next-lower prefix. The viewer is refreshed and the editor is marked dirty.
     * If nothing is selected or no lower-prefix database exists, this is a no-op.
     */
    private void moveSelectedDatabaseUp()
    {
        StructuredSelection selection = ( StructuredSelection ) databaseTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            OlcDatabaseConfig selectedDatabase = ( ( DatabaseWrapper ) selection.getFirstElement() ).getDatabase();
            int selectedDatabaseOrderingPrefix = OpenLdapConfigurationPluginUtils.getOrderingPrefix( selectedDatabase
                .getOlcDatabase() );
            String selectedDatabaseName = OpenLdapConfigurationPluginUtils.stripOrderingPrefix( selectedDatabase
                .getOlcDatabase() );

            OlcDatabaseConfig swapDatabase = findPreviousDatabase( selectedDatabaseOrderingPrefix );

            if ( swapDatabase != null )
            {
                int swapDatabaseOrderingPrefix = OpenLdapConfigurationPluginUtils.getOrderingPrefix( swapDatabase
                    .getOlcDatabase() );
                String swapDatabaseName = OpenLdapConfigurationPluginUtils.stripOrderingPrefix( swapDatabase
                    .getOlcDatabase() );

                selectedDatabase.setOlcDatabase( "{" + swapDatabaseOrderingPrefix + "}" + selectedDatabaseName );
                swapDatabase.setOlcDatabase( "{" + selectedDatabaseOrderingPrefix + "}" + swapDatabaseName );

                databaseTableViewer.refresh();
                refreshButtonStates();
                setEditorDirty();
            }
        }
    }


    // ── Find the Database Occupying the Slot Just Before ─────────────────────
    // Given a target ordering prefix, Lando scans all systems for the one
    // with the largest prefix that is still strictly less than the target.
    // That's the "previous" system — the one we'd swap with on an Up operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds the database whose ordering prefix is the largest value that is
     * still strictly less than the given {@code orderingPrefix}.
     * This is the database that the selected one would swap with on an Up move.
     *
     * @param orderingPrefix  the ordering prefix of the database being moved up
     * @return                the nearest-previous database, or {@code null} if none
     */
    private OlcDatabaseConfig findPreviousDatabase( int orderingPrefix )
    {
        OlcDatabaseConfig selectedDatabase = null;
        int selectedDatabaseOrderingPrefix = Integer.MIN_VALUE;

        for ( DatabaseWrapper databaseWrapper : databaseWrappers )
        {
            int databaseOrderingPrefix = OpenLdapConfigurationPluginUtils.getOrderingPrefix( databaseWrapper
                .getDatabase().getOlcDatabase() );

            if ( ( databaseOrderingPrefix < orderingPrefix )
                && ( databaseOrderingPrefix > selectedDatabaseOrderingPrefix ) )
            {
                selectedDatabase = databaseWrapper.getDatabase();
                selectedDatabaseOrderingPrefix = databaseOrderingPrefix;
            }
        }

        return selectedDatabase;
    }


    // ── Demote the Selected Database Down One Slot ────────────────────────────
    // Lando moves a system one step lower in priority by swapping its ordering
    // prefix with the system that currently occupies the slot just below it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Moves the currently selected database one position later in the ordering
     * by swapping its {@code {n}} ordering prefix with the database that has
     * the next-higher prefix. The viewer is refreshed and the editor is marked dirty.
     * If nothing is selected or no higher-prefix database exists, this is a no-op.
     */
    private void moveSelectedDatabaseDown()
    {
        StructuredSelection selection = ( StructuredSelection ) databaseTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            OlcDatabaseConfig selectedDatabase = ( ( DatabaseWrapper ) selection.getFirstElement() ).getDatabase();
            int selectedDatabaseOrderingPrefix = OpenLdapConfigurationPluginUtils.getOrderingPrefix( selectedDatabase
                .getOlcDatabase() );
            String selectedDatabaseName = OpenLdapConfigurationPluginUtils.stripOrderingPrefix( selectedDatabase
                .getOlcDatabase() );

            OlcDatabaseConfig swapDatabase = findNextDatabase( selectedDatabaseOrderingPrefix );

            if ( swapDatabase != null )
            {
                int swapDatabaseOrderingPrefix = OpenLdapConfigurationPluginUtils.getOrderingPrefix( swapDatabase
                    .getOlcDatabase() );
                String swapDatabaseName = OpenLdapConfigurationPluginUtils.stripOrderingPrefix( swapDatabase
                    .getOlcDatabase() );

                selectedDatabase.setOlcDatabase( "{" + swapDatabaseOrderingPrefix + "}" + selectedDatabaseName );
                swapDatabase.setOlcDatabase( "{" + selectedDatabaseOrderingPrefix + "}" + swapDatabaseName );

                databaseTableViewer.refresh();
                refreshButtonStates();
                setEditorDirty();
            }
        }
    }


    // ── Find the Database Occupying the Slot Just After ───────────────────────
    // Given a target ordering prefix, Lando scans all systems for the one
    // with the smallest prefix that is still strictly greater than the target.
    // That's the "next" system — the one we'd swap with on a Down operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds the database whose ordering prefix is the smallest value that is
     * still strictly greater than the given {@code orderingPrefix}.
     * This is the database that the selected one would swap with on a Down move.
     *
     * @param orderingPrefix  the ordering prefix of the database being moved down
     * @return                the nearest-next database, or {@code null} if none
     */
    private OlcDatabaseConfig findNextDatabase( int orderingPrefix )
    {
        OlcDatabaseConfig selectedDatabase = null;
        int selectedDatabaseOrderingPrefix = Integer.MAX_VALUE;

        for ( DatabaseWrapper databaseWrapper : databaseWrappers )
        {
            int databaseOrderingPrefix = OpenLdapConfigurationPluginUtils.getOrderingPrefix( databaseWrapper
                .getDatabase().getOlcDatabase() );

            if ( ( databaseOrderingPrefix > orderingPrefix )
                && ( databaseOrderingPrefix < selectedDatabaseOrderingPrefix ) )
            {
                selectedDatabase = databaseWrapper.getDatabase();
                selectedDatabaseOrderingPrefix = databaseOrderingPrefix;
            }
        }

        return selectedDatabase;
    }


    // ── Update the Action Buttons to Reflect the Current Selection ────────────
    // Lando's control room updates its status indicators: Delete is only enabled
    // when something is selected; Up is disabled when the selected system is
    // already at the top; Down is disabled when it's already at the bottom.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the enabled/disabled state of the Delete, Up, and Down buttons
     * based on the current table selection. If nothing is selected, all three
     * buttons are disabled. If something is selected, Delete is always enabled,
     * Up is enabled unless the selected database has the minimum ordering prefix,
     * and Down is enabled unless it has the maximum.
     */
    private void refreshButtonStates()
    {
        // Getting the selection of the table viewer
        StructuredSelection selection = ( StructuredSelection ) databaseTableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            OlcDatabaseConfig database = ( ( DatabaseWrapper ) selection.getFirstElement() ).getDatabase();

            deleteButton.setEnabled( true );
            upButton.setEnabled( getMinOrderingValue() != OpenLdapConfigurationPluginUtils
                .getOrderingPrefix( database.getOlcDatabase() ) );
            downButton.setEnabled( getMaxOrderingValue() != OpenLdapConfigurationPluginUtils
                .getOrderingPrefix( database.getOlcDatabase() ) );
        }
        else
        {
            deleteButton.setEnabled( false );
            upButton.setEnabled( false );
            downButton.setEnabled( false );
        }
    }


    // ── Mark the Config as Changed ────────────────────────────────────────────
    // Lando flags a change in the system roster. We mark the parent editor dirty,
    // commit the details page (to ensure its model is up to date), and refresh
    // the table to reflect any label changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent editor as dirty, commits the current details-page state
     * (to keep the model in sync), and refreshes the database table viewer
     * so any label changes are immediately visible.
     */
    public void setEditorDirty()
    {
        ( ( OpenLdapServerConfigurationEditor ) page.getEditor() ).setDirty( true );
        detailsPage.commit( false );
        databaseTableViewer.refresh();
    }


    // ── Register the Details Page ─────────────────────────────────────────────
    // Lando wires up the right-side detail panel: we create a single
    // DatabasesDetailsPage and register it for the DatabaseWrapper class so
    // that selecting any wrapper in the master list shows that details page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the {@link DatabasesDetailsPage} with the {@link DetailsPart}
     * for the {@link DatabaseWrapper} class. One shared details page handles
     * every database type — it adapts its content to whichever wrapper is selected.
     *
     * @param detailsPart  the Eclipse Forms details part that manages the right pane
     */
    protected void registerPages( DetailsPart detailsPart )
    {
        detailsPage = new DatabasesDetailsPage( this );
        detailsPart.registerPage( DatabaseWrapper.class, detailsPage );
    }


    // ── No Toolbar Actions ────────────────────────────────────────────────────
    // Cloud City's master panel doesn't need any toolbar buttons — the Add,
    // Delete, Up, Down buttons are already in the section body itself.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates toolbar actions for the master/details block.
     * We don't add any toolbar actions for this block — all controls are
     * placed directly inside the section body.
     *
     * @param managedForm  the Eclipse Forms managed form (not used)
     */
    protected void createToolBarActions( IManagedForm managedForm )
    {
        // No toolbar actions
    }


    // ── Expose the Associated Page ────────────────────────────────────────────
    // Callers (such as the details page) sometimes need to reach the parent
    // DatabasesPage to get the configuration model or the editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link DatabasesPage} that owns this master/details block.
     * Used by the details page and other collaborators to access the
     * configuration model and the parent editor.
     *
     * @return  the associated {@link DatabasesPage}
     */
    public DatabasesPage getPage()
    {
        return page;
    }


    // ── Persist the Current Roster to the Config Model ───────────────────────
    // When the user clicks Save, Lando files the final report: commit the
    // details page, clear the old database list from the config, and write
    // every wrapper's database object back in.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Commits the current details-page state and persists the master list
     * back to the configuration model. Clears the existing database list,
     * then re-adds each {@link DatabaseWrapper}'s underlying database in order.
     *
     * @param monitor  the Eclipse progress monitor (not used)
     */
    public void doSave( IProgressMonitor monitor )
    {
        // Committing information on the details page
        detailsPage.commit( true );

        // Saving the databases
        getPage().getConfiguration().clearDatabases();

        for ( DatabaseWrapper databaseWrapper : databaseWrappers )
        {
            getPage().getConfiguration().add( databaseWrapper.getDatabase() );
        }
    }
}
