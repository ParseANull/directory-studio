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


import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;


// ── CLASS: StartLdapServerWatchDogThread — THE REBEL SENSOR OPERATOR WATCHES THE CHARGE ──
// When the Death Star's superlaser starts charging, a sensor operator watches the power
// readout — if the charge completes, great.  If it stalls for too long without reaching
// full power, the operator cuts the reactor and marks the weapon as non-operational.
// This thread monitors a server that is in STARTING state: it waits up to one minute
// and declares the server STOPPED if it never transitions to STARTED.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A daemon {@link Thread} that watches an LDAP server's startup and forces it to STOPPED
 * if it stays in STARTING state longer than the watchdog timeout (currently 1 minute).
 * Without this, a server that hangs mid-startup would stay in STARTING state forever,
 * blocking the UI from letting the user try again.
 * Think of it as the sensor operator who cuts the reactor if the charge never completes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StartLdapServerWatchDogThread extends Thread
{
    /** The server */
    private LdapServer server;


    // ── The Sensor Operator Takes Their Station ──────────────────────────────────────────────
    // The sensor operator is assigned to monitor this specific weapons array — they sit down,
    // note which station they are watching, and start the clock.
    // We capture the server reference; the monitoring loop runs in {@link #run()}.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new watchdog thread for the given server.
     * Private — use {@link #runNewWatchDogThread(LdapServer)} to create and start in one step.
     *
     * @param server  the server whose startup state we will monitor
     */
    private StartLdapServerWatchDogThread( LdapServer server )
    {
        super();
        this.server = server;
    }


    // ── Watching The Power Readout Until Timeout Or Success ──────────────────────────────────
    // The sensor operator stares at the power readout, checking every second:
    // "Still charging... still charging... timeout! Cut the reactor — mark it non-operational."
    // We poll every second; if the server leaves STARTING (either STARTED or stopped externally)
    // we exit cleanly; if the watchdog timer expires we force STOPPED.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * The watchdog loop — runs until the server either leaves STARTING state on its own
     * or until the one-minute timeout expires.
     * If the server is still STARTING at timeout, we force it to STOPPED so the UI can
     * offer the user a chance to retry.
     *
     * <p>For example — the sensor operator's vigil:</p>
     * <pre>
     *   t=0s  server.getStatus() == STARTING → wait 1 second.
     *   t=30s server.getStatus() == STARTED  → exit loop, all good.
     *   OR
     *   t=60s server.getStatus() == STARTING still → force STOPPED.
     * </pre>
     */
    public void run()
    {
        // Getting the current time
        long startTime = System.currentTimeMillis();

        // Calculating the watchdog time
        final long watchDog = startTime + ( 1000 * 60 * 1 ); // 3 minutes

        // Looping until the end of the watchdog time or when the server status is no longer 'starting'
        while ( ( System.currentTimeMillis() < watchDog ) && ( LdapServerStatus.STARTING == server.getStatus() ) )
        {
            // We just wait one second before starting the test once
            // again
            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e1 )
            {
                // Nothing to do...
            }
        }

        // We exited from the waiting loop

        // Checking if the watchdog time is expired
        if ( ( System.currentTimeMillis() >= watchDog ) && ( LdapServerStatus.STARTING == server.getStatus() ) )
        {
            // TODO Display an error message...

            // Setting the status of the server to 'Stopped'
            server.setStatus( LdapServerStatus.STOPPED );
        }
    }


    // ── Deploying A Sensor Operator To A Station ─────────────────────────────────────────────
    // Command central dispatches a fresh sensor operator to watch the newly charging weapon —
    // one call creates and starts the watcher in a single smooth motion.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Factory method that creates a new {@link StartLdapServerWatchDogThread} for the given server
     * and immediately starts it.
     * Callers (like {@link StartLdapServerRunnable}) use this rather than constructing and starting
     * separately, because the watchdog must begin watching the instant the adapter's {@code start()}
     * call is invoked.
     *
     * @param server  the server whose STARTING state this watchdog will monitor
     */
    public static void runNewWatchDogThread( LdapServer server )
    {
        new StartLdapServerWatchDogThread( server ).start();
    }
}
