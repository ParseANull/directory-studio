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


// ── CLASS: BindMethod — How the Sector Command Authenticates to Imperial HQ ──
// When a sector command connects to Death Star Intelligence for its intelligence
// sync, it must first identify itself.  There are two ways: a simple password
// exchange ("simple" — like a sector code) or a full Kerberos/SASL challenge
// ("sasl" — like presenting Imperial credentials to a protocol droid).
// This enum models those two authentication modes for OpenLDAP's syncrepl
// "bindmethod" parameter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code bindmethod} parameter.
 * This controls how the consumer LDAP server authenticates to the provider
 * (master) server when establishing the replication connection.
 * {@code SIMPLE} uses a plain password (bind DN + credentials).
 * {@code SASL} uses a SASL exchange (Kerberos, DIGEST-MD5, etc.).
 * Think of this as whether the sector command uses a simple access code
 * or a full Imperial protocol droid challenge to authenticate.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum BindMethod
{
    /** The 'simple' bind method value */
    SIMPLE("simple"),

    /** The 'sash' bind method value */
    SASL("sasl");

    /** The value */
    private String value;


    // ── Sector Command Presents Its Credentials ───────────────────────────────
    // The parser checks the incoming string against each valid method name and
    // returns the matching enum constant — or throws ParseException if no
    // recognised credential format is presented.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a bind method string into the corresponding enum constant.
     * The comparison is case-insensitive so "SIMPLE", "Simple", and "simple"
     * all resolve to {@link #SIMPLE}.
     *
     * @param s  the bind method string to parse, e.g. {@code "simple"} or {@code "sasl"}.
     * @return   the matching {@link BindMethod} constant.
     * @throws ParseException  if {@code s} is not a recognised bind method name.
     */
    public static BindMethod parse( String s ) throws ParseException
    {
        // SIMPLE
        if ( SIMPLE.value.equalsIgnoreCase( s ) )
        {
            return SIMPLE;
        }
        // SASL
        else if ( SASL.value.equalsIgnoreCase( s ) )
        {
            return SASL;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid bind method.", 0 );
        }
    }


    // ── Imperial HQ Assigns the Authentication Protocol ──────────────────────
    // Each enum constant is created with the exact lowercase string token used
    // in the syncrepl directive, so we can round-trip parse → toString without
    // losing any information.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a BindMethod constant with its syncrepl wire token.
     *
     * @param value  the lowercase token as it appears in the syncrepl directive.
     */
    private BindMethod( String value )
    {
        this.value = value;
    }


    // ── Read the Method Name Back Off the Credential ──────────────────────────
    // When we need to write this value back into the syncrepl directive string,
    // toString() returns the exact token OpenLDAP expects.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the syncrepl directive token for this bind method.
     * This is what gets written into the OpenLDAP configuration file.
     *
     * @return  the lowercase token, e.g. {@code "simple"} or {@code "sasl"}.
     */
    public String toString()
    {
        return value;
    }
}
