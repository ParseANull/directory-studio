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
import org.apache.directory.studio.connection.core.jobs.OpenConnectionsRunnable;
import org.apache.directory.studio.connection.ui.actions.SelectionUtils;
import org.apache.directory.studio.connection.ui.widgets.ConnectionUniversalListener;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.ui.views.browser.BrowserView;
import org.apache.directory.studio.ldapbrowser.ui.views.modificationlogs.ModificationLogsView;
import org.apache.directory.studio.ldapbrowser.ui.views.searchlogs.SearchLogsView;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ConnectionViewUniversalListener — OBI-WAN SENSES A DISTURBANCE ───
// Obi-Wan closes his eyes on the Falcon and senses every ripple in the Force —
// a planet destroyed, a friend in danger — and reacts without being told.
// This class is that presence: it watches the workbench for selection changes
// and double-clicks in the connection view, then responds by opening companion
// views or toggling the connection state.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Listens to all workbench events relevant to the connection view and drives
 * the appropriate UI reactions — opening the browser view on selection,
 * toggling connections on double-click.
 * It extends {@link ConnectionUniversalListener} and adds LDAP-browser-specific
 * behavior like auto-showing the browser, search-logs, and modification-logs views.
 * Think of this class as Obi-Wan's Force sensitivity — every meaningful event
 * in the workbench is felt and acted upon without explicit wiring from callers.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionViewUniversalListener extends ConnectionUniversalListener
{

    /** The connection view */
    protected ConnectionView view;

    /** This listener is used to ensure that the browser view is opened
     when an object in the connection view is selected */
    private ISelectionChangedListener viewerSelectionListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            Connection[] connections = SelectionUtils.getConnections( event.getSelection() );
            if ( connections.length == 1 )
            {
                ensureViewVisible();
            }
        }
    };

    /** This listener opens/closes a connection when double clicking a connection */
    private IDoubleClickListener viewerDoubleClickListener = new IDoubleClickListener()
    {
        public void doubleClick( DoubleClickEvent event )
        {
            Connection[] connections = SelectionUtils.getConnections( event.getSelection() );
            if ( connections.length == 1 )
            {
                toggleConnection( connections[0] );
            }
        }
    };


    // ── Constructor: Obi-Wan Opens Himself to the Force ──────────────────────
    // Aboard the Millennium Falcon, Obi-Wan meditates and opens his mind to
    // the Force — registering his sensitivity to any disturbance, any shift.
    // We register the two inner listeners on the viewer so we'll be notified
    // on every selection change and every double-click in the connection list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates this listener and registers it with the connection viewer.
     * We attach both the selection-change listener (to auto-show companion views)
     * and the double-click listener (to toggle open/closed state) to the viewer.
     *
     * <p>For example — Obi-Wan tunes into every tremor in the Force:</p>
     * <pre>
     *   viewer.addSelectionChangedListener( viewerSelectionListener );
     *   viewer.addDoubleClickListener( viewerDoubleClickListener );
     *   // Now any selection or double-click flows through to our handlers
     * </pre>
     *
     * @param view  the connection view whose viewer we will listen to
     */
    public ConnectionViewUniversalListener( ConnectionView view )
    {
        super( view.getMainWidget().getViewer() );
        this.view = view;

        // listeners
        viewer.addSelectionChangedListener( viewerSelectionListener );
        viewer.addDoubleClickListener( viewerDoubleClickListener );
    }


    // ── dispose: Obi-Wan Releases His Connection to the Force ────────────────
    // At the moment Vader strikes him down, Obi-Wan releases his physical form
    // and his Force connection fades — no longer sensing, no longer reacting.
    // We null out the view reference and delegate to super which removes the
    // base listeners, severing all event hooks.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Cleans up this listener and releases the view reference.
     * We null the view here so that any late-arriving events don't act on
     * a disposed view; {@code super.dispose()} removes the base listeners.
     *
     * <p>For example — Obi-Wan's Force presence fades as Vader strikes:</p>
     * <pre>
     *   this.view = null;   // no longer reacting to events
     *   super.dispose();    // base listeners unregistered
     * </pre>
     */
    @Override
    public void dispose()
    {
        this.view = null;
        super.dispose();
    }


    // ── ensureViewVisible: Obi-Wan Nudges His Allies Into Position ───────────
    // When Obi-Wan senses the right moment, he reaches out through the Force
    // and nudges Luke, Han, and Leia into position — not forcing anything, just
    // making sure the right people are where they need to be.
    // We show the browser, modification-logs, and search-logs views so they're
    // ready when the user selects a connection.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Ensures that the browser, modification-logs, and search-logs views are
     * open and visible when a connection is selected in our view.
     * We use {@code VIEW_VISIBLE} for the browser (bring to front if already open)
     * and {@code VIEW_CREATE} for the logs (create if missing, but don't steal focus).
     * Exceptions from {@code showView} are swallowed because missing views are
     * non-fatal — we just won't show them.
     *
     * <p>For example — Obi-Wan positions the Rebel fleet before the battle:</p>
     * <pre>
     *   page.showView( BrowserView.getId(), null, VIEW_VISIBLE );      // front and center
     *   page.showView( ModificationLogsView.getId(), null, VIEW_CREATE ); // ready but quiet
     *   page.showView( SearchLogsView.getId(), null, VIEW_CREATE );    // ready but quiet
     * </pre>
     */
    private void ensureViewVisible()
    {
        if ( view != null )
        {
            try
            {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( BrowserView.getId(),
                    null, IWorkbenchPage.VIEW_VISIBLE );
            }
            catch ( PartInitException e )
            {
            }
            catch ( NullPointerException e )
            {
            }

            try
            {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                    ModificationLogsView.getId(), null, IWorkbenchPage.VIEW_CREATE );
            }
            catch ( PartInitException e )
            {
            }
            catch ( NullPointerException e )
            {
            }

            try
            {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView( SearchLogsView.getId(),
                    null, IWorkbenchPage.VIEW_CREATE );
            }
            catch ( PartInitException e )
            {
            }
            catch ( NullPointerException e )
            {
            }
        }
    }


    // ── toggleConnection: Obi-Wan Switches the Tractor Beam ─────────────────
    // In the Death Star, Obi-Wan walks to the control panel and flips the
    // switch — if the beam is on, he turns it off; if off, the Falcon is free.
    // We check if the connection is currently open and either disconnect it
    // or kick off an async open job, toggling its state cleanly.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Toggles the open/closed state of the given connection on double-click.
     * If the connection is already open we disconnect synchronously; if it's
     * closed we fire an async {@link OpenConnectionsRunnable} job.
     *
     * <p>For example — Obi-Wan flips the tractor beam switch:</p>
     * <pre>
     *   if ( connection.isConnected() ) {
     *       connection.disconnect(); // beam off, Falcon escapes
     *   } else {
     *       new StudioBrowserJob( new OpenConnectionsRunnable(...) ).execute();
     *   }
     * </pre>
     *
     * @param connection  the connection to toggle; must not be null
     */
    private void toggleConnection( Connection connection )
    {
        if ( connection.getConnectionWrapper().isConnected() )
        {
            connection.getConnectionWrapper().disconnect();
        }
        else
        {
            new StudioBrowserJob( new OpenConnectionsRunnable( connection ) ).execute();
        }
    }

}
