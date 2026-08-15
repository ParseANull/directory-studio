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


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenModificationLogsPreferencePageAction — PALPATINE RECONFIGURES ─
// Palpatine doesn't just issue orders in the moment — he also maintains the
// broader policy framework, opening the Imperial Directive document to fine-tune
// the standing rules around logging, retention, and surveillance.
// This action opens the Eclipse preference page for modification logs so the
// user can configure the fine-grained settings: how many log files to keep,
// maximum file size, and whether logging is on at all.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A menu action that opens the Preferences dialog directly to the modification
 * logs preference page.
 * Rather than making the user navigate Preferences &gt; LDAP Browser &gt; Modification Logs
 * manually, this shortcut jumps them straight there.
 * Think of this as Palpatine opening the Imperial Directive document to review and
 * adjust the standing logging policy.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenModificationLogsPreferencePageAction extends Action
{

    // ── Constructor: Palpatine Picks Up the Policy Document ──────────────────
    // Palpatine reaches for the Imperial Directive binder, reads the title, and
    // sets it in front of him — ready to open it when the moment calls for review.
    // We set the label and tooltip from the resource bundle, enable the action.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this action with the localized label and tooltip from the resource bundle.
     * We enable it unconditionally because the preferences page is always accessible.
     *
     * <p>For example — Palpatine picks up the Imperial Directive binder:</p>
     * <pre>
     *   setText( Messages.getString( "...Preferences" ) );
     *   setToolTipText( Messages.getString( "...PreferencesToolTip" ) );
     *   setEnabled( true );
     * </pre>
     */
    public OpenModificationLogsPreferencePageAction()
    {
        setText( Messages.getString( "OpenModificationLogsPreferencePageAction.Preferences" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenModificationLogsPreferencePageAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── run: Palpatine Opens the Policy Document for Review ──────────────────
    // The time has come for a policy review — Palpatine opens the Imperial
    // Directive binder and turns straight to the modification-logs section,
    // bypassing all the irrelevant chapters.
    // We open the Eclipse Preferences dialog filtered to just the modification
    // logs page so the user lands immediately where they need to be.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse Preferences dialog filtered to the modification logs page.
     * We use {@link PreferencesUtil#createPreferenceDialogOn} with the page ID
     * and a single-element filter array so only our page is shown.
     *
     * <p>For example — Palpatine opens the Directive straight to the logging section:</p>
     * <pre>
     *   PreferencesUtil.createPreferenceDialogOn( shell, mlPageId,
     *       new String[]{ mlPageId }, null ).open();
     *   // Dialog opens directly on the Modification Logs preferences page
     * </pre>
     */
    @Override
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String mlPageId = BrowserUIConstants.PREFERENCEPAGEID_MODIFICATIONLOGS;
        PreferencesUtil.createPreferenceDialogOn( shell, mlPageId, new String[]
            { mlPageId }, null ).open();
    }

}
