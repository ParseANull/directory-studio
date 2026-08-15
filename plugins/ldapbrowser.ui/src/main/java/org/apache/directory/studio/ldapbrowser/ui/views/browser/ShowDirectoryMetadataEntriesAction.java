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

package org.apache.directory.studio.ldapbrowser.ui.views.browser;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;


// ── CLASS: ShowDirectoryMetadataEntriesAction — LUKE SEES THE HIDDEN DETAIL ──
// In the binary sunset scene, Luke isn't just seeing pretty colours — if he
// looks carefully he can also see the moisture vaporators, the buried power
// lines, the infrastructure that makes the farm function. Most people miss
// that detail. Directory metadata entries (schema, subschema subentry, root
// DSE children) are exactly that: the hidden infrastructure of an LDAP server
// that casual users don't need but admins love to see.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggles visibility of directory metadata entries in the LDAP browser view.
 * Metadata entries are special server-level entries like the schema subentry
 * and root DSE children — they're not regular data, but they're invaluable
 * for understanding what a directory server actually supports.
 * Think of Luke finally noticing the vaporators on the horizon: once you
 * know they're there you can choose whether to keep them in view.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ShowDirectoryMetadataEntriesAction extends Action
{

    // ── Luke Notices the Hidden Infrastructure ───────────────────────────────────
    // Luke stands at the vantage point and consciously shifts his gaze —
    // past the beauty of the twin suns — to the functional detail underneath.
    // We initialise the toggle by reading the stored preference, so the
    // button accurately reflects what the user chose last time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ShowDirectoryMetadataEntriesAction and restores the user's
     * saved preference so the toggle starts in the correct checked/unchecked state.
     * Skipping this read would cause metadata entries to always default to hidden,
     * frustrating any admin who wanted them visible on startup.
     */
    public ShowDirectoryMetadataEntriesAction()
    {
        super( Messages.getString( "ShowDirectoryMetadataEntriesAction.ShowDirectoryMetadata" ), IAction.AS_CHECK_BOX ); //$NON-NLS-1$
        setEnabled( true );
        setChecked( BrowserCommonActivator.getDefault().getPreferenceStore().getBoolean(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIRECTORY_META_ENTRIES ) );
    }


    // ── Luke Chooses to Look — or Look Away ─────────────────────────────────────
    // He can shift his attention from the sunset to the vaporators and back;
    // the infrastructure is either part of his visible world or it isn't.
    // Clicking the toggle persists the new preference so the browser
    // content provider includes or excludes metadata entries on its next refresh.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Saves the new visibility preference for directory metadata entries.
     * The content provider reads this flag when building the tree, so changing
     * it here is what actually makes the metadata entries show up or disappear.
     */
    public void run()
    {
        BrowserCommonActivator.getDefault().getPreferenceStore().setValue(
            BrowserCommonConstants.PREFERENCE_BROWSER_SHOW_DIRECTORY_META_ENTRIES, isChecked() );
    }

}
