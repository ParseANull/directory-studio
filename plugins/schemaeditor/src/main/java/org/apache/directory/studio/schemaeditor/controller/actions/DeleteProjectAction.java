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


import java.util.Iterator;

import org.apache.directory.studio.connection.core.ConnectionCoreConstants;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Project.ProjectState;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: DeleteProjectAction — Clone Troopers Storm The Jedi Temple ────────
// When Order 66 hits its final phase, clone troopers march into the Jedi Temple
// itself and erase the Jedi presence there — thorough, irreversible, confirmed.
// This action does the same to schema projects: it asks for confirmation, then
// removes every selected project from the workspace, closing any that are still open.
// ────────────────────────────────────────────────────────────────────────────
/**
 * Deletes one or more schema projects from the Projects view.
 * We show a confirmation dialog first (listing project names for small selections),
 * then close any open projects before removing them entirely from the handler.
 * Think of this as clone troopers securing the Jedi Temple: they confirm the
 * target list, then execute systematically — no project left standing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteProjectAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TableViewer viewer;


    // ── Troopers Receive The Target List ──────────────────────────────────────
    // The clone captain studies the Temple floor plan and registers which corridors
    // to watch — he's ready to update the target count as the mission unfolds.
    // We attach a selection listener so we know how many projects are selected and
    // can update our label ("Delete Project" vs "Delete Projects") accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DeleteProjectAction wired to the given viewer.
     * We hook a selection listener so we can enable/disable and adjust our label
     * depending on how many projects the user has selected.
     *
     * @param viewer  the TableViewer listing the projects; we watch its selection
     *                to know when we should be enabled and what label to show
     */
    public DeleteProjectAction( TableViewer viewer )
    {
        super( Messages.getString( "DeleteProjectAction.DeleteProjectAction" ) ); //$NON-NLS-1$
        setToolTipText( Messages.getString( "DeleteProjectAction.DeleteProjectToolTip" ) ); //$NON-NLS-1$
        setId( PluginConstants.CMD_DELETE_PROJECT );
        setActionDefinitionId( PluginConstants.CMD_DELETE_PROJECT );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_DELETE ) );
        setEnabled( false );
        this.viewer = viewer;
        this.viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();
                if ( selection.size() == 1 )
                {
                    setText( Messages.getString( "DeleteProjectAction.DeleteProjectAction" ) ); //$NON-NLS-1$
                    setEnabled( true );
                }
                else if ( selection.size() > 1 )
                {
                    setText( Messages.getString( "DeleteProjectAction.DeleteProjectsAction" ) ); //$NON-NLS-1$
                    setEnabled( true );
                }
                else
                {
                    setText( Messages.getString( "DeleteProjectAction.DeleteProjectAction" ) ); //$NON-NLS-1$
                    setEnabled( false );
                }
            }
        } );
    }


    // ── Troopers Confirm Targets Then Open Fire ───────────────────────────────
    // The clone captain assembles the name list, shows it to command for sign-off,
    // and only then gives the order to proceed — then each target is taken down
    // in sequence, closed first if still active.
    // We build a confirmation message (listing names when there are five or fewer),
    // ask the user to confirm, then close-then-remove each selected project.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Asks the user to confirm, then deletes every selected project.
     * For selections of five or fewer we list the project names in the dialog; for
     * larger batches we use a generic count message to keep the dialog manageable.
     * Any open project is closed before deletion so the handler stays consistent.
     */
    public void run()
    {
        ProjectsHandler projectsHandler = Activator.getDefault().getProjectsHandler();
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();

        if ( !selection.isEmpty() )
        {
            StringBuilder title = new StringBuilder();
            StringBuilder message = new StringBuilder();

            int count = selection.size();

            if ( count <= 5 )
            {
                if ( count == 1 )
                {
                    // Only 1 project to delete
                    title.append( Messages.getString( "DeleteProjectAction.DeleteProjectTitle" ) ); //$NON-NLS-1$
                    message.append( Messages.getString( "DeleteProjectAction.SureDeleteFollowingProject" ) ); //$NON-NLS-1$
                }
                else
                {
                    // Between 2 to 5 projects to delete
                    title.append( Messages.getString( "DeleteProjectAction.DeleteProjectsTitle" ) ); //$NON-NLS-1$
                    message.append( Messages.getString( "DeleteProjectAction.SureDeleteFollowingProjects" ) ); //$NON-NLS-1$
                }

                // Appending the projects names
                for ( Iterator<?> iterator = selection.iterator(); iterator.hasNext(); )
                {
                    message.append( ConnectionCoreConstants.LINE_SEPARATOR );
                    message.append( "  - " ); //$NON-NLS-1$
                    message.append( ( ( ProjectWrapper ) iterator.next() ).getProject().getName() );
                }
            }
            else
            {
                // More than 5 projects to delete
                title.append( Messages.getString( "DeleteProjectAction.DeleteProjectsTitle" ) ); //$NON-NLS-1$
                message.append( Messages.getString( "DeleteProjectAction.SureDeleteSelectedProjects" ) ); //$NON-NLS-1$
            }

            // Showing the confirmation window
            if ( MessageDialog.openConfirm( viewer.getControl().getShell(), title.toString(), message.toString() ) )
            {
                for ( Iterator<?> iterator = selection.iterator(); iterator.hasNext(); )
                {
                    ProjectWrapper wrapper = ( ProjectWrapper ) iterator.next();
                    Project project = wrapper.getProject();

                    if ( project.getState() == ProjectState.OPEN )
                    {
                        // Closing the project before removing it.
                        projectsHandler.closeProject( project );
                    }

                    projectsHandler.removeProject( project );
                }
            }
        }
    }


    // ── Order Relayed Through Imperial Comms ──────────────────────────────────
    // The command arrives via the standard Imperial communications relay — the
    // trooper executes the same action regardless of which channel it came through.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Delegates to {@link #run()} when called via the workbench action delegate channel.
     *
     * @param action  the workbench action proxy; we ignore it and call our own run()
     */
    public void run( IAction action )
    {
        run();
    }


    // ── Temple Secured: Nothing Left To Release ───────────────────────────────
    // The troopers withdraw — the Temple is empty, the mission is done, nothing
     // to hand back. We hold no resources that need cleanup.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op dispose — we hold no resources that need explicit cleanup.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Captain Reports To Command: No Special Orders ─────────────────────────
    // The clone captain checks in at the command post but receives no additional
    // instructions tied to this workbench window — standing orders suffice.
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


    // ── Comms Idle: Workbench Selection Ignored ────────────────────────────────
    // The trooper's own viewer listener already handles selection updates; the
    // workbench-level selection event carries nothing new for us.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we track selection through our own viewer listener, not this callback.
     *
     * @param action     the workbench action proxy; not used
     * @param selection  the workbench selection; not used
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
