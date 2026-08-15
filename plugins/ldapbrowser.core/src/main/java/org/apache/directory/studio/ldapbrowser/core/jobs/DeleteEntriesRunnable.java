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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.Connection.AliasDereferencingMethod;
import org.apache.directory.studio.connection.core.Connection.ReferralHandlingMethod;
import org.apache.directory.studio.connection.core.StudioControl;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.connection.core.io.api.StudioSearchResultEnumeration;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.utils.JNDIUtils;


// ── CLASS: DeleteEntriesRunnable — THE DEATH STAR DESTROYS TARGETS ONE BY ONE ─
// Grand Moff Tarkin aims the Death Star's superlaser at Alderaan.  First he
// tries to destroy the planet in one shot.  If the planet has a planetary
// shield (error 66 — the entry has children), he switches to a sustained barrage:
// finds every moon and outpost (child entries), destroys those first, then
// fires the killing blow.  Lather, rinse, repeat — and the TreeDelete control
// lets him blow up the entire system in one shot when the server supports it.
// This runnable deletes LDAP entries.  It uses an optimistic strategy: try the
// simple delete first; on LDAP error 66 (NotAllowedOnNonLeaf) do a ONELEVEL
// search and recursively delete children first.  When done it fires a
// {@link BulkModificationEvent} and per-search {@link SearchUpdateEvent}s.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that deletes one or more LDAP entries, recursively if
 * necessary.  We use an optimistic strategy:
 * <ol>
 *   <li>Attempt to delete the entry directly.</li>
 *   <li>If the server returns error 66 (NotAllowedOnNonLeaf — entry has children),
 *       do an ONELEVEL search to find the children, recurse into each, then
 *       retry the top-level delete.</li>
 * </ol>
 * Optionally uses the {@code TreeDelete} LDAP control when the server supports
 * it (deletes an entire subtree in one operation).  After deletion, updates the
 * browser model (removes from parent, from search results, from cache) and fires
 * a {@link BulkModificationEvent} plus per-search {@link SearchUpdateEvent}s.
 * Think of it as the Death Star's optimistic planet-destruction sequence.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteEntriesRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The entries to delete. */
    private Collection<IEntry> entriesToDelete;

    /** The deleted entries. */
    private Set<IEntry> deletedEntriesSet;

    /** The searches to update. */
    private Set<ISearch> searchesToUpdateSet;

    /** The use tree delete control flag. */
    private boolean useTreeDeleteControl;


    // ── Grand Moff Tarkin Orders The Strike ───────────────────────────────────
    // Stores the list of targets and whether to use the TreeDelete control.
    // Initialises internal tracking sets for deleted entries and affected searches.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DeleteEntriesRunnable.
     *
     * <p>For example — deleting two entries:</p>
     * <pre>
     *   List&lt;IEntry&gt; targets = Arrays.asList(entryA, entryB);
     *   new StudioBrowserJob(new DeleteEntriesRunnable(targets, false)).execute();
     * </pre>
     *
     * @param entriesToDelete    the entries to delete.
     * @param useTreeDeleteControl if {@code true} and the server supports the
     *                           Tree Delete control, delete entire subtrees in
     *                           one LDAP operation rather than recursing.
     */
    public DeleteEntriesRunnable( final Collection<IEntry> entriesToDelete, boolean useTreeDeleteControl )
    {
        this.entriesToDelete = entriesToDelete;
        this.useTreeDeleteControl = useTreeDeleteControl;

        this.deletedEntriesSet = new HashSet<IEntry>();
        this.searchesToUpdateSet = new HashSet<ISearch>();
    }


    // ── One Connection Per Target ─────────────────────────────────────────────
    // Entries may come from different servers (multi-server browser session).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns one connection per entry to delete.
     *
     * @return array of {@link Connection} objects.
     */
    public Connection[] getConnections()
    {
        Connection[] connections = new Connection[entriesToDelete.size()];
        int i = 0;
        for ( IEntry entry : entriesToDelete )
        {
            connections[i] = entry.getBrowserConnection().getConnection();
            i++;
        }
        return connections;
    }


    // ── The Mission Name In The Progress Bar ──────────────────────────────────
    // "Delete entry" (singular) or "Delete entries" (plural).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Delete entry" or "Delete entries" label.
     */
    public String getName()
    {
        return entriesToDelete.size() == 1 ? BrowserCoreMessages.jobs__delete_entries_name_1
            : BrowserCoreMessages.jobs__delete_entries_name_n;
    }


    // ── Lock Targets During The Strike ────────────────────────────────────────
    // Prevents a parallel operation from modifying an entry we're deleting.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns all entries to delete as locked objects.
     *
     * @return the entries.
     */
    public Object[] getLockedObjects()
    {
        List<IEntry> l = new ArrayList<IEntry>();
        l.addAll( entriesToDelete );
        return l.toArray();
    }


    // ── If The Strike Fails ────────────────────────────────────────────────────
    // Singular vs plural error message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown if deletion fails.
     *
     * @return a localised error string.
     */
    public String getErrorMessage()
    {
        return entriesToDelete.size() == 1 ? BrowserCoreMessages.jobs__delete_entries_error_1
            : BrowserCoreMessages.jobs__delete_entries_error_n;
    }


    // ── The Death Star Fires At Each Target In Sequence ───────────────────────
    // Loops the targets and calls optimisticDeleteEntryRecursive per entry.
    // On success: removes from parent entry's children list, removes from any
    // search results that reference it, and removes from the browser cache.
    // On cancel: marks the parent's children as uninitialized so the next expand
    // does a fresh load.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deletes each entry using the optimistic recursive strategy.  On success
     * the entry is removed from its parent, from any {@link ISearch} result sets
     * that reference it, and from the browser model cache.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask(
            entriesToDelete.size() == 1 ? BrowserCoreMessages.bind( BrowserCoreMessages.jobs__delete_entries_task_1,
                new String[]
                    { entriesToDelete.iterator().next().getDn().getName() } ) : BrowserCoreMessages.bind(
                BrowserCoreMessages.jobs__delete_entries_task_n, new String[]
                    { Integer.toString( entriesToDelete.size() ) } ), 2 + entriesToDelete.size() );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        int num = 0;
        StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );
        for ( Iterator<IEntry> iterator = entriesToDelete.iterator(); !monitor.isCanceled()
            && !monitor.errorsReported() && iterator.hasNext(); )
        {
            IEntry entryToDelete = iterator.next();
            IBrowserConnection browserConnection = entryToDelete.getBrowserConnection();

            // delete from directory
            int errorStatusSize1 = monitor.getErrorStatus( "" ).getChildren().length; //$NON-NLS-1$
            num = optimisticDeleteEntryRecursive( browserConnection, entryToDelete.getDn(), entryToDelete.isReferral(),
                useTreeDeleteControl, num, dummyMonitor, monitor );
            int errorStatusSize2 = monitor.getErrorStatus( "" ).getChildren().length; //$NON-NLS-1$

            if ( !monitor.isCanceled() )
            {
                if ( errorStatusSize1 == errorStatusSize2 )
                {
                    // delete
                    deletedEntriesSet.add( entryToDelete );
                    //entryToDelete.setChildrenInitialized( false );

                    // delete from parent entry
                    entryToDelete.getParententry().setChildrenInitialized( false );
                    entryToDelete.getParententry().deleteChild( entryToDelete );

                    // delete from searches
                    List<ISearch> searches = browserConnection.getSearchManager().getSearches();
                    for ( ISearch search : searches )
                    {
                        if ( search.getSearchResults() != null )
                        {
                            ISearchResult[] searchResults = search.getSearchResults();
                            List<ISearchResult> searchResultList = new ArrayList<ISearchResult>();
                            searchResultList.addAll( Arrays.asList( searchResults ) );
                            for ( Iterator<ISearchResult> it = searchResultList.iterator(); it.hasNext(); )
                            {
                                ISearchResult result = it.next();
                                if ( entryToDelete.equals( result.getEntry() ) )
                                {
                                    it.remove();
                                    searchesToUpdateSet.add( search );
                                }
                            }
                            if ( searchesToUpdateSet.contains( search ) )
                            {
                                search.setSearchResults( searchResultList.toArray( new ISearchResult[searchResultList
                                    .size()] ) );
                            }
                        }
                    }

                    // delete from cache
                    browserConnection.uncacheEntryRecursive( entryToDelete );
                }
            }
            else
            {
                entryToDelete.setChildrenInitialized( false );
            }

            monitor.worked( 1 );
        }
    }


    // ── The Death Star Sends Its After-Action Report ───────────────────────────
    // We don't know exactly how many sub-entries were deleted (the recursive
    // algorithm is opaque), so we fire a BulkModificationEvent rather than
    // individual EntryDeletedEvents (which would cause massive UI thrash).
    // Also fires SearchUpdateEvent per affected search.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires a {@link BulkModificationEvent} and per-affected-search
     * {@link SearchUpdateEvent}s after deletion.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        // don't fire an EntryDeletedEvent for each deleted entry
        // that would cause massive UI updates
        // instead we unset children information and fire a BulkModificationEvent
        IBrowserConnection browserConnection = entriesToDelete.iterator().next().getBrowserConnection();
        EventRegistry.fireEntryUpdated( new BulkModificationEvent( browserConnection ), this );

        for ( ISearch search : searchesToUpdateSet )
        {
            EventRegistry.fireSearchUpdated( new SearchUpdateEvent( search,
                SearchUpdateEvent.EventDetail.SEARCH_PERFORMED ), this );
        }
    }


    // ── The Death Star's Optimistic Firing Sequence ────────────────────────────
    // Attempts the direct delete.  If the server says "not a leaf — has children"
    // (error 66), we do a ONELEVEL search to find children, then recursively call
    // ourselves on each one, then retry the parent delete.  A dummy monitor
    // absorbs per-child errors until we decide to propagate them.
    // Static and package-private so RenameEntryRunnable can call it for simulated
    // renames (rename via copy+delete).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deletes an entry recursively using an optimistic strategy.
     * <ol>
     *   <li>Try to delete the entry.</li>
     *   <li>On LDAP error 66, search for children in batches of 1,000 and
     *       recursively delete each one, then retry the parent delete.</li>
     * </ol>
     * Static so that {@link RenameEntryRunnable} can use it for simulated renames.
     *
     * @param browserConnection       the connection.
     * @param dn                      the DN to delete.
     * @param useManageDsaItControl   if {@code true}, add the ManageDsaIT control
     *                                (needed for deleting referral entries).
     * @param useTreeDeleteControl    if {@code true} and supported, use the
     *                                TreeDelete control.
     * @param numberOfDeletedEntries  running count of deleted entries (for progress).
     * @param dummyMonitor            absorbs per-child errors so we can retry.
     * @param monitor                 the real progress monitor.
     * @return the updated number of deleted entries.
     */
    static int optimisticDeleteEntryRecursive( IBrowserConnection browserConnection, Dn dn,
        boolean useManageDsaItControl, boolean useTreeDeleteControl, int numberOfDeletedEntries,
        StudioProgressMonitor dummyMonitor, StudioProgressMonitor monitor )
    {
        // try to delete entry
        dummyMonitor.reset();
        deleteEntry( browserConnection, dn, useManageDsaItControl, useTreeDeleteControl, dummyMonitor );

        if ( !dummyMonitor.errorsReported() )
        {
            numberOfDeletedEntries++;
            monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.model__deleted_n_entries,
                new String[]
                    { "" + numberOfDeletedEntries } ) ); //$NON-NLS-1$
        }
        else if ( StudioLdapException.isContextNotEmptyException( dummyMonitor.getException() ) )
        {
            // do not follow referrals or dereference aliases when deleting entries
            AliasDereferencingMethod aliasDereferencingMethod = AliasDereferencingMethod.NEVER;
            ReferralHandlingMethod referralsHandlingMethod = ReferralHandlingMethod.IGNORE;

            // perform one-level search and delete recursively
            int numberInBatch;
            dummyMonitor.reset();
            do
            {
                numberInBatch = 0;

                SearchControls searchControls = new SearchControls();
                searchControls.setCountLimit( 1000 );
                searchControls.setReturningAttributes( new String[0] );
                searchControls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
                StudioSearchResultEnumeration result = browserConnection
                    .getConnection()
                    .getConnectionWrapper()
                    .search( dn.getName(), ISearch.FILTER_TRUE, searchControls, aliasDereferencingMethod,
                        referralsHandlingMethod, null, dummyMonitor, null );

                try
                {
                    // delete all child entries
                    while ( !dummyMonitor.isCanceled() && !dummyMonitor.errorsReported() && result.hasMore() )
                    {
                        Dn childDn = result.next().getDn();

                        numberOfDeletedEntries = optimisticDeleteEntryRecursive( browserConnection, childDn, false,
                            false, numberOfDeletedEntries, dummyMonitor, monitor );
                        numberInBatch++;
                    }
                }
                catch ( Exception e )
                {
                    int ldapStatusCode = JNDIUtils.getLdapStatusCode( e );
                    if ( ldapStatusCode == 3 || ldapStatusCode == 4 || ldapStatusCode == 11 )
                    {
                        // continue with search
                    }
                    else
                    {
                        dummyMonitor.reportError( e );
                        break;
                    }
                }
            }
            while ( numberInBatch > 0 && !monitor.isCanceled() && !dummyMonitor.errorsReported() );

            // try to delete the entry again
            if ( !dummyMonitor.errorsReported() )
            {
                deleteEntry( browserConnection, dn, false, false, dummyMonitor );
            }
            if ( !dummyMonitor.errorsReported() )
            {
                numberOfDeletedEntries++;
                monitor.reportProgress( BrowserCoreMessages.bind( BrowserCoreMessages.model__deleted_n_entries,
                    new String[]
                        { "" + numberOfDeletedEntries } ) ); //$NON-NLS-1$
            }
        }
        else
        {
            Exception exception = dummyMonitor.getException();
            // we have another exception
            // report it to the dummy monitor if we are in the recursion
            dummyMonitor.reportError( exception );
            // also report it to the real monitor
            monitor.reportError( exception );
        }

        return numberOfDeletedEntries;
    }


    // ── The Death Star Fires A Single Shot ────────────────────────────────────
    // Sends the LDAP delete request with the appropriate controls.
    // TreeDelete removes an entire subtree; ManageDsaIT prevents the server
    // from following a referral when we actually want to delete the referral entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sends the LDAP "delete" request for the given DN.  Adds the TreeDelete
     * and/or ManageDsaIT controls when configured and supported.
     *
     * @param browserConnection      the connection.
     * @param dn                     the DN to delete.
     * @param useManageDsaItControl  if {@code true}, add the ManageDsaIT control.
     * @param useTreeDeleteControl   if {@code true}, add the TreeDelete control
     *                               (server must advertise support).
     * @param monitor                the progress monitor.
     */
    static void deleteEntry( IBrowserConnection browserConnection, Dn dn, boolean useManageDsaItControl,
        boolean useTreeDeleteControl, StudioProgressMonitor monitor )
    {
        // controls
        List<Control> controlList = new ArrayList<Control>();
        if ( useTreeDeleteControl
            && browserConnection.getRootDSE().isControlSupported( StudioControl.TREEDELETE_CONTROL.getOid() ) )
        {
            controlList.add( Controls.TREEDELETE_CONTROL );
        }
        if ( useManageDsaItControl
            && browserConnection.getRootDSE().isControlSupported( StudioControl.MANAGEDSAIT_CONTROL.getOid() ) )
        {
            controlList.add( Controls.MANAGEDSAIT_CONTROL );
        }
        Control[] controls = controlList.toArray( new Control[controlList.size()] );

        // delete entry
        if ( browserConnection.getConnection() != null )
        {
            browserConnection.getConnection().getConnectionWrapper()
                .deleteEntry( dn, controls, monitor, null );
        }
    }
}
