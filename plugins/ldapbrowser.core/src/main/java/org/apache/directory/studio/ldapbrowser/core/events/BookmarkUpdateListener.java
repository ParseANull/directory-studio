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


import java.util.EventListener;

import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;


// ── CLASS: BookmarkUpdateListener — LEIA AWAITING THE DEATH STAR PLANS ──────
// Princess Leia is on her ship, comlink open, waiting for R2-D2's transmission
// of the Death Star plans.  The moment it arrives she acts — reviewing the
// data, deciding what to do next.  She doesn't poll the channel; she just
// keeps the channel open and responds the instant the signal comes in.
// This interface is that open channel: any object that wants to know when a
// bookmark changes implements it, registers with the EventRegistry, and its
// {@link #bookmarkUpdated} method is called the moment a change fires.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for receiving {@link BookmarkUpdateEvent}s.
 * Implement this interface and register with
 * {@link EventRegistry#addBookmarkUpdateListener} to be notified whenever an
 * {@link IBookmark} is added, updated, or removed from the
 * {@link org.apache.directory.studio.ldapbrowser.core.BookmarkManager}.
 * The callback is delivered on the thread managed by the registered
 * {@link org.apache.directory.studio.connection.core.event.EventRunner}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface BookmarkUpdateListener extends EventListener
{
    // ── Leia Receives The Incoming Transmission ───────────────────────────────────
    // The comlink crackles and R2-D2's data burst arrives.  Leia immediately
    // processes it: what changed, is it complete, what action does she take?
    // Every implementing class decides its own response — the Bookmarks view
    // refreshes a row, another listener persists the change to disk.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event framework when a bookmark has been added, updated,
     * or removed.  Inspect {@link BookmarkUpdateEvent#getDetail()} to find out
     * what happened, and {@link BookmarkUpdateEvent#getBookmark()} for the
     * affected bookmark.
     *
     * <p>For example — a Bookmarks view updating itself:</p>
     * <pre>
     *   public void bookmarkUpdated(BookmarkUpdateEvent event) {
     *     switch (event.getDetail()) {
     *       case BOOKMARK_ADDED:   viewer.add(event.getBookmark());    break;
     *       case BOOKMARK_UPDATED: viewer.refresh(event.getBookmark()); break;
     *       case BOOKMARK_REMOVED: viewer.remove(event.getBookmark()); break;
     *     }
     *   }
     * </pre>
     *
     * @param bookmarkUpdateEvent the event carrying the affected bookmark and
     *                            the detail of what changed.
     */
    void bookmarkUpdated( BookmarkUpdateEvent bookmarkUpdateEvent );
}
