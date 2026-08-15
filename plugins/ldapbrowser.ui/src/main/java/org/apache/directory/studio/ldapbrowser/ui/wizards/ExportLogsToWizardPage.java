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


import org.apache.directory.studio.common.ui.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.swt.widgets.Composite;


// ── CLASS: ExportLogsToWizardPage — THE IMPERIAL ARCHIVIST FILES THE RECORD ──
// The Imperial Records Officer takes a stack of LDIF log files from the
// modification log subsystem and copies them into a single destination file
// for archival. This shared "To" page works for both the modification-logs
// and search-logs export wizards: it just asks where to deliver the concatenated
// log, with no extra text-format preferences (logs are already in LDIF format).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Shared "To" page for log-export wizards (modification logs and search logs).
 * Extends {@link ExportBaseToPage} with LDIF extension filters (*.ldif, *) and
 * the LDIF wizard icon, but unlike {@link ExportLdifToWizardPage} it does not
 * add a "See Text Formats" link because logs are always written in pre-formatted
 * LDIF; there are no formatting options to configure.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportLogsToWizardPage extends ExportBaseToPage
{

    /** The extensions used by LDIF files */
    private static final String[] EXTENSIONS = new String[]
        { "*.ldif", "*" }; //$NON-NLS-1$ //$NON-NLS-2$


    // ── The Archivist Accepts the Delivery Brief ──────────────────────────────────
    // The log destination page uses the LDIF icon because log files are LDIF format.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportLogsToWizardPage with the LDIF wizard icon.
     * The page name includes "ModificationLogs" in the original implementation
     * even though this page is now shared with the search logs wizard.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportLogsToWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard );
        setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_EXPORT_LDIF_WIZARD ) );
    }


    // ── The Archivist Lays Out the Filing Panel ───────────────────────────────────
    // Just the standard file-selector — no extra text-format link since log
    // files are already fully formatted LDIF.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Builds the page UI using only the base-class file browser in a three-column
     * grid. No extra links or labels are added because log files are already in
     * canonical LDIF format with no configurable formatting options.
     *
     * @param parent  the parent composite.
     */
    public void createControl( Composite parent )
    {
        final Composite composite = BaseWidgetUtils.createColumnContainer( parent, 3, 1 );
        super.createControl( composite );
    }


    // ── The Archive Accepts LDIF Format Only ──────────────────────────────────────
    // Log files are always LDIF, so the file browser filters accordingly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the file-extension filters for the logs save dialog.
     *
     * @return  {@code ["*.ldif", "*"]}.
     */
    protected String[] getExtensions()
    {
        return EXTENSIONS;
    }


    // ── The Archive Label ─────────────────────────────────────────────────────────
    // The format name appears in error messages like "please enter a Log file."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Returns the localised format name "Log" for use in page titles and
     * error messages.
     *
     * @return  the string "Log".
     */
    protected String getFileType()
    {
        return Messages.getString( "ExportLogsToWizardPage.Log" ); //$NON-NLS-1$
    }

}
