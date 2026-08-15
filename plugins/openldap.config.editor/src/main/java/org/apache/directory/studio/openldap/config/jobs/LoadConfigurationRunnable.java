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

package org.apache.directory.studio.openldap.config.jobs;


// ── CLASS: LoadConfigurationRunnable — The Intelligence Officer Loading Battle Plans
// Before the fleet can act, the fleet's intelligence officer must load the
// battle plans from wherever they are stored: a live LDAP connection, a slapd.d
// directory on disk, or a brand-new empty plan for a freshly stood-up server.
// LoadConfigurationRunnable runs that loading operation on a background Eclipse
// job thread.  When the plans arrive it dispatches them to the editor on the
// SWT UI thread; if loading fails it reports the exception to both the monitor
// and the editor so the user sees a clear error message.
// ─────────────────────────────────────────────────────────────────────────────
import org.apache.directory.studio.common.core.jobs.StudioProgressMonitor;
import org.apache.directory.studio.common.core.jobs.StudioRunnableWithProgress;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;

import org.apache.directory.studio.openldap.config.editor.ConnectionServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.DirectoryServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.NewServerConfigurationInput;
import org.apache.directory.studio.openldap.config.editor.OpenLdapServerConfigurationEditor;
import org.apache.directory.studio.openldap.config.model.OlcGlobal;
import org.apache.directory.studio.openldap.config.model.OpenLdapConfiguration;
import org.apache.directory.studio.openldap.config.model.io.ConfigurationReader;


/**
 * This class implements a {@link Job} that is used to load a server configuration.
 * It runs on a background thread, reads the OpenLDAP configuration from the editor
 * input (a live connection, a slapd.d directory, or a brand-new empty config),
 * and dispatches the result to the editor via asyncExec on the SWT UI thread.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LoadConfigurationRunnable implements StudioRunnableWithProgress
{
    /** The associated editor */
    private OpenLdapServerConfigurationEditor editor;


    // ── Constructor — Brief the Intelligence Officer ───────────────────────────
    // The intelligence officer is briefed with a reference to the editor so it
    // knows where to deliver the configuration when loading completes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of LoadConfigurationRunnable.
     *
     * @param editor the editor
     */
    public LoadConfigurationRunnable( OpenLdapServerConfigurationEditor editor )
    {
        this.editor = editor;
    }


    // ── getErrorMessage — Provide a Human-Readable Failure Message ────────────
    /**
     * {@inheritDoc}
     */
    public String getErrorMessage()
    {
        return "Unable to load the configuration.";
    }


    // ── getLockedObjects — Report No Objects Are Locked ───────────────────────
    /**
     * {@inheritDoc}
     */
    public Object[] getLockedObjects()
    {
        return new Object[0];
    }


    // ── getName — Identify This Job in the Progress UI ────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return "Load Configuration";
    }


    // ── run — Load and Dispatch the Configuration ─────────────────────────────
    // The officer loads the configuration from the editor input.  On success it
    // dispatches the result to the editor's configurationLoaded callback via
    // asyncExec; on failure it reports to the monitor and calls
    // configurationLoadFailed on the UI thread.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public void run( StudioProgressMonitor monitor )
    {
        IEditorInput input = editor.getEditorInput();

        try
        {
            final OpenLdapConfiguration configuration = getConfiguration( input, monitor );

            if ( configuration != null )
            {
                Display.getDefault().asyncExec( () -> editor.configurationLoaded( configuration ) );
            }
        }
        catch ( Exception e )
        {
            // Reporting the error to the monitor
            monitor.reportError( e );

            // Reporting the error to the editor
            final Exception exception = e;

            Display.getDefault().asyncExec( () -> editor.configurationLoadFailed( exception ) );
        }
    }


    // ── getConfiguration — Fetch the Right Config for the Input Type ──────────
    // The officer checks the type of the editor input to decide where to fetch
    // the battle plans from:
    //   - ConnectionServerConfigurationInput → read from a live LDAP connection
    //   - DirectoryServerConfigurationInput → read from a slapd.d directory
    //   - NewServerConfigurationInput → build a minimal blank config
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Gets the configuration from the input. It may come from an existing connection,
     * or from an existing file/directory on the disk, or a brand new configuration
     *
     * @param input the editor input
     * @param monitor the studio progress monitor
     * @return the configuration
     * @throws Exception
     */
    public OpenLdapConfiguration getConfiguration( IEditorInput input, StudioProgressMonitor monitor ) throws Exception
    {
        if ( input instanceof ConnectionServerConfigurationInput )
        {
            // If the input is a ConnectionServerConfigurationInput, then we
            // read the server configuration from the selected connection
            ConfigurationReader.readConfiguration( ( ConnectionServerConfigurationInput ) input );
        }
        else if ( input instanceof DirectoryServerConfigurationInput )
        {
            // If the input is a DirectoryServerConfigurationInput, then we
            // read the server configuration from the selected 'slapd.d' directory.
            return ConfigurationReader.readConfiguration( ( DirectoryServerConfigurationInput ) input );
        }
        else if ( input instanceof NewServerConfigurationInput )
        {
            // If the input is a NewServerConfigurationInput, then we only
            // need to create a server configuration and return.
            // The new configuration will be pretty empty, with just
            // the main container (and the olcGlobal instance
            OpenLdapConfiguration configuration = new OpenLdapConfiguration();
            configuration.setGlobal( new OlcGlobal() );

            return configuration;
        }

        return null;
    }
}
