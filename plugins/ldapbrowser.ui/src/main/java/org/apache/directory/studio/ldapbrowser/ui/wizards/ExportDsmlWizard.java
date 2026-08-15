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


import org.apache.directory.studio.ldapbrowser.core.jobs.ExportDsmlRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.ExportDsmlRunnable.ExportDsmlJobType;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ExportDsmlWizard — YODA LIFTS THE X-WING: DSML EDITION ────────────
// Yoda reaches out with the Force and transforms the heavy LDAP tree into
// portable XML — either as a searchResultEntry response (the entries themselves)
// or as a searchRequest (the query parameters that produced them).
// DSML is the XML format that bridges LDAP and web services, so Yoda must
// also decide HOW to package the payload: response or request format.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Two-page wizard that exports LDAP search results to a DSML XML file.
 * The From page defines the search; the To page picks the file and the DSML
 * variant (RESPONSE = searchResultEntry elements, REQUEST = searchRequest element).
 * {@code performFinish()} launches an {@link ExportDsmlRunnable} with the appropriate
 * {@link ExportDsmlJobType} based on the user's choice.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportDsmlWizard extends ExportBaseWizard
{

    /** The title. */
    public static final String WIZARD_TITLE = Messages.getString( "ExportDsmlWizard.DSMLExport" ); //$NON-NLS-1$

    /** The from page, used to select the exported data. */
    private ExportDsmlFromWizardPage fromPage;

    /** The to page, used to select the target file. */
    private ExportDsmlToWizardPage toPage;

    private ExportDsmlWizardSaveAsType saveAsType = ExportDsmlWizardSaveAsType.RESPONSE;

    // ── CLASS: ExportDsmlWizardSaveAsType — HAN PICKS THE CARGO FORMAT ────────────
    // The Falcon can carry the cargo in two containers: the full response payload
    // or just the request manifest. The RESPONSE type includes the entries themselves;
    // the REQUEST type includes only the search query.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * The two possible DSML export formats. RESPONSE writes the LDAP search results
     * as searchResultEntry elements; REQUEST writes the LDAP query as a
     * searchRequest element. The user picks one via radio buttons on the To page.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public enum ExportDsmlWizardSaveAsType
    {
        RESPONSE, REQUEST
    };


    // ── Yoda Announces the Lift ───────────────────────────────────────────────────
    // The wizard title is declared as a constant so other code can reference it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportDsmlWizard with the localised "DSML Export" window title.
     */
    public ExportDsmlWizard()
    {
        super( WIZARD_TITLE );
    }


    // ── Yoda Has a Registry ID ────────────────────────────────────────────────────
    // The DSML wizard is reachable by ID so actions can open it programmatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse wizard ID for the export DSML wizard.
     *
     * @return  the wizard ID string from {@link BrowserUIConstants}.
     */
    public static String getId()
    {
        return BrowserUIConstants.WIZARD_EXPORT_DSML;
    }


    // ── Yoda Outlines the Two Steps ───────────────────────────────────────────────
    // Step 1: pick the data (From page). Step 2: pick the file and DSML format
    // (To page, with RESPONSE/REQUEST radio buttons).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Adds the From page (search configuration) and the To page (DSML file and format choice).
     */
    public void addPages()
    {
        fromPage = new ExportDsmlFromWizardPage( ExportDsmlFromWizardPage.class.getName(), this );
        addPage( fromPage );
        toPage = new ExportDsmlToWizardPage( ExportDsmlToWizardPage.class.getName(), this );
        addPage( toPage );
    }


    // ── Yoda Connects the Help System ────────────────────────────────────────────
    // Pressing F1 on either page leads to the DSML export help topic.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Registers the DSML export help context ID on both wizard pages.
     *
     * @param pageContainer  the wizard page container.
     */
    public void createPageControls( Composite pageContainer )
    {
        super.createPageControls( pageContainer );

        // set help context ID
        PlatformUI.getWorkbench().getHelpSystem().setHelp( fromPage.getControl(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_dsmlexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
        PlatformUI.getWorkbench().getHelpSystem().setHelp( toPage.getControl(),
            BrowserUIConstants.PLUGIN_ID + "." + "tools_dsmlexport_wizard" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Yoda Executes the Lift ────────────────────────────────────────────────────
    // The Force surges — the export job is dispatched based on the user's choice:
    // RESPONSE writes the entries, REQUEST writes the query that found them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves dialog settings and dispatches the appropriate async export job based
     * on the user's RESPONSE/REQUEST radio button selection on the To page.
     * RESPONSE writes the LDAP entries as DSML searchResultEntry elements;
     * REQUEST writes the query as a DSML searchRequest element.
     *
     * @return  {@code true} always — the export job runs in the background.
     */
    public boolean performFinish()
    {
        fromPage.saveDialogSettings();
        toPage.saveDialogSettings();

        switch ( saveAsType )
        {
            case RESPONSE:
                new StudioBrowserJob( new ExportDsmlRunnable( exportFilename, search.getBrowserConnection(),
                    search.getSearchParameter(),
                    ExportDsmlJobType.RESPONSE ) ).execute();
                break;
            case REQUEST:
                new StudioBrowserJob( new ExportDsmlRunnable( exportFilename, search.getBrowserConnection(),
                    search.getSearchParameter(),
                    ExportDsmlJobType.REQUEST ) ).execute();
                break;
        }

        return true;
    }


    // ── Yoda Checks the Packaging Choice ─────────────────────────────────────────
    // The finish step needs to know whether the user chose RESPONSE or REQUEST.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DSML export format currently selected.
     * {@code performFinish()} reads this to decide which {@link ExportDsmlJobType}
     * to pass to the runnable.
     *
     * @return  the selected {@link ExportDsmlWizardSaveAsType}.
     */
    public ExportDsmlWizardSaveAsType getSaveAsType()
    {
        return saveAsType;
    }


    // ── Yoda Stores the Packaging Choice ─────────────────────────────────────────
    // The To page calls this each time the user clicks a radio button.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DSML export format.
     * Called by the To page's radio-button listeners when the user changes
     * their RESPONSE/REQUEST selection.
     *
     * @param saveAsType  the new DSML export format to use.
     */
    public void setSaveAsType( ExportDsmlWizardSaveAsType saveAsType )
    {
        this.saveAsType = saveAsType;
    }
}
