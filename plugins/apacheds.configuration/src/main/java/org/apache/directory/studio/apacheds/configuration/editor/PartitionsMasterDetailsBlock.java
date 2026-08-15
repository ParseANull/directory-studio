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
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.server.config.beans.DirectoryServiceBean;
import org.apache.directory.server.config.beans.IndexBean;
import org.apache.directory.server.config.beans.JdbmIndexBean;
import org.apache.directory.server.config.beans.JdbmPartitionBean;
import org.apache.directory.server.config.beans.PartitionBean;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;


// ── CLASS: PartitionsMasterDetailsBlock — Imperial Vault Registry ─────────
// In the Death Star's vault wing, there is a central registry room: on the left
// wall hangs the complete roster of all data vaults, and when you tap one, the
// right side of the room fills with that vault's full configuration dossier.
// This class is that registry room — the Eclipse MasterDetailsBlock that splits
// the Partitions tab into a master list on the left and a details panel on the
// right, with Add and Delete buttons to manage the registry.
// ─────────────────────────────────────────────────────────────────────────
/**
 * The master/details block displayed on the Partitions configuration page.
 * Extends Eclipse Forms' {@link MasterDetailsBlock} to provide a scrollable
 * partition list on the left and a {@link PartitionDetailsPage} on the right
 * that updates whenever the user clicks a different partition in the list.
 * Think of this class as the Imperial vault registry room: one wall shows the
 * complete vault roster, and the opposite wall shows all the details for
 * whichever vault the registry master has selected.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PartitionsMasterDetailsBlock extends MasterDetailsBlock
{
    private static final String NEW_ID = Messages.getString( "PartitionsMasterDetailsBlock.PartitionNewId" ); //$NON-NLS-1$

    /** The associated page */
    private PartitionsPage page;

    /** The Details Page */
    private PartitionDetailsPage detailsPage;

    /** The partition wrappers */
    private List<PartitionWrapper> partitionWrappers = new ArrayList<PartitionWrapper>();

    // UI Fields
    private TableViewer viewer;
    private Button addButton;
    private Button deleteButton;


    // ── Registry Master Opens the Vault Wing for Business ────────────────────
    // The registry master arrives at the vault wing entrance, badge in hand,
    // and takes their seat behind the master roster. They need to know which
    // Partitions page they report to so they can escalate dirty notifications.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code PartitionsMasterDetailsBlock} linked to the given page.
     * Stores the page reference so we can access the config bean and notify the
     * editor when changes occur.
     *
     * <p>For example — the registry master takes their post:</p>
     * <pre>
     *   Registry master arrives, checks which Partitions page they answer to,
     *   and sits down at the master roster desk ready to manage vault records.
     * </pre>
     *
     * @param page  the {@link PartitionsPage} that hosts this master/details block
     */
    public PartitionsMasterDetailsBlock( PartitionsPage page )
    {
        this.page = page;
    }


    // ── Master Roster Board Built on the Left Wall ────────────────────────────
    // The registry master supervises the construction of the roster display:
    // a scrollable table of vault names on the left, with Add and Delete buttons
    // beneath it, and the section part wired into the managed form so selection
    // events fire to the right-hand details panel.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds the master (left-side) panel containing the partition list and buttons.
     * Creates a titled Section, a table viewer showing all partitions, and Add/Delete
     * buttons. Wires the table viewer's selection to the managed form so that picking
     * a partition automatically updates the right-side details panel.
     * Calls {@link #initFromInput()} and {@link #addListeners()} to finish setup.
     *
     * <p>For example — the roster board goes up:</p>
     * <pre>
     *   Section title: "All Partitions"
     *   Table: | system        |
     *          | example       |   [Add]
     *          | ...           |   [Delete]
     *   Selecting a row fires a managed form selection event to the details panel.
     * </pre>
     *
     * @param managedForm  the managed form that coordinates master/details communication
     * @param parent       the left-side composite to place the section inside
     */
    protected void createMasterPart( final IManagedForm managedForm, Composite parent )
    {
        FormToolkit toolkit = managedForm.getToolkit();

        // Creating the Section
        Section section = toolkit.createSection( parent, Section.TITLE_BAR );
        section.setText( Messages.getString( "PartitionsMasterDetailsBlock.AllPartitions" ) ); //$NON-NLS-1$
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
        viewer.setLabelProvider( PartitionsPage.PARTITIONS_LABEL_PROVIDER );
        viewer.setComparator( PartitionsPage.PARTITIONS_COMPARATOR );

        // Creating the button(s)
        addButton = toolkit.createButton( client, Messages.getString( "PartitionsMasterDetailsBlock.Add" ), SWT.PUSH ); //$NON-NLS-1$
        addButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        deleteButton = toolkit.createButton( client,
            Messages.getString( "PartitionsMasterDetailsBlock.Delete" ), SWT.PUSH ); //$NON-NLS-1$
        deleteButton.setEnabled( false );
        deleteButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        initFromInput();
        addListeners();
    }


    // ── Roster Board Loaded from the Config Bean ──────────────────────────────
    // The registry master clears the current roster, walks to the master config
    // archive, reads out every registered partition, and posts each name on the
    // board wrapped in its own protective cover (PartitionWrapper).
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Populates the partition list from the current configuration bean.
     * Clears {@link #partitionWrappers}, creates a new {@link PartitionWrapper}
     * for each {@link PartitionBean} in the directory service, and feeds the
     * updated list into the table viewer.
     */
    private void initFromInput()
    {
        partitionWrappers.clear();

        for ( PartitionBean partition : page.getConfigBean().getDirectoryServiceBean().getPartitions() )
        {
            partitionWrappers.add( new PartitionWrapper( partition ) );
        }

        viewer.setInput( partitionWrappers );
    }


    // ── Registry Master Refreshes the Roster Board ───────────────────────────
    // A signal comes in that the underlying config has changed — perhaps a
    // partition was added or removed programmatically. The master clears and
    // reloads the board from scratch, then asks the table to repaint.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reloads the partition list from the configuration bean and refreshes
     * the table viewer.
     * Delegates to {@link #initFromInput()} for the data load, then calls
     * {@code viewer.refresh()} to repaint the table.
     */
    public void refreshUI()
    {
        initFromInput();
        viewer.refresh();
    }


    // ── Guards and Clerks Posted at the Roster Controls ──────────────────────
    // The registry master assigns clerks to watch the table and the two buttons.
    // The table clerk enables the Delete button when a vault is selected (and
    // disables it again if the system partition is selected — that one is
    // protected). The Add clerk creates a new vault; the Delete clerk removes one.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Wires the selection, Add, and Delete listeners to the master-list UI controls.
     * The table selection listener manages Delete button state and protects the system
     * partition from deletion. The Add button listener calls {@link #addNewPartition()};
     * the Delete button listener calls {@link #deleteSelectedPartition()}.
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

                // Delete button is not enabled in the case of the system partition
                if ( !selection.isEmpty() )
                {
                    PartitionWrapper partitionWrapper = ( PartitionWrapper ) selection.getFirstElement();
                    if ( PartitionsPage.isSystemPartition( partitionWrapper.getPartition() ) )
                    {
                        deleteButton.setEnabled( false );
                    }
                }
            }
        } );

        addButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                addNewPartition();
            }
        } );

        deleteButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                deleteSelectedPartition();
            }
        } );
    }


    // ── New Vault Registered with Default Spec Sheet ──────────────────────────
    // A new vault is being commissioned: the registry master generates a unique
    // call sign, writes up a standard JDBM spec sheet with sensible defaults and
    // a full set of starter indexes, and posts the new entry on the roster board.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new JDBM partition with default settings and adds it to the roster.
     * Generates a unique ID via {@link #getNewId()}, sets a default suffix DN, cache size,
     * sync-on-write, context entry LDIF, and a standard set of search indexes.
     * Wraps it in a {@link PartitionWrapper}, adds it to the list, refreshes the viewer,
     * selects the new row, and marks the editor dirty.
     */
    private void addNewPartition()
    {
        String newId = getNewId();

        JdbmPartitionBean newPartitionBean = new JdbmPartitionBean();
        newPartitionBean.setPartitionId( newId );
        try
        {
            newPartitionBean.setPartitionSuffix( new Dn( "dc=" + newId + ",dc=com" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch ( LdapInvalidDnException e1 )
        {
            // Will never happen
        }

        // Default values
        newPartitionBean.setPartitionCacheSize( 100 );
        newPartitionBean.setJdbmPartitionOptimizerEnabled( true );
        newPartitionBean.setPartitionSyncOnWrite( true );
        newPartitionBean.setContextEntry( getContextEntryLdif( newPartitionBean.getPartitionSuffix() ) );
        List<IndexBean> indexes = new ArrayList<IndexBean>();
        indexes.add( createJdbmIndex( "apacheAlias", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "apacheOneAlias", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "apacheOneLevel", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "apachePresence", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "apacheRdn", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "apacheSubAlias", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "apacheSubLevel", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "dc", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "entryCSN", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "entryUUID", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "krb5PrincipalName", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "objectClass", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "ou", 100 ) ); //$NON-NLS-1$
        indexes.add( createJdbmIndex( "uid", 100 ) ); //$NON-NLS-1$
        newPartitionBean.setIndexes( indexes );

        PartitionWrapper newPartitionWrapper = new PartitionWrapper( newPartitionBean );
        partitionWrappers.add( newPartitionWrapper );
        viewer.refresh();
        viewer.setSelection( new StructuredSelection( newPartitionWrapper ) );
        setEditorDirty();
    }


    // ── Unique Call Sign Generated for the New Vault ─────────────────────────
    // The new vault needs a call sign that nobody else in the registry is using.
    // We start with the base template name and append an incrementing counter
    // until we find one that isn't already taken on the current roster.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Generates a unique partition ID for a newly added partition.
     * Starts with the base template name and appends an incrementing counter
     * until the result doesn't collide with any existing partition ID (case-insensitive).
     *
     * <p>For example — the registry master finds an open call sign:</p>
     * <pre>
     *   "NewPartition1" is taken → try "NewPartition2" → also taken
     *   → "NewPartition3" is free → return "NewPartition3"
     * </pre>
     *
     * @return  a partition ID string that is not already in use
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

            for ( PartitionBean partition : page.getConfigBean().getDirectoryServiceBean().getPartitions() )
            {
                if ( partition.getPartitionId().equalsIgnoreCase( name ) )
                {
                    ok = false;
                }
            }
            counter++;
        }

        return name;
    }


    // ── Vault's Founding LDIF Document Generated from Its DN ─────────────────
    // Every new vault needs a founding document — the LDIF string that describes
    // the root entry anchoring the whole LDAP subtree. We derive it automatically
    // from the vault's suffix DN using standard schema constants.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Generates the LDIF string for a partition's context (root) entry from its suffix DN.
     * Builds a {@link DefaultEntry} with {@code objectClass: domain} and {@code objectClass: top},
     * adds {@code extensibleObject} if the RDN type is not {@code dc}, then serializes to LDIF.
     * Returns {@code null} if anything goes wrong during construction.
     *
     * <p>For example — the vault's founding charter drawn up:</p>
     * <pre>
     *   dn: dc=example,dc=com
     *   objectClass: domain
     *   objectClass: top
     *   dc: example
     * </pre>
     *
     * @param dn  the suffix DN to base the context entry on
     * @return    the LDIF string for the context entry, or {@code null} on error
     */
    public static String getContextEntryLdif( Dn dn )
    {
        try
        {
            Entry entry = new DefaultEntry( dn );
            entry.add( SchemaConstants.OBJECT_CLASS_AT, SchemaConstants.DOMAIN_OC );
            entry.add( SchemaConstants.OBJECT_CLASS_AT, SchemaConstants.TOP_OC );

            // Getting the RDN from the DN
            Rdn rdn = dn.getRdn();

            if ( !SchemaConstants.DC_AT.equalsIgnoreCase( rdn.getType() ) )
            {
                entry.add( SchemaConstants.OBJECT_CLASS_AT, SchemaConstants.EXTENSIBLE_OBJECT_OC );
                entry.add( SchemaConstants.DC_AT, rdn.getValue() );
            }

            entry.add( rdn.getType(), rdn.getValue() );

            LdifEntry ldifEntry = new LdifEntry( entry );

            return ldifEntry.toString();
        }
        catch ( Exception e )
        {
            return null;
        }
    }


    // ── Selected Vault Decommissioned After Confirmation ─────────────────────
    // The registry master selects a vault from the roster, confirms the deletion
    // request with the officer (a dialog box that asks "are you sure?"), and
    // removes it from the working list if approved. The system vault is immune.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Removes the currently selected partition from the working list after confirmation.
     * Shows a confirmation dialog with the partition's ID and suffix before deleting.
     * Refuses to delete the system partition even if selected.
     * Marks the editor dirty if the user confirms the deletion.
     */
    private void deleteSelectedPartition()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();

        if ( !selection.isEmpty() )
        {
            PartitionWrapper partitionWrapper = ( PartitionWrapper ) selection.getFirstElement();

            PartitionBean partition = partitionWrapper.getPartition();

            if ( !PartitionsPage.isSystemPartition( partition ) )
            {
                if ( MessageDialog
                    .openConfirm(
                        page.getManagedForm().getForm().getShell(),
                        Messages.getString( "PartitionsMasterDetailsBlock.ConfirmDelete" ), //$NON-NLS-1$
                        NLS.bind(
                            Messages.getString( "PartitionsMasterDetailsBlock.AreYouSureDeletePartition" ), partition.getPartitionId(), //$NON-NLS-1$
                            partition.getPartitionSuffix() ) ) )
                {
                    partitionWrappers.remove( partitionWrapper );
                    setEditorDirty();
                }
            }
        }
    }


    // ── Standard JDBM Index Entry Created ────────────────────────────────────
    // When we commission a new vault, it needs a set of pre-built index entries
    // to get it operational. Each index is a simple JDBM bean with an attribute
    // ID and a cache size — we stamp them out here like Imperial-standard parts.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a configured {@link JdbmIndexBean} with the given attribute ID and cache size.
     * Used when provisioning a new partition with its standard set of starter indexes.
     *
     * @param indexAttributeId  the LDAP attribute name to index (e.g., {@code "objectClass"})
     * @param indexCacheSize    the number of entries to keep in the index cache
     * @return                  a fully configured {@link JdbmIndexBean}
     */
    private JdbmIndexBean createJdbmIndex( String indexAttributeId, int indexCacheSize )
    {
        JdbmIndexBean index = new JdbmIndexBean();

        index.setIndexAttributeId( indexAttributeId );
        index.setIndexCacheSize( indexCacheSize );

        return index;
    }


    // ── Editor Flagged as Having Unsaved Changes ──────────────────────────────
    // Something on the roster or in the vault dossier has been modified.
    // We signal the Eclipse editor framework so the Save button lights up,
    // commit the details page to capture any in-flight edits, and refresh the table.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Marks the parent editor as dirty, commits the details page, and refreshes
     * the master list table.
     * Call this whenever a partition is added, deleted, or its properties change,
     * so the Eclipse framework knows unsaved changes exist.
     */
    public void setEditorDirty()
    {
        ( ( ServerConfigurationEditor ) page.getEditor() ).setDirty( true );
        detailsPage.commit( false );
        viewer.refresh();
    }


    // ── Details Panel Registered for Partition Wrappers ──────────────────────
    // The Eclipse Forms framework needs to know which details panel to show on
    // the right when the user clicks a PartitionWrapper in the master list.
    // We create our PartitionDetailsPage and register it for that class.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Registers the {@link PartitionDetailsPage} as the details panel for
     * {@link PartitionWrapper} objects in the master list.
     * Called by the Eclipse Forms framework during initialization of the
     * master/details block.
     *
     * @param detailsPart  the Eclipse Forms {@link DetailsPart} to register pages with
     */
    protected void registerPages( DetailsPart detailsPart )
    {
        detailsPage = new PartitionDetailsPage( this );
        detailsPart.registerPage( PartitionWrapper.class, detailsPage );
    }


    // ── Toolbar Actions Stub — Nothing Deployed Yet ───────────────────────────
    // The toolbar above the master/details block is currently empty — no
    // filter, sort, or view-mode buttons have been commissioned yet.
    // This is a placeholder that the Eclipse Forms framework requires us to provide.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Hook for adding toolbar actions to the managed form's toolbar.
     * Currently a no-op — no toolbar actions have been implemented for the
     * Partitions master/details block.
     *
     * @param managedForm  the managed form whose toolbar we could populate
     */
    protected void createToolBarActions( IManagedForm managedForm )
    {
        // TODO Auto-generated method stub

    }


    // ── Registry Master Hands Back the Parent Page Reference ─────────────────
    // The vault chief or other collaborators sometimes need to get back to the
    // containing Partitions page — for example, to access the config bean or
    // the Eclipse editor instance. This method hands out that reference.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link PartitionsPage} that hosts this master/details block.
     * Useful for collaborators (e.g., {@link PartitionDetailsPage}) that need
     * access to the editor or the configuration bean.
     *
     * @return  the owning {@link PartitionsPage}
     */
    public PartitionsPage getPage()
    {
        return page;
    }


    // ── Vault Registry Filed Away into the Official Config Archive ────────────
    // Save time: the registry master tells the vault chief to file their latest
    // edits, then sweeps through the entire working roster and copies every
    // PartitionBean back into the authoritative DirectoryServiceBean record.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Saves all partition data from the working list back into the configuration model.
     * Commits the details page first to capture any unsaved in-flight edits,
     * then builds a fresh partition list from the current {@link PartitionWrapper}
     * instances and pushes it to the {@link DirectoryServiceBean}.
     *
     * @param monitor  the progress monitor (not used here, but required by the signature)
     */
    public void doSave( IProgressMonitor monitor )
    {
        // Committing information on the details page
        detailsPage.commit( true );

        // Getting the directory service bean
        DirectoryServiceBean directoryServiceBean = page.getConfigBean().getDirectoryServiceBean();

        // Creating a new list of partitions
        List<PartitionBean> newPartitions = new ArrayList<PartitionBean>();

        // Saving the partitions
        for ( PartitionWrapper partitionWrapper : partitionWrappers )
        {
            newPartitions.add( partitionWrapper.getPartition() );
        }

        directoryServiceBean.setPartitions( newPartitions );
    }
}
