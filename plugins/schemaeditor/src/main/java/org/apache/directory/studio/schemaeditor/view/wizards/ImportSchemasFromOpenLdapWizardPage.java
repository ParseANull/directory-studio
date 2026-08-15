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
import java.util.List;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
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


// ── CLASS: ImportSchemasFromOpenLdapWizardPage — R2 At The Death Star Data Port ─
// R2-D2 has found the data port; now he needs to navigate the Imperial directory
// listing to choose which schematics to pull.  He scans the available files, shows
// Leia which ones exist, and waits for her to check off the ones she wants before he
// starts the actual download.
// ──────────────────────────────────────────────────────────────────────────────────
/**
 * The single wizard page shown by {@link ImportSchemasFromOpenLdapWizard} that lets
 * the user pick a filesystem directory and select which OpenLDAP .schema files to import.
 * It is the UI face of the import flow — all the real parsing work happens in the wizard
 * class once the user clicks Finish.
 * Think of this page as R2-D2's display screen while he's jacked into the Death Star:
 * it shows what's available, lets Leia check off what she needs, and won't let her
 * proceed until at least one item is selected.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportSchemasFromOpenLdapWizardPage extends AbstractWizardPage
{
    // UI Fields
    private Text fromDirectoryText;
    private Button fromDirectoryButton;
    private CheckboxTableViewer schemaFilesTableViewer;
    private Button schemaFilesTableSelectAllButton;
    private Button schemaFilesTableDeselectAllButton;


    // ── R2 Powers Up His Display Screen ──────────────────────────────────────
    // R2-D2 activates his little holoprojector screen to show the status readout
    // — before he can display anything useful, he initialises the display with a
    // title and description so the operator knows what they're looking at.
    // We call the superclass constructor to register our page ID, then set the
    // wizard page title, description, and banner image so the user is oriented.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the wizard page and sets its title, description, and header image.
     * The page ID string we pass to {@link AbstractWizardPage} is how Eclipse
     * identifies this specific page in the wizard's page list — it needs to be unique
     * within the wizard.
     */
    protected ImportSchemasFromOpenLdapWizardPage()
    {
        super( "ImportSchemasFromOpenLdapWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.ImportSchemaFromOpenLDAP" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.SelectOpenLDAPSchema" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT_WIZARD ) );
    }


    // ── R2 Lays Out His Control Panel ────────────────────────────────────────
    // R2 extends his interface panels — one for the connection address (which Imperial
    // directory to look in), and one for the file listing with check-boxes next to
    // each retrievable schematic.
    // We build two SWT groups: a directory picker row at the top, and a checkbox table
    // of .schema files below it, complete with Select All / Deselect All buttons.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets that make up this wizard page and wires their event
     * listeners so user actions immediately trigger validation.
     * Eclipse calls this once, lazily, just before the page becomes visible.
     * The {@code parent} composite is owned by the wizard dialog — we must set our
     * own composite as the page control via {@code setControl()} or Eclipse will complain.
     *
     * <p>For example — R2-D2 extends his control interface inside the Death Star:</p>
     * <pre>
     *   Panel 1: "Which directory am I connecting to?" — text field + Browse button.
     *   Panel 2: Check-list of .schema files found in that directory.
     *   R2 won't let Leia hit OK until at least one file is ticked.
     * </pre>
     *
     * @param parent  the parent composite provided by the Eclipse wizard framework;
     *                we embed our widgets inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // From Directory Group
        Group fromDirectoryGroup = new Group( composite, SWT.NONE );
        fromDirectoryGroup.setText( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.FromDirectory" ) ); //$NON-NLS-1$
        fromDirectoryGroup.setLayout( new GridLayout( 3, false ) );
        fromDirectoryGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // From Directory
        Label fromDirectoryLabel = new Label( fromDirectoryGroup, SWT.NONE );
        fromDirectoryLabel.setText( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.FromDirectoryColon" ) ); //$NON-NLS-1$
        fromDirectoryText = new Text( fromDirectoryGroup, SWT.BORDER );
        fromDirectoryText.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );
        fromDirectoryText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                dialogChanged();
            }
        } );
        fromDirectoryButton = new Button( fromDirectoryGroup, SWT.PUSH );
        fromDirectoryButton.setText( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.Browse" ) ); //$NON-NLS-1$
        fromDirectoryButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseFromDirectory();
            }
        } );

        // Schema Files Group
        Group schemaFilesGroup = new Group( composite, SWT.NONE );
        schemaFilesGroup.setText( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.SchemaFiles" ) ); //$NON-NLS-1$
        schemaFilesGroup.setLayout( new GridLayout( 2, false ) );
        schemaFilesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Schema Files
        schemaFilesTableViewer = new CheckboxTableViewer( new Table( schemaFilesGroup, SWT.BORDER | SWT.CHECK
            | SWT.FULL_SELECTION ) );
        GridData schemasTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 2 );
        schemasTableViewerGridData.heightHint = 125;
        schemaFilesTableViewer.getTable().setLayoutData( schemasTableViewerGridData );
        schemaFilesTableViewer.setContentProvider( new ArrayContentProvider() );
        schemaFilesTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof File )
                {
                    return ( ( File ) element ).getName();
                }

                // Default
                return super.getText( element );
            }


            public Image getImage( Object element )
            {
                if ( element instanceof File )
                {
                    return Activator.getDefault().getImage( PluginConstants.IMG_SCHEMA );
                }

                // Default
                return super.getImage( element );
            }
        } );
        schemaFilesTableViewer.addCheckStateListener( new ICheckStateListener()
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
        schemaFilesTableSelectAllButton = new Button( schemaFilesGroup, SWT.PUSH );
        schemaFilesTableSelectAllButton.setText( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.SelectAll" ) ); //$NON-NLS-1$
        schemaFilesTableSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        schemaFilesTableSelectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                schemaFilesTableViewer.setAllChecked( true );
                dialogChanged();
            }
        } );
        schemaFilesTableDeselectAllButton = new Button( schemaFilesGroup, SWT.PUSH );
        schemaFilesTableDeselectAllButton.setText( Messages
            .getString( "ImportSchemasFromOpenLdapWizardPage.DeselectAll" ) ); //$NON-NLS-1$
        schemaFilesTableDeselectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        schemaFilesTableDeselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                schemaFilesTableViewer.setAllChecked( false );
                dialogChanged();
            }
        } );

        initFields();
        dialogChanged();

        setControl( composite );
    }


    // ── R2 Clears His Screen Before The Mission Starts ───────────────────────
    // Before R2 starts browsing Imperial directories, he wipes his display clean
    // and sets his status indicator to "not ready" — he won't transmit until he
    // has a valid target directory and at least one file selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets the page to a blank, invalid state right after the widgets are built.
     * We clear any stale error message and mark the page as incomplete, which keeps
     * the Finish button disabled until {@link #dialogChanged()} decides everything
     * looks good.
     */
    private void initFields()
    {
        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── R2 Navigates The Imperial Directory Listing ───────────────────────────
    // Leia points to a sector of the Death Star's storage; R2 opens the directory
    // browser, navigates to the selected location, and retrieves the list of
    // available .schema files to display back on his screen.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the OS-native directory chooser dialog when the user clicks Browse,
     * then populates the schema files table with any .schema files found there.
     * We seed the dialog's initial path from either the text field or the saved
     * preference (last used path), so the user doesn't have to navigate from scratch
     * every time.
     */
    private void chooseFromDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.SelectFolderToImportFrom" ) ); //$NON-NLS-1$
        if ( "".equals( fromDirectoryText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_IMPORT_SCHEMAS_OPENLDAP ) );
        }
        else
        {
            dialog.setFilterPath( fromDirectoryText.getText() );
        }

        String selectedDirectory = dialog.open();
        if ( selectedDirectory != null )
        {
            fromDirectoryText.setText( selectedDirectory );
            fillInSchemaFilesTable( selectedDirectory );
        }
    }


    // ── R2 Scans The Directory For Schematics ────────────────────────────────
    // R2 runs a directory listing on the chosen Imperial storage sector, filters
    // out anything that isn't a schematic file (.schema extension), and populates
    // his display with the results so Leia can make her selections.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Scans the given filesystem path for .schema files and loads them into the
     * checkbox table viewer so the user can pick which ones to import.
     * Only files whose name ends in {@code .schema} are included — other files in
     * the directory are silently ignored.
     *
     * <p>For example — R2-D2 scans the Death Star's schema storage sector:</p>
     * <pre>
     *   R2 opens the directory at /imperial/schemas/.
     *   He filters: "inetOrgPerson.schema" — yes; "readme.txt" — no.
     *   The matching files appear in the checklist on his display.
     * </pre>
     *
     * @param path  the absolute filesystem path of the directory to scan; we create a
     *              {@link File} from it and list its direct children.
     */
    private void fillInSchemaFilesTable( String path )
    {
        List<File> schemaFiles = new ArrayList<File>();
        File selectedDirectory = new File( path );
        if ( selectedDirectory.exists() )
        {
            for ( File file : selectedDirectory.listFiles() )
            {
                String fileName = file.getName();
                if ( fileName.endsWith( ".schema" ) ) //$NON-NLS-1$
                {
                    schemaFiles.add( file );
                }
            }
        }

        schemaFilesTableViewer.setInput( schemaFiles );
    }


    // ── R2 Checks Whether The Mission Parameters Are Valid ───────────────────
    // R2 runs a pre-flight check: is the directory real? Can he read it? Has Leia
    // actually ticked at least one schematic?  If anything's wrong, he throws up a
    // warning on his display and keeps his status LED red.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current state of all input fields and updates the error message
     * and page-complete flag accordingly.
     * Called after every user interaction — text edits, checkbox toggles, directory
     * selection — so the Finish button stays in sync with whether the input is valid.
     * We check in order: a schema project must be open, the directory must be specified
     * and actually exist and be readable, and at least one .schema file must be ticked.
     */
    private void dialogChanged()
    {
        // Checking if a Schema Project is open
        if ( Activator.getDefault().getSchemaHandler() == null )
        {
            displayErrorMessage( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.ErrorNotSchemaProjectOpen" ) ); //$NON-NLS-1$
            return;
        }

        // Import Directory
        String directory = fromDirectoryText.getText();
        if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.ErrorNoDirectorySelected" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            File directoryFile = new File( directory );
            if ( !directoryFile.exists() )
            {
                displayErrorMessage( Messages
                    .getString( "ImportSchemasFromOpenLdapWizardPage.ErrorSelectedDirectoryNotExists" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.isDirectory() )
            {
                displayErrorMessage( Messages
                    .getString( "ImportSchemasFromOpenLdapWizardPage.ErrorSelectedDirectoryNotDirectory" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.canRead() )
            {
                displayErrorMessage( Messages
                    .getString( "ImportSchemasFromOpenLdapWizardPage.ErrorSelectedDirectoryNotReadable" ) ); //$NON-NLS-1$
                return;
            }
        }

        // Schemas table
        if ( schemaFilesTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ImportSchemasFromOpenLdapWizardPage.ErrorNoSchemaSelected" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── R2 Hands Over The Checked Schematics ─────────────────────────────────
    // Leia has ticked the files she wants; R2 reads the check-states off his
    // display and packages up the selected file references into an array that he
    // passes back to the Rebellion's data team for processing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the .schema files the user checked in the table as a plain {@link File}
     * array that the wizard's {@code performFinish()} can iterate over.
     * The order matches the order items were added to the viewer, not the order the
     * user checked them.
     *
     * @return  a non-null array of the selected {@link File} objects; may be empty if
     *          somehow called before validation passes (which shouldn't happen in practice).
     */
    public File[] getSelectedSchemaFiles()
    {
        Object[] selectedSchemaFile = schemaFilesTableViewer.getCheckedElements();

        List<File> schemaFiles = new ArrayList<File>();
        for ( Object schemaFile : selectedSchemaFile )
        {
            schemaFiles.add( ( File ) schemaFile );
        }

        return schemaFiles.toArray( new File[0] );
    }


    // ── R2 Logs The Mission Details For Next Time ─────────────────────────────
    // After transmitting the schematics, R2 records the Death Star sector he visited
    // in his memory banks so the next mission can start from the same location
    // without having to navigate all over again.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the chosen directory path into the plugin's preference store so the
     * next time this wizard opens it pre-fills the same location.
     * Called by the wizard's {@code performFinish()} just before the actual import
     * starts, so we only save a path the user has already confirmed is valid.
     */
    public void saveDialogSettings()
    {
        Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_IMPORT_SCHEMAS_OPENLDAP,
            fromDirectoryText.getText() );
    }
}
