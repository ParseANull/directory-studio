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


// ── CLASS: StartLdapServerRunnable — THE DEATH STAR POWERS UP ITS SUPERLASER ─────────────
// Grand Moff Tarkin gives the order — reactors to full power, superlaser charging.
// The crew marks the weapon status as "CHARGING" (STARTING), then hands off to the
// gunner team (the adapter) to actually fire up the system.  A watchdog monitors the
// charge so if it stalls, the status rolls back to STOPPED.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A background {@link Job} runnable that starts a given LDAP server.
 * The sequence is: (1) mark the server as STARTING, (2) spin up the watchdog thread that
 * monitors whether startup completes in time, (3) call the adapter's {@code start()} method.
 * If the adapter throws, we mark the server STOPPED and report the error.
 * Think of it as Tarkin's superlaser charging sequence — ordered, watched, and rolled back
 * if it stalls.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StartLdapServerRunnable implements StudioRunnableWithProgress
{
    /** The server */
    private LdapServer server;


    // ── Tarkin Selects The Target Station ───────────────────────────────────────────────────
    // Grand Moff Tarkin points to the station: "Power up THIS weapons array."
    // We record which server needs to be started so the run() method knows what to act on.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new start runnable for the given server.
     * The actual startup logic runs in {@link #run}; this just captures the target.
     *
     * @param server  the LDAP server to start
     */
    public StartLdapServerRunnable( LdapServer server )
    {
        super();
        this.server = server;
    }


    // ── The Status Board Error Message ──────────────────────────────────────────────────────
    // If the superlaser fails to charge, the status board shows: "Unable to start [weapon name]."
    // We return this error string so Eclipse can display it to the user.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message to display or log if the start operation fails.
     *
     * @return a localized "Unable to start server [name]" string
     */
    public String getErrorMessage()
    {
        return NLS.bind( Messages.getString( "StartLdapServerRunnable.UnableToStartServer" ), new String[] //$NON-NLS-1$
            { server.getName() } );
    }


    // ── Locking The Firing Controls ─────────────────────────────────────────────────────────
    // While the superlaser is charging, the firing controls are locked — no other operation
    // can interfere with the same weapons array at the same time.
    // We declare the server as the lock target so concurrent Studio jobs stay clear.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be exclusively locked while this start job runs.
     *
     * @return an array containing just the target server
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { server };
    }


    // ── Naming The Operation In The Mission Log ──────────────────────────────────────────────
    // The mission log entry: "Power-up sequence: [station name]."
    // Eclipse's progress dialog shows this so users know what's happening in the background.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable name shown in Eclipse's progress dialog while this job runs.
     *
     * @return a localized "Start Server [name]" string
     */
    public String getName()
    {
        return NLS
            .bind( Messages.getString( "StartLdapServerRunnable.StartServer" ), new String[] { server.getName() } ); //$NON-NLS-1$
    }


    // ── Tarkin Initiates The Charging Sequence ───────────────────────────────────────────────
    // "All power to the superlaser — begin charging!" The status board flips to CHARGING
    // (STARTING), the watchdog timer starts counting, and the gunner team (adapter) takes over.
    // If anything explodes mid-sequence, the board resets to STOPPED.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the server startup sequence in the background:
     * <ol>
     *   <li>Sets server status to STARTING.</li>
     *   <li>Launches the {@link StartLdapServerWatchDogThread} to detect timeout.</li>
     *   <li>Calls the adapter's {@code start()} method to do the actual process launch.</li>
     * </ol>
     * On failure, reverts to STOPPED and reports the error via the monitor.
     *
     * @param monitor  the progress monitor for reporting status
     */
    public void run( StudioProgressMonitor monitor )
    {
        // Setting the status on the server to 'starting'
        server.setStatus( LdapServerStatus.STARTING );

        // Starting a new watchdog thread
        StartLdapServerWatchDogThread.runNewWatchDogThread( server );

        // Launching the 'start()' method of the LDAP Server Adapter
        LdapServerAdapterExtension ldapServerAdapterExtension = server.getLdapServerAdapterExtension();
        if ( ldapServerAdapterExtension != null )
        {
            LdapServerAdapter ldapServerAdapter = ldapServerAdapterExtension.getInstance();
            if ( ldapServerAdapter != null )
            {
                try
                {
                    ldapServerAdapter.start( server, monitor );
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
    }
}
