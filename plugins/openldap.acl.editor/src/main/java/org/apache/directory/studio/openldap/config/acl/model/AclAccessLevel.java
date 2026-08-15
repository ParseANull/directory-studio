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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


// ── CLASS: AclAccessLevel — DEATH STAR CLEARANCE BADGE ───────────────────────
// Tarkin's clearance badge is more nuanced than just a rank: it can specify
// either a named level (manage, write, read, etc.) OR a custom set of
// individual privilege letters (=rsc, +w, -m), and it can optionally be
// prefixed with "self" to restrict it to the owner's own entry. This class
// models all those possibilities: the self-modifier flag, the named level,
// and the custom privileges list with its modifier (=, +, -).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The access level portion of a "by" clause — the part that says what
 * operations the matched subject is permitted to perform. It supports two modes:
 * <ul>
 *   <li>Named level: {@code [self] manage|write|read|search|compare|auth|disclose|none}</li>
 *   <li>Custom privileges: {@code [self] [=|+|-][m][w][r][s][d][c][x]}</li>
 * </ul>
 * Think of this class as Tarkin's multi-field clearance badge — rank tier,
 * optional owner-only restriction, and optional individual privilege overrides
 * all on one stamped card.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclAccessLevel
{
    /** The 'self' modifier' flag */
    private boolean isSelf = false;

    /** The level */
    private AclAccessLevelLevelEnum level;

    /** The privileges modifier */
    private AclAccessLevelPrivModifierEnum privilegesModifier;

    /** The privileges */
    private List<AclAccessLevelPrivilegeEnum> privileges = new ArrayList<AclAccessLevelPrivilegeEnum>();


    // ── Adding One Privilege to the Custom Set ────────────────────────────────
    // Tarkin's analyst adds one clearance letter at a time to the badge when
    // building a custom privileges set for this entry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single privilege to the custom privileges list. Use the
     * privileges mode (not the named-level mode) when you need precise control
     * over individual operations like read-but-not-search.
     *
     * <p>For example — adding READ privilege to a custom badge:</p>
     * <pre>
     *   level.addPrivilege(AclAccessLevelPrivilegeEnum.READ);
     *   level.addPrivilege(AclAccessLevelPrivilegeEnum.SEARCH);
     *   level.toString(); // → "=rs" (with EQUAL modifier)
     * </pre>
     *
     * @param p  The {@link AclAccessLevelPrivilegeEnum} to add.
     */
    public void addPrivilege( AclAccessLevelPrivilegeEnum p )
    {
        privileges.add( p );
    }


    // ── Adding Multiple Privileges at Once ────────────────────────────────────
    // The parser sometimes has a collection of privilege letters to add all at
    // once — this bulk method handles that in one call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds all privileges from the given collection to the custom privileges list.
     * Equivalent to calling {@link #addPrivilege(AclAccessLevelPrivilegeEnum)}
     * for each element, but more efficient for bulk imports.
     *
     * <p>For example — the parser bulk-adding privileges from a list:</p>
     * <pre>
     *   level.addPrivileges(Arrays.asList(READ, SEARCH, COMPARE));
     * </pre>
     *
     * @param c  Collection of privileges to add.
     */
    public void addPrivileges( Collection<? extends AclAccessLevelPrivilegeEnum> c )
    {
        privileges.addAll( c );
    }


    // ── Clearing the Privilege List ───────────────────────────────────────────
    // When the UI switches from custom-privileges mode to named-level mode it
    // clears the privileges list so no stale letters remain.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes all privileges from the custom privileges list. Called by the
     * visual editor when the user switches from "custom privileges" mode back
     * to "named level" mode.
     */
    public void clearPrivileges()
    {
        privileges.clear();
    }


    // ── Reading the Named Level ────────────────────────────────────────────────
    // Tarkin reads the named tier off the clearance badge — manage, write,
    // read, etc.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the named access level tier, or {@code null} if custom privileges
     * mode is active instead.
     *
     * <p>For example — reading Tarkin's named clearance tier:</p>
     * <pre>
     *   level.getLevel() // → AclAccessLevelLevelEnum.READ
     * </pre>
     *
     * @return  The {@link AclAccessLevelLevelEnum}; may be {@code null}.
     */
    public AclAccessLevelLevelEnum getLevel()
    {
        return level;
    }


    // ── Reading the Privilege Modifier ────────────────────────────────────────
    // Tarkin reads the operator symbol (=, +, -) from the custom badge to know
    // whether to replace, add to, or revoke from the existing privilege set.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the privileges modifier (=, +, -) used in custom privileges mode.
     * {@code null} if named-level mode is active.
     *
     * <p>For example — reading the operator from a custom-privileges badge:</p>
     * <pre>
     *   level.getPrivilegeModifier() // → AclAccessLevelPrivModifierEnum.EQUAL
     * </pre>
     *
     * @return  The {@link AclAccessLevelPrivModifierEnum}; may be {@code null}.
     */
    public AclAccessLevelPrivModifierEnum getPrivilegeModifier()
    {
        return privilegesModifier;
    }


    // ── Reading the Privileges List ───────────────────────────────────────────
    // Tarkin reads the list of individual clearance letters from the custom
    // badge section.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of individual privilege bits used in custom privileges
     * mode. Empty if named-level mode is active.
     *
     * <p>For example — reading the custom privilege letters:</p>
     * <pre>
     *   level.getPrivileges() // → [READ, SEARCH, COMPARE]
     * </pre>
     *
     * @return  The list of {@link AclAccessLevelPrivilegeEnum}; never {@code null}.
     */
    public List<AclAccessLevelPrivilegeEnum> getPrivileges()
    {
        return privileges;
    }


    // ── Checking the Self-Modifier Flag ───────────────────────────────────────
    // The "self" prefix restricts the access level to the owner's own entry.
    // Tarkin reads this flag to know whether to prepend "self" when serialising.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the "self" modifier is active, meaning this access
     * level only applies when the bound user is accessing their own entry.
     *
     * <p>For example — checking whether the self-only restriction is on:</p>
     * <pre>
     *   level.isSelf() // → true  →  "self write" in ACL text
     *   level.isSelf() // → false →  "write"       in ACL text
     * </pre>
     *
     * @return  {@code true} if "self" mode is enabled.
     */
    public boolean isSelf()
    {
        return isSelf;
    }


    // ── Stamping the Named Level ──────────────────────────────────────────────
    // The parser or UI stamps the named tier onto the clearance badge.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the named access level tier. Use this for the simple named-level mode.
     * Setting a level does not clear the privileges list — the caller decides
     * which mode is active by checking which field is non-null.
     *
     * @param level  The {@link AclAccessLevelLevelEnum} to apply.
     */
    public void setLevel( AclAccessLevelLevelEnum level )
    {
        this.level = level;
    }


    // ── Stamping the Privilege Modifier ───────────────────────────────────────
    // The parser stamps the operator symbol after recognising "=" / "+" / "-"
    // before the privilege letters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the privileges modifier (=, +, -) for custom privileges mode.
     *
     * @param privilegeModifier  The {@link AclAccessLevelPrivModifierEnum} to apply.
     */
    public void setPrivilegeModifier( AclAccessLevelPrivModifierEnum privilegeModifier )
    {
        this.privilegesModifier = privilegeModifier;
    }


    // ── Stamping the Self-Modifier Flag ───────────────────────────────────────
    // The parser stamps the self flag when it sees the "self" keyword before
    // the access level token.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the "self" modifier for this access level.
     *
     * @param isSelf  {@code true} to prepend "self" in the ACL text output.
     */
    public void setSelf( boolean isSelf )
    {
        this.isSelf = isSelf;
    }


    // ── Serialising the Full Access Level to ACL Text ─────────────────────────
    // Tarkin writes the final clearance badge as a string: optional "self ",
    // then either the named tier or the privilege modifier + letters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this access level to the OpenLDAP wire-format string that
     * appears after the who-clause identifier. Produces either
     * {@code [self] levelName} or {@code [self] [=|+|-][privilege-letters]}.
     *
     * <p>For example — serialising two different badge types:</p>
     * <pre>
     *   // Named level:
     *   level.setLevel(AclAccessLevelLevelEnum.READ);
     *   level.toString(); // → "read"
     *
     *   // Custom privileges, owner-only:
     *   level.setSelf(true);
     *   level.setPrivilegeModifier(EQUAL);
     *   level.addPrivilege(WRITE);
     *   level.toString(); // → "self =w"
     * </pre>
     *
     * @return  The full access-level string for inclusion in an ACL rule.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Self
        if ( isSelf )
        {
            sb.append( "self " );
        }

        // Level
        if ( level != null )
        {
            sb.append( level.toString() );
        }

        // Privilege Modifier
        if ( privilegesModifier != null )
        {
            sb.append( privilegesModifier.toString() );
        }

        // Privileges
        if ( ( privileges != null ) && ( privileges.size() > 0 ) )
        {
            for ( AclAccessLevelPrivilegeEnum privlege : privileges )
            {
                sb.append( privlege );
            }
        }

        return sb.toString();
    }
}
