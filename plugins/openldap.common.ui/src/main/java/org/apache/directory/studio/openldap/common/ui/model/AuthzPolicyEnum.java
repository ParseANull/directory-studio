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

// ── CLASS: AuthzPolicyEnum — JEDI HIGH COUNCIL AUTHORIZATION POLICY ──────────
// Picture the Jedi High Council deciding the rules for who can act on behalf
// of whom: "none" means no proxy identities are accepted, "from" allows the
// source to grant authority, "to" restricts to the destination, "both" covers
// both directions, and "any" or "all" throw open every gate. Each constant
// here is one policy mode the administrator can select for the OpenLDAP
// olcAuthzPolicy setting.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all valid values of the OpenLDAP {@code olcAuthzPolicy}
 * parameter. Each constant controls how the server handles authorization
 * identity proxying. The possible values are:
 * <ul>
 * <li>none</li>
 * <li>from</li>
 * <li>to</li>
 * <li>any</li>
 * <li>all</li>
 * <li>both</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AuthzPolicyEnum
{
    ALL( "all" ),
    ANY( "any" ),
    BOTH( "both" ),
    FROM( "from" ),
    NONE( "none" ),
    TO( "to" ),
    UNKNOWN( "---" );

    /** The interned name */
    private String name;

    // ── CONSTRUCTOR: AuthzPolicyEnum — RECORDING THE COUNCIL DECREE ──────────
    // Each policy mode carries the exact string the Council (and OpenLDAP)
    // recognizes so we can faithfully write it back to the configuration file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its OpenLDAP configuration-file string so
     * we can serialize and deserialize the policy mode without losing precision.
     *
     * @param name  the olcAuthzPolicy string value
     */
    private AuthzPolicyEnum( String name )
    {
        this.name = name;
    }


    // ── METHOD: getAuthzPolicy — READING THE COUNCIL DECREE FROM A STRING ─────
    // We perform a case-insensitive lookup across all constants. If the supplied
    // string doesn't match any known policy mode we return UNKNOWN so callers
    // always receive a valid, non-null result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up an {@link AuthzPolicyEnum} constant by its configuration-file
     * name using case-insensitive comparison. We return {@link #UNKNOWN} if no
     * constant matches.
     *
     * @param name  the policy name to look up (e.g., {@code "from"})
     * @return      the matching enum constant, or {@link #UNKNOWN}
     */
    public static AuthzPolicyEnum getAuthzPolicy( String name )
    {
        for ( AuthzPolicyEnum authzPolicy : values() )
        {
            if ( authzPolicy.name.equalsIgnoreCase( name ) )
            {
                return authzPolicy;
            }
        }

        return UNKNOWN;
    }


    // ── METHOD: getNames — LISTING ALL COUNCIL DECREES ───────────────────────
    // We collect every policy string into an array so UI components can present
    // the full list of choices without needing to know the enum structure.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all policy-name strings in declaration order,
     * useful for populating combo boxes or other list-based UI controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( AuthzPolicyEnum authzPolicy : values() )
        {
            names[pos] = authzPolicy.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getName — READING THE DECREE TEXT ─────────────────────────────
    // We return the exact OpenLDAP configuration string for this policy mode
    // so callers can embed it directly in generated configuration files.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the OpenLDAP configuration-file string for this authorization
     * policy (e.g., {@code "from"}).
     *
     * @return the olcAuthzPolicy string value
     */
    public String getName()
    {
        return name;
    }
}
