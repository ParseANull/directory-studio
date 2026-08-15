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

// ── CLASS: AclWhoClauseAnonymous — UNIDENTIFIED VISITOR ON TARKIN'S MANIFEST ─
// On Tarkin's clearance manifest there is always an entry for the unidentified
// visitor — someone who has not authenticated at all. OpenLDAP calls this
// "anonymous". Any unauthenticated connection matches this who-clause, so we
// can grant (or deny) a specific access level to people who have not even
// logged in yet. This class represents that one "anonymous" entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause representing unauthenticated (anonymous) connections.
 * In an OpenLDAP ACL this renders as {@code anonymous [accessLevel] [control]}.
 * All the access-level and control logic is inherited from
 * {@link AbstractAclWhoClause}; we only add the "anonymous" keyword prefix
 * in {@link #toString()}.
 * Think of this class as the "unidentified visitor" row on Tarkin's access
 * roster — it covers everyone who arrives without credentials.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseAnonymous extends AbstractAclWhoClause
{
    // ── Rendering the Anonymous Clause as ACL Text ────────────────────────────
    // The adjutant writes "anonymous" at the start of the entry, then appends
    // whatever access level and control word the base class provides.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "anonymous"} optionally followed by the access level and control
     * word from the parent class.
     *
     * <p>For example — Tarkin's adjutant writing the anonymous entry:</p>
     * <pre>
     *   clause.toString()
     *   // → "anonymous read stop"   (with level and control)
     *   // → "anonymous read"        (with level, no control)
     *   // → "anonymous"             (no level or control set yet)
     * </pre>
     *
     * @return  The ACL text fragment for this anonymous who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "anonymous" );

        String whoClauseToString = super.toString();

        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
