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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;


// ── CLASS: OpenConfigurationLdapServerRunnable — IMPERIAL ENGINEERS UNROLL THE SCHEMATICS ──
// In the Empire's operations room, the chief engineer unrolls the Death Star's technical
// schematics for the assigned station — the blueprint is pulled from the vault and displayed
// on the readout so every dial and switch is visible for editing.
// This runnable delegates to the server's adapter to open its configuration editor,
// running in the background so the UI doesn't freeze during the handoff.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A background {@link Job} runnable that opens the configuration editor for a given LDAP server.
 * It simply delegates to the server adapter's {@code openConfiguration()} method, which knows
 * how to open the right editor (e.g., the ApacheDS configuration multi-page form).
 * Think of it as the engineer who fetches the right schematic set for the right station.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenConfigurationLdapServerRunnable implements StudioRunnableWithProgress
{
    /** The server */
    private LdapServer server;


    // ── The Engineer Selects The Right Blueprint Set ─────────────────────────────────────────
    // The chief engineer receives the station ID — "this is the reactor section" — and sets
    // aside the right schematics roll before heading to the readout.
    // We simply record which server's config we need to open.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new runnable that will open the configuration editor for the given server.
     * The actual editor-opening happens in {@link #run}; this constructor just captures the target.
     *
     * @param server  the server whose configuration editor should be opened
     */
    public OpenConfigurationLdapServerRunnable( LdapServer server )
    {
        super();
        this.server = server;
    }


    // ── Filing The Error Report ──────────────────────────────────────────────────────────────
    // If the schematics can't be opened — file is missing, adapter threw — the engineer files
    // a clear error report: "Unable to open configuration for [station name]."
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized error message to display if the open-configuration operation fails.
     *
     * @return a localized "Unable to open configuration for [serverName]" string
     */
    public String getErrorMessage()
    {
        return NLS
            .bind(
                Messages.getString( "OpenConfigurationLdapServerRunnable.UnableToOpenConfigurationForServer" ), new String[] //$NON-NLS-1$
                    { server.getName() } );
    }


    // ── Marking The Blueprint As In Use ──────────────────────────────────────────────────────
    // While an engineer has the schematics unrolled, no one else can modify the same plans —
    // the lock prevents conflicting edits.
    // We declare the server as locked so concurrent Studio jobs don't interfere.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be exclusively locked while this job runs.
     * Prevents another job from touching the same server simultaneously.
     *
     * @return an array containing the target server
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { server };
    }


    // ── Naming The Mission In The Progress Log ───────────────────────────────────────────────
    // The mission log records the operation by name: "Open Configuration: [station name]."
    // Eclipse shows this in the progress dialog.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable name of this job as shown in Eclipse's progress dialog.
     *
     * @return a localized "Open Configuration for [serverName]" string
     */
    public String getName()
    {
        return NLS
            .bind(
                Messages.getString( "OpenConfigurationLdapServerRunnable.OpenConfigurationForServer" ), new String[] { server.getName() } ); //$NON-NLS-1$
    }


    // ── The Engineer Unrolls The Schematics ─────────────────────────────────────────────────
    // The chief engineer walks to the readout station and unrolls the blueprint — calling
    // through to the adapter that knows the exact format of each server type's config.
    // We delegate entirely to the adapter's openConfiguration() method.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the open-configuration operation by delegating to the server adapter.
     * The adapter (e.g., the ApacheDS adapter) knows how to open the right editor for its
     * specific server type — we just give it the server reference and the progress monitor.
     *
     * @param monitor  the progress monitor for reporting status
     */
    public void run( StudioProgressMonitor monitor )
    {
        try
        {
            // Letting the LDAP Server Adapter open the configuration of the server
            server.getLdapServerAdapterExtension().getInstance().openConfiguration( server, monitor );
        }
        catch ( Exception e )
        {
            // Reporting the error to the monitor
            monitor.reportError( e );
        }
    }
}
