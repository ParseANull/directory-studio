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
package org.apache.directory.studio.connection.core.event;


// ── CLASS: EventRunner — THE REBEL COURIER WHO DELIVERS DISPATCHES ────────────
// Rebel command sends dispatches (EventRunnables) but doesn't decide HOW they
// get delivered — that's the courier's job.  One courier runs immediately in
// the current thread; another queues the dispatch and delivers it on the UI thread.
// This interface is the courier contract: given a dispatch, deliver it.
// Implementations decide the delivery thread.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Interface that executes an {@link EventRunnable}, controlling which thread
 * the event notification runs on.
 * The basic {@link CoreEventRunner} executes synchronously in the current thread.
 * The UI plugin contributes a runner that dispatches to the SWT UI thread so
 * listeners can safely update widgets.
 * Callers register a runner alongside each listener so the event system knows
 * exactly how to deliver notifications to that listener.
 * Think of this as the Rebel courier: one courier (CoreEventRunner) runs
 * immediately; another queues the dispatch for the right delivery thread.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EventRunner
{
    // ── EXECUTE — DELIVER THE DISPATCH ───────────────────────────────────────────
    // The courier delivers the encoded dispatch to its destination.
    // Implementations decide whether this happens immediately in the calling thread,
    // asynchronously, or on the SWT UI thread.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the given {@link EventRunnable}, delivering the event notification.
     * The implementation decides which thread runs the runnable.
     * Callers must not make assumptions about the execution thread.
     *
     * @param runnable  The event notification to execute.
     */
    void execute( EventRunnable runnable );
}
