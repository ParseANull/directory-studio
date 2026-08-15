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

package org.apache.directory.studio.ldapservers.apacheds;


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;


// ── CLASS: RepairRunnable — Chewie Fixing the Falcon's Hyperdrive ─────────────
// When the Falcon's hyperdrive partitions get corrupted (bad shutdown, power
// loss, or a Star Destroyer knocking out the power), Chewbacca crawls into the
// engine room and runs ApacheDS in "repair" mode to rebuild the partition index
// files.  He marks the ship as REPAIRING so no one tries to launch during the
// fix, then waits for the repair JVM process to finish before cleaning up.
// RepairRunnable implements that repair mission: a StudioRunnableWithProgress
// that sets the server status, calls adapter.repair(), and reports any failure
// back through the monitor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link StudioRunnableWithProgress} that repairs the partitions of an
 * ApacheDS 2.0.0 server instance.
 * We set the server status to {@code REPAIRING}, delegate to
 * {@link ApacheDS200LdapServerAdapter#repair}, and report any exception
 * through the progress monitor so the error dialog shows it.
 * Think of this as Chewie crawling into the Falcon's engine room to rebuild
 * the corrupted hyperdrive partition indices.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RepairRunnable implements StudioRunnableWithProgress
{
    /** The server */
    private LdapServer server;


    // ── Accept the Ship That Needs Fixing ─────────────────────────────────────
    // We store the server reference so every method below can use it to identify
    // the server in status updates, error messages, and lock declarations.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a RepairRunnable for the given server.
     *
     * @param server  the LDAP server whose partitions need repairing.
     */
    public RepairRunnable( LdapServer server )
    {
        this.server = server;
    }


    // ── Supply the Failure Debrief Message ────────────────────────────────────
    // If the repair fails we tell the operator which ship was being fixed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message shown in the Eclipse error dialog if repair fails.
     *
     * @return  a localised error message naming the server.
     */
    public String getErrorMessage()
    {
        return NLS.bind( Messages.getString( "RepairRunnable.UnableToRepair" ), new String[] //$NON-NLS-1$
            { server.getName() } );
    }


    // ── Declare the Ship Is Locked While Being Repaired ───────────────────────
    // Returning the server object prevents another job from starting or stopping
    // this same server while the repair is underway.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the server as the only locked object, preventing concurrent
     * start/stop jobs from running on the same server during repair.
     *
     * @return  an array containing just this server.
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { server };
    }


    // ── Supply the Mission Codename for the Progress Dialog ───────────────────
    // The progress dialog shows this name so the operator can see which
    // server is being repaired.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable name for this repair operation.
     *
     * @return  a localised string like "Repairing 'MyServer'".
     */
    public String getName()
    {
        return NLS.bind( Messages.getString( "RepairRunnable.Repair" ), new String[] //$NON-NLS-1$
            { server.getName() } );
    }


    // ── Chewie Crawls into the Engine Room ────────────────────────────────────
    // We flip the server to REPAIRING state, let the adapter launch ApacheDS in
    // repair mode and wait for it to finish, then clean up.  Any exception is
    // caught, the server is set back to STOPPED, and the error goes into the
    // monitor for the operator to see.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Runs the repair.
     * Sets the server status to {@code REPAIRING}, calls
     * {@link ApacheDS200LdapServerAdapter#repair}, and on failure sets the
     * status back to {@code STOPPED} and reports the exception.
     *
     * @param monitor  the progress monitor for this job.
     */
    public void run( StudioProgressMonitor monitor )
    {
        // Setting the status on the server to 'repairing'
        server.setStatus( LdapServerStatus.REPAIRING );

        try
        {
            ApacheDS200LdapServerAdapter adapter = new ApacheDS200LdapServerAdapter();
            adapter.repair( server, monitor );
        }
        catch ( Exception e )
        {
            // Setting the server as stopped
            server.setStatus( LdapServerStatus.STOPPED );

            // Reporting the error to the monitor
            monitor.reportError( e );
        }
    }
}
