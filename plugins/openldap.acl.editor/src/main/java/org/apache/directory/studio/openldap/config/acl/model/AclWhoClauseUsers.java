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

// ── CLASS: AclWhoClauseUsers — ALL AUTHENTICATED USERS ON TARKIN'S MANIFEST ─
// Tarkin has a manifest row for "all authenticated users" — every connection
// that has successfully bound to the directory with valid credentials. OpenLDAP
// calls this "users". It is more selective than "*" (which catches everyone)
// but less selective than a specific DN pattern. This class models that row.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause matching all authenticated connections. In an OpenLDAP
 * ACL this renders as {@code users [accessLevel] [control]}. Unlike the star
 * clause, this only matches connections that have successfully bound — anonymous
 * binds are excluded.
 * Think of this class as Tarkin's "registered crew" row — anyone who showed up
 * with valid credentials but is not called out individually.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseUsers extends AbstractAclWhoClause
{
    // ── Rendering the Users Clause as ACL Text ────────────────────────────────
    // The adjutant writes "users" at the start of the row, then appends the
    // access level and control word inherited from the base class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "users"} optionally followed by the access level and control word
     * from the parent class.
     *
     * <p>For example — Tarkin's adjutant writing the authenticated-users row:</p>
     * <pre>
     *   clause.toString()
     *   // → "users read"      (authenticated users get read access)
     *   // → "users read stop" (read, then stop evaluation)
     *   // → "users"           (no level or control set yet)
     * </pre>
     *
     * @return  The ACL text fragment for this users who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "users" );

        String whoClauseToString = super.toString();
        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
