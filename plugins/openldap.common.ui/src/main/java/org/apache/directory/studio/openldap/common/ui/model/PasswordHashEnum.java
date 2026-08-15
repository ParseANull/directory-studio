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
package org.apache.directory.studio.openldap.common.ui.model;

// ── CLASS: PasswordHashEnum — REBEL ENCRYPTION PROTOCOLS ─────────────────────
// Picture the Rebel Alliance's roster of cipher codes for securing their
// communications: CLEARTEXT leaves the message in plain Basic (dangerous but
// sometimes necessary), SHA and SSHA are the workhorse encryption standards,
// MD5 and SMD5 are the older codes that some legacy systems still require,
// CRYPT uses the old Unix scramble, LANMAN is the ancient Imperial encoding,
// and UNIX rounds out the list. NO_CHOICE means no cipher has been selected
// yet — the comm channel is open and unsecured.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all password hashing algorithms supported by OpenLDAP's
 * {@code olcPasswordHash} attribute. Each constant carries a numeric ID
 * for switch-based lookup and the configuration-file prefix string (e.g.,
 * {@code "{SHA}"}).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum PasswordHashEnum
{
    NO_CHOICE( 0,"" ),
    CLEARTEXT( 1, "{CLEARTEXT}" ),
    CRYPT( 2, "{CRYPT}" ),
    LANMAN( 3, "{LANMAN}" ),
    MD5( 4, "{MD5}" ),
    SMD5( 5, "{SMD5}" ),
    SHA( 6, "{SHA}" ),
    SSHA( 7, "{SSHA}" ),
    UNIX( 8, "{UNIX}" );

    /** The hash number */
    private int number;

    /** The interned name */
    private String name;

    // ── CONSTRUCTOR: PasswordHashEnum — REGISTERING A CIPHER CODE ────────────
    // Each algorithm gets a numeric serial for fast switch-based lookup and the
    // string prefix that OpenLDAP wraps around hashed password values in the
    // directory.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its numeric identifier and its
     * configuration-file prefix string.
     *
     * @param number  the numeric identifier for switch-based lookup
     * @param name    the password hash prefix string (e.g., {@code "{SHA}"})
     */
    private PasswordHashEnum( int number, String name )
    {
        this.name = name;
        this.number = number;
    }


    // ── METHOD: getName — READING THE CIPHER CODE PREFIX ─────────────────────
    // We return the OpenLDAP password-hash prefix string so callers can display
    // or persist it without knowing the underlying integer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the password hash prefix string for this algorithm
     * (e.g., {@code "{SHA}"}).
     *
     * @return the password hash prefix string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL CIPHER CODES ───────────────────────────
    // We collect every prefix string into an array for combo-box population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all hash prefix strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( PasswordHashEnum passwordHash : values() )
        {
            names[pos] = passwordHash.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getNumber — READING THE CIPHER SERIAL NUMBER ─────────────────
    // We return the numeric ID for switch-based dispatching in lookup methods.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the numeric identifier for this hash algorithm, used in
     * switch-based lookups via {@link #getPasswordHash(int)}.
     *
     * @return the numeric identifier
     */
    public int getNumber()
    {
        return number;
    }


    // ── METHOD: getPasswordHash(int) — IDENTIFYING A CIPHER BY SERIAL NUMBER ──
    // We use a switch on the numeric ID to return the matching constant quickly.
    // Any unrecognized number returns NO_CHOICE — no cipher selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link PasswordHashEnum} constant by its numeric identifier
     * via a switch statement. We return {@link #NO_CHOICE} for any unrecognized
     * number.
     *
     * @param number  the numeric identifier to look up
     * @return        the matching enum constant, or {@link #NO_CHOICE}
     */
    public static PasswordHashEnum getPasswordHash( int number )
    {
        switch ( number )
        {
            case 1 : return CLEARTEXT;
            case 2 : return CRYPT;
            case 3 : return LANMAN;
            case 4 : return MD5;
            case 5 : return SMD5;
            case 6 : return SHA;
            case 7 : return SSHA;
            case 8 : return UNIX;
            default : return NO_CHOICE;
        }
    }


    // ── METHOD: getPasswordHash(String) — IDENTIFYING A CIPHER BY PREFIX ──────
    // We scan all constants for a case-insensitive match on the prefix string.
    // If no match is found we return NO_CHOICE so callers always get a valid
    // non-null result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link PasswordHashEnum} constant by its prefix string using
     * case-insensitive comparison. We return {@link #NO_CHOICE} if no constant
     * matches.
     *
     * @param name  the hash prefix string to look up (e.g., {@code "{SHA}"})
     * @return      the matching enum constant, or {@link #NO_CHOICE}
     */
    public static PasswordHashEnum getPasswordHash( String name )
    {
        for ( PasswordHashEnum passwordHash : values() )
        {
            if ( passwordHash.name.equalsIgnoreCase( name ) )
            {
                return passwordHash;
            }
        }

        return NO_CHOICE;
    }


    // ── METHOD: toString — PRINTING THE CIPHER PREFIX ────────────────────────
    // We return the prefix string so this enum can be used wherever a String
    // is expected, such as in list cell renderers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return name;
    }
}
