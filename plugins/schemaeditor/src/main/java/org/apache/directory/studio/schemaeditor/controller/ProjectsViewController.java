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
package org.apache.directory.studio.schemaeditor.controller;


import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.controller.actions.CloseProjectAction;
import org.apache.directory.studio.schemaeditor.controller.actions.DeleteProjectAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ExportProjectsAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ImportProjectsAction;
import org.apache.directory.studio.schemaeditor.controller.actions.NewProjectAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenProjectAction;
import org.apache.directory.studio.schemaeditor.controller.actions.RenameProjectAction;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Project.ProjectState;
import org.apache.directory.studio.schemaeditor.view.views.ProjectsView;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ProjectsViewRoot;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;


// ── CLASS: ProjectsViewController — LUKE'S BINARY SUNSET ────────────────────
// Luke stands on the ridge at Tatooine's edge, taking in the full panorama —
// every project in the workspace is laid out before him like the twin suns
// on the horizon, each one a potential adventure he could choose to open.
// This controller wires up the ProjectsView so users can see all their
// schema projects at a glance, open or close them, create new ones, and
// manage the full lifecycle with keyboard shortcuts and context menus.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Controller for the Projects View in the Schema Editor.
 * We initialize the toolbar, context menu, viewer, double-click navigation,
 * and the workbench part listener that manages keyboard shortcut activation.
 * Think of this class as Luke on the ridge — we make the full picture of all
 * available projects visible and interactive from the moment the view opens.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ProjectsViewController
{
    /** The associated view */
    private ProjectsView view;

    /** The Context Menu */
    private MenuManager contextMenu;

    /** The TableViewer */
    private TableViewer viewer;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;

    /** Token used to activate and deactivate shortcuts in the view */
    private IContextActivation contextActivation;

    // The Actions
    private NewProjectAction newProject;
    private OpenProjectAction openProject;
    private CloseProjectAction closeProject;
    private RenameProjectAction renameProject;
    private DeleteProjectAction deleteProject;
    private ImportProjectsAction importProjects;
    private ExportProjectsAction exportProjects;


    // ── Luke Surveys The Horizon Before Setting Out ───────────────────────────
    // Luke takes one last look at the twin suns, clips every tool onto his
    // belt, and sets the moisture vaporators to their correct coordinates
    // before heading out — everything configured upfront so nothing fails later.
    // We initialize all actions, the toolbar, the context menu, the viewer,
    // the double-click handler, and the part listener in one constructor call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the controller and fully initializes the ProjectsView.
     * Every piece — actions, menus, viewer, listeners — is set up here so
     * the view is immediately interactive when it first appears.
     *
     * <p>For example — Luke doesn't leave anything unconfigured before he sets out:</p>
     * <pre>
     *   controller = new ProjectsViewController( view );
     *   // All actions registered, toolbar populated, context menu live
     * </pre>
     *
     * @param view  the ProjectsView we are controlling; must not be null
     */
    public ProjectsViewController( ProjectsView view )
    {
        this.view = view;
        viewer = view.getViewer();

        projectsHandler = Activator.getDefault().getProjectsHandler();

        initActions();
        initToolbar();
        initContextMenu();
        initViewer();
        initDoubleClickListener();
        initPartListener();
    }


    // ── Luke Lays Out His Tools On The Speeder ───────────────────────────────
    // Before crossing the dunes, Luke arranges his tools on the speeder dash —
    // each one in its right place, ready to grab without looking.
    // We instantiate all seven project-management actions so the toolbar and
    // context menu have live, fully-configured handlers to work with.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates all Action objects used by this view's toolbar and context menu.
     * We create them all here so they're fully initialized before any UI element
     * tries to call their run() or isEnabled() methods.
     *
     * <p>For example — Luke grabs every tool he needs before crossing the desert:</p>
     * <pre>
     *   newProject    = new NewProjectAction();
     *   openProject   = new OpenProjectAction( viewer );
     *   deleteProject = new DeleteProjectAction( viewer );
     *   // ... and so on for all seven actions
     * </pre>
     */
    private void initActions()
    {
        newProject = new NewProjectAction();
        openProject = new OpenProjectAction( view.getViewer() );
        closeProject = new CloseProjectAction( view.getViewer() );
        renameProject = new RenameProjectAction( view.getViewer() );
        deleteProject = new DeleteProjectAction( view.getViewer() );
        importProjects = new ImportProjectsAction();
        exportProjects = new ExportProjectsAction( view.getViewer() );
    }


    // ── Luke Clips His Most-Used Tool Front And Center ───────────────────────
    // The macrobinoculars hang right at chest height — first thing Luke reaches
    // for when he needs a quick look at the horizon.  The "New Project" action
    // is that tool: it sits first in the toolbar because creating projects is
    // the most common starting point.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds the new-project action to the view toolbar.
     * We only put the most-used action here; everything else lives in the
     * context menu to keep the toolbar uncluttered.
     *
     * <p>For example — Luke keeps his most-used tool closest to hand:</p>
     * <pre>
     *   toolbar: [ newProject ]
     * </pre>
     */
    private void initToolbar()
    {
        IToolBarManager toolbar = view.getViewSite().getActionBars().getToolBarManager();
        toolbar.add( newProject );
    }


    // ── Luke Reads The Full Landscape At A Glance ────────────────────────────
    // From the ridge Luke can see every landmark — Anchorhead, the moisture
    // farm, the canyon passes — and pick exactly where to go next.
    // The context menu gives users that same panoramic choice: new, open,
    // close, rename, delete, import, export — every project operation in one place.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds and registers the right-click context menu for the Projects View table.
     * The menu is rebuilt every time it's shown (removeAllWhenShown) so action
     * enabled states are always current.  We also register it with the site so
     * third-party plugins can contribute their own entries.
     *
     * <p>For example — Luke's full toolkit is always one look away:</p>
     * <pre>
     *   right-click: [ New | --- | Open | Close | --- | Rename | --- |
     *                  Delete | --- | Import&gt; | Export&gt; | --- ]
     * </pre>
     */
    private void initContextMenu()
    {
        contextMenu = new MenuManager( "" ); //$NON-NLS-1$
        contextMenu.setRemoveAllWhenShown( true );
        contextMenu.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( IMenuManager manager )
            {
                MenuManager importManager = new MenuManager( Messages.getString( "ProjectsViewController.ImportAction" ) ); //$NON-NLS-1$
                MenuManager exportManager = new MenuManager( Messages.getString( "ProjectsViewController.ExportAction" ) ); //$NON-NLS-1$
                manager.add( newProject );
                manager.add( new Separator() );
                manager.add( openProject );
                manager.add( closeProject );
                manager.add( new Separator() );
                manager.add( renameProject );
                manager.add( new Separator() );
                manager.add( deleteProject );
                manager.add( new Separator() );
                manager.add( importManager );
                importManager.add( importProjects );
                manager.add( exportManager );
                exportManager.add( exportProjects );

                manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
            }
        } );

        // set the context menu to the table viewer
        viewer.getControl().setMenu( contextMenu.createContextMenu( viewer.getControl() ) );

        // register the context menu to enable extension actions
        view.getSite().registerContextMenu( contextMenu, viewer );
    }


    // ── Luke Calibrates The Vaporators To Show The Full Sky ──────────────────
    // Before sunset Luke sets the vaporator sensors to collect readings from
    // the whole horizon, not just one patch of sky — and he makes sure the
    // Delete key knocks out any selected reading immediately.
    // We seed the viewer with all known projects and wire up the keyboard
    // shortcut for quick deletion.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Seeds the table viewer with the full list of projects and adds a keyboard listener.
     * The key listener maps Delete and Backspace to the delete-project action so
     * users can remove projects without right-clicking.
     *
     * <p>For example — Luke's vaporators are tuned to catch every signal:</p>
     * <pre>
     *   viewer.setInput( new ProjectsViewRoot( viewer ) );
     *   // DELETE key → deleteProject.run()
     * </pre>
     */
    private void initViewer()
    {
        viewer.setInput( new ProjectsViewRoot( viewer ) );
        viewer.getTable().addKeyListener( new KeyAdapter()
        {
            public void keyReleased( KeyEvent e )
            {
                if ( ( e.keyCode == Action.findKeyCode( "BACKSPACE" ) ) //$NON-NLS-1$
                    || ( e.keyCode == Action.findKeyCode( "DELETE" ) ) ) //$NON-NLS-1$
                {
                    deleteProject.run();
                }
            }
        } );
    }


    // ── Luke Zooms In On A Point Of Light ────────────────────────────────────
    // Luke spots a glimmer on the horizon and double-taps his macrobinoculars
    // to zoom in — if it's a closed project, he "opens" it for a closer look.
    // A double-click on a closed project calls openProject() on the handler,
    // making that project active and loading its schemas.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the double-click listener that opens a closed project.
     * If the user double-clicks a project that is currently CLOSED we open it,
     * making it the active project.  Double-clicking an already-open project
     * does nothing — it's already in view.
     *
     * <p>For example — Luke zooms in on a closed project to open it:</p>
     * <pre>
     *   doubleClick( closedProject ) → projectsHandler.openProject( project )
     *   doubleClick( openProject )   → no-op
     * </pre>
     */
    private void initDoubleClickListener()
    {
        viewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                StructuredSelection selection = ( StructuredSelection ) viewer.getSelection();

                if ( ( !selection.isEmpty() ) && ( selection.size() == 1 ) )
                {
                    Project project = ( ( ProjectWrapper ) selection.getFirstElement() ).getProject();
                    if ( project.getState().equals( ProjectState.CLOSED ) )
                    {
                        projectsHandler.openProject( project );
                    }
                }
            }
        } );
    }


    // ── Luke Tunes His Comlink When He Steps Into The Command Post ───────────
    // When Luke walks into the Rebel command post, he activates his comlink to
    // the right frequency — keyboard shortcuts live; when he leaves, he powers
    // it down so his keys don't interfere with the next person's mission.
    // We activate and deactivate Eclipse keyboard context and command handlers
    // as the Projects View gains and loses focus.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers an IPartListener2 that activates keyboard shortcuts when this view
     * is focused and deactivates them when it loses focus.
     * This prevents our New/Rename/Delete shortcuts from firing when the user is
     * working in a different view or editor.
     *
     * <p>For example — Luke's comlink is only live when he's in the command post:</p>
     * <pre>
     *   partActivated   → activate CONTEXT_PROJECTS_VIEW context + bind command handlers
     *   partDeactivated → deactivate context + unbind command handlers
     * </pre>
     */
    private void initPartListener()
    {
        view.getSite().getPage().addPartListener( new IPartListener2()
        {
            /**
              * This implementation deactivates the shortcuts when the part is deactivated.
              */
            public void partDeactivated( IWorkbenchPartReference partRef )
            {
                if ( partRef.getPart( false ) == view && contextActivation != null )
                {
                    ICommandService commandService = ( ICommandService ) PlatformUI.getWorkbench().getAdapter(
                        ICommandService.class );
                    if ( commandService != null )
                    {
                        commandService.getCommand( newProject.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( renameProject.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( deleteProject.getActionDefinitionId() ).setHandler( null );
                    }

                    IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                        IContextService.class );
                    contextService.deactivateContext( contextActivation );
                    contextActivation = null;
                }
            }


            /**
             * This implementation activates the shortcuts when the part is activated.
             */
            public void partActivated( IWorkbenchPartReference partRef )
            {
                if ( partRef.getPart( false ) == view )
                {
                    IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                        IContextService.class );
                    contextActivation = contextService.activateContext( PluginConstants.CONTEXT_PROJECTS_VIEW );

                    ICommandService commandService = ( ICommandService ) PlatformUI.getWorkbench().getAdapter(
                        ICommandService.class );
                    if ( commandService != null )
                    {
                        commandService.getCommand( newProject.getActionDefinitionId() ).setHandler(
                            new ActionHandler( newProject ) );
                        commandService.getCommand( renameProject.getActionDefinitionId() ).setHandler(
                            new ActionHandler( renameProject ) );
                        commandService.getCommand( deleteProject.getActionDefinitionId() ).setHandler(
                            new ActionHandler( deleteProject ) );
                    }
                }
            }


            public void partBroughtToTop( IWorkbenchPartReference partRef )
            {
            }


            public void partClosed( IWorkbenchPartReference partRef )
            {
            }


            public void partHidden( IWorkbenchPartReference partRef )
            {
            }


            public void partInputChanged( IWorkbenchPartReference partRef )
            {
            }


            public void partOpened( IWorkbenchPartReference partRef )
            {
            }


            public void partVisible( IWorkbenchPartReference partRef )
            {
            }

        } );
    }
}
