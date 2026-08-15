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

package org.apache.directory.studio.combinededitor.editor;


import org.apache.directory.studio.connection.ui.actions.CollapseAllAction;
import org.apache.directory.studio.connection.ui.actions.ExpandAllAction;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.DeleteAllValuesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.FetchOperationalAttributesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.NewAttributeAction;
import org.apache.directory.studio.ldapbrowser.common.actions.RefreshAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.EntryEditorActionProxy;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EditAttributeDescriptionAction;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidget;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetActionGroup;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.EntryEditorWidgetConfiguration;
import org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor.OpenDefaultEditorAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyAttributeDescriptionAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyDnAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopySearchFilterAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyUrlAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.CopyValueAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.LocateDnInDitAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.NewBatchOperationAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.NewSearchAction;
import org.apache.directory.studio.ldapbrowser.ui.actions.OpenSchemaBrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.editors.entry.OpenEntryEditorAction;
import org.apache.directory.studio.ldapbrowser.ui.editors.entry.OpenEntryEditorPreferencePageAction;
import org.apache.directory.studio.utils.ActionUtils;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;

import org.apache.directory.studio.templateeditor.actions.EditorPagePropertiesAction;


// ── CLASS: TableEditorPageActionGroup — The Tantive IV Tactical Officer's Toolkit ─
// Every crew station on the Tantive IV bridge has a dedicated toolkit: the
// weapons officer knows which button fires the turbolasers, which copies a
// target's coordinates, and which opens a schema data-card on the target.
// Antilles briefs each officer before the mission and assigns their commands.
// TableEditorPageActionGroup is that briefing: it instantiates every action the
// Table Editor tab needs (new attribute, delete, copy DN, open schema browser,
// refresh, etc.) and distributes them into the toolbar, dropdown menu, and
// right-click context menu so the user has the right tool always within reach.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Manages all actions available in the Table Editor page of the combined entry editor.
 * We extend {@link EntryEditorWidgetActionGroup} and add the extra actions specific
 * to the combined editor context: copy DN/URL, schema browser submenu, search filter
 * copy, delete all values, fetch operational attributes, and entry properties.
 * Think of this as the Tantive IV weapons/tactical officer's toolkit — every button,
 * menu item, and keyboard shortcut the Table tab can offer, all assembled here.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TableEditorPageActionGroup extends EntryEditorWidgetActionGroup
{
    /** The open entry value editor action. */
    private EntryEditorActionProxy openEntryValueEditorActionProxy;

    /** The open entry editor preference page. */
    private OpenEntryEditorPreferencePageAction openEntryEditorPreferencePage;

    /** The collapse all action. */
    private CollapseAllAction collapseAllAction;

    /** The expand all action. */
    private ExpandAllAction expandAllAction;

    /** The Constant editAttributeDescriptionAction. */
    private static final String editAttributeDescriptionAction = "editAttributeDescriptionAction"; //$NON-NLS-1$

    /** The Constant refreshAttributesAction. */
    private static final String refreshAttributesAction = "refreshAttributesAction"; //$NON-NLS-1$

    /** The Constant newAttributeAction. */
    private static final String newAttributeAction = "newAttributeAction"; //$NON-NLS-1$

    /** The Constant newSearchAction. */
    private static final String newSearchAction = "newSearchDialogAction"; //$NON-NLS-1$

    /** The Constant newBatchOperationAction. */
    private static final String newBatchOperationAction = "newBatchOperationAction"; //$NON-NLS-1$

    /** The Constant copyDnAction. */
    private static final String copyDnAction = "copyDnAction"; //$NON-NLS-1$

    /** The Constant copyUrlAction. */
    private static final String copyUrlAction = "copyUrlAction"; //$NON-NLS-1$

    /** The Constant copyAttriuteDescriptionAction. */
    private static final String copyAttriuteDescriptionAction = "copyAttriuteDescriptionAction"; //$NON-NLS-1$

    /** The Constant copyDisplayValueAction. */
    private static final String copyDisplayValueAction = "copyDisplayValueAction"; //$NON-NLS-1$

    /** The Constant copyValueUtf8Action. */
    private static final String copyValueUtf8Action = "copyValueUtf8Action"; //$NON-NLS-1$

    /** The Constant copyValueBase64Action. */
    private static final String copyValueBase64Action = "copyValueBase64Action"; //$NON-NLS-1$

    /** The Constant copyValueHexAction. */
    private static final String copyValueHexAction = "copyValueHexAction"; //$NON-NLS-1$

    /** The Constant copyValueAsLdifAction. */
    private static final String copyValueAsLdifAction = "copyValueAsLdifAction"; //$NON-NLS-1$

    /** The Constant copySearchFilterAction. */
    private static final String copySearchFilterAction = "copySearchFilterAction"; //$NON-NLS-1$

    /** The Constant copyNotSearchFilterAction. */
    private static final String copyNotSearchFilterAction = "copyNotSearchFilterAction"; //$NON-NLS-1$

    /** The Constant copyAndSearchFilterAction. */
    private static final String copyAndSearchFilterAction = "copyAndSearchFilterAction"; //$NON-NLS-1$

    /** The Constant copyOrSearchFilterAction. */
    private static final String copyOrSearchFilterAction = "copyOrSearchFilterAction"; //$NON-NLS-1$

    /** The Constant deleteAllValuesAction. */
    private static final String deleteAllValuesAction = "deleteAllValuesAction"; //$NON-NLS-1$

    /** The Constant locateDnInDitAction. */
    private static final String locateDnInDitAction = "locateDnInDitAction"; //$NON-NLS-1$

    /** The Constant showOcdAction. */
    private static final String showOcdAction = "showOcdAction"; //$NON-NLS-1$

    /** The Constant showAtdAction. */
    private static final String showAtdAction = "showAtdAction"; //$NON-NLS-1$

    /** The Constant showEqualityMrdAction. */
    private static final String showEqualityMrdAction = "showEqualityMrdAction"; //$NON-NLS-1$

    /** The Constant showSubstringMrdAction. */
    private static final String showSubstringMrdAction = "showSubstringMrdAction"; //$NON-NLS-1$

    /** The Constant showOrderingMrdAction. */
    private static final String showOrderingMrdAction = "showOrderingMrdAction"; //$NON-NLS-1$

    /** The Constant showLsdAction. */
    private static final String showLsdAction = "showLsdAction"; //$NON-NLS-1$

    /** The Constant fetchOperationalAttributesAction. */
    private static final String fetchOperationalAttributesAction = "fetchOperationalAttributesAction"; //$NON-NLS-1$


    // ── Antilles Briefs the Tactical Officer Before the Mission ───────────────
    // Antilles walks the tactical officer through every action in their toolkit:
    // what button fires the turbolasers (new attribute), what copies the target DN,
    // what opens the schema browser, and where the refresh key is.  By the time
    // the constructor finishes every action is registered and ready.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates all actions for the Table Editor page and stores them in the action map.
     * We delegate to the parent class for the common set and then add the extra
     * actions this page needs: copy DN/URL, schema browser submenus, search filter
     * copying, delete-all-values, and fetch-operational-attributes.
     *
     * @param entryEditor   the entry editor that owns this page — used by the
     *                      properties action.
     * @param mainWidget    the entry editor widget whose viewer the actions operate on.
     * @param configuration the widget configuration — provides the value editor manager.
     */
    public TableEditorPageActionGroup( IEntryEditor entryEditor, EntryEditorWidget mainWidget,
        EntryEditorWidgetConfiguration configuration )
    {
        super( mainWidget, configuration );

        TreeViewer viewer = mainWidget.getViewer();
        ValueEditorManager valueEditorManager = configuration.getValueEditorManager( viewer );

        // create OpenDefaultEditorAction with enabled rename action flag
        openDefaultValueEditorActionProxy.dispose();
        openDefaultValueEditorActionProxy = new EntryEditorActionProxy( viewer, new OpenDefaultEditorAction( viewer,
            openBestValueEditorActionProxy ) );

        openEntryValueEditorActionProxy = new EntryEditorActionProxy( viewer, new OpenEntryEditorAction( viewer,
            valueEditorManager, valueEditorManager.getEntryValueEditor(), this ) );

        openEntryEditorPreferencePage = new OpenEntryEditorPreferencePageAction();
        collapseAllAction = new CollapseAllAction( viewer );
        expandAllAction = new ExpandAllAction( viewer );

        entryEditorActionMap.put( editAttributeDescriptionAction, new EntryEditorActionProxy( viewer,
            new EditAttributeDescriptionAction( viewer ) ) );

        entryEditorActionMap.put( refreshAttributesAction, new EntryEditorActionProxy( viewer, new RefreshAction() ) );

        entryEditorActionMap.put( newAttributeAction, new EntryEditorActionProxy( viewer, new NewAttributeAction() ) );
        entryEditorActionMap.put( newSearchAction, new EntryEditorActionProxy( viewer, new NewSearchAction() ) );
        entryEditorActionMap.put( newBatchOperationAction, new EntryEditorActionProxy( viewer,
            new NewBatchOperationAction() ) );

        entryEditorActionMap.put( locateDnInDitAction, new EntryEditorActionProxy( viewer, new LocateDnInDitAction() ) );
        entryEditorActionMap.put( showOcdAction, new EntryEditorActionProxy( viewer, new OpenSchemaBrowserAction(
            OpenSchemaBrowserAction.MODE_OBJECTCLASS ) ) );
        entryEditorActionMap.put( showAtdAction, new EntryEditorActionProxy( viewer, new OpenSchemaBrowserAction(
            OpenSchemaBrowserAction.MODE_ATTRIBUTETYPE ) ) );
        entryEditorActionMap.put( showEqualityMrdAction, new EntryEditorActionProxy( viewer,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_EQUALITYMATCHINGRULE ) ) );
        entryEditorActionMap.put( showSubstringMrdAction, new EntryEditorActionProxy( viewer,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_SUBSTRINGMATCHINGRULE ) ) );
        entryEditorActionMap.put( showOrderingMrdAction, new EntryEditorActionProxy( viewer,
            new OpenSchemaBrowserAction( OpenSchemaBrowserAction.MODE_ORDERINGMATCHINGRULE ) ) );
        entryEditorActionMap.put( showLsdAction, new EntryEditorActionProxy( viewer, new OpenSchemaBrowserAction(
            OpenSchemaBrowserAction.MODE_SYNTAX ) ) );

        entryEditorActionMap.put( copyDnAction, new EntryEditorActionProxy( viewer, new CopyDnAction() ) );
        entryEditorActionMap.put( copyUrlAction, new EntryEditorActionProxy( viewer, new CopyUrlAction() ) );
        entryEditorActionMap.put( copyAttriuteDescriptionAction, new EntryEditorActionProxy( viewer,
            new CopyAttributeDescriptionAction() ) );
        entryEditorActionMap.put( copyDisplayValueAction, new EntryEditorActionProxy( viewer, new CopyValueAction(
            CopyValueAction.Mode.DISPLAY, valueEditorManager ) ) );
        entryEditorActionMap.put( copyValueUtf8Action, new EntryEditorActionProxy( viewer, new CopyValueAction(
            CopyValueAction.Mode.UTF8, valueEditorManager ) ) );
        entryEditorActionMap.put( copyValueBase64Action, new EntryEditorActionProxy( viewer, new CopyValueAction(
            CopyValueAction.Mode.BASE64, valueEditorManager ) ) );
        entryEditorActionMap.put( copyValueHexAction, new EntryEditorActionProxy( viewer, new CopyValueAction(
            CopyValueAction.Mode.HEX, valueEditorManager ) ) );
        entryEditorActionMap.put( copyValueAsLdifAction, new EntryEditorActionProxy( viewer, new CopyValueAction(
            CopyValueAction.Mode.LDIF, valueEditorManager ) ) );

        entryEditorActionMap.put( copySearchFilterAction, new EntryEditorActionProxy( viewer,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_EQUALS ) ) );
        entryEditorActionMap.put( copyNotSearchFilterAction, new EntryEditorActionProxy( viewer,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_NOT ) ) );
        entryEditorActionMap.put( copyAndSearchFilterAction, new EntryEditorActionProxy( viewer,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_AND ) ) );
        entryEditorActionMap.put( copyOrSearchFilterAction, new EntryEditorActionProxy( viewer,
            new CopySearchFilterAction( CopySearchFilterAction.MODE_OR ) ) );

        entryEditorActionMap.put( deleteAllValuesAction, new EntryEditorActionProxy( viewer,
            new DeleteAllValuesAction() ) );
        entryEditorActionMap.put( fetchOperationalAttributesAction, new EntryEditorActionProxy( viewer,
            new FetchOperationalAttributesAction() ) );

        entryEditorActionMap.put( PROPERTY_DIALOG_ACTION, new EntryEditorActionProxy( viewer,
            new EditorPagePropertiesAction( entryEditor ) ) );
    }


    // ── The Mission is Over — Decommission the Toolkit ───────────────────────
    // When the Tantive IV docks and the mission ends Antilles decommissions
    // the tactical toolkit: all action proxies are disposed, the expand/collapse
    // actions are shut down, and the global action handlers are deactivated so
    // they don't interfere with other editors.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all actions managed by this group.
     * We deactivate global action handlers first to remove our keyboard bindings,
     * then dispose the extra proxies and actions we created before delegating
     * to the parent class.
     */
    public void dispose()
    {
        if ( expandAllAction != null )
        {
            deactivateGlobalActionHandlers();

            openEntryValueEditorActionProxy.dispose();
            openEntryValueEditorActionProxy = null;
            openEntryEditorPreferencePage = null;
            expandAllAction.dispose();
            expandAllAction = null;
            collapseAllAction.dispose();
            collapseAllAction = null;
        }

        super.dispose();
    }


    // ── Load the Toolbar with the Most-Used Controls ──────────────────────────
    // Antilles pins the most-used buttons to the tactical console's front panel:
    // new value, new attribute, delete, delete-all, refresh, expand, collapse,
    // and the quick filter toggle.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Table Editor tab's toolbar.
     * We add the frequently used actions as toolbar buttons with separators
     * between logical groups (create / delete / refresh / expand-collapse / filter).
     *
     * @param toolBarManager  the toolbar manager for the entry editor widget.
     */
    public void fillToolBar( IToolBarManager toolBarManager )
    {
        toolBarManager.add( new Separator() );
        toolBarManager.add( entryEditorActionMap.get( NEW_VALUE_ACTION ) );
        toolBarManager.add( entryEditorActionMap.get( newAttributeAction ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( entryEditorActionMap.get( DELETE_ACTION ) );
        toolBarManager.add( entryEditorActionMap.get( deleteAllValuesAction ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( entryEditorActionMap.get( refreshAttributesAction ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( expandAllAction );
        toolBarManager.add( collapseAllAction );
        toolBarManager.add( new Separator() );
        toolBarManager.add( showQuickFilterAction );
        toolBarManager.update( true );
    }


    // ── Dropdown Menu — Configure the Tactical Display ────────────────────────
    // The tactical officer's settings menu lets her configure the display mode:
    // sort order, decorated vs. raw values, and a shortcut to the preferences page.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the Table Editor tab's dropdown menu.
     * We add sort, decorated-values toggle, and a shortcut to the entry editor
     * preference page.  The decorated-values checkbox is refreshed just before
     * the menu opens so it always reflects the current preference.
     *
     * @param menuManager  the dropdown menu manager for the entry editor widget.
     */
    public void fillMenu( IMenuManager menuManager )
    {
        menuManager.add( openSortDialogAction );
        menuManager.add( new Separator() );
        menuManager.add( showDecoratedValuesAction );
        menuManager.add( new Separator() );
        menuManager.add( openEntryEditorPreferencePage );
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


    // ── Right-Click Context Menu — Full Tactical Options ──────────────────────
    // When the officer right-clicks a row the full tactical options appear:
    // create, navigate (locate in DIT, open schema browser, show in view),
    // copy/paste/delete, advanced copy options, edit inline, refresh, and
    // entry properties at the bottom — the complete toolkit for any situation.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the right-click context menu for the table viewer.
     * We lay out the items in standard Eclipse convention: create actions at top,
     * navigation submenu, clipboard actions in the middle, edit actions, refresh
     * at the bottom, and properties last.
     *
     * @param menuManager  the context menu manager — populated just before display.
     */
    protected void contextMenuAboutToShow( IMenuManager menuManager )
    {
        // new
        menuManager.add( entryEditorActionMap.get( newAttributeAction ) );
        menuManager.add( entryEditorActionMap.get( NEW_VALUE_ACTION ) );
        menuManager.add( entryEditorActionMap.get( newSearchAction ) );
        menuManager.add( entryEditorActionMap.get( newBatchOperationAction ) );
        menuManager.add( new Separator() );

        // navigation
        menuManager.add( entryEditorActionMap.get( locateDnInDitAction ) );
        MenuManager schemaMenuManager = new MenuManager( Messages
            .getString( "TableEditorPageActionGroup.OpenSchemaBrowser" ) ); //$NON-NLS-1$
        schemaMenuManager.add( entryEditorActionMap.get( showOcdAction ) );
        schemaMenuManager.add( entryEditorActionMap.get( showAtdAction ) );
        schemaMenuManager.add( entryEditorActionMap.get( showEqualityMrdAction ) );
        schemaMenuManager.add( entryEditorActionMap.get( showSubstringMrdAction ) );
        schemaMenuManager.add( entryEditorActionMap.get( showOrderingMrdAction ) );
        schemaMenuManager.add( entryEditorActionMap.get( showLsdAction ) );
        menuManager.add( schemaMenuManager );
        MenuManager showInSubMenu = new MenuManager( Messages.getString( "TableEditorPageActionGroup.ShowIn" ) ); //$NON-NLS-1$
        showInSubMenu.add( ContributionItemFactory.VIEWS_SHOW_IN.create( PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow() ) );
        menuManager.add( showInSubMenu );

        menuManager.add( new Separator() );

        // copy, paste, delete
        menuManager.add( entryEditorActionMap.get( COPY_ACTION ) );
        menuManager.add( entryEditorActionMap.get( PASTE_ACTION ) );
        menuManager.add( entryEditorActionMap.get( DELETE_ACTION ) );
        menuManager.add( entryEditorActionMap.get( SELECT_ALL_ACTION ) );
        MenuManager advancedMenuManager = new MenuManager( Messages.getString( "TableEditorPageActionGroup.Advanced" ) ); //$NON-NLS-1$
        advancedMenuManager.add( entryEditorActionMap.get( copyDnAction ) );
        advancedMenuManager.add( entryEditorActionMap.get( copyUrlAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( entryEditorActionMap.get( copyAttriuteDescriptionAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( entryEditorActionMap.get( copyDisplayValueAction ) );
        advancedMenuManager.add( entryEditorActionMap.get( copyValueUtf8Action ) );
        advancedMenuManager.add( entryEditorActionMap.get( copyValueBase64Action ) );
        advancedMenuManager.add( entryEditorActionMap.get( copyValueHexAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( entryEditorActionMap.get( copyValueAsLdifAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( entryEditorActionMap.get( copySearchFilterAction ) );
        advancedMenuManager.add( entryEditorActionMap.get( copyNotSearchFilterAction ) );
        advancedMenuManager.add( entryEditorActionMap.get( copyAndSearchFilterAction ) );
        advancedMenuManager.add( entryEditorActionMap.get( copyOrSearchFilterAction ) );
        advancedMenuManager.add( new Separator() );
        advancedMenuManager.add( entryEditorActionMap.get( deleteAllValuesAction ) );
        menuManager.add( advancedMenuManager );
        menuManager.add( new Separator() );

        // edit
        menuManager.add( entryEditorActionMap.get( editAttributeDescriptionAction ) );
        super.addEditMenu( menuManager );
        menuManager.add( openEntryValueEditorActionProxy );
        menuManager.add( new Separator() );

        // refresh
        menuManager.add( entryEditorActionMap.get( refreshAttributesAction ) );
        if ( entryEditorActionMap.get( fetchOperationalAttributesAction ).isEnabled() )
        {
            menuManager.add( entryEditorActionMap.get( fetchOperationalAttributesAction ) );
        }
        menuManager.add( new Separator() );

        // additions
        menuManager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );

        // properties
        menuManager.add( entryEditorActionMap.get( PROPERTY_DIALOG_ACTION ) );
    }


    // ── The Officer Takes the Helm — Register Global Action Handlers ──────────
    // When the tactical console gains focus it registers its keyboard shortcuts
    // (Refresh, New Attribute, Locate DN, Edit Attribute) as global handlers so
    // the user's keystrokes are routed here rather than to the previous console.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Activates global action handlers (keyboard shortcuts) for this page.
     * We register the Refresh key and the extra actions unique to this page
     * (New Attribute, Locate DN, Edit Attribute Description, Open Entry Value
     * Editor) so they work from the keyboard when this tab is active.
     */
    public void activateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars.setGlobalActionHandler( ActionFactory.REFRESH.getId(), entryEditorActionMap
                .get( refreshAttributesAction ) );
        }

        super.activateGlobalActionHandlers();

        IAction naa = entryEditorActionMap.get( newAttributeAction );
        ActionUtils.activateActionHandler( naa );
        IAction lid = entryEditorActionMap.get( locateDnInDitAction );
        ActionUtils.activateActionHandler( lid );
        IAction eada = entryEditorActionMap.get( editAttributeDescriptionAction );
        ActionUtils.activateActionHandler( eada );
        ActionUtils.activateActionHandler( openEntryValueEditorActionProxy );
    }


    // ── Console Loses Focus — Deregister Global Action Handlers ──────────────
    // When the user switches to a different tab the tactical console politely
    // removes its keyboard shortcuts from the global handler list so the next
    // console can register its own without conflict.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deactivates global action handlers when this tab loses focus.
     * We remove the Refresh key registration and deactivate the extra action
     * handlers registered in {@link #activateGlobalActionHandlers()}.
     */
    public void deactivateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars.setGlobalActionHandler( ActionFactory.REFRESH.getId(), null );
        }

        super.deactivateGlobalActionHandlers();

        IAction naa = entryEditorActionMap.get( newAttributeAction );
        ActionUtils.deactivateActionHandler( naa );
        IAction lid = entryEditorActionMap.get( locateDnInDitAction );
        ActionUtils.deactivateActionHandler( lid );
        IAction eada = entryEditorActionMap.get( editAttributeDescriptionAction );
        ActionUtils.deactivateActionHandler( eada );
        ActionUtils.deactivateActionHandler( openEntryValueEditorActionProxy );
    }

}
