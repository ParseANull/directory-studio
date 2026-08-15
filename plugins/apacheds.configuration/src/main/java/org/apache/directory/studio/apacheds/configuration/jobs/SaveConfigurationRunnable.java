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

package org.apache.directory.studio.apacheds.configuration.jobs;


import java.io.File;

import org.apache.directory.studio.apacheds.configuration.editor.ConnectionServerConfigurationInput;
import org.apache.directory.studio.apacheds.configuration.editor.NewServerConfigurationInput;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditor;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditorUtils;
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;


// ── CLASS: SaveConfigurationRunnable — IMPERIAL ENGINEER TRANSMITS SCHEMATICS ─
// An Imperial engineer sits down at the Death Star's main terminal with the
// freshly updated reactor schematics in hand.  She checks whether anything
// actually changed since the last save, then transmits the updated data back
// to the right destination: the live Imperial network if we're connected to a
// running server, or the local filing cabinet on disk otherwise.
// This class is that engineer: a background job that writes the in-memory
// Configuration back to a live LDAP connection or to the local config file,
// depending on what the editor's input says.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A background job ({@link StudioRunnableWithProgress}) that saves the current
 * ApacheDS server configuration back to its source.
 * The destination can be a live LDAP connection (where we apply only the diff),
 * a file on disk (where we rewrite the whole LDIF), or — for brand-new configs
 * that have never been saved — a "Save As" dialog.
 * Think of this class as an Imperial engineer transmitting updated schematics
 * back to the Death Star's main computer — she checks what changed, picks the
 * right channel, and sends exactly what needs to go.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SaveConfigurationRunnable implements StudioRunnableWithProgress
{
    /** The associated editor */
    private ServerConfigurationEditor editor;


    // ── Engineer Receives the Transmission Assignment ────────────────────────
    // The station commander hands the engineer her assignment dossier — the
    // editor reference she'll need to read the current configuration state and
    // to mark the editor clean once the transmission completes successfully.
    // We stash the editor here so run() can call getConfigWriter() and setDirty()
    // at the right moments during the save operation.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Wires this runnable to the editor that triggered the save.
     * We hold onto the editor so we can read its configuration, pick up the
     * config writer, and clear the dirty flag once the save succeeds.
     *
     * @param editor  the ServerConfigurationEditor that triggered this save job
     */
    public SaveConfigurationRunnable( ServerConfigurationEditor editor )
    {
        super();
        this.editor = editor;
    }


    // ── Engineer Reports the Transmission Failure ────────────────────────────
    // If the transmission fails mid-send, the engineer radios back a standard
    // error message so the control room knows what went wrong and can log it
    // for the Imperial incident report.
    // This method returns the localised "Unable to save configuration" string
    // that the Studio job framework will display if run() throws an exception.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable error message shown when the save job fails.
     * The message is looked up from the plugin's message bundle so it can be
     * localised for different languages.
     *
     * @return  a short description of the failure, e.g. "Unable to save configuration"
     */
    public String getErrorMessage()
    {
        return Messages.getString( "SaveConfigurationRunnable.UnableToSaveConfiguration" ); //$NON-NLS-1$
    }


    // ── Engineer Needs No Exclusive Access — Works Independently ────────────
    // The engineer can transmit without locking any shared consoles or resources;
    // she works at her own terminal and doesn't block other ongoing operations.
    // Returning an empty array tells the Studio job framework that this runnable
    // holds no exclusive locks and won't deadlock other concurrent jobs.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Reports which objects this job holds exclusive locks on — none, in our case.
     * The Studio job framework uses this list to detect and prevent deadlocks
     * between concurrent jobs.
     *
     * @return  an empty array because we don't lock any shared objects
     */
    public Object[] getLockedObjects()
    {
        return new Object[0];
    }


    // ── Engineer Announces the Transmission Codename ─────────────────────────
    // Every operation on the Death Star gets a codename so the control board can
    // track it.  This one is "Save Configuration" — succinct and unmistakable.
    // getName() supplies the display label the Eclipse Jobs progress dialog shows
    // while this runnable is executing in the background.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name for this job, shown in the Eclipse progress monitor
     * while the configuration is being saved.
     *
     * @return  the localised job name, e.g. "Save Configuration"
     */
    public String getName()
    {
        return Messages.getString( "SaveConfigurationRunnable.SaveConfiguration" ); //$NON-NLS-1$
    }


    // ── Engineer Checks Diff and Transmits to the Right Channel ─────────────
    // The engineer first checks whether the schematics are actually dirty — no
    // point transmitting if nothing changed.  If they are, she starts the progress
    // ticker, figures out the right destination (live connection, local path file,
    // or brand-new "Save As"), and delegates the actual write to the appropriate
    // utility.  Once done she marks the editor clean; on error she forwards the
    // exception to the progress monitor so the UI can show it to the user.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * The main entry point for this background job.
     * We skip the write entirely if the editor isn't dirty.  Otherwise we inspect
     * the input type and dispatch to the right save path: a live LDAP connection,
     * a file path, or a "Save As" dialog for brand-new configurations.
     * The editor's dirty flag is cleared on success and left set on failure.
     *
     * @param monitor  the Studio progress monitor used to track and report progress
     */
    public void run( StudioProgressMonitor monitor )
    {
        try
        {
            if ( editor.isDirty() )
            {
                monitor.beginTask( Messages.getString( "SaveConfigurationRunnable.SavingServerConfiguration" ), //$NON-NLS-1$
                    IProgressMonitor.UNKNOWN );

                IEditorInput input = editor.getEditorInput();
                String inputClassName = input.getClass().getName();
                boolean success = false;

                // If the input is a ConnectionServerConfigurationInput, then we
                // read the server configuration from the selected connection
                if ( input instanceof ConnectionServerConfigurationInput )
                {
                    // Saving the ServerConfiguration to the connection
                    ServerConfigurationEditorUtils.saveConfiguration( ( ConnectionServerConfigurationInput ) input,
                        editor.getConfigWriter(), monitor );
                    success = true;
                }
                else if ( input instanceof IPathEditorInput )
                {
                    // Saving the ServerConfiguration to disk
                    File file = ( ( IPathEditorInput ) input ).getPath().toFile();
                    ServerConfigurationEditorUtils.saveConfiguration( file, editor.getConfigWriter(),
                        editor.getConfiguration() );
                    success = true;
                }
                else if ( inputClassName.equals( "org.eclipse.ui.internal.editors.text.JavaFileEditorInput" ) //$NON-NLS-1$
                    || inputClassName.equals( "org.eclipse.ui.ide.FileStoreEditorInput" ) ) //$NON-NLS-1$
                // The class 'org.eclipse.ui.internal.editors.text.JavaFileEditorInput'
                // is used when opening a file from the menu File > Open... in Eclipse 3.2.x
                // The class 'org.eclipse.ui.ide.FileStoreEditorInput' is used when
                // opening a file from the menu File > Open... in Eclipse 3.3.x
                {
                    // Saving the ServerConfiguration to disk
                    File file = new File( input.getToolTipText() );
                    ServerConfigurationEditorUtils.saveConfiguration( file, editor.getConfigWriter(),
                        editor.getConfiguration() );
                    success = true;
                }
                else if ( input instanceof NewServerConfigurationInput )
                {
                    // The 'ServerConfigurationEditorInput' class is used when a
                    // new Server Configuration File is created.

                    // We are saving this as if it is a "Save as..." action.
                    success = editor.doSaveAs( monitor );
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
