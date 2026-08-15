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

package org.apache.directory.studio.ldapbrowser.ui.views.connection;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.ui.widgets.ConnectionConfiguration;
import org.apache.directory.studio.connection.ui.widgets.ConnectionWidget;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


// ── CLASS: ConnectionView — CHEWIE'S HYPERSPACE COORDINATES ──────────────────
// Chewie keeps a meticulous list of hyperspace jump coordinates aboard the
// Millennium Falcon — every destination, every route, organized so Han can
// jump at a moment's notice. This view is that list: it shows every LDAP
// connection the user has defined, ready to open and navigate.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The main Eclipse view panel that displays all configured LDAP connections.
 * It lives in the Eclipse workbench as a dockable ViewPart and wires together
 * the connection widget, the action group, and the universal listener.
 * Think of this class as Chewie's navigation panel — every saved connection is
 * a hyperspace coordinate, and selecting one kicks off the jump.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionView extends ViewPart
{

    /** The configuration. */
    private ConnectionConfiguration configuration;

    /** The actions */
    private ConnectionViewActionGroup actionGroup;

    /** The main widget */
    private ConnectionWidget mainWidget;

    /** The listeners */
    private ConnectionViewUniversalListener universalListener;


    // ── Static ID: The Falcon's Transponder Code ────────────────────────────
    // Chewie has a unique transponder code registered with the Rebel Alliance
    // so every ship in the fleet knows exactly which vessel to hail.
    // We return the stable view ID so Eclipse can find and activate this
    // particular panel among all registered views in the workbench.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse view ID string that identifies this connection view.
     * We need this static method because Eclipse's registry looks up views by
     * their string ID, not by class name.
     *
     * <p>For example — Chewie's transponder returns the Falcon's unique call sign:</p>
     * <pre>
     *   String falconId = Falcon.getTransponderId(); // "YT-1300-FALCON"
     *   Fleet.hail( falconId ); // Eclipse hails our view by this exact string
     * </pre>
     *
     * @return  the view ID constant from {@code BrowserUIConstants}
     */
    public static String getId()
    {
        return BrowserUIConstants.VIEW_CONNECTION_VIEW;
    }


    // ── Constructor: Chewie Powers Up the Nav Console ───────────────────────
    // Chewie slides into the co-pilot seat, powers up the navigation console,
    // and waits — no coordinates loaded yet, just systems ready.
    // We construct an empty view; Eclipse calls createPartControl() next to
    // actually build the UI widgets.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new, empty ConnectionView instance.
     * Eclipse calls this reflectively when the view is first opened; real
     * initialization happens in {@link #createPartControl(Composite)}.
     *
     * <p>For example — Chewie powers up but waits for Han's coordinates:</p>
     * <pre>
     *   ConnectionView view = new ConnectionView(); // console on, no coords
     *   // Eclipse then calls createPartControl() to fill the panel
     * </pre>
     */
    public ConnectionView()
    {
    }


    // ── setFocus: Chewie Points the Controls at the Nav Screen ──────────────
    // Han says "Chewie, get us the jump coordinates" and Chewie immediately
    // swings attention to the navigation display, ready for input.
    // We route keyboard focus to the connection list widget so the user can
    // immediately use arrow keys or keyboard shortcuts.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Transfers keyboard focus to the connection list widget.
     * Eclipse calls this whenever our view becomes active in the workbench, so
     * keyboard shortcuts work right away without the user having to click.
     *
     * <p>For example — Chewie locks the nav controls on the hyperspace display:</p>
     * <pre>
     *   view.setFocus();
     *   // Now pressing Enter or arrow keys acts on the connection list
     * </pre>
     */
    public void setFocus()
    {
        mainWidget.getViewer().getControl().setFocus();
    }


    // ── dispose: Chewie Powers Down the Nav Console ─────────────────────────
    // After landing safely on Endor, Chewie methodically powers down each nav
    // subsystem — life support last — so nothing burns out on standby.
    // We tear down each component in the right order: actions first, then the
    // listener, then the widget, finally null out references so GC can collect.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up all resources held by this view when it is closed.
     * We dispose the action group, universal listener, and main widget in order,
     * then call super to let Eclipse finish the teardown.
     * The null checks guard against being called twice (Eclipse can call
     * {@code dispose()} more than once in error paths).
     *
     * <p>For example — Chewie shuts down nav subsystems before powering off:</p>
     * <pre>
     *   actionGroup.dispose();   // unplug the action wiring
     *   universalListener.dispose(); // stop listening for events
     *   mainWidget.dispose();    // release SWT native handles
     * </pre>
     */
    public void dispose()
    {
        if ( configuration != null )
        {
            actionGroup.dispose();
            actionGroup = null;
            configuration.dispose();
            configuration = null;
            universalListener.dispose();
            universalListener = null;
            mainWidget.dispose();
            mainWidget = null;
            getSite().setSelectionProvider( null );
        }

        super.dispose();
    }


    // ── createPartControl: Chewie Wires Up the Full Navigation Suite ─────────
    // Chewie doesn't just turn on one screen — he brings up the full nav suite:
    // charts, shields, comms, and the auto-pilot all connected together.
    // We build the SWT composite, attach the connection widget, wire the action
    // group into the toolbar and menus, and register the universal listener.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Builds and wires the complete connection view UI inside the given parent.
     * This is Eclipse's hook for us to create our widgets; it's called once,
     * right after the constructor, when the view is first shown in the workbench.
     * We create the layout, the connection tree widget, all toolbar/menu actions,
     * the context menu, and the selection-change listener.
     *
     * <p>For example — Chewie assembles the Falcon's full nav console:</p>
     * <pre>
     *   configuration = new ConnectionConfiguration(); // nav charts loaded
     *   mainWidget.createWidget( composite );           // screens powered up
     *   actionGroup = new ConnectionViewActionGroup();  // controls mapped
     *   universalListener = new ConnectionViewUniversalListener(); // auto-pilot on
     * </pre>
     *
     * @param parent  the SWT Composite provided by Eclipse to host our widgets
     */
    public void createPartControl( Composite parent )
    {

        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout( layout );

        PlatformUI.getWorkbench().getHelpSystem().setHelp( composite,
            BrowserUIConstants.PLUGIN_ID + "." + "tools_connections_view" ); //$NON-NLS-1$ //$NON-NLS-2$

        // create configuration
        configuration = new ConnectionConfiguration();

        // create main widget
        mainWidget = new ConnectionWidget( configuration, getViewSite().getActionBars() );
        mainWidget.createWidget( composite );
        mainWidget.setInput( ConnectionCorePlugin.getDefault().getConnectionFolderManager() );

        // create actions and context menu (and register global actions)
        actionGroup = new ConnectionViewActionGroup( this );
        actionGroup.fillToolBar( mainWidget.getToolBarManager() );
        actionGroup.fillMenu( mainWidget.getMenuManager() );
        actionGroup.enableGlobalActionHandlers( getViewSite().getActionBars() );
        IMenuManager contextMenuManager = configuration.getContextMenuManager( mainWidget.getViewer() );
        actionGroup.fillContextMenu( contextMenuManager );
        getSite().registerContextMenu( ( MenuManager ) contextMenuManager, mainWidget.getViewer() );

        // create the listener
        getSite().setSelectionProvider( mainWidget.getViewer() );
        universalListener = new ConnectionViewUniversalListener( this );
    }


    // ── select: Chewie Locks Onto a Jump Destination ────────────────────────
    // Han calls out a destination and Chewie scrolls the coordinate list,
    // highlights the matching entry, and locks it in for the jump computer.
    // We reveal the connection in the tree, refresh its label, and set the
    // JFace structured selection so other views react to the new selection.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Selects and reveals the given connection object in the viewer.
     * This is how other parts of the UI (like the browser view) tell us
     * which connection they want highlighted in our list.
     * Only {@link Connection} objects are handled; anything else is silently ignored.
     *
     * <p>For example — Chewie locks onto "Mos Eisley Cantina" in the nav list:</p>
     * <pre>
     *   view.select( mosEisleyConnection );
     *   // The connection scrolls into view, gets highlighted, and the
     *   // selection-changed event fires for any listening views.
     * </pre>
     *
     * @param obj  the object to select — must be a {@link Connection} instance
     */
    public void select( Object obj )
    {
        if ( obj instanceof Connection )
        {
            Connection connection = ( Connection ) obj;

            mainWidget.getViewer().reveal( connection );
            mainWidget.getViewer().refresh( connection, true );
            mainWidget.getViewer().setSelection( new StructuredSelection( connection ), true );
        }
    }


    // ── getActionGroup: Chewie Hands Over the Controls Panel ────────────────
    // When Han needs to fire up the shields manually, Chewie points him to
    // the action panel mounted between the pilot seats.
    // We return the action group so callers can add or trigger toolbar actions.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the action group that owns all toolbar and context-menu actions for this view.
     * Other parts of the plugin call this to wire up global action handlers or
     * check action state.
     *
     * <p>For example — Han reaches for the action panel Chewie points out:</p>
     * <pre>
     *   ConnectionViewActionGroup ag = view.getActionGroup();
     *   ag.enableGlobalActionHandlers( bars );
     * </pre>
     *
     * @return  the {@link ConnectionViewActionGroup} managing this view's actions
     */
    public ConnectionViewActionGroup getActionGroup()
    {
        return actionGroup;
    }


    // ── getConfiguration: Chewie Retrieves the Nav Config Data ──────────────
    // The nav config holds the route tables, renderer settings, and context
    // menu definitions — Chewie pulls it out of the console for any system
    // that needs to know how the display is set up.
    // We return the ConnectionConfiguration so callers can read rendering rules.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the configuration object that controls how the connection tree renders.
     * The {@link ConnectionConfiguration} owns things like label providers, content
     * providers, and context-menu managers.
     *
     * <p>For example — Chewie retrieves the nav chart settings:</p>
     * <pre>
     *   ConnectionConfiguration cfg = view.getConfiguration();
     *   IMenuManager menu = cfg.getContextMenuManager( viewer );
     * </pre>
     *
     * @return  the {@link ConnectionConfiguration} for this view
     */
    public ConnectionConfiguration getConfiguration()
    {
        return configuration;
    }


    // ── getMainWidget: Chewie Points to the Main Nav Screen ─────────────────
    // The main nav screen is the centerpiece of Chewie's console — it displays
    // all the coordinates and is the primary interaction surface.
    // We return the ConnectionWidget that owns the JFace TreeViewer and toolbar.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the main widget that hosts the connection tree viewer.
     * The {@link ConnectionWidget} owns the actual SWT tree control, its
     * toolbar manager, and menu manager.
     *
     * <p>For example — Chewie points Han to the main nav display:</p>
     * <pre>
     *   ConnectionWidget widget = view.getMainWidget();
     *   widget.getViewer().setSelection( new StructuredSelection( conn ) );
     * </pre>
     *
     * @return  the {@link ConnectionWidget} at the center of this view
     */
    public ConnectionWidget getMainWidget()
    {
        return mainWidget;
    }


    // ── getUniversalListener: Chewie Hands Over the Sensor Array ────────────
    // The sensor array monitors hyperspace for incoming signals and route
    // updates — Chewie manages it but sometimes other systems need direct access.
    // We return the universal listener so callers can poke its input directly.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the universal listener that wires events from the rest of the
     * workbench into this view.
     * Callers (like the browser view) use it to programmatically update the
     * connection displayed in this panel.
     *
     * <p>For example — Chewie hands over the sensor array reference:</p>
     * <pre>
     *   ConnectionViewUniversalListener ul = view.getUniversalListener();
     *   ul.setInput( someConnection );
     * </pre>
     *
     * @return  the {@link ConnectionViewUniversalListener} for this view
     */
    public ConnectionViewUniversalListener getUniversalListener()
    {
        return universalListener;
    }

}
