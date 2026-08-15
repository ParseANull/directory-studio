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

package org.apache.directory.studio.templateeditor.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;


// ── CLASS: EntryTemplatePreferencePageAction — PALPATINE'S STANDING ORDER ────────
// Palpatine has standing orders encoded in the Imperial registry: when any officer
// says "preferences", a direct channel opens to the Template Entry Editor settings
// page. No matter who triggers it or from where, the same preference page always
// opens. This action is that standing order — a stateless shortcut that opens the
// Template Entry Editor preference page on demand, wherever it is called from
// (toolbar button, context menu, keyboard shortcut).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Template Entry Editor preferences page inside Eclipse's standard
 * Preferences dialog. Can be triggered from any context: the "Display Entry In"
 * dropdown, the right-click context menu in the editor, etc. The target page is
 * always {@link EntryTemplatePluginConstants#PREF_TEMPLATE_ENTRY_EDITOR_PAGE_ID}.
 * Think of this as Palpatine's standing order that immediately routes the caller
 * to the right settings page.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryTemplatePreferencePageAction extends Action
{

    // ── CONSTRUCTOR: STANDING ORDER ISSUED ────────────────────────────────────────
    // Palpatine signs the standing order: "When 'Preferences' is invoked, open the
    // Template Entry Editor page." We set the label and tooltip here so the action
    // shows up consistently in every menu and toolbar that includes it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures this action with the localized "Preferences" label and tooltip.
     * No parameters needed — the target preference page is hardcoded via the
     * {@link EntryTemplatePluginConstants#PREF_TEMPLATE_ENTRY_EDITOR_PAGE_ID} constant.
     */
    public EntryTemplatePreferencePageAction()
    {
        super.setText( Messages.getString( "EntryTemplatePreferencePageAction.Preferences" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "EntryTemplatePreferencePageAction.Preferences" ) ); //$NON-NLS-1$
    }


    // ── RUN: PALPATINE EXECUTES THE STANDING ORDER ────────────────────────────────
    // The order fires: open the preferences dialog focused on the Template Entry
    // Editor page. We use Eclipse's PreferencesUtil to open the standard Preferences
    // dialog with only this page displayed (the second String[] argument restricts
    // which pages appear in the left tree).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Eclipse Preferences dialog and navigates directly to the Template
     * Entry Editor preference page. The dialog shows only that page in its left
     * navigation tree.
     *
     * <p>For example — Palpatine's standing order fires:</p>
     * <pre>
     *   PreferencesUtil.createPreferenceDialogOn(shell, pageId, new String[]{pageId}, null).open();
     *   // The Preferences dialog opens, already on the Template Entry Editor page.
     * </pre>
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        String pageId = EntryTemplatePluginConstants.PREF_TEMPLATE_ENTRY_EDITOR_PAGE_ID;
        PreferencesUtil.createPreferenceDialogOn( shell, pageId, new String[]
            { pageId }, null ).open();
    }
}
