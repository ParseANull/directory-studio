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


import org.apache.directory.studio.common.core.jobs.StudioJob;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.Messages;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


// ── CLASS: StudioConnectionJob — HAN'S PRE-FLIGHT MISSION ORCHESTRATOR ────────
// Before Han launches on any mission, he checks that the Falcon is actually
// running (connect + bind), then executes the mission (the runnables), and
// finally fires the "mission complete" notifications so the crew knows what
// happened.  Bulk missions (StudioConnectionBulkRunnableWithProgress) get
// special treatment: event firing is suppressed while they run, then a
// runNotification() round fires all the accumulated changes at once.
// This class is that orchestrator: it's an Eclipse background Job that wires
// connection-open, runnable execution, and event notification together.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse background {@link Job} that executes one or more
 * {@link StudioConnectionRunnableWithProgress} runnables.
 * Before running each runnable, we open its required connections if they are
 * not already connected.  Bulk runnables ({@link StudioConnectionBulkRunnableWithProgress})
 * get event firing suppressed during their {@code run()} phase; a subsequent
 * {@code runNotification()} call fires all accumulated change events at once.
 * Scheduling is guarded: if an identical runnable type is already running against
 * the same connection (same host:port lock identifier), the new job is not scheduled.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioConnectionJob extends StudioJob<StudioConnectionRunnableWithProgress>
{
    // ── CONSTRUCTOR — CREATE A JOB WITH ONE OR MORE RUNNABLES ─────────────────────
    // Han assembles his mission briefing: which tasks to run (in order).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link StudioConnectionJob} wrapping the given runnables.
     * Multiple runnables are executed in order.
     *
     * @param runnables  One or more runnables to execute.
     */
    public StudioConnectionJob( StudioConnectionRunnableWithProgress... runnables )
    {
        super( runnables );
    }


    // ── RUN — EXECUTE THE MISSION ─────────────────────────────────────────────────
    // 1. Open any connections that aren't already connected.
    // 2. Run the runnables. Bulk runnables get event-firing suspended and a
    //    separate runNotification() call afterward.
    // 3. Report errors or OK status.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the job: opens required connections, runs the runnables, handles errors.
     * Overrides {@link Job#run(IProgressMonitor)}.
     *
     * @param ipm  The Eclipse progress monitor supplied by the job framework.
     * @return     An {@link IStatus} (OK, CANCEL, or error) reflecting the outcome.
     */
    protected IStatus run( IProgressMonitor ipm )
    {
        StudioProgressMonitor monitor = new StudioProgressMonitor( ipm );

        // ensure that connections are opened
        for ( StudioConnectionRunnableWithProgress runnable : runnables )
        {
            Connection[] connections = runnable.getConnections();
            if ( connections != null )
            {
                for ( Connection connection : connections )
                {
                    if ( connection != null && !connection.getConnectionWrapper().isConnected() )
                    {
                        monitor.setTaskName( Messages.bind( Messages.jobs__open_connections_task, new String[]
                            { connection.getName() } ) );
                        monitor.worked( 1 );

                        connection.getConnectionWrapper().connect( monitor );
                        if ( connection.getConnectionWrapper().isConnected() )
                        {
                            connection.getConnectionWrapper().bind( monitor );
                        }

                        if ( connection.getConnectionWrapper().isConnected() )
                        {
                            for ( IConnectionListener listener : ConnectionCorePlugin.getDefault()
                                .getConnectionListeners() )
                            {
                                listener.connectionOpened( connection, monitor );
                            }
                            ConnectionEventRegistry.fireConnectionOpened( connection, this );
                        }
                    }
                }
            }
        }

        // execute job
        if ( !monitor.errorsReported() )
        {
            try
            {
                for ( StudioConnectionRunnableWithProgress runnable : runnables )
                {
                    if ( runnable instanceof StudioConnectionBulkRunnableWithProgress )
                    {
                        StudioConnectionBulkRunnableWithProgress bulkRunnable = ( StudioConnectionBulkRunnableWithProgress ) runnable;
                        suspendEventFiringInCurrentThread();
                        try
                        {
                            bulkRunnable.run( monitor );
                        }
                        finally
                        {
                            resumeEventFiringInCurrentThread();
                        }
                        bulkRunnable.runNotification( monitor );
                    }
                    else
                    {
                        runnable.run( monitor );
                    }
                }
            }
            catch ( Exception e )
            {
                monitor.reportError( e );
            }
        }
        // always set done, even if errors were reported
        monitor.done();
        ipm.done();

        // error handling
        if ( monitor.isCanceled() )
        {
            return Status.CANCEL_STATUS;
        }
        else if ( monitor.errorsReported() )
        {
            return monitor.getErrorStatus( runnables[0].getErrorMessage() );
        }
        else
        {
            return Status.OK_STATUS;
        }

    }


    // ── SUSPEND / RESUME EVENT FIRING — DELEGATE TO REGISTRY ──────────────────────
    // Protected so subclasses can override the event-suspension strategy if needed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Suspends event firing in the current thread.
     * Called before a bulk runnable's {@code run()} phase.
     */
    protected void suspendEventFiringInCurrentThread()
    {
        ConnectionEventRegistry.suspendEventFiringInCurrentThread();
    }


    /**
     * Resumes event firing in the current thread.
     * Called in the {@code finally} block after a bulk runnable's {@code run()} phase.
     */
    protected void resumeEventFiringInCurrentThread()
    {
        ConnectionEventRegistry.resumeEventFiringInCurrentThread();
    }


    // ── SHOULD SCHEDULE — PREVENT DUPLICATE CONCURRENT JOBS ───────────────────────
    // Han won't fly the same mission twice at the same time to the same target.
    // We check whether an identical runnable type is already running against the
    // same connection (same host:port) and return false if so.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} if an identical runnable type is already running against
     * the same connection (determined by host:port lock identifier), preventing
     * duplicate concurrent jobs.
     *
     * @return  {@code true} if this job should be scheduled; {@code false} otherwise.
     */
    public boolean shouldSchedule()
    {
        // We don't schedule a job if the same type of runnable should run
        // that works on the same entry as the current runnable.

        for ( StudioConnectionRunnableWithProgress runnable : runnables )
        {
            Object[] myLockedObjects = runnable.getLockedObjects();
            String[] myLockedObjectsIdentifiers = getLockIdentifiers( myLockedObjects );

            Job[] jobs = getJobManager().find( null );
            for ( int i = 0; i < jobs.length; i++ )
            {
                Job job = jobs[i];
                if ( job instanceof StudioConnectionJob )
                {
                    StudioConnectionJob otherJob = ( StudioConnectionJob ) job;
                    for ( StudioConnectionRunnableWithProgress otherRunnable : otherJob.runnables )
                    {
                        if ( runnable.getClass() == otherRunnable.getClass() && runnable != otherRunnable )
                        {
                            Object[] otherLockedObjects = otherRunnable.getLockedObjects();
                            String[] otherLockedObjectIdentifiers = getLockIdentifiers( otherLockedObjects );

                            for ( int j = 0; j < otherLockedObjectIdentifiers.length; j++ )
                            {
                                String other = otherLockedObjectIdentifiers[j];
                                for ( int k = 0; k < myLockedObjectsIdentifiers.length; k++ )
                                {
                                    String my = myLockedObjectsIdentifiers[k];
                                    if ( other.startsWith( my ) || my.startsWith( other ) )
                                    {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.shouldSchedule();
    }


    // ── GET LOCK IDENTIFIERS — BUILD THE COLLISION-DETECTION KEYS ─────────────────
    // For each locked object, we produce a string key.  Connection objects get a
    // "host:port" key so two jobs talking to the same server are considered
    // conflicting.  Other objects get a generic "-toString" key.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Overrides the default implementation to use a host:port string for
     * {@link Connection} locked objects.
     */
    protected String[] getLockIdentifiers( Object[] objects )
    {
        String[] identifiers = new String[objects.length];
        for ( int i = 0; i < identifiers.length; i++ )
        {
            Object o = objects[i];
            if ( o instanceof Connection )
            {
                identifiers[i] = getLockIdentifier( ( Connection ) o );
            }
            else
            {
                identifiers[i] = getLockIdentifier( objects[i] );
            }
        }
        return identifiers;
    }


    /**
     * Returns the lock identifier for a {@link Connection}: {@code "host:port"}.
     * Two connections with the same host and port are treated as the same target
     * for scheduling conflict purposes.
     *
     * @param connection  The connection to identify.
     * @return  The {@code "host:port"} lock identifier string.
     */
    private String getLockIdentifier( Connection connection )
    {
        return connection.getHost() + ':' + connection.getPort();
    }


    /**
     * Returns the generic lock identifier for a non-connection object: {@code "-toString()"}.
     *
     * @param object  The object to identify.
     * @return  A string starting with {@code '-'} followed by the object's toString.
     */
    private String getLockIdentifier( Object object )
    {
        String s = object != null ? object.toString() : "null"; //$NON-NLS-1$
        s = '-' + s;
        return s;
    }
}
