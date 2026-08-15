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


// ── CLASS: LdapServerStatus — THE DEATH STAR'S OPERATIONAL STATUS BOARD ─────────────────
// The Death Star's control room has a large status board showing the weapon system's current
// state: fully powered and aimed (STARTED), charging (STARTING), powering down (STOPPING),
// offline (STOPPED), status unknown (UNKNOWN), or undergoing repairs (REPAIRING).
// Every engineer and officer reads from the same board; it drives what actions are available.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The set of possible lifecycle states for an LDAP server instance.
 * The UI (Servers view icons, enabled actions) is driven by this status — e.g., only a
 * STOPPED server can be started, and only a STARTED server can be stopped.
 * Think of it as the Death Star's status board: each value is a distinct operational mode.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum LdapServerStatus
{
    /** The server is fully running and accepting LDAP connections — superlaser at full power. */
    STARTED,

    /** The server is in the process of starting up — superlaser charging. */
    STARTING,

    /** The server is completely offline — station dark. */
    STOPPED,

    /** The server is in the process of shutting down — reactor powering down. */
    STOPPING,

    /** The server's state cannot be determined — sensor malfunction, status unknown. */
    UNKNOWN,

    /** The server is undergoing repair or recovery — engineering crew on-site. */
    REPAIRING
}
