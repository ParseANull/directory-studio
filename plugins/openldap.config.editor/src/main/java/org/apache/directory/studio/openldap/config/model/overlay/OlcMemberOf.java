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


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcMemberOf — The Emperor Tracking Force-Sensitives ────────────────
// Palpatine doesn't wait to discover who is Force-sensitive — he maintains
// a running list. When a new group entry is modified (a new member added),
// he automatically reaches out to that person's entry and stamps it with
// "you are now a member of this group." The knowledge flows both ways.
// The memberOf overlay does exactly this for LDAP: when a group entry's
// member attribute is modified, the overlay automatically updates each member's
// entry with a back-reference "memberOf" value pointing back at the group.
// OlcMemberOf configures that overlay: which DN acts as the modifier, which
// objectclass identifies groups, which attribute is the forward link (member)
// and which is the back-link (memberOf), plus dangling reference behavior.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcSyncProvConfig} object class (note: Javadoc in the
 * original source incorrectly says olcSyncProvConfig), which actually configures
 * the OpenLDAP memberOf overlay.
 * The memberOf overlay maintains reverse membership links: when a group's 'member'
 * attribute is modified, each member's entry is automatically updated with a
 * 'memberOf' back-reference to the group.
 * Think of this as Palpatine maintaining his Force-sensitive registry — the Emperor
 * knows exactly which groups each individual belongs to.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcMemberOf extends OlcOverlayConfig
{
    /**
     * Field for the 'olcMemberOfDangling' attribute.
     */
    @ConfigurationElement(attributeType = "olcMemberOfDangling", version="2.4.0")
    private String olcMemberOfDangling;

    /**
     * Field for the 'olcMemberOfDanglingError' attribute.
     */
    @ConfigurationElement(attributeType = "olcMemberOfDanglingError", version="2.4.8")
    private String olcMemberOfDanglingError;

    /**
     * Field for the 'olcMemberOfDN' attribute.
     */
    @ConfigurationElement(attributeType = "olcMemberOfDN", version="2.4.0")
    private Dn olcMemberOfDN;

    /**
     * Field for the 'olcMemberOfGroupOC' attribute.
     */
    @ConfigurationElement(attributeType = "olcMemberOfGroupOC", version="2.4.0")
    private String olcMemberOfGroupOC;

    /**
     * Field for the 'olcMemberOfMemberAD' attribute.
     */
    @ConfigurationElement(attributeType = "olcMemberOfMemberAD", version="2.4.0")
    private String olcMemberOfMemberAD;

    /**
     * Field for the 'olcMemberOfMemberOfAD' attribute.
     */
    @ConfigurationElement(attributeType = "olcMemberOfMemberOfAD", version="2.4.0")
    private String olcMemberOfMemberOfAD;

    /**
     * Field for the 'olcMemberOfRefInt' attribute.
     */
    @ConfigurationElement(attributeType = "olcMemberOfRefInt", version="2.4.0")
    private Boolean olcMemberOfRefInt;


    // ── Default Constructor — Palpatine Activates the Registry System ─────────────
    // Palpatine activates the memberOf registry with the overlay type name ("memberof")
    // so OpenLDAP loads the right back-reference plugin.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcMemberOf with the overlay type set to "memberof".
     *
     * <p>For example — Palpatine activates the registry:</p>
     * <pre>
     *   OlcMemberOf memberOf = new OlcMemberOf();
     *   memberOf.getOlcOverlay(); // "memberof"
     * </pre>
     */
    public OlcMemberOf()
    {
        super();
        olcOverlay = "memberof";
    }


    // ── Copy Constructor — Palpatine Copies the Registry Config ──────────────────
    // Palpatine makes an exact copy of the membership registry config so the editor
    // can modify it safely.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcMemberOf.
     *
     * <p>For example — Palpatine copies the registry config:</p>
     * <pre>
     *   OlcMemberOf copy = new OlcMemberOf( originalMemberOf );
     * </pre>
     *
     * @param o  the OlcMemberOf to copy
     */
    public OlcMemberOf( OlcMemberOf o )
    {
        super();
        olcMemberOfDangling = o.olcMemberOfDangling;
        olcMemberOfDanglingError = o.olcMemberOfDanglingError;
        olcMemberOfDN = o.olcMemberOfDN;
        olcMemberOfGroupOC = o.olcMemberOfGroupOC;
        olcMemberOfMemberAD = o.olcMemberOfMemberAD;
        olcMemberOfMemberOfAD = o.olcMemberOfMemberOfAD;
        olcMemberOfRefInt = o.olcMemberOfRefInt;
    }


    // ── getOlcMemberOfDangling — Palpatine Reads the Dangling Reference Policy ───
    // Palpatine checks what to do when a group's member entry no longer exists —
    // ignore it, drop it, or report an error.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the olcMemberOfDangling value, which controls how the overlay handles
     * dangling group membership references (member DNs that no longer exist).
     * Valid values are "ignore", "drop", or "error".
     *
     * <p>For example — Palpatine reads the dangling policy:</p>
     * <pre>
     *   String dangling = memberOf.getOlcMemberOfDangling(); // "ignore" / "drop" / "error"
     * </pre>
     *
     * @return  the dangling reference policy string, or null if unconfigured
     */
    public String getOlcMemberOfDangling()
    {
        return olcMemberOfDangling;
    }


    // ── getOlcMemberOfDanglingError — Palpatine Reads the Error Code for Dangles ─
    // Palpatine reads which specific LDAP error code to return when a dangling
    // reference is encountered and the policy is "error".
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP error code to use when olcMemberOfDangling is "error".
     * This is an optional override of the default error code.
     *
     * <p>For example — Palpatine reads the error code:</p>
     * <pre>
     *   String errCode = memberOf.getOlcMemberOfDanglingError(); // e.g., "constraintViolation"
     * </pre>
     *
     * @return  the LDAP error code string, or null if unconfigured
     */
    public String getOlcMemberOfDanglingError()
    {
        return olcMemberOfDanglingError;
    }


    // ── getOlcMemberOfDN — Palpatine Reads the Modifier DN ───────────────────────
    // Palpatine checks which DN is used as the modifier when the overlay writes
    // back-references to member entries — so audit logs show the correct identity.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN used as the modifiersName when the memberOf overlay writes
     * back-reference values to member entries.
     *
     * <p>For example — Palpatine reads the modifier DN:</p>
     * <pre>
     *   Dn modDn = memberOf.getOlcMemberOfDN();
     *   // e.g., cn=admin,dc=example,dc=com
     * </pre>
     *
     * @return  the modifier Dn, or null if unconfigured
     */
    public Dn getOlcMemberOfDN()
    {
        return olcMemberOfDN;
    }


    // ── getOlcMemberOfGroupOC — Palpatine Reads the Group Objectclass ─────────────
    // Palpatine checks which objectclass identifies group entries — the entries whose
    // member attribute changes trigger the back-reference updates.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objectclass name that identifies group entries.
     * Only entries of this objectclass trigger memberOf back-reference updates.
     * Default is "groupOfNames".
     *
     * <p>For example — Palpatine reads the group objectclass:</p>
     * <pre>
     *   String groupOC = memberOf.getOlcMemberOfGroupOC(); // "groupOfNames"
     * </pre>
     *
     * @return  the group objectclass name, or null if unconfigured (defaults apply)
     */
    public String getOlcMemberOfGroupOC()
    {
        return olcMemberOfGroupOC;
    }


    // ── getOlcMemberOfMemberAD — Palpatine Reads the Forward Link Attribute ───────
    // Palpatine checks which attribute on the group entry holds the list of member DNs
    // (the forward link that triggers back-reference updates).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type name for the forward membership link (the group's
     * member attribute). Default is "member".
     *
     * <p>For example — Palpatine reads the forward link:</p>
     * <pre>
     *   String memberAD = memberOf.getOlcMemberOfMemberAD(); // "member"
     * </pre>
     *
     * @return  the forward link attribute name, or null if unconfigured
     */
    public String getOlcMemberOfMemberAD()
    {
        return olcMemberOfMemberAD;
    }


    // ── getOlcMemberOfMemberOfAD — Palpatine Reads the Back-Link Attribute ────────
    // Palpatine checks which attribute on the member entry receives the back-reference
    // to the group DN (the reverse link the overlay maintains automatically).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute type name for the back-link (the attribute added to member
     * entries pointing back at the group). Default is "memberOf".
     *
     * <p>For example — Palpatine reads the back-link:</p>
     * <pre>
     *   String memberOfAD = memberOf.getOlcMemberOfMemberOfAD(); // "memberOf"
     * </pre>
     *
     * @return  the back-link attribute name, or null if unconfigured
     */
    public String getOlcMemberOfMemberOfAD()
    {
        return olcMemberOfMemberOfAD;
    }


    // ── getOlcMemberOfRefInt — Palpatine Checks the Referential Integrity Flag ────
    // Palpatine checks whether the memberOf overlay should also maintain referential
    // integrity — cleaning up dangling memberOf values when group entries are deleted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the memberOf overlay should maintain referential integrity.
     * If TRUE, when a group is deleted the overlay automatically removes the memberOf
     * back-references from all former member entries.
     *
     * <p>For example — Palpatine checks referential integrity:</p>
     * <pre>
     *   Boolean refInt = memberOf.getOlcMemberOfRefInt(); // TRUE or FALSE
     * </pre>
     *
     * @return  TRUE to enable referential integrity, FALSE otherwise, null if unconfigured
     */
    public Boolean getOlcMemberOfRefInt()
    {
        return olcMemberOfRefInt;
    }


    // ── setOlcMemberOfDangling — Palpatine Sets the Dangling Reference Policy ────
    // Palpatine sets what happens when a member DN no longer exists.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the policy for handling dangling group membership references.
     *
     * <p>For example — Palpatine sets the dangling policy:</p>
     * <pre>
     *   memberOf.setOlcMemberOfDangling( "error" );
     * </pre>
     *
     * @param olcMemberOfDangling  "ignore", "drop", or "error"
     */
    public void setOlcMemberOfDangling( String olcMemberOfDangling )
    {
        this.olcMemberOfDangling = olcMemberOfDangling;
    }


    // ── setOlcMemberOfDanglingError — Palpatine Sets the Dangling Error Code ──────
    // Palpatine sets the specific LDAP error code returned on dangling reference errors.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP error code returned when a dangling reference is detected
     * and olcMemberOfDangling is "error".
     *
     * <p>For example — Palpatine sets the error code:</p>
     * <pre>
     *   memberOf.setOlcMemberOfDanglingError( "constraintViolation" );
     * </pre>
     *
     * @param olcMemberOfDanglingError  the LDAP error code string
     */
    public void setOlcMemberOfDanglingError( String olcMemberOfDanglingError )
    {
        this.olcMemberOfDanglingError = olcMemberOfDanglingError;
    }


    // ── setOlcMemberOfDN — Palpatine Sets the Modifier DN ────────────────────────
    // Palpatine sets which identity signs back-reference update writes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN used as the modifiersName for back-reference update operations.
     *
     * <p>For example — Palpatine sets the modifier DN:</p>
     * <pre>
     *   memberOf.setOlcMemberOfDN( new Dn( "cn=admin,dc=example,dc=com" ) );
     * </pre>
     *
     * @param olcMemberOfDN  the modifier Dn
     */
    public void setOlcMemberOfDN( Dn olcMemberOfDN )
    {
        this.olcMemberOfDN = olcMemberOfDN;
    }


    // ── setOlcMemberOfGroupOC — Palpatine Sets the Group Objectclass ──────────────
    // Palpatine sets which objectclass identifies group entries he's tracking.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the objectclass name identifying group entries.
     *
     * <p>For example — Palpatine sets the group objectclass:</p>
     * <pre>
     *   memberOf.setOlcMemberOfGroupOC( "groupOfUniqueNames" );
     * </pre>
     *
     * @param olcMemberOfGroupOC  the group objectclass name
     */
    public void setOlcMemberOfGroupOC( String olcMemberOfGroupOC )
    {
        this.olcMemberOfGroupOC = olcMemberOfGroupOC;
    }


    // ── setOlcMemberOfMemberAD — Palpatine Sets the Forward Link Attribute ────────
    // Palpatine sets which attribute on the group entry holds the forward membership links.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the attribute type name for the group's forward member link.
     *
     * <p>For example — Palpatine sets the forward link:</p>
     * <pre>
     *   memberOf.setOlcMemberOfMemberAD( "uniqueMember" );
     * </pre>
     *
     * @param olcMemberOfMemberAD  the forward link attribute name
     */
    public void setOlcMemberOfMemberAD( String olcMemberOfMemberAD )
    {
        this.olcMemberOfMemberAD = olcMemberOfMemberAD;
    }


    // ── setOlcMemberOfMemberOfAD — Palpatine Sets the Back-Link Attribute ─────────
    // Palpatine sets which attribute on member entries receives the back-reference.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the attribute type name added to member entries as the back-reference.
     *
     * <p>For example — Palpatine sets the back-link attribute:</p>
     * <pre>
     *   memberOf.setOlcMemberOfMemberOfAD( "memberOf" );
     * </pre>
     *
     * @param olcMemberOfMemberOfAD  the back-link attribute name
     */
    public void setOlcMemberOfMemberOfAD( String olcMemberOfMemberOfAD )
    {
        this.olcMemberOfMemberOfAD = olcMemberOfMemberOfAD;
    }


    // ── setOlcMemberOfRefInt — Palpatine Enables or Disables Referential Integrity ─
    // Palpatine enables or disables automatic cleanup when groups are deleted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the memberOf overlay should maintain referential integrity.
     *
     * <p>For example — Palpatine enables referential integrity:</p>
     * <pre>
     *   memberOf.setOlcMemberOfRefInt( Boolean.TRUE );
     * </pre>
     *
     * @param olcMemberOfRefInt  TRUE to enable, FALSE to disable
     */
    public void setOlcMemberOfRefInt( Boolean olcMemberOfRefInt )
    {
        this.olcMemberOfRefInt = olcMemberOfRefInt;
    }


    // ── copy — Palpatine Duplicates the MemberOf Config ──────────────────────────
    // Palpatine creates an exact copy of the memberOf config for the editor.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcMemberOf.
     *
     * <p>For example — Palpatine duplicates the config:</p>
     * <pre>
     *   OlcMemberOf copy = memberOf.copy();
     * </pre>
     *
     * @return  a new OlcMemberOf with the same field values as this one
     */
    @Override
    public OlcMemberOf copy()
    {
        return new OlcMemberOf( this );
    }
}
