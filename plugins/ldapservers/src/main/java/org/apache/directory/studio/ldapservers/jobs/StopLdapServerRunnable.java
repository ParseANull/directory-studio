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


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapter;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapterExtension;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;


// ── CLASS: StopLdapServerRunnable — THE DEATH STAR POWERS DOWN AFTER YAVIN ───────────────
// After Luke's proton torpedoes hit the exhaust port, the Death Star's reactor starts to
// shut down — power drains, systems wind down one by one, until the station goes dark.
// This runnable does the equivalent for an LDAP server: marks it as STOPPING, spawns a
// watchdog to monitor the shutdown, then calls the adapter's stop() method to actually
// terminate the server process.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A background {@link Job} runnable that stops a running LDAP server.
 * The sequence mirrors {@link StartLdapServerRunnable}: (1) mark STOPPING, (2) spawn a
 * {@link StopLdapServerWatchDogThread}, (3) call the adapter's {@code stop()} method.
 * If the adapter throws, we revert to STARTED (because we couldn't stop it) and report.
 * Think of it as the reactor shutdown sequence — orderly, watched, and rolled back on failure.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StopLdapServerRunnable implements StudioRunnableWithProgress
{
    /** The server */
    private LdapServer server;


    // ── Luke Lines Up His Shot ───────────────────────────────────────────────────────────────
    // Luke locks in on the exhaust port — this specific station, this specific reactor.
    // We record the target server so the run() method knows exactly what to stop.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new stop runnable for the given server.
     * The actual shutdown logic runs in {@link #run}; this constructor just captures the target.
     *
     * @param server  the LDAP server to stop
     */
    public StopLdapServerRunnable( LdapServer server )
    {
        this.server = server;
    }


    // ── The Status Board Failure Message ────────────────────────────────────────────────────
    // If the shutdown fails — process refused to terminate — the status board reports:
    // "Unable to stop [station name]."
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message to display or log if the stop operation fails.
     *
     * @return a localized "Unable to stop server [name]" string
     */
    public String getErrorMessage()
    {
        return NLS.bind( Messages.getString( "StopLdapServerRunnable.UnableToStopServer" ), new String[] //$NON-NLS-1$
            { server.getName() } );
    }


    // ── Locking The Station During Shutdown ──────────────────────────────────────────────────
    // While the Death Star is powering down, no one touches the controls — the station is locked.
    // We declare the server as the exclusive lock so concurrent jobs don't interfere.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be exclusively locked while this stop job runs.
     *
     * @return an array containing the target server
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { server };
    }


    // ── Naming The Shutdown In The Mission Log ───────────────────────────────────────────────
    // The mission log records: "Reactor shutdown: [station name]."
    // Eclipse's progress dialog shows this so users know what's happening.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable name shown in Eclipse's progress dialog while this job runs.
     *
     * @return a localized "Stop Server [name]" string
     */
    public String getName()
    {
        return NLS.bind( Messages.getString( "StopLdapServerRunnable.StopServer" ), new String[] { server.getName() } ); //$NON-NLS-1$;
    }


    // ── The Reactor Starts Powering Down ────────────────────────────────────────────────────
    // The status board flips to STOPPING, the watchdog starts timing the shutdown,
    // and the gunner team (adapter) executes the actual stop sequence.
    // If the adapter fails, we flip back to STARTED — a failed stop means still running.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the server shutdown sequence in the background:
     * <ol>
     *   <li>Sets server status to STOPPING.</li>
     *   <li>Launches the {@link StopLdapServerWatchDogThread} to detect timeout.</li>
     *   <li>Calls the adapter's {@code stop()} method to terminate the server process.</li>
     * </ol>
     * On failure, reverts to STARTED (the stop failed, so the server is still running)
     * and reports the error via the monitor.
     *
     * @param monitor  the progress monitor for reporting status
     */
    public void run( StudioProgressMonitor monitor )
    {
        // Setting the status on the server to 'stopping'
        server.setStatus( LdapServerStatus.STOPPING );

        // Starting a new watchdog thread
        StopLdapServerWatchDogThread.runNewWatchDogThread( server );

        // Launching the 'stop()' method of the LDAP Server Adapter
        LdapServerAdapterExtension ldapServerAdapterExtension = server.getLdapServerAdapterExtension();
        if ( ldapServerAdapterExtension != null )
        {
            LdapServerAdapter ldapServerAdapter = ldapServerAdapterExtension.getInstance();
            if ( ldapServerAdapter != null )
            {
                try
                {
                    ldapServerAdapter.stop( server, monitor );
                }
                catch ( Exception e )
                {
                    // Setting the server as started
                    server.setStatus( LdapServerStatus.STARTED );

                    // Reporting the error to the monitor
                    monitor.reportError( e );
                }
            }
        }
    }
}
