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
package org.apache.directory.studio.combinededitor.editor;


import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.entryeditors.EntryEditorExtension;
import org.apache.directory.studio.entryeditors.EntryEditorInput;
import org.apache.directory.studio.entryeditors.EntryEditorManager;
import org.apache.directory.studio.entryeditors.EntryEditorUtils;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;


// ── CLASS: CombinedEntryEditorNavigationLocation — C-3PO Logs the Ship's Position ──
// C-3PO meticulously records the Tantive IV's exact position in hyperspace at
// each moment of the journey — connection ID, sector coordinates (DN), and
// search context — so the crew can reverse course and return to any prior
// position at the push of a button.
// CombinedEntryEditorNavigationLocation is our equivalent: it serialises the
// editor's current entry (connection + DN + entry type) into an Eclipse
// IMemento so the user can use the browser's back and forward buttons to revisit
// previously viewed LDAP entries.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a point in the workbench navigation history for the combined entry editor.
 * Eclipse's navigation history (the back/forward buttons in the toolbar) uses
 * this class to snapshot what entry the editor was showing at a given moment,
 * persist it (optionally to disk), and restore it later.
 * We support three types of editor input: a raw {@link IEntry}, an
 * {@link ISearchResult}, and an {@link IBookmark}.
 * Think of this as C-3PO logging the precise hyperspace coordinates so the
 * Tantive IV can jump back to any previous position.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CombinedEntryEditorNavigationLocation extends NavigationLocation
{
    private static final String BOOKMARK_TAG = "BOOKMARK"; //$NON-NLS-1$
    private static final String CONNECTION_TAG = "CONNECTION"; //$NON-NLS-1$
    private static final String DN_TAG = "DN"; //$NON-NLS-1$
    private static final String EXTENSION_TAG = "EXTENSION"; //$NON-NLS-1$
    private static final String SEARCH_TAG = "SEARCH"; //$NON-NLS-1$
    private static final String TYPE_BOOKMARK_VALUE = "IBookmark"; //$NON-NLS-1$
    private static final String TYPE_SEARCHRESULT_VALUE = "ISearchResult"; //$NON-NLS-1$
    private static final String TYPE_TAG = "TYPE"; //$NON-NLS-1$
    private static final String TYPE_ENTRY_VALUE = "IEntry"; //$NON-NLS-1$


    // ── C-3PO Opens a New Log Entry for the Current Position ──────────────────
    // C-3PO picks up his logbook, turns to a new page, and ties the log entry
    // to the specific editor (ship) it belongs to.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new navigation location snapshot for the given editor.
     * The parent class stores the editor reference so we can retrieve the
     * current input when {@link #saveState} or {@link #getText} is called.
     *
     * @param editor  the combined entry editor whose current input we're snapshotting.
     */
    protected CombinedEntryEditorNavigationLocation( IEditorPart editor )
    {
        super( editor );
    }


    // ── C-3PO Reads the Log Entry Aloud — Human-Readable Position ─────────────
    // When the crew asks "Where were we?", C-3PO reads the log entry aloud in
    // plain Galactic Basic so they understand which position is being referred to.
    // getText() returns the human-readable label for the navigation history popup.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable label for this history entry shown in the navigation popup.
     * We delegate to {@link EntryEditorUtils#getHistoryNavigationText} which builds
     * a DN-based string; if that returns {@code null} we fall back to the parent's default.
     *
     * @return  the navigation history label, never {@code null}.
     */
    public String getText()
    {
        String text = EntryEditorUtils.getHistoryNavigationText( getEntryEditorInput() );
        return text != null ? text : super.getText();
    }


    // ── C-3PO Writes the Position to the Log — Serialise to Memento ──────────
    // C-3PO carefully writes the current position into the ship's log book:
    // the connection ID, the DN, the type of reference (bookmark, search result,
    // or plain entry), and the editor extension being used.
    // saveState() serialises all this to an Eclipse IMemento so it can be
    // persisted across sessions.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serialises the current editor input into the given Eclipse {@link IMemento}.
     * We store enough data to reconstruct the exact entry later: the editor
     * extension ID, the connection ID, the entry DN, and (for search results or
     * bookmarks) the search name or bookmark name.
     *
     * @param memento  the Eclipse memento to write into — provided by the framework.
     */
    public void saveState( IMemento memento )
    {
        EntryEditorInput eei = getEntryEditorInput();
        if ( eei != null )
        {
            memento.putString( EXTENSION_TAG, eei.getExtension().getId() );
            if ( eei.getEntryInput() != null )
            {
                IEntry entry = eei.getEntryInput();
                memento.putString( TYPE_TAG, TYPE_ENTRY_VALUE );
                memento.putString( DN_TAG, entry.getDn().getName() );
                memento.putString( CONNECTION_TAG, entry.getBrowserConnection().getConnection().getId() );
            }
            else if ( eei.getSearchResultInput() != null )
            {
                ISearchResult searchResult = eei.getSearchResultInput();
                memento.putString( TYPE_TAG, TYPE_SEARCHRESULT_VALUE );
                memento.putString( DN_TAG, searchResult.getDn().getName() );
                memento.putString( SEARCH_TAG, searchResult.getSearch().getName() );
                memento.putString( CONNECTION_TAG, searchResult.getSearch().getBrowserConnection().getConnection()
                    .getId() );
            }
            else if ( eei.getBookmarkInput() != null )
            {
                IBookmark bookmark = eei.getBookmarkInput();
                memento.putString( TYPE_TAG, TYPE_BOOKMARK_VALUE );
                memento.putString( BOOKMARK_TAG, bookmark.getName() );
                memento.putString( CONNECTION_TAG, bookmark.getBrowserConnection().getConnection().getId() );
            }
        }
    }


    // ── C-3PO Reads the Log and Reconstructs the Previous Position ────────────
    // C-3PO opens the log book to the saved entry and reconstructs the full
    // position from the recorded coordinates — connection, DN, and entry type —
    // so the Tantive IV can jump back to exactly where it was.
    // restoreState() reads the memento and rebuilds the EntryEditorInput.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Deserialises a previously saved navigation location from an Eclipse {@link IMemento}.
     * We look up the connection, parse the DN, and reconstruct the correct
     * {@link EntryEditorInput} type (entry, search result, or bookmark).
     *
     * @param memento  the Eclipse memento written by a prior {@link #saveState} call.
     */
    public void restoreState( IMemento memento )
    {
        try
        {
            String type = memento.getString( TYPE_TAG );
            String extensionId = memento.getString( EXTENSION_TAG );
            EntryEditorManager entryEditorManager = BrowserUIPlugin.getDefault().getEntryEditorManager();
            EntryEditorExtension entryEditorExtension = entryEditorManager.getEntryEditorExtension( extensionId );
            if ( TYPE_ENTRY_VALUE.equals( type ) )
            {
                IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnectionById( memento.getString( CONNECTION_TAG ) );
                Dn dn = new Dn( memento.getString( DN_TAG ) );
                IEntry entry = connection.getEntryFromCache( dn );
                super.setInput( new EntryEditorInput( entry, entryEditorExtension ) );
            }
            else if ( TYPE_SEARCHRESULT_VALUE.equals( type ) )
            {
                IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnectionById( memento.getString( CONNECTION_TAG ) );
                ISearch search = connection.getSearchManager().getSearch( memento.getString( SEARCH_TAG ) );
                ISearchResult[] searchResults = search.getSearchResults();
                Dn dn = new Dn( memento.getString( DN_TAG ) );
                for ( int i = 0; i < searchResults.length; i++ )
                {
                    if ( dn.equals( searchResults[i].getDn() ) )
                    {
                        super.setInput( new EntryEditorInput( searchResults[i], entryEditorExtension ) );
                        break;
                    }
                }
            }
            else if ( TYPE_BOOKMARK_VALUE.equals( type ) )
            {
                IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnectionById( memento.getString( CONNECTION_TAG ) );
                IBookmark bookmark = connection.getBookmarkManager().getBookmark( memento.getString( BOOKMARK_TAG ) );
                super.setInput( new EntryEditorInput( bookmark, entryEditorExtension ) );
            }
        }
        catch ( LdapInvalidDnException e )
        {
            e.printStackTrace();
        }
    }


    // ── C-3PO Notes the Ship Has Arrived — Nothing Extra to Do ───────────────
    // The Tantive IV arrives at the logged position; C-3PO notes the arrival but
    // the actual navigation was handled by the editor itself.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — restoring the actual editor view is handled by the editor framework.
     * Eclipse calls this to tell us we've been navigated back to; the editor
     * will call {@link CombinedEntryEditor#showEditorInput} separately.
     */
    public void restoreLocation()
    {
    }


    // ── Is This the Same Position We Were Just At? ────────────────────────────
    // C-3PO checks whether the new log entry is the same as the current one —
    // if so, he merges them rather than creating a duplicate entry in the history.
    // mergeInto() prevents duplicate adjacent history entries for the same entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this location and the given current location refer to the same entry.
     * Eclipse uses this to suppress duplicate adjacent history entries — if you're
     * already showing entry X and you "navigate" to entry X again, we don't add
     * a second copy to the history stack.
     *
     * @param currentLocation  the location already at the top of the history stack.
     * @return                 {@code true} if both locations wrap the same entry input.
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

        CombinedEntryEditorNavigationLocation location = ( CombinedEntryEditorNavigationLocation ) currentLocation;
        Object other = location.getEntryEditorInput().getInput();
        Object entry = getEntryEditorInput().getInput();

        if ( other == null && entry == null )
        {
            return true;
        }
        else if ( other == null || entry == null )
        {
            return false;
        }
        else
        {
            return entry.equals( other );
        }
    }


    // ── C-3PO Checks Whether Any Data Has Changed ─────────────────────────────
    // C-3PO compares the last logged position to the current one — in our case
    // the comparison is handled externally, so this is a no-op.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we don't need to proactively update the stored location.
     * Eclipse calls this periodically to allow location objects to refresh their
     * state; we store a snapshot at creation time and don't change it.
     */
    public void update()
    {
    }


    // ── C-3PO Retrieves the Stored Entry Input ────────────────────────────────
    // C-3PO looks up the logged entry input from the parent's stored state,
    // casting it to the expected type for internal use.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the stored {@link EntryEditorInput} from the parent's input field.
     * This is the typed accessor used internally within this class.
     *
     * @return  the {@link EntryEditorInput}, or {@code null} if the stored input
     *          is not of that type.
     */
    private EntryEditorInput getEntryEditorInput()
    {
        Object editorInput = getInput();
        if ( editorInput instanceof EntryEditorInput )
        {
            EntryEditorInput entryEditorInput = ( EntryEditorInput ) editorInput;
            return entryEditorInput;
        }

        return null;
    }


    // ── C-3PO Reads the Coordinates Aloud for the Debug Log ──────────────────
    // C-3PO can always recite the logged coordinates on request — useful for
    // debugging and for the Eclipse navigation history tooltip.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a string representation of this navigation location.
     * Used in debugging and by Eclipse when displaying the location in tooltips.
     *
     * @return  a string describing the stored entry input.
     */
    public String toString()
    {
        return "" + getEntryEditorInput().getInput(); //$NON-NLS-1$
    }
}
