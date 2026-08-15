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


import org.apache.directory.studio.common.ui.widgets.ViewFormWidget;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;


// ── CLASS: ConnectionWidget — REBEL BASE TACTICAL DISPLAY ────────────────────────
// The Rebel base tactical display shows all known ships and sectors in a
// hierarchical tree: folders (sectors) at the top level, individual connections
// (ships) nested inside them.  It can run as a standalone panel (the Connection
// view) or as an embedded widget inside a dialog (SelectConnectionDialog).
// The two modes differ only in where toolbar/menu contributions go: in standalone
// mode they go to the ViewForm header, in embedded mode they go to the host view's
// IActionBars.
// ConnectionWidget handles both modes transparently.  It delegates all provider and
// sorter setup to a ConnectionConfiguration, and routes toolbar/menu queries to
// either the parent ViewFormWidget or the supplied IActionBars depending on which
// was provided at construction time.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Reusable widget that displays the connection tree (folders and connections) in
 * a {@link TreeViewer}.
 *
 * <p>Used by:</p>
 * <ul>
 *   <li>The Connections view (standalone mode — no {@link IActionBars}).</li>
 *   <li>{@code SelectConnectionDialog} and {@code SelectReferralConnectionDialog}
 *       (embedded mode — passes the host view's {@link IActionBars}).</li>
 * </ul>
 *
 * <p>In standalone mode the widget creates its own ViewForm with a toolbar and
 * local menu.  In embedded mode the toolbar/menu/context-menu are delegated to the
 * host view's {@link IActionBars} and to {@link ConnectionConfiguration} respectively.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionWidget extends ViewFormWidget
{

    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** Configuration: lazy factory for content/label providers, sorter, context menu. */
    private ConnectionConfiguration configuration;

    /** The host view's action bars, or {@code null} in standalone mode. */
    private IActionBars actionBars;

    /** The raw SWT tree — needed for dispose(). */
    private Tree tree;

    /** The JFace tree viewer wrapping the SWT tree. */
    private TreeViewer viewer;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionWidget}.
     *
     * @param configuration The {@link ConnectionConfiguration} that supplies the
     *                      content/label provider, sorter, and context menu.
     * @param actionBars    The host view's {@link IActionBars} for embedded mode,
     *                      or {@code null} to use the widget's own ViewForm header.
     */
    public ConnectionWidget( ConnectionConfiguration configuration, IActionBars actionBars )
    {
        super();
        this.configuration = configuration;
        this.actionBars = actionBars;
    }


    // ── CREATE WIDGET ─────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>In embedded mode (actionBars supplied) we skip the ViewForm header and
     * call {@link #createContent(Composite)} directly.  In standalone mode we
     * delegate to {@link ViewFormWidget#createWidget(Composite)} which builds the
     * ViewForm header before calling {@link #createContent(Composite)}.</p>
     *
     * @param parent The parent composite.
     */
    @Override
    public void createWidget( Composite parent )
    {
        if ( actionBars == null )
        {
            super.createWidget( parent );
        }
        else
        {
            createContent( parent );
        }
    }


    // ── GET TOOL BAR MANAGER ──────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>In embedded mode returns the host view's toolbar manager so actions
     * contributed by {@link ConnectionActionGroup} appear in the view toolbar.</p>
     *
     * @return The appropriate {@link IToolBarManager}.
     */
    @Override
    public IToolBarManager getToolBarManager()
    {
        if ( actionBars == null )
        {
            return super.getToolBarManager();
        }
        else
        {
            return actionBars.getToolBarManager();
        }
    }


    // ── GET MENU MANAGER ──────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>In embedded mode returns the host view's local menu manager.</p>
     *
     * @return The appropriate {@link IMenuManager}.
     */
    @Override
    public IMenuManager getMenuManager()
    {
        if ( actionBars == null )
        {
            return super.getMenuManager();
        }
        else
        {
            return actionBars.getMenuManager();
        }
    }


    // ── GET CONTEXT MENU MANAGER ──────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>In embedded mode delegates to the {@link ConnectionConfiguration} so the
     * context menu is registered against the tree control rather than the ViewForm
     * header.</p>
     *
     * @return The appropriate context menu {@link IMenuManager}.
     */
    @Override
    public IMenuManager getContextMenuManager()
    {
        if ( actionBars == null )
        {
            return super.getContextMenuManager();
        }
        else
        {
            return configuration.getContextMenuManager( viewer );
        }
    }


    // ── CREATE CONTENT ────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Creates the SWT {@link Tree} and wraps it in a {@link TreeViewer}.
     * Connects the {@link ConnectionConfiguration}'s sorter, content provider,
     * and label provider to the viewer.</p>
     *
     * @param parent The composite to create the tree inside.
     * @return The tree control.
     */
    protected Control createContent( Composite parent )
    {
        tree = new Tree( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        tree.setData( "org.eclipse.e4.ui.css.CssClassName", "studio-connection-tree" );
        GridData data = new GridData( GridData.FILL_BOTH );
        data.widthHint = 450;
        data.heightHint = 250;
        tree.setLayoutData( data );
        viewer = new TreeViewer( tree );

        // ── WIRE UP CONFIGURATION ─────────────────────────────────────────────────
        // Sort folders before connections, then attach the content and label providers.
        // ──────────────────────────────────────────────────────────────────────────
        configuration.getSorter().connect( viewer );
        viewer.setContentProvider( configuration.getContentProvider( viewer ) );
        viewer.setLabelProvider( configuration.getLabelProvider( viewer ) );

        return tree;
    }


    // ── SET INPUT ─────────────────────────────────────────────────────────────────
    /**
     * Sets the input on the tree viewer.
     *
     * <p>Pass a {@link org.apache.directory.studio.connection.core.ConnectionFolderManager}
     * to display the full connection tree.</p>
     *
     * @param input The input object for the tree viewer.
     */
    public void setInput( Object input )
    {
        viewer.setInput( input );
    }


    // ── SET FOCUS ─────────────────────────────────────────────────────────────────
    /**
     * Gives keyboard focus to the tree control.
     */
    public void setFocus()
    {
        viewer.getTree().setFocus();
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Disposes the {@link ConnectionConfiguration}, the SWT {@link Tree}, and
     * nulls all references.  Safe to call multiple times.</p>
     */
    @Override
    public void dispose()
    {
        if ( viewer != null )
        {
            configuration.dispose();
            configuration = null;

            tree.dispose();
            tree = null;
            viewer = null;
        }
    }


    // ── GET VIEWER ────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying {@link TreeViewer}.
     *
     * <p>Used by {@link ConnectionActionGroup} to wire actions and DnD support
     * against the viewer after the widget is constructed.</p>
     *
     * @return The tree viewer.
     */
    public TreeViewer getViewer()
    {
        return viewer;
    }
}
