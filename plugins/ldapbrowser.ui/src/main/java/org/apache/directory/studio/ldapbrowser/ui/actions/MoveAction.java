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


import java.util.LinkedHashSet;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.common.dialogs.MoveEntriesDialog;
import org.apache.directory.studio.ldapbrowser.common.dialogs.SimulateRenameDialogImpl;
import org.apache.directory.studio.ldapbrowser.core.jobs.MoveEntriesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.ReadEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.impl.RootDSE;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;


// ── CLASS: MoveAction — YODA LIFTS THE X-WING ────────────────────────────────
// On Dagobah, Yoda closes his eyes and raises Luke's sunken X-wing from the
// swamp with nothing but the Force — the ship lifts, pivots, and settles on
// dry land at a completely different spot.  MoveAction does the same thing to
// LDAP entries: it picks them up from their current DN and sets them down under
// a new parent, leaving the rest of the tree untouched.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Moves one or more LDAP entries from their current parent to a new parent DN.
 * It's the Eclipse action that drives the Move Entries dialog and then fires
 * the background job to actually execute the LDAP modifyDN operation on the server.
 * Think of this class as Yoda: it applies quiet, precise Force to relocate
 * something heavy without disturbing anything around it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MoveAction extends BrowserAction
{
    // ── Yoda Readies Himself On The Bank ────────────────────────────────────────
    // Yoda stands at the swamp's edge, centering himself before he lifts anything.
    // He doesn't grab the X-wing blindly — he first settles into a calm, ready state.
    // Our constructor does the same: calls super() to initialise the BrowserAction
    // infrastructure so we're properly connected to the browser's selection machinery.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new MoveAction and wires it into the Eclipse action framework.
     * We call super() so the parent BrowserAction can set up its selection-listener
     * plumbing; without that we'd never know what the user has selected.
     */
    public MoveAction()
    {
        super();
    }


    // ── The Ship Has A Name ──────────────────────────────────────────────────────
    // Luke's X-wing didn't just lift — Yoda knew exactly what he was moving and
    // could articulate it: "Your X-wing, young Skywalker."
    // We label the action based on what the user selected: one entry, many entries,
    // or just "Move" if the selection is ambiguous.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action, adapted to the current selection.
     * If exactly one entry is selected we say "Move Entry"; for multiple entries we
     * say "Move Entries"; otherwise we fall back to plain "Move".
     * This keeps the menu item readable and informative rather than generic.
     *
     * @return the localised label string Eclipse will show in menus and toolbars
     */
    public String getText()
    {
        IEntry[] entries = getEntries();
        ISearch[] searches = getSearches();
        IBookmark[] bookmarks = getBookmarks();

        if ( entries.length > 0 && searches.length == 0 && bookmarks.length == 0 )
        {
            return entries.length == 1 ? Messages.getString( "MoveAction.MoveEntry" ) : Messages.getString( "MoveAction.MoveEntries" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else
        {
            return Messages.getString( "MoveAction.Move" ); //$NON-NLS-1$
        }
    }


    // ── No Icon On This Lightsaber ───────────────────────────────────────────────
    // Yoda's power needed no flashy banner — the act spoke for itself.
    // Move has no dedicated toolbar icon, so we return null and let the menu
    // item stand on its text label alone.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for the action's icon.
     * We don't have a dedicated icon for Move right now, so this returns null,
     * which means Eclipse will show the action as text-only in menus.
     *
     * @return always null — no icon is registered for this action
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── Platform Knows This Gesture ──────────────────────────────────────────────
    // The Rebel Alliance already had a standard hand signal for "lift and move."
    // Eclipse's workbench has a standard command ID for Move — we wire to it so
    // keyboard shortcuts defined elsewhere still trigger us.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID this action is bound to.
     * Using the standard workbench Move command ID means any keybinding the user
     * or platform assigns to "Move" will automatically fire our action.
     *
     * @return the standard Eclipse workbench Move command identifier
     */
    public String getCommandId()
    {
        return IWorkbenchActionDefinitionIds.MOVE;
    }


    // ── Yoda Lifts The X-Wing ────────────────────────────────────────────────────
    // Yoda exhales, extends his hand, and the X-wing rises — only when conditions
    // are right (entries selected, no searches or bookmarks muddying the picture).
    // We do the same: only call moveEntries() when we have a clean entry selection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Executes the move by opening the Move Entries dialog and, if the user confirms,
     * kicking off the background LDAP modifyDN job.
     * We only act when the selection is purely entries (no searches, no bookmarks),
     * because moving a search definition or bookmark is a different concept entirely.
     */
    public void run()
    {
        IEntry[] entries = getEntries();
        ISearch[] searches = getSearches();
        IBookmark[] bookmarks = getBookmarks();

        if ( entries.length > 0 && searches.length == 0 && bookmarks.length == 0 )
        {
            moveEntries( entries );
        }
    }


    // ── Can The Force Be Used Right Now ─────────────────────────────────────────
    // Before Yoda commits to lifting the ship, he senses whether the conditions
    // are right — is the swamp too deep, is Luke interfering, is anything blocking?
    // isEnabled() checks those same preconditions so Eclipse can grey out the menu
    // item when a move operation isn't valid for the current selection.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Tells Eclipse whether the Move action should be available given the current
     * browser selection.
     * We return true only if there are entries selected and no searches or bookmarks
     * are mixed in — those types can't be moved the same way.
     * If anything goes wrong inspecting the selection we return false rather than
     * letting an exception propagate into the UI.
     *
     * @return true if we have a valid, move-able entry selection; false otherwise
     */
    public boolean isEnabled()
    {
        try
        {
            IEntry[] entries = getEntries();
            ISearch[] searches = getSearches();
            IBookmark[] bookmarks = getBookmarks();

            return entries.length > 0 && searches.length == 0 && bookmarks.length == 0;
        }
        catch ( Exception e )
        {
            return false;
        }
    }


    // ── Yoda Identifies The Ship To Lift ────────────────────────────────────────
    // Before lifting, Yoda identifies the exact object: the X-wing, not the mud
    // around it, not R2-D2 sitting on top.  We collect all selected entries and
    // search-result entries into one de-duplicated set, then reject anything that
    // includes the RootDSE (you can't move the root of the tree).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Collects the LDAP entries that are candidates for moving from the current
     * browser selection.
     * We merge both directly-selected entries and entries backing selected search
     * results into a de-duplicated set.  If any of those entries is null or is the
     * RootDSE we bail out entirely — you can't move the directory root.
     * Returns an empty array if the selection also contains bookmarks, searches,
     * attributes, or values, because we only move entries in isolation.
     *
     * @return the entries to move, or an empty array if the selection isn't moveable
     */
    protected IEntry[] getEntries()
    {
        if ( getSelectedBookmarks().length + getSelectedSearches().length + getSelectedAttributes().length
            + getSelectedValues().length == 0
            && getSelectedEntries().length + getSelectedSearchResults().length > 0 )
        {
            LinkedHashSet<IEntry> entriesSet = new LinkedHashSet<IEntry>();
            for ( int i = 0; i < getSelectedEntries().length; i++ )
            {
                entriesSet.add( getSelectedEntries()[i] );
            }
            for ( int i = 0; i < this.getSelectedSearchResults().length; i++ )
            {
                entriesSet.add( this.getSelectedSearchResults()[i].getEntry() );
            }
            IEntry[] entries = ( IEntry[] ) entriesSet.toArray( new IEntry[entriesSet.size()] );
            for ( int i = 0; i < entries.length; i++ )
            {
                if ( entries[i] == null || entries[i] instanceof RootDSE )
                {
                    return new IEntry[0];
                }
            }
            return entries;
        }
        else
        {
            return new IEntry[0];
        }
    }


    // ── The X-Wing Settles In Its New Spot ──────────────────────────────────────
    // Yoda guides the X-wing to the dry bank — but he first checks that the landing
    // zone exists.  If the target parent entry isn't cached, he reads it from the
    // server before placing the ship down.
    // We open the Move dialog, resolve the target parent DN (fetching from the server
    // if it's not in cache), then kick off the background MoveEntriesRunnable.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Shows the Move Entries dialog and, if the user picks a destination and clicks OK,
     * fires the background job that performs the actual LDAP modifyDN on the server.
     * If the target parent isn't in the local cache we read it from the server first
     * so the job has a real IEntry object to hand to the move runnable.
     *
     * @param entries  the entries we're going to move — must be non-null and non-empty,
     *                 all within the same browser connection
     */
    protected void moveEntries( final IEntry[] entries )
    {
        MoveEntriesDialog moveDialog = new MoveEntriesDialog( getShell(), entries );
        if ( moveDialog.open() == Dialog.OK )
        {
            Dn newParentDn = moveDialog.getParentDn();
            if ( newParentDn != null /* && !newRdn.equals(entry.getRdn()) */)
            {
                IEntry newParentEntry = entries[0].getBrowserConnection().getEntryFromCache( newParentDn );
                if ( newParentEntry == null )
                {
                    ReadEntryRunnable runnable = new ReadEntryRunnable( entries[0].getBrowserConnection(), newParentDn );
                    RunnableContextRunner.execute( runnable, null, true );
                    newParentEntry = runnable.getReadEntry();
                }
                if ( newParentEntry != null )
                {
                    new StudioBrowserJob( new MoveEntriesRunnable( entries, newParentEntry,
                        new SimulateRenameDialogImpl( getShell() ) ) ).execute();
                }
            }
        }
    }


    // ── Only One Ship In The Swamp ───────────────────────────────────────────────
    // Yoda can sense every object in the swamp, but on Dagobah he focuses on one
    // specific thing at a time.  getSearches() is a guard: we only handle exactly
    // one selected search, and only to block the move (searches can't be moved).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected searches if exactly one is selected, otherwise
     * an empty array.
     * This is used as a guard in {@link #getEntries()} and {@link #isEnabled()} to
     * detect mixed selections — if a search is selected alongside entries, the move
     * is not valid.
     *
     * @return a one-element array with the selected search, or an empty array
     */
    protected ISearch[] getSearches()
    {
        if ( getSelectedSearches().length == 1 )
        {
            return getSelectedSearches();
        }
        else
        {
            return new ISearch[0];
        }
    }


    // ── No Droids In This Lift Zone ─────────────────────────────────────────────
    // Yoda wasn't moving R2-D2 or C-3PO — just the X-wing.  Similarly, bookmarks
    // in the selection mean we shouldn't attempt a move at all.
    // getBookmarks() detects that situation so we can bail out cleanly.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the currently selected bookmarks if exactly one is selected, otherwise
     * an empty array.
     * Like {@link #getSearches()}, this is a mixed-selection guard: if a bookmark is
     * in the selection, we treat the overall selection as unmoveable.
     *
     * @return a one-element array with the selected bookmark, or an empty array
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

}
