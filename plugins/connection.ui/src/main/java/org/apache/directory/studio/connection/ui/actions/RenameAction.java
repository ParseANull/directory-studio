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
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchCommandConstants;


// ── CLASS: RenameAction — HAN RE-REGISTERS THE FALCON ─────────────────────────────
// When the Falcon needs a new registration name to slip past Imperial checkpoints,
// Han has to re-register it.  RenameAction does the same for connections and
// connection folders: it opens an InputDialog with the current name pre-filled,
// validates that the new name doesn't already exist, and applies the rename on OK.
// Only enabled when exactly one connection OR exactly one folder is selected
// (not both, not multiple).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Renames the selected connection or connection folder.
 *
 * <p>Opens an {@link InputDialog} with the current name pre-filled and an
 * {@link IInputValidator} that rejects names already in use.  On OK, applies the
 * new name to the model object (which fires a connection-updated event via the
 * Connection's or ConnectionFolder's setter).</p>
 *
 * <p>Only enabled when exactly one connection XOR exactly one folder is selected.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameAction extends StudioAction
{
    // ── GET TEXT — CONTEXT-SENSITIVE LABEL ────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getText()
    {
        Connection[] connections = getSelectedConnections();
        ConnectionFolder[] connectionFolders = getSelectedConnectionFolders();

        if ( ( connections.length == 1 ) && ( connectionFolders.length == 0 ) )
        {
            return Messages.getString( "RenameAction.Connection" ); //$NON-NLS-1$
        }
        else if ( ( connectionFolders.length == 1 ) && ( connections.length == 0 ) )
        {
            return Messages.getString( "RenameAction.ConnectionFolder" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "RenameAction.Rename" ); //$NON-NLS-1$
        }
    }


    // ── GET IMAGE DESCRIPTOR — NO ICON ────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code null} — no icon for rename.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── GET COMMAND ID — MAPS TO FILE > RENAME ────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return IWorkbenchCommandConstants.FILE_RENAME;
    }


    // ── RUN — SHOW THE RENAME DIALOG ──────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Delegates to {@link #renameConnection} or {@link #renameConnectionFolder}
     * depending on the selection.
     */
    public void run()
    {
        Connection[] connections = getSelectedConnections();
        ConnectionFolder[] connectionFolders = getSelectedConnectionFolders();

        if ( connections.length == 1 )
        {
            renameConnection( connections[0] );
        }
        else if ( connectionFolders.length == 1 )
        {
            renameConnectionFolder( connectionFolders[0] );
        }
    }


    // ── IS ENABLED — EXACTLY ONE ITEM SELECTED ────────────────────────────────────
    /**
     * {@inheritDoc}
     * Returns {@code true} when exactly one connection OR exactly one folder is selected.
     */
    public boolean isEnabled()
    {
        return getSelectedConnections().length + getSelectedConnectionFolders().length == 1;
    }


    // ── RENAME CONNECTION ─────────────────────────────────────────────────────────
    /**
     * Opens an input dialog to rename the given connection.
     * Validates that the new name is not already taken.
     *
     * @param connection  The connection to rename.
     */
    private void renameConnection( final Connection connection )
    {
        IInputValidator validator = newName ->
        {
            if ( connection.getName().equals( newName ) )
            {
                return null;
            }
            else if ( ConnectionCorePlugin.getDefault().getConnectionManager().getConnectionByName( newName ) == null )
            {
                return null;
            }
            else
            {
                return Messages.getString( "RenameAction.ConnectionAlreadyExists" ); //$NON-NLS-1$
            }
        };

        InputDialog dialog = new InputDialog(
            getShell(),
            Messages.getString( "RenameAction.RenameConnection" ), Messages.getString( "RenameAction.NewNameConnection" ), connection.getName(), //$NON-NLS-1$ //$NON-NLS-2$
            validator );

        dialog.open();
        String newName = dialog.getValue();

        if ( newName != null )
        {
            connection.setName( newName );
        }
    }


    // ── RENAME CONNECTION FOLDER ──────────────────────────────────────────────────
    /**
     * Opens an input dialog to rename the given connection folder.
     * Validates that the new name is not already taken.
     *
     * @param connectionFolder  The folder to rename.
     */
    private void renameConnectionFolder( final ConnectionFolder connectionFolder )
    {
        IInputValidator validator = newName ->
        {
            if ( connectionFolder.getName().equals( newName ) )
            {
                return null;
            }
            else if ( ConnectionCorePlugin.getDefault().getConnectionFolderManager().getConnectionFolderByName(
                newName ) == null )
            {
                return null;
            }
            else
            {
                return Messages.getString( "RenameAction.ConnectionFolderAlreadyExists" ); //$NON-NLS-1$
            }
        };

        InputDialog dialog = new InputDialog(
            getShell(),
            Messages.getString( "RenameAction.RenameConnectionFolder" ), Messages.getString( "RenameAction.NewNameConnectionFolder" ), connectionFolder.getName(), //$NON-NLS-1$ //$NON-NLS-2$
            validator );

        dialog.open();
        String newName = dialog.getValue();

        if ( newName != null )
        {
            connectionFolder.setName( newName );
        }
    }
}
