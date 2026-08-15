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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.jobs.CloseConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionJob;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;


// ── CLASS: DeleteAction — HAN DECOMMISSIONS SHIPS AND CLEARS BAYS ─────────────────
// Sometimes a ship is retired from the fleet, or a cargo bay is no longer needed.
// DeleteAction handles both: it decommissions selected connections (closes them
// first, then removes them from the connection manager) and removes selected
// connection folders (recursively, including all nested sub-folders and their
// connections).
// Before doing anything destructive, it presents a confirmation dialog listing
// the names of up to five items; for larger selections it shows a generic count.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Deletes the selected connections and/or connection folders.
 *
 * <p>Before deleting, presents a confirmation dialog.  Connections are closed
 * via a {@link CloseConnectionsRunnable} before being removed from the
 * {@link org.apache.directory.studio.connection.core.ConnectionManager}.
 * Folders are removed recursively (sub-folders and their connections first).</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteAction extends StudioAction
{
    // ── GET TEXT — CONTEXT-SENSITIVE LABEL ────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getText()
    {
        Connection[] connections = getSelectedConnections();
        ConnectionFolder[] connectionFolders = getSelectedConnectionFolders();

        if ( ( connections.length > 0 ) && ( connectionFolders.length == 0 ) )
        {
            if ( connections.length > 1 )
            {
                return Messages.getString( "DeleteAction.DeleteConnections" );
            }
            else
            {
                return Messages.getString( "DeleteAction.DeleteConnection" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else if ( ( connectionFolders.length > 0 ) && ( connections.length == 0 ) )
        {
            if ( connectionFolders.length > 1 )
            {
                return Messages.getString( "DeleteAction.DeleteConnectionFolders" );
            }
            else
            {
                return Messages.getString( "DeleteAction.DeleteConnectionFolder" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else
        {
            return Messages.getString( "DeleteAction.Delete" ); //$NON-NLS-1$
        }
    }


    // ── GET IMAGE DESCRIPTOR — ECLIPSE SHARED DELETE ICON ─────────────────────────
    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_TOOL_DELETE );
    }


    // ── GET COMMAND ID — MAPS TO EDIT > DELETE ────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return IWorkbenchCommandConstants.EDIT_DELETE;
    }


    // ── RUN — CONFIRM, THEN DELETE ────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Builds a confirmation message listing up to five items by name, shows a
     * confirmation dialog, and on OK closes then removes connections and removes folders.
     */
    public void run()
    {
        Connection[] connections = getSelectedConnections();
        ConnectionFolder[] connectionFolders = getSelectedConnectionFolders();

        StringBuilder message = new StringBuilder();

        // ── BUILD CONFIRMATION MESSAGE ─────────────────────────────────────────────
        if ( connections.length > 0 )
        {
            if ( connections.length <= 5 )
            {
                if ( connections.length == 1 )
                {
                    message.append( Messages.getString( "DeleteAction.SureDeleteFollowingConnection" ) ); //$NON-NLS-1$
                }
                else
                {
                    message.append( Messages.getString( "DeleteAction.SureDeleteFollowingConnections" ) ); //$NON-NLS-1$
                }

                for ( Connection connection : connections )
                {
                    message.append( ConnectionCoreConstants.LINE_SEPARATOR );
                    message.append( "  - " ); //$NON-NLS-1$
                    message.append( connection.getName() );
                }
            }
            else
            {
                message.append( Messages.getString( "DeleteAction.SureDeleteSelectedConnections" ) ); //$NON-NLS-1$
            }

            message.append( ConnectionCoreConstants.LINE_SEPARATOR );
            message.append( ConnectionCoreConstants.LINE_SEPARATOR );
        }

        if ( connectionFolders.length > 0 )
        {
            if ( connectionFolders.length <= 5 )
            {
                if ( connectionFolders.length == 1 )
                {
                    message.append( Messages.getString( "DeleteAction.SureDeleteFollowingFolder" ) ); //$NON-NLS-1$
                }
                else
                {
                    message.append( Messages.getString( "DeleteAction.SureDeleteFollowingFolders" ) ); //$NON-NLS-1$
                }

                for ( ConnectionFolder connectionFolder : connectionFolders )
                {
                    message.append( ConnectionCoreConstants.LINE_SEPARATOR );
                    message.append( "  - " ); //$NON-NLS-1$
                    message.append( connectionFolder.getName() );
                }
            }
            else
            {
                message.append( Messages.getString( "DeleteAction.SureDeleteSelectedConnectionFolders" ) ); //$NON-NLS-1$
            }

            message.append( ConnectionCoreConstants.LINE_SEPARATOR );
            message.append( ConnectionCoreConstants.LINE_SEPARATOR );
        }

        // ── CONFIRM AND EXECUTE ────────────────────────────────────────────────────
        if ( ( message.length() == 0 ) || MessageDialog.openConfirm( getShell(), getText(), message.toString() ) )
        {
            List<Connection> connectionsToDelete = getConnectionsToDelete();
            List<ConnectionFolder> connectionsFoldersToDelete = getConnectionsFoldersToDelete();

            if ( !connectionsToDelete.isEmpty() )
            {
                deleteConnections( connectionsToDelete );
            }

            if ( !connectionsFoldersToDelete.isEmpty() )
            {
                deleteConnectionFolders( connectionsFoldersToDelete );
            }
        }
    }


    // ── GET CONNECTIONS FOLDERS TO DELETE — RECURSIVE FOLDER EXPANSION ────────────
    /**
     * Returns all folders to delete, recursively expanding sub-folder IDs.
     *
     * @return  A flat list of all folders to delete (the selection plus all descendants).
     */
    private List<ConnectionFolder> getConnectionsFoldersToDelete()
    {
        List<ConnectionFolder> selectedFolders = new ArrayList<>( Arrays
            .asList( getSelectedConnectionFolders() ) );
        List<ConnectionFolder> foldersToDelete = new ArrayList<>();

        while ( !selectedFolders.isEmpty() )
        {
            ConnectionFolder folder = selectedFolders.get( 0 );

            for ( String subFolderId : folder.getSubFolderIds() )
            {
                ConnectionFolder subFolder = ConnectionCorePlugin.getDefault().getConnectionFolderManager()
                    .getConnectionFolderById( subFolderId );

                if ( subFolder != null )
                {
                    selectedFolders.add( subFolder );
                }
            }

            if ( !foldersToDelete.contains( folder ) )
            {
                foldersToDelete.add( folder );
            }

            selectedFolders.remove( folder );
        }

        return foldersToDelete;
    }


    // ── GET CONNECTIONS TO DELETE — INCLUDE CONNECTIONS INSIDE SELECTED FOLDERS ────
    /**
     * Returns all connections to delete: directly selected ones plus all connections
     * inside selected folders (recursively).
     *
     * @return  A flat list of all connections to delete.
     */
    private List<Connection> getConnectionsToDelete()
    {
        List<ConnectionFolder> selectedFolders = new ArrayList<>( Arrays
            .asList( getSelectedConnectionFolders() ) );
        List<Connection> selectedConnections = new ArrayList<>( Arrays.asList( getSelectedConnections() ) );
        List<Connection> connectionsToDelete = new ArrayList<>( selectedConnections );

        while ( !selectedFolders.isEmpty() )
        {
            ConnectionFolder folder = selectedFolders.get( 0 );

            for ( String subFolderId : folder.getSubFolderIds() )
            {
                ConnectionFolder subFolder = ConnectionCorePlugin.getDefault().getConnectionFolderManager()
                    .getConnectionFolderById( subFolderId );

                if ( subFolder != null )
                {
                    selectedFolders.add( subFolder );
                }
            }

            for ( String connectionId : folder.getConnectionIds() )
            {
                Connection connection = ConnectionCorePlugin.getDefault().getConnectionManager().getConnectionById(
                    connectionId );

                if ( ( connection != null ) && !connectionsToDelete.contains( connection ) )
                {
                    connectionsToDelete.add( connection );
                }
            }

            selectedFolders.remove( folder );
        }

        return connectionsToDelete;
    }


    // ── IS ENABLED — AT LEAST ONE CONNECTION OR FOLDER SELECTED ──────────────────
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled()
    {
        return getSelectedConnections().length + getSelectedConnectionFolders().length > 0;
    }


    // ── DELETE CONNECTIONS — CLOSE FIRST, THEN REMOVE ────────────────────────────
    /**
     * Closes and then removes the given connections from the connection manager.
     *
     * @param connectionsToDelete  The connections to remove.
     */
    private void deleteConnections( List<Connection> connectionsToDelete )
    {
        new StudioConnectionJob( new CloseConnectionsRunnable( connectionsToDelete ) ).execute();

        for ( Connection connection : connectionsToDelete )
        {
            ConnectionCorePlugin.getDefault().getConnectionManager().removeConnection( connection );
        }
    }


    // ── DELETE CONNECTION FOLDERS — REMOVE FROM FOLDER MANAGER ───────────────────
    /**
     * Removes the given connection folders from the connection folder manager.
     *
     * @param connectionsFoldersToDelete  The folders to remove.
     */
    private void deleteConnectionFolders( List<ConnectionFolder> connectionsFoldersToDelete )
    {
        for ( ConnectionFolder connectionFolder : connectionsFoldersToDelete )
        {
            ConnectionCorePlugin.getDefault().getConnectionFolderManager().removeConnectionFolder( connectionFolder );
        }
    }
}
