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


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.actions.CollapseAllAction;
import org.apache.directory.studio.schemaeditor.controller.actions.DeleteSchemaElementAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ExportSchemasAsOpenLdapAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ExportSchemasAsXmlAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ExportSchemasForADSAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ImportCoreSchemasAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ImportSchemasFromOpenLdapAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ImportSchemasFromXmlAction;
import org.apache.directory.studio.schemaeditor.controller.actions.LinkWithEditorSchemaViewAction;
import org.apache.directory.studio.schemaeditor.controller.actions.MergeSchemasAction;
import org.apache.directory.studio.schemaeditor.controller.actions.NewAttributeTypeAction;
import org.apache.directory.studio.schemaeditor.controller.actions.NewObjectClassAction;
import org.apache.directory.studio.schemaeditor.controller.actions.NewSchemaAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenElementAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenSchemaViewPreferenceAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenSchemaViewSortingDialogAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenTypeHierarchyAction;
import org.apache.directory.studio.schemaeditor.controller.actions.RenameSchemaElementAction;
import org.apache.directory.studio.schemaeditor.controller.actions.SwitchSchemaPresentationToFlatAction;
import org.apache.directory.studio.schemaeditor.controller.actions.SwitchSchemaPresentationToHierarchicalAction;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.schemachecker.SchemaCheckerListener;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditorInput;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditorInput;
import org.apache.directory.studio.schemaeditor.view.views.SchemaView;
import org.apache.directory.studio.schemaeditor.view.views.SchemaViewContentProvider;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.Folder;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.SchemaWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;


