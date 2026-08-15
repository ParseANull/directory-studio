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


// ── CLASS: LdapServerListener — MON MOTHMA LISTENING TO FIELD REPORTS ────────────────────
// Mon Mothma sits at the centre of the Rebel Alliance's command and listens to every field
// report: "X-wing is now airborne", "status changed — it's been hit."
// Any component that needs to react to a specific server's own changes (not the list-level
// changes tracked by {@link org.apache.directory.studio.ldapservers.LdapServersManagerListener})
// implements this interface and registers via {@link LdapServer#addListener}.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for components that track the lifecycle events of a specific
 * {@link LdapServer} instance — its renames and status changes.
 * Register via {@link LdapServer#addListener}; unregister via {@link LdapServer#removeListener}.
 * Think of it as Mon Mothma's ear — wired directly to one specific ship.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface LdapServerListener
{
    // ── Mon Mothma Receives A Field Report ───────────────────────────────────────────────────
    // A field report arrives: "X-wing engine status is now STARTED" or "the ship was renamed."
    // Mon Mothma reads the event type and reacts accordingly.
    // The event carries both the server reference and the type of change that occurred.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by a {@link LdapServer} whenever its name or status changes.
     * Inspect {@link LdapServerEvent#getKind()} to determine whether it was a rename or
     * a status change, then react accordingly (e.g., refresh the UI label or icon).
     *
     * <p>For example — Mon Mothma acts on a field report:</p>
     * <pre>
     *   event.getKind() == STATUS_CHANGED → update the server's icon in the Servers view.
     *   event.getKind() == RENAMED        → update the server's label text.
     * </pre>
     *
     * @param event  the event describing what changed and which server changed it
     */
    void serverChanged( LdapServerEvent event );
}
