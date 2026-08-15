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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenEntryEditorPreferencePageAction — PALPATINE OPENS THE ORDER BOOK
// Senator Palpatine doesn't just issue one-off orders — he has a standing rulebook
// that governs all behavior across the Empire. When a commander needs to understand
// or change the rules, they're directed to the order book (the preference page).
// OpenEntryEditorPreferencePageAction is the orderly that escorts commanders to
// that order book: it opens the Eclipse preference dialog directly to the entry
// editor page so users can adjust all the editor's standing configuration.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An action that opens the Eclipse Preferences dialog directly to the entry editor's preference page.
 * Instead of making users navigate the full preference tree, this action jumps straight
 * to the right page (and also shows the Attributes page as a related tab).
 * Think of this as Palpatine's orderly: one command, you're at the right page of the order book.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEntryEditorPreferencePageAction extends Action
{

    // ── THE ORDERLY TAKES HIS POST ────────────────────────────────────────────
    // The orderly stands at the door of Palpatine's strategy chamber, ready to
    // escort commanders to the order book whenever they need to consult the rules.
    // We set the label and tooltip so the menu item reads clearly, and mark the
    // action as always enabled since the preferences dialog is always available.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action with the localized "Preferences..." label and tooltip.
     * This action is always enabled — the preferences dialog can always be opened.
     */
    public OpenEntryEditorPreferencePageAction()
    {
        setText( Messages.getString( "OpenEntryEditorPreferencePageAction.Preferences" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenEntryEditorPreferencePageAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── THE ORDERLY ESCORTS THE COMMANDER TO THE ORDER BOOK ──────────────────
    // The orderly opens the heavy doors of Palpatine's order chamber and
    // guides the commander directly to the relevant section of the rulebook —
    // entry editor configuration first, attribute settings close at hand.
    // We open Eclipse's preference dialog pre-filtered to the entry editor page,
    // with the attributes page also available in the same dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse Preferences dialog, jumping directly to the entry editor preference page.
     * We also include the Attributes preference page as a related page so users can
     * configure attribute display settings in the same session without reopening preferences.
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String eePageId = BrowserUIConstants.PREFERENCEPAGEID_ENTRYEDITOR;
        String attPageId = BrowserUIConstants.PREFERENCEPAGEID_ATTRIBUTES;
        PreferencesUtil.createPreferenceDialogOn( shell, eePageId, new String[]
            { eePageId, attPageId }, null ).open();
    }

}
