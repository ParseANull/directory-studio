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

package org.apache.directory.studio.connection.core.jobs;


import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.apache.directory.studio.connection.core.Connection;


// ── INTERFACE: StudioConnectionRunnableWithProgress — HAN'S MISSION BRIEFING ──
// Every mission Han flies has the same form: a list of connections that need
// to be alive before the mission starts, the actual mission task, and a lock
// so we don't schedule two identical missions to the same target at once.
// This interface extends the base runnable with one extra method: getConnections(),
// which tells the StudioConnectionJob which connections to open before run() is called.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Extension of {@link StudioRunnableWithProgress} for runnables that require
 * live LDAP connections.
 * Implementations return the set of {@link Connection}s that must be opened
 * (connected and authenticated) before {@code run()} is called.
 * The {@link StudioConnectionJob} uses this list to automatically open any
 * connections that are not yet live.
 * Return {@code null} or an empty array if the runnable manages its own connections
 * or needs none pre-opened.
 * Think of this as Han's mission briefing: it tells the job framework which ships
 * need to be fuelled up and ready before the mission begins.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface StudioConnectionRunnableWithProgress extends StudioRunnableWithProgress
{
    // ── GET CONNECTIONS — LIST THE REQUIRED SHIPS ──────────────────────────────────
    // We return the connections that must be opened (connected + bound) before
    // our run() method is called.  Returning null means "no pre-opening needed."
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link Connection}s that must be opened before this runnable
     * is executed.
     * The {@link StudioConnectionJob} will call {@code connect()} and {@code bind()}
     * on each connection that is not yet live.
     *
     * @return  An array of {@link Connection}s, or {@code null} if none are required.
     */
    Connection[] getConnections();
}
