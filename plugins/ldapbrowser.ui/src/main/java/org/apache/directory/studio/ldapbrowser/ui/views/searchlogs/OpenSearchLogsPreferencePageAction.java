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


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenSearchLogsPreferencePageAction — PALPATINE OPENS THE SETTINGS ──
// Palpatine doesn't just issue standing orders — he maintains a preferences
// page for them. "Search logs: enabled? How many files to keep? Maximum file
// size?" This action opens exactly that preferences dialog, pre-focused on
// the search logs configuration so the user doesn't have to hunt through the
// Preferences tree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Preferences dialog directly on the search logs preference page.
 * This action lives in the search logs view's drop-down menu so users can
 * reach the log configuration with a single click instead of navigating
 * through Window → Preferences → LDAP Browser → Search Logs.
 * Think of it as Palpatine opening the Imperial directive console:
 * one command and you're looking at the controls that govern all logging behaviour.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSearchLogsPreferencePageAction extends Action
{

    // ── Palpatine Takes His Seat at the Console ───────────────────────────────────
    // He settles in and labels the button so everyone knows what it does.
    // We set the label and tooltip text so the menu item is descriptive.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenSearchLogsPreferencePageAction with label and tooltip set.
     */
    public OpenSearchLogsPreferencePageAction()
    {
        setText( Messages.getString( "OpenSearchLogsPreferencePageAction.Preferences" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenSearchLogsPreferencePageAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── Palpatine Opens the Imperial Directive Panel ─────────────────────────────
    // One decisive move: the panel opens, pre-set to the right configuration
    // screen. No hunting, no scrolling through menus.
    // We open the preferences dialog with the search logs page pre-selected
    // so the user lands exactly where they need to be.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Opens the Preferences dialog pre-focused on the search logs preference page.
     * We grab the active shell from the current display rather than from the view,
     * so this works even when invoked from a context where the view shell isn't
     * directly available.
     */
    @Override
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String mlPageId = BrowserUIConstants.PREFERENCEPAGEID_SEARCHLOGS;
        PreferencesUtil.createPreferenceDialogOn( shell, mlPageId, new String[]
            { mlPageId }, null ).open();
    }

}
