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


// ── CLASS: AclWhoClauseDnAttr — TARKIN GRANTS ACCESS BASED ON A DN ATTRIBUTE ─
// Tarkin has a clever trick on his manifest: instead of naming a specific user
// DN, he points to an attribute on the target entry itself — "let the value of
// 'manager' on this record tell you who has write access." OpenLDAP calls this
// "dnattr=attributeName". At evaluation time it reads the named attribute from
// the target entry, and if the bound user's DN appears in that attribute, the
// rule fires. This class stores the attribute name.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A concrete who-clause that grants access to whoever's DN appears in a
 * specified attribute of the target entry. Written as {@code dnattr=ATTRNAME},
 * this lets you define access control dynamically — for example "the user
 * named in the 'manager' attribute can write this entry."
 * Think of this class as Tarkin writing "whoever's name is on the badge at the
 * door gets in" — the door's badge field is the LDAP attribute.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AclWhoClauseDnAttr extends AbstractAclWhoClause
{
    /** The attribute */
    private String attribute;


    // ── Reading the Attribute Name ────────────────────────────────────────────
    // Tarkin reads the name of the DN attribute from the manifest entry so the
    // serialiser can write "dnattr=<attribute>" into the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the name of the DN-valued attribute on the target entry whose
     * value OpenLDAP compares against the bound user's DN.
     *
     * <p>For example — reading the manager-attribute name from Tarkin's order:</p>
     * <pre>
     *   clause.getAttribute() // → "manager"
     *   clause.toString()     // → "dnattr=manager"
     * </pre>
     *
     * @return  The LDAP attribute name; may be {@code null} if not yet set.
     */
    public String getAttribute()
    {
        return attribute;
    }


    // ── Stamping the Attribute Name ───────────────────────────────────────────
    // The parser stamps the attribute name onto the manifest entry after reading
    // "dnattr=manager" or similar from the ACL text.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the name of the DN attribute for this who-clause. Called by the ANTLR
     * parser when it recognises a {@code dnattr=} token in the ACL text.
     *
     * <p>For example — the parser storing "manager" from "dnattr=manager":</p>
     * <pre>
     *   clause.setAttribute("manager");
     * </pre>
     *
     * @param attribute  The LDAP attribute name whose DN value controls access.
     */
    public void setAttribute( String attribute )
    {
        this.attribute = attribute;
    }


    // ── Serialising the dnattr Clause to ACL Text ─────────────────────────────
    // The adjutant writes "dnattr=<attribute>" as the who-clause fragment that
    // OpenLDAP evaluates dynamically at access-check time.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Serialises this clause to its OpenLDAP wire-format string:
     * {@code dnattr=ATTRNAME}. The access level and control word are not
     * appended here — they are handled by the parent class if present.
     *
     * <p>For example — writing the dnattr clause:</p>
     * <pre>
     *   clause.setAttribute("owner");
     *   clause.toString(); // → "dnattr=owner"
     * </pre>
     *
     * @return  The ACL text fragment for this dnattr who-clause.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "dnattr=" );
        sb.append( attribute );

        return sb.toString();
    }
}
