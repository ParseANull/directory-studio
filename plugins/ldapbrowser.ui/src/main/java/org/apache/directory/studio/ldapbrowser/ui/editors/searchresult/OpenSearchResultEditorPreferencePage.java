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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenSearchResultEditorPreferencePage — Palpatine Issues the Standing Order ──
// Palpatine doesn't fight himself — he has aides who escort commanders to the
// order book.  When Palpatine says "show them the configuration", an aide opens
// the correct page of the Imperial rulebook and steps aside.
// This action is that aide: the user clicks "Preferences" and we open the right
// preference page in Eclipse's preferences dialog.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Action that opens the preference pages for the search result editor.
 * It navigates the user directly to the search result editor and attribute
 * preferences pages, rather than making them hunt through the preferences tree.
 * Think of this as Palpatine's aide who opens the Imperial rulebook to exactly
 * the right page on command.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenSearchResultEditorPreferencePage extends Action
{

    // ── Aide Reports for Duty ─────────────────────────────────────────────────
    // The aide is briefed: they know their role (open the preferences dialog) and
    // are given the correct label and tooltip so the menu item is self-explanatory.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the action with its label and tooltip set from the message bundle.
     * We also enable the action immediately — it should always be available when
     * the search result editor is open.
     */
    public OpenSearchResultEditorPreferencePage()
    {
        super.setText( Messages.getString( "OpenSearchResultEditorPreferencePage.Preferences" ) ); //$NON-NLS-1$
        super.setToolTipText( Messages.getString( "OpenSearchResultEditorPreferencePage.PreferencesToolTip" ) ); //$NON-NLS-1$
        super.setEnabled( true );
    }


    // ── Aide Escorts the User to the Rulebook ────────────────────────────────
    // The aide opens the Imperial rulebook directly to the search result editor
    // preferences page, with the attributes page also available in the navigation.
    // The user doesn't have to know the page ID or navigate the tree themselves.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse preferences dialog pre-navigated to the search result editor
     * preferences page, with the attributes preferences page also included.
     * We use the active shell so the dialog is modal to the current window.
     *
     * <p>For example — Palpatine's aide opens the correct page:</p>
     * <pre>
     *   PreferencesUtil.createPreferenceDialogOn(
     *     shell,
     *     srePageId,         // open this page first
     *     {srePageId, attPageId},  // these pages are also shown
     *     null
     *   ).open();
     * </pre>
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String srePageId = BrowserUIConstants.PREFERENCEPAGEID_SEARCHRESULTEDITOR;
        String attPageId = BrowserUIConstants.PREFERENCEPAGEID_ATTRIBUTES;
        PreferencesUtil.createPreferenceDialogOn( shell, srePageId, new String[]
            { srePageId, attPageId }, null ).open();
    }

}
