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


// ── CLASS: ExportSchemasAsXmlWizardPage — Leia Programs R2's Navigation ──────
// Crouched in the Tantive IV's corridor, Leia configures R2-D2's mission:
// which plans to carry, and whether to scatter them across multiple pods or
// seal them all into one. This page is that configuration moment — the user
// picks schemas and a destination before we commit to the export.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The single wizard page inside {@link ExportSchemasAsXmlWizard}.
 * It presents a checkbox table of available schemas and lets the user choose
 * whether to write each schema to its own XML file or combine them all into
 * one big XML file.
 * Think of this page as Leia's control panel: she selects which Death Star
 * blueprints to copy and specifies exactly where R2 should deliver them.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasAsXmlWizardPage extends AbstractWizardPage
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


    // ── Leia Activates R2's Memory Banks ──────────────────────────────────────
    // Leia flips open R2-D2's data port panel and initializes the unit with
    // the mission parameters — title, description, and the wizard's icon.
    // Our constructor does the same: we tell Eclipse who we are so it can
    // display us correctly inside the wizard container.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of this wizard page with title, description, and icon set.
     * Eclipse's wizard framework needs these right away so it can build the page header
     * before our content widgets even exist.
     * We also grab a reference to the {@link SchemaHandler} here so we can populate
     * the schema list later in {@link #initFields()}.
     *
     * <p>For example — Leia boots R2 for the mission:</p>
     * <pre>
     *   R2-D2 powers on. "Mission: deliver Death Star plans to Obi-Wan."
     *   Title, description, and mission icon are all set at startup.
     *   The SchemaHandler is our manifest of available schematics.
     * </pre>
     */
    protected ExportSchemasAsXmlWizardPage()
    {
        super( "ExportSchemasAsXmlWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ExportSchemasAsXmlWizardPage.ExportSchemaAsXML" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ExportSchemasAsXmlWizardPage.PleaseSelectSchemas" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_EXPORT_WIZARD ) );
        schemaHandler = Activator.getDefault().getSchemaHandler();
    }


    // ── Leia Lays Out The Control Panel ───────────────────────────────────────
    // In the Tantive IV's escape-pod bay, Leia arranges the controls she needs:
    // a list of blueprint modules to select, and a row of dials pointing to the
    // destination — scatter across pods or load everything into one.
    // This method builds all those SWT widgets and wires their listeners, then
    // runs initFields() to populate the schema list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets that make up this wizard page's UI.
     * Eclipse calls this once when the page is about to become visible for
     * the first time.
     * We create a checkbox table of schemas (so the user can pick which ones
     * to export) and two radio-button options: export each schema to its own
     * file, or combine them all into a single file.
     *
     * <p>For example — Leia assembles her mission control panel:</p>
     * <pre>
     *   "Schema modules: [x] inetOrgPerson  [ ] cosine  [x] nis"
     *   "Destination: ( ) separate files  (*) single file: /home/leia/plans.xml"
     *   Every checkbox and text field is a dial on Leia's control panel.
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides as our container
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Schemas Group
        Group schemasGroup = new Group( composite, SWT.NONE );
        schemasGroup.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.Schemas" ) ); //$NON-NLS-1$
        schemasGroup.setLayout( new GridLayout( 2, false ) );
        schemasGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Schemas TableViewer
        Label schemasLabel = new Label( schemasGroup, SWT.NONE );
        schemasLabel.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.SelectSchemasToExport" ) ); //$NON-NLS-1$
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
        schemasTableSelectAllButton.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.SelectAll" ) ); //$NON-NLS-1$
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
        schemasTableDeselectAllButton.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.DeselectAll" ) ); //$NON-NLS-1$
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
        exportDestinationGroup.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.ExportDdestination" ) ); //$NON-NLS-1$
        exportDestinationGroup.setLayout( new GridLayout( 4, false ) );
        exportDestinationGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Export Multiple Files
        exportMultipleFilesRadio = new Button( exportDestinationGroup, SWT.RADIO );
        exportMultipleFilesRadio.setText( Messages
            .getString( "ExportSchemasAsXmlWizardPage.ExportEachSchemaAsSeparateFile" ) ); //$NON-NLS-1$
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
        exportMultipleFilesLabel.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.Directory" ) ); //$NON-NLS-1$
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
        exportMultipleFilesButton.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.Browse" ) ); //$NON-NLS-1$
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
        exportSingleFileRadio.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.ExportSchemaAsSingleFile" ) ); //$NON-NLS-1$
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
        exportSingleFileLabel.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.ExportFile" ) ); //$NON-NLS-1$
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
        exportSingleFileButton.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.Browse" ) ); //$NON-NLS-1$
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


    // ── Leia Pre-loads R2's Schema Manifest ───────────────────────────────────
    // Leia pulls up the list of available blueprint modules on the terminal,
    // checks the ones she has already prioritized, and locks in the default
    // delivery mode: separate pods for each blueprint.
    // We populate the checkbox table from the SchemaHandler, pre-check any
    // schemas passed in via setSelectedSchemas(), and default to multi-file mode.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Seeds the UI with initial values before the page becomes visible.
     * We load all available schemas into the checkbox table (sorted A–Z),
     * tick any that were pre-selected, and flip the destination to
     * "multiple files" mode by default.
     * Also resets the error message and marks the page as not-yet-complete
     * so the Finish button stays disabled until the user fills everything in.
     *
     * <p>For example — Leia reviews the available blueprints:</p>
     * <pre>
     *   The terminal lists: inetOrgPerson, cosine, nis, apache...
     *   Leia checks the ones she's already flagged as priority.
     *   Default mode: one pod per schema. She can change that below.
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


    // ── Leia Chooses The Multi-Pod Broadcast ──────────────────────────────────
    // Leia decides to distribute the plans across multiple escape pods — one
    // blueprint module per pod — so even if Vader intercepts one, the rest
    // slip through. Each schema gets its own XML file.
    // We enable the directory picker and disable the single-file picker.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Switches the UI into "export each schema as a separate file" mode.
     * Enables the directory path field and Browse button, and disables the
     * single-file path field and its Browse button.
     * Called both from the radio-button listener and from {@link #initFields()}
     * to set the initial state.
     *
     * <p>For example — Leia picks the multi-pod strategy:</p>
     * <pre>
     *   "One pod per schema," Leia decides.
     *   The multi-file directory controls light up; the single-file ones go dark.
     *   Each schema will fly out in its own separate XML file.
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


    // ── Leia Chooses The Single-Capsule Transmission ──────────────────────────
    // Leia decides to consolidate all plans into one data capsule loaded into
    // R2 — simpler, and everything arrives together in one XML file.
    // We enable the single-file picker and disable the directory picker.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Switches the UI into "export all schemas as a single file" mode.
     * Disables the directory path field and Browse button, and enables the
     * single-file path field and its Browse button.
     * Called from the single-file radio-button listener.
     *
     * <p>For example — Leia consolidates everything into one capsule:</p>
     * <pre>
     *   "One file to rule them all," Leia decides (wrong franchise, but still).
     *   The single-file controls activate; the directory controls go dark.
     *   All selected schemas will be written into one combined XML file.
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


    // ── Leia Points The Pods At Alderaan ──────────────────────────────────────
    // Leia punches the destination coordinates for her escape pods into the
    // navigation computer — she's choosing the folder where the XML files land.
    // We open a DirectoryDialog so the user can browse to the target folder,
    // defaulting to the last-used location from preferences.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a folder-browser dialog so the user can pick the export directory.
     * If the text field is already filled in, we use that as the dialog's
     * starting path; otherwise we fall back to the preference store's last value.
     * The chosen path is written back into the text field on success.
     *
     * <p>For example — Leia sets the pod coordinates:</p>
     * <pre>
     *   The navigation dialog opens: "Choose your destination sector."
     *   Leia picks /rebel-base/schematics/ and confirms.
     *   The text field updates with the chosen path.
     * </pre>
     */
    private void chooseExportDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ExportSchemasAsXmlWizardPage.SelectFolderToExport" ) ); //$NON-NLS-1$
        if ( "".equals( exportMultipleFilesText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_XML ) );
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


    // ── Leia Loads Everything Into One Pod ────────────────────────────────────
    // Leia slots a single data capsule into R2, specifying the exact file path
    // where the consolidated blueprint archive will land.
    // We open a FileDialog with an *.xml filter so the user can name the file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a file-save dialog so the user can specify the single export file path.
     * We filter for {@code *.xml} files by default.
     * If the text field already has a path, we use its directory as the starting
     * location; otherwise we fall back to the preference store.
     *
     * <p>For example — Leia names her consolidated data capsule:</p>
     * <pre>
     *   The file dialog opens: "Choose output file."
     *   Leia types: /home/leia/schemas/all-plans.xml
     *   R2 will deliver one tidy package to Obi-Wan.
     * </pre>
     */
    private void chooseExportFile()
    {
        FileDialog dialog = new FileDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE );
        dialog.setText( Messages.getString( "ExportSchemasAsXmlWizardPage.SelectFile" ) ); //$NON-NLS-1$
        dialog.setFilterExtensions( new String[]
            { "*.xml", "*" } ); //$NON-NLS-1$ //$NON-NLS-2$
        dialog
            .setFilterNames( new String[]
                {
                    Messages.getString( "ExportSchemasAsXmlWizardPage.XMLFiles" ), Messages.getString( "ExportSchemasAsXmlWizardPage.AllFiles" ) } ); //$NON-NLS-1$ //$NON-NLS-2$
        if ( "".equals( exportSingleFileText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_XML ) );
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


    // ── Leia Runs Pre-launch Systems Check ────────────────────────────────────
    // Before Leia lets the escape pod bay open, she runs through a checklist:
    // is there a schema project? are any blueprints selected? is the destination
    // reachable and writable? Only when everything clears does she authorize launch.
    // This method re-validates the page every time the user changes anything.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current state of the page and updates the error message.
     * Called on every UI change — checkbox toggle, text edit, radio selection.
     * We check: (1) a schema project is open, (2) at least one schema is checked,
     * (3) the chosen destination is valid and writable.
     * Passes {@code null} to {@link #displayErrorMessage} when everything looks good,
     * which clears any previous error and enables the Finish button.
     *
     * <p>For example — Leia's pre-launch checklist:</p>
     * <pre>
     *   "Schema project loaded? Check."
     *   "At least one blueprint selected? Check."
     *   "Destination pod bay clear and writable? Check."
     *   "All systems go. Authorize launch."
     * </pre>
     */
    private void dialogChanged()
    {
        // Checking if a Schema Project is open
        if ( schemaHandler == null )
        {
            displayErrorMessage( Messages.getString( "ExportSchemasAsXmlWizardPage.ErrorNoOpenSchemaProject" ) ); //$NON-NLS-1$
            return;
        }

        // Schemas table
        if ( schemasTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ExportSchemasAsXmlWizardPage.ErrorNoSelectedSchema" ) ); //$NON-NLS-1$
            return;
        }

        // Export option
        if ( exportMultipleFilesRadio.getSelection() )
        {
            String directory = exportMultipleFilesText.getText();
            if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
            {
                displayErrorMessage( Messages.getString( "ExportSchemasAsXmlWizardPage.ErrorNotSelectedDirectory" ) ); //$NON-NLS-1$
                return;
            }
            else
            {
                File directoryFile = new File( directory );
                if ( !directoryFile.exists() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasAsXmlWizardPage.ErrorSelectedDirectoryNotExists" ) ); //$NON-NLS-1$
                    return;
                }
                else if ( !directoryFile.isDirectory() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasAsXmlWizardPage.ErrorSelectedDirectoryNotDirectory" ) ); //$NON-NLS-1$
                    return;
                }
                else if ( !directoryFile.canWrite() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasAsXmlWizardPage.ErrorSelectedDirectoryNotWritable" ) ); //$NON-NLS-1$
                    return;
                }
            }
        }
        else if ( exportSingleFileRadio.getSelection() )
        {
            String exportFile = exportSingleFileText.getText();
            if ( ( exportFile == null ) || ( exportFile.equals( "" ) ) ) //$NON-NLS-1$
            {
                displayErrorMessage( Messages.getString( "ExportSchemasAsXmlWizardPage.ErrorNoFileSelected" ) ); //$NON-NLS-1$
                return;
            }
            else
            {
                File file = new File( exportFile );
                if ( !file.getParentFile().canWrite() )
                {
                    displayErrorMessage( Messages
                        .getString( "ExportSchemasAsXmlWizardPage.ErrorSelectedFileNotWritable" ) ); //$NON-NLS-1$
                    return;
                }
            }
        }

        displayErrorMessage( null );
    }


    // ── Leia Hands Over The Blueprint Manifest ────────────────────────────────
    // After Vader's interrogation droid finishes, Leia holds up the list of
    // blueprints she agreed to hand over — only the ones she checked, nothing
    // more, nothing less.
    // We read the checked elements from the table and return them as a Schema[].
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schemas the user has checked in the table.
     * The wizard's {@link ExportSchemasAsXmlWizard#performFinish()} calls this
     * to know what to export.
     *
     * <p>For example — Leia identifies the selected blueprints:</p>
     * <pre>
     *   "inetOrgPerson: checked. cosine: unchecked. nis: checked."
     *   Only the checked schemas make it into R2's memory bank.
     * </pre>
     *
     * @return  the schemas the user ticked in the checkbox table, as an array
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


    // ── Leia Pre-flags Priority Blueprints ────────────────────────────────────
    // Before the mission briefing even starts, Leia marks the blueprints she
    // already knows are critical — so when the list appears, they're already
    // checked and ready to go.
    // The wizard calls this right after construction to forward the caller's
    // pre-selection into the page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects a set of schemas so they start checked when the page opens.
     * The wizard calls this right after creating the page, forwarding whatever
     * was passed to {@link ExportSchemasAsXmlWizard#setSelectedSchemas(Schema[])}.
     *
     * <p>For example — Leia flags the priority plans in advance:</p>
     * <pre>
     *   Leia marks inetOrgPerson and nis on her tablet before R2 boots up.
     *   When the checklist appears, those two are already ticked.
     * </pre>
     *
     * @param schemas  the schemas to pre-check; stored and applied in {@link #initFields()}
     */
    public void setSelectedSchemas( Schema[] schemas )
    {
        selectedSchemas = schemas;
    }


    // ── Leia Checks The Delivery Mode Setting ─────────────────────────────────
    // Leia glances at the pod-bay controls to confirm whether she set
    // "scatter to multiple pods" or "load everything into one" — she needs
    // this to tell the launch crew which procedure to follow.
    // We return one of our two constants so the wizard knows what to do.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the user chose to export schemas as separate files or one combined file.
     * The wizard reads this in {@link ExportSchemasAsXmlWizard#performFinish()} to decide
     * which branch of the export logic to run.
     *
     * <p>For example — Leia reads the pod-bay mode switch:</p>
     * <pre>
     *   "Multi-pod mode selected — each schema gets its own file."
     *   Returns EXPORT_MULTIPLE_FILES or EXPORT_SINGLE_FILE.
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


    // ── Leia Reads The Destination Sector ─────────────────────────────────────
    // Leia checks the navigation readout to confirm which sector the pods are
    // heading to — the directory where the separate XML files will land.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the directory path the user typed or browsed to for multi-file export.
     * The wizard uses this when {@link #getExportType()} returns {@link #EXPORT_MULTIPLE_FILES}.
     *
     * <p>For example — Leia confirms the pod-bay destination sector:</p>
     * <pre>
     *   "Destination: /rebel-base/schematics/"
     *   One XML file per schema will land in that directory.
     * </pre>
     *
     * @return  the export directory path as a string
     */
    public String getExportDirectory()
    {
        return exportMultipleFilesText.getText();
    }


    // ── Leia Reads The Single-Capsule Destination ─────────────────────────────
    // Leia checks the coordinates for the single consolidated capsule — the
    // exact file path where R2's combined payload will be written.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file path the user specified for single-file export.
     * The wizard uses this when {@link #getExportType()} returns {@link #EXPORT_SINGLE_FILE}.
     *
     * <p>For example — Leia reads R2's capsule coordinates:</p>
     * <pre>
     *   "Capsule destination: /home/leia/all-schemas.xml"
     *   All selected schemas will be serialized into that one file.
     * </pre>
     *
     * @return  the export file path as a string
     */
    public String getExportFile()
    {
        return exportSingleFileText.getText();
    }


    // ── Leia Logs The Coordinates For Next Time ───────────────────────────────
    // After the pods launch, Leia records the destination coordinates in the
    // ship's log so the next mission can start from the same location without
    // re-entering everything from scratch.
    // We persist the chosen directory to the Eclipse preference store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the chosen export path to Eclipse's preference store.
     * This means the next time the user opens this wizard, the directory
     * or file path field starts pre-populated with their last choice.
     * For the single-file option we store the parent directory, not the
     * full file path, so future sessions open in the same folder.
     *
     * <p>For example — Leia logs the sector coordinates:</p>
     * <pre>
     *   Mission complete. Leia writes "/rebel-base/schematics/" into the ship's log.
     *   Next time, the navigation computer will default to that sector.
     * </pre>
     */
    public void saveDialogSettings()
    {
        if ( exportMultipleFilesRadio.getSelection() )
        {
            Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_XML,
                exportMultipleFilesText.getText() );
        }
        else
        {
            Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_EXPORT_SCHEMAS_XML,
                new File( exportSingleFileText.getText() ).getParent() );
        }
    }
}
