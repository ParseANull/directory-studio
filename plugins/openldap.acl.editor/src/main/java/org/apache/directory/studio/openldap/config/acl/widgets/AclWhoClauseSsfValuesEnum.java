/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.widgets;


// ── CLASS: AclWhoClauseSsfValuesEnum — IMPERIAL SECURITY ENCRYPTION TIERS ────
// When the Imperial Security Bureau transmits classified data it requires a
// minimum encryption strength on the channel. Tarkin's clearance system defines
// common strength levels by name — ANY (no minimum), 40-bit, 56-bit, 64-bit,
// 128-bit, 164-bit, 256-bit — plus a CUSTOM option so the officer can dial in
// an arbitrary number. The SSF composite widget maps each of these enum values
// to a spinner setting and a human-readable label in the dropdown.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Enumeration of the preset security strength factor (SSF) levels used by the
 * SSF who-clause composite widgets. Each value maps to a common bit-strength
 * (or "any strength" / "custom") that the user can select from a dropdown.
 *
 * <p>Think of this enum as Tarkin's encryption tier list — predefined signal
 * security levels, plus a custom slot for non-standard requirements.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclWhoClauseSsfValuesEnum
{
    /** No minimum strength required — any connection is accepted. */
    ANY,

    /** 40-bit encryption strength. */
    FORTY,

    /** 56-bit encryption strength (DES equivalent). */
    FIFTY_SIX,

    /** 64-bit encryption strength. */
    SIXTY_FOUR,

    /** 128-bit encryption strength (standard TLS). */
    ONE_TWENTY_HEIGHT,

    /** 164-bit encryption strength. */
    ONE_SIXTY_FOUR,

    /** 256-bit encryption strength (AES-256 equivalent). */
    TWO_FIFTY_SIX,

    /** Custom strength — the officer types an arbitrary number into the spinner. */
    CUSTOM
}
