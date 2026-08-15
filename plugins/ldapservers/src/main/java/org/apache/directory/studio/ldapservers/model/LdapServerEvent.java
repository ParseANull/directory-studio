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
package org.apache.directory.studio.ldapservers.model;


// ── CLASS: LdapServerEvent — R2-D2 BEEPS AN URGENT STATUS MESSAGE ─────────────────────────
// R2-D2 bleeps and bloops — but each signal carries two pieces of data: which ship it's
// about ("the X-wing on pad 3") and what happened ("engine power just changed").
// This is a simple value object: it bundles a reference to the affected server with the
// type of event ({@link LdapServerEventType}) so listeners know exactly what changed.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A value object that carries information about a change to a specific {@link LdapServer}.
 * Instances are created by {@link LdapServer} when its name or status changes, and are
 * passed to every registered {@link LdapServerListener}.
 * Think of it as R2-D2's two-part alert: "ship X" + "what changed."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapServerEvent
{
    /** The server */
    private LdapServer server;

    /** The kind of event */
    private LdapServerEventType kind;


    // ── R2-D2 Sends A Two-Part Alert ────────────────────────────────────────────────────────
    // R2-D2 beeps two values simultaneously — the target ship and the nature of the alert.
    // We capture both in the constructor so the event object is always complete and immutable
    // in practice (setters exist for flexibility but the constructor is the primary builder).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fully populated server event with the affected server and event type.
     * Listeners receive this object in their {@link LdapServerListener#serverChanged} callback.
     *
     * @param server  the server that changed — never null in practice
     * @param kind    what kind of change happened (RENAMED or STATUS_CHANGED)
     */
    public LdapServerEvent( LdapServer server, LdapServerEventType kind )
    {
        super();
        this.server = server;
        this.kind = kind;
    }


    // ── R2-D2 Identifies The Ship ────────────────────────────────────────────────────────────
    // "Bweep bloop" — R2 identifies pad 3's X-wing as the source of the alert.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server that generated this event.
     *
     * @return the affected {@link LdapServer} — never null in normal usage
     */
    public LdapServer getServer()
    {
        return server;
    }


    // ── R2-D2 Updates The Ship Reference ─────────────────────────────────────────────────────
    // If the message needs to be re-targeted to a different ship, R2-D2 updates his bleet.
    // In practice this setter is rarely used — the constructor sets the server definitively.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the server reference in this event.
     *
     * @param server  the new server reference
     */
    public void setServer( LdapServer server )
    {
        this.server = server;
    }


    // ── R2-D2 Identifies The Nature Of The Alert ────────────────────────────────────────────
    // "Beep bwoop" — R2 indicates the alert type: engine power changed, not a hull breach.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the type of change that occurred.
     *
     * @return the {@link LdapServerEventType} — either RENAMED or STATUS_CHANGED
     */
    public LdapServerEventType getKind()
    {
        return kind;
    }


    // ── R2-D2 Corrects The Alert Type ────────────────────────────────────────────────────────
    // R2-D2 realizes he sent the wrong alert code and corrects it mid-transmission.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the event type in this event.
     *
     * @param kind  the new {@link LdapServerEventType}
     */
    public void setKind( LdapServerEventType kind )
    {
        this.kind = kind;
    }
}
