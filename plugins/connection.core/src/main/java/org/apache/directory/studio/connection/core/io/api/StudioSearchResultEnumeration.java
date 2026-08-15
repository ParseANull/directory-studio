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
package org.apache.directory.studio.connection.core.io.api;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchResultEntryImpl;
import org.apache.directory.api.ldap.model.message.SearchResultReference;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ILdapLogger;
import org.apache.directory.studio.connection.core.ReferralsInfo;
import org.apache.directory.studio.connection.core.io.ConnectionWrapperUtils;


// ── CLASS: StudioSearchResultEnumeration — R2'S FULL SENSOR SWEEP RESULT QUEUE ─
// After R2 fires the sensor array, he gets back a stream of hits from the
// server.  He doesn't get them all at once — he pulls them one by one from
// the SearchCursor as he iterates.
// On top of the raw cursor, he handles referrals: if the server sends a
// referral back during the sweep, he either queues it for manual follow-up,
// or opens a connection to the referral URL and recurses into a sub-sweep.
// This class wraps the Apache Directory API SearchCursor and implements that
// lazy iteration + referral-handling logic.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Lazy iterator over LDAP search results, with built-in referral handling.
 * Wraps an Apache Directory API {@link SearchCursor} and returns
 * {@link StudioSearchResult} instances one at a time via {@link #hasMore()} /
 * {@link #next()}.
 *
 * <p>Referral handling depends on the {@link ReferralHandlingMethod}:</p>
 * <ul>
 *   <li>{@code IGNORE} — referrals from the server are discarded.</li>
 *   <li>{@code FOLLOW_MANUALLY} — referral URLs are surfaced as placeholder
 *       {@link StudioSearchResult} entries with a {@code searchContinuationUrl}.</li>
 *   <li>{@code FOLLOW} — we automatically open the referral connection and recursively
 *       search it, returning results from both the original and the referral server.</li>
 * </ul>
 * Logging is performed after each result entry and after the search-done message
 * via the registered {@link ILdapLogger} instances.
 * Think of this as R2's sensor sweep return queue: he pulls hits from the
 * cursor, handles any referral redirects he encounters, and delivers each
 * result to the caller one at a time.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioSearchResultEnumeration
{
    /** The connection that originally issued this search. */
    private Connection connection;

    /** Search parameters — stored for referral recursion. */
    private String searchBase;
    private String filter;
    private SearchControls searchControls;
    private AliasDereferencingMethod aliasesDereferencingMethod;
    private ReferralHandlingMethod referralsHandlingMethod;
    private Control[] controls;

    /** The LDAP request number used for correlation in the search log. */
    private long requestNum;

    /** Progress monitor for cancellation support. */
    private StudioProgressMonitor monitor;

    /** Tracks referral URLs collected during this search (to avoid infinite loops). */
    private ReferralsInfo referralsInfo;

    /** Running count of result entries returned so far. */
    private long resultEntryCounter;

    /** The underlying Apache Directory API search cursor. */
    private SearchCursor cursor;

    /** The current search result entry pulled from the cursor; null between entries. */
    private SearchResultEntry currentSearchResultEntry;

    /**
     * Current referral URL list being consumed when referral handling is FOLLOW_MANUALLY.
     * We pick one URL at a time from this list and surface it to the caller.
     */
    private List<String> currentReferralUrlsList;

    /**
     * A nested enumeration wrapping a recursive search on a referral target.
     * Non-null when referral handling is FOLLOW and we are mid-way through a
     * referral sub-sweep.
     */
    private StudioSearchResultEnumeration referralEnumeration;

    /** The SearchResultDone response from the cursor (null until the cursor is exhausted). */
    private SearchResultDone searchResultDone;


    // ── CONSTRUCTOR — SET UP THE SENSOR SWEEP ─────────────────────────────────────
    // We store all the parameters we might need if we encounter a referral and
    // need to fire a recursive sub-sweep with slightly adjusted parameters.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link StudioSearchResultEnumeration}.
     *
     * @param connection                  The connection that issued the search.
     * @param cursor                      The raw Apache Directory API search cursor.
     * @param searchBase                  The original search base DN string.
     * @param filter                      The original LDAP filter string.
     * @param searchControls              The original search controls (scope, limits, attributes).
     * @param aliasesDereferencingMethod  How aliases should be dereferenced.
     * @param referralsHandlingMethod     How referrals should be handled.
     * @param controls                    LDAP controls attached to the search request.
     * @param requestNum                  The request number for log correlation.
     * @param monitor                     Progress monitor for cancellation.
     * @param referralsInfo               Referral tracking context; a fresh one is created if {@code null}.
     */
    public StudioSearchResultEnumeration( Connection connection, SearchCursor cursor, String searchBase, String filter,
        SearchControls searchControls, AliasDereferencingMethod aliasesDereferencingMethod,
        ReferralHandlingMethod referralsHandlingMethod, Control[] controls, long requestNum,
        StudioProgressMonitor monitor, ReferralsInfo referralsInfo )
    {
        this.connection = connection;
        this.searchBase = searchBase;
        this.filter = filter;
        this.searchControls = searchControls;
        this.aliasesDereferencingMethod = aliasesDereferencingMethod;
        this.referralsHandlingMethod = referralsHandlingMethod;
        this.controls = controls;
        this.requestNum = requestNum;
        this.monitor = monitor;
        this.referralsInfo = referralsInfo;
        this.resultEntryCounter = 0;

        if ( referralsInfo == null )
        {
            this.referralsInfo = new ReferralsInfo( false );
        }

        this.cursor = cursor;
    }


    // ── CLOSE — SHUT DOWN THE SENSOR ARRAY ────────────────────────────────────────
    // We close the underlying cursor and release server-side resources.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Closes the underlying {@link SearchCursor} and releases server-side resources.
     *
     * @throws LdapException  If closing the cursor fails.
     */
    public void close() throws LdapException
    {
        try
        {
            cursor.close();
        }
        catch ( Exception e )
        {
            throw new LdapException( e.getMessage() );
        }
    }


    // ── HAS MORE — ADVANCE THE CURSOR AND CHECK FOR THE NEXT ENTRY ────────────────
    // We pull from the cursor until we find a SearchResultEntry to deliver.
    // If we hit a referral, we queue it according to the handling method.
    // If we exhaust the cursor, we process any queued referrals.
    // Returns true if there's something for next() to return.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Advances the iteration and returns {@code true} if there is another result
     * to deliver via {@link #next()}.
     * This method advances the cursor, collects referrals from the response stream,
     * and recursively opens referral connections when needed.
     *
     * @return  {@code true} if {@link #next()} will return a result.
     * @throws LdapException  If the cursor encounters a protocol error.
     */
    public boolean hasMore() throws LdapException
    {
        try
        {
            // Nulling the current search result entry
            currentSearchResultEntry = null;

            // Do we have another response in the cursor?
            while ( cursor.next() )
            {
                Response currentResponse = cursor.get();

                // Is it a search result entry?
                if ( currentResponse instanceof SearchResultEntry )
                {
                    currentSearchResultEntry = ( SearchResultEntry ) currentResponse;

                    // return true if the current response is a search result entry
                    return true;
                }
                // Is it a search result reference (ie. a referral)?
                else if ( currentResponse instanceof SearchResultReference )
                {
                    // Are we ignoring referrals?
                    if ( referralsHandlingMethod != ReferralHandlingMethod.IGNORE )
                    {
                        // Storing the referral for later use
                        referralsInfo.addReferral( ( ( SearchResultReference ) currentResponse ).getReferral() );
                    }
                }
            }

            // Storing the search result done (if needed)
            if ( searchResultDone == null )
            {
                searchResultDone = ( ( SearchCursor ) cursor ).getSearchResultDone();
                Referral referral = searchResultDone.getLdapResult().getReferral();
                if ( referralsHandlingMethod != ReferralHandlingMethod.IGNORE && referral != null )
                {
                    // Storing the referral for later use
                    referralsInfo.addReferral( referral );
                }
            }

            // Are we following referrals manually?
            if ( referralsHandlingMethod == ReferralHandlingMethod.FOLLOW_MANUALLY )
            {
                // Checking the current referral's URLs list
                if ( ( currentReferralUrlsList != null ) && ( currentReferralUrlsList.size() > 0 ) )
                {
                    // return true if there's at least one referral LDAP URL to handle
                    return true;
                }

                // Checking the referrals list
                if ( referralsInfo.hasMoreReferrals() )
                {
                    // Getting the list of the next referral
                    currentReferralUrlsList = new ArrayList<String>( referralsInfo.getNextReferral().getLdapUrls() );

                    // return true if there's at least one referral LDAP URL to handle
                    return currentReferralUrlsList.size() > 0;
                }
            }
            // Are we following referrals automatically?
            else if ( referralsHandlingMethod == ReferralHandlingMethod.FOLLOW )
            {
                if ( ( referralEnumeration != null ) && ( referralEnumeration.hasMore() ) )
                {
                    // return true if there's at least one more entry in the current cursor naming enumeration
                    return true;
                }

                if ( referralsInfo.hasMoreReferrals() )
                {
                    Referral referral = referralsInfo.getNextReferral();
                    List<String> referralUrls = new ArrayList<String>( referral.getLdapUrls() );
                    LdapUrl url = new LdapUrl( referralUrls.get( 0 ) );

                    Connection referralConnection = ConnectionWrapperUtils.getReferralConnection( referral, monitor,
                        this );
                    if ( referralConnection != null )
                    {
                        String referralSearchBase = url.getDn() != null && !url.getDn().isEmpty()
                            ? url.getDn().getName()
                            : searchBase;
                        String referralFilter = url.getFilter() != null && url.getFilter().length() == 0
                            ? url.getFilter()
                            : filter;
                        SearchControls referralSearchControls = new SearchControls();
                        referralSearchControls.setSearchScope( url.getScope().getScope() > -1
                            ? url.getScope().getScope()
                            : searchControls.getSearchScope() );
                        referralSearchControls
                            .setReturningAttributes( url.getAttributes() != null && url.getAttributes().size() > 0
                                ? url.getAttributes().toArray( new String[url.getAttributes().size()] )
                                : searchControls.getReturningAttributes() );
                        referralSearchControls.setCountLimit( searchControls.getCountLimit() );
                        referralSearchControls.setTimeLimit( searchControls.getTimeLimit() );
                        referralSearchControls.setDerefLinkFlag( searchControls.getDerefLinkFlag() );
                        referralSearchControls.setReturningObjFlag( searchControls.getReturningObjFlag() );

                        referralEnumeration = referralConnection.getConnectionWrapper().search( referralSearchBase,
                            referralFilter, referralSearchControls, aliasesDereferencingMethod, referralsHandlingMethod,
                            controls, monitor, referralsInfo );

                        return referralEnumeration.hasMore();
                    }
                }
            }

            for ( ILdapLogger logger : ConnectionCorePlugin.getDefault().getLdapLoggers() )
            {
                logger.logSearchResultDone( connection, resultEntryCounter, requestNum, null );
            }

            return false;
        }
        catch ( CursorException e )
        {
            throw new LdapException( e.getMessage(), e );
        }
    }


    // ── NEXT — DELIVER THE CURRENT ENTRY ──────────────────────────────────────────
    // If we have an entry from the cursor, we log it and wrap it in a
    // StudioSearchResult.  If we're in manual-referral mode, we build a placeholder
    // result from the next referral URL.  If we're in auto-follow mode, we delegate
    // to the sub-sweep.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current {@link StudioSearchResult} and advances internal state.
     * Must only be called after {@link #hasMore()} returned {@code true}.
     * Logs the entry via {@link ILdapLogger} instances.
     *
     * @return  The current {@link StudioSearchResult}.
     * @throws LdapException  If an error occurs building the result.
     */
    public StudioSearchResult next() throws LdapException
    {
        try
        {
            if ( currentSearchResultEntry != null )
            {
                resultEntryCounter++;
                StudioSearchResult ssr = new StudioSearchResult( currentSearchResultEntry, connection, false, null );

                for ( ILdapLogger logger : ConnectionCorePlugin.getDefault().getLdapLoggers() )
                {
                    logger.logSearchResultEntry( connection, ssr, requestNum, null );
                }

                return ssr;
            }

            // Are we following referrals manually?
            if ( referralsHandlingMethod == ReferralHandlingMethod.FOLLOW_MANUALLY )
            {
                // Checking the current referral's URLs list
                if ( ( currentReferralUrlsList != null ) && ( currentReferralUrlsList.size() > 0 ) )
                {
                    resultEntryCounter++;
                    // Building an LDAP URL from the the url
                    LdapUrl url = new LdapUrl( currentReferralUrlsList.remove( 0 ) );

                    // Building the search result
                    SearchResultEntry sre = new SearchResultEntryImpl();
                    sre.setEntry( new DefaultEntry() );
                    sre.setObjectName( url.getDn() );

                    return new StudioSearchResult( sre, null, false, url );
                }
            }
            // Are we following referrals automatically?
            else if ( referralsHandlingMethod == ReferralHandlingMethod.FOLLOW )
            {
                resultEntryCounter++;
                return new StudioSearchResult( referralEnumeration.next().getSearchResultEntry(), connection,
                    true, null );
            }

            return null;
        }
        catch ( Exception e )
        {
            throw new LdapException( e.getMessage() );
        }
    }


    // ── GET CONNECTION — WHICH SHIP IS RUNNING THIS SWEEP? ────────────────────────
    // Returns the connection that originally issued this search.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Connection} that issued the original search request.
     *
     * @return  The source connection.
     */
    public Connection getConnection()
    {
        return connection;
    }


    // ── GET RESPONSE CONTROLS — WHAT DID THE SERVER SEND BACK IN THE DONE? ────────
    // Some servers attach response controls (e.g. a paged-results cookie) to the
    // SearchResultDone message.  We expose those here so callers can extract them.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP controls sent by the server in the {@link SearchResultDone} message.
     * These often include the paged-results response control carrying the next-page cookie.
     * Returns an empty collection if no controls were sent.
     *
     * @return  A {@link Collection} of response {@link Control}s; never {@code null}.
     */
    public Collection<Control> getResponseControls()
    {
        if ( searchResultDone != null )
        {
            Map<String, Control> controlsMap = searchResultDone
                .getControls();
            if ( ( controlsMap != null ) && ( controlsMap.size() > 0 ) )
            {
                return controlsMap.values();
            }
        }

        return Collections.emptyList();
    }

}
