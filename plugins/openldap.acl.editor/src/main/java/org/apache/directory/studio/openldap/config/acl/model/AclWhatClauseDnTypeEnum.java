/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.studio.openldap.config.acl.model;


// ── CLASS: AclWhatClauseDnTypeEnum — DEATH STAR TARGET SCOPE CODES ───────────
// When Tarkin orders "secure the east wing" his orders specify a scope: exactly
// this room, the floor this room is on, all rooms below this level, or the
// entire subtree of corridors. OpenLDAP's DN what-clause uses the same idea —
// you give a DN pattern and a scope type that says how broadly it matches.
// This enum lists all the scope types the parser and the UI need to know about.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All possible DN scope-type modifiers for the "what" side of an OpenLDAP ACL
 * rule. Written as {@code dn[.type]="pattern"}, the type controls which entries
 * in the directory tree are matched — just one exact entry, one subtree, only
 * immediate children, or a regular-expression pattern.
 * Think of this enum as Death Star corridor scope codes — exact room, one
 * level down, full subtree — that Tarkin uses when writing his security orders.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclWhatClauseDnTypeEnum
{
    REGEX( "regex" ),
    BASE( "base" ),
    BASE_OBJECT( "baseobject" ),
    EXACT( "exact" ),
    ONE( "one" ),
    ONE_LEVEL( "onelevel" ),
    SUB( "sub" ),
    SUBTREE( "subtree" ),
    CHILDREN( "children" );

    /** The interned name */
    private String name;


    // ── Registering the Scope With Its Protocol Name ───────────────────────────
    // Each scope code has an exact protocol name; we store it so serialisation
    // never introduces a typo that would confuse the OpenLDAP server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a type constant bound to the given OpenLDAP protocol string.
     *
     * @param name  The exact keyword OpenLDAP uses for this DN scope type.
     */
    private AclWhatClauseDnTypeEnum( String name )
    {
        this.name = name;
    }


    // ── Reading the Protocol Scope Name ───────────────────────────────────────
    // Tarkin's orders read "subtree" or "children" — not "SUBTREE" — so we
    // return the exact protocol keyword stored at construction time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP wire-format name for this DN scope type.
     * Use this when generating ACL text or populating a scope combo box.
     *
     * <p>For example — reading Tarkin's scope code back to a protocol string:</p>
     * <pre>
     *   AclWhatClauseDnTypeEnum.SUBTREE.getName()  // → "subtree"
     *   AclWhatClauseDnTypeEnum.CHILDREN.getName() // → "children"
     *   AclWhatClauseDnTypeEnum.REGEX.getName()    // → "regex"
     * </pre>
     *
     * @return  The OpenLDAP scope keyword string.
     */
    public String getName()
    {
        return name;
    }


    // ── Converting the Scope to Its Protocol Keyword ───────────────────────────
    // Same as getName() — toString() exists so we can use this enum in string
    // concatenation and the result is always the protocol keyword, not the
    // Java enum name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP protocol name for this type — same as
     * {@link #getName()}, provided here so {@code toString()} gives the wire
     * value rather than the enum constant name.
     *
     * @return  The OpenLDAP keyword string for this DN scope type.
     */
    public String toString()
    {
        return name;
    }
}
