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
package org.apache.directory.studio.ldapservers;


import org.apache.directory.studio.ldapservers.model.LdapServer;


// ── CLASS: LdapServersManagerListener — LANDO'S CLOUD CITY INTERCOM NETWORK ───────────────
// Cloud City's departments subscribe to Lando's intercom to hear about changes to the
// landing grid — ships arriving, departing, or being reconfigured on the fly.
// Any component that needs to react to server list changes (the Servers view, toolbar actions,
// etc.) implements this interface and registers with {@link LdapServersManager}.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for components that care about changes to the global server registry.
 * Implement this and register via {@link LdapServersManager#addListener} to receive
 * notifications when servers are added, removed, or updated.
 * Think of it as Cloud City's intercom subscription — plug in and hear every landing-pad event.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface LdapServersManagerListener
{
    // ── Lando Announces A New Arrival ───────────────────────────────────────────────────────
    // The intercom crackles: "Attention Cloud City — a new vessel has docked at pad 7."
    // Every department that cares about new arrivals gets this call so they can update their
    // displays, enable their actions, or run any other arrival-triggered logic.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by {@link LdapServersManager} immediately after a server has been added to the registry.
     * Implementors should refresh any UI or internal state that depends on the server list.
     *
     * <p>For example — Lando broadcasts a new docking:</p>
     * <pre>
     *   Intercom: "ApacheDS-Local has joined the grid."
     *   ServersView.serverAdded(server) → refreshes the table to show the new row.
     * </pre>
     *
     * @param server  the server that was just added — never null
     */
    void serverAdded( LdapServer server );


    // ── Lando Announces A Departure ─────────────────────────────────────────────────────────
    // The intercom crackles: "Pad 7 is now vacated — vessel 'ApacheDS-Local' has left the grid."
    // Every subscribed department reacts: clear the display row, disable relevant actions.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by {@link LdapServersManager} immediately after a server has been removed from the registry.
     * Implementors should clean up any UI elements or resources tied to this specific server.
     *
     * <p>For example — Lando broadcasts a departure:</p>
     * <pre>
     *   Intercom: "ApacheDS-Local has left the grid."
     *   ServersView.serverRemoved(server) → removes the row from the table.
     * </pre>
     *
     * @param server  the server that was just removed — never null
     */
    void serverRemoved( LdapServer server );


    // ── Lando Broadcasts A Configuration Change ─────────────────────────────────────────────
    // The intercom crackles: "Pad 7 vessel status update — 'ApacheDS-Local' is now running."
    // Subscribed systems refresh their status indicators so the board stays current.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by {@link LdapServersManager} (or by the server's adapter) whenever a server's
     * properties — name, status, configuration — have changed.
     * Implementors should re-read the server's current state and refresh any affected displays.
     *
     * <p>For example — Lando broadcasts a status update:</p>
     * <pre>
     *   Intercom: "ApacheDS-Local is now STARTED."
     *   ServersView.serverUpdated(server) → refreshes that row's icon and label.
     * </pre>
     *
     * @param server  the server whose state was just updated — never null
     */
    void serverUpdated( LdapServer server );
}
