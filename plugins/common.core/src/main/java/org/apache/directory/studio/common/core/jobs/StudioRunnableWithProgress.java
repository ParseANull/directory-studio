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


// ── CLASS: StudioRunnableWithProgress — The Rebel Mission Contract ────────────
// Every mission briefing handed to Yavin 4 mission control must answer four
// questions: (1) what is the codename of this mission (getName)?  (2) which
// entries does it lock so no parallel mission touches the same data
// (getLockedObjects)?  (3) what do we tell the operators if things go wrong
// (getErrorMessage)?  (4) carry out the mission and update the display board
// as you go (run).  StudioRunnableWithProgress is that mission contract: the
// interface every piece of Studio background work must implement.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Contract for all Studio background operations run by {@link StudioJob}.
 * Implementors supply a human-readable name (shown in the progress dialog),
 * the objects they lock (used by {@code StudioJob} to prevent duplicate
 * concurrent runs), an error message for the status dialog, and the actual
 * work in {@link #run}.
 * Think of this as the Rebel mission briefing form: fill it out and hand it
 * to mission control (StudioJob) to be dispatched.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface StudioRunnableWithProgress
{
    // ── Execute the Mission ───────────────────────────────────────────────────
    // This is where the rebel team does the actual work.  Progress updates go
    // through the monitor so the command board stays current.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Carries out the background work represented by this runnable.
     * Called by {@link StudioJob} on a worker thread.  Report progress through
     * {@code monitor.reportProgress()} and check {@code monitor.isCanceled()}
     * periodically if the work is long-running.
     *
     * @param monitor  the progress monitor for this run.
     */
    void run( StudioProgressMonitor monitor );


    // ── Declare Which Entries This Mission Locks ──────────────────────────────
    // The scheduler uses this to avoid running two missions that would write
    // to the same LDAP entry concurrently.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects (typically LDAP entries or connections) this runnable
     * intends to modify.  {@link StudioJob#shouldSchedule} compares these with
     * any already-running job's locked objects to avoid concurrent writes.
     *
     * @return  the objects to lock; never {@code null} (return an empty array
     *          if no locking is needed).
     */
    Object[] getLockedObjects();


    // ── Supply the Failure Debrief Message ───────────────────────────────────
    // If the mission fails, mission control needs a human-readable sentence
    // to show in the error dialog.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable error message shown in the Eclipse error dialog
     * if this runnable's {@link #run} reports an error.
     *
     * @return  the error message string; never {@code null}.
     */
    String getErrorMessage();


    // ── Supply the Mission Codename for the Progress Dialog ───────────────────
    // Shown in the Eclipse Jobs progress dialog while the mission is running.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable name of this operation, shown in the Eclipse
     * progress dialog while the job is running.
     *
     * @return  the operation name; never {@code null}.
     */
    String getName();
}
