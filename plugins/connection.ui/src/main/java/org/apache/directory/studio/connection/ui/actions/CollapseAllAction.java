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


import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: CollapseAllAction — HAN CLOSES ALL CARGO BAY HATCHES ───────────────────
// When the Falcon is prepping for hyperspace, Han tells Chewie to close all the
// cargo bay hatches so nothing rattles loose.  CollapseAllAction does the same
// for the Connections view tree: it collapses every expanded node in one go.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Toolbar action that collapses all nodes in the Connections view tree.
 *
 * <p>Calls {@link TreeViewer#collapseAll()} on the attached viewer.  Always enabled.
 * The viewer reference is nulled on {@link #dispose()} to prevent memory leaks.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CollapseAllAction extends Action
{
    /** The tree viewer to collapse. */
    protected TreeViewer viewer;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link CollapseAllAction} bound to the given tree viewer.
     *
     * @param viewer  The tree viewer whose nodes should be collapsed.
     */
    public CollapseAllAction( TreeViewer viewer )
    {
        super(
            Messages.getString( "CollapseAllAction.CollapseAll" ),
            ConnectionUIPlugin.getDefault().getImageDescriptor( ConnectionUIConstants.IMG_COLLAPSEALL ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setEnabled( true );

        this.viewer = viewer;
    }


    // ── RUN — COLLAPSE EVERYTHING ─────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     * Collapses all nodes in the attached tree viewer.
     */
    @Override
    public void run()
    {
        this.viewer.collapseAll();
    }


    // ── DISPOSE — RELEASE THE VIEWER REFERENCE ────────────────────────────────────
    /**
     * Releases the viewer reference to allow garbage collection.
     */
    public void dispose()
    {
        this.viewer = null;
    }
}
