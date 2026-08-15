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
package org.apache.directory.studio.ldapbrowser.core.jobs;


import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;


// ── CLASS: StudioBrowserJob — THE CLONE ARMY QUEUES AND EXECUTES ORDERS ──────
// The Grand Army of the Republic doesn't act on individual initiative.  When
// a mission is assigned, the clone army: silences the comms channel so
// mid-operation status updates don't cause chaos (suspend events), executes the
// operation in disciplined formation, then opens the comms channel again so
// the command staff can hear the full after-action report (resume events).
// This class is the ldapbrowser-specific Eclipse Job wrapper.  Before running
// a background LDAP operation it suspends browser event firing (so the UI
// doesn't get half-baked notifications), and after running it resumes event
// firing so all queued notifications go out in one coherent burst.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse background job that runs one or more
 * {@link StudioConnectionRunnableWithProgress} runnables and brackets the
 * execution with browser-specific event-firing suspension/resumption.
 * Suspending events during the job prevents the LDAP browser UI from receiving
 * a flood of partial update notifications mid-operation.  After the job
 * completes, the parent's {@code resumeEventFiringInCurrentThread()} delivers
 * all deferred notifications in a single consistent batch.
 * Think of this as the clone army's mission discipline: comms blackout during
 * the operation, full debrief when it's done.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioBrowserJob extends StudioConnectionJob
{
    // ── The Commanding Officer Assigns The Mission ────────────────────────────────
    // The clone commander receives the mission briefing packets — one or more
    // runnables that describe what the troopers must do.  He logs them into
    // the mission queue and stands ready to execute when Eclipse schedules the job.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new StudioBrowserJob wrapping the given runnables.
     * The runnables are executed in order when the job runs.  Pass multiple
     * runnables when a logical operation requires several sequential LDAP steps.
     *
     * <p>For example — scheduling an attribute initialisation:</p>
     * <pre>
     *   StudioBrowserJob job = new StudioBrowserJob(new InitializeAttributesRunnable(entry));
     *   job.execute();
     * </pre>
     *
     * @param runnables the LDAP operations to execute; at least one must be provided.
     */
    public StudioBrowserJob( StudioConnectionRunnableWithProgress... runnables )
    {
        super( runnables );
    }


    // ── The Commander Cuts The Comms Channel Before The Mission ──────────────────
    // "Comms blackout — no status updates until the mission is complete."
    // We suspend browser event firing so the UI doesn't get confused by
    // partial-state notifications while the LDAP operation is in flight.
    // The parent class suspends connection-level events on top of ours.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Suspends browser-specific event firing on the current thread, then
     * delegates to the parent to suspend connection-level events.
     * Called automatically by the job framework before {@code run()} starts.
     */
    @Override
    protected void suspendEventFiringInCurrentThread()
    {
        EventRegistry.suspendEventFiringInCurrentThread();
        super.suspendEventFiringInCurrentThread();
    }


    // ── The Commander Restores The Comms Channel After The Mission ────────────────
    // "Mission complete — resume comms.  Send the full after-action report."
    // We resume browser event firing first (so our deferred notifications go
    // out), then delegate to the parent to resume connection-level events.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Resumes browser-specific event firing on the current thread, then
     * delegates to the parent to resume connection-level events.
     * Called automatically by the job framework after {@code run()} completes
     * (including on error), so deferred notifications always get delivered.
     */
    @Override
    protected void resumeEventFiringInCurrentThread()
    {
        EventRegistry.resumeEventFiringInCurrentThread();
        super.resumeEventFiringInCurrentThread();
    }
}
