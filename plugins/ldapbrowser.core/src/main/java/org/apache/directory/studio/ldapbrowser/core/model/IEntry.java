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

package org.apache.directory.studio.ldapbrowser.core.model;


import java.io.Serializable;
import java.util.Collection;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.ConnectionPropertyPageProvider;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.EntryPropertyPageProvider;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: IEntry — THE DEATH STAR BLUEPRINT ──────────────────────────────────
// When R2-D2 finally extracts the Death Star schematics from the Imperial data
// vault, what he holds is the complete technical blueprint: every sub-system
// listed by name, every design detail accessible by catalog number, and a
// hierarchical structure of sections and sub-sections hanging off one root.
// An IEntry is exactly that: the in-memory blueprint of a single LDAP directory
// entry.  It knows its DN (catalog number), its attributes (technical specs),
// its children (sub-section entries), and its connection to the LDAP server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a single LDAP directory entry in the browser model.
 * An entry is identified by a distinguished name (DN), holds named attributes
 * (each with one or more values), and may have child entries beneath it forming
 * the directory information tree (DIT).
 * Think of this as the Death Star blueprint: a self-contained data structure
 * whose every field, flag, and sub-section is accessible through this interface.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IEntry extends Serializable, IAdaptable, EntryPropertyPageProvider, ConnectionPropertyPageProvider
{

    // ── R2-D2 Inserts A Sub-Schematic Into The Blueprint ─────────────────────────
    // The droid slots the reactor-core schematic into the correct section of the
    // Death Star plans — it's now part of the hierarchy, addressable by its own
    // catalog number.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the given child entry under this entry in the in-memory tree.
     * The child must already know its own DN; we just attach it here so the
     * browser tree can navigate to it.
     *
     * @param childToAdd the child entry to attach; must not be {@code null}.
     */
    void addChild( IEntry childToAdd );


    // ── R2-D2 Removes A Sub-Schematic And All Its Pages ──────────────────────────
    // "Purge section 7G — thermal exhaust port and all dependent schematics."
    // Deleting a child also removes everything beneath it in the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given child entry and all of its descendants from this entry's
     * in-memory child list.
     * Does not touch the LDAP server — this is a model-only operation triggered
     * after a successful server-side delete.
     *
     * @param childToDelete the child entry to remove; must not be {@code null}.
     */
    void deleteChild( IEntry childToDelete );


    // ── R2-D2 Registers A Technical Spec In The Blueprint ────────────────────────
    // "Turbolaser array specifications — catalogued under this schematic section."
    // Each attribute is a named spec; attaching it here makes it part of the entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the given attribute to this entry's attribute map.
     * The attribute's {@link IAttribute#getEntry()} must already point to this
     * entry; we reject it if it belongs to another entry or already exists here.
     *
     * @param attributeToAdd the attribute to attach; must not be {@code null}.
     * @throws IllegalArgumentException if the attribute already exists in this entry
     *                                  or if the attribute's entry is not this entry.
     */
    void addAttribute( IAttribute attributeToAdd ) throws IllegalArgumentException;


    // ── R2-D2 Removes A Technical Spec From The Blueprint ────────────────────────
    // "Remove the thermal exhaust port spec — we don't want the Rebels finding it."
    // The attribute is detached from the in-memory model after a successful server
    // delete.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given attribute from this entry's attribute map.
     * Called after a successful LDAP delete-attribute operation so the in-memory
     * model stays in sync with the server.
     *
     * @param attributeToDelete the attribute to remove; must not be {@code null}.
     * @throws IllegalArgumentException if the attribute is not present in this entry.
     */
    void deleteAttribute( IAttribute attributeToDelete ) throws IllegalArgumentException;


    // ── R2-D2 Marks Whether The Blueprint Was Fetched From The Actual Archive ─────
    // A blueprint is either an official Imperial copy (directoryEntry = true) or
    // a local mock-up used for testing (directoryEntry = false).  This flag
    // tells the UI whether the entry is real or a stand-in.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether this entry genuinely exists in the LDAP directory.
     * {@code true} means it was read from or created in the server;
     * {@code false} means it is a local placeholder (e.g. a dummy root entry).
     *
     * @param isDirectoryEntry {@code true} if this is a real directory entry.
     */
    void setDirectoryEntry( boolean isDirectoryEntry );


    // ── The Blueprint Notes: This Section Is An Alias ────────────────────────────
    // Some schematics are just pointers — "See Death Star II plans, section 4."
    // LDAP alias entries work the same way: they point elsewhere in the DIT.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this entry is an LDAP alias entry.
     * An alias entry has the {@code alias} objectClass and its
     * {@code aliasedObjectName} attribute points to the "real" entry.
     * The alias flag may be set even before attributes are loaded if it was
     * signalled during a search.
     *
     * @return {@code true} if this entry is an alias.
     */
    boolean isAlias();


    // ── R2-D2 Sets The Alias Flag On The Schematic ───────────────────────────────
    // The droid stamps "ALIAS" on the schematic page when a search tells him
    // this entry is just a pointer to another section.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the alias hint for this entry.
     * Called during a search when the server signals that this entry is (or is
     * not) an alias, even before the entry's full attributes have been loaded.
     *
     * @param b {@code true} to mark this entry as an alias.
     */
    void setAlias( boolean b );


    // ── The Blueprint Notes: This Section Is A Referral ──────────────────────────
    // A referral is like a "For full plans, contact the Kuat Drive Yards" note:
    // the entry says "go elsewhere for the real data."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this entry is an LDAP referral entry.
     * A referral entry has the {@code referral} objectClass and contains a
     * {@code ref} attribute pointing to another LDAP URL.
     *
     * @return {@code true} if this entry is a referral.
     */
    boolean isReferral();


    // ── R2-D2 Stamps The Referral Flag On The Schematic ──────────────────────────
    // The droid marks "REFERRAL" when the search signals this is a redirect entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the referral hint for this entry.
     *
     * @param b {@code true} to mark this entry as a referral.
     */
    void setReferral( boolean b );


    // ── The Blueprint Notes: This Section Is A Subentry ──────────────────────────
    // Subentries are special administrative sections of the DIT — they hold
    // schema rules and access control policies.  "Classified: Admin Eyes Only."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this entry is an LDAP subentry.
     * Subentries hold administrative information such as schema or access control
     * policies and are normally hidden from regular searches.
     *
     * @return {@code true} if this entry is a subentry.
     */
    boolean isSubentry();


    // ── R2-D2 Stamps The Subentry Flag On The Schematic ──────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the subentry hint for this entry.
     *
     * @param b {@code true} to mark this entry as a subentry.
     */
    void setSubentry( boolean b );


    // ── R2-D2 Reads The Catalog Number Off The Blueprint Cover ───────────────────
    // Every Death Star schematic has a catalog number on the cover —
    // "DS-1 Orbital Battle Station, plan ref: ou=weapons,dc=empire,dc=gov".
    // That catalog number is the DN; it uniquely identifies the entry in the DIT.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the distinguished name (DN) of this entry.
     * The DN uniquely identifies the entry in the directory information tree.
     * For example: {@code cn=Han Solo,ou=crew,dc=rebel,dc=org}.
     *
     * @return the {@link Dn}; never {@code null}.
     */
    Dn getDn();


    // ── R2-D2 Reads The Last Section Identifier ──────────────────────────────────
    // The RDN (relative distinguished name) is the last component of the DN —
    // the local name within the parent section: {@code cn=Han Solo}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the relative distinguished name (RDN) of this entry.
     * The RDN is the rightmost component of the DN — the entry's local name
     * within its parent container.
     *
     * @return the {@link Rdn}; never {@code null}.
     */
    Rdn getRdn();


    // ── R2-D2 Checks Whether All Tech Specs Have Been Downloaded ─────────────────
    // The droid can have the blueprint cover page (DN, RDN) without having the
    // full technical specs yet — he'd need another server read to get those.
    // This flag tells us whether the attributes have been fetched.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this entry's attributes have been fully loaded
     * from the LDAP server.
     * {@code false} means the attribute list is empty or only partially populated.
     * Check this before calling {@link #getAttributes()} if you need complete data.
     *
     * @return {@code true} if attributes are initialised.
     */
    boolean isAttributesInitialized();


    // ── R2-D2 Stamps "Attributes Loaded" On The Blueprint ────────────────────────
    // Once all the tech specs arrive from the server, the droid marks the
    // blueprint as complete so nobody triggers an unnecessary second download.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the attributes-initialised flag.
     * Called by the {@link org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable}
     * after it successfully loads the entry's attributes.
     *
     * @param b {@code true} once all attributes have been loaded.
     */
    void setAttributesInitialized( boolean b );


    // ── R2-D2 Checks Whether Operational Specs Should Be Fetched ─────────────────
    // Operational attributes are the "internal maintenance records" — create
    // timestamps, modify timestamps, subschema locations.  We only fetch them
    // if the user asked for them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if operational attributes should be loaded for this entry.
     * Operational attributes (e.g. {@code createTimestamp}, {@code modifyTimestamp})
     * are fetched from the server only when this flag is set.
     *
     * @return {@code true} if operational attributes should be initialised.
     */
    boolean isInitOperationalAttributes();


    // ── R2-D2 Toggles The "Fetch Maintenance Records" Switch ─────────────────────
    // "Load all the internal maintenance data too" — or don't.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether operational attributes should be fetched for this entry.
     *
     * @param b {@code true} to request operational attribute initialisation.
     */
    void setInitOperationalAttributes( boolean b );


    // ── The Blueprint Decides Whether To Expand Alias Sub-Sections ───────────────
    // Sometimes you want to follow alias pointers into their target sections;
    // sometimes you want to see the alias entries as-is.  This flag controls that.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if alias child entries should be fetched when loading
     * this entry's children.
     *
     * @return {@code true} if alias children should be fetched.
     */
    boolean isFetchAliases();


    // ── R2-D2 Sets The Alias-Children Switch ─────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether alias child entries should be fetched when loading children.
     *
     * @param b {@code true} to include alias children.
     */
    void setFetchAliases( boolean b );


    // ── The Blueprint Decides Whether To Follow Referral Sub-Sections ─────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if referral child entries should be fetched when loading
     * this entry's children.
     *
     * @return {@code true} if referral children should be fetched.
     */
    boolean isFetchReferrals();


    // ── R2-D2 Sets The Referral-Children Switch ───────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether referral child entries should be fetched when loading children.
     *
     * @param b {@code true} to include referral children.
     */
    void setFetchReferrals( boolean b );


    // ── The Blueprint Decides Whether To Include Administrative Sub-Entries ───────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if subentry child entries should be fetched when loading
     * this entry's children.
     *
     * @return {@code true} if subentry children should be fetched.
     */
    boolean isFetchSubentries();


    // ── R2-D2 Sets The Sub-Entries Switch ────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether subentry child entries should be fetched when loading children.
     *
     * @param b {@code true} to include subentry children.
     */
    void setFetchSubentries( boolean b );


    // ── R2-D2 Retrieves All Technical Specs From The Blueprint ───────────────────
    // Once the full blueprint is downloaded, the droid can hand over all the
    // technical specifications in one go.  If they haven't been fetched yet,
    // the result may be null or partial.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all attributes of this entry.
     * If {@link #isAttributesInitialized()} is {@code false}, the returned array
     * may be {@code null} or contain only a subset of the server-side attributes.
     *
     * @return the entry's attributes, or {@code null} if none have been loaded yet.
     */
    IAttribute[] getAttributes();


    // ── R2-D2 Looks Up A Single Named Spec In The Blueprint ──────────────────────
    // "Show me just the turbolaser power requirements" — fetch one named attribute.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute with the given description, or {@code null} if it
     * doesn't exist or the attributes haven't been initialised yet.
     *
     * @param attributeDescription the attribute name/description (e.g. {@code "mail"}).
     * @return the matching {@link IAttribute}, or {@code null}.
     */
    IAttribute getAttribute( String attributeDescription );


    // ── R2-D2 Retrieves A Spec Family, Including All Sub-Types ───────────────────
    // Some attribute types have subtypes — e.g. {@code name} covers {@code cn},
    // {@code sn}, {@code givenName}.  This fetches the whole family.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an {@link AttributeHierarchy} containing the requested attribute
     * type and all of its sub-types present on this entry.
     * Useful for editors that need to display a whole attribute family at once.
     *
     * @param attributeDescription the attribute description to search for.
     * @return the hierarchy, or {@code null} if neither the type nor any subtype exists.
     */
    AttributeHierarchy getAttributeWithSubtypes( String attributeDescription );


    // ── R2-D2 Checks Whether The Sub-Sections Have Been Explored ─────────────────
    // The blueprint might list sub-sections by name but not yet contain their
    // contents.  This flag says "yes, we went in and fetched all child pages."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this entry's children have been fully loaded
     * from the LDAP server.
     * {@code false} means the child list is empty or only partially populated —
     * the tree node will show a "Loading…" placeholder.
     *
     * @return {@code true} if children are initialised.
     */
    boolean isChildrenInitialized();


    // ── R2-D2 Stamps "Children Loaded" On The Blueprint ──────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the children-initialised flag.
     * Called after the {@link org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable}
     * completes successfully.
     *
     * @param b {@code true} once all children have been loaded.
     */
    void setChildrenInitialized( boolean b );


    // ── The Blueprint Reports Whether Sub-Sections Exist ─────────────────────────
    // The cover page of the blueprint may say "Sub-sections: yes" without yet
    // listing them.  This indicates whether children exist at all.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this entry has at least one child entry.
     * May be a hint rather than a guarantee if children haven't been loaded yet.
     *
     * @return {@code true} if this entry has children (or is expected to).
     */
    boolean hasChildren();


    // ── R2-D2 Sets The "Has Sub-Sections" Hint ────────────────────────────────────
    // During a search the server can hint that an entry has children via
    // {@code hasSubordinates} — we store that hint here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets a hint about whether this entry has children.
     * Used to control the expand triangle in the tree without loading children.
     *
     * @param b {@code true} if the entry is expected to have children.
     */
    void setHasChildrenHint( boolean b );


    // ── R2-D2 Lists All Sub-Sections Of The Blueprint ────────────────────────────
    // If children are initialised, the full list is here.  Otherwise we may get
    // {@code null} or a partial list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the child entries of this entry.
     * If {@link #isChildrenInitialized()} is {@code false}, the result may be
     * {@code null} or contain only a subset of the server-side children.
     *
     * @return the child entries, or {@code null} if none have been loaded yet.
     */
    IEntry[] getChildren();


    // ── The Blueprint Reports The Count Of Sub-Sections ──────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of child entries currently loaded in memory.
     * Returns {@code -1} if no children have been loaded yet.
     *
     * @return the child count, or {@code -1} if children haven't been initialised.
     */
    int getChildrenCount();


    // ── The Blueprint Reports Whether The Count Was Cut Off ───────────────────────
    // The server may impose a count or time limit, truncating the child list.
    // This flag tells the UI to show "more…" and offer a "Next page" button.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the server returned more children than were fetched —
     * i.e. the count or time limit was hit.
     * When this is {@code true} the UI typically shows a "More results…" indicator.
     *
     * @return {@code true} if there are more children than currently loaded.
     */
    boolean hasMoreChildren();


    // ── R2-D2 Flags "Count Limit Hit — More Pages Exist" ─────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether there are more children beyond the current page.
     *
     * @param b {@code true} if the server has more children than are currently held.
     */
    void setHasMoreChildren( boolean b );


    // ── The Blueprint Keeps A Runnable For Fetching The First Page ────────────────
    // If children are paged (the server uses Paged Results control), we store
    // the runnable that fetches page one so the UI can trigger it on demand.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the runnable responsible for fetching the first page of children.
     * {@code null} if no paging runnable has been set.
     *
     * @return the top-page runnable, or {@code null}.
     */
    StudioConnectionBulkRunnableWithProgress getTopPageChildrenRunnable();


    // ── R2-D2 Stores The First-Page Fetch Runnable ───────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the runnable for fetching the first page of children.
     * Set by the job that initialises children when paging is in use.
     *
     * @param topPageChildrenRunnable the runnable; {@code null} clears it.
     */
    void setTopPageChildrenRunnable( StudioConnectionBulkRunnableWithProgress topPageChildrenRunnable );


    // ── The Blueprint Keeps A Runnable For Fetching The Next Page ────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the runnable responsible for fetching the next page of children.
     * {@code null} if no more pages are available.
     *
     * @return the next-page runnable, or {@code null}.
     */
    StudioConnectionBulkRunnableWithProgress getNextPageChildrenRunnable();


    // ── R2-D2 Stores The Next-Page Fetch Runnable ────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the runnable for fetching the next page of children.
     *
     * @param nextPageChildrenRunnable the runnable; {@code null} clears it.
     */
    void setNextPageChildrenRunnable( StudioConnectionBulkRunnableWithProgress nextPageChildrenRunnable );


    // ── The Blueprint Reports Whether It Has A Parent Section ────────────────────
    // Every section of the Death Star plans except the root cover page belongs
    // to a parent section.  The root DSE and base DN entries have no parent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this entry has a parent entry in the tree.
     * The root DSE and base DN entries have no parent; all other entries do.
     *
     * @return {@code true} if there is a parent entry.
     */
    boolean hasParententry();


    // ── R2-D2 Retrieves The Parent Section Of The Blueprint ──────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent entry, or {@code null} if this is a root or base entry.
     *
     * @return the parent {@link IEntry}, or {@code null}.
     */
    IEntry getParententry();


    // ── The Blueprint Holds A Children Filter For Narrow Views ───────────────────
    // We can restrict which child entries appear in the tree by applying an
    // LDAP filter — like filtering the Death Star plans to show only weapon systems.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP filter string applied to this entry's children, or
     * {@code null} if no filter is set.
     * When set, only children matching this filter are shown in the browser tree.
     *
     * @return the children filter string, or {@code null}.
     */
    String getChildrenFilter();


    // ── R2-D2 Sets (Or Clears) The Children Filter ───────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the children filter for this entry.
     * Pass {@code null} to clear any existing filter and show all children.
     *
     * @param filter the LDAP filter string (e.g. {@code "(objectClass=person)"}),
     *               or {@code null} to clear.
     */
    void setChildrenFilter( String filter );


    // ── R2-D2 Identifies Which Server The Blueprint Came From ────────────────────
    // The blueprint has a provenance stamp: "Obtained from the Imperial data vault
    // at ldaps://deathstar.empire.gov:636."  This returns that connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection that owns this entry.
     * Every entry is associated with exactly one connection — the LDAP server
     * it was read from.
     *
     * @return the {@link IBrowserConnection}; never {@code null}.
     */
    IBrowserConnection getBrowserConnection();


    // ── R2-D2 Constructs The Full LDAP URL For The Blueprint ─────────────────────
    // The LDAP URL packages host, port, and DN into a single addressable string:
    // {@code ldap://host:389/cn=entry,dc=example,dc=com}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP URL that addresses this specific entry.
     * The URL encodes the server host/port and this entry's DN so it can be
     * shared or bookmarked.
     *
     * @return the {@link LdapUrl} for this entry.
     */
    LdapUrl getUrl();


    // ── R2-D2 Lists The Blueprint's Object-Class Designations ────────────────────
    // Every LDAP entry's objectClass attribute defines what type of object it is —
    // "person", "organizationalUnit", "groupOfNames", etc.  Those schema definitions
    // are returned here so the UI knows which attributes are mandatory vs optional.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the schema objectClass descriptions for this entry.
     * Derived from the entry's {@code objectClass} attribute values combined with
     * the server's schema.  Used by the attribute editor to know which attributes
     * are mandatory, optional, or inherited.
     *
     * @return a collection of {@link ObjectClass} definitions; may be empty but never {@code null}.
     */
    Collection<ObjectClass> getObjectClassDescriptions();

}
