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


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.io.OpenLdapSchemaFileExporter;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ExportSchemasAsOpenLdapWizard — Jyn Broadcasts From Scarif Tower ──
// At the climax of Rogue One, Jyn Erso climbs to the top of the Scarif data
// tower, aligns the satellite dish, and broadcasts the Death Star schematics
// in raw signal format — the one format the rebel fleet can actually receive
// and act on.
// This wizard does the same for LDAP schemas: it converts each selected schema
// into the raw OpenLDAP .schema text format (the format ldap servers actually
// read) and writes it to disk, one file per schema.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The wizard that exports one or more schemas to OpenLDAP {@code .schema}
 * format files.
 * It presents a single page where the user picks which schemas to export and
 * which directory to write them to, then performs the conversion and file-write
 * in {@code performFinish()}.
 * Think of it as Jyn's broadcast from the Scarif tower: we convert and
 * transmit each schema in the format the outside world can actually consume.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasAsOpenLdapWizard extends Wizard implements IExportWizard
{
    /** The selected schemas */
    private Schema[] selectedSchemas = new Schema[0];

    // The pages of the wizard
    private ExportSchemasAsOpenLdapWizardPage page;


    // ── Jyn Preps The Transmission Equipment ─────────────────────────────────
    // Jyn reaches the top of the Scarif tower and sets up the broadcast rig —
    // one page, one job: choose what to transmit and where the signal goes.
    // She queues up the schemas already selected when the wizard was launched
    // so the page starts with those checked by default.
    // We create the single wizard page, forward the pre-selected schemas, and
    // register it with the wizard framework.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates the single export page and registers it with the wizard framework.
     * We pass any pre-selected schemas to the page so they appear checked when
     * the wizard opens.
     *
     * <p>For example — Jyn loads the data cards into the broadcast rig:</p>
     * <pre>
     *   ExportSchemasAsOpenLdapWizardPage rig = new ExportSchemasAsOpenLdapWizardPage();
     *   rig.setSelectedSchemas( preSelectedSchematics );
     *   addPage( rig );
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        page = new ExportSchemasAsOpenLdapWizardPage();
        page.setSelectedSchemas( selectedSchemas );

        // Adding pages
        addPage( page );
    }


    // ── Jyn Aligns The Dish And Fires The Signal ─────────────────────────────
    // Jyn locks the dish into alignment and pushes the transmit switch — the
    // raw schema signal streams out, one schema file per burst, until either
    // all have been sent or an error interrupts a single burst.
    // We iterate the selected schemas, call OpenLdapSchemaFileExporter to render
    // each one to text, and write it to a .schema file; I/O errors are logged
    // and shown in a dialog but don't stop the remaining schemas from being
    // written.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Performs the actual export: converts each selected schema to OpenLDAP
     * source format and writes it to a {@code .schema} file in the chosen
     * directory.
     * Each schema gets its own sub-task on the progress monitor; any I/O error
     * for a single schema is reported in a dialog but does not abort the
     * remaining schemas.
     *
     * <p>For example — Jyn fires one burst per schema from the Scarif dish:</p>
     * <pre>
     *   for ( Schema schema : selectedSchemas ) {
     *       String schemaText = OpenLdapSchemaFileExporter.toSourceCode( schema );
     *       BufferedWriter writer = new BufferedWriter(
     *           new FileWriter( exportDir + "/" + schema.getSchemaName() + ".schema" ) );
     *       writer.write( schemaText );
     *       writer.close();
     *       monitor.worked( 1 );
     *   }
     * </pre>
     *
     * @return {@code true} to close the wizard after the export — even if some
     *         individual schemas failed, since those failures are already
     *         reported via dialogs.
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the schemas to be exported and where to export them
        final Schema[] selectedSchemas = page.getSelectedSchemas();
        final String exportDirectory = page.getExportDirectory();
        try
        {
            getContainer().run( false, false, new IRunnableWithProgress()
            {
                public void run( IProgressMonitor monitor )
                {
                    monitor.beginTask(
                        Messages.getString( "ExportSchemasAsOpenLdapWizard.ExportingSchemas" ), selectedSchemas.length ); //$NON-NLS-1$
                    for ( Schema schema : selectedSchemas )
                    {
                        monitor.subTask( schema.getSchemaName() );

                        try
                        {
                            BufferedWriter buffWriter = new BufferedWriter( new FileWriter( exportDirectory + "/" //$NON-NLS-1$
                                + schema.getSchemaName() + ".schema" ) ); //$NON-NLS-1$
                            buffWriter.write( OpenLdapSchemaFileExporter.toSourceCode( schema ) );
                            buffWriter.close();
                        }
                        catch ( IOException e )
                        {
                            PluginUtils
                                .logError(
                                    NLS
                                        .bind(
                                            Messages.getString( "ExportSchemasAsOpenLdapWizard.ErrorSavingSchema" ), new String[] { schema.getSchemaName() } ), //$NON-NLS-1$
                                    e );
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ExportSchemasAsOpenLdapWizard.Error" ), NLS.bind( Messages.getString( "ExportSchemasAsOpenLdapWizard.ErrorSavingSchema" ), new String[] { schema.getSchemaName() } ) ); //$NON-NLS-1$ //$NON-NLS-2$
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


    // ── Jyn Powers Up The Broadcast Tower ────────────────────────────────────
    // The moment Jyn arrives at the base of the Scarif tower, the first thing
    // she does is power on the equipment and request a progress display —
    // the mission is live, and observers need to see status.
    // We enable the progress monitor so the export progress bar appears,
    // and that's all init needs to do here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initialises the wizard when it opens — we only need to enable the
     * progress monitor so the user gets a progress bar during the export.
     * All the real setup (loading schemas, setting pre-selections) happens in
     * the page itself.
     *
     * <p>For example — Jyn powers up the tower equipment:</p>
     * <pre>
     *   setNeedsProgressMonitor( true );  // status display: active
     *   // tower is live — ready for dish alignment
     * </pre>
     *
     * @param workbench  the Eclipse workbench — not used directly.
     * @param selection  the current workbench selection — not used directly.
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( true );
    }


    // ── Jyn Swaps The Data Cards In The Rig ──────────────────────────────────
    // Before powering up the broadcast rig, Jyn can swap out which data cards
    // are loaded — the caller (the action that opens this wizard) hands her
    // a specific set of schematics to transmit.
    // We store the pre-selection so the wizard page can mark those schemas as
    // checked when it initialises.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects the schemas that should appear checked when the wizard page
     * opens.
     * Call this before the wizard is shown — typically from the action that
     * launches the wizard — so the user starts with their current selection
     * already checked.
     *
     * <p>For example — Jyn loads specific data cards into the broadcast rig:</p>
     * <pre>
     *   wizard.setSelectedSchemas( currentlyHighlightedSchemas );
     *   // wizard opens — those schemas are already checked in the table
     * </pre>
     *
     * @param schemas  the schemas to pre-check; pass an empty array to start
     *                 with nothing selected.
     */
    public void setSelectedSchemas( Schema[] schemas )
    {
        selectedSchemas = schemas;
    }
}
