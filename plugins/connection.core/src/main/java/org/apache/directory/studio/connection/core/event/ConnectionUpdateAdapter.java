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

package org.apache.directory.studio.connection.core.event;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;


// ── CLASS: ConnectionUpdateAdapter — OBI-WAN'S DEFAULT SENSING MODULE ─────────
// Obi-Wan had the full Force sense, but most Jedi only feel the disturbances
// they train to notice.  The Adapter pattern lets us provide a complete
// implementation with every callback doing nothing, so subclasses only override
// the events they actually care about.
// This class is Obi-Wan's default sensing module: all callbacks are no-ops
// unless the subclass overrides them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * No-op implementation of {@link ConnectionUpdateListener}.
 * Extend this class and override only the event methods you need to handle.
 * This is the classic Adapter pattern — we provide empty defaults so subclasses
 * don't have to implement all eight methods when they only care about one or two.
 * Think of this as Obi-Wan's default sensing module: it receives every event
 * but does nothing unless a subclass overrides a specific callback.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionUpdateAdapter implements ConnectionUpdateListener
{
    // ── CONNECTION OPENED — DEFAULT: DO NOTHING ────────────────────────────────────
    // Obi-Wan senses the engines igniting but ignores it by default.
    // Override if you care about connections becoming active.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionOpened( Connection connection )
    {
    }


    // ── CONNECTION CLOSED — DEFAULT: DO NOTHING ────────────────────────────────────
    // Obi-Wan senses the engines going dark but ignores it by default.
    // Override if you need to react when a connection closes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionClosed( Connection connection )
    {
    }


    // ── CONNECTION ADDED — DEFAULT: DO NOTHING ─────────────────────────────────────
    // A new ship joins the fleet — ignored by default.
    // Override to refresh views when a connection is created.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionAdded( Connection connection )
    {
    }


    // ── CONNECTION REMOVED — DEFAULT: DO NOTHING ───────────────────────────────────
    // A ship leaves the fleet — ignored by default.
    // Override to refresh views when a connection is deleted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionRemoved( Connection connection )
    {
    }


    // ── CONNECTION UPDATED — DEFAULT: DO NOTHING ───────────────────────────────────
    // A ship's specs have changed — ignored by default.
    // Override to refresh views when connection parameters are edited.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionUpdated( Connection connection )
    {
    }


    // ── CONNECTION FOLDER MODIFIED — DEFAULT: DO NOTHING ──────────────────────────
    // Chewie rearranged the filing cabinet — ignored by default.
    // Override to refresh the Connections tree when folders change.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
    }


    // ── CONNECTION FOLDER ADDED — DEFAULT: DO NOTHING ─────────────────────────────
    // Chewie opens a new binder — ignored by default.
    // Override to refresh the tree when a folder is created.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
    }


    // ── CONNECTION FOLDER REMOVED — DEFAULT: DO NOTHING ───────────────────────────
    // Chewie discards a binder — ignored by default.
    // Override to refresh the tree when a folder is deleted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
    }
}
