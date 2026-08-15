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


// ── CLASS: IQuickSearch — R2-D2'S RAPID DIRECTORY SCAN ───────────────────────
// When the Millennium Falcon needs a fast scan of the Death Star's systems,
// R2-D2 doesn't run a full deep-search (that takes minutes).  He does a rapid
// scan: start from the current corridor (the selected entry), fan out a few
// levels, and report back in seconds.  The key extra piece of information vs.
// a full ISearch is the "current corridor" — the tree node R2 starts from.
// IQuickSearch is that rapid scan: an {@link ISearch} that also knows which
// specific tree entry is its start point (the search base entry, not just the
// search base DN).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Extends {@link ISearch} to represent the instant-search bar at the top of the
 * LDAP Browser view.
 * A quick search is a lightweight, on-the-fly query anchored to a specific tree
 * entry rather than a bare DN.  Knowing the actual {@link IEntry} (not just its DN)
 * lets the browser refresh the right tree node when the search completes.
 * Think of this as R2-D2's rapid scan: same parameters as a full search, but
 * rooted to a concrete, already-loaded entry in the current view.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IQuickSearch extends ISearch
{
    // ── R2-D2 Reports Which Corridor He's Starting From ──────────────────────────
    // "Scanning from section 7G, turbolaser bay."  The entry, not just the DN, so
    // the tree view can refresh the right node when results land.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tree entry that serves as the base for this quick search.
     * This is the concrete {@link IEntry} object — not just a DN — so the
     * browser can navigate to it and refresh its display after the search.
     *
     * @return the search base entry; never {@code null}.
     */
    IEntry getSearchBaseEntry();
}
