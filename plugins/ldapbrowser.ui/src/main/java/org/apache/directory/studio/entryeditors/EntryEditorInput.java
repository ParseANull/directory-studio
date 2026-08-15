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

package org.apache.directory.studio.entryeditors;


import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.RootDSE;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: EntryEditorInput — LEIA DISGUISED AS BOUNTY HUNTER BOUSHH ────────
// At Jabba's palace, Leia walks in wearing Boushh's armour and voice modulator.
// To everyone in the throne room she is a bounty hunter — the real Leia underneath
// doesn't matter until she drops the disguise. This class does the same: it wraps
// an IEntry, ISearchResult, or IBookmark behind the IEditorInput face Eclipse
// expects, and resolves the real entry on demand.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The input object fed to every entry editor opened by Directory Studio.
 * Eclipse's workbench only knows about {@link IEditorInput}; this class wears
 * that disguise while hiding the real payload — an {@link IEntry}, an
 * {@link ISearchResult}, or an {@link IBookmark} — inside the armour.
 * It also manages the shared reference copy and shared working copy that
 * manual-save editors rely on for dirty-state tracking.
 * Think of it as Leia under Boushh's helmet: the bounty hunter face is for
 * Eclipse, the princess inside is the actual LDAP entry we care about.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorInput implements IEditorInput
{
    /** The entry input */
    private IEntry entry;

    /** The search result input */
    private ISearchResult searchResult;

    /** The bookmark input */
    private IBookmark bookmark;

    /** The entry editor extension. */
    private EntryEditorExtension extension;

    /** The resolved entry, based on the entry, searchResult and bookmark */
    private IEntry resolvedEntry;


    // ── Leia Arrives Carrying a Wookiee Bounty ───────────────────────────────────
    // Leia strides into Jabba's court with Chewbacca in tow — a live entry as her
    // "prize." The throne room accepts this without question.
    // We wrap that raw IEntry in the Boushh armour (IEditorInput) so Eclipse opens it.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an input backed directly by an {@link IEntry}.
     * Use this when the user clicked on a plain LDAP tree node.
     *
     * @param entry      the LDAP entry to display in the editor
     * @param extension  the editor extension that describes which editor panel to open
     */
    public EntryEditorInput( IEntry entry, EntryEditorExtension extension )
    {
        this( entry, null, null, extension );
    }


    // ── Leia Arrives with a Search-Result Prisoner ───────────────────────────────
    // This time Boushh's "prisoner" is someone found by a search — not a known ally
    // but an entity surfaced by querying the palace records.
    // We wrap the search result the same way so editors see a uniform IEditorInput.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an input backed by an {@link ISearchResult}.
     * Use this when the user clicked on an entry returned from a search operation.
     *
     * @param searchResult  the search result row to display in the editor
     * @param extension     the editor extension that describes which editor panel to open
     */
    public EntryEditorInput( ISearchResult searchResult, EntryEditorExtension extension )
    {
        this( null, searchResult, null, extension );
    }


    // ── Leia Arrives with a Bookmarked VIP ──────────────────────────────────────
    // Boushh walks in escorting a VIP flagged in the palace's own guest register —
    // a known contact saved under a familiar name.
    // We wrap the bookmark entry so the editor gets the same uniform input object.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates an input backed by an {@link IBookmark}.
     * Use this when the user clicked on a bookmark node in the browser tree.
     *
     * @param bookmark   the bookmark whose underlying entry to display in the editor
     * @param extension  the editor extension that describes which editor panel to open
     */
    public EntryEditorInput( IBookmark bookmark, EntryEditorExtension extension )
    {
        this( null, null, bookmark, extension );
    }


    // ── The Real Leia Steps Behind the Mask ─────────────────────────────────────
    // In the private antechamber, Leia assembles the full disguise: she picks the
    // right armour pieces, resolves who her "prisoner" actually is, and checks the
    // palace cache for the latest record on them before entering the throne room.
    // This private constructor does exactly that — normalises all three input kinds.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * The single private constructor that all public constructors delegate to.
     * It records whichever input kind is non-null, then immediately resolves the
     * underlying {@link IEntry} — including a cache lookup — so the editor never
     * has to think about which flavour of input it was given.
     *
     * @param entry         the raw entry, or {@code null}
     * @param searchResult  the search result, or {@code null}
     * @param bookmark      the bookmark, or {@code null}
     * @param extension     the editor extension metadata
     */
    private EntryEditorInput( IEntry entry, ISearchResult searchResult, IBookmark bookmark,
        EntryEditorExtension extension )
    {
        this.entry = entry;
        this.searchResult = searchResult;
        this.bookmark = bookmark;
        this.extension = extension;

        if ( entry != null )
        {
            resolvedEntry = entry;
        }
        else if ( searchResult != null )
        {
            resolvedEntry = searchResult.getEntry();
        }
        else if ( bookmark != null )
        {
            resolvedEntry = bookmark.getEntry();
        }

        if ( resolvedEntry != null )
        {
            resolvedEntry = resolvedEntry.getBrowserConnection().getEntryFromCache( resolvedEntry.getDn() );
        }
    }


    // ── Boushh Doesn't Appear in the "Recently Seen" List ───────────────────────
    // Jabba doesn't keep a "most recently seen bounty hunters" record on his wall.
    // Similarly, we don't want LDAP entries showing up in Eclipse's "Recently opened
    // files" menu — they're transient, not persistent documents.
    // Always returns false.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code false} — LDAP entries are not persistent files.
     * Eclipse uses this to decide whether to put the input in the "File > Recent"
     * list; we never want that for LDAP entries.
     *
     * @return  {@code false}, always
     */
    public boolean exists()
    {
        return false;
    }


    // ── Reading the Insignia on Boushh's Helmet ──────────────────────────────────
    // The iconography on Boushh's armour tells the court which guild she's from.
    // We delegate to the extension's icon so the editor tab shows the right picture.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the icon descriptor for the editor tab, taken from the extension.
     * Eclipse calls this to render the little picture next to the tab title.
     *
     * @return  the {@link ImageDescriptor} from our {@link EntryEditorExtension}
     */
    public ImageDescriptor getImageDescriptor()
    {
        return extension.getIcon();
    }


    // ── Announcing Boushh's Name in the Throne Room ─────────────────────────────
    // The herald bellows "Bounty Hunter Boushh!" as she enters — a short, memorable
    // label the court can display on the gallery board.
    // We return the DN of the resolved entry (or a placeholder) as the tab title.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display name shown in the editor tab.
     * For a normal entry this is the full DN string; for the Root DSE we return
     * a localised "Root DSE" label; if there's no entry yet, a "no entry" placeholder.
     *
     * @return  the string used as the editor tab title
     */
    public String getName()
    {
        if ( resolvedEntry != null )
        {
            if ( resolvedEntry instanceof RootDSE )
            {
                return Messages.getString( "EntryEditorNavigationLocation.RootDSE" ); //$NON-NLS-1$
            }
            else
            {
                return resolvedEntry.getDn().getName();
            }
        }

        return Messages.getString( "EntryEditorInput.NoEntrySelected" ); //$NON-NLS-1$
    }


    // ── Reading Boushh's Full Dossier Off the Wall ───────────────────────────────
    // When you hover over a bounty hunter's name badge, the guard reads out the full
    // record: name, guild, the planet they flew in from.
    // We return the DN plus the connection name as the tooltip text.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the tooltip text shown when hovering over the editor tab.
     * We combine the entry DN with the connection name so the user knows which
     * server this entry lives on — useful when multiple connections are open.
     *
     * @return  a string in the form "cn=Luke,dc=rebels — ConnectionName", or just the DN
     */
    public String getToolTipText()
    {
        if ( resolvedEntry != null )
        {
            IBrowserConnection connection = resolvedEntry.getBrowserConnection();

            if ( ( connection != null ) && ( connection.getConnection() != null ) )
            {
                return getName() + " - " + connection.getConnection().getName();//$NON-NLS-1$
            }
            else
            {
                return getName();
            }
        }

        return Messages.getString( "EntryEditorInput.NoEntrySelected" ); //$NON-NLS-1$
    }


    // ── The Disguise Has No Bounty Office ID ────────────────────────────────────
    // Boushh's armour is a disguise — it was never registered with the Imperial
    // bounty tracking system, so there's no persistent ID to retrieve.
    // We return null here because LDAP entries are not persistable editor inputs.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Always returns {@code null} — LDAP entries cannot be persisted across sessions.
     * Eclipse calls this to restore open editors after a restart; we opt out.
     *
     * @return  {@code null}, always
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── The Armour Doesn't Adapt to Other Factions ──────────────────────────────
    // Boushh's Ubese armour only works in Jabba's palace — it can't be adapted
    // into, say, an Imperial officer's uniform on demand.
    // We return null from getAdapter because we have no extra capabilities to expose.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} for all adapter requests.
     * We don't implement any additional Eclipse adapter interfaces, so we always
     * return null here.
     *
     * @param adapter  the adapter class requested by the Eclipse platform
     * @return         {@code null}, always
     */
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── Checking Which Bounty Guild Issued the Commission ───────────────────────
    // The palace guard looks at the guild sigil on Boushh's shoulder to know which
    // editor extension "issued" this input and what rules apply.
    // We return the extension metadata object that created this input.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link EntryEditorExtension} that this input was created for.
     * The extension tells us things like whether multi-window is enabled and
     * which editor part ID to open — so we keep a reference to it.
     *
     * @return  the extension metadata for the editor that owns this input
     */
    public EntryEditorExtension getExtension()
    {
        return extension;
    }


    // ── Pulling Off the Helmet to Reveal the Real Entry ─────────────────────────
    // At the critical moment, Leia removes Boushh's helmet and we see who she
    // really is — the underlying entry that the entire disguise was protecting.
    // We return the resolved IEntry regardless of whether it came via entry, result, or bookmark.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the underlying LDAP entry regardless of how this input was constructed.
     * Whether we were built from a bare {@link IEntry}, a search result, or a bookmark,
     * this always gives back the actual entry object from the connection cache.
     *
     * @return  the resolved {@link IEntry}, or {@code null} if nothing was provided
     */
    public IEntry getResolvedEntry()
    {
        return resolvedEntry;
    }


    // ── Retrieving the Working Copy from the Rebellion's Safe House ─────────────
    // Leia keeps a secure copy of the plans in the Rebellion's safe house so she
    // can work on them without exposing the originals to Imperial intercept.
    // We return the editor's private working copy of the entry for the same reason.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the shared working copy of the resolved entry for the given editor.
     * Manual-save editors must use this copy — never the original — so that
     * unsaved changes don't bleed into other views until the user explicitly saves.
     *
     * @param editor  the entry editor requesting its working copy
     * @return        the shared working copy {@link IEntry}, or {@code null} if there is no resolved entry
     */
    public IEntry getSharedWorkingCopy( IEntryEditor editor )
    {
        if ( resolvedEntry != null )
        {
            return BrowserUIPlugin.getDefault().getEntryEditorManager().getSharedWorkingCopy( resolvedEntry, editor );
        }

        return null;
    }


    // ── Checking Whether the Safe House Plans Have Been Altered ─────────────────
    // Before meeting with the Alliance council, Leia checks whether anyone has
    // scribbled changes on the working copy of the plans since they were last saved.
    // We check whether the working copy differs from the reference copy.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the shared working copy has unsaved changes.
     * The editor uses this to decide whether to show the "dirty" asterisk in
     * the tab title and whether to prompt the user before closing.
     *
     * @param editor  the entry editor whose working copy state we are checking
     * @return        {@code true} if the working copy differs from the reference copy
     */
    public boolean isSharedWorkingCopyDirty( IEntryEditor editor )
    {
        boolean dirty = BrowserUIPlugin.getDefault().getEntryEditorManager().isSharedWorkingCopyDirty(
            resolvedEntry, editor );

        return dirty;
    }


    // ── Transmitting the Plans to the Alliance Fleet ─────────────────────────────
    // When the council approves the changes, Leia transmits the updated plans to
    // the fleet — the working copy becomes the new official record.
    // We push the working-copy diff to the LDAP server here.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Saves the difference between the reference copy and the working copy to the
     * directory server.
     * We compute a diff (as an LDIF modify record) and execute it via the browser job
     * infrastructure, optionally showing an error dialog if something goes wrong.
     *
     * @param handleError  if {@code true}, show a dialog on failure rather than just returning a status
     * @param editor       the entry editor initiating the save
     * @return             an {@link IStatus} describing the outcome, or {@code null} if there was nothing to save
     */
    public IStatus saveSharedWorkingCopy( boolean handleError, IEntryEditor editor )
    {
        IStatus status = BrowserUIPlugin.getDefault().getEntryEditorManager().saveSharedWorkingCopy(
            resolvedEntry, handleError, editor );

        return status;
    }


    // ── Discarding the Working Copy and Starting Fresh ──────────────────────────
    // If the Alliance council rejects the proposed changes, Leia shreds the working
    // copy and fetches a fresh duplicate of the original plans to start again.
    // We reset both reference and working copies to the current server state.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Resets the shared reference and working copies, discarding any unsaved changes.
     * The editor calls this on "Revert" or when the user cancels a pending modification.
     * After this call, the working copy is a fresh clone of the current server state.
     *
     * @param editor  the entry editor requesting the reset
     */
    public void resetSharedWorkingCopy( IEntryEditor editor )
    {
        BrowserUIPlugin.getDefault().getEntryEditorManager().resetSharedWorkingCopy( resolvedEntry, editor );
    }


    // ── Checking What Boushh Actually Brought In ────────────────────────────────
    // Sometimes you need to peek inside the armour to see if there's a Wookiee, a
    // search result, or a bookmarked VIP in there — not just the resolved entry.
    // We return the raw IEntry as given to the constructor, which may be null.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw {@link IEntry} this input was constructed with, or {@code null}.
     * Use {@link #getResolvedEntry()} instead if you just want the underlying entry;
     * this is only useful when you need to know whether the input was originally
     * an entry (vs. a search result or bookmark).
     *
     * @return  the original entry, or {@code null} if this input was built from a search result or bookmark
     */
    public IEntry getEntryInput()
    {
        return entry;
    }


    // ── Checking Whether the Bounty Is a Search-Result Capture ──────────────────
    // Was this prisoner found by actively searching the palace records, or were
    // they already on file? We return the raw ISearchResult here for callers who care.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw {@link ISearchResult} this input was constructed with, or {@code null}.
     * Null means this input was not built from a search result row.
     *
     * @return  the original search result, or {@code null}
     */
    public ISearchResult getSearchResultInput()
    {
        return searchResult;
    }


    // ── Checking Whether the Bounty Came from the Palace Guest Register ─────────
    // Was this visitor already in the bookmarked guest list, or were they an unknown?
    // We return the raw IBookmark here for callers who need to distinguish.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw {@link IBookmark} this input was constructed with, or {@code null}.
     * Null means this input was not built from a bookmarked entry.
     *
     * @return  the original bookmark, or {@code null}
     */
    public IBookmark getBookmarkInput()
    {
        return bookmark;
    }


    // ── Returning Whatever Is Hidden Under the Helmet ───────────────────────────
    // Generic "what's in the armour?" — could be an entry, a search result, or a
    // bookmark; the caller decides what to do once they know which flavour it is.
    // We return whichever non-null input this was built from.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whichever input object is non-null: the {@link IEntry}, {@link ISearchResult},
     * or {@link IBookmark} this input wraps.
     * Callers can {@code instanceof}-check the result to find out what they got.
     *
     * @return  the underlying input object, or {@code null} if none was provided
     */
    public Object getInput()
    {
        if ( entry != null )
        {
            return entry;
        }
        else if ( searchResult != null )
        {
            return searchResult;
        }
        else if ( bookmark != null )
        {
            return bookmark;
        }
        else
        {
            return null;
        }
    }


    // ── Generating the Secure Hash for the Palace's Visitor Log ─────────────────
    // The palace scribe hashes visitor credentials so two visits by the same bounty
    // hunter to the same prisoner map to the same log entry.
    // We hash by resolved-entry DN for multi-window editors, or return 0 for single-window.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a hash code that Eclipse uses to decide whether two inputs are "the same."
     * For multi-window editors we hash on the resolved entry's DN — same entry, same tab.
     * For single-window editors we always return 0 so all entries share the one tab.
     *
     * @return  an integer hash code based on extension type and resolved-entry DN
     */
    public int hashCode()
    {
        if ( extension == null )
        {
            return 0;
        }
        else if ( extension.isMultiWindow() )
        {
            if ( resolvedEntry == null )
            {
                return 0;
            }
            else
            {
                return resolvedEntry.getDn().hashCode();
            }
        }
        else
        {
            return 0;
        }
    }


    // ── Verifying That Two Bounty Hunters Are Actually the Same Person ───────────
    // Two Boushh armours from the same guild visiting the same prisoner should
    // be treated as one visit, not two — otherwise the palace opens duplicate records.
    // We compare extension identity and the underlying input object for equality.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this input represents the same editor content as {@code obj}.
     * Two inputs are equal if they use the same extension instance AND wrap the same
     * underlying input object (entry/search result/bookmark).
     * Eclipse uses this to avoid opening a duplicate editor tab for the same entry.
     *
     * @param obj  the other object to compare against
     * @return     {@code true} if both inputs would show the same content in the same editor type
     */
    public boolean equals( Object obj )
    {
        if ( !( obj instanceof EntryEditorInput ) )
        {
            return false;
        }

        EntryEditorInput other = ( EntryEditorInput ) obj;

        if ( extension == null && other.extension == null )
        {
            return true;
        }

        if ( this.getExtension() != other.getExtension() )
        {
            return false;
        }

        if ( this.getInput() == null && other.getInput() == null )
        {
            return true;
        }
        else if ( this.getInput() == null || other.getInput() == null )
        {
            return false;
        }
        else
        {
            return other.getInput().equals( this.getInput() );
        }
    }


    // ── Printing the Dossier Summary for the Debrief ────────────────────────────
    // At the debrief, Leia reads out a one-line summary: "the resolved entry was X."
    // We do the same — just dump the resolved entry's toString for quick logging.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a short string representation of this input, mainly for debug logging.
     * We just delegate to the resolved entry's {@code toString()} so the DN shows up
     * in any log output that prints this object.
     *
     * @see Object#toString()
     * @return  the resolved entry's string representation, or "null" if there is none
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( resolvedEntry );

        return sb.toString();
    }
}
