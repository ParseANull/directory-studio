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


// ── CLASS: Scope — How Much of the Imperial Directory Tree to Replicate ───────
// A sector command doesn't necessarily need the entire Imperial Intelligence
// directory — it might only need the top-level dossier ("base"), just the
// immediate sub-dossiers ("one"), or the entire subtree ("sub").  The "subord"
// scope is a variant of "sub" that excludes the base entry itself.
// This enum models that LDAP search scope choice, which is the syncrepl
// "scope" parameter controlling which entries are replicated.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All valid values for the syncrepl {@code scope} parameter.
 * The scope controls which entries under the search base are replicated:
 * {@link #BASE} replicates only the base entry; {@link #ONE} replicates
 * immediate children; {@link #SUB} replicates the full subtree;
 * {@link #SUBORD} replicates the subtree excluding the base entry.
 * Think of this as telling the sector command how much of the Imperial
 * intelligence tree to mirror: just the cover page, one folder down,
 * or the entire filing cabinet.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum Scope
{
    /** The 'sub' scope value */
    SUB("sub"),

    /** The 'one' scope value */
    ONE("one"),

    /** The 'base' scope value */
    BASE("base"),

    /** The 'subord' scope value */
    SUBORD("subord");

    /** The value */
    private String value;


    // ── Select the Right Replication Coverage ────────────────────────────────
    // The parser matches the incoming scope name case-insensitively and returns
    // the correct constant, or throws ParseException for anything unrecognised.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses a scope string into the corresponding enum constant.
     *
     * @param s  the scope string — one of {@code "sub"}, {@code "one"},
     *           {@code "base"}, or {@code "subord"} (case-insensitive).
     * @return   the matching {@link Scope} constant.
     * @throws ParseException  if {@code s} is not a recognised scope value.
     */
    public static Scope parse( String s ) throws ParseException
    {
        // SUB
        if ( SUB.value.equalsIgnoreCase( s ) )
        {
            return SUB;
        }
        // ONE
        else if ( ONE.value.equalsIgnoreCase( s ) )
        {
            return ONE;
        }
        // BASE
        else if ( BASE.value.equalsIgnoreCase( s ) )
        {
            return BASE;
        }
        // SUBORD
        else if ( SUBORD.value.equalsIgnoreCase( s ) )
        {
            return SUBORD;
        }
        else
        {
            throw new ParseException( "Unable to parse string '" + s + "' as a valid scope.", 0 );
        }
    }


    // ── Create the Constant with Its Config Token ─────────────────────────────
    // Each constant carries its exact lowercase token for round-trip serialisation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a Scope constant with its config directive token.
     *
     * @param value  the lowercase token as it appears in the syncrepl directive.
     */
    private Scope( String value )
    {
        this.value = value;
    }


    // ── Write the Scope Back into the Configuration ───────────────────────────
    // Returns the exact token used in the OpenLDAP configuration file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the config file token for this scope value.
     *
     * @return  one of {@code "sub"}, {@code "one"}, {@code "base"}, {@code "subord"}.
     */
    public String toString()
    {
        return value;
    }
}
