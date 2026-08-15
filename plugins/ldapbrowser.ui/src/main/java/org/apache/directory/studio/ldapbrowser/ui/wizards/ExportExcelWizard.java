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


import org.apache.directory.studio.ldapbrowser.core.jobs.ExportXlsRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportExcelWizard — YODA LIFTS THE X-WING: EXCEL EDITION ──────────
// Yoda lifts the LDAP tree into a format that Rebel accountants can open:
// Excel .xls format. The transformation is the same Force principle — complex
// LDAP entries become simple rows and columns — but the destination format
// is an Excel workbook rather than a text file. Yoda also respects the
// operational-attributes checkbox since Excel power users may want to see
// modifyTimestamp and createTimestamp alongside regular attributes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Two-page wizard that exports LDAP search results to an Excel .xls file.
 * The From page configures the search (including optional DN and all-attributes
 * flags). The To page picks the destination file. {@code performFinish()} runs
 * an {@link ExportXlsRunnable} as an async background job.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportExcelWizard extends ExportBaseWizard
{

    /** The from page, used to select the exported data. */
    private ExportExcelFromWizardPage fromPage;

    /** The to page, used to select the target file. */
    private ExportExcelToWizardPage toPage;


    // ── Yoda Announces the Excel Lift ─────────────────────────────────────────────
    // The window title sets the context for the entire wizard.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportExcelWizard with the localised "Excel Export" window title.
     */
    public ExportExcelWizard()
    {
        super( Messages.getString( "ExportExcelWizard.ExcelExport" ) ); //$NON-NLS-1$
    }


    // ── Yoda Has a Registry ID ────────────────────────────────────────────────────
    // Actions that open the Excel export wizard look it up by this constant ID.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the export Excel wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_EXPORT_EXCEL;
    }


    // ── Yoda Lays Out the Two Steps ───────────────────────────────────────────────
    // From page = choose data; To page = choose file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the From page (search and attribute configuration) and the To page
     * (destination .xls file path).
     */
    public void addPages()
    {
        fromPage = new ExportExcelFromWizardPage( ExportExcelFromWizardPage.class.getName(), this );
        addPage( fromPage );
        toPage = new ExportExcelToWizardPage( ExportExcelToWizardPage.class.getName(), this );
        addPage( toPage );
    }


    // ── Yoda Registers the Help Topic ────────────────────────────────────────────
    // F1 on either page opens the Excel export help article.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Registers the Excel export help context ID on both wizard pages.
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( fromPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_excelexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( toPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_excelexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Yoda Executes the Excel Lift ─────────────────────────────────────────────
    // The Force flows, and the LDAP tree materialises as an Excel workbook
    // in an async background job.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings from both pages and launches an async
     * {@link ExportXlsRunnable} background job with the configured search
     * and DN flag.
     *
     * @return  {@code true} always — the export runs asynchronously after the wizard closes.
     */
    public boolean performFinish()
    {
        fromPage.saveDialogSettings();
        toPage.saveDialogSettings();
        boolean exportDn = this.fromPage.isExportDn();

        new StudioBrowserJob( new ExportXlsRunnable( exportFilename, search.getBrowserConnection(),
            search.getSearchParameter(), exportDn ) ).execute();

        return true;
    }

}
