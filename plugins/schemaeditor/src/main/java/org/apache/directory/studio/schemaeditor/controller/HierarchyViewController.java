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

import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.PluginUtils;
import org.apache.directory.studio.schemaeditor.controller.actions.LinkWithEditorHierarchyViewAction;
import org.apache.directory.studio.schemaeditor.controller.actions.OpenHierarchyViewPreferencesAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ShowSubtypeHierarchyAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ShowSupertypeHierarchyAction;
import org.apache.directory.studio.schemaeditor.controller.actions.ShowTypeHierarchyAction;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditor;
import org.apache.directory.studio.schemaeditor.view.editors.attributetype.AttributeTypeEditorInput;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditor;
import org.apache.directory.studio.schemaeditor.view.editors.objectclass.ObjectClassEditorInput;
import org.apache.directory.studio.schemaeditor.view.views.HierarchyView;
import org.apache.directory.studio.schemaeditor.view.wrappers.AttributeTypeWrapper;
import org.apache.directory.studio.schemaeditor.view.wrappers.ObjectClassWrapper;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: HierarchyViewController — LUKE'S BINARY SUNSET ───────────────────
// Luke stands on Tatooine's ridge staring at twin suns sinking behind the
// horizon — seeing the whole galaxy laid out before him in one sweeping view.
// That's this controller: it wires up the HierarchyView so users can see the
// full inheritance landscape of the schema, type by type, from root to leaf.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Controller for the Hierarchy View in the Schema Editor.
 * We wire up all the listeners, actions, toolbar, and menu that make the
 * HierarchyView interactive and responsive to project changes.
 * Think of this class as Luke on that ridge — our job is to keep the full
 * schema inheritance picture visible and in sync no matter what changes.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class HierarchyViewController
{
    /** The associated view */
    private HierarchyView view;

    /** The authorized Preferences keys*/
    List<String> authorizedPrefs;

    /** The ProjectsHandlerListener */
    private ProjectsHandlerListener projectsHandlerListener = new ProjectsHandlerAdapter()
    {
        public void openProjectChanged( Project oldProject, Project newProject )
        {
            // Since we're changing of project, let's set the input as null
            view.setInput( null );

            if ( newProject != null )
            {
                view.getViewer().getTree().setEnabled( true );
                showTypeHierarchy.setEnabled( true );
                showSupertypeHierarchy.setEnabled( true );
                showSubtypeHierarchy.setEnabled( true );
                linkWithEditor.setEnabled( true );
                openPreferencePage.setEnabled( true );
            }
            else
            {
                view.getViewer().getTree().setEnabled( false );
                showTypeHierarchy.setEnabled( false );
                showSupertypeHierarchy.setEnabled( false );
                showSubtypeHierarchy.setEnabled( false );
                linkWithEditor.setEnabled( false );
                openPreferencePage.setEnabled( false );
            }
        }
    };

    /** The IDoubleClickListener */
    private IDoubleClickListener doubleClickListener = new IDoubleClickListener()
    {
        public void doubleClick( DoubleClickEvent event )
        {
            // What we get from the treeViewer is a StructuredSelection
            StructuredSelection selection = ( StructuredSelection ) event.getSelection();

            // Here's the real object (an AttributeTypeWrapper, ObjectClassWrapper or IntermediateNode)
            Object objectSelection = selection.getFirstElement();
            IEditorInput input = null;
            String editorId = null;

            // Selecting the right editor and input
            if ( objectSelection instanceof AttributeTypeWrapper )
            {
                input = new AttributeTypeEditorInput( ( ( AttributeTypeWrapper ) objectSelection ).getAttributeType() );
                editorId = AttributeTypeEditor.ID;
            }
            else if ( objectSelection instanceof ObjectClassWrapper )
            {
                input = new ObjectClassEditorInput( ( ( ObjectClassWrapper ) objectSelection ).getObjectClass() );
                editorId = ObjectClassEditor.ID;
            }

            // Let's open the editor
            if ( input != null )
            {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try
                {
                    page.openEditor( input, editorId );
                }
                catch ( PartInitException e )
                {
                    PluginUtils.logError( Messages.getString( "HierarchyViewController.ErrorOpeningEditor" ), e ); //$NON-NLS-1$
                    ViewUtils.displayErrorMessageDialog(
                        Messages.getString( "HierarchyViewController.Error" ), Messages //$NON-NLS-1$
                            .getString( "HierarchyViewController.ErrorOpeningEditor" ) ); //$NON-NLS-1$
                }
            }
        }
    };

    /** The IPropertyChangeListener */
    private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener()
    {
        public void propertyChange( PropertyChangeEvent event )
        {
            if ( authorizedPrefs.contains( event.getProperty() ) )
            {
                view.refresh();
            }
        }
    };

    // The Actions
    private Action showTypeHierarchy;
    private Action showSupertypeHierarchy;
    private Action showSubtypeHierarchy;
    private Action linkWithEditor;
    private Action openPreferencePage;


    // ── Luke Takes In The Twin Sunset ────────────────────────────────────────
    // Luke stands on the ridge, hands on his belt, taking in the full sweep of
    // Tatooine's horizon — two suns, one moment, everything clicking into place.
    // We do the same here: wire up every listener and action so the view is
    // ready to show the complete schema hierarchy the moment it's opened.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the controller and fully initializes the HierarchyView.
     * We set up actions, toolbar, menus, listeners, and initial state in one
     * shot so the view is immediately usable when it opens.
     *
     * <p>For example — Luke doesn't ease into the sunset gradually:</p>
     * <pre>
     *   controller = new HierarchyViewController( view );
     *   // At this point everything is wired — toolbar, double-click, prefs.
     * </pre>
     *
     * @param view  the HierarchyView we are controlling; must not be null
     */
    public HierarchyViewController( HierarchyView view )
    {
        this.view = view;

        initAuthorizedPrefs();
        initActions();
        initToolbar();
        initMenu();
        initProjectsHandlerListener();
        initDoubleClickListener();
        initPreferencesListener();
        initState();
    }


    // ── Scanning The Horizon For Landmarks ───────────────────────────────────
    // Luke sweeps his macrobinoculars across the dunes, cataloguing exactly
    // which landmarks are worth watching — Anchorhead, Tosche Station, the ridge.
    // We do the same: build the list of preference keys that actually affect
    // how the hierarchy view renders, so we only refresh when it matters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the allowlist of preference keys that should trigger a view refresh.
     * We only want to react to prefs that genuinely change what the hierarchy
     * looks like — label format, abbreviation, secondary label — not every pref
     * in the store.
     *
     * <p>For example — Luke ignores the Mos Eisley cantina chatter and focuses
     * only on what matters to his mission:</p>
     * <pre>
     *   authorizedPrefs = [ LABEL, ABBREVIATE, MAX_LENGTH,
     *                        SECONDARY_LABEL_DISPLAY, ... ]
     * </pre>
     */
    private void initAuthorizedPrefs()
    {
        authorizedPrefs = new ArrayList<String>();
        authorizedPrefs.add( PluginConstants.PREFS_HIERARCHY_VIEW_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_HIERARCHY_VIEW_ABBREVIATE_MAX_LENGTH );
        authorizedPrefs.add( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_DISPLAY );
        authorizedPrefs.add( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL );
        authorizedPrefs.add( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE );
        authorizedPrefs.add( PluginConstants.PREFS_HIERARCHY_VIEW_SECONDARY_LABEL_ABBREVIATE_MAX_LENGTH );
    }


    // ── Luke Equips His Tool Belt ─────────────────────────────────────────────
    // Before setting out across the dunes, Luke clips his macrobinoculars,
    // grappling hook, and comlink onto his belt — the right tool for each task.
    // We instantiate every Action object here so the toolbar and menus have
    // live, fully-configured handlers ready to go.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Instantiates all the Action objects used by this view's toolbar and menu.
     * We also seed the default hierarchy mode into the dialog settings if it
     * hasn't been stored before — so we don't start in an undefined state.
     *
     * <p>For example — Luke doesn't grab his tools mid-mission:</p>
     * <pre>
     *   showTypeHierarchy = new ShowTypeHierarchyAction( view );
     *   showSupertypeHierarchy = new ShowSupertypeHierarchyAction( view );
     *   // ... all five actions ready before anyone clicks anything
     * </pre>
     */
    private void initActions()
    {
        // Setting up the default key value (if needed)
        if ( Activator.getDefault().getDialogSettings().get( PluginConstants.PREFS_HIERARCHY_VIEW_MODE ) == null )
        {
            Activator.getDefault().getDialogSettings().put( PluginConstants.PREFS_HIERARCHY_VIEW_MODE,
                PluginConstants.PREFS_HIERARCHY_VIEW_MODE_TYPE );
        }
        showTypeHierarchy = new ShowTypeHierarchyAction( view );
        showSupertypeHierarchy = new ShowSupertypeHierarchyAction( view );
        showSubtypeHierarchy = new ShowSubtypeHierarchyAction( view );
        linkWithEditor = new LinkWithEditorHierarchyViewAction( view );
        openPreferencePage = new OpenHierarchyViewPreferencesAction();
    }


    // ── Laying Out The Horizon Controls ──────────────────────────────────────
    // Luke arranges his macrobinoculars, compass, and comlink on the speeder's
    // dash — everything reachable at a glance, nothing buried in a saddlebag.
    // The toolbar gives users one-click access to the most common hierarchy
    // modes right at the top of the view.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the view's toolbar with the hierarchy-mode and link-to-editor actions.
     * We add all three hierarchy-mode buttons plus a separator before the
     * link action so there's a clear visual grouping.
     *
     * <p>For example — Luke doesn't bury his macrobinoculars in a pack:</p>
     * <pre>
     *   toolbar: [ showType | showSupertype | showSubtype | --- | linkWithEditor ]
     * </pre>
     */
    private void initToolbar()
    {
        IToolBarManager toolbar = view.getViewSite().getActionBars().getToolBarManager();
        toolbar.add( showTypeHierarchy );
        toolbar.add( showSupertypeHierarchy );
        toolbar.add( showSubtypeHierarchy );
        toolbar.add( new Separator() );
        toolbar.add( linkWithEditor );
    }


    // ── The Ridge Offers Every Vantage Point ─────────────────────────────────
    // From the ridge Luke can choose to look left toward Anchorhead, right
    // toward the desert flats, or straight ahead at the suns — the menu is that
    // same set of vantage-point choices, just for schema hierarchy directions.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Populates the view's drop-down menu with hierarchy-mode, link, and preferences actions.
     * The menu mirrors the toolbar choices but also adds the preferences page link
     * for users who want to dig deeper into the view configuration.
     *
     * <p>For example — every vantage point is one pick away:</p>
     * <pre>
     *   menu: [ showType, showSupertype, showSubtype, ---, linkWithEditor,
     *           ---, openPreferencePage ]
     * </pre>
     */
    private void initMenu()
    {
        IMenuManager menu = view.getViewSite().getActionBars().getMenuManager();
        menu.add( showTypeHierarchy );
        menu.add( showSupertypeHierarchy );
        menu.add( showSubtypeHierarchy );
        menu.add( new Separator() );
        menu.add( linkWithEditor );
        menu.add( new Separator() );
        menu.add( openPreferencePage );
    }


    // ── Luke Watches For The Binary Shift ────────────────────────────────────
    // Luke keeps one eye on the sky; the moment those suns move, his whole
    // mental picture of the day changes — new project, new sky.
    // We register the ProjectsHandlerListener so we react immediately when
    // the active project switches, enabling or disabling the view accordingly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers our ProjectsHandlerListener with the global ProjectsHandler.
     * This is how we find out when the user switches to a different project
     * so we can reset the view input and toggle the enabled state of all actions.
     *
     * <p>For example — Luke doesn't wait to be told the suns have moved:</p>
     * <pre>
     *   projectsHandler.addListener( projectsHandlerListener );
     *   // From this point on, openProjectChanged() fires on every project switch
     * </pre>
     */
    private void initProjectsHandlerListener()
    {
        Activator.getDefault().getProjectsHandler().addListener( projectsHandlerListener );
    }


    // ── Luke Zooms In On A Point Of Interest ─────────────────────────────────
    // Luke spots something interesting on the horizon and zooms in with his
    // macrobinoculars — double-tap, focus, open on the detail.
    // A double-click in the hierarchy tree should open the corresponding
    // attribute type or object class editor, just like zooming in for a closer look.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Registers the double-click listener on the hierarchy tree viewer.
     * When the user double-clicks an attribute type or object class node we
     * open the appropriate editor in the workbench, letting them drill into
     * the detail of whatever caught their eye.
     *
     * <p>For example — Luke zooms in on a glint in the desert:</p>
     * <pre>
     *   viewer.addDoubleClickListener( doubleClickListener );
     *   // Double-click AttributeTypeWrapper → AttributeTypeEditor opens
     *   // Double-click ObjectClassWrapper  → ObjectClassEditor opens
     * </pre>
     */
    private void initDoubleClickListener()
    {
        view.getViewer().addDoubleClickListener( doubleClickListener );
    }


    // ── The Sunset Changes, The View Refreshes ───────────────────────────────
    // As the twin suns inch lower, Luke's view of the horizon shifts — the
    // colors change, the shadows lengthen, the whole picture updates.
    // We attach a preference-change listener so the hierarchy view refreshes
    // whenever the user tweaks a label or abbreviation preference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches a preference-change listener to the plugin's preference store.
     * We only refresh the view for preference keys in our authorized list —
     * everything else is ignored to avoid unnecessary redraws.
     *
     * <p>For example — Luke's horizon shifts with every degree the suns drop:</p>
     * <pre>
     *   preferenceStore.addPropertyChangeListener( propertyChangeListener );
     *   // When PREFS_HIERARCHY_VIEW_LABEL changes → view.refresh()
     * </pre>
     */
    private void initPreferencesListener()
    {
        Activator.getDefault().getPreferenceStore().addPropertyChangeListener( propertyChangeListener );
    }


    // ── Checking Whether The Suns Are Still Up ───────────────────────────────
    // Luke glances at the sky: are the suns still visible? If yes, he can work;
    // if they've set, it's time to wrap up and disable the controls.
    // We check whether a project is currently open and set the enabled/disabled
    // state of every action to match that reality right from the start.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the initial enabled/disabled state of all view controls based on
     * whether a project is currently open.
     * We call this once at construction time so the view never starts in a
     * half-initialized limbo — everything is either ready or visibly disabled.
     *
     * <p>For example — Luke checks the sky before deciding what to do next:</p>
     * <pre>
     *   if ( projectsHandler.getOpenProject() != null ) {
     *       // enable all actions, tree is interactive
     *   } else {
     *       // disable all actions, tree is greyed out
     *   }
     * </pre>
     */
    private void initState()
    {
        if ( Activator.getDefault().getProjectsHandler().getOpenProject() != null )
        {
            view.getViewer().getTree().setEnabled( true );
            showTypeHierarchy.setEnabled( true );
            showSupertypeHierarchy.setEnabled( true );
            showSubtypeHierarchy.setEnabled( true );
            linkWithEditor.setEnabled( true );
            openPreferencePage.setEnabled( true );
        }
        else
        {
            view.getViewer().getTree().setEnabled( false );
            showTypeHierarchy.setEnabled( false );
            showSupertypeHierarchy.setEnabled( false );
            showSubtypeHierarchy.setEnabled( false );
            linkWithEditor.setEnabled( false );
            openPreferencePage.setEnabled( false );
        }
    }


    // ── Luke Heads Home As The Suns Set ──────────────────────────────────────
    // The twin suns finally vanish below the horizon and Luke turns back toward
    // the homestead — no point watching an empty sky; time to clean up.
    // We remove every listener we registered so we don't leak memory or fire
    // updates into a view that no longer exists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up all listeners when the view is disposed.
     * We remove the ProjectsHandler listener, the double-click listener, and
     * the preference listener — if we skip any of these, we'll hold references
     * to a dead view and potentially trigger NPEs later.
     *
     * <p>For example — Luke doesn't leave his macrobinoculars on the ridge:</p>
     * <pre>
     *   projectsHandler.removeListener( projectsHandlerListener );
     *   viewer.removeDoubleClickListener( doubleClickListener );
     *   preferenceStore.removePropertyChangeListener( propertyChangeListener );
     * </pre>
     */
    public void dispose()
    {
        Activator.getDefault().getProjectsHandler().removeListener( projectsHandlerListener );
        view.getViewer().removeDoubleClickListener( doubleClickListener );
        Activator.getDefault().getPreferenceStore().removePropertyChangeListener( propertyChangeListener );
    }
}
