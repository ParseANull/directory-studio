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
import org.apache.directory.studio.schemaeditor.model.io.OpenLdapSchemaFileImportException;
import org.apache.directory.studio.schemaeditor.model.io.OpenLdapSchemaFileImporter;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaChecker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ImportSchemasFromOpenLdapWizard — R2-D2 Plugs Into The Death Star ─
// R2-D2 rolls up to the Death Star's network port, jacks in, and starts pulling
// schematics out of the Imperial system — format unknown to everyone else, but
// R2 knows exactly how to decode it.  We do the same: plug into an OpenLDAP
// directory's on-disk .schema files and pull the schema definitions into our project.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The multi-step wizard that drives importing schemas stored in OpenLDAP's native
 * .schema file format into the currently open schema project.
 * It lives in the Eclipse import wizard registry and is kicked off whenever the user
 * chooses File → Import → Schema Editor → OpenLDAP Schemas.
 * Think of this class as R2-D2 at the Death Star's network port: it orchestrates the
 * whole retrieval, decoding each .schema file and handing the result off to the
 * {@link SchemaHandler} to register in our project.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImportSchemasFromOpenLdapWizard extends Wizard implements IImportWizard
{
    /** The SchemaHandler */
    private SchemaHandler schemaHandler;

    /** The SchemaChecker */
    private SchemaChecker schemaChecker;

    // The pages of the wizard
    private ImportSchemasFromOpenLdapWizardPage page;


    // ── R2 Extends His Network Cable ─────────────────────────────────────────
    // R2-D2 finds the Death Star's data port, extends his interface cable, and
    // connects — this is the moment the wizard gets its one input page registered.
    // We create the page that lets the user pick a directory and tick the .schema
    // files they want, then hand it to Eclipse's wizard framework.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the single wizard page that collects user input for this import.
     * Eclipse calls this early in the wizard lifecycle so it can build the page
     * navigation controls before the dialog even opens.
     */
    public void addPages()
    {
        // Creating pages
        page = new ImportSchemasFromOpenLdapWizardPage();

        // Adding pages
        addPage( page );
    }


    // ── R2 Transmits The Death Star Schematics ───────────────────────────────
    // R2 has the data — now he streams the Death Star plans back to the Rebel
    // base, file by file, reporting each transfer on his little status display.
    // We do the same: iterate over every .schema file the user selected, parse it
    // with OpenLdapSchemaFileImporter, and add the result to the live schema project.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the actual import when the user clicks Finish.
     * We iterate through each selected .schema file, parse it into a {@link Schema}
     * object, attach it to the open project, and register it with the schema handler.
     * We temporarily silence the schema checker during the bulk load so it doesn't
     * fire validation events for every incremental addition — it re-enables once we're done.
     *
     * <p>For example — R2-D2 transmits the Death Star schematics:</p>
     * <pre>
     *   R2 opens each data segment in sequence.
     *   Each segment is decoded from Imperial format into Rebel-readable plans.
     *   If one segment is corrupted, R2 logs the error and moves on to the next.
     * </pre>
     *
     * @return  {@code true} always — we handle per-file errors via the progress
     *          monitor rather than aborting the whole wizard.
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
                monitor
                    .beginTask(
                        Messages.getString( "ImportSchemasFromOpenLdapWizard.ImportingSchemas" ), selectedSchemasFiles.length ); //$NON-NLS-1$

                for ( File schemaFile : selectedSchemasFiles )
                {
                    monitor.subTask( schemaFile.getName() );
                    try
                    {
                        Schema schema = OpenLdapSchemaFileImporter.getSchema( new FileInputStream( schemaFile ),
                            schemaFile.getAbsolutePath() );
                        schema.setProject( Activator.getDefault().getProjectsHandler().getOpenProject() );
                        schemaHandler.addSchema( schema );
                    }
                    catch ( OpenLdapSchemaFileImportException e )
                    {
                        reportError( e, schemaFile, monitor );
                    }
                    catch ( FileNotFoundException e )
                    {
                        reportError( e, schemaFile, monitor );
                    }
                    monitor.worked( 1 );
                }
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
                    Messages.getString( "ImportSchemasFromOpenLdapWizard.ErrorImportingSchema" ), schemaFile.getName() ); //$NON-NLS-1$
                monitor.reportError( message, e );
            }


            public String getName()
            {
                return Messages.getString( "ImportSchemasFromOpenLdapWizard.ImportingSchemas" ); //$NON-NLS-1$
            }

        };
        RunnableContextRunner.execute( runnable, getContainer(), true );

        schemaChecker.enableModificationsListening();

        return true;
    }


    // ── R2 Boots Up And Identifies The Target System ─────────────────────────
    // Before R2 can plug in, he powers up and scans the environment to learn
    // what kind of system he's connecting to — is it friendly, what protocols
    // does it speak, what will he need?
    // We grab the SchemaHandler and SchemaChecker from the plugin activator so
    // we know where to route the imported schemas once we have them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the wizard when Eclipse launches it, before any pages are shown.
     * We grab references to the {@link SchemaHandler} and {@link SchemaChecker} from
     * the plugin activator so the import logic in {@link #performFinish()} can use them.
     * We also tell Eclipse that this wizard needs a progress monitor, since we may be
     * reading many large files.
     *
     * <p>For example — R2-D2 powers up and scans the Death Star's network topology:</p>
     * <pre>
     *   R2 identifies the data port location.
     *   He logs the system type: Imperial schema, OpenLDAP format.
     *   He readies his transmission buffer before jacking in.
     * </pre>
     *
     * @param workbench  the Eclipse workbench — we don't use it directly but Eclipse
     *                   requires it as part of the {@link IImportWizard} contract.
     * @param selection  the current workbench selection — also part of the contract,
     *                   not used here since we get our input from the wizard page.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( true );
        schemaHandler = Activator.getDefault().getSchemaHandler();
        schemaChecker = Activator.getDefault().getSchemaChecker();
    }
}
