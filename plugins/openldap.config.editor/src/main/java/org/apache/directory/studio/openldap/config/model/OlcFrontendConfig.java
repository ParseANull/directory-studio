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
package org.apache.directory.studio.openldap.config.model;


import java.util.ArrayList;
import java.util.List;


// ── CLASS: OlcFrontendConfig — PALPATINE'S GALAXY-WIDE STANDING ORDERS ────────
// Palpatine doesn't need to call every planet personally — he issues galaxy-wide
// standing orders (like Order 66) that automatically apply to all stormtroopers
// everywhere, in every sector. The frontend database in OpenLDAP works exactly
// like that: it's a special pseudo-database whose settings apply globally to ALL
// real databases, not just one. The default search base, password hashing schemes,
// and attribute sort rules set here become the empire-wide defaults every database
// inherits unless it overrides them locally.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Java bean for the {@code OlcFrontendConfig} auxiliary object class — models the
 * OpenLDAP frontend database settings that apply globally to all databases in slapd.
 * The frontend isn't a real storage backend; it's the galaxy-wide policy layer.
 * Settings here (default search base, password hash algorithms, attribute sort rules)
 * are inherited by every real database unless individually overridden.
 * Think of this as Palpatine's standing-orders datapad: one configuration that
 * cascades out to every corner of the Empire.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcFrontendConfig implements AuxiliaryObjectClass
{
    /**
     * Field for the 'olcDefaultSearchBase' attribute.
     */
    @ConfigurationElement(attributeType = "olcDefaultSearchBase", version="2.4.0")
    private String olcDefaultSearchBase;

    /**
     * Field for the 'olcPasswordHash' attribute.
     */
    @ConfigurationElement(attributeType = "olcPasswordHash", version="2.4.0")
    private List<String> olcPasswordHash = new ArrayList<>();

    /**
     * Field for the 'olcSortVals' attribute.
     */
    @ConfigurationElement(attributeType = "olcSortVals", version="2.4.6")
    private List<String> olcSortVals = new ArrayList<>();


    // ── Adding A Password Hashing Scheme To The Standing Orders ──────────────────
    // Palpatine adds another approved encryption protocol to his standing orders —
    // all Imperial facilities must now also accept SSHA-encoded passwords in addition
    // to whatever was there before. We can have multiple hash schemes in the list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more password hash algorithm names to the global password-hash list.
     * OpenLDAP uses this list to decide which hash to apply when storing new passwords.
     * Multiple schemes are allowed — slapd will use the first one in the list.
     *
     * <p>For example — Palpatine adds a new approved cipher to the standing orders:</p>
     * <pre>
     *   frontend.addOlcPasswordHash("{SSHA}", "{MD5}");
     *   // all databases now accept both SSHA and MD5 unless they override locally
     * </pre>
     *
     * @param strings  one or more hash scheme strings to add, e.g. "{SSHA}", "{CRYPT}"
     */
    public void addOlcPasswordHash( String... strings )
    {
        for ( String string : strings )
        {
            olcPasswordHash.add( string );
        }
    }


    // ── Adding An Attribute To The Galaxy-Wide Sort List ──────────────────────────
    // Palpatine amends the standing orders to specify which attribute values must be
    // kept sorted across all databases — a galaxy-wide directive about how data is
    // presented to clients who query for multi-valued attributes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more attribute names to the global {@code olcSortVals} list.
     * Attributes in this list will have their values automatically sorted by slapd
     * before returning them to clients — useful for multi-valued attributes where
     * order matters.
     *
     * <p>For example — Palpatine orders that roleOccupant values be kept sorted:</p>
     * <pre>
     *   frontend.addOlcSortVals("roleOccupant", "member");
     *   // all databases now return these attributes' values in sorted order
     * </pre>
     *
     * @param strings  one or more attribute type names whose values should be sorted
     */
    public void addOlcSortVals( String... strings )
    {
        for ( String string : strings )
        {
            olcSortVals.add( string );
        }
    }


    // ── Rescinding All Password Hash Standing Orders ───────────────────────────────
    // Palpatine wipes the approved cipher list from his standing orders — perhaps
    // he's about to issue a completely new set and needs a clean slate first.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the entire password hash list. Use this before re-populating from
     * scratch — for example when the editor replaces the full set of allowed
     * hash schemes.
     *
     * <p>For example — Palpatine clears the cipher list to start fresh:</p>
     * <pre>
     *   frontend.clearOlcPasswordHash();
     *   frontend.addOlcPasswordHash("{SSHA}");  // new canonical list
     * </pre>
     */
    public void clearOlcPasswordHash()
    {
        olcPasswordHash.clear();
    }


    // ── Rescinding All Sort-Value Standing Orders ──────────────────────────────────
    // Palpatine wipes the sort-values directive from the standing orders — the
    // requirement to keep certain attribute values sorted across the galaxy is lifted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the entire sort-vals list. Use before re-populating from scratch.
     *
     * <p>For example — Palpatine rescinds the sort directive:</p>
     * <pre>
     *   frontend.clearOlcSortVals();  // no galaxy-wide sort requirements remain
     * </pre>
     */
    public void clearOlcSortVals()
    {
        olcSortVals.clear();
    }


    // ── Reading The Default Search Territory ──────────────────────────────────────
    // Palpatine's standing orders specify the default Imperial sector — when a
    // stormtrooper receives an order with no sector specified, this is where they
    // operate. Similarly, when a client sends an LDAP search with an empty base DN,
    // olcDefaultSearchBase is where slapd starts the search.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@code olcDefaultSearchBase} DN string — the base DN slapd uses
     * when a client sends a search with an empty base DN and scope baseObject.
     * Without this set, searches against an empty DN may fail or behave unexpectedly.
     *
     * <p>For example — Palpatine reads which sector is the default operating territory:</p>
     * <pre>
     *   String base = frontend.getOlcDefaultSearchBase();
     *   // e.g. "dc=example,dc=com" — clients searching "" start here
     * </pre>
     *
     * @return  the default search base DN string, or {@code null} if not configured
     */
    public String getOlcDefaultSearchBase()
    {
        return olcDefaultSearchBase;
    }


    // ── Reading The Approved Cipher List ──────────────────────────────────────────
    // Palpatine reads out the list of approved encryption protocols from his standing
    // orders — the list of hash algorithms all databases must use unless they override.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of password hash scheme strings for the frontend config.
     * This is the internal list — callers should treat it as read-only unless they
     * know what they're doing.
     *
     * <p>For example — Palpatine reads the approved cipher list:</p>
     * <pre>
     *   List&lt;String&gt; hashes = frontend.getOlcPasswordHash();
     *   // e.g. ["{SSHA}", "{CRYPT}"]
     * </pre>
     *
     * @return  the list of password hash scheme strings
     */
    public List<String> getOlcPasswordHash()
    {
        return olcPasswordHash;
    }


    // ── Reading The Galaxy-Wide Sort Directives ───────────────────────────────────
    // Palpatine reads the list of attributes that must be kept sorted across all
    // Imperial databases — his galaxy-wide sort directive for multi-valued attributes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of attribute names whose values slapd should keep sorted.
     * This is the internal list — callers should treat as read-only unless they
     * know what they're doing.
     *
     * <p>For example — Palpatine reads the sort-directive list:</p>
     * <pre>
     *   List&lt;String&gt; sortVals = frontend.getOlcSortVals();
     *   // e.g. ["roleOccupant", "member"]
     * </pre>
     *
     * @return  the list of attribute names that should have their values sorted
     */
    public List<String> getOlcSortVals()
    {
        return olcSortVals;
    }


    // ── Issuing A New Default Territory Directive ─────────────────────────────────
    // Palpatine rewrites the standing order that names the default Imperial sector —
    // from now on, all operations without an explicit sector designation land here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the {@code olcDefaultSearchBase} DN string — the base DN used for
     * searches with an empty base DN.
     *
     * <p>For example — Palpatine designates a new default Imperial sector:</p>
     * <pre>
     *   frontend.setOlcDefaultSearchBase("dc=example,dc=com");
     *   // all empty-base searches now start at dc=example,dc=com
     * </pre>
     *
     * @param olcDefaultSearchBase  the default search base DN string to set
     */
    public void setOlcDefaultSearchBase( String olcDefaultSearchBase )
    {
        this.olcDefaultSearchBase = olcDefaultSearchBase;
    }


    // ── Replacing The Entire Approved Cipher List ─────────────────────────────────
    // Palpatine replaces the entire approved cipher list in his standing orders —
    // not just adding to it, but substituting a completely new set of approved
    // hashing algorithms across the galaxy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire password hash list with the given list. This replaces
     * rather than appends — if you want to add to the existing list, use
     * {@link #addOlcPasswordHash} instead.
     *
     * <p>For example — Palpatine issues a completely new cipher directive:</p>
     * <pre>
     *   frontend.setOlcPasswordHash(Arrays.asList("{SSHA}", "{SHA}"));
     *   // previous list is gone; these two are now the galaxy-wide standard
     * </pre>
     *
     * @param olcPasswordHash  the new list of password hash scheme strings
     */
    public void setOlcPasswordHash( List<String> olcPasswordHash )
    {
        this.olcPasswordHash = olcPasswordHash;
    }


    // ── Replacing The Galaxy-Wide Sort Directive List ─────────────────────────────
    // Palpatine replaces the entire sort-values standing order — a full list swap,
    // not a partial amendment. The new list defines exactly which attributes must
    // be kept sorted across all databases going forward.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire sort-vals list with the given list. This replaces rather
     * than appends — use {@link #addOlcSortVals} to add to the existing list.
     *
     * <p>For example — Palpatine issues a completely new sort directive:</p>
     * <pre>
     *   frontend.setOlcSortVals(Arrays.asList("member", "uniqueMember"));
     *   // old list is gone; these two attributes now get galaxy-wide sorting
     * </pre>
     *
     * @param olcSortVals  the new list of attribute names whose values should be sorted
     */
    public void setOlcSortVals( List<String> olcSortVals )
    {
        this.olcSortVals = olcSortVals;
    }
}
