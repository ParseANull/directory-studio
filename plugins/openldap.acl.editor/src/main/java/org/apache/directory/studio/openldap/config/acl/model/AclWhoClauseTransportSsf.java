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


// ── CLASS: AclWhoClauseTransportSsf — IMPERIAL PHYSICAL TRANSPORT SECURITY ───
// Tarkin's physical transport security division requires that even before TLS
// or SASL kick in, the underlying transport layer (e.g. a Unix-domain socket
// with peer credentials) must meet a minimum security strength. "transport_ssf=N"
// matches connections where the transport itself provides at least N SSF.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause that matches connections whose transport-layer Security
 * Strength Factor is at or above a configured threshold. The transport SSF is
 * provided by the underlying connection mechanism (e.g. Unix sockets, IPSEC)
 * before any TLS or SASL is applied. In an OpenLDAP ACL this renders as
 * {@code transport_ssf=N [accessLevel] [control]}.
 * Think of this class as Tarkin's physical-corridor security checkpoint — the
 * minimum protection the delivery channel itself must provide before you even
 * reach the application-layer guards.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseTransportSsf extends AbstractAclWhoClauseCryptoStrength
{
    // ── Rendering the Transport SSF Clause as ACL Text ────────────────────────
    // The adjutant writes "transport_ssf=N" for the transport-layer strength
    // requirement, then appends the access level and control word.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "transport_ssf=N"} where N is the minimum transport strength,
     * followed by the access level and control word if set.
     *
     * <p>For example — Tarkin requiring strength 64 at the transport layer:</p>
     * <pre>
     *   clause.setStrength(64);
     *   clause.toString()
     *   // → "transport_ssf=64 read"
     * </pre>
     *
     * @return  The ACL text fragment for this transport_ssf who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "transport_ssf=" + strength );

        String whoClauseToString = super.toString();
        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
