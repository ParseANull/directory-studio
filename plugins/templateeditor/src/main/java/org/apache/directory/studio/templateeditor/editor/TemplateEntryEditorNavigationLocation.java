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
package org.apache.directory.studio.templateeditor.editor;


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


// ── CLASS: TemplateEntryEditorNavigationLocation — C-3PO LOGGING THE ROUTE ───────
// When C-3PO navigates the Millennium Falcon's route from Tatooine to Alderaan, he
// records every waypoint in the ship's navicomputer: which system, which connection,
// what the entry point was. If they need to backtrack, he reads those coordinates
// back out and jumps to exactly the same spot. This class does the same thing for
// Eclipse's Back/Forward navigation: it records the current LDAP entry (DN,
// connection, entry type) to a memento, and restores it later so the editor can
// jump back to that exact entry.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse navigation history entry for the template entry editor. When the user
 * navigates between LDAP entries in the editor, Eclipse uses this class to record
 * and restore navigation waypoints — supporting the workbench Back and Forward
 * buttons. The state is persisted to an {@link IMemento} (XML-backed key-value
 * store) and restored on demand.
 * Think of this as C-3PO logging each hyperspace jump in the navicomputer so the
 * ship can retrace its route.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TemplateEntryEditorNavigationLocation extends NavigationLocation
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


    // ── CONSTRUCTOR: LOG THE CURRENT POSITION ─────────────────────────────────────
    // C-3PO is handed the navicomputer console (the editor part) and is now
    // responsible for recording its current position. The superclass
    // NavigationLocation stores the editor reference so we can read its input
    // later during getText(), saveState(), and mergeInto().
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a navigation location snapshot for the given editor. The snapshot
     * captures the editor's current input (LDAP entry, search result, or bookmark)
     * so Eclipse can restore it when the user clicks Back or Forward.
     *
     * <p>For example — C-3PO logs the current position:</p>
     * <pre>
     *   new TemplateEntryEditorNavigationLocation(templateEditor);
     *   // "Current position logged: cn=Luke, dc=rebels, dc=org"
     * </pre>
     *
     * @param editor  the template entry editor whose current input to record
     */
    protected TemplateEntryEditorNavigationLocation( IEditorPart editor )
    {
        super( editor );
    }


    // ── GET TEXT: WHAT DOES THE NAVICOMPUTER SHOW FOR THIS WAYPOINT? ─────────────
    // C-3PO reads the waypoint label from the log: usually the LDAP entry's
    // distinguished name or display name. This text appears in Eclipse's navigation
    // history dropdown. Falls back to the superclass text if the entry input is null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the human-readable label for this navigation waypoint — typically the
     * LDAP entry's distinguished name. Eclipse shows this in the navigation history
     * dropdown. Falls back to {@link NavigationLocation#getText()} if the entry
     * editor input is {@code null}.
     *
     * @return a display label for this history entry; never {@code null}
     */
    public String getText()
    {
        String text = EntryEditorUtils.getHistoryNavigationText( getEntryEditorInput() );
        return text != null ? text : super.getText();
    }


    // ── SAVE STATE: WRITE THE WAYPOINT TO THE NAVICOMPUTER ───────────────────────
    // C-3PO writes the current position into the ship's log: connection ID, DN,
    // entry type (direct entry, search result, or bookmark), and the editor extension
    // ID so we know which editor to reopen on restore. The IMemento is Eclipse's
    // XML-backed key-value store for persistent workbench state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Persists the current editor input to the given {@link IMemento}. Records
     * the entry type (IEntry, ISearchResult, or IBookmark), the connection ID,
     * the DN (or bookmark/search name), and the editor extension ID so
     * {@link #restoreState(IMemento)} can reconstruct the exact input later.
     *
     * <p>For example — C-3PO writes the waypoint to the navicomputer:</p>
     * <pre>
     *   memento.putString(CONNECTION_TAG, connection.getId());
     *   memento.putString(DN_TAG, entry.getDn().getName());
     *   // "Waypoint saved: Alderaan system, cn=Leia, dc=rebels, dc=org"
     * </pre>
     *
     * @param memento  the Eclipse memento to write into; never {@code null}
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


    // ── RESTORE STATE: JUMP TO THE LOGGED COORDINATES ────────────────────────────
    // C-3PO reads the navicomputer log and reconstructs the route: which connection,
    // which DN, which entry type. We look up the live objects (IEntry, ISearch,
    // IBookmark) from the browser's connection manager and reconstitute the
    // EntryEditorInput so the editor can show the right entry.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reconstructs the editor input from the previously saved {@link IMemento}.
     * Looks up the live {@link IBrowserConnection}, then resolves the LDAP entry,
     * search result, or bookmark by DN/name. Sets the restored input via
     * {@link NavigationLocation#setInput(Object)} so the editor can re-display it.
     *
     * <p>For example — C-3PO restores the navicomputer coordinates:</p>
     * <pre>
     *   Dn dn = new Dn(memento.getString(DN_TAG));
     *   IEntry entry = connection.getEntryFromCache(dn);
     *   setInput(new EntryEditorInput(entry, extension));
     *   // "Jump coordinates restored. Entering hyperspace."
     * </pre>
     *
     * @param memento  the Eclipse memento to read from; never {@code null}
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


    // ── RESTORE LOCATION: NO-OP — NAVIGATION HANDLED ELSEWHERE ──────────────────
    // Eclipse calls this when the user actually clicks Back/Forward. For this editor
    // type, the actual navigation is handled via showEditorInput() on the editor
    // itself — we don't need to do anything additional here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — the actual "navigate to this location" behavior is handled by
     * {@link TemplateEntryEditor#showEditorInput(org.eclipse.ui.IEditorInput)}.
     * Eclipse calls this method but the framework handles the editor reactivation
     * before we get here.
     */
    public void restoreLocation()
    {
    }


    // ── MERGE INTO: AVOID DUPLICATE WAYPOINTS IN THE LOG ────────────────────────
    // C-3PO checks: "Are these coordinates the same as the last entry in the log?"
    // If the current location and the new one point to the same LDAP entry, we
    // merge them (return true) so the history doesn't accumulate duplicates when
    // the user refreshes the same entry multiple times.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Merges this navigation location into {@code currentLocation} if they represent
     * the same LDAP entry. Eclipse calls this before adding a new history entry to
     * avoid duplicating the same location. Returns {@code true} if the inputs are
     * equal (same entry), {@code false} otherwise.
     *
     * <p>For example — deduplicating the navicomputer log:</p>
     * <pre>
     *   if (entry.equals(other)) return true; // "Same location — no new entry."
     * </pre>
     *
     * @param currentLocation  the most recent history entry; may be {@code null}
     * @return {@code true} if this and {@code currentLocation} represent the same entry
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

        TemplateEntryEditorNavigationLocation location = ( TemplateEntryEditorNavigationLocation ) currentLocation;
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


    // ── UPDATE: NO-OP — LOCATION IS IMMUTABLE ────────────────────────────────────
    // Navigation location snapshots don't change after creation — they record a
    // fixed point in time. Eclipse calls this as part of the location lifecycle
    // but we have nothing to update.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — navigation location snapshots are immutable once created.
     * Eclipse calls this as part of the {@link INavigationLocation} lifecycle
     * but there is nothing to update here.
     */
    public void update()
    {
    }


    // ── GET ENTRY EDITOR INPUT: READ THE CURRENT DOSSIER FROM THE EDITOR ─────────
    // Internal helper that casts the raw input (stored by the superclass) to
    // EntryEditorInput — our LDAP-specific subclass that carries the entry, the
    // connection, and the working copy. Returns null if the cast fails (shouldn't
    // happen in normal usage but guarding against it keeps things safe).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the current editor input cast to {@link EntryEditorInput}.
     * Used internally by {@link #getText()}, {@link #saveState(IMemento)}, and
     * {@link #mergeInto(INavigationLocation)}.
     *
     * @return the current {@link EntryEditorInput}, or {@code null} if not set
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


    // ── TO STRING: A QUICK LABEL FOR DEBUGGING ───────────────────────────────────
    // C-3PO reads the waypoint entry aloud: "Destination: cn=Luke,dc=rebels,dc=org."
    // This is used in debug output and Eclipse log messages when navigation history
    // entries need a human-readable label.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a string representation of this navigation location — the raw input
     * object's {@link Object#toString()} value, which typically shows the LDAP
     * entry's distinguished name.
     *
     * @return a string label for this navigation location
     */
    public String toString()
    {
        return "" + getEntryEditorInput().getInput(); //$NON-NLS-1$
    }
}
