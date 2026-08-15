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

package org.apache.directory.studio.ldapservers.jobs;


import org.apache.directory.studio.common.core.jobs.StudioJob;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.apache.directory.studio.ldapservers.model.LdapServer;


// ── CLASS: StudioLdapServerJob — THE MILLENNIUM FALCON'S JOB SCHEDULER ───────────────────
// The Millennium Falcon doesn't just fly itself — Han Solo's ship needs a flight computer
// that knows how to schedule jumps, prevent two systems from firing at once, and identify
// each ship uniquely in the hyperspace lane so routes don't collide.
// This class extends the generic StudioJob with LDAP-server-specific lock identifier logic
// so Eclipse knows which server is busy and can serialise concurrent operations correctly.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The Eclipse {@link org.eclipse.core.runtime.jobs.Job} wrapper for all LDAP server background operations.
 * Extends {@link StudioJob} to add server-aware lock identifier resolution — when Eclipse
 * needs to prevent two jobs from touching the same server simultaneously, it uses the
 * server's UUID as the lock key.
 * Think of it as the Falcon's flight computer: it knows how to uniquely identify each ship
 * in the hyperspace lane and prevent scheduling conflicts.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StudioLdapServerJob extends StudioJob<StudioRunnableWithProgress>
{
    // ── Han Briefs The Flight Computer On The Mission ────────────────────────────────────────
    // Han punches in the runnables — start sequence, stop sequence, whatever the mission needs —
    // and the flight computer queues them up for execution.
    // We pass the runnables through to StudioJob which handles the Eclipse scheduling machinery.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new server job wrapping the given runnable(s).
     * The runnables are executed sequentially in the Eclipse job framework; this class adds
     * the lock-identifier logic on top.
     *
     * <p>For example — Han programs the Falcon's jump sequence:</p>
     * <pre>
     *   new StudioLdapServerJob(new StartLdapServerRunnable(server))
     *   → schedules the start job; Eclipse ensures no other job holds a lock on the same server.
     * </pre>
     *
     * @param runnables  one or more {@link StudioRunnableWithProgress} tasks to run in sequence
     */
    public StudioLdapServerJob( StudioRunnableWithProgress... runnables )
    {
        super( runnables );
    }


    // ── The Flight Computer Identifies Each Ship In The Lane ─────────────────────────────────
    // The hyperspace lane controller gets a list of objects and needs a unique string ID for each
    // one so it can track occupancy — the Falcon by name, cargo containers by serial number.
    // We convert each object to a string lock key, with special handling for LdapServer instances.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Converts an array of lock-target objects into an array of string lock identifiers.
     * Eclipse uses these strings to detect conflicts between concurrent jobs — if two jobs
     * list the same lock identifier, only one runs at a time.
     * {@link LdapServer} objects are identified by their UUID; all other objects use toString().
     *
     * @param objects  the lock targets declared by the runnable's {@code getLockedObjects()}
     * @return an array of unique string identifiers, one per object
     */
    @Override
    protected String[] getLockIdentifiers( Object... objects )
    {
        String[] identifiers = new String[objects.length];
        for ( int i = 0; i < identifiers.length; i++ )
        {
            Object o = objects[i];
            if ( o instanceof LdapServer )
            {
                identifiers[i] = getLockIdentifier( ( LdapServer ) o );
            }
            else
            {
                identifiers[i] = getLockIdentifier( objects[i] );
            }
        }
        return identifiers;
    }


    // ── The Falcon's Transponder Beacon ──────────────────────────────────────────────────────
    // The Falcon's transponder emits its unique beacon ID so the hyperspace controller
    // can track it — "YT-1300 light freighter, ID: Millennium Falcon."
    // We use the server's UUID as its unique lock key — stable across renames.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the lock identifier for a specific {@link LdapServer} — its UUID.
     * UUIDs are stable even if the server is renamed, making them the right choice for locking.
     *
     * @param server  the server to identify
     * @return the server's UUID string
     */
    private String getLockIdentifier( LdapServer server )
    {
        return server.getId();
    }


    // ── Generic Cargo Container Serial Number ────────────────────────────────────────────────
    // For anything that isn't a known ship, the controller uses whatever serial number is
    // printed on the side — toString() of the object.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a fallback lock identifier for non-{@link LdapServer} objects — their
     * {@code toString()} value, or {@code "null"} if the object is null.
     *
     * @param object  the generic lock target
     * @return a string representation suitable for use as a lock key
     */
    private String getLockIdentifier( Object object )
    {
        return ( object != null ? object.toString() : "null" ); //$NON-NLS-1$
    }
}
