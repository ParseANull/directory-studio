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

import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.osgi.util.NLS;


// ── CLASS: BookmarkManager — LANDO RUNS CLOUD CITY'S DOCKING REGISTRY ────────
// Lando Calrissian keeps a meticulous registry of every ship docked at Cloud
// City — who's there, where they're parked, and what bay number they hold.
// He can add a new docking slot, revoke one, or hand you the full manifest
// on demand, always firing off the right alerts to his staff when things change.
// This class is that registry, but for bookmarks: named shortcuts to LDAP
// entries that the user wants to jump back to quickly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages the set of {@link IBookmark}s attached to a single
 * {@link IBrowserConnection}.
 * A bookmark is a saved shortcut to a specific LDAP entry — think of it like a
 * browser favourite, but pointing at a DN in the directory tree.
 * This manager keeps the list ordered, prevents duplicate names (renaming
 * collisions automatically), and fires the appropriate events whenever the
 * list changes so the UI can update.
 * Think of this class as Lando running Cloud City's docking registry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BookmarkManager implements Serializable
{
    private static final long serialVersionUID = 7605293576518974531L;

    private List<IBookmark> bookmarkList;

    private IBrowserConnection connection;


    // ── Lando Opens A New Registry For A Docking Port ───────────────────────────
    // When a new docking port opens at Cloud City, Lando initialises a fresh
    // registry for it — "This bay is now under administration."
    // We do the same: create an empty bookmark list tied to the given connection
    // so that subsequent add/remove calls know which connection they belong to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a fresh bookmark manager for the given browser connection.
     * We start with an empty list — no bookmarks until the user (or persistence
     * layer) adds them.
     *
     * <p>For example — Lando sets up a new port registry:</p>
     * <pre>
     *   BookmarkManager mgr = new BookmarkManager(myConnection);
     *   // mgr.getBookmarkCount() == 0 at this point
     * </pre>
     *
     * @param connection  the browser connection this manager belongs to; all
     *                    bookmarks in this manager point at entries reachable
     *                    via that connection.
     */
    public BookmarkManager( IBrowserConnection connection )
    {
        this.connection = connection;
        bookmarkList = new ArrayList<IBookmark>();
    }


    // ── Lando Checks Which Port This Registry Serves ─────────────────────────────
    // Lando's aide asks "Which docking port is this manifest for?" — Lando
    // points to the connection plate on the wall.
    // We just return the connection field set at construction time.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection this manager is attached to.
     * Every bookmark in this manager points at entries accessible through
     * that connection.
     *
     * <p>For example — Lando identifies the port:</p>
     * <pre>
     *   IBrowserConnection conn = bookmarkManager.getConnection();
     *   // conn == the connection passed to the constructor
     * </pre>
     *
     * @return the {@link IBrowserConnection} owning this bookmark list.
     */
    public IBrowserConnection getConnection()
    {
        return connection;
    }


    // ── Lando Adds A Ship To The End Of The Manifest ─────────────────────────────
    // A new ship pulls into Cloud City and Lando logs it at the bottom of the
    // current manifest — "Bay 47, just arrived."
    // We append the bookmark to the end of our list (and let the overloaded
    // version handle collision detection).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a bookmark at the end of the list.
     * Delegates to {@link #addBookmark(int, IBookmark)} so name-collision
     * handling happens in one place.
     *
     * <p>For example — Lando logs the new arrival:</p>
     * <pre>
     *   bookmarkManager.addBookmark(new Bookmark(conn, "My Users OU"));
     *   // bookmark appears last in the list
     * </pre>
     *
     * @param bookmark  the bookmark to append; its name will be auto-renamed
     *                  if another bookmark already has the same name.
     */
    public void addBookmark( IBookmark bookmark )
    {
        addBookmark( bookmarkList.size(), bookmark );
    }


    // ── Lando Inserts A VIP Ship Into A Reserved Bay ─────────────────────────────
    // A VIP shuttle needs bay 3 specifically — Lando shuffles other ships down
    // and slots the newcomer in.  If the VIP name is already taken he renames it
    // "Copy of VIP" before logging it.
    // We do exactly that: insert the bookmark at the given index, resolving any
    // name collision first, then fire a BOOKMARK_ADDED event.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a bookmark at a specific position in the ordered list.
     * If a bookmark with the same name already exists we generate a unique name
     * of the form "Copy of …" or "2 Copy of …" to avoid collisions.
     * After inserting we fire a {@link BookmarkUpdateEvent#BOOKMARK_ADDED}
     * event so the UI tree can refresh.
     *
     * <p>For example — Lando inserts with collision handling:</p>
     * <pre>
     *   if (existingBookmarkNamed(bookmark.getName()) != null):
     *     bookmark.setName("Copy of " + bookmark.getName())
     *   bookmarkList.add(index, bookmark)
     *   EventRegistry.fireBookmarkUpdated(BOOKMARK_ADDED, ...)
     * </pre>
     *
     * @param index     the zero-based position at which to insert; must be in
     *                  the range [0, size].
     * @param bookmark  the bookmark to insert; may be renamed if its name
     *                  collides with an existing bookmark.
     */
    public void addBookmark( int index, IBookmark bookmark )
    {
        if ( getBookmark( bookmark.getName() ) != null )
        {
            String newBookmarkName = NLS.bind( BrowserCoreMessages.copy_n_of_s, "", bookmark.getName() ); //$NON-NLS-1$

            for ( int i = 2; this.getBookmark( newBookmarkName ) != null; i++ )
            {
                newBookmarkName = NLS.bind( BrowserCoreMessages.copy_n_of_s, i + " ", bookmark.getName() ); //$NON-NLS-1$
            }

            bookmark.setName( newBookmarkName );
        }

        bookmarkList.add( index, bookmark );
        EventRegistry.fireBookmarkUpdated(
            new BookmarkUpdateEvent( bookmark, BookmarkUpdateEvent.Detail.BOOKMARK_ADDED ), this );
    }


    // ── Lando Looks Up A Ship By Its Callsign ────────────────────────────────────
    // "Control, I need the manifest entry for the Millennium Falcon."
    // Lando's operator scans the list and returns the entry for that callsign,
    // or reports "not in registry" if it's not there.
    // We scan our bookmark list for the matching name and return it (or null).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Finds and returns the bookmark with the given name, or {@code null} if
     * none exists.
     * Names are case-sensitive (LDAP attribute values generally are).
     *
     * <p>For example — Lando's operator looks up the callsign:</p>
     * <pre>
     *   IBookmark b = bookmarkManager.getBookmark("My Users OU");
     *   if (b == null) { /* not bookmarked yet *&#47; }
     * </pre>
     *
     * @param name  the exact name of the bookmark to look up.
     * @return the matching {@link IBookmark}, or {@code null} if not found.
     */
    public IBookmark getBookmark( String name )
    {
        for ( IBookmark bookmark : bookmarkList )
        {
            if ( bookmark.getName().equals( name ) )
            {
                return bookmark;
            }
        }

        return null;
    }


    // ── Lando Checks Which Bay A Registered Ship Occupies ───────────────────────
    // "What position is the Falcon in the manifest?" — Lando checks the registry
    // and returns the line number.
    // We return the list index of the bookmark so callers can know where to
    // re-insert it if they need to move it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index of the given bookmark in the ordered list,
     * or {@code -1} if it is not present.
     * Useful when we need to remove and re-insert a bookmark at its original
     * position (e.g., after a rename).
     *
     * <p>For example — Lando checks the manifest position:</p>
     * <pre>
     *   int pos = bookmarkManager.indexOf(myBookmark); // e.g., 2
     *   bookmarkManager.removeBookmark(myBookmark);
     *   bookmark.setName("New Name");
     *   bookmarkManager.addBookmark(pos, myBookmark);
     * </pre>
     *
     * @param bookmark  the bookmark whose position we want.
     * @return the index of the first occurrence of {@code bookmark}, or
     *         {@code -1} if not found.
     */
    public int indexOf( IBookmark bookmark )
    {
        return bookmarkList.indexOf( bookmark );
    }


    // ── Lando Strikes A Ship From The Manifest ───────────────────────────────────
    // "The Millennium Falcon has left Cloud City — remove it from the registry."
    // Lando crosses it off the manifest and alerts his staff so they stop
    // reserving that docking bay.
    // We remove the bookmark from our list and fire a BOOKMARK_REMOVED event.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given bookmark from the list and fires a
     * {@link BookmarkUpdateEvent#BOOKMARK_REMOVED} event.
     * After this call the bookmark is gone from the manager — callers that
     * hold a reference to it can still read it but it won't appear in the UI.
     *
     * <p>For example — Lando de-registers the ship:</p>
     * <pre>
     *   bookmarkManager.removeBookmark(myBookmark);
     *   // myBookmark no longer in bookmarkManager.getBookmarks()
     * </pre>
     *
     * @param bookmark  the bookmark to remove; silently does nothing if it
     *                  was not in the list.
     */
    public void removeBookmark( IBookmark bookmark )
    {
        bookmarkList.remove( bookmark );
        EventRegistry.fireBookmarkUpdated( new BookmarkUpdateEvent( bookmark,
            BookmarkUpdateEvent.Detail.BOOKMARK_REMOVED ), this );
    }


    // ── Lando Strikes A Ship By Its Callsign ─────────────────────────────────────
    // "Remove whatever ship is registered under callsign 'Rogue One' — I don't
    // care about the bay number, just get it off the books."
    // We look up by name and delegate to the by-reference remove above.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the bookmark with the given name from the list.
     * Convenience wrapper around {@link #getBookmark(String)} and
     * {@link #removeBookmark(IBookmark)} — saves callers from doing the
     * lookup themselves.
     *
     * <p>For example — Lando removes by name:</p>
     * <pre>
     *   bookmarkManager.removeBookmark("My Users OU");
     *   // equivalent to: removeBookmark(getBookmark("My Users OU"))
     * </pre>
     *
     * @param name  the name of the bookmark to remove; does nothing if no
     *              bookmark with that name exists.
     */
    public void removeBookmark( String name )
    {
        this.removeBookmark( this.getBookmark( name ) );
    }


    // ── Lando Hands Over The Full Manifest ───────────────────────────────────────
    // "Give me the complete list of every ship currently docked."
    // Lando's operator prints out the full registry as a typed array —
    // snapshot-safe, so modifications to the list later don't corrupt the copy.
    // We return an array copy so callers can iterate safely.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all bookmarks in this manager as an array.
     * The array is freshly allocated each time, so callers can iterate it
     * without worrying about concurrent modifications.
     *
     * <p>For example — Lando prints the manifest:</p>
     * <pre>
     *   for (IBookmark b : bookmarkManager.getBookmarks()) {
     *     System.out.println(b.getName() + " -> " + b.getDn());
     *   }
     * </pre>
     *
     * @return an array (possibly empty, never {@code null}) of all
     *         {@link IBookmark}s managed here, in insertion order.
     */
    public IBookmark[] getBookmarks()
    {
        return bookmarkList.toArray( new IBookmark[0] );
    }


    // ── Lando Counts The Ships In Port ───────────────────────────────────────────
    // "How many ships do we have docked right now?" — Lando glances at the tally
    // at the bottom of the manifest.
    // We just return the list size.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the number of bookmarks currently managed.
     * Useful for UI components that need to decide whether to show an
     * "empty" placeholder or a populated list.
     *
     * <p>For example — Lando checks the tally:</p>
     * <pre>
     *   int count = bookmarkManager.getBookmarkCount(); // e.g., 5
     * </pre>
     *
     * @return the count of bookmarks, {@code 0} if none have been added yet.
     */
    public int getBookmarkCount()
    {
        return bookmarkList.size();
    }
}
