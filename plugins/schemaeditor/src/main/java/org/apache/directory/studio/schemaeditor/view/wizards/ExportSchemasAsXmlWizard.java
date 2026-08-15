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
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileExporter;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;


// ── CLASS: ExportSchemasAsXmlWizard — Leia Loads Plans Into R2-D2 ─────────────
// On the Tantive IV's bridge, Princess Leia copies the stolen Death Star plans
// into R2-D2 before Vader's troops breach the door — a structured data payload
// transmitted under pressure to a specific destination.
// We do the same thing here: serialize LDAP schema objects to XML files and
// write them to wherever the user told us to send them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main wizard that drives exporting one or more LDAP schemas as XML files.
 * It wires together the single wizard page, collects the user's choices, then
 * writes each selected schema out to disk in XML format — either as separate
 * files (one per schema) or bundled together into a single XML file.
 * Think of this class as Princess Leia loading the Death Star plans into R2-D2:
 * we package up the schema data and send it off to the destination the user chose.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportSchemasAsXmlWizard extends Wizard implements IExportWizard
{
    /** The selected schemas */
    private Schema[] selectedSchemas = new Schema[0];

    // The pages of the wizard
    private ExportSchemasAsXmlWizardPage page;


    // ── Leia Slots The Plans Into R2 ─────────────────────────────────────────
    // On the Tantive IV, Leia kneels beside R2-D2 and inserts the data card
    // carrying the Death Star schematics, setting him up for his mission.
    // Here we create our one wizard page and register it with the framework
    // so Eclipse can display it to the user in the right sequence.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates and registers the single wizard page for this export flow.
     * Eclipse calls this during wizard startup so we can contribute our pages
     * before the dialog opens.
     * We also forward any pre-selected schemas to the page so the checkboxes
     * start in the right state.
     *
     * <p>For example — Leia loads the plans before sending R2 on his way:</p>
     * <pre>
     *   Leia slots the data card into R2's memory bank.
     *   "The plans are inside," she says. "Get them to the Alliance."
     *   The wizard page holds the plans; addPages() is the slot.
     * </pre>
     */
    public void addPages()
    {
        // Creating pages
        page = new ExportSchemasAsXmlWizardPage();
        page.setSelectedSchemas( selectedSchemas );

        // Adding pages
        addPage( page );
    }


    // ── R2 Rolls Out, Plans Aboard ────────────────────────────────────────────
    // R2-D2 trundles down the Tantive IV's corridor and launches in an escape
    // pod, carrying Leia's precious data out into the void.
    // When the user clicks Finish, we do the real work: iterate the selected
    // schemas, serialize each to XML, and write the bytes to disk.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the export when the user clicks the Finish button.
     * We pull the schema selection and destination from the page, then write
     * each schema out as XML — either one file per schema or everything in
     * one combined file.
     * If anything goes wrong writing to disk we log the error and show a dialog,
     * but we still return {@code true} so the wizard closes cleanly.
     *
     * <p>For example — R2 launches with the Death Star plans:</p>
     * <pre>
     *   The escape pod fires. R2-D2 carries the plans toward Tatooine.
     *   Each schema is a blueprint; each XML file is a data capsule.
     *   Even if one capsule fails to seal, the pod still launches.
     * </pre>
     *
     * @return {@code true} always — we handle errors internally and let the wizard close
     */
    public boolean performFinish()
    {
        // Saving the dialog settings
        page.saveDialogSettings();

        // Getting the schemas to be exported and where to export them
        final Schema[] selectedSchemas = page.getSelectedSchemas();
        int exportType = page.getExportType();
        if ( exportType == ExportSchemasAsXmlWizardPage.EXPORT_MULTIPLE_FILES )
        {
            final String exportDirectory = page.getExportDirectory();
            try
            {
                getContainer().run( false, false, new IRunnableWithProgress()
                {
                    public void run( IProgressMonitor monitor )
                    {
                        monitor.beginTask(
                            Messages.getString( "ExportSchemasAsXmlWizard.ExportingSchemas" ), selectedSchemas.length ); //$NON-NLS-1$
                        for ( Schema schema : selectedSchemas )
                        {
                            monitor.subTask( schema.getSchemaName() );

                            try
                            {
                                BufferedWriter buffWriter = new BufferedWriter( new FileWriter( exportDirectory + "/" //$NON-NLS-1$
                                    + schema.getSchemaName() + ".xml" ) ); //$NON-NLS-1$
                                buffWriter.write( XMLSchemaFileExporter.toXml( schema ) );
                                buffWriter.close();
                            }
                            catch ( IOException e )
                            {
                                PluginUtils
                                    .logError(
                                        NLS
                                            .bind(
                                                Messages.getString( "ExportSchemasAsXmlWizard.ErrorWhenSavingSchema" ), new String[] { schema.getSchemaName() } ), e ); //$NON-NLS-1$
                                ViewUtils
                                    .displayErrorMessageDialog(
                                        Messages.getString( "ExportSchemasAsXmlWizard.Error" ), NLS.bind( Messages.getString( "ExportSchemasAsXmlWizard.ErrorWhenSavingSchema" ), new String[] { schema.getSchemaName() } ) ); //$NON-NLS-1$ //$NON-NLS-2$
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
        }
        else if ( exportType == ExportSchemasAsXmlWizardPage.EXPORT_SINGLE_FILE )
        {
            final String exportFile = page.getExportFile();
            try
            {
                getContainer().run( false, false, new IRunnableWithProgress()
                {
                    public void run( IProgressMonitor monitor )
                    {
                        monitor.beginTask( Messages.getString( "ExportSchemasAsXmlWizard.ExportingSchemas" ), 1 ); //$NON-NLS-1$
                        try
                        {
                            BufferedWriter buffWriter = new BufferedWriter( new FileWriter( exportFile ) );
                            buffWriter.write( XMLSchemaFileExporter.toXml( selectedSchemas ) );
                            buffWriter.close();
                        }
                        catch ( IOException e )
                        {
                            PluginUtils.logError( Messages
                                .getString( "ExportSchemasAsXmlWizard.ErrorWhenSavingSchemas" ), e ); //$NON-NLS-1$
                            ViewUtils
                                .displayErrorMessageDialog(
                                    Messages.getString( "ExportSchemasAsXmlWizard.Error" ), Messages.getString( "ExportSchemasAsXmlWizard.ErrorWhenSavingSchemas" ) ); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        monitor.worked( 1 );
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
        }

        return true;
    }


    // ── Leia Briefs R2 On The Mission ─────────────────────────────────────────
    // Before loading the plans, Leia crouches in front of R2 and explains the
    // situation: "Help me, Obi-Wan Kenobi, you're my only hope."
    // Eclipse calls init() right after instantiating the wizard — we use it
    // to enable the progress monitor bar so long exports don't freeze the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Initializes the wizard with the Eclipse workbench context.
     * Eclipse calls this once right after creating the wizard instance, before
     * any pages are shown.
     * We just turn on the progress monitor here — the actual setup happens in
     * {@link #addPages()}.
     *
     * <p>For example — Leia primes R2 before the mission begins:</p>
     * <pre>
     *   Leia activates R2's holographic recorder.
     *   "You're my only hope," she says, enabling all systems.
     *   setNeedsProgressMonitor(true) is our equivalent activation.
     * </pre>
     *
     * @param workbench  the Eclipse workbench — we don't use it directly but Eclipse requires it
     * @param selection  whatever the user had selected in the UI — not used here
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( true );
    }


    // ── Leia Chooses Which Plans To Copy ──────────────────────────────────────
    // The stolen Death Star plans are vast; Leia selects only the most critical
    // schematics to load into R2 before Vader's troops break through the door.
    // We let callers pre-select which schemas will be checked in the wizard's
    // table viewer, so the user starts with a sensible default.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pre-selects which schemas will be checked when the wizard opens.
     * If the user invoked this export action with specific schemas already
     * highlighted in the Schema Editor, we pass those in here so the wizard
     * page checks them automatically.
     * The page also calls this directly after construction.
     *
     * <p>For example — Leia decides which blueprints to take:</p>
     * <pre>
     *   "We only have time for the reactor schematics," Leia says,
     *   loading just those data cards into R2's memory bank.
     *   setSelectedSchemas() is us handing R2 the priority list.
     * </pre>
     *
     * @param schemas  the schemas to pre-check in the export page's table
     */
    public void setSelectedSchemas( Schema[] schemas )
    {
        selectedSchemas = schemas;
    }
}
