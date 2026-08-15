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
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ICompareableEntry;


// ── CLASS: BookmarkEntry — LANDO'S PLACEHOLDER ENTRY FOR A WAYPOINT TARGET ───
// When Lando has a waypoint in his navicomp, he doesn't always have a real
// entry object loaded — he just has coordinates (DN).  BookmarkEntry is a
// thin delegate entry whose only job is to give the bookmark a browseable
// IEntry proxy so the UI can render it like a real entry node without requiring
// a full load from the directory server.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the target entry of a {@link Bookmark}.
 * Extends {@link DelegateEntry} to provide a lightweight {@link IEntry}
 * proxy for the bookmarked DN without requiring a directory load.
 * Equality is based on DN + connection, matching the convention used
 * across the entry model.
 *
 * <p>Think of this as Lando's placeholder entry for a waypoint target —
 * just the coordinates without the full crew manifest.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BookmarkEntry extends DelegateEntry implements ICompareableEntry
{

    private static final long serialVersionUID = -6351277968774226912L;


    // ── No-Arg Constructor For Serialisation ─────────────────────────────────────
    protected BookmarkEntry()
    {
    }


    // ── Lando Creates A Placeholder Entry For A Bookmarked DN ────────────────────
    /**
     * Creates a new instance of BookmarkEntry.
     *
     * @param connection the browser connection the bookmark belongs to
     * @param dn the Dn of the bookmark target
     */
    public BookmarkEntry( IBrowserConnection connection, Dn dn )
    {
        super( connection, dn );
    }


    // ── Hash Code Comes From The DN ───────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return getDn().hashCode();
    }


    // ── Mace Windu Checks: Same DN And Connection? ────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        // check argument
        if (!( o instanceof ICompareableEntry ) )
        {
            return false;
        }
        ICompareableEntry e = ( ICompareableEntry ) o;

        // compare dn and connection
        return getDn() == null ? e.getDn() == null : ( getDn().equals( e.getDn() ) && getBrowserConnection().equals(
            e.getBrowserConnection() ) );
    }
}
