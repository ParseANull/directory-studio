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


// ── CLASS: TlsReqCert — The Sector Command's Certificate Validation Policy ───
// When the sector command opens an encrypted HoloNet link, it must decide how
// strictly to validate Imperial HQ's TLS certificate.  "never" means skip
// validation entirely (trust everyone — dangerous but sometimes necessary for
// self-signed certs).  "allow" tries to verify but continues on failure.
// "try" also tries but may stop if the cert is just invalid.  "demand" is the
// strictest: reject the connection outright if the cert isn't valid and trusted.
// This enum models those four strictness levels — the syncrepl "tls_reqcert"
// parameter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code tls_reqcert} parameter.
 * Controls how strictly the consumer validates the provider's TLS certificate.
 * {@link #NEVER} skips validation entirely.
 * {@link #ALLOW} tries to validate but proceeds even on failure.
 * {@link #TRY} tries to validate and may abort if the cert is explicitly invalid.
 * {@link #DEMAND} is the strictest: aborts if the cert can't be verified.
 * Think of this as the sector command's Imperial certificate trust policy —
 * from "trust everyone" to "verify and refuse if not trusted."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum TlsReqCert
{
    /** The 'never' TLS REQ Cert value */
    NEVER("never"),

    /** The 'allow' TLS REQ Cert value */
    ALLOW("allow"),

    /** The 'try' TLS REQ Cert value */
    TRY("try"),

    /** The 'demand' TLS REQ Cert value */
    DEMAND("demand");

    /** The value */
    private String value;


    // ── Parse the Certificate Trust Level ────────────────────────────────────
    // The parser matches the incoming string case-insensitively and returns
    // the matching constant, or throws for anything unrecognised.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a TLS certificate requirement string into the corresponding enum constant.
     *
     * @param s  the string — one of {@code "never"}, {@code "allow"}, {@code "try"},
     *           or {@code "demand"} (case-insensitive).
     * @return   the matching {@link TlsReqCert} constant.
     * @throws ParseException  if {@code s} is not a recognised certificate requirement value.
     */
    public static TlsReqCert parse( String s ) throws ParseException
    {
        // NEVER
        if ( NEVER.value.equalsIgnoreCase( s ) )
        {
            return NEVER;
        }
        // ALLOW
        else if ( ALLOW.value.equalsIgnoreCase( s ) )
        {
            return ALLOW;
        }
        // TRY
        else if ( TRY.value.equalsIgnoreCase( s ) )
        {
            return TRY;
        }
        // DEMAND
        else if ( DEMAND.value.equalsIgnoreCase( s ) )
        {
            return DEMAND;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid tls req cert.", 0 );
        }
    }


    // ── Create the Constant with Its Config Token ─────────────────────────────
    // Each constant stores its lowercase token for round-trip serialisation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a TlsReqCert constant with its config directive token.
     *
     * @param value  the lowercase token — one of {@code "never"}, {@code "allow"},
     *               {@code "try"}, or {@code "demand"}.
     */
    private TlsReqCert( String value )
    {
        this.value = value;
    }


    // ── Write the Certificate Policy Back into the Configuration ─────────────
    // Returns the exact token OpenLDAP expects in the tls_reqcert= parameter.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the config file token for this certificate requirement policy.
     *
     * @return  one of {@code "never"}, {@code "allow"}, {@code "try"}, {@code "demand"}.
     */
    public String toString()
    {
        return value;
    }
}
