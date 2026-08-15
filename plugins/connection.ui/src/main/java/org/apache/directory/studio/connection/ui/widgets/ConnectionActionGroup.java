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

package org.apache.directory.studio.connection.ui.widgets;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.directory.studio.connection.ui.ConnectionUIConstants;
import org.apache.directory.studio.connection.ui.actions.ActionHandlerManager;
import org.apache.directory.studio.connection.ui.actions.CloseConnectionAction;
import org.apache.directory.studio.connection.ui.actions.CollapseAllAction;
import org.apache.directory.studio.connection.ui.actions.ConnectionViewActionProxy;
import org.apache.directory.studio.connection.ui.actions.CopyAction;
import org.apache.directory.studio.connection.ui.actions.DeleteAction;
import org.apache.directory.studio.connection.ui.actions.ExpandAllAction;
import org.apache.directory.studio.connection.ui.actions.NewConnectionAction;
import org.apache.directory.studio.connection.ui.actions.NewConnectionFolderAction;
import org.apache.directory.studio.connection.ui.actions.OpenConnectionAction;
import org.apache.directory.studio.connection.ui.actions.PasteAction;
import org.apache.directory.studio.connection.ui.actions.PropertiesAction;
import org.apache.directory.studio.connection.ui.actions.RenameAction;
import org.apache.directory.studio.connection.ui.actions.StudioActionProxy;
import org.apache.directory.studio.connection.ui.dnd.ConnectionTransfer;
import org.apache.directory.studio.connection.ui.dnd.DragConnectionListener;
import org.apache.directory.studio.connection.ui.dnd.DropConnectionListener;
import org.apache.directory.studio.utils.ActionUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;


