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

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.ConnectionPropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.BookmarkManager;
import org.apache.directory.studio.ldapbrowser.core.SearchManager;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: IBrowserConnection — HAN SOLO COMMITTING TO THE KESSEL RUN ────────
// Han Solo punches the hyperdrive and commits the Millennium Falcon to the
// Kessel Run: he has a destination (the LDAP server URL), a route strategy
// (alias/referral handling, paging), fuel limits (count/time limits), and a
// full cargo manifest of entries cached in memory.  Once he's committed, the
// whole browser model hangs off this connection — the entry cache, the schema,
// the saved searches, the bookmarks.
// IBrowserConnection is that hyperdrive commit: the root object in the browser
// model that combines a raw {@link Connection} with all the LDAP-browser-specific
// parameters and the in-memory cache of fetched entries and schema.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The browser-level representation of an LDAP server connection.
 * Wraps a raw {@link Connection} (which handles the network socket and
 * authentication) and adds:
 * <ul>
 *   <li>LDAP browser parameters — base DN, count/time limits, alias and
 *       referral handling, paging, modify mode, etc.</li>
 *   <li>An in-memory entry cache keyed by {@link Dn}.</li>
 *   <li>The server's {@link Schema}.</li>
 *   <li>A {@link SearchManager} and {@link BookmarkManager}.</li>
 *   <li>The root DSE ({@link IRootDSE}) — the top of the directory tree.</li>
 * </ul>
 * Think of this as Han committing to the Kessel Run: once this connection is
 * open, the whole browser world hangs off it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IBrowserConnection extends Serializable, IAdaptable, ConnectionPropertyPageProvider
{
    // ── ENUM: ModifyMode — HAN'S NAVIGATION STRATEGY CHOICE ─────────────────────
    // "Do we swap out the navicomp route in one atomic replace, or do we delete
    // the old route and add the new one separately?"  Han picks his strategy
    // based on the server's LDAP dialect.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Controls how attribute modifications are sent to the server.
     * Different LDAP servers behave differently, so we let the user choose
     * whether to use a REPLACE modify operation or a pair of ADD + DELETE.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    enum ModifyMode
    {
        /** Use the server's default behaviour (usually replace for single-valued, add/delete for multi-valued). */
        DEFAULT(0),

        /** Always send a REPLACE modify operation, regardless of attribute type. */
        REPLACE(1),

        /** Always send an ADD followed by a DELETE modify operation. */
        ADD_DELETE(2);

        private final int ordinal;


        private ModifyMode( int ordinal )
        {
            this.ordinal = ordinal;
        }


        /**
         * Returns the integer ordinal for serialisation.
         *
         * @return the ordinal value (0 = DEFAULT, 1 = REPLACE, 2 = ADD_DELETE).
         */
        public int getOrdinal()
        {
            return ordinal;
        }


        /**
         * Looks up a {@link ModifyMode} by its integer ordinal.
         * Returns {@code null} for unknown values.
         *
         * @param ordinal the ordinal to look up.
         * @return the matching {@link ModifyMode}, or {@code null}.
         */
        public static ModifyMode getByOrdinal( int ordinal )
        {
            switch ( ordinal )
            {
                case 0:
                    return DEFAULT;
                case 1:
                    return REPLACE;
                case 2:
                    return ADD_DELETE;
                default:
                    return null;
            }
        }
    }

    // ── ENUM: ModifyOrder — DELETE FIRST OR ADD FIRST? ────────────────────────────
    // When Han has to swap a cargo bay module — does he offload the old module
    // first (DELETE_FIRST) or load the new one in first (ADD_FIRST)?  Some
    // servers require a specific order when both operations happen together.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Controls the order of ADD and DELETE operations when modifying a multi-valued
     * attribute using the ADD_DELETE {@link ModifyMode}.
     * Some LDAP servers reject a DELETE of a value that isn't yet present if the
     * ADD of the new value happens first — {@code DELETE_FIRST} avoids that.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    enum ModifyOrder
    {
        /** Send the DELETE before the ADD. */
        DELETE_FIRST(0),

        /** Send the ADD before the DELETE. */
        ADD_FIRST(1);

        private final int ordinal;


        private ModifyOrder( int ordinal )
        {
            this.ordinal = ordinal;
        }


        /**
         * Returns the integer ordinal for serialisation.
         *
         * @return the ordinal value (0 = DELETE_FIRST, 1 = ADD_FIRST).
         */
        public int getOrdinal()
        {
            return ordinal;
        }


        /**
         * Looks up a {@link ModifyOrder} by its integer ordinal.
         *
         * @param ordinal the ordinal to look up.
         * @return the matching {@link ModifyOrder}, or {@code null}.
         */
        public static ModifyOrder getByOrdinal( int ordinal )
        {
            switch ( ordinal )
            {
                case 0:
                    return DELETE_FIRST;
                case 1:
                    return ADD_FIRST;
                default:
                    return null;
            }
        }
    }

    /** Connection-parameter key: whether to auto-fetch base DNs from the Root DSE. */
    String CONNECTION_PARAMETER_FETCH_BASE_DNS = "ldapbrowser.fetchBaseDns"; //$NON-NLS-1$

    /** Connection-parameter key: manually defined base DN. */
    String CONNECTION_PARAMETER_BASE_DN = "ldapbrowser.baseDn"; //$NON-NLS-1$

    /** Connection-parameter key: maximum number of entries to return per search. */
    String CONNECTION_PARAMETER_COUNT_LIMIT = "ldapbrowser.countLimit"; //$NON-NLS-1$

    /** Connection-parameter key: maximum time (seconds) to wait for a search. */
    String CONNECTION_PARAMETER_TIME_LIMIT = "ldapbrowser.timeLimit"; //$NON-NLS-1$

    /** Connection-parameter key: how to handle LDAP alias entries during browsing. */
    String CONNECTION_PARAMETER_ALIASES_DEREFERENCING_METHOD = "ldapbrowser.aliasesDereferencingMethod"; //$NON-NLS-1$

    /** Connection-parameter key: how to handle LDAP referral entries during browsing. */
    String CONNECTION_PARAMETER_REFERRALS_HANDLING_METHOD = "ldapbrowser.referralsHandlingMethod"; //$NON-NLS-1$

    /** Connection-parameter key: whether to fetch operational attributes alongside regular ones. */
    String CONNECTION_PARAMETER_FETCH_OPERATIONAL_ATTRIBUTES = "ldapbrowser.fetchOperationalAttributes"; //$NON-NLS-1$

    /** Connection-parameter key: whether to show subentry children in the tree. */
    String CONNECTION_PARAMETER_FETCH_SUBENTRIES = "ldapbrowser.fetchSubentries"; //$NON-NLS-1$

    /** Connection-parameter key: whether to use the LDAP Paged Results control. */
    String CONNECTION_PARAMETER_PAGED_SEARCH = "ldapbrowser.pagedSearch"; //$NON-NLS-1$

    /** Connection-parameter key: number of entries per page when paging is enabled. */
    String CONNECTION_PARAMETER_PAGED_SEARCH_SIZE = "ldapbrowser.pagedSearchSize"; //$NON-NLS-1$

    /** Connection-parameter key: whether paged results should be shown in scroll (vs. page-at-a-time) mode. */
    String CONNECTION_PARAMETER_PAGED_SEARCH_SCROLL_MODE = "ldapbrowser.pagedSearchScrollMode"; //$NON-NLS-1$

    /** Connection-parameter key: modify mode for attributes that have an equality matching rule. */
    String CONNECTION_PARAMETER_MODIFY_MODE = "ldapbrowser.modifyMode"; //$NON-NLS-1$

    /** Connection-parameter key: modify mode for attributes that lack an equality matching rule. */
    String CONNECTION_PARAMETER_MODIFY_MODE_NO_EMR = "ldapbrowser.modifyModeNoEMR"; //$NON-NLS-1$

    /** Connection-parameter key: whether to send DELETE before ADD or vice-versa in add/delete mode. */
    String CONNECTION_PARAMETER_MODIFY_ORDER = "ldapbrowser.modifyOrder"; //$NON-NLS-1$

    /** Connection-parameter key: whether to attach the ManageDsaIT control to operations. */
    String CONNECTION_PARAMETER_MANAGE_DSA_IT = "ldapbrowser.manageDsaIT"; //$NON-NLS-1$


    // ── Han Reads The Destination Coordinates ────────────────────────────────────
    // "Where are we going?" — the LDAP URL encodes host, port, and optionally
    // a base DN into a single string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP URL for this connection, encoding host, port, and
     * (if applicable) the base DN.
     *
     * @return the {@link LdapUrl}; never {@code null}.
     */
    LdapUrl getUrl();


    // ── Han Checks Whether To Auto-Detect The Base DN ────────────────────────────
    // "Do we let the server tell us where the cargo bays are, or did we specify
    // a manual destination?"  Auto-fetch = ask Root DSE; manual = we know already.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the browser should automatically fetch base DNs
     * from the server's Root DSE, rather than using a manually defined base DN.
     *
     * @return {@code true} to fetch from Root DSE; {@code false} to use a manual DN.
     */
    boolean isFetchBaseDNs();


    // ── Han Toggles The Auto-Detect Switch ───────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether base DNs should be fetched automatically from the Root DSE.
     *
     * @param fetchBaseDNs {@code true} to auto-fetch; {@code false} to use a manual DN.
     */
    void setFetchBaseDNs( boolean fetchBaseDNs );


    // ── Han Reads The Manually Set Destination ───────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the manually defined base DN.
     * Only used when {@link #isFetchBaseDNs()} is {@code false}.
     *
     * @return the manually configured base {@link Dn}.
     */
    Dn getBaseDN();


    // ── Han Sets The Manual Destination ──────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the manually defined base DN.
     *
     * @param baseDn the new base {@link Dn}.
     */
    void setBaseDN( Dn baseDn );


    // ── Han Sets The Cargo Manifest Limit ────────────────────────────────────────
    // "We can't carry more than 500 crates — tell the server to stop at 500."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of entries to return per search.
     * A value of 0 means no limit (use the server's default).
     *
     * @return the count limit.
     */
    int getCountLimit();


    // ── Han Sets The Cargo Manifest Limit ────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the count limit for searches on this connection.
     *
     * @param countLimit the maximum number of entries; 0 means no limit.
     */
    void setCountLimit( int countLimit );


    // ── Han Reads The Alias Policy ────────────────────────────────────────────────
    // "Do we follow the docking-bay alias signs or navigate directly?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the alias dereferencing method for searches on this connection.
     *
     * @return the {@link AliasDereferencingMethod}; never {@code null}.
     */
    AliasDereferencingMethod getAliasesDereferencingMethod();


    // ── Han Sets The Alias Policy ─────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the alias dereferencing method for searches on this connection.
     *
     * @param aliasesDereferencingMethod the new method; must not be {@code null}.
     */
    void setAliasesDereferencingMethod( AliasDereferencingMethod aliasesDereferencingMethod );


    // ── Han Reads The Referral Policy ────────────────────────────────────────────
    // "If a docking bay refers us to another bay on another moon, do we follow?"
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the referral handling method for searches on this connection.
     *
     * @return the {@link ReferralHandlingMethod}; never {@code null}.
     */
    ReferralHandlingMethod getReferralsHandlingMethod();


    // ── Han Sets The Referral Policy ─────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the referral handling method for searches on this connection.
     *
     * @param referralsHandlingMethod the new method; must not be {@code null}.
     */
    void setReferralsHandlingMethod( ReferralHandlingMethod referralsHandlingMethod );


    // ── Han Sets The Jump Timer ───────────────────────────────────────────────────
    // "If the server doesn't respond in N seconds, abort and try something else."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum time (in seconds) to wait for a search to complete.
     * A value of 0 means no limit.
     *
     * @return the time limit in seconds.
     */
    int getTimeLimit();


    // ── Han Sets The Jump Timer ───────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the time limit for searches on this connection.
     *
     * @param timeLimit the maximum wait in seconds; 0 means no limit.
     */
    void setTimeLimit( int timeLimit );


    // ── Han Checks Whether To Include Hidden Administrative Bays ─────────────────
    // Subentries are like the Empire's hidden administrative bays — they're there
    // but not shown by default unless you specifically ask for them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if subentry child entries should be fetched when
     * browsing this connection's tree.
     *
     * @return {@code true} to fetch subentries.
     */
    boolean isFetchSubentries();


    // ── Han Toggles The Hidden-Bays Switch ───────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether subentry children should be fetched during browsing.
     *
     * @param fetchSubentries {@code true} to include subentries.
     */
    void setFetchSubentries( boolean fetchSubentries );


    // ── Han Checks Whether To Use ManageDsaIT ────────────────────────────────────
    // The ManageDsaIT control tells the server "treat referral entries as
    // regular entries" — useful for editing referrals themselves.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the ManageDsaIT LDAP control should be sent with
     * requests.  This instructs the server to treat referral entries as regular
     * entries so they can be read and modified directly.
     *
     * @return {@code true} if ManageDsaIT is enabled.
     */
    boolean isManageDsaIT();


    // ── Han Toggles The ManageDsaIT Control ──────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether the ManageDsaIT control should be used.
     *
     * @param manageDsaIT {@code true} to enable ManageDsaIT.
     */
    void setManageDsaIT( boolean manageDsaIT );


    // ── Han Checks Whether Operational Specs Are Fetched ─────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if operational attributes (timestamps, entryDN, etc.)
     * should be fetched alongside regular attributes.
     *
     * @return {@code true} to fetch operational attributes.
     */
    boolean isFetchOperationalAttributes();


    // ── Han Toggles The Operational-Specs Switch ──────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether operational attributes should be fetched.
     *
     * @param fetchOperationalAttributes {@code true} to include operational attributes.
     */
    void setFetchOperationalAttributes( boolean fetchOperationalAttributes );


    // ── Han Checks Whether Paged Results Are Active ───────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the LDAP Paged Results control should be used for
     * large result sets.  Paging avoids loading all results at once.
     *
     * @return {@code true} if paged search is enabled.
     */
    boolean isPagedSearch();


    // ── Han Toggles Paged Results ─────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether paged search is used.
     *
     * @param pagedSearch {@code true} to enable the Paged Results control.
     */
    void setPagedSearch( boolean pagedSearch );


    // ── Han Reads The Page Size ───────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of entries per page when paged search is enabled.
     *
     * @return the paged search page size.
     */
    int getPagedSearchSize();


    // ── Han Sets The Page Size ────────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the page size for paged searches.
     *
     * @param pagedSearchSize the number of entries per page.
     */
    void setPagedSearchSize( int pagedSearchSize );


    // ── Han Checks Whether Scrolling Mode Is Active ───────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if paged results should be accumulated in a scrollable
     * view rather than one page at a time.
     *
     * @return {@code true} if scroll mode is enabled.
     */
    boolean isPagedSearchScrollMode();


    // ── Han Toggles Scroll Mode ───────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether paged search uses scroll mode.
     *
     * @param pagedSearchScrollMode {@code true} for scroll mode.
     */
    void setPagedSearchScrollMode( boolean pagedSearchScrollMode );


    // ── Han Reads The Cargo-Swap Strategy ────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the modify mode for attributes that have an equality matching rule.
     *
     * @return the {@link ModifyMode}.
     */
    ModifyMode getModifyMode();


    // ── Han Sets The Cargo-Swap Strategy ─────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the modify mode for attributes with an equality matching rule.
     *
     * @param mode the new {@link ModifyMode}.
     */
    void setModifyMode( ModifyMode mode );


    // ── Han Reads The EMR-Less Cargo-Swap Strategy ────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the modify mode for attributes that lack an equality matching rule.
     * Some attribute types have no equality rule, which means the server can't
     * compare old and new values — so a REPLACE may be the only safe option.
     *
     * @return the {@link ModifyMode} for no-EMR attributes.
     */
    ModifyMode getModifyModeNoEMR();


    // ── Han Sets The EMR-Less Cargo-Swap Strategy ─────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the modify mode for attributes without an equality matching rule.
     *
     * @param mode the new {@link ModifyMode}.
     */
    void setModifyModeNoEMR( ModifyMode mode );


    // ── Han Reads The Offload/Load Order ──────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the order in which ADD and DELETE operations are sent to the server
     * when using {@link ModifyMode#ADD_DELETE}.
     *
     * @return the {@link ModifyOrder}.
     */
    ModifyOrder getModifyAddDeleteOrder();


    // ── Han Sets The Offload/Load Order ───────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the ADD/DELETE order for modify operations.
     *
     * @param mode the new {@link ModifyOrder}.
     */
    void setModifyAddDeleteOrder( ModifyOrder mode );


    // ── Han Stores The Quick-Search Preset ────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the quick-search definition for this connection.
     * Quick search is the instant-search bar at the top of the browser view.
     *
     * @param quickSearch the new quick search; may be {@code null} to clear.
     */
    void setQuickSearch( IQuickSearch quickSearch );


    // ── Han Retrieves The Quick-Search Preset ────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the quick-search definition for this connection.
     *
     * @return the {@link IQuickSearch}, or {@code null} if none is configured.
     */
    IQuickSearch getQuickSearch();


    // ── Han Checks The Root Of The Directory Tree ─────────────────────────────────
    // The Root DSE is the top of the directory — the "root of all roots."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Root DSE (Distinguished Service Entry) for this connection.
     * The Root DSE is the topmost entry in the directory and contains metadata
     * such as supported controls, naming contexts, and schema location.
     *
     * @return the {@link IRootDSE}; never {@code null} once the connection is initialised.
     */
    IRootDSE getRootDSE();


    // ── The Jedi Archives On Board The Falcon ────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP schema loaded from this server.
     * The schema describes all attribute types, object classes, and syntaxes
     * available on the server.
     *
     * @return the {@link Schema}; never {@code null} (falls back to a default schema).
     */
    Schema getSchema();


    // ── Han Installs A New Jedi Archives Module ────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the schema for this connection.
     * Called after a schema reload job completes.
     *
     * @param schema the new {@link Schema}; must not be {@code null}.
     */
    void setSchema( Schema schema );


    // ── Han Checks The Search Mission Log ────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link SearchManager} that manages saved searches for this connection.
     *
     * @return the search manager; never {@code null}.
     */
    SearchManager getSearchManager();


    // ── Han Checks The Bookmarked Waypoints ───────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BookmarkManager} that manages saved entry shortcuts for
     * this connection.
     *
     * @return the bookmark manager; never {@code null}.
     */
    BookmarkManager getBookmarkManager();


    // ── Han Checks The Cargo Hold For A Known Crate ───────────────────────────────
    // "Do we already have the entry for cn=Han Solo in the hold?  If so, use that
    // instead of going back to the warehouse."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry with the given DN from the in-memory cache, or {@code null}
     * if it hasn't been loaded yet.
     * Always check the cache before triggering a network fetch.
     *
     * @param dn the distinguished name of the entry to look up.
     * @return the cached {@link IEntry}, or {@code null} if not cached.
     */
    IEntry getEntryFromCache( Dn dn );


    // ── Han Retrieves The Underlying Network Socket ────────────────────────────────
    // The raw {@link Connection} handles actual LDAP protocol on the wire.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link Connection} that handles the raw LDAP network
     * socket, authentication, and low-level protocol.
     *
     * @return the raw {@link Connection}; never {@code null}.
     */
    Connection getConnection();


    // ── Han Stores A New Crate In The Cargo Hold ──────────────────────────────────
    // "Load this entry into the hold so we don't have to fetch it again."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the given entry to the in-memory cache.
     * Called after a job successfully fetches or creates an entry.
     *
     * @param entry the entry to cache; must not be {@code null}.
     */
    void cacheEntry( IEntry entry );


    // ── Han Empties A Cargo Bay And All Its Sub-Bays ──────────────────────────────
    // If an entry is deleted or moved, we must evict it and all of its cached
    // descendants so the stale data doesn't mislead the browser.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given entry and all of its descendants from the cache.
     * Called after a delete or move operation to prevent stale data from appearing.
     *
     * @param entry the root entry to uncache; its entire sub-tree is removed.
     */
    void uncacheEntryRecursive( IEntry entry );


    // ── Han Empties The Entire Cargo Hold ────────────────────────────────────────
    // A full schema reload or reconnect may invalidate all cached data — nuke it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears all in-memory caches for this connection, including the entry cache
     * and any other cached state.
     * Called when the connection is closed or when a full refresh is requested.
     */
    void clearCaches();
}
