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


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Messages;


// ── CLASS: CheckBindRunnable — HAN'S AUTHENTICATION TEST FLIGHT ───────────────
// Before Han commits to a mission, he sometimes does a quick test flight to make
// sure the Falcon's Imperial access code still works at the checkpoint.
// This runnable does the same thing: it connects, binds (authenticates), and
// immediately disconnects — without firing any connection events or registering
// with the ConnectionManager.  It's used by the "Check Authentication" button
// in the connection configuration dialog.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable that verifies the bind (authentication) credentials of a connection.
 * We connect, send the bind request, and immediately disconnect.
 * This is a read-only probe that does not affect the connection's live state
 * (no events are fired, no listeners are notified).
 * Think of this as Han's quick test: fly to the checkpoint, present the code,
 * and come straight back with a pass or fail.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CheckBindRunnable implements StudioConnectionRunnableWithProgress
{
    /** The connection whose bind credentials we want to test. */
    private Connection connection;


    // ── CONSTRUCTOR — PICK THE CONNECTION TO TEST ──────────────────────────────────
    // Han decides which ship to do the test flight with.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CheckBindRunnable} for the given connection.
     *
     * @param connection  The connection to test.
     */
    public CheckBindRunnable( Connection connection )
    {
        this.connection = connection;
    }


    // ── GET LOCKED OBJECTS — THE CONNECTION IS THE LOCK OBJECT ────────────────────
    // We lock on the connection object to prevent duplicate test jobs.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns the connection being tested as the lock object.
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { connection };
    }


    // ── GET NAME — HUMAN-READABLE TASK LABEL ──────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns the NLS-localised task name for the progress dialog.
     */
    public String getName()
    {
        return Messages.jobs__check_bind_name;
    }


    // ── RUN — CONNECT, BIND, DISCONNECT ───────────────────────────────────────────
    // Han flies to the checkpoint (connect), presents his access code (bind),
    // and immediately turns around and comes home (disconnect).
    // Any failure is reported to the monitor — the progress dialog will show it.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the bind check: connect, bind, and immediately disconnect.
     * On authentication failure the monitor receives an error status.
     *
     * @param monitor  Progress monitor for cancellation and error reporting.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( Messages.jobs__check_bind_task, 4 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        connection.getConnectionWrapper().connect( monitor );
        connection.getConnectionWrapper().bind( monitor );
        connection.getConnectionWrapper().disconnect();
    }


    // ── GET ERROR MESSAGE — WHAT TO SHOW IF THE JOB FAILS ─────────────────────────
    /**
     * {@inheritDoc}
     * Returns the NLS-localised error message for the progress dialog on failure.
     */
    public String getErrorMessage()
    {
        return Messages.jobs__check_bind_error;
    }


    // ── GET CONNECTIONS — WE MANAGE OUR OWN CONNECTION ────────────────────────────
    // We don't ask the job framework to pre-open the connection because we need
    // to control the full connect/bind/disconnect sequence ourselves.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code null} — this runnable manages its own connection lifecycle.
     */
    public Connection[] getConnections()
    {
        return null;
    }
}
