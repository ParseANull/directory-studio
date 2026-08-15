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


// ── CLASS: EventRunnable — HAN'S ENCODED MISSION DISPATCH ─────────────────────
// When Rebel command needs to transmit an order to Han's crew, they encode the
// complete action into a self-contained dispatch packet: "do this exact thing
// when you receive it."  The dispatch is runnable — you just execute it.
// This interface is that dispatch packet: a {@link Runnable} that, when
// executed, notifies a specific listener about a specific connection event.
// The {@link EventRunner} decides which thread runs the dispatch.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A tagging interface that marks a {@link Runnable} as a connection event notification.
 * Each {@code EventRunnable} is created by an {@link EventRunnableFactory} and captures
 * a specific listener and a specific event (e.g. "call
 * {@link ConnectionUpdateListener#connectionOpened(org.apache.directory.studio.connection.core.Connection)}
 * on this listener").
 * The {@link EventRunner} then decides which thread invokes {@link #run()}.
 * Think of this as Han's encoded mission dispatch: a self-contained task that
 * carries everything needed to notify one listener about one event.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface EventRunnable extends Runnable
{
}
