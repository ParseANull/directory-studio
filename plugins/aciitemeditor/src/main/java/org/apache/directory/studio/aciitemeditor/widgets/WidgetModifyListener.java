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
package org.apache.directory.studio.aciitemeditor.widgets;


// ── CLASS: WidgetModifyListener — THE GRAND MOFF'S SENSOR CALLBACK ─────────────
// Grand Moff Tarkin has sensors tied to every instrument panel; when one
// changes he is notified instantly so he can update his situational display.
// WidgetModifyListener is the callback interface those sensors expose.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for objects that want to be notified when an ACI editor
 * widget changes its value.
 * Typically implemented by parent composites (e.g. the tab folder) that need
 * to synchronise their state or enable/disable OK buttons when a child widget
 * is edited.
 * Think of this as the Grand Moff's sensor callback: when any instrument panel
 * on the bridge fires, the Grand Moff is told immediately.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface WidgetModifyListener
{

    // ── RECEIVE THE PANEL CHANGE SIGNAL ───────────────────────────────────────
    // The Grand Moff's display updates the moment a panel fires: he checks
    // {@code event.getSource()} to identify the panel and reacts accordingly.
    // Implementors do the same — inspect the event, then refresh whatever state
    // depends on the changed widget.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when the widget identified by {@code event.getSource()} has been modified.
     * Implementations should react by re-reading the widget's value and updating
     * dependent UI state (e.g. re-validating the ACI string, toggling buttons).
     *
     * <p>For example — the tab folder re-enables the OK button:</p>
     * <pre>
     *   public void widgetModified(WidgetModifyEvent event) {
     *     okButton.setEnabled(isInputValid());
     *   }
     * </pre>
     *
     * @param event  the modification event containing a reference to the changed widget
     */
    void widgetModified( WidgetModifyEvent event );
}
