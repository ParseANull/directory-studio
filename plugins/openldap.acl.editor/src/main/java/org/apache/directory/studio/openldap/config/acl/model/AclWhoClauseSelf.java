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


// ── CLASS: AclWhoClauseSelf — TARKIN GRANTS A USER ACCESS TO THEIR OWN RECORD
// On Tarkin's manifest there is a special row: "self" — it matches a user
// who is accessing their own directory entry. A common pattern is "by self
// write" which lets a user update their own attributes. This class models
// that special case: the authenticated user is both the subject and the object
// of the access request.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause matching the entry owner — the user whose own DN is
 * the DN of the entry being accessed. In an OpenLDAP ACL this renders as
 * {@code self [accessLevel] [control]}. It is typically used to let users
 * update their own profile attributes.
 * Think of this class as Tarkin's "owner" row — the clearance that applies
 * when the person knocking is the same person whose file is on the desk.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseSelf extends AbstractAclWhoClause
{
    // ── Rendering the Self Clause as ACL Text ─────────────────────────────────
    // The adjutant writes "self" and then appends the access level and control
    // word from the base class, giving us the wire-format fragment.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "self"} optionally followed by the access level and control word
     * from the parent class.
     *
     * <p>For example — Tarkin's adjutant writing the owner-access entry:</p>
     * <pre>
     *   clause.toString()
     *   // → "self write"      (owner can write their own attributes)
     *   // → "self write stop" (write, then stop evaluation)
     *   // → "self"            (no level or control set yet)
     * </pre>
     *
     * @return  The ACL text fragment for this self who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "self" );

        String whoClauseToString = super.toString();
        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
