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


// ── CLASS: AclWhoClauseDnTypeEnum — IMPERIAL WHO-CLAUSE SCOPE CODES ───────────
// Tarkin's who-clause identifies which users this rule applies to by DN. The
// type qualifier tells OpenLDAP how broadly to interpret the DN pattern: exactly
// this one entry, everyone in a subtree below it, only immediate children, or
// even a regex pattern. The LEVEL type adds a numeric depth — "level{2}" means
// two hops down the tree. This enum encodes all those scope types plus LEVEL
// with its mutable depth field.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * All DN scope-type options for the "who" side of an OpenLDAP ACL rule.
 * Written as {@code dn[.type]="pattern"}, the type narrows or broadens the set
 * of users matched. The special {@code LEVEL} type carries a numeric depth
 * accessible via {@link #getLevel()} / {@link #setLevel(int)}.
 * Think of this enum as the scope codes on Tarkin's identity-check directive:
 * regex to match a pattern, exact for one person, subtree for a whole branch
 * of the hierarchy, and level{N} for exactly N hops down the tree.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum AclWhoClauseDnTypeEnum
{
    REGEX,
    BASE,
    EXACT,
    ONE,
    SUBTREE,
    CHILDREN,
    LEVEL;

    /** The level*/
    private int level = -1;


    // ── Reading the Numeric Depth for LEVEL-Type Scopes ───────────────────────
    // When Tarkin writes "level{3}" he means exactly three hops down from the
    // given DN. We store that integer here so the serialiser can reconstruct
    // "level{3}" rather than just "level".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the numeric depth for the {@code LEVEL} type. For all other types
     * this returns {@code -1} (unused). For {@code LEVEL}, whatever value was
     * set via {@link #setLevel(int)} is returned.
     *
     * <p>For example — reading the depth from a LEVEL type:</p>
     * <pre>
     *   AclWhoClauseDnTypeEnum.LEVEL.setLevel(3);
     *   AclWhoClauseDnTypeEnum.LEVEL.getLevel() // → 3
     *   AclWhoClauseDnTypeEnum.LEVEL.toString() // → "level{3}"
     * </pre>
     *
     * @return  The depth integer, or {@code -1} if this is not a {@code LEVEL} type.
     */
    public int getLevel()
    {
        return level;
    }


    // ── Setting the Numeric Depth for LEVEL-Type Scopes ───────────────────────
    // Tarkin stamps a specific depth onto the LEVEL directive before the order
    // is finalised. The parser calls this after recognising "level{N}" in the
    // ACL text to store N in the enum constant.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the numeric depth for the {@code LEVEL} scope type. The ANTLR parser
     * calls this after recognising a {@code level{N}} token in the ACL text.
     * Calling this on any type other than {@code LEVEL} is a no-op at the Java
     * level but semantically meaningless.
     *
     * <p>For example — the parser recording depth 2 from "level{2}":</p>
     * <pre>
     *   AclWhoClauseDnTypeEnum t = AclWhoClauseDnTypeEnum.LEVEL;
     *   t.setLevel(2);
     *   t.toString(); // → "level{2}"
     * </pre>
     *
     * @param level  The numeric depth extracted from the ACL text.
     */
    public void setLevel( int level )
    {
        this.level = level;
    }


    // ── Serialising the Scope Type to Its Wire Keyword ────────────────────────
    // Tarkin's order must use the exact protocol keyword OpenLDAP understands.
    // For LEVEL types we embed the depth in curly braces; all other types just
    // return their fixed lowercase keyword.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the OpenLDAP wire-format string for this scope type. For
     * {@code LEVEL} this is {@code "level{N}"} where N is the stored depth;
     * for all others it is the fixed keyword string.
     *
     * <p>For example — serialising scope types to ACL text:</p>
     * <pre>
     *   AclWhoClauseDnTypeEnum.REGEX.toString()     // → "regex"
     *   AclWhoClauseDnTypeEnum.SUBTREE.toString()   // → "subtree"
     *   AclWhoClauseDnTypeEnum.CHILDREN.toString()  // → "children"
     *   // After setLevel(2):
     *   AclWhoClauseDnTypeEnum.LEVEL.toString()     // → "level{2}"
     * </pre>
     *
     * @return  The OpenLDAP keyword for this scope type.
     */
    public String toString()
    {
        switch ( this )
        {
            case REGEX:
                return "regex";
            case BASE:
                return "base";
            case EXACT:
                return "exact";
            case ONE:
                return "one";
            case SUBTREE:
                return "subtree";
            case CHILDREN:
                return "children";
            case LEVEL:
                return "level{" + level + "}";
        }

        return super.toString();
    }
}
