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

package org.apache.directory.studio.ldapbrowser.core.events;


import org.apache.directory.studio.ldapbrowser.core.model.ISearch;


// ── CLASS: SearchUpdateEvent — C-3PO RELAYS A SEARCH BULLETIN TO THE ALLIANCE
// C-3PO has just finished translating a complex transmission from the Jawa
// sand-crawlers: "They've finished the search!  Found two droids matching your
// description — but wait, the search criteria have been updated.  Now they're
// looking for different serial numbers."  Each bulletin says which search mission
// it refers to and what happened to it: was it added to the board, completed,
// parameter-changed, or struck off the board entirely?
// This class is that bulletin: it wraps an {@link ISearch} — a saved LDAP search
// with a filter, scope, and base DN — and a {@link EventDetail} code describing
// what changed about the search's lifecycle or parameters.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an {@link ISearch} was added, removed, performed, renamed, or
 * had its parameters updated.
 * A search in Directory Studio is a saved LDAP query (filter + scope + base DN)
 * that appears in the Searches view.  Whenever the
 * {@link org.apache.directory.studio.ldapbrowser.core.SearchManager} changes a
 * search, or a job runs it, it fires one of these events.
 * Listeners check the {@link EventDetail} code to decide whether to insert a row,
 * refresh results, update a name, or remove the search from the view.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchUpdateEvent
{

    /**
     * The bulletin codes describing what happened to the search.
     * C-3PO has five types of dispatch: the mission was filed, completed,
     * briefing-updated, re-named, or cancelled.
     */
    public enum EventDetail
    {

        /** Indicates that the search was added. */
        SEARCH_ADDED,

        /** Indicates that the search was removed. */
        SEARCH_REMOVED,

        /** Indicates that the search was performed. */
        SEARCH_PERFORMED,

        /**
         * Indicates that the search parameters were updated.
         * Note: This event detail doesn't include the renaming of a search!
         */
        SEARCH_PARAMETER_UPDATED,

        /** Indicates that the search was renamed. */
        SEARCH_RENAMED
    }

    /** The event detail. */
    private EventDetail detail;

    /** The updated search. */
    private ISearch search;


    // ── C-3PO Files The Bulletin With Mission And Action ─────────────────────────
    // "Mission: Locate Skywalker.  Status: SEARCH_PERFORMED."  Both the mission
    // object and the action code go into the same bulletin so listeners don't
    // have to look them up separately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SearchUpdateEvent.
     *
     * <p>For example — fired when a search run completes:</p>
     * <pre>
     *   new SearchUpdateEvent(mySearch, EventDetail.SEARCH_PERFORMED);
     * </pre>
     *
     * @param search the search that was updated.
     * @param detail what happened to it (ADDED, REMOVED, PERFORMED, etc.).
     */
    public SearchUpdateEvent( ISearch search, EventDetail detail )
    {
        this.search = search;
        this.detail = detail;
    }


    // ── C-3PO Hands Over The Mission Dossier ─────────────────────────────────────
    // "Here's the full mission file — filter, scope, base DN, results."
    // Listeners use the search object to refresh the Searches view row, render
    // results, update name labels, and so on.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search that was affected by this event.
     * For SEARCH_REMOVED events the object still exists in memory; for all
     * other events it reflects the current (updated) state of the search.
     *
     * @return the {@link ISearch}; never {@code null}.
     */
    public ISearch getSearch()
    {
        return search;
    }


    // ── C-3PO Reads Out The Action Code ──────────────────────────────────────────
    // "Status: SEARCH_ADDED."  The listener switches on this to decide what
    // to do — insert a new row, refresh results, update a label, remove a row.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EventDetail} constant indicating what changed.
     * Listeners switch on this value:
     * <ul>
     *   <li>{@code SEARCH_ADDED}             — insert a new row in the Searches view.</li>
     *   <li>{@code SEARCH_REMOVED}           — remove the row.</li>
     *   <li>{@code SEARCH_PERFORMED}         — refresh the results display.</li>
     *   <li>{@code SEARCH_PARAMETER_UPDATED} — refresh parameter labels (filter, scope).</li>
     *   <li>{@code SEARCH_RENAMED}           — refresh the name label.</li>
     * </ul>
     *
     * @return the event detail; never {@code null}.
     */
    public EventDetail getDetail()
    {
        return detail;
    }

}
