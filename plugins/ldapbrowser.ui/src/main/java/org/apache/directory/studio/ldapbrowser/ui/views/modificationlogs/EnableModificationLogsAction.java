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

package org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs;


import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;


// ── CLASS: EnableModificationLogsAction — PALPATINE ISSUES ORDER 66 ──────────
// Palpatine sits on the throne and speaks one directive that changes everything:
// "Execute Order 66." With that single command, the entire clone army switches
// mode — from allies to hunters — instantly and globally.
// This action works the same way: one toggle flips a global preference that
// tells every connection in the workspace to start (or stop) recording
// modification logs to disk. One command, system-wide effect.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A checkbox action that globally enables or disables modification logging across
 * all connections in the workspace.
 * When checked, the {@link ConnectionCorePlugin} begins writing every LDAP
 * modify/add/delete operation to rotating LDIF log files on disk.
 * Think of this as Palpatine's Order 66: one toggle, immediate system-wide effect.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EnableModificationLogsAction extends Action
{

    // ── Constructor: Palpatine Takes the Throne ───────────────────────────────
    // Palpatine settles into the Emperor's throne, surveys the galaxy, and
    // reads the current status — are the clones already following his orders?
    // We create the checkbox action, initialize its checked state from the
    // current preference value so the toggle reflects reality on first display.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this toggle action and initializes it from the current modification
     * logging preference value.
     * We read {@link ConnectionCorePlugin#isModificationLogsEnabled()} so the
     * checkbox is already in the right state when the menu first opens.
     *
     * <p>For example — Palpatine reads the galaxy's current status before issuing orders:</p>
     * <pre>
     *   setChecked( ConnectionCorePlugin.getDefault().isModificationLogsEnabled() );
     *   // Checkbox matches whatever state the preference is already in
     * </pre>
     */
    public EnableModificationLogsAction()
    {
        super( Messages.getString( "EnableModificationLogsAction.EnableModificationLogs" ), AS_CHECK_BOX ); //$NON-NLS-1$
        setToolTipText( getText() );
        setEnabled( true );
        setChecked( ConnectionCorePlugin.getDefault().isModificationLogsEnabled() );
    }


    // ── run: Palpatine Speaks the Order ──────────────────────────────────────
    // Palpatine presses the comm button and speaks: "Execute Order 66."
    // The command propagates instantly to every clone trooper in the galaxy.
    // We persist the new checkbox state to the Eclipse instance-scope preference
    // store, which immediately affects all connections in this workspace.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Persists the new enabled/disabled state to the Eclipse preference store.
     * We write to the instance-scope preferences (per-workspace, not per-user)
     * and flush immediately so the change takes effect for all open connections.
     *
     * <p>For example — Palpatine broadcasts Order 66 across the HoloNet:</p>
     * <pre>
     *   instancePreferences.putBoolean( PREFERENCE_MODIFICATIONLOGS_ENABLE, isChecked() );
     *   ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
     *   // All connections in the workspace now obey the new setting
     * </pre>
     */
    @Override
    public void run()
    {
        IEclipsePreferences instancePreferences = ConnectionCorePlugin.getDefault().getInstanceScopePreferences();
        instancePreferences.putBoolean( ConnectionCoreConstants.PREFERENCE_MODIFICATIONLOGS_ENABLE,
            super.isChecked() );
        ConnectionCorePlugin.getDefault().flushInstanceScopePreferences();
    }

}
