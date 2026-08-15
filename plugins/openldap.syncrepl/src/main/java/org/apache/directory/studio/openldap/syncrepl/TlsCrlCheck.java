/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.studio.openldap.syncrepl;


import java.text.ParseException;


// ── CLASS: TlsCrlCheck — How Strictly to Check Imperial Certificate Revocations ─
// When a sector command establishes an encrypted HoloNet link to Imperial HQ,
// it may need to verify that the HQ's TLS certificate hasn't been revoked —
// i.e. that the Imperial Security Bureau hasn't invalidated it.  "none" means
// skip revocation checks entirely; "peer" means check only the peer's cert;
// "all" means check every cert in the chain.
// This enum models those three levels for the syncrepl "tls_crlcheck" parameter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code tls_crlcheck} parameter.
 * Controls how strictly the consumer checks Certificate Revocation Lists (CRLs)
 * when validating the TLS certificate of the provider server.
 * {@link #NONE} skips revocation checking.
 * {@link #PEER} checks only the peer's own certificate.
 * {@link #ALL} checks every certificate in the chain.
 * Think of this as the security droid's Imperial certificate revocation policy
 * for the encrypted HoloNet link.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum TlsCrlCheck
{
    /** The 'none' TLS CRL Check value */
    NONE("none"),

    /** The 'peer' TLS CRL Check value */
    PEER("peer"),

    /** The 'all' TLS CRL Check value */
    ALL("all");

    /** The value */
    private String value;


    // ── Parse the Revocation Policy Token ────────────────────────────────────
    // The parser matches the incoming string case-insensitively and returns
    // NONE, PEER, or ALL.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a TLS CRL check string into the corresponding enum constant.
     *
     * @param s  the string to parse — {@code "none"}, {@code "peer"}, or {@code "all"}
     *           (case-insensitive).
     * @return   the matching {@link TlsCrlCheck} constant.
     * @throws ParseException  if {@code s} is not a recognised CRL check value.
     */
    public static TlsCrlCheck parse( String s ) throws ParseException
    {
        // NONE
        if ( NONE.value.equalsIgnoreCase( s ) )
        {
            return NONE;
        }
        // PEER
        else if ( PEER.value.equalsIgnoreCase( s ) )
        {
            return PEER;
        }
        // ALL
        else if ( ALL.value.equalsIgnoreCase( s ) )
        {
            return ALL;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid tls crl check method.", 0 );
        }
    }


    // ── Create the Constant with Its Config Token ─────────────────────────────
    // Each constant stores its exact lowercase token for the configuration file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a TlsCrlCheck constant with its config directive token.
     *
     * @param value  the lowercase token — {@code "none"}, {@code "peer"}, or {@code "all"}.
     */
    private TlsCrlCheck( String value )
    {
        this.value = value;
    }


    // ── Write the Revocation Policy Back into the Configuration ───────────────
    // Returns the exact token OpenLDAP expects in the tls_crlcheck= parameter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the config file token for this CRL check policy.
     *
     * @return  one of {@code "none"}, {@code "peer"}, {@code "all"}.
     */
    public String toString()
    {
        return value;
    }
}
