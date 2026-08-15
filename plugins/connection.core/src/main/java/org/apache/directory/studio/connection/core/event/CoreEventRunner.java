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


// ── CLASS: CoreEventRunner — THE REBEL COURIER WHO DELIVERS IMMEDIATELY ────────
// The simplest possible courier: he takes the dispatch and runs it right now,
// in the same thread, without any scheduling or queuing.  Fast, simple, and
// appropriate for background threads that don't need to touch the UI.
// The UI plugin can swap in a fancier courier that queues dispatch on the
// SWT event thread for safe widget updates.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Default synchronous implementation of {@link EventRunner}.
 * We execute the given {@link EventRunnable} directly in the current thread,
 * with no queuing or thread switching.
 * This is the implementation used when registering listeners from non-UI code
 * (background jobs, unit tests) where updating a widget is not required.
 * The connection.ui plugin registers listeners with a UI-thread runner instead,
 * so SWT widgets are updated safely.
 * Think of this as the no-frills rebel courier: he reads the dispatch and acts
 * on it immediately, right there in the hallway.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CoreEventRunner implements EventRunner
{

    // ── EXECUTE — RUN THE DISPATCH RIGHT NOW ──────────────────────────────────────
    // The courier grabs the dispatch and runs it immediately in the current thread.
    // No queuing, no thread switching — just immediate synchronous execution.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the given {@link EventRunnable} synchronously in the current thread.
     *
     * @param runnable  The event notification to run immediately.
     */
    public void execute( EventRunnable runnable )
    {
        runnable.run();
    }

}
