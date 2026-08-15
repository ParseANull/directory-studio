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

import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: BrowserConnectionUpdateListener — MON MOTHMA TRACKS THE REBEL FLEET
// Mon Mothma stands at the Rebel Alliance's central war table, watching every
// ship's status indicator.  The moment the Millennium Falcon's blip changes —
// "hyperspace jump initiated", "arrived at Endor" — she sees it and updates
// her strategy.  She doesn't ask for status; the table tells her automatically.
// This interface is that war table notification contract: any component that
// needs to know when an LDAP browser connection opens, closes, or refreshes
// its schema implements this and gets called the moment it happens.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for receiving {@link BrowserConnectionUpdateEvent}s.
 * Implement this and register with
 * {@link EventRegistry#addBrowserConnectionUpdateListener} to be notified
 * whenever an {@link IBrowserConnection} is opened, closed, or has its schema
 * reloaded.  The callback arrives on the thread managed by the registered
 * {@link org.apache.directory.studio.connection.core.event.EventRunner}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface BrowserConnectionUpdateListener extends EventListener
{
    // ── Mon Mothma Receives The Fleet Status Update ───────────────────────────────
    // The war-table blip changes state and Mon Mothma immediately reads the
    // signal: which ship, and what happened.  She updates her strategy board.
    // Every implementing class decides what to do: the Connection view changes
    // the icon, the Browser view clears or populates its tree, and so on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event framework when a browser connection's lifecycle state
     * changes.  Check {@link BrowserConnectionUpdateEvent#getDetail()} to see
     * what happened and {@link BrowserConnectionUpdateEvent#getBrowserConnection()}
     * for the affected connection.
     *
     * <p>For example — a Connections view updating connection icons:</p>
     * <pre>
     *   public void browserConnectionUpdated(BrowserConnectionUpdateEvent ev) {
     *     viewer.refresh(ev.getBrowserConnection());
     *   }
     * </pre>
     *
     * @param browserConnectionUpdateEvent the event carrying the affected
     *                                     connection and the change detail.
     */
    void browserConnectionUpdated( BrowserConnectionUpdateEvent browserConnectionUpdateEvent );
}
