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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DirectoryMetadataEntry;


// ── CLASS: FetchBaseDNsRunnable — REBEL SCOUTS MAP ALL KNOWN BASE LOCATIONS ──
// Before the Battle of Endor, Rebel scouts criss-cross the galaxy finding every
// Rebel base location (namingContext) registered on the central relay.
// "Echo Base — confirmed. Dantooine — confirmed. D'Qar — confirmed."
// Each base DN is a top-level entry under the RootDSE, like a separate galaxy
// sector that the browser can navigate into.  This runnable loads the RootDSE
// and collects all those top-level base DNs so that, for example, the connection
// wizard can present a drop-down of valid starting points.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that fetches the base DNs available on a directory server.
 * A "base DN" in LDAP is a top-level naming context — the root of a subtree.
 * For example, a server might have {@code dc=example,dc=com} and
 * {@code dc=test,dc=com} as separate naming contexts.
 * This runnable loads the RootDSE (which lists the {@code namingContexts}
 * attribute) and collects those DNs into a list that callers can query.
 * Think of it as the Rebel scouts mapping every known base location from the
 * central HoloNet relay.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FetchBaseDNsRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The connection */
    private IBrowserConnection connection;

    /** The base DNs*/
    private List<String> baseDNs;


    // ── The Scout Team Gets Their Target Connection ───────────────────────────────
    // "You're scouting the Coruscant relay — here's your comms channel."
    // We record which LDAP server to scan and initialise the result list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FetchBaseDNsRunnable for the given connection.
     * The discovered base DNs are accumulated in an internal list; retrieve
     * them with {@link #getBaseDNs()} after the job completes.
     *
     * <p>For example — used in the New Connection wizard to populate a combo:</p>
     * <pre>
     *   FetchBaseDNsRunnable runnable = new FetchBaseDNsRunnable(conn);
     *   new StudioBrowserJob(runnable).execute();
     *   // ... after job completes:
     *   List&lt;String&gt; dns = runnable.getBaseDNs();
     * </pre>
     *
     * @param connection the browser connection to query.
     */
    public FetchBaseDNsRunnable( IBrowserConnection connection )
    {
        this.connection = connection;
        this.baseDNs = new ArrayList<String>();
    }


    // ── The Scout Doesn't Need A Dedicated Comlink ────────────────────────────────
    // Scout teams share the general comms network; they don't need a dedicated
    // channel assigned.  Returning null tells the job framework to use whatever
    // connection the runnable itself manages.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this runnable manages its own connection reference
     * internally via the {@link IBrowserConnection}.
     *
     * @return {@code null}.
     */
    public Connection[] getConnections()
    {
        return null;
    }


    // ── The Mission Briefing Title ────────────────────────────────────────────────
    // "Scout mission: enumerate all base locations."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job, shown in the Eclipse
     * progress view.
     *
     * @return a localised "Fetch base DNs" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__fetch_basedns_name;
    }


    // ── The Scout Locks The Connection During The Mission ────────────────────────
    // The scouts lock the relay channel so no other team uses it concurrently
    // and garbles the signal.  We lock the underlying Connection object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be locked (not concurrently accessed) while
     * this job runs.  We lock the underlying {@link Connection} to prevent
     * another job from reading/writing the same server simultaneously.
     *
     * @return an array containing the underlying connection.
     */
    public Object[] getLockedObjects()
    {
        return new Connection[]
            { connection.getConnection() };
    }


    // ── The Error Debrief Title ───────────────────────────────────────────────────
    // "Scout report: mission failed — base DN fetch error."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message displayed to the user if this job fails.
     *
     * @return a localised "Could not fetch base DNs" error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__fetch_basedns_error;
    }


    // ── The Scouts Execute The Mission ────────────────────────────────────────────
    // The team loads the RootDSE (the relay manifest), reads the children that
    // represent real base DNs (not schema meta-entries), and logs each one.
    // Any DirectoryMetadataEntry (schema sub-entry) is skipped — it's not a
    // navigable base DN, just internal server plumbing.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the base-DN fetch.
     * We load the RootDSE (which populates its {@code namingContexts} children),
     * then collect the DN of every child that is NOT a
     * {@link DirectoryMetadataEntry} (schema sub-entries and similar internal
     * entries are excluded).
     *
     * <p>For example — what happens under the hood:</p>
     * <pre>
     *   InitializeRootDSERunnable.loadRootDSE(connection, monitor);
     *   for (IEntry child : rootDSE.getChildren()) {
     *     if (!(child instanceof DirectoryMetadataEntry)) {
     *       baseDNs.add(child.getDn().getName());
     *     }
     *   }
     * </pre>
     *
     * @param monitor the Eclipse progress monitor; check {@code isCanceled()} for cancellation.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.jobs__fetch_basedns_task, 5 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        InitializeRootDSERunnable.loadRootDSE( connection, monitor );

        IEntry[] baseDNEntries = connection.getRootDSE().getChildren();
        if ( baseDNEntries != null )
        {
            for ( IEntry baseDNEntry : baseDNEntries )
            {
                if ( !( baseDNEntry instanceof DirectoryMetadataEntry ) )
                {
                    baseDNs.add( baseDNEntry.getDn().getName() );
                }
            }
        }

        monitor.worked( 1 );
    }


    // ── The Scouts File A No-Op After-Action Report ───────────────────────────────
    // The scouts' job is just to collect the list — they don't need to fire
    // any model change events afterward.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — this runnable does not fire any model change events.
     * The caller retrieves results via {@link #getBaseDNs()}.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
    }


    // ── The Scouts Hand Over Their Map ────────────────────────────────────────────
    // "Here's every base location we found."  The caller uses this list to
    // populate a combo box, validate a user-entered base DN, or set connection
    // properties.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of base DN strings discovered during {@link #run}.
     * Call this after the job has completed.
     *
     * <p>For example:</p>
     * <pre>
     *   String first = runnable.getBaseDNs().get(0); // e.g. "dc=example,dc=com"
     * </pre>
     *
     * @return a mutable list of base DN strings; never {@code null}, but may be
     *         empty if the server exposes no naming contexts.
     */
    public List<String> getBaseDNs()
    {
        return baseDNs;
    }
}
