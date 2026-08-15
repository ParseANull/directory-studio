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

// ── CLASS: SsfStrengthEnum — LIGHTSABER CRYSTAL POWER GRADES ─────────────────
// Every Jedi knows that kyber crystals come in different grades of power:
// no crystal (NO_PROTECTION, 0 bits) provides no encryption, a cracked crystal
// (INTEGRITY_CHECK, 1 bit) gives integrity alone, DES-grade (56 bits) is an
// old and weak cut, 3DES (112 bits) is stronger, and AES at 128 or 256 bits
// are the most powerful available. NONE (-1) means no strength has been
// specified yet.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized Security Strength Factor (SSF) strength levels.
 * Each constant carries both a descriptive name and the number of bits that
 * represents that encryption strength in OpenLDAP's configuration.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SsfStrengthEnum
{
    NONE( -1, "None" ),
    NO_PROTECTION( 0, "No protection" ),
    INTEGRITY_CHECK(1,  "Integrity check" ),
    DES( 56, "DES" ),
    THREE_DES( 112, "3DES" ),
    AES_128( 128, "AES-128" ),
    AES_256( 256, "AES-256" );

    /** The associated name */
    private String name;

    /** The SSF strength position */
    private int nbBits;

    // ── CONSTRUCTOR: SsfStrengthEnum — GRADING A CRYSTAL ─────────────────────
    // Each strength constant gets the bit-count that represents it in OpenLDAP's
    // ssf numeric configuration and a human-readable label for the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its bit-count and display name.
     *
     * @param nbBits  the number of bits representing this SSF strength level
     * @param name    the human-readable display name for this strength
     */
    private SsfStrengthEnum( int nbBits, String name )
    {
        this.nbBits = nbBits;
        this.name = name;
    }

    // ── METHOD: getName — READING THE CRYSTAL GRADE LABEL ────────────────────
    // We return the display name for this strength so the UI can show something
    // meaningful instead of a raw integer.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the human-readable display name for this SSF strength level
     * (e.g., {@code "AES-128"}).
     *
     * @return the display name
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL CRYSTAL GRADES ────────────────────────
    // We compile all display names into an array for combo-box population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all strength display name strings in declaration
     * order, suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( SsfStrengthEnum ssfStrength : values() )
        {
            names[pos] = ssfStrength.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getNbBits — READING THE CRYSTAL POWER RATING ─────────────────
    // We return the numeric bit-count so callers can pass it directly to
    // OpenLDAP configuration attributes that expect an integer SSF value.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the number of bits that represents this SSF strength in
     * OpenLDAP configuration (e.g., {@code 128} for AES-128).
     *
     * @return the number of bits for this strength level
     */
    public int getNbBits()
    {
        return nbBits;
    }


    // ── METHOD: getSsfStrength(int) — IDENTIFYING A GRADE BY BIT-COUNT ────────
    // We use a switch on the bit-count to return the matching constant quickly.
    // Any bit-count outside the known set returns NONE.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link SsfStrengthEnum} constant by its bit-count via a
     * switch statement. We return {@link #NONE} for any unrecognized value.
     *
     * @param nbBits  the bit-count to look up
     * @return        the matching enum constant, or {@link #NONE}
     */
    public static SsfStrengthEnum getSsfStrength( int nbBits )
    {
        switch ( nbBits )
        {
            case 0 : return NO_PROTECTION;
            case 1 : return INTEGRITY_CHECK;
            case 56 : return DES;
            case 112 : return THREE_DES;
            case 128 : return AES_128;
            case 256 : return AES_256;
            default : return NONE;
        }
    }

    // ── METHOD: getSsfStrength(String) — IDENTIFYING A GRADE BY DISPLAY NAME ──
    // We scan all constants for a case-insensitive match on the display name.
    // An unrecognized string returns NONE as the safe default.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link SsfStrengthEnum} constant by its display name string
     * using case-insensitive comparison. We return {@link #NONE} if no match
     * is found.
     *
     * @param text  the display name to look up
     * @return      the matching enum constant, or {@link #NONE}
     */
    public static SsfStrengthEnum getSsfStrength( String text )
    {
        for ( SsfStrengthEnum ssfStrength : values() )
        {
            if ( ssfStrength.name.equalsIgnoreCase( text ) )
            {
                return ssfStrength;
            }
        }

        // Default...
        return NONE;
    }
}
