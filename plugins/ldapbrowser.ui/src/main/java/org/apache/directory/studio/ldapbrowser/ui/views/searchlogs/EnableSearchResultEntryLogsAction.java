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

package org.apache.directory.studio.ldapbrowser.ui.views.searchlogs;


import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;


// ── CLASS: EnableSearchResultEntryLogsAction — PALPATINE'S SECOND ORDER ───────
// Palpatine has already issued Order 66 for search requests; now he issues a
// companion order: "Also log every search result entry that comes back."
// This is a separate toggle because request logs and result-entry logs can
// be enormous — you might want one without the other. Same mechanism as
// EnableSearchRequestLogsAction, different preference key.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggle action that enables or disables logging of incoming LDAP search result entries.
 * When enabled, every entry returned by the server during a search is written
 * to the log file alongside the original request.
 * This is separate from request logging because result sets can be very large —
 * enabling both at once on a busy server can fill disk quickly.
 * Think of it as Palpatine's second transmission: request logging is Order 66,
 * this is the follow-up order confirming what the clones should report back.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EnableSearchResultEntryLogsAction extends Action
{

    // ── Palpatine Checks the Current Standing Orders ─────────────────────────────
    // Before broadcasting, Palpatine checks the current logs-enabled status
    // so the toggle button starts in sync with reality.
    // We read the preference so the check state is accurate from the moment
    // the menu opens.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EnableSearchResultEntryLogsAction, restoring the toggle state
     * from the saved preference so it reflects the current logging configuration.
     */
    public EnableSearchResultEntryLogsAction()
    {
        super( Messages.getString( "EnableSearchResultEntryLogsAction.EnableSearchResultLogs" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( getText() );
        setEnabled( true );
        setChecked( ConnectionCorePlugin.getDefault().isSearchResultEntryLogsEnabled() );
    }


    // ── Palpatine Issues the Follow-Up Transmission ──────────────────────────────
    // The second holographic message goes out: "Also capture result entries."
    // The effect is immediate — every subsequent search response is captured.
    // We write the preference and flush it so the connection layer starts
    // (or stops) logging result entries without needing a restart.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Persists the new search-result-entry logging preference and flushes it
     * to the instance scope so it takes effect immediately.
     */
    @Override
    public void run()
    {
        IEclipsePreferences instancePreferences = ConnectionCorePlugin.getDefault().getInstanceScopePreferences();
        instancePreferences.putBoolean( ConnectionCoreConstants.PREFERENCE_SEARCHRESULTENTRYLOGS_ENABLE,
            super.isChecked() );
        ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
    }

}
