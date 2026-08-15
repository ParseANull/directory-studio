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


// ── CLASS: StudioRunnableWithProgressAdapter — The Quick-Mission Briefing Form ─
// Most rebel missions don't lock specific entries (they operate on the whole
// connection) and don't need a custom error message — they're happy with the
// default empty string.  Rather than forcing every implementor to write the
// same two trivial methods, we provide this adapter: extend it, implement
// run() and getName(), and the other two methods are already handled.
// Think of this as the standard-issue quick-mission form at Yavin 4: fill in
// the mission name and the mission plan; leave the locked-objects and error-
// message fields blank and the desk officer fills them in with defaults.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Convenience base class for {@link StudioRunnableWithProgress} implementations.
 * Provides no-op / default implementations of {@link #getLockedObjects()} and
 * {@link #getErrorMessage()} so subclasses only need to implement {@link #run}
 * and {@link #getName}.
 * Think of this as the standard quick-mission briefing form: fill in the
 * work and the name; the rest uses sensible defaults.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class StudioRunnableWithProgressAdapter implements StudioRunnableWithProgress
{
    /** The locked objects */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


    // ── No Entries Locked — Open to All ──────────────────────────────────────
    // By default a quick mission doesn't compete with any other mission for
    // entry locks; the scheduler will never suppress it because of locking.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty array — this runnable does not lock any specific objects
     * by default.  Override if the subclass needs to prevent concurrent runs
     * on the same entry.
     *
     * @return  an empty object array.
     */
    public Object[] getLockedObjects()
    {
        return EMPTY_OBJECT_ARRAY;
    }


    // ── Default Error Message Is Empty ────────────────────────────────────────
    // If the mission fails, we return an empty string; the Eclipse error dialog
    // will still show whatever exception message was reported.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an empty string — the default error message.
     * Override to provide a meaningful message shown in the Eclipse error dialog
     * when this runnable reports an error.
     *
     * @return  an empty string.
     */
    public String getErrorMessage()
    {
        return ""; //$NON-NLS-1$
    }
}
