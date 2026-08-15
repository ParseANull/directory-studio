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

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import org.apache.directory.studio.common.ui.widgets.WidgetModifyEvent;
import org.apache.directory.studio.common.ui.widgets.WidgetModifyListener;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.BrowserConnectionWidget;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


// ── CLASS: BrowserConnectionWidgetContributionItem — Han Jumping To Hyperspace ─
// In the Millennium Falcon's cockpit, Han Solo sits at the navigation controls:
// a dropdown of available hyperspace routes (connections) that he selects before
// pulling the lever.  As soon as he picks a route the ship leaps to that
// destination.  This class embeds exactly that dropdown in the schema browser's
// toolbar — a {@link BrowserConnectionWidget} that lets the user pick which LDAP
// connection to navigate to, and as soon as they pick one, the schema browser
// jumps to show that server's schema.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * An Eclipse toolbar contribution item that embeds a connection-picker widget
 * in the schema browser toolbar.
 * When the user selects a different connection in the dropdown, we immediately
 * update the schema browser's input so all five schema pages reload.
 * We also listen for connection events from the registry so we can react if
 * the currently selected connection is updated or removed while the schema
 * browser is open.
 * Think of this class as Han's hyperspace route selector: pick a connection,
 * and the schema browser instantly jumps to that server's blueprints.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserConnectionWidgetContributionItem extends ContributionItem implements ConnectionUpdateListener
{
    /** The schema page */
    private SchemaPage schemaPage;

    /** The tool item */
    private ToolItem toolitem;

    /** The tool item composite */
    private Composite toolItemComposite;
    private BrowserConnectionWidget browserConnectionWidget;


    // ── Han Takes His Seat At The Navigation Console ──────────────────────────────
    // Han drops into the pilot's chair and links his controls to the ship's
    // navigation system so every route he picks goes somewhere meaningful.
    // We store the schema page reference so the widget can push connection changes
    // back to the page when the user picks a different route.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the contribution item and links it to the given schema page.
     * We store the schema page so the widget can call
     * {@code getSchemaBrowser().setInput()} when a new connection is selected.
     *
     * @param schemaPage  the schema page whose toolbar this item belongs to
     */
    public BrowserConnectionWidgetContributionItem( SchemaPage schemaPage )
    {
        this.schemaPage = schemaPage;
    }


    // ── Han Wires Up The Navigation Controls ──────────────────────────────────────
    // Han plugs his navigation panel into the ship's systems, sets the width of
    // the control so it fits the cockpit layout, and registers for hyperspace
    // status updates so he is notified if a route changes.
    // We build the BrowserConnectionWidget here, register for connection events,
    // and set the tool item width so it fits neatly in the toolbar.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the SWT control for this contribution item inside the given parent.
     * We build a composite containing a {@link BrowserConnectionWidget}, register
     * a modify listener that fires when the user picks a new connection, and
     * subscribe to connection events from the registry.
     *
     * <p>For example — Han wires his nav panel:</p>
     * <pre>
     *   Control c = createControl(parent);
     *   // browserConnectionWidget embedded, event listener registered
     *   // toolitem width = 250px
     * </pre>
     *
     * @param parent  the parent composite provided by the toolbar fill machinery
     * @return        the root SWT control of the widget
     */
    private Control createControl( Composite parent )
    {
        // Creating the ToolItem Composite
        toolItemComposite = new Composite( parent, SWT.NONE );
        GridLayout gridLayout = new GridLayout( 2, false );
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        toolItemComposite.setLayout( gridLayout );
        toolItemComposite.setLayoutData( new GridData( SWT.FILL, SWT.NONE, true, false ) );

        // Creating the Browser Connection Widget
        browserConnectionWidget = new BrowserConnectionWidget();
        browserConnectionWidget.createWidget( toolItemComposite );
        browserConnectionWidget.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                schemaPage.getSchemaBrowser().setInput( new SchemaBrowserInput( getConnection(), null ) );
            }
        } );

        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionUIPlugin.getDefault().getEventRunner() );

        // Initializing the width for the toolbar item
        toolitem.setWidth( 250 );

        return toolItemComposite;
    }


    // ── Han Powers Down The Navigation Panel ──────────────────────────────────────
    // When the Falcon is decommissioned Han disconnects the navigation console
    // and walks away — the controls are no longer useful.
    // We deregister from connection events and dispose the SWT composite to free
    // the resources when the schema browser closes.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up when the schema browser editor is closing.
     * We deregister from the connection event registry so we stop receiving
     * updates, then dispose the SWT composite.
     *
     * <p>For example — Han disconnects the nav panel:</p>
     * <pre>
     *   dispose();
     *   // no more connection events; toolItemComposite == null
     * </pre>
     */
    @Override
    public void dispose()
    {
        ConnectionEventRegistry.removeConnectionUpdateListener( this );
        toolItemComposite.dispose();
        toolItemComposite = null;
        browserConnectionWidget = null;
    }


    // ── Han Slots The Panel Into A Composite Bay ───────────────────────────────────
    // The ship mechanic slots Han's nav panel into an open bay in the composite
    // section of the cockpit — the standard fill path for composite parents.
    // We delegate to createControl() to build the actual widget.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Fills a {@link Composite} parent with this contribution item's control.
     * Called by the Eclipse contribution framework for composite-hosted toolbars.
     *
     * @param parent  the parent composite to embed the control in
     */
    @Override
    public final void fill( Composite parent )
    {
        createControl( parent );
    }


    // ── Han Refuses To Mount The Panel In The Cafeteria ───────────────────────────
    // Han's navigation controls do not belong in the galley — you cannot slot a
    // cockpit instrument into a cafeteria menu, and trying is an error.
    // Controls cannot be added to menus; we throw immediately rather than silently
    // doing nothing.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Throws {@link UnsupportedOperationException} because controls cannot be
     * embedded inside a menu.
     * If you need a connection picker in a menu, use an Action with a ComboBoxContribution
     * instead.
     *
     * @param parent  the menu (not used)
     * @param index   the menu index (not used)
     * @throws UnsupportedOperationException always
     */
    @Override
    public final void fill( Menu parent, int index )
    {
        throw new UnsupportedOperationException( Messages.getString( "BrowserConnectionWidgetContributionItem.CantAddControl" ) ); //$NON-NLS-1$
    }


    // ── Han Slots The Panel Into The Cockpit Toolbar ──────────────────────────────
    // The proper home for Han's navigation controls is the cockpit instrument bar —
    // a ToolBar with a separator-style ToolItem that hosts the composite widget.
    // We create the ToolItem first (so createControl() can set its width), then
    // fill the tool item with our composite control.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Fills a {@link ToolBar} parent by creating a separator-style
     * {@link ToolItem} and embedding our composite control inside it.
     * We must create the ToolItem first because {@link #createControl(Composite)}
     * needs the reference to set the item's width.
     *
     * @param parent  the tool bar to embed this item in
     * @param index   the position in the toolbar at which to insert the item
     */
    @Override
    public void fill( ToolBar parent, int index )
    {
        toolitem = new ToolItem( parent, SWT.SEPARATOR, index );
        Control control = createControl( parent );
        toolitem.setControl( control );
    }


    // ── Han Checks The Active Hyperspace Route ─────────────────────────────────────
    // "Which route are we jumping to?" — Han reads the currently dialled-in
    // connection from the navigation widget.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP connection currently selected in the embedded dropdown widget.
     * The schema page calls this to find out which server to show schema for.
     *
     * @return the selected browser connection, or null if none is selected
     */
    public IBrowserConnection getConnection()
    {
        return browserConnectionWidget.getBrowserConnection();
    }


    // ── Han Dials In A New Hyperspace Route ───────────────────────────────────────
    // The navigator calls out new coordinates and Han dials them in on the panel,
    // ready for the next jump.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Sets the selected connection in the embedded dropdown widget.
     * Used when the schema browser's input changes programmatically (e.g.
     * during navigation history restore) so the toolbar stays in sync.
     *
     * @param connection  the browser connection to select; may be null to clear
     */
    public void setConnection( IBrowserConnection connection )
    {
        browserConnectionWidget.setBrowserConnection( connection );
    }


    // ── Han Checks Whether The Hyperdrive Is Ready ────────────────────────────────
    // Han cannot jump to hyperspace if they are already at the destination or if
    // the default-schema switch is on — the panel is greyed out in those cases.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Enables or disables the connection dropdown based on the current schema mode.
     * When the user has switched to "Show Default Schema" the dropdown is disabled
     * because there is no server to pick from.
     *
     * <p>For example — Han checks if the hyperdrive is ready:</p>
     * <pre>
     *   updateEnabledState();
     *   // widget.setEnabled(true)  when live schema mode is on
     *   // widget.setEnabled(false) when default schema mode is on
     * </pre>
     */
    public void updateEnabledState()
    {
        browserConnectionWidget.setEnabled( !schemaPage.isShowDefaultSchema() );
    }


    // ── The Empire Updates The Hyperspace Route Data ───────────────────────────────
    // The Imperial nav beacon sends an update about one of the hyperspace routes —
    // Han checks if it is the route he is currently dialled in on, and if so
    // refreshes the widget to reflect the new data.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when any connection in the registry has been updated.
     * If the updated connection is the one currently selected in the dropdown,
     * we push it back into the widget to trigger a UI refresh.
     *
     * @param connection  the connection that was updated; may be null (we ignore null)
     */
    @Override
    public final void connectionUpdated( Connection connection )
    {
        if ( connection == null )
        {
            return;
        }

        IBrowserConnection selectedConnection = browserConnectionWidget.getBrowserConnection();
        if ( connection.equals( selectedConnection.getConnection() ) )
        {
            browserConnectionWidget.setBrowserConnection( browserConnectionWidget.getBrowserConnection() );
        }
    }


    // ── A New Hyperspace Route Becomes Available ───────────────────────────────────
    // A new route is added to the nav database — Han does not need to react
    // immediately; the widget will pick it up on the next refresh.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a new connection is added to the registry; no action needed here.
     *
     * @param connection  the newly added connection
     */
    @Override
    public void connectionAdded( Connection connection )
    {
        // Nothing to do
    }


    // ── A Hyperspace Route Is Removed ─────────────────────────────────────────────
    // One of the routes in the nav database is deleted — if it was the route
    // Han had dialled in, he needs to clear the controls so the ship does not
    // try to jump to a dead coordinate.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection is removed from the registry.
     * If the removed connection is the one currently selected, we clear the schema
     * browser input so the browser does not try to load schema from a gone connection.
     *
     * @param connection  the connection that was removed; may be null (we ignore null)
     */
    @Override
    public void connectionRemoved( Connection connection )
    {
        if ( connection == null )
        {
            return;
        }

        IBrowserConnection selectedConnection = browserConnectionWidget.getBrowserConnection();
        if ( connection.equals( selectedConnection.getConnection() ) )
        {
            schemaPage.getSchemaBrowser().setInput( new SchemaBrowserInput( null, null ) );
        }
    }


    // ── The Hyperdrive Engages ────────────────────────────────────────────────────
    // The ship makes the jump to hyperspace — Han notes it but has nothing to
    // update in the nav panel just because the connection opened.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection is opened; no action needed here.
     *
     * @param connection  the opened connection
     */
    @Override
    public void connectionOpened( Connection connection )
    {
        // Nothing to do
    }


    // ── The Hyperdrive Cuts Out ────────────────────────────────────────────────────
    // The ship drops out of hyperspace — Han notes it but has nothing to update
    // in the nav panel just because the connection closed.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection is closed; no action needed here.
     *
     * @param connection  the closed connection
     */
    @Override
    public void connectionClosed( Connection connection )
    {
        // Nothing to do
    }


    // ── A Route Group Is Reorganised ─────────────────────────────────────────────
    // The nav database reorganises a folder of routes — Han does not need to
    // react; the folder change does not affect the currently selected connection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection folder is modified; no action needed here.
     *
     * @param connectionFolder  the modified folder
     */
    @Override
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
        // Nothing to do
    }


    /**
     * Called when a connection folder is added; no action needed here.
     *
     * @param connectionFolder  the added folder
     */
    @Override
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
        // Nothing to do
    }


    /**
     * Called when a connection folder is removed; no action needed here.
     *
     * @param connectionFolder  the removed folder
     */
    @Override
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
        // Nothing to do
    }

}
