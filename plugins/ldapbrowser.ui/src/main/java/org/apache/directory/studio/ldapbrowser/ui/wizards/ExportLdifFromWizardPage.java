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


// ── CLASS: ExportLdifFromWizardPage — C-3PO IDENTIFIES WHAT TO TRANSLATE ─────
// C-3PO must know which LDAP entries to translate into LDIF before he can
// write the scroll. LDIF exports typically need all attributes (since the
// goal is a complete server-importable snapshot), and operational attributes
// like createTimestamp are often included too. The DN is always present in
// LDIF format by definition — no separate checkbox needed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "From" page of the LDIF export wizard: defines the LDAP search for the export.
 * Shows the "return all attributes" and "return operational attributes" checkboxes
 * (no DN checkbox since LDIF always includes the DN as the first line of each entry).
 * Pre-checks all-attributes when no explicit returning-attributes list is configured.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportLdifFromWizardPage extends ExportBaseFromWizardPage
{

    // ── C-3PO Reads the LDIF Translation Config ───────────────────────────────────
    // No name field, no DN checkbox (always in LDIF), all-attributes and
    // operational-attributes visible. Pre-checks all-attributes when no explicit
    // returning list is set so the exported LDIF is a complete snapshot.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportLdifFromWizardPage with the LDIF wizard icon and a
     * SearchPageWrapper configured for LDIF export: name and referral-manual
     * options hidden; all-attributes and operational-attributes checkboxes visible.
     * Pre-checks all-attributes when the search has no explicit returning-attributes
     * list so the user gets a complete LDIF snapshot by default.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportLdifFromWizardPage( String pageName, ExportBaseWizard wizard )
    {
        super( pageName, wizard, new SearchPageWrapper(
            SearchPageWrapper.NAME_INVISIBLE
                | SearchPageWrapper.REFERRALOPTIONS_FOLLOW_MANUAL_INVISIBLE
                | SearchPageWrapper.RETURN_ALLATTRIBUTES_VISIBLE
                | SearchPageWrapper.RETURN_OPERATIONALATTRIBUTES_VISIBLE
                | ( ( wizard.getSearch().getReturningAttributes() == null || wizard.getSearch()
                    .getReturningAttributes().length == 0 ) ? SearchPageWrapper.RETURN_ALLATTRIBUTES_CHECKED
                    : SearchPageWrapper.NONE ) ) );
        super.setImageDescriptor( BrowserUIPlugin.getDefault().getImageDescriptor(
            BrowserUIConstants.IMG_EXPORT_LDIF_WIZARD ) );
    }

}
