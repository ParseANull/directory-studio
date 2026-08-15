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


import org.apache.directory.studio.connection.core.event.EventRunnable;
import org.apache.directory.studio.connection.core.event.EventRunner;
import org.eclipse.swt.widgets.Display;


// ── CLASS: UiThreadEventRunner — THE SWT INTERCOM ────────────────────────────────
// In the Falcon's crew, if someone in the engine room (a background thread) needs
// the pilot to flip a switch on the cockpit panel (update the SWT UI), they can't
// just reach over — they have to use the intercom.  UiThreadEventRunner is that
// intercom.
// ConnectionEventRegistry fires EventRunnables to notify listeners of connection
// events.  If an event is fired from an LDAP background thread, the listener (a
// JFace TreeViewer, for example) must be updated on the SWT UI thread or we get
// invalid-thread-access errors.
// This implementation delegates to Display.asyncExec, which queues the runnable
// for execution on the SWT event loop.  Async (not sync) keeps the background
// thread from blocking while the UI processes the event.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link EventRunner} implementation that executes {@link EventRunnable}s on the
 * SWT UI thread via {@link Display#asyncExec}.
 *
 * <p>Registered with {@link ConnectionCorePlugin} by {@link ConnectionUIPlugin#start}
 * so that all connection state events (opened, closed, updated, etc.) are delivered
 * on the SWT thread, allowing listeners (views, label providers) to update their
 * widgets safely.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UiThreadEventRunner implements EventRunner
{
    // ── EXECUTE — POST TO THE SWT EVENT LOOP ──────────────────────────────────────
    /**
     * {@inheritDoc}
     * Queues the given {@link EventRunnable} for asynchronous execution on the
     * SWT UI thread.  The caller is not blocked; the runnable executes on the
     * next pass of the SWT event loop.
     *
     * @param runnable  The event runnable to dispatch on the UI thread.
     */
    public void execute( EventRunnable runnable )
    {
        Display.getDefault().asyncExec( runnable );
    }
}
