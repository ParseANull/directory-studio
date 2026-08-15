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


// ── CLASS: DnSpecTypeEnum — JEDI NAVIGATOR REFERENCE POINTS ──────────────────
// When a Jedi navigator sets a bearing they choose a reference point: "self"
// means the Jedi's own ship is the anchor (their own DN), "this" means the
// current object being accessed is the anchor. NONE means no reference point
// has been chosen yet. Together with DnSpecStyleEnum, these two constants
// fully describe how a limit selector's DN is interpreted.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate the DN-specification type values used in the {@code olcLimits}
 * selector. Each constant identifies whether the DN anchor is the requesting
 * identity ("self") or the accessed object ("this").
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum DnSpecTypeEnum
{
    SELF( "self" ),
    THIS( "this" ),
    NONE( "---" );

    /** The associated name */
    private String name;

    // ── CONSTRUCTOR: DnSpecTypeEnum — MARKING THE NAVIGATION ANCHOR ──────────
    // Each type constant gets its configuration string so the server can
    // interpret limit rules correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its olcLimits configuration-file string.
     *
     * @param name  the dnspec type string value
     */
    private DnSpecTypeEnum( String name )
    {
        this.name = name;
    }

    // ── METHOD: getName — READING THE ANCHOR LABEL ────────────────────────────
    // We return the exact string the server configuration parser expects for
    // this DN-specification type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the olcLimits configuration-file string for this DN spec type
     * (e.g., {@code "self"}).
     *
     * @return the dnspec type string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getType — IDENTIFYING THE ANCHOR TYPE FROM TEXT ──────────────
    // We scan all constants for a case-insensitive match and return NONE if
    // the supplied string doesn't match any known type.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link DnSpecTypeEnum} constant by its configuration-file
     * string using case-insensitive comparison. We return {@link #NONE} if no
     * match is found.
     *
     * @param name  the dnspec type string to look up
     * @return      the matching enum constant, or {@link #NONE}
     */
    public static DnSpecTypeEnum getType( String name )
    {
        for ( DnSpecTypeEnum dnSpecType : values() )
        {
            if ( dnSpecType.name.equalsIgnoreCase( name ) )
            {
                return dnSpecType;
            }
        }

        return NONE;
    }


    // ── METHOD: getNames — LISTING ALL ANCHOR TYPE LABELS ────────────────────
    // We compile the full list for combo-box population in the limits editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all dnspec type strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( DnSpecTypeEnum dnSpecType : values() )
        {
            names[pos] = dnSpecType.name;
            pos++;
        }

        return names;
    }
}
