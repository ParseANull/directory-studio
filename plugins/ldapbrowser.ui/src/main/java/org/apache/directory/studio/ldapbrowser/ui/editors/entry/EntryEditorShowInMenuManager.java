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


import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;


// ── CLASS: EntryEditorShowInMenuManager — LANDO RUNNING CLOUD CITY ────────────
// Lando Calrissian oversees Cloud City: he knows every platform, every bay,
// every corridor — and when a guest needs to get somewhere, he assembles exactly
// the right set of directions from the options currently available.
// EntryEditorShowInMenuManager is that administrator: it knows the current editor
// input (entry, search result, or bookmark) and builds the "Show In" submenu with
// exactly the navigation actions that make sense for what's on screen right now.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Builds and manages the "Show In" submenu for the entry editor's context menu.
 * The "Show In" menu lets users jump from the entry editor to the LDAP Browser tree,
 * the Searches view, or the Bookmarks view — whichever makes sense for the current input.
 * Think of this class as Lando: he knows what's available in the city at any given moment
 * and routes guests to exactly the right destination.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorShowInMenuManager
{
    /** The entry editor */
    private EntryEditor entryEditor;

    /** The locate entry in DIT action */
    private LocateEntryInLdapBrowserAction locateEntryInDitAction;

    /** The locate search result or bookmark action */
    private LocateSearchResultOrBookmarkAction locateSearchResultOrBookmarkAction;


    // ── LANDO SETS UP THE WELCOME DESK ───────────────────────────────────────
    // Lando opens Cloud City for business: he assigns escorts to the entry-in-DIT
    // corridor and to the search-result/bookmark bay, ready to guide guests
    // to whichever destination the situation calls for.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new "Show In" menu manager tied to the given entry editor.
     * We pre-create both navigation actions here (one for locating entries in the
     * LDAP Browser DIT, one for locating search results or bookmarks) so they're
     * ready to be added to the menu when the context menu opens.
     *
     * @param entryEditor  The entry editor we're building navigation for.
     */
    public EntryEditorShowInMenuManager( EntryEditor entryEditor )
    {
        this.entryEditor = entryEditor;

        locateEntryInDitAction = new LocateEntryInLdapBrowserAction( entryEditor, this );
        locateSearchResultOrBookmarkAction = new LocateSearchResultOrBookmarkAction( entryEditor, this );
    }


    // ── LANDO OPENS THE GUEST DIRECTORY AND BUILDS A ROUTE ───────────────────
    // Lando checks his guest manifest — what kind of guest is this? An entry
    // visitor gets the DIT corridor tour; a search-result or bookmark guest
    // also gets the Searches/Bookmarks bay option. Everyone gets the generic
    // "Show In..." Eclipse menu at the bottom as a fallback.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Adds the "Show In" submenu to the given parent menu manager.
     * The submenu always contains the "Locate in LDAP Browser" action if input is non-null.
     * If the input is a search result or bookmark, we also add the locate-in-searches/bookmarks
     * action. The Eclipse-provided "Show In" submenu item is always appended last.
     *
     * @param parent  The parent menu manager into which we add the "Show In" submenu.
     */
    public void createMenuManager( IMenuManager parent )
    {
        MenuManager showInMenuManager = new MenuManager( Messages.getString( "EntryEditorShowInMenuManager.ShowIn" ) ); //$NON-NLS-1$
        parent.add( showInMenuManager );

        Object input = getInput();

        if ( input != null )
        {
            showInMenuManager.add( locateEntryInDitAction );

            if ( inputIsSearchResultOrBookmark() )
            {
                showInMenuManager.add( locateSearchResultOrBookmarkAction );
            }
        }

        showInMenuManager.add( ContributionItemFactory.VIEWS_SHOW_IN.create( PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow() ) );
    }


    // ── LANDO CONSULTS THE GUEST MANIFEST ────────────────────────────────────
    // Lando checks who's currently in the docking bay — pulling the guest record
    // so he can decide which corridors to open for them.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current editor input object — the LDAP entry, search result, or bookmark.
     * Actions use this to know what they're navigating to; the menu manager uses it to
     * decide which navigation options to include in the submenu.
     *
     * @return the current input object, or {@code null} if the editor has no input.
     */
    public Object getInput()
    {
        if ( entryEditor != null )
        {
            EntryEditorInput editorInput = entryEditor.getEntryEditorInput();

            if ( editorInput != null )
            {
                return editorInput.getInput();
            }
        }

        return null;
    }


    // ── LANDO CHECKS IF THE GUEST IS A SEARCH PARTY OR A BOOKMARKED VIP ─────
    // Not every guest in Cloud City is the same — some are ordinary visitors
    // (entries), others are part of a coordinated search party (ISearchResult)
    // or pre-registered VIPs (IBookmark). Lando checks the badge.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the current input is a search result or a bookmark.
     * Used to decide whether to include the "Locate in Searches/Bookmarks" action —
     * that action only makes sense when the user is viewing a result or bookmark,
     * not when they're viewing a plain entry directly.
     *
     * @return {@code true} if input is {@link ISearchResult} or {@link IBookmark}.
     */
    private boolean inputIsSearchResultOrBookmark()
    {
        Object input = getInput();

        return ( ( input instanceof ISearchResult ) || ( input instanceof IBookmark ) );
    }
}
