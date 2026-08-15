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

package org.apache.directory.studio.ldapbrowser.core.model.impl;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;


// ── CLASS: SearchContinuation — HAN FOLLOWING A REFERRAL URL TO THE NEXT JUMP ─
// Sometimes a search comes back and says "for more results, follow this URL".
// That is a search continuation — Han writes down the referral URL, marks the
// continuation as UNRESOLVED, and when the user says "follow it", he resolves
// the connection and re-runs the search.  If the user cancels, it stays
// CANCELED.  The search parameters are cloned from the original search and
// overridden with whatever the referral URL specifies (new base DN, filter,
// scope, or attributes).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents an LDAP search continuation (a referral URL returned by a search).
 * Extends {@link Search} and implements {@link IContinuation} to manage the
 * referral resolution lifecycle: UNRESOLVED → RESOLVED / CANCELED.
 * Parameters from the continuation URL override the cloned original search
 * parameters on construction.
 *
 * <p>Think of this as Han following a referral URL to the next hyperspace
 * jump — he parks the continuation as unresolved, waits for the user to pick
 * a connection, then executes the jump.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchContinuation extends Search implements IContinuation
{

    private static final long serialVersionUID = 9039452279802784225L;

    /** The search continuation URL */
    private LdapUrl searchContinuationURL;

    /** The state */
    private State state;

    /** The dummy connection. */
    private DummyConnection dummyConnection;


    // ── Han Sets Up The Continuation From The Original Search And A Referral URL ─
    // "Cloning search parameters... applying URL overrides (DN, filter, scope,
    // attributes)... state = UNRESOLVED.  Waiting for the user to pick a connection."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of SearchContinuation.
     * Clones the parameters from {@code originalSearch} and overrides them
     * with any values specified by {@code searchContinuationURL}.
     * Initial state is {@link State#UNRESOLVED}.
     *
     * @param originalSearch the search whose parameters should be used as
     *                       the starting point
     * @param searchContinuationURL the referral URL to resolve
     */
    public SearchContinuation( ISearch originalSearch, LdapUrl searchContinuationURL )
    {
        super( null, ( SearchParameter ) originalSearch.getSearchParameter().clone() );
        this.searchContinuationURL = searchContinuationURL;
        this.state = State.UNRESOLVED;

        getSearchParameter().setName( searchContinuationURL.toString() );

        // apply parameters from URL
        if ( searchContinuationURL.getDn() != null && !searchContinuationURL.getDn().isEmpty() )
        {
            getSearchParameter().setSearchBase( searchContinuationURL.getDn() );
        }
        if ( searchContinuationURL.getFilter() != null && getSearchParameter().getFilter().length() > 0 )
        {
            getSearchParameter().setFilter( searchContinuationURL.getFilter() );
        }
        if ( searchContinuationURL.getScope().getScope() > -1 )
        {
            switch ( searchContinuationURL.getScope() )
            {
                case OBJECT:
                    getSearchParameter().setScope( SearchScope.OBJECT );
                    break;
                case ONELEVEL:
                    getSearchParameter().setScope( SearchScope.ONELEVEL );
                    break;
                case SUBTREE:
                    getSearchParameter().setScope( SearchScope.SUBTREE );
                    break;
            }
        }
        if ( searchContinuationURL.getAttributes() != null && !searchContinuationURL.getAttributes().isEmpty() )
        {
            getSearchParameter()
                .setReturningAttributes( searchContinuationURL.getAttributes().toArray( new String[0] ) );
        }
    }


    // ── Han Returns The Resolved Connection Or A Dummy While Pending ─────────────
    @Override
    public IBrowserConnection getBrowserConnection()
    {
        if ( state == State.RESOLVED )
        {
            return super.getBrowserConnection();
        }
        else
        {
            if ( dummyConnection == null )
            {
                dummyConnection = new DummyConnection( Schema.DEFAULT_SCHEMA );
            }
            return dummyConnection;
        }
    }


    // ── Han Returns Results Only If The Jump Has Been Completed ──────────────────
    @Override
    public ISearchResult[] getSearchResults()
    {
        if ( state == State.RESOLVED )
        {
            return super.getSearchResults();
        }
        else
        {
            return null;
        }
    }


    // ── Han Reports The Current Jump State ───────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public State getState()
    {
        return state;
    }


    // ── Han Executes The Jump: Resolve The Referral And Set The Connection ────────
    // "User confirmed.  Get referral connection.  If null, mark CANCELED.
    // Otherwise, assign the connection and mark RESOLVED."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void resolve()
    {
        // get referral connection, exit if canceled
        List<String> urls = new ArrayList<String>();
        urls.add( searchContinuationURL.toString() );
        Connection referralConnection = ConnectionCorePlugin.getDefault().getReferralHandler().getReferralConnection(
            urls );
        if ( referralConnection == null )
        {
            state = State.CANCELED;
            return;
        }
        else
        {
            super.connection = BrowserCorePlugin.getDefault().getConnectionManager().getBrowserConnection(
                referralConnection );
            state = State.RESOLVED;
        }
    }


    // ── Han Returns The Continuation URL ─────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public LdapUrl getUrl()
    {
        return searchContinuationURL;
    }


    // ── Han Clones The Continuation For Re-Use ────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public SearchContinuation clone()
    {
        SearchContinuation clone = new SearchContinuation( this, getUrl() );
        clone.state = this.state;
        clone.dummyConnection = this.dummyConnection;
        clone.connection = super.connection;
        return clone;
    }
}
