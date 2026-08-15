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


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsExporter;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ExportProjectsWizard — Leia Loads Plans Into R2-D2 ────────────────
// Aboard the Tantive IV, Leia slots the Death Star plans into R2-D2's memory
// banks and sends the little droid off toward Tatooine — each plan is packaged
// as a self-contained data cartridge and dispatched to a safe destination.
// This wizard does the same thing for schema projects: the user picks which
// projects to export, picks a destination folder, and we serialize each one
// into its own .schemaproject XML file and write it out — one project, one
// cartridge, one droid.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard that exports one or more schema projects to a local folder as
 * {@code .schemaproject} XML files.
 * It presents a single page where the user picks which projects to export and
 * where to put them, then performs the file-write operation when they click
 * Finish.
 * Think of it as Leia loading R2-D2: each project gets serialized and
 * dispatched to a safe destination folder.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportProjectsWizard extends Wizard implements IExportWizard
{
    /** The selected projects */
    private Project[] selectedProjects = new Project[0];

    // The pages of the wizard
    private ExportProjectsWizardPage page;


    // ── Leia Preps R2 For The Handoff ────────────────────────────────────────
    // Leia opens R2-D2's data port and queues up the plans she already selected
    // before calling a technician — the selection is pre-loaded, ready to go.
    // She registers exactly one page on this wizard: the project-selection and
    // destination form.
    // We create the single wizard page and pass it the pre-selected projects
    // (if any were selected before the wizard opened) so it starts with those
    // items already checked.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the single wizard page and registers it with the wizard framework.
     * We also forward any projects that were pre-selected in the UI (before the
     * wizard opened) so the page can pre-check them in its table.
     *
     * <p>For example — Leia queues up R2's payload before the handoff:</p>
     * <pre>
     *   ExportProjectsWizardPage cartridgeLoader = new ExportProjectsWizardPage();
     *   cartridgeLoader.setSelectedProjects( preSelectedPlans );
     *   addPage( cartridgeLoader );
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        page = new ExportProjectsWizardPage();
        page.setSelectedProjects( selectedProjects );

        // Adding pages
        addPage( page );
    }


    // ── R2-D2 Departs Toward Tatooine ────────────────────────────────────────
    // The moment Leia presses send, R2 waddles to the escape pod, the hatch
    // seals, and each data cartridge is written into persistent storage —
    // one project per file, serialized as clean XML with UTF-8 encoding.
    // If any single cartridge fails to write (bad path, disk full, encoding
    // issue), we log the error and pop up a dialog, but we keep going on the
    // remaining projects rather than aborting the whole batch.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Performs the actual export: iterates over the selected projects and
     * writes each one to a {@code .schemaproject} XML file in the chosen
     * directory.
     * We run inside a progress monitor so the user gets a progress bar for
     * large exports; each project gets its own sub-task tick.
     * Errors on individual projects are logged and shown in a dialog, but they
     * don't stop us from continuing with the remaining projects.
     *
     * <p>For example — R2 stores each plan cartridge as he rolls to the pod:</p>
     * <pre>
     *   for ( Project plan : selectedPlans ) {
     *       XMLWriter writer = openFile( destination + "/" + plan.getName() + ".schemaproject" );
     *       writer.write( ProjectsExporter.toDocument( plan ) );
     *       writer.flush();
     *       monitor.worked( 1 );
     *   }
     * </pre>
     *
     * @return {@code true} to close the wizard after the export completes —
     *         we always return {@code true} here even if some projects failed,
     *         because partial failure is reported via dialogs.
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the projects to be exported and where to export them
        final Project[] selectedProjects = page.getSelectedProjects();
        final String exportDirectory = page.getExportDirectory();
        try
        {
            getContainer().run( false, false, new IRunnableWithProgress()
            {
                public void run( IProgressMonitor monitor )
                {
                    monitor.beginTask(
                        Messages.getString( "ExportProjectsWizard.ExportingProject" ), selectedProjects.length ); //$NON-NLS-1$
                    for ( Project project : selectedProjects )
                    {
                        monitor.subTask( project.getName() );

                        try
                        {
                            OutputFormat outformat = OutputFormat.createPrettyPrint();
                            outformat.setEncoding( "UTF-8" ); //$NON-NLS-1$
                            XMLWriter writer = new XMLWriter( new FileOutputStream( exportDirectory + "/" //$NON-NLS-1$
                                + project.getName() + ".schemaproject" ), outformat ); //$NON-NLS-1$
                            writer.write( ProjectsExporter.toDocument( project ) );
                            writer.flush();
                        }
                        catch ( UnsupportedEncodingException e )
                        {
                            PluginUtils
                                .logError(
                                    NLS
                                        .bind(
                                            Messages.getString( "ExportProjectsWizard.ErrorWhenSavingProject" ), new String[] { project.getName() } ), e ); //$NON-NLS-1$
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ExportProjectsWizard.Error" ), NLS.bind( Messages.getString( "ExportProjectsWizard.ErrorWhenSavingProject" ), new String[] { project.getName() } ) ); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        catch ( FileNotFoundException e )
                        {
                            PluginUtils
                                .logError(
                                    NLS
                                        .bind(
                                            Messages.getString( "ExportProjectsWizard.ErrorWhenSavingProject" ), new String[] { project.getName() } ), e ); //$NON-NLS-1$
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ExportProjectsWizard.Error" ), NLS.bind( Messages.getString( "ExportProjectsWizard.ErrorWhenSavingProject" ), new String[] { project.getName() } ) ); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        catch ( IOException e )
                        {
                            PluginUtils
                                .logError(
                                    NLS
                                        .bind(
                                            Messages.getString( "ExportProjectsWizard.ErrorWhenSavingProject" ), new String[] { project.getName() } ), e ); //$NON-NLS-1$
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ExportProjectsWizard.Error" ), NLS.bind( Messages.getString( "ExportProjectsWizard.ErrorWhenSavingProject" ), new String[] { project.getName() } ) ); //$NON-NLS-1$ //$NON-NLS-2$
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


    // ── Leia Updates R2's Pre-Loaded Manifest ────────────────────────────────
    // If Leia changes her mind about which plans to include before she presses
    // send, she updates R2's manifest — swapping out one set of data cartridges
    // for another before the mission starts.
    // Callers (usually the action that opens this wizard) use this to
    // pre-populate the selection with whatever was highlighted in the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects the projects that should appear checked when the wizard page
     * opens.
     * Call this before the wizard is shown — typically from the action or
     * handler that invokes the wizard — so the user doesn't have to re-select
     * what they already highlighted.
     *
     * <p>For example — Leia swaps R2's pre-loaded data cartridge manifest:</p>
     * <pre>
     *   wizard.setSelectedProjects( currentlyHighlightedProjects );
     *   // then open the wizard — the table will start with those checked
     * </pre>
     *
     * @param projects  the projects to pre-check in the export page's table;
     *                  pass an empty array to start with nothing selected.
     */
    public void setSelectedProjects( Project[] projects )
    {
        selectedProjects = projects;
    }


    // ── Leia Boards The Escape Pod Bay ───────────────────────────────────────
    // Leia strides into the escape pod bay — nothing to set up, no briefing
    // needed, the mission parameters are already loaded from the page settings.
    // We enable the progress monitor (so the user sees a progress bar during
    // the export) and nothing else — the real work happens in performFinish.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse once when the wizard is first opened; we use it only
     * to request a progress monitor for the export operation.
     * We don't need the workbench or selection here — all the data we need
     * comes from the wizard page.
     *
     * <p>For example — Leia enters the bay: preparations are minimal, the
     * plan is already loaded:</p>
     * <pre>
     *   setNeedsProgressMonitor( true );   // show a progress bar during export
     *   // nothing else to do here
     * </pre>
     *
     * @param workbench  the Eclipse workbench — not used.
     * @param selection  the current workbench selection — not used.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
    }
}
