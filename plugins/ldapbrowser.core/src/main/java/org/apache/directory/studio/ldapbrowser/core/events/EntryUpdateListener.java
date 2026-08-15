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

package org.apache.directory.studio.ldapbrowser.core.events;


import java.util.EventListener;

import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: EntryUpdateListener — OBI-WAN'S VOICE GUIDES LUKE FROM BEYOND ────
// Even after Obi-Wan becomes one with the Force, his voice continues reaching
// Luke at the critical moments: "Run, Luke, run!", "Use the Force, Luke."
// Obi-Wan doesn't need to be physically present; whenever something important
// happens he gets the message through and Luke responds.
// This interface is that same open channel: any component that cares about
// changes to LDAP entries implements it, registers with {@link EventRegistry},
// and its single callback method is invoked every time any
// {@link EntryModificationEvent} fires — attributes added, entries deleted,
// values renamed, bulk changes, whatever.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for receiving all {@link EntryModificationEvent}s.
 * A single method handles every concrete event subtype — the listener
 * uses {@code instanceof} checks to distinguish what actually happened.
 * Register with {@link EventRegistry#addEntryUpdateListener} to start receiving
 * events; deregister with {@link EventRegistry#removeEntryUpdateListener} when
 * done (e.g. when a view is closed).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EntryUpdateListener extends EventListener
{
    // ── Obi-Wan's Voice Arrives With The Event Details ───────────────────────────
    // "Luke — something changed.  An attribute was added.  Go look."  The voice
    // tells Luke just enough: which entry, which kind of event.  Luke (the
    // implementing class) decides what to do — refresh the attribute table,
    // remove a tree node, show a toast notification, whatever.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event framework whenever any LDAP entry modification event
     * fires.  Use {@code instanceof} to determine the concrete event type:
     *
     * <p>For example — a tree viewer responding to entry changes:</p>
     * <pre>
     *   public void entryUpdated(EntryModificationEvent event) {
     *     if (event instanceof EntryDeletedEvent) {
     *       viewer.remove(((EntryDeletedEvent) event).getModifiedEntry());
     *     } else if (event instanceof AttributeAddedEvent) {
     *       viewer.refresh(event.getModifiedEntry());
     *     }
     *     // ... and so on
     *   }
     * </pre>
     *
     * @param event the modification event.  Cast to the appropriate concrete
     *              type to access event-specific getters (e.g.
     *              {@link AttributeAddedEvent#getAddedAttribute()}).
     */
    void entryUpdated( EntryModificationEvent event );
}
