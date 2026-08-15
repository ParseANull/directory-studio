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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.ContinuedSearchResultEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchContinuation;


// ── CLASS: InitializeChildrenRunnable — IMPERIAL SCOUTS MAP HOTH'S TUNNELS ───
// Vader's scouts land on Hoth and fan out through the ice caves, mapping every
// tunnel that branches off the main corridor.  They check sub-passages
// (subentries), side tunnels flagged as aliases or referrals, and note whether
// there are "more tunnels" than their scanner can show at once (count limit).
// If the cave system is huge they use a paging protocol: complete one page, mark
// where they stopped, and create a "next page" probe for when the user scrolls.
// This runnable expands the children of one or more LDAP entries.  It runs an
// ONELEVEL search (and optional sub-searches for subentries, aliases, referrals),
// populates the browser model's child list, handles paged-search controls, and
// fires a {@link ChildrenInitializedEvent} per parent when done.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that loads the child entries of one or more parent
 * entries in the LDAP browser tree.
 * When the user clicks the expand arrow on a tree node, this job runs to
 * discover what sits directly beneath that entry (ONELEVEL scope).  It also
 * optionally searches for subentries ({@code (objectClass=subentry)} filter with
 * Subentries control) and alias/referral children when those flags are set.
 * For large directories it transparently handles paged search: in "transparent"
 * mode it keeps fetching pages until done; in "scroll" mode it creates
 * "next page" / "top page" runnable handles so the user can page through results.
 * Think of it as Imperial scouts mapping an ice-cave network level by level.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitializeChildrenRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The entries. */
    private IEntry[] entries;

    /** The purge all caches flag. */
    private boolean purgeAllCaches;

    /** The paged search control, only used internally. */
    private PagedResults pagedSearchControl;


    // ── Vader Sends Out His Full Scout Platoon ────────────────────────────────
    // Public constructor for the normal "expand node(s)" use case.  Pass
    // purgeAllCaches=true when you want a full refresh (e.g. after a delete);
    // false to do a gentler cache invalidation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new InitializeChildrenRunnable for one or more parent entries.
     *
     * <p>For example:</p>
     * <pre>
     *   new StudioBrowserJob(new InitializeChildrenRunnable(true, ouEntry)).execute();
     * </pre>
     *
     * @param purgeAllCaches {@code true} to also clear attribute data (deep refresh);
     *                       {@code false} for a lighter cache clear.
     * @param entries        the parent entries whose children to load.
     */
    public InitializeChildrenRunnable( boolean purgeAllCaches, IEntry... entries )
    {
        this.entries = entries;
        this.purgeAllCaches = purgeAllCaches;
    }


    // ── Vader Sends The Next-Page Probe Into The Caves ────────────────────────
    // Private constructor for the paged-search continuation case.  Used
    // internally to create "next page" / "top page" runnables that the
    // tree stores on the entry for user-driven paging.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new InitializeChildrenRunnable that continues a paged search
     * from the given {@link PagedResults} control (carrying the cookie from
     * the last response).
     *
     * @param entry              the parent entry whose next page to load.
     * @param pagedSearchControl the paged-results control with the continuation cookie.
     */
    private InitializeChildrenRunnable( IEntry entry, PagedResults pagedSearchControl )
    {
        this.entries = new IEntry[]
            { entry };
        this.pagedSearchControl = pagedSearchControl;
    }


    // ── One Scout Per Parent Entry ────────────────────────────────────────────
    // Each parent entry may belong to a different server; return one connection
    // per entry so the job framework acquires the right connections.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns one {@link Connection} per parent entry.
     *
     * @return array of connections.
     */
    public Connection[] getConnections()
    {
        Connection[] connections = new Connection[entries.length];
        for ( int i = 0; i < connections.length; i++ )
        {
            connections[i] = entries[i].getBrowserConnection().getConnection();
        }
        return connections;
    }


    // ── The Mission Title For The Progress Bar ────────────────────────────────
    // "Initialising sub-entries..." in the Eclipse progress view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "initialise sub-entries" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__init_entries_title_subonly;
    }


    // ── Lock All Parent Entries During The Mapping ────────────────────────────
    // Prevents two expand operations on the same node running concurrently.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all parent entries as the set of locked objects.
     *
     * @return the entries array.
     */
    public Object[] getLockedObjects()
    {
        List<Object> l = new ArrayList<Object>();
        l.addAll( Arrays.asList( entries ) );
        return l.toArray();
    }


    // ── If The Cave-Mapping Fails ──────────────────────────────────────────────
    // Singular vs plural error message depending on how many entries failed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown when child initialisation fails.
     *
     * @return a localised error string.
     */
    public String getErrorMessage()
    {
        return entries.length == 1 ? BrowserCoreMessages.jobs__init_entries_error_1
            : BrowserCoreMessages.jobs__init_entries_error_n;
    }


    // ── The Scouts Fan Out Through The Cave Network ────────────────────────────
    // Root DSE is a special case (full server reload via loadRootDSE).
    // For other entries: initialise the pagedSearchControl if the connection
    // has paged search enabled, then call initializeChildren per entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Main execution method.  Loops over parent entries and calls
     * {@link #initializeChildren} for each.  The Root DSE is handled as a
     * special case by delegating to
     * {@link InitializeRootDSERunnable#loadRootDSE}.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( " ", entries.length + 2 ); //$NON-NLS-1$
        monitor.reportProgress( " " ); //$NON-NLS-1$

        for ( IEntry entry : entries )
        {
            monitor.setTaskName( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__init_entries_task, new String[]
                { entry.getDn().getName() } ) );
            monitor.worked( 1 );

            IBrowserConnection browserConnection = entry.getBrowserConnection();
            if ( browserConnection != null )
            {
                if ( entry instanceof IRootDSE )
                {
                    // special handling for Root DSE
                    InitializeRootDSERunnable.loadRootDSE( browserConnection, monitor );
                    continue;
                }

                if ( pagedSearchControl == null && browserConnection.isPagedSearch() )
                {
                    pagedSearchControl = Controls.newPagedResultsControl( browserConnection.getPagedSearchSize() );
                }

                initializeChildren( entry, monitor, pagedSearchControl );
            }
        }
    }


    // ── The Scouts Report All Discovered Tunnels ───────────────────────────────
    // Fires a ChildrenInitializedEvent per entry so the tree node updates.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires one {@link ChildrenInitializedEvent} per successfully initialized
     * parent entry.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        for ( IEntry entry : entries )
        {
            if ( entry.getBrowserConnection() != null && entry.isChildrenInitialized() )
            {
                EventRegistry.fireEntryUpdated( new ChildrenInitializedEvent( entry ), this );
            }
        }
    }


    // ── One Scout Maps A Single Corridor ──────────────────────────────────────
    // 1. Clear old children (and optionally old attribute caches).
    // 2. Run the main ONELEVEL search (with optional paged search control).
    // 3. Add all regular children; handle search continuations (referrals).
    // 4. If paged and more pages exist: scroll mode → create next/top runnables;
    //    transparent mode → keep fetching until no more cookies.
    // 5. Run sub-search for subentries if configured.
    // 6. Run sub-search for aliases/referrals if requested on the parent.
    // 7. Set hasMoreChildren from countLimitExceeded.
    // 8. Mark childrenInitialized = true.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Core logic for loading children of a single parent entry.  Runs the main
     * ONELEVEL search, handles paged results, executes optional sub-searches for
     * subentries / aliases / referrals, and sets the parent's
     * {@code childrenInitialized} flag to {@code true}.
     *
     * @param parent             the parent entry to expand.
     * @param monitor            the progress monitor.
     * @param pagedSearchControl the paged-results request control (may be
     *                           {@code null} when paging is not in use).
     */
    private void initializeChildren( IEntry parent, StudioProgressMonitor monitor, PagedResults pagedSearchControl )
    {
        monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__init_entries_progress_sub,
            new String[]
            { parent.getDn().getName() } ) );

        // clear old children
        clearCaches( parent, purgeAllCaches );

        // create search
        ISearch search = createSearch( parent, pagedSearchControl, false, false, false );

        // search
        executeSearch( parent, search, monitor );
        ISearchResult[] srs = search.getSearchResults();
        SearchContinuation[] scs = search.getSearchContinuations();

        // fill children in search result
        if ( ( srs != null && srs.length > 0 ) || ( scs != null && scs.length > 0 ) )
        {
            // clearing old children before filling new children is
            // necessary to handle aliases and referrals.
            clearCaches( parent, false );

            do
            {
                if ( srs != null )
                {
                    for ( ISearchResult searchResult : srs )
                    {
                        parent.addChild( searchResult.getEntry() );
                    }
                    srs = null;
                }

                if ( scs != null )
                {
                    for ( SearchContinuation searchContinuation : scs )
                    {
                        ContinuedSearchResultEntry entry = new ContinuedSearchResultEntry( parent
                            .getBrowserConnection(), searchContinuation.getUrl().getDn() );
                        entry.setUnresolved( searchContinuation.getUrl() );
                        parent.addChild( entry );
                    }
                    scs = null;
                }

                PagedResults prRequestControl = null;
                PagedResults prResponseControl = null;
                for ( Control responseControl : search.getResponseControls() )
                {
                    if ( responseControl instanceof PagedResults )
                    {
                        prResponseControl = ( PagedResults ) responseControl;
                    }
                }
                for ( Control requestControl : search.getControls() )
                {
                    if ( requestControl instanceof PagedResults )
                    {
                        prRequestControl = ( PagedResults ) requestControl;
                    }
                }

                if ( prRequestControl != null && prResponseControl != null )
                {
                    if ( search.isPagedSearchScrollMode() )
                    {
                        if ( ArrayUtils.isNotEmpty( prRequestControl.getCookie() ) )
                        {
                            // create top page search runnable, same as original search
                            InitializeChildrenRunnable topPageChildrenRunnable = new InitializeChildrenRunnable(
                                parent, null );
                            parent.setTopPageChildrenRunnable( topPageChildrenRunnable );
                        }

                        if ( ArrayUtils.isNotEmpty( prResponseControl.getCookie() ) )
                        {
                            PagedResults newPrc = Controls.newPagedResultsControl( prRequestControl.getSize(),
                                prResponseControl.getCookie() );
                            InitializeChildrenRunnable nextPageChildrenRunnable = new InitializeChildrenRunnable(
                                parent, newPrc );
                            parent.setNextPageChildrenRunnable( nextPageChildrenRunnable );
                        }
                    }
                    else
                    {
                        // transparently continue search, till count limit is reached
                        if ( ArrayUtils.isNotEmpty( prResponseControl.getCookie() )
                            && ( search.getCountLimit() == 0 || search.getSearchResults().length < search
                                .getCountLimit() ) )
                        {

                            search.setSearchResults( new ISearchResult[0] );
                            search.getResponseControls().clear();
                            prRequestControl.setCookie( prResponseControl.getCookie() );

                            executeSearch( parent, search, monitor );
                            srs = search.getSearchResults();
                            scs = search.getSearchContinuations();
                        }
                    }
                }
            }
            while ( srs != null && srs.length > 0 );
        }
        else
        {
            parent.setHasChildrenHint( false );
        }

        // get sub-entries
        ISearch subSearch = createSearch( parent, null, true, false, false );
        if ( parent.getBrowserConnection().isFetchSubentries() || parent.isFetchSubentries() )
        {
            executeSubSearch( parent, subSearch, monitor );
        }

        // get aliases and referrals
        ISearch aliasOrReferralSearch = createSearch( parent, null, false, parent.isFetchAliases(), parent
            .isFetchReferrals() );
        if ( parent.isFetchAliases() || parent.isFetchReferrals() )
        {
            executeSubSearch( parent, aliasOrReferralSearch, monitor );
        }

        // check exceeded limits / canceled
        parent.setHasMoreChildren( search.isCountLimitExceeded() || subSearch.isCountLimitExceeded()
            || aliasOrReferralSearch.isCountLimitExceeded() || monitor.isCanceled() );

        // set initialized state
        parent.setChildrenInitialized( true );
    }


    // ── Scouts Check The Side Passages ─────────────────────────────────────────
    // Runs a sub-search (for subentries, aliases, or referrals) and adds results
    // to the parent's children list, including any search continuation entries
    // (unresolved referral URLs).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes a supplementary sub-search (subentries / aliases / referrals)
     * and adds the results to the parent's child list.
     *
     * @param parent    the parent entry.
     * @param subSearch the sub-search to execute.
     * @param monitor   the progress monitor.
     */
    private void executeSubSearch( IEntry parent, ISearch subSearch, StudioProgressMonitor monitor )
    {
        executeSearch( parent, subSearch, monitor );
        ISearchResult[] subSrs = subSearch.getSearchResults();
        SearchContinuation[] subScs = subSearch.getSearchContinuations();

        // fill children in search result
        if ( subSrs != null && subSrs.length > 0 )
        {
            for ( ISearchResult searchResult : subSrs )
            {
                parent.addChild( searchResult.getEntry() );
            }
            for ( SearchContinuation searchContinuation : subScs )
            {
                ContinuedSearchResultEntry entry = new ContinuedSearchResultEntry( parent.getBrowserConnection(),
                    searchContinuation.getUrl().getDn() );
                entry.setUnresolved( searchContinuation.getUrl() );
                parent.addChild( entry );
            }
        }
    }


    // ── Scouts Send Their Findings Back To Vader ───────────────────────────────
    // Executes the search and reports a progress line "Found N entries under DN".
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs {@link SearchRunnable#searchAndUpdateModel} and reports the number
     * of results found to the progress monitor.
     *
     * @param parent  the parent entry (for progress labelling).
     * @param search  the search to execute.
     * @param monitor the progress monitor.
     */
    private static void executeSearch( IEntry parent, ISearch search, StudioProgressMonitor monitor )
    {
        SearchRunnable.searchAndUpdateModel( parent.getBrowserConnection(), search, monitor );
        ISearchResult[] srs = search.getSearchResults();
        monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__init_entries_progress_subcount,
            new String[]
            { srs == null ? Integer.toString( 0 ) : Integer.toString( srs.length ), parent.getDn().getName() } ) );
    }


    // ── Scouts Configure Their Search Instruments ──────────────────────────────
    // Builds the ISearch object for the ONELEVEL search.  Switches filter for
    // subentries (FILTER_SUBENTRY), aliases, or referrals.  Adjusts alias
    // de-referencing to NEVER for alias sub-searches.  Adds ManageDsaIT control
    // when needed.  Optionally adds Subentries and PagedResults controls.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Builds an {@link ISearch} configured for ONELEVEL search from the parent
     * entry.  Applies the correct filter and controls for the requested search
     * type (normal children, subentries, alias children, or referral children).
     *
     * @param parent              the parent entry.
     * @param pagedSearchControl  the paged-results control to attach, or
     *                            {@code null} for no paging.
     * @param isSubentriesSearch  {@code true} to use the subentries filter.
     * @param isAliasSearch       {@code true} to filter for alias children.
     * @param isReferralsSearch   {@code true} to filter for referral children.
     * @return a fully configured {@link ISearch}.
     */
    private static ISearch createSearch( IEntry parent, PagedResults pagedSearchControl, boolean isSubentriesSearch,
        boolean isAliasSearch, boolean isReferralsSearch )
    {
        // scope
        SearchScope scope = SearchScope.ONELEVEL;

        // filter
        String filter = parent.getChildrenFilter();
        if ( isSubentriesSearch )
        {
            filter = ISearch.FILTER_SUBENTRY;
        }
        else if ( isAliasSearch && isReferralsSearch )
        {
            filter = ISearch.FILTER_ALIAS_OR_REFERRAL;
        }
        else if ( isAliasSearch )
        {
            filter = ISearch.FILTER_ALIAS;
        }
        else if ( isReferralsSearch )
        {
            filter = ISearch.FILTER_REFERRAL;
        }

        // alias handling
        AliasDereferencingMethod aliasesDereferencingMethod = parent.getBrowserConnection()
            .getAliasesDereferencingMethod();
        if ( parent.isAlias() || isAliasSearch )
        {
            aliasesDereferencingMethod = AliasDereferencingMethod.NEVER;
        }

        // referral handling
        ReferralHandlingMethod referralsHandlingMethod = parent.getBrowserConnection().getReferralsHandlingMethod();

        // create search
        ISearch search = new Search( null, parent.getBrowserConnection(), parent.getDn(), filter,
            ISearch.NO_ATTRIBUTES, scope, parent.getBrowserConnection().getCountLimit(),
            parent.getBrowserConnection().getTimeLimit(),
            aliasesDereferencingMethod, referralsHandlingMethod,
            BrowserCorePlugin.getDefault()
                .getPluginPreferences().getBoolean( BrowserCoreConstants.PREFERENCE_CHECK_FOR_CHILDREN ),
            null, parent.getBrowserConnection().isPagedSearchScrollMode() );

        // controls
        if ( parent.isReferral() || isReferralsSearch || parent.getBrowserConnection().isManageDsaIT() )
        {
            search.getSearchParameter().getControls().add( Controls.MANAGEDSAIT_CONTROL );
        }
        if ( isSubentriesSearch )
        {
            search.getSearchParameter().getControls().add( Controls.SUBENTRIES_CONTROL );
        }
        if ( pagedSearchControl != null )
        {
            search.getSearchParameter().getControls().add( pagedSearchControl );
        }

        return search;
    }


    // ── Vader Orders The Tunnels Sealed And Remapped ───────────────────────────
    // Removes all child entries, resets initialized flags, and clears paging
    // runnables.  When purgeAllCaches=true also resets attribute-loaded flags.
    // Static and package-private so other runnables can call it too.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the cached children (and optionally cached attributes) of the
     * given entry, recursively.  Also resets the next/top paged-search runnables.
     * Called before re-loading children so stale data doesn't linger.
     *
     * @param entry          the entry whose caches to clear.
     * @param purgeAllCaches if {@code true}, also clear attribute flags
     *                       ({@code setAttributesInitialized(false)}).
     */
    static void clearCaches( IEntry entry, boolean purgeAllCaches )
    {
        // clear the parent-child relationship, recursively
        IEntry[] children = entry.getChildren();
        if ( children != null )
        {
            for ( IEntry child : children )
            {
                if ( child != null )
                {
                    entry.deleteChild( child );
                    clearCaches( child, purgeAllCaches );
                }
            }
        }
        entry.setChildrenInitialized( false );

        // reset paging runnables
        entry.setTopPageChildrenRunnable( null );
        entry.setNextPageChildrenRunnable( null );

        // reset attributes and additional flags
        if ( purgeAllCaches )
        {
            entry.setAttributesInitialized( false );
            entry.setHasChildrenHint( true );
            entry.setHasMoreChildren( false );
        }
    }
}
