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

package org.apache.directory.studio.ldapbrowser.ui.editors.entry;


import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: LocateSearchResultOrBookmarkAction — R2-D2 FINDS A SPECIFIC RECORD ──
// R2-D2 has already located the general sector; now he drills down further —
// not just "the tractor beam level" but the exact search result or pre-marked
// location in the Imperial archives that Leia's message pointed to.
// LocateSearchResultOrBookmarkAction extends the base locate action to handle
// the case where the editor input is a search result or bookmark rather than a
// plain entry — it navigates to that search result row or bookmark node instead.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Extends {@link LocateEntryInLdapBrowserAction} to locate search results and bookmarks.
 * When the entry editor is showing a search result or a bookmark (not a plain entry),
 * the "Show In" menu needs a second action that navigates to the result row in the
 * Searches view (or the bookmark node in the Bookmarks view) rather than to the DIT.
 * Think of this as R2 zooming in on a specific pre-marked archive record rather than
 * just navigating to the general sector.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LocateSearchResultOrBookmarkAction extends LocateEntryInLdapBrowserAction
{
    // ── R2 LINKS UP WITH THE SPECIFIC RECORD HANDLER ─────────────────────────
    // R2 hooks into the more specialized archive subsystem — the one that handles
    // pre-saved search results and bookmarked locations specifically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates the action, delegating setup to the superclass.
     * The parameters are the same as {@link LocateEntryInLdapBrowserAction} —
     * we just override the behavior in {@link #run()}, {@link #getText()}, and
     * {@link #getImageDescriptor()} to target search results and bookmarks.
     *
     * @param entryEditor       The entry editor whose input we'll locate.
     * @param showInMenuManager The menu manager that holds the current resolved input.
     */
    public LocateSearchResultOrBookmarkAction( EntryEditor entryEditor, EntryEditorShowInMenuManager showInMenuManager )
    {
        super( entryEditor, showInMenuManager );
    }


    // ── R2 NAVIGATES TO THE SPECIFIC SEARCH-RESULT OR BOOKMARK RECORD ─────────
    // R2 queries the Imperial archive for the exact search-result row or bookmarked
    // location that the mission plan referenced, not the underlying raw entry node.
    // We select the raw input object (ISearchResult or IBookmark) directly rather
    // than resolving it down to the underlying IEntry first.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Navigates to the search result or bookmark in the Browser view.
     * Unlike the parent which resolves to an {@link IEntry}, we pass the original
     * input object ({@link ISearchResult} or {@link IBookmark}) to {@link #select(Object)}
     * so the Browser view can highlight the correct row in its Searches or Bookmarks section.
     */
    public void run()
    {
        Object input = showInMenuManager.getInput();

        if ( input != null )
        {
            select( input );
        }
    }


    // ── R2 LABELS THE BUTTON BASED ON WHAT KIND OF RECORD THIS IS ────────────
    // R2 checks whether the target is a search result or a bookmark and picks
    // the right label — he knows the difference and uses the correct terminology.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a label that reflects whether the current input is a search result or a bookmark.
     * The label changes dynamically based on the actual input type so the menu item
     * always says "Searches" or "Bookmarks" rather than a generic string.
     *
     * @return the localized menu label for the current input type, or the superclass label if unknown.
     */
    public String getText()
    {
        Object input = showInMenuManager.getInput();

        if ( input != null )
        {
            if ( input instanceof ISearchResult )
            {
                return Messages.getString( "LocateSearchResultOrBookmarkAction.Searches" ); //$NON-NLS-1$
            }
            else if ( input instanceof IBookmark )
            {
                return Messages.getString( "LocateSearchResultOrBookmarkAction.Bookmarks" ); //$NON-NLS-1$
            }
        }

        return super.getText();
    }


    // ── R2 ATTACHES THE RIGHT ICON FOR THE ARCHIVE TYPE ──────────────────────
    // R2 picks the right archive badge — a magnifying glass for search results,
    // a flag icon for bookmarks — so users recognize the target at a glance.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon that matches the current input type.
     * Search results get the "locate search result in DIT" icon; bookmarks get
     * the "locate bookmark in DIT" icon; unknown inputs fall back to the superclass icon.
     *
     * @return the {@link ImageDescriptor} appropriate for the current input type.
     */
    public ImageDescriptor getImageDescriptor()
    {
        Object input = showInMenuManager.getInput();

        if ( input != null )
        {
            if ( input instanceof ISearchResult )
            {
                return BrowserUIPlugin.getDefault().getImageDescriptor(
                    BrowserUIConstants.IMG_LOCATE_SEARCHRESULT_IN_DIT );
            }
            else if ( input instanceof IBookmark )
            {
                return BrowserUIPlugin.getDefault().getImageDescriptor(
                    BrowserUIConstants.IMG_LOCATE_BOOKMARK_IN_DIT );
            }
        }

        return super.getImageDescriptor();
    }
}
