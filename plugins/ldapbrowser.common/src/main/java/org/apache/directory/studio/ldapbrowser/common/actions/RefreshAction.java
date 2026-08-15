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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.SearchRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IContinuation.State;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: RefreshAction — LUKE CLEARS HIS MIND ON DAGOBAH ───────────────────
// During his training on Dagobah, whenever Luke's connection to the Force grows
// cloudy from effort or frustration, Yoda tells him: "Still your mind.  Breathe.
// Reach out again."  Luke lets go of his assumptions, opens himself up fresh,
// and reconnects with the living Force — seeing what's actually there, not what
// he remembered.  That's Refresh in a nutshell: we drop our cached view of the
// LDAP directory and ask the server what's really there right now.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Handles the "Refresh" action in the LDAP browser.  Depending on what's
 * selected, we'll reload entry children, re-run searches, reload attributes, or
 * any combination thereof — all by firing background jobs that hit the live LDAP
 * server and flush our local cache.
 *
 * <p>LDAP data can change at any time (other admins, automated processes).  Refresh
 * is how we stop trusting our stale snapshot and go ask the server: "What's
 * actually there right now?"</p>
 *
 * <p>Think of this class as Luke clearing his mind on Dagobah: we throw out
 * what we thought we knew and re-feel the Force (the live directory) from
 * scratch.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RefreshAction extends BrowserAction
{
    // ── Luke Settles into Meditation Posture ──────────────────────────────────
    // Luke sits cross-legged in the mud, ready to clear his mind whenever the
    // moment calls for it.  No special setup required.
    // Our constructor is equally simple — just call the parent.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code RefreshAction}.  Delegates to the parent
     * constructor which handles standard Eclipse action setup.
     */
    public RefreshAction()
    {
        super();
    }


    // ── Yoda Names the Exercise Based on the Situation ───────────────────────
    // Yoda adapts what he calls each exercise depending on what Luke needs:
    // "Reload your awareness," "Search again," "Perform the scan" — the label
    // changes with the context.
    // Our label similarly adapts: "Reload Entry," "Search Again," "Perform
    // Search" — based on what's selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a context-sensitive display label for this action.  The label
     * adapts based on what's selected:
     * <ul>
     *   <li>Entries selected → "Reload Entry" / "Reload Entries"</li>
     *   <li>Searches selected → "Search Again" or "Perform Search(es)"</li>
     *   <li>Entry editor input → "Reload Attributes"</li>
     *   <li>Search editor input → "Perform Search" / "Search Again"</li>
     *   <li>Anything else → generic "Refresh"</li>
     * </ul>
     *
     * @return the localized, context-sensitive action label; never {@code null}
     */
    public String getText()
    {
        List<IEntry> entries = getEntries();
        ISearch[] searches = getSearches();
        IEntry entryInput = getEntryInput();
        ISearch searchInput = getSearchInput();

        if ( entries.size() > 0 && searches.length == 0 && entryInput == null && searchInput == null )
        {
            return entries.size() == 1 ? Messages.getString( "RefreshAction.ReloadEntry" ) : Messages.getString( "RefreshAction.ReloadEntries" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else if ( searches.length > 0 && entries.size() == 0 && entryInput == null && searchInput == null )
        {
            boolean searchAgain = true;
            for ( int i = 0; i < searches.length; i++ )
            {
                if ( searches[i].getSearchResults() == null )
                {
                    searchAgain = false;
                    break;
                }
            }
            if ( searchAgain )
            {
                return Messages.getString( "RefreshAction.SearchAgain" ); //$NON-NLS-1$
            }
            else
            {
                return searches.length == 1 ? Messages.getString( "RefreshAction.PerformSearch" ) : Messages.getString( "RefreshAction.PerformSearches" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else if ( entryInput != null && searches.length == 0 && entries.size() == 0 && searchInput == null )
        {
            return Messages.getString( "RefreshAction.RelaodAttributes" ); //$NON-NLS-1$
        }
        else if ( searchInput != null && searches.length == 0 && entryInput == null )
        {
            return searchInput.getSearchResults() == null ? Messages.getString( "RefreshAction.PerformSearch" ) : Messages.getString( "RefreshAction.SearchAgain" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else
        {
            return Messages.getString( "RefreshAction.Refresh" ); //$NON-NLS-1$
        }
    }


    // ── Luke's Meditation Cushion Has a Recognizable Symbol ──────────────────
    // Even in the dark swamp, you can spot the icon for "Luke meditating" —
    // the circular refresh glyph that says "start over, see fresh."
    // We return the refresh icon from our plugin image registry.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action — the standard "refresh" image from the
     * plugin's image registry.
     *
     * @return the {@link ImageDescriptor} for the refresh icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_REFRESH );
    }


    // ── The Meditation Exercise Is in Eclipse's Official Logs ─────────────────
    // The Jedi Council catalogues every technique by ID so anyone on any planet
    // can trigger it on demand.  Ours maps to Eclipse's standard file refresh
    // command, which is what the F5 keybinding fires.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for this action — the standard
     * {@code org.eclipse.ui.file.refresh} command, which maps to F5 in the
     * default keybinding scheme.
     *
     * @return the refresh command ID string
     */
    public String getCommandId()
    {
        return "org.eclipse.ui.file.refresh"; //$NON-NLS-1$
    }


    // ── Luke Reaches Out and Reconnects ───────────────────────────────────────
    // Luke closes his eyes, lets go of the stale impression of the swamp, and
    // feels everything as it actually is right now — roots, creatures, water.
    // We do the same: flush cached state and kick off background jobs to reload
    // children, re-run searches, or reload attributes from the live server.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the refresh: dispatches background jobs to reload whatever is
     * currently selected.
     *
     * <ul>
     *   <li>Entries (including search results and bookmarks) → re-initialize children</li>
     *   <li>Searches → clear results and re-run</li>
     *   <li>Entry editor input → reload attributes from server</li>
     *   <li>Search editor input → clear results and re-run</li>
     * </ul>
     *
     * <p>For example — Luke reaches out with fresh eyes:</p>
     * <pre>
     *   Luke clears the old image (flushes the cache / clears search results).
     *   He reaches out again (fires InitializeChildrenRunnable / SearchRunnable).
     *   The Force (the LDAP server) tells him what's really there now.
     * </pre>
     *
     * <p>Continuations (paged or referral results) are resolved before
     * reloading so the full data set is available.</p>
     */
    public void run()
    {
        List<IEntry> entries = getEntries();
        ISearch[] searches = getSearches();
        IEntry entryInput = getEntryInput();
        ISearch searchInput = getSearchInput();

        if ( entries.size() > 0 )
        {
            for ( IEntry entry : entries )
            {
                if ( entry instanceof IContinuation )
                {
                    IContinuation continuation = ( IContinuation ) entry;
                    if ( continuation.getState() != State.RESOLVED )
                    {
                        continuation.resolve();
                    }
                }
            }
            InitializeChildrenRunnable initializeChildrenRunnable = new InitializeChildrenRunnable( true, entries
                .toArray( new IEntry[0] ) );
            new StudioBrowserJob( initializeChildrenRunnable ).execute();
        }
        if ( searches.length > 0 )
        {
            for ( ISearch search : searches )
            {
                search.setSearchResults( null );
                if ( search instanceof IContinuation )
                {
                    IContinuation continuation = ( IContinuation ) search;
                    if ( continuation.getState() != State.RESOLVED )
                    {
                        continuation.resolve();
                    }
                }
            }
            new StudioBrowserJob( new SearchRunnable( searches ) ).execute();
        }

        if ( entryInput != null )
        {
            // the entry input is usually a cloned entry, lookup the real entry from connection
            IEntry entry = entryInput.getBrowserConnection().getEntryFromCache( entryInput.getDn() );
            new StudioBrowserJob( new InitializeAttributesRunnable( entry ) ).execute();
        }
        if ( searchInput != null )
        {
            // the search input is usually a cloned search, lookup the real search from connection
            ISearch search = searchInput.getBrowserConnection().getSearchManager().getSearch( searchInput.getName() );
            search.setSearchResults( null );
            new StudioBrowserJob( new SearchRunnable( new ISearch[]
                { search } ) ).execute();
        }
    }


    // ── Yoda Checks Whether Luke Is Ready to Clear His Mind ──────────────────
    // Yoda won't start a meditation session if there's nothing to meditate on —
    // Luke needs at least one entry, one search, or one input in view.
    // We check the same: is there anything worth refreshing in the current
    // selection or editor input?
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled.  We need at least one
     * refreshable thing in context — selected entries, selected searches, or an
     * entry/search editor input.
     *
     * @return {@code true} if there is something to refresh; {@code false} otherwise
     */
    public boolean isEnabled()
    {
        List<IEntry> entries = getEntries();
        ISearch[] searches = getSearches();
        IEntry entryInput = getEntryInput();
        ISearch searchInput = getSearchInput();

        return entries.size() > 0 || searches.length > 0 || entryInput != null || searchInput != null;
    }


    // ── Luke Gathers All the Creatures He Can Feel ───────────────────────────
    // As Luke reaches out, he collects every living thing in his awareness:
    // direct entries, entries behind bookmarks, entries from search results.
    // We similarly aggregate entries from all three selection sources.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Collects all the entries that should be refreshed.  We pull from three
     * sources: directly selected entries, entries backing selected search
     * results, and entries backing selected bookmarks.
     *
     * @return a mutable list of all entries to refresh; never {@code null}
     */
    protected List<IEntry> getEntries()
    {
        List<IEntry> entries = new ArrayList<IEntry>();
        entries.addAll( Arrays.asList( getSelectedEntries() ) );
        for ( ISearchResult searchResult : getSelectedSearchResults() )
        {
            entries.add( searchResult.getEntry() );
        }
        for ( IBookmark bookmark : getSelectedBookmarks() )
        {
            entries.add( bookmark.getEntry() );
        }
        return entries;
    }


    // ── Luke Senses the Searches in His Awareness ────────────────────────────
    // Among everything Luke feels, he picks out the "searches" — the active
    // queries humming in the background.
    // We simply return the selected searches for the caller to re-execute.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected searches that should be re-run.  This is
     * a thin delegate to {@link #getSelectedSearches()} — subclasses can
     * override to add filtering logic.
     *
     * @return the array of selected {@link ISearch} objects; never {@code null}
     */
    protected ISearch[] getSearches()
    {
        return getSelectedSearches();
    }


    // ── Luke Identifies the Entry That Is His Focus ───────────────────────────
    // Sometimes the editor has a specific entry as its input — that's what Luke
    // is tuned to, even if nothing is "selected" in the tree.
    // We extract the entry from the editor input if present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the entry that is currently set as the editor's input object, or
     * {@code null} if the input is not an entry.  The editor input is the
     * entry being displayed in the entry editor panel, which may differ from
     * the tree selection.
     *
     * @return the entry input, or {@code null} if the input is not an {@link IEntry}
     */
    private IEntry getEntryInput()
    {
        if ( getInput() instanceof IEntry )
        {
            return ( IEntry ) getInput();
        }
        else
        {
            return null;
        }
    }


    // ── Luke Identifies the Search That Is His Focus ──────────────────────────
    // Same as above, but for searches — sometimes the editor input is a search
    // rather than an entry.
    // We extract the search from the editor input if present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search that is currently set as the editor's input object, or
     * {@code null} if the input is not a search.  Used to refresh the results
     * panel when a search is open in the editor.
     *
     * @return the search input, or {@code null} if the input is not an {@link ISearch}
     */
    private ISearch getSearchInput()
    {
        if ( getInput() instanceof ISearch )
        {
            return ( ISearch ) getInput();
        }
        else
        {
            return null;
        }
    }
}
