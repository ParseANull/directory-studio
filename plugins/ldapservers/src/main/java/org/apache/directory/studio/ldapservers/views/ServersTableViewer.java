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


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.studio.ldapservers.LdapServersManager;
import org.apache.directory.studio.ldapservers.LdapServersManagerListener;
import org.apache.directory.studio.ldapservers.model.LdapServer;
import org.apache.directory.studio.ldapservers.model.LdapServerEvent;
import org.apache.directory.studio.ldapservers.model.LdapServerEventType;
import org.apache.directory.studio.ldapservers.model.LdapServerListener;
import org.apache.directory.studio.ldapservers.model.LdapServerStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;


// ── CLASS: ServersTableViewer — LANDO'S ANIMATED FACILITY DISPLAY BOARD ──────────────────
// Lando's control room has a live animated display: each facility row shows a blinking
// status icon and cycling dots text while it's starting up or shutting down.
// When a facility's state changes, the board updates its row in real time.
// This class is that animated board: a JFace TreeViewer that listens to both the server list
// (for add/remove) and each individual server's status events (for animation), and drives
// a timed animation thread for in-progress states.
// ─────────────────────────────────────────────────────────────────────────────────────────────
/**
 * A JFace {@link TreeViewer} that displays the list of LDAP server instances with live
 * animated status updates.
 * Registers a {@link LdapServersManagerListener} for list-level changes and a
 * {@link LdapServerListener} on each server for per-server status/rename events.
 * Drives a 200 ms animation thread (via {@code Display.timerExec}) for servers in
 * STARTING, STOPPING, or REPAIRING state.
 * Think of it as Lando's animated facility display board.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServersTableViewer extends TreeViewer
{
    /** The root element */
    protected static final String ROOT = "root"; //$NON-NLS-1$

    /** The label provider */
    private ServersViewLabelProvider labelProvider;

    /** The comparator */
    private ServersComparator comparator;

    /** The server handler listener */
    private LdapServersManagerListener serversHandlerListener;

    /** The server listener */
    private LdapServerListener serverListener;

    /** A flag to stop the animation */
    private boolean stopAnimation;

    /** The list of server needing animation */
    private List<LdapServer> serversNeedingAnimation = new ArrayList<LdapServer>();


    // ── Setting Up The Display Board ──────────────────────────────────────────────────────────
    // The board wires itself to the label provider, content provider, and comparator, then
    // loads the initial server list and hooks up all the live-update listeners.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the viewer, attaches a {@link ServersViewLabelProvider}, {@link ServersViewContentProvider},
     * and an alphabetic/status comparator, loads the initial server list, and calls
     * {@link #addListeners()} to register all live-update hooks.
     *
     * @param tree  the SWT Tree widget to render the servers in
     */
    public ServersTableViewer( Tree tree )
    {
        super( tree );

        labelProvider = new ServersViewLabelProvider();
        setLabelProvider( labelProvider );
        setContentProvider( new ServersViewContentProvider() );

        comparator = new ServersComparator();
        setComparator( new ViewerComparator( comparator ) );

        setInput( ROOT );

        addListeners();
    }


    // ── Wiring Up All The Live-Update Listeners ───────────────────────────────────────────────
    // Two listener families: one watches the server list for add/remove, one watches each
    // server's own events (status change, rename) so individual rows update immediately.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Registers two listener chains:
     * <ol>
     *   <li>A {@link LdapServersManagerListener} that adds/removes rows as servers appear
     *       or disappear from the global list.</li>
     *   <li>A {@link LdapServerListener} attached to every existing server that triggers
     *       per-row refresh and animation start/stop on status and name changes.</li>
     * </ol>
     */
    private void addListeners()
    {
        // The server handler listener
        serversHandlerListener = new LdapServersManagerListener()
        {
            public void serverAdded( LdapServer server )
            {
                addServer( server );
                server.addListener( serverListener );
            }


            public void serverRemoved( LdapServer server )
            {
                refreshServer( server );
            }


            public void serverUpdated( LdapServer server )
            {
                removeServer( server );
                server.removeListener( serverListener );

            }
        };

        // Adding the listener to the servers handler
        LdapServersManager.getDefault().addListener( serversHandlerListener );

        // The server listener
        serverListener = new LdapServerListener()
        {
            public void serverChanged( LdapServerEvent event )
            {
                // Checking if the event is null
                if ( event == null )
                {
                    return;
                }

                // Getting the kind of event and the associated server
                LdapServerEventType kind = event.getKind();
                LdapServer server = event.getServer();
                switch ( kind )
                {
                // The server status has changed
                    case STATUS_CHANGED:
                        // First, we refresh the server
                        refreshServer( server );

                        // Then, we get the status of the server to see if we
                        // need to start or stop the animation thread
                        LdapServerStatus state = server.getStatus();

                        // If the state is STARTING or STOPPING, we need to
                        // add the server to the list of servers needing
                        // animation and eventually start the animation thread
                        if ( ( state == LdapServerStatus.STARTING ) || ( state == LdapServerStatus.STOPPING )
                            || ( state == LdapServerStatus.REPAIRING ) )
                        {
                            boolean startAnimationThread = false;

                            synchronized ( serversNeedingAnimation )
                            {
                                if ( !serversNeedingAnimation.contains( server ) )
                                {
                                    if ( serversNeedingAnimation.isEmpty() )
                                        startAnimationThread = true;
                                    serversNeedingAnimation.add( server );
                                }
                            }

                            if ( startAnimationThread )
                            {
                                startAnimationThread();
                            }
                        }

                        // If the state is *not* STARTING or STOPPING, we need
                        // to remove the server from the list of servers
                        // needing animation and eventually stop the animation
                        // if this list is empty
                        else
                        {
                            boolean stopAnimationThread = false;

                            synchronized ( serversNeedingAnimation )
                            {
                                if ( serversNeedingAnimation.contains( server ) )
                                {
                                    serversNeedingAnimation.remove( server );
                                    if ( serversNeedingAnimation.isEmpty() )
                                        stopAnimationThread = true;
                                }
                            }

                            if ( stopAnimationThread )
                            {
                                stopAnimationThread();
                            }
                        }
                        break;
                    // The server has been renamed
                    case RENAMED:
                        // We simply refresh the server
                        refreshServer( server );
                        break;
                }

            }
        };

        // Adding the listener to the servers
        for ( LdapServer server : LdapServersManager.getDefault().getServersList() )
        {
            server.addListener( serverListener );
        }
    }


    // ── Adding A New Row To The Status Board ─────────────────────────────────────────────────
    // When a new server is registered, its row appears on the board.
    // Done asynchronously on the display thread so SWT doesn't complain.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Adds a new server row to the viewer asynchronously.
     *
     * @param server  the newly added server
     */
    private void addServer( final LdapServer server )
    {
        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {
                add( ROOT, server );
            }
        } );
    }


    // ── Refreshing A Single Row On The Status Board ───────────────────────────────────────────
    // When a server's state or name changes, we refresh just its row and restore the selection.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Refreshes the row for the given server asynchronously, preserving the current selection.
     *
     * @param server  the server whose row needs refreshing
     */
    private void refreshServer( final LdapServer server )
    {
        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {
                refresh( server );
                ISelection sel = ServersTableViewer.this.getSelection();
                ServersTableViewer.this.setSelection( sel );
            }
        } );
    }


    // ── Removing A Row From The Status Board ──────────────────────────────────────────────────
    // When a server is deleted, its row disappears from the board.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes the given server's row from the viewer asynchronously.
     *
     * @param server  the server whose row should be removed
     */
    private void removeServer( final LdapServer server )
    {
        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {
                remove( server );
            }
        } );
    }


    // ── Starting The Animation Thread ─────────────────────────────────────────────────────────
    // When the first server enters an in-progress state, we start a 200 ms timer loop that
    // advances the animation frame and repaints the affected rows.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Starts the 200 ms animation timer loop.
     * Each tick calls {@link ServersViewLabelProvider#animate()} to advance the frame counter,
     * then repaints each in-progress server's row via {@link #updateAnimation(LdapServer)}.
     * The loop self-reschedules via {@code Display.timerExec} until {@link #stopAnimationThread()}
     * is called.
     */
    private void startAnimationThread()
    {
        stopAnimation = false;

        final Display display = getTree().getDisplay();
        final int SLEEP = 200;
        final Runnable[] animatorThread = new Runnable[1];
        animatorThread[0] = new Runnable()
        {
            public void run()
            {
                // Checking if we need to stop the animation
                if ( !stopAnimation )
                {
                    // Changing the animation state on the label provider
                    labelProvider.animate();

                    // Looping on the currently starting servers
                    for ( LdapServer server : serversNeedingAnimation.toArray( new LdapServer[0] ) )
                    {
                        if ( server != null && getTree() != null && !getTree().isDisposed() )
                        {
                            updateAnimation( server );
                        }
                    }

                    // Re-launching the animation
                    display.timerExec( SLEEP, animatorThread[0] );
                }
            }
        };

        // Launching the animation asynchronously
        Display.getDefault().asyncExec( new Runnable()
        {
            public void run()
            {
                display.timerExec( SLEEP, animatorThread[0] );
            }
        } );
    }


    // ── Stopping The Animation Thread ─────────────────────────────────────────────────────────
    // When the last in-progress server reaches a stable state, the animation loop notices the
    // flag and exits on its next tick.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Signals the animation loop to stop at its next tick.
     * Called when {@code serversNeedingAnimation} becomes empty.
     */
    private void stopAnimationThread()
    {
        stopAnimation = true;
    }


    // ── Repainting A Single Row's Animation Frame ─────────────────────────────────────────────
    // For each in-progress server, we directly set the TreeItem's column-1 text and image to
    // the current animation frame — bypassing a full refresh for efficiency.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the animated status text and icon for a single server's TreeItem.
     * Calls the label provider's column text/image methods for column 1 and applies
     * them directly to the SWT {@link TreeItem} for the given server.
     *
     * @param server  the in-progress server whose row needs its animation frame updated
     */
    private void updateAnimation( LdapServer server )
    {
        Widget widget = doFindItem( server );
        TreeItem item = ( TreeItem ) widget;
        item.setText( 1, labelProvider.getColumnText( server, 1 ) );
        item.setImage( 1, labelProvider.getColumnImage( server, 1 ) );
    }


    // ── Sorting The Status Board By Column ────────────────────────────────────────────────────
    // Clicking a column header sorts the list by that column (ascending then descending).
    // We update the comparator, refresh the viewer, and update the sort arrow.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Sorts the viewer by the given column.
     * If the same column is clicked again, reverses the sort direction.
     * Updates the Tree's sort column and direction arrow accordingly.
     *
     * @param treeColumn  the header column that was clicked
     * @param column      the column index (0 = Name, 1 = State)
     */
    public void sort( final TreeColumn treeColumn, int column )
    {
        if ( column == comparator.column )
        {
            comparator.reverseOrdering();
        }
        else
        {
            comparator.column = column;
        }

        PlatformUI.getWorkbench().getDisplay().asyncExec( new Runnable()
        {
            public void run()
            {
                refresh();

                Tree tree = getTree();
                tree.setSortColumn( treeColumn );
                if ( comparator.order == ServersComparator.ASCENDING )
                {
                    tree.setSortDirection( SWT.UP );
                }
                else
                {
                    tree.setSortDirection( SWT.DOWN );
                }
            }
        } );
    }

    // ── CLASS: ServersComparator — THE STATUS BOARD'S SORT ORDER ────────────────────────────
    // The status board can sort rows alphabetically by name or by status text, ascending or
    // descending.  This comparator drives that sort order.
    // ────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two servers by their label-provider text for the current sort column,
     * respecting the current sort direction (ascending or descending).
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     */
    private class ServersComparator implements Comparator<Object>
    {
        public static final int ASCENDING = 1;

        /** The comparison order */
        int order = ASCENDING;

        /** The column used to compare objects */
        int column = 0;


        // ── Flipping The Sort Arrow ───────────────────────────────────────────────────────────
        // Clicking the same column header a second time reverses the sort direction.
        // ────────────────────────────────────────────────────────────────────────────────────
        /**
         * Reverses the current sort direction (ascending → descending or vice versa).
         */
        public void reverseOrdering()
        {
            order = order * -1;
        }


        // ── Comparing Two Rows ────────────────────────────────────────────────────────────────
        // We delegate to the label provider to get the display text, then do a case-insensitive
        // string comparison, multiplied by the current order to flip ascending/descending.
        // ────────────────────────────────────────────────────────────────────────────────────
        /**
         * Compares two objects by their column text, case-insensitively, in the current sort direction.
         *
         * @param o1  first element
         * @param o2  second element
         * @return negative, zero, or positive as per {@link Comparator#compare}
         */
        public int compare( Object o1, Object o2 )
        {
            String s1 = labelProvider.getColumnText( o1, column );
            String s2 = labelProvider.getColumnText( o2, column );

            return s1.compareToIgnoreCase( s2 ) * order;
        }
    }
}
