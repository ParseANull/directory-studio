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


import org.apache.directory.api.ldap.model.constants.LdapConstants;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.message.AliasDerefMode;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.server.config.beans.ReplConsumerBean;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPluginConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: ReplicationMasterDetailsBlock — THE REBEL INTELLIGENCE NETWORK BOARD ─────────
// General Dodonna stands before the Rebel intelligence board on Yavin 4: on the left side
// is the roster of every active replication consumer — each one an outpost pulling data
// from a remote server.  Tap one and the right side opens its full communications brief
// for editing.  That split-screen operations board is exactly what this class renders.
// ─────────────────────────────────────────────────────────────────────────────────────────
/**
 * The master/details block that drives the Replication page of the ApacheDS configuration
 * editor.
 * It wires together a sorted list of all known replication consumers (the master side) and
 * the full settings panel for whichever consumer is currently selected (the details side).
 * Think of this class as the Rebel intelligence network board — the left column lists every
 * sync outpost; tap one and the right column opens its communications dossier for editing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReplicationMasterDetailsBlock extends MasterDetailsBlock
{
    private static final String NEW_ID = "consumer";

    /** The associated page */
    private ReplicationPage page;

    /** The Details Page */
    private ReplicationDetailsPage detailsPage;

    // UI Fields
    private TableViewer viewer;
    private Button addButton;
    private Button deleteButton;


    // ── Commissioning The Intelligence Board ──────────────────────────────────────────────
    // General Dodonna walks into the operations room and plugs the board into the Replication
    // page — handing it the page reference so it can reach the editor's config bean later.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a new ReplicationMasterDetailsBlock and ties it to its parent page.
     * We store the page reference so later methods can query the config bean and
     * the browser connection.
     *
     * <p>For example — Dodonna initialises the intelligence board for the operations room:</p>
     * <pre>
     *   He hands the board a master key to the Replication page so it can reach all state.
     *   From that moment the board knows where to send every status update.
     * </pre>
     *
     * @param page  the {@link ReplicationPage} that hosts this block
     */
    public ReplicationMasterDetailsBlock( ReplicationPage page )
    {
        this.page = page;
    }


    // ── Splitting The Operations Screen ───────────────────────────────────────────────────
    // The Rebel ops room divides its main display into two panes: the consumer roster on
    // the left takes up 30% of the width, the consumer briefing on the right gets 70%.
    // We call super to lay out the sash form, then set those proportions explicitly.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Lays out the master/details split and sets the sash weights so the consumer list gets
     * 30% of the width and the detail panel gets 70%.
     * Delegates the heavy lifting to the parent {@link MasterDetailsBlock}, then adjusts
     * the proportions to give the detail panel plenty of space for the connection settings.
     *
     * <p>For example — the Rebel ops team sizes their split-screen display:</p>
     * <pre>
     *   The roster shrinks to show consumer IDs.
     *   The briefing panel expands to show all the connection and configuration fields.
     * </pre>
     *
     * @param managedForm  the Eclipse managed form that owns this block's widgets
     */
    public void createContent( IManagedForm managedForm )
    {
        super.createContent( managedForm );

        this.sashForm.setWeights( new int[]
            { 30, 70 } );
    }


    // ── Building The Consumer Roster ──────────────────────────────────────────────────────
    // A Rebel archivist sets up the left-hand roster board in the operations room: a sorted
    // list of every replication consumer, each shown by ID and accompanied by an Add and a
    // Delete button so the general can commission or decommission outposts on the fly.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the master (left-hand) panel: a titled section containing a sorted table
     * of all replication consumers plus Add and Delete action buttons.
     * This is the list side of the master/details split — clicking a consumer here drives
     * what appears in the detail panel on the right.
     *
     * <p>For example — the Rebel archivist assembles the intelligence outpost roster:</p>
     * <pre>
     *   She mounts a scrollable list, each row showing a consumer ID.
     *   An Add button lets commanders enrol new outposts; Delete decommissions old ones.
     *   Selecting a row fires a selection event so the detail briefing refreshes.
     * </pre>
     *
     * @param managedForm  the form that coordinates selection events between master and detail
     * @param parent       the SWT composite to build inside
     */
    protected void createMasterPart( final IManagedForm managedForm, Composite parent )
    {
        FormToolkit toolkit = managedForm.getToolkit();

        sashForm.setOrientation( SWT.HORIZONTAL );

        // Creating the Section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( "All Replication Consummers" );
        section.marginWidth = 10;
        section.marginHeight = 5;
        Composite client = toolkit.createComposite( section, SWT.WRAP );
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout( layout );
        toolkit.paintBordersFor( client );
        section.setClient( client );

        // Creating the Table and Table Viewer
        Table table = toolkit.createTable( client, SWT.NULL );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 2 );
        gd.heightHint = 20;
        gd.widthHint = 100;
        table.setLayoutData( gd );
        final SectionPart spart = new SectionPart( section );
        managedForm.addPart( spart );
        viewer = new TableViewer( table );
        viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                managedForm.fireSelectionChanged( spart, event.getSelection() );
            }
        } );
        viewer.setContentProvider( new ArrayContentProvider() );
        viewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof ReplConsumerBean )
                {
                    ReplConsumerBean consumer = ( ReplConsumerBean ) element;

                    return consumer.getReplConsumerId();
                }

                return super.getText( element );
            }


            public Image getImage( Object element )
            {
                if ( element instanceof ReplConsumerBean )
                {
                    return ApacheDS2ConfigurationPlugin.getDefault().getImage(
                        ApacheDS2ConfigurationPluginConstants.IMG_REPLICATION_CONSUMER );
                }

                return super.getImage( element );
            }
        } );
        viewer.setComparator( new ViewerComparator()
        {
            public int compare( Viewer viewer, Object e1, Object e2 )
            {
                if ( ( e1 instanceof ReplConsumerBean ) && ( e2 instanceof ReplConsumerBean ) )
                {
                    ReplConsumerBean o1 = ( ReplConsumerBean ) e1;
                    ReplConsumerBean o2 = ( ReplConsumerBean ) e2;

                    String id1 = o1.getReplConsumerId();
                    String id2 = o2.getReplConsumerId();

                    if ( ( id1 != null ) && ( id2 != null ) )
                    {
                        return id1.compareTo( id2 );
                    }
                }

                return super.compare( viewer, e1, e2 );
            }
        } );

        // Creating the button(s)
        addButton = toolkit.createButton( client, "Add", SWT.PUSH );
        addButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        deleteButton = toolkit.createButton( client, "Delete", SWT.PUSH );
        deleteButton.setEnabled( false );
        deleteButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        initFromInput();
        addListeners();
    }


    // ── Refreshing The Roster Display ─────────────────────────────────────────────────────
    // After a config reload the archivist re-reads the consumer list from the model and
    // redraws every row on the roster board so nothing is stale.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the consumer list from the config model and forces the table to repaint.
     * Call this after external changes (such as loading a new config file) that the viewer
     * wouldn't otherwise know about.
     *
     * <p>For example — the archivist refreshes the board after a new intelligence report arrives:</p>
     * <pre>
     *   She re-reads the consumer list from the LDAP server bean.
     *   The roster board redraws so every officer sees the latest picture.
     * </pre>
     */
    public void refreshUI()
    {
        initFromInput();
        viewer.refresh();
    }

    // ── Loading The Consumer Dossiers ─────────────────────────────────────────────────────
    // The archivist walks to the model's filing cabinet, grabs the current list of
    // replication consumers from the LDAP server bean, and stacks them on the roster board.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the table viewer from the LDAP server bean's current list of replication
     * consumers.
     * Called during initial build and again on every {@link #refreshUI()} call.
     *
     * <p>For example — the archivist loads the intelligence outpost dossiers:</p>
     * <pre>
     *   She locates the LDAP server bean in the config model.
     *   She drops all its consumer beans onto the roster board.
     * </pre>
     */
    private void initFromInput()
    {
        viewer.setInput( page.getConfigBean().getDirectoryServiceBean().getLdapServerBean().getReplConsumers() );
    }


    // ── Arming The Roster Controls ─────────────────────────────────────────────────────────
    // A Rebel technician wires the selection trigger and the Add/Delete buttons so every
    // interaction routes to the correct handler: selection enables Delete, Add creates a
    // consumer, Delete removes the selected one.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Attaches event listeners to the table viewer and the Add/Delete buttons.
     * The viewer selection listener keeps the Delete button in sync with the current row.
     *
     * <p>For example — the technician wires the operations board's control panel:</p>
     * <pre>
     *   Selecting a row lights up the Delete button.
     *   Pressing Add triggers a new consumer with safe defaults.
     *   Pressing Delete opens a confirmation dialog and removes on confirm.
     * </pre>
     */
    private void addListeners()
    {
        viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                viewer.refresh();

                // Getting the selection of the table viewer
                StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();

                // Delete button is enabled when something is selected
                deleteButton.setEnabled( !selection.isEmpty() );
            }
        } );

        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addNewConsumer();
            }
        } );

        deleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                deleteSelectedConsumer();
            }
        } );
    }


    // ── Commissioning A New Replication Outpost ───────────────────────────────────────────
    // A Rebel commander decides to add a new sync outpost to the network: we mint a unique
    // consumer ID, build a bean with sensible defaults, register it with the LDAP server
    // bean, and select the new row so the detail panel opens immediately.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ReplConsumerBean} with safe defaults, registers it with the LDAP
     * server bean, refreshes the viewer, and selects the new row so the detail panel opens.
     * Auto-selecting the new row means the user lands straight in the detail panel ready
     * to customise the consumer's connection settings.
     *
     * <p>For example — a Rebel commander commissions a new intelligence outpost:</p>
     * <pre>
     *   She stamps a fresh form with a unique ID like "consumer1".
     *   She fills in defaults: localhost:10389, admin bind, subtree scope, all-user-attributes.
     *   The outpost goes onto the board and she highlights it so it's ready to configure.
     * </pre>
     */
    private void addNewConsumer()
    {
        String newId = getNewId();

        ReplConsumerBean consumerBean = getNewReplConsumerBean();

        consumerBean.setReplConsumerId( newId );

        page.getConfigBean().getDirectoryServiceBean().getLdapServerBean().addReplConsumers( consumerBean );
        viewer.refresh();
        viewer.setSelection( new StructuredSelection( consumerBean ) );
        setEditorDirty();
    }


    // ── Minting A Default Consumer Bean ───────────────────────────────────────────────────
    // Before a new consumer gets its unique ID stamped on it, we need a bean with all the
    // standard field values that will let the consumer actually connect on first try.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a new {@link ReplConsumerBean} pre-populated with sensible defaults: enabled,
     * pointing at localhost:10389, using the admin DN and a known password, subtree scope,
     * all-user-attributes, refresh-and-persist mode with a 60-second interval.
     * These defaults match a typical local-development ApacheDS setup and can be overridden
     * in the detail panel immediately after creation.
     *
     * <p>For example — the Rebel supply office issues a standard outpost field kit:</p>
     * <pre>
     *   Enabled = true, host = "localhost", port = 10389, bind = "uid=admin,ou=system".
     *   Scope = subtree, attributes = *, refresh interval = 60 000 ms.
     *   The outpost commander can then tailor each setting to her actual target.
     * </pre>
     *
     * @return  a new {@link ReplConsumerBean} filled with usable default values
     */
    private ReplConsumerBean getNewReplConsumerBean()
    {
        ReplConsumerBean consumerBean = new ReplConsumerBean();

        consumerBean.setEnabled( true );
        consumerBean.setReplAliasDerefMode( AliasDerefMode.NEVER_DEREF_ALIASES.getJndiValue() );
        consumerBean.setReplProvHostName( "localhost" );
        consumerBean.setReplProvPort( 10389 );
        consumerBean.setReplSearchFilter( LdapConstants.OBJECT_CLASS_STAR );
        consumerBean.setReplSearchScope( SearchScope.SUBTREE.getLdapUrlValue() );
        consumerBean.setReplUserDn( "uid=admin,ou=system" );
        consumerBean.setReplUserPassword( "secret".getBytes() );
        consumerBean.setReplRefreshInterval( 60 * 1000 );
        consumerBean.setReplRefreshNPersist( true );
        consumerBean.addReplAttributes( SchemaConstants.ALL_USER_ATTRIBUTES );
        consumerBean.setSearchBaseDn( "dc=example,dc=com" );

        return consumerBean;
    }


    // ── Minting A Unique Consumer ID ──────────────────────────────────────────────────────
    // The Rebel naming bureau counts up from 1 until it finds an ID that no existing
    // consumer has already claimed — "consumer1", "consumer2", and so on.
    // We loop through the existing consumers and increment the counter until we find a
    // collision-free name.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Generates a unique ID for a new replication consumer by appending an incrementing
     * counter to the base name {@code "consumer"} until we find one that isn't taken.
     * This keeps IDs predictable and guarantees we never silently clobber an existing entry.
     *
     * <p>For example — the Rebel naming bureau stamps a fresh outpost badge:</p>
     * <pre>
     *   She checks "consumer1" — already in use by another outpost.
     *   She tries "consumer2" — the roster confirms it's free.
     *   That becomes the new consumer's ID.
     * </pre>
     *
     * @return  a unique ID string that no existing consumer is currently using
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

            for ( ReplConsumerBean consumer : page.getConfigBean().getDirectoryServiceBean().getLdapServerBean()
                .getReplConsumers() )
            {
                if ( consumer.getReplConsumerId().equalsIgnoreCase( name ) )
                {
                    ok = false;
                }
            }
            counter++;
        }

        return name;
    }


    // ── Decommissioning A Selected Outpost ────────────────────────────────────────────────
    // A Rebel general decides an outpost is no longer needed and hits Delete — a confirmation
    // dialog pops up to make sure it wasn't a mis-click.  On confirmation the consumer is
    // removed from the LDAP server bean and the editor is marked dirty.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected replication consumer after prompting the user to confirm.
     * We guard against an empty selection to avoid a confusing no-op.
     *
     * <p>For example — the general decommissions an obsolete outpost:</p>
     * <pre>
     *   She selects "consumer3" on the roster and hits Delete.
     *   A dialog asks: "Are you sure you want to delete replication consumer 'consumer3'?"
     *   On confirmation, the consumer is removed and the editor turns dirty.
     * </pre>
     */
    private void deleteSelectedConsumer()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( !selection.isEmpty() )
        {
            ReplConsumerBean consumer = ( ReplConsumerBean ) selection.getFirstElement();

            if ( MessageDialog.openConfirm( page.getManagedForm().getForm().getShell(), "Confirm Delete",
                NLS.bind( "Are you sure you want to delete replication consumer ''{0}''?",
                    consumer.getReplConsumerId() ) ) )
            {
                page.getConfigBean().getDirectoryServiceBean().getLdapServerBean().getReplConsumers().remove( consumer );
                setEditorDirty();
            }
        }

    }


    // ── Hooking Up The Detail Briefing Panel ──────────────────────────────────────────────
    // The operations room installs the detail briefing screen and tells the framework:
    // "whenever a ReplConsumerBean is selected, show it on this panel."
    // We create a ReplicationDetailsPage and register it for that class.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers {@link ReplicationDetailsPage} as the details renderer for
     * {@link ReplConsumerBean} objects.
     * Eclipse calls this once during setup; from then on, selecting a consumer in the
     * master list automatically loads its data into the details page.
     *
     * <p>For example — the ops room installs the intelligence briefing screen:</p>
     * <pre>
     *   She tells the framework: "ReplConsumerBean goes to ReplicationDetailsPage."
     *   From that moment, every roster selection auto-populates the right-hand panel.
     * </pre>
     *
     * @param detailsPart  the Eclipse details part that manages which page is shown
     */
    protected void registerPages( DetailsPart detailsPart )
    {
        detailsPage = new ReplicationDetailsPage( this );
        detailsPart.registerPage( ReplConsumerBean.class, detailsPage );
    }


    // ── Skipping The Toolbar This Time ────────────────────────────────────────────────────
    // The Rebel ops team checked the spec and decided the roster board needs no toolbar.
    // This override is required by the framework but has nothing to do.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Intentional no-op: this block does not need a toolbar.
     * The {@link MasterDetailsBlock} contract demands we provide this method even when we
     * have nothing to add.
     *
     * @param managedForm  the form that would host any toolbar actions (unused here)
     */
    protected void createToolBarActions( IManagedForm managedForm )
    {
        // TODO Auto-generated method stub

    }


    // ── Flagging The Config File As Modified ──────────────────────────────────────────────
    // After any change to the consumer list, a Rebel dispatcher sends a "MODIFIED" signal
    // to the main editor console so the save button lights up.
    // We flip the dirty flag and refresh the viewer to keep labels consistent.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent {@link ServerConfigurationEditor} as dirty (unsaved changes) and
     * triggers a viewer refresh so consumer labels update immediately.
     * Any mutation — add, delete, or field edit — should call this so the editor's save
     * button activates.
     *
     * <p>For example — the Rebel dispatcher signals an unsaved change to the general:</p>
     * <pre>
     *   She presses the "MODIFIED" indicator after a consumer is added or edited.
     *   The save button lights up on the editor console.
     * </pre>
     */
    public void setEditorDirty()
    {
        ( ( ServerConfigurationEditor ) page.getEditor() ).setDirty( true );
        viewer.refresh();
    }


    // ── Committing The Consumer List To The Model ─────────────────────────────────────────
    // When the Rebel general issues the save order, the archivist tells the detail page to
    // flush whatever is on screen back into the in-memory model before it gets serialised.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current state of the details page into the underlying config model.
     * Called by the editor's save flow to ensure whatever is visible in the detail panel
     * is persisted alongside the rest of the configuration.
     *
     * <p>For example — the archivist files the open briefing document before shutdown:</p>
     * <pre>
     *   She takes whatever the officer was editing in the right-hand panel.
     *   She stamps it "COMMITTED" and pushes it back into the in-memory config model.
     * </pre>
     */
    public void save()
    {
        detailsPage.commit( true );
    }


    // ── Exposing The Parent Page Reference ────────────────────────────────────────────────
    // Other classes (notably ReplicationDetailsPage) need to reach the ReplicationPage to
    // resolve the browser connection tied to this server's configuration.
    // ─────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ReplicationPage} that hosts this master/details block.
     * Used by {@link ReplicationDetailsPage} to resolve the browser connection for the
     * entry and filter widgets.
     *
     * <p>For example — a Rebel officer asks the board for its control room reference:</p>
     * <pre>
     *   The detail panel needs to know which server connection to use for DN auto-complete.
     *   She asks the block, which hands back the Replication page reference.
     * </pre>
     *
     * @return  the parent {@link ReplicationPage}
     */
    public ReplicationPage getPage()
    {
        return this.page;
    }
}
