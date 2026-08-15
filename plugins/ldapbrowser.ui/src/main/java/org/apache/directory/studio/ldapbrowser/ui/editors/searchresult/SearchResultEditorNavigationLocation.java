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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;


// ── CLASS: SearchResultEditorNavigationLocation — R2 Logging His Sector Position ──
// When R2-D2 moves through the Death Star, he doesn't just navigate forward —
// he logs each sector he visits so he can retrace his steps.  "I was in sector
// 7-G, then sector 4-J.  Let me go back."  That log entry holds the coordinates
// and the connection ID (which Death Star terminal he was on).
// This class is that log entry: it records which search was displayed and on which
// connection, so the Eclipse back/forward navigation history can reopen it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Marks a position in the Eclipse navigation history for the search result editor.
 * Each time the editor shows a new search, we create one of these to record
 * the search name and connection ID.  Eclipse's back/forward arrows use it to
 * navigate between previously viewed searches.
 * Think of this as R2-D2's position log in the Death Star computer system.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorNavigationLocation extends NavigationLocation
{

    // ── R2 Starts a New Log Entry ─────────────────────────────────────────────
    // R2 opens a new log record for this editor instance.  The actual search
    // position is filled in later as the editor's input changes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a navigation location for the given search result editor.
     * The position is empty until the editor calls {@link #update()} or the
     * superclass sets the input.
     *
     * @param editor the search result editor this location belongs to
     */
    SearchResultEditorNavigationLocation( SearchResultEditor editor )
    {
        super( editor );
    }


    // ── R2 Labels His Log Entry ───────────────────────────────────────────────
    // When the user hovers over the back/forward history, Eclipse calls getText()
    // to label each history entry.  R2 produces a human-readable label that names
    // the search and the connection it ran on.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this history entry.
     * We format it as "Search 'name' - connectionName" so the user can identify
     * which search this refers to in the navigation history dropdown.
     *
     * @return the formatted label string, or the superclass default if no search is available
     */
    public String getText()
    {
        ISearch search = getSearch();
        if ( search != null )
        {
            String connectionName = search.getBrowserConnection().getConnection() != null ? " - " //$NON-NLS-1$
                + search.getBrowserConnection().getConnection().getName() : ""; //$NON-NLS-1$
            return NLS.bind(
                Messages.getString( "SearchResultEditorNavigationLocation.Search" ), new String[] { search.getName() } ) //$NON-NLS-1$
                + connectionName;
        }
        else
        {
            return super.getText();
        }
    }


    // ── R2 Writes Down the Coordinates ────────────────────────────────────────
    // R2 commits the current position to persistent storage — search name and
    // connection ID — so it survives an Eclipse restart.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current search name and connection ID to the given memento.
     * Called by Eclipse when saving the navigation history.
     *
     * @param memento the memento to write into; we add "SEARCH" and "CONNECTION" keys
     */
    public void saveState( IMemento memento )
    {
        ISearch search = getSearch();
        memento.putString( "SEARCH", search.getName() ); //$NON-NLS-1$
        memento.putString( "CONNECTION", search.getBrowserConnection().getConnection().getId() ); //$NON-NLS-1$
    }


    // ── R2 Reads the Coordinates Back ────────────────────────────────────────
    // On restart, R2 retrieves his saved coordinates: connection ID → look up
    // the connection → look up the search by name → reconstruct the input.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Restores the search from the persisted state in the memento.
     * We look up the connection by ID and the search by name, then reconstruct
     * the editor input from them.
     *
     * @param memento the memento holding the "SEARCH" and "CONNECTION" keys
     */
    public void restoreState( IMemento memento )
    {
        IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager().getBrowserConnectionById(
            memento.getString( "CONNECTION" ) ); //$NON-NLS-1$
        ISearch search = connection.getSearchManager().getSearch( memento.getString( "SEARCH" ) ); //$NON-NLS-1$
        super.setInput( new SearchResultEditorInput( search ) );
    }


    // ── R2 Navigates Back to This Logged Position ─────────────────────────────
    // When the user clicks Back, Eclipse calls restoreLocation() on this log entry.
    // R2 tells the editor to load the stored input again.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Restores the editor to display the search stored in this location.
     * We cast the editor part to {@link SearchResultEditor} and call
     * {@link SearchResultEditor#setInput} with our stored input.
     */
    public void restoreLocation()
    {
        IEditorPart editorPart = getEditorPart();
        if ( editorPart instanceof SearchResultEditor )
        {
            SearchResultEditor searchResultEditor = ( SearchResultEditor ) editorPart;
            searchResultEditor.setInput( ( SearchResultEditorInput ) getInput() );
        }
    }


    // ── R2 Decides If Two Locations Are the Same ──────────────────────────────
    // R2 checks "have I already logged this position?" — if the current location
    // in the history stack is the same search, we merge rather than duplicate it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this location represents the same search as the current
     * top-of-stack location, in which case Eclipse won't add a duplicate history entry.
     *
     * @param currentLocation the current top-of-stack navigation location
     * @return {@code true} if both locations wrap the same {@link ISearch}
     */
    public boolean mergeInto( INavigationLocation currentLocation )
    {
        if ( currentLocation == null )
        {
            return false;
        }

        if ( getClass() != currentLocation.getClass() )
        {
            return false;
        }

        SearchResultEditorNavigationLocation location = ( SearchResultEditorNavigationLocation ) currentLocation;
        ISearch other = location.getSearch();
        ISearch search = getSearch();

        if ( other == null && search == null )
        {
            return true;
        }
        else if ( other == null || search == null )
        {
            return false;
        }
        else
        {
            return search.equals( other );
        }
    }


    // ── R2 Notes That No Update Is Needed ────────────────────────────────────
    // Some navigation locations update their state dynamically (e.g. text editor
    // cursor position) but ours is static — the search doesn't move.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — the search location doesn't change after it's created.
     */
    public void update()
    {
    }


    // ── R2 Decodes the Search From the Current Input ──────────────────────────
    // A private helper: extract the ISearch from whatever input the editor currently holds.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the {@link ISearch} from the current editor input.
     * Returns {@code null} if the input is null, not a {@link SearchResultEditorInput},
     * or contains a null search.
     *
     * @return the search wrapped by the current input, or {@code null}
     */
    private ISearch getSearch()
    {
        Object editorInput = getInput();
        if ( editorInput instanceof SearchResultEditorInput )
        {
            SearchResultEditorInput searchResultEditorInput = ( SearchResultEditorInput ) editorInput;
            ISearch search = searchResultEditorInput.getSearch();
            if ( search != null )
            {
                return search;
            }
        }

        return null;
    }


    // ── R2 Produces a Debug String ────────────────────────────────────────────
    // Useful for logging: the search's toString gives us enough context.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a string representation suitable for debugging.
     *
     * @return the search's toString, or empty string if no search is set
     */
    public String toString()
    {
        return "" + getSearch(); //$NON-NLS-1$
    }

}
