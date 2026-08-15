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


import java.util.EventObject;


// ── CLASS: WidgetModifyEvent — INSTRUMENT PANEL CHANGE ALERT ─────────────────
// On the Death Star command bridge, whenever an officer flips a switch on an
// instrument panel, an alert packet travels up to the Grand Moff's console so
// he knows exactly which panel fired the change.
// WidgetModifyEvent is that alert packet.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Event fired by an ACI editor widget whenever its value changes.
 * Carries a reference to the widget (the {@code source}) so that
 * {@link WidgetModifyListener} implementations know which composite
 * triggered the modification.
 * Think of this class as the instrument-panel change alert on the Death Star:
 * it tells the Grand Moff's console exactly which panel just moved.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class WidgetModifyEvent extends EventObject
{
    /** Serialization UUID */
    private static final long serialVersionUID = 2421335730580648878L;


    // ── PACKAGE THE ALERT ─────────────────────────────────────────────────────
    // The instrument panel bundles its own identity into the alert packet and
    // dispatches it up the command chain so no one has to guess who fired.
    // We call {@code super(source)} and the EventObject base class handles storage.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code WidgetModifyEvent} for the given source widget.
     * The {@code source} is typically the composite that was modified —
     * listeners can retrieve it via {@link #getSource()}.
     *
     * <p>For example — a protected-items composite notifies its parent:</p>
     * <pre>
     *   fireWidgetModified();  // internally creates new WidgetModifyEvent(this)
     *   // listeners receive the event and know protectedItemsComposite changed
     * </pre>
     *
     * @param source  the widget object on which the modification occurred; must not be {@code null}
     */
    public WidgetModifyEvent( Object source )
    {
        super( source );
    }
}
