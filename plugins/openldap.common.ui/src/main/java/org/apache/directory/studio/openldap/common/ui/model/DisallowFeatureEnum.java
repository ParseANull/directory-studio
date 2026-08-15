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

// ── CLASS: DisallowFeatureEnum — IMPERIAL SECURITY RESTRICTIONS ───────────────
// Picture a Death Star security bulletin listing all the maneuvers that are
// explicitly banned aboard the station: anonymous binds, simple credentials,
// TLS-to-anonymous downgrades, non-critical proxy extensions, and so on.
// Each constant here is one capability the administrator explicitly forbids —
// the mirror image of AllowFeatureEnum. UNKNOWN is the default when a disallow
// code isn't recognized at the checkpoint.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all valid values of the OpenLDAP {@code olcDisallows} parameter.
 * Each constant maps to one feature the server can be configured to explicitly
 * prohibit. The possible values are:
 * <ul>
 * <li>bind_anon</li>
 * <li>bind_simple</li>
 * <li>tls_2_anon</li>
 * <li>tls_authc</li>
 * <li>proxy_authz_non_critical</li>
 * <li>dontusecopy_non_critical</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum DisallowFeatureEnum
{
    UNKNOWN( "---" ),
    BIND_ANON( "bind_anon" ),
    BIND_SIMPLE( "bind_simple" ),
    TLS_2_ANON( "tls_2_anon" ),
    TLS_AUTHC( "tls_authc" ),
    PROXY_AUTHZ_NON_CRITICAL( "proxy_authz_non_critical" ),
    DONTUSECOPY_NON_CRITICAL( "dontusecopy_non_critical" );

    /** The interned name */
    private String name;

    // ── CONSTRUCTOR: DisallowFeatureEnum — POSTING A BANNED MANEUVER ─────────
    // Each prohibition gets its configuration-file name so we can read the
    // server's config and write it back faithfully without translation errors.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its OpenLDAP configuration-file string
     * so we can round-trip between Java and the server's configuration format.
     *
     * @param name  the olcDisallows string value for this feature
     */
    private DisallowFeatureEnum( String name )
    {
        this.name = name;
    }


    // ── METHOD: getName — READING THE PROHIBITION LABEL ──────────────────────
    // We return the exact string that OpenLDAP recognizes in its configuration
    // file so callers can embed it directly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the OpenLDAP configuration-file string for this disallow
     * feature (e.g., {@code "bind_anon"}).
     *
     * @return the olcDisallows string value
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL BANNED MANEUVERS ──────────────────────
    // We collect every disallow string into an array for combo-box population
    // in the UI editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all disallow-feature name strings in declaration
     * order, suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( DisallowFeatureEnum disallowFeature : values() )
        {
            names[pos] = disallowFeature.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getFeature(int) — LOOKING UP A PROHIBITION BY SERIAL NUMBER ───
    // When we have an ordinal index we hand back the matching constant. Out-of-
    // range indices return UNKNOWN so callers always get a safe non-null result.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the {@link DisallowFeatureEnum} at the given ordinal position in
     * the values array. If the number is out of range we return {@link #UNKNOWN}.
     *
     * @param number  the ordinal index to look up
     * @return        the matching enum constant, or {@link #UNKNOWN}
     */
    public static DisallowFeatureEnum getFeature( int number )
    {
        DisallowFeatureEnum[] values = DisallowFeatureEnum.values();

        if ( ( number > 0 ) && ( number < values.length ) )
        {
            return values[number];
        }
        else
        {
            return UNKNOWN;
        }
    }


    // ── METHOD: getFeature(String) — SCANNING THE BULLETIN FOR A MATCH ────────
    // We do a case-insensitive scan of all constants. If no name matches the
    // supplied string we return UNKNOWN — the bulletin doesn't list that action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link DisallowFeatureEnum} constant by its configuration-
     * file name using case-insensitive comparison. We return {@link #UNKNOWN}
     * if no constant matches.
     *
     * @param name  the feature name to look up (e.g., {@code "bind_anon"})
     * @return      the matching enum constant, or {@link #UNKNOWN}
     */
    public static DisallowFeatureEnum getFeature( String name )
    {
        for ( DisallowFeatureEnum disallowFeature : values() )
        {
            if ( disallowFeature.name.equalsIgnoreCase( name ) )
            {
                return disallowFeature;
            }
        }

        return UNKNOWN;
    }


    // ── METHOD: toString — PRINTING THE PROHIBITION LABEL ────────────────────
    // We return the configuration string so this enum can be used directly
    // anywhere a String is expected, such as in list renderers.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return name;
    }
}
