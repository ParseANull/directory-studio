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
package org.apache.directory.studio.openldap.config.model.io;


import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;

import org.apache.directory.studio.openldap.config.editor.ConnectionServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.DirectoryServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.NewServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditorUtils;


// ── CLASS: SaveConfigurationRunnable — R2 Executing the Upload Sequence ───────
// After Han's team has modified the Death Star plans, R2 queues up the upload
// sequence that will push the updated data back into the Imperial network.
// He checks which type of terminal he's plugged into (live server, slapd.d directory,
// or a brand-new config) and chooses the right upload method for each case.
// That's exactly what SaveConfigurationRunnable does: an Eclipse background Job
// that checks the editor's input type and routes the save to the correct path.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Job} that saves the OpenLDAP server configuration back to its source —
 * either a live LDAP server (ConnectionServerConfigurationInput), a slapd.d directory
 * on disk (DirectoryServerConfigurationInput), or triggers a "Save As" flow
 * for a brand-new configuration (NewServerConfigurationInput).
 * Think of this as R2 running the upload sequence — he knows which port to plug
 * into and completes the mission or reports the error.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SaveConfigurationRunnable implements StudioRunnableWithProgress
{
    /** The associated editor */
    private OpenLdapServerConfigurationEditor editor;


    // ── Constructor — R2 Receives His Mission Briefing ────────────────────────────
    // R2 receives a reference to the editor he's serving — that's the mission briefing
    // that tells him whose configuration to save and where.
    // We store the editor so run() can access its input and configuration state.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SaveConfigurationRunnable bound to the given editor.
     * The editor provides both the IEditorInput (which tells us where to save)
     * and the configuration object (which tells us what to save).
     *
     * <p>For example — R2 gets his mission briefing:</p>
     * <pre>
     *   SaveConfigurationRunnable runnable =
     *       new SaveConfigurationRunnable( configEditor );
     * </pre>
     *
     * @param editor  the OpenLdapServerConfigurationEditor whose config we're saving
     */
    public SaveConfigurationRunnable( OpenLdapServerConfigurationEditor editor )
    {
        super();
        this.editor = editor;
    }


    // ── getErrorMessage — R2 Reports What Went Wrong ──────────────────────────────
    // If the upload fails, R2 emits a distress beep that the system translates into
    // a human-readable error message for the UI.
    // Eclipse's job framework displays this string when run() throws an exception.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable error message shown in the Eclipse job error dialog
     * if the save operation fails.
     *
     * <p>For example — R2 emits his error code:</p>
     * <pre>
     *   "Unable to save the configuration."
     * </pre>
     *
     * @return  the error message string displayed to the user on failure
     */
    public String getErrorMessage()
    {
        return "Unable to save the configuration.";
    }


    // ── getLockedObjects — R2 Declares No Exclusive Locks ────────────────────────
    // R2 doesn't need exclusive access to any other droids or terminals to do his job.
    // We return an empty array because the save doesn't require locking other Studio resources.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the set of objects that need to be locked (exclusively acquired) while
     * this job runs. We don't lock anything — the LDAP connection handles concurrency.
     *
     * <p>For example — R2 needs no exclusive terminal access:</p>
     * <pre>
     *   Object[] locks = runnable.getLockedObjects(); // empty array
     * </pre>
     *
     * @return  an empty Object array
     */
    public Object[] getLockedObjects()
    {
        return new Object[0];
    }


    // ── getName — R2 Labels His Mission ───────────────────────────────────────────
    // R2 attaches a label to the task so the progress dialog shows something useful.
    // Eclipse uses this as the Job name in the progress view.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this job, shown in the Eclipse progress view.
     *
     * <p>For example — R2 labels his mission:</p>
     * <pre>
     *   "Save Configuration"
     * </pre>
     *
     * @return  the job name string
     */
    public String getName()
    {
        return "Save Configuration";
    }


    // ── run — R2 Executes the Upload Sequence ─────────────────────────────────────
    // R2 checks which type of terminal he's connected to and runs the appropriate
    // upload sequence: live server upload, slapd.d directory write, or "Save As" dialog.
    // He marks the editor clean when the upload succeeds, or reports the error if not.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the save operation. We check the editor's IEditorInput type and route
     * to the right save path:
     * <ul>
     *   <li>ConnectionServerConfigurationInput — push changes to a live LDAP server</li>
     *   <li>DirectoryServerConfigurationInput — write to a slapd.d directory on disk</li>
     *   <li>NewServerConfigurationInput — trigger the "Save As" wizard flow</li>
     * </ul>
     * On success we mark the editor clean (setDirty(false)); on failure we report
     * the exception to the monitor.
     *
     * <p>For example — R2 runs the upload sequence:</p>
     * <pre>
     *   if ( input instanceof ConnectionServerConfigurationInput ) {
     *       // push to live LDAP server via LDAP modify operations
     *   } else if ( input instanceof DirectoryServerConfigurationInput ) {
     *       // write LDIF files to the slapd.d directory on disk
     *   }
     * </pre>
     *
     * @param monitor  the Studio progress monitor for reporting progress and errors
     */
    public void run( StudioProgressMonitor monitor )
    {
        try
        {
            if ( editor.isDirty() )
            {
                monitor.beginTask( "Saving the server configuration", IProgressMonitor.UNKNOWN );

                IEditorInput input = editor.getEditorInput();
                boolean success = false;

                if ( input instanceof ConnectionServerConfigurationInput )
                {
                    // Saving the ServerConfiguration to the connection
                    OpenLdapServerConfigurationEditorUtils.saveConfiguration( ( ConnectionServerConfigurationInput ) input,
                        editor, monitor );
                    success = true;
                }
                else if ( input instanceof DirectoryServerConfigurationInput )
                {
                    // Saving the ServerConfiguration to the 'slapd.d' directory
                    OpenLdapServerConfigurationEditorUtils.saveConfiguration( editor.getConfiguration(),
                        ( ( DirectoryServerConfigurationInput ) input ).getDirectory() );
                    success = true;
                }
                else if ( input instanceof NewServerConfigurationInput )
                {
                    // The 'ServerConfigurationEditorInput' class is used when a
                    // new Server Configuration File is created.

                    // We are saving this as if it is a "Save as..." action.
                    editor.doSaveAs( monitor );
                }

                editor.setDirty( !success );
            }
        }
        catch ( Exception e )
        {
            // Reporting the error to the monitor
            monitor.reportError( e );
        }
    }
}
