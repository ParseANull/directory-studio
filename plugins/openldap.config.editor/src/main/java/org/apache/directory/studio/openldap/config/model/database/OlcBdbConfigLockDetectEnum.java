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
package org.apache.directory.studio.openldap.config.model.database;


// ── CLASS: OlcBdbConfigLockDetectEnum — Mace Windu Choosing Who Gets Removed ──
// When two Jedi and two Sith walk into the same room and create a deadlock —
// nobody yields, nobody moves — Mace Windu has to decide who gets escorted out.
// He has a policy: oldest troublemaker, the youngest, the one with fewest allies,
// a random pick, or just let the system default.
// That's the BDB lock detection algorithm. BerkeleyDB can detect transaction
// deadlocks and needs to know which transaction to abort to break the deadlock.
// OLDEST aborts the longest-running, YOUNGEST the newest, FEWEST the one with
// the fewest locks, RANDOM picks arbitrarily, and DEFAULT uses BDB's built-in choice.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Enum representing the valid values for the {@code olcDbLockDetect} attribute,
 * which controls BerkeleyDB's deadlock detection algorithm — specifically, which
 * transaction gets aborted when a deadlock is detected.
 * Think of this as Mace Windu's policy for resolving deadlocked standoffs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum OlcBdbConfigLockDetectEnum
{
    /** Enum value for 'oldest' */
    OLDEST,

    /** Enum value for 'youngest' */
    YOUNGEST,

    /** Enum value for 'fewest' */
    FEWEST,

    /** Enum value for 'random' */
    RANDOM,

    /** Enum value for 'default' */
    DEFAULT;

    /** The constant string for 'oldest' */
    private static final String OLDEST_STRING = "oldest";

    /** The constant string for 'youngest' */
    private static final String YOUNGEST_STRING = "youngest";

    /** The constant string for 'fewest' */
    private static final String FEWEST_STRING = "fewest";

    /** The constant string for 'random' */
    private static final String RANDOM_STRING = "random";

    /** The constant string for 'default' */
    private static final String DEFAULT_STRING = "default";


    // ── fromString — Mace Windu Reads the Policy from the LDAP Attribute ─────────
    // Mace reads the written policy keyword and translates it into the corresponding
    // Java constant so the system knows which algorithm to apply.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Parses the string value of the {@code olcDbLockDetect} attribute into this enum.
     * Comparison is case-insensitive ("Oldest" and "OLDEST" both work).
     *
     * <p>For example — Mace reads the policy keyword:</p>
     * <pre>
     *   OlcBdbConfigLockDetectEnum alg =
     *       OlcBdbConfigLockDetectEnum.fromString( "fewest" );
     *   // returns FEWEST
     * </pre>
     *
     * @param s  the olcDbLockDetect attribute value string
     * @return   the matching enum constant, or null if unrecognized
     */
    public static OlcBdbConfigLockDetectEnum fromString( String s )
    {
        if ( OLDEST_STRING.equalsIgnoreCase( s ) )
        {
            return OLDEST;
        }
        else if ( YOUNGEST_STRING.equalsIgnoreCase( s ) )
        {
            return YOUNGEST;
        }
        else if ( FEWEST_STRING.equalsIgnoreCase( s ) )
        {
            return FEWEST;
        }
        else if ( RANDOM_STRING.equalsIgnoreCase( s ) )
        {
            return RANDOM;
        }
        else if ( DEFAULT_STRING.equalsIgnoreCase( s ) )
        {
            return DEFAULT;
        }

        return null;
    }


    // ── toString — Mace Windu Records the Policy in the LDAP Attribute ────────────
    // Mace writes the policy keyword back into the olcDbLockDetect attribute value
    // in the exact lowercase format BDB and OpenLDAP expect.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the string representation suitable for the {@code olcDbLockDetect} attribute.
     *
     * <p>For example — Mace records the policy:</p>
     * <pre>
     *   OlcBdbConfigLockDetectEnum.OLDEST.toString();   // "oldest"
     *   OlcBdbConfigLockDetectEnum.DEFAULT.toString();  // "default"
     * </pre>
     *
     * @return  the lowercase policy keyword for the LDAP attribute
     */
    @Override
    public String toString()
    {
        switch ( this )
        {
            case OLDEST:
                return OLDEST_STRING;
            case YOUNGEST:
                return YOUNGEST_STRING;
            case FEWEST:
                return FEWEST_STRING;
            case RANDOM:
                return RANDOM_STRING;
            case DEFAULT:
                return DEFAULT_STRING;
        }

        return super.toString();
    }
}
