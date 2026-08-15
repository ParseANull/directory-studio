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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryMovedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;


// ── CLASS: MoveEntriesRunnable — THE REBEL FLEET RETREATS FROM HOTH ──────────
// The Empire has discovered Echo Base.  The Rebel fleet begins an emergency
// evacuation, moving transports from Hoth's atmosphere to a new rendezvous
// point in the Anoat system.  For a single transport (single entry), we issue
// a direct jump order (LDAP moddn).  For a large convoy (multiple entries)
// we avoid individual landing announcements (individual events would kill the UI)
// and instead send one bulk fleet-relocated signal (BulkModificationEvent).
// If a transport has too many cargo containers to jump directly (error 66), we
// ask if we should simulate the move by copying everything to the new location
// and destroying the originals.
// This runnable moves one or more entries to a new parent DN using LDAP moddn.
// For non-leaf entries that can't be moved directly it falls back to simulated
// move (copy+delete) via {@link SimulateRenameDialog}.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that moves one or more LDAP entries to a new parent DN.
 * Like {@link RenameEntryRunnable}, we try a direct LDAP moddn first.  On
 * error 66 (NotAllowedOnNonLeaf) we ask the user (once, even for multiple
 * entries) whether to simulate the move as copy+delete.
 * For a single moved entry we fire an {@link EntryMovedEvent}; for multiple
 * entries the UI impact of per-entry events would be severe, so we fire a
 * single {@link BulkModificationEvent} and mark both old and new parents as
 * uninitialized.
 * Think of it as the Rebel evacuation from Hoth.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MoveEntriesRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The entries to move. */
    private IEntry[] oldEntries;

    /** The new parent. */
    private IEntry newParent;

    /** The moved entries. */
    private IEntry[] newEntries;

    /** The searches to update. */
    private Set<ISearch> searchesToUpdateSet = new HashSet<ISearch>();

    /** The dialog to ask for simulated renaming */
    private SimulateRenameDialog dialog;


    // ── The Rebel Fleet Picks Up Its Evacuation Orders ────────────────────────
    // Stores entries to move, the new parent, and the dialog.  New entries array
    // starts null and is filled as each move succeeds.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new MoveEntriesRunnable.
     *
     * <p>For example:</p>
     * <pre>
     *   new StudioBrowserJob(new MoveEntriesRunnable(
     *       new IEntry[]{entry1, entry2}, newParent, dialog)).execute();
     * </pre>
     *
     * @param entries   the entries to move.
     * @param newParent the destination parent entry.
     * @param dialog    the dialog for simulated-rename prompting; may be
     *                  {@code null}.
     */
    public MoveEntriesRunnable( IEntry[] entries, IEntry newParent, SimulateRenameDialog dialog )
    {
        this.browserConnection = newParent.getBrowserConnection();
        this.oldEntries = entries;
        this.newParent = newParent;
        this.dialog = dialog;
        this.newEntries = new IEntry[oldEntries.length];
    }


    // ── One Comms Channel For The Fleet ───────────────────────────────────────
    // All moves go to the same server (the one holding the new parent).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the connection for the destination parent entry.
     *
     * @return single-element array with the underlying {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── The Job Name For The Progress Bar ─────────────────────────────────────
    // "Move entry" (singular) or "Move entries" (plural).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Move entry" or "Move entries" label.
     */
    public String getName()
    {
        return oldEntries.length == 1 ? BrowserCoreMessages.jobs__move_entry_name_1
            : BrowserCoreMessages.jobs__move_entry_name_n;
    }


    // ── Lock All Involved Entries ──────────────────────────────────────────────
    // We lock the new parent and all source entries to avoid concurrent changes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the new parent and all source entries as locked objects.
     *
     * @return the lock list.
     */
    public Object[] getLockedObjects()
    {
        List<IEntry> l = new ArrayList<IEntry>();
        l.add( newParent );
        l.addAll( Arrays.asList( oldEntries ) );
        return l.toArray();
    }


    // ── If The Evacuation Fails ────────────────────────────────────────────────
    // Singular vs plural error message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown if the move fails.
     *
     * @return a localised error string.
     */
    public String getErrorMessage()
    {
        return oldEntries.length == 1 ? BrowserCoreMessages.jobs__move_entry_error_1
            : BrowserCoreMessages.jobs__move_entry_error_n;
    }


    // ── The Fleet Jumps To The New Rendezvous Point ────────────────────────────
    // For each entry: compose the new DN (same RDN, new parent), try moddn.
    // On error 66: offer simulation (only ask the user once, use cached answer).
    // On success: uncache old entry, delete from old parent, read new entry,
    //             add to new parent, reset affected search results.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Moves each entry to the new parent using LDAP moddn.  Falls back to
     * simulated move on error 66.  After each successful move, updates the
     * browser model and affected search result sets.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.bind(
            oldEntries.length == 1 ? BrowserCoreMessages.jobs__move_entry_task_1
                : BrowserCoreMessages.jobs__move_entry_task_n, new String[]
                {} ), 3 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        // use a dummy monitor to be able to handle exceptions
        StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );

        int numAdd = 0;
        int numDel = 0;
        boolean isSimulatedRename = false;
        Dn parentDn = newParent.getDn();

        for ( int i = 0; i < oldEntries.length; i++ )
        {
            dummyMonitor.reset();

            IEntry oldEntry = oldEntries[i];
            Dn oldDn = oldEntry.getDn();

            Dn newDn = null;

            try
            {
                newDn = parentDn.add( oldDn.getRdn() );
            }
            catch ( LdapInvalidDnException lide )
            {
                newDn = Dn.EMPTY_DN;
            }

            // try to move entry
            RenameEntryRunnable.renameEntry( browserConnection, oldEntry, newDn, dummyMonitor );

            // do a simulated rename, if renaming of a non-leaf entry is not supported.
            if ( dummyMonitor.errorsReported() )
            {
                if ( dialog != null && StudioLdapException.isContextNotEmptyException( dummyMonitor.getException() ) )
                {
                    // open dialog
                    if ( numAdd == 0 )
                    {
                        dialog.setEntryInfo( browserConnection, oldDn, newDn );
                        dialog.open();
                        isSimulatedRename = dialog.isSimulateRename();
                    }

                    if ( isSimulatedRename )
                    {
                        // do simulated rename operation
                        dummyMonitor.reset();

                        numAdd = CopyEntriesRunnable.copyEntry( oldEntry, newParent, null,
                            SearchControls.SUBTREE_SCOPE,
                            numAdd, null, dummyMonitor, monitor );

                        if ( !dummyMonitor.errorsReported() )
                        {
                            dummyMonitor.reset();
                            numDel = DeleteEntriesRunnable.optimisticDeleteEntryRecursive( browserConnection, oldDn,
                                oldEntry.isReferral(), false, numDel, dummyMonitor, monitor );
                        }
                    }
                    else
                    {
                        // no simulated rename operation
                        // report the exception to the real monitor
                        Exception exception = dummyMonitor.getException();
                        monitor.reportError( exception );
                    }
                }
                else
                {
                    // we have another exception
                    // report it to the real monitor
                    Exception exception = dummyMonitor.getException();
                    monitor.reportError( exception );
                }
            }

            // update model
            if ( !dummyMonitor.errorsReported() )
            {
                // uncache old entry
                browserConnection.uncacheEntryRecursive( oldEntry );

                // remove old entry from old parent
                oldEntry.getParententry().deleteChild( oldEntry );

                // add new entry to new parent
                boolean hasMoreChildren = newParent.hasMoreChildren() || !newParent.isChildrenInitialized();
                List<Control> controls = new ArrayList<>();
                if ( oldEntry.isReferral() )
                {
                    controls.add( Controls.MANAGEDSAIT_CONTROL );
                }
                IEntry newEntry = ReadEntryRunnable.getEntry( browserConnection, newDn, controls, monitor );
                newEntries[i] = newEntry;
                newParent.addChild( newEntry );
                newParent.setHasMoreChildren( hasMoreChildren );

                // reset searches, if the moved entry is a result of a search
                List<ISearch> searches = browserConnection.getSearchManager().getSearches();
                for ( ISearch search : searches )
                {
                    if ( search.getSearchResults() != null )
                    {
                        ISearchResult[] searchResults = search.getSearchResults();
                        for ( ISearchResult result : searchResults )
                        {
                            if ( oldEntry.equals( result.getEntry() ) )
                            {
                                search.setSearchResults( null );
                                searchesToUpdateSet.add( search );
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    // ── The Fleet Signals Its New Position ────────────────────────────────────
    // Single-entry move: fire EntryMovedEvent per entry.
    // Multi-entry move: too many events would thrash the UI — instead mark the
    // old and new parents as uninitialized and fire one BulkModificationEvent.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires the appropriate update event(s).  For a single moved entry fires
     * an {@link EntryMovedEvent}.  For multiple entries fires a
     * {@link BulkModificationEvent} and marks old/new parents as uninitialized.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        // notify the entries and their parents
        if ( newEntries.length < 2 )
        {
            // notify fore each moved entry
            for ( int i = 0; i < newEntries.length; i++ )
            {
                if ( oldEntries[i] != null && newEntries[i] != null )
                {
                    EventRegistry.fireEntryUpdated( new EntryMovedEvent( oldEntries[i], newEntries[i] ), this );
                }
            }
        }
        else
        {
            // reset the old and new parents and send only a bulk update event
            // notifying for each moved entry would cause lot of UI updates...
            for ( IEntry oldEntry : oldEntries )
            {
                oldEntry.getParententry().setChildrenInitialized( false );
            }
            newParent.setChildrenInitialized( false );
            EventRegistry.fireEntryUpdated( new BulkModificationEvent( browserConnection ), this );
        }
    }
}
