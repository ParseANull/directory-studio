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

package org.apache.directory.studio.ldapbrowser.core.jobs;


import java.util.List;

import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;


// ── CLASS: ReadEntryRunnable — R2-D2 LOOKS UP A SPECIFIC BLUEPRINTZ LOCATION ─
// R2-D2 needs to retrieve a specific technical schematic from the Death Star's
// database.  He knows the exact co-ordinate (DN) of the blueprint.  First he
// checks his own memory banks — maybe he already downloaded it.  If not, he
// plugs into the terminal and fetches it directly.
// This runnable does exactly that for a single LDAP entry: check the in-memory
// cache first (fast, no network call), and if the entry isn't there, issue a
// SCOPE_OBJECT search against the LDAP server to read it.  The result is
// stored and retrievable via {@link #getReadEntry()}.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that reads a single LDAP entry by its DN.
 * We first check the connection's in-memory entry cache; if the entry is
 * already there we return it immediately without hitting the network.  If not,
 * we issue a base-scope (SCOPE_OBJECT) LDAP search and cache the result.
 * After the job completes, call {@link #getReadEntry()} to obtain the entry.
 * Think of it as R2-D2 checking his own circuits before plugging into the
 * Death Star terminal.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ReadEntryRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The Dn of the entry. */
    private Dn dn;

    /** The entry read from directory. */
    private IEntry readEntry;


    // ── R2-D2 Prepares The Blueprint Retrieval Mission ────────────────────────────
    // The droid notes which connection to use and which DN to look up.
    // The result slot starts null — populated by {@link #run}.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ReadEntryRunnable.
     * Call {@link #getReadEntry()} after the job finishes to get the result.
     *
     * <p>For example — reading a specific user entry:</p>
     * <pre>
     *   Dn dn = new Dn("cn=Luke Skywalker,ou=Rebels,dc=example,dc=com");
     *   ReadEntryRunnable r = new ReadEntryRunnable(conn, dn);
     *   new StudioBrowserJob(r).execute();
     * </pre>
     *
     * @param browserConnection the connection to query.
     * @param dn                the distinguished name of the entry to read.
     */
    public ReadEntryRunnable( IBrowserConnection browserConnection, Dn dn )
    {
        this.browserConnection = browserConnection;
        this.dn = dn;
        this.readEntry = null;
    }


    // ── R2-D2 Plugs Into The Right Terminal ──────────────────────────────────────
    // The droid needs to identify which LDAP server socket to connect to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw connection used for the LDAP search.
     *
     * @return single-element array with the underlying {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── The Mission Name For The Progress Bar ─────────────────────────────────────
    // "R2-D2 is reading entry..." shown in the Eclipse progress UI.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Read entry" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__read_entry_name;
    }


    // ── R2-D2 Locks The Connection During Retrieval ───────────────────────────────
    // No other job should poke the connection while the droid is reading — they
    // might overwrite his half-fetched result.  We lock the browser connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be exclusively locked while this job runs.
     *
     * @return the browser connection.
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { browserConnection };
    }


    // ── The Error Report If The Retrieval Fails ───────────────────────────────────
    // "R2-D2 couldn't find the blueprint at that location."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message if the entry read fails.
     *
     * @return a localised "Could not read entry" error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__read_entry_error;
    }


    // ── R2-D2 Checks His Memory Banks, Then Plugs Into The Terminal ──────────────
    // First try the cache (instant).  If not found, issue a base-scope search
    // to fetch the entry from the server and populate the model.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reads the entry.  Checks the cache first; if the entry is absent, issues
     * an LDAP SCOPE_OBJECT search to retrieve it from the directory.
     *
     * @param pm the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor pm )
    {
        readEntry = browserConnection.getEntryFromCache( dn );
        if ( readEntry == null )
        {
            pm.beginTask( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__read_entry_task, new String[]
                { dn.toString() } ), 2 );
            pm.reportProgress( " " ); //$NON-NLS-1$
            pm.worked( 1 );

            readEntry = getEntry( browserConnection, dn, null, pm );
        }
    }


    // ── No Event Needed — The Result Is In The Return Value ──────────────────────
    // R2-D2 doesn't broadcast his findings; the caller retrieves them directly
    // via {@link #getReadEntry()}.  No model change event needed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — this runnable does not fire model-change events.
     * The caller retrieves the entry via {@link #getReadEntry()}.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
    }


    // ── R2-D2's Cache-First Lookup Helper ─────────────────────────────────────────
    // Package-visible helper also used by other runnables.  "Check memory first,
    // then plug into the terminal."  Issued as a SCOPE_OBJECT (base) search
    // so we only fetch the exact entry at the given DN.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Retrieves a single entry by DN — from cache if available, otherwise from
     * the directory via a SCOPE_OBJECT search.  Used by other runnables as a
     * shared helper.
     *
     * <p>For example — verifying an entry exists before a copy:</p>
     * <pre>
     *   IEntry target = ReadEntryRunnable.getEntry(conn, targetDn, null, monitor);
     * </pre>
     *
     * @param browserConnection the connection to query.
     * @param dn                the DN of the entry.
     * @param controls          optional LDAP controls (may be {@code null}).
     * @param monitor           the progress monitor.
     * @return the {@link IEntry}, or {@code null} if not found or on error.
     */
    static IEntry getEntry( IBrowserConnection browserConnection, Dn dn, List<Control> controls,
        StudioProgressMonitor monitor )
    {
        try
        {
            // first check cache
            IEntry entry = browserConnection.getEntryFromCache( dn );
            if ( entry != null )
            {
                return entry;
            }

            // search in directory
            ISearch search = new Search( null, browserConnection, dn, null, ISearch.NO_ATTRIBUTES, SearchScope.OBJECT,
                1, 0, AliasDereferencingMethod.NEVER, ReferralHandlingMethod.IGNORE, true, controls, false );
            SearchRunnable.searchAndUpdateModel( browserConnection, search, monitor );
            ISearchResult[] srs = search.getSearchResults();
            if ( srs.length > 0 )
            {
                return srs[0].getEntry();
            }
            else
            {
                monitor.reportError( BrowserCoreMessages.bind( BrowserCoreMessages.model__no_such_entry, dn ) );
                return null;
            }
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
            return null;
        }
    }


    // ── R2-D2 Hands Over The Blueprint He Retrieved ───────────────────────────────
    // The droid extends his holo-projector arm: "Here's what I found."
    // Callers access the result after the job completes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry that was read during {@link #run}.
     * Call this only after the job has completed.
     *
     * @return the {@link IEntry}, or {@code null} if the entry was not found
     *         or the job failed.
     */
    public IEntry getReadEntry()
    {
        return readEntry;
    }
}
