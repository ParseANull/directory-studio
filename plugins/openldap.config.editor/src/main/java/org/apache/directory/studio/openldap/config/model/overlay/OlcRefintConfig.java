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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.openldap.config.model.ConfigurationElement;
import org.apache.directory.studio.openldap.config.model.OlcOverlayConfig;


// ── CLASS: OlcRefintConfig — Vader Maintaining Imperial Referential Integrity ─
// Vader doesn't tolerate broken links in the Imperial command structure — if an
// officer's record is deleted, any group memberships or manager references that
// pointed to that officer must be cleaned up immediately.
// The refint (Referential Integrity) overlay does exactly that: when an entry is
// deleted or renamed, it automatically updates all other entries that reference it
// via specified attributes (like member, managedBy, seeAlso).
// OlcRefintConfig holds the list of watched attributes, a fallback "nothing" DN,
// and the modifier name used when making cleanup writes.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code olcRefintConfig} object class, which configures the
 * OpenLDAP referential integrity (refint) overlay.
 * When an entry is deleted or renamed, the refint overlay searches for other entries
 * whose specified attributes (olcRefintAttribute) reference the affected DN and
 * either updates them or removes the dangling value.
 * Think of this as Vader purging all traces of a deleted officer from the Imperial
 * org chart — no dangling references allowed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcRefintConfig extends OlcOverlayConfig
{
    /**
     * Field for the 'olcRefintAttribute' attribute.
     */
    @ConfigurationElement(attributeType = "olcRefintAttribute", version="2.4.0")
    private List<String> olcRefintAttribute = new ArrayList<>();

    /**
     * Field for the 'olcRefintNothing' attribute.
     */
    @ConfigurationElement(attributeType = "olcRefintNothing", version="2.4.0")
    private Dn olcRefintNothing;

    /**
     * Field for the 'olcRefintModifiersName' attribute.
     */
    @ConfigurationElement(attributeType = "olcRefintModifiersName", version="2.4.10")
    private Dn olcRefintModifiersName;


    // ── Default Constructor — Vader Activates the Integrity Protocol ──────────────
    // Vader activates the refint protocol and stamps it with the overlay type name
    // ("refint") so OpenLDAP knows which plugin to load.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OlcRefintConfig with the overlay type set to "refint".
     *
     * <p>For example — Vader activates the integrity protocol:</p>
     * <pre>
     *   OlcRefintConfig refint = new OlcRefintConfig();
     *   refint.getOlcOverlay(); // "refint"
     * </pre>
     */
    public OlcRefintConfig()
    {
        super();
        olcOverlay = "refint";
    }


    // ── Copy Constructor — Vader Duplicates the Integrity Protocol ────────────────
    // Vader makes an exact copy of the integrity protocol config for safe editing
    // without corrupting the original.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of the given OlcRefintConfig.
     * Used by the editor to create a working copy before the user commits changes.
     *
     * <p>For example — Vader duplicates the protocol config:</p>
     * <pre>
     *   OlcRefintConfig copy = new OlcRefintConfig( originalRefintConfig );
     * </pre>
     *
     * @param o  the OlcRefintConfig to copy
     */
    public OlcRefintConfig( OlcRefintConfig o )
    {
        super();
        olcRefintAttribute = o.olcRefintAttribute;
        olcRefintNothing = o.olcRefintNothing;
        olcRefintModifiersName = o.olcRefintModifiersName;
    }


    // ── addOlcRefintAttribute — Vader Adds Attributes to Watch ───────────────────
    // Vader adds more attributes to the watch list — every attribute in this list
    // will be scanned and cleaned up when a referenced DN is deleted or renamed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more attribute names to the list of attributes monitored for
     * referential integrity (e.g., "member", "manager", "seeAlso").
     * When an entry is deleted, the overlay will search for any entry whose monitored
     * attribute values reference that DN and clean them up.
     *
     * <p>For example — Vader adds "member" to the watch list:</p>
     * <pre>
     *   refintConfig.addOlcRefintAttribute( "member", "manager" );
     * </pre>
     *
     * @param strings  the attribute type names to add to the watch list
     */
    public void addOlcRefintAttribute( String... strings )
    {
        for ( String string : strings )
        {
            olcRefintAttribute.add( string );
        }
    }


    // ── getOlcRefintAttribute — Vader Reads the Watch List ───────────────────────
    // Vader reads the list of attributes his integrity protocol is watching.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of attribute type names monitored for referential integrity.
     *
     * <p>For example — Vader reads the watch list:</p>
     * <pre>
     *   List&lt;String&gt; attrs = refintConfig.getOlcRefintAttribute();
     *   // ["member", "manager", "seeAlso"]
     * </pre>
     *
     * @return  the live list of watched attribute names; never null
     */
    public List<String> getOlcRefintAttribute()
    {
        return olcRefintAttribute;
    }


    // ── getOlcRefintNothing — Vader Reads the Fallback DN ────────────────────────
    // Vader checks what DN to substitute when the only remaining value in an
    // attribute would become dangling — the "nothing" DN is a placeholder.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the fallback DN used when a monitored attribute's only value becomes a
     * dangling reference. If set, the overlay replaces the dangling value with this DN
     * rather than deleting the attribute entirely.
     *
     * <p>For example — Vader reads the fallback placeholder:</p>
     * <pre>
     *   Dn nothing = refintConfig.getOlcRefintNothing();
     *   // e.g., cn=nobody,dc=example,dc=com
     * </pre>
     *
     * @return  the fallback Dn, or null if not configured
     */
    public Dn getOlcRefintNothing()
    {
        return olcRefintNothing;
    }


    // ── getOlcRefintModifiersName — Vader Checks Who Signs the Cleanup Writes ────
    // Vader checks which identity (DN) is used as the modifier when the overlay
    // writes the cleanup changes — so audit logs attribute the update correctly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the DN used as the modifiersName when the refint overlay writes cleanup
     * changes to entries that had dangling references. If unset, the overlay uses
     * the root DN or the original operation's modifiersName.
     *
     * <p>For example — Vader checks who signs the cleanup:</p>
     * <pre>
     *   Dn modifier = refintConfig.getOlcRefintModifiersName();
     *   // e.g., cn=admin,dc=example,dc=com
     * </pre>
     *
     * @return  the modifier Dn, or null if not configured
     */
    public Dn getOlcRefintModifiersName()
    {
        return olcRefintModifiersName;
    }


    // ── setOlcRefintAttribute — Vader Replaces the Watch List ────────────────────
    // Vader replaces the entire list of watched attributes in one go.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire list of monitored attribute type names.
     *
     * <p>For example — Vader replaces the watch list:</p>
     * <pre>
     *   refintConfig.setOlcRefintAttribute( Arrays.asList( "member", "owner" ) );
     * </pre>
     *
     * @param olcRefintAttribute  the new list of attribute names to monitor
     */
    public void setOlcRefintAttribute( List<String> olcRefintAttribute )
    {
        this.olcRefintAttribute = olcRefintAttribute;
    }


    // ── setOlcRefintNothing — Vader Sets the Fallback Placeholder ────────────────
    // Vader sets the fallback DN that substitutes for dangling references.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the fallback DN substituted for dangling references when an attribute
     * would otherwise be left with no values.
     *
     * <p>For example — Vader sets the fallback placeholder:</p>
     * <pre>
     *   refintConfig.setOlcRefintNothing( new Dn( "cn=nobody,dc=example,dc=com" ) );
     * </pre>
     *
     * @param olcRefintNothing  the fallback Dn to use when all values become dangling
     */
    public void setOlcRefintNothing( Dn olcRefintNothing )
    {
        this.olcRefintNothing = olcRefintNothing;
    }


    // ── setOlcRefintModifiersName — Vader Sets the Cleanup Signer ────────────────
    // Vader sets which identity signs the cleanup writes in the audit log.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the DN used as the modifiersName for cleanup writes made by the refint overlay.
     *
     * <p>For example — Vader sets the cleanup signer:</p>
     * <pre>
     *   refintConfig.setOlcRefintModifiersName( new Dn( "cn=admin,dc=example,dc=com" ) );
     * </pre>
     *
     * @param olcRefintModifiersName  the Dn to record as modifiersName on cleanup operations
     */
    public void setOlcRefintModifiersName( Dn olcRefintModifiersName )
    {
        this.olcRefintModifiersName = olcRefintModifiersName;
    }


    // ── copy — Vader Duplicates the Refint Config Object ─────────────────────────
    // Vader creates an exact copy of the refint config for safe editing in the UI.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcRefintConfig.
     *
     * <p>For example — Vader duplicates the config:</p>
     * <pre>
     *   OlcRefintConfig copy = refintConfig.copy();
     * </pre>
     *
     * @return  a new OlcRefintConfig with the same field values as this one
     */
    @Override
    public OlcRefintConfig copy()
    {
        return new OlcRefintConfig( this );
    }
}
