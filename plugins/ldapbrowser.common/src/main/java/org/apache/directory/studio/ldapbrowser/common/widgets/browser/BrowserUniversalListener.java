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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent.EventDetail;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: BrowserUniversalListener — Obi-Wan Senses the Force ───────────────
// In A New Hope, Obi-Wan Kenobi reaches out through the Force and feels every
// tremor in the galaxy: the Alderaan system's destruction, Luke's distress,
// distant changes he's never seen directly. He doesn't act on everything —
// only on the signals that matter — but he's always listening.
// BrowserUniversalListener is that same universal awareness: it registers with
// every event registry (connections, entries, searches) and decides how to
// refresh the browser tree in response to each disturbance.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The central event hub for the browser widget. It listens for connection
 * changes, entry modifications and search updates — three completely separate
 * event channels — and translates each one into a tree-viewer refresh or
 * selection change.
 * Without this class the browser tree would be static: you'd have to manually
 * reload to see that a new connection was added or an entry was modified.
 * Think of this class as Obi-Wan: always sensing, selectively reacting.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserUniversalListener implements ConnectionUpdateListener, EntryUpdateListener, SearchUpdateListener
{
    /** The browser widget */
    protected BrowserWidget widget;

    /** The tree viewer */
    protected TreeViewer viewer;

    /** The tree viewer listener */
    private ITreeViewerListener treeViewerListener = new ITreeViewerListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation checks if the collapsed entry more children
         * than currently fetched. If this is the case cached children are
         * cleared an must be fetched newly when expanding the tree.
         *
         * This could happen when first using a search that returns
         * only some of an entry's children.
         */
        public void treeCollapsed( TreeExpansionEvent event )
        {
            if ( event.getElement() instanceof IEntry )
            {
                IEntry entry = ( IEntry ) event.getElement();
                if ( entry.isChildrenInitialized() && entry.hasMoreChildren()
                    && entry.getChildrenCount() < entry.getBrowserConnection().getCountLimit() )
                {
                    entry.setChildrenInitialized( false );
                }
            }
        }


        /**
         * {@inheritDoc}
         */
        public void treeExpanded( TreeExpansionEvent event )
        {
        }
    };

    /** The double click listener. */
    private IDoubleClickListener doubleClickListener = new IDoubleClickListener()
    {

        public void doubleClick( DoubleClickEvent event )
        {
            if ( event.getSelection() instanceof IStructuredSelection )
            {
                Object obj = ( ( IStructuredSelection ) event.getSelection() ).getFirstElement();
                if ( viewer.getExpandedState( obj ) )
                {
                    viewer.collapseToLevel( obj, 1 );
                }
                else if ( ( ( ITreeContentProvider ) viewer.getContentProvider() ).hasChildren( obj ) )
                {
                    viewer.expandToLevel( obj, 1 );
                }
            }
        }

    };


    // ── OBI-WAN OPENS HIMSELF TO THE FORCE ────────────────────────────────────
    // In the Mos Eisley cantina, Obi-Wan reaches out and attunes himself to the
    // ripples around him: new arrivals, departures, disturbances from afar.
    // Here we register with every event registry the browser cares about —
    // connections, entries, searches — so we receive every relevant notification
    // and can keep the tree viewer in sync without anyone having to ask us to.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new universal listener and immediately wires it into all
     * three event channels: connection updates, entry updates, and search
     * updates.
     * We also attach a tree-collapse listener (to reset partially loaded
     * children) and a double-click listener (to toggle expand/collapse) directly
     * on the viewer.
     *
     * <p>For example — Obi-Wan attunes himself in the cantina:</p>
     * <pre>
     *   viewer.addTreeListener(this);          // sense collapses
     *   viewer.addDoubleClickListener(this);   // sense double-clicks
     *   ConnectionEventRegistry.addListener(this);   // sense connection events
     *   EventRegistry.addEntryUpdateListener(this);  // sense entry changes
     *   EventRegistry.addSearchUpdateListener(this); // sense search results
     * </pre>
     *
     * @param widget  The {@link BrowserWidget} whose tree viewer we'll manage.
     *                We keep a reference so event handlers can reach the widget
     *                if needed.
     */
    public BrowserUniversalListener( BrowserWidget widget )
    {
        this.widget = widget;
        this.viewer = widget.getViewer();

        viewer.addTreeListener( treeViewerListener );
        viewer.addDoubleClickListener( doubleClickListener );

        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionUIPlugin.getDefault().getEventRunner() );
        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addSearchUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
    }


    // ── OBI-WAN BECOMES ONE WITH THE FORCE ────────────────────────────────────
    // When Vader strikes Obi-Wan down he doesn't vanish — he dissolves his
    // connection to the physical world deliberately. "If you strike me down I
    // shall become more powerful than you can possibly imagine." Here, when the
    // widget is closed, we do the same: gracefully deregister from every event
    // channel so we stop receiving notifications we can no longer handle.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Detaches this listener from all event channels and releases the viewer
     * reference. Call this when the browser widget is being disposed, otherwise
     * we'll keep receiving events and trying to update a destroyed tree.
     * We null out the viewer after deregistration so any late-arriving events
     * that slip through won't cause a NullPointerException.
     */
    public void dispose()
    {
        if ( viewer != null )
        {
            viewer.removeTreeListener( treeViewerListener );
            viewer.removeDoubleClickListener( doubleClickListener );

            ConnectionEventRegistry.removeConnectionUpdateListener( this );
            EventRegistry.removeEntryUpdateListener( this );
            EventRegistry.removeSearchUpdateListener( this );

            viewer = null;
        }
    }


    // ── OBI-WAN SENSES A NEW PRESENCE ARRIVE ─────────────────────────────────
    // "I feel a new presence in the Force — someone has opened a connection."
    // When an LDAP connection is successfully opened, the tree now has a new
    // root to display, so we refresh the entire viewer to show it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a connection being opened by doing a full tree refresh.
     * The opened connection's root entry is now accessible, so the tree
     * needs to update to show it.
     *
     * @param connection  The connection that was just opened.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionOpened(Connection)
     */
    public void connectionOpened( Connection connection )
    {
        viewer.refresh();
    }


    // ── OBI-WAN SENSES A PRESENCE FADE ───────────────────────────────────────
    // Obi-Wan felt Alderaan vanish from the Force: "I felt a great disturbance
    // in the Force, as if millions of voices suddenly cried out in terror and
    // were suddenly silenced." A closed connection can no longer expand its tree,
    // so we collapse everything to avoid showing stale data.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a connection being closed by collapsing the entire tree.
     * There's no point showing expanded entries when we can't connect to the
     * server anymore — collapse them all so the user gets a clean slate when
     * they reconnect.
     *
     * @param connection  The connection that was just closed.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionClosed(Connection)
     */
    public void connectionClosed( Connection connection )
    {
        viewer.collapseAll();
    }


    // ── OBI-WAN SENSES A RIPPLE IN THE FORCE ─────────────────────────────────
    // Something changed — a connection's hostname, credentials, or display name.
    // Obi-Wan doesn't know exactly what shifted, so he takes in the whole
    // situation again. We refresh the tree so the label provider can pick up
    // whatever changed in the connection's metadata.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a connection being modified (e.g. renamed or its settings
     * changed) by refreshing the tree so the new details are shown.
     *
     * @param connection  The connection whose properties were updated.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionUpdated(Connection)
     */
    public void connectionUpdated( Connection connection )
    {
        viewer.refresh();
    }


    // ── OBI-WAN SENSES A NEW SPIRIT JOIN ─────────────────────────────────────
    // A new connection entry appears in the Force — someone added an LDAP
    // connection to the system. Obi-Wan refreshes his awareness of the
    // surroundings so this newcomer shows up in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a connection being added to the connection manager by
     * refreshing the tree so the new connection appears.
     *
     * @param connection  The newly added connection.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionAdded(Connection)
     */
    public void connectionAdded( Connection connection )
    {
        viewer.refresh();
    }


    // ── OBI-WAN SENSES A SPIRIT DEPART ───────────────────────────────────────
    // A connection is gone from the Force — deleted from the manager. Obi-Wan
    // refreshes his sense of things so the departed connection's row disappears
    // from the tree instead of lingering as a ghost.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a connection being removed from the connection manager by
     * refreshing the tree so the deleted connection's row disappears.
     *
     * @param connection  The connection that was removed.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionRemoved(Connection)
     */
    public void connectionRemoved( Connection connection )
    {
        viewer.refresh();
    }


    // ── OBI-WAN SENSES A DISTANT FOLDER SHIFT — AND HOLDS STILL ─────────────
    // A connection folder was renamed or reordered. Obi-Wan feels it, far away
    // in the Force, but knows it won't affect the browser tree — folders are
    // a connection-manager concept, not an LDAP tree concept. He does nothing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection folder's properties change. We don't need to
     * react because the browser tree doesn't display connection folders —
     * they're managed in a separate connection view.
     *
     * @param connectionFolder  The modified connection folder.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionFolderModified(ConnectionFolder)
     */
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
    }


    // ── OBI-WAN SENSES A FOLDER APPEAR — AND HOLDS STILL ─────────────────────
    // A new connection folder was created. Obi-Wan is aware of it but, again,
    // folders live in the connection manager, not in the LDAP browser tree.
    // No action needed here.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a new connection folder is added to the connection manager.
     * No action needed — folder structure doesn't affect the LDAP browser tree.
     *
     * @param connectionFolder  The newly created connection folder.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionFolderAdded(ConnectionFolder)
     */
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
    }


    // ── OBI-WAN SENSES A FOLDER VANISH — AND HOLDS STILL ─────────────────────
    // A connection folder was deleted. Obi-Wan notes it and moves on — the
    // browser tree doesn't show folders, so nothing here needs to change.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called when a connection folder is removed from the connection manager.
     * No action needed — folder structure doesn't affect the LDAP browser tree.
     *
     * @param connectionFolder  The removed connection folder.
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionFolderRemoved(ConnectionFolder)
     */
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
    }


    // ── OBI-WAN FEELS AN ENTRY SHIFT IN THE FORCE ────────────────────────────
    // "I felt a great disturbance" — an LDAP entry was modified. Obi-Wan pays
    // careful attention: if it's just attributes being loaded (AttributesInitializedEvent)
    // he knows not to react yet, because a second job (InitializeChildrenJob) is
    // still coming and a premature refresh would kick off a third round-trip.
    // For children being loaded he collapses then re-expands the node cleanly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Responds to an entry being modified in the model by refreshing the
     * relevant part of the tree viewer.
     * We deliberately ignore {@link AttributesInitializedEvent} (unless it's
     * the RootDSE) to avoid a race condition where a double-click spawns two
     * parallel jobs — if we refresh on the first job completing we'd trigger a
     * third job before the second finishes, causing a duplicate fetch.
     * For {@link ChildrenInitializedEvent} we collapse-then-expand to force
     * the viewer to rebuild its child list from scratch.
     *
     * <p>For example — Obi-Wan reacts to a disturbance:</p>
     * <pre>
     *   if (event is AttributesInitialized and not RootDSE) return; // too early
     *   if (event is ChildrenInitialized) {
     *       collapseToLevel(entry, ALL);
     *       if (wasExpanded) expandToLevel(entry, 1);
     *   }
     *   viewer.refresh(entry);
     * </pre>
     *
     * @param event  The entry modification event carrying the affected entry
     *               and the type of change.
     */
    public void entryUpdated( EntryModificationEvent event )
    {
        // Don't handle attribute initalization, could cause double
        // retrieval of children.
        //
        // When double-clicking an entry two Jobs/Threads are started:
        // - InitializeAttributesJob and
        // - InitializeChildrenJob
        // If the InitializeAttributesJob is finished first the
        // AttributesInitializedEvent is fired. If this causes
        // a refresh of the tree before the children are initialized
        // another InitializeChildrenJob is executed.
        if ( event instanceof AttributesInitializedEvent && !( event.getModifiedEntry() instanceof IRootDSE ) )
        {
            return;
        }

        if ( event instanceof ChildrenInitializedEvent )
        {
            boolean expandedState = viewer.getExpandedState( event.getModifiedEntry() );
            viewer.collapseToLevel( event.getModifiedEntry(), TreeViewer.ALL_LEVELS );
            if ( expandedState )
            {
                viewer.expandToLevel( event.getModifiedEntry(), 1 );
            }
            viewer.refresh( event.getModifiedEntry(), true );
        }
        else
        {
            viewer.refresh( event.getModifiedEntry(), true );
        }
    }


    // ── OBI-WAN SENSES A SEARCH MISSION COMPLETE ─────────────────────────────
    // A search has finished — results are in. Obi-Wan refreshes his overview of
    // the battlefield. If it was a quick search being removed, he makes sure the
    // quick-search reference is cleaned up. If it was a quick search being added
    // or updated, he selects it in the tree and expands it so the results are
    // immediately visible. For everything else, a plain refresh does the job.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Responds to a search being added, updated, or removed by refreshing
     * the tree and, for quick searches, updating the selection and expansion
     * state so the user sees the results immediately.
     * If a quick search is removed we also clean up the
     * {@code BrowserConnection.quickSearch} reference so the model stays
     * consistent.
     *
     * <p>For example — Obi-Wan receives the scout's report:</p>
     * <pre>
     *   if (quickSearch was REMOVED) clearQuickSearchReference();
     *   viewer.refresh();
     *   if (quickSearch was ADDED or UPDATED) {
     *       viewer.setSelection(search);
     *       viewer.expandToLevel(search, 1);
     *   }
     * </pre>
     *
     * @param searchUpdateEvent  The event carrying the affected search and
     *                           what happened to it (added, updated, removed).
     */
    public void searchUpdated( SearchUpdateEvent searchUpdateEvent )
    {
        ISearch search = searchUpdateEvent.getSearch();

        if ( ( search instanceof IQuickSearch ) && ( searchUpdateEvent.getDetail() == EventDetail.SEARCH_REMOVED ) )
        {
            if ( search.getBrowserConnection().getQuickSearch() == search )
            {
                search.getBrowserConnection().setQuickSearch( null );
            }
        }

        viewer.refresh();

        if ( ( search instanceof IQuickSearch ) && ( searchUpdateEvent.getDetail() != EventDetail.SEARCH_REMOVED ) )
        {
            viewer.setSelection( new StructuredSelection( search ), true );
            viewer.expandToLevel( search, 1 );
        }
    }
}
