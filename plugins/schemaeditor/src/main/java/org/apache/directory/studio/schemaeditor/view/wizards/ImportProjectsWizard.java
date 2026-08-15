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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsImportException;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsImporter;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ImportProjectsWizard — R2-D2 Plugging Into The Death Star Computer ─
// R2-D2 rolls up to a Death Star computer terminal, extends his interface arm,
// and jacks in — downloading stored project data from the external system into
// his own memory banks so it becomes available for the team to use.
// We do the same: read {@code .schemaproject} files from disk and register
// each one with the ProjectsHandler so it appears in the Schema Editor UI.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard that imports schema projects from {@code .schemaproject} files on disk.
 * A schema project is the Schema Editor's unit of work — it bundles schema definitions
 * and configuration together in a single file you can share or back up.
 * Think of R2-D2 at the Death Star terminal: he plugs in, reads the stored data,
 * and makes it available to the Rebel team without altering the source files.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportProjectsWizard extends Wizard implements IImportWizard
{
    // The pages of the wizard
    private ImportProjectsWizardPage page;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;


    // ── R2 Opens The Terminal Interface ───────────────────────────────────────
    // R2-D2 powers up his interface port and brings up the data-retrieval panel —
    // the UI he'll use to select which stored files to pull into his memory.
    // Here we create and register the wizard's one page so the user can pick
    // which project files to import before the real work begins.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers the wizard's file-selection page.
     * Eclipse calls this at wizard startup so our pages are ready before
     * the dialog opens.
     *
     * <p>For example — R2 activates the Death Star data terminal:</p>
     * <pre>
     *   R2's interface arm extends. The selection panel powers up.
     *   "Choose which project files to retrieve," the screen prompts.
     *   The page appears, waiting for the user to browse to a directory.
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        page = new ImportProjectsWizardPage();

        // Adding pages
        addPage( page );
    }


    // ── R2 Downloads The Selected Project Files ────────────────────────────────
    // R2 identifies the project data files the user selected, reads each one
    // from disk, and registers it with the active system — but if a project with
    // the same name is already loaded, R2 skips it and reports the conflict rather
    // than silently overwriting existing work.
    // When the user clicks Finish, we run the actual import inside a progress monitor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the import when the user clicks the Finish button.
     * We iterate the selected {@code .schemaproject} files, parse each one via
     * {@link ProjectsImporter#getProject(java.io.InputStream, String)},
     * check for name conflicts, and add successful ones to the {@link ProjectsHandler}.
     * Name conflicts and parse errors are logged and displayed in dialogs — we
     * still return {@code true} so the wizard closes regardless.
     *
     * <p>For example — R2 retrieves each data file from the terminal:</p>
     * <pre>
     *   "Reading project1.schemaproject... loaded."
     *   "Reading project2.schemaproject... name conflict! Already have 'project2'."
     *   Conflict is reported; R2 skips that one and moves to the next.
     *   Wizard closes cleanly after processing all files.
     * </pre>
     *
     * @return {@code true} always — errors are handled inline; the wizard always closes
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the projects to be imported
        final File[] selectedProjectFiles = page.getSelectedProjectFiles();
        try
        {
            getContainer().run( false, false, new IRunnableWithProgress()
            {
                public void run( IProgressMonitor monitor )
                {
                    monitor.beginTask(
                        Messages.getString( "ImportProjectsWizard.ImportingProjects" ), selectedProjectFiles.length ); //$NON-NLS-1$

                    for ( File projectFile : selectedProjectFiles )
                    {
                        monitor.subTask( projectFile.getName() );
                        try
                        {
                            Project project = ProjectsImporter.getProject( new FileInputStream( projectFile ),
                                projectFile.getAbsolutePath() );

                            if ( projectsHandler.isProjectNameAlreadyTaken( project.getName() ) )
                            {
                                PluginUtils
                                    .logError(
                                        NLS
                                            .bind(
                                                Messages.getString( "ImportProjectsWizard.ErrorImportingProject" ), //$NON-NLS-1$
                                                new String[]
                                                    { project.getName() } ), null );
                                ViewUtils
                                    .displayErrorMessageDialog(
                                        Messages.getString( "ImportProjectsWizard.ImportError" ), //$NON-NLS-1$
                                        NLS
                                            .bind(
                                                Messages.getString( "ImportProjectsWizard.ErrorImportingProject" ) //$NON-NLS-1$
                                                    + "\n" //$NON-NLS-1$
                                                    + Messages
                                                        .getString( "ImportProjectsWizard.ErrorProjectNameExists" ), //$NON-NLS-1$
                                                new String[]
                                                    { project.getName() } ) );
                            }
                            else
                            {
                                projectsHandler.addProject( project );
                            }
                        }
                        catch ( ProjectsImportException e )
                        {
                            PluginUtils
                                .logError(
                                    NLS
                                        .bind(
                                            Messages.getString( "ImportProjectsWizard.ErrorImportingProject" ), new String[] { projectFile.getName() } ), e ); //$NON-NLS-1$
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ImportProjectsWizard.ImportError" ), //$NON-NLS-1$
                                    NLS
                                        .bind(
                                            Messages.getString( "ImportProjectsWizard.ErrorImportingProject" ), new String[] { projectFile.getName() } ) ); //$NON-NLS-1$
                        }
                        catch ( FileNotFoundException e )
                        {
                            PluginUtils
                                .logError(
                                    NLS
                                        .bind(
                                            Messages.getString( "ImportProjectsWizard.ErrorImportingProject" ), new String[] { projectFile.getName() } ), e ); //$NON-NLS-1$
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ImportProjectsWizard.ImportError" ), //$NON-NLS-1$
                                    NLS
                                        .bind(
                                            Messages.getString( "ImportProjectsWizard.ErrorImportingProject" ), new String[] { projectFile.getName() } ) ); //$NON-NLS-1$
                        }
                        monitor.worked( 1 );
                    }

                    monitor.done();
                }
            } );
        }
        catch ( InvocationTargetException e )
        {
            // Nothing to do (it will never occur)
        }
        catch ( InterruptedException e )
        {
            // Nothing to do.
        }

        return true;
    }


    // ── R2 Gets His Mission Context ────────────────────────────────────────────
    // Before R2 can plug into the Death Star terminal, he needs to know two
    // things: the Eclipse workbench context (who's running this show) and where
    // the ProjectsHandler is so he knows where to register what he finds.
    // We enable the progress monitor and grab a reference to the handler.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initializes the wizard with the Eclipse workbench context.
     * Eclipse calls this right after constructing the wizard, before pages appear.
     * We enable the progress monitor (so long imports don't freeze the UI) and
     * grab the {@link ProjectsHandler} reference we'll need in {@link #performFinish()}.
     *
     * <p>For example — R2 prepares his interface arm for the terminal:</p>
     * <pre>
     *   "Workbench context received. ProjectsHandler located."
     *   R2 enables his progress display — blinking lights on.
     *   "Ready to plug in whenever you are."
     * </pre>
     *
     * @param workbench  the Eclipse workbench — we don't use it directly, but the interface requires it
     * @param selection  the current UI selection — not used by this wizard
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( true );

        projectsHandler = Activator.getDefault().getProjectsHandler();
    }
}
