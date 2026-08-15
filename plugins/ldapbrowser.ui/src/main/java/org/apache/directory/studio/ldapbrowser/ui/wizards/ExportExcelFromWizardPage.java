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


// ── CLASS: ExportExcelFromWizardPage — YODA SCOPES THE EXCEL LIFT ────────────
// Before Yoda lifts the LDAP tree into Excel, he decides what to include:
// the DN column (pre-checked since Excel rows need identifying keys),
// "return all attributes" (pre-checked when no explicit attribute list exists),
// and "return operational attributes" (visible so power users can get
// createTimestamp, modifyTimestamp, etc.). More attributes than any other
// export format — Excel can handle the columns.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "From" page of the Excel export wizard: defines the LDAP search and
 * attribute options for the export. Shows the DN-return, all-attributes, and
 * operational-attributes checkboxes; DN and all-attributes are both pre-checked
 * by default for the broadest useful output. Pre-checks all-attributes only
 * when the search has no explicit returning-attributes list.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportExcelFromWizardPage extends ExportBaseFromWizardPage
{

    // ── Yoda Reads the Excel Mission Config ───────────────────────────────────────
    // All checkboxes that are useful for an Excel export are made visible;
    // all-attributes is pre-checked to avoid an empty spreadsheet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportExcelFromWizardPage with the Excel wizard icon and a
     * SearchPageWrapper configured for Excel export: name and referral-manual
     * options hidden; DN-return visible and checked; all-attributes and
     * operational-attributes checkboxes visible. When the search has no explicit
     * returning-attributes list, all-attributes is pre-checked so the spreadsheet
     * isn't empty.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportExcelFromWizardPage( String pageName, ExportBaseWizard wizard )
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
            BrowserUIConstants.IMG_EXPORT_XLS_WIZARD ) );
    }


    // ── Yoda Checks the DN Column Preference ─────────────────────────────────────
    // Each Excel row represents an LDAP entry; the DN column identifies it.
    // The wizard reads this before launching the export job.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the "Return DN" checkbox is currently checked.
     * Passed to {@link ExportXlsRunnable} by the wizard so it knows whether
     * to include a leading DN column in the spreadsheet.
     *
     * @return  {@code true} if the DN should appear as the first column.
     */
    public boolean isExportDn()
    {
        return spw.isReturnDn();
    }

}
