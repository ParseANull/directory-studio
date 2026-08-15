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


import java.util.EventObject;


// ── CLASS: WidgetModifyEvent — REBEL ALLIANCE SENSOR FLARE ───────────────────
// When the Rebel sensor network detects a change — an enemy ship entering the
// system, a door opening in the base — it fires off a signal flare so every
// monitoring station knows something happened.  This class is that flare: a
// lightweight event object that carries a reference to the widget that changed
// and gets broadcast to every registered listener.
// ────────────────────────────────────────────────────────────────────────────
/**
 * We represent a modification event fired by an {@link AbstractWidget} whenever
 * its state changes.  We extend {@link EventObject} so listeners receive a
 * reference to the widget that triggered the event via {@code getSource()}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WidgetModifyEvent extends EventObject
{

    /** The serialVersionUID */
    private static final long serialVersionUID = 2421335730580648878L;


    // ── CONSTRUCTOR WidgetModifyEvent — FIRING THE SENSOR FLARE ─────────────
    // We light the flare and attach it to the sensor that triggered it.
    // The source reference lets any listener ask "which widget changed?" without
    // needing a separate field — it is all in the inherited EventObject.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * We create a new modification event originating from the given source
     * widget.  Listeners can call {@code getSource()} to find out which widget
     * fired this event.
     *
     * @param source the {@link AbstractWidget} whose state just changed
     */
    public WidgetModifyEvent( Object source )
    {
        super( source );
    }
}
