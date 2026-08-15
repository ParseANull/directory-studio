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


// ── CLASS: AclWhoClauseGroupTypeEnum — CLOUD CITY ROSTER LOOKUP MODES ────────
// Lando runs Cloud City's security roster with two lookup modes: exact-match
// (the group entry DN must match exactly) or expand (treat the group DN as a
// pattern with back-reference substitution). This enum captures those two
// modes for OpenLDAP's group who-clause.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The type modifier for the group who-clause. Written as
 * {@code group[/oc[/attr]][.type]="pattern"}, the type is either
 * {@code exact} (the DN is taken literally) or {@code expand} (regex
 * back-references are substituted before matching).
 * Think of this enum as Lando's two roster lookup modes — pinpoint exact
 * match versus expanded pattern lookup.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclWhoClauseGroupTypeEnum
{
    EXACT,
    EXPAND;


    // ── Serialising the Group Lookup Mode ─────────────────────────────────────
    // Lando's roster uses "exact" or "expand" in the security directive; we
    // return those exact strings so the ACL serialiser writes the right keyword.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP wire-format keyword for this group-type modifier.
     *
     * <p>For example — Lando picking a roster lookup mode:</p>
     * <pre>
     *   AclWhoClauseGroupTypeEnum.EXACT.toString()  // → "exact"
     *   AclWhoClauseGroupTypeEnum.EXPAND.toString() // → "expand"
     * </pre>
     *
     * @return  The OpenLDAP keyword string for this group type.
     */
    public String toString()
    {
        switch ( this )
        {
            case EXACT:
                return "exact";
            case EXPAND:
                return "expand";
        }

        return super.toString();
    }
}
