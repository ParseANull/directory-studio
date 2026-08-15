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


// ── CLASS: LdapServerEventType — R2-D2'S ALARM VOCABULARY ────────────────────────────────
// R2-D2 has a limited but precise alarm vocabulary: one beep means "the ship's name changed",
// two beeps means "the engine status changed."  You don't need a lot of codes — just
// enough to tell listeners what kind of event they're dealing with.
// This enum is that vocabulary: two values covering the only two things that change on a
// live server that listeners need to react to immediately.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The two possible types of event that a {@link LdapServer} fires to its
 * {@link LdapServerListener}s.
 * <ul>
 *   <li>{@link #RENAMED} — the server's display name was changed by the user.</li>
 *   <li>{@link #STATUS_CHANGED} — the server's lifecycle status changed (e.g., STOPPED → STARTING).</li>
 * </ul>
 * Think of it as R2-D2's alarm codes: two distinct beeps for two distinct events.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum LdapServerEventType
{
    /** The server's human-readable name changed — update the UI label. */
    RENAMED,

    /** The server's lifecycle status changed — update the icon and action enablement. */
    STATUS_CHANGED
}
