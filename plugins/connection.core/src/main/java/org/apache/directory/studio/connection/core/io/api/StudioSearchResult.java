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
package org.apache.directory.studio.connection.core.io.api;


import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.studio.connection.core.Connection;


// ── CLASS: StudioSearchResult — R2'S SENSOR RETURN PACKET ────────────────────
// After R2 scans a sector, each hit comes back as a little packet: the entry
// that was found, which ship (connection) found it, whether it came from a
// follow-on referral scan, and the referral URL to use if we're handing the
// referral back to the caller.
// This class is that packet.  It bundles the Apache Directory API
// SearchResultEntry with metadata we add for our own tracking.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value object wrapping a single LDAP search result entry together with
 * Studio-specific metadata.
 * The raw {@link SearchResultEntry} from the Apache Directory API carries the
 * entry DN and attributes.  We augment it with:
 * <ul>
 *   <li>The {@link Connection} that produced this result (useful when following referrals,
 *       where the result may come from a different server than the original request).</li>
 *   <li>{@code isContinuedSearchResult} — {@code true} if this entry arrived via an
 *       automatically followed referral rather than the original search.</li>
 *   <li>{@code searchContinuationUrl} — the referral URL, set when we're surfacing referrals
 *       to the caller rather than following them automatically.</li>
 * </ul>
 * Think of this as R2's sensor return packet: it includes not just the raw data
 * but also provenance information about where the data came from.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioSearchResult
{

    /** The raw Apache Directory API search result entry carrying the DN and attributes. */
    private final SearchResultEntry searchResultEntry;

    /** The connection that produced this search result. */
    private Connection connection;

    /**
     * {@code true} if this result arrived via an automatically followed referral
     * rather than directly from the original search target.
     */
    private final boolean isContinuedSearchResult;

    /**
     * The referral URL to present to the caller when referral handling is set to
     * FOLLOW_MANUALLY; {@code null} otherwise.
     */
    private final LdapUrl searchContinuationUrl;


    // ── CONSTRUCTOR — PACKAGE THE SENSOR RETURN ───────────────────────────────────
    // We store all four fields immutably (except connection, which can be re-set
    // when following a referral opens a different connection).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link StudioSearchResult} wrapping the given raw entry.
     *
     * @param searchResultEntry       The Apache Directory API search result entry.
     * @param connection              The connection that produced this result.
     * @param isContinuedSearchResult {@code true} if this came from an auto-followed referral.
     * @param searchContinuationUrl   The referral URL for manual-follow mode; {@code null}
     *                                for directly retrieved results.
     */
    public StudioSearchResult( SearchResultEntry searchResultEntry, Connection connection,
        boolean isContinuedSearchResult, LdapUrl searchContinuationUrl )
    {
        this.searchResultEntry = searchResultEntry;
        this.connection = connection;
        this.isContinuedSearchResult = isContinuedSearchResult;
        this.searchContinuationUrl = searchContinuationUrl;
    }


    // ── GET SEARCH RESULT ENTRY — RETRIEVE THE RAW PACKET ─────────────────────────
    // Package-private access so StudioSearchResultEnumeration can use it internally.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link SearchResultEntry} from the Apache Directory API.
     * Package-private — external callers should use {@link #getEntry()} or {@link #getDn()}.
     *
     * @return  The raw {@link SearchResultEntry}.
     */
    SearchResultEntry getSearchResultEntry()
    {
        return searchResultEntry;
    }


    // ── GET DN — RETURN THE ENTRY'S COORDINATES ───────────────────────────────────
    // R2 reports the target's exact coordinates (the DN).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Distinguished Name of the returned entry.
     *
     * @return  The entry {@link Dn}.
     */
    public Dn getDn()
    {
        return getEntry().getDn();
    }


    // ── GET ENTRY — RETURN THE FULL SCANNED ENTRY ─────────────────────────────────
    // R2 hands over the full scan result: DN plus all attributes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP {@link Entry} (DN and attributes) from the search result.
     *
     * @return  The {@link Entry}.
     */
    public Entry getEntry()
    {
        return searchResultEntry.getEntry();
    }


    // ── GET / SET CONNECTION — WHICH SHIP FOUND THIS ─────────────────────────────
    // When following referrals, the result may come from a different connection
    // than the one that started the search.  We allow setting the connection
    // after construction for that reason.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Connection} that produced this search result.
     *
     * @return  The source connection.
     */
    public Connection getConnection()
    {
        return connection;
    }


    /**
     * Overrides the source connection.
     * Used when a referral is followed and the result comes from a different connection.
     *
     * @param connection  The new source connection.
     */
    public void setConnection( Connection connection )
    {
        this.connection = connection;
    }


    // ── IS CONTINUED SEARCH RESULT — DID R2 FOLLOW A REFERRAL? ───────────────────
    // Returns true if this result came from an automatically followed referral.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this result arrived via an automatically followed referral
     * rather than directly from the original search.
     *
     * @return  {@code true} for a referral-continued result.
     */
    public boolean isContinuedSearchResult()
    {
        return isContinuedSearchResult;
    }


    // ── GET SEARCH CONTINUATION URL — THE REFERRAL ADDRESS ───────────────────────
    // If the caller handles referrals manually, we return the referral URL here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the referral URL that the caller should follow next, or {@code null}
     * if this is not a manual-referral placeholder result.
     *
     * @return  The {@link LdapUrl} for the next hop, or {@code null}.
     */
    public LdapUrl getSearchContinuationUrl()
    {
        return searchContinuationUrl;
    }

}
