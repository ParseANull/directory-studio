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


// ── CLASS: SsfFeatureEnum — REBEL SHIELD FEATURE SYSTEMS ─────────────────────
// The Rebel Alliance's shield network is made up of individual subsystems:
// the overall SSF (Security Strength Factor) umbrella, the transport-layer
// shield, the TLS encrypted layer, the SASL authentication channel, and the
// simple-bind frequency guard. The "update_*" variants apply the same strength
// requirements specifically to write (update) operations. NONE means no
// specific feature has been selected.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized Security Strength Factor feature names used in
 * OpenLDAP's security strength configuration directives. Each constant maps to
 * the configuration-file string for that feature.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SsfFeatureEnum
{
    SSF( "ssf" ),
    TRANSPORT( "transport" ),
    TLS( "tls" ),
    SASL( "sasl" ),
    UPDATE_SSF( "update_ssf" ),
    UPDATE_TRANSPORT( "update_transport" ),
    UPDATE_TLS( "update_tls" ),
    UPDATE_SASL( "update_sasl" ),
    SIMPLE_BIND( "simple_bind" ),
    NONE( "---" );

    /** The associated name */
    private String name;

    // ── CONSTRUCTOR: SsfFeatureEnum — REGISTERING A SHIELD SUBSYSTEM ─────────
    // Each feature constant gets its configuration string so we can produce
    // accurate configuration output.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its SSF configuration-file string.
     *
     * @param name  the SSF feature string value
     */
    private SsfFeatureEnum( String name )
    {
        this.name = name;
    }

    // ── METHOD: getName — READING THE SHIELD SUBSYSTEM LABEL ─────────────────
    // We return the configuration string so callers can embed it in security
    // strength configuration directives.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the SSF configuration-file string for this feature
     * (e.g., {@code "tls"} or {@code "update_sasl"}).
     *
     * @return the SSF feature string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL SHIELD SUBSYSTEMS ─────────────────────
    // We compile all feature strings into an array for combo-box population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all SSF feature strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( SsfFeatureEnum ssfFeature : values() )
        {
            names[pos] = ssfFeature.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getSsfFeature — IDENTIFYING A SHIELD SUBSYSTEM FROM TEXT ──────
    // We scan all constants for a case-insensitive match and return NONE if the
    // string isn't recognized.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link SsfFeatureEnum} constant by its configuration-file
     * string using case-insensitive comparison. We return {@link #NONE} if no
     * match is found.
     *
     * @param name  the SSF feature string to look up
     * @return      the matching enum constant, or {@link #NONE}
     */
    public static SsfFeatureEnum getSsfFeature( String name )
    {
        for ( SsfFeatureEnum ssfFeature : values() )
        {
            if ( ssfFeature.name.equalsIgnoreCase( name ) )
            {
                return ssfFeature;
            }
        }

        return NONE;
    }
}
