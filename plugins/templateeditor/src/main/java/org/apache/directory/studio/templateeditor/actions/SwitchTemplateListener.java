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


import org.apache.directory.studio.templateeditor.editor.TemplateEditorWidget;
import org.apache.directory.studio.templateeditor.model.Template;


// ── CLASS: SwitchTemplateListener — CLONE TROOPER LISTENING FOR THE SWITCH SIGNAL ─
// When the order "switch formation" is broadcast over the Clone comm network, only
// the troopers who are tuned to that channel react. Any editor class that cares about
// template switches implements this interface and gets called the moment the user
// picks a different template. This keeps the action decoupled from the editor —
// the action does the switch, then checks if anyone's listening, and calls them.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Observer interface for template-switch events triggered by {@link SwitchTemplateAction}.
 * Implement this on an entry editor class to receive a callback whenever the user
 * selects a different template from the "Display Entry In" chooser. This lets the
 * editor react (e.g. marking a new navigation history location) without the action
 * needing to know anything about the editor's internals.
 * Think of this as a clone trooper tuned to the switch-formation comm channel.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface SwitchTemplateListener
{
    // ── TEMPLATE SWITCHED: FORMATION CHANGE CONFIRMED ────────────────────────────
    // The signal confirms that the formation change has been executed. The listener
    // gets both the widget (which now shows the new template) and the template
    // itself (so it knows which one was chosen) — enough information to do whatever
    // post-switch bookkeeping it needs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by {@link SwitchTemplateAction#run()} immediately after the editor
     * widget has been switched to the new template. Use this callback to update
     * navigation history, refresh titles, or do any other post-switch work.
     *
     * <p>For example — the formation change is confirmed:</p>
     * <pre>
     *   void templateSwitched(widget, template) {
     *     getSite().getPage().getNavigationHistory().markLocation(this);
     *     // "Navigation history updated — new template recorded."
     *   }
     * </pre>
     *
     * @param templateEditorWidget  the widget that just switched to the new template
     * @param template              the template that is now displayed
     */
    void templateSwitched( TemplateEditorWidget templateEditorWidget, Template template );
}
