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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import org.apache.directory.studio.ldapbrowser.core.jobs.ExportCsvRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportCsvWizard — YODA LIFTS THE X-WING: CSV EDITION ──────────────
// Yoda closes his eyes, reaches out with the Force, and lifts a complex LDAP
// tree — transforming it into a flat CSV spreadsheet that anyone can open in
// Excel. The magic is that what seems complex (an LDAP DIT) becomes something
// simple (rows and columns). This wizard directs that transformation: from
// a search result set to a CSV file on disk.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Two-page wizard that exports LDAP search results to a CSV file.
 * The From page defines the search (connection, base DN, filter, scope,
 * attributes, and whether to include the DN column). The To page picks the
 * destination file. {@code performFinish()} launches an {@code ExportCsvRunnable}
 * as an async background job.
 * Think of Yoda transforming the X-wing: the Force converts the LDAP tree
 * into something flat and portable without losing the essential data.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportCsvWizard extends ExportBaseWizard
{

    /** The from page, used to select the exported data. */
    private ExportCsvFromWizardPage fromPage;

    /** The to page, used to select the target file. */
    private ExportCsvToWizardPage toPage;


    // ── Yoda Prepares for the Lift ────────────────────────────────────────────────
    // The wizard title announces the transformation before it starts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportCsvWizard with the localised "CSV Export" window title.
     */
    public ExportCsvWizard()
    {
        super( Messages.getString( "ExportCsvWizard.CSVExport" ) ); //$NON-NLS-1$
    }


    // ── Yoda Has a Known ID ───────────────────────────────────────────────────────
    // The wizard registry knows this wizard by a constant ID so other parts
    // of the UI can open it programmatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the export CSV wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_EXPORT_CSV;
    }


    // ── Yoda Lays Out the Two Steps ───────────────────────────────────────────────
    // Step 1: choose what to lift (the search). Step 2: choose where to set it
    // down (the file path).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the From page (search configuration) and the To page (destination file).
     */
    public void addPages()
    {
        fromPage = new ExportCsvFromWizardPage( ExportCsvFromWizardPage.class.getName(), this );
        addPage( fromPage );
        toPage = new ExportCsvToWizardPage( ExportCsvToWizardPage.class.getName(), this );
        addPage( toPage );
    }


    // ── Yoda Wires the Help Context ───────────────────────────────────────────────
    // F1 on either page should open the CSV export help article.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Registers the CSV export help context ID on both wizard pages.
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( fromPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_csvexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( toPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_csvexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Yoda Lifts the X-Wing ─────────────────────────────────────────────────────
    // Eyes closed, the Force flows, and the X-wing rises. The export job is
    // queued and runs asynchronously while the wizard closes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings from both pages and launches an async
     * {@link ExportCsvRunnable} background job with the configured search
     * parameters and destination file path.
     *
     * @return  {@code true} always — the job runs asynchronously after the wizard closes.
     */
    public boolean performFinish()
    {
        fromPage.saveDialogSettings();
        toPage.saveDialogSettings();
        boolean exportDn = this.fromPage.isExportDn();

        new StudioBrowserJob( new ExportCsvRunnable( exportFilename, search.getBrowserConnection(),
            search.getSearchParameter(), exportDn ) ).execute();

        return true;
    }
}
