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


import java.io.File;

import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerAdapter;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;


// ── CLASS: DeleteLdapServerRunnable — THE DEATH STAR FIRES ON ALDERAAN ────────────────────
// Grand Moff Tarkin gives the order: stop the reactor, then obliterate the target — irreversibly
// and completely.  The Death Star doesn't half-delete a planet.
// This runnable does the equivalent: if the server is running, stop it first, then remove it
// from the registry, delete its data directory from disk, and let the adapter do its cleanup.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A background {@link Job} runnable that fully and irreversibly deletes an LDAP server.
 * The sequence is: (1) stop the server if running, (2) unregister it from
 * {@link LdapServersManager}, (3) recursively delete its on-disk data folder,
 * (4) call the adapter's own {@code delete()} hook for any additional cleanup.
 * Think of it as Tarkin's firing order — once triggered, there is no undo.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteLdapServerRunnable implements StudioRunnableWithProgress
{
    /** The server */
    private LdapServer server;


    // ── Tarkin Selects The Target ────────────────────────────────────────────────────────────
    // Grand Moff Tarkin points to Alderaan on the holographic display — the target is locked in.
    // We simply record which server is condemned so every subsequent method knows what to act on.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new delete runnable for the specified server.
     * We just capture the reference here; the actual deletion happens in {@link #run}.
     *
     * <p>For example — Tarkin selects the target:</p>
     * <pre>
     *   Tarkin: "Target: Alderaan. Prepare the superlaser."
     *   new DeleteLdapServerRunnable(server) → target locked in.
     * </pre>
     *
     * @param server  the LDAP server to delete — must not be null
     */
    public DeleteLdapServerRunnable( LdapServer server )
    {
        super();
        this.server = server;
    }


    // ── The Error Message When The Firing Fails ─────────────────────────────────────────────
    // If something goes wrong during deletion, the Death Star's crew needs a precise error report
    // to log — "unable to destroy target: [name]."
    // We return a human-readable error string so Eclipse's job framework can log or display it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the error message to display/log if the delete operation fails.
     * Eclipse's job infrastructure calls this when the job ends with an exception.
     *
     * @return a localized "Unable to delete server [name]" message
     */
    public String getErrorMessage()
    {
        return NLS.bind( Messages.getString( "DeleteLdapServerRunnable.UnableToDeleteServer" ), new String[] //$NON-NLS-1$
            { server.getName() } );
    }


    // ── Locking The Target During The Strike ────────────────────────────────────────────────
    // The Death Star's targeting system locks the beam on Alderaan — nothing else can interfere
    // with the same target while the strike is in progress.
    // We return the server as the locked object so concurrent Studio jobs can't touch it.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the objects that must be exclusively locked while this job runs.
     * Eclipse uses this to prevent two jobs from operating on the same server simultaneously.
     *
     * @return an array containing just the target server
     */
    public Object[] getLockedObjects()
    {
        return new Object[]
            { server };
    }


    // ── Announcing The Operation Name ────────────────────────────────────────────────────────
    // The Death Star's mission log records the operation: "Strike: [target name]."
    // Eclipse displays this in the progress dialog so users see what's happening.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable name for this job, shown in Eclipse's progress UI.
     *
     * @return a localized "Delete Server [name]" string
     */
    public String getName()
    {
        return NLS.bind(
            Messages.getString( "DeleteLdapServerRunnable.DeleteServer" ), new String[] { server.getName() } ); //$NON-NLS-1$
    }


    // ── Tarkin Gives The Order — Execute ────────────────────────────────────────────────────
    // Tarkin nods: the reactor stops, the superlaser fires, Alderaan is gone in seconds.
    // Step by step: stop (if running) → unregister → delete disk files → adapter cleanup.
    // If anything fails mid-way, we restore the server's status so the UI isn't left in limbo.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the full deletion sequence in the background:
     * <ol>
     *   <li>If the server is running, stops it (blocking until done).</li>
     *   <li>Removes the server from {@link LdapServersManager}.</li>
     *   <li>Recursively deletes the server's data directory from disk.</li>
     *   <li>Calls the adapter's own {@code delete()} method for adapter-specific cleanup.</li>
     * </ol>
     * On any failure the server's status is restored to its pre-deletion state and the error
     * is reported to the monitor.
     *
     * @param monitor  the progress monitor for reporting status and sub-task names
     */
    public void run( StudioProgressMonitor monitor )
    {
        // Storing the started status of the server
        boolean serverStarted = server.getStatus() == LdapServerStatus.STARTED;

        try
        {
            // Checking if the server is running
            // If yes, we need to shut it down before removing its data
            if ( serverStarted )
            {
                // Creating, scheduling and waiting on the job to stop the server
                StudioLdapServerJob job = new StudioLdapServerJob( new StopLdapServerRunnable( server ) );
                job.schedule();
                job.join();
            }

            // Removing the server
            LdapServersManager.getDefault().removeServer( server );

            // Deleting the associated directory on disk
            deleteDirectory( LdapServersManager.getServerFolder( server ).toFile() );

            // Letting the LDAP Server Adapter finish the deletion of the server
            LdapServerAdapter ldapServerAdapter = server.getLdapServerAdapterExtension().getInstance();
            if ( ldapServerAdapter != null )
            {
                ldapServerAdapter.delete( server, monitor );
            }
        }
        catch ( InterruptedException e )
        {
            // Nothing to do
        }
        catch ( Exception e )
        {
            if ( serverStarted )
            {
                // Setting the server as started
                server.setStatus( LdapServerStatus.STARTED );
            }
            else
            {
                // Setting the server as stopped
                server.setStatus( LdapServerStatus.STOPPED );
            }

            // Reporting the error to the monitor
            monitor.reportError( e );
        }
    }


    // ── The Superlaser Sweeps Every Last Tile ───────────────────────────────────────────────
    // The Death Star's beam doesn't leave rubble — it vaporizes everything, recursively,
    // down to the last molecule.
    // We recursively delete every file and subdirectory in the server's data folder.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Recursively deletes a directory and all its contents.
     * Called after the server has been removed from the registry so its on-disk data is gone too.
     *
     * <p>For example — the superlaser clears the debris field:</p>
     * <pre>
     *   deleteDirectory(serverFolder/)
     *     → deletes config.xml, data/, data/index/, ...
     *     → then deletes serverFolder/ itself.
     * </pre>
     *
     * @param path  the directory (or file) to delete
     * @return {@code true} if the final {@code path.delete()} succeeded; {@code false} otherwise
     */
    private boolean deleteDirectory( File path )
    {
        if ( path.exists() )
        {
            File[] files = path.listFiles();
            for ( int i = 0; i < files.length; i++ )
            {
                if ( files[i].isDirectory() )
                {
                    deleteDirectory( files[i] );
                }
                else
                {
                    files[i].delete();
                }
            }
        }
        return ( path.delete() );
    }
}
