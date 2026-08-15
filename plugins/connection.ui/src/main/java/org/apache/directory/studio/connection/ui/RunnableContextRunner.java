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

package org.apache.directory.studio.connection.ui;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.IConnectionListener;
import org.apache.directory.studio.connection.core.Messages;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionBulkRunnableWithProgress;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;


// ── CLASS: RunnableContextRunner — THE FALCON'S CO-PILOT RUN LOOP ─────────────────
// When Han has a mission, he needs someone to handle the navigation computer
// while he flies.  RunnableContextRunner is that co-pilot: it wraps a
// StudioConnectionRunnableWithProgress in the IRunnableWithProgress contract
// that Eclipse's progress context (wizard, progress service) can execute.
// What the co-pilot does inside run():
//   1. Ensures all required connections are open (connect + bind).
//   2. Fires "connection opened" events so the UI updates.
//   3. If the runnable is a bulk runnable, suspends event firing, runs it,
//      then resumes event firing and calls runNotification().
//   4. Collects any errors into a StudioProgressMonitor.
//   5. After the context returns, checks the status and optionally shows the
//      ExceptionHandler dialog.
//   6. Notifies all registered Listener observers with the final status.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility that executes a {@link StudioConnectionRunnableWithProgress} inside
 * an Eclipse {@link IRunnableContext} (a wizard page, a progress-service dialog, etc.).
 *
 * <p>The runner handles:</p>
 * <ul>
 *   <li>Opening any connections declared by {@link StudioConnectionRunnableWithProgress#getConnections()}.</li>
 *   <li>Firing "connection opened" events after each successful connect+bind.</li>
 *   <li>Suppressing and batch-resuming events for {@link StudioConnectionBulkRunnableWithProgress}.</li>
 *   <li>Collecting errors into a {@link StudioProgressMonitor} and optionally showing
 *       an error dialog via {@link ExceptionHandler}.</li>
 *   <li>Notifying registered {@link Listener}s when the runnable finishes.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RunnableContextRunner
{
    /** Listeners that are notified when a runnable finishes. */
    private static List<Listener> listeners = new LinkedList<>();


    /**
     * Prevents instantiation.  All methods are static.
     */
    private RunnableContextRunner()
    {
        // Nothing to do
    }


    // ── EXECUTE — RUN A RUNNABLE IN A PROGRESS CONTEXT ────────────────────────────
    /**
     * Executes the given runnable inside the given {@link IRunnableContext}.
     * If {@code runnableContext} is {@code null}, we fall back to the Eclipse
     * workbench progress service (which shows a progress dialog).
     *
     * <p>Steps performed inside the context's run loop:</p>
     * <ol>
     *   <li>Open and bind each connection declared by
     *       {@link StudioConnectionRunnableWithProgress#getConnections()}.</li>
     *   <li>Fire "connection opened" events for newly opened connections.</li>
     *   <li>If {@code runnable} is a {@link StudioConnectionBulkRunnableWithProgress},
     *       suspend event firing, run it, resume event firing, and call
     *       {@link StudioConnectionBulkRunnableWithProgress#runNotification}.</li>
     *   <li>Otherwise, run the runnable normally.</li>
     * </ol>
     *
     * @param runnable         The runnable to execute.
     * @param runnableContext  The runnable context, or {@code null} for the progress service.
     * @param handleError      If {@code true}, show an error dialog on failure.
     * @return  The final execution status.
     */
    public static IStatus execute( final StudioConnectionRunnableWithProgress runnable,
        IRunnableContext runnableContext, boolean handleError )
    {
        if ( runnableContext == null )
        {
            runnableContext = PlatformUI.getWorkbench().getProgressService();
        }

        final StudioProgressMonitor[] spm = new StudioProgressMonitor[1];
        IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress()
        {
            public void run( IProgressMonitor monitor ) throws InterruptedException
            {
                spm[0] = new StudioProgressMonitor( monitor );

                // ── STEP 1: OPEN REQUIRED CONNECTIONS ─────────────────────────────
                // Like Han making sure the engines are running before asking for
                // a hyperspace calculation.
                // ──────────────────────────────────────────────────────────────────
                Connection[] connections = runnable.getConnections();

                if ( connections != null )
                {
                    for ( Connection connection : connections )
                    {
                        if ( ( connection != null ) && !connection.getConnectionWrapper().isConnected() )
                        {
                            spm[0].setTaskName( Messages.bind( Messages.jobs__open_connections_task, new String[]
                                { connection.getName() } ) );
                            spm[0].worked( 1 );

                            connection.getConnectionWrapper().connect( spm[0] );

                            if ( connection.getConnectionWrapper().isConnected() )
                            {
                                connection.getConnectionWrapper().bind( spm[0] );
                            }

                            // ── STEP 2: FIRE "CONNECTION OPENED" EVENTS ────────────
                            if ( connection.getConnectionWrapper().isConnected() )
                            {
                                for ( IConnectionListener listener : ConnectionCorePlugin.getDefault()
                                    .getConnectionListeners() )
                                {
                                    listener.connectionOpened( connection, spm[0] );
                                }

                                ConnectionEventRegistry.fireConnectionOpened( connection, this );
                            }
                        }
                    }
                }

                if ( !spm[0].errorsReported() )
                {
                    try
                    {
                        // ── STEP 3: RUN THE RUNNABLE ───────────────────────────────
                        // Bulk runnables get event suppression; regular ones run as-is.
                        // ──────────────────────────────────────────────────────────
                        if ( runnable instanceof StudioConnectionBulkRunnableWithProgress )
                        {
                            StudioConnectionBulkRunnableWithProgress bulkRunnable = ( StudioConnectionBulkRunnableWithProgress ) runnable;
                            ConnectionEventRegistry.suspendEventFiringInCurrentThread();

                            try
                            {
                                bulkRunnable.run( spm[0] );
                            }
                            finally
                            {
                                ConnectionEventRegistry.resumeEventFiringInCurrentThread();
                            }

                            bulkRunnable.runNotification( spm[0] );
                        }
                        else
                        {
                            runnable.run( spm[0] );
                        }
                    }
                    catch ( Exception e )
                    {
                        spm[0].reportError( e );
                    }
                    finally
                    {
                        spm[0].done();
                        monitor.done();
                    }
                }
            }
        };

        try
        {
            runnableContext.run( true, true, runnableWithProgress );
        }
        catch ( Exception ex )
        {
            ConnectionUIPlugin
                .getDefault()
                .getExceptionHandler()
                .handleException(
                    new Status( IStatus.ERROR, ConnectionUIConstants.PLUGIN_ID, IStatus.ERROR,
                        ex.getMessage() != null ? ex.getMessage() : StringUtils.EMPTY, ex ) );
        }

        IStatus status = spm[0].getErrorStatus( runnable.getErrorMessage() );

        // ── OPTIONAL: SHOW ERROR DIALOG ────────────────────────────────────────────
        if ( ( handleError && !spm[0].isCanceled() ) && !status.isOK() )
        {
            ConnectionUIPlugin.getDefault().getExceptionHandler().handleException( status );
        }

        notifyDone( runnable, status );
        return status;
    }


    // ── LISTENER INTERFACE — COMPLETION OBSERVER ──────────────────────────────────
    /**
     * Callback interface for observers that want to be notified when a runnable
     * finishes executing inside this runner.
     */
    public interface Listener
    {
        /**
         * Called after the runnable finishes (success or failure).
         *
         * @param runnable  The runnable that just finished.
         * @param status    The final execution status.
         */
        void done( StudioConnectionRunnableWithProgress runnable, IStatus status );
    }


    // ── ADD / REMOVE LISTENER ─────────────────────────────────────────────────────
    /**
     * Registers a completion listener.
     *
     * @param listener  The listener to add.
     */
    public synchronized static void addListener( Listener listener )
    {
        listeners.add( listener );
    }


    /**
     * Removes a previously registered completion listener.
     *
     * @param listener  The listener to remove.
     */
    public synchronized static void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }


    // ── NOTIFY DONE — FIRE COMPLETION EVENTS ──────────────────────────────────────
    /**
     * Notifies all registered {@link Listener}s that the given runnable has
     * finished with the given status.
     *
     * @param runnable  The runnable that finished.
     * @param status    The final execution status.
     */
    public synchronized static void notifyDone( StudioConnectionRunnableWithProgress runnable, IStatus status )
    {
        for ( Listener listener : listeners )
        {
            listener.done( runnable, status );
        }
    }

}
