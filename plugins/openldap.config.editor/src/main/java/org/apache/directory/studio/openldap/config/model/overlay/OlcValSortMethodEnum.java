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


// ── CLASS: OlcValSortMethodEnum — Lando Chooses the Ledger Ordering Algorithm ─
// Lando has four ways to sort his Cloud City trade records: alphabetically A-Z,
// alphabetically Z-A, numerically smallest-first, or numerically largest-first.
// He picks the right one depending on what makes the ledger most useful.
// The valsort overlay has the same four sort modes for LDAP attribute values:
// alpha-ascend, alpha-descend, numeric-ascend, numeric-descend.
// This enum encodes that choice as a Java constant that the configuration model
// and the I/O layer both understand.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the sort method for an {@code olcValSortAttr} rule, specifying how
 * the valsort overlay orders multi-valued attribute values in search results.
 * Think of this as Lando choosing the ordering algorithm for one of his ledgers.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OlcValSortMethodEnum
{
    /** Enum value for 'alpha-ascend' */
    ALPHA_ASCEND,

    /** Enum value for 'alpha-descend' */
    ALPHA_DESCEND,

    /** Enum value for 'numeric-ascend' */
    NUMERIC_ASCEND,

    /** Enum value for 'numeric-descend' */
    NUMERIC_DESCEND;

    /** The constant string for 'alpha-ascend' */
    private static final String ALPHA_ASCEND_STRING = "alpha-ascend";

    /** The constant string for 'alpha-descend' */
    private static final String ALPHA_DESCEND_STRING = "alpha-descend";

    /** The constant string for 'numeric-ascend' */
    private static final String NUMERIC_ASCEND_STRING = "numeric-ascend";

    /** The constant string for 'numeric-descend' */
    private static final String NUMERIC_DESCEND_STRING = "numeric-descend";


    // ── fromString — Lando Reads the Sort Method from the Ledger Entry ────────────
    // Lando reads the sorting keyword from the raw ledger entry and translates it
    // into the corresponding algorithm constant.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the sort method keyword from an olcValSortAttr rule string.
     * Comparison is case-insensitive ("Alpha-Ascend" and "alpha-ascend" both work).
     *
     * <p>For example — Lando reads the sort method:</p>
     * <pre>
     *   OlcValSortMethodEnum method =
     *       OlcValSortMethodEnum.fromString( "numeric-descend" );
     *   // returns NUMERIC_DESCEND
     * </pre>
     *
     * @param s  the sort method keyword from the olcValSortAttr rule
     * @return   the matching enum constant, or null if unrecognized
     */
    public static OlcValSortMethodEnum fromString( String s )
    {
        if ( ALPHA_ASCEND_STRING.equalsIgnoreCase( s ) )
        {
            return ALPHA_ASCEND;
        }
        else if ( ALPHA_DESCEND_STRING.equalsIgnoreCase( s ) )
        {
            return ALPHA_DESCEND;
        }
        else if ( NUMERIC_ASCEND_STRING.equalsIgnoreCase( s ) )
        {
            return NUMERIC_ASCEND;
        }
        else if ( NUMERIC_DESCEND_STRING.equalsIgnoreCase( s ) )
        {
            return NUMERIC_DESCEND;
        }

        return null;
    }


    // ── toString — Lando Records the Sort Method in the LDAP Attribute ────────────
    // Lando writes the sort method keyword back into the raw attribute string in the
    // exact format the valsort overlay expects.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation suitable for the olcValSortAttr attribute value.
     *
     * <p>For example — Lando records the sort method:</p>
     * <pre>
     *   OlcValSortMethodEnum.ALPHA_ASCEND.toString();   // "alpha-ascend"
     *   OlcValSortMethodEnum.NUMERIC_DESCEND.toString(); // "numeric-descend"
     * </pre>
     *
     * @return  the lowercase sort method keyword
     */
    @Override
    public String toString()
    {
        switch ( this )
        {
            case ALPHA_ASCEND:
                return ALPHA_ASCEND_STRING;
            case ALPHA_DESCEND:
                return ALPHA_DESCEND_STRING;
            case NUMERIC_ASCEND:
                return NUMERIC_ASCEND_STRING;
            case NUMERIC_DESCEND:
                return NUMERIC_DESCEND_STRING;
        }

        return super.toString();
    }
}
