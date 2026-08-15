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
package org.apache.directory.studio.connection.core;


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;


// ── CLASS: IConnectionListener — OBI-WAN SENSES A DISTURBANCE IN THE FORCE ────
// Obi-Wan felt it the moment Alderaan was destroyed: a disturbance, millions of
// voices crying out and then suddenly silenced. He didn't need anyone to tell him
// — he sensed the event through the Force directly.
// This interface is that Force-sensitivity: implementors are notified the moment
// a connection opens or closes, without having to poll or check.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Callback interface for plugins that need to react when a connection opens or closes.
 * Implementors register via the {@code org.apache.directory.studio.connection.core.connectionListener}
 * extension point.
 * Think of this interface as Obi-Wan's Force sensitivity: you implement it, register it,
 * and the Force (event system) notifies you when the relevant disturbance occurs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface IConnectionListener
{
    // ── CONNECTION OPENED — OBI-WAN SENSES THE FORCE AWAKENING ───────────────────
    // Obi-Wan feels a great surge in the Force: a new connection has been established.
    // Every sensitive on the ship notices it simultaneously.
    // We notify the implementor so it can initialize any connection-scoped state.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called immediately after a connection is successfully opened.
     * Implementors can use this to initialize any state that is scoped to the lifetime
     * of the connection — for example loading schema caches or starting background monitors.
     *
     * @param connection  The connection that was just opened.
     * @param monitor     The progress monitor for the opening operation;
     *                    use it to report sub-task progress or check for cancellation.
     */
    void connectionOpened( Connection connection, StudioProgressMonitor monitor );


    // ── CONNECTION CLOSED — OBI-WAN SENSES THE DISTURBANCE OF DEPARTURE ──────────
    // Obi-Wan feels it when a presence in the Force vanishes: the connection is gone.
    // We notify the implementor so it can clean up any connection-scoped state.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called immediately after a connection is closed.
     * Implementors should clean up any resources that were allocated in
     * {@link #connectionOpened(Connection, StudioProgressMonitor)}.
     *
     * @param connection  The connection that was just closed.
     * @param monitor     The progress monitor for the closing operation.
     */
    void connectionClosed( Connection connection, StudioProgressMonitor monitor );
}
