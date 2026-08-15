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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.directory.studio.connection.core.jobs.OpenConnectionsRunnable;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation.State;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DirectoryMetadataEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchContinuation;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: BrowserContentProvider — R2 PLUGS INTO THE DEATH STAR TERMINAL ────
// In the Death Star's detention block, R2-D2 plugs into the main data terminal
// and starts navigating the directory structure — following references, loading
// sub-levels on demand, paging through long lists, and knowing exactly which
// "folder" each data record lives in. He answers the crew's question of
// "What's in here?" for every node in the hierarchy.
// BrowserContentProvider does the same for the LDAP browser tree: given any
// node (a connection, a category, an entry, a search, a page), it tells JFace
// exactly which children to display, handles lazy loading (kicks off background
// jobs to fetch children), and manages pagination when there are too many
// children to display at once.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The JFace tree content provider for the LDAP browser widget.
 * It maps LDAP model objects (connections, entries, searches, bookmarks) to
 * what gets shown as tree nodes, including handling lazy loading, pagination
 * (folding), search continuations, and quick-search sub-nodes.
 * Think of this class as R2-D2 at the Death Star terminal: given any node,
 * it figures out exactly what children live below it and returns them.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserContentProvider implements ITreeContentProvider
{
    /** The viewer. */
    private TreeViewer viewer;

    /** The preferences */
    protected BrowserPreferences preferences;

    /** The sorter */
    protected BrowserSorter sorter;

    /** This map contains the pages for entries with many children (if folding is activated) */
    private Map<IEntry, BrowserEntryPage[]> entryToEntryPagesMap;

    /** This map contains the pages for searches with many results (if folding is activated) */
    private Map<ISearch, BrowserSearchResultPage[]> searchToSearchResultPagesMap;

    /** This map contains the top-level categories for each connection */
    private Map<IBrowserConnection, BrowserCategory[]> connectionToCategoriesMap;

    /** The page listener. */
    private ISelectionChangedListener pageListener = new ISelectionChangedListener()
    {
        public void selectionChanged( SelectionChangedEvent event )
        {
            IStructuredSelection selection = ( IStructuredSelection ) event.getSelection();
            if ( selection.size() == 1 && selection.getFirstElement() instanceof StudioConnectionRunnableWithProgress )
            {
                StudioConnectionRunnableWithProgress runnable = ( StudioConnectionRunnableWithProgress ) selection
                    .getFirstElement();
                new StudioBrowserJob( runnable ).execute();
            }
        }
    };


    // ── R2 CONNECTS TO THE TERMINAL AND INITIALIZES HIS BUFFERS ─────────────
    // R2 plugs into the Death Star data port: he sets up his internal memory
    // maps to track which nodes he's already fetched data for — entry pages,
    // search result pages, and connection categories. Then he registers a
    // listener so he can auto-execute background jobs when a page-loading
    // node gets selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new content provider and registers it with the browser tree viewer.
     * We set up three internal caches (entry pages, search result pages, connection
     * categories) and attach a selection listener that automatically runs background
     * jobs when the user clicks a "load more" page node.
     *
     * <p>For example — R2 initializes his buffers before querying the Death Star:</p>
     * <pre>
     *   R2.initBuffer(ENTRY_PAGES_CACHE);
     *   R2.initBuffer(SEARCH_PAGES_CACHE);
     *   R2.initBuffer(CATEGORY_CACHE);
     *   R2.attachListener(terminal, AUTO_EXECUTE_ON_SELECT);
     * </pre>
     *
     * @param widget        the browser widget — we extract its viewer to attach listeners
     * @param preferences   the display preferences (folding size, which categories to show, etc.)
     * @param sorter        the entry sorter — applied when we slice pages out of a sorted child list
     */
    public BrowserContentProvider( BrowserWidget widget, BrowserPreferences preferences, BrowserSorter sorter )
    {
        this.viewer = widget.getViewer();
        this.preferences = preferences;
        this.sorter = sorter;
        this.entryToEntryPagesMap = new HashMap<IEntry, BrowserEntryPage[]>();
        this.searchToSearchResultPagesMap = new HashMap<ISearch, BrowserSearchResultPage[]>();
        this.connectionToCategoriesMap = new HashMap<IBrowserConnection, BrowserCategory[]>();

        viewer.addSelectionChangedListener( pageListener );
    }


    // ── R2 ACKNOWLEDGES A CHANGE IN THE MAIN TERMINAL ────────────────────────
    // The Death Star's main data feed switches from one connection to another —
    // R2 beeps to acknowledge the switch but doesn't need to do anything special
    // since our caches are keyed by connection object and remain valid.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Called by JFace when the tree's root input changes.
     * For example, when the user switches to a different LDAP connection. We don't
     * need to do anything here because our internal caches are keyed by connection
     * object — the new connection simply gets its own cache entry on next access.
     *
     * <p>For example — R2 acknowledges the terminal switch without resetting his buffers:</p>
     * <pre>
     *   R2.beep(); // "Got it, switching data feeds."
     *   // Internal buffers remain valid — keyed by connection
     * </pre>
     *
     * @param v         the JFace viewer (unused)
     * @param oldInput  the previous input object (unused)
     * @param newInput  the new input object (unused)
     */
    public void inputChanged( Viewer v, Object oldInput, Object newInput )
    {
    }


    // ── R2 DISCONNECTS AND WIPES HIS BUFFERS ─────────────────────────────────
    // When the Falcon flies away from the Death Star, R2 disconnects from the
    // terminal and clears his session buffers — leaving no sensitive data
    // behind and releasing memory so the Falcon's computer doesn't run out.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Releases all internal caches and unregisters listeners.
     * Always called when the browser widget is disposed. We clear all three caches
     * and remove the selection listener to avoid memory leaks and stale references.
     *
     * <p>For example — R2 disconnects and wipes his session buffers as the Falcon departs:</p>
     * <pre>
     *   R2.clearBuffer(ENTRY_PAGES_CACHE);
     *   R2.clearBuffer(SEARCH_PAGES_CACHE);
     *   R2.clearBuffer(CATEGORY_CACHE);
     *   terminal.removeListener(R2);
     * </pre>
     */
    public void dispose()
    {
        if ( entryToEntryPagesMap != null )
        {
            entryToEntryPagesMap.clear();
            entryToEntryPagesMap = null;
        }
        if ( searchToSearchResultPagesMap != null )
        {
            searchToSearchResultPagesMap.clear();
            searchToSearchResultPagesMap = null;
        }
        if ( connectionToCategoriesMap != null )
        {
            connectionToCategoriesMap.clear();
            connectionToCategoriesMap = null;
        }
        viewer.removeSelectionChangedListener( pageListener );
    }


    // ── R2 LISTS THE TOP-LEVEL DIRECTORIES AT THE ROOT ───────────────────────
    // R2 plugs into the Death Star root terminal and sees three top-level
    // directories: Military Records, Prisoner Registry, and Surveillance Logs.
    // He lists whichever ones the crew wants to see based on the current filter
    // settings (show DIT? show searches? show bookmarks?).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the top-level elements for the tree — the three category nodes
     * (DIT, Searches, Bookmarks) for the given connection.
     * This is the JFace method for "what goes at the very top of the tree?"
     * We filter the three categories based on user preferences (they can hide
     * bookmarks or searches if they prefer a cleaner view).
     *
     * <p>For example — R2 reports the top-level directories visible at the Death Star root:</p>
     * <pre>
     *   if (showMilitaryRecords) list.add(DIT_CATEGORY);
     *   if (showPrisonerRegistry) list.add(SEARCHES_CATEGORY);
     *   if (showSurveillanceLogs) list.add(BOOKMARKS_CATEGORY);
     * </pre>
     *
     * @param parent   the root object — should be an {@link IBrowserConnection}
     * @return array of top-level tree nodes; empty array if parent is unrecognized
     */
    public Object[] getElements( Object parent )
    {
        if ( parent instanceof IBrowserConnection )
        {
            IBrowserConnection connection = ( IBrowserConnection ) parent;
            if ( !connectionToCategoriesMap.containsKey( connection ) )
            {
                BrowserCategory[] categories = new BrowserCategory[3];
                categories[0] = new BrowserCategory( BrowserCategory.TYPE_DIT, connection );
                categories[1] = new BrowserCategory( BrowserCategory.TYPE_SEARCHES, connection );
                categories[2] = new BrowserCategory( BrowserCategory.TYPE_BOOKMARKS, connection );
                connectionToCategoriesMap.put( connection, categories );
            }

            BrowserCategory[] categories = connectionToCategoriesMap.get( connection );

            List<BrowserCategory> catList = new ArrayList<BrowserCategory>( 3 );
            if ( preferences.isShowDIT() )
            {
                catList.add( categories[0] );
            }
            if ( preferences.isShowSearches() )
            {
                catList.add( categories[1] );
            }
            if ( preferences.isShowBookmarks() )
            {
                catList.add( categories[2] );
            }

            return catList.toArray( new BrowserCategory[0] );
        }
        else if ( parent instanceof IEntry[] )
        {
            return ( IEntry[] ) parent;
        }
        else
        {
            return getChildren( parent );
        }
    }


    // ── R2 TRACES BACK TO THE PARENT RECORD ──────────────────────────────────
    // R2 is deep in the Death Star filing system and needs to navigate back
    // up to a record's parent directory. He follows the directory structure
    // backward: a file goes to its folder, a folder to its section, a section
    // to the root category.
    // JFace needs this to keep the tree's selection and reveal-in-tree features
    // working correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent node of a given tree element.
     * JFace uses this to support "reveal in tree" and proper selection behavior.
     * We handle every possible node type (entries, pages, categories, searches,
     * bookmarks, search results) and trace back to their logical parent.
     * Pagination complicates things: if an entry has too many siblings, its parent
     * in the tree might be a {@link BrowserEntryPage} rather than the parent entry directly.
     *
     * <p>For example — R2 traces a file back through the Death Star filing hierarchy:</p>
     * <pre>
     *   prisonerRecord.getParent();   // → prisoner section folder
     *   prisonerSection.getParent();  // → Military Records category
     *   militaryRecords.getParent();  // → Death Star root connection
     * </pre>
     *
     * @param child   the tree node whose parent we need to find
     * @return the parent node, or null if child is at the root or unrecognized
     */
    public Object getParent( final Object child )
    {
        if ( child instanceof BrowserCategory )
        {
            return ( ( BrowserCategory ) child ).getParent();
        }
        else if ( child instanceof BrowserEntryPage )
        {
            return ( ( BrowserEntryPage ) child ).getParent();
        }
        else if ( child instanceof IEntry )
        {
            IEntry parentEntry = ( ( IEntry ) child ).getParententry();
            if ( parentEntry == null )
            {
                if ( connectionToCategoriesMap.get( ( ( IEntry ) child ).getBrowserConnection() ) != null )
                {
                    return connectionToCategoriesMap.get( ( ( IEntry ) child ).getBrowserConnection() )[0];
                }
                else
                {
                    return null;
                }
            }
            else if ( parentEntry.getChildrenCount() <= preferences.getFoldingSize() || !preferences.isUseFolding() )
            {
                return parentEntry;
            }
            else
            {
                BrowserEntryPage[] entryPages = getEntryPages( parentEntry );
                BrowserEntryPage ep = null;
                for ( int i = 0; i < entryPages.length && ep == null; i++ )
                {
                    ep = entryPages[i].getParentOf( ( IEntry ) child );
                }
                return ep;
            }
        }
        else if ( child instanceof BrowserSearchResultPage )
        {
            return ( ( BrowserSearchResultPage ) child ).getParent();
        }
        else if ( child instanceof IQuickSearch )
        {
            IQuickSearch quickSearch = ( ( IQuickSearch ) child );
            IEntry entry = quickSearch.getBrowserConnection().getEntryFromCache( quickSearch.getSearchBase() );
            return entry;
        }
        else if ( child instanceof ISearch )
        {
            ISearch search = ( ( ISearch ) child );
            if ( connectionToCategoriesMap.get( search.getBrowserConnection() ) != null )
            {
                return connectionToCategoriesMap.get( search.getBrowserConnection() )[1];
            }
            else
            {
                return null;
            }
        }
        else if ( child instanceof ISearchResult )
        {
            ISearch parentSearch = ( ( ISearchResult ) child ).getSearch();

            if ( parentSearch == null || parentSearch.getSearchResults().length <= preferences.getFoldingSize()
                || !preferences.isUseFolding() )
            {
                return parentSearch;
            }
            else
            {
                BrowserSearchResultPage[] srPages = getSearchResultPages( parentSearch );
                BrowserSearchResultPage srp = null;
                for ( int i = 0; i < srPages.length && srp == null; i++ )
                {
                    srp = srPages[i].getParentOf( ( ISearchResult ) child );
                }
                return srp;
            }
        }
        else if ( child instanceof IBookmark )
        {
            IBookmark bookmark = ( ( IBookmark ) child );
            if ( connectionToCategoriesMap.get( bookmark.getBrowserConnection() ) != null )
            {
                return connectionToCategoriesMap.get( bookmark.getBrowserConnection() )[2];
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }


    // ── R2 OPENS A DIRECTORY AND LISTS ITS CONTENTS ──────────────────────────
    // R2 opens a directory on the Death Star terminal. If it's a huge directory
    // he splits it into numbered pages. If the data isn't loaded yet he kicks
    // off a background job and returns a "Fetching…" placeholder. If the
    // directory is a search category he runs the search. If it's empty he says so.
    // This is the core method JFace calls whenever the user expands a tree node.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the child nodes for a given tree element, triggering background
     * loading jobs as needed.
     * This is the heart of the content provider. For every possible parent type
     * (entry page, root DSE, entry, search result page, search, category), we
     * return the right children. If children aren't loaded yet, we fire a background
     * job and return a temporary "Fetching entries..." placeholder string so the UI
     * doesn't freeze.
     *
     * <p>For example — R2 opens a Death Star directory and handles each case:</p>
     * <pre>
     *   if (directory.isTooLarge) return paginatedPages;
     *   if (directory.notLoaded) { kickOffLoadJob(); return FETCHING_PLACEHOLDER; }
     *   if (directory.isEmpty) return NO_RESULTS_PLACEHOLDER;
     *   return directory.contents;
     * </pre>
     *
     * @param parent   the tree node being expanded
     * @return array of child objects to display; may include placeholder strings
     */
    public Object[] getChildren( Object parent )
    {
        if ( parent instanceof BrowserEntryPage )
        {
            BrowserEntryPage entryPage = ( BrowserEntryPage ) parent;
            Object[] objects = entryPage.getChildren();
            if ( objects == null )
            {
                return new String[]
                    { Messages.getString( "BrowserContentProvider.FetchingEntries" ) }; //$NON-NLS-1$
            }
            else if ( objects instanceof IEntry[] )
            {
                IEntry[] entries = ( IEntry[] ) objects;
                return entries;
            }
            else
            {
                return objects;
            }
        }
        else if ( parent instanceof IRootDSE )
        {
            final IRootDSE rootDSE = ( IRootDSE ) parent;

            if ( !rootDSE.isChildrenInitialized() )
            {
                new StudioBrowserJob( new InitializeChildrenRunnable( false, rootDSE ) ).execute();
                return new String[]
                    { Messages.getString( "BrowserContentProvider.FetchingEntries" ) }; //$NON-NLS-1$
            }

            // get base entries
            List<IEntry> entryList = new ArrayList<IEntry>();
            entryList.addAll( Arrays.asList( rootDSE.getChildren() ) );

            // remove non-visible entries
            for ( Iterator<IEntry> it = entryList.iterator(); it.hasNext(); )
            {
                Object o = it.next();
                if ( !preferences.isShowDirectoryMetaEntries() && ( o instanceof DirectoryMetadataEntry ) )
                {
                    it.remove();
                }
            }

            return entryList.toArray();
        }
        else if ( parent instanceof IEntry )
        {
            final IEntry parentEntry = ( IEntry ) parent;

            if ( parentEntry instanceof IContinuation )
            {
                IContinuation continuation = ( IContinuation ) parentEntry;
                if ( continuation.getState() == State.UNRESOLVED )
                {
                    continuation.resolve();
                }
                if ( continuation.getState() == State.CANCELED )
                {
                    return new Object[0];
                }
            }

            List<Object> objects = new ArrayList<Object>();

            IQuickSearch quickSearch = getQuickSearchForEntry( parentEntry );
            if ( quickSearch != null )
            {
                objects.add( quickSearch );
            }

            if ( !parentEntry.isChildrenInitialized() )
            {
                new StudioBrowserJob( new InitializeChildrenRunnable( false, parentEntry ) ).execute();
                return new String[]
                    { Messages.getString( "BrowserContentProvider.FetchingEntries" ) }; //$NON-NLS-1$
            }
            else if ( parentEntry.getChildrenCount() <= preferences.getFoldingSize() || !preferences.isUseFolding() )
            {
                if ( entryToEntryPagesMap.containsKey( parentEntry ) )
                {
                    entryToEntryPagesMap.remove( parentEntry );
                }

                IEntry[] results = parentEntry.getChildren();

                if ( parentEntry.getTopPageChildrenRunnable() != null )
                {
                    objects.add( parentEntry.getTopPageChildrenRunnable() );
                }

                objects.addAll( Arrays.asList( results ) );

                if ( parentEntry.getNextPageChildrenRunnable() != null )
                {
                    objects.add( parentEntry.getNextPageChildrenRunnable() );
                }

                return objects.toArray();
            }
            else
            {
                BrowserEntryPage[] entryPages = getEntryPages( parentEntry );

                objects.addAll( Arrays.asList( entryPages ) );

                return objects.toArray();
            }
        }
        else if ( parent instanceof BrowserSearchResultPage )
        {
            BrowserSearchResultPage srPage = ( BrowserSearchResultPage ) parent;
            Object[] objects = srPage.getChildren();
            if ( objects == null )
            {
                return new String[]
                    { Messages.getString( "BrowserContentProvider.FetchingSearchResults" ) }; //$NON-NLS-1$
            }
            else if ( objects instanceof ISearchResult[] )
            {
                ISearchResult[] srs = ( ISearchResult[] ) objects;
                return srs;
            }
            else
            {
                return objects;
            }
        }
        else if ( parent instanceof ISearch )
        {
            ISearch search = ( ISearch ) parent;
            if ( search instanceof IContinuation )
            {
                IContinuation continuation = ( IContinuation ) search;
                if ( continuation.getState() == State.UNRESOLVED )
                {
                    continuation.resolve();
                }
                if ( continuation.getState() == State.CANCELED )
                {
                    return new Object[0];
                }
            }

            if ( search.getSearchResults() == null || search.getSearchContinuations() == null )
            {
                new StudioBrowserJob( new SearchRunnable( new ISearch[]
                    { search } ) ).execute();
                return new String[]
                    { Messages.getString( "BrowserContentProvider.PerformingSearch" ) }; //$NON-NLS-1$
            }
            else if ( search.getSearchResults().length + search.getSearchContinuations().length == 0 )
            {
                return new String[]
                    { Messages.getString( "BrowserContentProvider.NoResults" ) }; //$NON-NLS-1$
            }
            else if ( search.getSearchResults().length <= preferences.getFoldingSize() || !preferences.isUseFolding() )
            {
                if ( searchToSearchResultPagesMap.containsKey( search ) )
                {
                    searchToSearchResultPagesMap.remove( search );
                }

                ISearchResult[] results = search.getSearchResults();
                SearchContinuation[] scs = search.getSearchContinuations();
                List<Object> objects = new ArrayList<Object>();

                if ( search.getTopSearchRunnable() != null )
                {
                    objects.add( search.getTopSearchRunnable() );
                }

                objects.addAll( Arrays.asList( results ) );

                if ( scs != null )
                {
                    objects.addAll( Arrays.asList( scs ) );
                }

                if ( search.getNextSearchRunnable() != null )
                {
                    objects.add( search.getNextSearchRunnable() );
                }

                return objects.toArray();
            }
            else
            {
                BrowserSearchResultPage[] srPages = getSearchResultPages( search );
                return srPages;
            }
        }
        else if ( parent instanceof BrowserCategory )
        {
            BrowserCategory category = ( BrowserCategory ) parent;
            IBrowserConnection browserConnection = category.getParent();

            switch ( category.getType() )
            {
                case BrowserCategory.TYPE_DIT:
                {
                    // open connection when expanding DIT
                    if ( browserConnection.getConnection() != null
                        && !browserConnection.getConnection().getConnectionWrapper().isConnected() )
                    {
                        new StudioBrowserJob( new OpenConnectionsRunnable( browserConnection.getConnection() ) )
                            .execute();
                        return new String[]
                            { Messages.getString( "BrowserContentProvider.OpeningConnection" ) }; //$NON-NLS-1$
                    }

                    return new Object[]
                        { browserConnection.getRootDSE() };
                }

                case BrowserCategory.TYPE_SEARCHES:
                {
                    return browserConnection.getSearchManager().getSearches().toArray();
                }

                case BrowserCategory.TYPE_BOOKMARKS:
                {
                    return browserConnection.getBookmarkManager().getBookmarks();
                }
            }

            return new Object[0];
        }
        else
        {
            return new Object[0];
        }
    }


    // ── R2 CHECKS: DOES THIS DIRECTORY HAVE SUB-DIRECTORIES? ─────────────────
    // R2 quickly scans a directory header on the Death Star terminal to decide
    // whether to show an expand-arrow next to it in the tree. Some directories
    // (prisoner records) always have sub-files; others (a single memo file) never do.
    // We check the model — does this entry have children? Does this search have results?
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Tells JFace whether a tree node should show an expand arrow (has children).
     * JFace calls this efficiently for every visible node to decide whether to show
     * the little triangle. We check the model rather than loading full child data —
     * for example, an entry reports {@code hasChildren()} from its already-cached
     * attribute data without triggering a new LDAP query.
     *
     * <p>For example — R2 checks directory headers on the Death Star for sub-entries:</p>
     * <pre>
     *   prisonerSection.hasSubDirectories();  // true — always has files
     *   singleMemoFile.hasSubDirectories();   // false — no sub-entries
     * </pre>
     *
     * @param parent   the tree node to check
     * @return true if this node can be expanded (has or might have children)
     */
    public boolean hasChildren( Object parent )
    {
        if ( parent instanceof IEntry )
        {
            IEntry parentEntry = ( IEntry ) parent;
            return parentEntry.hasChildren() || getQuickSearchForEntry( parentEntry ) != null;
        }
        else if ( parent instanceof SearchContinuation )
        {
            return true;
        }
        else if ( parent instanceof BrowserEntryPage )
        {
            return true;
        }
        else if ( parent instanceof BrowserSearchResultPage )
        {
            return true;
        }
        else if ( parent instanceof ISearchResult )
        {
            return false;
        }
        else if ( parent instanceof ISearch )
        {
            return true;
        }
        else if ( parent instanceof BrowserCategory )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    private IQuickSearch getQuickSearchForEntry( IEntry parentEntry )
    {
        IQuickSearch quickSearch = parentEntry.getBrowserConnection().getQuickSearch();
        if ( quickSearch != null
            && parentEntry.getDn().equals( quickSearch.getSearchBase() ) )
        {
            return quickSearch;
        }

        return null;
    }


    private BrowserEntryPage[] getEntryPages( final IEntry parentEntry )
    {
        BrowserEntryPage[] entryPages;
        if ( !entryToEntryPagesMap.containsKey( parentEntry ) )
        {
            entryPages = getEntryPages( parentEntry, 0, parentEntry.getChildrenCount() - 1 );
            entryToEntryPagesMap.put( parentEntry, entryPages );
        }
        else
        {
            entryPages = entryToEntryPagesMap.get( parentEntry );
            if ( parentEntry.getChildrenCount() - 1 != entryPages[entryPages.length - 1].getLast() )
            {
                entryPages = getEntryPages( parentEntry, 0, parentEntry.getChildrenCount() - 1 );
                entryToEntryPagesMap.put( parentEntry, entryPages );
            }
        }
        return entryPages;
    }


    /**
     * Creates and returns the entry pages for the given entry. The number of pages
     * depends on the number of entries and the paging size.
     *
     * @param entry the parent entry
     * @param first the index of the first child entry
     * @param last the index of the last child entry
     * @return the created entry pages
     */
    private BrowserEntryPage[] getEntryPages( IEntry entry, int first, int last )
    {
        int pagingSize = preferences.getFoldingSize();

        int diff = last - first;
        int factor = diff > 0 ? ( int ) ( Math.log( diff ) / Math.log( pagingSize ) ) : 0;

        int groupFirst = first;
        int groupLast = first;
        BrowserEntryPage[] pages = new BrowserEntryPage[( int ) ( diff / Math.pow( pagingSize, factor ) ) + 1];
        for ( int i = 0; i < pages.length; i++ )
        {
            groupFirst = ( int ) ( i * Math.pow( pagingSize, factor ) ) + first;
            groupLast = ( int ) ( ( i + 1 ) * Math.pow( pagingSize, factor ) ) + first - 1;
            groupLast = groupLast > last ? last : groupLast;
            BrowserEntryPage[] subpages = ( factor > 1 ) ? getEntryPages( entry, groupFirst, groupLast ) : null;
            pages[i] = new BrowserEntryPage( entry, groupFirst, groupLast, subpages, sorter );
        }

        return pages;
    }


    private BrowserSearchResultPage[] getSearchResultPages( ISearch search )
    {
        BrowserSearchResultPage[] srPages;
        if ( !searchToSearchResultPagesMap.containsKey( search ) )
        {
            srPages = getSearchResultPages( search, 0, search.getSearchResults().length - 1 );
            searchToSearchResultPagesMap.put( search, srPages );
        }
        else
        {
            srPages = searchToSearchResultPagesMap.get( search );
            if ( search.getSearchResults().length - 1 != srPages[srPages.length - 1].getLast() )
            {
                srPages = getSearchResultPages( search, 0, search.getSearchResults().length - 1 );
                searchToSearchResultPagesMap.put( search, srPages );
            }
        }
        return srPages;
    }


    /**
     * Creates and returns the search result pages for the given search. The number of pages
     * depends on the number of search results and the paging size.
     *
     * @param search the parent search
     * @param first the index of the first search result
     * @param last the index of the last child search result
     * @return the created search result pages
     */
    private BrowserSearchResultPage[] getSearchResultPages( ISearch search, int first, int last )
    {
        int pagingSize = preferences.getFoldingSize();

        int diff = last - first;
        int factor = diff > 0 ? ( int ) ( Math.log( diff ) / Math.log( pagingSize ) ) : 0;

        int groupFirst = first;
        int groupLast = first;
        BrowserSearchResultPage[] pages = new BrowserSearchResultPage[( int ) ( diff / Math.pow( pagingSize, factor ) ) + 1];
        for ( int i = 0; i < pages.length; i++ )
        {
            groupFirst = ( int ) ( i * Math.pow( pagingSize, factor ) ) + first;
            groupLast = ( int ) ( ( i + 1 ) * Math.pow( pagingSize, factor ) ) + first - 1;
            groupLast = groupLast > last ? last : groupLast;
            BrowserSearchResultPage[] subpages = ( factor > 1 ) ? getSearchResultPages( search, groupFirst, groupLast )
                : null;
            pages[i] = new BrowserSearchResultPage( search, groupFirst, groupLast, subpages, sorter );
        }

        return pages;
    }
}