// ── CLASS: SchemaViewController — LUKE'S BINARY SUNSET ──────────────────────
// Luke stands on Tatooine's ridge at dusk, arms at his sides, watching the
// twin suns paint the whole horizon — seeing every schema, every attribute
// type, every object class spread out in one sweeping tree view before him.
// This controller wires up the SchemaView: it keeps the tree in sync with
// the SchemaHandler, handles double-click navigation to editors, manages
// keyboard shortcuts, and ties in the SchemaChecker for live error overlay.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Controller for the Schema View in the Schema Editor.
 * We initialize actions, toolbar, menus, context menu, project and schema
 * handler listeners, the schema checker listener, double-click navigation,
 * preference listeners, and part-activation keyboard shortcuts.
 * Think of this class as Luke on the ridge — our job is to keep the full
 * schema picture visible, interactive, and in sync no matter what changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaViewController
{
    /** The associated view */
    private SchemaView view;

    /** The TreeViewer */
    private TreeViewer viewer;

    /** The authorized Preferences keys*/
    private List<String> authorizedPrefs;

    /** The Context Menu */
    private MenuManager contextMenu;

    /** The selection */
    private ISelection selection;

    /** The SchemaHandlerListener */
    private SchemaHandlerListener schemaHandlerListener = new SchemaHandlerAdapter()
    {

        /**
         * {@inheritDoc}
         */
        public void attributeTypeAdded( AttributeType at )
        {
            SchemaViewContentProvider contentProvider = ( SchemaViewContentProvider ) viewer.getContentProvider();
            contentProvider.attributeTypeAdded( at );

            TreeNode wrapper = contentProvider.getWrapper( at );
            if ( wrapper != null )
            {
                selection = new StructuredSelection( wrapper );
            }
        }


        /**
         * {@inheritDoc}
         */
        public void attributeTypeModified( AttributeType at )
        {
            ( ( SchemaViewContentProvider ) viewer.getContentProvider() ).attributeTypeModified( at );
        }


        /**
         * {@inheritDoc}
         */
        public void attributeTypeRemoved( AttributeType at )
        {
            ( ( SchemaViewContentProvider ) viewer.getContentProvider() ).attributeTypeRemoved( at );
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassAdded( ObjectClass oc )
        {
            SchemaViewContentProvider contentProvider = ( SchemaViewContentProvider ) viewer.getContentProvider();
            contentProvider.objectClassAdded( oc );

            TreeNode wrapper = contentProvider.getWrapper( oc );
            if ( wrapper != null )
            {
                selection = new StructuredSelection( wrapper );
            }
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassModified( ObjectClass oc )
        {
            ( ( SchemaViewContentProvider ) viewer.getContentProvider() ).objectClassModified( oc );
        }


        /**
         * {@inheritDoc}
         */
        public void objectClassRemoved( ObjectClass oc )
        {
            ( ( SchemaViewContentProvider ) viewer.getContentProvider() ).objectClassRemoved( oc );
        }


        /**
         * {@inheritDoc}
         */
        public void schemaAdded( Schema schema )
        {
            SchemaViewContentProvider contentProvider = ( SchemaViewContentProvider ) viewer.getContentProvider();
            contentProvider.schemaAdded( schema );

            TreeNode wrapper = contentProvider.getWrapper( schema );
            if ( wrapper != null )
            {
                selection = new StructuredSelection( wrapper );
            }
        }


        /**
         * {@inheritDoc}
         */
        public void schemaRemoved( Schema schema )
        {
            ( ( SchemaViewContentProvider ) viewer.getContentProvider() ).schemaRemoved( schema );
        }


        /**
         * {@inheritDoc}
         */
        public void schemaRenamed( Schema schema )
        {

            view.refresh();
        }

    };

    /** The SchemaCheckerListener */
    private SchemaCheckerListener schemaCheckerListener = new SchemaCheckerListener()
    {
        public void schemaCheckerUpdated()
        {
            Display.getDefault().asyncExec( new Runnable()
            {
                public void run()
                {
                    if ( selection != null )
                    {
                        view.refresh( selection );
                        selection = null;
                    }
                    else
                    {
                        view.refresh();
                    }
                }
            } );
        }
    };

    /** Token used to activate and deactivate shortcuts in the view */
    private IContextActivation contextActivation;

    // The Actions
    private NewSchemaAction newSchema;
    private NewAttributeTypeAction newAttributeType;
    private NewObjectClassAction newObjectClass;
    private OpenElementAction openElement;
    private OpenTypeHierarchyAction openTypeHierarchy;
    private DeleteSchemaElementAction deleteSchemaElement;
    private RenameSchemaElementAction renameSchemaElement;
    private ImportCoreSchemasAction importCoreSchemas;
    private ImportSchemasFromOpenLdapAction importSchemasFromOpenLdap;
    private ImportSchemasFromXmlAction importSchemasFromXml;
    private ExportSchemasAsOpenLdapAction exportSchemasAsOpenLdap;
    private ExportSchemasAsXmlAction exportSchemasAsXml;
    private ExportSchemasForADSAction exportSchemasForADS;
    private CollapseAllAction collapseAll;
    private OpenSchemaViewSortingDialogAction openSchemaViewSortingDialog;
    private SwitchSchemaPresentationToFlatAction switchSchemaPresentationToFlat;
    private SwitchSchemaPresentationToHierarchicalAction switchSchemaPresentationToHierarchical;
    private OpenSchemaViewPreferenceAction openSchemaViewPreference;
    private LinkWithEditorSchemaViewAction linkWithEditor;
    private MergeSchemasAction mergeSchema;


    //    private CommitChangesAction commitChanges;

    // ── Luke Takes In Every Horizon Line At Once ──────────────────────────────
    // Luke stands on the ridge and in one long exhale takes in everything:
    // the suns, the dunes, the distant settlement, the moisture farms — he
    // knows exactly where each one is before he even starts walking.
    // We do the same here: initialize every action, toolbar, menu, listener,
    // and state in one constructor call so the view is immediately complete.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the controller and fully initializes the SchemaView.
     * Every piece — actions, toolbar, menu, context menu, project listener,
     * schema checker listener, double-click handler, preferences listener,
     * initial state, and part listener — is set up here.
     *
     * <p>For example — Luke configures his whole vantage point at once:</p>
     * <pre>
     *   controller = new SchemaViewController( view );
     *   // From this point the view is live and responds to all schema changes
     * </pre>
     *
     * @param view  the SchemaView we are controlling; must not be null
     */
    public SchemaViewController( SchemaView view )
    {
        this.view = view;
        viewer = view.getViewer();

        initActions();
        initToolbar();
        initMenu();
        initContextMenu();
        initProjectsHandlerListener();
        initSchemaCheckerListener();
        initDoubleClickListener();
        initAuthorizedPrefs();
        initPreferencesListener();
        initState();
        initPartListener();
    }


    // ── Luke Clips Every Tool Onto His Belt ───────────────────────────────────
    // Before setting out across the dunes, Luke clips every tool — macrobinoculars,
    // grappling hook, comlink, survival kit — onto his belt in their correct
    // positions.  We do the same: instantiate all twenty-ish Action objects.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates all Action objects used by the schema view's toolbar, menus,
     * and context menu.  We create them here so they're fully configured before
     * any UI element queries their enabled state or label.
     *
     * <p>For example — Luke lays out every tool before the journey starts:</p>
     * <pre>
     *   newSchema       = new NewSchemaAction();
     *   newAttributeType = new NewAttributeTypeAction( viewer );
     *   // ... all actions ready
     * </pre>
     */
    private void initActions()
    {
        newSchema = new NewSchemaAction();
        newAttributeType = new NewAttributeTypeAction( viewer );
        newObjectClass = new NewObjectClassAction( viewer );
        openElement = new OpenElementAction( viewer );
        openTypeHierarchy = new OpenTypeHierarchyAction( viewer );
        deleteSchemaElement = new DeleteSchemaElementAction( viewer );
        renameSchemaElement = new RenameSchemaElementAction( viewer );
        importCoreSchemas = new ImportCoreSchemasAction();
        importSchemasFromOpenLdap = new ImportSchemasFromOpenLdapAction();
        importSchemasFromXml = new ImportSchemasFromXmlAction();
        exportSchemasAsOpenLdap = new ExportSchemasAsOpenLdapAction( viewer );
        exportSchemasAsXml = new ExportSchemasAsXmlAction( viewer );
        exportSchemasForADS = new ExportSchemasForADSAction( viewer );
        collapseAll = new CollapseAllAction( viewer );
        openSchemaViewSortingDialog = new OpenSchemaViewSortingDialogAction();
        switchSchemaPresentationToFlat = new SwitchSchemaPresentationToFlatAction();
        switchSchemaPresentationToHierarchical = new SwitchSchemaPresentationToHierarchicalAction();
        openSchemaViewPreference = new OpenSchemaViewPreferenceAction();
        linkWithEditor = new LinkWithEditorSchemaViewAction( view );
        mergeSchema = new MergeSchemasAction();
        //        commitChanges = new CommitChangesAction();
    }


    // ── Luke Arranges His Most-Used Tools At The Front ────────────────────────
    // The three tools Luke reaches for most often — macrobinoculars, grappling
    // hook, and comlink — ride at the front of his belt for immediate access.
    // New Schema, New Attribute Type, and New Object Class sit first in the
    // toolbar for the same reason.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the view toolbar with the most-used schema-creation and navigation actions.
     * We add a separator between creation actions and utility actions (collapse, link)
     * to create a clear visual grouping.
     *
     * <p>For example — Luke's most-used tools are right at the front:</p>
     * <pre>
     *   toolbar: [ newSchema | newAttributeType | newObjectClass | --- | collapseAll | linkWithEditor ]
     * </pre>
     */
    private void initToolbar()
    {
        IToolBarManager toolbar = view.getViewSite().getActionBars().getToolBarManager();
        toolbar.add( newSchema );
        toolbar.add( newAttributeType );
        toolbar.add( newObjectClass );
        //        toolbar.add( new Separator() );
        //        toolbar.add( commitChanges );
        toolbar.add( new Separator() );
        toolbar.add( collapseAll );
        toolbar.add( linkWithEditor );
    }


    // ── Luke Maps The Full Horizon In His Head ────────────────────────────────
    // Luke mentally maps every landmark he can see from the ridge — sorting
    // them, framing them, choosing which way to go.  The view drop-down menu
    // is that map: sorting options, presentation mode, link-to-editor, and prefs.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the view's drop-down menu with sorting, presentation, link, and preferences actions.
     * The schema-presentation sub-menu lets users switch between flat and hierarchical
     * display modes without opening the full preferences page.
     *
     * <p>For example — Luke surveys every option before choosing his path:</p>
     * <pre>
     *   menu: [ sortingDialog | --- | SchemaPresentation&gt;[flat|hierarchical] |
     *           --- | linkWithEditor | --- | openPreference ]
     * </pre>
     */
    private void initMenu()
    {
        IMenuManager menu = view.getViewSite().getActionBars().getMenuManager();
        menu.add( openSchemaViewSortingDialog );
        menu.add( new Separator() );
        IMenuManager schemaPresentationMenu = new MenuManager( Messages
            .getString( "SchemaViewController.SchemaPresentationAction" ) ); //$NON-NLS-1$
        schemaPresentationMenu.add( switchSchemaPresentationToFlat );
        schemaPresentationMenu.add( switchSchemaPresentationToHierarchical );
        menu.add( schemaPresentationMenu );
        menu.add( new Separator() );
        menu.add( linkWithEditor );
        menu.add( new Separator() );
        menu.add( openSchemaViewPreference );
    }


    // ── Luke Can Reach Every Landmark From Where He Stands ───────────────────
    // From the ridge Luke can see and access every part of his world — and the
    // right-click context menu is that same comprehensive reach: new elements,
    // open, hierarchy, delete, rename, import, export.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds and registers the right-click context menu on the schema tree viewer.
     * The menu is rebuilt each time it's shown so enabled states are always current.
     * We also register it with the site so third-party plugins can contribute entries.
     *
     * <p>For example — every part of the schema landscape is reachable from the ridge:</p>
     * <pre>
     *   right-click: [ New&gt;[schema|at|oc] | --- | Open | TypeHierarchy | --- |
     *                  Delete | --- | Rename | --- | Import&gt; | Export&gt; ]
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
                MenuManager newManager = new MenuManager( Messages.getString( "SchemaViewController.NewAction" ) ); //$NON-NLS-1$
                MenuManager importManager = new MenuManager( Messages.getString( "SchemaViewController.ImportAction" ) ); //$NON-NLS-1$
                MenuManager exportManager = new MenuManager( Messages.getString( "SchemaViewController.ExportAction" ) ); //$NON-NLS-1$
                manager.add( newManager );
                newManager.add( newSchema );
                newManager.add( newAttributeType );
                newManager.add( newObjectClass );
                manager.add( new Separator() );
                manager.add( openElement );
                manager.add( openTypeHierarchy );
                manager.add( new Separator() );
                manager.add( deleteSchemaElement );
                manager.add( new Separator() );
                manager.add( renameSchemaElement );
                manager.add( new Separator() );
                manager.add( importManager );
                importManager.add( importCoreSchemas );
                importManager.add( new Separator() );
                importManager.add( importSchemasFromOpenLdap );
                importManager.add( importSchemasFromXml );
                importManager.add( new Separator() );
                importManager.add( mergeSchema );
                manager.add( exportManager );
                exportManager.add( exportSchemasAsOpenLdap );
                exportManager.add( exportSchemasAsXml );
                exportManager.add( new Separator() );
                exportManager.add( exportSchemasForADS );

                manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
            }
        } );

        // set the context menu to the table viewer
        viewer.getControl().setMenu( contextMenu.createContextMenu( viewer.getControl() ) );

        // register the context menu to enable extension actions
        view.getSite().registerContextMenu( contextMenu, viewer );
    }


    // ── Luke Adjusts His Horizon As The Project Changes ──────────────────────
    // When Luke switches which direction he's looking, his whole horizon
    // changes — a new project means a new schema landscape, and the old one's
    // listener should be cleaned up before the new one's is wired in.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers a ProjectsHandlerListener that wires/unwires the schema handler
     * listener as the active project changes.
     * When a new project opens we attach our schemaHandlerListener to its
     * SchemaHandler and reload the viewer.  When the project closes we detach
     * the listener and clear the viewer.
     *
     * <p>For example — Luke re-scans the horizon when he turns to face a new direction:</p>
     * <pre>
     *   openProjectChanged( old, new ) → removeSchemaHandlerListener( old )
     *                                  → addSchemaHandlerListener( new )
     *                                  → view.reloadViewer()
     * </pre>
     */
    private void initProjectsHandlerListener()
    {
        Activator.getDefault().getProjectsHandler().addListener( new ProjectsHandlerAdapter()
        {
            public void openProjectChanged( Project oldProject, Project newProject )
            {
                if ( oldProject != null )
                {
                    removeSchemaHandlerListener( oldProject );
                }

                if ( newProject != null )
                {
                    viewer.getTree().setEnabled( true );
                    newSchema.setEnabled( true );
                    newAttributeType.setEnabled( true );
                    newObjectClass.setEnabled( true );
                    collapseAll.setEnabled( true );
                    linkWithEditor.setEnabled( true );
                    openSchemaViewSortingDialog.setEnabled( true );
                    switchSchemaPresentationToFlat.setEnabled( true );
                    switchSchemaPresentationToHierarchical.setEnabled( true );
                    openSchemaViewPreference.setEnabled( true );
                    //                    commitChanges.setEnabled( true );

                    addSchemaHandlerListener( newProject );
                    view.reloadViewer();
                }
                else
                {
                    viewer.setInput( null );
                    viewer.getTree().setEnabled( false );
                    newSchema.setEnabled( false );
                    newAttributeType.setEnabled( false );
                    newObjectClass.setEnabled( false );
                    collapseAll.setEnabled( false );
                    linkWithEditor.setEnabled( false );
                    openSchemaViewSortingDialog.setEnabled( false );
                    switchSchemaPresentationToFlat.setEnabled( false );
                    switchSchemaPresentationToHierarchical.setEnabled( false );
                    openSchemaViewPreference.setEnabled( false );
                    //                    commitChanges.setEnabled( false );
                }
            }
        } );
    }


    // ── Luke Tunes Into A New Horizon's Frequency ────────────────────────────
    // When Luke turns to face a new project's horizon, he tunes his macrobinoculars
    // to that direction's frequency so every change in that landscape reaches him.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches our schemaHandlerListener to the given project's SchemaHandler.
     * Called when a new project is opened so we start receiving schema mutation
     * events for it.
     *
     * <p>For example — Luke re-tunes to the new horizon:</p>
     * <pre>
     *   addSchemaHandlerListener( project );
     *   // schemaHandlerListener now receives add/modify/remove events from project
     * </pre>
     *
     * @param project  the newly opened project; its SchemaHandler must not be null
     */
    private void addSchemaHandlerListener( Project project )
    {
        SchemaHandler schemaHandler = project.getSchemaHandler();
        if ( schemaHandler != null )
        {
            schemaHandler.addListener( schemaHandlerListener );
        }
    }


    // ── Luke Stops Watching An Old Horizon ───────────────────────────────────
    // When Luke turns away from the first horizon he stops listening to its
    // signals so they don't clutter his attention while he watches the new one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches our schemaHandlerListener from the given project's SchemaHandler.
     * Called when a project is closed so we stop receiving stale events from it.
     *
     * @param project  the project being closed; its SchemaHandler must not be null
     */
    private void removeSchemaHandlerListener( Project project )
    {
        SchemaHandler schemaHandler = project.getSchemaHandler();
        if ( schemaHandler != null )
        {
            schemaHandler.removeListener( schemaHandlerListener );
        }
    }


    // ── Luke Listens For The Error-Overlay Signal ─────────────────────────────
    // While watching the sunset, Luke keeps one ear tuned to the farm's alarm
    // system — if something breaks in the schema, the error overlay fires and
    // Luke knows which node needs attention before anyone else does.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the SchemaCheckerListener so schema validation results trigger
     * a view refresh with error decorators on the affected nodes.
     * We always refresh asynchronously on the UI thread to avoid SWT
     * cross-thread violations.
     *
     * <p>For example — Luke stays tuned to the farm's alarm while watching the sunset:</p>
     * <pre>
     *   schemaChecker.addListener( schemaCheckerListener );
     *   // On each validation run, view.refresh() or view.refresh( selection ) fires
     * </pre>
     */
    private void initSchemaCheckerListener()
    {
        Activator.getDefault().getSchemaChecker().addListener( schemaCheckerListener );
    }


    // ── Luke Zooms In For A Closer Look ──────────────────────────────────────
    // Luke spots an interesting silhouette on the horizon and double-taps his
    // macrobinoculars to zoom in — if it's an attribute type, he opens that
    // editor; if it's an object class, he opens the OC editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the double-click listener on the schema tree viewer.
     * A double-click on an attribute type or object class node opens the
     * corresponding editor.  Double-clicking a folder or schema wrapper just
     * toggles expand/collapse without opening an editor.
     *
     * <p>For example — Luke zooms in on exactly the node he cares about:</p>
     * <pre>
     *   doubleClick( AttributeTypeWrapper ) → AttributeTypeEditor opens
     *   doubleClick( ObjectClassWrapper )   → ObjectClassEditor opens
     *   doubleClick( Folder | Schema )      → toggle expand/collapse
     * </pre>
     */
    private void initDoubleClickListener()
    {
        viewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

                TreeViewer viewer = view.getViewer();

                // What we get from the viewer is a StructuredSelection
                StructuredSelection selection = ( StructuredSelection ) event.getSelection();

                // Here's the real object (an AttributeTypeWrapper, ObjectClassWrapper or IntermediateNode)
                Object objectSelection = selection.getFirstElement();
                IEditorInput input = null;
                String editorId = null;

                // Selecting the right editor and input
                if ( objectSelection instanceof AttributeTypeWrapper )
                {
                    input = new AttributeTypeEditorInput( ( ( AttributeTypeWrapper ) objectSelection )
                        .getAttributeType() );
                    editorId = AttributeTypeEditor.ID;
                }
                else if ( objectSelection instanceof ObjectClassWrapper )
                {
                    input = new ObjectClassEditorInput( ( ( ObjectClassWrapper ) objectSelection ).getObjectClass() );
                    editorId = ObjectClassEditor.ID;
                }
                else if ( ( objectSelection instanceof Folder ) || ( objectSelection instanceof SchemaWrapper ) )
                {
                    // Here we don't open an editor, we just expand the node.
                    viewer.setExpandedState( objectSelection, !viewer.getExpandedState( objectSelection ) );
                }

                // Let's open the editor
                if ( input != null )
                {
                    try
                    {
                        page.openEditor( input, editorId );
                    }
                    catch ( PartInitException e )
                    {
                        PluginUtils.logError( Messages.getString( "SchemaViewController.ErrorOpeningEditor" ), e ); //$NON-NLS-1$
                        ViewUtils.displayErrorMessageDialog(
                            Messages.getString( "SchemaViewController.error" ), Messages //$NON-NLS-1$
                                .getString( "SchemaViewController.ErrorOpeningEditor" ) ); //$NON-NLS-1$
                    }
                }
            }
        } );
    }


    // ── Luke Notes Which Parts Of The Horizon Are Worth Watching ─────────────
    // Luke knows which landmark changes actually matter for his mission —
    // label format, abbreviation, grouping — and ignores the rest.
    // We build the authorized-prefs list so the preference listener only
    // triggers a refresh for changes that actually affect what the tree shows.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the list of preference keys that should trigger a view refresh.
     * Only prefs that affect how the schema tree renders are included; everything
     * else is ignored to avoid unnecessary tree redraws.
     *
     * <p>For example — Luke only watches the landmarks that affect his route:</p>
     * <pre>
     *   authorizedPrefs = [ LABEL, ABBREVIATE, MAX_LENGTH, SECONDARY_LABEL_DISPLAY,
     *                        GROUPING, SORTING_BY, SORTING_ORDER, SCHEMA_PRESENTATION, ... ]
     * </pre>
     */
    private void initAuthorizedPrefs()
    {
        authorizedPrefs = new ArrayList<String>();
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_ABBREVIATE_MAX_LENGTH );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_DISPLAY );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_BY );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SORTING_ORDER );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION );
        authorizedPrefs.add( PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_LABEL_DISPLAY );
    }


    // ── Luke Watches For Changes In The Light ────────────────────────────────
    // As the suns shift, Luke notices when the light on the horizon changes —
    // some changes are dramatic enough to require a full reload (grouping mode
    // switch), others just a repaint (label format tweak).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches a preference-store listener that refreshes or reloads the viewer
     * when a schema-view preference changes.
     * Grouping and schema-presentation changes require a full reload because they
     * restructure the tree; all other authorized-pref changes just repaint.
     *
     * <p>For example — Luke reacts differently to a dramatic sunset versus a slight haze:</p>
     * <pre>
     *   PREFS_SCHEMA_VIEW_GROUPING → view.reloadViewer()   (tree structure changes)
     *   PREFS_SCHEMA_VIEW_LABEL    → view.refresh()        (repaint only)
     * </pre>
     */
    private void initPreferencesListener()
    {
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void propertyChange( PropertyChangeEvent event )
            {
                if ( authorizedPrefs.contains( event.getProperty() ) )
                {
                    if ( PluginConstants.PREFS_SCHEMA_VIEW_GROUPING.equals( event.getProperty() )
                        || PluginConstants.PREFS_SCHEMA_VIEW_SCHEMA_PRESENTATION.equals( event.getProperty() ) )
                    {
                        view.reloadViewer();
                    }
                    else
                    {
                        view.refresh();
                    }
                }
            }
        } );
    }


    // ── Luke Checks Whether The Suns Are Still Up ────────────────────────────
    // Before starting his evening routine, Luke checks: are the suns still
    // visible?  If yes, he enables all controls; if not, he disables them.
    // We check whether a project is currently open and configure the view
    // accordingly so it never starts in an ambiguous half-enabled state.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the initial enabled/disabled state of all actions based on whether
     * a project is currently open.
     * If a project is already open (e.g., we're being created after a restart),
     * we wire the schema handler listener and reload the viewer immediately.
     *
     * <p>For example — Luke checks the sky before deciding what to do:</p>
     * <pre>
     *   if ( openProject != null ) { enable all; addSchemaHandlerListener; reloadViewer }
     *   else                       { disable all }
     * </pre>
     */
    private void initState()
    {
        Project project = Activator.getDefault().getProjectsHandler().getOpenProject();
        if ( project != null )
        {
            viewer.getTree().setEnabled( true );
            newSchema.setEnabled( true );
            newAttributeType.setEnabled( true );
            newObjectClass.setEnabled( true );
            collapseAll.setEnabled( true );
            linkWithEditor.setEnabled( true );
            openSchemaViewSortingDialog.setEnabled( true );
            switchSchemaPresentationToFlat.setEnabled( true );
            switchSchemaPresentationToHierarchical.setEnabled( true );
            openSchemaViewPreference.setEnabled( true );
            //            commitChanges.setEnabled( true );

            addSchemaHandlerListener( project );
            view.reloadViewer();
        }
        else
        {
            viewer.getTree().setEnabled( false );
            newSchema.setEnabled( false );
            newAttributeType.setEnabled( false );
            newObjectClass.setEnabled( false );
            collapseAll.setEnabled( false );
            linkWithEditor.setEnabled( false );
            openSchemaViewSortingDialog.setEnabled( false );
            switchSchemaPresentationToFlat.setEnabled( false );
            switchSchemaPresentationToHierarchical.setEnabled( false );
            openSchemaViewPreference.setEnabled( false );
            //            commitChanges.setEnabled( false );
        }
    }


    // ── Luke Activates His Comlink When He Enters The Command Post ────────────
    // Luke walks into the Rebel command post and clicks his comlink live —
    // keyboard shortcuts are active only while he's there; when he steps out,
    // he powers it down so the keys don't fire in the wrong context.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers a workbench part listener that activates keyboard shortcuts when
     * this view gains focus and deactivates them when it loses focus.
     * This prevents our schema-creation and navigation shortcuts from firing
     * while the user is working in a different view or editor.
     *
     * <p>For example — Luke's comlink is only live when he's in the command post:</p>
     * <pre>
     *   partActivated   → activate CONTEXT_SCHEMA_VIEW + bind command handlers
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
                        commandService.getCommand( newSchema.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( newAttributeType.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( newObjectClass.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( openElement.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( openTypeHierarchy.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( deleteSchemaElement.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( renameSchemaElement.getActionDefinitionId() ).setHandler( null );
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
                    contextActivation = contextService.activateContext( PluginConstants.CONTEXT_SCHEMA_VIEW );

                    ICommandService commandService = ( ICommandService ) PlatformUI.getWorkbench().getAdapter(
                        ICommandService.class );
                    if ( commandService != null )
                    {
                        commandService.getCommand( newSchema.getActionDefinitionId() ).setHandler(
                            new ActionHandler( newSchema ) );
                        commandService.getCommand( newAttributeType.getActionDefinitionId() ).setHandler(
                            new ActionHandler( newAttributeType ) );
                        commandService.getCommand( newObjectClass.getActionDefinitionId() ).setHandler(
                            new ActionHandler( newObjectClass ) );
                        commandService.getCommand( openElement.getActionDefinitionId() ).setHandler(
                            new ActionHandler( openElement ) );
                        commandService.getCommand( openTypeHierarchy.getActionDefinitionId() ).setHandler(
                            new ActionHandler( openTypeHierarchy ) );
                        commandService.getCommand( deleteSchemaElement.getActionDefinitionId() ).setHandler(
                            new ActionHandler( deleteSchemaElement ) );
                        commandService.getCommand( renameSchemaElement.getActionDefinitionId() ).setHandler(
                            new ActionHandler( renameSchemaElement ) );
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
