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

package org.apache.directory.studio.ldapbrowser.ui.actions;


import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: LocateEntryInDitAction — R2-D2 FINDS THE ENTRY IN THE DIT TREE ───
// R2-D2 is in the search results or bookmarks panel. He's already found the
// data he was looking for — now he needs to navigate the main browser tree to
// show where that entry actually lives in the directory hierarchy. Whether it's
// a search result row or a saved bookmark, R2 resolves the entry's real DN
// and tells the browser to scroll to it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Navigates the LDAP browser tree to the entry represented by the currently
 * selected search result or bookmark.
 * Used from the search results view and bookmark list: the user can right-click
 * a result and select "Show in DIT" (or similar) to jump the browser tree to
 * that entry's location. The action label and icon change contextually based
 * on whether a search result or bookmark is selected.
 * Think of R2-D2 triangulating the entry's physical location in the directory
 * tree and piloting the browser there.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LocateEntryInDitAction extends LocateInDitAction
{

    // ── R2 Boots His Locate-Entry Mode ────────────────────────────────────────
    // R2 initialises his entry-locator subroutine — no configuration needed,
    // the target comes from the live selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code LocateEntryInDitAction} with default state.
     * The entry to locate is derived from the current selection when the action fires.
     */
    public LocateEntryInDitAction()
    {
    }


    // ── R2 Labels the Navigation Function Contextually ───────────────────────
    // R2 knows whether he's tracking a search result or a bookmark, and he
    // labels the action accordingly: "Show Search Result", "Show Bookmark",
    // or generic "Show in DIT" if neither is exclusively selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a context-sensitive label: "Show Search Result" when a search result
     * is exclusively selected, "Show Bookmark" when a bookmark is exclusively
     * selected, or the generic "Show in DIT" label otherwise.
     *
     * @return  the localised display name; never {@code null}
     */
    public String getText()
    {
        if ( getSelectedSearchResults().length == 1
            && getSelectedBookmarks().length + getSelectedEntries().length + getSelectedBrowserViewCategories().length == 0 )
        {
            return Messages.getString( "LocateEntryInDitAction.ShowSearchResult" ); //$NON-NLS-1$
        }
        else if ( getSelectedBookmarks().length == 1
            && getSelectedSearchResults().length + getSelectedEntries().length
                + getSelectedBrowserViewCategories().length == 0 )
        {
            return Messages.getString( "LocateEntryInDitAction.ShowBookmark" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "LocateEntryInDitAction.ShowInDit" ); //$NON-NLS-1$
        }
    }


    // ── R2 Selects the Right Navigation Icon ──────────────────────────────────
    // R2 picks the icon that matches the type of thing he's navigating to —
    // a search-result icon, a bookmark icon, or the generic entry-locator icon.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a context-sensitive image descriptor: the search-result icon when
     * navigating to a search result, the bookmark icon when navigating to a
     * bookmark, or the generic "locate entry" icon otherwise.
     *
     * @return  the appropriate {@link ImageDescriptor}; never {@code null}
     */
    public ImageDescriptor getImageDescriptor()
    {
        if ( getSelectedSearchResults().length == 1
            && getSelectedBookmarks().length + getSelectedEntries().length + getSelectedBrowserViewCategories().length == 0 )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LOCATE_SEARCHRESULT_IN_DIT );
        }
        else if ( getSelectedBookmarks().length == 1
            && getSelectedSearchResults().length + getSelectedEntries().length
                + getSelectedBrowserViewCategories().length == 0 )
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LOCATE_BOOKMARK_IN_DIT );
        }
        else
        {
            return BrowserUIPlugin.getDefault().getImageDescriptor( BrowserUIConstants.IMG_LOCATE_ENTRY_IN_DIT );
        }
    }


    // ── R2 Resolves the Entry DN From the Selected Item ───────────────────────
    // R2 checks whether it's a search result or a bookmark that's selected,
    // and pulls the connection + DN pair from whichever one it is. If neither
    // is exclusively selected, he has no unambiguous target and returns null.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link ConnectionAndDn} bundle derived from the exclusively
     * selected search result or bookmark.
     * For a search result, uses the result entry's connection and DN.
     * For a bookmark, uses the bookmark's connection and stored DN.
     * Returns {@code null} if neither a search result nor a bookmark is
     * exclusively selected.
     *
     * @return  the connection and DN to navigate to, or {@code null} if not applicable
     */
    protected ConnectionAndDn getConnectionAndDn()
    {
        if ( getSelectedSearchResults().length == 1
            && getSelectedBookmarks().length + getSelectedEntries().length + getSelectedBrowserViewCategories().length == 0 )
        {
            return new ConnectionAndDn( getSelectedSearchResults()[0].getEntry().getBrowserConnection(),
                getSelectedSearchResults()[0].getEntry().getDn() );
        }
        else if ( getSelectedBookmarks().length == 1
            && getSelectedSearchResults().length + getSelectedEntries().length
                + getSelectedBrowserViewCategories().length == 0 )
        {
            return new ConnectionAndDn( getSelectedBookmarks()[0].getBrowserConnection(), getSelectedBookmarks()[0]
                .getDn() );
        }
        else
        {
            return null;
        }
    }
}
