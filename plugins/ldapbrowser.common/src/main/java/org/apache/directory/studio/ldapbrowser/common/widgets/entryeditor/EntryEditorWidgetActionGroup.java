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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.CopyAction;
import org.apache.directory.studio.ldapbrowser.common.actions.DeleteAction;
import org.apache.directory.studio.ldapbrowser.common.actions.NewValueAction;
import org.apache.directory.studio.ldapbrowser.common.actions.PropertiesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.SelectAllAction;
import org.apache.directory.studio.ldapbrowser.common.actions.ShowDecoratedValuesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.ValueEditorPreferencesAction;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.ActionHandlerManager;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.BrowserActionProxy;
import org.apache.directory.studio.ldapbrowser.common.actions.proxy.EntryEditorActionProxy;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.utils.ActionUtils;
import org.apache.directory.studio.valueeditors.IValueEditor;
import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;


// ── CLASS: EntryEditorWidgetActionGroup — The Rebel Briefing Room At Yavin 4 ─────────────
// General Dodonna stands at the holographic display assigning every pilot to a specific role
// before the assault on the Death Star: Red Leader, Gold Leader, who handles the trench run,
// who flies cover, who handles copy/paste/delete.  This class is that briefing room.
// It instantiates every action the entry editor needs, registers them under named keys,
// fills the toolbar and context menu, and manages the handoff of global keyboard shortcuts
// (like Ctrl+C) to and from Eclipse's action bar system.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * Central action coordinator for the entry editor widget.
 * Creates, owns, and wires together all actions: new value, copy, paste, delete, select-all,
 * properties, sort dialog, quick filter, show-decorated-values, and the full suite of
 * value-editor open actions.  Also manages activation and deactivation of Eclipse global
 * action handlers (Ctrl+C, Ctrl+V, Delete, etc.) so they route correctly to this editor
 * when it has focus.
 * Think of this class as Dodonna's briefing room — every pilot (action) gets their role,
 * their callsign (key constant), and their slot in the toolbar or context menu.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetActionGroup implements ActionHandlerManager
{

    /** The open sort dialog action. */
    protected OpenSortDialogAction openSortDialogAction;

    /** The show decorated values action. */
    protected ShowDecoratedValuesAction showDecoratedValuesAction;

    /** The show quick filter action. */
    protected ShowQuickFilterAction showQuickFilterAction;

    /** The open default editor action. */
    protected EntryEditorActionProxy openDefaultValueEditorActionProxy;

    /** The open best editor action. */
    protected EntryEditorActionProxy openBestValueEditorActionProxy;

    /** The open editor actions. */
    protected EntryEditorActionProxy[] openValueEditorActionProxies;

    /** The open value editor preferences action. */
    protected ValueEditorPreferencesAction openValueEditorPreferencesAction;

    /** The Constant newValueAction. */
    protected final static String NEW_VALUE_ACTION = "newValueAction"; //$NON-NLS-1$

    /** The Constant copyAction. */
    protected final static String COPY_ACTION = "copyAction"; //$NON-NLS-1$

    /** The Constant pasteAction. */
    protected final static String PASTE_ACTION = "pasteAction"; //$NON-NLS-1$

    /** The Constant deleteAction. */
    protected final static String DELETE_ACTION = "deleteAction"; //$NON-NLS-1$

    /** The Constant selectAllAction. */
    protected final static String SELECT_ALL_ACTION = "selectAllAction"; //$NON-NLS-1$

    /** The Constant propertyDialogAction. */
    protected final static String PROPERTY_DIALOG_ACTION = "propertyDialogAction"; //$NON-NLS-1$

    /** The entry editor action map. */
    protected Map<String, EntryEditorActionProxy> entryEditorActionMap;

    /** The action bars. */
    protected IActionBars actionBars;

    /** The main widget. */
    private EntryEditorWidget mainWidget;


    // ── Dodonna Assigns Every Pilot To Their Role ─────────────────────────────────────────
    // The constructor is the full mission briefing: every action gets created, given its
    // callsign (key constant), and stored in the action map.  Value editor actions are created
    // dynamically — one per registered value editor plugin — so the roster adapts to however
    // many editors are installed.  After this constructor returns, every pilot is in their seat.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates all actions and wires them to the given widget and configuration.
     * Value-editor actions are created dynamically based on the installed set of
     * {@link IValueEditor} plugins discovered by the {@link ValueEditorManager}.
     * The action map stores named proxies; the keys are the {@code *_ACTION} constants
     * on this class.
     *
     * <p>For example — Dodonna assigns the pilots:</p>
     * <pre>
     *   entryEditorActionMap.put( NEW_VALUE_ACTION, ... );   // Wedge — create
     *   entryEditorActionMap.put( COPY_ACTION, ... );        // Biggs  — copy
     *   entryEditorActionMap.put( PASTE_ACTION, ... );       // Porkins — paste
     *   entryEditorActionMap.put( DELETE_ACTION, ... );      // Red Leader — delete
     *   // ... and one OpenEditorAction per installed value-editor plugin
     * </pre>
     *
     * @param mainWidget     the entry editor widget that owns this action group
     * @param configuration  provides the viewer, value editor manager, and preferences
     */
    public EntryEditorWidgetActionGroup( EntryEditorWidget mainWidget, EntryEditorWidgetConfiguration configuration )
    {
        this.mainWidget = mainWidget;

        entryEditorActionMap = new HashMap<String, EntryEditorActionProxy>();
        TreeViewer viewer = mainWidget.getViewer();
        ValueEditorManager valueEditorManager = configuration.getValueEditorManager( viewer );

        openSortDialogAction = new OpenSortDialogAction( configuration.getPreferences() );
        showDecoratedValuesAction = new ShowDecoratedValuesAction();
        showQuickFilterAction = new ShowQuickFilterAction( mainWidget.getQuickFilterWidget() );

        openBestValueEditorActionProxy = new EntryEditorActionProxy( viewer, new OpenBestEditorAction( viewer,
            valueEditorManager, this ) );
        openDefaultValueEditorActionProxy = new EntryEditorActionProxy( viewer, new OpenDefaultEditorAction( viewer,
            openBestValueEditorActionProxy ) );
        IValueEditor[] valueEditors = valueEditorManager.getAllValueEditors();
        openValueEditorActionProxies = new EntryEditorActionProxy[valueEditors.length];

        for ( int i = 0; i < openValueEditorActionProxies.length; i++ )
        {
            openValueEditorActionProxies[i] = new EntryEditorActionProxy( viewer, new OpenEditorAction( viewer,
                valueEditorManager, valueEditors[i], this ) );
        }

        openValueEditorPreferencesAction = new ValueEditorPreferencesAction();

        entryEditorActionMap.put( NEW_VALUE_ACTION, new EntryEditorActionProxy( viewer, new NewValueAction() ) );

        entryEditorActionMap.put( PASTE_ACTION, new EntryEditorActionProxy( viewer, new EntryEditorPasteAction() ) );
        entryEditorActionMap.put( COPY_ACTION, new EntryEditorActionProxy( viewer, new CopyAction(
            ( BrowserActionProxy ) entryEditorActionMap.get( PASTE_ACTION ), valueEditorManager ) ) );
        entryEditorActionMap.put( DELETE_ACTION, new EntryEditorActionProxy( viewer, new DeleteAction() ) );
        entryEditorActionMap.put( SELECT_ALL_ACTION, new EntryEditorActionProxy( viewer, new SelectAllAction( viewer ) ) );

        entryEditorActionMap.put( PROPERTY_DIALOG_ACTION, new EntryEditorActionProxy( viewer, new PropertiesAction() ) );

        //viewer.addSelectionChangedListener( entryEditorListener );
    }


    // ── Dodonna Stands Down The Briefing Room ─────────────────────────────────────────────
    // After the mission, the briefing room is cleared out: each action proxy is disposed
    // (releasing its listeners), the action map is emptied, and all references are set to
    // null so the garbage collector can clean up.  Dodonna turns off the holographic display.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all actions and releases all held references.
     * Call this when the entry editor widget is being disposed — every action proxy gets
     * disposed to detach its selection-change listeners, and the action map is cleared.
     * Failing to call this would leave listeners dangling on the viewer.
     */
    public void dispose()
    {
        if ( mainWidget != null )
        {
            openSortDialogAction = null;
            showQuickFilterAction.dispose();
            showQuickFilterAction = null;
            showDecoratedValuesAction = null;

            openDefaultValueEditorActionProxy.dispose();
            openDefaultValueEditorActionProxy = null;
            openBestValueEditorActionProxy.dispose();
            openBestValueEditorActionProxy = null;
            for ( EntryEditorActionProxy action : openValueEditorActionProxies )
            {
                action.dispose();
            }
            openValueEditorPreferencesAction = null;

            for ( EntryEditorActionProxy action : entryEditorActionMap.values() )
            {
                action.dispose();
            }
            entryEditorActionMap.clear();
            entryEditorActionMap = null;

            actionBars = null;
            mainWidget = null;
        }
    }


    // ── Dodonna Connects The Briefing Room To Wing Command ────────────────────────────────
    // The action bars represent Eclipse's global keyboard shortcut system — Ctrl+C, Ctrl+V,
    // Delete, etc.  Calling this method tells the action group "here are the action bars;
    // use them when you need to register global handlers."  The actual registration happens
    // in activateGlobalActionHandlers().
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Stores the Eclipse {@link IActionBars} reference so this group can register and
     * unregister global action handlers (Ctrl+C, Ctrl+V, etc.) when the entry editor
     * gains or loses focus.  Must be called before {@link #activateGlobalActionHandlers()}.
     *
     * @param actionBars  the Eclipse action bars provided by the containing workbench part
     */
    public void enableGlobalActionHandlers( IActionBars actionBars )
    {
        this.actionBars = actionBars;
        //        activateGlobalActionHandlers();
    }


    // ── Dodonna Posts The Duty Roster To The Toolbar ──────────────────────────────────────
    // The toolbar gets the most frequently used actions: new value, a separator, delete,
    // another separator, and the quick-filter toggle.  Dodonna posts the key pilots' names
    // on the mission board in order of priority.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds this group's toolbar actions to the given {@link IToolBarManager}.
     * The toolbar contains: New Value, separator, Delete, separator, Show Quick Filter.
     * Call this from the containing Eclipse view or editor's {@code createPartControl}.
     *
     * <p>For example — Dodonna posts the duty roster:</p>
     * <pre>
     *   [New Value] | [Delete] | [Quick Filter]
     * </pre>
     *
     * @param toolBarManager  the Eclipse toolbar to populate
     */
    public void fillToolBar( IToolBarManager toolBarManager )
    {
        toolBarManager.add( entryEditorActionMap.get( NEW_VALUE_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( entryEditorActionMap.get( DELETE_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( showQuickFilterAction );
        toolBarManager.update( true );
    }


    // ── Dodonna Posts Static Briefing Options To The View Menu ───────────────────────────
    // The view pulldown menu (not the right-click context menu) gets two persistent items:
    // the sort dialog action and the show-decorated-values toggle.  The decorated-values
    // toggle refreshes its checked state each time the menu opens, reflecting the current
    // preference store value.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds persistent actions to the view's pulldown menu (not the right-click context menu).
     * Currently adds "Open Sort Dialog" and "Show Decorated Values" (a toggle that reflects
     * the current preference store setting whenever the menu is opened).
     *
     * @param menuManager  the Eclipse menu manager representing the view's pulldown menu
     */
    public void fillMenu( IMenuManager menuManager )
    {
        menuManager.add( openSortDialogAction );
        menuManager.add( showDecoratedValuesAction );
        menuManager.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( IMenuManager manager )
            {
                showDecoratedValuesAction.setChecked( !BrowserCommonActivator.getDefault().getPreferenceStore()
                    .getBoolean( BrowserCommonConstants.PREFERENCE_SHOW_RAW_VALUES ) );
            }
        } );
    }


    // ── Dodonna Sets Up The Right-Click Intercept ─────────────────────────────────────────
    // The context menu is dynamic — it rebuilds itself every time the user right-clicks
    // because the enabled/disabled state of each action depends on the current selection.
    // We configure the manager to clear and rebuild on each show, then add a listener that
    // calls contextMenuAboutToShow() to do the actual population.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the given {@link IMenuManager} so that the right-click context menu rebuilds
     * itself dynamically every time it is shown — needed because action availability depends
     * on the current viewer selection.
     * The actual menu content is determined by {@link #contextMenuAboutToShow(IMenuManager)}.
     *
     * @param menuManager  the menu manager to configure; it will be told to remove-all-when-shown
     *                     and given a listener that calls {@link #contextMenuAboutToShow}
     */
    public void fillContextMenu( IMenuManager menuManager )
    {
        menuManager.setRemoveAllWhenShown( true );
        menuManager.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( IMenuManager manager )
            {
                contextMenuAboutToShow( manager );
            }
        } );
    }


    // ── Dodonna Reads Out The Mission Assignments At Right-Click Time ─────────────────────
    // Every time the context menu pops up, this method rebuilds its content from scratch.
    // The sections are: new (create), copy/paste/delete/select-all, the edit submenu
    // (value editors), and properties.  Subclasses can override to add more items.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Populates the context menu with all relevant actions for the current selection.
     * Called by the menu listener set up in {@link #fillContextMenu}.
     * Subclasses override this to add extra items (e.g. {@link EntryEditorWidgetActionGroupWithAttribute}
     * adds a "New Attribute" entry and an attribute-description editor).
     *
     * <p>For example — Dodonna reads the assignments:</p>
     * <pre>
     *   New Value
     *   ---
     *   Copy | Paste | Delete | Select All
     *   ---
     *   Edit Value (submenu with value editor options)
     *   ---
     *   Properties
     * </pre>
     *
     * @param menuManager  the menu manager to populate; already cleared by the menu listener
     */
    protected void contextMenuAboutToShow( IMenuManager menuManager )
    {
        // new
        menuManager.add( entryEditorActionMap.get( NEW_VALUE_ACTION ) );
        menuManager.add( new Separator() );

        // copy, paste, delete
        menuManager.add( entryEditorActionMap.get( COPY_ACTION ) );
        menuManager.add( entryEditorActionMap.get( PASTE_ACTION ) );
        menuManager.add( entryEditorActionMap.get( DELETE_ACTION ) );
        menuManager.add( entryEditorActionMap.get( SELECT_ALL_ACTION ) );
        menuManager.add( new Separator() );

        // edit
        addEditMenu( menuManager );
        menuManager.add( new Separator() );

        // properties
        menuManager.add( entryEditorActionMap.get( PROPERTY_DIALOG_ACTION ) );
    }


    // ── Dodonna Assembles The "Edit Value With" Submenu ───────────────────────────────────
    // The "Edit Value With" submenu lists the default editor, then (if enabled) the best
    // editor for the selected value type, then any other compatible editor plugins, and
    // finally a link to value editor preferences.  Editors that don't apply to the current
    // value type are hidden.  Dodonna giving pilots the right equipment for the right target.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the "Edit Value" default action and the "Edit Value With" submenu to the given
     * menu manager.  The submenu contains the best-fit editor (if enabled), all other
     * applicable value editors, and a link to preferences.
     * Editors whose type matches the best-editor type are excluded from the "other" list
     * to avoid showing the same editor twice.
     *
     * <p>For example — Dodonna hands out the right weapons:</p>
     * <pre>
     *   [Edit Value]
     *   [Edit Value With ▶]
     *       Best Editor (e.g. DN Editor)
     *       ---
     *       Text Editor
     *       Hex Editor
     *       ---
     *       Value Editor Preferences...
     * </pre>
     *
     * @param menuManager  the menu manager to add the edit actions to
     */
    protected void addEditMenu( IMenuManager menuManager )
    {
        menuManager.add( openDefaultValueEditorActionProxy );
        MenuManager editorMenuManager = new MenuManager( Messages
            .getString( "EntryEditorWidgetActionGroup.EditValueWith" ) ); //$NON-NLS-1$
        if ( openBestValueEditorActionProxy.isEnabled() )
        {
            editorMenuManager.add( openBestValueEditorActionProxy );
            editorMenuManager.add( new Separator() );
        }
        for ( EntryEditorActionProxy action : openValueEditorActionProxies )
        {
            if ( action.isEnabled()
                && ( ( OpenEditorAction ) action.getAction() ).getValueEditor().getClass() != ( ( OpenBestEditorAction ) openBestValueEditorActionProxy
                    .getAction() ).getBestValueEditor().getClass() )
            {
                editorMenuManager.add( action );
            }
        }
        editorMenuManager.add( new Separator() );
        editorMenuManager.add( openValueEditorPreferencesAction );
        menuManager.add( editorMenuManager );
    }


    // ── Dodonna Opens The Comm Channel — Global Shortcuts Are Live ────────────────────────
    // When the entry editor gains focus, we register our actions as handlers for the
    // platform-wide Ctrl+C, Ctrl+V, Delete, etc. shortcuts.  If we have action bars
    // (running inside an Eclipse view or editor), we use setGlobalActionHandler; otherwise
    // we register action-definition IDs directly via ActionUtils.
    // Dodonna patching the Rebel comms into the Alliance-wide frequency so command can hear them.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers this group's actions as global Eclipse action handlers so that standard
     * keyboard shortcuts (Ctrl+C, Ctrl+V, Delete, Ctrl+A, F2, etc.) route to the entry
     * editor actions while it has focus.
     * Uses either the {@link IActionBars} API (preferred, inside Eclipse views) or the
     * {@link ActionUtils} key-binding approach (fallback for standalone widget usage).
     */
    public void activateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars.setGlobalActionHandler( ActionFactory.COPY.getId(), entryEditorActionMap.get( COPY_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.PASTE.getId(), entryEditorActionMap.get( PASTE_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(), entryEditorActionMap.get( DELETE_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.SELECT_ALL.getId(), entryEditorActionMap
                .get( SELECT_ALL_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), entryEditorActionMap
                .get( PROPERTY_DIALOG_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.FIND.getId(), showQuickFilterAction ); // IWorkbenchActionDefinitionIds.FIND_REPLACE

            actionBars.updateActionBars();
        }
        else
        {
            IAction da = entryEditorActionMap.get( DELETE_ACTION );
            da.setActionDefinitionId( BrowserCommonConstants.CMD_DELETE );
            ActionUtils.activateActionHandler( da );

            IAction ca = entryEditorActionMap.get( COPY_ACTION );
            ca.setActionDefinitionId( BrowserCommonConstants.CMD_COPY );
            ActionUtils.activateActionHandler( ca );

            IAction pa = entryEditorActionMap.get( PASTE_ACTION );
            pa.setActionDefinitionId( BrowserCommonConstants.CMD_PASTE );
            ActionUtils.activateActionHandler( pa );

            showQuickFilterAction.setActionDefinitionId( BrowserCommonConstants.CMD_FIND );
            ActionUtils.activateActionHandler( showQuickFilterAction );

            IAction pda = entryEditorActionMap.get( PROPERTY_DIALOG_ACTION );
            pda.setActionDefinitionId( BrowserCommonConstants.CMD_PROPERTIES );
            ActionUtils.activateActionHandler( pda );
        }

        IAction nva = entryEditorActionMap.get( NEW_VALUE_ACTION );
        ActionUtils.activateActionHandler( nva );
        ActionUtils.activateActionHandler( openDefaultValueEditorActionProxy );
    }


    // ── Dodonna Closes The Comm Channel — Shortcuts Go Silent ────────────────────────────
    // When the entry editor loses focus, we unregister our handlers so that the global
    // Ctrl+C etc. revert to whatever the newly focused widget wants them to do.
    // Dodonna switching the comms back to standby so other ships can use the frequency.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Unregisters all global Eclipse action handlers that were set by
     * {@link #activateGlobalActionHandlers()}.  Called when the entry editor loses focus —
     * allows the shortcuts to route to the newly focused view/editor instead.
     */
    public void deactivateGlobalActionHandlers()
    {
        if ( actionBars != null )
        {
            actionBars.setGlobalActionHandler( ActionFactory.COPY.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.PASTE.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.SELECT_ALL.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.FIND.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), null );

            actionBars.updateActionBars();
        }
        else
        {
            IAction ca = entryEditorActionMap.get( COPY_ACTION );
            ActionUtils.deactivateActionHandler( ca );
            IAction pa = entryEditorActionMap.get( PASTE_ACTION );
            ActionUtils.deactivateActionHandler( pa );
            IAction da = entryEditorActionMap.get( DELETE_ACTION );
            ActionUtils.deactivateActionHandler( da );
            ActionUtils.deactivateActionHandler( showQuickFilterAction );
            IAction pda = entryEditorActionMap.get( PROPERTY_DIALOG_ACTION );
            ActionUtils.deactivateActionHandler( pda );
        }

        IAction nva = entryEditorActionMap.get( NEW_VALUE_ACTION );
        ActionUtils.deactivateActionHandler( nva );
        ActionUtils.deactivateActionHandler( openDefaultValueEditorActionProxy );
    }


    // ── Dodonna Hands Over The Default Editor Briefing ────────────────────────────────────
    // The default editor action is special — it's the one that opens when the user just
    // presses F2 or double-clicks a value without picking a specific editor.  Other parts
    // of the widget need to access it directly (e.g. the cell modifier), so we expose it.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link OpenDefaultEditorAction} that is invoked by the default F2/Enter
     * key binding or double-click in the viewer.  Other components (e.g. the cell modifier)
     * need direct access to this action to check its state.
     *
     * @return the unwrapped {@link OpenDefaultEditorAction} from its proxy wrapper
     */
    public OpenDefaultEditorAction getOpenDefaultEditorAction()
    {
        return ( OpenDefaultEditorAction ) openDefaultValueEditorActionProxy.getAction();
    }


    // ── Dodonna Updates The Roster For A New Target Entry ────────────────────────────────
    // When the user navigates to a different LDAP entry, all actions need to know which
    // entry is now active so they can re-evaluate their enabled state.  This overload handles
    // the case where a full IEntry is loaded — Dodonna re-briefing the squad on the new target.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all actions in this group that the active LDAP entry has changed.
     * Each action re-evaluates its enabled state based on the new entry.
     * Called when the entry editor switches to displaying a different {@link IEntry}.
     *
     * @param entry  the newly active LDAP entry to show in the editor
     */
    public void setInput( IEntry entry )
    {
        for ( EntryEditorActionProxy action : entryEditorActionMap.values() )
        {
            action.inputChanged( entry );
        }
    }


    // ── Dodonna Updates The Roster For An Attribute-Scoped View ───────────────────────────
    // Some entry editor views show only a subset of an entry's attributes — an AttributeHierarchy
    // rather than the full entry.  When the input switches to that kind of view, actions need
    // to know the hierarchy so they can adapt.  Dodonna re-briefing for a partial-entry target.
    // ─────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Notifies all actions that the active input has changed to an {@link AttributeHierarchy}
     * (a filtered view showing only certain attributes of an entry).
     * Actions re-evaluate their enabled state for this narrower input.
     *
     * @param attributeHierarchy  the attribute hierarchy now displayed in the editor
     */
    public void setInput( AttributeHierarchy attributeHierarchy )
    {
        for ( EntryEditorActionProxy action : entryEditorActionMap.values() )
        {
            action.inputChanged( attributeHierarchy );
        }
    }

}
