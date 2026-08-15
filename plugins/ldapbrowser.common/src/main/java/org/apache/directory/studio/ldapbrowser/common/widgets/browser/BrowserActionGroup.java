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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.connection.ui.actions.CollapseAllAction;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.FilterChildrenAction;
import org.apache.directory.studio.ldapbrowser.common.actions.OpenQuickSearchAction;
import org.apache.directory.studio.ldapbrowser.common.actions.PropertiesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.RefreshAction;
import org.apache.directory.studio.ldapbrowser.common.actions.UnfilterChildrenAction;
import org.apache.directory.studio.ldapbrowser.common.actions.UpAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.ActionHandlerManager;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.BrowserViewActionProxy;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.utils.ActionUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;


// ── CLASS: BrowserActionGroup — REBEL BRIEFING ROOM, YAVIN 4 ─────────────────
// In the Rebel briefing room before the Battle of Yavin, Mon Mothma assigns
// each pilot a specific role: Gold Leader escorts, Red Leader attacks the
// exhaust port. Every pilot knows their action and when to execute it.
// BrowserActionGroup does the same — it owns and coordinates all the actions
// that can be performed on the browser tree (refresh, filter, properties, etc.)
// and wires them to toolbars, menus, and keyboard shortcuts.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages all the actions available in the browser widget.
 * This includes toolbar buttons, context menu items, and keyboard shortcuts
 * for operations like navigating up, refreshing the tree, filtering entries,
 * and opening the quick search bar.
 * Think of this class as Mon Mothma running the Rebel briefing room — every
 * action gets its assignment, and we make sure it fires at the right time.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserActionGroup implements ActionHandlerManager, IMenuListener
{

    /** The open sort dialog action. */
    protected OpenSortDialogAction openSortDialogAction;

    /** The show quick search action. */
    protected ShowQuickSearchAction showQuickSearchAction;

    /** The collapse all action. */
    protected CollapseAllAction collapseAllAction;

    /** The Constant upAction. */
    protected static final String UP_ACTION = "upAction"; //$NON-NLS-1$

    /** The Constant refreshAction. */
    protected static final String REFRESH_ACTION = "refreshAction"; //$NON-NLS-1$

    /** The Constant filterChildrenAction. */
    protected static final String FILTER_CHILDREN_ACTION = "filterChildrenAction"; //$NON-NLS-1$

    /** The Constant openQuickSearchAction. */
    protected static final String OPEN_QUICK_SEARCH_ACTION = "openQuickSearch"; //$NON-NLS-1$

    /** The Constant unfilterChildrenAction. */
    protected static final String UNFILTER_CHILDREN_ACTION = "unfilterChildrenAction"; //$NON-NLS-1$

    /** The Constant propertyDialogAction. */
    protected static final String PROPERTY_DIALOG_ACTION = "propertyDialogAction"; //$NON-NLS-1$

    /** The browser action map. */
    protected Map<String, BrowserViewActionProxy> browserActionMap;

    /** The action bars. */
    protected IActionBars actionBars;

    /** The browser's main widget. */
    protected BrowserWidget mainWidget;


    // ── BRIEFING ROOM: ASSIGNING PILOTS TO ROLES ─────────────────────────────
    // Mon Mothma stands at the holo-display on Yavin 4, handing each pilot
    // their mission assignment — Red Five takes the trench run, Gold Leader
    // provides cover, others handle the surface defenses.
    // We do the same here: instantiate each action and park it in a map so the
    // rest of the class can look them up by name.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BrowserActionGroup and wires up all the browser actions.
     * We create actions for navigating up the tree, refreshing, filtering children,
     * opening the quick search, and viewing entry properties — then register them
     * with the viewer so they can respond to the current selection.
     *
     * <p>For example — Mon Mothma assigns roles before the Death Star assault:</p>
     * <pre>
     *   briefingRoom.assign(RED_FIVE, trenchRunAction);
     *   briefingRoom.assign(GOLD_LEADER, coverAction);
     *   briefingRoom.assign(RED_LEADER, surfaceDefenseAction);
     * </pre>
     *
     * @param mainWidget      the browser widget that owns these actions — we need its
     *                        viewer to attach selection-aware action proxies
     * @param configuration   the browser's configuration, which carries the preferences
     *                        we hand off to the sort dialog action
     */
    public BrowserActionGroup( BrowserWidget mainWidget, BrowserConfiguration configuration )
    {
        this.mainWidget = mainWidget;
        this.browserActionMap = new HashMap<String, BrowserViewActionProxy>();

        TreeViewer viewer = mainWidget.getViewer();
        openSortDialogAction = new OpenSortDialogAction( configuration.getPreferences() );
        showQuickSearchAction = new ShowQuickSearchAction( mainWidget.getQuickSearchWidget() );
        collapseAllAction = new CollapseAllAction( viewer );

        browserActionMap.put( OPEN_QUICK_SEARCH_ACTION, new BrowserViewActionProxy( viewer, new OpenQuickSearchAction() ) );
        browserActionMap.put( UP_ACTION, new BrowserViewActionProxy( viewer, new UpAction( viewer ) ) );
        browserActionMap.put( REFRESH_ACTION, new BrowserViewActionProxy( viewer, new RefreshAction() ) );
        browserActionMap.put( FILTER_CHILDREN_ACTION, new BrowserViewActionProxy( viewer, new FilterChildrenAction() ) );
        browserActionMap
            .put( UNFILTER_CHILDREN_ACTION, new BrowserViewActionProxy( viewer, new UnfilterChildrenAction() ) );
        browserActionMap.put( PROPERTY_DIALOG_ACTION, new BrowserViewActionProxy( viewer, new PropertiesAction() ) );
    }


    // ── STAND DOWN: PILOTS RETURN TO THE HANGAR ──────────────────────────────
    // After the battle, the surviving pilots power down and return their ships
    // to the hangar. Mon Mothma thanks everyone and closes the mission log.
    // When our widget is closed, we need to release all the action objects to
    // prevent memory leaks — this is that clean-up step.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases all actions and frees their resources.
     * This is the clean-up counterpart to the constructor — we null out all
     * references and call dispose() on each action so Eclipse can garbage-collect
     * them. Always call this when the browser widget is being closed.
     *
     * <p>For example — Mon Mothma stands down the Rebel assault force:</p>
     * <pre>
     *   trenchRunAction.standDown();
     *   coverAction.standDown();
     *   missionLog.clear();
     * </pre>
     */
    public void dispose()
    {
        if ( mainWidget != null )
        {
            openSortDialogAction.dispose();
            openSortDialogAction = null;
            showQuickSearchAction.dispose();
            showQuickSearchAction = null;
            collapseAllAction.dispose();
            collapseAllAction = null;

            for ( BrowserViewActionProxy action : browserActionMap.values() )
            {
                action.dispose();
            }
            browserActionMap.clear();
            browserActionMap = null;

            actionBars = null;
            mainWidget = null;
        }
    }


    // ── OPEN COMMS: PILOT CALLS READY ON CHANNEL ─────────────────────────────
    // Red Five radios in "Red Five, standing by" — confirming he's on the
    // shared channel and ready to receive orders from the command center.
    // We store the Eclipse action bars reference so we can wire our actions
    // into the global command framework when the widget gets focus.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Stores the Eclipse action bars so we can register global action handlers later.
     * Eclipse "action bars" are the shared toolbar and menu infrastructure for the
     * whole workbench. By holding onto them here, we can hook our browser actions
     * into global keyboard shortcuts (like F5 for Refresh) when the browser is active.
     *
     * <p>For example — Red Five checks in on the Rebel comms channel:</p>
     * <pre>
     *   commandCenter.registerPilot(RED_FIVE, commandChannel);
     *   // Red Five is now reachable through the shared channel
     * </pre>
     *
     * @param actionBars   the Eclipse action bars to register our handlers against;
     *                     if null we fall back to manual action handler registration
     */
    public void enableGlobalActionHandlers( IActionBars actionBars )
    {
        this.actionBars = actionBars;
    }


    // ── LOADING WEAPONS: TOOLBAR GETS ITS BUTTONS ────────────────────────────
    // The X-wing's weapon systems are loaded in a specific order before launch:
    // proton torpedoes first, then laser cannons, then the targeting computer.
    // We populate the browser toolbar in the same deliberate order — up arrow,
    // then separator, then refresh, then collapse all.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Adds our browser actions to the toolbar in the correct order.
     * The toolbar is the row of icon buttons at the top of the browser panel.
     * We put "up" first (navigate to parent entry), then "refresh", then
     * "collapse all" — each separated by a visual divider for clarity.
     *
     * <p>For example — loading the X-wing weapons systems before the trench run:</p>
     * <pre>
     *   xwing.loadWeapon(PROTON_TORPEDOES);
     *   xwing.loadWeapon(LASER_CANNONS);
     *   xwing.loadWeapon(TARGETING_COMPUTER);
     * </pre>
     *
     * @param toolBarManager   the Eclipse toolbar manager — think of it as the
     *                         panel of switches on the X-wing's instrument console
     */
    public void fillToolBar( IToolBarManager toolBarManager )
    {
        toolBarManager.add( browserActionMap.get( UP_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( browserActionMap.get( REFRESH_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( collapseAllAction );
        toolBarManager.update( true );
    }


    // ── MISSION OPTIONS: THE LOCAL MENU BRIEFING ─────────────────────────────
    // Mon Mothma opens the mission options menu and lists the two strategic
    // choices: sort the attack wave order, then toggle the quick search radar.
    // Our local menu (the small dropdown arrow on the view toolbar) similarly
    // offers two browser-wide settings: sort order and quick search visibility.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fills the local (view-level) pull-down menu with browser-wide options.
     * The local menu appears when the user clicks the down-arrow on the
     * browser panel toolbar. We put the sort dialog and quick search toggle here
     * since they affect the whole browser, not a specific selected entry.
     *
     * <p>For example — Mon Mothma listing strategic options at the briefing:</p>
     * <pre>
     *   briefingMenu.add(SORT_ATTACK_WAVE_ORDER);
     *   briefingMenu.add(separator);
     *   briefingMenu.add(TOGGLE_QUICK_RADAR);
     * </pre>
     *
     * @param menuManager   the Eclipse menu manager for the view's local pull-down menu
     */
    public void fillMenu( IMenuManager menuManager )
    {
        menuManager.add( openSortDialogAction );
        menuManager.add( new Separator() );
        menuManager.add( showQuickSearchAction );
        menuManager.add( new Separator() );
        menuManager.update( true );
    }


    // ── CONTEXT MENU: REGISTERING THE DYNAMIC MENU LISTENER ──────────────────
    // The battle plan changes with each new target — when you right-click a
    // different node in the tree, the context menu needs to rebuild itself
    // to show relevant actions. We set that up here by registering a listener.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets up the context menu (right-click menu) to dynamically rebuild whenever
     * it's about to be shown.
     * We configure the menu to clear itself and re-call our {@link #menuAboutToShow}
     * each time — this ensures the menu always shows actions relevant to whatever
     * the user has right-clicked on.
     *
     * <p>For example — the battle plan committee reconvenes before each new target:</p>
     * <pre>
     *   contextMenu.clearOnShow = true;
     *   contextMenu.addListener(briefingRoom::rebuildForTarget);
     * </pre>
     *
     * @param menuManager   the context (right-click) menu manager to set up
     */
    public void fillContextMenu( IMenuManager menuManager )
    {
        menuManager.setRemoveAllWhenShown( true );
        menuManager.addMenuListener( this );
    }


    // ── LAST-SECOND BRIEFING: BUILD THE RIGHT-CLICK MENU ────────────────────
    // Just before each X-wing pilot hits the trench, Mon Mothma broadcasts
    // the final set of instructions specific to that moment — no generic orders,
    // just what's needed right now for this target.
    // {@link IMenuListener} calls this method just before the context menu appears,
    // so we build the exact set of actions relevant to the current selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Dynamically populates the context menu right before it is displayed.
     * Eclipse calls this method every time the user is about to see the right-click
     * menu. We add actions in logical groups — navigation, filtering, refresh, and
     * properties — separated by visual dividers. The unfilter action is only added
     * when it's actually enabled (i.e., a filter is currently active).
     *
     * <p>For example — Mon Mothma issues last-second instructions as Red Five enters the trench:</p>
     * <pre>
     *   broadcastChannel.send(NAVIGATE_UP_INSTRUCTION);
     *   broadcastChannel.send(FILTER_ENEMY_FIRE_INSTRUCTION);
     *   broadcastChannel.send(USE_THE_FORCE_INSTRUCTION);
     * </pre>
     *
     * @param menuManager   the context menu manager that we populate right now
     */
    public void menuAboutToShow( IMenuManager menuManager )
    {
        // up
        menuManager.add( browserActionMap.get( UP_ACTION ) );
        menuManager.add( new Separator() );

        // filter
        menuManager.add( browserActionMap.get( FILTER_CHILDREN_ACTION ) );
        if ( ( browserActionMap.get( UNFILTER_CHILDREN_ACTION ) ).isEnabled() )
        {
            menuManager.add( browserActionMap.get( UNFILTER_CHILDREN_ACTION ) );
        }
        menuManager.add( browserActionMap.get( OPEN_QUICK_SEARCH_ACTION ) );
        menuManager.add( new Separator() );

        // refresh
        menuManager.add( browserActionMap.get( REFRESH_ACTION ) );
        menuManager.add( new Separator() );

        // additions
        menuManager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        menuManager.add( new Separator() );

        // properties
        menuManager.add( browserActionMap.get( PROPERTY_DIALOG_ACTION ) );
    }


    // ── SCRAMBLE: PILOTS ACTIVATE COMMS AND STAND READY ─────────────────────
    // When the Death Star appears in the Yavin system, the alarm sounds and
    // every pilot rushes to their ship — comms go live, weapons go hot,
    // everyone is reachable on the shared channel.
    // When our browser gets focus, we wire our actions to the global Eclipse
    // keyboard shortcuts (F5 = Refresh, Alt+Enter = Properties, Ctrl+F = Find).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Wires our browser actions into Eclipse's global keyboard shortcuts.
     * When the browser panel gains focus, we want F5 to refresh, Alt+Enter to
     * open properties, and Ctrl+F to toggle the quick search. This method does
     * that wiring — connecting our action objects to the shared Eclipse command
     * framework so the keyboard shortcuts work.
     *
     * <p>For example — Rebel pilots activate their comms when the Death Star arrives:</p>
     * <pre>
     *   pilot.openChannel(SHARED_REBEL_FREQUENCY);
     *   pilot.readyWeapons();
     *   commandCenter.register(pilot, SHARED_REBEL_FREQUENCY);
     * </pre>
     */
    public void activateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars.setGlobalActionHandler( ActionFactory.REFRESH.getId(), ( IAction ) browserActionMap
                .get( REFRESH_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), ( IAction ) browserActionMap
                .get( PROPERTY_DIALOG_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.FIND.getId(), showQuickSearchAction ); // IWorkbenchActionDefinitionIds.FIND_REPLACE
            actionBars.updateActionBars();
        }
        else
        {
            IAction pda = browserActionMap.get( PROPERTY_DIALOG_ACTION );
            pda.setActionDefinitionId( BrowserCommonConstants.CMD_PROPERTIES );
            ActionUtils.activateActionHandler( pda );

            IAction ra = browserActionMap.get( REFRESH_ACTION );
            ActionUtils.activateActionHandler( ra );

            showQuickSearchAction.setActionDefinitionId( BrowserCommonConstants.CMD_FIND );
            ActionUtils.activateActionHandler( showQuickSearchAction );
        }

        IAction ua = browserActionMap.get( UP_ACTION );
        ActionUtils.activateActionHandler( ua );
    }


    // ── STAND DOWN: PILOTS POWER OFF THEIR COMMS ─────────────────────────────
    // After the battle, pilots switch off their mission-specific comms so
    // they don't conflict with another squadron using the same channels.
    // When our browser loses focus, we unregister our keyboard shortcuts so
    // a different view (like the Entry Editor) can claim those same keys.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters our browser actions from Eclipse's global keyboard shortcuts.
     * This is the counterpart to {@link #activateGlobalActionHandlers()}. When the
     * browser loses focus, we remove our actions so the next active view can
     * register its own handlers for the same keys (F5, Alt+Enter, etc.).
     *
     * <p>For example — pilots power off comms when Gold Squadron takes over the channel:</p>
     * <pre>
     *   pilot.closeChannel(SHARED_REBEL_FREQUENCY);
     *   commandCenter.unregister(pilot, SHARED_REBEL_FREQUENCY);
     * </pre>
     */
    public void deactivateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars.setGlobalActionHandler( ActionFactory.REFRESH.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), null );
            actionBars.updateActionBars();
        }
        else
        {
            IAction ra = browserActionMap.get( REFRESH_ACTION );
            ActionUtils.deactivateActionHandler( ra );

            IAction pda = browserActionMap.get( PROPERTY_DIALOG_ACTION );
            ActionUtils.deactivateActionHandler( pda );
        }

        IAction ua = browserActionMap.get( UP_ACTION );
        ActionUtils.deactivateActionHandler( ua );
    }


    // ── NEW MISSION: RETARGETING FOR A DIFFERENT CONNECTION ──────────────────
    // After the Death Star is destroyed, Mon Mothma redirects the Rebel fleet
    // to a new target — every pilot's nav computer gets updated to the new
    // coordinates, and their actions re-align to the new mission context.
    // When the user switches to a different LDAP connection in the browser,
    // all actions need to update their context to the new connection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates all browser actions to reflect a newly selected LDAP connection.
     * Many actions (refresh, filter, properties) are context-sensitive — they need
     * to know which connection is active so they operate on the right server.
     * Call this when the user switches connections in the browser tree.
     *
     * <p>For example — Mon Mothma redirects the Rebel fleet to a new target after Yavin:</p>
     * <pre>
     *   fleet.retarget(NEW_IMPERIAL_BASE_COORDINATES);
     *   // All pilots update their nav computers to the new mission
     * </pre>
     *
     * @param connection   the newly active LDAP connection; all actions will
     *                     update their internal state to operate on this connection
     */
    public void setInput( IBrowserConnection connection )
    {
        for ( BrowserViewActionProxy action : browserActionMap.values() )
        {
            action.inputChanged( connection );
        }
    }

}
