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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.StudioControl;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResult;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.model.impl.BaseDNEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.ContinuedSearchResultEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Entry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.ldapbrowser.core.utils.JNDIUtils;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;


// ── CLASS: SearchRunnable — CLONE TROOPERS SCOUTING THE GALAXY ───────────────
// Clone troopers fan out across the galaxy (LDAP directory) under Order 66.
// Each ISearch is a scout mission: send the search request, receive results,
// populate the browser entry cache, and fire a SearchUpdateEvent when done.
// Paged-results pagination is handled transparently or via scroll-mode runnables.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable to perform LDAP search operations and update the browser model.
 *
 * <p>Think of this as clone troopers executing Order 66 across the galaxy:
 * each search is a scout mission — entries are fetched, cached, and their
 * flags (alias, referral, hasChildren) initialised.  Pagination is handled
 * transparently; events are fired when the mission completes.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The searches. */
    protected ISearch[] searches;

    /** The searches to perform. */
    protected ISearch[] searchesToPerform;


    // ── Clone Troopers Receive A Batch Of Search Missions ────────────────────────
    // Stores the full array of ISearch objects that this runnable will execute.
    // Both searches and searchesToPerform initially point to the same array.
    /**
     * Creates a new instance of SearchRunnable for multiple searches.
     *
     * @param searches the searches to perform
     */
    public SearchRunnable( ISearch[] searches )
    {
        this.searches = searches;
        this.searchesToPerform = searches;
    }


    // ── Clone Troopers Receive A Paged-Continuation Mission ───────────────────────
    // Private constructor used when creating next-page / top-page runnables.
    // The outer search accumulates results; the inner searchToPerform holds
    // the cloned search with the updated paged-results cookie.
    /**
     * Creates a new instance of SearchRunnable for a single paged continuation.
     *
     * @param search the outer search that accumulates results
     * @param searchToPerform the cloned search carrying the next-page cookie
     */
    private SearchRunnable( ISearch search, ISearch searchToPerform )
    {
        this.searches = new ISearch[]
            { search };
        this.searchesToPerform = new ISearch[]
            { searchToPerform };
    }


    // ── Clone Troopers Report Their LDAP Connections ──────────────────────────────
    // One connection per search object; used by the job scheduler.
    /**
     * {@inheritDoc}
     */
    public Connection[] getConnections()
    {
        Connection[] connections = new Connection[searches.length];
        for ( int i = 0; i < connections.length; i++ )
        {
            connections[i] = searches[i].getBrowserConnection().getConnection();
        }
        return connections;
    }


    // ── Clone Troopers Report Their Mission Name ──────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__search_name;
    }


    // ── Clone Troopers Lock Their Search Objects Against Concurrent Missions ─────
    // Lando runs Cloud City: each ISearch object is used as its own lock token.
    /**
     * {@inheritDoc}
     */
    public Object[] getLockedObjects()
    {
        List<Object> l = new ArrayList<Object>();
        l.addAll( Arrays.asList( searches ) );
        return l.toArray();
    }


    // ── Clone Troopers Return The Appropriate Error Message ───────────────────────
    // Han shoots first: singular vs. plural message depending on search count.
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return searches.length == 1 ? BrowserCoreMessages.jobs__search_error_1
            : BrowserCoreMessages.jobs__search_error_n;
    }


    // ── Clone Troopers Execute All Search Missions In Sequence ────────────────────
    // Iterates all searches, calls searchAndUpdateModel() for each, handles
    // paged-results response controls, and sets up next-page / top-page runnables
    // for scroll-mode pagination.  Transparent pagination loops until done.
    /**
     * {@inheritDoc}
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( " ", searches.length + 1 ); //$NON-NLS-1$
        monitor.reportProgress( " " ); //$NON-NLS-1$

        for ( int pi = 0; pi < searches.length; pi++ )
        {
            ISearch search = searches[pi];
            ISearch searchToPerform = searchesToPerform[pi];

            monitor.setTaskName( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__search_task, new String[]
                { search.getName() } ) );
            monitor.worked( 1 );

            if ( search.getBrowserConnection() != null )
            {
                // reset search results
                search.setSearchResults( new ISearchResult[0] );
                search.getResponseControls().clear();
                search.setNextPageSearchRunnable( null );
                search.setTopPageSearchRunnable( null );
                searchToPerform.setSearchResults( new ISearchResult[0] );
                searchToPerform.setNextPageSearchRunnable( null );
                searchToPerform.setTopPageSearchRunnable( null );
                searchToPerform.getResponseControls().clear();

                do
                {
                    // perform search
                    searchAndUpdateModel( searchToPerform.getBrowserConnection(), searchToPerform, monitor );

                    if ( search != searchToPerform )
                    {
                        // merge search results
                        ISearchResult[] sr1 = search.getSearchResults();
                        ISearchResult[] sr2 = searchToPerform.getSearchResults();
                        ISearchResult[] sr = new ISearchResult[sr1.length + sr2.length];
                        System.arraycopy( sr1, 0, sr, 0, sr1.length );
                        System.arraycopy( sr2, 0, sr, sr1.length, sr2.length );
                        search.setSearchResults( sr );
                    }
                    else
                    {
                        // set search results
                        search.setSearchResults( searchToPerform.getSearchResults() );
                    }

                    // check response controls
                    ISearch clonedSearch = ( ISearch ) searchToPerform.clone();
                    clonedSearch.getResponseControls().clear();
                    PagedResults prResponseControl = null;
                    PagedResults prRequestControl = null;
                    for ( org.apache.directory.api.ldap.model.message.Control responseControl : searchToPerform
                        .getResponseControls() )
                    {
                        if ( responseControl instanceof PagedResults )
                        {
                            prResponseControl = ( PagedResults ) responseControl;
                        }
                    }
                    for ( Iterator<Control> it = clonedSearch.getControls().iterator(); it.hasNext(); )
                    {
                        Control requestControl = it.next();
                        if ( requestControl instanceof PagedResults )
                        {
                            prRequestControl = ( PagedResults ) requestControl;
                            it.remove();
                        }
                    }
                    searchToPerform = null;

                    // paged search
                    if ( prResponseControl != null && prRequestControl != null )
                    {
                        PagedResults nextPrc = Controls.newPagedResultsControl( prRequestControl.getSize(),
                            prResponseControl.getCookie() );
                        ISearch nextPageSearch = ( ISearch ) clonedSearch.clone();
                        nextPageSearch.getResponseControls().clear();
                        nextPageSearch.getControls().add( nextPrc );
                        if ( search.isPagedSearchScrollMode() )
                        {
                            if ( ArrayUtils.isNotEmpty( prRequestControl.getCookie() ) )
                            {
                                // create top page search runnable, same as original search
                                ISearch topPageSearch = ( ISearch ) search.clone();
                                topPageSearch.getResponseControls().clear();
                                SearchRunnable topPageSearchRunnable = new SearchRunnable( search, topPageSearch );
                                search.setTopPageSearchRunnable( topPageSearchRunnable );
                            }
                            if ( ArrayUtils.isNotEmpty( prResponseControl.getCookie() ) )
                            {
                                // create next page search runnable
                                SearchRunnable nextPageSearchRunnable = new SearchRunnable( search, nextPageSearch );
                                search.setNextPageSearchRunnable( nextPageSearchRunnable );
                            }
                        }
                        else
                        {
                            // transparently continue search, till count limit is reached
                            if ( ArrayUtils.isNotEmpty( prResponseControl.getCookie() )
                                && ( search.getCountLimit() == 0 || search.getSearchResults().length < search
                                    .getCountLimit() ) )
                            {
                                searchToPerform = nextPageSearch;
                            }
                        }
                    }
                }
                while ( searchToPerform != null );
            }
        }
    }


    // ── Clone Troopers Fire A SearchUpdateEvent After Each Mission Completes ─────
    // Obi-Wan senses a disturbance in the Force: each search fires a
    // SEARCH_PERFORMED event so the UI can refresh the results view.
    /**
     * {@inheritDoc}
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        for ( int pi = 0; pi < searches.length; pi++ )
        {
            EventRegistry.fireSearchUpdated( new SearchUpdateEvent( searches[pi],
                SearchUpdateEvent.EventDetail.SEARCH_PERFORMED ), this );
        }
    }


    // ── Clone Trooper Executes One Search And Populates The Browser Cache ────────
    // Calls search() to get the raw enumeration, iterates every result,
    // creates or retrieves the IEntry from cache, initialises flags, fills
    // attributes, builds search-result objects, and propagates response controls.
    /**
     * Searches the directory and updates the browser model.
     *
     * @param browserConnection the browser connection
     * @param search the search to execute and populate
     * @param monitor the progress monitor
     */
    public static void searchAndUpdateModel( IBrowserConnection browserConnection, ISearch search,
        StudioProgressMonitor monitor )
    {
        if ( browserConnection.getConnection() == null )
        {
            return;
        }

        try
        {
            if ( !monitor.isCanceled() )
            {
                // add returning attributes for children and alias detection
                SearchParameter searchParameter = getSearchParameter( search );
                ArrayList<ISearchResult> searchResultList = new ArrayList<ISearchResult>();
                ArrayList<SearchContinuation> searchContinuationList = new ArrayList<SearchContinuation>();

                StudioSearchResultEnumeration enumeration = null;
                // search
                try
                {
                    enumeration = search( browserConnection, searchParameter, monitor );

                    // iterate through the search result
                    while ( !monitor.isCanceled() && enumeration != null && enumeration.hasMore() )
                    {
                        StudioSearchResult sr = enumeration.next();
                        boolean isContinuedSearchResult = sr.isContinuedSearchResult();
                        LdapUrl searchContinuationUrl = sr.getSearchContinuationUrl();

                        if ( searchContinuationUrl == null )
                        {
                            Dn dn = sr.getDn();
                            IEntry entry = null;

                            Connection resultConnection = sr.getConnection();
                            IBrowserConnection resultBrowserConnection = BrowserCorePlugin.getDefault()
                                .getConnectionManager().getBrowserConnection( resultConnection );
                            if ( resultBrowserConnection == null )
                            {
                                resultBrowserConnection = browserConnection;
                            }

                            // get entry from cache or create it
                            entry = resultBrowserConnection.getEntryFromCache( dn );
                            if ( entry == null )
                            {
                                entry = createAndCacheEntry( resultBrowserConnection, dn, monitor );

                                // If the entry is still null, we return
                                // See https://issues.apache.org/jira/browse/DIRSTUDIO-865
                                if ( entry == null )
                                {
                                    return;
                                }
                            }

                            // initialize special flags
                            initFlags( entry, sr, searchParameter );

                            // fill the attributes
                            fillAttributes( entry, sr, search.getSearchParameter() );

                            if ( isContinuedSearchResult )
                            {
                                // the result is from a continued search
                                // we create a special entry that displays the URL of the entry
                                entry = new ContinuedSearchResultEntry( resultBrowserConnection, dn );
                            }

                            searchResultList
                                .add( new org.apache.directory.studio.ldapbrowser.core.model.impl.SearchResult( entry,
                                    search ) );
                        }
                        else
                        {
                            //entry = new ContinuedSearchResultEntry( resultBrowserConnection, dn );
                            SearchContinuation searchContinuation = new SearchContinuation( search,
                                searchContinuationUrl );
                            searchContinuationList.add( searchContinuation );
                        }

                        monitor
                            .reportProgress( searchResultList.size() == 1 ? BrowserCoreMessages.model__retrieved_1_entry
                                : BrowserCoreMessages.bind( BrowserCoreMessages.model__retrieved_n_entries,
                                    new String[]
                                    { Integer.toString( searchResultList.size() ) } ) );
                    }
                }
                catch ( Exception e )
                {
                    int ldapStatusCode = JNDIUtils.getLdapStatusCode( e );
                    if ( ldapStatusCode == 3 || ldapStatusCode == 4 || ldapStatusCode == 11 )
                    {
                        search.setCountLimitExceeded( true );
                    }
                    else
                    {
                        monitor.reportError( e );
                    }
                }

                // check for response controls
                try
                {
                    if ( enumeration != null )
                    {
                        for ( org.apache.directory.api.ldap.model.message.Control control : enumeration
                            .getResponseControls() )
                        {
                            search.getResponseControls().add( control );
                            if ( control instanceof PagedResults )
                            {
                                search.setCountLimitExceeded(
                                    ArrayUtils.isNotEmpty( ( ( PagedResults ) control ).getCookie() ) );
                            }
                        }
                    }
                }
                catch ( Exception e )
                {
                    monitor.reportError( e );
                }

                monitor.reportProgress( searchResultList.size() == 1 ? BrowserCoreMessages.model__retrieved_1_entry
                    : BrowserCoreMessages.bind( BrowserCoreMessages.model__retrieved_n_entries, new String[]
                    { Integer.toString( searchResultList.size() ) } ) );
                monitor.worked( 1 );

                search.setSearchResults( ( ISearchResult[] ) searchResultList
                    .toArray( new ISearchResult[searchResultList.size()] ) );
                search.setSearchContinuations( ( SearchContinuation[] ) searchContinuationList
                    .toArray( new SearchContinuation[searchContinuationList.size()] ) );
            }
        }
        catch ( Exception e )
        {
            if ( search != null )
            {
                search.setSearchResults( new ISearchResult[0] );
            }
            monitor.reportError( e );
        }
    }


    // ── Clone Trooper Sends The Raw LDAP Search Request To The Server ────────────
    // Translates a SearchParameter into JNDI SearchControls and delegates to
    // the connection wrapper.  Returns null when no connection is available.
    // The time limit is decremented by 1 ms to avoid off-by-one timeouts.
    /**
     * Sends a raw LDAP search to the server and returns the result enumeration.
     *
     * @param browserConnection the browser connection
     * @param parameter the search parameters
     * @param monitor the progress monitor
     * @return the result enumeration, or {@code null} if no connection is available
     */
    public static StudioSearchResultEnumeration search( IBrowserConnection browserConnection, SearchParameter parameter,
        StudioProgressMonitor monitor )
    {
        if ( browserConnection == null )
        {
            return null;
        }

        String searchBase = parameter.getSearchBase().getName();
        SearchControls searchControls = new SearchControls();
        SearchScope scope = parameter.getScope();

        switch ( scope )
        {
            case OBJECT:
                searchControls.setSearchScope( SearchControls.OBJECT_SCOPE );
                break;
            case ONELEVEL:
                searchControls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
                break;
            case SUBTREE:
                searchControls.setSearchScope( SearchControls.SUBTREE_SCOPE );
                break;
            default:
                searchControls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
        }

        searchControls.setReturningAttributes( parameter.getReturningAttributes() );
        searchControls.setCountLimit( parameter.getCountLimit() );
        int timeLimit = parameter.getTimeLimit() * 1000;
        if ( timeLimit > 1 )
        {
            timeLimit--;
        }
        searchControls.setTimeLimit( timeLimit );
        String filter = parameter.getFilter();
        AliasDereferencingMethod aliasesDereferencingMethod = parameter.getAliasesDereferencingMethod();
        ReferralHandlingMethod referralsHandlingMethod = parameter.getReferralsHandlingMethod();

        Control[] controls = null;
        if ( parameter.getControls() != null )
        {
            controls = parameter.getControls().toArray( new Control[0] );
        }

        StudioSearchResultEnumeration result = browserConnection
            .getConnection()
            .getConnectionWrapper()
            .search( searchBase, filter, searchControls, aliasesDereferencingMethod, referralsHandlingMethod, controls,
                monitor, null );
        return result;
    }


    // ── Clone Trooper Enhances The Search Parameters Before Firing The Request ───
    // Clones the search's parameter, then conditionally appends hasSubordinates /
    // numSubordinates / subordinateCount for child detection, always appends
    // objectClass for alias/referral/icon detection, and strips unsupported
    // controls by comparing against the rootDSE's supportedControl attribute.
    private static SearchParameter getSearchParameter( ISearch search )
    {
        SearchParameter searchParameter = ( SearchParameter ) search.getSearchParameter().clone();

        // add children detetion attributes
        if ( search.isInitHasChildrenFlag() )
        {
            if ( search.getBrowserConnection().getSchema()
                .hasAttributeTypeDescription( SchemaConstants.HAS_SUBORDINATES_AT )
                && !Utils.containsIgnoreCase( Arrays.asList( searchParameter.getReturningAttributes() ),
                    SchemaConstants.HAS_SUBORDINATES_AT ) )
            {
                String[] returningAttributes = new String[searchParameter.getReturningAttributes().length + 1];
                System.arraycopy( searchParameter.getReturningAttributes(), 0, returningAttributes, 0,
                    searchParameter.getReturningAttributes().length );
                returningAttributes[returningAttributes.length - 1] = SchemaConstants.HAS_SUBORDINATES_AT;
                searchParameter.setReturningAttributes( returningAttributes );
            }
            else if ( search.getBrowserConnection().getSchema()
                .hasAttributeTypeDescription( SchemaConstants.NUM_SUBORDINATES_AT )
                && !Utils.containsIgnoreCase( Arrays.asList( searchParameter.getReturningAttributes() ),
                    SchemaConstants.NUM_SUBORDINATES_AT ) )
            {
                String[] returningAttributes = new String[searchParameter.getReturningAttributes().length + 1];
                System.arraycopy( searchParameter.getReturningAttributes(), 0, returningAttributes, 0,
                    searchParameter.getReturningAttributes().length );
                returningAttributes[returningAttributes.length - 1] = SchemaConstants.NUM_SUBORDINATES_AT;
                searchParameter.setReturningAttributes( returningAttributes );
            }
            else if ( search.getBrowserConnection().getSchema()
                .hasAttributeTypeDescription( SchemaConstants.SUBORDINATE_COUNT_AT )
                && !Utils.containsIgnoreCase( Arrays.asList( searchParameter.getReturningAttributes() ),
                    SchemaConstants.SUBORDINATE_COUNT_AT ) )
            {
                String[] returningAttributes = new String[searchParameter.getReturningAttributes().length + 1];
                System.arraycopy( searchParameter.getReturningAttributes(), 0, returningAttributes, 0,
                    searchParameter.getReturningAttributes().length );
                returningAttributes[returningAttributes.length - 1] = SchemaConstants.SUBORDINATE_COUNT_AT;
                searchParameter.setReturningAttributes( returningAttributes );
            }
        }

        // always add the objectClass attribute, we need it  
        // - to detect alias and referral entries
        // - to determine the entry's icon
        // - to determine must and may attributes
        if ( !Utils.containsIgnoreCase( Arrays.asList( searchParameter.getReturningAttributes() ),
            SchemaConstants.OBJECT_CLASS_AT )
            && !Utils.containsIgnoreCase( Arrays.asList( searchParameter.getReturningAttributes() ),
                SchemaConstants.ALL_USER_ATTRIBUTES ) )
        {
            String[] returningAttributes = new String[searchParameter.getReturningAttributes().length + 1];
            System.arraycopy( searchParameter.getReturningAttributes(), 0, returningAttributes, 0,
                searchParameter.getReturningAttributes().length );
            returningAttributes[returningAttributes.length - 1] = SchemaConstants.OBJECT_CLASS_AT;
            searchParameter.setReturningAttributes( returningAttributes );
        }

        // filter controls if not supported by server
        if ( searchParameter.getControls() != null )
        {
            IBrowserConnection connection = search.getBrowserConnection();
            Set<String> supportedConrolSet = new HashSet<String>();
            if ( connection.getRootDSE() != null
                && connection.getRootDSE().getAttribute( SchemaConstants.SUPPORTED_CONTROL_AT ) != null )
            {
                IAttribute scAttribute = connection.getRootDSE().getAttribute( SchemaConstants.SUPPORTED_CONTROL_AT );
                String[] supportedControls = scAttribute.getStringValues();
                for ( int i = 0; i < supportedControls.length; i++ )
                {
                    supportedConrolSet.add( Strings.toLowerCase( supportedControls[i] ) );
                }
            }

            List<Control> controls = searchParameter.getControls();
            for ( Iterator<Control> it = controls.iterator(); it.hasNext(); )
            {
                Control control = it.next();
                if ( !supportedConrolSet.contains( Strings.toLowerCase( control.getOid() ) ) )
                {
                    it.remove();
                }
            }
        }

        return searchParameter;
    }


    // ── Clone Trooper Ensures Every Ancestor Exists In The Cache Tree ────────────
    // Walks up from the target DN to the nearest cached ancestor, then walks
    // back down creating Entry or BaseDNEntry nodes and linking them to their
    // parents.  A lightweight OBJECT-scope search verifies base-DN entries exist.
    /**
     * Creates the entry and all missing ancestors in the browser connection's cache.
     *
     * @param browserConnection the browser connection
     * @param dn the DN of the entry to create
     * @param monitor the progress monitor
     * @return the created (or found) entry, or {@code null} if creation failed
     */
    private static IEntry createAndCacheEntry( IBrowserConnection browserConnection, Dn dn,
        StudioProgressMonitor monitor )
    {
        StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );
        IEntry entry = null;

        // build tree to parent
        LinkedList<Dn> parentDnList = new LinkedList<Dn>();
        Dn parentDn = dn;
        while ( parentDn != null && browserConnection.getEntryFromCache( parentDn ) == null )
        {
            parentDnList.addFirst( parentDn );
            parentDn = parentDn.getParent();
        }

        for ( Dn aDn : parentDnList )
        {
            parentDn = aDn.getParent();
            if ( parentDn == null )
            {
                // only the root DSE has a null parent
                entry = browserConnection.getRootDSE();
            }
            else if ( !parentDn.isEmpty() && browserConnection.getEntryFromCache( parentDn ) != null )
            {
                // a normal entry has a parent but the parent isn't the rootDSE
                IEntry parentEntry = browserConnection.getEntryFromCache( parentDn );
                entry = new Entry( parentEntry, aDn.getRdn() );
                entry.setDirectoryEntry( true );
                parentEntry.addChild( entry );
                parentEntry.setChildrenInitialized( true );
                parentEntry.setHasMoreChildren( true );
                parentEntry.setHasChildrenHint( true );
                browserConnection.cacheEntry( entry );
            }
            else
            {
                // we have a base Dn, check if the entry really exists in LDAP
                // this is to avoid that a node "dc=com" is created for "dc=example,dc=com" context entry
                SearchParameter searchParameter = new SearchParameter();
                searchParameter.setSearchBase( aDn );
                searchParameter.setFilter( null );
                searchParameter.setReturningAttributes( ISearch.NO_ATTRIBUTES );
                searchParameter.setScope( SearchScope.OBJECT );
                searchParameter.setCountLimit( 1 );
                searchParameter.setTimeLimit( 0 );
                searchParameter.setAliasesDereferencingMethod( browserConnection.getAliasesDereferencingMethod() );
                searchParameter.setReferralsHandlingMethod( browserConnection.getReferralsHandlingMethod() );
                searchParameter.setInitHasChildrenFlag( true );
                dummyMonitor.reset();
                StudioSearchResultEnumeration enumeration = search( browserConnection, searchParameter, dummyMonitor );
                try
                {
                    if ( enumeration != null && enumeration.hasMore() )
                    {
                        // create base Dn entry
                        entry = new BaseDNEntry( aDn, browserConnection );
                        browserConnection.getRootDSE().addChild( entry );
                        browserConnection.cacheEntry( entry );
                        enumeration.close();
                    }
                }
                catch ( LdapException e )
                {
                }
            }
        }

        return entry;
    }


    // ── Clone Trooper Sets The Entry's Tactical Flags From The Search Result ─────
    // Mace Windu confronts Palpatine: the entry's objectClass values are inspected
    // to mark it as alias, referral, or subentry; subordinate-count attributes
    // are used to set hasChildrenHint=false when the count is 0 or "FALSE".
    /**
     * Initialises the hasChildren, isAlias, isReferral and isSubentry flags
     * of the entry from the search result attributes.
     *
     * @param entry the entry to initialise
     * @param sr the search result
     * @param searchParameter the search parameters (controls flag initialisation)
     */
    private static void initFlags( IEntry entry, StudioSearchResult sr, SearchParameter searchParameter )
    {
        for ( Attribute attribute : sr.getEntry() )
        {
            if ( attribute != null )
            {
                String attributeDescription = attribute.getUpId();
                if ( SchemaConstants.OBJECT_CLASS_AT.equalsIgnoreCase( attributeDescription ) )
                {
                    if ( entry.getAttribute( attributeDescription ) != null )
                    {
                        entry.deleteAttribute( entry.getAttribute( attributeDescription ) );
                    }
                    entry.addAttribute( new org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute( entry,
                        attributeDescription ) );
                }
                for ( org.apache.directory.api.ldap.model.entry.Value valueObject : attribute )
                {
                    if ( valueObject.isHumanReadable() )
                    {
                        String value = valueObject.getString();

                        if ( searchParameter.isInitHasChildrenFlag() )
                        {
                            // hasChildren flag
                            if ( SchemaConstants.HAS_SUBORDINATES_AT.equalsIgnoreCase( attributeDescription ) )
                            {
                                if ( "FALSE".equalsIgnoreCase( value ) ) //$NON-NLS-1$
                                {
                                    entry.setHasChildrenHint( false );
                                }
                            }
                            if ( SchemaConstants.NUM_SUBORDINATES_AT.equalsIgnoreCase( attributeDescription ) )
                            {
                                if ( "0".equalsIgnoreCase( value ) ) //$NON-NLS-1$
                                {
                                    entry.setHasChildrenHint( false );
                                }
                            }
                            if ( SchemaConstants.SUBORDINATE_COUNT_AT.equalsIgnoreCase( attributeDescription ) )
                            {
                                if ( "0".equalsIgnoreCase( value ) ) //$NON-NLS-1$
                                {
                                    entry.setHasChildrenHint( false );
                                }
                            }
                        }

                        if ( SchemaConstants.OBJECT_CLASS_AT.equalsIgnoreCase( attributeDescription ) )
                        {
                            if ( SchemaConstants.ALIAS_OC.equalsIgnoreCase( value ) )
                            {
                                entry.setAlias( true );
                                entry.setHasChildrenHint( false );
                            }

                            if ( SchemaConstants.REFERRAL_OC.equalsIgnoreCase( value ) )
                            {
                                entry.setReferral( true );
                                entry.setHasChildrenHint( false );
                            }

                            IAttribute ocAttribute = entry.getAttribute( attributeDescription );
                            Value ocValue = new Value( ocAttribute, value );
                            ocAttribute.addValue( ocValue );
                        }
                    }
                }
            }
        }

        if ( ( searchParameter.getControls() != null && searchParameter.getControls().contains(
            StudioControl.SUBENTRIES_CONTROL ) )
            || ISearch.FILTER_SUBENTRY.equalsIgnoreCase( searchParameter.getFilter() ) )
        {
            entry.setSubentry( true );
            entry.setHasChildrenHint( false );
        }
    }


    // ── Clone Trooper Overwrites The Entry's Attributes With Fresh Search Data ───
    // Clears previously-cached attributes that match the requested attribute list
    // (honouring * and + wildcards), then populates the entry with the new values
    // from the search result.  String and binary values are handled separately.
    /**
     * Fills the attributes and values from the search result into the entry,
     * clearing any previously cached values for the returned attributes.
     *
     * @param entry the entry to populate
     * @param sr the search result containing fresh attribute data
     * @param searchParameter the search parameters (used to determine which attributes to clear)
     */
    private static void fillAttributes( IEntry entry, StudioSearchResult sr, SearchParameter searchParameter )
    {
        if ( searchParameter.getReturningAttributes() == null || searchParameter.getReturningAttributes().length > 0 )
        {
            // clear old attributes defined as returning attributes or clear all
            if ( searchParameter.getReturningAttributes() != null )
            {
                String[] ras = searchParameter.getReturningAttributes();

                // special case *
                if ( Arrays.asList( ras ).contains( SchemaConstants.ALL_USER_ATTRIBUTES ) )
                {
                    // clear all user attributes
                    IAttribute[] oldAttributes = entry.getAttributes();
                    for ( int i = 0; oldAttributes != null && i < oldAttributes.length; i++ )
                    {
                        if ( !oldAttributes[i].isOperationalAttribute() )
                        {
                            entry.deleteAttribute( oldAttributes[i] );
                        }
                    }
                }

                // special case +
                if ( Arrays.asList( ras ).contains( SchemaConstants.ALL_OPERATIONAL_ATTRIBUTES ) )
                {
                    // clear all operational attributes
                    IAttribute[] oldAttributes = entry.getAttributes();
                    for ( int i = 0; oldAttributes != null && i < oldAttributes.length; i++ )
                    {
                        if ( oldAttributes[i].isOperationalAttribute() )
                        {
                            entry.deleteAttribute( oldAttributes[i] );
                        }
                    }
                }

                for ( int r = 0; r < ras.length; r++ )
                {
                    // clear attributes requested from server, also include sub-types
                    AttributeHierarchy ah = entry.getAttributeWithSubtypes( ras[r] );
                    if ( ah != null )
                    {
                        for ( Iterator<IAttribute> it = ah.iterator(); it.hasNext(); )
                        {
                            IAttribute attribute = it.next();
                            entry.deleteAttribute( attribute );
                        }
                    }
                }
            }
            else
            {
                // clear all
                IAttribute[] oldAttributes = entry.getAttributes();
                for ( int i = 0; oldAttributes != null && i < oldAttributes.length; i++ )
                {
                    entry.deleteAttribute( oldAttributes[i] );
                }
            }

            // additional clear old attributes if the record contains the attribute
            for ( Attribute attribute : sr.getEntry() )
            {
                String attributeDescription = attribute.getUpId();
                IAttribute oldAttribute = entry.getAttribute( attributeDescription );
                if ( oldAttribute != null )
                {
                    entry.deleteAttribute( oldAttribute );
                }
            }

            // set new attributes and values
            for ( Attribute attribute : sr.getEntry() )
            {
                String attributeDescription = attribute.getUpId();

                if ( attribute.iterator().hasNext() )
                {
                    IAttribute studioAttribute = null;
                    if ( entry.getAttribute( attributeDescription ) == null )
                    {
                        studioAttribute = new org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute( entry,
                            attributeDescription );
                        entry.addAttribute( studioAttribute );
                    }
                    else
                    {
                        studioAttribute = entry.getAttribute( attributeDescription );
                    }

                    for ( org.apache.directory.api.ldap.model.entry.Value value : attribute )
                    {
                        if ( value.isHumanReadable() )
                        {
                            studioAttribute.addValue( new Value( studioAttribute, value.getString() ) );
                        }
                        else
                        {
                            studioAttribute.addValue( new Value( studioAttribute, value.getBytes() ) );
                        }
                    }
                }
            }
        }
    }
}
