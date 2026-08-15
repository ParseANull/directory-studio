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
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.ProjectType;
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


// ── CLASS: ExportProjectsWizardPage — Mon Mothma's Briefing Room Selection ───
// In the Rebellion briefing room on Yavin 4, Mon Mothma reviews which intel
// packets to include in the mission drop and precisely which channel to beam
// them through — wrong choice means the plans don't reach their destination.
// This wizard page is that briefing-room table: the user checks off which
// schema projects to export, types (or browses to) the destination folder,
// and validation runs continuously so nothing moves until both choices are
// confirmed correct.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The sole page of the Export Projects wizard — lets the user pick which
 * schema projects to export and where to put the resulting files.
 * It hosts a checkbox table of available projects, Select All / Deselect All
 * shortcuts, and a directory picker, and runs live validation so the Finish
 * button only enables when everything is in order.
 * Think of it as Mon Mothma's briefing table: choose which intel leaves the
 * base, and confirm the delivery route before anyone moves.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportProjectsWizardPage extends AbstractWizardPage
{
    /** The selected projects */
    private Project[] selectedProjects = new Project[0];

    // UI Fields
    private CheckboxTableViewer projectsTableViewer;
    private Button projectsTableSelectAllButton;
    private Button projectsTableDeselectAllButton;
    private Label exportDirectoryLabel;
    private Text exportDirectoryText;
    private Button exportDirectoryButton;


    // ── Mon Mothma Prepares The Briefing Dossier ─────────────────────────────
    // Mon Mothma labels the briefing dossier — "Export Schema Projects",
    // accompanied by the Rebellion's export crest — so every officer entering
    // the room knows immediately what this session is about.
    // We set the page title, subtitle description, and wizard header image
    // that Eclipse will display when this page is the active step.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Configures the page identity: title, description, and the header image
     * shown in the wizard while this page is active.
     * We take no constructor arguments because all project data is loaded from
     * the plugin's project handler in {@link #initFields()}.
     *
     * <p>For example — Mon Mothma labels the briefing room dossier:</p>
     * <pre>
     *   page.setTitle( "Export Schema Projects" );
     *   page.setDescription( "Select the projects to export and the destination." );
     *   page.setImageDescriptor( exportWizardBanner );
     * </pre>
     */
    protected ExportProjectsWizardPage()
    {
        super( "ExportProjectsWizardPage" ); //$NON-NLS-1$
        setTitle( Messages.getString( "ExportProjectsWizardPage.ExportSchemaProjects" ) ); //$NON-NLS-1$
        setDescription( Messages.getString( "ExportProjectsWizardPage.PleaseSelectSchemaProjects" ) ); //$NON-NLS-1$
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_PROJECT_EXPORT_WIZARD ) );
    }


    // ── Mon Mothma Arranges The Briefing Room ────────────────────────────────
    // Mon Mothma sets up the briefing room: a holographic table listing all
    // available intel packets (projects), flanked by Select All / Deselect All
    // toggles, and a channel-selector (directory picker) to choose where each
    // packet gets routed.
    // We build the full SWT widget hierarchy — groups, table viewer, buttons,
    // text field — attach all the event listeners, then call initFields() to
    // populate the table and run the first validation pass.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the entire SWT widget tree for this page and wires up all
     * event listeners.
     * Eclipse calls this before the page becomes visible; we create the two
     * groups (projects selection + export destination), populate the projects
     * table, and run an initial validation so the page starts in the right state.
     *
     * <p>For example — Mon Mothma sets up the briefing room layout:</p>
     * <pre>
     *   Group intelTable   = new Group( ... );  // projects to export
     *   Group channelPanel = new Group( ... );  // where to send them
     *   initFields();      // fill the table, pre-check known selections
     *   dialogChanged();   // run first validation pass
     * </pre>
     *
     * @param parent  the parent composite that Eclipse's wizard shell provides —
     *                we attach our own composite to it as a child.
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        GridLayout layout = new GridLayout();
        composite.setLayout( layout );

        // Projects Group
        Group schemaProjectsGroup = new Group( composite, SWT.NONE );
        schemaProjectsGroup.setText( Messages.getString( "ExportProjectsWizardPage.SchemaProjects" ) ); //$NON-NLS-1$
        schemaProjectsGroup.setLayout( new GridLayout( 2, false ) );
        schemaProjectsGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Projects TableViewer
        Label projectsLabel = new Label( schemaProjectsGroup, SWT.NONE );
        projectsLabel.setText( Messages.getString( "ExportProjectsWizardPage.SelectSchemaProjects" ) ); //$NON-NLS-1$
        projectsLabel.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false, 2, 1 ) );
        projectsTableViewer = new CheckboxTableViewer( new Table( schemaProjectsGroup, SWT.BORDER | SWT.CHECK
            | SWT.FULL_SELECTION ) );
        GridData projectsTableViewerGridData = new GridData( SWT.FILL, SWT.NONE, true, false, 1, 2 );
        projectsTableViewerGridData.heightHint = 125;
        projectsTableViewer.getTable().setLayoutData( projectsTableViewerGridData );
        projectsTableViewer.setContentProvider( new ArrayContentProvider() );
        projectsTableViewer.setLabelProvider( new LabelProvider()
        {
            public String getText( Object element )
            {
                if ( element instanceof Project )
                {
                    return ( ( Project ) element ).getName();
                }

                // Default
                return super.getText( element );
            }


            public Image getImage( Object element )
            {
                if ( element instanceof Project )
                {
                    ProjectType type = ( ( Project ) element ).getType();
                    switch ( type )
                    {
                        case OFFLINE:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_OFFLINE_CLOSED );
                        case ONLINE:
                            return Activator.getDefault().getImage( PluginConstants.IMG_PROJECT_ONLINE_CLOSED );
                    }
                }

                // Default
                return super.getImage( element );
            }
        } );
        projectsTableViewer.addCheckStateListener( new ICheckStateListener()
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
        projectsTableSelectAllButton = new Button( schemaProjectsGroup, SWT.PUSH );
        projectsTableSelectAllButton.setText( Messages.getString( "ExportProjectsWizardPage.SelectAll" ) ); //$NON-NLS-1$
        projectsTableSelectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        projectsTableSelectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                projectsTableViewer.setAllChecked( true );
                dialogChanged();
            }
        } );
        projectsTableDeselectAllButton = new Button( schemaProjectsGroup, SWT.PUSH );
        projectsTableDeselectAllButton.setText( Messages.getString( "ExportProjectsWizardPage.DeselectAll" ) ); //$NON-NLS-1$
        projectsTableDeselectAllButton.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, false, false ) );
        projectsTableDeselectAllButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                projectsTableViewer.setAllChecked( false );
                dialogChanged();
            }
        } );

        // Export Destination Group
        Group exportDestinationGroup = new Group( composite, SWT.NULL );
        exportDestinationGroup.setText( Messages.getString( "ExportProjectsWizardPage.ExportDestination" ) ); //$NON-NLS-1$
        exportDestinationGroup.setLayout( new GridLayout( 3, false ) );
        exportDestinationGroup.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        exportDirectoryLabel = new Label( exportDestinationGroup, SWT.NONE );
        exportDirectoryLabel.setText( Messages.getString( "ExportProjectsWizardPage.Directory" ) ); //$NON-NLS-1$
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
        exportDirectoryButton.setText( Messages.getString( "ExportProjectsWizardPage.Browse" ) ); //$NON-NLS-1$
        exportDirectoryButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                chooseExportDirectory();
                dialogChanged();
            }
        } );

        initFields();

        setControl( composite );
    }


    // ── Filling The Briefing Room Intel Board ────────────────────────────────
    // Mon Mothma's aide sorts all available mission dossiers alphabetically
    // and pins them to the briefing board — then highlights the ones that were
    // pre-selected before the briefing started.
    // We load all projects from the plugin's handler, sort them by name, feed
    // them into the table viewer, and check the ones passed in via
    // setSelectedProjects() so the page starts pre-populated.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the projects table with all available projects (sorted
     * alphabetically) and pre-checks any that were passed in via
     * {@link #setSelectedProjects(Project[])}.
     * We also reset validation to start fresh — the page begins in an
     * incomplete state until the user (or a pre-selection) satisfies both
     * the project and directory requirements.
     *
     * <p>For example — Mon Mothma's aide pins all dossiers to the board:</p>
     * <pre>
     *   List allProjects = sortAlphabetically( pluginHandler.getProjects() );
     *   briefingBoard.setInput( allProjects );
     *   briefingBoard.setCheckedElements( preSelectedProjects );
     *   // board is ready — validation starts immediately
     * </pre>
     */
    private void initFields()
    {
        // Filling the Schemas table
        List<Project> projects = new ArrayList<Project>();
        projects.addAll( Activator.getDefault().getProjectsHandler().getProjects() );
        Collections.sort( projects, new Comparator<Project>()
        {
            public int compare( Project o1, Project o2 )
            {
                return o1.getName().compareToIgnoreCase( o2.getName() );
            }

        } );
        projectsTableViewer.setInput( projects );

        // Setting the selected projects
        projectsTableViewer.setCheckedElements( selectedProjects );

        displayErrorMessage( null );
        setPageComplete( false );
    }


    // ── Mon Mothma Opens The Channel Directory ───────────────────────────────
    // Mon Mothma flips open the secure channel directory — a native OS dialog
    // that lets her navigate the filesystem and pick the exact drop-point for
    // the mission packages.
    // We open an SWT DirectoryDialog, seed it with the last-used path from
    // preferences so the user doesn't start at the filesystem root every time,
    // and update the text field with whatever they pick.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens an OS-native directory picker dialog and puts the chosen path into
     * the export directory text field.
     * If the text field already has a path, we use that as the starting
     * location; otherwise we fall back to the last-used export path stored
     * in the plugin's preference store.
     *
     * <p>For example — Mon Mothma consults the secure channel directory:</p>
     * <pre>
     *   DirectoryDialog channelDirectory = new DirectoryDialog( shell );
     *   channelDirectory.setFilterPath( lastUsedDropPoint );
     *   String dropPoint = channelDirectory.open();
     *   if ( dropPoint != null ) exportDirectoryText.setText( dropPoint );
     * </pre>
     */
    private void chooseExportDirectory()
    {
        DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        dialog.setText( Messages.getString( "ExportProjectsWizardPage.ChooseFolder" ) ); //$NON-NLS-1$
        dialog.setMessage( Messages.getString( "ExportProjectsWizardPage.SelectFolderToExportTo" ) ); //$NON-NLS-1$
        if ( "".equals( exportDirectoryText.getText() ) ) //$NON-NLS-1$
        {
            dialog.setFilterPath( Activator.getDefault().getPreferenceStore().getString(
                PluginConstants.FILE_DIALOG_EXPORT_PROJECTS ) );
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


    // ── Mon Mothma Checks The Mission Is Go ──────────────────────────────────
    // Before any intel leaves the base, Mon Mothma runs through her checklist:
    // at least one dossier must be selected, the drop point must be specified,
    // the folder must exist, it must actually be a directory, and it must be
    // writable — only then does she authorise departure.
    // We call displayErrorMessage() for each failed check, which automatically
    // disables the Finish button; clearing the message re-enables it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Validates the current state of the page and updates the error message
     * accordingly — called every time the user changes anything.
     * We check (in order): at least one project is checked, the directory
     * field is not empty, the directory exists, it is actually a directory,
     * and it is writable.
     * The first failing check sets the error and returns; passing all checks
     * clears the error and enables Finish.
     *
     * <p>For example — Mon Mothma's pre-flight checklist:</p>
     * <pre>
     *   if ( noProjectsChecked )        displayErrorMessage( "No projects selected." );
     *   if ( noDirectorySpecified )     displayErrorMessage( "No directory chosen." );
     *   if ( !directory.exists() )      displayErrorMessage( "Directory not found." );
     *   if ( !directory.isDirectory() ) displayErrorMessage( "Not a directory." );
     *   if ( !directory.canWrite() )    displayErrorMessage( "Directory read-only." );
     *   displayErrorMessage( null );    // all clear — authorise departure
     * </pre>
     */
    private void dialogChanged()
    {
        // Schemas table
        if ( projectsTableViewer.getCheckedElements().length == 0 )
        {
            displayErrorMessage( Messages.getString( "ExportProjectsWizardPage.ErrorNoSchemaSelected" ) ); //$NON-NLS-1$
            return;
        }

        // Export Directory
        String directory = exportDirectoryText.getText();
        if ( ( directory == null ) || ( directory.equals( "" ) ) ) //$NON-NLS-1$
        {
            displayErrorMessage( Messages.getString( "ExportProjectsWizardPage.ErrorNoDirectorySelected" ) ); //$NON-NLS-1$
            return;
        }
        else
        {
            File directoryFile = new File( directory );
            if ( !directoryFile.exists() )
            {
                displayErrorMessage( Messages.getString( "ExportProjectsWizardPage.SelectedDirectoryNotExists" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.isDirectory() )
            {
                displayErrorMessage( Messages.getString( "ExportProjectsWizardPage.SelectedDirectoryNotDirectory" ) ); //$NON-NLS-1$
                return;
            }
            else if ( !directoryFile.canWrite() )
            {
                displayErrorMessage( Messages.getString( "ExportProjectsWizardPage.SelectedDirectoryNotWritable" ) ); //$NON-NLS-1$
                return;
            }
        }

        displayErrorMessage( null );
    }


    // ── Mon Mothma Reads Off The Final Manifest ───────────────────────────────
    // At mission go-time, Mon Mothma reads off the final list of dossiers
    // that have been checked and authorised for dispatch — only the confirmed
    // items, typed as Project objects, not raw table selection objects.
    // We collect everything checked in the table viewer and return it as a
    // typed Project array ready for the wizard's performFinish method.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the projects that are currently checked in the table viewer.
     * The wizard calls this in {@code performFinish()} to know exactly which
     * projects to write to disk.
     *
     * <p>For example — Mon Mothma reads the final authorised manifest:</p>
     * <pre>
     *   Object[] checkedItems = briefingBoard.getCheckedElements();
     *   // cast each to Project and collect into the mission manifest
     *   return manifest.toArray( new Project[0] );
     * </pre>
     *
     * @return an array of the checked {@link Project} objects — never
     *         {@code null}, may be empty if somehow called before validation.
     */
    public Project[] getSelectedProjects()
    {
        Object[] selectedProjects = projectsTableViewer.getCheckedElements();

        List<Project> schemas = new ArrayList<Project>();
        for ( Object project : selectedProjects )
        {
            schemas.add( ( Project ) project );
        }

        return schemas.toArray( new Project[0] );
    }


    // ── Mon Mothma Updates The Pre-Authorised Manifest ───────────────────────
    // Before the briefing starts, someone hands Mon Mothma a pre-authorised
    // list — projects already vetted for export — so she can mark them on the
    // board before the officer sits down.
    // The wizard calls this right after constructing the page so our initFields
    // method can pre-check those projects when it runs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the pre-selected projects so they will appear checked when the
     * page is first displayed.
     * Must be called before {@link #createControl(Composite)} runs (i.e.,
     * before the page becomes visible) for the pre-checks to take effect.
     *
     * <p>For example — Mon Mothma receives the pre-authorised manifest:</p>
     * <pre>
     *   page.setSelectedProjects( previouslyApprovedProjects );
     *   // briefing room opens — those projects are already checked
     * </pre>
     *
     * @param projects  the projects to pre-check; pass an empty array to start
     *                  with nothing selected.
     */
    public void setSelectedProjects( Project[] projects )
    {
        selectedProjects = projects;
    }


    // ── Mon Mothma Reads The Drop-Point Coordinates ──────────────────────────
    // When it's time to dispatch the packages, Mon Mothma reads the exact
    // filesystem coordinates from the channel selector field — that's the
    // directory path that gets handed to the file-writer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the filesystem path of the export destination directory as
     * typed (or browsed-to) by the user.
     * The wizard reads this in {@code performFinish()} when building the output
     * file paths.
     *
     * <p>For example — Mon Mothma reads the drop-point from the channel
     * selector:</p>
     * <pre>
     *   String dropPoint = page.getExportDirectory();
     *   // e.g. "/home/rebel/exports"
     *   writer.writeTo( dropPoint + "/" + project.getName() + ".schemaproject" );
     * </pre>
     *
     * @return the export directory path string; may be empty if the user hasn't
     *         typed anything yet (validation prevents Finish in that case).
     */
    public String getExportDirectory()
    {
        return exportDirectoryText.getText();
    }


    // ── Logging The Drop-Point For Next Time ─────────────────────────────────
    // After a successful mission, Mon Mothma's aide logs the drop-point
    // coordinates in the Rebellion's secure records so the next mission can
    // start from the same location without re-entering it.
    // We persist the chosen directory to the plugin's preference store so the
    // directory dialog starts there next time this wizard opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Persists the currently chosen export directory to the plugin's preference
     * store so the directory dialog remembers it the next time this wizard opens.
     * The wizard calls this at the start of {@code performFinish()} before any
     * file I/O begins.
     *
     * <p>For example — the aide logs the drop-point for future missions:</p>
     * <pre>
     *   preferenceStore.putValue( FILE_DIALOG_EXPORT_PROJECTS,
     *       exportDirectoryText.getText() );
     * </pre>
     */
    public void saveDialogSettings()
    {
        Activator.getDefault().getPreferenceStore().putValue( PluginConstants.FILE_DIALOG_EXPORT_PROJECTS,
            exportDirectoryText.getText() );
    }
}
