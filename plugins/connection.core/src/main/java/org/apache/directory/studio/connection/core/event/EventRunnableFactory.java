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


// ── CLASS: EventRunnableFactory — REBEL COMMAND ENCODING DISPATCHES ───────────
// When Rebel command needs to send the same order to multiple crews, they have
// an encoding officer who creates one personalized dispatch packet per crew:
// the order is the same, but each packet is addressed to a specific recipient.
// This interface is the encoding officer: given a listener, it creates a
// personalized {@link EventRunnable} that, when run, delivers the event to
// exactly that listener.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Factory interface that creates {@link EventRunnable} objects for a specific
 * connection event.
 * The {@link ConnectionEventRegistry.EventManager} calls this factory once per
 * registered listener to produce a targeted dispatch; it then hands each dispatch
 * to the appropriate {@link EventRunner} for execution.
 * Think of this as Rebel command's encoding officer: given a listener (the crew),
 * she produces a personalized dispatch that will notify exactly that listener.
 *
 * @param <L>  The listener type the factory creates runnables for.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EventRunnableFactory<L>
{

    // ── CREATE EVENT RUNNABLE — ENCODE A PERSONALIZED DISPATCH ───────────────────
    // The encoding officer takes the recipient (listener) and packages the event
    // into a dispatch addressed specifically to them.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an {@link EventRunnable} that, when executed, delivers a specific
     * connection event to the given listener.
     * The factory implementation captures the event data as a closure and calls the
     * appropriate listener method from within {@link EventRunnable#run()}.
     *
     * @param listener  The listener that will receive the event notification.
     * @return  An {@link EventRunnable} targeted at the given listener.
     */
    EventRunnable createEventRunnable( L listener );
}
