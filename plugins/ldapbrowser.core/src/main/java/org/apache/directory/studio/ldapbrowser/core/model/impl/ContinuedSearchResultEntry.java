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

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;


// ── CLASS: ContinuedSearchResultEntry — HAN FOLLOWING A HYPESPACE TRAIL ─────
// When a search result comes back with a referral URL, Han doesn't ignore it —
// he logs it as an unresolved jump and presents it to the user.  When the user
// says "go", Han resolves the referral connection, jumps to hyperspace, and
// fetches attributes from the remote server.  If the user cancels, the jump is
// marked cancelled.  At any point the entry can be in one of three states:
// RESOLVED (we know which connection owns it), UNRESOLVED (waiting on the user
// to pick a referral connection), or CANCELED.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents a search result entry that arrived via a search continuation
 * (referral URL).  Extends {@link DelegateEntry} and implements
 * {@link IContinuation} to manage the referral resolution lifecycle:
 * UNRESOLVED → RESOLVED / CANCELED.
 *
 * <p>Think of this as Han following a hyperspace trail to a referral URL —
 * he tracks the state, resolves the remote connection when told to, and
 * fetches the entry's attributes once the jump succeeds.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ContinuedSearchResultEntry extends DelegateEntry implements IContinuation
{

    private static final long serialVersionUID = -6351277968774226912L;

    /** The search continuation URL. */
    private LdapUrl url;

    /** The state. */
    private State state;

    /** The dummy connection. */
    private DummyConnection dummyConnection;


    // ── No-Arg Constructor For Serialisation ─────────────────────────────────────
    protected ContinuedSearchResultEntry()
    {
    }


    // ── Han Sets Up A Pre-Resolved Continuation Entry ────────────────────────────
    /**
     * Creates a new instance of ContinuedSearchResultEntry.
     * Sets the initial state to {@code RESOLVED} — the connection is already
     * known and no referral lookup is required.
     *
     * @param connection the browser connection this entry belongs to
     * @param dn the Dn of the entry
     */
    public ContinuedSearchResultEntry( IBrowserConnection connection, Dn dn )
    {
        super( connection, dn );
        this.state = State.RESOLVED;
    }


    // ── Han Parks The Jump: Mark As Unresolved With A Referral URL ───────────────
    // "We have coordinates but no connection assigned yet.  Park it as UNRESOLVED
    // and wait for the user to tell us which connection to use."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the internal state of the target connection to "unresolved".
     * This means, when calling {@link #getAttributes()} or {@link #getChildren()}
     * the user is asked for the target connection to use.
     *
     * @param url the referral URL to resolve later
     */
    public void setUnresolved( LdapUrl url )
    {
        this.state = State.UNRESOLVED;
        this.url = url;
        super.connectionId = null;
    }


    // ── Han Returns The Active Connection Or A Dummy While Unresolved ────────────
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


    // ── Han Returns The Real Entry Or Null If Still Unresolved ───────────────────
    @Override
    protected IEntry getDelegate()
    {
        if ( state == State.RESOLVED )
        {
            return super.getDelegate();
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


    // ── Han Returns The Referral URL Or Falls Back To The Entry's Own URL ─────────
    /**
     * {@inheritDoc}
     */
    public LdapUrl getUrl()
    {
        return url != null ? url : super.getUrl();
    }


    // ── Han Executes The Jump: Resolve The Referral And Fetch Attributes ──────────
    // "User confirmed — get the referral connection, plot the jump, execute, and
    // kick off InitializeAttributesRunnable to load the remote entry."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void resolve()
    {
        // get referral connection, exit if canceled
        List<String> urls = new ArrayList<String>();
        urls.add( url.toString() );
        Connection referralConnection = ConnectionCorePlugin.getDefault().getReferralHandler().getReferralConnection(
            urls );
        if ( referralConnection == null )
        {
            state = State.CANCELED;
            entryDoesNotExist = true;
        }
        else
        {
            state = State.RESOLVED;
            super.connectionId = referralConnection.getId();

            InitializeAttributesRunnable iar = new InitializeAttributesRunnable( this );
            new StudioBrowserJob( iar ).execute();
        }
    }


    // ── Hash Code Comes From The DN ───────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return getDn().hashCode();
    }


    // ── Mace Windu Checks: Same DN And Connection? ────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        // check argument
        if ( !( o instanceof ContinuedSearchResultEntry ) )
        {
            return false;
        }
        ContinuedSearchResultEntry e = ( ContinuedSearchResultEntry ) o;

        // compare dn and connection
        return getDn() == null ? e.getDn() == null : ( getDn().equals( e.getDn() ) && getBrowserConnection().equals(
            e.getBrowserConnection() ) );
    }
}
