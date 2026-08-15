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


import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgressAdapter;
import org.apache.directory.studio.connection.core.Connection;


// ── CLASS: StudioConnectionRunnableWithProgressAdapter — HAN'S DEFAULT MISSION STUB ──
// Not every mission needs a ship from the pre-launch checklist — some runnables
// manage their own connection or don't need one at all.  Instead of implementing
// getConnections() everywhere and returning an empty array by hand, those
// runnables just extend this adapter and get the default "no connections needed"
// behaviour for free.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Convenience abstract base class for {@link StudioConnectionRunnableWithProgress}
 * implementations that need no pre-opened connections.
 * Extends {@link StudioRunnableWithProgressAdapter} and overrides
 * {@link #getConnections()} to return an empty array, so subclasses only need to
 * implement the parts that actually differ ({@code getName()}, {@code run()},
 * {@code getErrorMessage()}, {@code getLockedObjects()}).
 * Think of this as Han's default mission template: pre-fill the "no ship needed"
 * fields and let each specific mission fill in the rest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class StudioConnectionRunnableWithProgressAdapter extends StudioRunnableWithProgressAdapter implements
    StudioConnectionRunnableWithProgress
{
    /** Empty array returned by the default getConnections() implementation. */
    private static final Connection[] EMPTY_CONNECTION_ARRAY = new Connection[0];


    // ── GET CONNECTIONS — DEFAULT: NO CONNECTIONS REQUIRED ────────────────────────
    // Subclasses that do need connections should override this.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty {@link Connection} array, indicating that no connections
     * need to be pre-opened before this runnable executes.
     * Override this in subclasses that require live connections.
     *
     * @return  An empty {@code Connection[]} array.
     */
    public Connection[] getConnections()
    {
        return EMPTY_CONNECTION_ARRAY;
    }
}
