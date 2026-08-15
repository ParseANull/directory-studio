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

package org.apache.directory.studio.schemaeditor.controller.actions;


import org.apache.directory.studio.schemaeditor.view.preferences.HierarchyViewPreferencePage;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: OpenHierarchyViewPreferencesAction — Palpatine Issues Order 66 ────
// In the Senate chamber, Palpatine leans forward and transmits the encrypted
// order that reconfigures how every clone trooper in the galaxy behaves from
// that moment on — one command, system-wide effect.
// Here, when the user clicks "Preferences," we open the Hierarchy View
// preference page, the single control panel that reconfigures how the type
// hierarchy is displayed across the whole session.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the Eclipse preference page for the Hierarchy View.
 * This action is wired to the "Preferences..." menu item in the Hierarchy View's
 * toolbar so the user can tweak display settings without hunting through the
 * global Preferences dialog.
 * Think of this as Palpatine transmitting Order 66: one click opens the command
 * center where you reshape how the hierarchy behaves system-wide.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenHierarchyViewPreferencesAction extends Action
{
    // ── Palpatine's Hologram Emitter Is Configured ────────────────────────────────
    // Before the Emperor can transmit Order 66, his holographic comm unit must be
    // powered up, labeled, and set to the right frequency.
    // Our constructor does the same: sets the menu label, the tooltip, and enables
    // the action immediately — unlike most schema actions, preferences are always
    // accessible, even with no project open.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of OpenHierarchyViewPreferencesAction and configures its label and tooltip.
     * We enable it immediately because opening preferences doesn't require a project to be loaded.
     */
    public OpenHierarchyViewPreferencesAction()
    {
        super( Messages.getString( "OpenHierarchyViewPreferencesAction.PreferencesAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenHierarchyViewPreferencesAction.PreferencesToolTip" ) ); //$NON-NLS-1$
        setEnabled( true );
    }


    // ── Order 66 Transmitted To All Commanders ────────────────────────────────────
    // Palpatine's encrypted message propagates across the holonet — every clone
    // commander receives it simultaneously and the configuration snaps into place.
    // We grab the active shell (the window currently in focus) and use Eclipse's
    // PreferencesUtil to open the Hierarchy View preference page directly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the Hierarchy View preference dialog, navigating directly to our page.
     * We use {@link PreferencesUtil#createPreferenceDialogOn} with the page ID so
     * only our page is visible — no need to wade through the full preferences tree.
     */
    public void run()
    {
        Shell shell = Display.getCurrent().getActiveShell();
        PreferencesUtil.createPreferenceDialogOn( shell, HierarchyViewPreferencePage.ID, new String[]
            { HierarchyViewPreferencePage.ID }, null ).open();
    }
}
