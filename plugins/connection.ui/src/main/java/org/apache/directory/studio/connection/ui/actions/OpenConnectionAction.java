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

package org.apache.directory.studio.connection.ui.actions;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.jobs.OpenConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: OpenConnectionAction — HAN FIRES UP THE FALCON ─────────────────────────
// When the mission is ready to launch, Han hits the ignition switch.  This action
// fires up the selected LDAP connections: it dispatches an OpenConnectionsRunnable
// that connect()s and bind()s each selected connection on a background thread.
// Only enabled when at least one selected connection is not already open.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Opens (connects and authenticates) the selected LDAP connection(s) in the
 * Connections view.
 *
 * <p>Creates an {@link OpenConnectionsRunnable} for the selected connections and
 * submits it to a {@link StudioConnectionJob}.  The job runs on a background
 * thread with a progress dialog.</p>
 *
 * <p>Only enabled when at least one selected connection is currently closed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenConnectionAction extends StudioAction
{
    // ── RUN — DISPATCH THE OPEN JOB ───────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Opens the selected connections via a background {@link StudioConnectionJob}.
     */
    public void run()
    {
        new StudioConnectionJob( new OpenConnectionsRunnable( getSelectedConnections() ) ).execute();
    }


    // ── GET TEXT — SINGULAR OR PLURAL LABEL ───────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns a singular or plural label depending on how many connections are selected.
     */
    public String getText()
    {
        if ( getSelectedConnections().length > 1 )
        {
            return Messages.getString( "OpenConnectionAction.OpenConnections" );
        }
        else
        {
            return Messages.getString( "OpenConnectionAction.OpenConnection" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }


    // ── GET IMAGE DESCRIPTOR — THE CONNECT ICON ────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns the connect icon.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ConnectionUIPlugin.getDefault().getImageDescriptor( ConnectionUIConstants.IMG_CONNECTION_CONNECT );
    }


    // ── GET COMMAND ID — NO GLOBAL KEY BINDING ────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code null} — no Eclipse command binding.
     */
    public String getCommandId()
    {
        return null;
    }


    // ── IS ENABLED — ONLY IF AT LEAST ONE CLOSED CONNECTION IS SELECTED ───────────
    /**
     * {@inheritDoc}
     * Returns {@code true} if at least one selected connection is currently closed.
     */
    public boolean isEnabled()
    {
        boolean canOpen = false;

        for ( Connection connection : getSelectedConnections() )
        {
            if ( !connection.getConnectionWrapper().isConnected() )
            {
                canOpen = true;
                break;
            }
        }

        return getSelectedConnections().length > 0 && canOpen;
    }
}
