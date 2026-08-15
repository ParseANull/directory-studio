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
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.view.dialogs.RenameProjectDialog;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


// ── CLASS: RenameProjectAction — Clone Troopers Execute Order 66 ──────────────
// On Coruscant, Commander Fox receives Order 66 with a specific target: a named
// Jedi whose identity must be changed in every Imperial record — the old name
// struck out, a new designation written in. The troopers don't freelance;
// they confirm the target, open the rename dialog with the current name,
// and apply the new one only if the operator confirms.
// We do the same: show the current project name in the dialog, commit the rename
// only on Dialog.OK, and cancel cleanly if the user backs out.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Renames the selected project by showing a {@link RenameProjectDialog} pre-filled
 * with the project's current name, then committing the new name if confirmed.
 * We enable ourselves only when exactly one project is selected in the Projects View.
 * Think of this as the Imperial records office receiving the rename order: confirm
 * the single target, open the form, and apply it only on approval.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameProjectAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated viewer */
    private TableViewer viewer;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;


    // ── Commander Fox Reads The Target List And Stands Ready ─────────────────────
    // Fox reviews the manifest: one target at a time, one rename at a time. He hooks
    // up a scanner that watches the selection in the projects roster — as soon as
    // exactly one project is highlighted, the rename option goes live.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new RenameProjectAction tied to the given project table viewer.
     * We wire a selection listener so we enable ourselves when exactly one project
     * is selected, and start disabled.
     *
     * @param viewer  the Projects View table viewer we watch for selection changes
     */
    public RenameProjectAction( TableViewer viewer )
    {
        super( Messages.getString( "RenameProjectAction.RenameProjectAction" ) ); //$NON-NLS-1$
        setToolTipText( getText() );
        setId( PluginConstants.CMD_RENAME_PROJECT );
        setActionDefinitionId( PluginConstants.CMD_RENAME_PROJECT );
        setImageDescriptor( Activator.getDefault().getImageDescriptor( PluginConstants.IMG_RENAME ) );
        setEnabled( false );
        this.viewer = viewer;
        this.viewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();
                setEnabled( selection.size() == 1 );
            }
        } );
        projectsHandler = Activator.getDefault().getProjectsHandler();
    }


    // ── Fox Opens The Imperial Record And Awaits Confirmation ────────────────────
    // Commander Fox identifies the target in the roster (the one selected project),
    // opens the rename form pre-filled with the project's current designation, and
    // waits. If the operator signs off (Dialog.OK), Fox transmits the new name to
    // the central record system. If the operator cancels, nothing changes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the rename dialog pre-populated with the current project name, then
     * applies the new name via {@link ProjectsHandler#renameProject} if the user
     * clicks OK.
     * Nothing happens if the selection is empty or the user cancels the dialog.
     */
    public void run()
    {
        StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();
        if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
        {
            Project project = ( ( ProjectWrapper ) selection.getFirstElement() ).getProject();
            RenameProjectDialog dialog = new RenameProjectDialog( project.getName() );
            if ( dialog.open() == Dialog.OK )
            {
                projectsHandler.renameProject( project, dialog.getNewName() );
            }
        }
    }


    // ── Fox Relays The Order Down The Chain ──────────────────────────────────────
    // A subordinate repeats the rename order verbatim — no deviation.
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


    // ── Records Office Closes For The Day ────────────────────────────────────────
    // Once the rename is processed, the records office shuts down — nothing to release.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Releases any resources held by this action. We hold none, so this is a no-op.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── Fox Receives His Post Assignment ─────────────────────────────────────────
    // The commander is assigned to the Coruscant garrison — no per-window setup needed.
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


    // ── Roster Changes, Enable State Managed By Listener ────────────────────────
    // The garrison roster shifts as troopers rotate — but our selection listener on
    // the table viewer already handles enable/disable, so nothing extra needed here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when the workbench selection changes. Our table viewer's
     * selection listener already handles enable/disable; nothing extra needed here.
     *
     * @param action     the IAction proxy; unused
     * @param selection  the current selection; unused
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
