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

// ── CLASS: AclWhoClauseDn — DEATH STAR MANIFEST: NAMED USER BY DN ────────────
// Tarkin's manifest has rows for specific named individuals — not just
// categories like "all users." The DN who-clause does the same: it specifies
// a Distinguished Name pattern identifying exactly which user (or which set
// of users by scope) the rule applies to. It can also carry a modifier like
// "expand" to substitute regex back-references into the DN before matching.
// Written as dn[.type[,modifier]]="pattern" in the ACL text.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause that identifies the subject by Distinguished Name.
 * The type qualifier narrows or broadens the set of matching DNs (subtree,
 * one level, exact, etc.); the optional modifier enables regex back-reference
 * expansion. Together: {@code dn[.type[,expand]]="pattern"}.
 * Think of this class as Tarkin's named-individual row — the person is
 * identified by their directory path (DN) with an optional scope and
 * optional pattern-expansion marker.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseDn extends AbstractAclWhoClause
{
    /** The type, default to BASE */
    private AclWhoClauseDnTypeEnum type;

    /** The modifier */
    private AclWhoClauseDnModifierEnum modifier;

    /** The pattern */
    private String pattern;


    // ── Reading the Pattern-Expansion Modifier ────────────────────────────────
    // Tarkin checks whether a modifier like "expand" was stamped on this row,
    // which means regex back-references should be substituted in the pattern.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the optional modifier for this DN who-clause. Currently the only
     * defined modifier is {@code EXPAND}, which tells OpenLDAP to do regex
     * back-reference substitution in the DN pattern before matching.
     *
     * <p>For example — reading whether expansion was requested:</p>
     * <pre>
     *   clause.getModifier() == AclWhoClauseDnModifierEnum.EXPAND
     *   // → "dn.subtree,expand=\"...\""
     * </pre>
     *
     * @return  The {@link AclWhoClauseDnModifierEnum}, or {@code null} if none.
     */
    public AclWhoClauseDnModifierEnum getModifier()
    {
        return modifier;
    }


    // ── Reading the DN Pattern ────────────────────────────────────────────────
    // Tarkin reads the specific named user or pattern from the manifest row.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN pattern or regular expression used to identify the subject.
     * For exact/base/subtree types this is a canonical DN; for regex types it
     * is a regular expression OpenLDAP evaluates against every bound user's DN.
     *
     * <p>For example — reading the DN pattern:</p>
     * <pre>
     *   clause.getPattern() // → "cn=Luke Skywalker,ou=Rebels,dc=galaxy,dc=far"
     * </pre>
     *
     * @return  The DN or regex pattern string; may be {@code null} if not set.
     */
    public String getPattern()
    {
        return pattern;
    }


    // ── Reading the Scope Type ─────────────────────────────────────────────────
    // Tarkin reads the corridor-scope code from the named-user row to know how
    // broadly the pattern applies.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN scope type that qualifies the pattern match. If {@code null}
     * OpenLDAP defaults to base (exact match).
     *
     * <p>For example — reading the scope type from the manifest row:</p>
     * <pre>
     *   clause.getType() // → AclWhoClauseDnTypeEnum.SUBTREE
     * </pre>
     *
     * @return  The {@link AclWhoClauseDnTypeEnum}; may be {@code null}.
     */
    public AclWhoClauseDnTypeEnum getType()
    {
        return type;
    }


    // ── Stamping the Pattern-Expansion Modifier ───────────────────────────────
    // The parser stamps "expand" onto the row when it sees the modifier keyword
    // between the type and the "=" in the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the modifier for this DN who-clause. Called by the ANTLR parser when
     * it recognises the {@code expand} keyword after the type qualifier.
     *
     * @param modifier  The {@link AclWhoClauseDnModifierEnum} to apply.
     */
    public void setModifier( AclWhoClauseDnModifierEnum modifier )
    {
        this.modifier = modifier;
    }


    // ── Stamping the DN Pattern ───────────────────────────────────────────────
    // The parser stamps the quoted DN or regex string onto the row after the "=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN pattern or regular expression for this clause. Called by the
     * ANTLR parser after extracting the quoted value following {@code dn=}.
     *
     * @param pattern  The DN or regex pattern string.
     */
    public void setPattern( String pattern )
    {
        this.pattern = pattern;
    }


    // ── Stamping the Scope Type ───────────────────────────────────────────────
    // The parser stamps the scope type after reading the qualifier keyword
    // between "dn." and the optional modifier comma.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the scope type for this DN who-clause. Called by the parser when it
     * finds a type keyword after the dot in {@code dn.subtree=...}.
     *
     * @param type  The {@link AclWhoClauseDnTypeEnum} to apply.
     */
    public void setType( AclWhoClauseDnTypeEnum type )
    {
        this.type = type;
    }


    // ── Serialising the DN Clause to ACL Text ─────────────────────────────────
    // The adjutant writes the full named-user row: "dn", optional ".type",
    // optional ",modifier", then ="pattern" — all combined into one string
    // that OpenLDAP expects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to OpenLDAP wire format:
     * {@code dn[.type[,modifier]]="pattern"}. The access level and control word
     * are inherited from the parent class and not included here — the calling
     * serialiser appends them.
     *
     * <p>For example — serialising a subtree + expand DN clause:</p>
     * <pre>
     *   clause.setType(AclWhoClauseDnTypeEnum.SUBTREE);
     *   clause.setModifier(AclWhoClauseDnModifierEnum.EXPAND);
     *   clause.setPattern("ou=Rebels,dc=galaxy,dc=far");
     *   clause.toString()
     *   // → "dn.subtree,expand=\"ou=Rebels,dc=galaxy,dc=far\""
     * </pre>
     *
     * @return  The ACL text fragment for this DN who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // DN
        sb.append( "dn" );

        // Type
        if ( type != null )
        {
            sb.append( "." );
            sb.append( type );
        }

        // Modifier
        if ( modifier != null )
        {
            sb.append( "," );
            sb.append( modifier );
        }

        // Pattern
        sb.append( '=' );
        sb.append( '"' );
        sb.append( pattern );
        sb.append( '"' );

        return sb.toString();
    }
}
