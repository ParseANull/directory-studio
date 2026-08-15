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


// ── INTERFACE: StudioConnectionBulkRunnableWithProgress — THE BULK MISSION PLAN ──
// Some missions move a lot of cargo at once — you don't want the Falcon's status
// panel blinking after every crate.  Instead, you suppress the notifications while
// the cargo is being loaded, then fire a single "cargo loaded" dispatch at the end.
// This interface adds a runNotification() method to the runnable contract.
// StudioConnectionJob calls run() with event firing suppressed, then calls
// runNotification() (with event firing re-enabled) so listeners get one clean update.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Extension of {@link StudioConnectionRunnableWithProgress} for bulk operations that
 * should suppress connection event notifications during execution and then fire them
 * all at once afterward.
 * When {@link StudioConnectionJob} encounters this interface, it:
 * <ol>
 *   <li>Calls {@link org.apache.directory.studio.connection.core.event.ConnectionEventRegistry#suspendEventFiringInCurrentThread()}.</li>
 *   <li>Calls {@link #run(StudioProgressMonitor)} to execute the bulk work.</li>
 *   <li>Calls {@link org.apache.directory.studio.connection.core.event.ConnectionEventRegistry#resumeEventFiringInCurrentThread()}.</li>
 *   <li>Calls {@link #runNotification(StudioProgressMonitor)} to fire all accumulated events.</li>
 * </ol>
 * Think of this as the "bulk cargo mission" pattern: silence the status board while
 * loading, then do a single broadcast when the hold is full.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface StudioConnectionBulkRunnableWithProgress extends StudioConnectionRunnableWithProgress
{
    // ── RUN NOTIFICATION — FIRE THE POST-BULK BROADCAST ──────────────────────────
    // After run() finishes and event firing is re-enabled, the job calls this
    // method so we can send the appropriate connection update events.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Fires connection update notifications after the bulk {@link #run(StudioProgressMonitor)}
     * has completed and event firing has been re-enabled.
     * Implementations should call the relevant
     * {@code ConnectionEventRegistry.fire*()} methods here.
     *
     * @param monitor  The progress monitor from the enclosing job.
     */
    void runNotification( StudioProgressMonitor monitor );
}
