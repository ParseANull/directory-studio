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


// ── CLASS: AclWhoClauseSsf — IMPERIAL ENCRYPTION CLEARANCE: OVERALL SSF ──────
// Tarkin's security division requires that certain files may only be accessed
// if the connection's overall cryptographic strength (Security Strength Factor)
// meets a minimum bar. The "ssf=N" who-clause does exactly that — it only
// matches connections whose combined session SSF is at or above N. This class
// models that strength-based who-clause for the overall SSF.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause that matches connections whose overall Security Strength
 * Factor (SSF) is at or above a configured threshold. SSF is a numeric measure
 * of cryptographic strength (roughly equivalent to effective key length in
 * bits). In an OpenLDAP ACL this renders as {@code ssf=N [accessLevel] [control]}.
 * Think of this class as Tarkin's minimum encryption badge — only connections
 * with sufficient overall crypto strength get past this checkpoint.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseSsf extends AbstractAclWhoClauseCryptoStrength
{
    // ── Rendering the SSF Clause as ACL Text ──────────────────────────────────
    // The adjutant writes "ssf=N" where N is the minimum strength value, then
    // appends the access level and control word from the base class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "ssf=N"} where N is the minimum SSF, followed by the access level
     * and control word if set.
     *
     * <p>For example — Tarkin requiring 128-bit overall session strength:</p>
     * <pre>
     *   clause.setStrength(128);
     *   clause.toString()
     *   // → "ssf=128 read"   (only 128-bit-or-stronger sessions get read)
     * </pre>
     *
     * @return  The ACL text fragment for this ssf who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "ssf=" + strength );

        String whoClauseToString = super.toString();
        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
