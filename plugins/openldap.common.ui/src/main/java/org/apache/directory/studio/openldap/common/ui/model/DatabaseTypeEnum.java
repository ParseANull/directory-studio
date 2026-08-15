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


// ── CLASS: DatabaseTypeEnum — STARSHIPS IN THE REBEL FLEET ───────────────────
// Picture the Rebel hangar at Echo Base where each docking bay holds a
// different class of vessel: the speedy LDIF courier, the heavy-lifting BDB
// freighter, the memory-mapped MDB interceptor, the relay shuttle, the
// null probe with no storage at all. Each constant here is one database backend
// the administrator can deploy. NONE means no backend has been selected yet —
// the docking bay is empty.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * We enumerate all recognized OpenLDAP database backend types. Each constant
 * carries the human-readable display name shown in the UI. The possible values
 * are:
 * <ul>
 * <li>None</li>
 * <li>Frontend DB</li>
 * <li>Config DB</li>
 * <li>BDB</li>
 * <li>DB Perl</li>
 * <li>DB_Socket</li>
 * <li>HDB</li>
 * <li>MDB</li>
 * <li>LDAP</li>
 * <li>LDIF</li>
 * <li>META</li>
 * <li>MONITOR</li>
 * <li>NDB</li>
 * <li>PASSWORD</li>
 * <li>RELAY</li>
 * <li>SHELL</li>
 * <li>SQL DB</li>
 * <li>NULL</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum DatabaseTypeEnum
{
    /** None */
    NONE("None"),

    /** Frontend DB */
    FRONTEND("Frontend DB"),

    /** Config DB */
    CONFIG("Config DB"),

    /** Berkeley DB */
    BDB("BDB (Berkeley DB)"),

    /** DB Perl */
    DB_PERL("DB Perl"),

    /** DB Socket */
    DB_SOCKET("DB Socket"),

    /** Hierarchical Berkeley DB */
    HDB("HDB (Hierarchical Berkeley DB)"),

    /** LDAP DB*/
    LDAP("LDAP DB"),

    /** LDIF DB*/
    LDIF("LDIF DB"),

    /** META DB*/
    META("META DB"),

    /** Memory-Mapped DB */
    MDB("MDB (Memory-Mapped DB)"),

    /** MONITOR DB*/
    MONITOR("MONITOR DB"),

    /** NDB DB*/
    NDB("NDB DB"),

    /** Null DB*/
    NULL("Null DB"),

    /** PASSWD DB */
    PASSWD("PASSWD DB"),

    /** Relay DB*/
    RELAY("Relay DB"),

    /** Shell DB*/
    SHELL("Shell DB"),

    /** SQL DB*/
    SQL("SQL DB");

    /** The internal name of the database */
    private String name;


    // ── CONSTRUCTOR: DatabaseTypeEnum — REGISTERING A VESSEL IN THE FLEET ────
    // Each ship gets its class name stamped on the hull so we can display it
    // in the UI and write it back into configuration files. The name is the
    // human-readable label, not the raw OpenLDAP backend type keyword.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We associate each constant with its display name as shown in the UI.
     *
     * @param name  the human-readable display name for this database type
     */
    private DatabaseTypeEnum( String name )
    {
        this.name = name;
    }


    // ── METHOD: getName — READING THE VESSEL CLASS MARKING ───────────────────
    // We return the display name exactly as it should appear in combo boxes and
    // configuration editors.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return the human-readable display name for this database backend type
     * (e.g., {@code "MDB (Memory-Mapped DB)"}).
     *
     * @return the display name
     */
    public String getName()
    {
        return name;
    }


    // ── METHOD: getNames — LISTING ALL VESSELS IN THE HANGAR ─────────────────
    // We assemble the complete roster of database type display names so UI
    // components can populate drop-down lists without knowing the enum internals.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We return an array of all display name strings in declaration order,
     * suitable for populating combo boxes or other list UI controls.
     *
     * @return an array of all database type display name strings
     */
    public static String[] getNames()
    {
        String[] names = new String[values().length];
        int pos = 0;

        for ( DatabaseTypeEnum databaseType : values() )
        {
            names[pos] = databaseType.name;
            pos++;
        }

        return names;
    }


    // ── METHOD: getDatabaseType — IDENTIFYING A VESSEL BY NAME ───────────────
    // We check both the enum constant name (e.g., "MDB") and the display name
    // (e.g., "MDB (Memory-Mapped DB)") using case-insensitive comparison. If
    // neither matches we return NONE — the docking bay is empty.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * We look up a {@link DatabaseTypeEnum} constant by matching the supplied
     * string against both the enum constant's identifier and its display name,
     * using case-insensitive comparison. We return {@link #NONE} if no match
     * is found.
     *
     * @param name  the string to match against enum identifiers and display names
     * @return      the matching enum constant, or {@link #NONE}
     */
    public static DatabaseTypeEnum getDatabaseType( String name )
    {
        for ( DatabaseTypeEnum databaseType : values() )
        {
            if ( name.equalsIgnoreCase( databaseType.name() ) || name.equalsIgnoreCase( databaseType.getName() ) )
            {
                return databaseType;
            }
        }

        return NONE;
    }
}
