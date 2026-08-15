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


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: ConnectionUniversalListener — REBEL BASE HANGAR CONTROL TOWER ─────────
// The control tower at the Rebel base keeps the status board up-to-date whenever a
// ship launches, lands, explodes, or changes callsign.  It also handles the hangar
// bay doors: double-clicking a sector label opens or closes it.
// ConnectionUniversalListener is that control tower for the connection tree.  It
// listens for all ConnectionUpdateListener events and calls viewer.refresh() on
// every one.  It also installs a double-click listener on the tree so that
// double-clicking a ConnectionFolder expands or collapses it.
// On dispose() it de-registers itself from ConnectionEventRegistry so no phantom
// refresh calls happen after the view is closed.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * {@link ConnectionUpdateListener} that keeps the connection tree viewer in sync
 * with the connection registry.
 *
 * <p>On every connection/folder add, remove, open, close, or update event we call
 * {@link TreeViewer#refresh()} to redraw the tree.  We also:</p>
 * <ul>
 *   <li>Select the newly added connection or folder after an add event, so it is
 *       visible in the viewport.</li>
 *   <li>Install a double-click listener that expands or collapses a
 *       {@link ConnectionFolder} when double-clicked.</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionUniversalListener implements ConnectionUpdateListener
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** The tree viewer we keep in sync. */
    protected TreeViewer viewer;

    /**
     * Double-click listener: expands a collapsed folder or collapses an expanded
     * one.  Ignored for plain Connection elements (those are opened by
     * OpenConnectionAction).
     */
    private IDoubleClickListener viewerDoubleClickListener = event ->
    {
        if ( event.getSelection() instanceof IStructuredSelection )
        {
            Object obj = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();

            // ── TOGGLE FOLDER EXPANSION ───────────────────────────────────────────
            // Only act on folders — plain connections have their own double-click
            // action wired up by ConnectionActionGroup.
            // ──────────────────────────────────────────────────────────────────────
            if ( obj instanceof ConnectionFolder )
            {
                if ( viewer.getExpandedState( obj ) )
                {
                    viewer.collapseToLevel( obj, 1 );
                }
                else if ( ( ( ITreeContentProvider ) viewer.getContentProvider() ).hasChildren( obj ) )
                {
                    viewer.expandToLevel( obj, 1 );
                }
            }
        }
    };


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionUniversalListener} and attaches it to the
     * given tree viewer.
     *
     * <p>Registers this listener with the {@link ConnectionEventRegistry} using
     * the plugin's event runner, and installs the folder double-click listener on
     * the viewer.</p>
     *
     * @param viewer The {@link TreeViewer} to keep in sync.
     */
    public ConnectionUniversalListener( TreeViewer viewer )
    {
        this.viewer = viewer;

        this.viewer.addDoubleClickListener( viewerDoubleClickListener );
        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionUIPlugin.getDefault().getEventRunner() );
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────────
    /**
     * Disposes this listener.
     *
     * <p>De-registers from the {@link ConnectionEventRegistry} and nulls the
     * viewer reference so no further refreshes can occur.</p>
     */
    public void dispose()
    {
        if ( viewer != null )
        {
            ConnectionEventRegistry.removeConnectionUpdateListener( this );
            viewer = null;
        }
    }


    // ── CONNECTION UPDATED ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree viewer.  This is the common implementation called by
     * all other event callbacks — any change to any connection causes a full
     * refresh so labels and icons stay current.</p>
     *
     * @param connection The connection that changed (may be {@code null} for
     *                   folder events).
     */
    public void connectionUpdated( Connection connection )
    {
        if ( viewer != null )
        {
            viewer.refresh();
        }
    }


    // ── CONNECTION ADDED ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree and then selects the newly added connection so it is
     * visible in the viewport.</p>
     *
     * @param connection The connection that was added.
     */
    public void connectionAdded( Connection connection )
    {
        connectionUpdated( connection );

        if ( viewer != null )
        {
            viewer.setSelection( new StructuredSelection( connection ), true );
        }
    }


    // ── CONNECTION REMOVED ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree to remove the deleted connection's row.</p>
     *
     * @param connection The connection that was removed.
     */
    public void connectionRemoved( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── CONNECTION OPENED ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree so the icon changes from disconnected to connected.</p>
     *
     * @param connection The connection that was opened.
     */
    public void connectionOpened( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── CONNECTION CLOSED ─────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree so the icon changes back to disconnected.</p>
     *
     * @param connection The connection that was closed.
     */
    public void connectionClosed( Connection connection )
    {
        connectionUpdated( connection );
    }


    // ── CONNECTION FOLDER MODIFIED ────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree after a folder rename or reorder.</p>
     *
     * @param connectionFolder The folder that was modified.
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }


    // ── CONNECTION FOLDER ADDED ───────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree and then selects the new folder so it is visible
     * in the viewport.</p>
     *
     * @param connectionFolder The folder that was added.
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
        if ( viewer != null )
        {
            viewer.setSelection( new StructuredSelection( connectionFolder ), true );
        }
    }


    // ── CONNECTION FOLDER REMOVED ─────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Refreshes the tree to remove the deleted folder's row.</p>
     *
     * @param connectionFolder The folder that was removed.
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
        connectionUpdated( null );
    }
}
