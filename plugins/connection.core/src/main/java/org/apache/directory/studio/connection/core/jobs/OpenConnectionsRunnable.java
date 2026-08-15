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
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.Messages;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;


// ── CLASS: OpenConnectionsRunnable — HAN FIRES UP THE FALCON'S ENGINES ────────
// When Han gets a new mission (or when the user double-clicks a connection in
// the UI), someone has to fire up the engines: connect to the server and then
// authenticate (bind).  This is that runnable.
// The run() phase does the actual connect + bind.  The runNotification() phase
// tells all the IConnectionListeners and the event registry that the ship is live.
// Uses the bulk pattern so the "connection opened" event fires once, cleanly,
// after the work is done.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Bulk runnable that opens (connects and authenticates) one or more LDAP connections.
 * The {@code run()} phase calls {@code connect()} and (if successful) {@code bind()}
 * on each connection that is not already open.
 * The {@code runNotification()} phase fires {@link IConnectionListener#connectionOpened}
 * and {@link ConnectionEventRegistry#fireConnectionOpened} for every connection that is
 * now live, so UI components (the Connections view) can update.
 * Implements {@link StudioConnectionBulkRunnableWithProgress} so event firing is
 * suppressed during the run phase and batched into one notification pass.
 * Think of this as Han starting the Falcon: the engines come up in run(), and then
 * the whole crew is notified that the ship is ready to fly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenConnectionsRunnable implements StudioConnectionBulkRunnableWithProgress
{

    /** The connections to open. */
    private Connection[] connections;


    // ── CONSTRUCTORS — SINGLE OR ARRAY ───────────────────────────────────────────
    /**
     * Creates a runnable that opens a single connection.
     *
     * @param connection  The connection to open.
     */
    public OpenConnectionsRunnable( Connection connection )
    {
        this( new Connection[]
            { connection } );
    }


    /**
     * Creates a runnable that opens an array of connections.
     *
     * @param connections  The connections to open.
     */
    public OpenConnectionsRunnable( Connection[] connections )
    {
        this.connections = connections;
    }


    // ── GET NAME — HUMAN-READABLE TASK LABEL ──────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns a singular or plural task name depending on how many connections we are opening.
     */
    public String getName()
    {
        return connections.length == 1 ? Messages.jobs__open_connections_name_1
            : Messages.jobs__open_connections_name_n;
    }


    // ── GET LOCKED OBJECTS — THE CONNECTIONS ARE THE LOCK OBJECTS ─────────────────
    /**
     * {@inheritDoc}
     * Returns the connections being opened as lock objects for the job scheduler.
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
        return connections.length == 1 ? Messages.jobs__open_connections_error_1
            : Messages.jobs__open_connections_error_n;
    }


    // ── RUN — CONNECT AND BIND ────────────────────────────────────────────────────
    // Han fires the engines (connect) and presents the access code (bind) for
    // each connection that isn't already live.  Events are suppressed during
    // this phase by the enclosing StudioConnectionJob.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Connects and (if the connect succeeded) binds each connection that is not
     * already open.
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
            if ( !connection.getConnectionWrapper().isConnected() )
            {
                monitor.setTaskName( Messages.bind( Messages.jobs__open_connections_task, new String[]
                    { connection.getName() } ) );
                monitor.worked( 1 );

                connection.getConnectionWrapper().connect( monitor );
                if ( connection.getConnectionWrapper().isConnected() )
                {
                    connection.getConnectionWrapper().bind( monitor );
                }
            }
        }
    }


    // ── RUN NOTIFICATION — FIRE THE "CONNECTION OPENED" EVENTS ────────────────────
    // Now that event firing is re-enabled, we tell all listeners and the event
    // registry that each successfully opened connection is now live.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires {@link IConnectionListener#connectionOpened} and
     * {@link ConnectionEventRegistry#fireConnectionOpened} for each connection that
     * is now open.
     * Called by {@link StudioConnectionJob} after {@link #run(StudioProgressMonitor)}
     * and after event firing has been re-enabled.
     *
     * @param monitor  Progress monitor from the enclosing job.
     */
    public void runNotification( StudioProgressMonitor monitor )
    {
        for ( Connection connection : connections )
        {
            if ( connection.getConnectionWrapper().isConnected() )
            {
                for ( IConnectionListener listener : ConnectionCorePlugin.getDefault().getConnectionListeners() )
                {
                    listener.connectionOpened( connection, monitor );
                }
            }
        }

        for ( Connection connection : connections )
        {
            if ( connection.getConnectionWrapper().isConnected() )
            {
                ConnectionEventRegistry.fireConnectionOpened( connection, this );
            }
        }
    }


    // ── GET CONNECTIONS — WE MANAGE OUR OWN CONNECTION LIFECYCLE ─────────────────
    /**
     * {@inheritDoc}
     * Returns {@code null} — this runnable manages the full connect+bind sequence itself.
     */
    public Connection[] getConnections()
    {
        return null;
    }
}
