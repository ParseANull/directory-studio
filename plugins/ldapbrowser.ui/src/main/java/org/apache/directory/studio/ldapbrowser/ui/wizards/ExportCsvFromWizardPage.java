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


import org.apache.directory.studio.ldapbrowser.common.widgets.search.SearchPageWrapper;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;


// ── CLASS: ExportCsvFromWizardPage — YODA IDENTIFIES WHAT TO LIFT ────────────
// Before Yoda lifts the X-wing, he decides what to lift and how.
// For CSV export, the "what" is: which connection, base DN, filter, scope,
// and attributes — plus a unique CSV option: include the entry's DN as an
// extra first column. This page shows that configuration with the DN checkbox
// visible and checked by default.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "From" page of the CSV export wizard: defines the LDAP search that
 * selects the entries to export and whether to include the entry DN column.
 * The SearchPageWrapper is configured with the DN-return checkbox visible and
 * pre-checked, since CSV consumers often need the DN to identify each row.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportCsvFromWizardPage extends ExportBaseFromWizardPage
{

    // ── Yoda Checks the Configuration ────────────────────────────────────────────
    // The Force tells Yoda which fields matter for a CSV lift: show the DN
    // checkbox (visible and checked), hide the name field and manual referral
    // options — keep it simple.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportCsvFromWizardPage with a SearchPageWrapper configured for
     * CSV export: name hidden, DN-return visible and checked, all-attributes and
     * operational-attributes checkboxes hidden.
     * The DN checkbox is checked by default because CSV rows usually need it to
     * identify entries, but the user can uncheck it if they want attribute data only.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportCsvFromWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard, new SearchPageWrapper( SearchPageWrapper.NAME_INVISIBLE
            | SearchPageWrapper.REFERRALOPTIONS_FOLLOW_MANUAL_INVISIBLE | SearchPageWrapper.RETURN_DN_VISIBLE
            | SearchPageWrapper.RETURN_DN_CHECKED ) );
        super.setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor(
            BrowserUIConstants.IMG_EXPORT_CSV_WIZARD ) );
    }


    // ── Yoda Checks Whether to Include the DN ────────────────────────────────────
    // The wizard needs to know if the DN column should appear in the output file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the "Return DN" checkbox is currently checked.
     * Passed to {@link ExportCsvRunnable} by the wizard so it knows whether
     * to include a leading DN column in the CSV output.
     *
     * @return  {@code true} if the DN should appear as the first column.
     */
    public boolean isExportDn()
    {
        return spw.isReturnDn();
    }

}
