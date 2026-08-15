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


// ── CLASS: ExportDsmlFromWizardPage — YODA SCOPES THE DSML LIFT ──────────────
// Before Yoda lifts the DSML X-wing, he decides exactly which attributes to
// include: DSML exports are often consumed by XML-aware services that need
// both user and operational attributes. This page therefore shows the "return
// all attributes" and "return operational attributes" checkboxes so the user
// can choose breadth. The DN checkbox is hidden — DSML entries always carry
// their DN implicitly in the XML structure.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The "From" page of the DSML export wizard: defines the LDAP search for the export.
 * Shows the "return all attributes" and "return operational attributes" checkboxes;
 * pre-checks all-attributes when no explicit returning-attributes list is set.
 * The DN is omitted from the SPW configuration because DSML encodes it in the
 * {@code <dsml:entry dn="...">} attribute automatically.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExportDsmlFromWizardPage extends ExportBaseFromWizardPage
{

    // ── Yoda Reads the DSML Mission Config ────────────────────────────────────────
    // Name hidden (DSML exports don't need a search name), all-attributes and
    // operational-attributes visible, pre-checked when no specific list exists.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExportDsmlFromWizardPage with the DSML wizard icon and a
     * SearchPageWrapper configured for DSML export: name and referral-manual
     * options hidden; all-attributes and operational-attributes checkboxes visible.
     * When the wizard's search has no explicit returning-attributes list, "return
     * all attributes" is pre-checked as a sensible default.
     *
     * @param pageName  the wizard page name.
     * @param wizard    the parent export wizard.
     */
    public ExportDsmlFromWizardPage( String pageName, ExportBaseWizard wizard )
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
            BrowserUIConstants.IMG_EXPORT_DSML_WIZARD ) );
    }
}
