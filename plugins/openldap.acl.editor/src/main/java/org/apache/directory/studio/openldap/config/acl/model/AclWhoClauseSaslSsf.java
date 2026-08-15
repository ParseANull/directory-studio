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


// ── CLASS: AclWhoClauseSaslSsf — IMPERIAL SASL AUTHENTICATION STRENGTH CHECK ─
// Tarkin's SASL security wing requires that certain operations are only
// permitted if the SASL authentication mechanism itself provided sufficient
// cryptographic strength. "sasl_ssf=N" matches connections whose SASL layer
// alone meets the strength bar — distinct from TLS-layer or overall-session SSF.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause that matches connections whose SASL-layer Security
 * Strength Factor is at or above a configured threshold. This is independent
 * of the TLS layer; a connection could have strong SASL encryption but no TLS,
 * or vice versa. In an OpenLDAP ACL this renders as
 * {@code sasl_ssf=N [accessLevel] [control]}.
 * Think of this class as Tarkin's SASL authentication checkpoint — only
 * connections whose SASL layer is sufficiently strong get through.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseSaslSsf extends AbstractAclWhoClauseCryptoStrength
{
    // ── Rendering the SASL SSF Clause as ACL Text ─────────────────────────────
    // The adjutant writes "sasl_ssf=N" for the SASL-layer strength requirement,
    // then appends the access level and control word from the base class.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string. The output is
     * {@code "sasl_ssf=N"} where N is the minimum SASL strength, followed by
     * the access level and control word if set.
     *
     * <p>For example — Tarkin requiring 56-bit SASL layer strength:</p>
     * <pre>
     *   clause.setStrength(56);
     *   clause.toString()
     *   // → "sasl_ssf=56 read"
     * </pre>
     *
     * @return  The ACL text fragment for this sasl_ssf who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "sasl_ssf=" + strength );

        String whoClauseToString = super.toString();
        if ( whoClauseToString.length() > 0 )
        {
            sb.append( " " );
            sb.append( whoClauseToString );
        }

        return sb.toString();
    }
}
