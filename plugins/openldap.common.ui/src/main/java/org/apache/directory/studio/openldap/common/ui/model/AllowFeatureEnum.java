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

// ── CLASS: AllowFeatureEnum — REBEL ALLIANCE CLEARANCE LEVELS ────────────────
// Think of Mon Mothma handing out different security clearances to Rebel
// operatives: bind_v2 lets older droids connect, bind_anon allows anonymous
// scouts to enter the base, update_anon lets them even modify mission plans.
// Each constant here represents one specific capability that the server
// administrator is willing to permit — "UNKNOWN" means the clearance code
// wasn't recognized at the checkpoint.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all valid values of the OpenLDAP {@code olcAllows} parameter.
 * Each constant maps to one feature the server can be configured to permit.
 * The possible values are:
 * <ul>
 * <li>bind_v2</li>
 * <li>bind_anon_cred</li>
 * <li>bind_anon_dn</li>
 * <li>update_anon</li>
 * <li>proxy_authz_anon</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AllowFeatureEnum
{
    UNKNOWN( "---" ),
    BIND_ANON_CRED( "bind_anon_cred" ),
    BIND_ANON_DN( "bind_anon_dn" ),
    BIND_V2( "bind_v2" ),
    PROXY_AUTHZ_ANON( "proxy_authz_anon" ),
    UPDATE_ANON( "update_anon" );

    /** The interned name */
    private String name;

    // ── CONSTRUCTOR: AllowFeatureEnum — ISSUING A CLEARANCE BADGE ────────────
    // Each enum constant gets its configuration-file name stamped on its badge
    // so we can convert back and forth between the Java enum and the string
    // that OpenLDAP actually understands.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We create each constant with its OpenLDAP configuration-file string name
     * so we can round-trip between Java and the server's configuration format.
     *
     * @param name  the olcAllows string value for this feature
     */
    private AllowFeatureEnum( String name )
    {
        this.name = name;
    }


    // ── METHOD: getName — READING THE BADGE ───────────────────────────────────
    // We return the clearance code exactly as OpenLDAP expects to see it in
    // its configuration file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the OpenLDAP configuration-file string for this feature (e.g.,
     * {@code "bind_v2"}).
     *
     * @return the olcAllows string value
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL CLEARANCE CODES ───────────────────────
    // We compile the full roster of recognized clearance codes so UI components
    // (like combo boxes) can display every option without knowing the enum
    // internals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all clearance-code strings in declaration order,
     * useful for populating combo boxes or other list-based UI controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( AllowFeatureEnum allowFeature : values() )
        {
            names[pos] = allowFeature.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getAllowFeature(int) — LOOKING UP A CLEARANCE BY SERIAL NUMBER ─
    // When we have an index into the enum values array we hand back the matching
    // constant. Out-of-range numbers return UNKNOWN so callers always get a
    // safe, non-null result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link AllowFeatureEnum} at the given ordinal position in
     * the values array. If the number is out of range we return {@link #UNKNOWN}.
     *
     * @param number  the ordinal index to look up
     * @return        the matching enum constant, or {@link #UNKNOWN}
     */
    public static AllowFeatureEnum getAllowFeature( int number )
    {
        AllowFeatureEnum[] values = AllowFeatureEnum.values();

        if ( ( number > 0 ) && ( number < values.length ) )
        {
            return values[number];
        }
        else
        {
            return UNKNOWN;
        }
    }


    // ── METHOD: getAllowFeature(String) — SCANNING THE BADGE AT THE GATE ──────
    // We do a case-insensitive scan of all constants looking for the one whose
    // name matches the supplied string. If nothing matches we hand back UNKNOWN
    // so the caller always gets a valid, non-null result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up an {@link AllowFeatureEnum} constant by its configuration-file
     * name, using case-insensitive comparison. We return {@link #UNKNOWN} if no
     * constant matches.
     *
     * @param name  the feature name to look up (e.g., {@code "bind_v2"})
     * @return      the matching enum constant, or {@link #UNKNOWN}
     */
    public static AllowFeatureEnum getAllowFeature( String name )
    {
        for ( AllowFeatureEnum allowFeature : values() )
        {
            if ( allowFeature.name.equalsIgnoreCase( name ) )
            {
                return allowFeature;
            }
        }

        return UNKNOWN;
    }


    // ── METHOD: toString — PRINTING THE CLEARANCE BADGE ──────────────────────
    // We return the configuration-file string so this enum can be used directly
    // anywhere a String is expected, such as in list cell renderers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return name;
    }
}