// ── CLASS: ConnectionActionGroup — REBEL BASE OPERATIONS OFFICER ──────────────────
// The operations officer at the Rebel base wires everything up before the mission:
// assigns pilots to ships (New Connection / New Folder), clears pilots for scramble
// (Open/Close), authorises copy/paste/delete/rename of records, and finally
// broadcasts who handles each global command over the base's PA system.
// ConnectionActionGroup is that officer for the connection tree widget.  The
// constructor builds all nine action proxies, wires up drag-and-drop, and stores
// them in a keyed map.  fillToolBar() and fillContextMenu() push subsets of those
// actions into the right UI slots.  activateGlobalActionHandlers() either registers
// the actions as Eclipse global command handlers (standalone mode via ActionUtils)
// or sets them into the host view's IActionBars (embedded mode).
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Manages all actions for the connection tree widget.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Creates all {@link ConnectionViewActionProxy} wrappers around the concrete
 *       action objects.</li>
 *   <li>Installs {@link DragConnectionListener} and {@link DropConnectionListener}
 *       on the tree viewer for drag-and-drop support.</li>
 *   <li>Fills the toolbar, local menu, and context menu.</li>
 *   <li>Activates/deactivates the global action handlers for Copy, Paste, Delete,
 *       and Properties — routing to either {@link ActionUtils} (standalone mode) or
 *       {@link IActionBars} (embedded mode).</li>
 * </ul>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionActionGroup implements ActionHandlerManager, IMenuListener
{
    // ── ACTION KEY CONSTANTS ──────────────────────────────────────────────────────

    /** Map key for the "New Connection" action. */
    protected static final String NEW_CONNECTION_ACTION = "newConnectionAction"; //$NON-NLS-1$

    /** Map key for the "New Connection Folder" action. */
    protected static final String NEW_CONNECTION_FOLDER_ACTION = "newConnectionFolderAction"; //$NON-NLS-1$

    /** Map key for the "Open Connection" action. */
    protected static final String OPEN_CONNECTION_ACTION = "openConnectionAction"; //$NON-NLS-1$

    /** Map key for the "Close Connection" action. */
    protected static final String CLOSE_CONNECTION_ACTION = "closeConnectionAction"; //$NON-NLS-1$

    /** Map key for the "Copy Connection" action. */
    protected static final String COPY_CONNECTION_ACTION = "copyConnectionAction"; //$NON-NLS-1$

    /** Map key for the "Paste Connection" action. */
    protected static final String PASTE_CONNECTION_ACTION = "pasteConnectionAction"; //$NON-NLS-1$

    /** Map key for the "Delete Connection" action. */
    protected static final String DELETE_CONNECTION_ACTION = "deleteConnectionAction"; //$NON-NLS-1$

    /** Map key for the "Rename Connection" action. */
    protected static final String RENAME_CONNECTION_ACTION = "renameConnectionAction"; //$NON-NLS-1$

    /** Map key for the "Properties" action. */
    protected static final String PROPERTY_DIALOG_ACTION = "propertyDialogAction"; //$NON-NLS-1$


    // ── FIELDS ────────────────────────────────────────────────────────────────────

    /** Toolbar-only action: collapse all tree nodes. */
    private CollapseAllAction collapseAllAction;

    /** Toolbar-only action: expand all tree nodes. */
    private ExpandAllAction expandAllAction;

    /** DnD: listens on the drag source (the tree viewer). */
    private DragConnectionListener dragConnectionListener;

    /** DnD: listens on the drop target (the tree viewer). */
    private DropConnectionListener dropConnectionListener;

    /** Map from key constants above to {@link ConnectionViewActionProxy} instances. */
    protected Map<String, ConnectionViewActionProxy> connectionActionMap;

    /**
     * The host view's action bars for embedded mode, or {@code null} in standalone
     * mode.  Set by {@link #enableGlobalActionHandlers(IActionBars)}.
     */
    protected IActionBars actionBars;

    /** The connection widget whose viewer we manage actions for. */
    protected ConnectionWidget mainWidget;


    // ── CONSTRUCTOR ───────────────────────────────────────────────────────────────
    /**
     * Creates a new {@link ConnectionActionGroup} for the given widget.
     *
     * <p>Builds all action proxies, wires drag-and-drop on the viewer, and stores
     * everything in the action map.  Note that the Paste action is created first
     * because the Copy action needs a reference to it.</p>
     *
     * @param mainWidget    The {@link ConnectionWidget} to manage.
     * @param configuration The {@link ConnectionConfiguration} (unused here but
     *                      kept for API symmetry with the view's setup code).
     */
    public ConnectionActionGroup( ConnectionWidget mainWidget, ConnectionConfiguration configuration )
    {
        this.mainWidget = mainWidget;
        TreeViewer viewer = mainWidget.getViewer();

        collapseAllAction = new CollapseAllAction( viewer );
        expandAllAction = new ExpandAllAction( viewer );

        connectionActionMap = new HashMap<>();

        connectionActionMap.put( NEW_CONNECTION_ACTION, new ConnectionViewActionProxy( viewer, this,
            new NewConnectionAction() ) );
        connectionActionMap.put( NEW_CONNECTION_FOLDER_ACTION, new ConnectionViewActionProxy( viewer, this,
            new NewConnectionFolderAction() ) );
        connectionActionMap.put( OPEN_CONNECTION_ACTION, new ConnectionViewActionProxy( viewer, this,
            new OpenConnectionAction() ) );
        connectionActionMap.put( CLOSE_CONNECTION_ACTION, new ConnectionViewActionProxy( viewer, this,
            new CloseConnectionAction() ) );

        // ── PASTE BEFORE COPY ─────────────────────────────────────────────────────
        // CopyAction holds a reference to PasteAction so it can update paste
        // availability after a copy — create Paste first.
        // ──────────────────────────────────────────────────────────────────────────
        connectionActionMap
            .put( PASTE_CONNECTION_ACTION, new ConnectionViewActionProxy( viewer, this, new PasteAction() ) );
        connectionActionMap.put( COPY_CONNECTION_ACTION, new ConnectionViewActionProxy( viewer, this, new CopyAction(
            ( StudioActionProxy ) connectionActionMap.get( PASTE_CONNECTION_ACTION ) ) ) );

        connectionActionMap.put( DELETE_CONNECTION_ACTION, new ConnectionViewActionProxy( viewer, this,
            new DeleteAction() ) );
        connectionActionMap.put( RENAME_CONNECTION_ACTION, new ConnectionViewActionProxy( viewer, this,
            new RenameAction() ) );
        connectionActionMap.put( PROPERTY_DIALOG_ACTION, new ConnectionViewActionProxy( viewer, this,
            new PropertiesAction() ) );

        // ── DRAG AND DROP ─────────────────────────────────────────────────────────
        // Allow both COPY and MOVE operations; use ConnectionTransfer for the
        // serialisation format.
        // ──────────────────────────────────────────────────────────────────────────
        dropConnectionListener = new DropConnectionListener();
        dragConnectionListener = new DragConnectionListener( viewer );
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[]
            { ConnectionTransfer.getInstance() };
        viewer.addDragSupport( ops, transfers, dragConnectionListener );
        viewer.addDropSupport( ops, transfers, dropConnectionListener );
    }


    // ── DISPOSE ───────────────────────────────────────────────────────────────────
    /**
     * Disposes all actions and releases all references.
     *
     * <p>Safe to call multiple times — subsequent calls are no-ops.</p>
     */
    public void dispose()
    {
        if ( mainWidget != null )
        {
            for ( Iterator<String> it = connectionActionMap.keySet().iterator(); it.hasNext(); )
            {
                String key = it.next();
                ConnectionViewActionProxy action = this.connectionActionMap.get( key );
                action.dispose();
                it.remove();
            }

            collapseAllAction.dispose();
            collapseAllAction = null;

            expandAllAction.dispose();
            expandAllAction = null;

            connectionActionMap.clear();
            connectionActionMap = null;

            actionBars = null;
            mainWidget = null;

            dragConnectionListener = null;
            dropConnectionListener = null;
        }
    }


    // ── ENABLE GLOBAL ACTION HANDLERS ─────────────────────────────────────────────
    /**
     * Stores the host view's {@link IActionBars} and activates the global action
     * handlers.
     *
     * <p>Call this from the view's {@code setFocus()} or
     * {@code createPartControl()} to wire the actions into the global Eclipse
     * command framework.</p>
     *
     * @param actionBars The host view's {@link IActionBars}.
     */
    public void enableGlobalActionHandlers( IActionBars actionBars )
    {
        this.actionBars = actionBars;
        activateGlobalActionHandlers();
    }


    // ── FILL TOOL BAR ─────────────────────────────────────────────────────────────
    /**
     * Populates the toolbar with the New Connection, Open/Close, and
     * Expand/Collapse actions.
     *
     * @param toolBarManager The toolbar to populate.
     */
    public void fillToolBar( IToolBarManager toolBarManager )
    {
        toolBarManager.add( ( IAction ) this.connectionActionMap.get( NEW_CONNECTION_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( ( IAction ) this.connectionActionMap.get( OPEN_CONNECTION_ACTION ) );
        toolBarManager.add( ( IAction ) this.connectionActionMap.get( CLOSE_CONNECTION_ACTION ) );
        toolBarManager.add( new Separator() );
        toolBarManager.add( expandAllAction );
        toolBarManager.add( collapseAllAction );

        toolBarManager.update( true );
    }


    // ── FILL MENU ─────────────────────────────────────────────────────────────────
    /**
     * Populates the local (view) menu.
     *
     * <p>Currently empty — all actions are in the context menu or toolbar.</p>
     *
     * @param menuManager The local menu manager.
     */
    public void fillMenu( IMenuManager menuManager )
    {
    }


    // ── FILL CONTEXT MENU ─────────────────────────────────────────────────────────
    /**
     * Wires up the context menu so it populates dynamically via
     * {@link #menuAboutToShow(IMenuManager)}.
     *
     * <p>Sets {@code removeAllWhenShown(true)} and registers this class as the
     * menu listener so the menu is rebuilt from scratch each time it opens.</p>
     *
     * @param menuManager The context menu manager.
     */
    public void fillContextMenu( IMenuManager menuManager )
    {
        menuManager.setRemoveAllWhenShown( true );
        menuManager.addMenuListener( this );
    }


    // ── MENU ABOUT TO SHOW ────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * <p>Populates the context menu immediately before it is displayed.  The menu
     * is structured as:</p>
     * <ol>
     *   <li>New Connection / New Folder</li>
     *   <li>Open or Close (only the enabled one appears)</li>
     *   <li>Copy / Paste / Delete / Rename</li>
     *   <li>MB_ADDITIONS extension point separator</li>
     *   <li>Properties</li>
     * </ol>
     *
     * @param menuManager The context menu being built.
     */
    public void menuAboutToShow( IMenuManager menuManager )
    {
        // ── NEW ───────────────────────────────────────────────────────────────────
        menuManager.add( ( IAction ) connectionActionMap.get( NEW_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( NEW_CONNECTION_FOLDER_ACTION ) );
        menuManager.add( new Separator() );

        // ── OPEN / CLOSE ──────────────────────────────────────────────────────────
        // Show Close if possible, Open otherwise — never show both at once.
        // ──────────────────────────────────────────────────────────────────────────
        if ( ( connectionActionMap.get( CLOSE_CONNECTION_ACTION ) ).isEnabled() )
        {
            menuManager.add( ( IAction ) connectionActionMap.get( CLOSE_CONNECTION_ACTION ) );
        }
        else if ( ( connectionActionMap.get( OPEN_CONNECTION_ACTION ) ).isEnabled() )
        {
            menuManager.add( ( IAction ) connectionActionMap.get( OPEN_CONNECTION_ACTION ) );
        }
        menuManager.add( new Separator() );

        // ── EDIT ──────────────────────────────────────────────────────────────────
        menuManager.add( ( IAction ) connectionActionMap.get( COPY_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( PASTE_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( DELETE_CONNECTION_ACTION ) );
        menuManager.add( ( IAction ) connectionActionMap.get( RENAME_CONNECTION_ACTION ) );
        menuManager.add( new Separator() );

        // ── ADDITIONS ─────────────────────────────────────────────────────────────
        menuManager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
        menuManager.add( new Separator() );

        // ── PROPERTIES ────────────────────────────────────────────────────────────
        menuManager.add( ( IAction ) connectionActionMap.get( PROPERTY_DIALOG_ACTION ) );
    }


    // ── ACTIVATE GLOBAL ACTION HANDLERS ──────────────────────────────────────────
    /**
     * Activates the global command handlers for Copy, Paste, Delete, and
     * Properties.
     *
     * <p>In standalone mode (no {@link IActionBars}) uses {@link ActionUtils} to
     * register each action as an Eclipse global handler.  In embedded mode uses
     * the host view's {@link IActionBars#setGlobalActionHandler}.</p>
     */
    public void activateGlobalActionHandlers()
    {
        if ( actionBars == null )
        {
            // ── STANDALONE MODE — USE ActionUtils ─────────────────────────────────
            IAction copyConnectionAction = connectionActionMap.get( COPY_CONNECTION_ACTION );
            copyConnectionAction.setActionDefinitionId( ConnectionUIConstants.CMD_COPY );
            ActionUtils.activateActionHandler( copyConnectionAction );

            IAction pasteConnectionAction = connectionActionMap.get( PASTE_CONNECTION_ACTION );
            pasteConnectionAction.setActionDefinitionId( ConnectionUIConstants.CMD_PASTE );
            ActionUtils.activateActionHandler( pasteConnectionAction );

            IAction deleteConnectionAction = connectionActionMap.get( DELETE_CONNECTION_ACTION );
            deleteConnectionAction.setActionDefinitionId( ConnectionUIConstants.CMD_DELETE );
            ActionUtils.activateActionHandler( deleteConnectionAction );

            IAction propertyDialogAction = connectionActionMap.get( PROPERTY_DIALOG_ACTION );
            propertyDialogAction.setActionDefinitionId( ConnectionUIConstants.CMD_PROPERTIES );
            ActionUtils.activateActionHandler( propertyDialogAction );
        }
        else
        {
            // ── EMBEDDED MODE — USE IActionBars ───────────────────────────────────
            actionBars.setGlobalActionHandler( ActionFactory.COPY.getId(), ( IAction ) connectionActionMap
                .get( COPY_CONNECTION_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.PASTE.getId(), ( IAction ) connectionActionMap
                .get( PASTE_CONNECTION_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(), ( IAction ) connectionActionMap
                .get( DELETE_CONNECTION_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.RENAME.getId(), ( IAction ) connectionActionMap
                .get( RENAME_CONNECTION_ACTION ) );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), ( IAction ) connectionActionMap
                .get( PROPERTY_DIALOG_ACTION ) );
            actionBars.updateActionBars();
        }
    }


    // ── DEACTIVATE GLOBAL ACTION HANDLERS ────────────────────────────────────────
    /**
     * Deactivates the global command handlers for Copy, Paste, Delete, and
     * Properties.
     *
     * <p>Call this when the connection widget loses focus so other views can
     * reclaim the global handlers.</p>
     */
    public void deactivateGlobalActionHandlers()
    {
        if ( actionBars == null )
        {
            // ── STANDALONE MODE — USE ActionUtils ─────────────────────────────────
            IAction copyConnectionAction = connectionActionMap.get( COPY_CONNECTION_ACTION );
            ActionUtils.deactivateActionHandler( copyConnectionAction );
            IAction pasteConnectionAction = connectionActionMap.get( PASTE_CONNECTION_ACTION );
            ActionUtils.deactivateActionHandler( pasteConnectionAction );
            IAction deleteConnectionAction = connectionActionMap.get( DELETE_CONNECTION_ACTION );
            ActionUtils.deactivateActionHandler( deleteConnectionAction );
            IAction propertyDialogAction = connectionActionMap.get( PROPERTY_DIALOG_ACTION );
            ActionUtils.deactivateActionHandler( propertyDialogAction );
        }
        else
        {
            // ── EMBEDDED MODE — CLEAR IActionBars ─────────────────────────────────
            actionBars.setGlobalActionHandler( ActionFactory.COPY.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.PASTE.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.DELETE.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.RENAME.getId(), null );
            actionBars.setGlobalActionHandler( ActionFactory.PROPERTIES.getId(), null );
            actionBars.updateActionBars();
        }
    }
}
