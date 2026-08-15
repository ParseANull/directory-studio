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

// ── CLASS: RequireConditionEnum — JEDI TEMPLE ENTRY REQUIREMENTS ──────────────
// Picture the Jedi Temple gate with its list of entry conditions: visitors
// must be authenticated (authc), must have already performed a bind, must
// speak the ancient LDAPv3 protocol, must use SASL encryption, must present
// strong credentials, or the gate may be open to all (none). UNKNOWN is the
// fallback when a condition code isn't recognized.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all valid values of the OpenLDAP {@code olcRequires} parameter.
 * Each constant specifies one condition that clients must satisfy before the
 * server allows certain operations. The possible values are:
 * <ul>
 * <li>authc</li>
 * <li>bind</li>
 * <li>LDAPv3</li>
 * <li>none</li>
 * <li>sasl</li>
 * <li>strong</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum RequireConditionEnum
{
    UNKNOWN( "---" ),
    AUTHC( "authc" ),
    BIND( "bind" ),
    LDAP_V3( "LDAPv3" ),
    NONE( "none" ),
    SASL( "sasl" ),
    STRONG( "strong" );

    /** The interned name */
    private String name;

    // ── CONSTRUCTOR: RequireConditionEnum — POSTING AN ENTRY REQUIREMENT ──────
    // Each condition gets its configuration string so we can write it back into
    // the OpenLDAP configuration faithfully.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its OpenLDAP configuration-file string.
     *
     * @param name  the olcRequires string value for this condition
     */
    private RequireConditionEnum( String name )
    {
        this.name = name;
    }


    // ── METHOD: getName — READING THE ENTRY REQUIREMENT LABEL ────────────────
    // We return the exact string the server configuration parser expects for
    // this requirement condition.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the OpenLDAP configuration-file string for this requirement
     * condition (e.g., {@code "sasl"}).
     *
     * @return the olcRequires string value
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL ENTRY REQUIREMENTS ────────────────────
    // We collect all condition strings into an array for combo-box population
    // in the server-configuration editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all condition name strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( RequireConditionEnum requireCondition : values() )
        {
            names[pos] = requireCondition.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getCondition(int) — IDENTIFYING A REQUIREMENT BY ORDINAL ──────
    // When we have a position index we hand back the matching constant.
    // Out-of-range indices return UNKNOWN.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link RequireConditionEnum} at the given ordinal position
     * in the values array. If the number is out of range we return
     * {@link #UNKNOWN}.
     *
     * @param number  the ordinal index to look up
     * @return        the matching enum constant, or {@link #UNKNOWN}
     */
    public static RequireConditionEnum getCondition( int number )
    {
        RequireConditionEnum[] values = RequireConditionEnum.values();

        if ( ( number > 0 ) && ( number < values.length ) )
        {
            return values[number];
        }
        else
        {
            return UNKNOWN;
        }
    }


    // ── METHOD: getCondition(String) — SCANNING THE ENTRY REQUIREMENTS LIST ───
    // We do a case-insensitive scan of all constants. An unrecognized string
    // returns UNKNOWN.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link RequireConditionEnum} constant by its configuration-
     * file string using case-insensitive comparison. We return {@link #UNKNOWN}
     * if no constant matches.
     *
     * @param name  the condition name to look up (e.g., {@code "authc"})
     * @return      the matching enum constant, or {@link #UNKNOWN}
     */
    public static RequireConditionEnum getCondition( String name )
    {
        for ( RequireConditionEnum requireCondition : values() )
        {
            if ( requireCondition.name.equalsIgnoreCase( name ) )
            {
                return requireCondition;
            }
        }

        return UNKNOWN;
    }


    // ── METHOD: toString — PRINTING THE ENTRY REQUIREMENT LABEL ──────────────
    // We return the configuration string so this enum can be used wherever a
    // String is expected, such as in list cell renderers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return name;
    }
}
