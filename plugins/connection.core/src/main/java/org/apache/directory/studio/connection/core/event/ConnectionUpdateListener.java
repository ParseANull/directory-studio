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


import java.util.EventListener;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;


// ── CLASS: ConnectionUpdateListener — OBI-WAN SENSING EVERY DISTURBANCE ───────
// Obi-Wan felt it the moment Alderaan was destroyed — a sudden, profound
// disturbance in the Force.  He didn't need a messenger; the Force itself
// notified him.
// This listener interface is that Force sense for connection state: the
// {@link ConnectionEventRegistry} fires events when connections open, close,
// get added, removed, or updated, and when folders change.
// Any class that cares about connection state — the Connections view, the
// status bar, the editor — implements this and registers with the registry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Listener interface for connection and connection-folder lifecycle events.
 * Implementations register with {@link ConnectionEventRegistry} and receive
 * callbacks when the connection model changes.
 * Events cover: connections being opened, closed, added, removed, or updated;
 * and folders being added, removed, or modified.
 * Think of this interface as Obi-Wan's Force sense: any disturbance in the
 * connection model is instantly felt by every registered listener.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface ConnectionUpdateListener extends EventListener
{
    // ── CONNECTION OPENED — THE FALCON'S ENGINES LIGHT UP ────────────────────────
    // Obi-Wan senses the moment the Falcon's engines ignite — the connection
    // is live and data can flow.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a {@link Connection} transitions from closed to open.
     * The UI should update the connection's status indicator to show it is connected.
     *
     * @param connection  The connection that was just opened.
     */
    void connectionOpened( Connection connection );


    // ── CONNECTION CLOSED — THE FALCON'S ENGINES CUT OUT ─────────────────────────
    // Obi-Wan senses the moment the Falcon's engines go dark — the connection
    // has been closed, either normally or due to a failure.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a {@link Connection} transitions from open to closed.
     * The UI should update to show the connection is no longer active.
     *
     * @param connection  The connection that was just closed.
     */
    void connectionClosed( Connection connection );


    // ── CONNECTION ADDED — A NEW SHIP JOINS THE FLEET ─────────────────────────────
    // Obi-Wan senses a new presence joining the fleet.
    // A new connection has been saved into the ConnectionManager.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a new {@link Connection} is added to the connection manager.
     * The Connections view should refresh its list to show the new entry.
     *
     * @param connection  The connection that was just added.
     */
    void connectionAdded( Connection connection );


    // ── CONNECTION REMOVED — A SHIP LEAVES THE FLEET ──────────────────────────────
    // Obi-Wan senses a presence fading — a connection has been deleted.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a {@link Connection} is deleted from the connection manager.
     * The Connections view should remove the entry from its list.
     *
     * @param connection  The connection that was just removed.
     */
    void connectionRemoved( Connection connection );


    // ── CONNECTION UPDATED — A SHIP'S SPECS HAVE CHANGED ─────────────────────────
    // Obi-Wan senses a change in a known presence — a connection's parameters
    // have been edited (new host, different port, different auth, etc.).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a {@link Connection}'s parameters (name, host, port, auth, etc.)
     * have been changed.
     * The Connections view and any open editors should refresh their display.
     *
     * @param connection  The connection whose parameters were updated.
     */
    void connectionUpdated( Connection connection );


    // ── CONNECTION FOLDER MODIFIED — CHEWIE REARRANGED THE FILING CABINET ─────────
    // Chewie changed something about an existing folder — its name or its
    // child connections list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when an existing {@link ConnectionFolder}'s properties or child list
     * have been modified.
     * The Connections view tree should refresh the affected folder node.
     *
     * @param connectionFolder  The folder that was modified.
     */
    void connectionFolderModified( ConnectionFolder connectionFolder );


    // ── CONNECTION FOLDER ADDED — CHEWIE OPENS A NEW BINDER ──────────────────────
    // Chewie opens a brand-new binder (folder) in the filing cabinet.
    // The tree should gain a new folder node.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a new {@link ConnectionFolder} is added to the folder manager.
     * The Connections view tree should refresh to show the new folder.
     *
     * @param connectionFolder  The folder that was just added.
     */
    void connectionFolderAdded( ConnectionFolder connectionFolder );


    // ── CONNECTION FOLDER REMOVED — CHEWIE DISCARDS A BINDER ─────────────────────
    // Chewie removes an empty binder (folder) from the filing cabinet.
    // The tree should drop the corresponding folder node.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a {@link ConnectionFolder} is removed from the folder manager.
     * The Connections view tree should remove the folder from its display.
     *
     * @param connectionFolder  The folder that was just removed.
     */
    void connectionFolderRemoved( ConnectionFolder connectionFolder );
}
