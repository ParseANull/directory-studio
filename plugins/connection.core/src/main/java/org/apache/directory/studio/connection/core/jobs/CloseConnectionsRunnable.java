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


import java.util.List;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.Messages;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;


// ── CLASS: CloseConnectionsRunnable — HAN POWERS DOWN THE FALCON'S ENGINES ────
// When Han is done for the day (or the server session expires), he shuts down
// the Falcon's engines: unbind (sign out), then disconnect (cut power).
// This bulk runnable does exactly that for one or more connections in the
// run() phase, then fires the "connection closed" events in runNotification().
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Bulk runnable that closes (unbinds and disconnects) one or more LDAP connections.
 * The {@code run()} phase calls {@code unbind()} and {@code disconnect()} on each
 * connection that is currently open.
 * The {@code runNotification()} phase fires {@link IConnectionListener#connectionClosed}
 * and {@link ConnectionEventRegistry#fireConnectionClosed} for every connection that
 * is now closed, so UI components (the Connections view) update correctly.
 * Implements {@link StudioConnectionBulkRunnableWithProgress} so event firing is
 * suppressed during the run phase and batched into one notification pass.
 * Think of this as Han powering down the Falcon: the work happens silently, then the
 * whole crew gets notified that the ship is dark.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CloseConnectionsRunnable implements StudioConnectionBulkRunnableWithProgress
{

    /** The connections to close. */
    private Connection[] connections;


    // ── CONSTRUCTORS — SINGLE, ARRAY, OR LIST ─────────────────────────────────────
    // Three ways to hand us the connections to close, because the UI
    // uses whichever form is most convenient at the call site.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a runnable that closes a single connection.
     *
     * @param connection  The connection to close.
     */
    public CloseConnectionsRunnable( Connection connection )
    {
        this( new Connection[]
            { connection } );
    }


    /**
     * Creates a runnable that closes an array of connections.
     *
     * @param connections  The connections to close.
     */
    public CloseConnectionsRunnable( Connection[] connections )
    {
        this.connections = connections;
    }


    /**
     * Creates a runnable that closes a list of connections.
     *
     * @param connections  The connections to close.
     */
    public CloseConnectionsRunnable( List<Connection> connections )
    {
        this.connections = connections.toArray( new Connection[connections.size()] );
    }


    // ── GET NAME — HUMAN-READABLE TASK LABEL ──────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns a singular or plural task name depending on how many connections we are closing.
     */
    public String getName()
    {
        return connections.length == 1 ? Messages.jobs__close_connections_name_1
            : Messages.jobs__close_connections_name_n;
    }


    // ── GET LOCKED OBJECTS — THE CONNECTIONS ARE THE LOCK OBJECTS ─────────────────
    /**
     * {@inheritDoc}
     * Returns the connections being closed as lock objects for the job scheduler.
     */
    public Object[] getLockedObjects()
    {
        return connections;
    }


    // ── GET ERROR MESSAGE — WHAT TO SHOW ON FAILURE ────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns a singular or plural error message depending on how many connections failed.
     */
    public String getErrorMessage()
    {
        return connections.length == 1 ? Messages.jobs__close_connections_error_1
            : Messages.jobs__close_connections_error_n;
    }


    // ── RUN — UNBIND AND DISCONNECT ────────────────────────────────────────────────
    // Han signs out (unbind) and then cuts power (disconnect) for each connection
    // that is currently live.  Events are suppressed during this phase.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Unbinds and disconnects each connection that is currently open.
     * Event firing is suppressed during this phase by the enclosing
     * {@link StudioConnectionJob}.
     *
     * @param monitor  Progress monitor for cancellation and error reporting.
     */
    public void run( StudioProgressMonitor monitor )
    {
        monitor.beginTask( " ", connections.length * 6 + 1 ); //$NON-NLS-1$
        monitor.reportProgress( " " ); //$NON-NLS-1$

        for ( Connection connection : connections )
        {
            if ( connection.getConnectionWrapper().isConnected() )
            {
                monitor.setTaskName( Messages.bind( Messages.jobs__close_connections_task, new String[]
                    { connection.getName() } ) );
                monitor.worked( 1 );

                connection.getConnectionWrapper().unbind();
                connection.getConnectionWrapper().disconnect();
            }
        }
    }


    // ── RUN NOTIFICATION — FIRE THE "CONNECTION CLOSED" EVENTS ────────────────────
    // Now that event firing is re-enabled, we tell all listeners and the event
    // registry that each connection is now closed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires {@link IConnectionListener#connectionClosed} and
     * {@link ConnectionEventRegistry#fireConnectionClosed} for each connection that
     * is now closed.
     * Called by {@link StudioConnectionJob} after {@link #run(StudioProgressMonitor)}
     * and after event firing has been re-enabled.
     *
     * @param monitor  Progress monitor from the enclosing job.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        for ( Connection connection : connections )
        {
            if ( !connection.getConnectionWrapper().isConnected() )
            {
                for ( IConnectionListener listener : ConnectionCorePlugin.getDefault().getConnectionListeners() )
                {
                    listener.connectionClosed( connection, monitor );
                }
            }
        }

        for ( Connection connection : connections )
        {
            if ( !connection.getConnectionWrapper().isConnected() )
            {
                ConnectionEventRegistry.fireConnectionClosed( connection, this );
            }
        }
    }


    // ── GET CONNECTIONS — WE MANAGE OUR OWN CONNECTION LIFECYCLE ─────────────────
    /**
     * {@inheritDoc}
     * Returns {@code null} — closing connections don't need pre-opening.
     */
    public Connection[] getConnections()
    {
        return null;
    }
}
