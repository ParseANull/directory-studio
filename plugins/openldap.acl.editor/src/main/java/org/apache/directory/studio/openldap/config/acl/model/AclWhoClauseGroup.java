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


// ── CLASS: AclWhoClauseGroup — CLOUD CITY SECURITY ROSTER: GROUP-BASED ACCESS ─
// Lando manages Cloud City's security by putting staff into groups —
// groupOfNames, groupOfUniqueNames, etc. — and granting access to those groups
// rather than individual people. The "group" who-clause does the same: you
// point to a DN pattern that names a group entry; OpenLDAP checks whether the
// bound user's DN appears in a named attribute of that group entry. You can
// customise which objectClass and which attribute define membership.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause that grants access to members of an LDAP group.
 * Written as {@code group[/oc[/attr]][.type]="pattern"}:
 * <ul>
 *   <li>oc — the group's objectClass (defaults to groupOfNames)</li>
 *   <li>attr — the membership attribute (defaults to member)</li>
 *   <li>type — how to match the group DN (exact or expand)</li>
 * </ul>
 * Think of this class as Lando's crew roster — membership in the named group
 * entry determines who gets through the checkpoint.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseGroup extends AbstractAclWhoClause
{
    /** The object class */
    private String objectclass;

    /** The attribute */
    private String attribute;

    /** The type */
    private AclWhoClauseGroupTypeEnum type;

    /** The pattern */
    private String pattern;


    // ── Reading the Group Membership Attribute ────────────────────────────────
    // Lando reads the name of the attribute that lists group members —
    // typically "member" or "uniqueMember".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name of the group membership attribute. OpenLDAP checks
     * whether the bound user's DN appears as a value of this attribute in the
     * group entry identified by the pattern.
     *
     * <p>For example — reading Lando's roster attribute name:</p>
     * <pre>
     *   clause.getAttribute() // → "member"
     * </pre>
     *
     * @return  The membership attribute name; may be {@code null} (OpenLDAP defaults to "member").
     */
    public String getAttribute()
    {
        return attribute;
    }


    // ── Reading the Group ObjectClass ─────────────────────────────────────────
    // Lando reads the objectClass that the group entry must have —
    // typically "groupOfNames" or "groupOfUniqueNames".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objectClass that the referenced group entry must have.
     * If {@code null}, OpenLDAP defaults to {@code groupOfNames}. Specifying
     * an explicit objectClass lets you use custom group types.
     *
     * <p>For example — reading the Cloud City crew-roster schema type:</p>
     * <pre>
     *   clause.getObjectclass() // → "groupOfUniqueNames"
     * </pre>
     *
     * @return  The group objectClass name; may be {@code null} for the default.
     */
    public String getObjectclass()
    {
        return objectclass;
    }


    // ── Reading the Group DN Pattern ──────────────────────────────────────────
    // Lando reads the address of the group entry — the DN or regex that
    // identifies which roster entry to check.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the group entry DN pattern or regex. OpenLDAP resolves this to
     * a group entry and then checks the membership attribute.
     *
     * <p>For example — reading the group DN pattern:</p>
     * <pre>
     *   clause.getPattern() // → "cn=CloudCityAdmins,ou=Groups,dc=galaxy,dc=far"
     * </pre>
     *
     * @return  The group DN pattern; may be {@code null} if not yet set.
     */
    public String getPattern()
    {
        return pattern;
    }


    // ── Reading the Group Match Type ──────────────────────────────────────────
    // Lando reads the lookup mode from the roster entry — exact match or
    // regex expansion.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the type qualifier controlling how the group DN pattern is matched
     * against actual group entries (exact or expand).
     *
     * @return  The {@link AclWhoClauseGroupTypeEnum}; may be {@code null}.
     */
    public AclWhoClauseGroupTypeEnum getType()
    {
        return type;
    }


    // ── Stamping the Membership Attribute Name ────────────────────────────────
    // The parser stamps the attribute name after the second "/" in "group/oc/attr".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the membership attribute name. Called by the parser when it finds
     * the second slash-separated token in {@code group/oc/attr=...}.
     *
     * @param attribute  The membership attribute name.
     */
    public void setAttribute( String attribute )
    {
        this.attribute = attribute;
    }


    // ── Stamping the ObjectClass Name ─────────────────────────────────────────
    // The parser stamps the objectClass name after the first "/" in "group/oc".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the objectClass for the group entry. Called by the parser when it
     * finds the first slash-separated token in {@code group/oc...}.
     *
     * @param objectclass  The group entry objectClass name.
     */
    public void setObjectclass( String objectclass )
    {
        this.objectclass = objectclass;
    }


    // ── Stamping the Group DN Pattern ─────────────────────────────────────────
    // The parser stamps the quoted DN or regex string after the "=".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the group entry DN pattern or regex. Called by the parser after
     * extracting the quoted string following the {@code group...=} token.
     *
     * @param pattern  The group DN or regex pattern.
     */
    public void setPattern( String pattern )
    {
        this.pattern = pattern;
    }


    // ── Stamping the Match Type ───────────────────────────────────────────────
    // The parser stamps the match type after the dot in "group.exact" or
    // "group.expand".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the match type for this group who-clause. Called by the parser when
     * it finds a dot-qualifier in the group token.
     *
     * @param type  The {@link AclWhoClauseGroupTypeEnum} to apply.
     */
    public void setType( AclWhoClauseGroupTypeEnum type )
    {
        this.type = type;
    }


    // ── Serialising the Group Clause to ACL Text ──────────────────────────────
    // Lando writes the full roster entry into the security directive:
    // "group", optional "/oc", optional "/attr", optional ".type", then ="pattern".
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to OpenLDAP wire format:
     * {@code group[/oc[/attr]][.type]="pattern"}. Only non-null optional
     * components are included.
     *
     * <p>For example — writing a full group clause:</p>
     * <pre>
     *   clause.setObjectclass("groupOfUniqueNames");
     *   clause.setAttribute("uniqueMember");
     *   clause.setType(AclWhoClauseGroupTypeEnum.EXACT);
     *   clause.setPattern("cn=Admins,ou=Groups,dc=galaxy,dc=far");
     *   clause.toString()
     *   // → "group/groupOfUniqueNames/uniqueMember.exact=\"cn=Admins,...\""
     * </pre>
     *
     * @return  The ACL text fragment for this group who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // DN
        sb.append( "group" );

        // Object Class
        if ( objectclass != null )
        {
            sb.append( "/" );
            sb.append( objectclass );

            // Attribute Name
            if ( attribute != null )
            {
                sb.append( "/" );
                sb.append( attribute );
            }
        }

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
