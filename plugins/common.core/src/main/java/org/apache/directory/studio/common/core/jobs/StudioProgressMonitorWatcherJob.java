/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor contract agreements.  See the NOTICE file
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


import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


// ── CLASS: StudioProgressMonitorWatcherJob — The Yavin 4 Sentinel Officer ────
// The Yavin 4 command room has one dedicated sentinel officer who walks the
// boards once per second.  For each active mission monitor he checks:
// (1) is the mission still running and not cancelled?  Reset the message-rate
//     gate (allowMessageReporting) so the next progress update goes through.
// (2) is the operator requesting abort?  Fire the cancel signal immediately.
// (3) is the mission complete or cancelled?  Remove it from the watch list.
// The sentinel runs as a system (hidden) Job and sleeps 1 000 ms between rounds.
// It stops its loop when stop() is called (typically at bundle shutdown).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background system {@link Job} that polls all registered
 * {@link StudioProgressMonitor} instances once per second.
 * For each monitor it resets the message-rate gate (so progress updates are
 * allowed again), checks for cancellation (firing {@code fireCancelRequested}
 * if needed), and removes any completed or cancelled monitors from the watch list.
 * Think of this as the Yavin 4 sentinel: walking the mission boards every
 * second to keep the status current and propagate any abort orders.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioProgressMonitorWatcherJob extends Job
{

    /** The running flag */
    private final AtomicBoolean running;

    /** The list of active monitors being watched */
    private final ConcurrentLinkedQueue<StudioProgressMonitor> monitors;


    // ── Sentinel Reports for Duty ─────────────────────────────────────────────
    // We give the job a human-readable name (not shown to users since it's
    // marked as a system job) and initialise the running flag to true.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the watcher job, ready to be scheduled.
     * The job's display name is looked up from the message resource bundle.
     */
    public StudioProgressMonitorWatcherJob()
    {
        super( Messages.getString( "StudioProgressMonitor.CheckCancellation" ) );
        running = new AtomicBoolean( true );
        monitors = new ConcurrentLinkedQueue<StudioProgressMonitor>();
    }


    // ── Sentinel Stands Down at Bundle Shutdown ───────────────────────────────
    // Setting running to false causes the while loop in run() to exit on the
    // next iteration, letting the Job thread finish cleanly.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Signals the sentinel loop to exit on its next iteration.
     * Called by {@link CommonCorePlugin#stop} during bundle deactivation.
     */
    public void stop()
    {
        running.set( false );
    }


    // ── Register a New Mission Monitor for Surveillance ───────────────────────
    // Every StudioProgressMonitor constructor calls this so the sentinel knows
    // to watch it.  ConcurrentLinkedQueue handles concurrent access without
    // additional synchronization.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a {@link StudioProgressMonitor} to the list of monitors being watched.
     * Called by {@link StudioProgressMonitor}'s constructor.
     *
     * @param monitor  the monitor to watch.
     */
    public void addMonitor( StudioProgressMonitor monitor )
    {
        monitors.add( monitor );
    }


    // ── Sentinel's Patrol Loop ────────────────────────────────────────────────
    // Runs continuously while running is true, sleeping 1 s between rounds.
    // For each monitor (and each inner wrapped monitor) it resets the message
    // gate, fires cancel events if needed, and removes done/cancelled monitors.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * The sentinel's main patrol loop — runs until {@link #stop()} is called.
     * Each iteration visits every registered monitor, resets message-rate gates
     * for active monitors, propagates cancel events, and removes finished ones.
     * Sleeps for 1 000 ms between iterations.
     *
     * @param monitor  the Eclipse progress monitor for this job (not used — the
     *                 sentinel doesn't report its own progress).
     * @return         {@link Status#OK_STATUS} when the loop exits.
     */
    @Override
    protected IStatus run( IProgressMonitor monitor )
    {
        while ( running.get() )
        {
            for ( Iterator<StudioProgressMonitor> it = monitors.iterator(); it.hasNext(); )
            {
                StudioProgressMonitor next = it.next();
                StudioProgressMonitor spm = next;

                do
                {
                    // reset allow message reporting
                    if ( !spm.isCanceled() && !spm.isDone )
                    {
                        spm.allowMessageReporting.set( true );
                    }

                    // check if canceled
                    if ( spm.isCanceled() )
                    {
                        spm.fireCancelRequested();
                    }

                    if ( spm.isCanceled() || spm.isDone )
                    {
                        it.remove();
                        break;
                    }

                    if ( spm.getWrappedProgressMonitor() instanceof StudioProgressMonitor )
                    {
                        spm = ( StudioProgressMonitor ) spm.getWrappedProgressMonitor();
                    }
                    else
                    {
                        spm = null;
                    }
                }
                while ( spm != null );
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
            }
        }

        return Status.OK_STATUS;
    }
}
