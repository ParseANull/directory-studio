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


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: BrowserConnectionUpdateEvent — HAN SOLO JUMPS TO HYPERSPACE ──────
// Han Solo pushes the hyperdrive lever and the Millennium Falcon blasts into
// hyperspace — the ship transitions states dramatically: it was docked, now
// it's flying; it was in-system, now it's in transit; it was flying, now it
// drops back to realspace at the target.  Everyone on the bridge knows which
// phase just happened.
// This event fires whenever an {@link IBrowserConnection}'s lifecycle changes —
// opened (we connected to the LDAP server), closed (we disconnected), or the
// schema was refreshed.  Listeners know which connection and exactly what
// phase-transition just happened so they can update the UI.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an {@link IBrowserConnection}'s lifecycle state changed.
 * Fired by the connection management layer whenever a browser connection is
 * opened, closed, or has its schema reloaded.  Listeners — typically the
 * Connections view and the LDAP Browser view — respond by refreshing the
 * connection node's icon and available actions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserConnectionUpdateEvent
{

    /**
     * The phase-transition codes for a browser connection's lifecycle.
     * Think of these as the three drive states of the Millennium Falcon:
     * sub-light (closed), hyperspace jump (opened), and shield recalibration
     * (schema updated).
     */
    public enum Detail
    {
        /** Indicates that the browser connection was opened. */
        BROWSER_CONNECTION_OPENED,

        /** Indicates that the browser connection was closed. */
        BROWSER_CONNECTION_CLOSED,

        /** Indicates that the schema was updated. */
        SCHEMA_UPDATED
    }

    /** The event detail. */
    private Detail detail;

    /** The updated browser connection. */
    private IBrowserConnection browserConnection;


    // ── Han Records Which Ship And Which Drive Phase ──────────────────────────────
    // "Falcon made the jump to hyperspace — connection: Tatooine run."
    // Both the ship identity and the drive phase are logged together so the
    // co-pilot (the listener) has everything in one event object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BrowserConnectionUpdateEvent.
     *
     * <p>For example — fired when a connection is opened:</p>
     * <pre>
     *   new BrowserConnectionUpdateEvent(myConn, Detail.BROWSER_CONNECTION_OPENED);
     * </pre>
     *
     * @param browserConnection the connection whose state changed.
     * @param detail            what happened (OPENED, CLOSED, or SCHEMA_UPDATED).
     */
    public BrowserConnectionUpdateEvent( IBrowserConnection browserConnection, Detail detail )
    {
        this.browserConnection = browserConnection;
        this.detail = detail;
    }


    // ── Han Hands Over The Ship's ID Badge ───────────────────────────────────────
    // "Which ship made the jump?" — the co-pilot needs to update the right row
    // in the fleet display.  This returns the connection object so listeners can
    // match it against their own references.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection whose state changed.
     * Listeners compare this against their registered connections to decide
     * whether to handle the event.
     *
     * @return the {@link IBrowserConnection}; never {@code null}.
     */
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    // ── Han Reads The Drive-Phase Indicator ──────────────────────────────────────
    // "Hyperspace jump — OPENED."  The detail tells the listener exactly what
    // changed so it can update the icon, menu items, or cached data appropriately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Detail} constant indicating what changed on the connection.
     * Listeners typically switch on this value:
     * <ul>
     *   <li>{@code BROWSER_CONNECTION_OPENED} — show connected icon, enable actions.</li>
     *   <li>{@code BROWSER_CONNECTION_CLOSED} — show disconnected icon, disable actions.</li>
     *   <li>{@code SCHEMA_UPDATED} — refresh schema-dependent UI (auto-complete, etc.).</li>
     * </ul>
     *
     * @return the event detail; never {@code null}.
     */
    public Detail getDetail()
    {
        return detail;
    }

}
