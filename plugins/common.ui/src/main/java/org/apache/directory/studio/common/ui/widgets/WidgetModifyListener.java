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

package org.apache.directory.studio.common.ui.widgets;


// ── CLASS: WidgetModifyListener — REBEL ALLIANCE SENSOR MONITORING STATION ───
// Every Rebel sensor monitoring station keeps a standing watch for incoming
// signals.  When the sensor relay (AbstractWidget) fires a flare
// (WidgetModifyEvent), every registered monitoring station reacts.
// Implementing this interface means you are a station ready to respond whenever
// the watched widget tells you something has changed.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We define the callback contract for objects that want to be notified when an
 * {@link AbstractWidget} is modified.  Register an implementation via
 * {@link AbstractWidget#addWidgetModifyListener} and we will call
 * {@link #widgetModified} every time the widget's state changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface WidgetModifyListener
{
    // ── METHOD widgetModified — RECEIVING THE SENSOR FLARE ───────────────────
    // The monitoring station receives the flare and springs into action.
    // Implementations react to the change encoded in the event — for instance
    // by validating input, enabling buttons, or refreshing a display.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We are called whenever the observed {@link AbstractWidget} changes its
     * state.  The event's {@code getSource()} method returns the widget that
     * fired the notification.
     *
     * @param event the modification event carrying a reference to the changed widget
     */
    void widgetModified( WidgetModifyEvent event );
}
