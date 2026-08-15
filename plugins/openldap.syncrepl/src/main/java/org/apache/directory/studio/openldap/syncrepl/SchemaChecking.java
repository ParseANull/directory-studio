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
package org.apache.directory.studio.openldap.syncrepl;


import java.text.ParseException;


// ── CLASS: SchemaChecking — Whether the Sector Command Validates Intelligence ─
// When a sector command receives intelligence packages from Imperial HQ, it can
// either validate each package against its own schema ("on" — the data officer
// double-checks every record before filing) or accept the package blindly ("off"
// — trust HQ completely, file without checking).  Schema checking on makes
// sense when the schemas might differ; off is faster when you know they match.
// This enum models that on/off toggle — the syncrepl "schemachecking" parameter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code schemachecking} parameter.
 * When {@link #ON}, the consumer validates replicated entries against its
 * local schema before applying them.  When {@link #OFF}, validation is skipped
 * (useful when the provider and consumer have different schemas).
 * Think of this as the data officer choosing whether to verify each intelligence
 * package against the sector command's own records before filing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum SchemaChecking
{
    /** The 'on' schema checking value */
    ON("on"),

    /** The 'off' schema checking value */
    OFF("off");

    /** The value */
    private String value;


    // ── Check Whether Schema Validation is On or Off ──────────────────────────
    // The parser matches the incoming string case-insensitively and returns
    // the ON or OFF constant, or throws ParseException for anything else.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a schema checking string into the corresponding enum constant.
     *
     * @param s  the string to parse — {@code "on"} or {@code "off"} (case-insensitive).
     * @return   the matching {@link SchemaChecking} constant.
     * @throws ParseException  if {@code s} is neither {@code "on"} nor {@code "off"}.
     */
    public static SchemaChecking parse( String s ) throws ParseException
    {
        // ON
        if ( ON.value.equalsIgnoreCase( s ) )
        {
            return ON;
        }
        // OFF
        else if ( OFF.value.equalsIgnoreCase( s ) )
        {
            return OFF;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid schema checking.", 0 );
        }
    }


    // ── Create the Constant with Its Config Token ─────────────────────────────
    // Each constant stores its exact lowercase token for use in the config file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a SchemaChecking constant with its config directive token.
     *
     * @param value  the lowercase token — {@code "on"} or {@code "off"}.
     */
    private SchemaChecking( String value )
    {
        this.value = value;
    }


    // ── Write the Flag Back into the Configuration ────────────────────────────
    // Returns the exact token used in the OpenLDAP configuration file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the config file token for this schema checking flag.
     *
     * @return  {@code "on"} or {@code "off"}.
     */
    public String toString()
    {
        return value;
    }
}
