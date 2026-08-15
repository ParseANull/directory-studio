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

import org.apache.directory.studio.ldapbrowser.core.model.ISearch;


// ── CLASS: SearchUpdateListener — LEIA DECIPHERS R2'S SEARCH RESULTS ────────
// Princess Leia is watching R2-D2's holographic projector.  Whenever R2
// updates his "search" status — "new mission filed", "search completed",
// "mission parameters changed" — Leia responds immediately: she reads the
// bulletin, updates the tactical board, and tells the Alliance what to do next.
// She doesn't pull data from R2; R2 pushes it to her the moment it changes.
// This interface is Leia's receiving role: any class that needs to react to
// changes in a saved {@link ISearch} implements this and registers with
// {@link EventRegistry#addSearchUpdateListener}.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for receiving {@link SearchUpdateEvent}s.
 * Implement this and register with {@link EventRegistry#addSearchUpdateListener}
 * to be notified whenever an {@link ISearch} is added, removed, performed,
 * renamed, or has its parameters updated.
 * The callback arrives on the thread managed by the registered
 * {@link org.apache.directory.studio.connection.core.event.EventRunner}.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface SearchUpdateListener extends EventListener
{
    // ── Leia Receives R2's Latest Search Bulletin ────────────────────────────────
    // R2 flickers — a new status update from the search droid network.
    // Leia reads the detail code and the mission file, then decides whether to
    // add a row to the Searches view, refresh its results panel, or remove it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Called by the event framework when a search's lifecycle or parameters change.
     * Check {@link SearchUpdateEvent#getDetail()} to see what happened and
     * {@link SearchUpdateEvent#getSearch()} for the affected search.
     *
     * <p>For example — a Searches view responding to events:</p>
     * <pre>
     *   public void searchUpdated(SearchUpdateEvent event) {
     *     switch (event.getDetail()) {
     *       case SEARCH_ADDED:    viewer.add(event.getSearch());     break;
     *       case SEARCH_REMOVED:  viewer.remove(event.getSearch());  break;
     *       case SEARCH_PERFORMED:
     *       case SEARCH_PARAMETER_UPDATED:
     *       case SEARCH_RENAMED:  viewer.refresh(event.getSearch()); break;
     *     }
     *   }
     * </pre>
     *
     * @param searchUpdateEvent the event carrying the affected search and the
     *                          detail of what changed.
     */
    void searchUpdated( SearchUpdateEvent searchUpdateEvent );
}
