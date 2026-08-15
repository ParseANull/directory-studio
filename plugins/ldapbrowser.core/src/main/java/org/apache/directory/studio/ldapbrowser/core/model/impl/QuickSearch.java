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


import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;


// ── CLASS: QuickSearch — HAN JUMPING TO HYPERSPACE WITHOUT A DESTINATION ─────
// Han doesn't always have coordinates plotted.  Sometimes he just pulls the
// lever and picks a heading on the fly — quick, minimal setup, full speed.
// QuickSearch is that impulse jump: a Search subtype anchored to a specific
// entry with SUBTREE scope and the connection's default limits already filled
// in.  You hand it a base entry and go — no need to configure everything from
// scratch.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Default implementation of {@link IQuickSearch}.
 * A convenience subclass of {@link Search} that is anchored to a specific
 * base {@link IEntry} and pre-populated with the connection's default
 * scope, limits, and dereferencing settings.
 *
 * <p>Think of this as Han jumping to hyperspace with minimal setup — the
 * base entry is your destination, and the connection fills in the rest.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class QuickSearch extends Search implements IQuickSearch
{
    private static final long serialVersionUID = 4387604973869066354L;

    /** The search base entry. */
    private IEntry searchBaseEntry;


    // ── Han Plots A Quick Jump With Just A Base Entry ─────────────────────────────
    /**
     * Instantiates a new quick search anchored to the given entry.
     * Connection defaults are not applied by this constructor.
     *
     * @param searchBaseEntry the entry to use as the search base
     */
    public QuickSearch( IEntry searchBaseEntry )
    {
        this.searchBaseEntry = searchBaseEntry;
    }


    // ── Han Plots A Quick Jump With Entry + Connection Defaults ──────────────────
    // "Base entry set.  SUBTREE scope.  Connection limits applied.  Ready to go."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates a new quick search anchored to the given entry with all
     * connection defaults (scope, limits, dereferencing, referral handling)
     * pre-populated from {@code connection}.
     *
     * @param searchBaseEntry the entry to use as the search base
     * @param connection the browser connection to read default settings from
     */
    public QuickSearch( IEntry searchBaseEntry, IBrowserConnection connection )
    {
        this.searchBaseEntry = searchBaseEntry;
        this.connection = connection;

        // set default parameter
        getSearchParameter().setName( BrowserCoreMessages.model__quick_search_name );
        getSearchParameter().setSearchBase( searchBaseEntry.getDn() );
        getSearchParameter().setReturningAttributes( ISearch.NO_ATTRIBUTES );
        getSearchParameter().setAliasesDereferencingMethod( connection.getAliasesDereferencingMethod() );
        getSearchParameter().setReferralsHandlingMethod( connection.getReferralsHandlingMethod() );
        getSearchParameter().setCountLimit( connection.getCountLimit() );
        getSearchParameter().setTimeLimit( connection.getTimeLimit() );
        getSearchParameter().setScope( SearchScope.SUBTREE );
    }


    // ── Han Returns His Jump Destination ─────────────────────────────────────────
    /**
     * Returns the entry this quick search is anchored to.
     *
     * @return the search base entry
     */
    public IEntry getSearchBaseEntry()
    {
        return searchBaseEntry;
    }
}
