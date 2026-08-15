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


// ── CLASS: StartTls — Whether to Activate the HoloNet Encryption Shield ──────
// Before exchanging intelligence data, the sector command can request that the
// plaintext HoloNet link be upgraded to an encrypted channel.  "yes" means
// "try to upgrade, but proceed without encryption if the provider can't support
// it" — a polite request.  "critical" means "refuse to proceed without the
// encryption shield — abort the connection if TLS fails" — a hard requirement.
// This enum models those two modes of the syncrepl "starttls" parameter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code starttls} parameter.
 * StartTLS is the LDAP mechanism for upgrading a plain connection to TLS
 * within the same TCP session (contrast with LDAPS, which starts TLS immediately).
 * {@link #YES} requests TLS but falls back to plain if the provider refuses.
 * {@link #CRITICAL} requires TLS and aborts if it can't be established.
 * Think of this as the sector command's HoloNet encryption policy: optional
 * ({@code "yes"}) or mandatory ({@code "critical"}).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum StartTls
{
    /** The 'yes' Start TLS value */
    YES("yes"),

    /** The 'critical' Start TLS value */
    CRITICAL("critical");

    /** The value */
    private String value;


    // ── Parse the Encryption Policy Token ────────────────────────────────────
    // The parser matches "yes" or "critical" (case-insensitive) and returns
    // the appropriate constant.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a StartTLS string into the corresponding enum constant.
     *
     * @param s  the string to parse — {@code "yes"} or {@code "critical"} (case-insensitive).
     * @return   the matching {@link StartTls} constant.
     * @throws ParseException  if {@code s} is neither {@code "yes"} nor {@code "critical"}.
     */
    public static StartTls parse( String s ) throws ParseException
    {
        // YES
        if ( YES.value.equalsIgnoreCase( s ) )
        {
            return YES;
        }
        // CRITICAL
        else if ( CRITICAL.value.equalsIgnoreCase( s ) )
        {
            return CRITICAL;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid start tls.", 0 );
        }
    }


    // ── Create the Constant with Its Config Token ─────────────────────────────
    // Each constant stores its lowercase token for round-trip configuration
    // serialisation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a StartTls constant with its config directive token.
     *
     * @param value  the lowercase token — {@code "yes"} or {@code "critical"}.
     */
    private StartTls( String value )
    {
        this.value = value;
    }


    // ── Write the Encryption Policy Back into the Configuration ───────────────
    // Returns the exact token OpenLDAP expects in the configuration file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the config file token for this StartTLS policy.
     *
     * @return  {@code "yes"} or {@code "critical"}.
     */
    public String toString()
    {
        return value;
    }
}
