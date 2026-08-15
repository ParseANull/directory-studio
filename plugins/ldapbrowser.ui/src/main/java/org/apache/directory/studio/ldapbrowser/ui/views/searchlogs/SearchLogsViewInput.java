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

package org.apache.directory.studio.ldapbrowser.ui.views.searchlogs;


import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;


// ── CLASS: SearchLogsViewInput — THE DEATH STAR'S SPECIFIC DATA RECORD ───────
// Deep in the Imperial data vaults, each log file is tagged with an index
// number and a connection ID — so the clerk knows exactly which spool of
// records to pull off the shelf and display. SearchLogsViewInput is that
// tag: it bundles together the connection (which server?) and the file index
// (which log rotation? 0 = newest, 1 = one older, etc.) into one tidy parcel.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Value object that identifies which search log file the view should display.
 * The search logger rotates log files per connection, so we need both the
 * connection reference and an array index to uniquely identify one log file.
 * Index 0 is the most recent file; higher indices are older rotations.
 * Think of it as the file-cabinet tag used by the Imperial data clerk:
 * connection + index = pull exactly this spool of records.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchLogsViewInput
{

    /** The browser connection. */
    private IBrowserConnection browserConnection;

    /** The index of the displayed log file */
    private int index;


    // ── The Clerk Writes the Record Tag ─────────────────────────────────────────
    // An Imperial clerk labels a new spool: connection X, file rotation N.
    // Without both pieces of information, no one knows which records to pull.
    // We store both so the universal listener can load the right file.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SearchLogsViewInput identifying one specific log file.
     * The caller must supply both the connection (to identify the server)
     * and the index (to pick which rotation within that server's log files).
     *
     * @param browserConnection  the connection whose search logs we want to show.
     * @param index              which log file rotation to display; 0 = newest.
     */
    public SearchLogsViewInput( IBrowserConnection browserConnection, int index )
    {
        this.browserConnection = browserConnection;
        this.index = index;
    }


    // ── The Clerk Reads the Connection Label ─────────────────────────────────────
    // The clerk glances at the tag and reads off which server these records
    // belong to — so the viewer knows where to look.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the connection whose search log files this input references.
     * Used by the universal listener to locate the right log file on disk.
     *
     * @return  the browser connection associated with these log files.
     */
    public IBrowserConnection getBrowserConnection()
    {
        return browserConnection;
    }


    // ── The Clerk Reads the Rotation Number ─────────────────────────────────────
    // The clerk checks the rotation number — is this the latest spool or an
    // archived one from last week?
    // The index tells the logger which file in its rotation array to load.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the file-rotation index for this input.
     * Index 0 is the most recent log file; higher values are older rotations.
     * The "Older" and "Newer" actions increment and decrement this value to
     * page through the log history.
     *
     * @return  the zero-based log file rotation index.
     */
    public int getIndex()
    {
        return index;
    }

}
