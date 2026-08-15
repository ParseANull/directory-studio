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
package org.apache.directory.studio.ldapservers.views;


import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.LdapServersManagerListener;
import org.apache.directory.studio.ldapservers.LdapServersPluginConstants;
import org.apache.directory.studio.ldapservers.actions.DeleteAction;
import org.apache.directory.studio.ldapservers.actions.NewServerAction;
import org.apache.directory.studio.ldapservers.actions.OpenConfigurationAction;
import org.apache.directory.studio.ldapservers.actions.PropertiesAction;
import org.apache.directory.studio.ldapservers.actions.RenameAction;
import org.apache.directory.studio.ldapservers.actions.StartAction;
import org.apache.directory.studio.ldapservers.actions.StopAction;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: ServersView — LANDO'S CLOUD CITY MASTER CONTROL ROOM ──────────────────────────
// Lando's master control room shows every facility in Cloud City on one big panel.
// Operators can start a gas extractor, stop the carbon-freezing chamber, rename a landing pad,
// or open a facility's engineering schematic — all from the same control room.
// This is that control room: the Eclipse ViewPart that shows the list of LDAP server instances,
// wires up all the toolbar and context-menu actions, and keeps the display in sync with the
// live LdapServersManager as servers are added, removed, or updated.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * The "Servers" view — the main Eclipse ViewPart that lists all configured LDAP server instances.
 * Contains a two-column tree: server name on the left, status text on the right.
 * Toolbar offers New Server and Start/Stop; right-click adds Open Configuration, Delete,
 * Rename, and Properties.
 * Registers keyboard shortcuts via the Eclipse command/context service when the view is active.
 * Think of it as Lando's Cloud City master control room.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServersView extends ViewPart
{
    /** The ID of the view */
    //    public static final String ID = ApacheDsPluginConstants.VIEW_SERVERS_VIEW; // TODO

    /** The tree*/
    private Tree tree;

    /** The table viewer */
    private ServersTableViewer tableViewer;

    /** The view instance */
    private ServersView instance;

    /** Token used to activate and deactivate shortcuts in the view */
    private IContextActivation contextActivation;

    private static final String TAG_COLUMN_WIDTH = "columnWidth"; //$NON-NLS-1$
    protected int[] columnWidths;

    // Actions
    private NewServerAction newServer;
    private OpenConfigurationAction openConfiguration;
    private DeleteAction delete;
    private RenameAction rename;
    private StartAction start;
    private StopAction stop;
    private PropertiesAction properties;

    // Listeners
    private LdapServersManagerListener ldapServersManagerListener = new LdapServersManagerListener()
    {
        public void serverAdded( LdapServer server )
        {
            asyncRefresh();
        }


        public void serverRemoved( LdapServer server )
        {
            asyncRefresh();
        }


        public void serverUpdated( LdapServer server )
        {
            asyncRefresh();
        }
    };


    // ── Building The Control Room Panel ──────────────────────────────────────────────────────
    // Lando's team assembles the control room: they lay out the main status board (the Tree),
    // add column headers ("Server", "State"), then wire up the actions, toolbar, context menu,
    // and live-update listeners.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds the view's widget tree: creates the SWT Tree, adds the two columns ("Server" and
     * "State"), wraps it in a {@link ServersTableViewer}, then calls init helpers for actions,
     * toolbar, context menu, and listeners.
     *
     * @param parent  the parent composite provided by the Eclipse view framework
     */
    public void createPartControl( Composite parent )
    {
        instance = this;

        // Creating the Tree
        tree = new Tree( parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL );
        tree.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        tree.setHeaderVisible( true );
        tree.setLinesVisible( false );

        // Adding columns
        TreeColumn serverColumn = new TreeColumn( tree, SWT.SINGLE );
        serverColumn.setText( Messages.getString( "ServersView.server" ) ); //$NON-NLS-1$
        serverColumn.setWidth( columnWidths[0] );
        serverColumn.addSelectionListener( getColumnSelectionListener( 0 ) );
        tree.setSortColumn( serverColumn );
        tree.setSortDirection( SWT.UP );

        TreeColumn stateColumn = new TreeColumn( tree, SWT.SINGLE );
        stateColumn.setText( Messages.getString( "ServersView.state" ) ); //$NON-NLS-1$
        stateColumn.setWidth( columnWidths[1] );
        stateColumn.addSelectionListener( getColumnSelectionListener( 1 ) );

        // Creating the viewer
        tableViewer = new ServersTableViewer( tree );

        initActions();
        initToolbar();
        initContextMenu();
        initListeners();

        // set help context
        // TODO
        //        PlatformUI.getWorkbench().getHelpSystem()
        //            .setHelp( parent, ApacheDsPluginConstants.PLUGIN_ID + "." + "gettingstarted_views_servers" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    // ── Restoring Column Widths From The Last Session ────────────────────────────────────────
    // When Studio restarts, it restores the column widths the user had set last time.
    // We read these from the Eclipse memento and fall back to sensible defaults (150, 80).
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse before {@link #createPartControl}. Restores the saved column widths
     * from the memento (or uses defaults of 150px and 80px if none are saved).
     *
     * @param site     the view's site
     * @param memento  the saved state, or {@code null} on first launch
     * @throws PartInitException if the superclass init fails
     */
    public void init( IViewSite site, IMemento memento ) throws PartInitException
    {
        super.init( site, memento );
        columnWidths = new int[]
            { 150, 80 };
        for ( int i = 0; i < 2; i++ )
        {
            if ( memento != null )
            {
                Integer in = memento.getInteger( TAG_COLUMN_WIDTH + i );
                if ( in != null && in.intValue() > 5 )
                {
                    columnWidths[i] = in.intValue();
                }
            }
        }
    }


    // ── Saving Column Widths Before Studio Shuts Down ────────────────────────────────────────
    // Before Lando closes the control room, the team records the current column widths so the
    // panel looks the same when they re-open it next time.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the current column widths into the Eclipse memento so they survive a restart.
     *
     * @param memento  the mutable memento object to write into
     */
    public void saveState( IMemento memento )
    {
        TreeColumn[] tc = tableViewer.getTree().getColumns();
        for ( int i = 0; i < 2; i++ )
        {
            int width = tc[i].getWidth();
            if ( width != 0 )
            {
                memento.putInteger( TAG_COLUMN_WIDTH + i, width );
            }
        }
    }


    // ── Handing Focus To The Status Board ────────────────────────────────────────────────────
    // When Eclipse gives the view focus (e.g., the user clicks on it), we forward focus to
    // the tree so keyboard navigation works immediately.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this view should receive keyboard focus.
     * Delegates focus to the underlying {@link Tree} widget.
     */
    public void setFocus()
    {
        if ( tree != null )
        {
            tree.setFocus();
        }
    }


    // ── Wiring Up The Toolbar Buttons And Context Menu Actions ────────────────────────────────
    // Lando's operators all need buttons: "New", "Start", "Stop", "Open Config", "Delete", etc.
    // We create action objects here; we disable them all except New until a server is selected.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates all action instances and sets their initial enabled state.
     * All selection-dependent actions start disabled; they're re-evaluated on each selection change.
     */
    private void initActions()
    {
        newServer = new NewServerAction();

        openConfiguration = new OpenConfigurationAction( this );
        openConfiguration.setEnabled( false );

        delete = new DeleteAction( this );
        delete.setEnabled( false );

        rename = new RenameAction( this );
        rename.setEnabled( false );

        start = new StartAction( this );
        start.setEnabled( false );

        stop = new StopAction( this );
        stop.setEnabled( false );

        properties = new PropertiesAction( this );
        properties.setEnabled( false );
    }


    // ── Adding Buttons To The Toolbar ────────────────────────────────────────────────────────
    // The main toolbar gets the most-used buttons: New Server, and Start/Stop.
    // Less common operations live in the right-click context menu.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the New Server, Start, and Stop actions to the view's toolbar.
     */
    private void initToolbar()
    {
        IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        toolbar.add( newServer );
        toolbar.add( new Separator() );
        toolbar.add( start );
        toolbar.add( stop );
    }


    // ── Wiring Up The Right-Click Context Menu ────────────────────────────────────────────────
    // Right-clicking a server in the list opens the context menu: New, Open Configuration,
    // separator, Delete, Rename, separator, Start, Stop, separator, Properties.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Builds and registers the right-click context menu.
     * The menu is rebuilt from scratch every time it's shown (setRemoveAllWhenShown) so
     * action enabled states are always current.
     */
    private void initContextMenu()
    {
        MenuManager contextMenu = new MenuManager( "" ); //$NON-NLS-1$
        contextMenu.setRemoveAllWhenShown( true );
        contextMenu.addMenuListener( new IMenuListener()
        {
            public void menuAboutToShow( IMenuManager manager )
            {
                MenuManager newManager = new MenuManager( Messages.getString( "ServersView.new" ) ); //$NON-NLS-1$
                newManager.add( newServer );
                manager.add( newManager );
                manager.add( openConfiguration );
                manager.add( new Separator() );
                manager.add( delete );
                manager.add( rename );
                manager.add( new Separator() );
                manager.add( start );
                manager.add( stop );
                manager.add( new Separator() );
                manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
                manager.add( new Separator() );
                manager.add( new Separator() );
                manager.add( properties );
            }
        } );

        // set the context menu to the table viewer
        tableViewer.getControl().setMenu( contextMenu.createContextMenu( tableViewer.getControl() ) );

        // register the context menu to enable extension actions
        getSite().registerContextMenu( contextMenu, tableViewer );
    }


    // ── Wiring Up All The Live-Update Listeners ───────────────────────────────────────────────
    // The control room stays in sync through a network of listeners:
    //   - LdapServersManagerListener refreshes the view when servers are added/removed/updated.
    //   - Double-click opens the server's configuration editor.
    //   - Selection change re-evaluates action enabled states.
    //   - Part activation/deactivation activates/deactivates keyboard shortcuts.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers all listeners: the LdapServersManager listener for list changes, the double-click
     * listener that opens configuration, the selection listener that updates action states, and
     * the part listener that activates/deactivates keyboard shortcuts.
     */
    private void initListeners()
    {
        LdapServersManager serversHandler = LdapServersManager.getDefault();
        serversHandler.addListener( ldapServersManagerListener );

        tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                openConfiguration.run();
            }
        } );

        tableViewer.addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                updateActionsStates();
            }
        } );

        // Initializing the PartListener
        getSite().getPage().addPartListener( new IPartListener2()
        {
            /**
              * This implementation deactivates the shortcuts when the part is deactivated.
              */
            public void partDeactivated( IWorkbenchPartReference partRef )
            {
                if ( partRef.getPart( false ) == instance && contextActivation != null )
                {
                    ICommandService commandService = ( ICommandService ) PlatformUI.getWorkbench().getAdapter(
                        ICommandService.class );
                    if ( commandService != null )
                    {
                        commandService.getCommand( newServer.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( openConfiguration.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( delete.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( rename.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( start.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( stop.getActionDefinitionId() ).setHandler( null );
                        commandService.getCommand( properties.getActionDefinitionId() ).setHandler( null );
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
                if ( partRef.getPart( false ) == instance )
                {
                    IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                        IContextService.class );
                    contextActivation = contextService
                        .activateContext( LdapServersPluginConstants.CONTEXTS_SERVERS_VIEW );

                    ICommandService commandService = ( ICommandService ) PlatformUI.getWorkbench().getAdapter(
                        ICommandService.class );
                    if ( commandService != null )
                    {
                        commandService.getCommand( newServer.getActionDefinitionId() ).setHandler(
                            new ActionHandler( newServer ) );
                        commandService.getCommand( openConfiguration.getActionDefinitionId() ).setHandler(
                            new ActionHandler( openConfiguration ) );
                        commandService.getCommand( delete.getActionDefinitionId() ).setHandler(
                            new ActionHandler( delete ) );
                        commandService.getCommand( rename.getActionDefinitionId() ).setHandler(
                            new ActionHandler( rename ) );
                        commandService.getCommand( start.getActionDefinitionId() ).setHandler(
                            new ActionHandler( start ) );
                        commandService.getCommand( stop.getActionDefinitionId() )
                            .setHandler( new ActionHandler( stop ) );
                        commandService.getCommand( properties.getActionDefinitionId() ).setHandler(
                            new ActionHandler( properties ) );
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


    // ── Re-Evaluating Which Buttons Are Active After A Selection Change ───────────────────────
    // When the user clicks a different server row, the control room re-checks: can this server
    // be started?  Can it be stopped?  Are other actions available?
    // The logic is driven entirely by the selected server's current LdapServerStatus.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Re-evaluates the enabled state of all actions based on the current viewer selection
     * and the selected server's {@link org.apache.directory.studio.ldapservers.model.LdapServerStatus}.
     * Called whenever the selection changes or a server's status changes.
     *
     * <p>For example — a STARTED server:</p>
     * <pre>
     *   start.setEnabled(false) — can't start what's already running.
     *   stop.setEnabled(true)   — can stop it.
     *   delete, rename, properties all enabled.
     * </pre>
     */
    public void updateActionsStates()
    {
        // Getting the selection
        StructuredSelection selection = ( StructuredSelection ) tableViewer.getSelection();

        if ( !selection.isEmpty() )
        {
            LdapServer server = ( LdapServer ) selection.getFirstElement();

            switch ( server.getStatus() )
            {
                case STARTED:
                    start.setEnabled( false );
                    stop.setEnabled( true );
                    break;
                case REPAIRING:
                case STARTING:
                    start.setEnabled( false );
                    stop.setEnabled( false );
                    break;
                case STOPPED:
                    start.setEnabled( true );
                    stop.setEnabled( false );
                    break;
                case STOPPING:
                    start.setEnabled( false );
                    stop.setEnabled( false );
                    break;
                case UNKNOWN:
                    start.setEnabled( false );
                    stop.setEnabled( false );
                    break;
            }

            openConfiguration.setEnabled( server.getLdapServerAdapterExtension().isOpenConfigurationActionEnabled() );
            delete.setEnabled( true );
            rename.setEnabled( true );
            properties.setEnabled( true );
        }
        else
        {
            openConfiguration.setEnabled( false );
            delete.setEnabled( false );
            rename.setEnabled( false );
            start.setEnabled( false );
            stop.setEnabled( false );
            properties.setEnabled( false );
        }
    }


    // ── Handing Out A Reference To The Viewer ────────────────────────────────────────────────
    // Actions need access to the viewer to read the current selection.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link TreeViewer} backing this view.
     * Used by actions that need the current viewer selection.
     *
     * @return the table viewer
     */
    public TreeViewer getViewer()
    {
        return tableViewer;
    }


    // ── Shutting Down The Control Room ────────────────────────────────────────────────────────
    // When the view is disposed, we unregister our LdapServersManager listener so we don't
    // hold a stale reference and leak memory.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Called by Eclipse when this view is being disposed.
     * Unregisters the {@link LdapServersManagerListener} to release the reference and
     * avoid memory leaks, then delegates to the superclass.
     */
    public void dispose()
    {
        LdapServersManager.getDefault().removeListener( ldapServersManagerListener );

        super.dispose();
    }


    // ── Refreshing The Status Board Off The UI Thread ─────────────────────────────────────────
    // Server events may arrive on background threads; SWT requires all UI updates to happen
    // on the display thread.  asyncExec schedules the refresh safely.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Schedules a {@link ServersTableViewer#refresh()} on the SWT display thread.
     * Safe to call from any thread — uses {@code Display.asyncExec}.
     */
    private void asyncRefresh()
    {
        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {
                tableViewer.refresh();
            }
        } );
    }


    private SelectionListener getColumnSelectionListener( final int column )
    {
        return new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                TreeColumn treeColumn = ( TreeColumn ) e.widget;
                tableViewer.sort( treeColumn, column );
            }
        };
    }
}
