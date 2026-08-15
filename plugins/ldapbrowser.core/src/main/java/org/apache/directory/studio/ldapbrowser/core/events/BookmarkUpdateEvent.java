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


import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;


// ── CLASS: BookmarkUpdateEvent — LANDO LOGS A NEW WAYPOINT IN THE FALCON ────
// Lando Calrissian is flying the Millennium Falcon toward the second Death Star
// and keeps the navigation log meticulously: "Waypoint added — Endor system."
// "Waypoint removed — Bespin (too hot now)."  Each log entry says which
// waypoint changed and what happened to it.
// This class is that navigation log entry: it records which {@link IBookmark}
// was affected and whether it was added, updated, or removed, so any UI
// listening to the Cloud City docking board knows exactly what changed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Signals that an {@link IBookmark} was added, updated, or removed.
 * A bookmark in Directory Studio is a saved shortcut to a specific LDAP entry —
 * you give it a name and a DN and it appears in the Bookmarks view for quick
 * navigation.  Whenever the {@link org.apache.directory.studio.ldapbrowser.core.BookmarkManager}
 * changes the bookmark list it fires one of these events.
 * Listeners (usually the Bookmarks view) check the {@link Detail} enum value
 * to decide whether to insert a row, update an existing row, or remove one.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BookmarkUpdateEvent
{

    /**
     * Contains constants to specify the event detail.
     * Think of these as the action codes in Lando's navigation log:
     * ADDED means a new waypoint was registered, UPDATED means the coordinates
     * changed, and REMOVED means it was struck from the log.
     */
    public enum Detail
    {
        /** Indicates that the bookmark was added. */
        BOOKMARK_ADDED,

        /** Indicates that the bookmark was updated. */
        BOOKMARK_UPDATED,

        /** Indicates that the bookmark was removed. */
        BOOKMARK_REMOVED
    }

    /** The event detail. */
    private Detail detail;

    /** The updated bookmark. */
    private IBookmark bookmark;


    // ── Lando Records The Waypoint And The Action ────────────────────────────────
    // "Endor waypoint — ADDED."  Lando writes both the waypoint name and the
    // action in a single log entry so the co-pilot can read it at a glance.
    // We store both the affected bookmark and the Detail enum so listeners
    // don't have to cross-reference two separate objects.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BookmarkUpdateEvent.
     *
     * <p>For example — fired when a bookmark is added:</p>
     * <pre>
     *   new BookmarkUpdateEvent(myBookmark, BookmarkUpdateEvent.Detail.BOOKMARK_ADDED);
     * </pre>
     *
     * @param bookmark the bookmark that was added, updated, or removed.
     * @param detail   the action that happened (ADDED, UPDATED, or REMOVED).
     */
    public BookmarkUpdateEvent( IBookmark bookmark, Detail detail )
    {
        this.bookmark = bookmark;
        this.detail = detail;
    }


    // ── Lando Hands Over The Waypoint Object ─────────────────────────────────────
    // The co-pilot asks "which waypoint changed?" — Lando slides the nav-card
    // across the console.  This method returns the actual bookmark object so
    // the listener can read its name, DN, and other metadata.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the bookmark that was affected by this event.
     * For ADDED or UPDATED events the bookmark is in its new state; for REMOVED
     * events the bookmark object still exists in memory even though it has been
     * removed from the manager's list.
     *
     * @return the {@link IBookmark}; never {@code null}.
     */
    public IBookmark getBookmark()
    {
        return bookmark;
    }


    // ── Lando Reads The Action Code From The Log ─────────────────────────────────
    // "Action: ADDED."  The co-pilot doesn't need to re-examine the whole nav
    // board — the log entry tells them exactly what to do with the waypoint.
    // Listeners call this to decide whether to insert, refresh, or remove a
    // row in the Bookmarks view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Detail} constant indicating what happened to the bookmark.
     * Listeners typically switch on this value:
     * <ul>
     *   <li>{@code BOOKMARK_ADDED}   — insert a new row.</li>
     *   <li>{@code BOOKMARK_UPDATED} — refresh an existing row.</li>
     *   <li>{@code BOOKMARK_REMOVED} — remove the row.</li>
     * </ul>
     *
     * @return the event detail; never {@code null}.
     */
    public Detail getDetail()
    {
        return detail;
    }

}
