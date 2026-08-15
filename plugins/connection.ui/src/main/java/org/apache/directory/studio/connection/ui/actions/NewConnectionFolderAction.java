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


import org.apache.commons.lang3.StringUtils;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.connection.ui.dialogs.ConnectionFolderDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;


// ── CLASS: NewConnectionFolderAction — HAN OPENS A NEW CARGO BAY ──────────────────
// Sometimes the fleet needs a new cargo bay to organise the ships.  This action
// opens the ConnectionFolderDialog, prompts for a name, creates the new folder in
// the ConnectionFolderManager, and adds it as a sub-folder of whichever folder is
// currently selected (or the root folder if nothing is selected).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Opens the New Connection Folder dialog and creates a new {@link ConnectionFolder}.
 *
 * <p>The new folder is added as a child of the currently selected folder,
 * or as a child of the root folder if no folder is selected.</p>
 *
 * <p>Always enabled — you can always create a new folder.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewConnectionFolderAction extends StudioAction
{
    // ── RUN — OPEN THE FOLDER DIALOG AND CREATE THE FOLDER ────────────────────────
    /**
     * {@inheritDoc}
     * Opens the {@link ConnectionFolderDialog}, creates the folder on OK, and
     * adds it to the selected parent folder (or the root if none is selected).
     */
    public void run()
    {
        ConnectionFolderDialog dialog = new ConnectionFolderDialog(
            PlatformUI.getWorkbench().getDisplay().getActiveShell(),
            Messages.getString( "NewConnectionFolderAction.NewConnectionFolder" ),
            Messages.getString( "NewConnectionFolderAction.NeterNameNewFolder" ), StringUtils.EMPTY, null ); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$

        if ( dialog.open() == ConnectionFolderDialog.OK )
        {
            String name = dialog.getValue();
            ConnectionFolder folder = new ConnectionFolder( name );
            ConnectionCorePlugin.getDefault().getConnectionFolderManager().addConnectionFolder( folder );

            ConnectionFolder[] folders = getSelectedConnectionFolders();

            if ( ( folders != null ) && ( folders.length > 0 ) )
            {
                folders[0].addSubFolderId( folder.getId() );
            }
            else
            {
                ConnectionCorePlugin.getDefault().getConnectionFolderManager().getRootConnectionFolder()
                    .addSubFolderId( folder.getId() );
            }
        }
    }


    // ── GET TEXT ──────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getText()
    {
        return Messages.getString( "NewConnectionFolderAction.NewConnectionFolderDots" ); //$NON-NLS-1$
    }


    // ── GET IMAGE DESCRIPTOR — THE "ADD FOLDER" ICON ──────────────────────────────
    /**
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return ConnectionUIPlugin.getDefault().getImageDescriptor( ConnectionUIConstants.IMG_CONNECTION_FOLDER_ADD );
    }


    // ── GET COMMAND ID — NO GLOBAL KEY BINDING ────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public String getCommandId()
    {
        return null;
    }


    // ── IS ENABLED — ALWAYS ───────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Always returns {@code true}.
     */
    public boolean isEnabled()
    {
        return true;
    }
}
