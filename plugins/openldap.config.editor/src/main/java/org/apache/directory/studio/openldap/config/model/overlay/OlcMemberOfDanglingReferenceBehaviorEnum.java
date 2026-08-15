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
package org.apache.directory.studio.openldap.config.model.overlay;


// ── CLASS: OlcMemberOfDanglingReferenceBehaviorEnum — Mace Windu's Verdict ───
// When Mace Windu corners Palpatine in his office, he faces a choice: ignore the
// threat (IGNORE), quietly let it pass (DROP), or report it as a criminal act (ERROR).
// The memberOf overlay faces the same dilemma when an olcMemberOf value points to
// a group entry that no longer exists — a dangling reference. This enum says what
// to do when that happens: pretend it's fine, silently drop it, or return an error.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the possible values for the {@code olcMemberOfDangling} attribute,
 * which controls how the memberOf overlay handles a dangling reference — when a
 * group entry points at a member DN that no longer exists in the directory.
 * Think of this as Mace Windu's verdict: ignore, drop, or error.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OlcMemberOfDanglingReferenceBehaviorEnum
{
    /** Enum value for 'ignore' */
    IGNORE,

    /** Enum value for 'drop' */
    DROP,

    /** Enum value for 'error' */
    ERROR;

    /** The constant string for 'ignore' */
    private static final String IGNORE_STRING = "ignore";

    /** The constant string for 'drop' */
    private static final String DROP_STRING = "drop";

    /** The constant string for 'error' */
    private static final String ERROR_STRING = "error";


    // ── fromString — Mace Reads the Verdict from the LDAP Attribute ──────────────
    // Mace reads the written verdict from Palpatine's records and translates it into
    // the official judgment (IGNORE, DROP, or ERROR).
    // We parse the olcMemberOfDangling attribute value into the corresponding enum constant.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the string value of the {@code olcMemberOfDangling} attribute into this enum.
     * Comparison is case-insensitive so "Ignore" and "IGNORE" both work.
     *
     * <p>For example — Mace reads the verdict:</p>
     * <pre>
     *   OlcMemberOfDanglingReferenceBehaviorEnum behavior =
     *       OlcMemberOfDanglingReferenceBehaviorEnum.fromString( "error" );
     *   // returns ERROR
     * </pre>
     *
     * @param s  the raw attribute value string (e.g., "ignore", "drop", "error")
     * @return   the matching enum constant, or null if the string doesn't match any value
     */
    public static OlcMemberOfDanglingReferenceBehaviorEnum fromString( String s )
    {
        if ( IGNORE_STRING.equalsIgnoreCase( s ) )
        {
            return IGNORE;
        }
        else if ( DROP_STRING.equalsIgnoreCase( s ) )
        {
            return DROP;
        }
        else if ( ERROR_STRING.equalsIgnoreCase( s ) )
        {
            return ERROR;
        }

        return null;
    }


    // ── toString — Mace Records the Verdict in the Imperial Ledger ───────────────
    // Mace writes the verdict back into the official record in the format the
    // directory server understands — lowercase, as the LDAP attribute expects it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation suitable for writing back into the
     * {@code olcMemberOfDangling} LDAP attribute.
     *
     * <p>For example — Mace records the verdict:</p>
     * <pre>
     *   OlcMemberOfDanglingReferenceBehaviorEnum.ERROR.toString(); // "error"
     *   OlcMemberOfDanglingReferenceBehaviorEnum.DROP.toString();  // "drop"
     * </pre>
     *
     * @return  the lowercase LDAP attribute value string
     */
    @Override
    public String toString()
    {
        switch ( this )
        {
            case IGNORE:
                return IGNORE_STRING;
            case DROP:
                return DROP_STRING;
            case ERROR:
                return ERROR_STRING;
        }

        return super.toString();
    }
}
