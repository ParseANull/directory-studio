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

package org.apache.directory.studio.apacheds.configuration.actions;


import org.apache.directory.studio.apacheds.configuration.ApacheDS2ConfigurationPlugin;
import org.apache.directory.studio.apacheds.configuration.editor.ConnectionServerConfigurationInput;
import org.apache.directory.studio.apacheds.configuration.editor.ServerConfigurationEditor;
import org.apache.directory.studio.connection.core.Connection;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: OpenConfigurationAction — THE IMPERIAL ENGINEERING OFFICER OPENS THE READOUT ────
// When an Imperial officer right-clicks a connection in the Connections view and selects
// "Open Configuration", we open the Death Star engineering readout editor for that
// connection's live ApacheDS server.
// This action does exactly that: it takes the selected Connection object and opens the
// ServerConfigurationEditor with a ConnectionServerConfigurationInput backed by that connection.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Object-level action contributed to the Connections view that opens the
 * {@link ServerConfigurationEditor} for the currently selected {@link Connection}.
 * Wraps the connection in a {@link ConnectionServerConfigurationInput} and calls
 * {@link IWorkbenchPage#openEditor} to show the editor in the workbench.
 * Logs errors if the editor cannot be opened.
 * Think of it as the Imperial engineering officer opening the Death Star technical readout.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenConfigurationAction implements IObjectActionDelegate
{
    /** The selected connection */
    private Connection selectedConnection;


    // ── Opening The Configuration Editor For The Selected Connection ──────────────────────────
    // When the officer selects "Open Configuration", we build the editor input from the selected
    // connection and open the ServerConfigurationEditor in the active workbench page.
    // Any PartInitException (can't open the editor) is logged silently.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link ServerConfigurationEditor} for the currently selected connection.
     * Wraps the connection in a {@link ConnectionServerConfigurationInput} and calls
     * {@link IWorkbenchPage#openEditor}.
     * Logs (but does not display) errors if the editor fails to open.
     *
     * @param action  the Eclipse action proxy (unused — we determine enablement ourselves)
     */
    public void run( IAction action )
    {
        if ( selectedConnection != null )
        {
            try
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                try
                {
                    page.openEditor( new ConnectionServerConfigurationInput( selectedConnection ),
                        ServerConfigurationEditor.ID );
                }
                catch ( PartInitException e )
                {
                    ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                        new Status( Status.ERROR, "org.apache.directory.studio.apacheds.configuration", //$NON-NLS-1$
                            e.getMessage() ) );
                }
            }
            catch ( Exception e )
            {
                ApacheDS2ConfigurationPlugin.getDefault().getLog().log(
                    new Status( Status.ERROR, "org.apache.directory.studio.apacheds.configuration", //$NON-NLS-1$
                        e.getMessage() ) );
            }
        }
    }


    // ── Updating The Stored Connection When The Selection Changes ─────────────────────────────
    // As the user clicks around the Connections view, Eclipse fires selection-changed events.
    // We store the connection if exactly one Connection is selected; clear it otherwise.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes.
     * Stores the selected {@link Connection} if exactly one is selected; clears it otherwise.
     *
     * @param action     the Eclipse action proxy
     * @param selection  the current workbench selection
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        StructuredSelection structuredSelection = ( StructuredSelection ) selection;

        if ( ( structuredSelection.size() == 1 ) && ( structuredSelection.getFirstElement() instanceof Connection ) )
        {
            selectedConnection = ( Connection ) structuredSelection.getFirstElement();
        }
        else
        {
            selectedConnection = null;
        }
    }


    // ── Reacting To The Active Part Changing ─────────────────────────────────────────────────
    // IObjectActionDelegate requires setActivePart; we have nothing to do here since we don't
    // need a reference to the part.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the active workbench part changes. Nothing to do here.
     *
     * @param action      the Eclipse action proxy
     * @param targetPart  the newly activated workbench part
     */
    public void setActivePart( IAction action, IWorkbenchPart targetPart )
    {
        // Nothing to do
    }
}
