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

package org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs;


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: ModificationLogsViewInput — THE DEATH STAR'S DATA RECORDS ─────────
// The Death Star's data vault doesn't just store everything in one pile —
// each record is tagged with the ship or installation it belongs to, and an
// index identifying which log volume in that installation's archive is open.
// This class is that data record tag: it binds a browser connection (the
// "installation") to an integer index (which log file in the rotating set),
// so the view always knows exactly which file to display.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A simple value object that captures what the modification logs view is currently
 * showing: which LDAP connection's logs, and which file in the rotating log set.
 * The modification logger writes to a rotating set of files (like log.0, log.1, …);
 * the index tells us which one the user is currently viewing.
 * Think of this as a tagged Imperial data record — connection is the installation,
 * index is the volume number in the archive.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModificationLogsViewInput
{

    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The index of the displayed log file */
    private int index;


    // ── Constructor: Imperial Clerk Files the Record ──────────────────────────
    // The Imperial clerk receives the installation ID and the archive volume
    // number, stamps them on the record, and files it under active display.
    // We store both values so the view and its actions can look them up later.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new input value object for the modification logs view.
     * We bind the given connection and log-file index together so the view
     * always knows exactly which file to read from disk.
     *
     * <p>For example — the Imperial clerk stamps the installation ID and volume number:</p>
     * <pre>
     *   ModificationLogsViewInput input = new ModificationLogsViewInput( conn, 0 );
     *   // conn = the LDAP connection whose logs to show
     *   // 0   = the most-recent log file (index 0 in the rotating set)
     * </pre>
     *
     * @param browserConnection  the LDAP browser connection whose log files we want to display
     * @param index              the zero-based index into the rotating log-file array (0 = newest)
     */
    public ModificationLogsViewInput( IBrowserConnection browserConnection, int index )
    {
        this.browserConnection = browserConnection;
        this.index = index;
    }


    // ── getBrowserConnection: Clerk Looks Up the Installation ID ─────────────
    // The clerk opens the record and reads the installation ID stamped on it —
    // without this, nobody knows which server's logs we're looking at.
    // We return the stored browser connection reference.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP browser connection whose modification log files this input represents.
     * The universal listener uses this to locate the correct log files on disk.
     *
     * <p>For example — the clerk reads the installation ID from the record:</p>
     * <pre>
     *   IBrowserConnection conn = input.getBrowserConnection();
     *   File[] logFiles = modificationLogger.getFiles( conn.getConnection() );
     * </pre>
     *
     * @return  the {@link IBrowserConnection} this input is bound to
     */
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    // ── getIndex: Clerk Reads the Archive Volume Number ───────────────────────
    // The clerk checks which volume of the archive is tagged on this record —
    // log.0 is the most recent, log.1 is one rotation older, and so on.
    // We return the integer index into the rotating log-file array.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index into the rotating log-file array.
     * Index 0 is the most-recently written log file; higher numbers are older.
     * The {@link OlderAction} increments this, {@link NewerAction} decrements it.
     *
     * <p>For example — the clerk reads the archive volume number:</p>
     * <pre>
     *   int vol = input.getIndex(); // 0 = newest, 1 = one rotation older, ...
     *   File logFile = files[vol];
     * </pre>
     *
     * @return  the log-file index (0 = newest)
     */
    public int getIndex()
    {
        return index;
    }

}
