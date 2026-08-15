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

package org.apache.directory.studio.ldapservers.model;


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;


// ── CLASS: LdapServerAdapter — C-3PO TRANSLATING BETWEEN THE EMPIRE'S PROTOCOL AND OURS ──
// C-3PO is fluent in over six million forms of communication — but the critical skill is
// that he provides a single consistent interface regardless of which species he's talking to.
// You say "start", C-3PO translates, the Jawas understand.
// This interface does the same: no matter what LDAP server type is behind the scenes
// (ApacheDS, OpenLDAP, 389-DS), the generic framework always calls the same five methods —
// add, delete, openConfiguration, start, stop — and the adapter translates.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The plugin contract that every LDAP server type must implement.
 * Third-party plugins (like the ApacheDS adapter) register an implementation of this interface
 * via Eclipse's extension point; the framework then calls these methods without knowing
 * which actual server type is underneath.
 * Think of it as C-3PO's universal protocol: one consistent API regardless of the alien species.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface LdapServerAdapter
{
    // ── C-3PO Greets A New Crew Member ──────────────────────────────────────────────────────
    // When a new alien crewmember joins the Rebellion, C-3PO does the diplomatic onboarding —
    // setting up the communication channel and preparing the working relationship.
    // add() is the hook for when a server is first created: copy default configs, create dirs, etc.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called once when a new server instance is first added to the registry.
     * The adapter should use this to set up any server-specific resources: creating the initial
     * configuration files, setting up the data directory, etc.
     *
     * <p>For example — C-3PO welcomes a new crewmember:</p>
     * <pre>
     *   Rebel base assigns new slot → adapter.add(server, monitor)
     *   Adapter copies default ApacheDS config.ldif, creates /data folder.
     * </pre>
     *
     * @param server   the newly created server instance
     * @param monitor  the progress monitor for reporting sub-tasks
     * @throws Exception  if any setup step fails
     */
    void add( LdapServer server, StudioProgressMonitor monitor ) throws Exception;


    // ── C-3PO Handles The Diplomatic Goodbye ────────────────────────────────────────────────
    // When a crewmember's service ends, C-3PO manages the offboarding: closing out their
    // access, cleaning up their quarters.
    // delete() is the adapter's chance to clean up server-specific resources after deletion.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called after the server has been removed from the registry and its base folder deleted.
     * The adapter may need to do additional cleanup specific to its server type — e.g.,
     * removing Eclipse launch configurations, stopping platform-specific processes.
     *
     * @param server   the server being deleted
     * @param monitor  the progress monitor
     * @throws Exception  if any cleanup step fails
     */
    void delete( LdapServer server, StudioProgressMonitor monitor ) throws Exception;


    // ── C-3PO Opens The Technical Manual For The Ambassador ─────────────────────────────────
    // When the Ambassador needs to review the ship's full technical specifications, C-3PO
    // fetches the right manual — each species has a different format.
    // openConfiguration() opens the server-type-specific editor (e.g., the ApacheDS config form).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user double-clicks a server or invokes "Open Configuration."
     * The adapter should open the appropriate configuration editor for its server type —
     * e.g., the ApacheDS multi-page FormEditor for ApacheDS servers.
     *
     * @param server   the server whose configuration editor should be opened
     * @param monitor  the progress monitor
     * @throws Exception  if the editor cannot be opened
     */
    void openConfiguration( LdapServer server, StudioProgressMonitor monitor ) throws Exception;


    // ── C-3PO Initiates The Startup Handshake ───────────────────────────────────────────────
    // "Greetings — initiating connection protocol for ApacheDS Server v2.x."
    // C-3PO sends the start command in the server's native language and waits for the response.
    // start() launches the actual server process — what that means depends on the server type.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks "Start" for a server.
     * The adapter is responsible for the actual process launch — spawning the JVM, passing
     * the right arguments, setting up classpath, etc.  The framework handles status tracking.
     *
     * @param server   the server to start
     * @param monitor  the progress monitor for reporting launch progress
     * @throws Exception  if the server cannot be started
     */
    void start( LdapServer server, StudioProgressMonitor monitor ) throws Exception;


    // ── C-3PO Sends The Shutdown Protocol ───────────────────────────────────────────────────
    // "Initiating shutdown sequence for ApacheDS Server v2.x — all connections will be closed."
    // C-3PO sends the stop command in the server's native language.
    // stop() terminates the server process — gracefully if possible, forcefully if not.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user clicks "Stop" for a server.
     * The adapter terminates the server process, using whatever mechanism the server type
     * supports (graceful shutdown, kill signal, etc.).
     *
     * @param server   the server to stop
     * @param monitor  the progress monitor
     * @throws Exception  if the server cannot be stopped
     */
    void stop( LdapServer server, StudioProgressMonitor monitor ) throws Exception;


    // ── C-3PO Checks That The Ports Are Clear Before The Handshake ──────────────────────────
    // Before C-3PO opens a comm channel, he verifies it isn't already in use by another party —
    // "I'm afraid port 389 is already occupied, sir."
    // We call this before starting so the user gets a clear warning about port conflicts.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether any of the server's required ports are already in use before starting.
     * Called by {@code StartAction} before scheduling the start job — if ports are in conflict,
     * we show a warning dialog instead of blundering into a failed startup.
     *
     * <p>For example — C-3PO checks the comm channels:</p>
     * <pre>
     *   checkPortsBeforeServerStart(server) → ["LDAP (389)", "LDAPS (636)"]
     *   → two ports already in use — show warning to user.
     *   → returns [] → all clear, proceed with start.
     * </pre>
     *
     * @param server  the server whose ports to check
     * @return an array of error strings (one per conflicted port) in the format "{PROTOCOL} ({PORT})";
     *         empty array means all ports are available
     * @throws Exception  if the port-check itself encounters an error
     */
    String[] checkPortsBeforeServerStart( LdapServer server ) throws Exception;
}
