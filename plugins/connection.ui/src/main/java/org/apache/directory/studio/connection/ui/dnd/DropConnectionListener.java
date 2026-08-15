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

package org.apache.directory.studio.connection.ui.dnd;


import java.util.Set;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionFolderManager;
import org.apache.directory.studio.connection.core.ConnectionManager;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


// ── CLASS: DropConnectionListener — THE GROUND CREW ACCEPTING THE INCOMING SHIPS ──
// After the Rebel ships lift off (DragConnectionListener), someone on the destination
// hangar has to decide whether the incoming ships are allowed to land.  Can't land a
// folder inside one of its own sub-folders — that would create a circular hierarchy.
// Can't use a LINK operation — we don't have symlinks in this model.
// If everything checks out (dragOver), the ground crew execute the actual transfer
// (drop): move the ship/folder out of its old bay, put it in the new one; or copy it
// to the new bay if that's what was requested.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link DropTargetListener} for drag-and-drop operations within the Connections view.
 *
 * <p>The listener validates each drag-over event to reject unsupported operations
 * (LINK, moving a folder into one of its own ancestors) and executes the actual
 * move or copy in {@link #drop}.</p>
 *
 * <p>Move semantics: the dragged item is removed from its current parent and added
 * to the target folder.  Copy semantics: a deep clone is created, registered with
 * the {@link ConnectionManager} or {@link ConnectionFolderManager}, and added to the
 * target folder.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DropConnectionListener implements DropTargetListener
{
    // ── DRAG ENTER ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Rejects the drag immediately if the transfer type is not
     * {@link ConnectionTransfer}.</p>
     */
    public void dragEnter( DropTargetEvent event )
    {
        if ( !ConnectionTransfer.getInstance().isSupportedType( event.currentDataType ) )
        {
            event.detail = DND.DROP_NONE;
        }
    }


    // ── DRAG OPERATION CHANGED ────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Re-runs the full {@link #dragOver} check whenever the user changes the
     * modifier key (e.g., holding Ctrl to switch from MOVE to COPY).</p>
     */
    public void dragOperationChanged( DropTargetEvent event )
    {
        dragOver( event );
    }


    // ── DRAG LEAVE ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Nothing to clean up when the cursor leaves the drop target.</p>
     */
    public void dragLeave( DropTargetEvent event )
    {
        // Nothing to do.
    }


    // ── DRAG OVER ─────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Validates whether the current drag gesture is permitted.  The rules are:</p>
     * <ol>
     *   <li>If the transfer type is not {@link ConnectionTransfer}, reject (DROP_NONE).</li>
     *   <li>If the user is trying to move a folder into one of its own ancestor
     *       folders (which would create a cycle), reject (DROP_NONE).</li>
     *   <li>LINK operations are not supported — reject (DROP_NONE).</li>
     *   <li>If the detail is DROP_NONE coming in (no modifier key), promote to
     *       DROP_DEFAULT so the platform picks the preferred operation.</li>
     * </ol>
     */
    public void dragOver( DropTargetEvent event )
    {
        try
        {
            // ── CHECK FOR FOLDER-INTO-ANCESTOR CYCLE ──────────────────────────────
            // If the target item is a folder, collect all its ancestor folders.
            // If any dragged item is itself in that ancestor set, the move would
            // create a circular folder hierarchy — which we must forbid.
            // ──────────────────────────────────────────────────────────────────────
            boolean isMoveConnectionFolderForbidden = false;

            if ( ( ( event.detail == DND.DROP_MOVE ) || ( event.detail == DND.DROP_NONE ) ) &&
                ( ConnectionTransfer.getInstance().isSupportedType( event.currentDataType ) ) &&
                ( ( event.item != null ) && ( event.item.getData() instanceof ConnectionFolder ) ) )
            {
                ConnectionFolderManager connectionFolderManager = ConnectionCorePlugin.getDefault()
                    .getConnectionFolderManager();
                ConnectionFolder overFolder = ( ConnectionFolder ) event.item.getData();
                Set<ConnectionFolder> allParentFolders = connectionFolderManager
                    .getAllParentFolders( overFolder );

                if ( event.widget instanceof DropTarget )
                {
                    DropTarget dropTarget = ( DropTarget ) event.widget;

                    if ( dropTarget.getControl() instanceof Tree )
                    {
                        Tree tree = ( Tree ) dropTarget.getControl();

                        for ( TreeItem treeItem : tree.getSelection() )
                        {
                            if ( treeItem.getData() instanceof ConnectionFolder )
                            {
                                ConnectionFolder folder = ( ConnectionFolder ) treeItem.getData();

                                if ( allParentFolders.contains( folder ) )
                                {
                                    isMoveConnectionFolderForbidden = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // ── APPLY VALIDATION RESULTS ──────────────────────────────────────────
            if ( !ConnectionTransfer.getInstance().isSupportedType( event.currentDataType ) )
            {
                event.detail = DND.DROP_NONE;
            }
            else if ( isMoveConnectionFolderForbidden )
            {
                event.detail = DND.DROP_NONE;
            }
            else if ( event.detail == DND.DROP_LINK )
            {
                // LINK is not a meaningful operation in our model.
                event.detail = DND.DROP_NONE;
            }
            else if ( event.detail == DND.DROP_NONE )
            {
                // No modifier key pressed — let the platform pick the default action.
                event.detail = DND.DROP_DEFAULT;
            }
        }
        catch ( Exception e )
        {
            event.detail = DND.DROP_NONE;
            e.printStackTrace();
        }
    }


    // ── DROP ACCEPT ───────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Nothing to do — acceptance is already enforced in {@link #dragOver}.</p>
     */
    public void dropAccept( DropTargetEvent event )
    {
        // Nothing to do.
    }


    // ── DROP ──────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Executes the actual move or copy operation:</p>
     * <ul>
     *   <li><b>Connection + MOVE</b>: removes the connection ID from its old parent
     *       folder, adds it to the target folder.</li>
     *   <li><b>Connection + COPY</b>: clones the connection, registers the clone with
     *       {@link ConnectionManager}, adds its ID to the target folder.</li>
     *   <li><b>ConnectionFolder + MOVE</b>: removes the folder ID from its old parent,
     *       adds it to the target folder.</li>
     *   <li><b>ConnectionFolder + COPY</b>: clones the folder, registers the clone
     *       with {@link ConnectionFolderManager}, adds its ID to the target folder.</li>
     * </ul>
     *
     * <p>If {@code event.item} is {@code null} the user dropped onto empty space —
     * we use the root connection folder as the target.</p>
     *
     * <p>Sets {@code event.detail = DND.DROP_NONE} on any exception so the OS does
     * not show a misleading success cursor.</p>
     */
    public void drop( DropTargetEvent event )
    {
        ConnectionManager connectionManager = ConnectionCorePlugin.getDefault().getConnectionManager();
        ConnectionFolderManager connectionFolderManager = ConnectionCorePlugin.getDefault()
            .getConnectionFolderManager();

        try
        {
            if ( ConnectionTransfer.getInstance().isSupportedType( event.currentDataType ) )
            {
                Object[] objects = ( Object[] ) event.data;

                // ── RESOLVE TARGET FOLDER ─────────────────────────────────────────
                // If the cursor landed on empty space we use the root folder.
                // If it landed on a folder that's the target; if on a connection we
                // use that connection's parent folder.
                // ──────────────────────────────────────────────────────────────────
                Object target;

                if ( event.item == null )
                {
                    target = connectionFolderManager.getRootConnectionFolder();
                }
                else
                {
                    target = event.item.getData();
                }

                ConnectionFolder targetFolder = null;

                if ( target instanceof ConnectionFolder )
                {
                    targetFolder = ( ConnectionFolder ) target;
                }
                else if ( target instanceof Connection )
                {
                    Connection connection = ( Connection ) target;
                    targetFolder = connectionFolderManager.getParentConnectionFolder( connection );
                }

                // ── PROCESS EACH DRAGGED OBJECT ───────────────────────────────────
                for ( Object object : objects )
                {
                    if ( object instanceof Connection )
                    {
                        Connection connection = ( Connection ) object;

                        if ( event.detail == DND.DROP_MOVE )
                        {
                            // ── MOVE: relocate the connection to targetFolder ──────
                            ConnectionFolder parentConnectionFolder = connectionFolderManager
                                .getParentConnectionFolder( connection );
                            parentConnectionFolder.removeConnectionId( connection.getId() );
                            targetFolder.addConnectionId( connection.getId() );
                        }
                        else if ( event.detail == DND.DROP_COPY )
                        {
                            // ── COPY: clone + register the connection ─────────────
                            Connection newConnection = ( Connection ) connection.clone();
                            connectionManager.addConnection( newConnection );
                            targetFolder.addConnectionId( newConnection.getId() );
                        }
                    }
                    else if ( object instanceof ConnectionFolder )
                    {
                        ConnectionFolder folder = ( ConnectionFolder ) object;

                        if ( event.detail == DND.DROP_MOVE )
                        {
                            // ── MOVE: relocate the folder to targetFolder ─────────
                            ConnectionFolder parentConnectionFolder = connectionFolderManager
                                .getParentConnectionFolder( folder );
                            parentConnectionFolder.removeSubFolderId( folder.getId() );
                            targetFolder.addSubFolderId( folder.getId() );
                            // TODO: expand target folder
                        }
                        else if ( event.detail == DND.DROP_COPY )
                        {
                            // ── COPY: clone + register the folder ─────────────────
                            ConnectionFolder newFolder = ( ConnectionFolder ) folder.clone();
                            connectionFolderManager.addConnectionFolder( newFolder );
                            targetFolder.addSubFolderId( newFolder.getId() );
                            // TODO: expand target folder
                        }
                    }
                }
            }
            else
            {
                event.detail = DND.DROP_NONE;
            }
        }
        catch ( Exception e )
        {
            event.detail = DND.DROP_NONE;
            e.printStackTrace();
        }
    }
}
