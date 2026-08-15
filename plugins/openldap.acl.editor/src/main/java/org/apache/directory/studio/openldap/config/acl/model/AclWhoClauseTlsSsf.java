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


// ── CLASS: AclWhoClauseTlsSsf — IMPERIAL TLS LAYER ENCRYPTION STRENGTH CHECK ─
// Tarkin's TLS security wing specifically checks the strength of the TLS
// transport layer — not SASL, not overall session SSF, just the TLS cipher.
// "tls_ssf=N" matches connections whose TLS layer provides at least N bits
// of cryptographic strength. This class models that TLS-specific strength check.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause that matches connections whose TLS-layer Security
 * Strength Factor is at or above a configured threshold. The TLS SSF is
 * separate from SASL SSF — it only measures the transport encryption, not the
 * authentication layer. In an OpenLDAP ACL this renders as
 * {@code tls_ssf=N [accessLevel] [control]}.
 * Think of this class as Tarkin's TLS checkpoint — only connections with
 * sufficiently strong TLS encryption reach this access level.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseTlsSsf extends AbstractAclWhoClauseCryptoStrength
{
    // ── Rendering the TLS SSF Clause as ACL Text ──────────────────────────────
    // The adjutant writes "tls_ssf=N" for the TLS-layer strength requirement,
    // then appends the access level and control word from the base class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "tls_ssf=N"} where N is the minimum TLS strength, followed by the
     * access level and control word if set.
     *
     * <p>For example — Tarkin requiring 128-bit TLS encryption:</p>
     * <pre>
     *   clause.setStrength(128);
     *   clause.toString()
     *   // → "tls_ssf=128 read"
     * </pre>
     *
     * @return  The ACL text fragment for this tls_ssf who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "tls_ssf=" + strength );

        String whoClauseToString = super.toString();
        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
