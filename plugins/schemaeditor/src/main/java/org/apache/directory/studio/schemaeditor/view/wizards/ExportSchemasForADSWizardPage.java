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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportSchemasForADSWizardPage — Jyn At The Scarif Control Console ──
// Jyn Erso reaches the top of the Scarif communications tower and faces the
// transmission control panel — she must choose which data modules to send and
// whether to scatter them in multiple bursts or consolidate into one signal.
// This wizard page is that control panel: pick schemas, choose a destination,
// and authorize the LDIF transmission to ApacheDS.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The configuration page inside {@link ExportSchemasForADSWizard}.
 * It shows a checkbox list of all available schemas and lets the user choose
 * whether to export each one as its own {@code .ldif} file or bundle everything
 * into a single combined file — matching the structure ApacheDS expects.
 * Think of this as Jyn's transmission console at Scarif: she picks which
 * blueprint modules go out and specifies the exact transmission channel.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasForADSWizardPage extends AbstractWizardPage
{
    /** The selected schemas */
    private Schema[] selectedSchemas = new Schema[0];

    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    public static final int EXPORT_MULTIPLE_FILES = 0;
    public static final int EXPORT_SINGLE_FILE = 1;

    // UI Fields
    private CheckboxTableViewer schemasTableViewer;
    private Button schemasTableSelectAllButton;
    private Button schemasTableDeselectAllButton;
    private Button exportMultipleFilesRadio;
    private Label exportMultipleFilesLabel;
    private Text exportMultipleFilesText;
    private Button exportMultipleFilesButton;
    private Button exportSingleFileRadio;
    private Label exportSingleFileLabel;
    private Text exportSingleFileText;
    private Button exportSingleFileButton;


    // ── Jyn Powers Up The Scarif Console ──────────────────────────────────────
    // Jyn reaches the communications tower control room and flips the master
    // power switch — title, mission description, and the interface icon all
    // light up as the systems come online.
    // Our constructor sets up the page metadata that Eclipse uses to build
    // the wizard header before we render any widgets.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new page instance, setting the title, description, and icon.
     * Eclipse needs these metadata items before it calls {@link #createControl(Composite)},
     * so we supply them in the constructor.
     * We also grab a reference to the {@link SchemaHandler} so we can populate
     * the schema list when the page becomes visible.
     *
     * <p>For example — Jyn activates the Scarif control room:</p>
     * <pre>
     *   The console lights up: "SCARIF TRANSMISSION SYSTEM — ACTIVE"
     *   Title: "Export schemas for ApacheDS." Description: "Select schemas to export."
     *   Mission parameters set; awaiting further instructions.
     * </pre>
     */
    protected ExportSchemasForADSWizardPage()
    {
        super( "ExportSchemasForADSWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ExportSchemasForADSWizardPage.ExportSchemas" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ExportSchemasForADSWizardPage.PleaseSelectSchemasToExport" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor(
            PluginConstants.IMG_SCHEMAS_EXPORT_FOR_ADS_WIZARD ) );
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── Jyn Lays Out The Transmission Controls ────────────────────────────────
    // Jyn surveys the Scarif control panel and arranges the controls she needs:
    // a manifest of available data modules on the left, and the transmission
    // mode selector — scatter across multiple bursts or consolidate into one.
    // We build all those SWT widgets here and wire up their event listeners.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets that make up this wizard page's UI.
     * Eclipse calls this once, just before the page becomes visible.
     * We create a checkbox table listing available schemas (sorted alphabetically)
     * and two radio buttons letting the user choose between multiple LDIF files
     * or one combined file. Listeners are attached to everything so validation
     * fires on every change.
     *
     * <p>For example — Jyn configures the Scarif transmission console:</p>
     * <pre>
     *   Top section: schema manifest with checkboxes. Select the modules to transmit.
     *   Bottom section: "Multiple files" or "Single file" — choose your burst mode.
     *   A directory picker or file picker lights up depending on the choice.
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides as our page container
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Schemas Group
        Group schemasGroup = new Group( composite, SWT.NONE );
        schemasGroup.setText( Messages.getString( "ExportSchemasForADSWizardPage.Schemas" ) ); //$NON-NLS-1$
        schemasGroup.setLayout( new GridLayout( 2, false ) );
        schemasGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Schemas TableViewer
        Label schemasLabel = new Label( schemasGroup, SWT.NONE );
        schemasLabel.setText( Messages.getString( "ExportSchemasForADSWizardPage.SelectSchemaToExport" ) ); //$NON-NLS-1$
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
        schemasTableSelectAllButton.setText( Messages.getString( "ExportSchemasForADSWizardPage.SelectAll" ) ); //$NON-NLS-1$
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
        schemasTableDeselectAllButton.setText( Messages.getString( "ExportSchemasForADSWizardPage.DeselectAll" ) ); //$NON-NLS-1$
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
        exportDestinationGroup.setText( Messages.getString( "ExportSchemasForADSWizardPage.ExportDestination" ) ); //$NON-NLS-1$
        exportDestinationGroup.setLayout( new GridLayout( 4, false ) );
        exportDestinationGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Export Multiple Files
        exportMultipleFilesRadio = new Button( exportDestinationGroup, SWT.RADIO );
        exportMultipleFilesRadio.setText( Messages
            .getString( "ExportSchemasForADSWizardPage.ExportSchemaAsSeparateFiles" ) ); //$NON-NLS-1$
        exportMultipleFilesRadio.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 4, 1 ) );
        exportMultipleFilesRadio.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                exportMultipleFilesSelected();
                dialogChanged();
            }
        } );
        Label exportMultipleFilesFiller = new Label( exportDestinationGroup, SWT.NONE );
        exportMultipleFilesFiller.setText( "    " ); //$NON-NLS-1$
        exportMultipleFilesLabel = new Label( exportDestinationGroup, SWT.NONE );
        exportMultipleFilesLabel.setText( Messages.getString( "ExportSchemasForADSWizardPage.Directory" ) ); //$NON-NLS-1$
        exportMultipleFilesText = new Text( exportDestinationGroup, SWT.BORDER );
        exportMultipleFilesText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        exportMultipleFilesText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        exportMultipleFilesButton = new Button( exportDestinationGroup, SWT.PUSH );
        exportMultipleFilesButton.setText( Messages.getString( "ExportSchemasForADSWizardPage.Browse" ) ); //$NON-NLS-1$
        exportMultipleFilesButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseExportDirectory();
                dialogChanged();
            }
        } );

        // Export Single File
        exportSingleFileRadio = new Button( exportDestinationGroup, SWT.RADIO );
        exportSingleFileRadio.setText( Messages.getString( "ExportSchemasForADSWizardPage.ExportSchemaAsSingleFile" ) ); //$NON-NLS-1$
        exportSingleFileRadio.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 4, 1 ) );
        exportSingleFileRadio.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                exportSingleFileSelected();
                dialogChanged();
            }
        } );
        Label exportSingleFileFiller = new Label( exportDestinationGroup, SWT.NONE );
        exportSingleFileFiller.setText( "    " ); //$NON-NLS-1$
        exportSingleFileLabel = new Label( exportDestinationGroup, SWT.NONE );
        exportSingleFileLabel.setText( Messages.getString( "ExportSchemasForADSWizardPage.ExportFile" ) ); //$NON-NLS-1$
        exportSingleFileText = new Text( exportDestinationGroup, SWT.BORDER );
        exportSingleFileText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        exportSingleFileText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        exportSingleFileButton = new Button( exportDestinationGroup, SWT.PUSH );
        exportSingleFileButton.setText( Messages.getString( "ExportSchemasForADSWizardPage.Browse" ) ); //$NON-NLS-1$
        exportSingleFileButton.addSelectionListener( new SelectionAdapter()
        {
            /**
             * {@inheritDoc}
             */
            public void widgetSelected( SelectionEvent e )
            {
                chooseExportFile();
                dialogChanged();
            }
        } );

        initFields();
        dialogChanged();

        setControl( composite );
    }


    // ── Jyn Loads The Schema Manifest ─────────────────────────────────────────
    // Jyn pulls up the Scarif data vault index on the control screen — a sorted
    // list of every blueprint module available. She pre-checks the ones she was
    // briefed on, and sets the default delivery mode to multi-burst.
    // We sort all schemas alphabetically, populate the checkbox table, and
    // restore any pre-selected schemas from the wizard.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Seeds the UI with initial values before the page becomes visible.
     * We fetch all available schemas from the {@link SchemaHandler}, sort them
     * alphabetically, and load them into the checkbox table.
     * Any schemas pre-selected via {@link #setSelectedSchemas(Schema[])} are
     * ticked automatically. The page defaults to multi-file export mode.
     *
     * <p>For example — Jyn reviews the Scarif vault index:</p>
     * <pre>
     *   "Available modules: apache, cosine, inetOrgPerson, nis..."
     *   Pre-flagged modules appear checked. Multi-burst mode is default.
     *   Error cleared, Finish button locked until validation passes.
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

        // Selecting the Multiple Files choice
        exportMultipleFilesSelected();

        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── Jyn Selects Multi-Burst Transmission Mode ─────────────────────────────
    // Jyn decides to scatter the plans across multiple transmission bursts —
    // one blueprint module per burst — so each arrives as its own LDIF file.
    // We enable the directory controls and disable the single-file controls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Switches the UI to "export each schema as a separate LDIF file" mode.
     * Enables the directory path field and Browse button, and disables the
     * single-file path field and its Browse button.
     * Called from the radio-button listener and from {@link #initFields()} to set
     * the initial default state.
     *
     * <p>For example — Jyn sets the Scarif dish to multi-burst mode:</p>
     * <pre>
     *   "One LDIF file per schema," Jyn decides. "Scatter them across the directory."
     *   The directory controls light up; the single-file controls go dark.
     * </pre>
     */
    private void exportMultipleFilesSelected()
    {
        exportMultipleFilesRadio.setSelection( true );
        exportMultipleFilesLabel.setEnabled( true );
        exportMultipleFilesText.setEnabled( true );
        exportMultipleFilesButton.setEnabled( true );

        exportSingleFileRadio.setSelection( false );
        exportSingleFileLabel.setEnabled( false );
        exportSingleFileText.setEnabled( false );
        exportSingleFileButton.setEnabled( false );
    }


    // ── Jyn Switches To Single-Capsule Transmission Mode ──────────────────────
    // Jyn chooses to consolidate all the blueprint modules into one transmission
    // burst — one combined LDIF file that the rebel fleet receives as a unit.
    // We enable the single-file controls and disable the directory controls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Switches the UI to "export all schemas into one combined LDIF file" mode.
     * Disables the directory path field and Browse button, and enables the
     * single-file path field and its Browse button.
     * Called from the single-file radio-button listener.
     *
     * <p>For example — Jyn consolidates into a single transmission:</p>
     * <pre>
     *   "Combine everything into one burst," Jyn tells Cassian.
     *   The single-file controls activate; the directory controls go dark.
     *   All schemas will land in one consolidated LDIF file.
     * </pre>
     */
    private void exportSingleFileSelected()
    {
        exportMultipleFilesRadio.setSelection( false );
        exportMultipleFilesLabel.setEnabled( false );
        exportMultipleFilesText.setEnabled( false );
        exportMultipleFilesButton.setEnabled( false );

        exportSingleFileRadio.setSelection( true );
        exportSingleFileLabel.setEnabled( true );
        exportSingleFileText.setEnabled( true );
        exportSingleFileButton.setEnabled( true );
    }


    // ── Jyn Dials In The Destination Coordinates ──────────────────────────────
    // Jyn enters the destination sector coordinates into the Scarif dish — the
    // directory where each individual LDIF file will land.
    // We open a DirectoryDialog defaulting to the last-used path from preferences.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a folder-browser dialog so the user can pick the export directory.
     * If the text field already has a path, we use that as the starting location;
     * otherwise we fall back to the preference store's last known directory.
     * The chosen path is written back into the text field on success.
     *
     * <p>For example — Jyn inputs the transmission destination sector:</p>
     * <pre>
     *   The directory dialog opens: "Choose the destination sector."
     *   Jyn selects /apacheds/schemas/ and confirms.
     *   The directory field updates with her choice.
     * </pre>
     */
    private void chooseExportDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ExportSchemasForADSWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ExportSchemasForADSWizardPage.SelectFolderToExportTo" ) ); //$NON-NLS-1$
        if ( "".equals( exportMultipleFilesText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_APACHE_DS ) );
        }
        else
        {
            dialog.setFilterPath( exportMultipleFilesText.getText() );
        }

        String selectedDirectory = dialog.open();
        if ( selectedDirectory != null )
        {
            exportMultipleFilesText.setText( selectedDirectory );
        }
    }


    // ── Jyn Points The Single Beam At Its Target ───────────────────────────────
    // Jyn fine-tunes the dish for a single precision transmission — one file,
    // one exact path, everything bundled together into a {@code .ldif} file.
    // We open a file-save dialog filtered for LDIF files.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a file-save dialog so the user can specify the single combined LDIF file.
     * We filter for {@code *.ldif} files by default but also allow all files.
     * The chosen path is written back into the text field.
     *
     * <p>For example — Jyn targets the single transmission beam:</p>
     * <pre>
     *   "Single burst to: /home/jyn/all-schemas.ldif"
     *   The file dialog opens with *.ldif filter active.
     *   Jyn confirms; the path appears in the text field.
     * </pre>
     */
    private void chooseExportFile()
    {
        FileDialog dialog = new FileDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE );
        dialog.setText( Messages.getString( "ExportSchemasForADSWizardPage.SelectFile" ) ); //$NON-NLS-1$
        dialog.setFilterExtensions( new String[]
            { "*.ldif", "*" } ); //$NON-NLS-1$ //$NON-NLS-2$
        dialog
            .setFilterNames( new String[]
                {
                    Messages.getString( "ExportSchemasForADSWizardPage.LDIFFiles" ), Messages.getString( "ExportSchemasForADSWizardPage.AllFiles" ) } ); //$NON-NLS-1$ //$NON-NLS-2$
        if ( "".equals( exportSingleFileText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_APACHE_DS ) );
        }
        else
        {
            dialog.setFilterPath( exportSingleFileText.getText() );
        }

        String selectedFile = dialog.open();
        if ( selectedFile != null )
        {
            exportSingleFileText.setText( selectedFile );
        }
    }


    // ── Jyn Runs Pre-Transmission Systems Check ────────────────────────────────
    // Before throwing the switch, Jyn runs through her checklist: is the vault
    // accessible? Is there at least one module selected? Is the target sector
    // reachable and writable? Everything must be green before she authorizes.
    // We validate the page state on every UI change and update the error banner.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current page state and updates the error message accordingly.
     * Called on every user interaction — checkbox toggle, text edit, radio click.
     * We check in order: (1) a schema project is open, (2) at least one schema is
     * checked, (3) the chosen destination is valid and writable.
     * Passing {@code null} to {@link #displayErrorMessage} clears any previous
     * error and enables the Finish button.
     *
     * <p>For example — Jyn's Scarif pre-transmission checklist:</p>
     * <pre>
     *   "Vault connection active? Check."
     *   "At least one schema module selected? Check."
     *   "Destination directory exists and is writable? Check."
     *   "All systems green. Transmission authorized."
     * </pre>
     */
    private void dialogChanged()
    {
        // Checking if a Schema Project is open
        if ( schemaHandler == null )
        {
            displayErrorMessage( Messages.getString( "ExportSchemasForADSWizardPage.ErrorNoSchemaProjectOpen" ) ); //$NON-NLS-1$
            return;
        }

        // Schemas table
        if ( schemasTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ExportSchemasForADSWizardPage.ErrorNoSchemaSelected" ) ); //$NON-NLS-1$
            return;
        }

        // Export option
        if ( exportMultipleFilesRadio.getSelection() )
        {
            String directory = exportMultipleFilesText.getText();
            if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
            {
                displayErrorMessage( Messages.getString( "ExportSchemasForADSWizardPage.ErrorNoDirectorySelected" ) ); //$NON-NLS-1$
                return;
            }
            else
            {
                File directoryFile = new File( directory );
                if ( !directoryFile.exists() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasForADSWizardPage.ErrorSelectedDirectoryNotExists" ) ); //$NON-NLS-1$
                    return;
                }
                else if ( !directoryFile.isDirectory() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasForADSWizardPage.ErrorSelectedDirectoryNotDirectory" ) ); //$NON-NLS-1$
                    return;
                }
                else if ( !directoryFile.canWrite() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasForADSWizardPage.ErrorSelectedDirectoryNotWritable" ) ); //$NON-NLS-1$
                    return;
                }
            }
        }
        else if ( exportSingleFileRadio.getSelection() )
        {
            String exportFile = exportSingleFileText.getText();
            if ( ( exportFile == null ) || ( exportFile.equals( "" ) ) ) //$NON-NLS-1$
            {
                displayErrorMessage( Messages.getString( "ExportSchemasForADSWizardPage.ErrorNoFileSelected" ) ); //$NON-NLS-1$
                return;
            }
            else
            {
                File file = new File( exportFile );
                if ( !file.getParentFile().canWrite() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasForADSWizardPage.ErrorSelectedFileNotWritable" ) ); //$NON-NLS-1$
                    return;
                }
            }
        }

        displayErrorMessage( null );
    }


    // ── Jyn Hands Over The Selected Data Modules ──────────────────────────────
    // After the transmission authorization, Cassian asks Jyn which modules actually
    // made it into the burst — the ones she checked, nothing else.
    // We read the checked elements from the table and return them as a Schema array.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schemas the user checked in the table.
     * The wizard calls this in {@link ExportSchemasForADSWizard#performFinish()}
     * to know exactly which schemas to convert and write.
     *
     * <p>For example — Cassian reads off the selected modules:</p>
     * <pre>
     *   "inetOrgPerson: checked. cosine: unchecked. nis: checked."
     *   Only the ticked schemas go into the LDIF export.
     * </pre>
     *
     * @return  the schemas the user has ticked in the checkbox table, as an array
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


    // ── Jyn Pre-flags The Priority Modules ────────────────────────────────────
    // The mission briefing specified which vault modules are critical — Jyn marks
    // them on her manifest before arriving at the control tower so she doesn't
    // have to search the list from scratch.
    // We store these for use when the table is populated in initFields().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects schemas so they start checked when the wizard page opens.
     * The wizard calls this immediately after creating the page, forwarding
     * whatever was passed to {@link ExportSchemasForADSWizard#setSelectedSchemas(Schema[])}.
     *
     * <p>For example — Jyn's pre-mission schema prioritization:</p>
     * <pre>
     *   "These three schemas are flagged as mission-critical."
     *   When the manifest appears, they're already checked.
     * </pre>
     *
     * @param schemas  the schemas to pre-check; applied in {@link #initFields()}
     */
    public void setSelectedSchemas( Schema[] schemas )
    {
        selectedSchemas = schemas;
    }


    // ── Jyn Checks The Transmission Mode Setting ──────────────────────────────
    // Jyn glances at the mode indicator on the Scarif console — is it set to
    // multi-burst or single-beam? The answer tells the wizard which export
    // branch to execute when the user clicks Finish.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user chose multi-file or single-file export mode.
     * The wizard reads this in {@link ExportSchemasForADSWizard#performFinish()}
     * to decide which export branch to run.
     *
     * <p>For example — Jyn reads the Scarif mode indicator:</p>
     * <pre>
     *   Multi-burst mode selected → EXPORT_MULTIPLE_FILES.
     *   Single-beam mode selected → EXPORT_SINGLE_FILE.
     * </pre>
     *
     * @return  {@link #EXPORT_MULTIPLE_FILES} or {@link #EXPORT_SINGLE_FILE}
     */
    public int getExportType()
    {
        if ( exportMultipleFilesRadio.getSelection() )
        {
            return EXPORT_MULTIPLE_FILES;
        }
        else if ( exportSingleFileRadio.getSelection() )
        {
            return EXPORT_SINGLE_FILE;
        }

        // Default
        return EXPORT_MULTIPLE_FILES;
    }


    // ── Jyn Reads The Multi-Burst Destination Sector ──────────────────────────
    // Jyn checks the navigation readout showing the destination directory —
    // where each individual LDIF file will land after the burst goes out.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the directory path for multi-file export.
     * Used by the wizard when {@link #getExportType()} returns {@link #EXPORT_MULTIPLE_FILES}.
     *
     * <p>For example — Jyn confirms the multi-burst destination sector:</p>
     * <pre>
     *   "Destination: /apacheds/schema/"
     *   One LDIF file per schema will land there.
     * </pre>
     *
     * @return  the export directory path as a string
     */
    public String getExportDirectory()
    {
        return exportMultipleFilesText.getText();
    }


    // ── Jyn Reads The Single-Beam Target Path ─────────────────────────────────
    // Jyn reads the exact file path for the consolidated single-burst output —
    // the one LDIF that will contain all selected schemas combined.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file path for single-file export.
     * Used by the wizard when {@link #getExportType()} returns {@link #EXPORT_SINGLE_FILE}.
     *
     * <p>For example — Jyn reads the single-beam target coordinates:</p>
     * <pre>
     *   "Single burst to: /home/jyn/combined-schemas.ldif"
     *   All selected schemas packed into that one file.
     * </pre>
     *
     * @return  the export file path as a string
     */
    public String getExportFile()
    {
        return exportSingleFileText.getText();
    }


    // ── Jyn Logs The Coordinates For The Next Mission ─────────────────────────
    // After the Scarif transmission completes (or the tower explodes — same thing),
    // the coordinates are logged so the next mission can pick up from the same spot.
    // We persist the destination path to Eclipse's preference store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the chosen export path to Eclipse's preference store.
     * Next time the wizard opens, the directory or file path starts pre-filled
     * with the user's last choice.
     * For single-file mode we store the parent directory, not the full file name,
     * so future sessions browse to the same folder.
     *
     * <p>For example — Jyn logs the Scarif coordinates before the dish explodes:</p>
     * <pre>
     *   "Destination logged: /apacheds/schemas/" — persisted to preferences.
     *   Next mission, the control panel opens with that sector pre-selected.
     * </pre>
     */
    public void saveDialogSettings()
    {
        if ( exportMultipleFilesRadio.getSelection() )
        {
            Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_APACHE_DS,
                exportMultipleFilesText.getText() );
        }
        else
        {
            Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_APACHE_DS,
                new File( exportSingleFileText.getText() ).getParent() );
        }
    }
}
