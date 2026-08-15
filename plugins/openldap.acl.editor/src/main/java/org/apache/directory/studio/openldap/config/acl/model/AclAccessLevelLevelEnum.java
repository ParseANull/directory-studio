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
package org.apache.directory.studio.openldap.config.acl.model;


// ── CLASS: AclAccessLevelLevelEnum — DEATH STAR CLEARANCE TIERS ──────────────
// Grand Moff Tarkin's access control matrix has eight tiers: from "manage"
// (full administrative control) down to "none" (no access whatsoever). Each
// tier corresponds to exactly one keyword OpenLDAP recognises in an ACL rule.
// This enum encodes those eight tiers and converts them to their wire-format
// strings when we need to write an ACL back to the directory server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The set of named access levels OpenLDAP recognises in a "by" clause — from
 * most-powerful ({@code manage}) to most-restrictive ({@code none}). Each
 * constant serialises to the exact keyword OpenLDAP expects in the ACL text.
 * Think of this enum as the Death Star's security clearance tiers — each rank
 * grants a precisely specified amount of access, and Tarkin's system recognises
 * exactly eight.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclAccessLevelLevelEnum
{
    MANAGE,
    WRITE,
    READ,
    SEARCH,
    COMPARE,
    AUTH,
    DISCLOSE,
    NONE;


    // ── Rendering the Clearance Tier as a Wire-Format Keyword ─────────────────
    // When Tarkin drafts a security directive he doesn't write "WRITE", he
    // writes "write" — lower-case, exactly as the protocol specifies. We do
    // the same when serialising ACL rules back to OpenLDAP's text format.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lower-case keyword that OpenLDAP uses in ACL text for this
     * level. We use this when serialising an {@link AclAccessLevel} to a string
     * — the output goes straight into the ACL rule being built.
     *
     * <p>For example — Tarkin stamping the clearance level onto a directive:</p>
     * <pre>
     *   AclAccessLevelLevelEnum.READ.toString()   // → "read"
     *   AclAccessLevelLevelEnum.MANAGE.toString() // → "manage"
     *   AclAccessLevelLevelEnum.NONE.toString()   // → "none"
     * </pre>
     *
     * @return  The OpenLDAP keyword for this access level.
     */
    public String toString()
    {
        switch ( this )
        {
            case MANAGE:
                return "manage";
            case WRITE:
                return "write";
            case READ:
                return "read";
            case SEARCH:
                return "search";
            case COMPARE:
                return "compare";
            case AUTH:
                return "auth";
            case DISCLOSE:
                return "disclose";
            case NONE:
                return "none";
        }

        return super.toString();
    }
}
