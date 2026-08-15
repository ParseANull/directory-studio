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


import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.EventRunnable;
import org.apache.directory.studio.connection.core.event.EventRunnableFactory;
import org.apache.directory.studio.connection.core.event.EventRunner;


// ── CLASS: EventRegistry — THE IMPERIAL HOLONET RELAY STATION ───────────────
// The Imperial HoloNet is the galaxy-spanning broadcast network that routes
// official communications to all Star Destroyers, planetary garrisons, and
// Imperial outposts simultaneously.  It doesn't originate the messages; it
// just receives them from authorised sources and fans them out to every
// subscriber on the channel.
// This class is the LDAP browser's HoloNet: a static, central hub that holds
// four event channels (search, bookmark, browser-connection, and entry) and
// provides add-listener / remove-listener / fire methods for each.  Any model
// code that changes something fires through here; any UI code that cares about
// changes subscribes here.  Nobody talks to nobody directly — it all goes
// through the relay station.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The central event-routing hub for the ldapbrowser.core plugin.
 * Extends {@code ConnectionEventRegistry} (which handles raw connection events)
 * and adds four ldapbrowser-specific channels:
 * <ul>
 *   <li>Search updates — {@link SearchUpdateListener} / {@link SearchUpdateEvent}</li>
 *   <li>Bookmark updates — {@link BookmarkUpdateListener} / {@link BookmarkUpdateEvent}</li>
 *   <li>Browser-connection updates — {@link BrowserConnectionUpdateListener} /
 *       {@link BrowserConnectionUpdateEvent}</li>
 *   <li>Entry updates — {@link EntryUpdateListener} / {@link EntryModificationEvent}</li>
 * </ul>
 * All methods are {@code static} — there is only one registry per JVM.
 * Think of it as the Imperial HoloNet: every model change transmits through
 * here, every UI listener subscribes here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EventRegistry extends ConnectionEventRegistry
{

    static final EventManager<SearchUpdateListener, EventRunner> searchUpdateEventManager = new EventManager<SearchUpdateListener, EventRunner>();


    // ── HoloNet: Open A Channel To Search Bulletins ──────────────────────────────
    // "Relay station to Star Destroyer Chimaera — you are now subscribed to
    //  the Search Updates channel."  The manager maps the listener to its runner
    //  (the thread on which it should receive the broadcast).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link SearchUpdateListener} to receive search update events.
     * The supplied {@link EventRunner} determines which thread the callback runs on
     * — typically the UI thread for viewer-based listeners.
     *
     * <p>For example — subscribing a Searches view:</p>
     * <pre>
     *   EventRegistry.addSearchUpdateListener(this, BrowserCorePlugin.getDefault().getEventRunner());
     * </pre>
     *
     * @param listener the listener to register; must not be {@code null}.
     * @param runner   the runner that will invoke the listener's callback.
     */
    public static void addSearchUpdateListener( SearchUpdateListener listener, EventRunner runner )
    {
        searchUpdateEventManager.addListener( listener, runner );
    }


    // ── HoloNet: Close The Search Bulletins Channel ───────────────────────────────
    // "Relay station — Chimaera is standing down; remove from the distribution
    //  list."  The listener will no longer receive search bulletins.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters a {@link SearchUpdateListener}.
     * Call this when the listening component is closed or disposed, so it no
     * longer receives (and potentially crashes on) stale events.
     *
     * @param listener the listener to remove; silently ignored if not registered.
     */
    public static void removeSearchUpdateListener( SearchUpdateListener listener )
    {
        searchUpdateEventManager.removeListener( listener );
    }


    // ── HoloNet: Broadcast A Search Bulletin To All Subscribers ─────────────────
    // "Attention all stations — search update bulletin inbound."  The relay
    // station wraps the event in an EventRunnable and fans it out to every
    // registered listener on its dedicated runner (thread).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches a {@link SearchUpdateEvent} to all registered
     * {@link SearchUpdateListener}s.  Each listener is invoked on its own
     * registered {@link EventRunner}.
     *
     * <p>For example — fired by SearchManager after adding a search:</p>
     * <pre>
     *   EventRegistry.fireSearchUpdated(
     *       new SearchUpdateEvent(mySearch, EventDetail.SEARCH_ADDED), this);
     * </pre>
     *
     * @param searchUpdateEvent the event to broadcast; must not be {@code null}.
     * @param source            the object that triggered the event (used for loop prevention).
     */
    public static void fireSearchUpdated( final SearchUpdateEvent searchUpdateEvent, final Object source )
    {
        EventRunnableFactory<SearchUpdateListener> factory = new EventRunnableFactory<SearchUpdateListener>()
        {
            public EventRunnable createEventRunnable( final SearchUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.searchUpdated( searchUpdateEvent );
                    }
                };
            }
        };
        searchUpdateEventManager.fire( factory );
    }

    static final EventManager<BookmarkUpdateListener, EventRunner> bookmarkUpdateEventManager = new EventManager<BookmarkUpdateListener, EventRunner>();


    // ── HoloNet: Open A Channel To Bookmark Bulletins ────────────────────────────
    // "Adding you to the Bookmark Updates distribution list."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link BookmarkUpdateListener} to receive bookmark update events.
     * The supplied {@link EventRunner} controls the dispatch thread.
     *
     * @param listener the listener to register; must not be {@code null}.
     * @param runner   the runner that will invoke the listener's callback.
     */
    public static void addBookmarkUpdateListener( BookmarkUpdateListener listener, EventRunner runner )
    {
        bookmarkUpdateEventManager.addListener( listener, runner );
    }


    // ── HoloNet: Close The Bookmark Bulletins Channel ────────────────────────────
    // "Removing you from the Bookmark Updates distribution list."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters a {@link BookmarkUpdateListener}.
     * Call this when the listening component is disposed.
     *
     * @param listener the listener to remove; silently ignored if not registered.
     */
    public static void removeBookmarkUpdateListener( BookmarkUpdateListener listener )
    {
        bookmarkUpdateEventManager.removeListener( listener );
    }


    // ── HoloNet: Broadcast A Bookmark Bulletin To All Subscribers ───────────────
    // "Attention all stations — bookmark update bulletin inbound."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches a {@link BookmarkUpdateEvent} to all registered
     * {@link BookmarkUpdateListener}s.
     *
     * <p>For example — fired by BookmarkManager after removing a bookmark:</p>
     * <pre>
     *   EventRegistry.fireBookmarkUpdated(
     *       new BookmarkUpdateEvent(bm, Detail.BOOKMARK_REMOVED), this);
     * </pre>
     *
     * @param bookmarkUpdateEvent the event to broadcast; must not be {@code null}.
     * @param source              the object that triggered the event.
     */
    public static void fireBookmarkUpdated( final BookmarkUpdateEvent bookmarkUpdateEvent, final Object source )
    {
        EventRunnableFactory<BookmarkUpdateListener> factory = new EventRunnableFactory<BookmarkUpdateListener>()
        {
            public EventRunnable createEventRunnable( final BookmarkUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.bookmarkUpdated( bookmarkUpdateEvent );
                    }
                };
            }
        };
        bookmarkUpdateEventManager.fire( factory );
    }

    static final EventManager<BrowserConnectionUpdateListener, EventRunner> browserConnectionUpdateEventManager = new EventManager<BrowserConnectionUpdateListener, EventRunner>();


    // ── HoloNet: Open A Channel To Connection-State Bulletins ────────────────────
    // "Adding you to the Browser Connection Updates distribution list."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a {@link BrowserConnectionUpdateListener} to receive browser
     * connection lifecycle events (opened, closed, schema updated).
     *
     * @param listener the listener to register; must not be {@code null}.
     * @param runner   the runner that will invoke the listener's callback.
     */
    public static void addBrowserConnectionUpdateListener( BrowserConnectionUpdateListener listener, EventRunner runner )
    {
        browserConnectionUpdateEventManager.addListener( listener, runner );
    }


    // ── HoloNet: Close The Connection-State Bulletins Channel ────────────────────
    // "Removing you from the Browser Connection Updates distribution list."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters a {@link BrowserConnectionUpdateListener}.
     * Call this when the listening component is disposed.
     *
     * @param listener the listener to remove; silently ignored if not registered.
     */
    public static void removeBrowserConnectionUpdateListener( BrowserConnectionUpdateListener listener )
    {
        browserConnectionUpdateEventManager.removeListener( listener );
    }


    // ── HoloNet: Broadcast A Connection-State Bulletin ───────────────────────────
    // "All stations — browser connection status update inbound."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches a {@link BrowserConnectionUpdateEvent} to all registered
     * {@link BrowserConnectionUpdateListener}s.
     *
     * <p>For example — fired by BrowserConnectionListener after schema loads:</p>
     * <pre>
     *   EventRegistry.fireBrowserConnectionUpdated(
     *       new BrowserConnectionUpdateEvent(bc, Detail.SCHEMA_UPDATED), this);
     * </pre>
     *
     * @param browserConnectionUpdateEvent the event to broadcast; must not be {@code null}.
     * @param source                       the object that triggered the event.
     */
    public static void fireBrowserConnectionUpdated( final BrowserConnectionUpdateEvent browserConnectionUpdateEvent,
        final Object source )
    {
        EventRunnableFactory<BrowserConnectionUpdateListener> factory = new EventRunnableFactory<BrowserConnectionUpdateListener>()
        {
            public EventRunnable createEventRunnable( final BrowserConnectionUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.browserConnectionUpdated( browserConnectionUpdateEvent );
                    }
                };
            }
        };
        browserConnectionUpdateEventManager.fire( factory );
    }

    static final EventManager<EntryUpdateListener, EventRunner> entryUpdateEventManager = new EventManager<EntryUpdateListener, EventRunner>();


    // ── HoloNet: Open A Channel To Entry Modification Bulletins ─────────────────
    // "Adding you to the Entry Updates distribution list — you'll hear about
    //  every attribute add, delete, rename, move, and value change."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers an {@link EntryUpdateListener} to receive all
     * {@link EntryModificationEvent} subtypes (attribute changes, entry
     * added/deleted/renamed/moved, value changes, bulk modifications, etc.).
     *
     * @param listener the listener to register; must not be {@code null}.
     * @param runner   the runner that will invoke the listener's callback.
     */
    public static void addEntryUpdateListener( EntryUpdateListener listener, EventRunner runner )
    {
        entryUpdateEventManager.addListener( listener, runner );
    }


    // ── HoloNet: Close The Entry Modification Bulletins Channel ─────────────────
    // "Removing you from the Entry Updates distribution list."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters an {@link EntryUpdateListener}.
     * Call this when the listening component (e.g. an attribute editor view)
     * is closed or disposed.
     *
     * @param listener the listener to remove; silently ignored if not registered.
     */
    public static void removeEntryUpdateListener( EntryUpdateListener listener )
    {
        entryUpdateEventManager.removeListener( listener );
    }


    // ── HoloNet: Broadcast An Entry Modification Bulletin ───────────────────────
    // "All stations — entry update bulletin inbound.  Sender: [source]."
    // We stamp the source onto the event before broadcasting so listeners can
    // check whether they themselves fired it and skip the update if so.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Dispatches an {@link EntryModificationEvent} to all registered
     * {@link EntryUpdateListener}s.
     * The {@code source} is stamped onto the event before dispatch; listeners
     * can check {@link EntryModificationEvent#getSource()} to skip processing
     * events they themselves caused.
     *
     * <p>For example — fired after a background attribute-load job finishes:</p>
     * <pre>
     *   EventRegistry.fireEntryUpdated(
     *       new AttributesInitializedEvent(loadedEntry), this);
     * </pre>
     *
     * @param entryUpdateEvent the event to broadcast; must not be {@code null}.
     * @param source           the object that triggered the event (stamped onto the event).
     */
    public static void fireEntryUpdated( final EntryModificationEvent entryUpdateEvent, final Object source )
    {
        entryUpdateEvent.setSource( source );
        EventRunnableFactory<EntryUpdateListener> factory = new EventRunnableFactory<EntryUpdateListener>()
        {
            public EventRunnable createEventRunnable( final EntryUpdateListener listener )
            {
                return new EventRunnable()
                {
                    public void run()
                    {
                        listener.entryUpdated( entryUpdateEvent );
                    }
                };
            }


            public String toString()
            {
                return "EventRunnableFactory [entryUpdateEvent=" + entryUpdateEvent + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        entryUpdateEventManager.fire( factory );
    }

}
