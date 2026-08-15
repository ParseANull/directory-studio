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


// ── CLASS: StopLdapServerWatchDogThread — REBEL SENSORS CONFIRM THE DEATH STAR WENT DARK ──
// After Luke's torpedoes hit, Rebel sensors on Yavin watch the Death Star's reactor signature
// drop — if it doesn't fade within the expected window, something went wrong and the station
// may still be operational (threatening another planet).
// This thread monitors a server in STOPPING state: if it never reaches STOPPED within one
// minute, we assume the shutdown hung and force the status back to STARTED.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A daemon {@link Thread} that watches an LDAP server's shutdown and restores it to STARTED
 * if it remains stuck in STOPPING longer than the watchdog timeout (currently 1 minute).
 * Without this guard, a server that refuses to stop would stay in STOPPING forever, leaving
 * the UI unable to retry or report the issue.
 * Think of it as the Rebel sensor operator checking whether the Death Star really went dark.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StopLdapServerWatchDogThread extends Thread
{
    /** The server */
    private LdapServer server;


    // ── The Sensor Operator Takes Their Watch Station ────────────────────────────────────────
    // A Rebel sensor operator is assigned to monitor the Death Star's reactor signature — they
    // sit down, note which station they are watching, and start tracking the energy readings.
    // We capture the server reference; the monitoring loop runs in {@link #run()}.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new stop-watchdog thread for the given server.
     * Private — use {@link #runNewWatchDogThread(LdapServer)} to create and start together.
     *
     * @param server  the server whose STOPPING state we will monitor
     */
    private StopLdapServerWatchDogThread( LdapServer server )
    {
        super();
        this.server = server;
    }


    // ── Watching The Reactor Signature Until It Fades Or Times Out ───────────────────────────
    // The sensor operator checks every second: "Reactor still active... still active... timeout!
    // It didn't shut down — mark it as still operational."
    // We poll every second; if the server leaves STOPPING naturally we exit clean.
    // If the watchdog timer expires while still STOPPING, we force STARTED (shutdown failed).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * The watchdog loop — runs until the server leaves STOPPING state on its own or until the
     * one-minute timeout expires.
     * On timeout we force the server back to STARTED, because a server that didn't stop is
     * still running and should be represented as such in the UI.
     *
     * <p>For example — the sensor operator's vigil:</p>
     * <pre>
     *   t=0s  server.getStatus() == STOPPING → wait 1 second.
     *   t=15s server.getStatus() == STOPPED  → exit loop, shutdown confirmed.
     *   OR
     *   t=60s server.getStatus() == STOPPING still → force STARTED (shutdown timed out).
     * </pre>
     */
    public void run()
    {
        // Getting the current time
        long startTime = System.currentTimeMillis();

        // Calculating the watchdog time
        final long watchDog = startTime + ( 1000 * 60 * 1 ); // 3 minutes

        // Looping until the end of the watchdog time or when the server status is no longer 'starting'
        while ( ( System.currentTimeMillis() < watchDog ) && ( LdapServerStatus.STOPPING == server.getStatus() ) )
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
        if ( ( System.currentTimeMillis() >= watchDog ) && ( LdapServerStatus.STOPPING == server.getStatus() ) )
        {
            // TODO Display an error message...

            // Setting the status of the server to 'Started'
            server.setStatus( LdapServerStatus.STARTED );
        }
    }


    // ── Deploying A Sensor Operator To Watch The Shutdown ───────────────────────────────────
    // Command dispatches a fresh sensor operator the moment a shutdown sequence begins —
    // create and start in one call so there is no gap in coverage.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Factory method that creates a new {@link StopLdapServerWatchDogThread} for the given server
     * and immediately starts it.
     * Called by {@link StopLdapServerRunnable} right before invoking the adapter's {@code stop()}.
     *
     * @param server  the server whose STOPPING state this watchdog will monitor
     */
    public static void runNewWatchDogThread( LdapServer server )
    {
        new StopLdapServerWatchDogThread( server ).start();
    }
}
