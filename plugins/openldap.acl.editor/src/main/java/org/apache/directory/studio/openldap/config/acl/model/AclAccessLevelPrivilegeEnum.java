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


// ── CLASS: AclAccessLevelPrivilegeEnum — DEATH STAR ACCESS PRIVILEGE CODES ──
// When Tarkin's team uses "custom" access — instead of a named tier — they
// specify individual privilege bits: m for manage, w for write, r for read,
// s for search, d for disclose, c for compare, x for authentication. These
// single-character codes are what OpenLDAP puts after the "=" / "+" / "-"
// modifier in a privilege-set ACL rule.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Individual privilege bits available in OpenLDAP's custom-privileges syntax.
 * When you use the {@code priv=} form instead of a named level, you mix and
 * match these — for example {@code =rsc} means read, search, and compare only.
 * Each constant serialises to its single-letter code.
 * Think of this enum as the Death Star's individual clearance codes — each
 * letter grants one specific operation, letting Tarkin compose exactly the
 * permission set he wants.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclAccessLevelPrivilegeEnum
{
    MANAGE,
    WRITE,
    READ,
    SEARCH,
    DISCLOSE,
    COMPARE,
    AUTHENTICATION;


    // ── Rendering the Privilege as Its Single-Letter Code ─────────────────────
    // Tarkin's codebook uses single letters for compactness: m, w, r, s, d, c,
    // x — each one authorises exactly one operation type. We return those
    // letters so the serialiser can write a compact priv string like "=rsc".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the single-letter code OpenLDAP expects in a privilege set. These
     * letters are concatenated after the modifier (=, +, -) to form things like
     * {@code =rsc} (read + search + compare) in the ACL text.
     *
     * <p>For example — Tarkin stamping individual operation codes:</p>
     * <pre>
     *   AclAccessLevelPrivilegeEnum.READ.toString()           // → "r"
     *   AclAccessLevelPrivilegeEnum.WRITE.toString()          // → "w"
     *   AclAccessLevelPrivilegeEnum.AUTHENTICATION.toString() // → "x"
     * </pre>
     *
     * @return  The single-character OpenLDAP privilege code.
     */
    public String toString()
    {
        switch ( this )
        {
            case MANAGE:
                return "m";
            case WRITE:
                return "w";
            case READ:
                return "r";
            case SEARCH:
                return "s";
            case DISCLOSE:
                return "d";
            case COMPARE:
                return "c";
            case AUTHENTICATION:
                return "x";
        }

        return super.toString();
    }
}
