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
package org.apache.directory.studio.schemaeditor.view.wizards;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportSchemasAsOpenLdapWizardPage — Jyn At The Scarif Data Terminal
// Before Jyn can broadcast, she has to stand at the data terminal and choose
// exactly which schematics go into the transmission — wrong file selection
// and the rebel fleet gets useless data.
// This page is that terminal: a checklist of every schema loaded in the open
// project (sorted so they're easy to scan), a Select All / Deselect All pair,
// and a directory picker for the drop-point — with continuous validation to
// make sure nothing moves until the selection and destination are both sound.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The sole page of the Export Schemas as OpenLDAP wizard — lets the user pick
 * which schemas to export and where to write the resulting {@code .schema} files.
 * It validates continuously: you need an open project, at least one schema
 * checked, and a valid writable directory before Finish becomes available.
 * Think of it as Jyn's data terminal on Scarif: select the right schematics,
 * lock in the transmission channel, and only then does the broadcast go live.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasAsOpenLdapWizardPage extends AbstractWizardPage
{
    /** The selected schemas */
    private Schema[] selectedSchemas = new Schema[0];

    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    // UI Fields
    private CheckboxTableViewer schemasTableViewer;
    private Button schemasTableSelectAllButton;
    private Button schemasTableDeselectAllButton;
    private Label exportDirectoryLabel;
    private Text exportDirectoryText;
    private Button exportDirectoryButton;


    // ── Jyn Powers Up The Data Terminal ──────────────────────────────────────
    // Jyn steps up to the Scarif data terminal, types in her authorisation,
    // and the screen lights up: "Export Schemas as OpenLDAP — select the
    // schematics you want to broadcast."
    // We set the page title, description, and wizard header image, then grab
    // the SchemaHandler from the plugin so we can load the schema list later.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Configures the page identity (title, description, wizard header image)
     * and grabs the active {@link SchemaHandler} from the plugin.
     * If no schema project is open, {@code schemaHandler} will be {@code null}
     * and {@link #dialogChanged()} will block Finish with an appropriate error.
     *
     * <p>For example — Jyn logs in at the Scarif data terminal:</p>
     * <pre>
     *   page.setTitle( "Export Schema as OpenLDAP" );
     *   page.setDescription( "Select the schemas to broadcast." );
     *   schemaHandler = plugin.getSchemaHandler();  // null if no project open
     * </pre>
     */
    protected ExportSchemasAsOpenLdapWizardPage()
    {
        super( "ExportSchemasAsOpenLdapWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.ExportSchemaAsOpenLDAP" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.PleaseSelectSchemaExportOpenLDAP" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_EXPORT_WIZARD ) );
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── Jyn Pulls Up The Full Schema Index ───────────────────────────────────
    // Jyn types the access code and the terminal displays the full archive
    // index: every schema in the vault, sorted, with checkboxes next to each,
    // and a slot to enter the broadcast frequency (destination directory).
    // We build the SWT composite, drop in the two groups (schemas + destination),
    // hook up all the listeners, populate the table, and fire the first
    // validation pass so the page starts in a known state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the full SWT widget tree for this page: schemas selection group
     * (table + Select All / Deselect All) and export destination group
     * (label + text field + Browse button).
     * Eclipse calls this once before the page is shown; after construction we
     * immediately call {@link #initFields()} and {@link #dialogChanged()} so
     * the page is in a valid (or correctly errored) state from the start.
     *
     * <p>For example — Jyn pulls up the archive index on the terminal:</p>
     * <pre>
     *   Group schemaIndex    = new Group( ... );  // all schemas in the vault
     *   Group broadcastSlot  = new Group( ... );  // where the signal goes
     *   initFields();      // populate the index with sorted schemas
     *   dialogChanged();   // validate before showing the page
     * </pre>
     *
     * @param parent  the parent composite Eclipse's wizard shell provides —
     *                we attach our composite to it as a child.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Schemas Group
        Group schemasGroup = new Group( composite, SWT.NONE );
        schemasGroup.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.Schemas" ) ); //$NON-NLS-1$
        schemasGroup.setLayout( new GridLayout( 2, false ) );
        schemasGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Schemas TableViewer
        Label schemasLabel = new Label( schemasGroup, SWT.NONE );
        schemasLabel.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.SelectSchemasToExport" ) ); //$NON-NLS-1$
        schemasLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        schemasTableViewer = new CheckboxTableViewer( new Table( schemasGroup, SWT.BORDER | SWT.CHECK
            | SWT.FULL_SELECTION ) );
        GridData schemasTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 2 );
        schemasTableViewerGridData.heightHint = 125;
        schemasTableViewer.getTable().setLayoutData( schemasTableViewerGridData );
        schemasTableViewer.setContentProvider( new ArrayContentProvider() );
        schemasTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof Schema )
                {
                    return ( ( Schema ) element ).getSchemaName();
                }

                // Default
                return super.getText( element );
            }


            public Image getImage( Object element )
            {
                if ( element instanceof Schema )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA );
                }

                // Default
                return super.getImage( element );
            }
        } );
        schemasTableViewer.addCheckStateListener( new ICheckStateListener()
        {
            /**
             * Notifies of a change to the checked state of an element.
             *
             * @param event
             *      event object describing the change
             */
            public void checkStateChanged( CheckStateChangedEvent event )
            {
                dialogChanged();
            }
        } );
        schemasTableSelectAllButton = new Button( schemasGroup, SWT.PUSH );
        schemasTableSelectAllButton.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.SelectAll" ) ); //$NON-NLS-1$
        schemasTableSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        schemasTableSelectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                schemasTableViewer.setAllChecked( true );
                dialogChanged();
            }
        } );
        schemasTableDeselectAllButton = new Button( schemasGroup, SWT.PUSH );
        schemasTableDeselectAllButton.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.DeselectAll" ) ); //$NON-NLS-1$
        schemasTableDeselectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        schemasTableDeselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                schemasTableViewer.setAllChecked( false );
                dialogChanged();
            }
        } );

        // Export Destination Group
        Group exportDestinationGroup = new Group( composite, SWT.NULL );
        exportDestinationGroup.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.ExportDestination" ) ); //$NON-NLS-1$
        exportDestinationGroup.setLayout( new GridLayout( 3, false ) );
        exportDestinationGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        exportDirectoryLabel = new Label( exportDestinationGroup, SWT.NONE );
        exportDirectoryLabel.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.Directory" ) ); //$NON-NLS-1$
        exportDirectoryText = new Text( exportDestinationGroup, SWT.BORDER );
        exportDirectoryText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        exportDirectoryText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        exportDirectoryButton = new Button( exportDestinationGroup, SWT.PUSH );
        exportDirectoryButton.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.Browse" ) ); //$NON-NLS-1$
        exportDirectoryButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseExportDirectory();
                dialogChanged();
            }
        } );

        initFields();
        dialogChanged();

        setControl( composite );
    }


    // ── Jyn Loads The Schema Index From The Vault ─────────────────────────────
    // The terminal asks the vault for a complete list of schematics, sorts them
    // alphabetically so Jyn can scan quickly, and marks the ones she requested
    // before sitting down.
    // If the vault is offline (schemaHandler is null), we skip populating the
    // table — dialogChanged() will catch the missing project and set an error.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the schemas table with all schemas from the active project
     * (sorted alphabetically) and pre-checks any passed in via
     * {@link #setSelectedSchemas(Schema[])}.
     * If no project is open ({@code schemaHandler} is {@code null}), we skip
     * the table population entirely and let {@link #dialogChanged()} show
     * the appropriate error.
     *
     * <p>For example — the terminal loads and sorts the vault index:</p>
     * <pre>
     *   List schemas = sortAlphabetically( schemaHandler.getSchemas() );
     *   schemasTable.setInput( schemas );
     *   schemasTable.setCheckedElements( preSelectedSchemas );
     * </pre>
     */
    private void initFields()
    {
        // Filling the Schemas table
        if ( schemaHandler != null )
        {
            List<Schema> schemas = new ArrayList<Schema>();
            schemas.addAll( schemaHandler.getSchemas() );

            Collections.sort( schemas, new Comparator<Schema>()
            {
                public int compare( Schema o1, Schema o2 )
                {
                    return o1.getSchemaName().compareToIgnoreCase( o2.getSchemaName() );
                }
            } );

            schemasTableViewer.setInput( schemas );

            // Setting the selected schemas
            schemasTableViewer.setCheckedElements( selectedSchemas );
        }

        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── Jyn Dials In The Broadcast Frequency ─────────────────────────────────
    // Jyn taps the "browse frequencies" button and an OS dialog opens showing
    // the available transmission channels (filesystem directories) — she picks
    // one, and the frequency field updates automatically.
    // We seed the dialog with the last-used path from preferences so she
    // doesn't have to navigate from the root every time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens an OS-native directory picker dialog and puts the selected path
     * into the export directory text field.
     * We seed the starting location from the preference store (last-used path)
     * or from the current text field value if the user has already typed one.
     *
     * <p>For example — Jyn selects the broadcast frequency from the channel
     * directory:</p>
     * <pre>
     *   DirectoryDialog freqPicker = new DirectoryDialog( shell );
     *   freqPicker.setFilterPath( lastUsedChannel );
     *   String freq = freqPicker.open();
     *   if ( freq != null ) exportDirectoryText.setText( freq );
     * </pre>
     */
    private void chooseExportDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.SelectFolderToExport" ) ); //$NON-NLS-1$
        if ( "".equals( exportDirectoryText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_OPENLDAP ) );
        }
        else
        {
            dialog.setFilterPath( exportDirectoryText.getText() );
        }

        String selectedDirectory = dialog.open();
        if ( selectedDirectory != null )
        {
            exportDirectoryText.setText( selectedDirectory );
        }
    }


    // ── Jyn Runs The Pre-Transmission Checklist ───────────────────────────────
    // Before firing the dish, the Scarif terminal runs a five-point safety
    // check: Is the vault online? Are any schematics selected? Is a broadcast
    // channel set? Does that channel exist? Is it actually a directory and
    // writable? — any failure and the transmit button stays locked.
    // We call displayErrorMessage() for the first failure we find; passing all
    // five checks clears the error and lets Finish proceed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current page state on every user change and updates the
     * error message / Finish button accordingly.
     * Checks run in order: open project exists, at least one schema is checked,
     * directory field is not empty, directory exists, is a directory, is
     * writable.
     *
     * <p>For example — Jyn's five-point pre-transmission checklist:</p>
     * <pre>
     *   if ( schemaHandler == null )       displayErrorMessage( "No project open." );
     *   if ( nothingChecked )              displayErrorMessage( "No schema selected." );
     *   if ( directoryEmpty )              displayErrorMessage( "No directory set." );
     *   if ( !directory.exists() )         displayErrorMessage( "Directory not found." );
     *   if ( !directory.isDirectory() )    displayErrorMessage( "Not a directory." );
     *   if ( !directory.canWrite() )       displayErrorMessage( "Directory read-only." );
     *   displayErrorMessage( null );       // all clear — transmit authorised
     * </pre>
     */
    private void dialogChanged()
    {
        // Checking if a Schema Project is open
        if ( schemaHandler == null )
        {
            displayErrorMessage( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.ErrorNoOpenSchemaProject" ) ); //$NON-NLS-1$
            return;
        }

        // Schemas table
        if ( schemasTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.ErrorNoSchemaSelected" ) ); //$NON-NLS-1$
            return;
        }

        // Export Directory
        String directory = exportDirectoryText.getText();
        if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "ExportSchemasAsOpenLdapWizardPage.ErrorNotDirectorySelected" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            File directoryFile = new File( directory );
            if ( !directoryFile.exists() )
            {
                displayErrorMessage( Messages
                    .getString( "ExportSchemasAsOpenLdapWizardPage.ErrorSelectedDirectoryNotExists" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.isDirectory() )
            {
                displayErrorMessage( Messages
                    .getString( "ExportSchemasAsOpenLdapWizardPage.ErrorSelectedDirectoryNotDirectory" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.canWrite() )
            {
                displayErrorMessage( Messages
                    .getString( "ExportSchemasAsOpenLdapWizardPage.ErrorSelectedDirectoryNotWritable" ) ); //$NON-NLS-1$
                return;
            }
        }

        displayErrorMessage( null );
    }


    // ── Jyn Reads The Final Transmission Manifest ─────────────────────────────
    // With all checks passed, the terminal displays the confirmed manifest:
    // exactly the schemas Jyn has checked, cast from raw archive objects to
    // typed Schema references ready for the exporter.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schemas that are currently checked in the table viewer.
     * The wizard calls this in {@code performFinish()} to know exactly which
     * schemas to convert and write.
     *
     * <p>For example — the terminal confirms the transmission manifest:</p>
     * <pre>
     *   Object[] checkedItems = schemasTable.getCheckedElements();
     *   // cast each to Schema and collect into the final manifest
     *   return manifest.toArray( new Schema[0] );
     * </pre>
     *
     * @return a typed array of the checked {@link Schema} objects — never
     *         {@code null}, may be empty if somehow called before validation.
     */
    public Schema[] getSelectedSchemas()
    {
        Object[] selectedSchemas = schemasTableViewer.getCheckedElements();

        List<Schema> schemas = new ArrayList<Schema>();
        for ( Object schema : selectedSchemas )
        {
            schemas.add( ( Schema ) schema );
        }

        return schemas.toArray( new Schema[0] );
    }


    // ── Jyn Pre-Loads The Transmission Manifest ───────────────────────────────
    // Before the terminal session starts, someone else has already flagged which
    // schematics need to go out — Jyn just confirms them on the screen when
    // the session opens.
    // The wizard calls this right after constructing the page so initFields()
    // can pre-check those schemas when it runs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the pre-selected schemas so they appear checked when the page
     * is first displayed.
     * Must be called before {@link #createControl(Composite)} runs — i.e.,
     * before the page becomes visible — for the pre-checks to take effect.
     *
     * <p>For example — someone pre-flags the schematics before Jyn's session:</p>
     * <pre>
     *   page.setSelectedSchemas( alreadyHighlightedSchemas );
     *   // terminal opens — those schemas are pre-checked in the index
     * </pre>
     *
     * @param schemas  the schemas to pre-check; pass an empty array to start
     *                 with nothing selected.
     */
    public void setSelectedSchemas( Schema[] schemas )
    {
        selectedSchemas = schemas;
    }


    // ── Jyn Reads The Broadcast Channel Setting ───────────────────────────────
    // The wizard needs the exact broadcast channel (directory path) right before
    // it starts writing files — Jyn reads it straight off the frequency field.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the filesystem path of the export directory as entered by the user.
     * The wizard reads this in {@code performFinish()} when building the output
     * file paths for each schema.
     *
     * <p>For example — Jyn reads the broadcast frequency off the terminal:</p>
     * <pre>
     *   String channel = page.getExportDirectory();
     *   writer.writeTo( channel + "/" + schema.getSchemaName() + ".schema" );
     * </pre>
     *
     * @return the directory path string; may be empty if the user hasn't typed
     *         anything (validation blocks Finish in that case).
     */
    public String getExportDirectory()
    {
        return exportDirectoryText.getText();
    }


    // ── Logging The Frequency For The Next Transmission ──────────────────────
    // After a successful broadcast, the terminal logs the used frequency so the
    // next operator doesn't have to dial in from scratch.
    // We write the chosen directory to the plugin preference store so the
    // directory dialog starts there next time this wizard runs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the currently chosen export directory to the plugin preference
     * store so the directory dialog remembers it on the next run.
     * The wizard calls this at the start of {@code performFinish()} before any
     * file I/O begins.
     *
     * <p>For example — the terminal saves the broadcast frequency for next
     * time:</p>
     * <pre>
     *   preferenceStore.putValue( FILE_DIALOG_EXPORT_SCHEMAS_OPENLDAP,
     *       exportDirectoryText.getText() );
     * </pre>
     */
    public void saveDialogSettings()
    {
        Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_OPENLDAP,
            exportDirectoryText.getText() );
    }
}
