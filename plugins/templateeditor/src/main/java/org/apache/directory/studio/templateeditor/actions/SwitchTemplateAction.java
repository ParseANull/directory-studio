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


import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.eclipse.jface.action.Action;

import org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget;
import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: SwitchTemplateAction — CLONE TROOPER EXECUTING AN EXACT FORMATION CHANGE ─
// In the Clone Wars, when the order "switch to Diamond Formation" comes down,
// clone troopers snap to it instantly — no deliberation, no hesitation. This action
// is that order: it is bound to one specific template, and when it fires it switches
// the editor widget to display the entry using that template. It also notifies the
// editor itself (if the editor implements {@link SwitchTemplateListener}) so the
// editor can react (e.g. updating its tab title or history location).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A checkable {@link Action} that switches the {@link TemplateEditorWidget} to
 * display the current LDAP entry using a specific {@link Template}. Created by
 * {@link DisplayEntryInTemplateMenuManager} — one instance per available template
 * in the chooser dropdown. When triggered it calls
 * {@link TemplateEditorWidget#switchTemplate(Template)} and, if the editor
 * implements {@link SwitchTemplateListener}, notifies the editor as well.
 * Think of this as a clone trooper executing an exact formation change order.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SwitchTemplateAction extends Action
{
    /** The template editor widget */
    private TemplateEditorWidget templateEditorWidget;

    /** The template */
    private Template template;


    // ── CONSTRUCTOR: TROOPER BRIEFED ON THE NEW FORMATION ────────────────────────
    // The trooper is told exactly which formation to execute — the template — and
    // which widget to perform the switch on. The action label comes from the
    // template's title so the menu item reads "User Account" or "Staff Record".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action, using the template's title as the menu item label.
     * The action style is {@link Action#AS_CHECK_BOX} so the currently active
     * template appears checked in the chooser menu.
     *
     * <p>For example — the trooper is briefed on the new formation:</p>
     * <pre>
     *   new SwitchTemplateAction(editorWidget, userAccountTemplate);
     *   // Menu item label: "User Account" (with a checkmark if it's active)
     * </pre>
     *
     * @param templateEditorWidget  the editor widget to switch
     * @param template              the template this action will activate
     */
    public SwitchTemplateAction( TemplateEditorWidget templateEditorWidget, Template template )
    {
        super( template.getTitle(), Action.AS_CHECK_BOX );

        this.templateEditorWidget = templateEditorWidget;
        this.template = template;
    }


    // ── RUN: TROOPER EXECUTES THE FORMATION CHANGE ───────────────────────────────
    // The order fires: switch to the new template. We tell the widget to rebuild
    // its form using this template's definition, then inform the editor (if it
    // listens) that the switch happened so it can update its state accordingly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Switches the editor widget to display the entry through this action's bound
     * template. Also calls {@link SwitchTemplateListener#templateSwitched} on the
     * editor if the editor implements that interface, so the editor can react to the
     * change (e.g. updating navigation history).
     *
     * <p>For example — the trooper executes the formation change:</p>
     * <pre>
     *   templateEditorWidget.switchTemplate(template);
     *   // "Formation changed to User Account. Editor notified."
     * </pre>
     */
    public void run()
    {
        if ( ( templateEditorWidget != null ) && ( template != null ) )
        {
            // Switching the template
            templateEditorWidget.switchTemplate( template );

            // Getting the associated editor
            IEntryEditor editor = templateEditorWidget.getEditor();
            if ( editor instanceof SwitchTemplateListener )
            {
                // Calling the listener
                ( ( SwitchTemplateListener ) editor ).templateSwitched( templateEditorWidget, template );
            }
        }
    }
}
