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
import java.util.List;

import org.apache.directory.studio.common.ui.ClipboardUtils;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionFolderManager;
import org.apache.directory.studio.connection.core.ConnectionManager;
import org.apache.directory.studio.connection.ui.dnd.ConnectionTransfer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;


// ── CLASS: PasteAction — HAN UNLOADS CARGO FROM THE TRANSMISSION ──────────────────
// When the Rebel Alliance receives a transmitted ship manifest (from CopyAction),
// someone has to unload the cargo and add each ship to the local fleet.
// PasteAction does that: it reads Connection and ConnectionFolder objects from the
// clipboard's ConnectionTransfer, clones each one (so the copy is independent of
// the original), and registers the clones with the connection manager and the
// target folder.
// The target folder is determined by the current selection: the first selected
// folder wins; if no folder is selected, the parent of the first selected
// connection; if nothing is selected, the root folder.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Pastes {@link Connection} and {@link ConnectionFolder} objects from the clipboard
 * into the Connections view.
 *
 * <p>Reads the clipboard via {@link ConnectionTransfer}, clones each object to make
 * the pasted copy independent, registers it with the appropriate manager, and
 * inserts it into the target folder (determined by the current selection).</p>
 *
 * <p>Only enabled when the clipboard contains data in {@link ConnectionTransfer} format.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasteAction extends StudioAction
{
    // ── GET TEXT — CONTEXT-SENSITIVE LABEL ────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns a singular/plural label based on the type and count of clipboard content.
     */
    public String getText()
    {
        List<Connection> connections = getConnectionsToPaste();
        List<ConnectionFolder> connectionFolders = getConnectionFoldersToPaste();

        if ( !connections.isEmpty() && connectionFolders.isEmpty() )
        {
            if ( connections.size() > 1 )
            {
                return Messages.getString( "PasteAction.PasteConnections" );
            }
            else
            {
                return Messages.getString( "PasteAction.PasteConnection" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else if ( !connectionFolders.isEmpty() && connections.isEmpty() )
        {
            if ( connectionFolders.size() > 1 )
            {
                return Messages.getString( "PasteAction.PasteConnectionFolders" );
            }
            else
            {
                return Messages.getString( "PasteAction.PasteConnectionFolder" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else
        {
            return Messages.getString( "PasteAction.Paste" ); //$NON-NLS-1$
        }
    }


    // ── GET IMAGE DESCRIPTOR — ECLIPSE SHARED PASTE ICON ──────────────────────────
    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_TOOL_PASTE );
    }


    // ── GET COMMAND ID — MAPS TO EDIT > PASTE ─────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return IWorkbenchCommandConstants.EDIT_PASTE;
    }


    // ── IS ENABLED — CLIPBOARD HAS CONNECTION TRANSFER DATA ───────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code true} if the clipboard contains data in {@link ConnectionTransfer} format.
     */
    public boolean isEnabled()
    {
        return ClipboardUtils.isAvailable( ConnectionTransfer.getInstance() );
    }


    // ── RUN — CLONE AND REGISTER CLIPBOARD CONTENT ────────────────────────────────
    /**
     * {@inheritDoc}
     * Clones each clipboard connection and folder, adds them to the appropriate
     * manager, and inserts them into the target folder.
     */
    public void run()
    {
        ConnectionFolderManager connectionFolderManager = ConnectionCorePlugin.getDefault()
            .getConnectionFolderManager();
        ConnectionManager connectionManager = ConnectionCorePlugin.getDefault().getConnectionManager();

        // ── DETERMINE THE TARGET FOLDER ────────────────────────────────────────────
        // First selected folder wins; then parent of first selected connection;
        // then fall back to the root folder.
        // ──────────────────────────────────────────────────────────────────────────
        ConnectionFolder[] selectedFolders = getSelectedConnectionFolders();
        Connection[] selectedConnections = getSelectedConnections();
        ConnectionFolder targetFolder = null;

        if ( selectedFolders.length > 0 )
        {
            targetFolder = selectedFolders[0];
        }
        else if ( selectedConnections.length > 0 )
        {
            targetFolder = connectionFolderManager.getParentConnectionFolder( selectedConnections[0] );
        }

        if ( targetFolder == null )
        {
            targetFolder = connectionFolderManager.getRootConnectionFolder();
        }

        // ── PASTE CONNECTIONS ──────────────────────────────────────────────────────
        List<Connection> connections = getConnectionsToPaste();

        for ( Connection connection : connections )
        {
            Connection newConnection = ( Connection ) connection.clone();
            connectionManager.addConnection( newConnection );
            targetFolder.addConnectionId( newConnection.getId() );
        }

        // ── PASTE CONNECTION FOLDERS ───────────────────────────────────────────────
        List<ConnectionFolder> connectionFolders = getConnectionFoldersToPaste();

        for ( ConnectionFolder connectionFolder : connectionFolders )
        {
            ConnectionFolder newConnectionFolder = ( ConnectionFolder ) connectionFolder.clone();
            connectionFolderManager.addConnectionFolder( newConnectionFolder );
            targetFolder.addSubFolderId( newConnectionFolder.getId() );
        }
    }


    // ── GET CONNECTIONS TO PASTE — READ CONNECTION OBJECTS FROM CLIPBOARD ──────────
    /**
     * Returns the {@link Connection} objects currently in the clipboard.
     *
     * @return  A list of connections from the clipboard; possibly empty.
     */
    private List<Connection> getConnectionsToPaste()
    {
        List<Connection> connections = new ArrayList<>();

        Object content = ClipboardUtils.getFromClipboard( ConnectionTransfer.getInstance() );

        if ( content instanceof Object[] )
        {
            for ( Object object : ( Object[] ) content )
            {
                if ( object instanceof Connection )
                {
                    connections.add( ( Connection ) object );
                }
            }
        }

        return connections;
    }


    // ── GET CONNECTION FOLDERS TO PASTE — READ FOLDER OBJECTS FROM CLIPBOARD ───────
    /**
     * Returns the {@link ConnectionFolder} objects currently in the clipboard.
     *
     * @return  A list of folders from the clipboard; possibly empty.
     */
    private List<ConnectionFolder> getConnectionFoldersToPaste()
    {
        List<ConnectionFolder> folders = new ArrayList<>();

        Object content = ClipboardUtils.getFromClipboard( ConnectionTransfer.getInstance() );

        if ( content instanceof Object[] )
        {
            for ( Object object : ( Object[] ) content )
            {
                if ( object instanceof ConnectionFolder )
                {
                    folders.add( ( ConnectionFolder ) object );
                }
            }
        }

        return folders;
    }

}
