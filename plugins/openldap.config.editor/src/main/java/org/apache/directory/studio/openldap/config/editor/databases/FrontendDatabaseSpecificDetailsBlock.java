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
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.apache.directory.studio.openldap.common.ui.dialogs.AttributeDialog;
import org.apache.directory.studio.openldap.common.ui.widgets.EntryWidget;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPlugin;
import org.apache.directory.studio.openldap.config.OpenLdapConfigurationPluginConstants;
import org.apache.directory.studio.openldap.config.model.AuxiliaryObjectClass;
import org.apache.directory.studio.openldap.config.model.OlcFrontendConfig;
import org.apache.directory.studio.openldap.config.model.database.OlcDatabaseConfig;


// ── CLASS: FrontendDatabaseSpecificDetailsBlock — The Death Star's Front Gate ──
// Every request entering the Death Star passes through the front gate where guards
// check credentials, apply universal policy, and route traffic to the right sector.
// OpenLDAP's frontend pseudo-database is that front gate: it holds global defaults
// that apply to every operation before any real backend sees it — default search
// base, allowed password hash schemes, and which attributes to keep sorted.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Database-specific UI block for the OpenLDAP frontend pseudo-database.
 * The frontend database isn't a real storage backend — it's a global policy layer
 * applied to all operations. Settings here include the default search base
 * (where unauthenticated searches land), which password hash algorithms the
 * server will accept, and which attributes should be kept in sorted order.
 * Think of this as the Death Star's universal processing station: every request
 * passes through it and gets stamped with these defaults.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FrontendDatabaseSpecificDetailsBlock extends AbstractDatabaseSpecificDetailsBlock<OlcDatabaseConfig>
{
    private static final String SHA = "{SHA}";
    private static final String SSHA = "{SSHA}";
    private static final String CRYPT = "{CRYPT}";
    private static final String MD5 = "{MD5}";
    private static final String SMD5 = "{SMD5}";
    private static final String CLEARTEXT = "{CLEARTEXT}";

    /** The list of sorted attributes values kept in sync with the table viewer. */
    private List<String> sortedValuesAttributesList = new ArrayList<>();

    // UI Fields
    /** The default search base DN picker. */
    private EntryWidget defaultSearchBaseEntryWidget;
    /** Checkboxes for each supported password hash algorithm. */
    private Button shaCheckbox;
    private Button sshaCheckbox;
    private Button cryptCheckbox;
    private Button md5Checkbox;
    private Button smd5Checkbox;
    private Button cleartextCheckbox;
    /** The table showing which attributes are kept in sorted order. */
    private Table sortedValuesTable;
    private TableViewer sortedValuesTableViewer;
    /** Add/delete buttons for the sorted-values attribute list. */
    private Button addSortedValueButton;
    private Button deleteSortedValueButton;

    // Listeners
    private ISelectionChangedListener sortedValuesTableViewerSelectionChangedListener = event ->
        deleteSortedValueButton.setEnabled( !sortedValuesTableViewer.getSelection().isEmpty() );

    private SelectionListener addSortedValueButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            AttributeDialog dialog = new AttributeDialog( addSortedValueButton.getShell(), browserConnection );
            if ( dialog.open() == AttributeDialog.OK )
            {
                String attribute = dialog.getAttribute();

                if ( !sortedValuesAttributesList.contains( attribute ) )
                {
                    sortedValuesAttributesList.add( attribute );
                    sortedValuesTableViewer.refresh();
                    sortedValuesTableViewer.setSelection( new StructuredSelection( attribute ) );

                    detailsPage.setEditorDirty();
                }
            }
        }
    };

    private SelectionListener deleteSortedValueButtonSelectionListener = new SelectionAdapter()
    {
        @Override
        public void widgetSelected( SelectionEvent e )
        {
            StructuredSelection selection = ( StructuredSelection ) sortedValuesTableViewer.getSelection();

            if ( !selection.isEmpty() )
            {
                String selectedAttribute = ( String ) selection.getFirstElement();

                sortedValuesAttributesList.remove( selectedAttribute );
                sortedValuesTableViewer.refresh();

                detailsPage.setEditorDirty();
            }
        }
    };


    // ── Staff the Front Gate ───────────────────────────────────────────────────
    // The Death Star's gate-keeper takes up their post, knowing which master
    // page they report to and which live LDAP connection to use for DN browsing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the frontend-database block with the required page, model, and connection.
     * A browser connection is needed for the default search base entry widget so users
     * can browse the live LDAP tree to select a DN.
     *
     * @param detailsPage       the parent details page
     * @param database          the {@link OlcDatabaseConfig} model — carries the OlcFrontendConfig auxiliary
     * @param browserConnection the live LDAP connection for the DN picker
     */
    public FrontendDatabaseSpecificDetailsBlock( DatabasesDetailsPage detailsPage, OlcDatabaseConfig database,
        IBrowserConnection browserConnection )
    {
        super( detailsPage, database, browserConnection );
    }


    // ── Build the Front-Gate Control Panel ────────────────────────────────────
    // The gate-keeper sets up three control stations:
    //   1. Where to send unauthenticated searches (default search base)
    //   2. Which credential formats to accept (password hash)
    //   3. Which attribute values to keep sorted
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the full UI for the frontend database block.
     * Three sections: default search base, password hash algorithm checkboxes,
     * and the sorted-values attribute table.
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     * @return         the top-level composite
     */
    public Composite createBlockContent( Composite parent, FormToolkit toolkit )
    {
        // Composite
        Composite composite = toolkit.createComposite( parent );
        composite.setLayout( new GridLayout( 2, false ) );
        composite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Default Search Base Text
        toolkit.createLabel( composite, "Default Search Base:" );
        defaultSearchBaseEntryWidget = new EntryWidget( browserConnection, null, true );
        defaultSearchBaseEntryWidget.createWidget( composite, toolkit );
        defaultSearchBaseEntryWidget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Password Hash
        createPasswordHashContent( composite, toolkit );

        // Sorted Values Attributes
        createSortedValuesAttributesContent( composite, toolkit );

        return composite;
    }


    // ── Build the Credential-Format Station ───────────────────────────────────
    // The gate-keeper sets up the password hash checkpoint: six checkboxes,
    // one per supported hash format. Only ticked boxes are accepted.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the password hash checkbox group inside the block composite.
     * Each checkbox corresponds to an OpenLDAP password hash prefix ({SHA}, {SSHA}, etc.).
     * The server will only store passwords in the checked formats.
     *
     * @param parent   the parent composite to attach into
     * @param toolkit  the JFace Forms toolkit
     */
    private void createPasswordHashContent( Composite parent, FormToolkit toolkit )
    {
        // Label
        Label passwordHashLabel = toolkit.createLabel( parent, "Password Hash:" );
        passwordHashLabel.setLayoutData( new GridData( SWT.NONE, SWT.TOP, false, false ) );

        // Composite
        Composite passwordHashComposite = toolkit.createComposite( parent );
        GridLayout passwordHashCompositeGridLayout = new GridLayout( 3, true );
        passwordHashCompositeGridLayout.marginHeight = passwordHashCompositeGridLayout.marginWidth = 0;
        passwordHashComposite.setLayout( passwordHashCompositeGridLayout );
        passwordHashComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // SHA Checkbox
        shaCheckbox = toolkit.createButton( passwordHashComposite, "SHA", SWT.CHECK );

        // SSHA Checkbox
        sshaCheckbox = toolkit.createButton( passwordHashComposite, "SSHA", SWT.CHECK );

        // CRYPT Checkbox
        cryptCheckbox = toolkit.createButton( passwordHashComposite, "CRYPT", SWT.CHECK );

        // MD5 Checkbox
        md5Checkbox = toolkit.createButton( passwordHashComposite, "MD5", SWT.CHECK );

        // SMD5 Checkbox
        smd5Checkbox = toolkit.createButton( passwordHashComposite, "SMD5", SWT.CHECK );

        // CLEARTEXT Checkbox
        cleartextCheckbox = toolkit.createButton( passwordHashComposite, "CLEARTEXT", SWT.CHECK );

    }


    // ── Build the Sorted-Attributes Station ───────────────────────────────────
    // The gate-keeper sets up a table of attributes whose values will always
    // be kept in a consistent sorted order. Users can add or remove attributes
    // from this list via the Add/Delete buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the sorted-values attributes table and its Add/Delete buttons.
     * Attributes listed here will have their multi-values kept sorted by OpenLDAP's
     * valueSorting mechanism — useful for attributes like {@code cn} where order matters.
     *
     * @param parent   the parent composite
     * @param toolkit  the JFace Forms toolkit
     */
    private void createSortedValuesAttributesContent( Composite parent, FormToolkit toolkit )
    {
        // Label
        Label sortedValuesLabel = toolkit.createLabel( parent, "Maintain sorted values for these attributes:" );
        sortedValuesLabel.setLayoutData( new GridData( SWT.NONE, SWT.NONE, false, false, 2, 1 ) );

        // Composite
        Composite sortedValuesComposite = toolkit.createComposite( parent );
        GridLayout attributesCompositeGridLayout = new GridLayout( 2, false );
        attributesCompositeGridLayout.marginHeight = attributesCompositeGridLayout.marginWidth = 0;
        //        attributesCompositeGridLayout.verticalSpacing = attributesCompositeGridLayout.horizontalSpacing = 0;
        sortedValuesComposite.setLayout( attributesCompositeGridLayout );
        sortedValuesComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );

        // Table and Table Viewer
        sortedValuesTable = toolkit.createTable( sortedValuesComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        GridData gd = new GridData( SWT.FILL, SWT.FILL, true, true, 1, 3 );
        gd.heightHint = 20;
        gd.widthHint = 100;
        sortedValuesTable.setLayoutData( gd );
        sortedValuesTableViewer = new TableViewer( sortedValuesTable );
        sortedValuesTableViewer.setContentProvider( new ArrayContentProvider() );
        sortedValuesTableViewer.setLabelProvider( new LabelProvider()
        {
            @Override
            public Image getImage( Object element )
            {
                return OpenLdapConfigurationPlugin.getDefault().getImage(
                    OpenLdapConfigurationPluginConstants.IMG_ATTRIBUTE );
            }
        } );
        sortedValuesTableViewer.setInput( sortedValuesAttributesList );

        // Add Button
        addSortedValueButton = toolkit.createButton( sortedValuesComposite, "Add...", SWT.PUSH );
        addSortedValueButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );

        // Delete Button
        deleteSortedValueButton = toolkit.createButton( sortedValuesComposite, "Delete", SWT.PUSH );
        deleteSortedValueButton.setEnabled( false );
        deleteSortedValueButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
    }


    // ── Refresh the Gate's Display ────────────────────────────────────────────
    // The gate-keeper checks the current official record and updates all three
    // control stations to reflect whatever is saved there. If the auxiliary
    // OlcFrontendConfig object doesn't exist yet, everything resets to blank.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Reloads all UI widgets from the current frontend config model.
     * The frontend config is stored as an {@link OlcFrontendConfig} auxiliary object
     * on the main database entry; if none exists, we blank out all fields.
     */
    public void refresh()
    {
        removeListeners();

        if ( database != null )
        {
            OlcFrontendConfig frontendConfig = getFrontendConfig();

            if ( frontendConfig == null )
            {
                // Default Search Base
                defaultSearchBaseEntryWidget.setInput( null );

                // Password Hash
                shaCheckbox.setSelection( false );
                sshaCheckbox.setSelection( false );
                cryptCheckbox.setSelection( false );
                md5Checkbox.setSelection( false );
                smd5Checkbox.setSelection( false );
                cleartextCheckbox.setSelection( false );

                // Sorted Values Attributes
                sortedValuesAttributesList.clear();
                sortedValuesTableViewer.refresh();
            }
            else
            {
                // Default Search Base
                String defaultSearchBase = frontendConfig.getOlcDefaultSearchBase();

                if ( Strings.isEmpty( defaultSearchBase ) )
                {
                    defaultSearchBaseEntryWidget.setInput( null );
                }
                else
                {
                    try
                    {
                        defaultSearchBaseEntryWidget.setInput( new Dn( defaultSearchBase ) );
                    }
                    catch ( LdapInvalidDnException e )
                    {
                        // Nothing to do.
                    }
                }

                // Password Hash
                List<String> passwordHashList = frontendConfig.getOlcPasswordHash();

                if ( ( passwordHashList != null ) && !passwordHashList.isEmpty() )
                {
                    shaCheckbox.setSelection( passwordHashList.contains( SHA ) );
                    sshaCheckbox.setSelection( passwordHashList.contains( SSHA ) );
                    cryptCheckbox.setSelection( passwordHashList.contains( CRYPT ) );
                    md5Checkbox.setSelection( passwordHashList.contains( MD5 ) );
                    smd5Checkbox.setSelection( passwordHashList.contains( SMD5 ) );
                    cleartextCheckbox.setSelection( passwordHashList.contains( CLEARTEXT ) );
                }

                // Sorted Values Attributes
                sortedValuesAttributesList.clear();

                List<String> sortVals = frontendConfig.getOlcSortVals();

                for ( String attribute : sortVals )
                {
                    sortedValuesAttributesList.add( attribute );
                }
                sortedValuesTableViewer.refresh();
            }
        }

        addListeners();
    }


    // ── Watch for Gate-Setting Changes ────────────────────────────────────────
    // The gate-keeper attaches change-detection to all three stations:
    // the search-base picker, the sorted-values table buttons, and all
    // six hash checkboxes. Any modification marks the editor as dirty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches dirty listeners to all editable widgets in this block.
     * After this call, any user interaction with the default search base,
     * hash checkboxes, or sorted-values list will mark the editor dirty.
     */
    private void addListeners()
    {
        defaultSearchBaseEntryWidget.addWidgetModifyListener( dirtyWidgetModifyListener );

        sortedValuesTableViewer.addSelectionChangedListener( sortedValuesTableViewerSelectionChangedListener );
        addSortedValueButton.addSelectionListener( addSortedValueButtonSelectionListener );
        deleteSortedValueButton.addSelectionListener( deleteSortedValueButtonSelectionListener );

        shaCheckbox.addSelectionListener( dirtySelectionListener );
        sshaCheckbox.addSelectionListener( dirtySelectionListener );
        cryptCheckbox.addSelectionListener( dirtySelectionListener );
        md5Checkbox.addSelectionListener( dirtySelectionListener );
        smd5Checkbox.addSelectionListener( dirtySelectionListener );
        cleartextCheckbox.addSelectionListener( dirtySelectionListener );
    }


    // ── Stop Watching for Gate-Setting Changes ────────────────────────────────
    // Before we update fields from the model, the gate-keeper stands down the
    // change detectors so we don't generate spurious dirty signals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches dirty listeners from all editable widgets.
     * Called before populating widgets from the model to avoid false dirty signals.
     */
    private void removeListeners()
    {
        defaultSearchBaseEntryWidget.removeWidgetModifyListener( dirtyWidgetModifyListener );

        sortedValuesTableViewer.removeSelectionChangedListener( sortedValuesTableViewerSelectionChangedListener );
        addSortedValueButton.removeSelectionListener( addSortedValueButtonSelectionListener );
        deleteSortedValueButton.removeSelectionListener( deleteSortedValueButtonSelectionListener );

        shaCheckbox.removeSelectionListener( dirtySelectionListener );
        sshaCheckbox.removeSelectionListener( dirtySelectionListener );
        cryptCheckbox.removeSelectionListener( dirtySelectionListener );
        md5Checkbox.removeSelectionListener( dirtySelectionListener );
        smd5Checkbox.removeSelectionListener( dirtySelectionListener );
        cleartextCheckbox.removeSelectionListener( dirtySelectionListener );
    }


    // ── Look Up the Auxiliary Frontend Record ─────────────────────────────────
    // The gate-keeper searches the database's auxiliary object class list
    // for the OlcFrontendConfig auxiliary. It's there if the server has
    // ever had frontend settings configured.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link OlcFrontendConfig} auxiliary object from the database, or null.
     * The frontend settings are stored as an auxiliary object class on the main
     * database config entry — this method finds it in the auxiliary list.
     *
     * @return the {@link OlcFrontendConfig} if present, or {@code null} if not yet set
     */
    private OlcFrontendConfig getFrontendConfig()
    {
        if ( database != null )
        {
            List<AuxiliaryObjectClass> auxiliaryObjectClassesList = database.getAuxiliaryObjectClasses();

            if ( ( auxiliaryObjectClassesList != null ) && !auxiliaryObjectClassesList.isEmpty() )
            {
                for ( AuxiliaryObjectClass auxiliaryObjectClass : auxiliaryObjectClassesList )
                {
                    if ( auxiliaryObjectClass instanceof OlcFrontendConfig )
                    {
                        return ( OlcFrontendConfig ) auxiliaryObjectClass;
                    }
                }
            }
        }

        return null;
    }


    // ── Commit All Gate Settings to the Record ────────────────────────────────
    // The gate-keeper reads every control station and stamps the results into
    // the official record. If no frontend config object exists yet, we create one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pushes all UI field values back into the frontend config model.
     * Writes the default search base, password hash selections, and sorted-values
     * list into the {@link OlcFrontendConfig} auxiliary. Creates the auxiliary if
     * it doesn't yet exist.
     *
     * @param onSave  {@code true} for a full save; {@code false} for page-change commits
     */
    public void commit( boolean onSave )
    {
        OlcFrontendConfig frontendConfig = getOrCreateFrontendConfig();

        if ( frontendConfig != null )
        {
            // Default Search Base
            Dn defaultSearchBase = defaultSearchBaseEntryWidget.getDn();

            if ( defaultSearchBase == null )
            {
                frontendConfig.setOlcDefaultSearchBase( null );
            }
            else
            {
                frontendConfig.setOlcDefaultSearchBase( defaultSearchBase.toString() );
            }

            // Password Hash
            frontendConfig.clearOlcPasswordHash();

            if ( shaCheckbox.getSelection() )
            {
                frontendConfig.addOlcPasswordHash( SHA );
            }
            if ( sshaCheckbox.getSelection() )
            {
                frontendConfig.addOlcPasswordHash( SSHA );
            }
            if ( cryptCheckbox.getSelection() )
            {
                frontendConfig.addOlcPasswordHash( CRYPT );
            }
            if ( md5Checkbox.getSelection() )
            {
                frontendConfig.addOlcPasswordHash( MD5 );
            }
            if ( smd5Checkbox.getSelection() )
            {
                frontendConfig.addOlcPasswordHash( SMD5 );
            }
            if ( cleartextCheckbox.getSelection() )
            {
                frontendConfig.addOlcPasswordHash( CLEARTEXT );
            }

            // Sorted Values Attributes
            frontendConfig.clearOlcSortVals();
            for ( String attribute : sortedValuesAttributesList )
            {
                frontendConfig.addOlcSortVals( attribute );
            }
        }
    }


    // ── Get or Create the Frontend Auxiliary Record ───────────────────────────
    // If the official record already has a frontend section, use it;
    // if not, create a fresh one and attach it so future commits have somewhere
    // to write to.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the existing {@link OlcFrontendConfig} auxiliary, or creates and attaches one.
     * We need this because the frontend config may not exist on the database entry yet —
     * for example, on a fresh configuration that has never had frontend settings set.
     *
     * @return the {@link OlcFrontendConfig} auxiliary, newly created if needed
     */
    private OlcFrontendConfig getOrCreateFrontendConfig()
    {
        OlcFrontendConfig frontendConfig = getFrontendConfig();

        if ( ( frontendConfig == null ) && ( database != null ) )
        {
            frontendConfig = new OlcFrontendConfig();
            database.addAuxiliaryObjectClasses( frontendConfig );
        }

        return frontendConfig;
    }
}
