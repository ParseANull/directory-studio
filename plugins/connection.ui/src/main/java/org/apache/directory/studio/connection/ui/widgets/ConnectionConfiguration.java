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


import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;


// ── CLASS: ConnectionConfiguration — REBEL BASE EQUIPMENT LOCKER ─────────────────
// Before the Battle of Yavin the Rebel quartermaster hands each pilot exactly the
// equipment they need: targeting computer, comm unit, and a copy of the tactical
// display.  He doesn't build everything up-front — he fetches each item on demand
// and caches it so the same pilot always gets the same gear.
// ConnectionConfiguration is that quartermaster for the ConnectionWidget.  It lazily
// creates the content provider, label provider, sorter, and context menu, returning
// the same cached instance on subsequent calls.  Calling dispose() returns all the
// gear to the locker (nulls the references and disposes resources) so the GC can
// clean up.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Lazy factory and lifecycle manager for the {@link ConnectionWidget}'s
 * collaborator objects.
 *
 * <p>Provides on-demand access to:</p>
 * <ul>
 *   <li>{@link ConnectionContentProvider} — tree structure</li>
 *   <li>{@link ConnectionLabelProvider} — text and icons</li>
 *   <li>{@link ConnectionSorter} — folder-before-connection ordering</li>
 *   <li>{@link MenuManager} — the context menu</li>
 * </ul>
 *
 * <p>Each object is created on the first call and cached.  Call {@link #dispose()}
 * when the widget is closed to release all resources.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionConfiguration
{
    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** Guard flag — prevents double-dispose. */
    private boolean disposed = false;

    /** The content provider (lazily created). */
    private ConnectionContentProvider contentProvider;

    /** The label provider (lazily created). */
    private ConnectionLabelProvider labelProvider;

    /** The sorter (lazily created). */
    private ConnectionSorter sorter;

    /** The context menu manager (lazily created). */
    private MenuManager contextMenuManager;


    // ── DISPOSE ───────────────────────────────────────────────────────────────────
    /**
     * Disposes all resources held by this configuration.
     *
     * <p>Safe to call multiple times — subsequent calls are no-ops.  After this
     * returns, all collaborator references are {@code null} and the
     * {@link MenuManager} has been disposed.</p>
     */
    public void dispose()
    {
        if ( !disposed )
        {
            if ( contentProvider != null )
            {
                contentProvider.dispose();
                contentProvider = null;
            }

            if ( labelProvider != null )
            {
                labelProvider.dispose();
                labelProvider = null;
            }

            if ( contextMenuManager != null )
            {
                contextMenuManager.dispose();
                contextMenuManager = null;
            }

            disposed = true;
        }
    }


    // ── GET CONTEXT MENU MANAGER ──────────────────────────────────────────────────
    /**
     * Returns the context menu manager for the tree viewer, creating and
     * registering it lazily on the first call.
     *
     * <p>The {@link MenuManager} is created, its SWT {@link Menu} is attached to
     * the viewer's control, and the result is cached.</p>
     *
     * @param viewer The {@link TreeViewer} that will host the context menu.
     * @return The shared {@link IMenuManager} for the tree.
     */
    public IMenuManager getContextMenuManager( TreeViewer viewer )
    {
        if ( contextMenuManager == null )
        {
            // ── CREATE AND REGISTER THE CONTEXT MENU ──────────────────────────────
            contextMenuManager = new MenuManager();
            Menu menu = contextMenuManager.createContextMenu( viewer.getControl() );
            viewer.getControl().setMenu( menu );
        }

        return contextMenuManager;
    }


    // ── GET CONTENT PROVIDER ──────────────────────────────────────────────────────
    /**
     * Returns the content provider for the tree viewer, creating it lazily on
     * the first call.
     *
     * @param viewer The {@link TreeViewer} that will use this provider (not
     *               stored; present for API symmetry).
     * @return The shared {@link ConnectionContentProvider}.
     */
    public ConnectionContentProvider getContentProvider( TreeViewer viewer )
    {
        if ( contentProvider == null )
        {
            contentProvider = new ConnectionContentProvider();
        }

        return contentProvider;
    }


    // ── GET LABEL PROVIDER ────────────────────────────────────────────────────────
    /**
     * Returns the label provider for the tree viewer, creating it lazily on the
     * first call.
     *
     * @param viewer The {@link TreeViewer} that will use this provider (not
     *               stored; present for API symmetry).
     * @return The shared {@link ConnectionLabelProvider}.
     */
    public ConnectionLabelProvider getLabelProvider( TreeViewer viewer )
    {
        if ( labelProvider == null )
        {
            labelProvider = new ConnectionLabelProvider();
        }

        return labelProvider;
    }


    // ── GET SORTER ────────────────────────────────────────────────────────────────
    /**
     * Returns the sorter for the tree viewer, creating it lazily on the first
     * call.
     *
     * @return The shared {@link ConnectionSorter}.
     */
    public ConnectionSorter getSorter()
    {
        if ( sorter == null )
        {
            sorter = new ConnectionSorter();
        }

        return sorter;
    }
}
