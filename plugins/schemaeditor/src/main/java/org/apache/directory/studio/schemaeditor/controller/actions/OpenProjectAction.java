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


// ── CLASS: OpenProjectAction — Han Jumps To Hyperspace ────────────────────────
// Han Solo sits in the Millennium Falcon's cockpit, fingers on the hyperdrive
// lever. He checks: is the ship ready? Is the nav computer locked? Only when
// those conditions are met does he throw the lever and make the jump.
// We do the same: the action is only enabled when exactly one project is selected
// and it's in CLOSED state. When run() is called, we tell the ProjectsHandler
// to open it — the Falcon lurches into hyperspace and the project goes live.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens a closed project so it becomes the active schema-editing context.
 * We gate activation on a single CLOSED project being selected — you can't jump
 * to hyperspace twice, and you can't open an already-open project.
 * Think of Han pulling the hyperdrive lever: pre-flight checks run first,
 * and only when everything lines up do we make the jump.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenProjectAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TableViewer viewer;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;


    // ── Han Runs Pre-Flight Checks And Grabs The Throttle ────────────────────────
    // Han walks into the cockpit, checks the instruments, and hooks up listeners:
    // one to the selection (is there a ship in the bay?) and one to the projects
    // handler (did another ship just take off or land?). Both affect whether he
    // can pull the lever.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenProjectAction tied to the given project table viewer.
     * We attach listeners to both the viewer (for selection changes) and the
     * {@link ProjectsHandler} (for project state changes) so we stay in sync
     * with what's actually openable at any given moment.
     *
     * @param viewer  the Projects View table viewer we watch for selection
     */
    public OpenProjectAction( TableViewer viewer )
    {
        super( Messages.getString( "OpenProjectAction.OpenProjectAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "OpenProjectAction.OpenProjectToolTip" ) ); //$NON-NLS-1$
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


    // ── Instrument Check: Is The Ship Ready To Fly? ───────────────────────────────
    // Han glances at the instruments: exactly one ship in the bay, and its status
    // light shows CLOSED (docked, not already running). Only then does the lever
    // light up green.
    // We check: exactly one row selected, and that project's state is CLOSED.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables this action based on the current selection and project state.
     * We're only enabled when exactly one project is selected and its state is CLOSED —
     * there's no point in opening an already-open project.
     */
    private void enableDisable()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
        {
            setEnabled( ( ( ProjectWrapper ) selection.getFirstElement() ).getProject().getState().equals(
                ProjectState.CLOSED ) );
        }
        else
        {
            setEnabled( false );
        }
    }


    // ── Han Throws The Hyperdrive Lever ───────────────────────────────────────────
    // The pre-flight check passes: one ship, CLOSED status, ready to go. Han grabs
    // the lever, Chewie howls, and the Falcon blasts into hyperspace.
    // We tell projectsHandler.openProject() to transition the selected project from
    // CLOSED to OPEN — the schema data loads and the editor views come alive.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the selected project, transitioning it from CLOSED to OPEN state.
     * If the selection is empty or has more than one item we bail — this is a guard
     * that should never trigger in practice since we're disabled in those cases.
     */
    public void run()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
        {
            projectsHandler.openProject( ( ( ProjectWrapper ) selection.getFirstElement() ).getProject() );
        }

    }


    // ── Chewie Relays Han's Decision ──────────────────────────────────────────────
    // Chewie repeats Han's command through the comm: same message, different channel.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} so Eclipse's command framework can invoke us.
     *
     * @param action  the IAction proxy; unused
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Falcon Docked, Crew Stands Down ──────────────────────────────────────────
    // The mission's over; the Falcon is docked and the crew rests — no resources
    // to release.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Han Gets His Docking Bay Assignment ──────────────────────────────────────
    // The docking master assigns Han to Bay 94 — but he doesn't need to do anything
    // special per docking bay.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when this action is bound to a workbench window. No per-window setup needed.
     *
     * @param window  the workbench window; unused
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    // ── Traffic Control Announces Changes, Han Checks His Own Instruments ─────────
    // Traffic control calls out a new ship arrival — but Han's already watching
    // his own instruments via the listeners set up in the constructor.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes. Our internal listener
     * on the table viewer already handles enable/disable, so nothing needed here.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
