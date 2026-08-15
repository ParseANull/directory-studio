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


import org.apache.directory.studio.ldapbrowser.core.jobs.ExportLdifRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportLdifWizard — C-3PO WRITES THE LDIF SCROLL ──────────────────
// C-3PO is fluent in over six million forms of communication, and LDIF
// (LDAP Data Interchange Format) is the native tongue of LDAP servers.
// This wizard has C-3PO translate the selected LDAP entries into a proper
// LDIF text file — the most portable and server-compatible export format.
// Every LDIF file can be fed directly back to an LDAP server with ldapadd.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Two-page wizard that exports LDAP search results to an LDIF text file.
 * The From page defines the search; the To page picks the destination file.
 * {@code performFinish()} launches an {@link ExportLdifRunnable} as an async
 * background job. LDIF is the canonical LDAP interchange format and can be
 * loaded back into any standard LDAP server.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportLdifWizard extends ExportBaseWizard
{

    /** The from page, used to select the exported data. */
    private ExportLdifFromWizardPage fromPage;

    /** The to page, used to select the target file. */
    private ExportLdifToWizardPage toPage;


    // ── C-3PO Announces the Translation Session ───────────────────────────────────
    // The wizard title sets the context for translating LDAP into LDIF.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportLdifWizard with the localised "LDIF Export" window title.
     */
    public ExportLdifWizard()
    {
        super( Messages.getString( "ExportLdifWizard.LDIFExport" ) ); //$NON-NLS-1$
    }


    // ── C-3PO Has a Registry ID ───────────────────────────────────────────────────
    // The LDIF export wizard is look-upable by this constant so actions can
    // open it programmatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the export LDIF wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_EXPORT_LDIF;
    }


    // ── C-3PO Prepares the Two Translation Steps ──────────────────────────────────
    // Step 1: what to translate (From page). Step 2: where to write the output
    // (To page, *.ldif file).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the From page (search configuration) and the To page (destination .ldif file).
     */
    public void addPages()
    {
        fromPage = new ExportLdifFromWizardPage( ExportLdifFromWizardPage.class.getName(), this );
        addPage( fromPage );
        toPage = new ExportLdifToWizardPage( ExportLdifToWizardPage.class.getName(), this );
        addPage( toPage );
    }


    // ── C-3PO Connects the Help Dictionary ───────────────────────────────────────
    // F1 on either page opens the LDIF export help article.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Registers the LDIF export help context ID on both wizard pages.
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( fromPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_ldifexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem()
            .setHelp( toPage.getControl(), BrowserUIConstants.PLUGIN_ID + "." + "tools_ldifexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── C-3PO Writes the LDIF Scroll ─────────────────────────────────────────────
    // Fluent in LDIF, C-3PO dictates the entries to a background job that
    // writes each entry in correct LDIF format.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings from both pages and launches an async
     * {@link ExportLdifRunnable} background job with the search and destination path.
     *
     * @return  {@code true} always — the export runs asynchronously.
     */
    public boolean performFinish()
    {
        fromPage.saveDialogSettings();
        toPage.saveDialogSettings();

        new StudioBrowserJob( new ExportLdifRunnable( exportFilename, search.getBrowserConnection(),
            search.getSearchParameter() ) ).execute();

        return true;
    }

}
