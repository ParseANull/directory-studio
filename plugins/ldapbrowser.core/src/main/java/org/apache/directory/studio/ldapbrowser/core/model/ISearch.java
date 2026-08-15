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
import java.util.List;

import org.apache.directory.api.ldap.model.constants.LdapConstants;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionPropertyPageProvider;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchContinuation;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.SearchPropertyPageProvider;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: ISearch — C-3PO READING THE JAWA DIALECT MISSION BRIEF ────────────
// C-3PO sits down with the Jawas' trade request scroll and decodes every line:
// "Starting point: Mos Eisley market (search base)."
// "Range: ten-kilometre radius (scope)."
// "Matching condition: droids with red eyes (filter)."
// "Time budget: two sunsets max (time limit)."
// "Show me: name, price, condition (returning attributes)."
// This interface is that decoded mission brief: everything needed to describe
// AND execute a single LDAP search, plus a slot for the results that come back.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Holds all parameters and results for a single LDAP search.
 * A search is defined by its base DN, filter, scope, returning attributes,
 * count/time limits, and optional LDAP controls.  After execution it also
 * holds the results array and any search continuations (referrals that were
 * followed).  Named searches are persisted in the Searches view.
 * Think of this as C-3PO's decoded mission brief: everything you told the Jawas
 * about what you need, and the items they brought back.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ISearch extends Serializable, IAdaptable, SearchPropertyPageProvider, ConnectionPropertyPageProvider
{
    /** Empty search base (root of the DIT, DN = ""). */
    Dn EMPTY_SEARCH_BASE = new Dn(); //$NON-NLS-1$

    /** Empty returning-attributes array — tells the server to return no attributes. */
    String[] NO_ATTRIBUTES = new String[0];

    /** True LDAP filter — matches every entry: {@code (objectClass=*)}. */
    String FILTER_TRUE = LdapConstants.OBJECT_CLASS_STAR;

    /** False LDAP filter — matches nothing: {@code (!(objectClass=*))}. */
    String FILTER_FALSE = "(!(objectClass=*))"; //$NON-NLS-1$

    /** Filter that matches only subentry objects. */
    String FILTER_SUBENTRY = "(|(objectClass=subentry)(objectClass=ldapSubentry))"; //$NON-NLS-1$

    /** Filter that matches only alias objects. */
    String FILTER_ALIAS = "(objectClass=alias)"; //$NON-NLS-1$

    /** Filter that matches only referral objects. */
    String FILTER_REFERRAL = "(objectClass=referral)"; //$NON-NLS-1$

    /** Filter that matches either alias or referral objects. */
    String FILTER_ALIAS_OR_REFERRAL = "(|(objectClass=alias)(objectClass=referral))"; //$NON-NLS-1$


    // ── C-3PO Packages The Mission Brief As An LDAP URL ──────────────────────────
    // The entire search definition can be encoded as a single LDAP URL:
    // {@code ldap://host/base?attrs?scope?filter}
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP URL that represents this search, encoding the connection
     * host/port, base DN, returning attributes, scope, and filter.
     *
     * @return the {@link LdapUrl} for this search; never {@code null}.
     */
    LdapUrl getUrl();


    // ── C-3PO Checks Whether To Ask About Sub-Sections ───────────────────────────
    // When displaying results in a tree, we sometimes need to know whether each
    // result entry has children — this flag enables that extra check.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the search job should initialise the
     * {@code hasChildren} flag on each returned entry.
     * When {@code true}, the job issues an extra one-level search per result
     * to determine whether child entries exist.
     *
     * @return {@code true} if {@code hasChildren} should be initialised.
     */
    boolean isInitHasChildrenFlag();


    // ── C-3PO Lists The LDAP Controls Attached To The Request ────────────────────
    // LDAP controls are extension packets attached to a request — "use Paged
    // Results", "sort by this attribute."  C-3PO files them with the mission.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP controls to include in the search request.
     * Controls are server extensions such as Paged Results or Sort Control.
     *
     * @return a mutable list of {@link Control}s; may be empty, never {@code null}.
     */
    List<Control> getControls();


    // ── C-3PO Notes The Controls The Server Sent Back ────────────────────────────
    // The server's response also carries controls — "page token for next page."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the controls the server returned in its search response.
     * For paged search these include the cookie for the next page.
     *
     * @return the response controls; may be empty, never {@code null}.
     */
    List<Control> getResponseControls();


    // ── C-3PO Reads The Cargo Limit ───────────────────────────────────────────────
    // "No more than 100 droids — we can't afford more."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum number of entries to return, or {@code 0} for no limit.
     *
     * @return the count limit; 0 means unlimited.
     */
    int getCountLimit();


    // ── C-3PO Sets The Cargo Limit ────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the count limit for this search.
     *
     * @param countLimit the max entries to return; 0 means no limit.
     */
    void setCountLimit( int countLimit );


    // ── C-3PO Reads The Matching Condition ────────────────────────────────────────
    // "Only droids with red photoreceptors" — the LDAP filter string.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP filter string for this search.
     * A {@code null} or empty filter is treated as {@code (objectClass=*)}.
     *
     * @return the filter string; never {@code null}.
     */
    String getFilter();


    // ── C-3PO Updates The Matching Condition ─────────────────────────────────────
    // "Actually, find droids with both red photoreceptors AND copper plating."
    // Changing the filter fires a {@link org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the LDAP filter for this search.
     * A {@code null} or empty string is normalised to {@code (objectClass=*)}.
     * Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
     *
     * @param filter the LDAP filter string; {@code null} resets to match-all.
     */
    void setFilter( String filter );


    // ── C-3PO Lists Which Data Fields To Bring Back ───────────────────────────────
    // "Bring me the model number and price only — not the full spec sheet."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute names to request from the server.
     * An empty array means "no attributes" ({@link #NO_ATTRIBUTES});
     * {@code null} is stored as {@code null} but treated as "all user attributes."
     *
     * @return the returning attributes array; may be empty, may be {@code null}.
     */
    String[] getReturningAttributes();


    // ── C-3PO Specifies Which Fields To Include In The Report ────────────────────
    // Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets which attributes to return.
     * {@code null} is stored as "all user attributes"; an empty array requests
     * no attributes.
     *
     * @param returningAttributes the attribute names; {@code null} means all.
     */
    void setReturningAttributes( String[] returningAttributes );


    // ── C-3PO Sets The Search Radius ──────────────────────────────────────────────
    // "Scan just Mos Eisley (BASE), the whole district (ONE), or the whole planet (SUB)."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search scope — BASE, ONE, or SUB.
     * <ul>
     *   <li>BASE — only the base entry itself.</li>
     *   <li>ONE — direct children of the base.</li>
     *   <li>SUB — all descendants of the base.</li>
     * </ul>
     *
     * @return the {@link SearchScope}.
     */
    SearchScope getScope();


    // ── C-3PO Adjusts The Search Radius ──────────────────────────────────────────
    // Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the search scope.
     *
     * @param scope the new {@link SearchScope}; must not be {@code null}.
     */
    void setScope( SearchScope scope );


    // ── C-3PO Reads The Alias Policy ─────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the alias dereferencing policy for this search.
     *
     * @return the {@link Connection.AliasDereferencingMethod}.
     */
    Connection.AliasDereferencingMethod getAliasesDereferencingMethod();


    // ── C-3PO Sets The Alias Policy ───────────────────────────────────────────────
    // Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the alias dereferencing method for this search.
     *
     * @param aliasesDereferencingMethod the new method; must not be {@code null}.
     */
    void setAliasesDereferencingMethod( Connection.AliasDereferencingMethod aliasesDereferencingMethod );


    // ── C-3PO Reads The Referral-Follow Policy ────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the referral handling policy for this search.
     *
     * @return the {@link Connection.ReferralHandlingMethod}.
     */
    Connection.ReferralHandlingMethod getReferralsHandlingMethod();


    // ── C-3PO Sets The Referral-Follow Policy ─────────────────────────────────────
    // Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the referral handling method for this search.
     *
     * @param referralsHandlingMethod the new method; must not be {@code null}.
     */
    void setReferralsHandlingMethod( Connection.ReferralHandlingMethod referralsHandlingMethod );


    // ── C-3PO Identifies The Starting Point Of The Search ────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the base DN — the root of the directory subtree to search.
     * Use {@link #EMPTY_SEARCH_BASE} to search from the directory root.
     *
     * @return the search base {@link Dn}; never {@code null}.
     */
    Dn getSearchBase();


    // ── C-3PO Updates The Starting Point ─────────────────────────────────────────
    // A {@code null} base is normalised to an empty (root) DN.
    // Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the search base DN.
     * {@code null} is normalised to {@link #EMPTY_SEARCH_BASE}.
     *
     * @param searchBase the new base DN.
     */
    void setSearchBase( Dn searchBase );


    // ── C-3PO Reads The Time Budget ──────────────────────────────────────────────
    // "If the Jawas haven't replied in two sunsets, cancel the deal."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the maximum time in seconds to wait for the search to complete.
     * {@code 0} means no limit.
     *
     * @return the time limit in seconds.
     */
    int getTimeLimit();


    // ── C-3PO Sets The Time Budget ───────────────────────────────────────────────
    // Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the time limit for this search.
     *
     * @param timeLimit the maximum wait in seconds; 0 means no limit.
     */
    void setTimeLimit( int timeLimit );


    // ── C-3PO Reads The Mission Code Name ────────────────────────────────────────
    // "Mission 'Find Luke' — we can reference this search by name in the UI."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable name of this saved search.
     * Displayed in the Searches view and used for identification.
     *
     * @return the search name; may be {@code null} for unnamed/transient searches.
     */
    String getName();


    // ── C-3PO Renames The Mission ─────────────────────────────────────────────────
    // Firing a {@code SEARCH_RENAMED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the name of this saved search.
     *
     * @param name the new display name.
     */
    void setName( String name );


    // ── C-3PO Reads The Trade Goods Delivered By The Jawas ───────────────────────
    // After the search completes the results (the droids the Jawas brought out)
    // are stored here.  {@code null} means the search hasn't been run yet.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search results from the most recent execution.
     * {@code null} if the search has not been performed yet.
     * An empty array means the search ran but returned no entries.
     *
     * @return the {@link ISearchResult} array, or {@code null} if not yet run.
     */
    ISearchResult[] getSearchResults();


    // ── C-3PO Stores The Returned Goods In The Inventory ─────────────────────────
    // Firing a {@code SEARCH_PERFORMED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the search results.
     * Called by the search job after the LDAP search completes.
     *
     * @param searchResults the results; may be {@code null} to clear.
     */
    void setSearchResults( ISearchResult[] searchResults );


    // ── C-3PO Notes Whether The Jawa Market Hit Its Capacity ─────────────────────
    // "They had more droids than our budget — we only got the first 100."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the server returned fewer entries than exist because
     * the count limit was hit.
     *
     * @return {@code true} if count limit was exceeded.
     */
    boolean isCountLimitExceeded();


    // ── C-3PO Flags The Capacity-Exceeded Notice ──────────────────────────────────
    // Firing a {@code SEARCH_PERFORMED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the count-limit-exceeded flag.
     *
     * @param countLimitExceeded {@code true} if the count limit was hit.
     */
    void setCountLimitExceeded( boolean countLimitExceeded );


    // ── C-3PO Identifies Which Jawa Market This Search Runs Against ───────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection this search is associated with.
     *
     * @return the {@link IBrowserConnection}; never {@code null}.
     */
    IBrowserConnection getBrowserConnection();


    // ── C-3PO Reassigns The Search To A Different Market ──────────────────────────
    // Firing a {@code SEARCH_PARAMETER_UPDATED} event follows automatically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the browser connection for this search.
     *
     * @param browserConnection the new connection; must not be {@code null}.
     */
    void setBrowserConnection( IBrowserConnection browserConnection );


    // ── C-3PO Makes A Copy Of The Mission Brief ───────────────────────────────────
    // "Duplicate the mission file — we'll run it against a different market."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a deep copy of this search.
     * The clone has the same parameters but no results.
     *
     * @return a new {@link ISearch} with identical parameters.
     */
    ISearch clone();


    // ── C-3PO Retrieves The Condensed Mission Brief Object ────────────────────────
    // {@link SearchParameter} is a compact data-bean holding the same fields as
    // this interface — used for serialisation and for passing to LDAP API calls.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link SearchParameter} encapsulating this search's
     * base DN, filter, scope, returning attributes, limits, and controls.
     * Used for serialisation and for invoking the underlying LDAP API.
     *
     * @return the search parameter; never {@code null}.
     */
    SearchParameter getSearchParameter();


    // ── C-3PO Installs A New Condensed Mission Brief ─────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the {@link SearchParameter} for this search.
     *
     * @param searchParameter the new parameter; must not be {@code null}.
     */
    void setSearchParameter( SearchParameter searchParameter );


    // ── C-3PO Checks Whether Results Accumulate In Scroll Mode ───────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if paged results should be accumulated continuously
     * rather than replaced on each page fetch.
     *
     * @return {@code true} if scroll mode is enabled.
     */
    boolean isPagedSearchScrollMode();


    // ── C-3PO Toggles Scroll Mode ────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets whether paged-search scroll mode is active.
     *
     * @param isPagedSearchScrollMode {@code true} for scroll mode.
     */
    void setPagedSearchScrollMode( boolean isPagedSearchScrollMode );


    // ── C-3PO Retrieves The "Fetch Next Page" Runnable ───────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the runnable that fetches the next page of results, or {@code null}
     * if no more pages are available.
     *
     * @return the next-page runnable, or {@code null}.
     */
    StudioConnectionBulkRunnableWithProgress getNextSearchRunnable();


    // ── C-3PO Stores The "Fetch Next Page" Runnable ──────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the runnable for fetching the next page of paged search results.
     *
     * @param nextSearchRunnable the next-page runnable; {@code null} clears it.
     */
    void setNextPageSearchRunnable( StudioConnectionBulkRunnableWithProgress nextSearchRunnable );


    // ── C-3PO Retrieves The "Fetch First Page" Runnable ──────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the runnable that fetches the first page of results (top of paged
     * search), or {@code null} if paging is not active.
     *
     * @return the top-page runnable, or {@code null}.
     */
    StudioConnectionBulkRunnableWithProgress getTopSearchRunnable();


    // ── C-3PO Stores The "Fetch First Page" Runnable ─────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the runnable for re-fetching from the first page.
     *
     * @param nextSearchRunnable the top-page runnable; {@code null} clears it.
     */
    void setTopPageSearchRunnable( StudioConnectionBulkRunnableWithProgress nextSearchRunnable );


    // ── C-3PO Retrieves Referral Threads Followed During The Search ───────────────
    // If the server returned referrals during the search and the handler followed
    // them, the extra results are stored as continuations alongside the main results.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search continuations — referral results followed during this search.
     * Each {@link SearchContinuation} records the referral URL and the entries
     * returned from it.
     *
     * @return the continuations array; may be empty, never {@code null}.
     */
    SearchContinuation[] getSearchContinuations();


    // ── C-3PO Stores The Followed-Referral Threads ───────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the search continuations recorded during execution.
     *
     * @param searchContinuations the continuations; may be empty.
     */
    void setSearchContinuations( SearchContinuation[] searchContinuations );
}
