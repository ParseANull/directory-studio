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
import org.apache.directory.studio.connection.core.jobs.CloseConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: CloseConnectionAction — HAN POWERS DOWN THE FALCON ─────────────────────
// When Han is done with a mission and needs to power down the Falcon's engines,
// he hits the shutdown sequence.  This action does the same for LDAP connections:
// it dispatches a CloseConnectionsRunnable to close all currently selected open
// connections.  Only enabled when at least one selected connection is actually open.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Closes the currently selected LDAP connection(s) in the Connections view.
 *
 * <p>Creates a {@link CloseConnectionsRunnable} for the selected connections and
 * submits it to a {@link StudioConnectionJob}.  The job runs on a background
 * thread with a progress dialog.</p>
 *
 * <p>Only enabled when at least one selected connection is currently open.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CloseConnectionAction extends StudioAction
{

    // ── RUN — DISPATCH THE CLOSE JOB ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Closes the selected connections via a background {@link StudioConnectionJob}.
     */
    public void run()
    {
        new StudioConnectionJob( new CloseConnectionsRunnable( getSelectedConnections() ) ).execute();
    }


    // ── GET TEXT — SINGULAR OR PLURAL LABEL ───────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns a singular or plural action label depending on the number of
     * selected connections.
     */
    public String getText()
    {
        if ( getSelectedConnections().length > 1 )
        {
            return Messages.getString( "CloseConnectionAction.CloseConnections" );
        }
        else
        {
            return Messages.getString( "CloseConnectionAction.CloseConnection" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }


    // ── GET IMAGE DESCRIPTOR — THE DISCONNECT ICON ────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns the disconnect icon.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ConnectionUIPlugin.getDefault().getImageDescriptor( ConnectionUIConstants.IMG_CONNECTION_DISCONNECT );
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


    // ── IS ENABLED — ONLY IF AT LEAST ONE OPEN CONNECTION IS SELECTED ─────────────
    /**
     * {@inheritDoc}
     * Returns {@code true} if at least one selected connection is currently open.
     */
    public boolean isEnabled()
    {
        boolean canClose = false;

        for ( Connection connection : getSelectedConnections() )
        {
            if ( connection.getConnectionWrapper().isConnected() )
            {
                canClose = true;
                break;
            }
        }

        return getSelectedConnections().length > 0 && canClose;
    }
}
