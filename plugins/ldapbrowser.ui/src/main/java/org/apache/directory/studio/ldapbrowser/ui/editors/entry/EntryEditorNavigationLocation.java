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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.NavigationLocation;


// ── CLASS: EntryEditorNavigationLocation — R2-D2 PLUGGING INTO THE DEATH STAR ──
// R2-D2 jacks into the Death Star's computer port, reads the current sector
// coordinates, serializes them to his memory banks, and can later reload them
// to navigate right back to the same location — even after a power cycle.
// EntryEditorNavigationLocation is that memory bank: it captures an LDAP entry's
// DN (and connection/search/bookmark context) so Eclipse's Back/Forward history
// can jump back to the exact entry the user was looking at.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A snapshot of what the entry editor was displaying at a point in time.
 * Eclipse's navigation history system uses this to implement the Back and Forward
 * buttons in the workbench toolbar — when the user navigates to a different entry,
 * we create one of these to remember where they came from.
 * Think of this as R2's memory bank: it records the current LDAP address (DN +
 * connection + entry type) and can reload it later to restore the exact view.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorNavigationLocation extends NavigationLocation
{

    // ── R2 JACKS INTO THE DEATH STAR PORT ────────────────────────────────────
    // R2-D2 inserts his interface arm into the computer terminal on the Death Star,
    // establishing the connection that makes all later queries possible.
    // We bind this navigation location to its editor so Eclipse can ask us
    // later to restore the editor state when the user hits Back.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation location associated with the given entry editor.
     * Eclipse will call {@link #saveState(IMemento)} immediately after construction
     * to serialize the current entry into the workbench's persistent history.
     *
     * @param editor  The entry editor whose current input this location represents.
     */
    EntryEditorNavigationLocation( EntryEditor editor )
    {
        super( editor );
    }


    // ── R2 DISPLAYS THE SECTOR LABEL ON HIS DOME ──────────────────────────────
    // R2-D2 beeps out a human-readable label for the sector he's currently in —
    // "Detention Block AA-23" — so Leia can read it on the screen above.
    // We return the entry's display name so Eclipse can show a meaningful
    // label for this history entry in the navigation dropdown.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable label for this history entry.
     * Eclipse shows this text in the navigation history dropdown so users
     * can recognize which entry they'd be going back to.
     *
     * @return the entry's display name, or the superclass default if we can't compute one.
     */
    public String getText()
    {
        String text = EntryEditorUtils.getHistoryNavigationText( getEntryEditorInput() );
        return text != null ? text : super.getText();
    }


    // ── R2 WRITES THE COORDINATES TO HIS MEMORY BANKS ────────────────────────
    // R2 encodes the Death Star's current sector coordinates into a compact
    // binary format and stores them in his internal memory for later retrieval.
    // We serialize the entry type (IEntry / ISearchResult / IBookmark), DN,
    // connection ID, and search name into the Eclipse memento so the workbench
    // can persist this history entry across restarts.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Serializes this navigation location into an Eclipse {@link IMemento} for persistence.
     * Called by Eclipse when the workbench is saving session state — we write
     * enough information to recreate the full {@link EntryEditorInput} on restore:
     * the entry type tag, DN, connection ID, and (for search results) the search name.
     *
     * @param memento  The Eclipse memento to write our state into.
     */
    public void saveState( IMemento memento )
    {
        EntryEditorInput eei = getEntryEditorInput();
        if ( eei != null )
        {
            memento.putString( "EXTENSION", eei.getExtension().getId() ); //$NON-NLS-1$
            if ( eei.getEntryInput() != null )
            {
                IEntry entry = eei.getEntryInput();
                memento.putString( "TYPE", "IEntry" ); //$NON-NLS-1$ //$NON-NLS-2$
                memento.putString( "Dn", entry.getDn().getName() ); //$NON-NLS-1$
                memento.putString( "CONNECTION", entry.getBrowserConnection().getConnection().getId() ); //$NON-NLS-1$
            }
            else if ( eei.getSearchResultInput() != null )
            {
                ISearchResult searchResult = eei.getSearchResultInput();
                memento.putString( "TYPE", "ISearchResult" ); //$NON-NLS-1$ //$NON-NLS-2$
                memento.putString( "Dn", searchResult.getDn().getName() ); //$NON-NLS-1$
                memento.putString( "SEARCH", searchResult.getSearch().getName() ); //$NON-NLS-1$
                memento.putString(
                    "CONNECTION", searchResult.getSearch().getBrowserConnection().getConnection().getId() ); //$NON-NLS-1$
            }
            else if ( eei.getBookmarkInput() != null )
            {
                IBookmark bookmark = eei.getBookmarkInput();
                memento.putString( "TYPE", "IBookmark" ); //$NON-NLS-1$ //$NON-NLS-2$
                memento.putString( "BOOKMARK", bookmark.getName() ); //$NON-NLS-1$
                memento.putString( "CONNECTION", bookmark.getBrowserConnection().getConnection().getId() ); //$NON-NLS-1$
            }
        }
    }


    // ── R2 READS HIS MEMORY BANKS AND NAVIGATES BACK ─────────────────────────
    // R2 decodes the stored coordinates from his memory banks, looks up the
    // sector in the Death Star's current map, and reconstructs the target location.
    // We rebuild the full EntryEditorInput from the memento — resolving the
    // connection, looking up the DN in the cache, and re-finding the search result.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Rebuilds the {@link EntryEditorInput} from a previously saved memento.
     * Eclipse calls this at startup when restoring session history, or when
     * the user navigates back to a history entry that was saved in a prior session.
     * We re-resolve the connection, DN, and search/bookmark references from the stored IDs.
     *
     * @param memento  The memento containing the state we wrote in {@link #saveState(IMemento)}.
     */
    public void restoreState( IMemento memento )
    {
        try
        {
            String type = memento.getString( "TYPE" ); //$NON-NLS-1$
            String extensionId = memento.getString( "EXTENSION" ); //$NON-NLS-1$
            EntryEditorManager entryEditorManager = BrowserUIPlugin.getDefault().getEntryEditorManager();
            EntryEditorExtension entryEditorExtension = entryEditorManager.getEntryEditorExtension( extensionId );
            if ( "IEntry".equals( type ) ) //$NON-NLS-1$
            {
                IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnectionById( memento.getString( "CONNECTION" ) ); //$NON-NLS-1$
                Dn dn = new Dn( memento.getString( "Dn" ) ); //$NON-NLS-1$
                IEntry entry = connection.getEntryFromCache( dn );
                super.setInput( new EntryEditorInput( entry, entryEditorExtension ) );
            }
            else if ( "ISearchResult".equals( type ) ) //$NON-NLS-1$
            {
                IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnectionById( memento.getString( "CONNECTION" ) ); //$NON-NLS-1$
                ISearch search = connection.getSearchManager().getSearch( memento.getString( "SEARCH" ) ); //$NON-NLS-1$
                ISearchResult[] searchResults = search.getSearchResults();
                Dn dn = new Dn( memento.getString( "Dn" ) ); //$NON-NLS-1$
                for ( int i = 0; i < searchResults.length; i++ )
                {
                    if ( dn.equals( searchResults[i].getDn() ) )
                    {
                        super.setInput( new EntryEditorInput( searchResults[i], entryEditorExtension ) );
                        break;
                    }
                }
            }
            else if ( "IBookmark".equals( type ) ) //$NON-NLS-1$
            {
                IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnectionById( memento.getString( "CONNECTION" ) ); //$NON-NLS-1$
                IBookmark bookmark = connection.getBookmarkManager().getBookmark( memento.getString( "BOOKMARK" ) ); //$NON-NLS-1$
                super.setInput( new EntryEditorInput( bookmark, entryEditorExtension ) );
            }
        }
        catch ( LdapInvalidDnException e )
        {
            e.printStackTrace();
        }
    }


    // ── R2 NAVIGATES TO THE STORED LOCATION ──────────────────────────────────
    // R2 was supposed to navigate the ship back to Tatooine — but the hyperspace
    // coordinates are already baked into the editor input, so nothing extra needed.
    // This is intentionally empty; input restoration is handled by restoreState.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — restoring the editor state is done via input restoration, not here.
     * Eclipse calls this after {@link #restoreState(IMemento)} to trigger any
     * additional navigation; we don't need extra steps beyond what restoreState did.
     */
    public void restoreLocation()
    {
    }


    // ── R2 CHECKS IF THIS PORT LEADS TO THE SAME SECTOR ──────────────────────
    // Before plugging in again, R2 checks if the current computer port is already
    // connected to the same sector he was just in — no point logging duplicate locations.
    // We compare input objects to avoid duplicate adjacent entries in the history stack.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if this location represents the same entry as {@code currentLocation}.
     * Eclipse uses this to collapse adjacent identical history entries — navigating
     * back and forth between the same two entries shouldn't grow the stack indefinitely.
     *
     * @param currentLocation  The most recent location on the history stack to compare against.
     * @return {@code true} if both locations point to the same LDAP input object.
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

        EntryEditorNavigationLocation location = ( EntryEditorNavigationLocation ) currentLocation;
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


    // ── R2 REFRESHES HIS INTERNAL MAP ────────────────────────────────────────
    // R2 receives a signal to sync his local sector map — but in this case
    // the map is already current and no action is needed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we don't need to update any state when Eclipse asks us to refresh.
     * Eclipse calls this after the editor input changes, but we rebuild lazily.
     */
    public void update()
    {
    }


    // ── R2 RETRIEVES THE SECTOR DOSSIER FROM HIS MEMORY ──────────────────────
    // R2 reaches into his memory banks and pulls out the typed sector dossier —
    // the raw coordinates packaged in a format the mission planners can read.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Extracts the stored input as an {@link EntryEditorInput}, or returns {@code null}.
     * The base class stores an untyped {@code Object}; this helper casts it safely
     * so callers get a typed reference without repeated instanceof checks.
     *
     * @return the {@link EntryEditorInput} stored in this location, or {@code null} if none.
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


    // ── R2 BEEPS OUT A SUMMARY FOR THE REBELS ────────────────────────────────
    // R2 translates his internal coordinates into a short readable status beep
    // that C-3PO can relay to the rest of the team in plain language.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a string representation of this navigation location for debugging.
     * Eclipse may also use this for display in some history UIs.
     *
     * @return a string showing the underlying LDAP input object.
     */
    public String toString()
    {
        return "" + getEntryEditorInput().getInput(); //$NON-NLS-1$
    }

}
