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

package org.apache.directory.studio.ldapbrowser.core.model;


import java.io.Serializable;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.core.ConnectionPropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.BookmarkPropertyPageProvider;
import org.apache.directory.studio.ldapbrowser.core.propertypageproviders.EntryPropertyPageProvider;
import org.eclipse.core.runtime.IAdaptable;


// ── CLASS: IBookmark — LANDO CALRISSIAN'S FAVOURITE DOCKING WAYPOINTS ────────
// Lando keeps a personal list of docking-bay waypoints in the Falcon's navicomp:
// "Mos Eisley cantina — lat/lon 3.7°N 14.2°W", "Cloud City docking bay 12."
// Each waypoint has a friendly name and a precise location so he can jump
// there instantly without re-keying the coordinates.
// An IBookmark is exactly that: a named shortcut to a specific LDAP entry.
// Give it a display name like "Admin User" and a DN like
// "cn=admin,dc=example,dc=com" and it appears in the Bookmarks view for
// instant one-click navigation to that entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A named shortcut to a specific LDAP entry in the directory.
 * Bookmarks are persisted per-connection and shown in the Bookmarks view.
 * They let users jump directly to frequently-visited entries without having
 * to navigate the full tree.
 * Think of this as one of Lando's navicomp waypoints: a friendly name plus
 * a precise directory address.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IBookmark extends Serializable, IAdaptable, BookmarkPropertyPageProvider, EntryPropertyPageProvider,
    ConnectionPropertyPageProvider
{
    // ── Lando Reads The Waypoint's Coordinates ────────────────────────────────────
    // "Cloud City docking bay 12 — lat/lon stored as DN."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the distinguished name (DN) of the target LDAP entry.
     * This is the precise directory address the bookmark points to.
     *
     * @return the target {@link Dn}; never {@code null}.
     */
    Dn getDn();


    // ── Lando Updates The Waypoint's Coordinates ──────────────────────────────────
    // "Move the Cloud City waypoint — they've rebuilt the docking bay."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the target DN of this bookmark.
     *
     * @param dn the new target {@link Dn}; must not be {@code null}.
     */
    void setDn( Dn dn );


    // ── Lando Reads The Waypoint's Friendly Label ─────────────────────────────────
    // "Cloud City docking bay 12" — the human-readable name shown in the UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name of this bookmark.
     * Shown in the Bookmarks view to identify the shortcut.
     *
     * @return the bookmark name; may be {@code null} if not yet set.
     */
    String getName();


    // ── Lando Renames The Waypoint ────────────────────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the display name of this bookmark.
     *
     * @param name the new name; must not be {@code null}.
     */
    void setName( String name );


    // ── Lando Checks Which Hyperspace Network This Waypoint Is On ────────────────
    // Each waypoint belongs to a specific comms channel — the browser connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the browser connection this bookmark belongs to.
     * A bookmark's target DN is only meaningful within its connection's DIT.
     *
     * @return the owning {@link IBrowserConnection}; never {@code null}.
     */
    IBrowserConnection getBrowserConnection();


    // ── Lando Resolves The Waypoint To An Actual Location ────────────────────────
    // "Navigate to Cloud City docking bay 12 — pull up the entry."
    // The entry is resolved from the connection's cache using the bookmark's DN.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IEntry} this bookmark points to, or {@code null} if the
     * entry hasn't been loaded into the cache yet.
     * Navigating to a bookmark typically triggers a fetch if the entry isn't cached.
     *
     * @return the target entry, or {@code null} if not yet cached.
     */
    IEntry getEntry();


    // ── Lando Retrieves The Compact Waypoint Data-Bean ────────────────────────────
    // The {@link BookmarkParameter} is the serialisable data-bean that persists
    // the bookmark's name and DN across sessions.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link BookmarkParameter} data-bean for this bookmark.
     * Used for serialisation and persistence.
     *
     * @return the bookmark parameter; never {@code null}.
     */
    BookmarkParameter getBookmarkParameter();


    // ── Lando Installs A New Waypoint Data-Bean ───────────────────────────────────
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the {@link BookmarkParameter} for this bookmark.
     *
     * @param bookmarkParameter the new parameter; must not be {@code null}.
     */
    void setBookmarkParameter( BookmarkParameter bookmarkParameter );
}
