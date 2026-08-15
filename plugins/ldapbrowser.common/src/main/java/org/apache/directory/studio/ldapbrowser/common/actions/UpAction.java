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

package org.apache.directory.studio.ldapbrowser.common.actions;


import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserEntryPage;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserSearchResultPage;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;


// ── CLASS: UpAction — LUKE CLIMBS THE SHAFT TO REACH THE FALCON ──────────────
// At the climax of The Empire Strikes Back, after Vader reveals the truth and
// Luke drops into the reactor shaft, he must climb — hand over hand, step by
// step — back up toward the Falcon waiting above.  He moves from a deeper node
// in the structure back toward the parent.  That's exactly what "Up" does in
// the LDAP browser tree: from whatever is selected, we navigate one level up to
// the parent node in the directory tree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Implements the "Up" navigation action for the LDAP browser tree viewer.
 *
 * <p>When triggered, this action takes whatever is currently selected in the
 * tree (an entry, search, search result, bookmark, or pagination page) and
 * navigates the selection one level up to its parent node.  The parent is
 * resolved via the tree's own content provider so we stay consistent with
 * whatever the tree is actually showing.</p>
 *
 * <p>Think of this as Luke climbing up Cloud City's reactor shaft: we move
 * from the current node upward toward the parent, and the viewer scrolls to
 * reveal and select the parent.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class UpAction extends BrowserAction
{
    protected TreeViewer viewer;


    // ── Luke Gets His Footing in the Shaft ───────────────────────────────────
    // Before climbing, Luke identifies which shaft he's in — the specific
    // viewer (the specific vertical column of the structure) he'll be moving
    // through.  We store the tree viewer reference so run() can navigate it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code UpAction} bound to the given tree viewer.
     * We store the viewer reference so {@link #run()} can ask it for the
     * current content provider and push new selections back into it.
     *
     * @param viewer  the {@link TreeViewer} this action navigates; must not
     *                be {@code null}
     */
    public UpAction( TreeViewer viewer )
    {
        super();
        this.viewer = viewer;
    }


    // ── The Direction Is Simply: Up ───────────────────────────────────────────
    // Luke doesn't need a complicated name for his goal — "up" says everything.
    // We return the localized "Up" label for menus and tooltips.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the localized display label for this action — typically "Up".
     *
     * @return the localized label string; never {@code null}
     */
    public String getText()
    {
        return Messages.getString( "UpAction.Up" ); //$NON-NLS-1$
    }


    // ── Luke's Upward Path Is Marked by the Parent Icon ──────────────────────
    // Even in the dim reactor shaft, Luke can see the opening above — there's
    // a visual cue for which way is up.
    // We return the "parent" icon from the plugin's image registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action — the "parent/up" image from the
     * plugin's image registry.
     *
     * @return the {@link ImageDescriptor} for the up-navigation icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_PARENT );
    }


    // ── Luke's Exit Route Is on the Official Evacuation Plan ─────────────────
    // Every ship has an official exit path — Luke's shaft climb maps to the
    // Rebel fleet's registered evacuation corridors.
    // We return the Eclipse command ID that corresponds to this navigation.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action.  We reuse the
     * {@code CMD_OPEN_SEARCH_RESULT} ID because "Up" is conceptually about
     * jumping to a related node in the browser tree.
     *
     * @return the command ID string
     */
    public String getCommandId()
    {
        return BrowserCommonConstants.CMD_OPEN_SEARCH_RESULT;
    }


    // ── Luke Grabs the First Handhold He Finds ────────────────────────────────
    // Luke doesn't pause to deliberate — he grabs whatever is in front of him
    // (entry, search, search result, bookmark, paging node) and starts
    // climbing from it.  We pick up whatever is selected and navigate from it.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the action: identifies the current selection (whichever type is
     * present), asks the tree's content provider for its parent, then reveals
     * and selects that parent in the viewer.
     *
     * <p>For example — Luke climbs from his current grip to the next level:</p>
     * <pre>
     *   Luke looks up (contentProvider.getParent(selection)).
     *   He hauls himself up (viewer.reveal(newSelection)).
     *   The shaft recedes below: the parent node is now in focus.
     * </pre>
     *
     * <p>Priority order for determining the "current" selection: entry &gt;
     * search &gt; search result &gt; bookmark &gt; browser entry page &gt;
     * browser search result page.</p>
     */
    public void run()
    {
        IEntry[] entries = getSelectedEntries();
        ISearch[] searches = getSelectedSearches();
        ISearchResult[] searchResults = getSelectedSearchResults();
        IBookmark[] bookmarks = getSelectedBookmarks();
        BrowserEntryPage[] browserEntryPages = getSelectedBrowserEntryPages();
        BrowserSearchResultPage[] browserSearchResultPages = getSelectedBrowserSearchResultPages();

        Object selection = null;

        if ( entries.length > 0 )
        {
            selection = entries[0];
        }
        else if ( searches.length > 0 )
        {
            selection = searches[0];
        }
        else if ( searchResults.length > 0 )
        {
            selection = searchResults[0];
        }
        else if ( bookmarks.length > 0 )
        {
            selection = bookmarks[0];
        }
        else if ( browserEntryPages.length > 0 )
        {
            selection = browserEntryPages[0];
        }
        else if ( browserSearchResultPages.length > 0 )
        {
            selection = browserSearchResultPages[0];
        }

        if ( selection != null )
        {
            ITreeContentProvider contentProvider = ( ITreeContentProvider ) viewer.getContentProvider();
            Object newSelection = contentProvider.getParent( selection );
            viewer.reveal( newSelection );
            viewer.setSelection( new StructuredSelection( newSelection ), true );
        }
    }


    // ── Luke Can Climb Only If He Has Something to Push Off From ─────────────
    // Luke can't climb if he's floating in empty space with nothing to grab.
    // He needs at least one foothold — one selected item in the tree — to
    // start moving upward from.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled.  We need at least one
     * item of any navigable type selected in the tree — entry, search, search
     * result, bookmark, or paging page — to have something to navigate "up"
     * from.
     *
     * @return {@code true} if at least one navigable item is selected;
     *         {@code false} otherwise
     */
    public boolean isEnabled()
    {
        IEntry[] entries = getSelectedEntries();
        ISearch[] searches = getSelectedSearches();
        ISearchResult[] searchResults = getSelectedSearchResults();
        IBookmark[] bookmarks = getSelectedBookmarks();
        BrowserEntryPage[] browserEntryPages = getSelectedBrowserEntryPages();
        BrowserSearchResultPage[] browserSearchResultPages = getSelectedBrowserSearchResultPages();

        return entries.length > 0 || searches.length > 0 || searchResults.length > 0 || bookmarks.length > 0
            || browserEntryPages.length > 0 || browserSearchResultPages.length > 0;
    }
}
