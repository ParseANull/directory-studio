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

package org.apache.directory.studio.ldapbrowser.common.dialogs;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.connection.ui.widgets.ConnectionActionGroup;
import org.apache.directory.studio.connection.ui.widgets.ConnectionConfiguration;
import org.apache.directory.studio.connection.ui.widgets.ConnectionUniversalListener;
import org.apache.directory.studio.connection.ui.widgets.ConnectionWidget;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;


// ── CLASS: SelectBrowserConnectionDialog — CHOOSING AN X-WING ON YAVIN IV ────
// The Rebel pilots line up in front of the hangar on Yavin IV — each X-wing is
// a different LDAP connection, with its own callsign (hostname), shield rating
// (TLS mode), and combat history.  The pilot in command walks down the row,
// picks one, climbs in, and that is the ship that flies the mission.  A
// double-click means "this one — go now"; single-click + OK means "confirm."
// If you walk away without choosing, no ship is assigned.
// This dialog shows the full list of configured LDAP connections, lets the user
// single-click or double-click to select one, and returns the result.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dialog that presents the list of configured LDAP connections and lets the
 * user pick one.  It embeds the same {@link ConnectionWidget} used in the main
 * connection view so the experience is familiar.  Double-clicking a connection
 * immediately confirms it (like double-clicking a file in a file-open dialog).
 * Think of this class as the Rebel hangar scene: pick your X-wing, then fly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SelectBrowserConnectionDialog extends Dialog
{
    /** The title */
    private String title;

    /** The initial browser connection */
    private IBrowserConnection initialBrowserConnection;

    /** The selected browser connection */
    private IBrowserConnection selectedBrowserConnection;

    /** The connection configuration */
    private ConnectionConfiguration connectionConfiguration;

    /** The connection universal listener */
    private ConnectionUniversalListener connectionUniversalListener;

    /** The connection action group */
    private ConnectionActionGroup connectionActionGroup;

    /** The connection main widget */
    private ConnectionWidget connectionMainWidget;


    // ── THE PILOT WALKS INTO THE HANGAR ──────────────────────────────────────
    // The pilot strides into the hangar, a mission briefing in hand.  She notes
    // which ship she was flying last time (the initial connection) so she can
    // walk straight to it — but she is free to choose a different one.
    // We capture the initial connection so it can be pre-selected in the list,
    // and initialise selectedBrowserConnection to null until the user confirms.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SelectBrowserConnectionDialog.  If an initial connection is
     * provided it will be pre-selected in the list so the user can confirm
     * quickly without scrolling.  The dialog is resizable so long connection
     * names are visible.
     *
     * <p>For example — the pilot walks in knowing her favourite ship:</p>
     * <pre>
     *   SelectBrowserConnectionDialog dialog = new SelectBrowserConnectionDialog(
     *       shell, "Select LDAP Connection", lastUsedConnection);
     *   if (dialog.open() == OK) {
     *       IBrowserConnection chosen = dialog.getSelectedBrowserConnection();
     *   }
     * </pre>
     *
     * @param parentShell              the shell that owns this dialog
     * @param title                    the dialog title shown in the title bar
     * @param initialBrowserConnection the connection to pre-select; may be {@code null}
     */
    public SelectBrowserConnectionDialog( Shell parentShell, String title, IBrowserConnection initialBrowserConnection )
    {
        super( parentShell );
        super.setShellStyle( super.getShellStyle() | SWT.RESIZE );
        this.title = title;
        this.initialBrowserConnection = initialBrowserConnection;
        this.selectedBrowserConnection = null;
    }


    // ── THE OFFICER LABELS THE HANGAR BAY ENTRANCE ───────────────────────────
    // A sign above the hangar door reads: "Select Connection — Yavin IV Flight
    // Pool."  Without the sign pilots would not know which bay they are in.
    // We set the dialog window title before the shell is shown.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Sets the dialog window title before the shell is shown.
     *
     * <p>For example — the sign above the hangar door:</p>
     * <pre>
     *   shell.setText("Select LDAP Connection");
     * </pre>
     *
     * @param shell  the shell Eclipse hands us to configure
     */
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
        shell.setText( title );
    }


    // ── THE GROUND CREW SECURES AND POWERS DOWN ALL SHIPS ────────────────────
    // After the pilot leaves the hangar the ground crew secures everything:
    // refuelling lines disconnected, action handlers removed, systems shut down.
    // We dispose the connection widget stack to avoid leaking listeners or
    // SWT resources when the dialog closes.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up the {@link ConnectionWidget} and related infrastructure when the
     * dialog closes.  We dispose in the reverse order of creation: deactivate
     * global action handlers, dispose action group, dispose universal listener,
     * dispose connection widget.
     *
     * <p>For example — the ground crew secures the hangar after the mission:</p>
     * <pre>
     *   connectionActionGroup.deactivateGlobalActionHandlers();
     *   connectionActionGroup.dispose();
     *   connectionUniversalListener.dispose();
     *   connectionMainWidget.dispose();
     * </pre>
     *
     * @return  {@code true} if the dialog was successfully closed
     */
    public boolean close()
    {
        if ( connectionMainWidget != null )
        {
            connectionConfiguration.dispose();
            connectionConfiguration = null;
            connectionActionGroup.deactivateGlobalActionHandlers();
            connectionActionGroup.dispose();
            connectionActionGroup = null;
            connectionUniversalListener.dispose();
            connectionUniversalListener = null;
            connectionMainWidget.dispose();
            connectionMainWidget = null;
        }
        return super.close();
    }


    // ── THE PILOT CLIMBS IN AND GIVES THE THUMBS-UP ──────────────────────────
    // The pilot settles into the cockpit of the selected X-wing, gives a
    // thumbs-up to the ground crew, and locks the canopy — she has chosen and
    // she is committed.
    // On OK we record the currently highlighted connection as the selection and
    // hand off to the superclass to close the dialog.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user presses OK.  We store the currently highlighted
     * connection as the chosen result so {@link #getSelectedBrowserConnection()}
     * can return it.
     *
     * <p>For example — the pilot gives the thumbs-up:</p>
     * <pre>
     *   selectedBrowserConnection = initialBrowserConnection;
     *   super.okPressed();
     * </pre>
     */
    protected void okPressed()
    {
        selectedBrowserConnection = initialBrowserConnection;
        super.okPressed();
    }


    // ── THE PILOT STANDS DOWN — NO SHIP TODAY ────────────────────────────────
    // The pilot changes her mind in the hangar and walks back out — no ship is
    // assigned, the mission is aborted.
    // On Cancel we clear selectedBrowserConnection so callers see null and
    // know no choice was made.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Called when the user cancels.  We explicitly clear the selected connection
     * so callers reliably see {@code null} and know to abort their operation.
     *
     * <p>For example — the pilot stands down without a ship:</p>
     * <pre>
     *   selectedBrowserConnection = null;
     *   super.cancelPressed();
     * </pre>
     */
    protected void cancelPressed()
    {
        selectedBrowserConnection = null;
        super.cancelPressed();
    }


    // ── THE GROUND OFFICER PREPARES LAUNCH AND ABORT ORDERS ──────────────────
    // Two orders are prepared: "Launch" (OK) and "Stand down" (Cancel).
    // Neither is the default — the pilot must make a deliberate choice.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates OK and Cancel buttons.  Neither is flagged as the default button
     * so the user must explicitly click one — this prevents an accidental Enter
     * from selecting whatever is highlighted.
     *
     * <p>For example — the ground officer prepares launch and abort orders:</p>
     * <pre>
     *   createButton(OK,     defaultButton=false);
     *   createButton(CANCEL, defaultButton=false);
     * </pre>
     *
     * @param parent  the button-bar composite Eclipse provides
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false );
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    // ── THE HANGAR DOORS OPEN AND THE SHIPS ROLL OUT ─────────────────────────
    // The hangar doors slide up and the X-wings roll into view — each one is a
    // connection in the list.  The pilot's favourite is already highlighted at
    // the front.  Single-click changes the highlight; double-click means "this
    // one — launch now."
    // We build the full ConnectionWidget stack with listeners for single-click
    // selection changes and double-click confirmation, and pre-scroll to and
    // select the initial connection if one was provided.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Builds the dialog content area: the full {@link ConnectionWidget} with its
     * toolbar, menus, and action group, plus selection and double-click listeners.
     * A single-click updates {@code initialBrowserConnection} (the tentative
     * selection); a double-click immediately confirms it by calling
     * {@link #okPressed()}.  If an initial connection was provided we scroll to
     * it and pre-select it.
     *
     * <p>For example — the hangar opens and highlights the pilot's ship:</p>
     * <pre>
     *   connectionMainWidget.setInput(connectionFolderManager);
     *   // single-click: update tentative selection
     *   // double-click: confirm and close
     *   viewer.reveal(initialConnection);
     *   viewer.setSelection(new StructuredSelection(initialConnection), true);
     * </pre>
     *
     * @param parent  the parent composite Eclipse provides
     * @return        the completed content area control
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = ( Composite ) super.createDialogArea( parent );
        GridLayout gl = new GridLayout();
        composite.setLayout( gl );
        GridData gd = new GridData( GridData.FILL_BOTH );
        gd.widthHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH );
        gd.heightHint = convertHorizontalDLUsToPixels( IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH / 2 );
        composite.setLayoutData( gd );

        // create configuration
        connectionConfiguration = new ConnectionConfiguration();

        // create main widget
        connectionMainWidget = new ConnectionWidget( connectionConfiguration, null );
        connectionMainWidget.createWidget( composite );
        connectionMainWidget.setInput( ConnectionCorePlugin.getDefault().getConnectionFolderManager() );

        // create actions and context menu (and register global actions)
        connectionActionGroup = new ConnectionActionGroup( connectionMainWidget, connectionConfiguration );
        connectionActionGroup.fillToolBar( connectionMainWidget.getToolBarManager() );
        connectionActionGroup.fillMenu( connectionMainWidget.getMenuManager() );
        connectionActionGroup.fillContextMenu( connectionMainWidget.getContextMenuManager() );
        connectionActionGroup.activateGlobalActionHandlers();

        // create the listener
        connectionUniversalListener = new ConnectionUniversalListener( connectionMainWidget.getViewer() );

        connectionMainWidget.getViewer().addSelectionChangedListener( new ISelectionChangedListener()
        {
            public void selectionChanged( SelectionChangedEvent event )
            {
                if ( !event.getSelection().isEmpty() )
                {
                    Object o = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();
                    if ( o instanceof Connection )
                    {
                        Connection connection = ( Connection ) o;
                        IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                            .getBrowserConnection( connection );
                        initialBrowserConnection = browserConnection;
                    }
                }
            }
        } );

        connectionMainWidget.getViewer().addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                if ( !event.getSelection().isEmpty() )
                {
                    Object o = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();
                    if ( o instanceof Connection )
                    {
                        Connection connection = ( Connection ) o;
                        IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
                            .getBrowserConnection( connection );
                        initialBrowserConnection = browserConnection;
                        okPressed();
                    }
                }
            }
        } );

        if ( initialBrowserConnection != null )
        {
            Connection connection = initialBrowserConnection.getConnection();
            if ( connection != null )
            {
                connectionMainWidget.getViewer().reveal( connection );
                connectionMainWidget.getViewer().setSelection( new StructuredSelection( connection ), true );
            }
        }

        applyDialogFont( composite );

        connectionMainWidget.setFocus();

        return composite;
    }


    // ── MISSION CONTROL ASKS: WHICH SHIP WAS CHOSEN? ─────────────────────────
    // After the dialog closes mission control asks: "Which X-wing is flying this
    // mission?"  If the pilot walked away without choosing, the answer is null —
    // the mission is aborted.
    // Callers retrieve the chosen browser connection here after the dialog closes.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IBrowserConnection} the user selected, or {@code null}
     * if the dialog was cancelled.  Call this after the dialog closes.
     *
     * <p>For example — mission control checks the selected ship:</p>
     * <pre>
     *   IBrowserConnection conn = dialog.getSelectedBrowserConnection();
     *   if (conn != null) { performOperation(conn); }
     * </pre>
     *
     * @return  the selected connection, or {@code null} if the user cancelled
     */
    public IBrowserConnection getSelectedBrowserConnection()
    {
        return selectedBrowserConnection;
    }
}
