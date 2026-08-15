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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.PlatformUI;

import org.apache.directory.studio.templateeditor.EntryTemplatePlugin;
import org.apache.directory.studio.templateeditor.EntryTemplatePluginConstants;
import org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget;


// ── CLASS: DisplayEntryInTemplateAction — CLONE TROOPER RAISING THE SHIELD ───────
// In Episode III, a clone trooper executes Order 66 at a precise moment: one
// signal, one coordinated action across the entire operation. This action is that
// single click on the toolbar — it receives the command (the user clicks the button)
// and immediately executes a precise, coordinated response: build the template menu
// and make it visible. It doesn't deliberate; it just acts.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Toolbar drop-down action that shows the "Display Entry In" template-chooser menu
 * when the user clicks the switch-template button in the entry editor toolbar.
 * On activation it delegates to {@link DisplayEntryInTemplateMenuManager} to build
 * the live menu of available templates, then pops it up over the workbench shell.
 * Think of this as the clone trooper receiving the order and executing immediately.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DisplayEntryInTemplateAction extends Action
{
    /** The associated {@link TemplateEditorWidget} */
    private TemplateEditorWidget templateEditorPage;


    // ── CONSTRUCTOR: TROOPER RECEIVES HIS ASSIGNMENT ──────────────────────────────
    // The clone trooper is briefed: "You're on toolbar duty for this editor widget.
    // When the user clicks the switch-template button, you pop up the menu."
    // We configure the action label, drop-down style, and icon here so the
    // toolbar renders it correctly from the start.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action bound to the given editor widget. Configures it as a
     * drop-down action with the switch-template icon so Eclipse renders an arrow
     * on the toolbar button.
     *
     * <p>For example — the trooper receives his assignment:</p>
     * <pre>
     *   new DisplayEntryInTemplateAction(templateEditorWidget);
     *   // "You're on toolbar duty. Pop up the template menu on click."
     * </pre>
     *
     * @param templateEditorPage  the editor widget whose templates this action
     *                            will present in the chooser menu
     */
    public DisplayEntryInTemplateAction( TemplateEditorWidget templateEditorPage )
    {
        super( Messages.getString( "DisplayEntryInTemplateAction.DisplayEntryIn" ), Action.AS_DROP_DOWN_MENU ); //$NON-NLS-1$
        setImageDescriptor( EntryTemplatePlugin.getDefault().getImageDescriptor(
            EntryTemplatePluginConstants.IMG_SWITCH_TEMPLATE ) );
        this.templateEditorPage = templateEditorPage;
    }


    // ── RUN: TROOPER EXECUTES THE ORDER — DEPLOY THE MENU ────────────────────────
    // The signal comes in: the user clicked the button. The trooper doesn't ask
    // questions — he builds the menu manager, creates a context menu on the shell,
    // and sets it visible. One precise, immediate action.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds a fresh {@link MenuManager}, fills it with the available templates via
     * {@link DisplayEntryInTemplateMenuManager#fillInMenuManager}, creates a context
     * menu on the active shell, and makes it visible. Called by Eclipse when the
     * user clicks the toolbar button.
     *
     * <p>For example — the trooper deploys the menu:</p>
     * <pre>
     *   menuManager.createContextMenu(shell);
     *   menuManager.getMenu().setVisible(true);
     *   // The template chooser pops up instantly.
     * </pre>
     */
    public void run()
    {
        MenuManager menuManager = new MenuManager();
        DisplayEntryInTemplateMenuManager.fillInMenuManager( menuManager, templateEditorPage );

        menuManager.createContextMenu( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        menuManager.getMenu().setVisible( true );
    }


    // ── IS ENABLED: TROOPER IS ALWAYS READY ──────────────────────────────────────
    // Unlike some actions that check preconditions, this clone trooper is always
    // on duty — the action is always enabled. The menu itself handles the case
    // where no templates are available (it shows a disabled "No template" item).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code true} — the action is permanently enabled. The menu
     * it produces handles the "no templates available" case gracefully with a
     * disabled placeholder item.
     *
     * @return {@code true} always
     */
    public boolean isEnabled()
    {
        return true;
    }
}
