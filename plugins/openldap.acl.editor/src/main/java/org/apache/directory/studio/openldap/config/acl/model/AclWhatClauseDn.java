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


// ── CLASS: AclWhatClauseDn — DEATH STAR MANIFEST: DN PATTERN RESOURCE SELECTOR ──
// Tarkin writes a security order for a specific corridor on the Death Star —
// "protect entries under ou=Rebels,dc=galaxy,dc=far using subtree scope."
// The DN what-clause does the same: it names a Distinguished Name pattern
// (a hierarchical directory path) and a scope type that determines how broadly
// that pattern matches. This class stores the type and pattern and serialises
// them to OpenLDAP's wire format: dn[.type]="pattern".
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete what-clause targeting entries by their Distinguished Name (DN).
 * The type qualifier (base, subtree, one, children, exact, regex) controls
 * how broadly the pattern matches the directory tree. Without a type the
 * OpenLDAP default is base (exact DN match). With a regex type the pattern
 * can use regular-expression syntax.
 * Think of this class as Tarkin's corridor designation on the clearance order
 * — a DN pattern with a scope qualifier that defines which part of the
 * directory tree this rule covers.
 *
 * <p>The type can be one of:</p>
 * <ul>
 *   <li>base : AclWhatClauseDnTypeEnum.BASE</li>
 *   <li>baseObject : AclWhatClauseDnTypeEnum.BASE_OBJECT</li>
 *   <li>one : AclWhatClauseDnTypeEnum.ONE</li>
 *   <li>oneLevel : AclWhatClauseDnTypeEnum.ONE_LEVEL</li>
 *   <li>sub/subtree : AclWhatClauseDnTypeEnum.SUB</li>
 *   <li>subtree : AclWhatClauseDnTypeEnum.SUBTREE</li>
 *   <li>children : AclWhatClauseDnTypeEnum.CHILDREN</li>
 *   <li>exact : AclWhatClauseDnTypeEnum.EXACT</li>
 *   <li>regex : AclWhatClauseDnTypeEnum.REGEX</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhatClauseDn extends AclWhatClause
{
    /** The type, default to BASE */
    private AclWhatClauseDnTypeEnum type;

    /** The pattern */
    private String pattern;


    // ── Reading the Scope Type ─────────────────────────────────────────────────
    // Tarkin's adjutant reads the corridor scope code from the order to know
    // whether the rule applies to one room, a floor, or an entire wing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN scope type that qualifies the pattern match. Determines
     * which entries in the directory hierarchy are selected.
     *
     * <p>For example — reading the scope code from Tarkin's order:</p>
     * <pre>
     *   clause.getType() == AclWhatClauseDnTypeEnum.SUBTREE
     *   // → selects all entries below the given DN
     * </pre>
     *
     * @return  The {@link AclWhatClauseDnTypeEnum}; may be {@code null} if no type was specified.
     */
    public AclWhatClauseDnTypeEnum getType()
    {
        return type;
    }


    // ── Stamping the Scope Type ───────────────────────────────────────────────
    // Tarkin stamps "subtree" or "children" onto the corridor designation
    // in his security order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN scope type for this what-clause. Called by the ANTLR parser
     * when it recognises a type qualifier after the "dn" keyword, or by the UI
     * when the user picks a scope from the drop-down.
     *
     * <p>For example — the parser stamping SUBTREE after reading "dn.subtree=":</p>
     * <pre>
     *   clause.setType(AclWhatClauseDnTypeEnum.SUBTREE);
     * </pre>
     *
     * @param type  The {@link AclWhatClauseDnTypeEnum} to apply.
     */
    public void setType( AclWhatClauseDnTypeEnum type )
    {
        this.type = type;
    }


    // ── Reading the DN Pattern ────────────────────────────────────────────────
    // Tarkin reads the specific corridor address (DN or regex) from the order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN pattern or regular expression stored in this clause.
     * For exact/base/subtree/etc. types this is a full DN string; for regex
     * types it is a regular expression that OpenLDAP evaluates against each
     * candidate DN.
     *
     * <p>For example — reading the target DN pattern:</p>
     * <pre>
     *   clause.getPattern() // → "ou=Rebels,dc=galaxy,dc=far"
     * </pre>
     *
     * @return  The pattern string; may be {@code null} if not yet set.
     */
    public String getPattern()
    {
        return pattern;
    }


    // ── Stamping the DN Pattern ───────────────────────────────────────────────
    // The parser stamps the corridor address onto the order after reading it
    // from the quoted string in the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN pattern or regular expression for this clause. Called by the
     * ANTLR parser after extracting the quoted string following the {@code dn=}
     * token, or by the UI after the user picks an entry with the DN browser.
     *
     * <p>For example — the parser storing the DN after "dn.exact=\"ou=Rebels\"":</p>
     * <pre>
     *   clause.setPattern("ou=Rebels,dc=galaxy,dc=far");
     * </pre>
     *
     * @param pattern  The DN or regex pattern string.
     */
    public void setPattern( String pattern )
    {
        this.pattern = pattern;
    }


    // ── Serialising the DN Clause to ACL Text ─────────────────────────────────
    // Tarkin's adjutant writes the full corridor designation: "dn.subtree="
    // followed by the quoted DN. If no type was specified we just write "dn=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to OpenLDAP wire format: {@code dn[.type]="pattern"}.
     * If no type is set, the {@code .type} part is omitted.
     *
     * <p>For example — serialising a subtree DN clause:</p>
     * <pre>
     *   clause.setType(AclWhatClauseDnTypeEnum.SUBTREE);
     *   clause.setPattern("ou=Rebels,dc=galaxy,dc=far");
     *   clause.toString()
     *   // → "dn.subtree=\"ou=Rebels,dc=galaxy,dc=far\""
     * </pre>
     *
     * @return  The ACL text fragment for this DN what-clause.
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

        // Pattern
        sb.append( '=' );
        sb.append( '"' );
        sb.append( pattern );
        sb.append( '"' );

        return sb.toString();
    }
}
