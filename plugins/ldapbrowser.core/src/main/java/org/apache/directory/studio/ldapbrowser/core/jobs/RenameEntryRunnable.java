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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.io.StudioLdapException;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.EntryRenamedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;


// ── CLASS: RenameEntryRunnable — ANAKIN SKYWALKER BECOMES DARTH VADER ─────────
// Anakin kneels before Palpatine.  Palpatine attempts a direct ritual
// transformation (the LDAP moddn call).  If Anakin has too many apprentices to
// cleanly transfer (error 66 — contextNotEmpty), Palpatine opens the SimulateRename
// dialog: "Shall we copy the entire Jedi identity to the new Sith name, then
// destroy the old one?"  If Anakin agrees, they copy the whole subtree under
// "Darth Vader" and then optimistically delete everything under "Anakin Skywalker".
// This runnable renames an LDAP entry (changes its RDN).  It tries a direct
// LDAP moddn first.  If the server rejects it because the entry has children,
// we ask the user via {@link SimulateRenameDialog} whether to simulate the rename
// by copying the subtree to the new DN and then deleting the originals.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that renames an LDAP entry by changing its RDN.
 * The primary mechanism is an LDAP moddn (modifyDN) operation.  If the server
 * returns error 66 (NotAllowedOnNonLeaf, i.e. the entry has children), we offer
 * the user a simulated rename via copy+delete.  After a successful rename we
 * update the browser model: remove the old entry from cache, read back the entry
 * under its new DN, update the parent's children list, and reset any search
 * results that referenced the old entry.  We then fire an
 * {@link EntryRenamedEvent} and per-search {@link SearchUpdateEvent}s.
 * Think of it as Anakin's transformation into Darth Vader.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameEntryRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The old entry. */
    private IEntry oldEntry;

    /** The new Rdn. */
    private Rdn newRdn;

    /** The new entry. */
    private IEntry newEntry;

    /** The updated searches. */
    private Set<ISearch> searchesToUpdateSet = new HashSet<ISearch>();

    /** The dialog to ask for simulated renaming */
    private SimulateRenameDialog dialog;


    // ── Anakin Stands Before The Throne ───────────────────────────────────────
    // Stores the entry, the new RDN, and the dialog.  The newEntry field stays
    // null until after the rename succeeds (we read it back from the server).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new RenameEntryRunnable.
     *
     * <p>For example:</p>
     * <pre>
     *   new StudioBrowserJob(new RenameEntryRunnable(entry, newRdn, dialog)).execute();
     * </pre>
     *
     * @param entry  the entry to rename.
     * @param newRdn the new RDN to give the entry.
     * @param dialog the dialog to ask about simulated rename; may be
     *               {@code null} if simulated rename should never be offered.
     */
    public RenameEntryRunnable( IEntry entry, Rdn newRdn, SimulateRenameDialog dialog )
    {
        this.browserConnection = entry.getBrowserConnection();
        this.oldEntry = entry;
        this.newEntry = null;
        this.newRdn = newRdn;
        this.dialog = dialog;
    }


    // ── The Sith Need One Comms Channel ───────────────────────────────────────
    // One connection — all operations go to the same server.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the connection for this rename operation.
     *
     * @return single-element array with the underlying {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── The Job Name For The Progress Bar ─────────────────────────────────────
    // "Rename entry..." in the Eclipse progress view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Rename entry" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__rename_entry_name;
    }


    // ── Lock The Parent Entry During The Ritual ────────────────────────────────
    // We lock the parent because we're modifying its children list.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent entry as the locked object (the parent's children list
     * changes after rename).
     *
     * @return single-element array with the old entry's parent.
     */
    public Object[] getLockedObjects()
    {
        List<Object> l = new ArrayList<Object>();
        l.add( oldEntry.getParententry() );
        return l.toArray();
    }


    // ── If The Ritual Fails ────────────────────────────────────────────────────
    // "Could not rename entry." displayed to the user.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown if renaming fails.
     *
     * @return a localised error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__rename_entry_error;
    }


    // ── Palpatine Performs The Transformation ─────────────────────────────────
    // 1. Build the new DN.
    // 2. Try LDAP moddn.
    // 3. If error 66 (contextNotEmpty) and we have a dialog, ask if simulated.
    // 4. If simulated: copy subtree then optimisticDeleteEntryRecursive.
    // 5. On success: uncache old, read new entry, update parent's children list,
    //    reset affected searches.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the rename.  Tries LDAP moddn first; falls back to simulated
     * rename (copy+delete) if the server returns error 66 and the user agrees.
     * After success, reads back the new entry and updates the model.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__rename_entry_task, new String[]
            { oldEntry.getDn().getName() } ), 3 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        Dn oldDn = oldEntry.getDn();
        Dn parentDn = oldDn.getParent();
        Dn newDn = null;

        try
        {
            newDn = parentDn.add( newRdn );
        }
        catch ( LdapInvalidDnException lide )
        {
            newDn = Dn.EMPTY_DN;
        }

        // use a dummy monitor to be able to handle exceptions
        StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );

        // try to rename entry
        renameEntry( browserConnection, oldEntry, newDn, dummyMonitor );

        // do a simulated rename, if renaming of a non-leaf entry is not supported.
        if ( dummyMonitor.errorsReported() && !monitor.isCanceled() )
        {
            if ( dialog != null && StudioLdapException.isContextNotEmptyException( dummyMonitor.getException() ) )
            {
                // open dialog
                dialog.setEntryInfo( browserConnection, oldDn, newDn );
                dialog.open();
                boolean isSimulatedRename = dialog.isSimulateRename();

                if ( isSimulatedRename )
                {
                    // do simulated rename operation
                    dummyMonitor.reset();
                    CopyEntriesRunnable.copyEntry( oldEntry, oldEntry.getParententry(), newRdn,
                        SearchControls.SUBTREE_SCOPE, 0, null, dummyMonitor, monitor );

                    if ( !dummyMonitor.errorsReported() )
                    {
                        dummyMonitor.reset();
                        DeleteEntriesRunnable.optimisticDeleteEntryRecursive( browserConnection, oldDn,
                            oldEntry.isReferral(), false, 0, dummyMonitor, monitor );
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
        if ( !monitor.errorsReported() && !monitor.isCanceled() )
        {
            // uncache old entry
            browserConnection.uncacheEntryRecursive( oldEntry );

            // remove old entry and add new entry to parent
            IEntry parent = oldEntry.getParententry();
            if ( parent != null )
            {
                boolean hasMoreChildren = parent.hasMoreChildren();
                parent.deleteChild( oldEntry );

                List<Control> controls = new ArrayList<>();
                if ( oldEntry.isReferral() )
                {
                    controls.add( Controls.MANAGEDSAIT_CONTROL );
                }

                // Here we try to read the renamed entry to be able to send the right event notification.
                // In some cases this don't work:
                // - if there was a referral and the entry was created on another (master) server and not yet sync'ed to the current server
                // So we use a dummy monitor to no bother the user with an error message.
                dummyMonitor.reset();
                newEntry = ReadEntryRunnable.getEntry( browserConnection, newDn, controls, dummyMonitor );
                dummyMonitor.done();
                if ( newEntry != null )
                {
                    parent.addChild( newEntry );
                }
                parent.setHasMoreChildren( hasMoreChildren );

                // reset searches, if the renamed entry is a result of a search
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


    // ── Anakin Is Now Darth Vader — Announce The Change ───────────────────────
    // Fires an EntryRenamedEvent for the browser tree plus SearchUpdateEvents
    // for any saved searches whose results referenced the old DN.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires an {@link EntryRenamedEvent} and per-search {@link SearchUpdateEvent}s.
     * Only fires if both the old and new entries are available (rename succeeded).
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        if ( oldEntry != null && newEntry != null )
        {
            EventRegistry.fireEntryUpdated( new EntryRenamedEvent( oldEntry, newEntry ), this );

            for ( ISearch search : searchesToUpdateSet )
            {
                EventRegistry.fireSearchUpdated( new SearchUpdateEvent( search,
                    SearchUpdateEvent.EventDetail.SEARCH_PERFORMED ), this );
            }
        }
    }


    // ── The Sith Ritual Itself — The LDAP moddn Call ───────────────────────────
    // Static so MoveEntriesRunnable can reuse it.  Adds the ManageDsaIT control
    // when the entry is a referral (so the server modifies the referral itself
    // rather than following it).  The "deleteOldRdn=true" flag means the old RDN
    // attribute value is removed from the entry's attributes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sends the LDAP moddn (modifyDN) request.  Adds the ManageDsaIT control
     * for referral entries.  The old RDN attribute value is deleted
     * ({@code deleteOldRdn = true}).
     * Static so that {@link MoveEntriesRunnable} can reuse it.
     *
     * @param browserConnection the connection.
     * @param entry             the entry to rename.
     * @param newDn             the new DN (full, including the new parent).
     * @param monitor           the progress monitor.
     */
    static void renameEntry( IBrowserConnection browserConnection, IEntry entry, Dn newDn,
        StudioProgressMonitor monitor )
    {
        // ManageDsaIT control
        Control[] controls = null;
        if ( entry.isReferral() )
        {
            controls = new Control[]
                { Controls.MANAGEDSAIT_CONTROL };
        }

        if ( browserConnection.getConnection() != null )
        {
            browserConnection.getConnection().getConnectionWrapper()
                .renameEntry( entry.getDn(), newDn, true, controls, monitor, null );
        }
    }
}
