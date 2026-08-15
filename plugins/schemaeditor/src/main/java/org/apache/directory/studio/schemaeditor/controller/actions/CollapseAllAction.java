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
package org.apache.directory.studio.schemaeditor.controller.actions;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: CollapseAllAction — Obi-Wan Kills the Tractor Beam ───────────────
// In A New Hope, Obi-Wan slips through the Death Star's corridors and shuts down
// the tractor beam in one decisive move — the clutter of enemy hold vanishes and
// the Millennium Falcon is free to go.
// This action does the same thing to the schema tree: one click collapses every
// expanded node, clearing visual noise so the user can see the big picture again.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Collapses every expanded node in the associated TreeViewer with a single click.
 * This is the "zoom out" button for the schema tree — when the user has drilled
 * deep into nested object classes and attribute types, this action resets the
 * view back to its compact, root-only state.
 * Think of this as Obi-Wan throwing the tractor-beam lever: all the expanded
 * branches snap shut in one move.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CollapseAllAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The TreeViewer */
    private TreeViewer viewer;


    // ── Obi-Wan Studies The Control Panel ────────────────────────────────────
    // Before touching anything, Obi-Wan notes which lever controls the tractor beam
    // and confirms he can reach it — then he waits, ready to act.
    // We store the viewer reference here so we know exactly which tree to collapse
    // when the moment comes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new CollapseAllAction attached to the given tree viewer.
     * We grab the viewer reference and set ourselves up with a label, tooltip,
     * and the collapse-all icon so the toolbar button looks right.
     *
     * @param viewer  the TreeViewer whose nodes we will collapse; must not be null
     */
    public CollapseAllAction( TreeViewer viewer )
    {
        super( Messages.getString( "CollapseAllAction.CollapseAllAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_COLLAPSE_ALL ) );
        setEnabled( false );
        this.viewer = viewer;
    }


    // ── Obi-Wan Throws The Lever ─────────────────────────────────────────────
    // Obi-Wan reaches up, pulls the lever, and the massive tractor-beam array goes
    // dark — every arm of the beam retracts simultaneously.
    // We call collapseAll() and every expanded subtree in the viewer snaps shut
    // in one single operation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Collapses every expanded node in the tree viewer.
     * One call to {@code viewer.collapseAll()} handles the whole thing — the JFace
     * TreeViewer walks the tree and collapses every branch for us.
     */
    public void run()
    {
        viewer.collapseAll();
    }


    // ── Relay Through The Workbench Channel ──────────────────────────────────
    // The Death Star intercom pipes the command through to Obi-Wan regardless of
    // which comm system it arrives on — the lever gets pulled either way.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when triggered through the workbench action delegate.
     *
     * @param action  the workbench action proxy; we ignore it and call our own run()
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Obi-Wan Fades: Nothing Left To Release ────────────────────────────────
    // Obi-Wan steps back and lets his cloak dissolve — no unfinished business,
    // nothing to hand back. We have nothing to clean up here either.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Report To Duty: No Special Instructions ───────────────────────────────
    // Obi-Wan checks in at the Death Star's control room, but the only thing he
    // needs is already in his hand — the workbench window adds nothing here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op init — we don't need the workbench window reference.
     *
     * @param window  the workbench window; not used here
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Tractor Beam Down: Global Selection Irrelevant ────────────────────────
    // Once the beam is down, it doesn't matter what ships are in the hangar bay —
    // the action is unconditional. We don't track workbench selection at all.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — collapse-all doesn't depend on which element is selected.
     *
     * @param action     the workbench action proxy; not used
     * @param selection  the workbench selection; not used
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
