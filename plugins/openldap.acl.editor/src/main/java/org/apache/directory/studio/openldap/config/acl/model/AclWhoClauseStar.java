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


// ── CLASS: AclWhoClauseStar — ALL BEINGS IN THE GALAXY: TARKIN'S CATCH-ALL ──
// At the bottom of Tarkin's clearance manifest there is always a catch-all row
// marked with a star — "everyone else, no exceptions." OpenLDAP uses "*" in
// the who-clause to mean every connection, authenticated or not. This class
// models that universal catch-all entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause matching every connection regardless of authentication
 * status. In an OpenLDAP ACL this renders as {@code * [accessLevel] [control]}.
 * Typically placed as the last "by" clause in a rule so that anyone not matched
 * by a more specific clause falls through to this default.
 * Think of this class as Tarkin's universal "everyone else" row — the star that
 * covers every being in the galaxy who wasn't caught by a named entry above it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseStar extends AbstractAclWhoClause
{
    // ── Rendering the Star Clause as ACL Text ────────────────────────────────
    // The adjutant writes "*" at the start of the catch-all entry, then appends
    // the access level and control word inherited from the base class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "*"} optionally followed by the access level and control word from
     * the parent class.
     *
     * <p>For example — Tarkin's adjutant writing the universal catch-all entry:</p>
     * <pre>
     *   clause.toString()
     *   // → "* none stop"   (no access for everyone else, stop evaluation)
     *   // → "* read"        (read-only for everyone)
     *   // → "*"             (no level or control set yet)
     * </pre>
     *
     * @return  The ACL text fragment for this star who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "*" );

        String whoClauseToString = super.toString();
        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
