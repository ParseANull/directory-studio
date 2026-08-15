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

package org.apache.directory.studio.ldapbrowser.core;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.eclipse.osgi.util.NLS;


// ── CLASS: SearchManager — LANDO RUNS CLOUD CITY'S SEARCH ORDERS BOARD ───────
// Lando Calrissian keeps a board in the Baron Administrator's office that
// tracks every active intelligence-gathering mission launched from Cloud City —
// who ordered it, what they're looking for, and in which sector.
// When a new mission is added he logs it; when one concludes he removes it;
// and he can retrieve any mission by name for a status report.
// This class is that board, but for saved LDAP searches: named queries the user
// has stored so they can re-run them later without retyping the filter.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages the ordered list of saved {@link ISearch}es for a single
 * {@link IBrowserConnection}.
 * A search is a named, re-runnable LDAP query — base DN, scope, filter, and
 * the attributes to fetch.  This manager keeps the list, handles name
 * collisions on add, and fires {@link SearchUpdateEvent}s so the UI tree can
 * refresh.
 * Think of this class as Lando tracking Cloud City's intelligence missions.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchManager implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8665227628274097691L;

    /** The list of searches. */
    private List<ISearch> searchList;

    /** The connection. */
    private IBrowserConnection connection;


    // ── Lando's Empty Board, Before Any Missions ─────────────────────────────────
    // Before Cloud City accepts its first intelligence commission, Lando sets up
    // an empty mission board — no fields filled in yet.
    // This no-arg constructor exists purely for Java serialization; regular code
    // should use the public constructor that takes a connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-argument constructor required by the Java serialization mechanism.
     * The search list and connection are left uninitialised; this constructor
     * should not be called by application code — use
     * {@link #SearchManager(IBrowserConnection)} instead.
     *
     * <p>For example — Lando sets up a blank board for the serializer:</p>
     * <pre>
     *   // only the persistence layer calls this:
     *   SearchManager sm = new SearchManager();
     * </pre>
     */
    protected SearchManager()
    {
    }


    // ── Lando Opens A New Mission Board For A Sector ──────────────────────────────
    // A new sector comes under Cloud City's jurisdiction and Lando opens a fresh
    // mission board for it — empty, but ready to receive intelligence orders.
    // We create an empty search list and bind it to the given connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a search manager for the given browser connection with an
     * initially empty search list.
     * Searches added later will be associated with this connection — their
     * results come from that LDAP server.
     *
     * <p>For example — Lando opens a board for a new sector:</p>
     * <pre>
     *   SearchManager sm = new SearchManager(myConnection);
     *   // sm.getSearchCount() == 0
     * </pre>
     *
     * @param connection  the browser connection whose searches this manager
     *                    tracks; must not be {@code null}.
     */
    public SearchManager( IBrowserConnection connection )
    {
        this.connection = connection;
        this.searchList = new ArrayList<ISearch>();
    }


    // ── Lando Identifies The Sector This Board Covers ────────────────────────────
    // "Which sector is this mission board for?" — Lando points to the connection
    // label at the top of the board.
    // We just return the connection field.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection this search manager belongs to.
     * All searches in the list target entries reachable via this connection.
     *
     * <p>For example — Lando identifies the sector:</p>
     * <pre>
     *   IBrowserConnection conn = searchManager.getConnection();
     * </pre>
     *
     * @return the {@link IBrowserConnection} owning this search list.
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── Lando Logs A New Mission At The End Of The Board ─────────────────────────
    // A new intelligence order arrives and Lando logs it at the bottom of the
    // active missions board.
    // We append the search to the end of the list by delegating to the
    // index-aware add method.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a search to the end of the list.
     * Delegates to {@link #addSearch(int, ISearch)} for name-collision handling.
     *
     * <p>For example — Lando logs the new order:</p>
     * <pre>
     *   searchManager.addSearch(mySearch);
     *   // mySearch now last in searchManager.getSearches()
     * </pre>
     *
     * @param search  the search to add; its name will be auto-renamed if a
     *                search with the same name already exists.
     */
    public void addSearch( ISearch search )
    {
        addSearch( searchList.size(), search );
    }


    // ── Lando Inserts A Priority Mission At A Specific Slot ──────────────────────
    // The Empire has a priority request — it needs to go into position 2 on the
    // board.  If slot 2 is already taken by another mission of the same name,
    // Lando renames the newcomer "Copy of …" before inserting it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Inserts a search at a specific position in the ordered list, resolving
     * name collisions before inserting.
     * If a search with the same name already exists we generate a unique name
     * of the form "Copy of …" or "2 Copy of …".
     * After inserting we fire a {@link SearchUpdateEvent#SEARCH_ADDED} event.
     *
     * <p>For example — Lando inserts with collision handling:</p>
     * <pre>
     *   if (getSearch(search.getName()) != null):
     *     search.setName("Copy of " + search.getName())
     *   searchList.add(index, search)
     *   EventRegistry.fireSearchUpdated(SEARCH_ADDED, ...)
     * </pre>
     *
     * @param index   the zero-based insertion position; must be in [0, size].
     * @param search  the search to insert; may be renamed on name collision.
     */
    public void addSearch( int index, ISearch search )
    {
        if ( getSearch( search.getName() ) != null )
        {
            String newSearchName = NLS.bind( BrowserCoreMessages.copy_n_of_s, "", search.getName() ); //$NON-NLS-1$

            for ( int i = 2; this.getSearch( newSearchName ) != null; i++ )
            {
                newSearchName = NLS.bind( BrowserCoreMessages.copy_n_of_s, i + " ", search.getName() ); //$NON-NLS-1$
            }

            search.setName( newSearchName );
        }

        searchList.add( index, search );
        EventRegistry.fireSearchUpdated( new SearchUpdateEvent( search, SearchUpdateEvent.EventDetail.SEARCH_ADDED ),
            this );
    }


    // ── Lando Retrieves A Mission By Its Codename ────────────────────────────────
    // "What's the status of Operation Blue Harvest?" — Lando's aide scans the
    // board and returns the full mission dossier for that codename.
    // We do a linear scan for the matching name and return the search (or null).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the search with the given name, or {@code null} if none
     * exists.
     * The name comparison is case-sensitive.
     *
     * <p>For example — Lando looks up a mission:</p>
     * <pre>
     *   ISearch s = searchManager.getSearch("All Users");
     *   if (s != null) { s.run(); }
     * </pre>
     *
     * @param name  the exact name of the search to find.
     * @return the matching {@link ISearch}, or {@code null} if not found.
     */
    public ISearch getSearch( String name )
    {
        for ( ISearch search : searchList )
        {
            if ( search.getName().equals( name ) )
            {
                return search;
            }
        }

        return null;
    }


    // ── Lando Checks The Slot Number Of A Mission ────────────────────────────────
    // "Which position on the board is Operation Blue Harvest?"
    // Lando checks the numbered rows and returns the slot.
    // We return the list index of the search.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index of the given search in the ordered list,
     * or {@code -1} if it is not present.
     * Useful when a search is being renamed and needs to be re-inserted at its
     * original position.
     *
     * <p>For example — Lando checks the slot:</p>
     * <pre>
     *   int pos = searchManager.indexOf(mySearch); // e.g., 3
     * </pre>
     *
     * @param search  the search whose position we want.
     * @return the index of the first occurrence, or {@code -1} if not found.
     */
    public int indexOf( ISearch search )
    {
        return searchList.indexOf( search );
    }


    // ── Lando Closes A Completed Mission ─────────────────────────────────────────
    // "Operation Blue Harvest is complete — remove it from the active board."
    // Lando crosses it off and fires the "mission closed" alert to the team.
    // We remove the search and fire a SEARCH_REMOVED event.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given search from the list and fires a
     * {@link SearchUpdateEvent#SEARCH_REMOVED} event.
     *
     * <p>For example — Lando closes a mission:</p>
     * <pre>
     *   searchManager.removeSearch(mySearch);
     *   // mySearch no longer in searchManager.getSearches()
     * </pre>
     *
     * @param search  the search to remove; silently ignored if not in the list.
     */
    public void removeSearch( ISearch search )
    {
        searchList.remove( search );

        EventRegistry.fireSearchUpdated( new SearchUpdateEvent( search, SearchUpdateEvent.EventDetail.SEARCH_REMOVED ),
            this );
    }


    // ── Lando Closes A Mission By Its Codename ───────────────────────────────────
    // "Close out whichever mission is called 'Blue Harvest' — I don't have the
    // reference object handy."
    // We look up by name and delegate to the by-reference remove.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the search with the given name from the list.
     * Convenience wrapper — avoids callers having to do the
     * {@link #getSearch(String)} lookup themselves.
     *
     * <p>For example — Lando closes by codename:</p>
     * <pre>
     *   searchManager.removeSearch("All Users");
     * </pre>
     *
     * @param name  the name of the search to remove; does nothing if not found.
     */
    public void removeSearch( String name )
    {
        removeSearch( getSearch( name ) );
    }


    // ── Lando Prints The Full Mission Board ──────────────────────────────────────
    // "Give me the complete list of all active intelligence missions."
    // Lando's aide hands over a printed copy — snapshot-safe, won't be affected
    // by subsequent additions or removals.
    // We return a defensive copy of the list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a snapshot of all searches currently in this manager.
     * The returned list is a copy, so callers can iterate it safely even if
     * the manager is modified concurrently (though note this class is not
     * thread-safe in general).
     *
     * <p>For example — Lando prints the board:</p>
     * <pre>
     *   List&lt;ISearch&gt; all = searchManager.getSearches();
     *   for (ISearch s : all) { s.run(); }
     * </pre>
     *
     * @return a new {@link List} containing all {@link ISearch}es, in order;
     *         never {@code null}, may be empty.
     */
    public List<ISearch> getSearches()
    {
        // clone the internal list
        return new ArrayList<ISearch>( searchList );
    }


    // ── Lando Checks The Mission Count ───────────────────────────────────────────
    // "How many active missions are on the board right now?"
    // Lando glances at the row tally at the bottom of the board.
    // We just return the list size.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the total number of saved searches in this manager.
     *
     * <p>For example — Lando checks the tally:</p>
     * <pre>
     *   int n = searchManager.getSearchCount(); // e.g., 4
     * </pre>
     *
     * @return the number of searches, {@code 0} if none have been added.
     */
    public int getSearchCount()
    {
        return searchList.size();
    }
}
