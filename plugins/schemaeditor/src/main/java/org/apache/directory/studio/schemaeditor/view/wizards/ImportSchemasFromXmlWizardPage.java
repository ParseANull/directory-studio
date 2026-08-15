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


// ── CLASS: ImportSchemasFromXmlWizardPage — C-3PO Surveys The Scroll Archive ─
// C-3PO stands before a shelf of Jawa data scrolls (XML files), reads out the
// titles on each one, and lets the user check off the scrolls they want him to
// translate.  The .xml extension is his filter for "yes, this is a data scroll I
// can read" versus "this is something else entirely."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard page shown by {@link ImportSchemasFromXmlWizard} that lets the user
 * choose a filesystem directory and select which XML schema files to import.
 * It provides a directory picker and a checkbox table of .xml files found there.
 * Think of this page as C-3PO surveying a Jawa archive shelf: he lists the readable
 * scrolls, lets you check the ones you want, and refuses to proceed until at least
 * one is selected and the archive path is valid.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportSchemasFromXmlWizardPage extends AbstractWizardPage
{
    // UI Fields
    private Text fromDirectoryText;
    private Button fromDirectoryButton;
    private CheckboxTableViewer schemaFilesTableViewer;
    private Button schemaFilesTableSelectAllButton;
    private Button schemaFilesTableDeselectAllButton;


    // ── C-3PO Introduces Himself And States His Purpose ───────────────────────
    // "I am C-3PO, human-cyborg relations, and I am here to translate your XML
    // schema files."  Before doing anything, 3PO sets the page title and description
    // so the operator knows exactly what kind of scrolls he handles.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the wizard page and registers its title, description, and banner image.
     * The string we pass to the superclass constructor is the page's unique ID within
     * the wizard — Eclipse uses it internally to manage page navigation.
     */
    protected ImportSchemasFromXmlWizardPage()
    {
        super( "ImportSchemasFromXmlWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ImportSchemasFromXmlWizardPage.ImportSchemasFromXML" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ImportSchemasFromXmlWizardPage.SelectXMLSchemaToImport" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_SCHEMAS_IMPORT_WIZARD ) );
    }


    // ── C-3PO Arranges His Translation Workspace ─────────────────────────────
    // 3PO clears a table, sets up a directory locator (so he knows which shelf to
    // browse), and arranges a checklist display where he can list each readable
    // scroll.  He also places "Select All" and "Deselect All" shortcuts so the
    // operator doesn't have to tick every item individually.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all SWT widgets for the page and wires their event listeners so
     * every user interaction triggers immediate validation.
     * Eclipse calls this once, lazily, just before the page becomes visible.
     * We must call {@code setControl()} at the end or Eclipse will throw.
     *
     * <p>For example — C-3PO lays out his translation workspace:</p>
     * <pre>
     *   Top section: "Which archive shelf?" — text field + Browse button.
     *   Bottom section: Checklist of .xml files on that shelf.
     *   3PO blocks the "proceed" button until at least one scroll is ticked.
     * </pre>
     *
     * @param parent  the parent composite provided by the wizard framework;
     *                we nest our own composite inside it.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // From Directory Group
        Group fromDirectoryGroup = new Group( composite, SWT.NONE );
        fromDirectoryGroup.setText( Messages.getString( "ImportSchemasFromXmlWizardPage.FromDirectory" ) ); //$NON-NLS-1$
        fromDirectoryGroup.setLayout( new GridLayout( 3, false ) );
        fromDirectoryGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // From Directory
        Label fromDirectoryLabel = new Label( fromDirectoryGroup, SWT.NONE );
        fromDirectoryLabel.setText( Messages.getString( "ImportSchemasFromXmlWizardPage.FromDirectoryColon" ) ); //$NON-NLS-1$
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
        fromDirectoryButton.setText( Messages.getString( "ImportSchemasFromXmlWizardPage.Browse" ) ); //$NON-NLS-1$
        fromDirectoryButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseFromDirectory();
            }
        } );

        // Schema Files Group
        Group schemaFilesGroup = new Group( composite, SWT.NONE );
        schemaFilesGroup.setText( Messages.getString( "ImportSchemasFromXmlWizardPage.SchemaFiles" ) ); //$NON-NLS-1$
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
        schemaFilesTableSelectAllButton.setText( Messages.getString( "ImportSchemasFromXmlWizardPage.SelectAll" ) ); //$NON-NLS-1$
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
        schemaFilesTableDeselectAllButton.setText( Messages.getString( "ImportSchemasFromXmlWizardPage.DeselectAll" ) ); //$NON-NLS-1$
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


    // ── C-3PO Resets His Workspace Before Starting ───────────────────────────
    // Before 3PO begins, he clears his notepad and sets his status light to red —
    // he's not ready to translate anything yet; the operator needs to tell him
    // which shelf to look at first.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resets the page to a clean, incomplete state right after the widgets are built.
     * Clears any prior error message and marks the page as not complete, keeping the
     * Finish button disabled until {@link #dialogChanged()} confirms valid input.
     */
    private void initFields()
    {
        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── C-3PO Locates The Scroll Archive Shelf ───────────────────────────────
    // The operator points 3PO to a storage room; he opens the directory browser,
    // navigates to the indicated location, and loads the list of .xml scrolls he
    // finds there onto his checklist display.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the OS-native directory chooser when the user clicks Browse, then
     * populates the file table with .xml files found in the chosen directory.
     * We pre-fill the dialog's starting path from the text field or the saved
     * preference so the user doesn't have to navigate from the root every time.
     */
    private void chooseFromDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ImportSchemasFromXmlWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ImportSchemasFromXmlWizardPage.SelectFolderToImportFrom" ) ); //$NON-NLS-1$
        if ( "".equals( fromDirectoryText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_IMPORT_SCHEMAS_XML ) );
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


    // ── C-3PO Reads The Scroll Titles On The Shelf ───────────────────────────
    // 3PO scans the shelf at the given location, reads the title tag on each
    // container, and lists only the ones ending in ".xml" — the standard wrapping
    // for Jawa-encoded schema data scrolls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Scans the given filesystem directory for .xml files and loads them into the
     * checkbox table viewer so the user can pick which ones to import.
     * Only files ending in {@code .xml} are included — everything else is ignored.
     *
     * <p>For example — C-3PO scans the Jawa archive shelf:</p>
     * <pre>
     *   3PO lists: "schema-bundle.xml" — yes; "readme.txt" — no; "inetOrgPerson.xml" — yes.
     *   The matching scrolls appear as check-boxes on his display.
     * </pre>
     *
     * @param path  the absolute filesystem path to list; we open it as a {@link File}
     *              and iterate its direct children.
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
                if ( fileName.endsWith( ".xml" ) ) //$NON-NLS-1$
                {
                    schemaFiles.add( file );
                }
            }
        }

        schemaFilesTableViewer.setInput( schemaFiles );
    }


    // ── C-3PO Runs His Pre-Translation Checklist ─────────────────────────────
    // Before 3PO starts reading, he runs through his checklist: is there an open
    // project to put the results in? Is the shelf path specified and readable? Has
    // the operator checked at least one scroll?  If anything is off, 3PO displays
    // a polite error message and keeps the proceed button locked.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates all user inputs and updates the page's error message and
     * completion state on every interaction.
     * We check that a schema project is open, the directory path is non-empty and
     * valid, and at least one .xml file is checked.
     */
    private void dialogChanged()
    {
        // Checking if a Schema Project is open
        if ( Activator.getDefault().getSchemaHandler() == null )
        {
            displayErrorMessage( Messages.getString( "ImportSchemasFromXmlWizardPage.ErrorNoSchemaProjectOpen" ) ); //$NON-NLS-1$
            return;
        }

        // Export Directory
        String directory = fromDirectoryText.getText();
        if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "ImportSchemasFromXmlWizardPage.ErrorNoDirectorySelected" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            File directoryFile = new File( directory );
            if ( !directoryFile.exists() )
            {
                displayErrorMessage( Messages
                    .getString( "ImportSchemasFromXmlWizardPage.ErrorSelectedDirectoryNotExists" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.isDirectory() )
            {
                displayErrorMessage( Messages
                    .getString( "ImportSchemasFromXmlWizardPage.ErrorSelectedDirectoryNotDirectory" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.canRead() )
            {
                displayErrorMessage( Messages
                    .getString( "ImportSchemasFromXmlWizardPage.ErrorSelectedDirectoryNotReadable" ) ); //$NON-NLS-1$
                return;
            }
        }

        // Schemas table
        if ( schemaFilesTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ImportSchemasFromXmlWizardPage.ErrorNoSchemaSelected" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── C-3PO Hands Over The Selected Scrolls ────────────────────────────────
    // The operator has ticked the scrolls they want; 3PO gathers them from the
    // shelf and passes the stack to the wizard's translation engine so it can
    // begin decoding each one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the .xml files the user checked in the table as a {@link File} array
     * for the wizard's {@code performFinish()} to process.
     * Casting is safe here because we only ever put {@link File} instances into
     * the viewer's input.
     *
     * @return  a non-null (possibly empty) array of the user-selected {@link File}
     *          objects.
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


    // ── C-3PO Notes The Archive Location For Future Missions ─────────────────
    // After completing the translation job, 3PO records the shelf location in his
    // memory so the next time someone asks him to translate scrolls they start in
    // the right place without hunting through every storage room on the ship.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the directory path the user chose into the plugin's preference store
     * so the next wizard invocation starts from the same location.
     * Called by the wizard just before the import runs, so we only save a path that
     * has passed validation.
     */
    public void saveDialogSettings()
    {
        Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_IMPORT_SCHEMAS_XML,
            fromDirectoryText.getText() );
    }
}
