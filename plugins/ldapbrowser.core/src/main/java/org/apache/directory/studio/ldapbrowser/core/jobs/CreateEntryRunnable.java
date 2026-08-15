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

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Control;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Controls;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.EntryAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;


// ── CLASS: CreateEntryRunnable — THE REBELS ESTABLISH A NEW BASE ON HOTH ─────
// The Rebel Alliance finds a new ice-planet hideout (the target DN) and starts
// constructing Echo Base: they set up the power generators, shield projectors,
// and hangar bays (the entry's objectClass and attributes).  Once construction
// is done, they check the base actually appears on the HoloNet map (reading the
// newly-created entry back from the server).
// This runnable translates a browser-model {@link IEntry} into an LDAP "add"
// operation, sends it to the server, then reads the created entry back so we can
// fire a proper {@link EntryAddedEvent} with the real server-side entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that creates a new LDAP entry on the directory server.
 * We convert the browser-model {@link IEntry} (which may be a template built
 * by the New Entry wizard) into an Apache LDAP API {@link Entry}, send it with
 * an LDAP "add" request, then read the entry back from the server.  Reading it
 * back lets us fire an {@link EntryAddedEvent} with the actual server-confirmed
 * entry, and lets us detect whether the new entry is an alias, referral, or
 * subentry so the parent's display flags stay accurate.
 * Think of it as the Rebels confirming Echo Base is actually on the HoloNet map
 * before they announce the new position to the fleet.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CreateEntryRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The entry to create. */
    private IEntry entryToCreate;

    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The created entry. */
    private IEntry createdEntry;


    // ── Rebels Draft The Blueprint For Echo Base ───────────────────────────────
    // The Alliance designates the DN, notes which connection to use, and marks
    // the "base confirmed" flag as null until after the server confirms it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new CreateEntryRunnable.
     * The {@code entryToCreate} should be a fully-formed browser-model entry
     * (populated by the New Entry wizard).  After the job runs, retrieve the
     * server-side entry via {@link #getCreatedEntry()}.
     *
     * <p>For example — creating a new user entry:</p>
     * <pre>
     *   IEntry template = ...; // built by New Entry wizard
     *   new StudioBrowserJob(new CreateEntryRunnable(template, conn)).execute();
     * </pre>
     *
     * @param entryToCreate    the browser-model entry to add to the directory.
     * @param browserConnection the connection to the LDAP server.
     */
    public CreateEntryRunnable( IEntry entryToCreate, IBrowserConnection browserConnection )
    {
        this.entryToCreate = entryToCreate;
        this.browserConnection = browserConnection;
        this.createdEntry = null;
    }


    // ── The Rebels Need One Specific Comms Channel ────────────────────────────
    // Echo Base uses a single designated comms relay — the underlying connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw LDAP connection used for the "add" operation.
     *
     * @return single-element array with the underlying {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── The Mission Name For The Progress Indicator ────────────────────────────
    // "Creating entry..." shown in the Eclipse progress view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Create entry" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__create_entry_name_1;
    }


    // ── The Browser Connection Is Locked During Construction ─────────────────
    // No other job should touch this connection while Echo Base is being built;
    // concurrent operations could leave the model in an inconsistent state.
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


    // ── The Error Report If Base Construction Fails ───────────────────────────
    // "Echo Base construction failed — LDAP add operation error."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message displayed to the user if the entry creation fails.
     *
     * @return a localised "Could not create entry" error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__create_entry_error_1;
    }


    // ── Rebels Build The Base, Then Confirm It On The HoloNet ────────────────
    // Step 1: send the LDAP "add" (build Echo Base on Hoth).
    // Step 2: read the entry back from the server (confirm it appears on the
    //         HoloNet map).  A dummy monitor absorbs read-back errors so they
    //         don't bother the user (the base might be on a master server
    //         that hasn't yet replicated to the current replica).
    // Step 3: set hints on the parent so the browser tree shows child/alias flags.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the LDAP "add" and then reads the created entry back from the server.
     * The read-back is best-effort: if the server can't return it (e.g. referral
     * created on a remote master), we proceed anyway — the
     * {@link EntryAddedEvent} is simply not fired in that case.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.bind( BrowserCoreMessages.jobs__create_entry_task_1, new String[]
            { entryToCreate.getDn().getName() } ), 2 + 1 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        try
        {
            createEntry( browserConnection, entryToCreate, monitor );
        }
        catch ( LdapException e )
        {
            monitor.reportError( e );
        }

        if ( !monitor.errorsReported() && !monitor.isCanceled() )
        {
            List<org.apache.directory.api.ldap.model.message.Control> controls = new ArrayList<>();
            if ( entryToCreate.isReferral() )
            {
                controls.add( Controls.MANAGEDSAIT_CONTROL );
            }

            // Here we try to read the created entry to be able to send the right event notification.
            // In some cases that doesn't work:
            // - if there was a referral and the entry was created on another (master) server and not yet sync'ed to the current server
            // So we use a dummy monitor to no bother the user with an error message.
            StudioProgressMonitor dummyMonitor = new StudioProgressMonitor( monitor );
            createdEntry = ReadEntryRunnable
                .getEntry( browserConnection, entryToCreate.getDn(), controls, dummyMonitor );
            dummyMonitor.done();
            if ( createdEntry != null )
            {
                createdEntry.setHasChildrenHint( false );

                // set some flags at the parent
                if ( createdEntry.hasParententry() )
                {
                    if ( createdEntry.isAlias() )
                    {
                        createdEntry.getParententry().setFetchAliases( true );
                    }
                    if ( createdEntry.isReferral() )
                    {
                        createdEntry.getParententry().setFetchReferrals( true );
                    }
                    if ( createdEntry.isSubentry() )
                    {
                        createdEntry.getParententry().setFetchSubentries( true );
                    }
                }
            }
        }

        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );
    }


    // ── Rebels Announce Echo Base Is Live On The HoloNet ─────────────────────
    // "Echo Base is operational — Rebel fleet updated."  We fire an
    // EntryAddedEvent so the LDAP browser tree adds the new node immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires an {@link EntryAddedEvent} so the browser tree adds the new entry
     * to the UI.  Only fired if we successfully read the created entry back
     * from the server.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        if ( createdEntry != null )
        {
            EventRegistry.fireEntryUpdated( new EntryAddedEvent( browserConnection, createdEntry ), this );
        }
    }


    // ── The Actual Construction Crew Builds The Base ──────────────────────────
    // This static helper converts the browser-model IEntry to an Apache LDAP API
    // Entry, optionally adds the ManageDsaIT control for referral entries, then
    // sends the "add" request over the connection wrapper.
    // It's static so other runnables (e.g. CopyEntriesRunnable) can reuse it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sends an LDAP "add" request for the given entry.  Converts the
     * browser-model {@link IEntry} to an API-level {@link Entry} first.
     * If the entry is a referral, the {@code ManageDsaIT} control is added
     * so the server treats it as a regular entry rather than following the
     * referral.
     *
     * <p>For example — used by both the wizard and copy operations:</p>
     * <pre>
     *   createEntry(browserConn, templateEntry, monitor);
     * </pre>
     *
     * @param browserConnection the connection to send the add request on.
     * @param entryToCreate     the entry to create, as a browser-model object.
     * @param monitor           the progress monitor.
     * @throws LdapException    if the LDAP add operation fails.
     */
    static void createEntry( IBrowserConnection browserConnection, IEntry entryToCreate, StudioProgressMonitor monitor ) throws LdapException
    {
        Entry entry = ModelConverter.toLdapApiEntry( entryToCreate );

        // ManageDsaIT control
        Control[] controls = null;
        if ( entryToCreate.isReferral() )
        {
            controls = new Control[]
                { Controls.MANAGEDSAIT_CONTROL };
        }

        browserConnection.getConnection().getConnectionWrapper()
            .createEntry( entry, controls, monitor, null );
    }
}
