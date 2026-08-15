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


import org.apache.directory.studio.ldapbrowser.core.jobs.ExportOdfRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportOdfWizard — YODA LIFTS THE X-WING: ODF EDITION ──────────────
// ODF (Open Document Format) is the spreadsheet format of the Rebel Alliance:
// free, open, and compatible with LibreOffice and OpenOffice. Yoda lifts the
// LDAP tree into an ODS spreadsheet — the open-standards alternative to Excel.
// Like the Excel export, it can include the DN column and operational attributes,
// giving Rebel systems administrators the full picture.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Two-page wizard that exports LDAP search results to an ODF spreadsheet (.ods).
 * The From page configures the search (including optional DN and all-attributes
 * flags). The To page picks the destination file. {@code performFinish()} runs
 * an {@link ExportOdfRunnable} as an async background job.
 * ODF is the open-standards equivalent of Excel export — same structure,
 * different wire format.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportOdfWizard extends ExportBaseWizard
{

    /** The from page, used to select the exported data. */
    private ExportOdfFromWizardPage fromPage;

    /** The to page, used to select the target file. */
    private ExportOdfToWizardPage toPage;


    // ── Yoda Announces the ODF Lift ───────────────────────────────────────────────
    // The window title marks the mission as an ODF export.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportOdfWizard with the localised "ODF Export" window title.
     */
    public ExportOdfWizard()
    {
        super( Messages.getString( "ExportOdfWizard.OdfExport" ) ); //$NON-NLS-1$
    }


    // ── Yoda Has a Registry ID ────────────────────────────────────────────────────
    // The ODF export wizard is reachable by constant ID from any action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the export ODF wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_EXPORT_ODF;
    }


    // ── Yoda Lays Out the Two ODF Steps ──────────────────────────────────────────
    // From page = define the LDAP search; To page = choose the ODS file path.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the From page (search configuration) and the To page (destination .ods file).
     */
    public void addPages()
    {
        fromPage = new ExportOdfFromWizardPage( ExportOdfFromWizardPage.class.getName(), this );
        addPage( fromPage );
        toPage = new ExportOdfToWizardPage( ExportOdfToWizardPage.class.getName(), this );
        addPage( toPage );
    }


    // ── Yoda Connects the Help System ────────────────────────────────────────────
    // F1 on either page opens the ODF export help article.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Registers the ODF export help context ID on both wizard pages.
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( fromPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_odfexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( toPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_odfexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Yoda Executes the ODF Lift ────────────────────────────────────────────────
    // The Force lifts the LDAP tree into ODS format in a background job.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings from both pages and launches an async
     * {@link ExportOdfRunnable} background job with the search and DN flag.
     *
     * @return  {@code true} always — the export runs asynchronously.
     */
    public boolean performFinish()
    {
        fromPage.saveDialogSettings();
        toPage.saveDialogSettings();
        boolean exportDn = this.fromPage.isExportDn();

        new StudioBrowserJob( new ExportOdfRunnable( exportFilename, search.getBrowserConnection(),
            search.getSearchParameter(), exportDn ) ).execute();

        return true;
    }

}
