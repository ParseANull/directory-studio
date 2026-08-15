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

package org.apache.directory.studio;


import org.apache.directory.studio.actions.OpenFileAction;
import org.apache.directory.studio.actions.ReportABugAction;
import org.apache.directory.studio.view.ImageKeys;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardDropDownAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.plugin.AbstractUIPlugin;


// ── CLASS: ApplicationActionBarAdvisor — The Rebel Mission Briefing Room ──────
// In the Yavin IV briefing room, Mon Mothma assigns every pilot their role:
// General Dodonna points to the holographic Death Star and names each squadron's
// target, each pilot is handed a specific weapon (X-wing, Y-wing, blaster).
// ApplicationActionBarAdvisor does the same for Eclipse: it creates every menu
// item and toolbar button, gives each one an ID and an icon, then arranges them
// in the correct positions on the menu bar and cool bar.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Builds the menu bar, toolbar (cool bar), and keyboard-shortcut bindings for
 * each workbench window.
 * Eclipse calls {@link #makeActions} to create and register the actions, then
 * calls {@link #fillMenuBar} and {@link #fillCoolBar} to arrange them on screen.
 * Registering actions (rather than just creating them) ensures keyboard bindings
 * defined in plugin.xml actually work, and also means Eclipse disposes the actions
 * when the window closes.
 * Think of this as General Dodonna assigning roles in the Yavin briefing room —
 * every pilot gets their mission, every action gets its place in the UI.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{
    private static final String OS_MACOSX = "macosx"; //$NON-NLS-1$
    private OpenFileAction openFileAction;
    private IWorkbenchAction closeAction;
    private IWorkbenchAction closeAllAction;
    private IWorkbenchAction saveAction;
    private IWorkbenchAction saveAsAction;
    private IWorkbenchAction saveAllAction;
    private IWorkbenchAction printAction;
    private IWorkbenchAction refreshAction;
    private IWorkbenchAction renameAction;
    private IWorkbenchAction moveAction;
    private IWorkbenchAction exitAction;
    private IWorkbenchAction aboutAction;
    private IWorkbenchAction preferencesAction;
    private IWorkbenchAction helpAction;
    private IWorkbenchAction dynamicHelpAction;
    private IWorkbenchAction newAction;
    private IWorkbenchAction newDropDownAction;
    private IWorkbenchAction importAction;
    private IWorkbenchAction exportAction;
    private IWorkbenchAction propertiesAction;
    private IWorkbenchAction closePerspectiveAction;
    private IWorkbenchAction closeAllPerspectivesAction;
    private IWorkbenchAction undoAction;
    private IWorkbenchAction redoAction;
    private IWorkbenchAction cutAction;
    private IWorkbenchAction copyAction;
    private IWorkbenchAction pasteAction;
    private IWorkbenchAction deleteAction;
    private IWorkbenchAction selectAllAction;
    private IWorkbenchAction findAction;
    private IContributionItem perspectivesList;
    private IContributionItem viewsList;
    private IContributionItem reopenEditorsList;
    private ReportABugAction reportABug;
    private IWorkbenchAction backwardHistoryAction;
    private IWorkbenchAction forwardHistoryAction;
    private IWorkbenchAction nextAction;
    private IWorkbenchAction previousAction;
    private IWorkbenchAction introAction;


    // ── Dodonna Receives the Briefing Packet ──────────────────────────────────
    // General Dodonna receives the mission configuration packet from Mon Mothma
    // before the briefing begins — it tells him which window he's briefing and
    // what facilities he has available to register actions with.
    // Our constructor just passes the configurer to the parent ActionBarAdvisor
    // so Eclipse can do its internal setup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action bar advisor with the given configuration handle.
     * Eclipse passes us the {@code configurer} so we can call
     * {@code register(action)} during {@link #makeActions} to wire up keyboard
     * bindings and ensure automatic disposal.
     *
     * @param configurer  Eclipse's action bar configurer — passed to the parent.
     */
    public ApplicationActionBarAdvisor( IActionBarConfigurer configurer )
    {
        super( configurer );
    }


    // ── Dodonna Assigns Every Pilot a Role in the Attack ──────────────────────
    // Dodonna points to the holographic Death Star and assigns each squadron:
    // Red Leader takes the trench run, Gold Squadron goes for the surface
    // cannons, and so on — every pilot registered and ready.
    // makeActions() creates every menu action (File > Open, Edit > Copy, etc.),
    // sets labels and icons, and registers them with Eclipse so keyboard bindings
    // work and disposal is automatic when the window closes.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates all workbench actions and registers them for keyboard binding support.
     * Registering is needed to ensure that key bindings work — the corresponding
     * command keybindings are defined in plugin.xml.  Registering also provides
     * automatic disposal of the actions when the window is closed.
     * We create standard Eclipse actions (via {@link ActionFactory}) plus our own
     * custom actions ({@link OpenFileAction}, {@link ReportABugAction}).
     *
     * @param window  the workbench window this advisor is serving — we pass it
     *                to {@link ActionFactory} methods to create window-scoped actions.
     */
    protected void makeActions( final IWorkbenchWindow window )
    {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

        newAction = ActionFactory.NEW.create( window );
        register( newAction );
        newAction.setText( Messages.getString( "ApplicationActionBarAdvisor.new" ) ); //$NON-NLS-1$

        newDropDownAction = new NewWizardDropDownAction( window );

        openFileAction = new OpenFileAction( window );
        register( openFileAction );

        closeAction = ActionFactory.CLOSE.create( window );
        register( closeAction );

        closeAllAction = ActionFactory.CLOSE_ALL.create( window );
        register( closeAllAction );

        saveAction = ActionFactory.SAVE.create( window );
        register( saveAction );

        saveAsAction = ActionFactory.SAVE_AS.create( window );
        register( saveAsAction );

        saveAllAction = ActionFactory.SAVE_ALL.create( window );
        register( saveAllAction );

        printAction = ActionFactory.PRINT.create( window );
        register( printAction );

        moveAction = ActionFactory.MOVE.create( window );
        register( moveAction );

        renameAction = ActionFactory.RENAME.create( window );
        register( renameAction );

        refreshAction = ActionFactory.REFRESH.create( window );
        register( refreshAction );

        importAction = ActionFactory.IMPORT.create( window );
        register( importAction );

        exportAction = ActionFactory.EXPORT.create( window );
        register( exportAction );

        propertiesAction = ActionFactory.PROPERTIES.create( window );
        register( propertiesAction );

        exitAction = ActionFactory.QUIT.create( window );
        register( exitAction );

        undoAction = ActionFactory.UNDO.create( window );
        register( undoAction );

        redoAction = ActionFactory.REDO.create( window );
        register( redoAction );

        cutAction = ActionFactory.CUT.create( window );
        register( cutAction );

        copyAction = ActionFactory.COPY.create( window );
        register( copyAction );

        pasteAction = ActionFactory.PASTE.create( window );
        register( pasteAction );

        deleteAction = ActionFactory.DELETE.create( window );
        register( deleteAction );

        selectAllAction = ActionFactory.SELECT_ALL.create( window );
        register( selectAllAction );

        findAction = ActionFactory.FIND.create( window );
        register( findAction );

        closePerspectiveAction = ActionFactory.CLOSE_PERSPECTIVE.create( window );
        register( closePerspectiveAction );

        closeAllPerspectivesAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create( window );
        register( closeAllPerspectivesAction );

        aboutAction = ActionFactory.ABOUT.create( window );
        aboutAction.setImageDescriptor( AbstractUIPlugin.imageDescriptorFromPlugin( Application.PLUGIN_ID,
            ImageKeys.ABOUT ) );
        register( aboutAction );

        preferencesAction = ActionFactory.PREFERENCES.create( window );
        preferencesAction.setImageDescriptor( AbstractUIPlugin.imageDescriptorFromPlugin( Application.PLUGIN_ID,
            ImageKeys.SHOW_PREFERENCES ) );
        register( preferencesAction );

        helpAction = ActionFactory.HELP_CONTENTS.create( window );
        register( helpAction );

        dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create( window );
        register( dynamicHelpAction );

        viewsList = ContributionItemFactory.VIEWS_SHORTLIST.create( window );
        perspectivesList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create( window );
        reopenEditorsList = ContributionItemFactory.REOPEN_EDITORS.create( window );

        reportABug = new ReportABugAction( window );
        reportABug.setImageDescriptor( AbstractUIPlugin.imageDescriptorFromPlugin( Application.PLUGIN_ID,
            ImageKeys.REPORT_BUG ) );
        register( reportABug );

        forwardHistoryAction = ActionFactory.FORWARD_HISTORY.create( window );
        register( forwardHistoryAction );

        backwardHistoryAction = ActionFactory.BACKWARD_HISTORY.create( window );
        register( backwardHistoryAction );

        nextAction = ActionFactory.NEXT.create( window );
        register( nextAction );

        previousAction = ActionFactory.PREVIOUS.create( window );
        register( previousAction );

        introAction = ActionFactory.INTRO.create( window );
        introAction.setImageDescriptor( AbstractUIPlugin.imageDescriptorFromPlugin( Application.PLUGIN_ID,
            ImageKeys.INTRO ) );
        register( introAction );

    }


    // ── Dodonna Arranges the Squadrons on the Holographic Map ─────────────────
    // Dodonna projects the tactical display and places each squadron at its
    // assigned position: Red Squadron to the north trench, Gold Squadron to the
    // south surface — File menu here, Edit menu there, Help menu at the end.
    // fillMenuBar() takes all the registered actions and arranges them into the
    // correct menus in the correct order, with OS-specific special cases for macOS.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the workbench window's menu bar with all our registered actions.
     * We create the top-level menus (File, Edit, Navigate, Window, Help) and add
     * each action to the appropriate menu in the conventional order.
     * On macOS, the "Preferences" and "About" items are handled by the OS menu;
     * we hide our copies in a hidden menu to avoid duplicates.
     *
     * @param menuBar  the JFace menu manager for the window's menu bar — we add
     *                 our top-level menus and separators to it.
     */
    protected void fillMenuBar( IMenuManager menuBar )
    {
        // Getting the OS
        String os = Platform.getOS();

        // Creating menus
        MenuManager fileMenu = new MenuManager(
            Messages.getString( "ApplicationActionBarAdvisor.file" ), IWorkbenchActionConstants.M_FILE ); //$NON-NLS-1$
        MenuManager editMenu = new MenuManager(
            Messages.getString( "ApplicationActionBarAdvisor.edit" ), IWorkbenchActionConstants.M_EDIT ); //$NON-NLS-1$
        MenuManager navigateMenu = new MenuManager(
            Messages.getString( "ApplicationActionBarAdvisor.navigate" ), IWorkbenchActionConstants.M_NAVIGATE ); //$NON-NLS-1$
        MenuManager windowMenu = new MenuManager(
            Messages.getString( "ApplicationActionBarAdvisor.windows" ), IWorkbenchActionConstants.M_WINDOW ); //$NON-NLS-1$
        MenuManager helpMenu = new MenuManager(
            Messages.getString( "ApplicationActionBarAdvisor.help" ), IWorkbenchActionConstants.M_HELP ); //$NON-NLS-1$
        MenuManager hiddenMenu = new MenuManager( "Hidden", "org.apache.directory.studio.rcp.hidden" ); //$NON-NLS-1$ //$NON-NLS-2$
        hiddenMenu.setVisible( false );

        // Adding menus
        menuBar.add( fileMenu );
        menuBar.add( editMenu );
        menuBar.add( navigateMenu );
        // Add a group marker indicating where action set menus will appear.
        menuBar.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
        menuBar.add( windowMenu );
        menuBar.add( helpMenu );
        menuBar.add( hiddenMenu );

        // Populating File Menu
        fileMenu.add( newAction );
        fileMenu.add( new GroupMarker( IWorkbenchActionConstants.NEW_EXT ) );
        fileMenu.add( openFileAction );
        fileMenu.add( new GroupMarker( IWorkbenchActionConstants.OPEN_EXT ) );
        fileMenu.add( new Separator() );
        fileMenu.add( closeAction );
        fileMenu.add( closeAllAction );
        fileMenu.add( new GroupMarker( IWorkbenchActionConstants.CLOSE_EXT ) );
        fileMenu.add( new Separator() );
        fileMenu.add( saveAction );
        fileMenu.add( saveAsAction );
        fileMenu.add( saveAllAction );
        fileMenu.add( new GroupMarker( IWorkbenchActionConstants.SAVE_EXT ) );
        fileMenu.add( new Separator() );
        fileMenu.add( refreshAction );
        fileMenu.add( new Separator() );
        fileMenu.add( printAction );
        fileMenu.add( new GroupMarker( IWorkbenchActionConstants.PRINT_EXT ) );
        fileMenu.add( new Separator() );
        fileMenu.add( importAction );
        fileMenu.add( exportAction );
        fileMenu.add( new GroupMarker( IWorkbenchActionConstants.IMPORT_EXT ) );
        fileMenu.add( new Separator() );
        fileMenu.add( propertiesAction );
        fileMenu.add( reopenEditorsList );
        fileMenu.add( new GroupMarker( IWorkbenchActionConstants.MRU ) );
        if ( ApplicationActionBarAdvisor.OS_MACOSX.equalsIgnoreCase( os ) )
        {
            // We hide the exit (quit) action, it will be added by the "Carbon" plugin
            hiddenMenu.add( exitAction );
        }
        else
        {
            fileMenu.add( new Separator() );
            fileMenu.add( exitAction );
        }

        // Populating Edit Menu
        editMenu.add( undoAction );
        editMenu.add( redoAction );
        editMenu.add( new Separator() );
        editMenu.add( cutAction );
        editMenu.add( copyAction );
        editMenu.add( pasteAction );
        editMenu.add( new Separator() );
        editMenu.add( deleteAction );
        editMenu.add( selectAllAction );
        editMenu.add( new Separator() );
        editMenu.add( moveAction );
        editMenu.add( renameAction );
        editMenu.add( new Separator() );
        editMenu.add( findAction );
        editMenu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));

        // Populating Navigate Menu
        navigateMenu.add( nextAction );
        navigateMenu.add( previousAction );
        navigateMenu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        navigateMenu.add( new GroupMarker( IWorkbenchActionConstants.NAV_END ) );
        navigateMenu.add( new Separator() );
        navigateMenu.add( backwardHistoryAction );
        navigateMenu.add( forwardHistoryAction );

        // Window
        MenuManager perspectiveMenu = new MenuManager( Messages
            .getString( "ApplicationActionBarAdvisor.openPerspective" ), "openPerspective" ); //$NON-NLS-1$ //$NON-NLS-2$
        perspectiveMenu.add( perspectivesList );
        windowMenu.add( perspectiveMenu );
        MenuManager viewMenu = new MenuManager( Messages.getString( "ApplicationActionBarAdvisor.showView" ) ); //$NON-NLS-1$
        viewMenu.add( viewsList );
        windowMenu.add( viewMenu );
        windowMenu.add( new Separator() );
        windowMenu.add( closePerspectiveAction );
        windowMenu.add( closeAllPerspectivesAction );
        if ( ApplicationActionBarAdvisor.OS_MACOSX.equalsIgnoreCase( os ) )
        {
            // We hide the preferences action, it will be added by the "Carbon" plugin
            hiddenMenu.add( preferencesAction );
        }
        else
        {
            windowMenu.add( new Separator() );
            windowMenu.add( preferencesAction );
        }

        // Help
        helpMenu.add( introAction );
        helpMenu.add( new Separator() );
        helpMenu.add( helpAction );
        helpMenu.add( dynamicHelpAction );
        helpMenu.add( new Separator() );
        helpMenu.add( reportABug );
        helpMenu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        if ( ApplicationActionBarAdvisor.OS_MACOSX.equalsIgnoreCase( os ) )
        {
            // We hide the about action, it will be added by the "Carbon" plugin
            hiddenMenu.add( aboutAction );
        }
        else
        {
            helpMenu.add( new Separator() );
            helpMenu.add( aboutAction );
        }
    }


    // ── Dodonna Stocks the Weapons Locker — Toolbar Actions Ready ─────────────
    // After the verbal briefing, Dodonna's team loads the physical weapons into
    // the ready racks: New-wizard launcher, Save, Print, Preferences, and the
    // navigation history controls for flying back through previous positions.
    // fillCoolBar() places the most-used actions into the Eclipse cool bar
    // (the icon toolbar area) so they're one click away.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the cool bar (the main toolbar area) with the most-used actions.
     * We add a main toolbar with New, Save, Print, and Preferences, a group marker
     * for other plugins to add their own toolbar items, and a navigation toolbar
     * with back/forward history buttons.
     *
     * @param coolBar  the JFace cool bar manager — we add {@link ToolBarContributionItem}
     *                 instances and group markers to it.
     */
    protected void fillCoolBar( ICoolBarManager coolBar )
    {
        // add main tool bar
        IToolBarManager toolbar = new ToolBarManager( SWT.FLAT | SWT.RIGHT );
        toolbar.add( newDropDownAction );
        toolbar.add( saveAction );
        toolbar.add( printAction );
        toolbar.add( preferencesAction );
        coolBar.add( new ToolBarContributionItem( toolbar, Application.PLUGIN_ID + ".toolbar" ) ); //$NON-NLS-1$

        // add marker for additions
        coolBar.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );

        // add navigation tool bar
        // some actions are added from org.eclipse.ui.editor to the HISTORY_GROUP
        IToolBarManager navToolBar = new ToolBarManager( SWT.FLAT | SWT.RIGHT );
        navToolBar.add( new Separator( IWorkbenchActionConstants.HISTORY_GROUP ) );
        navToolBar.add( backwardHistoryAction );
        navToolBar.add( forwardHistoryAction );
        coolBar.add( new ToolBarContributionItem( navToolBar, IWorkbenchActionConstants.TOOLBAR_NAVIGATE ) );
    }
}
