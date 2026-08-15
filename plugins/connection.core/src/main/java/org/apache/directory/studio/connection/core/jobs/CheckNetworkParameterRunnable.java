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


import javax.net.ssl.SSLSession;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.Messages;


// ── CLASS: CheckNetworkParameterRunnable — HAN'S CONNECTION REACHABILITY TEST ──
// Before Han trusts the Falcon's hyperdrive settings, he does a quick ping:
// "Can I actually reach the server?"  He doesn't bother to authenticate —
// he just connects, grabs the TLS session details (if there are any), and
// disconnects.  If the shields challenge him with a certificate, that's
// handled by the TrustManager automatically.
// This runnable is that reachability probe, used by the "Check Network Parameter"
// button in the connection dialog.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Runnable that checks whether a network connection can be established to the
 * directory server without authenticating.
 * We connect, capture the {@link SSLSession} (for TLS connections, so the caller
 * can inspect the server certificate), and immediately disconnect.
 * No bind, no events, no listener notification.
 * Think of this as Han's quick ping: "Is anyone home on that server?"
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CheckNetworkParameterRunnable implements StudioConnectionRunnableWithProgress
{

    /** The connection whose network reachability we want to verify. */
    private Connection connection;

    /** The SSL/TLS session captured during connect, or null for non-TLS connections. */
    private SSLSession sslSession;


    // ── CONSTRUCTOR — PICK THE CONNECTION TO TEST ──────────────────────────────────
    // Han chooses which ship's hyperdrive coordinates to test.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CheckNetworkParameterRunnable} for the given connection.
     *
     * @param connection  The connection to test.
     */
    public CheckNetworkParameterRunnable( Connection connection )
    {
        this.connection = connection;
    }


    // ── GET LOCKED OBJECTS — LOCK ON THE CONNECTION ────────────────────────────────
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
        return Messages.jobs__check_network_name;
    }


    // ── RUN — CONNECT, CAPTURE TLS SESSION, DISCONNECT ───────────────────────────
    // Han pings the server: connect (which may trigger the TLS handshake and
    // certificate challenge), grab the session, and disconnect.
    // The SSLSession is saved so the caller can inspect the server certificate.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the network check: connect, capture the {@link SSLSession}, disconnect.
     * On failure (TCP refused, certificate rejected) the monitor receives an error.
     *
     * @param monitor  Progress monitor for cancellation and error reporting.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( Messages.jobs__check_network_task, 3 );
        monitor.reportProgress( " " ); //$NON-NLS-1$
        monitor.worked( 1 );

        connection.getConnectionWrapper().connect( monitor );
        this.sslSession = connection.getConnectionWrapper().getSslSession();
        connection.getConnectionWrapper().disconnect();
    }


    // ── GET ERROR MESSAGE — WHAT TO SHOW IF THE JOB FAILS ─────────────────────────
    /**
     * {@inheritDoc}
     * Returns the NLS-localised error message for the progress dialog on failure.
     */
    public String getErrorMessage()
    {
        return Messages.jobs__check_network_error;
    }


    // ── GET CONNECTIONS — WE MANAGE OUR OWN CONNECTION ────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code null} — this runnable manages its own connection lifecycle.
     */
    public Connection[] getConnections()
    {
        return null;
    }


    // ── GET SSL SESSION — RETRIEVE THE CAPTURED TLS SESSION ───────────────────────
    // The dialog uses this after run() completes to extract the server certificate
    // and show it to the user.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link SSLSession} captured during the connect phase.
     * Valid only after {@link #run(StudioProgressMonitor)} has returned successfully.
     * Returns {@code null} for non-TLS connections.
     *
     * @return  The {@link SSLSession}, or {@code null}.
     */
    public SSLSession getSslSession()
    {
        return sslSession;
    }
}
