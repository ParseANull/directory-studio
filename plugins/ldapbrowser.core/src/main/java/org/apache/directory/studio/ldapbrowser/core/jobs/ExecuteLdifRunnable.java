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


import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldifparser.model.LdifEnumeration;
import org.apache.directory.studio.ldifparser.parser.LdifParser;


// ── CLASS: ExecuteLdifRunnable — CASSIAN ANDOR EXECUTES THE REBEL ORDER ───────
// Cassian Andor receives a mission dossier from Alliance Intelligence: a list
// of operations to perform on the Empire's systems.  He works through them one
// by one — add this file, delete that record, modify this data — until the whole
// list is done (or he hits an error).  When he's done he files a bulk-mission
// report back to base.
// This runnable takes an LDIF string — which is like a batch script of LDAP
// operations (add, delete, modify, moddn) — parses it, and executes each
// record against the server.  Afterwards it fires a {@link BulkModificationEvent}
// so the browser model knows something changed and needs to refresh.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background runnable that parses and executes an LDIF string against the
 * LDAP server.  An LDIF (LDAP Data Interchange Format) string is a text-based
 * batch of one or more LDAP operations: add entries, delete entries, modify
 * attributes, or rename/move entries.  We parse it with {@link LdifParser},
 * then pass each record to {@link ImportLdifRunnable#importLdif} which handles
 * the per-record execution.
 * After completion we fire a {@link BulkModificationEvent} since we don't know
 * exactly which entries changed — the browser tree reloads the affected nodes.
 * Think of it as Cassian Andor working through his mission briefing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ExecuteLdifRunnable implements StudioConnectionBulkRunnableWithProgress
{
    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The LDIF to execute. */
    private String ldif;

    /** The update if entry exists flag. */
    private boolean updateIfEntryExists;

    /** The continue on error flag. */
    private boolean continueOnError;


    // ── Cassian Receives His Mission Dossier ──────────────────────────────────
    // "Here's the connection, here's the LDIF script, and here's how to handle
    //  errors and duplicates."  We store all four parameters for use in run().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ExecuteLdifRunnable.
     *
     * <p>For example — executing an LDIF modification:</p>
     * <pre>
     *   String ldif = "dn: cn=Han,ou=Rebels,...\nchangetype: modify\nreplace: rank\nrank: General\n";
     *   new StudioBrowserJob(new ExecuteLdifRunnable(conn, ldif, false, false)).execute();
     * </pre>
     *
     * @param browserConnection  the connection to execute against.
     * @param ldif               the LDIF string to parse and execute.
     * @param updateIfEntryExists if {@code true}, silently convert a failed "add"
     *                           (entry-already-exists error 68) into a "modify".
     * @param continueOnError    if {@code true}, continue processing records even
     *                           when one fails; errors are logged but not fatal.
     */
    public ExecuteLdifRunnable( IBrowserConnection browserConnection, String ldif, boolean updateIfEntryExists,
        boolean continueOnError )
    {
        this.browserConnection = browserConnection;
        this.ldif = ldif;
        this.continueOnError = continueOnError;
        this.updateIfEntryExists = updateIfEntryExists;
    }


    // ── Cassian's Comms Channel ────────────────────────────────────────────────
    // Returns the raw LDAP connection so the job framework knows which server
    // to acquire before the LDIF operations begin.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw connection used for the LDAP operations.
     *
     * @return single-element array with the underlying {@link Connection}.
     */
    public Connection[] getConnections()
    {
        return new Connection[]
            { browserConnection.getConnection() };
    }


    // ── The Mission Name For The Progress Bar ─────────────────────────────────
    // "Executing LDIF..." shown in the Eclipse progress view.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this background job.
     *
     * @return a localised "Execute LDIF" label.
     */
    public String getName()
    {
        return BrowserCoreMessages.jobs__execute_ldif_name;
    }


    // ── Cassian Locks The Target System During The Mission ────────────────────
    // We lock the URL+LDIF combination to prevent two identical LDIF jobs from
    // running simultaneously against the same connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lock key for this job.  We use a hash of the connection URL
     * and the LDIF content so the same LDIF can't run twice simultaneously on
     * the same server.
     *
     * @return the lock key as a single-element array.
     */
    public Object[] getLockedObjects()
    {
        List<Object> l = new ArrayList<Object>();
        l.add( browserConnection.getUrl() + "_" + DigestUtils.shaHex( ldif ) ); //$NON-NLS-1$
        return l.toArray();
    }


    // ── The Error Report If The Mission Fails ─────────────────────────────────
    // "Cassian's mission failed — LDIF execution error."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message displayed to the user if the LDIF execution fails.
     *
     * @return a localised "Execute LDIF failed" error string.
     */
    public String getErrorMessage()
    {
        return BrowserCoreMessages.jobs__execute_ldif_error;
    }


    // ── Cassian Works Through The Dossier ─────────────────────────────────────
    // He delegates to the static executeLdif helper, which wraps the string in
    // a Reader, parses it with LdifParser, and passes records to importLdif.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the LDIF by delegating to {@link #executeLdif}.
     *
     * @param monitor the Eclipse progress monitor.
     */
    public void run( StudioProgressMonitor monitor )
    {
        executeLdif( browserConnection, ldif, updateIfEntryExists, continueOnError, monitor );
    }


    // ── The Shared Mission-Execution Engine ───────────────────────────────────
    // Static so UpdateEntryRunnable can reuse it without creating a full job.
    // Parses the LDIF string with LdifParser and hands each record to importLdif.
    // A no-op log writer discards output since we don't need a log file here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Parses and executes an LDIF string.  This static method is shared with
     * {@link UpdateEntryRunnable} which needs to re-execute the same logic.
     * A no-op {@link Writer} is passed as the log writer since in-memory
     * LDIF execution doesn't need a log file.
     *
     * <p>For example — executing a delete record:</p>
     * <pre>
     *   String ldif = "dn: cn=Jar Jar,...\nchangetype: delete\n";
     *   ExecuteLdifRunnable.executeLdif(conn, ldif, false, false, monitor);
     * </pre>
     *
     * @param browserConnection  the connection to execute against.
     * @param ldif               the LDIF string to parse and execute.
     * @param updateIfEntryExists if {@code true}, convert error-68 adds to modifies.
     * @param continueOnError    if {@code true}, log errors and continue.
     * @param monitor            the progress monitor.
     */
    public static void executeLdif( IBrowserConnection browserConnection, String ldif, boolean updateIfEntryExists,
        boolean continueOnError, StudioProgressMonitor monitor )
    {
        monitor.beginTask( BrowserCoreMessages.jobs__execute_ldif_task, 2 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        try
        {
            Reader ldifReader = new StringReader( ldif );
            LdifParser parser = new LdifParser();
            LdifEnumeration enumeration = parser.parse( ldifReader );

            Writer logWriter = new Writer()
            {
                public void close()
                {
                }


                public void flush()
                {
                }


                public void write( char[] cbuf, int off, int len )
                {
                }
            };

            ImportLdifRunnable.importLdif( browserConnection, enumeration, logWriter, updateIfEntryExists,
                continueOnError, monitor );

            logWriter.close();
            ldifReader.close();
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    // ── Cassian Files His After-Action Report ─────────────────────────────────
    // "Mission complete — multiple entries affected."  We don't know exactly
    // which entries changed so we fire a BulkModificationEvent; the UI will
    // refresh stale parts of the tree on demand.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires a {@link BulkModificationEvent} to notify the browser model that an
     * unknown number of entries may have changed.  The UI responds by marking
     * cached entries as stale.
     *
     * @param monitor ignored.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        EventRegistry.fireEntryUpdated( new BulkModificationEvent( browserConnection ), this );
    }
}
