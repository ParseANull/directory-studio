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

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgressAdapter;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.controller.SchemaHandler;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileImportException;
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileImporter;
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileImporter.SchemaFileType;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaChecker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ImportSchemasFromXmlWizard — C-3PO Reading The Jawa Data Scroll ──
// C-3PO holds an ancient Jawa data scroll written in XML dialect — a language that
// looks like structured noise to most humans but that 3PO can decode fluently.
// He reads each encoded section, translates it into standard schema objects, and
// hands them off to the team.  A single scroll may contain one schema or many.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard that orchestrates importing schemas stored in Directory Studio's XML
 * format into the currently open schema project.
 * It handles both single-schema XML files and multi-schema bundle files — the
 * {@link XMLSchemaFileImporter} sniffs the file type first and branches accordingly.
 * Think of this class as C-3PO reading a Jawa data scroll: he deciphers the XML
 * dialect, extracts every schema definition encoded within, and delivers them to
 * the schema handler, one by one.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportSchemasFromXmlWizard extends Wizard implements IImportWizard
{
    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The SchemaChecker */
    private SchemaChecker schemaChecker;

    // The pages of the wizard
    private ImportSchemasFromXmlWizardPage page;


    // ── C-3PO Unrolls The Data Scroll ────────────────────────────────────────
    // C-3PO carefully unrolls the data scroll on the table, preparing the reading
    // surface — one page, one job.  The page asks the user which XML files to import
    // before 3PO starts decoding anything.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the single wizard page that collects user input for this XML import.
     * Eclipse calls this before the dialog opens so it can build the page navigation
     * controls and wire the page into the wizard's lifecycle.
     */
    public void addPages()
    {
        // Creating pages
        page = new ImportSchemasFromXmlWizardPage();

        // Adding pages
        addPage( page );
    }


    // ── C-3PO Reads And Translates Each Section ───────────────────────────────
    // C-3PO reads through the scroll section by section: "Ah, this one is a single
    // schema — straightforward."  "This one contains multiple schemas — I'll read
    // each sub-section in turn."  He translates everything into standard form and
    // delivers it to the Rebel data team.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the XML import when the user clicks Finish.
     * For each selected XML file we first ask {@link XMLSchemaFileImporter} whether
     * it is a SINGLE-schema or MULTIPLE-schema file, then call the appropriate
     * parser method.  All resulting {@link Schema} objects are linked to the open
     * project and registered with the schema handler.
     * We silence the schema checker during the import so it doesn't fire incremental
     * validation events for every partial addition.
     *
     * <p>For example — C-3PO translates the Jawa data scroll:</p>
     * <pre>
     *   3PO inspects the first scroll: "Type SINGLE — one schema within."
     *   He translates it, attaches it to the project, and moves on.
     *   Next scroll: "Type MULTIPLE — three schemas encoded here."
     *   He decodes all three and hands each to the schema handler in turn.
     * </pre>
     *
     * @return  {@code true} always — per-file errors are reported via the progress
     *          monitor rather than stopping the whole wizard.
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the schemas to be imported
        final File[] selectedSchemasFiles = page.getSelectedSchemaFiles();
        schemaChecker.disableModificationsListening();

        StudioConnectionRunnableWithProgress runnable = new StudioConnectionRunnableWithProgressAdapter()
        {
            public void run( StudioProgressMonitor monitor )
            {
                monitor.beginTask(
                    Messages.getString( "ImportSchemasFromXmlWizard.ImportingSchemas" ), selectedSchemasFiles.length ); //$NON-NLS-1$

                for ( File schemaFile : selectedSchemasFiles )
                {
                    monitor.subTask( schemaFile.getName() );
                    try
                    {
                        SchemaFileType schemaFileType = XMLSchemaFileImporter.getSchemaFileType( new FileInputStream(
                            schemaFile ), schemaFile.getAbsolutePath() );
                        switch ( schemaFileType )
                        {
                            case SINGLE:
                                Schema importedSchema = XMLSchemaFileImporter.getSchema( new FileInputStream(
                                    schemaFile ), schemaFile.getAbsolutePath() );
                                importedSchema
                                    .setProject( Activator.getDefault().getProjectsHandler().getOpenProject() );
                                schemaHandler.addSchema( importedSchema );
                                break;
                            case MULTIPLE:
                                Schema[] schemas = XMLSchemaFileImporter.getSchemas( new FileInputStream( schemaFile ),
                                    schemaFile.getAbsolutePath() );
                                for ( Schema schema : schemas )
                                {
                                    schema.setProject( Activator.getDefault().getProjectsHandler().getOpenProject() );
                                    schemaHandler.addSchema( schema );
                                }
                                break;
                        }
                    }
                    catch ( XMLSchemaFileImportException e )
                    {
                        reportError( e, schemaFile, monitor );
                    }
                    catch ( FileNotFoundException e )
                    {
                        reportError( e, schemaFile, monitor );
                    }
                    monitor.worked( 1 );
                }

                monitor.done();
                schemaChecker.enableModificationsListening();
            }


            /**
             * Reports the error raised.
             *
             * @param e
             *      the exception
             * @param schemaFile
             *      the schema file
             * @param monitor
             *      the monitor the error is reported to
             *
             */
            private void reportError( Exception e, File schemaFile, StudioProgressMonitor monitor )
            {
                String message = NLS.bind(
                    Messages.getString( "ImportSchemasFromXmlWizard.ErrorImportingSchema" ), schemaFile.getName() ); //$NON-NLS-1$
                monitor.reportError( message, e );
            }


            public String getName()
            {
                return Messages.getString( "ImportSchemasFromXmlWizard.ImportingSchemas" ); //$NON-NLS-1$
            }

        };
        RunnableContextRunner.execute( runnable, getContainer(), true );
        schemaChecker.enableModificationsListening();

        return true;
    }


    // ── C-3PO Prepares His Translation Apparatus ─────────────────────────────
    // Before C-3PO can read the scroll he needs to set up his translation matrix
    // and confirm which language pack is active — here that means wiring up the
    // SchemaHandler (where decoded schemas land) and the SchemaChecker (the grammar
    // validator that ensures what we import is well-formed).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the wizard when Eclipse launches it, before any pages are shown.
     * We fetch the {@link SchemaHandler} and {@link SchemaChecker} from the plugin
     * activator so they're ready for {@link #performFinish()}.
     * We also register that a progress monitor is needed, since XML parsing can be slow
     * for large multi-schema files.
     *
     * @param workbench  the Eclipse workbench — required by {@link IImportWizard} but
     *                   not used directly here.
     * @param selection  the current workbench selection — required by the contract but
     *                   not used; our file list comes from the wizard page.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( true );
        schemaHandler = Activator.getDefault().getSchemaHandler();
        schemaChecker = Activator.getDefault().getSchemaChecker();
    }
}
