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

package org.apache.directory.studio.connection.ui.widgets;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.ConnectionFolderManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: ConnectionContentProvider — REBEL BASE HANGAR MANIFEST ─────────────────
// When you walk into the Rebel base hangar and look at the boards, you see a
// hierarchy: sectors (folders) containing individual ships (connections).  The
// board doesn't store the ships itself — it just knows the IDs and calls the
// logistics officer (ConnectionManager / ConnectionFolderManager) to resolve them
// into real objects.
// ConnectionContentProvider is that logistics board for the JFace TreeViewer.  The
// viewer asks us "what are the top-level items?" and "what are the children of this
// folder?" and we answer by resolving IDs through the plugin registries.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link ITreeContentProvider} for the connection tree widget.
 *
 * <p>Accepts a {@link ConnectionFolderManager} as its input and builds a two-level
 * tree:</p>
 * <ul>
 *   <li>Top level: the children of the root {@link ConnectionFolder} — a mix of
 *       sub-folders and {@link Connection} objects.</li>
 *   <li>Nested level: the children of any sub-folder — more sub-folders and/or
 *       connections.</li>
 * </ul>
 *
 * <p>IDs stored in the folder objects are resolved to live objects via
 * {@link ConnectionCorePlugin#getConnectionFolderManager()} and
 * {@link ConnectionCorePlugin#getConnectionManager()} on every call, so the tree
 * always reflects the current state of the registries.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionContentProvider implements ITreeContentProvider
{
    // ── INPUT CHANGED ─────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>We keep no cached state, so there is nothing to update when the viewer's
     * input changes.</p>
     */
    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Nothing to release — no listeners or caches held.</p>
     */
    @Override
    public void dispose()
    {
    }


    // ── GET ELEMENTS ─────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>When the input is a {@link ConnectionFolderManager} we return the children
     * of its root folder.  Otherwise we fall through to {@link #getChildren(Object)}
     * which handles {@link ConnectionFolder} inputs directly.</p>
     *
     * @param inputElement The input set on the viewer — normally a
     *                     {@link ConnectionFolderManager}.
     * @return The top-level elements to display in the tree.
     */
    public Object[] getElements( Object inputElement )
    {
        if ( inputElement instanceof ConnectionFolderManager )
        {
            ConnectionFolderManager cfm = ( ConnectionFolderManager ) inputElement;
            ConnectionFolder rootConnectionFolder = cfm.getRootConnectionFolder();

            return getChildren( rootConnectionFolder );
        }
        else
        {
            return getChildren( inputElement );
        }
    }


    // ── GET CHILDREN ─────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>For a {@link ConnectionFolder}, resolves its sub-folder IDs and
     * connection IDs into live objects via the plugin registries.  Any ID that
     * no longer maps to a live object is silently skipped (the item was deleted
     * while the tree was open).</p>
     *
     * @param parentElement The folder whose children we want.
     * @return An array of {@link ConnectionFolder} and/or
     *         {@link Connection} children, or {@code null} if the element is
     *         not a folder.
     */
    public Object[] getChildren( Object parentElement )
    {
        if ( parentElement instanceof ConnectionFolder )
        {
            List<Object> children = new ArrayList<>();

            ConnectionFolder folder = ( ConnectionFolder ) parentElement;
            List<String> subFolderIds = folder.getSubFolderIds();
            List<String> connectionIds = folder.getConnectionIds();

            // ── RESOLVE SUB-FOLDERS ───────────────────────────────────────────────
            for ( String subFolderId : subFolderIds )
            {
                ConnectionFolder subFolder = ConnectionCorePlugin.getDefault().getConnectionFolderManager()
                    .getConnectionFolderById( subFolderId );

                if ( subFolder != null )
                {
                    children.add( subFolder );
                }
            }

            // ── RESOLVE CONNECTIONS ───────────────────────────────────────────────
            for ( String connectionId : connectionIds )
            {
                Connection conn = ConnectionCorePlugin.getDefault().getConnectionManager().getConnectionById(
                    connectionId );

                if ( conn != null )
                {
                    children.add( conn );
                }
            }

            return children.toArray();
        }

        return null;
    }


    // ── GET PARENT ────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Resolves the parent of a folder or connection via the
     * {@link ConnectionFolderManager}.  Returns {@code null} for unknown element
     * types.</p>
     *
     * @param element The element whose parent we need.
     * @return The parent {@link ConnectionFolder}, or {@code null}.
     */
    public Object getParent( Object element )
    {
        if ( element instanceof ConnectionFolder )
        {
            return ConnectionCorePlugin.getDefault().getConnectionFolderManager().getParentConnectionFolder(
                ( ConnectionFolder ) element );
        }
        else if ( element instanceof Connection )
        {
            return ConnectionCorePlugin.getDefault().getConnectionFolderManager().getParentConnectionFolder(
                ( Connection ) element );
        }
        else
        {
            return null;
        }
    }


    // ── HAS CHILDREN ─────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #getChildren(Object)} and checks whether the result
     * is non-empty.  JFace calls this to decide whether to draw the expand
     * triangle on a tree node.</p>
     *
     * @param element The element to test.
     * @return {@code true} if the element has at least one child.
     */
    public boolean hasChildren( Object element )
    {
        Object[] children = getChildren( element );

        return children != null && children.length > 0;
    }

}
