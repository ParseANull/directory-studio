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


// ── CLASS: StudioBulkRunnableWithProgress — Silent Bulk Missions with Debrief ─
// When the Rebel Alliance plans a large-scale operation (e.g. simultaneously
// downloading the Death Star plans from multiple nodes) it doesn't want every
// sub-task firing event notifications mid-flight — that would flood the command
// board and confuse the officers.  Instead, event firing is suspended for the
// entire operation, and only after the last task completes does the mission
// command broadcast all the results in one go via the debrief (runNotification).
// StudioBulkRunnableWithProgress is the contract for those "silent bulk" missions:
// it extends StudioRunnableWithProgress with a runNotification() hook that
// StudioJob calls after run() finishes and events are re-enabled.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link StudioRunnableWithProgress} variant for bulk operations that need
 * event-notification suppression during the work phase.
 * When {@link StudioJob} executes a StudioBulkRunnableWithProgress it calls
 * {@link #run} with event firing suspended, then re-enables events and
 * calls {@link #runNotification} so listeners receive one batch update instead
 * of a storm of individual events.
 * Think of this as a large-scale Rebel operation that goes silent during
 * execution and then debriefs the command board all at once when it's done.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface StudioBulkRunnableWithProgress extends StudioRunnableWithProgress
{
    // ── Debrief Mission Control After the Silent Operation ───────────────────
    // Event firing is re-enabled before this is called; this is the moment to
    // fire whatever notifications the bulk operation accumulated during run().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Fires all notifications accumulated during the silent bulk {@link #run} phase.
     * Called by {@link StudioJob} after {@code run()} returns and event firing
     * has been re-enabled in the current thread.
     *
     * @param monitor  the progress monitor, in case notification itself needs
     *                 to report progress or check for cancellation.
     */
    void runNotification( StudioProgressMonitor monitor );
}
