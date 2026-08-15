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


import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.ldapbrowser.common.dialogs.RenameEntryDialog;
import org.apache.directory.studio.ldapbrowser.common.dialogs.SimulateRenameDialogImpl;
import org.apache.directory.studio.ldapbrowser.core.jobs.RenameEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.impl.RootDSE;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: RenameAction — OBI-WAN BECOMES "BEN KENOBI" ───────────────────────
// After the fall of the Republic, Obi-Wan Kenobi takes up a new life on
// Tatooine under the alias "Ben Kenobi."  Same person, same Force connection,
// same wisdom — but a different label attached to the identity.  He didn't
// vanish; he just changed what his name says on the outside.
// That's exactly what Rename does in the LDAP browser: for entries, we issue
// an LDAP ModifyDN to change the RDN (the entry's identity key in the
// directory); for searches and bookmarks, we just update the local display
// name.  Same data, new label.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Handles the "Rename" action for LDAP entries, saved searches, and bookmarks.
 *
 * <p>Renaming means different things depending on the target:</p>
 * <ul>
 *   <li><b>Entries</b> — we open the {@link RenameEntryDialog}, collect the
 *       new RDN, and fire an LDAP ModifyDN operation via
 *       {@link RenameEntryRunnable}.  This is a real server-side operation.</li>
 *   <li><b>Searches</b> — we prompt for a new name and update the local search
 *       object.  No server operation required.</li>
 *   <li><b>Bookmarks</b> — same as searches: local rename only.</li>
 * </ul>
 *
 * <p>Think of this class as Obi-Wan adopting the alias "Ben Kenobi": same
 * underlying identity, but the label that the galaxy sees has changed.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class RenameAction extends BrowserAction
{
    // ── Obi-Wan Settles Into His New Desert Home ──────────────────────────────
    // Obi-Wan arrives at his hut on Tatooine and simply begins — no fanfare,
    // no elaborate initialization.  He's ready to do what needs doing.
    // Our constructor is equally unadorned: just delegate to the parent.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new {@code RenameAction}.  Calls the parent constructor
     * which wires up all the standard Eclipse action plumbing.
     */
    public RenameAction()
    {
        super();
    }


    // ── Obi-Wan Introduces Himself Appropriately ──────────────────────────────
    // In the village, he says "Ben Kenobi."  In his own head, he knows who he
    // is.  The label shifts depending on context — entry rename vs. search
    // rename vs. bookmark rename.
    // We return a context-sensitive label to match.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a context-sensitive display label for this action.  The label
     * adapts to the current selection:
     * <ul>
     *   <li>Single entry selected → "Rename Entry"</li>
     *   <li>Single search selected → "Rename Search"</li>
     *   <li>Single bookmark selected → "Rename Bookmark"</li>
     *   <li>Anything else → generic "Rename"</li>
     * </ul>
     *
     * @return the localized, context-sensitive label; never {@code null}
     */
    public String getText()
    {

        IEntry[] entries = getEntries();
        ISearch[] searches = getSearches();
        IBookmark[] bookmarks = getBookmarks();

        if ( entries.length == 1 && searches.length == 0 && bookmarks.length == 0 )
        {
            return Messages.getString( "RenameAction.RenameEntry" ); //$NON-NLS-1$
        }
        else if ( searches.length == 1 && entries.length == 0 && bookmarks.length == 0 )
        {
            return Messages.getString( "RenameAction.RenameSearch" ); //$NON-NLS-1$
        }
        else if ( bookmarks.length == 1 && entries.length == 0 && searches.length == 0 )
        {
            return Messages.getString( "RenameAction.RenameBookmark" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "RenameAction.Rename" ); //$NON-NLS-1$
        }
    }


    // ── Ben Kenobi Carries No Flashy Badge ───────────────────────────────────
    // A man trying to stay hidden doesn't advertise with bright insignia.
    // Rename has no custom icon — it relies on the menu label alone.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon for this action.  Rename has no custom icon, so we
     * return {@code null} — Eclipse will render the action as text-only in
     * menus.
     *
     * @return {@code null} — no custom icon
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── "Ben" Still Answers to the Republic's Old Registry ───────────────────
    // Even as Ben Kenobi, Obi-Wan still responds when called by the right
    // frequency — the one the old Republic registered.
    // Our command ID maps to Eclipse's standard RENAME command for F2 / inline
    // rename support.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse workbench RENAME command ID.  This ties us to the
     * standard F2 keybinding (or platform equivalent) in the Eclipse command
     * framework.
     *
     * @return the workbench RENAME command ID
     */
    public String getCommandId()
    {
        return IWorkbenchActionDefinitionIds.RENAME;
    }


    // ── Obi-Wan Acts When Called Upon ─────────────────────────────────────────
    // When Luke finally calls on him for help, Obi-Wan doesn't hesitate — he
    // assesses the situation (one entry?  one search?  one bookmark?) and takes
    // the appropriate action.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the rename: determines what's selected and delegates to the
     * appropriate rename helper method.  If exactly one entry, search, or
     * bookmark is selected, we rename it; mixed or empty selections are
     * silently ignored.
     *
     * <p>For example — Obi-Wan adopts his alias in the appropriate context:</p>
     * <pre>
     *   One entry selected?   → Call renameEntry(entry) — LDAP ModifyDN.
     *   One search selected?  → Call renameSearch(search) — local rename.
     *   One bookmark?         → Call renameBookmark(bookmark) — local rename.
     * </pre>
     */
    public void run()
    {
        IEntry[] entries = getEntries();
        ISearch[] searches = getSearches();
        IBookmark[] bookmarks = getBookmarks();

        if ( entries.length == 1 && searches.length == 0 && bookmarks.length == 0 )
        {
            renameEntry( entries[0] );
        }
        else if ( searches.length == 1 && entries.length == 0 && bookmarks.length == 0 )
        {
            renameSearch( searches[0] );
        }
        else if ( bookmarks.length == 1 && entries.length == 0 && searches.length == 0 )
        {
            renameBookmark( bookmarks[0] );
        }
    }


    // ── Obi-Wan Only Shifts Identity When There's One Clear Target ────────────
    // Obi-Wan could only become "Ben Kenobi" — singular alias, singular person.
    // Trying to rename two people at once would just cause confusion.
    // We enable the action only when exactly one renameable thing is selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this action should be enabled.  We need exactly one
     * renameable item in the selection — one entry, one search, or one
     * bookmark.  Zero or multiple items disable the action.
     *
     * @return {@code true} if exactly one renameable item is selected;
     *         {@code false} otherwise
     */
    public boolean isEnabled()
    {
        try
        {
            IEntry[] entries = getEntries();
            ISearch[] searches = getSearches();
            IBookmark[] bookmarks = getBookmarks();

            return entries.length + searches.length + bookmarks.length == 1;

        }
        catch ( Exception e )
        {
            return false;
        }
    }


    // ── Obi-Wan Identifies Which Person He Is in This Scene ──────────────────
    // Obi-Wan knows he's the one being renamed — he checks: am I the selected
    // entry?  Am I reachable through this value's RDN part?  He's careful not
    // to include the Root DSE (that can never be renamed).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Collects the entries eligible for rename from the current selection.
     * We look at directly selected entries, search results, and RDN-part
     * values (since renaming the RDN value is equivalent to renaming the
     * entry).  The Root DSE is explicitly excluded — you can't rename the
     * root of the directory.
     *
     * @return an array containing at most one renameable {@link IEntry};
     *         an empty array if none applies
     */
    protected IEntry[] getEntries()
    {

        IEntry entry = null;

        if ( getSelectedEntries().length == 1 )
        {
            entry = getSelectedEntries()[0];
        }
        else if ( getSelectedSearchResults().length == 1 )
        {
            entry = getSelectedSearchResults()[0].getEntry();
        }
        else if ( getSelectedValues().length == 1 && getSelectedValues()[0].isRdnPart() )
        {
            entry = getSelectedValues()[0].getAttribute().getEntry();
        }

        if ( entry != null && !( entry instanceof RootDSE ) )
        {
            return new IEntry[]
                { entry };
        }
        else
        {
            return new IEntry[0];
        }
    }


    // ── Ben Kenobi Sends a New Alias to the Galaxy ───────────────────────────
    // The actual moment: Obi-Wan opens a dialog with the galaxy's registry,
    // hands over the new name, and the record is updated.
    // We open the RenameEntryDialog, collect the new RDN, and if it changed,
    // fire a real LDAP ModifyDN operation via a background job.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Opens the {@link RenameEntryDialog} for the given entry, then fires a
     * background {@link RenameEntryRunnable} if the user confirmed a new RDN.
     * This is the one rename method that hits the LDAP server — entries are
     * real directory objects with server-side identity.
     *
     * @param entry  the LDAP entry to rename; must not be {@code null}
     */
    protected void renameEntry( final IEntry entry )
    {
        RenameEntryDialog renameDialog = new RenameEntryDialog( getShell(), entry );
        if ( renameDialog.open() == Dialog.OK )
        {
            Rdn newRdn = renameDialog.getRdn();
            if ( newRdn != null && !newRdn.equals( entry.getRdn() ) )
            {
                new StudioBrowserJob(
                    new RenameEntryRunnable( entry, newRdn, new SimulateRenameDialogImpl( getShell() ) ) ).execute();
            }
        }
    }


    // ── Obi-Wan Checks Which Searches Can Legally Be Renamed ─────────────────
    // Not every search is renameable — quick searches are ephemeral and have
    // no business being renamed.  Obi-Wan knows the rules.
    // We exclude IQuickSearch instances from the results.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected searches that are eligible for rename.
     * Quick searches ({@link IQuickSearch}) are excluded because they're
     * ephemeral and not stored by name.
     *
     * @return an array of at most one renameable {@link ISearch};
     *         an empty array if none qualifies
     */
    protected ISearch[] getSearches()
    {
        if ( getSelectedSearches().length == 1 && !( getSelectedSearches()[0] instanceof IQuickSearch ) )
        {
            return getSelectedSearches();
        }
        else
        {
            return new ISearch[0];
        }
    }


    // ── Ben Kenobi Reassigns a Saved Nickname ────────────────────────────────
    // A search is just a nickname for a set of criteria.  Renaming it is purely
    // local — we prompt for the new name, validate it's unique, and set it.
    // No LDAP server involved, no Force required.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Prompts the user for a new name for the given search using an
     * {@link InputDialog}, validates uniqueness within the connection, and
     * updates the search name if the user confirmed.  This is a purely local
     * operation — no LDAP request is sent.
     *
     * @param search  the search to rename; must not be {@code null}
     */
    protected void renameSearch( final ISearch search )
    {
        IInputValidator validator = new IInputValidator()
        {
            public String isValid( String newName )
            {
                if ( search.getName().equals( newName ) )
                    return null;
                else if ( search.getBrowserConnection().getSearchManager().getSearch( newName ) != null )
                    return Messages.getString( "RenameAction.ConnectionWithThisNameAlreadyExists" ); //$NON-NLS-1$
                else
                    return null;
            }
        };

        InputDialog dialog = new InputDialog(
            getShell(),
            Messages.getString( "RenameAction.RenameSearchDialog" ), Messages.getString( "RenameAction.RenameSearchNewName" ), search.getName(), validator ); //$NON-NLS-1$ //$NON-NLS-2$

        dialog.open();
        String newName = dialog.getValue();
        if ( newName != null )
        {
            search.setName( newName );
        }
    }


    // ── Obi-Wan Checks Which Bookmarks Can Be Renamed ────────────────────────
    // Bookmarks are like safe-house addresses — they point to real entries but
    // can carry any display name the user chooses.  All of them are renameable.
    // We return the single selected bookmark, if one exists.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected bookmarks eligible for rename.  We
     * return at most one bookmark — multi-selection is not supported for rename.
     *
     * @return an array of at most one {@link IBookmark}; empty if none selected
     */
    protected IBookmark[] getBookmarks()
    {
        if ( getSelectedBookmarks().length == 1 )
        {
            return getSelectedBookmarks();
        }
        else
        {
            return new IBookmark[0];
        }
    }


    // ── Ben Kenobi Changes a Safe-House Sign ─────────────────────────────────
    // The safe house at coordinate X can go from "the old farm" to "the ridge
    // hut" — same location, new label, purely local change.
    // We prompt for the new name, validate uniqueness, and update.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Prompts the user for a new name for the given bookmark using an
     * {@link InputDialog}, validates that the new name is unique within the
     * connection, and updates the bookmark name if the user confirmed.  Like
     * search rename, this is a local-only operation.
     *
     * @param bookmark  the bookmark to rename; must not be {@code null}
     */
    protected void renameBookmark( final IBookmark bookmark )
    {
        IInputValidator validator = new IInputValidator()
        {
            public String isValid( String newName )
            {
                if ( bookmark.getName().equals( newName ) )
                    return null;
                else if ( bookmark.getBrowserConnection().getBookmarkManager().getBookmark( newName ) != null )
                    return Messages.getString( "RenameAction.BookmarkWithThisNameAlreadyExists" ); //$NON-NLS-1$
                else
                    return null;
            }
        };

        InputDialog dialog = new InputDialog(
            getShell(),
            Messages.getString( "RenameAction.RenameBookmarkDialog" ), Messages.getString( "RenameAction.RenameBookmarkNewName" ), bookmark.getName(), validator ); //$NON-NLS-1$ //$NON-NLS-2$

        dialog.open();
        String newName = dialog.getValue();
        if ( newName != null )
        {
            bookmark.setName( newName );
        }
    }
}
