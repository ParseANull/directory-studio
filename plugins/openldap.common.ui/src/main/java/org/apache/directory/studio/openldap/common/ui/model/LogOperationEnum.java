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


// ── CLASS: LogOperationEnum — REBEL STARFIGHTER MISSION TYPE CODES ────────────
// Rebel squadron commanders use mission codes to classify every sortie:
// WRITES covers all attack runs, ADD is a new strike, DELETE is a takedown,
// MODIFY is a course correction, MODIFY_RDN renames a target, READS covers
// reconnaissance, COMPARE is target verification, SEARCH is an active sweep,
// SESSION tracks active squadron connections, ABANDON aborts a mission in
// flight, BIND opens a secure comm channel, UNBIND closes it, and ALL means
// log every single sortie regardless of type.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized OpenLDAP access-log operation types. Each
 * constant maps to the string used in the {@code olcAccessLogOps} configuration
 * attribute.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum LogOperationEnum
{
    WRITES( "writes" ),
    ADD( "add" ),
    DELETE( "delete" ),
    MODIFY( "modify" ),
    MODIFY_RDN( "modrdn" ),
    READS( "reads" ),
    COMPARE( "compare" ),
    SEARCH( "search" ),
    SESSION( "session" ),
    ABANDON( "abandon" ),
    BIND( "bind" ),
    UNBIND( "unbind" ),
    ALL( "all" );

    /** The name */
    private String name;


    // ── CONSTRUCTOR: LogOperationEnum — REGISTERING A MISSION CODE ────────────
    // Each operation gets the string the access-log overlay understands so we
    // can produce correct configuration output.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its access-log operation string.
     *
     * @param name  the olcAccessLogOps string value for this operation
     */
    private LogOperationEnum( String name )
    {
        this.name = name;
    }


    // ── METHOD: getName — READING THE MISSION CODE LABEL ─────────────────────
    // We return the access-log string so callers can embed it in configuration
    // or display it in the UI.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the access-log operation string for this constant
     * (e.g., {@code "search"}).
     *
     * @return the operation name string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL MISSION CODES ─────────────────────────
    // We assemble all operation strings into an array for combo-box population
    // in the access-log configuration editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all operation name strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( LogOperationEnum logOperation : values() )
        {
            names[pos] = logOperation.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: fromString — DECODING A MISSION CODE FROM TEXT ───────────────
    // We match the supplied string case-insensitively against every known
    // operation name. We return null if no match is found so callers can
    // distinguish an unrecognized value from a legitimate null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link LogOperationEnum} constant by its access-log string
     * using case-insensitive comparison. We return {@code null} if the input
     * is null or no constant matches (callers should handle null defensively).
     *
     * @param s  the operation name string to look up
     * @return   the matching enum constant, or {@code null}
     */
    public static LogOperationEnum fromString( String s )
    {
        if ( s != null )
        {
            if ( s.equalsIgnoreCase( WRITES.name ) )
            {
                return WRITES;
            }
            else if ( s.equalsIgnoreCase( ADD.name ) )
            {
                return ADD;
            }
            else if ( s.equalsIgnoreCase( DELETE.name ) )
            {
                return DELETE;
            }
            else if ( s.equalsIgnoreCase( MODIFY.name ) )
            {
                return MODIFY;
            }
            else if ( s.equalsIgnoreCase( MODIFY_RDN.name ) )
            {
                return MODIFY_RDN;
            }
            else if ( s.equalsIgnoreCase( READS.name ) )
            {
                return READS;
            }
            else if ( s.equalsIgnoreCase( COMPARE.name ) )
            {
                return COMPARE;
            }
            else if ( s.equalsIgnoreCase( SEARCH.name ) )
            {
                return SEARCH;
            }
            else if ( s.equalsIgnoreCase( SESSION.name ) )
            {
                return SESSION;
            }
            else if ( s.equalsIgnoreCase( ABANDON.name ) )
            {
                return ABANDON;
            }
            else if ( s.equalsIgnoreCase( BIND.name ) )
            {
                return BIND;
            }
            else if ( s.equalsIgnoreCase( UNBIND.name ) )
            {
                return UNBIND;
            }
            else if ( s.equalsIgnoreCase( ALL.name ) )
            {
                return ALL;
            }
        }

        return null;
    }
}
