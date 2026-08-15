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

// ── CLASS: RestrictOperationEnum — IMPERIAL BLOCKADE OPERATION RESTRICTIONS ───
// Picture Grand Admiral Thrawn setting the blockade rules: all traffic blocked
// (ALL), only adds refused (ADD), searches quarantined (SEARCH), extended
// operations like START_TLS denied (EXTENDED_START_TLS), and so on. Each
// constant here represents one LDAP operation that the administrator can
// forbid via the olcRestrict parameter. UNKNOWN is the fallback for any
// unrecognized restriction code.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all valid values of the OpenLDAP {@code olcRestrict} parameter.
 * Each constant carries both its wire-format name (used in configuration) and
 * a shorter external label (used in the UI). The possible values are:
 * <ul>
 * <li>add</li>
 * <li>all</li>
 * <li>bind</li>
 * <li>compare</li>
 * <li>delete</li>
 * <li>extended</li>
 * <li>extended=1.3.6.1.4.1.1466.20037</li>
 * <li>extended=1.3.6.1.4.1.4203.1.11.1</li>
 * <li>extended=1.3.6.1.4.1.4203.1.11.3</li>
 * <li>extended=1.3.6.1.1.8</li>
 * <li>modify</li>
 * <li>modrdn</li>
 * <li>read</li>
 * <li>rename</li>
 * <li>search</li>
 * <li>write</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum RestrictOperationEnum
{
    UNKNOWN( "---", "" ),
    ADD( "add", "add" ),
    ALL( "all", "all" ),
    BIND( "bind", "bind" ),
    COMPARE( "compare", "compare" ),
    DELETE( "delete", "delete" ),
    EXTENDED( "extended", "extended" ),
    EXTENDED_START_TLS( "extended=1.3.6.1.4.1.1466.20037", "START_TLS" ),
    EXTENDED_MODIFY_PASSWD( "extended=1.3.6.1.4.1.4203.1.11.1", "MODIFY_PASSWORD" ),
    EXTENDED_WHOAMI( "extended=1.3.6.1.4.1.4203.1.11.3", "WHOAMI" ),
    EXTENDED_CANCEL( "extended=1.3.6.1.1.8", "CANCEL" ),
    MODIFY( "modify", "modify" ),
    MODRDN( "modrdn", "modrdn" ),
    READ( "read", "read" ),
    RENAME( "rename", "rename" ),
    SEARCH( "search", "search" ),
    WRITE( "write", "write" );

    /** The interned name */
    private String name;

    /** The externalized name */
    private String externalName;

    // ── CONSTRUCTOR: RestrictOperationEnum — POSTING A BLOCKADE ORDER ────────
    // Each restriction gets two labels: the full wire-format name that goes
    // into the configuration file and a shorter external name displayed in the
    // UI so users don't have to read raw OIDs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its configuration-file name and its
     * shorter UI display name.
     *
     * @param name          the full olcRestrict wire-format string
     * @param externalName  the short UI display label
     */
    private RestrictOperationEnum( String name, String externalName )
    {
        this.name = name;
        this.externalName = externalName;
    }


    // ── METHOD: getName — READING THE BLOCKADE ORDER WIRE FORMAT ─────────────
    // We return the full configuration-file string so callers can embed it
    // directly in generated olcRestrict attribute values.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the full olcRestrict wire-format string for this operation
     * (e.g., {@code "extended=1.3.6.1.4.1.1466.20037"}).
     *
     * @return the wire-format configuration string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL BLOCKADE ORDER WIRE FORMATS ───────────
    // We collect all wire-format strings into an array for combo-box population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all wire-format name strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum wire-format name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( RestrictOperationEnum restrictOperation : values() )
        {
            names[pos] = restrictOperation.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getExternalName — READING THE SHORT UI LABEL ─────────────────
    // We return the human-friendly short name so the UI can display "START_TLS"
    // instead of the full OID-qualified extended operation string.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the short UI display label for this operation
     * (e.g., {@code "START_TLS"} or {@code "search"}).
     *
     * @return the external display name
     */
    public String getExternalName()
    {
        return externalName;
    }


    // ── METHOD: getOperation(int) — LOOKING UP A RESTRICTION BY ORDINAL ───────
    // When we have an ordinal index we hand back the matching constant.
    // Out-of-range indices return UNKNOWN.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link RestrictOperationEnum} at the given ordinal position
     * in the values array. If the number is out of range we return
     * {@link #UNKNOWN}.
     *
     * @param number  the ordinal index to look up
     * @return        the matching enum constant, or {@link #UNKNOWN}
     */
    public static RestrictOperationEnum getOperation( int number )
    {
        RestrictOperationEnum[] values = RestrictOperationEnum.values();

        if ( ( number > 0 ) && ( number < values.length ) )
        {
            return values[number];
        }
        else
        {
            return UNKNOWN;
        }
    }


    // ── METHOD: getRestrictOperation — IDENTIFYING A RESTRICTION FROM TEXT ────
    // We scan all constants for a case-insensitive match on the wire-format
    // name. An unrecognized string returns UNKNOWN.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link RestrictOperationEnum} constant by its wire-format
     * name string using case-insensitive comparison. We return {@link #UNKNOWN}
     * if no constant matches.
     *
     * @param name  the operation name to look up
     * @return      the matching enum constant, or {@link #UNKNOWN}
     */
    public static RestrictOperationEnum getRestrictOperation( String name )
    {
        for ( RestrictOperationEnum restrictOperation : values() )
        {
            if ( restrictOperation.name.equalsIgnoreCase( name ) )
            {
                return restrictOperation;
            }
        }

        return UNKNOWN;
    }


    // ── METHOD: toString — PRINTING THE SHORT UI LABEL ───────────────────────
    // We return the external name so this enum works naturally in list renderers
    // and anywhere else a String is expected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return externalName;
    }
}
