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


import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.internal.search.LdapSearchPageScoreComputer;
import org.apache.directory.studio.ldapbrowser.core.model.BookmarkParameter;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.search.ui.ISearchPageScoreComputer;


// ── CLASS: Bookmark — LANDO'S NAVICOMP WAYPOINT FOR A FAVOURITE ENTRY ────────
// Lando keeps a list of waypoints in the Cloud City navicomp — named locations
// he wants to jump back to quickly.  Each waypoint has a DN (the coordinates)
// and a friendly name ("Bespin Admin Office") so he doesn't need to remember
// the raw address.  When coordinates change, Lando updates the waypoint and
// broadcasts the update to anyone who cares.
// Bookmark is that waypoint: a connection + DN + name tuple with a live
// DelegateEntry so the rest of the UI can treat the bookmarked DN as a
// browseable entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Default implementation of {@link IBookmark}.
 * Pairs a {@link BookmarkParameter} (DN + name) with a {@link DelegateEntry}
 * so the bookmarked DN can be treated as a browseable entry by the UI.
 * Any DN or name change fires a {@link BookmarkUpdateEvent} through
 * {@link EventRegistry}.
 *
 * <p>Think of this as Lando's navicomp waypoint — a named entry in the
 * favourites list that you can jump back to at any time.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Bookmark implements IBookmark
{

    /** The serialVersionUID. */
    private static final long serialVersionUID = 2914726541167255499L;

    /** The connection. */
    private IBrowserConnection connection;

    /** The bookmark parameter. */
    private BookmarkParameter bookmarkParameter;

    /** The bookmark entry. */
    private DelegateEntry bookmarkEntry;


    // ── No-Arg Constructor For Serialisation ─────────────────────────────────────
    /**
     * Creates a new instance of Bookmark.
     * For serialisation use only — fields will be set by the deserialiser.
     */
    protected Bookmark()
    {
    }


    // ── Lando Adds A Waypoint From An Existing Parameter Bean ────────────────────
    /**
     * Creates a new instance of Bookmark.
     *
     * @param connection the browser connection this bookmark belongs to
     * @param bookmarkParameter the bookmark parameter carrying the DN and name
     */
    public Bookmark( IBrowserConnection connection, BookmarkParameter bookmarkParameter )
    {
        this.connection = connection;
        this.bookmarkParameter = bookmarkParameter;
        this.bookmarkEntry = new BookmarkEntry( connection, bookmarkParameter.getDn() );
    }


    // ── Lando Adds A Waypoint From Raw DN And Name ────────────────────────────────
    /**
     * Creates a new instance of Bookmark.
     *
     * @param connection the browser connection this bookmark belongs to
     * @param dn the target Dn
     * @param name the symbolic name shown in the bookmarks view
     */
    public Bookmark( IBrowserConnection connection, Dn dn, String name )
    {
        this.connection = connection;
        this.bookmarkParameter = new BookmarkParameter( dn, name );
        this.bookmarkEntry = new BookmarkEntry( connection, dn );
    }


    // ── Lando Reads The Waypoint's DN Coordinates ─────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public Dn getDn()
    {
        return this.bookmarkParameter.getDn();
    }


    // ── Lando Updates The Waypoint's DN And Notifies Listeners ───────────────────
    /**
     * {@inheritDoc}
     */
    public void setDn( Dn dn )
    {
        this.bookmarkParameter.setDn( dn );
        this.fireBookmarkUpdated( BookmarkUpdateEvent.Detail.BOOKMARK_UPDATED );
    }


    // ── Lando Reads The Waypoint's Friendly Name ──────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return this.bookmarkParameter.getName();
    }


    // ── Lando Renames The Waypoint And Notifies Listeners ────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setName( String name )
    {
        this.bookmarkParameter.setName( name );
        this.fireBookmarkUpdated( BookmarkUpdateEvent.Detail.BOOKMARK_UPDATED );
    }


    // ── R2-D2 Adapts The Bookmark To Any Requested Interface ─────────────────────
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter )
    {
        Class<?> clazz = ( Class<?> ) adapter;
        if ( clazz.isAssignableFrom( ISearchPageScoreComputer.class ) )
        {
            return new LdapSearchPageScoreComputer();
        }
        if ( clazz.isAssignableFrom( Connection.class ) )
        {
            return getBrowserConnection().getConnection();
        }
        if ( clazz.isAssignableFrom( IBrowserConnection.class ) )
        {
            return getBrowserConnection();
        }
        if ( clazz.isAssignableFrom( IEntry.class ) )
        {
            return getEntry();
        }
        if ( clazz.isAssignableFrom( IBookmark.class ) )
        {
            return this;
        }

        return null;
    }


    // ── Obi-Wan Senses A Bookmark Change And Notifies The Force ──────────────────
    private void fireBookmarkUpdated( BookmarkUpdateEvent.Detail detail )
    {
        if ( this.getName() != null && !"".equals( this.getName() ) ) { //$NON-NLS-1$
            EventRegistry.fireBookmarkUpdated( new BookmarkUpdateEvent( this, detail ), this );
        }
    }


    // ── Lando Retrieves The Full Waypoint Parameter Bean ─────────────────────────
    /**
     * {@inheritDoc}
     */
    public BookmarkParameter getBookmarkParameter()
    {
        return bookmarkParameter;
    }


    // ── Lando Replaces The Full Waypoint Parameter Bean ──────────────────────────
    /**
     * {@inheritDoc}
     */
    public void setBookmarkParameter( BookmarkParameter bookmarkParameter )
    {
        this.bookmarkParameter = bookmarkParameter;
    }


    // ── Lando Returns Which Connection Owns This Waypoint ────────────────────────
    /**
     * {@inheritDoc}
     */
    public IBrowserConnection getBrowserConnection()
    {
        return this.connection;
    }


    // ── Lando Returns The Entry Proxy For The Bookmarked DN ──────────────────────
    /**
     * {@inheritDoc}
     */
    public IEntry getEntry()
    {
        return this.bookmarkEntry;
    }


    // ── Waypoint Prints Its Friendly Name ────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return this.getName();
    }

}
