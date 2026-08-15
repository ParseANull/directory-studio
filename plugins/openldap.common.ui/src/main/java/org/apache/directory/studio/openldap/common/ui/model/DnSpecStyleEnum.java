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


// ── CLASS: DnSpecStyleEnum — JEDI NAVIGATOR COORDINATE STYLES ────────────────
// When a Jedi navigator locks onto a destination, they can describe it in
// different ways: exact coordinates pinpoint a single planet (EXACT/BASE),
// one-level broadens the search to the neighboring system (ONE/ONE_LEVEL),
// subtree spreads the net across an entire sector (SUB/SUBTREE), children
// includes everything orbiting that system, regexp opens a pattern-based scan
// across the galaxy, and anonymous covers all unidentified contacts. NONE
// means no style has been selected.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized DN-specification style values used in the
 * {@code olcLimits} selector. Each constant controls how the server matches
 * a subject DN against the limit rule.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum DnSpecStyleEnum
{
    EXACT( "exact" ),
    BASE( "base" ),
    ONE( "one" ),
    ONE_LEVEL( "onelevel" ),
    SUB( "sub" ),
    SUBTREE( "subtree" ),
    CHILDREN( "children" ),
    REGEXP( "regexp" ),
    ANONYMOUS( "anonymous" ),
    NONE( "---" );

    /** The associated name */
    private String name;

    // ── CONSTRUCTOR: DnSpecStyleEnum — CHARTING A NAVIGATION STYLE ───────────
    // Each style constant gets the string that the OpenLDAP configuration parser
    // actually reads so we can produce correct configuration output.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its olcLimits configuration-file string.
     *
     * @param name  the dnspec style string value
     */
    private DnSpecStyleEnum( String name )
    {
        this.name = name;
    }

    // ── METHOD: getName — READING THE NAVIGATION CHART ENTRY ─────────────────
    // We return the exact string the server configuration parser expects for
    // this DN-specification style.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the olcLimits configuration-file string for this DN spec style
     * (e.g., {@code "subtree"}).
     *
     * @return the dnspec style string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getStyle — IDENTIFYING A NAVIGATION STYLE FROM TEXT ───────────
    // We scan all constants for a case-insensitive match. If no style matches
    // the supplied string we return NONE so callers always get a safe result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link DnSpecStyleEnum} constant by its configuration-file
     * string using case-insensitive comparison. We return {@link #NONE} if no
     * match is found.
     *
     * @param name  the dnspec style string to look up
     * @return      the matching enum constant, or {@link #NONE}
     */
    public static DnSpecStyleEnum getStyle( String name )
    {
        for ( DnSpecStyleEnum dnSpecStyle : values() )
        {
            if ( dnSpecStyle.name.equalsIgnoreCase( name ) )
            {
                return dnSpecStyle;
            }
        }

        return NONE;
    }


    // ── METHOD: getNames — LISTING ALL NAVIGATION CHART STYLES ───────────────
    // We compile the full list of style strings for combo-box population in
    // the limits-editor UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all dnspec style strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( DnSpecStyleEnum dnSpecStyle : values() )
        {
            names[pos] = dnSpecStyle.name;
            pos++;
        }

        return names;
    }
}
