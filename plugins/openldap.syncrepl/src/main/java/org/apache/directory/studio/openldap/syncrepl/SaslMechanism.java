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


// ── CLASS: SaslMechanism — The Imperial SASL Protocol Droid's Language List ──
// When a sector command authenticates using SASL (instead of a simple bind DN
// and password), it must declare which SASL protocol it speaks: DIGEST-MD5
// (a challenge-response password hash exchange) or GSSAPI (Kerberos).
// This enum lists the two mechanisms supported by OpenLDAP's syncrepl
// and handles the two-part naming: a human-readable "title" displayed in the
// UI and a lowercase "value" used in the configuration directive.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code saslmech} parameter.
 * These are the SASL authentication mechanisms the consumer can use when
 * connecting to the provider with {@code bindmethod=sasl}.
 * {@link #DIGEST_MD5} uses a challenge-response MD5 hash exchange.
 * {@link #GSSAPI} uses Kerberos ticket-based authentication.
 * Think of this as the protocol droid's list of supported Imperial authentication
 * dialects: either DIGEST-MD5 or Kerberos.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SaslMechanism
{
    /** The 'digest-md5' SASL mechanism */
    DIGEST_MD5("DIGEST-MD5", "digest-md5"),

    /** The 'gssapi' SASL mechanism */
    GSSAPI("GSSAPI", "gssapi"), ;

    /** The title */
    private String title;

    /** The value */
    private String value;


    // ── Sector Command Declares Its SASL Dialect ──────────────────────────────
    // The parser checks the incoming mechanism name against each known dialect
    // (case-insensitive) and returns the matching enum constant.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a SASL mechanism string into the corresponding enum constant.
     * The comparison is case-insensitive against the lowercase {@code value}
     * field of each constant.
     *
     * @param s  the mechanism string, e.g. {@code "digest-md5"} or {@code "gssapi"}.
     * @return   the matching {@link SaslMechanism} constant.
     * @throws ParseException  if {@code s} is not a recognised SASL mechanism name.
     */
    public static SaslMechanism parse( String s ) throws ParseException
    {
        // DIGEST_MD5
        if ( DIGEST_MD5.value.equalsIgnoreCase( s ) )
        {
            return DIGEST_MD5;
        }
        // GSSAPI
        else if ( GSSAPI.value.equalsIgnoreCase( s ) )
        {
            return GSSAPI;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid sasl mechanism method.", 0 );
        }
    }


    // ── Protocol Droid Gets Its Title and Value ───────────────────────────────
    // Each constant carries both a human-readable title (for the UI dropdown)
    // and a lowercase value token (for the configuration file).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SaslMechanism constant with its display title and config value.
     *
     * @param title  the human-readable label, e.g. {@code "DIGEST-MD5"}.
     * @param value  the lowercase token used in the configuration, e.g. {@code "digest-md5"}.
     */
    private SaslMechanism( String title, String value )
    {
        this.title = title;
        this.value = value;
    }


    // ── Read the Human-Readable Title ────────────────────────────────────────
    // Used by the UI to show a friendly name in dropdowns and combo boxes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable title for display in the UI.
     *
     * @return  the title, e.g. {@code "DIGEST-MD5"}.
     */
    public String getTitle()
    {
        return title;
    }


    // ── Read the Config Directive Value ──────────────────────────────────────
    // Used when building the syncrepl directive string for the config file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lowercase config file token for this mechanism.
     *
     * @return  the value token, e.g. {@code "digest-md5"}.
     */
    public String getValue()
    {
        return value;
    }


    // ── Return the Title for General String Representation ────────────────────
    // toString() returns the title so it looks good in UI labels and log messages.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable title — the same as {@link #getTitle()}.
     * This makes the enum display cleanly in combo boxes and toString() calls.
     *
     * @return  the title string.
     */
    public String toString()
    {
        return title;
    }
}
