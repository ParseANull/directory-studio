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

package org.apache.directory.studio.ldapbrowser.ui.views.browser;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.ui.ConnectionUIPlugin;
import org.apache.directory.studio.entryeditors.EntryEditorExtension;
import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.EntryEditorManager;
import org.apache.directory.studio.entryeditors.IEntryEditor;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserContentProvider;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserUniversalListener;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.events.AttributesInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.BookmarkUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.BulkModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryAddedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryDeletedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryMovedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryRenamedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateEvent.EventDetail;
import org.apache.directory.studio.ldapbrowser.core.events.SearchUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.apache.directory.studio.ldapbrowser.ui.editors.searchresult.SearchResultEditor;
import org.apache.directory.studio.ldapbrowser.ui.editors.searchresult.SearchResultEditorInput;
import org.apache.directory.studio.ldapbrowser.ui.views.connection.ConnectionView;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;


// ── CLASS: BrowserViewUniversalListener — OBI-WAN SENSES EVERY DISTURBANCE ───
// Obi-Wan Kenobi doesn't just react to one type of event — he's attuned to
// everything: a new connection opening is like a ship landing on Tatooine,
// a search update is like a message arriving through the Force, a bookmark
// change is like someone moving a marker on the desert map. He senses it all
// and responds appropriately, keeping Luke (the browser view) fully informed
// of what's happening across the galaxy (the LDAP model and Eclipse workspace).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The central event listener for the LDAP browser view.
 * This class registers with multiple event buses (entry events, search events,
 * bookmark events, connection events, part events, preference events) and reacts
 * to each one by refreshing or re-selecting elements in the browser tree.
 * Without this class the tree would be a static snapshot — changes to the
 * underlying LDAP model would never appear until the user manually refreshed.
 * Think of Obi-Wan's all-encompassing Force sensitivity: no event goes unnoticed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserViewUniversalListener extends BrowserUniversalListener implements SearchUpdateListener,
    BookmarkUpdateListener
{
    /** This map contains all expanded elements for a particular connection */
    private Map<IBrowserConnection, Object[]> connectionToExpandedElementsMap;

    /** This map contains all selected elements for a particular connection */
    private Map<IBrowserConnection, ISelection> connectionToSelectedElementMap;

    /** The browser view */
    private BrowserView view;

    /** Token used to activate and deactivate shortcuts in the view */
    private IContextActivation contextActivation;

    /** Listener that listens for selections of connections */
    private INullSelectionListener connectionSelectionListener = new INullSelectionListener()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation sets the input when another connection was selected.
         */
        public void selectionChanged( IWorkbenchPart part, ISelection selection )
        {
            if ( view != null && part != null )
            {
                if ( view.getSite().getWorkbenchWindow() == part.getSite().getWorkbenchWindow() )
                {
                    Connection[] connections = BrowserSelectionUtils.getConnections( selection );
                    if ( connections.length == 1 )
                    {
                        IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                            .getBrowserConnection( connections[0] );
                        setInput( connection );
                    }
                    else
                    {
                        setInput( null );
                    }
                }
            }
        }
    };

    /** The part listener used to activate and deactivate the shortcuts */
    private IPartListener2 partListener = new IPartListener2()
    {
        /**
         * {@inheritDoc}
         *
         * This implementation deactivates the shortcuts when the part is deactivated.
         */
        public void partDeactivated( IWorkbenchPartReference partRef )
        {
            if ( partRef.getPart( false ) == view && contextActivation != null )
            {

                view.getActionGroup().deactivateGlobalActionHandlers();

                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextService.deactivateContext( contextActivation );
                contextActivation = null;
            }
        }


        /**
         * {@inheritDoc}
         *
         * This implementation activates the shortcuts when the part is activated.
         */
        public void partActivated( IWorkbenchPartReference partRef )
        {
            if ( partRef.getPart( false ) == view )
            {

                IContextService contextService = ( IContextService ) PlatformUI.getWorkbench().getAdapter(
                    IContextService.class );
                contextActivation = contextService.activateContext( BrowserCommonConstants.CONTEXT_WINDOWS );
                // org.eclipse.ui.contexts.dialogAndWindow
                // org.eclipse.ui.contexts.window
                // org.eclipse.ui.text_editor_context

                view.getActionGroup().activateGlobalActionHandlers();
            }
        }


        /**
         * {@inheritDoc}
         */
        public void partBroughtToTop( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partClosed( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partOpened( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partHidden( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partVisible( IWorkbenchPartReference partRef )
        {
        }


        /**
         * {@inheritDoc}
         */
        public void partInputChanged( IWorkbenchPartReference partRef )
        {
        }
    };

    /** This listener is used to ensure that the entry editor and search result editor are opened
     when an object in the browser view is selected */
    private ISelectionChangedListener viewerSelectionListener = new ISelectionChangedListener()
    {
        /**
         * {@inheritDoc}
         */
        public void selectionChanged( SelectionChangedEvent event )
        {
            openEditor( event.getSelection() );
        }
    };

    /** This listerner is used to listen on the preference settings modifications, especially
     * the open mode preference value change. */
    private IPropertyChangeListener preferencePropertyChangeListener = new IPropertyChangeListener()
    {
        /**
         * {@inheritDoc}
         */
        public void propertyChange( org.eclipse.jface.util.PropertyChangeEvent event )
        {
            if ( BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE.equals( event.getProperty() ) )
            {
                setupOpenModeListeners();
            }
        };
    };

    /** The open mode listener */
    private IOpenListener openListener = new IOpenListener()
    {
        public void open( OpenEvent event )
        {
            openEditor( event.getSelection() );
        }
    };


    // ── Obi-Wan Opens All His Senses at Once ────────────────────────────────────
    // Obi-Wan arrives on Tatooine and immediately extends his awareness in every
    // direction: he tunes into the planet's Force signature, the movements of
    // people around him, and any ripples from across the galaxy.
    // We register with every relevant event system so no change to the LDAP model
    // or Eclipse workspace goes unnoticed — entry events, search events, bookmark
    // events, connection events, part events, and preference events.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new BrowserViewUniversalListener and registers with all relevant
     * event registries and Eclipse services.
     * We also initialise the per-connection maps that save/restore expanded tree
     * state when the user switches between connections — so switching back to a
     * connection restores the exact tree state you left it in.
     *
     * @param view  the browser view this listener serves; must not be null.
     */
    public BrowserViewUniversalListener( BrowserView view )
    {
        super( view.getMainWidget() );
        this.view = view;

        // create maps
        connectionToExpandedElementsMap = new HashMap<IBrowserConnection, Object[]>();
        connectionToSelectedElementMap = new HashMap<IBrowserConnection, ISelection>();

        // register listeners
        EventRegistry.addSearchUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addBookmarkUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );
        ConnectionEventRegistry.addConnectionUpdateListener( this, ConnectionUIPlugin.getDefault().getEventRunner() );

        // listener for shortcuts activation/deactivation
        view.getSite().getPage().addPartListener( partListener );

        // listener for connections
        view.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener( ConnectionView.getId(),
            connectionSelectionListener );

        // listener for open mode
        BrowserUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( preferencePropertyChangeListener );
        setupOpenModeListeners();
    }


    // ── Obi-Wan Adjusts to the Ambient Force Conditions ─────────────────────────
    // Depending on what's going on around him, Obi-Wan chooses whether to react
    // to subtle ripples (selection-changed, quieter mode) or to loud shouts
    // (double-click / open events, louder mode).
    // We configure the viewer listeners to match the preference: historical
    // behaviour (react to any selection change) vs. application-wide (react
    // only to explicit open/double-click gestures).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Configures the tree viewer listeners to match the current "open mode" preference.
     * "Historical behaviour" opens the editor on every selection-changed event;
     * "application-wide" only opens it when the user explicitly double-clicks.
     * We have to remove one kind of listener and add the other — they're mutually
     * exclusive and both fire {@link #openEditor(ISelection)}.
     */
    private void setupOpenModeListeners()
    {
        int openMode = BrowserUIPlugin.getDefault().getPluginPreferences().getInt(
            BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE );

        if ( openMode == BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_HISTORICAL_BEHAVIOR )
        {
            // Historical Behavior
            viewer.removeOpenListener( openListener );
            viewer.addSelectionChangedListener( viewerSelectionListener );
        }
        else if ( openMode == BrowserUIConstants.PREFERENCE_ENTRYEDITORS_OPEN_MODE_APPLICATION_WIDE )
        {
            // Application Wide Setting
            viewer.removeSelectionChangedListener( viewerSelectionListener );
            viewer.addOpenListener( openListener );
        }
    }


    // ── Obi-Wan Acts on What He Senses ──────────────────────────────────────────
    // Obi-Wan senses a disturbance — something has been selected — and immediately
    // decides what the right response is: open the entry editor, open the search
    // results editor, or blank any stale editors.
    // We inspect the selection, route it to the right editor, and handle the
    // multi-selection case by blanking single-tab editors first.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Opens or updates the appropriate editor based on the current browser selection.
     * A single entry/search-result/bookmark triggers the entry editor; a single
     * search triggers the search result editor. Anything else blanks any open
     * single-tab entry editors so they don't show stale content.
     * This is called on both selection-changed and open events, depending on the
     * open-mode preference.
     *
     * @param selection  the current selection in the browser tree viewer.
     */
    private void openEditor( ISelection selection )
    {
        if ( view != null )
        {
            IEntry[] entries = BrowserSelectionUtils.getEntries( selection );
            ISearchResult[] searchResults = BrowserSelectionUtils.getSearchResults( selection );
            IBookmark[] bookmarks = BrowserSelectionUtils.getBookmarks( selection );
            ISearch[] searches = BrowserSelectionUtils.getSearches( selection );
            EntryEditorManager entryEditorManager = BrowserUIPlugin.getDefault().getEntryEditorManager();

            if ( entries.length + searchResults.length + bookmarks.length == 1 )
            {
                entryEditorManager.openEntryEditor( entries, searchResults, bookmarks );
                // atm it is not necessary to blank the search result editor, it blanks itself
            }
            else
            {
                // Checking if there's at least one entry editor open.
                // We need to blank them.
                // This is done before the search result editor is opened,
                // otherwise the entry editor would be activated.
                // We can blank them directly here, without using the  OpenEntryEditorRunnable.
                blankSingleTabEntryEditors();

                if ( searches.length == 1 )
                {
                    try
                    {
                        SearchResultEditorInput input = new SearchResultEditorInput( searches[0] );
                        view.getSite().getPage().openEditor( input, SearchResultEditor.getId(), false );
                    }
                    catch ( PartInitException e )
                    {
                    }
                }
            }
        }
    }


    // ── Obi-Wan Clears Out Old Impressions ───────────────────────────────────────
    // Before focusing on a new disturbance, Obi-Wan clears the lingering
    // impressions of previous events — otherwise old visions would muddy the new.
    // We blank all single-tab entry editors that still show a resolved entry,
    // so they don't display stale data when the user selects something different.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Blanks all single-tab (non-multi-window) entry editors that currently show
     * a resolved entry, replacing their input with an empty placeholder.
     * We do this before opening the search result editor so that the entry editor
     * doesn't steal focus by activating itself in response to the blank operation.
     */
    private void blankSingleTabEntryEditors()
    {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for ( IEditorReference ref : activePage.getEditorReferences() )
        {
            IWorkbenchPart part = ref.getPart( false );
            if ( part instanceof IEntryEditor )
            {
                IEntryEditor editor = ( IEntryEditor ) part;
                if ( ( editor != null ) && ( editor.getEntryEditorInput() != null )
                    && ( editor.getEntryEditorInput().getResolvedEntry() != null )
                    && ( editor.getEntryEditorInput().getExtension() != null )
                    && ( !editor.getEntryEditorInput().getExtension().isMultiWindow() ) )
                {

                    EntryEditorExtension extension = editor.getEntryEditorInput().getExtension();
                    String editorId = extension.getEditorId();
                    EntryEditorInput input = new EntryEditorInput( ( IEntry ) null, extension );
                    try
                    {
                        view.getSite().getPage().openEditor( input, editorId, false );
                    }
                    catch ( PartInitException e )
                    {
                    }
                }
            }
        }
    }


    // ── Obi-Wan Retires From His Watch ──────────────────────────────────────────
    // Obi-Wan's vigil is over — he withdraws from all his attunements, clears his
    // maps, and releases his references so nothing lingers.
    // We unregister from every event system and clear the connection-state maps
    // to prevent memory leaks.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Unregisters from all event registries and Eclipse services, then clears
     * all state held by this listener.
     * Call this when the browser view is disposed; failure to do so will leave
     * dangling listeners that keep the view object alive indefinitely.
     */
    @Override
    public void dispose()
    {
        if ( view != null )
        {
            EventRegistry.removeSearchUpdateListener( this );
            EventRegistry.removeBookmarkUpdateListener( this );
            EventRegistry.removeEntryUpdateListener( this );
            ConnectionEventRegistry.removeConnectionUpdateListener( this );

            view.getSite().getPage().removePartListener( partListener );
            view.getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(
                ConnectionView.getId(), connectionSelectionListener );

            viewer.removeOpenListener( openListener );
            viewer.removeSelectionChangedListener( viewerSelectionListener );

            view = null;
            connectionToExpandedElementsMap.clear();
            connectionToExpandedElementsMap = null;
            connectionToSelectedElementMap.clear();
            connectionToSelectedElementMap = null;
        }

        super.dispose();
    }


    // ── Obi-Wan Shifts His Attention to a New Planet ─────────────────────────────
    // Obi-Wan's focus shifts: he was watching Tatooine, now he turns his awareness
    // toward Alderaan — but first he memorises the exact state of Tatooine
    // so he can pick up exactly where he left off when he returns.
    // We save the current connection's expanded and selected tree state, switch
    // the viewer's input, and restore the saved state for the new connection.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Changes the browser tree's input to the given connection, saving and
     * restoring expanded/selected state per connection.
     * This means switching from connection A to connection B and back to A
     * restores exactly which nodes were expanded and selected in A.
     * Called when the user selects a different connection in the connection view.
     *
     * @param connection  the new connection to display, or {@code null} to clear the view.
     */
    void setInput( IBrowserConnection connection )
    {
        // only if another connection is selected
        if ( connection != viewer.getInput() )
        {

            IBrowserConnection currentConnection = viewer.getInput() instanceof IBrowserConnection ? ( IBrowserConnection ) viewer
                .getInput()
                : null;

            // save expanded elements and selection
            if ( currentConnection != null )
            {
                connectionToExpandedElementsMap.put( currentConnection, viewer.getExpandedElements() );
                if ( !viewer.getSelection().isEmpty() )
                {
                    connectionToSelectedElementMap.put( currentConnection, viewer.getSelection() );
                }
            }

            // change input
            viewer.setInput( connection );
            view.getActionGroup().setInput( connection );
            view.getMainWidget().getQuickSearchWidget().setInput( connection );

            // restore expanded elements and selection
            if ( view != null && connection != null )
            {
                if ( connectionToExpandedElementsMap.containsKey( connection ) )
                {
                    viewer.setExpandedElements( ( Object[] ) connectionToExpandedElementsMap.get( connection ) );
                }
                if ( connectionToSelectedElementMap.containsKey( connection )
                    && this.view.getSite().getPage().isPartVisible( view ) )
                {
                    viewer.setSelection( ( ISelection ) connectionToSelectedElementMap.get( connection ), true );
                }
            }
        }
    }


    // ── A Ship Lands on Tatooine — Obi-Wan Watches ──────────────────────────────
    // A new ship descends through the atmosphere and touches down — Obi-Wan notes
    // its arrival, extends his awareness, and prepares to interact with whoever
    // just arrived.
    // When a connection opens we expand the tree to show the root DSE and,
    // optionally, the base entries, so the user sees something useful right away.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionOpened(org.apache.directory.studio.connection.core.Connection)
     *
     * Refreshes and auto-expands the tree when a connection opens.
     * We expand to level 2 (root DSE children) and optionally to level 3
     * (base entry children) based on the user's preference, giving an immediate
     * overview of the directory without requiring manual expand clicks.
     * Silently skips browser connections that aren't in the connection manager
     * (e.g. temporary connections created by dialogs).
     *
     * @param connection  the connection that just opened.
     */
    public void connectionOpened( Connection connection )
    {
        IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( connection );

        if ( browserConnection == null )
        {
            //
            // If browser connection is null then it has been temporarily created
            // by a dialog or other transient entity. Only display in the view those
            // browser connections stored in the connection manager
            //
            return;
        }

        // expand viewer
        viewer.refresh( browserConnection );
        viewer.expandToLevel( 2 );

        // expand root DSE to show base entries
        IRootDSE rootDSE = browserConnection.getRootDSE();
        viewer.expandToLevel( rootDSE, 1 );

        // expand base entries, if requested
        if ( view.getConfiguration().getPreferences().isExpandBaseEntries() )
        {
            viewer.expandToLevel( rootDSE, 2 );
        }
    }


    // ── The Ship Lifts Off and Leaves ────────────────────────────────────────────
    // The ship ascends and disappears — Obi-Wan watches it go and tidies up:
    // clearing the map state he was tracking for that ship, collapsing any
    // nodes it had expanded.
    // When a connection closes we collapse the tree and remove saved state for
    // that connection so memory doesn't grow unbounded.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * @see org.apache.directory.studio.connection.core.event.ConnectionUpdateListener#connectionClosed(org.apache.directory.studio.connection.core.Connection)
     *
     * Collapses the tree and clears saved state when a connection is closed.
     * We remove both the expanded-elements and selected-elements maps for the
     * connection so we don't hold stale references to its entries.
     * Silently skips browser connections not in the connection manager.
     *
     * @param connection  the connection that just closed.
     */
    public void connectionClosed( Connection connection )
    {
        IBrowserConnection browserConnection = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( connection );

        if (browserConnection == null)
        {
            //
            // If browser connection is null then it has been temporarily created
            // by a dialog or other transient entity. Only display in the view those
            // browser connections stored in the connection manager
            //
            return;
        }

        viewer.collapseAll();
        connectionToExpandedElementsMap.remove( browserConnection );
        connectionToSelectedElementMap.remove( browserConnection );
        viewer.refresh( browserConnection );
    }


    // ── A Message Arrives Through the Force ─────────────────────────────────────
    // Obi-Wan receives a ripple in the Force: a search has been updated.
    // He refreshes his mental map and adjusts his focus to the updated search.
    // We refresh the tree and move the selection to the updated search object
    // so the user always sees the current state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Refreshes the tree and moves selection to the updated search.
     * We also handle the quick-search lifecycle: if a quick search is removed
     * we clear it from the connection so it doesn't appear as a phantom node.
     * For continuation (referral) searches we always select the search itself;
     * for regular searches we select the search if it still exists in the manager,
     * otherwise we select its parent category node.
     *
     * @param searchUpdateEvent  the event describing what changed and why.
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

        if ( search instanceof IContinuation )
        {
            viewer.setSelection( new StructuredSelection( search ), true );
        }
        else if ( search.getBrowserConnection().getSearchManager().getSearches().contains( search ) )
        {
            viewer.setSelection( new StructuredSelection( search ), true );
        }
        else if ( ( search instanceof IQuickSearch ) && ( searchUpdateEvent.getDetail() != EventDetail.SEARCH_REMOVED ) )
        {
            if ( search.getBrowserConnection().getQuickSearch() == search )
            {
                viewer.setSelection( new StructuredSelection( search ), true );
            }
        }
        else
        {
            Object searchCategory = ( ( ITreeContentProvider ) viewer.getContentProvider() ).getParent( search );
            if ( searchCategory != null )
            {
                viewer.setSelection( new StructuredSelection( searchCategory ), true );
            }
        }
    }


    // ── A Bookmark Moves on the Desert Map ──────────────────────────────────────
    // Someone moved a marker on the Tatooine desert map — Obi-Wan notices and
    // refreshes his mental picture of where things are.
    // We just refresh the whole viewer; bookmarks don't need fine-grained
    // selection logic like searches do.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Refreshes the tree when a bookmark is added, removed, or changed.
     * Bookmarks appear as nodes in the tree, so any change to them requires
     * a viewer refresh to stay in sync.
     *
     * @param bookmarkUpdateEvent  the event carrying details of the bookmark change.
     */
    public void bookmarkUpdated( BookmarkUpdateEvent bookmarkUpdateEvent )
    {
        viewer.refresh();
    }


    // ── Obi-Wan Senses a Disturbance in the Entry Landscape ──────────────────────
    // Something changed in the directory — an entry added, renamed, moved, or
    // deleted. Obi-Wan's map of Tatooine needs updating: he collapses stale
    // branches and re-expands only what's needed, then sets focus appropriately.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     *
     * Refreshes the tree and moves selection in response to an entry modification.
     * We handle each event type differently:
     * <ul>
     *   <li>{@link EntryAddedEvent}: refresh parent, select new entry.</li>
     *   <li>{@link EntryRenamedEvent}: refresh new parent, select new entry.</li>
     *   <li>{@link EntryMovedEvent}: refresh both old and new parents, select new entry.</li>
     *   <li>{@link EntryDeletedEvent}: refresh parent (or root), select parent.</li>
     *   <li>{@link BulkModificationEvent}: full viewer refresh.</li>
     *   <li>{@link ChildrenInitializedEvent}: collapse stale child nodes to force re-expansion.</li>
     *   <li>{@link AttributesInitializedEvent}: skipped to avoid double-loading children (see inline comment).</li>
     * </ul>
     *
     * @param event  the entry modification event describing what changed.
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

        if ( event instanceof EntryAddedEvent )
        {
            viewer.refresh( event.getModifiedEntry(), true );
            viewer.refresh( event.getModifiedEntry().getParententry(), true );
            viewer.setSelection( new StructuredSelection( event.getModifiedEntry() ), true );
        }
        else if ( event instanceof EntryRenamedEvent )
        {
            EntryRenamedEvent ere = ( EntryRenamedEvent ) event;
            viewer.refresh( ere.getNewEntry().getParententry(), true );
            viewer.setSelection( new StructuredSelection( ere.getNewEntry() ), true );
        }
        else if ( event instanceof EntryMovedEvent )
        {
            EntryMovedEvent eme = ( EntryMovedEvent ) event;
            viewer.refresh( eme.getOldEntry().getParententry(), true );
            viewer.refresh( eme.getNewEntry().getParententry(), true );
            viewer.setSelection( new StructuredSelection( eme.getNewEntry() ), true );
        }
        else if ( event instanceof EntryDeletedEvent )
        {
            EntryDeletedEvent ede = ( EntryDeletedEvent ) event;
            if ( ede.getModifiedEntry().getParententry() != null )
            {
                viewer.refresh( ede.getModifiedEntry().getParententry(), true );
                viewer.setSelection( new StructuredSelection( ede.getModifiedEntry().getParententry() ), true );
            }
            else
            {
                viewer.refresh();
            }
        }
        else if ( event instanceof BulkModificationEvent )
        {
            viewer.refresh();
        }
        else if ( event instanceof ChildrenInitializedEvent )
        {
            // Getting the children of the entry to collapse their nodes
            // See DIRSTUDIO-481 (refreshing of attributes and children)
            Object[] children = ( ( BrowserContentProvider ) viewer.getContentProvider() ).getChildren( event
                .getModifiedEntry() );
            for ( Object child : children )
            {
                // We're only collapsing the node if it is expanded
                if ( viewer.getExpandedState( child ) )
                {
                    viewer.collapseToLevel( child, TreeViewer.ALL_LEVELS );

                    // There seem to be a bug (maybe it's a feature?!?) with nodes that were expanded
                    // but does not have any child.
                    // The call to 'viewer.collapseToLevel(...)' has no effect, the node stays expanded...
                    if ( viewer.getExpandedState( child ) )
                    {
                        // In that particular case, we need to remove the child from the tree viewer.
                        // As it's a costly operation we're only using this in that particular case,
                        // and not as default option.
                        viewer.remove( child );
                    }
                }
            }

            viewer.refresh( event.getModifiedEntry(), true );
        }
        else if ( !( event.getModifiedEntry() instanceof DummyEntry ) )
        {
            viewer.refresh( event.getModifiedEntry(), true );
        }
    }

}
