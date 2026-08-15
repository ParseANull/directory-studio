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

// ── CLASS: SaslSecPropEnum — REBEL SHIELD GENERATOR SECURITY PROPERTIES ───────
// Think of the Rebel base's shield generator with its list of security
// properties: noplain forbids transmitting clear credentials over the shield
// frequency, noactive prevents active signal intercept attacks, nodict blocks
// dictionary attacks on the handshake, noanonymous bars unidentified ships,
// forwardsec requires perfect forward secrecy, passcred demands credential
// passing, and minssf/maxssf/maxbufsize tune the encryption strength and
// buffer size. NONE means no extra constraint; UNKNOWN is the fallback for
// unrecognized property names.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized values for the OpenLDAP {@code olcSaslSecProps}
 * parameter. Constants with {@code hasValue = true} require a numeric argument
 * (e.g., {@code minssf=56}); the rest are boolean flags.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SaslSecPropEnum
{
    NONE( "none", false ),
    NO_PLAIN( "noplain", false ),
    NO_ACTIVE( "noactive", false ),
    NO_DICT( "nodict", false ),
    NO_ANONYMOUS( "noanonymous", false ),
    FORWARD_SEC( "forwardsec", false ),
    PASS_CRED( "passcred", false ),
    MIN_SSF( "minssf", true ),
    MAX_SSF( "maxssf", true ),
    MAX_BUF_SIZE( "maxbufsize", true ),
    UNKNOWN( "---", false);

    /** The interned name */
    private String name;

    /** A flag set when the property has a value */
    private boolean hasValue;

    // ── CONSTRUCTOR: SaslSecPropEnum — REGISTERING A SHIELD PROPERTY ─────────
    // Each property gets its configuration string and a flag indicating whether
    // a numeric value must accompany it in the configuration file.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its configuration-file name and a flag
     * indicating whether it requires a numeric argument.
     *
     * @param name      the olcSaslSecProps string value
     * @param hasValue  {@code true} if this property requires a numeric argument
     */
    private SaslSecPropEnum( String name, boolean hasValue )
    {
        this.name = name;
        this.hasValue = hasValue;
    }


    // ── METHOD: getSaslSecProp — IDENTIFYING A SHIELD PROPERTY FROM TEXT ──────
    // We scan all constants for a case-insensitive match. An unrecognized string
    // returns UNKNOWN.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link SaslSecPropEnum} constant by its configuration-file
     * name using case-insensitive comparison. We return {@link #UNKNOWN} if no
     * constant matches.
     *
     * @param name  the property name to look up (e.g., {@code "noplain"})
     * @return      the matching enum constant, or {@link #UNKNOWN}
     */
    public static SaslSecPropEnum getSaslSecProp( String name )
    {
        for ( SaslSecPropEnum saslSecProp : values() )
        {
            if ( saslSecProp.name.equalsIgnoreCase( name ) )
            {
                return saslSecProp;
            }
        }

        return UNKNOWN;
    }


    // ── METHOD: getName — READING THE SHIELD PROPERTY LABEL ──────────────────
    // We return the configuration-file string so callers can embed it directly
    // in generated configuration output.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the olcSaslSecProps configuration-file string for this property
     * (e.g., {@code "noplain"}).
     *
     * @return the property name string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL SHIELD PROPERTIES ─────────────────────
    // We compile every property string into an array for combo-box population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all property name strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( SaslSecPropEnum saslSecProp : values() )
        {
            names[pos] = saslSecProp.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: hasValue — CHECKING IF THE SHIELD PROPERTY NEEDS A SETTING ────
    // Some properties like minssf require a numeric argument; others are simple
    // boolean flags. We expose this flag so the UI can decide whether to show
    // an additional numeric input field.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return {@code true} if this security property requires a numeric
     * argument (e.g., {@code minssf=56}), or {@code false} if it is a
     * standalone flag.
     *
     * @return {@code true} if a numeric value is required
     */
    public boolean hasValue()
    {
        return hasValue;
    }
}
