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


// ── CLASS: ImportProjectsWizardPage — R2 Navigating The Death Star Data Vault ─
// R2-D2 is plugged into the Death Star terminal and sees the directory listing —
// row after row of stored data files.  He scans for files matching the right
// format (*.schemaproject, not random Imperial tax records), presents the matching
// ones to Luke's team, and waits for them to select which ones to retrieve.
// This page is R2's data-vault navigation interface.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The file-selection page inside {@link ImportProjectsWizard}.
 * It shows a directory picker so the user can navigate to a folder, then
 * automatically scans that folder for {@code .schemaproject} files and displays
 * them in a checkbox table.  The user ticks the ones they want to import.
 * Think of R2 filtering the Death Star's file listings: only the relevant
 * project files appear in the list — everything else is ignored.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportProjectsWizardPage extends AbstractWizardPage
{
    // UI Fields
    private Text fromDirectoryText;
    private Button fromDirectoryButton;
    private CheckboxTableViewer projectFilesTableViewer;
    private Button projectFilesTableSelectAllButton;
    private Button projectFilesTableDeselectAllButton;


    // ── R2 Powers Up His Navigation Interface ─────────────────────────────────
    // R2-D2 boots up the terminal display: title, description, and the mission
    // icon all appear as his systems come online — ready to start navigating
    // the Death Star's data directories.
    // We set these metadata fields so Eclipse renders the wizard header correctly
    // before any of our content widgets exist.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new page instance with title, description, and icon set.
     * Eclipse needs these before it calls {@link #createControl(Composite)},
     * so we set them up here in the constructor.
     *
     * <p>For example — R2's terminal display powers on:</p>
     * <pre>
     *   Screen header: "Import Schema Projects"
     *   Subtitle: "Select schema project files to import."
     *   The project-import icon appears in the wizard's corner.
     *   R2 beeps cheerfully. Ready for directory navigation.
     * </pre>
     */
    protected ImportProjectsWizardPage()
    {
        super( "ImportProjectsWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ImportProjectsWizardPage.ImportSchemaProjects" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ImportProjectsWizardPage.SelechtSchemaProject" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_IMPORT_WIZARD ) );
    }


    // ── R2 Builds The Data-Vault Navigation Screen ────────────────────────────
    // On R2's Death Star terminal display, two panels appear: at the top, a
    // directory path field with a Browse button — so Luke's team can point R2
    // at the right storage sector — and below it, a list of project files found
    // in that sector, ready to check off and retrieve.
    // We build both those SWT groups and wire all their listeners here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds all the SWT widgets that make up this wizard page.
     * Eclipse calls this exactly once, just before the page becomes visible.
     * We create two sections: a directory-picker at the top, and a checkbox
     * table of project files found in the chosen directory below it.
     * Listeners on the directory field and table automatically trigger
     * validation on every change.
     *
     * <p>For example — R2 sets up his two-panel navigation display:</p>
     * <pre>
     *   [Top panel]    From directory: [/rebel-base/projects/  ] [Browse]
     *   [Bottom panel] Project files found:
     *                    [x] rebel-schema.schemaproject
     *                    [ ] old-backup.schemaproject
     *                  [Select All] [Deselect All]
     * </pre>
     *
     * @param parent  the SWT composite Eclipse provides as our container
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // From Directory Group
        Group fromDirectoryGroup = new Group( composite, SWT.NONE );
        fromDirectoryGroup.setText( Messages.getString( "ImportProjectsWizardPage.FromDirectory" ) ); //$NON-NLS-1$
        fromDirectoryGroup.setLayout( new GridLayout( 3, false ) );
        fromDirectoryGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // From Directory
        Label fromDirectoryLabel = new Label( fromDirectoryGroup, SWT.NONE );
        fromDirectoryLabel.setText( Messages.getString( "ImportProjectsWizardPage.FromDirectoryColon" ) ); //$NON-NLS-1$
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
        fromDirectoryButton.setText( Messages.getString( "ImportProjectsWizardPage.Browse" ) ); //$NON-NLS-1$
        fromDirectoryButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseFromDirectory();
            }
        } );

        // Schema Files Group
        Group schemaFilesGroup = new Group( composite, SWT.NONE );
        schemaFilesGroup.setText( Messages.getString( "ImportProjectsWizardPage.SchemaProjectFiles" ) ); //$NON-NLS-1$
        schemaFilesGroup.setLayout( new GridLayout( 2, false ) );
        schemaFilesGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Schema Files
        projectFilesTableViewer = new CheckboxTableViewer( new Table( schemaFilesGroup, SWT.BORDER | SWT.CHECK
            | SWT.FULL_SELECTION ) );
        GridData schemasTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 2 );
        schemasTableViewerGridData.heightHint = 125;
        projectFilesTableViewer.getTable().setLayoutData( schemasTableViewerGridData );
        projectFilesTableViewer.setContentProvider( new ArrayContentProvider() );
        projectFilesTableViewer.setLabelProvider( new LabelProvider()
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
                    return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_FILE );
                }

                // Default
                return super.getImage( element );
            }
        } );
        projectFilesTableViewer.addCheckStateListener( new ICheckStateListener()
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
        projectFilesTableSelectAllButton = new Button( schemaFilesGroup, SWT.PUSH );
        projectFilesTableSelectAllButton.setText( Messages.getString( "ImportProjectsWizardPage.SelectAll" ) ); //$NON-NLS-1$
        projectFilesTableSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        projectFilesTableSelectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                projectFilesTableViewer.setAllChecked( true );
                dialogChanged();
            }
        } );
        projectFilesTableDeselectAllButton = new Button( schemaFilesGroup, SWT.PUSH );
        projectFilesTableDeselectAllButton.setText( Messages.getString( "ImportProjectsWizardPage.DeselectAll" ) ); //$NON-NLS-1$
        projectFilesTableDeselectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        projectFilesTableDeselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                projectFilesTableViewer.setAllChecked( false );
                dialogChanged();
            }
        } );

        initFields();

        setControl( composite );
    }


    // ── R2 Sets The Starting State Of The Display ─────────────────────────────
    // When R2 first brings up his Death Star terminal display, the file list
    // is empty and the error banner is clear — he's waiting for Luke's team
    // to point him at a directory before he can show anything useful.
    // We clear the error message and mark the page incomplete until the user
    // selects a directory and ticks at least one project file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the initial UI state before the page becomes visible.
     * We clear any error message and mark the page as not-yet-complete, which
     * locks the Finish button until the user picks a directory and selects files.
     *
     * <p>For example — R2's display starts empty:</p>
     * <pre>
     *   "No directory selected yet."
     *   File list: (empty)
     *   Finish button: disabled. Waiting for input.
     * </pre>
     */
    private void initFields()
    {
        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── R2 Navigates To A New Sector ──────────────────────────────────────────
    // Luke's team points R2 at a specific storage sector on the Death Star —
    // R2 navigates there and immediately scans for project files, populating
    // the file list with whatever he finds that matches the right format.
    // We open a DirectoryDialog, get the path, then call fillInSchemaFilesTable().
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens a folder-browser dialog and populates the project files table with
     * any {@code .schemaproject} files found in the chosen directory.
     * If the text field is already filled in, we use that as the starting path;
     * otherwise we default to the preference store's last known directory.
     *
     * <p>For example — R2 navigates to a new Death Star sector:</p>
     * <pre>
     *   The directory dialog opens: "Navigate to sector..."
     *   User picks: /rebel-base/projects/
     *   R2 scans the sector and lists every *.schemaproject file he finds.
     * </pre>
     */
    private void chooseFromDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ImportProjectsWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ImportProjectsWizardPage.SelectFoldertoImportFrom" ) ); //$NON-NLS-1$
        if ( "".equals( fromDirectoryText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_IMPORT_PROJECTS ) );
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


    // ── R2 Scans The Sector For Project Files ─────────────────────────────────
    // R2 sweeps through the specified storage sector looking for files with
    // the {@code .schemaproject} extension — ignoring everything else — and
    // loads the matches into the file list so Luke's team can pick from them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Scans the given directory for {@code .schemaproject} files and loads them
     * into the checkbox table.
     * Files that don't end in {@code .schemaproject} are silently skipped —
     * we only want schema project files, not random directory contents.
     * The table is updated in-place; existing content is replaced.
     *
     * <p>For example — R2 scans the Death Star sector for matching files:</p>
     * <pre>
     *   "Scanning /rebel-base/projects/..."
     *   rebel-schema.schemaproject → added to list.
     *   readme.txt → ignored (wrong format).
     *   old-backup.schemaproject → added to list.
     * </pre>
     *
     * @param path  the directory path to scan; if it doesn't exist we produce an empty list
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
                if ( fileName.endsWith( ".schemaproject" ) ) //$NON-NLS-1$
                {
                    schemaFiles.add( file );
                }
            }
        }

        projectFilesTableViewer.setInput( schemaFiles );
    }


    // ── R2 Runs A Systems Check Before Authorizing Retrieval ──────────────────
    // Before R2 confirms the download, he checks: is there a directory specified?
    // Does it actually exist, is it readable, and has at least one file been selected?
    // Only when all those lights go green does he unlock the retrieve command.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current page state and updates the error message.
     * Called on every user interaction — directory text change, checkbox toggle.
     * We check: (1) a directory path is entered, (2) it exists and is a readable
     * directory, (3) at least one project file is checked.
     * When all checks pass we call {@link #displayErrorMessage(String)} with
     * {@code null}, which clears the error and enables the Finish button.
     *
     * <p>For example — R2's pre-retrieval checklist:</p>
     * <pre>
     *   "Directory specified? Yes."
     *   "Directory exists and is readable? Yes."
     *   "At least one project file selected? Yes."
     *   "All green. Retrieval authorized."
     * </pre>
     */
    private void dialogChanged()
    {
        // Export Directory
        String directory = fromDirectoryText.getText();
        if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "ImportProjectsWizardPage.ErrorNoDirectorySelected" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            File directoryFile = new File( directory );
            if ( !directoryFile.exists() )
            {
                displayErrorMessage( Messages.getString( "ImportProjectsWizardPage.ErrorSelectedDirectoryNotExists" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.isDirectory() )
            {
                displayErrorMessage( Messages.getString( "ImportProjectsWizardPage.ErrorSelectedDirectoryNotDirectory" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.canRead() )
            {
                displayErrorMessage( Messages.getString( "ImportProjectsWizardPage.ErrorSelectedDirectoryNotReadable" ) ); //$NON-NLS-1$
                return;
            }
        }

        // Schemas table
        if ( projectFilesTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ImportProjectsWizardPage.ErrorNoSchemaProjectSelected" ) ); //$NON-NLS-1$
            return;
        }

        displayErrorMessage( null );
    }


    // ── R2 Returns The Files Marked For Retrieval ──────────────────────────────
    // Luke's team told R2 which project files to grab from the Death Star sector.
    // R2 reads back the checked items from his display and hands over the list.
    // We collect the checked File objects from the table and return them as an array.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link File} objects the user has checked in the project files table.
     * The wizard's {@link ImportProjectsWizard#performFinish()} calls this to know
     * which {@code .schemaproject} files to open and parse.
     *
     * <p>For example — R2 reports the selected files to Luke's team:</p>
     * <pre>
     *   "Files marked for retrieval:"
     *   "  rebel-schema.schemaproject"
     *   "  archive-2024.schemaproject"
     *   The wizard will open and parse each of these.
     * </pre>
     *
     * @return  the project files the user ticked in the checkbox table, as a File array
     */
    public File[] getSelectedProjectFiles()
    {
        Object[] selectedProjectFile = projectFilesTableViewer.getCheckedElements();

        List<File> schemaFiles = new ArrayList<File>();
        for ( Object projectFile : selectedProjectFile )
        {
            schemaFiles.add( ( File ) projectFile );
        }

        return schemaFiles.toArray( new File[0] );
    }


    // ── R2 Logs The Sector Coordinates For Next Time ──────────────────────────
    // After completing the retrieval, R2 saves the sector path in his nav memory
    // so the next import mission can start from the same location without
    // the team having to remember and re-enter it manually.
    // We write the chosen directory to Eclipse's preference store.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the chosen directory path to Eclipse's preference store.
     * The next time this wizard opens, the directory text field will start
     * pre-filled with the user's last-used path.
     *
     * <p>For example — R2 saves the sector coordinates to nav memory:</p>
     * <pre>
     *   "Sector /rebel-base/projects/ logged to nav memory."
     *   Next time: the directory field opens pre-filled with that path.
     *   No need to re-enter it from scratch on every import session.
     * </pre>
     */
    public void saveDialogSettings()
    {
        Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_IMPORT_PROJECTS,
            fromDirectoryText.getText() );
    }
}
