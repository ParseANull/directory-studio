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


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenBrowserPreferencePageAction — PALPATINE ISSUES ORDER 66 ───────
// Palpatine sits in his Senate office and issues Order 66: a single command
// that reconfigures every clone trooper across the galaxy at once, changing
// their standing orders permanently. Opening the Preferences dialog is our
// version of that: one click and the user can reconfigure how the entire
// browser view behaves.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Preferences dialog pre-scrolled to the LDAP browser preference page.
 * We need a dedicated action for this so users can reach the browser settings quickly
 * from the view's own drop-down menu, without hunting through the main Preferences tree.
 * Think of Palpatine issuing Order 66: one decisive command that immediately
 * reconfigures behaviour across the whole system.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenBrowserPreferencePageAction extends Action
{

    // ── Palpatine Takes His Seat ─────────────────────────────────────────────────
    // The Emperor settles into his chair in the Senate, Order 66 protocol
    // loaded and ready — one transmission away from changing everything.
    // We set the button label and tooltip so it's crystal clear to the user
    // what clicking this action will do.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenBrowserPreferencePageAction with label and tooltip wired up.
     * No parameters needed — everything this action requires (the preference page ID)
     * is a compile-time constant in {@link BrowserUIConstants}.
     */
    public OpenBrowserPreferencePageAction()
    {
        super.setText( Messages.getString( "OpenBrowserPreferencePageAction.Preferences" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenBrowserPreferencePageAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── Order 66 Is Transmitted ──────────────────────────────────────────────────
    // Palpatine sends the holographic command and every clone trooper snaps
    // into their new configuration — the galaxy changes in an instant.
    // We pop open the Preferences dialog, pre-selected to the browser page,
    // so the user lands exactly where they need to be to tweak settings.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Opens the Eclipse Preferences dialog pre-focused on the browser preference page.
     * We grab the active shell from the current display — not from the view itself —
     * so this works even when called from a context menu.
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String pageId = BrowserUIConstants.PREFERENCEPAGEID_BROWSER;
        PreferencesUtil.createPreferenceDialogOn( shell, pageId, new String[]
            { pageId }, null ).open();
    }

}
