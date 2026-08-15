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



// ── CLASS: DbIndexTypeEnum — REBEL SPY NETWORK INDEXING METHODS ──────────────
// Picture the Rebel Intelligence network deciding how to file the intercepts
// it receives: by exact match (EQ), by approximate phonetic match (APPROX),
// by presence in a database (PRES), by a substring pattern (SUB/SUBSTR), and
// so on. Each constant here is one indexing strategy that OpenLDAP can apply
// to an attribute, with a numeric ID for internal bookkeeping. NONE means
// no indexing at all.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized OpenLDAP database index types. Each constant
 * carries both a numeric identifier and the configuration-file string. The
 * possible values are:
 * <ul>
 * <li>approx</li>
 * <li>eq</li>
 * <li>nolang</li>
 * <li>nosubtypes</li>
 * <li>notags</li>
 * <li>pres</li>
 * <li>sub</li>
 * <li>subany</li>
 * <li>subfinal</li>
 * <li>subinitial</li>
 * <li>substr</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum DbIndexTypeEnum
{
    APPROX( 0, "approx" ),
    EQ( 1, "eq" ),
    NOLANG( 2, "nolang" ),
    NOSUBTYPES( 3, "nosubtypes" ),
    NOTAGS( 4, "notags" ),
    PRES( 5, "pres" ),
    SUB( 6, "sub" ),
    SUBANY( 7, "subany" ),
    SUBFINAL( 8, "subfinal" ),
    SUBINITIAL( 9, "subinitial" ),
    SUBSTR( 10, "substr" ),  // Same as SUB
    NONE( 11, "none" );

    /** The internal name */
    private String name;

    /** The internal number */
    private int number;

    // ── CONSTRUCTOR: DbIndexTypeEnum — REGISTERING AN INTELLIGENCE FILING CODE ─
    // Each index type gets a numeric ID for switch statements and a string name
    // for configuration-file round-tripping. Both are set at construction time
    // and are immutable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We initialize each constant with its numeric identifier and configuration-
     * file name string.
     *
     * @param number  the numeric identifier used in switch-based lookups
     * @param name    the olcDbIndex string value for this index type
     */
    private DbIndexTypeEnum( int number, String name )
    {
        this.name = name;
        this.number = number;
    }


    // ── METHOD: getName — READING THE FILING CODE LABEL ──────────────────────
    // We return the configuration-file string so callers can embed it directly
    // in generated configuration or display it in the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the OpenLDAP configuration-file string for this index type
     * (e.g., {@code "eq"} or {@code "sub"}).
     *
     * @return the index type string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL INTELLIGENCE FILING CODES ─────────────
    // We assemble every index type string into an array for list-based UI
    // components that need the full set of options.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all index type name strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all index type name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( DbIndexTypeEnum dbIndexType : values() )
        {
            names[pos] = dbIndexType.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getNumber — READING THE FILING CODE NUMBER ───────────────────
    // We return the numeric identifier, useful for switch statements and
    // position-based lookups within the values array.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the numeric identifier for this index type, used in switch-
     * based lookups via {@link #getIndexType(int)}.
     *
     * @return the numeric identifier
     */
    public int getNumber()
    {
        return number;
    }


    // ── METHOD: getIndexType(String) — IDENTIFYING THE FILING METHOD BY NAME ──
    // We scan for a case-insensitive name match. Note that "substr" and "sub"
    // are aliases for the same index — we normalize "substr" to return SUB so
    // the codebase has exactly one canonical constant for that strategy.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link DbIndexTypeEnum} by its configuration-file name using
     * case-insensitive comparison. Note that {@code "substr"} is an alias for
     * {@code "sub"} — both return {@link #SUB}. We return {@link #NONE} if no
     * match is found.
     *
     * @param name  the index type string to look up
     * @return      the matching enum constant, or {@link #NONE}
     */
    public static DbIndexTypeEnum getIndexType( String name )
    {
        for ( DbIndexTypeEnum indexType : values() )
        {
            if ( indexType.getName().equalsIgnoreCase( name ) )
            {
                if ( SUBSTR.getName().equalsIgnoreCase( name ) )
                {
                    // SUB and SUBSTR are the same. Return SUB
                    return SUB;
                }
                else
                {
                    return indexType;
                }
            }
        }

        return NONE;
    }


    // ── METHOD: getIndexType(int) — IDENTIFYING THE FILING METHOD BY NUMBER ───
    // We use a fast switch on the numeric ID to return the matching constant.
    // Any number outside the known range returns NONE.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link DbIndexTypeEnum} by its numeric identifier using a
     * switch statement. We return {@link #NONE} for any unrecognized number.
     *
     * @param number  the numeric identifier to look up
     * @return        the matching enum constant, or {@link #NONE}
     */
    public static DbIndexTypeEnum getIndexType( int number )
    {
        switch ( number )
        {
            case 0 : return APPROX;
            case 1 : return EQ;
            case 2 : return NOLANG;
            case 3 : return NOSUBTYPES;
            case 4 : return NOTAGS;
            case 5 : return PRES;
            case 6 : return SUB;
            case 7 : return SUBANY;
            case 8 : return SUBFINAL;
            case 9 : return SUBINITIAL;
            case 10 : return SUBSTR;
            default : return NONE;
        }
    }
}
