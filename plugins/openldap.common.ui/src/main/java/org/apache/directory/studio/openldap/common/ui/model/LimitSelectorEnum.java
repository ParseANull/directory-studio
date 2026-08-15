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


// ── CLASS: LimitSelectorEnum — MOS EISLEY CANTINA PATRON CATEGORIES ──────────
// Picture Wuher the bartender deciding which category of patron gets which
// serving limit: ANY means every being in the cantina, ANONYMOUS covers those
// without credentials, USERS are the authenticated regulars, DNSPEC targets a
// specific patron by their distinguished name, and GROUP applies to an entire
// crew. NONE means no selector has been configured yet.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized selector values for the OpenLDAP
 * {@code olcLimits} directive. Each constant identifies which set of
 * requesting identities a limit rule applies to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum LimitSelectorEnum
{
    ANY( "*" ),
    ANONYMOUS( "anonymous" ),
    USERS( "users" ),
    DNSPEC( "dn" ),
    GROUP( "group" ),
    NONE( "---" );

    /** The associated name */
    private String name;

    // ── CONSTRUCTOR: LimitSelectorEnum — REGISTERING A PATRON CATEGORY ────────
    // Each selector gets its configuration-file token so the server understands
    // which population the limit applies to.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its olcLimits configuration-file token.
     *
     * @param name  the selector token string
     */
    private LimitSelectorEnum( String name )
    {
        this.name = name;
    }


    // ── METHOD: getName — READING THE CATEGORY LABEL ─────────────────────────
    // We return the configuration token exactly as OpenLDAP expects it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the olcLimits configuration-file token for this selector
     * (e.g., {@code "*"} for ANY or {@code "dn"} for DNSPEC).
     *
     * @return the selector token string
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL PATRON CATEGORIES ─────────────────────
    // We collect every selector token into an array for combo-box population.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all selector token strings in declaration order,
     * suitable for populating combo boxes or list controls.
     *
     * @return an array of all enum value name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( LimitSelectorEnum limitSelector : values() )
        {
            names[pos] = limitSelector.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getSelector — IDENTIFYING THE PATRON CATEGORY FROM TEXT ───────
    // We scan all constants for a case-insensitive match and return NONE if
    // the token isn't recognized.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link LimitSelectorEnum} constant by its configuration-file
     * token using case-insensitive comparison. We return {@link #NONE} if no
     * match is found.
     *
     * @param name  the selector token to look up
     * @return      the matching enum constant, or {@link #NONE}
     */
    public static LimitSelectorEnum getSelector( String name )
    {
        for ( LimitSelectorEnum selector : values() )
        {
            if ( selector.name.equalsIgnoreCase( name ) )
            {
                return selector;
            }
        }

        return NONE;
    }
}
