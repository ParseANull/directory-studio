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

package org.apache.directory.studio.common.core.jobs;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;


// ── CLASS: StudioProgressMonitor — Yavin 4 Mission Status Tracker ─────────────
// The Alliance mission board at Yavin 4 tracks each active mission: it shows
// sub-task progress labels (reportProgress), accumulates any errors that come
// in (reportError), and lets operators signal a cancel (setCanceled → fires
// fireCancelRequested to all registered CancelListeners).
// A rate limiter (allowMessageReporting, reset once per second by the sentinel
// watcher job) prevents the board from being flooded with too-frequent updates.
// At the end of the mission done() marks it complete; getErrorStatus() assembles
// all accumulated error statuses into a MultiStatus for the Eclipse error dialog.
// The inner CancelEvent and CancelListener classes form the cancel notification
// mechanism used by long-running operations that need to stop quickly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An {@link IProgressMonitor} extension that adds active cancellation, error
 * accumulation, and rate-limited progress reporting to a wrapped monitor.
 * Key features:
 * <ul>
 *   <li>Cancel listeners: interested objects register via
 *       {@link #addCancelListener} and receive a {@link CancelEvent} when
 *       the user clicks Cancel.</li>
 *   <li>Error accumulation: call {@link #reportError} at any time; the monitor
 *       collects all errors in a list which is assembled into a
 *       {@link MultiStatus} by {@link #getErrorStatus}.</li>
 *   <li>Rate limiting: {@link #reportProgress} only forwards the sub-task
 *       message once per second (the {@link StudioProgressMonitorWatcherJob}
 *       resets the gate).</li>
 * </ul>
 * Think of this as the Yavin 4 mission status tracker: it records what's
 * happening, what went wrong, and notifies everyone when the operator says "abort."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioProgressMonitor extends ProgressMonitorWrapper
{
    /** The plugin ID */
    protected String pluginId;

    /** The flag indicating if the work is done */
    protected boolean isDone;

    /** The list of error statuses */
    protected List<Status> errorStatusList;

    /** The list of cancel listeners */
    protected List<CancelListener> cancelListenerList;

    /** Flag to indicate if message reporting is allowed. Whenever reporting a message
     * this flag is set to false. The {@link StudioProgressMonitorWatcherJob} is resetting
     * it to true once a second. This way too many updates are prevented. */
    protected AtomicBoolean allowMessageReporting;


    // ── Create a Mission Tracker Using the Default Plugin ID ─────────────────
    // The most common constructor — wraps a monitor and uses the standard
    // CommonCore plugin ID for error statuses.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a StudioProgressMonitor wrapping the given monitor, using
     * {@link CommonCoreConstants#PLUGIN_ID} as the error source identifier.
     * Also registers itself with the {@link StudioProgressMonitorWatcherJob}
     * for cancellation polling and rate-limit resets.
     *
     * @param monitor  the Eclipse progress monitor to forward progress calls to.
     */
    public StudioProgressMonitor( IProgressMonitor monitor )
    {
        this( CommonCoreConstants.PLUGIN_ID, monitor );
    }


    // ── Create a Mission Tracker for a Specific Plugin ────────────────────────
    // Plugins that host their own jobs supply their own pluginId so error
    // statuses are attributed to the right bundle in the Eclipse error log.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a StudioProgressMonitor wrapping the given monitor, using the
     * supplied {@code pluginId} as the error source identifier.
     * Also registers itself with the {@link StudioProgressMonitorWatcherJob}.
     *
     * @param pluginId  the bundle ID used when constructing {@link Status} objects.
     * @param monitor   the Eclipse progress monitor to forward progress calls to.
     */
    public StudioProgressMonitor( String pluginId, IProgressMonitor monitor )
    {
        super( monitor );
        this.pluginId = pluginId;
        isDone = false;
        CommonCorePlugin.getDefault().getStudioProgressMonitorWatcherJob().addMonitor( this );
        allowMessageReporting = new AtomicBoolean( true );
    }


    // ── Operator Presses the Abort Button ────────────────────────────────────
    // We forward the cancel to the wrapped monitor and, if actually being
    // cancelled (not un-cancelled), fire the cancel event to all listeners.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the cancelled state and, if {@code canceled} is {@code true},
     * notifies all registered {@link CancelListener}s via
     * {@link #fireCancelRequested}.
     *
     * @param canceled  {@code true} to cancel; {@code false} to clear.
     * @see org.eclipse.core.runtime.ProgressMonitorWrapper#setCanceled(boolean)
     */
    public void setCanceled( boolean canceled )
    {
        super.setCanceled( canceled );

        if ( canceled )
        {
            fireCancelRequested();
        }
    }


    // ── Mission Complete — Close the Status Board Entry ───────────────────────
    // We synchronize to avoid racing with the watcher job's done-check and then
    // mark the board entry as closed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Marks this monitor as done and forwards the call to the wrapped monitor.
     * Synchronized to avoid racing with the watcher job's {@code isDone} check.
     *
     * @see org.eclipse.core.runtime.ProgressMonitorWrapper#done()
     */
    public void done()
    {
        synchronized ( this )
        {
            isDone = true;
            super.done();
        }
    }


    // ── Register a Cancel Observer ────────────────────────────────────────────
    // Any object that needs to know about a cancel (e.g. a long network read)
    // registers here; duplicates are ignored.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link CancelListener} that will be notified when cancel is
     * requested.  Duplicate listeners are ignored.
     *
     * @param listener  the listener to register.
     */
    public void addCancelListener( CancelListener listener )
    {
        if ( cancelListenerList == null )
        {
            cancelListenerList = new ArrayList<CancelListener>();
        }

        if ( !cancelListenerList.contains( listener ) )
        {
            cancelListenerList.add( listener );
        }
    }


    // ── Remove a Cancel Observer ──────────────────────────────────────────────
    // Once an operation is done with its long-running phase it should remove its
    // cancel listener so it isn't notified for unrelated future cancellations.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a previously registered {@link CancelListener}.
     * Silently does nothing if the listener is not registered.
     *
     * @param listener  the listener to remove.
     */
    public void removeCancelListener( CancelListener listener )
    {
        if ( ( cancelListenerList != null ) && cancelListenerList.contains( listener ) )
        {
            cancelListenerList.remove( listener );
        }
    }


    // ── Broadcast the Cancel Signal to All Observers ──────────────────────────
    // Called both from setCanceled(true) and from the watcher job's polling
    // loop when it detects isCanceled() is true.
    // Package-protected so the watcher job can call it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all registered {@link CancelListener}s that a cancel has been
     * requested.  Called when {@link #setCanceled(boolean)} is set to
     * {@code true} and by the {@link StudioProgressMonitorWatcherJob} when it
     * detects the cancelled state.
     */
    /* Package protected */void fireCancelRequested()
    {
        CancelEvent event = new CancelEvent( this );

        if ( cancelListenerList != null )
        {
            for ( CancelListener cancelListener : cancelListenerList )
            {
                cancelListener.cancelRequested( event );
            }
        }
    }


    // ── Update the Mission Board Sub-Task Label ───────────────────────────────
    // We only forward the update at most once per second (the watcher resets
    // the gate) to avoid flooding the UI with progress updates.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reports a progress message to the UI at most once per second.
     * The {@link StudioProgressMonitorWatcherJob} resets the {@code allowMessageReporting}
     * gate once per second, so rapid-fire calls are silently dropped in between.
     *
     * @param message  the sub-task message to display.
     */
    public void reportProgress( String message )
    {
        boolean doReport = allowMessageReporting.getAndSet( false );

        if ( doReport )
        {
            subTask( message );
        }
    }


    // ── File an Error Without an Exception ───────────────────────────────────
    // Convenience overload; calls the two-arg version with a null exception.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Accumulates an error with the given message and no associated exception.
     *
     * @param message  the error message.
     */
    public void reportError( String message )
    {
        reportError( message, null );
    }


    // ── File an Error with Just an Exception ─────────────────────────────────
    // Convenience overload; calls the two-arg version with a null message.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Accumulates an error with just an exception; no explicit message.
     *
     * @param exception  the exception that caused the error.
     */
    public void reportError( Exception exception )
    {
        reportError( null, exception );
    }


    // ── File a Full Error Report ──────────────────────────────────────────────
    // Creates a Status object from the message + exception and appends it to the
    // error list.  If no errors were reported before we allocate the list lazily.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Accumulates an error status built from the given message and exception.
     * Either argument may be {@code null}.  The collected statuses are assembled
     * into a {@link MultiStatus} by {@link #getErrorStatus}.
     *
     * @param message    the error message, or {@code null}.
     * @param exception  the causing exception, or {@code null}.
     */
    public void reportError( String message, Exception exception )
    {
        if ( errorStatusList == null )
        {
            errorStatusList = new ArrayList<Status>( 3 );
        }

        if ( message == null )
        {
            message = ""; //$NON-NLS-1$
        }

        Status errorStatus = new Status( IStatus.ERROR, pluginId, IStatus.ERROR, message, exception );
        errorStatusList.add( errorStatus );
    }


    // ── Has Anything Gone Wrong? ──────────────────────────────────────────────
    // Returns true as soon as at least one error has been reported.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if at least one error has been reported via
     * {@link #reportError}.
     *
     * @return  {@code true} if any errors are accumulated.
     */
    public boolean errorsReported()
    {
        return errorStatusList != null;
    }


    // ── Assemble the Full Error Report for the Eclipse Dialog ────────────────
    // Builds a MultiStatus from all accumulated statuses.  The summary message
    // is the job's error message plus one bullet per status entry.  Stack traces
    // are appended as child-status messages so Eclipse's error dialog "Details"
    // section shows them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Assembles all accumulated error statuses into an Eclipse {@link IStatus}.
     * If no errors are present, returns {@link Status#CANCEL_STATUS} (when
     * cancelled) or {@link Status#OK_STATUS}.  Otherwise returns a
     * {@link MultiStatus} whose summary message is {@code message} plus a
     * bullet-list of individual status messages and exception texts.
     *
     * @param message  the job-level error message (e.g. "Error opening connection").
     * @return         an {@link IStatus} suitable for returning from {@link Job#run}.
     */
    public IStatus getErrorStatus( String message )
    {
        if ( ( errorStatusList == null ) || errorStatusList.isEmpty() )
        {
            if ( isCanceled() )
            {
                return Status.CANCEL_STATUS;
            }
            else
            {
                return Status.OK_STATUS;
            }
        }
        else
        {
            StringBuilder buffer = new StringBuilder();
            buffer.append( message );

            // append status messages to message
            for ( Status status : errorStatusList )
            {
                String statusMessage = status.getMessage();
                Throwable exception = status.getException();
                String exceptionMessage = null;

                if ( exception != null )
                {
                    exceptionMessage = exception.getMessage();
                }

                // append explicit status message
                if ( !StringUtils.isEmpty( statusMessage ) )
                {
                    buffer.append( "\n - " ).append( statusMessage );
                }

                // append exception message if different to status message
                if ( ( exception != null ) && ( exceptionMessage != null ) && !exceptionMessage.equals( statusMessage ) )
                {
                    // strip control characters
                    int indexOfAny = StringUtils.indexOfAny( exceptionMessage, "\n\r\t" ); //$NON-NLS-1$

                    if ( indexOfAny > -1 )
                    {
                        exceptionMessage = exceptionMessage.substring( 0, indexOfAny );
                    }

                    buffer.append( "\n - " ).append( exceptionMessage ); //$NON-NLS-1$
                }
            }

            // create main status
            MultiStatus multiStatus = new MultiStatus( pluginId, IStatus.ERROR, buffer.toString(), null );

            // append child status
            for ( Status status : errorStatusList )
            {
                String statusMessage = status.getMessage();

                if ( status.getException() != null )
                {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter( stringWriter );
                    status.getException().printStackTrace( printWriter );
                    statusMessage = stringWriter.toString();
                }

                multiStatus.add( new Status( status.getSeverity(), status.getPlugin(), status.getCode(), statusMessage,
                    status.getException() ) );
            }

            return multiStatus;
        }
    }


    // ── Get the First Accumulated Exception ───────────────────────────────────
    // Convenience method for callers that only care about the first error.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first accumulated {@link Exception}, or {@code null} if no
     * errors have been reported.
     *
     * @return  the first exception, or {@code null}.
     */
    public Exception getException()
    {
        if ( errorStatusList != null )
        {
            return ( Exception ) errorStatusList.get( 0 ).getException();
        }

        return null;
    }


    // ── Reset the Board Entry for Reuse ──────────────────────────────────────
    // Some monitor instances are reused across multiple runs; reset() clears
    // the done flag and the error list so the next run starts fresh.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Resets this monitor so it can be reused for another operation.
     * Clears the {@code isDone} flag and the accumulated error status list.
     */
    public void reset()
    {
        isDone = false;
        errorStatusList = null;
    }


    /**
     * A cancel event carrying the monitor that was cancelled.
     * Passed to all registered {@link CancelListener}s when the user
     * clicks Cancel or when {@link StudioProgressMonitor#setCanceled(boolean)}
     * is called with {@code true}.
     * Think of this as the abort-signal packet broadcast over the Yavin 4
     * mission command channel.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public static class CancelEvent
    {
        /** The Monitor used by the Cancel Event */
        private IProgressMonitor monitor;


        // ── Wrap the Cancelling Monitor in an Event ────────────────────────────
        // The event carries the monitor so listeners can query its state if needed.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Creates a CancelEvent for the given monitor.
         *
         * @param monitor  the progress monitor that has been cancelled.
         */
        public CancelEvent( IProgressMonitor monitor )
        {
            this.monitor = monitor;
        }


        // ── Retrieve the Cancelled Monitor ────────────────────────────────────
        // Listeners call this to check the monitor's state or, in multi-level
        // scenarios, to walk the wrapped chain.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Returns the progress monitor that raised this cancel event.
         *
         * @return  the cancelled {@link IProgressMonitor}.
         */
        public IProgressMonitor getMonitor()
        {
            return monitor;
        }
    }


    /**
     * Listener interface for cancellation notifications.
     * Register with {@link StudioProgressMonitor#addCancelListener} to be
     * notified when the user or the system cancels a running operation.
     * Think of this as the Alliance abort-signal receiver: any officer who needs
     * to know when a mission is aborted implements this interface.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    public interface CancelListener
    {
        // ── Handle the Abort Signal ────────────────────────────────────────────
        // The implementor should stop whatever long-running work it is doing as
        // quickly as safely possible.
        // ────────────────────────────────────────────────────────────────────────────
        /**
         * Called when the user or system requests cancellation of a running operation.
         *
         * @param event  the cancel event carrying the monitor that was cancelled.
         */
        void cancelRequested( CancelEvent event );
    }
}
