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


// ── CLASS: EnableSearchRequestLogsAction — PALPATINE ISSUES ORDER 66 ─────────
// Palpatine transmits Order 66 to every clone trooper across the galaxy:
// "From this moment forward, log every search request." One command, global
// effect. That's exactly what this action does — it flips the instance-scope
// preference that tells the connection layer to start (or stop) recording
// every outbound LDAP search request into the log files.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Toggle action that enables or disables logging of outgoing LDAP search requests.
 * When enabled, the connection layer writes every search request to a rotating
 * log file that appears in the search logs view.
 * Think of Palpatine issuing Order 66: one flip of this toggle and every
 * search request from that point forward is recorded for review.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EnableSearchRequestLogsAction extends Action
{

    // ── Palpatine Sits Down and Prepares the Transmission ────────────────────────
    // Palpatine settles into his seat, checks the current standing orders,
    // and prepares to broadcast — already knowing whether Order 66 is active.
    // We initialise the toggle with the current preference value so the
    // checked state reflects whatever the user set last time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new EnableSearchRequestLogsAction, reading the current preference
     * so the toggle button starts in the right checked/unchecked state.
     * Without reading the preference here, the toggle would always default to
     * unchecked and logging would silently reset on every restart.
     */
    public EnableSearchRequestLogsAction()
    {
        super( Messages.getString( "EnableSearchRequestLogsAction.EnableSearchRequestLogs" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( getText() );
        setEnabled( true );
        setChecked( ConnectionCorePlugin.getDefault().isSearchRequestLogsEnabled() );
    }


    // ── Palpatine Broadcasts Order 66 ────────────────────────────────────────────
    // The hologram flickers to life and Palpatine transmits: all clone troopers
    // receive the updated standing orders immediately and permanently.
    // We write the new preference to the instance scope (persisted across restarts)
    // and flush it so the connection layer picks it up without a restart.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Persists the new search request logging preference to the instance scope
     * and flushes it so the change takes effect immediately.
     * The connection layer reads this preference each time it decides whether
     * to write a search request to the log file.
     */
    @Override
    public void run()
    {
        IEclipsePreferences instancePreferences = ConnectionCorePlugin.getDefault().getInstanceScopePreferences();
        instancePreferences.putBoolean( ConnectionCoreConstants.PREFERENCE_SEARCHREQUESTLOGS_ENABLE,
            super.isChecked() );
        ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
    }

}
