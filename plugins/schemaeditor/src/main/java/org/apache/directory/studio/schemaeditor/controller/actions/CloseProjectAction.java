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
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandlerAdapter;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Project.ProjectState;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: CloseProjectAction — Order 66: Standing Down ─────────────────────
// Palpatine transmits Order 66 and clone troopers execute a precise shutdown of
// Jedi operations galaxy-wide — no hesitation, no second-guessing.
// This action does the same: when the user clicks "Close Project," we shut it
// down cleanly and update the UI so the button grays out until there's something
// open to close again.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Closes the currently selected project in the Projects view.
 * We listen to both the viewer selection and the ProjectsHandler's open-project
 * state so the action stays enabled only when a single open project is selected.
 * Think of this class as a clone trooper awaiting Order 66: it watches for the
 * right conditions and fires the shutdown the moment they are met.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CloseProjectAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TableViewer viewer;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;


    // ── Order Received: Wire Up The Listener ─────────────────────────────────
    // Palpatine records the clone-trooper frequency and patches Order 66 into the
    // comm network — every trooper is now ready to act the instant the signal arrives.
    // We register selection and project-state listeners here so we always know
    // whether there is an open project worth closing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new CloseProjectAction wired to the given viewer.
     * We hook into viewer selection changes and into the ProjectsHandler's
     * open-project events so we can enable or disable ourselves automatically.
     *
     * @param viewer  the TableViewer showing the list of projects; we attach
     *                selection listeners to it so we know what the user has picked
     */
    public CloseProjectAction( TableViewer viewer )
    {
        super( Messages.getString( "CloseProjectAction.CloseProjectAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "CloseProjectAction.CloseProjectToolTip" ) ); //$NON-NLS-1$
        setEnabled( false );
        this.viewer = viewer;
        this.viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                enableDisable();
            }
        } );
        projectsHandler = Activator.getDefault().getProjectsHandler();
        projectsHandler.addListener( new ProjectsHandlerAdapter()
        {
            public void openProjectChanged( Project oldProject, Project newProject )
            {
                enableDisable();
            }
        } );
    }


    // ── Trooper Checks Clearance Before Firing ───────────────────────────────
    // The clone checks his HUD: is there exactly one Jedi in the sights, and is
    // that Jedi currently active (not already neutralised)?
    // We do the same: one selected project, in OPEN state — only then do we arm
    // the action; otherwise we stand down.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Enables this action only when exactly one open project is selected.
     * If the selection is empty, has more than one item, or the selected project
     * is already closed, we disable ourselves so the user can't click a no-op button.
     */
    private void enableDisable()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
        {
            setEnabled( ( ( ProjectWrapper ) selection.getFirstElement() ).getProject().getState().equals(
                ProjectState.OPEN ) );
        }
        else
        {
            setEnabled( false );
        }
    }


    // ── Execute Order 66: Close The Project ──────────────────────────────────
    // The clone pulls the trigger — precise, immediate, irreversible for this cycle.
    // We grab the selected project and tell the ProjectsHandler to close it, which
    // transitions it from OPEN to CLOSED and notifies all listeners.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Closes the selected project by delegating to ProjectsHandler.
     * We guard against an empty selection (belt-and-suspenders), then call
     * {@code closeProject} which does the real state transition work.
     */
    public void run()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
        {
            projectsHandler.closeProject( ( ( ProjectWrapper ) selection.getFirstElement() ).getProject() );
        }

    }


    // ── Relay The Order Through The Chain ────────────────────────────────────
    // When command comes in via the workbench action delegate channel rather than
    // directly, the trooper relays the order down to the standard execution path.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when called via the {@link IWorkbenchWindowActionDelegate} channel.
     *
     * @param action  the workbench action proxy; we ignore it and call our own run()
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Trooper Dismissed: Nothing To Clean Up ───────────────────────────────
    // Order 66 is complete; the clone trooper stands down — no equipment to return,
    // no loose ends. We have nothing to release here either.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Briefing Room: Trooper Reports For Duty ───────────────────────────────
    // The trooper checks in at the briefing room but receives no special instructions
    // for this window — the standing order is already baked in.
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


    // ── Comms Idle: Global Selection Change Ignored ───────────────────────────
    // The trooper's comm crackles with galaxy-wide chatter, but the only signal
    // that matters to him is his own HUD — the viewer's selection listener handles
    // our updates, so the workbench-level selection event is irrelevant.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we handle selection changes via our viewer listener, not this callback.
     *
     * @param action     the workbench action proxy; not used
     * @param selection  the workbench selection; not used
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
