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


// ── CLASS: ExportOdfFromWizardPage — YODA SCOPES THE ODF LIFT ────────────────
// Before Yoda lifts the X-wing into ODF format, he decides exactly what goes
// into the spreadsheet: DN column, all attributes, and operational attributes —
// the same rich configuration as the Excel export. ODS consumers (LibreOffice
// users) appreciate getting both user and operational attributes in the same
// spreadsheet.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "From" page of the ODF export wizard: defines the LDAP search and
 * attribute options. Identical configuration to {@link ExportExcelFromWizardPage}
 * (DN-return visible and checked; all-attributes and operational-attributes visible;
 * all-attributes pre-checked when no explicit list is set) but uses the ODF wizard
 * icon.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportOdfFromWizardPage extends ExportBaseFromWizardPage
{

    // ── Yoda Reads the ODF Mission Config ─────────────────────────────────────────
    // Same attribute configuration as Excel: DN visible and checked, all-attributes
    // and operational-attributes visible, all-attributes pre-checked when no
    // explicit returning list is set.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportOdfFromWizardPage with the ODF wizard icon and a
     * SearchPageWrapper configured for ODF export: name and referral-manual options
     * hidden; DN-return visible and checked; all-attributes and operational-attributes
     * visible. Pre-checks all-attributes when no explicit returning-attributes list
     * is configured so the spreadsheet is fully populated by default.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportOdfFromWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard, new SearchPageWrapper(
            SearchPageWrapper.NAME_INVISIBLE
                | SearchPageWrapper.REFERRALOPTIONS_FOLLOW_MANUAL_INVISIBLE
                | SearchPageWrapper.RETURN_DN_VISIBLE
                | SearchPageWrapper.RETURN_DN_CHECKED
                | SearchPageWrapper.RETURN_ALLATTRIBUTES_VISIBLE
                | SearchPageWrapper.RETURN_OPERATIONALATTRIBUTES_VISIBLE
                | ( ( wizard.getSearch().getReturningAttributes() == null || wizard.getSearch()
                    .getReturningAttributes().length == 0 ) ? SearchPageWrapper.RETURN_ALLATTRIBUTES_CHECKED
                    : SearchPageWrapper.NONE ) ) );
        super.setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor(
            BrowserUIConstants.IMG_EXPORT_ODF_WIZARD ) );
    }


    // ── Yoda Checks the DN Column Preference ─────────────────────────────────────
    // ODF spreadsheet rows need the DN to identify each entry, same as Excel.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the "Return DN" checkbox is currently checked.
     * Passed to {@link ExportOdfRunnable} by the wizard so it knows whether
     * to include a leading DN column in the ODS spreadsheet.
     *
     * @return  {@code true} if the DN should appear as the first column.
     */
    public boolean isExportDn()
    {
        return spw.isReturnDn();
    }

}
