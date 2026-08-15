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


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


// ── CLASS: StudioJob — Yavin 4 Mission Command Dispatching the Team ──────────
// Yavin 4 mission control accepts a stack of mission runnables, gives the
// whole operation the name of the first runnable, then runs them in sequence.
// For bulk runnables it suspends event broadcasts before run() and restores
// them after, then calls runNotification() for the debrief.
// If any runnable throws, the exception is captured in the monitor rather than
// crashing the thread.  At the end, if errors were accumulated we return an
// ERROR status; if the operator hit cancel we return CANCEL; otherwise OK.
// shouldSchedule() stops us from queuing a duplicate mission on the same entry.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse {@link Job} that executes one or more {@link StudioRunnableWithProgress} instances.
 * We wrap each runnable's execution in a {@link StudioProgressMonitor}, handle
 * bulk runnables by suspending/resuming event firing around {@code run()}, and
 * collect any thrown exceptions into the monitor rather than propagating them.
 * {@link #shouldSchedule()} prevents queuing a duplicate job that would operate
 * on the same locked objects as an already-running job of the same type.
 * Think of this as the mission dispatcher at Yavin 4: it takes a briefing stack,
 * runs each mission in order, and reports the outcome back to mission control.
 *
 * @param <T>  the type of runnable managed by this job.
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioJob<T extends StudioRunnableWithProgress> extends Job
{
    /** The runnables. */
    protected T[] runnables;


    // ── Accept the Mission Stack and Name the Operation ───────────────────────
    // The job borrows its display name from the first runnable so the progress
    // dialog shows a sensible label.  All provided runnables are stored so
    // run() can iterate them in order.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new StudioJob that will run the supplied runnables in sequence.
     * The job's display name is taken from {@code runnables[0].getName()}.
     *
     * @param runnables  one or more runnables to execute; must not be empty.
     */
    public StudioJob( T... runnables )
    {
        super( runnables[0].getName() );
        this.runnables = runnables;
    }


    // ── Execute All Missions in Order ─────────────────────────────────────────
    // We wrap the IProgressMonitor in a StudioProgressMonitor that adds cancel
    // listening and error accumulation.  For bulk runnables we suspend event
    // firing before run() and restore it after (in a finally block), then call
    // runNotification().  For plain runnables we just call run().  Any exception
    // is caught and reported through the monitor.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs all runnables in sequence inside a {@link StudioProgressMonitor}.
     * Handles bulk runnables by suspending/resuming event firing.
     * Any unchecked exception from a runnable is reported via
     * {@link StudioProgressMonitor#reportError(Exception)} rather than
     * propagating up.
     *
     * @param ipm  the Eclipse progress monitor provided by the Job framework.
     * @return     {@link Status#CANCEL_STATUS} if cancelled;
     *             an ERROR {@link IStatus} if errors were reported;
     *             {@link Status#OK_STATUS} otherwise.
     */
    protected IStatus run( IProgressMonitor ipm )
    {
        StudioProgressMonitor monitor = new StudioProgressMonitor( ipm );

        // Execute job
        if ( !monitor.errorsReported() )
        {
            try
            {
                for ( StudioRunnableWithProgress runnable : runnables )
                {
                    if ( runnable instanceof StudioBulkRunnableWithProgress )
                    {
                        StudioBulkRunnableWithProgress bulkRunnable = ( StudioBulkRunnableWithProgress ) runnable;
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


    // ── Silence the Event Board Before a Bulk Mission ────────────────────────
    // Subclasses override this to suspend model-change event firing on the
    // current thread so bulk writes don't flood listeners.  The default no-op
    // is correct for any subclass that doesn't use a framework-level event bus.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Hook called by {@link #run} immediately before executing a
     * {@link StudioBulkRunnableWithProgress#run}.
     * Subclasses should suppress model-change event firing on the current thread
     * so bulk operations don't trigger cascading UI updates.
     * The default implementation does nothing.
     */
    protected void suspendEventFiringInCurrentThread()
    {
        // Default implementation does nothing.
    }


    // ── Restore the Event Board After a Bulk Mission ─────────────────────────
    // Always called in a finally block so events are never permanently silenced
    // even if the bulk mission throws.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Hook called by {@link #run} in a {@code finally} block after executing a
     * {@link StudioBulkRunnableWithProgress#run}.
     * Subclasses should re-enable model-change event firing on the current thread.
     * The default implementation does nothing.
     */
    protected void resumeEventFiringInCurrentThread()
    {
        // Default implementation does nothing.
    }


    // ── Launch the Mission on the User's Behalf ───────────────────────────────
    // Calling execute() marks the job as user-visible (shows in the progress
    // dialog) and schedules it immediately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Marks this job as user-visible and schedules it for immediate execution.
     * This is the standard way to start a Studio job from UI code.
     */
    public void execute()
    {
        setUser( true );
        schedule();
    }


    // ── Check Whether This Mission Should Be Queued ───────────────────────────
    // We skip scheduling if an identical type of runnable is already running on
    // the same locked object(s).  We check every running StudioJob's runnables
    // for a class match, then compare lock identifiers: if any identifier in the
    // other job is a prefix of ours (or vice versa), we refuse to queue.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code false} if a job of the same runnable type is already running
     * on the same (or an ancestor/descendant) locked object, to prevent duplicate
     * concurrent operations on the same entry.
     * Falls back to {@code super.shouldSchedule()} when no conflict is found.
     *
     * @return  {@code false} to suppress scheduling; {@code true} to proceed.
     */
    public boolean shouldSchedule()
    {
        // We don't schedule a job if the same type of runnable should run
        // that works on the same entry as the current runnable.

        for ( T runnable : runnables )
        {
            Object[] myLockedObjects = runnable.getLockedObjects();
            String[] myLockedObjectsIdentifiers = getLockIdentifiers( myLockedObjects );

            Job[] jobs = getJobManager().find( null );

            for ( Job job : jobs )
            {
                if ( job instanceof StudioJob )
                {
                    @SuppressWarnings("unchecked")
                    StudioJob<StudioRunnableWithProgress> otherJob = ( StudioJob<StudioRunnableWithProgress> ) job;

                    for ( StudioRunnableWithProgress otherRunnable : otherJob.runnables )
                    {
                        if ( ( runnable.getClass() == otherRunnable.getClass() ) && ( runnable != otherRunnable ) )
                        {
                            Object[] otherLockedObjects = otherRunnable.getLockedObjects();
                            String[] otherLockedObjectIdentifiers = getLockIdentifiers( otherLockedObjects );

                            for ( String other : otherLockedObjectIdentifiers )
                            {
                                for ( String myLockedObjectIdentifier : myLockedObjectsIdentifiers )
                                {
                                    if ( other.startsWith( myLockedObjectIdentifier ) || myLockedObjectIdentifier.startsWith( other ) )
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


    // ── Convert Locked Objects to String Identifiers ──────────────────────────
    // Lock comparison is done by string prefix so DN hierarchies are handled:
    // a job on "dc=example,dc=com" blocks a job on "ou=People,dc=example,dc=com"
    // because the latter starts with the former.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Converts an array of objects to their string lock identifiers.
     * Used by {@link #shouldSchedule} to compare locked objects between jobs.
     *
     * @param objects  the objects to convert.
     * @return         an array of string identifiers, one per object.
     */
    protected String[] getLockIdentifiers( Object... objects )
    {
        String[] identifiers = new String[objects.length];

        for ( int i = 0; i < identifiers.length; i++ )
        {
            identifiers[i] = getLockIdentifier( objects[i] );
        }

        return identifiers;
    }


    // ── Get the Lock String for a Single Object ───────────────────────────────
    // Null objects get the literal string "null" so the comparison logic always
    // has a safe string to work with.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lock identifier string for a single object.
     * {@code null} maps to the literal string {@code "null"}; any other object
     * maps to its {@code toString()} value.
     *
     * @param object  the object to identify.
     * @return        the string identifier.
     */
    private String getLockIdentifier( Object object )
    {
        if ( object == null )
        {
            return "null";
        }
        else
        {
            return object.toString();
        }
    }


    // ── Tell the Scheduler This Job Belongs to the Alliance Family ────────────
    // Eclipse's job scheduler lets callers ask "is there a job running for
    // family X?" via IJobManager.find(family).  We answer true for our family.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this job belongs to the given family.
     * We belong to the {@link #getFamily()} family, which is
     * {@link CommonCoreConstants#JOB_FAMILY_ID}.
     *
     * @param family  the family object to test.
     * @return        {@code true} if {@code family.equals(getFamily())}.
     * @see Job#belongsTo(Object)
     */
    public boolean belongsTo( Object family )
    {
        return getFamily().equals( family );
    }


    // ── Return Our Alliance Family Badge ──────────────────────────────────────
    // Subclasses may override to join a narrower family if they want finer-
    // grained job-manager queries.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the family object for this job.
     * All Studio jobs belong to {@link CommonCoreConstants#JOB_FAMILY_ID} by default.
     * Subclasses may override to return a narrower family.
     *
     * @return  the family identifier.
     */
    public Object getFamily()
    {
        return CommonCoreConstants.JOB_FAMILY_ID;
    }
}
