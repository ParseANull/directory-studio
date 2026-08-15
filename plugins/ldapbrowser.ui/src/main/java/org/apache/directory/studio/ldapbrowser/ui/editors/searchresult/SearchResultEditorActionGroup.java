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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.common.actions.NewValueAction;
import org.apache.directory.studio.ldapbrowser.common.actions.PropertiesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.RefreshAction;
import org.apache.directory.studio.ldapbrowser.common.actions.ShowDecoratedValuesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.ValueEditorPreferencesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.ActionHandlerManager;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.BrowserActionProxy;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyAttributeDescriptionAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyDnAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyEntryAsCsvAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopySearchFilterAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyUrlAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyValueAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.LocateDnInDitAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.NewBatchOperationAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.NewSearchAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.OpenSchemaBrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.OpenSearchResultAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.proxy.SearchResultEditorActionProxy;
import org.apache.directory.studio.utils.ActionUtils;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;


// ── CLASS: SearchResultEditorActionGroup — Clone Troopers Receiving Order 66 ──
// When Order 66 goes out, every clone trooper across the galaxy has their
// assignment: some guard the toolbar, some manage the context menu, some handle
// global keybindings.  Each trooper knows their role and activates or deactivates
// on command.  This class is the commander that deploys all of them.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages all actions available in the search result editor: toolbar buttons,
 * context menu items, and global keybinding handlers.
 * We create every action at construction time, wire them into their menu/toolbar
 * slots, and expose {@link #activateGlobalActionHandlers()} /
 * {@link #deactivateGlobalActionHandlers()} so the editor can toggle them on/off
 * while a cell editor is active.
 * Think of this as the clone commander who deploys each trooper (action) to the
 * right position when Order 66 is issued.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorActionGroup implements ActionHandlerManager, IMenuListener
{

    /** The show Dn action. */
    private ShowDNAction showDNAction;

    /** The show links action. */
    private ShowLinksAction showLinksAction;

    /** The show decorated values action. */
    private ShowDecoratedValuesAction showDecoratedValuesAction;

    /** The open search result editor preference page. */
    private OpenSearchResultEditorPreferencePage openSearchResultEditorPreferencePage;

    /** The show quick filter action. */
    private ShowQuickFilterAction showQuickFilterAction;

    /** The open default editor action. */
    private SearchResultEditorActionProxy openDefaultValueEditorActionProxy;

    /** The open best editor action. */
    private SearchResultEditorActionProxy openBestValueEditorActionProxy;

    /** The open editor actions. */
    private SearchResultEditorActionProxy[] openValueEditorActionProxies;

    /** The open entry value editor action. */
    private SearchResultEditorActionProxy openEntryValueEditorActionProxy;

    /** The open value editor preferences action. */
    private ValueEditorPreferencesAction openValueEditorPreferencesAction;

    private static final String copyTableAction = "copyTableAction"; //$NON-NLS-1$

    private static final String refreshSearchAction = "refreshSearchAction"; //$NON-NLS-1$

    private final static String newValueAction = "newValueAction"; //$NON-NLS-1$

    private final static String newSearchAction = "newSearchAction"; //$NON-NLS-1$

    private static final String newBatchOperationAction = "newBatchOperationAction"; //$NON-NLS-1$

    private final static String copyAction = "copyAction"; //$NON-NLS-1$

    private final static String pasteAction = "pasteAction"; //$NON-NLS-1$

    private final static String deleteAction = "deleteAction"; //$NON-NLS-1$

    private static final String copyDnAction = "copyDnAction"; //$NON-NLS-1$

    private static final String copyUrlAction = "copyUrlAction"; //$NON-NLS-1$

    private static final String copyAttriuteDescriptionAction = "copyAttriuteDescriptionAction"; //$NON-NLS-1$

    private static final String copyDisplayValueAction = "copyDisplayValueAction"; //$NON-NLS-1$

    private static final String copyValueUtf8Action = "copyValueUtf8Action"; //$NON-NLS-1$

    private static final String copyValueBase64Action = "copyValueBase64Action"; //$NON-NLS-1$

    private static final String copyValueHexAction = "copyValueHexAction"; //$NON-NLS-1$

    private static final String copyValueAsLdifAction = "copyValueAsLdifAction"; //$NON-NLS-1$

    private static final String copySearchFilterAction = "copySearchFilterAction"; //$NON-NLS-1$

    private static final String copyNotSearchFilterAction = "copyNotSearchFilterAction"; //$NON-NLS-1$

    private static final String copyAndSearchFilterAction = "copyAndSearchFilterAction"; //$NON-NLS-1$

    private static final String copyOrSearchFilterAction = "copyOrSearchFilterAction"; //$NON-NLS-1$

    private static final String openSearchResultAction = "showEntryInSearchResultsAction"; //$NON-NLS-1$

    private static final String locateDnInDitAction = "locateDnInDitAction"; //$NON-NLS-1$

    private static final String showOcdAction = "showOcdAction"; //$NON-NLS-1$

    private static final String showAtdAction = "showAtdAction"; //$NON-NLS-1$

    private static final String showEqualityMrdAction = "showEqualityMrdAction"; //$NON-NLS-1$

    private static final String showSubstringMrdAction = "showSubstringMrdAction"; //$NON-NLS-1$

    private static final String showOrderingMrdAction = "showOrderingMrdAction"; //$NON-NLS-1$

    private static final String showLsdAction = "showLsdAction"; //$NON-NLS-1$

    private final static String propertyDialogAction = "propertyDialogAction"; //$NON-NLS-1$

    /** The search result editor action map. */
    private Map<String, SearchResultEditorActionProxy> searchResultEditorActionMap;

    /** The action bars. */
    private IActionBars actionBars;

    /** The search result editor. */
    private SearchResultEditor searchResultEditor;


    // ── Commander Deploys All Troopers ────────────────────────────────────────
    // When the commander receives their orders, they immediately deploy every trooper
    // to their station: ShowDNAction to the menu, value editor actions to the context
    // menu, copy/paste/delete actions to the global handlers.  Each trooper is created,
    // briefed with the current viewer/cursor, and stored in the map.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates all actions for the search result editor and stores them ready for use.
     * We resolve the viewer and cursor from the editor, then instantiate every action
     * that can appear in a toolbar, menu, or keybinding.  The editor doesn't need to
     * know which action class handles which command — it just asks us.
     *
     * <p>For example — the commander deploys the squad:</p>
     * <pre>
     *   viewer = editor.getMainWidget().getViewer();
     *   cursor = configuration.getCursor(viewer);
     *   showDNAction = new ShowDNAction();          // trooper 1
     *   openBestValueEditorProxy = new ...;         // trooper 2
     *   // ... many more ...
     * </pre>
     *
     * @param searchResultEditor the editor this group belongs to; used to resolve
     *                           the viewer, cursor, and configuration
     */
    public SearchResultEditorActionGroup( SearchResultEditor searchResultEditor )
    {
        this.searchResultEditor = searchResultEditor;
        searchResultEditorActionMap = new HashMap<String, SearchResultEditorActionProxy>();

        TableViewer viewer = searchResultEditor.getMainWidget().getViewer();
        SearchResultEditorCursor cursor = searchResultEditor.getConfiguration().getCursor( viewer );
        ValueEditorManager valueEditorManager = searchResultEditor.getConfiguration().getValueEditorManager( viewer );

        showDNAction = new ShowDNAction();
        showLinksAction = new ShowLinksAction();
        showDecoratedValuesAction = new ShowDecoratedValuesAction();
        openSearchResultEditorPreferencePage = new OpenSearchResultEditorPreferencePage();
        showQuickFilterAction = new ShowQuickFilterAction( searchResultEditor.getMainWidget().getQuickFilterWidget() );

        openBestValueEditorActionProxy = new SearchResultEditorActionProxy( cursor, new OpenBestEditorAction( viewer,
            cursor, valueEditorManager, this ) );
        openDefaultValueEditorActionProxy = new SearchResultEditorActionProxy( cursor, new OpenDefaultEditorAction(
            viewer, cursor, valueEditorManager, openBestValueEditorActionProxy, this ) );
        IValueEditor[] valueEditors = searchResultEditor.getConfiguration().getValueEditorManager( viewer )
            .getAllValueEditors();
        openValueEditorActionProxies = new SearchResultEditorActionProxy[valueEditors.length];
        for ( int i = 0; i < openValueEditorActionProxies.length; i++ )
        {
            openValueEditorActionProxies[i] = new SearchResultEditorActionProxy( cursor, new OpenEditorAction( viewer,
                cursor, valueEditorManager, valueEditors[i], this ) );
        }
        openEntryValueEditorActionProxy = new SearchResultEditorActionProxy( cursor, new OpenEntryEditorAction( viewer,
            cursor, valueEditorManager, valueEditorManager.getEntryValueEditor(), this ) );
        openValueEditorPreferencesAction = new ValueEditorPreferencesAction();

        searchResultEditorActionMap.put( copyTableAction, new SearchResultEditorActionProxy( cursor,
            new CopyEntryAsCsvAction( CopyEntryAsCsvAction.MODE_TABLE ) ) );
        searchResultEditorActionMap.put( refreshSearchAction, new SearchResultEditorActionProxy( cursor,
            new RefreshAction() ) );

        searchResultEditorActionMap.put( newValueAction, new SearchResultEditorActionProxy( cursor,
            new NewValueAction() ) );
        searchResultEditorActionMap.put( newSearchAction, new SearchResultEditorActionProxy( cursor,
            new NewSearchAction() ) );
        searchResultEditorActionMap.put( newBatchOperationAction, new SearchResultEditorActionProxy( cursor,
            new NewBatchOperationAction() ) );

        searchResultEditorActionMap.put( locateDnInDitAction, new SearchResultEditorActionProxy( cursor,
            new LocateDnInDitAction() ) );
        searchResultEditorActionMap.put( openSearchResultAction, new SearchResultEditorActionProxy( cursor,
            new OpenSearchResultAction() ) );

        searchResultEditorActionMap.put( showOcdAction, new SearchResultEditorActionProxy( cursor,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_OBJECTCLASS ) ) );
        searchResultEditorActionMap.put( showAtdAction, new SearchResultEditorActionProxy( cursor,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_ATTRIBUTETYPE ) ) );
        searchResultEditorActionMap.put( showEqualityMrdAction, new SearchResultEditorActionProxy( cursor,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_EQUALITYMATCHINGRULE ) ) );
        searchResultEditorActionMap.put( showSubstringMrdAction, new SearchResultEditorActionProxy( cursor,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_SUBSTRINGMATCHINGRULE ) ) );
        searchResultEditorActionMap.put( showOrderingMrdAction, new SearchResultEditorActionProxy( cursor,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_ORDERINGMATCHINGRULE ) ) );
        searchResultEditorActionMap.put( showLsdAction, new SearchResultEditorActionProxy( cursor,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_SYNTAX ) ) );

        searchResultEditorActionMap.put( pasteAction, new SearchResultEditorActionProxy( cursor,
            new SearchResultEditorPasteAction() ) );
        searchResultEditorActionMap.put( copyAction, new SearchResultEditorActionProxy( cursor, new CopyAction(
            ( BrowserActionProxy ) this.searchResultEditorActionMap.get( pasteAction ), valueEditorManager ) ) );
        searchResultEditorActionMap.put( deleteAction, new SearchResultEditorActionProxy( cursor,
            new SearchResultDeleteAction() ) );

        searchResultEditorActionMap.put( copyDnAction, new SearchResultEditorActionProxy( cursor, new CopyDnAction() ) );
        searchResultEditorActionMap
            .put( copyUrlAction, new SearchResultEditorActionProxy( cursor, new CopyUrlAction() ) );
        searchResultEditorActionMap.put( copyAttriuteDescriptionAction, new SearchResultEditorActionProxy( cursor,
            new CopyAttributeDescriptionAction() ) );
        searchResultEditorActionMap.put( copyDisplayValueAction, new SearchResultEditorActionProxy( cursor,
            new CopyValueAction( CopyValueAction.Mode.DISPLAY, valueEditorManager ) ) );
        searchResultEditorActionMap.put( copyValueUtf8Action, new SearchResultEditorActionProxy( cursor,
            new CopyValueAction( CopyValueAction.Mode.UTF8, valueEditorManager ) ) );
        searchResultEditorActionMap.put( copyValueBase64Action, new SearchResultEditorActionProxy( cursor,
            new CopyValueAction( CopyValueAction.Mode.BASE64, valueEditorManager ) ) );
        searchResultEditorActionMap.put( copyValueHexAction, new SearchResultEditorActionProxy( cursor,
            new CopyValueAction( CopyValueAction.Mode.HEX, valueEditorManager ) ) );
        searchResultEditorActionMap.put( copyValueAsLdifAction, new SearchResultEditorActionProxy( cursor,
            new CopyValueAction( CopyValueAction.Mode.LDIF, valueEditorManager ) ) );

        searchResultEditorActionMap.put( copySearchFilterAction, new SearchResultEditorActionProxy( cursor,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_EQUALS ) ) );
        searchResultEditorActionMap.put( copyNotSearchFilterAction, new SearchResultEditorActionProxy( cursor,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_NOT ) ) );
        searchResultEditorActionMap.put( copyAndSearchFilterAction, new SearchResultEditorActionProxy( cursor,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_AND ) ) );
        searchResultEditorActionMap.put( copyOrSearchFilterAction, new SearchResultEditorActionProxy( cursor,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_OR ) ) );

        searchResultEditorActionMap.put( propertyDialogAction, new SearchResultEditorActionProxy( cursor,
            new PropertiesAction() ) );
    }


    // ── Commander Recalls All Troopers ────────────────────────────────────────
    // Mission over — the commander calls every trooper back and releases them.
    // We null out every action reference and clear the map to avoid memory leaks.
    // This is called when the editor itself is closed.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all actions managed by this group.
     * We dispose every proxy in the map, null out standalone actions, and clear
     * all state.  After this call the group must not be used.
     */
    public void dispose()
    {
        if ( searchResultEditor != null )
        {
            showDecoratedValuesAction = null;
            showDNAction.dispose();
            showDNAction = null;
            showLinksAction.dispose();
            showLinksAction = null;
            openSearchResultEditorPreferencePage = null;
            showQuickFilterAction.dispose();
            showQuickFilterAction = null;

            openDefaultValueEditorActionProxy.dispose();
            openDefaultValueEditorActionProxy = null;
            openBestValueEditorActionProxy.dispose();
            openBestValueEditorActionProxy = null;
            for ( int i = 0; i < openValueEditorActionProxies.length; i++ )
            {
                openValueEditorActionProxies[i].dispose();
                openValueEditorActionProxies[i] = null;
            }
            openEntryValueEditorActionProxy.dispose();
            openEntryValueEditorActionProxy = null;
            openValueEditorPreferencesAction = null;

            for ( Iterator<String> it = this.searchResultEditorActionMap.keySet().iterator(); it.hasNext(); )
            {
                String key = it.next();
                SearchResultEditorActionProxy action = searchResultEditorActionMap.get( key );
                action.dispose();
                action = null;
                it.remove();
            }
            searchResultEditorActionMap.clear();
            searchResultEditorActionMap = null;

            actionBars = null;
            searchResultEditor = null;
        }
    }


    // ── Commander Stations Troopers on the Toolbar ───────────────────────────
    // The commander assigns specific troopers to guard the toolbar — new value,
    // delete, refresh, copy table, and quick filter each get a spot.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the editor's toolbar with the standard action buttons.
     * We add new-value, delete, refresh, copy-as-CSV, and show-quick-filter buttons,
     * separated by visual dividers.
     *
     * @param toolBarManager the toolbar manager to add actions to
     */
    public void fillToolBar( IToolBarManager toolBarManager )
    {
        toolBarManager.add( new Separator() );
        toolBarManager.add( searchResultEditorActionMap.get( newValueAction ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( searchResultEditorActionMap.get( deleteAction ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( searchResultEditorActionMap.get( refreshSearchAction ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( searchResultEditorActionMap.get( copyTableAction ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( showQuickFilterAction );
        toolBarManager.update( true );
    }


    // ── Commander Stations Troopers on the View Menu ──────────────────────────
    // The commander puts the view-level toggles in the dropdown menu: show/hide DN,
    // show links, decorated values, and a shortcut to preferences.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the editor's view menu (the drop-down from the toolbar chevron).
     * We add the DN toggle, links toggle, decorated-values toggle, and a link to
     * the preference page.  The decorated-values action updates its checked state
     * dynamically from the preference store each time the menu opens.
     *
     * @param menuManager the menu manager for the editor's view menu
     */
    public void fillMenu( IMenuManager menuManager )
    {
        menuManager.add( showDNAction );
        menuManager.add( showLinksAction );
        menuManager.add( showDecoratedValuesAction );
        menuManager.add( new Separator() );
        menuManager.add( openSearchResultEditorPreferencePage );
        menuManager.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( IMenuManager manager )
            {
                showDecoratedValuesAction.setChecked( !BrowserCommonActivator.getDefault().getPreferenceStore()
                    .getBoolean( BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES ) );
            }
        } );
        menuManager.update( true );
    }


    // ── Commander Registers the Action Bars Headquarters ─────────────────────
    // The commander needs to know where the main command channel (action bars) is
    // so they can broadcast global action handler updates to the workbench.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the action bars reference so we can register/unregister global handlers.
     * Call this once when the editor initializes, before any
     * {@link #activateGlobalActionHandlers()} calls.
     *
     * @param actionBars the workbench action bars for this editor's site
     */
    public void enableGlobalActionHandlers( IActionBars actionBars )
    {
        this.actionBars = actionBars;
    }


    // ── Commander Sets Up the Context Menu ────────────────────────────────────
    // The context menu is populated fresh each time it opens (via menuAboutToShow),
    // so we just register the listener here and let menuAboutToShow do the work.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the context menu to rebuild itself fresh each time it opens.
     * We set remove-all-when-shown and register ourselves as the menu listener
     * so {@link #menuAboutToShow(IMenuManager)} is called every time.
     *
     * @param menuManager the context menu manager for the table viewer
     */
    public void fillContextMenu( IMenuManager menuManager )
    {
        menuManager.setRemoveAllWhenShown( true );
        menuManager.addMenuListener( this );
    }


    // ── Commander Briefs the Troops Just Before the Door Opens ───────────────
    // Right before the context menu appears, the commander assembles the full squad:
    // navigation actions, copy/paste/delete, value editors, schema browser links.
    // Each trooper gets their position in the menu in the right order.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the context menu content immediately before it is shown.
     * We add actions in logical groups separated by dividers: new operations,
     * navigation, copy/paste/delete (with advanced sub-menu), value editors,
     * refresh, and properties.
     *
     * @param menuManager the context menu manager; we add actions directly to it
     */
    public void menuAboutToShow( IMenuManager menuManager )
    {
        // new
        menuManager.add( searchResultEditorActionMap.get( newValueAction ) );
        menuManager.add( searchResultEditorActionMap.get( newSearchAction ) );
        menuManager.add( searchResultEditorActionMap.get( newBatchOperationAction ) );
        menuManager.add( new Separator() );

        // navigation
        menuManager.add( searchResultEditorActionMap.get( locateDnInDitAction ) );
        menuManager.add( searchResultEditorActionMap.get( openSearchResultAction ) );
        MenuManager schemaMenuManager = new MenuManager( Messages
            .getString( "SearchResultEditorActionGroup.OpenSchemaBrowser" ) ); //$NON-NLS-1$
        schemaMenuManager.add( searchResultEditorActionMap.get( showOcdAction ) );
        schemaMenuManager.add( searchResultEditorActionMap.get( showAtdAction ) );
        schemaMenuManager.add( searchResultEditorActionMap.get( showEqualityMrdAction ) );
        schemaMenuManager.add( searchResultEditorActionMap.get( showSubstringMrdAction ) );
        schemaMenuManager.add( searchResultEditorActionMap.get( showOrderingMrdAction ) );
        schemaMenuManager.add( searchResultEditorActionMap.get( showLsdAction ) );
        menuManager.add( schemaMenuManager );
        MenuManager showInSubMenu = new MenuManager( Messages.getString( "SearchResultEditorActionGroup.ShowIn" ) ); //$NON-NLS-1$
        showInSubMenu.add( ContributionItemFactory.VIEWS_SHOW_IN.create( PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow() ) );
        menuManager.add( showInSubMenu );
        menuManager.add( new Separator() );

        // copy, paste, delete
        menuManager.add( searchResultEditorActionMap.get( copyAction ) );
        menuManager.add( searchResultEditorActionMap.get( pasteAction ) );
        menuManager.add( searchResultEditorActionMap.get( deleteAction ) );
        MenuManager advancedMenuManager = new MenuManager( Messages
            .getString( "SearchResultEditorActionGroup.Advanced" ) ); //$NON-NLS-1$
        advancedMenuManager.add( searchResultEditorActionMap.get( copyDnAction ) );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyUrlAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyAttriuteDescriptionAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyDisplayValueAction ) );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyValueUtf8Action ) );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyValueBase64Action ) );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyValueHexAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyValueAsLdifAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( searchResultEditorActionMap.get( copySearchFilterAction ) );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyNotSearchFilterAction ) );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyAndSearchFilterAction ) );
        advancedMenuManager.add( searchResultEditorActionMap.get( copyOrSearchFilterAction ) );
        menuManager.add( advancedMenuManager );
        menuManager.add( new Separator() );

        // edit
        menuManager.add( openDefaultValueEditorActionProxy );
        MenuManager editorMenuManager = new MenuManager( Messages.getString( "SearchResultEditorActionGroup.EditValue" ) ); //$NON-NLS-1$
        if ( openBestValueEditorActionProxy.isEnabled() )
        {
            editorMenuManager.add( openBestValueEditorActionProxy );
            editorMenuManager.add( new Separator() );
        }
        for ( int i = 0; i < openValueEditorActionProxies.length; i++ )
        {
            if ( openValueEditorActionProxies[i].isEnabled()
                && ( ( OpenEditorAction ) openValueEditorActionProxies[i].getAction() ).getValueEditor().getClass() != ( ( OpenBestEditorAction ) openBestValueEditorActionProxy
                    .getAction() ).getBestValueEditor().getClass() )
            {
                editorMenuManager.add( openValueEditorActionProxies[i] );
            }
        }
        editorMenuManager.add( new Separator() );
        editorMenuManager.add( openValueEditorPreferencesAction );
        menuManager.add( editorMenuManager );
        menuManager.add( openEntryValueEditorActionProxy );
        menuManager.add( new Separator() );

        // refresh
        menuManager.add( searchResultEditorActionMap.get( refreshSearchAction ) );
        menuManager.add( new Separator() );

        // additions
        menuManager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );

        // / properties
        menuManager.add( searchResultEditorActionMap.get( propertyDialogAction ) );
    }


    // ── Commander Opens the Command Channel ───────────────────────────────────
    // When the editor gains focus, the commander opens the main channel and registers
    // every trooper (action) with the workbench global action handler map.
    // This makes Ctrl+C, Ctrl+V, Delete, F5, etc. work in this editor.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Registers global action handlers so standard keybindings (Ctrl+C, Delete, F5,
     * etc.) trigger the right actions in this editor.
     * Called when the editor part is activated.  We also activate individual
     * action handlers via {@link ActionUtils} for actions with explicit command IDs.
     */
    public void activateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars
                .setGlobalActionHandler( ActionFactory.COPY.getId(), searchResultEditorActionMap.get( copyAction ) );
            actionBars.setGlobalActionHandler( ActionFactory.PASTE.getId(), searchResultEditorActionMap
                .get( pasteAction ) );
            actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(), searchResultEditorActionMap
                .get( deleteAction ) );
            actionBars.setGlobalActionHandler( ActionFactory.REFRESH.getId(), searchResultEditorActionMap
                .get( refreshSearchAction ) );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), searchResultEditorActionMap
                .get( propertyDialogAction ) );
            actionBars.setGlobalActionHandler( ActionFactory.FIND.getId(), showQuickFilterAction );
            actionBars.updateActionBars();
        }

        IAction nva = searchResultEditorActionMap.get( newValueAction );
        ActionUtils.activateActionHandler( nva );
        IAction lid = searchResultEditorActionMap.get( locateDnInDitAction );
        ActionUtils.activateActionHandler( lid );
        IAction osr = searchResultEditorActionMap.get( openSearchResultAction );
        ActionUtils.activateActionHandler( osr );
        ActionUtils.activateActionHandler( openDefaultValueEditorActionProxy );
        ActionUtils.activateActionHandler( openEntryValueEditorActionProxy );
    }


    // ── Commander Closes the Command Channel ──────────────────────────────────
    // When a cell editor opens, the global actions must be suspended so that
    // Ctrl+C, Delete, etc. go to the cell editor, not to this action group.
    // The commander recalls all troopers from the global handler map.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters all global action handlers so keybindings are not intercepted
     * by this editor while a cell editor is active.
     * Called when a cell editor opens (in {@link AbstractOpenEditorAction#activateEditor})
     * and when the editor part is deactivated.
     */
    public void deactivateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars.setGlobalActionHandler( ActionFactory.COPY.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.PASTE.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.REFRESH.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.FIND.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), null );
            actionBars.updateActionBars();
        }

        IAction nva = searchResultEditorActionMap.get( newValueAction );
        ActionUtils.deactivateActionHandler( nva );
        IAction lid = searchResultEditorActionMap.get( locateDnInDitAction );
        ActionUtils.deactivateActionHandler( lid );
        IAction osr = searchResultEditorActionMap.get( openSearchResultAction );
        ActionUtils.deactivateActionHandler( osr );
        ActionUtils.deactivateActionHandler( openDefaultValueEditorActionProxy );
        ActionUtils.deactivateActionHandler( openEntryValueEditorActionProxy );
    }


    // ── Commander Identifies the Lead Trooper ────────────────────────────────
    // The lead trooper for the editing mission is the best-editor action — callers
    // need direct access to it to check which editor was selected.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link OpenBestEditorAction} from its proxy wrapper.
     * The universal listener uses this to wire up the "start edit on double-click"
     * behavior by calling the action directly.
     *
     * @return the unwrapped {@link OpenBestEditorAction} instance
     */
    public OpenBestEditorAction getOpenBestEditorAction()
    {
        return ( OpenBestEditorAction ) openBestValueEditorActionProxy.getAction();
    }


    // ── Commander Updates All Troopers' Target ────────────────────────────────
    // When the user selects a different search in the browser, the commander
    // updates every trooper's context so they know what they're operating on.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all actions that the editor's input has changed to a new search.
     * Each proxy's {@code inputChanged()} updates the underlying action's context
     * so that enablement checks and operations target the new search.
     *
     * @param search the newly selected {@link ISearch}, or {@code null} if nothing is selected
     */
    public void setInput( ISearch search )
    {
        for ( SearchResultEditorActionProxy action : searchResultEditorActionMap.values() )
        {
            action.inputChanged( search );
        }
    }


    // ── Commander Checks if Any Trooper Is Mid-Mission ────────────────────────
    // If any cell editor is currently open, the editor is "active" and we should
    // suppress other editing actions until the current one finishes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if any value editor is currently open and active.
     * We check the default, best, entry, and all alternative editor proxies —
     * if any of them report active, we return true.
     * The editor uses this to decide whether to allow new editing operations.
     *
     * @return {@code true} if at least one cell editor is currently open
     */
    public boolean isEditorActive()
    {
        if ( ( ( AbstractOpenEditorAction ) openDefaultValueEditorActionProxy.getAction() ).isActive() )
        {
            return true;
        }
        if ( ( ( AbstractOpenEditorAction ) openBestValueEditorActionProxy.getAction() ).isActive() )
        {
            return true;
        }
        if ( ( ( AbstractOpenEditorAction ) openEntryValueEditorActionProxy.getAction() ).isActive() )
        {
            return true;
        }
        for ( int i = 0; i < openValueEditorActionProxies.length; i++ )
        {
            if ( ( ( AbstractOpenEditorAction ) openValueEditorActionProxies[i].getAction() ).isActive() )
            {
                return true;
            }
        }

        return false;
    }

}
