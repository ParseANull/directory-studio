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


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: UpdateEntryRunnable — LANDO ALTERS THE DEAL, THEN CHECKS THE RESULT
// Lando agrees to a new version of the deal with Vader.  He signs the revised
// contract (execute the LDIF), then immediately walks back to the carbon-freeze
// chamber to verify the new terms are actually in effect (re-read attributes).
// This is a thin subclass of {@link ExecuteLdifRunnable}: it runs the LDIF-based
// attribute update on a single entry, then immediately calls
// {@link InitializeAttributesRunnable#initializeAttributes} to reload the
// entry's attributes from the server.  That way the browser's in-memory model
// always reflects the real server state after the edit.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that modifies a single {@link IEntry} by executing
 * an LDIF change record, then immediately reloads the entry's attributes.
 * The reload happens even if the LDIF execution was cancelled, because the
 * entry's attributes are considered uninitialized until reconfirmed from the
 * server.  After completion a generic {@link EntryModificationEvent} is fired
 * so the UI refreshes the entry's attribute display.
 * Think of it as Lando signing the revised deal and then walking back to the
 * chamber to confirm the new terms are actually applied.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UpdateEntryRunnable extends ExecuteLdifRunnable
{
    /** The entry */
    private IEntry entry;


    // ── Lando Receives The Revised Contract And Notes The Entry ──────────────────
    // "Here's the new deal terms, and here's which facility the change applies to."
    // We pass the LDIF up to the parent (ExecuteLdifRunnable) and note the
    // specific entry so we can reload its attributes afterward.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new UpdateEntryRunnable.
     * The LDIF should be a valid LDIF change record (modify/add/delete) targeting
     * the given entry's DN.
     *
     * <p>For example — modifying a user's email address via LDIF:</p>
     * <pre>
     *   String ldif = "dn: cn=Luke,...\nchangetype: modify\nreplace: mail\nmail: new@example.com\n";
     *   new StudioBrowserJob(new UpdateEntryRunnable(entry, ldif)).execute();
     * </pre>
     *
     * @param entry the LDAP entry to modify.
     * @param ldif  the LDIF change record to execute against the entry.
     */
    public UpdateEntryRunnable( IEntry entry, String ldif )
    {
        super( entry.getBrowserConnection(), ldif, false, false );
        this.entry = entry;
    }


    // ── Lando Signs The Contract, Then Verifies The New Terms ────────────────────
    // First: execute the LDIF against the server (sign the deal).
    // Then: reload the entry's attributes (verify the terms are applied).
    // Even if LDIF execution was cancelled we reload anyway — the entry's
    // attribute state may be partially applied and we need a clean snapshot.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the LDIF change, then immediately reloads the entry's attributes
     * from the directory.  The reload is performed even on cancellation, because
     * the entry's attributes may be in an inconsistent intermediate state.
     *
     * @param monitor the Eclipse progress monitor.
     */
    @Override
    public void run( StudioProgressMonitor monitor )
    {
        super.run( monitor );
        if ( monitor.isCanceled() )
        {
            // update attributes in any case, because the attributes are not initialized
            monitor.setCanceled( false );
        }
        InitializeAttributesRunnable.initializeAttributes( entry, monitor );
    }


    // ── Lando Announces The Deal Is Done ─────────────────────────────────────────
    // "The new terms are in effect — all parties have been notified."
    // We fire a generic EntryModificationEvent so the attribute table view
    // refreshes to show the server's confirmed attribute state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires an {@link EntryModificationEvent} for the entry so the LDAP
     * browser UI refreshes the attribute display with the newly loaded values.
     *
     * @param monitor ignored (notification has no progress to report).
     */
    @Override
    public void runNotification( StudioProgressMonitor monitor )
    {
        EventRegistry.fireEntryUpdated( new EntryModificationEvent( entry.getBrowserConnection(), entry ), this );
    }
}
