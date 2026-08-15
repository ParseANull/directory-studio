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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenEntryEditorsPreferencePageAction — PALPATINE OPENS THE CONTROL ROOM ──
// When Palpatine issued Order 66, he didn't shout it from the floor of the Senate —
// he walked calmly into his private control room and transmitted the command to
// every clone unit simultaneously.  This action is the "walk to the control room"
// step: it opens the Entry Editors preference page, the one place where the user
// can configure which entry editors are enabled, in what priority order, and how
// they behave.  One page, all the controls.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse Preferences dialog directly to the Entry Editors preference page.
 * This is a convenience shortcut — rather than navigating Window > Preferences >
 * LDAP Browser > Entry Editors, the user gets there in one click.
 * Think of this class as Palpatine's direct line to the control room: one action,
 * exactly the right configuration panel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEntryEditorsPreferencePageAction extends Action
{

    // ── Palpatine Memorises The Room Number ──────────────────────────────────────
    // Before walking to the control room, Palpatine knows exactly which corridor to
    // take — he's done this before.  Our constructor sets the action's text,
    // tooltip, and enabled state up front so Eclipse can render the button correctly
    // before the user even clicks it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action and sets its label, tooltip, and enabled state.
     * We configure everything in the constructor so Eclipse can display a fully
     * formed menu item or toolbar button without needing to call any other methods.
     */
    public OpenEntryEditorsPreferencePageAction()
    {
        super.setText( Messages.getString( "OpenEntryEditorsPageAction.Preferences" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenEntryEditorsPageAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── Palpatine Enters The Control Room ───────────────────────────────────────
    // Palpatine strides through the door, sits at the console, and opens the
    // transmission controls.  run() does the same: it resolves the active shell,
    // looks up the Entry Editors preference page ID, and opens the Preferences
    // dialog scoped to just that page.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse Preferences dialog, scoped to the Entry Editors page.
     * We use {@link PreferencesUtil#createPreferenceDialogOn} with the entry editors
     * page ID to ensure the dialog opens to the right page and doesn't show an
     * unrelated "filter" list on the left.
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String pageId = BrowserUIConstants.PREFERENCEPAGEID_ENTRYEDITORS;
        PreferencesUtil.createPreferenceDialogOn( shell, pageId, new String[]
            { pageId }, null ).open();
    }
}
