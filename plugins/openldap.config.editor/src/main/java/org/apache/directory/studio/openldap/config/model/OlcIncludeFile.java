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


// ── CLASS: OlcIncludeFile — R2-D2 Fetching the External Plans ────────────────
// R2 plugs into an Imperial terminal and grabs a pointer to a remote data store
// he'll need later — not the data itself, just the address of where to fetch it.
// That's exactly what this class does: it holds the paths to external config files
// that OpenLDAP should pull in when it starts up.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds the 'olcInclude' configuration entry — essentially a list of external
 * config file paths that OpenLDAP should read and merge into its main configuration.
 * The I/O layer reads this object and knows to go fetch those extra files.
 * Think of this class as R2-D2 carrying a manifest of locations where the real
 * plans are stored.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OlcIncludeFile extends OlcConfig
{
    /**
     * Field for the 'cn' attribute.
     */
    @ConfigurationElement(attributeType = "cn", isRdn = true, version="2.4.0")
    private List<String> cn = new ArrayList<>();

    /**
     * Field for the 'olcInclude' attribute.
     */
    @ConfigurationElement(attributeType = "olcInclude", isOptional = false, version="2.4.0")
    private List<String> olcInclude = new ArrayList<>();

    /**
     * Field for the 'olcRootDSE' attribute.
     */
    @ConfigurationElement(attributeType = "olcRootDSE", version="2.4.0")
    private List<String> olcRootDSE = new ArrayList<>();


    // ── Default Constructor — R2 Powers On Empty ──────────────────────────────────
    // R2 has just been wiped and rebooted — no memory of previous missions yet.
    // He's ready to receive new coordinates and data pointers.
    // We need a blank slate so the I/O layer can populate fields via setters.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a fresh, empty OlcIncludeFile instance with no paths loaded yet.
     * The I/O layer typically uses this and then calls the setters to populate it.
     *
     * <p>For example — R2 boots up fresh:</p>
     * <pre>
     *   OlcIncludeFile f = new OlcIncludeFile();
     *   f.addOlcInclude( "file:///etc/ldap/slapd.d/extra.conf" );
     * </pre>
     */
    public OlcIncludeFile()
    {
    }


    // ── Copy Constructor — R2 Duplicates the Data Chip ───────────────────────────
    // R2 copies the memory chip from one unit to another so both droids have
    // the same mission data — a perfect duplicate, no shared references.
    // We need this to safely clone config objects during edit/undo operations.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a deep copy of an existing OlcIncludeFile, so changes to the copy
     * don't accidentally affect the original.
     *
     * <p>For example — R2 makes a backup chip:</p>
     * <pre>
     *   OlcIncludeFile original = ...;
     *   OlcIncludeFile backup = new OlcIncludeFile( original );
     *   // backup and original share no list references
     * </pre>
     *
     * @param o  the source object to copy from
     */
    public OlcIncludeFile( OlcIncludeFile o )
    {
        olcInclude = copyListString( o.olcInclude );
    }


    // ── Add CN Values — R2 Appends Identifiers ────────────────────────────────────
    // R2 tags each data packet with its identifier so the Alliance can reference it.
    // He can receive multiple identifiers in one transmission burst.
    // We bulk-add cn values here rather than calling setCn repeatedly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more cn values (the naming attribute for this entry's DN) to our list.
     * You'd typically call this when building the object from scratch rather than
     * replacing the whole list with setCn.
     *
     * <p>For example — R2 tags packets with identifiers:</p>
     * <pre>
     *   includeFile.addCn( "include0" );
     * </pre>
     *
     * @param strings  one or more cn string values to add
     */
    public void addCn( String... strings )
    {
        for ( String string : strings )
        {
            cn.add( string );
        }
    }


    // ── Add Include Paths — R2 Queues Up Data Locations ──────────────────────────
    // R2 queues a list of coordinates pointing to remote data stores.
    // Each one gets recorded so he can retrieve them all during startup.
    // We add external config file URIs to the include list here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more 'olcInclude' file paths to the list.
     * These are the external config files that slapd will read and merge at startup.
     *
     * <p>For example — R2 adds coordinates to his mission list:</p>
     * <pre>
     *   includeFile.addOlcInclude(
     *       "file:///etc/ldap/extra.conf",
     *       "file:///etc/ldap/acls.conf"
     *   );
     * </pre>
     *
     * @param strings  one or more file paths or URIs pointing to additional config files
     */
    public void addOlcInclude( String... strings )
    {
        for ( String string : strings )
        {
            olcInclude.add( string );
        }
    }


    // ── Add RootDSE Paths — R2 Tags Root Data Sources ────────────────────────────
    // R2 records extra data source references for the directory's root entry.
    // These tell slapd where to find extra root DSE attribute data.
    // We append them here so the LDAP root entry can surface extra info.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends one or more 'olcRootDSE' file paths — these tell OpenLDAP where to
     * find extra LDIF files that add extra attributes to the root DSE entry.
     *
     * <p>For example — R2 adds root data pointers:</p>
     * <pre>
     *   includeFile.addOlcRootDSE( "file:///etc/ldap/rootdse.ldif" );
     * </pre>
     *
     * @param strings  one or more file paths pointing to root DSE LDIF supplements
     */
    public void addOlcRootDSE( String... strings )
    {
        for ( String string : strings )
        {
            olcRootDSE.add( string );
        }
    }


    // ── Clear CN — R2 Wipes the ID Tags ──────────────────────────────────────────
    // R2 erases all the identifier tags from his memory buffer before reloading.
    // He's clearing the slate to accept a fresh batch of identifiers.
    // We do this when the UI replaces the cn list rather than appending to it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all cn values from this entry.
     * Useful when you want to replace the entire cn list rather than append to it.
     *
     * <p>For example — R2 clears his ID memory:</p>
     * <pre>
     *   includeFile.clearCn();
     *   includeFile.addCn( "include0" );
     * </pre>
     */
    public void clearCn()
    {
        cn.clear();
    }


    // ── Clear Include List — R2 Wipes All Mission Coordinates ────────────────────
    // R2 clears every coordinate from his mission manifest before receiving new ones.
    // He's ready to accept a completely fresh set of file locations.
    // We use this before a wholesale update of the include list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the entire olcInclude path list.
     * Call this before loading a new set of includes if you're replacing rather
     * than appending.
     *
     * <p>For example — R2 resets his mission list:</p>
     * <pre>
     *   includeFile.clearOlcInclude();
     *   includeFile.addOlcInclude( "file:///etc/ldap/new.conf" );
     * </pre>
     */
    public void clearOlcInclude()
    {
        olcInclude.clear();
    }


    // ── Clear RootDSE List — R2 Wipes Root Pointers ──────────────────────────────
    // R2 clears any root DSE data pointers before reloading from a new source.
    // He's making room for a fresh set of root entry supplements.
    // We reset the rootDSE list before a complete replacement.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all olcRootDSE paths stored in this object.
     *
     * <p>For example — R2 clears root data pointers:</p>
     * <pre>
     *   includeFile.clearOlcRootDSE();
     * </pre>
     */
    public void clearOlcRootDSE()
    {
        olcRootDSE.clear();
    }


    // ── Get CN — R2 Reads the ID Tags ────────────────────────────────────────────
    // R2 reads out his identifier tags list and hands over a copy.
    // He never gives direct access to his internal memory banks.
    // We return a defensive copy so callers can't mutate our internal list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the cn (common name) list for this include entry.
     * Modifying the returned list won't affect our internal state.
     *
     * <p>For example — R2 hands out a copy of his ID list:</p>
     * <pre>
     *   List&lt;String&gt; names = includeFile.getCn();
     *   // "include0"
     * </pre>
     *
     * @return  a copy of the cn list; never null, may be empty
     */
    public List<String> getCn()
    {
        return copyListString( cn );
    }


    // ── Get Include Paths — R2 Reads Mission Coordinates ─────────────────────────
    // R2 reads his mission manifest and hands over a copy of all recorded locations.
    // He gives a copy so nobody can tamper with his internal coordinates.
    // We return the list of external config file paths to include.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcInclude path list.
     * These paths point to additional config files that slapd will read at startup.
     *
     * <p>For example — R2 reads out his include list:</p>
     * <pre>
     *   List&lt;String&gt; paths = includeFile.getOlcInclude();
     *   // [ "file:///etc/ldap/extra.conf" ]
     * </pre>
     *
     * @return  a copy of the include path list; never null
     */
    public List<String> getOlcInclude()
    {
        return copyListString( olcInclude );
    }


    // ── Get RootDSE Paths — R2 Reads Root Data Sources ───────────────────────────
    // R2 reads out his list of root entry supplement pointers.
    // He hands over a copy so the caller can read without risk of tampering.
    // We expose the root DSE path list for serialization and display.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a defensive copy of the olcRootDSE paths list.
     *
     * <p>For example — R2 hands over root DSE pointer list:</p>
     * <pre>
     *   List&lt;String&gt; rootPaths = includeFile.getOlcRootDSE();
     * </pre>
     *
     * @return  a copy of the root DSE path list; never null
     */
    public List<String> getOlcRootDSE()
    {
        return copyListString( olcRootDSE );
    }


    // ── Set CN — R2 Receives New ID Tags ─────────────────────────────────────────
    // R2 receives a fresh set of identifier tags and stores a defensive copy.
    // He doesn't keep a reference to the caller's list — his memory is his own.
    // We defensively copy the incoming list so external mutation doesn't break us.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire cn list with a defensive copy of the given list.
     *
     * <p>For example — R2 loads new identifiers:</p>
     * <pre>
     *   includeFile.setCn( List.of( "include0" ) );
     * </pre>
     *
     * @param cn  the new list of cn values to store; may be null (treated as empty)
     */
    public void setCn( List<String> cn )
    {
        this.cn = copyListString( cn );
    }


    // ── Set Include Paths — R2 Loads Fresh Mission Manifest ──────────────────────
    // R2 receives a brand new manifest of coordinates and stores them safely.
    // He makes a copy so the caller can change their list without affecting his.
    // We replace the entire include list here, rather than appending.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the entire olcInclude path list with a defensive copy of the given list.
     *
     * <p>For example — R2 loads his fresh mission manifest:</p>
     * <pre>
     *   includeFile.setOlcInclude( List.of( "file:///etc/ldap/extra.conf" ) );
     * </pre>
     *
     * @param olcInclude  the new list of file paths; may be null (treated as empty)
     */
    public void setOlcInclude( List<String> olcInclude )
    {
        this.olcInclude = copyListString( olcInclude );
    }


    // ── Set RootDSE Paths — R2 Loads Root Data Pointers ──────────────────────────
    // R2 receives a fresh list of root entry supplement locations.
    // He stores a copy, not the original, to keep his memory independent.
    // We replace the root DSE list with a defensive copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the olcRootDSE path list with a defensive copy of the given list.
     *
     * <p>For example — R2 loads new root DSE pointers:</p>
     * <pre>
     *   includeFile.setOlcRootDSE( List.of( "file:///etc/ldap/rootdse.ldif" ) );
     * </pre>
     *
     * @param olcRootDSE  the new list of root DSE paths; may be null (treated as empty)
     */
    public void setOlcRootDSE( List<String> olcRootDSE )
    {
        this.olcRootDSE = copyListString( olcRootDSE );
    }


    // ── Copy — R2 Clones His Data Chip ───────────────────────────────────────────
    // R2 produces a complete clone of himself with an identical mission manifest.
    // The clone is fully independent — no shared references between them.
    // We implement copy() so the I/O and edit layers can safely duplicate this object.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this OlcIncludeFile.
     * Changes to the returned copy won't affect this instance.
     *
     * <p>For example — R2 clones his data chip:</p>
     * <pre>
     *   OlcIncludeFile copy = original.copy();
     *   copy.addOlcInclude( "file:///tmp/extra.conf" ); // original unchanged
     * </pre>
     *
     * @return  a new OlcIncludeFile that is a deep copy of this one
     */
    public OlcIncludeFile copy()
    {
        return new OlcIncludeFile( this );
    }
}
