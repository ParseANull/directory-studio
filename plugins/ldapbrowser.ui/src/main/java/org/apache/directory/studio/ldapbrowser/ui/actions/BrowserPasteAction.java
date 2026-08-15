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


import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.studio.common.ui.ClipboardUtils;
import org.apache.directory.studio.connection.core.Utils;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.actions.PasteAction;
import org.apache.directory.studio.ldapbrowser.common.dialogs.EntryExistsCopyStrategyDialogImpl;
import org.apache.directory.studio.ldapbrowser.common.dialogs.ScopeDialog;
import org.apache.directory.studio.ldapbrowser.common.dnd.EntryTransfer;
import org.apache.directory.studio.ldapbrowser.common.dnd.SearchTransfer;
import org.apache.directory.studio.ldapbrowser.common.dnd.ValuesTransfer;
import org.apache.directory.studio.ldapbrowser.core.jobs.CopyEntriesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.jobs.UpdateEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.SearchParameter;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;


// ── CLASS: BrowserPasteAction — HAN SHOOTS FIRST IN THE CANTINA ─────────────
// In the Mos Eisley Cantina, Han Solo doesn't wait politely for Greedo to make
// his move — he reads the situation fast, acts decisively, and handles the
// problem before it handles him. This class does the same: whatever the
// clipboard holds (entries, searches, or values), we figure out the type
// quickly and act without hesitation.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Handles paste operations in the LDAP browser view, routing clipboard contents
 * to the right destination — entries under a parent, searches into a connection,
 * or attribute values onto the current entry.
 * We extend {@link PasteAction} and override behavior for browser-specific
 * scenarios; the actual LDAP writes happen via background job runnables.
 * Think of this class as Han Solo in the cantina: fast, decisive, and always
 * acts before the other guy gets a shot off.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserPasteAction extends PasteAction
{
    // ── Han Walks Into the Cantina Ready ─────────────────────────────────────
    // Han strolls into Mos Eisley, hand near his blaster, ready for whatever
    // comes — no fanfare, just preparedness.
    // We do the same: construct with no arguments and let the parent set up
    // the shared infrastructure.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code BrowserPasteAction} with default state inherited
     * from {@link PasteAction}.
     * No parameters needed — the action discovers what's on the clipboard
     * lazily, at the moment the user triggers it.
     */
    public BrowserPasteAction()
    {
        super();
    }


    // ── Han Reads the Room Before Drawing ────────────────────────────────────
    // Han doesn't announce his intentions — he glances around, reads who's at
    // the table (Greedo? a search? some values?), and labels his move accordingly.
    // We inspect the clipboard right now and return the most specific label
    // ("Paste Entry", "Paste Searches", etc.) that fits what's there.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable label for the paste action that reflects exactly
     * what's on the clipboard right now — entries, searches, or values, singular
     * or plural as appropriate.
     * Eclipse shows this text in menus, so it needs to be context-sensitive and
     * up-to-date every time the menu opens.
     *
     * @return  the localised display name; falls back to a generic "Paste" if
     *          the clipboard holds nothing we can handle
     */
    public String getText()
    {
        // entry
        IEntry[] entries = getEntriesToPaste();
        if ( entries != null )
        {
            return entries.length > 1 ? Messages.getString( "BrowserPasteAction.PasteEntries" ) : Messages.getString( "BrowserPasteAction.PasteEntry" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // searches
        ISearch[] searches = getSearchesToPaste();
        if ( searches != null )
        {
            return searches.length > 1 ? Messages.getString( "BrowserPasteAction.PasteSearches" ) : Messages.getString( "BrowserPasteAction.PasteSearch" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // value
        IValue[] values = getValuesToPaste();
        if ( values != null )
        {
            return values.length > 1 ? Messages.getString( "BrowserPasteAction.PasteValues" ) : Messages.getString( "BrowserPasteAction.PasteValue" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return Messages.getString( "BrowserPasteAction.Paste" ); //$NON-NLS-1$
    }


    // ── Han Checks If He Has a Clean Shot ────────────────────────────────────
    // Han won't draw if there's nothing worth shooting at — he first confirms
    // Greedo is actually there and armed.
    // We check whether the clipboard has anything we can act on before
    // letting Eclipse enable the menu item.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} if the clipboard contains data that can be pasted
     * in the current selection context — entries, searches, or values.
     * Eclipse calls this before painting every menu to decide whether "Paste"
     * should be clickable or greyed out.
     *
     * @return  {@code true} if we have something pasteable; {@code false} otherwise
     */
    public boolean isEnabled()
    {
        // entry
        if ( getEntriesToPaste() != null )
        {
            return true;
        }

        // search
        else if ( getSearchesToPaste() != null )
        {
            return true;
        }

        // value
        else if ( getValuesToPaste() != null )
        {
            return true;
        }

        return false;
    }


    // ── Han Fires — No Hesitation ─────────────────────────────────────────────
    // The moment is here: Han pulls the trigger before Greedo can react.
    // We dispatch immediately to whichever paste sub-operation matches the
    // clipboard contents, and return — no second-guessing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Executes the paste by routing clipboard contents to the correct handler:
     * entries get copied under the selected parent, searches get cloned into the
     * active connection, and values get merged into the selected entry.
     * Only the first matching type is processed per invocation.
     */
    public void run()
    {
        // entry
        IEntry[] entries = getEntriesToPaste();
        if ( entries != null )
        {
            pasteEntries( getSelectedEntries()[0], entries );
            return;
        }

        // search
        ISearch[] searches = getSearchesToPaste();
        if ( searches != null )
        {
            pasteSearches( searches );
            return;
        }

        // value
        IValue[] values = getValuesToPaste();
        if ( values != null )
        {
            pasteValues( values );
            return;
        }

    }


    // ── Han Drops Entries Into Their New Home ─────────────────────────────────
    // Han has the cargo, he knows the destination — now he just has to figure out
    // whether to take the scenic route (subtree copy) or drop it off quick.
    // If any source entry has children, we ask the user how deep to copy; then
    // we fire off a background copy job.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Copies the given LDAP entries under {@code parent}, prompting for copy
     * depth if any source entry has children.
     * The actual work runs in a {@link StudioBrowserJob} so the UI stays
     * responsive; conflict resolution is delegated to
     * {@link EntryExistsCopyStrategyDialogImpl}.
     *
     * @param parent          the destination entry that will become the new parent
     * @param entriesToPaste  the entries being pasted; must be non-null and non-empty
     */
    private void pasteEntries( final IEntry parent, final IEntry[] entriesToPaste )
    {
        SearchScope scope = SearchScope.OBJECT;
        boolean askForScope = false;
        for ( int i = 0; i < entriesToPaste.length; i++ )
        {
            if ( entriesToPaste[i].hasChildren() )
            {
                askForScope = true;
                break;
            }
        }
        if ( askForScope )
        {
            ScopeDialog scopeDialog = new ScopeDialog( Display.getDefault().getActiveShell(),
                Messages.getString( "BrowserPasteAction.SelectCopyDepth" ), //$NON-NLS-1$
                entriesToPaste.length > 1 );
            scopeDialog.open();
            scope = scopeDialog.getScope();
        }

        new StudioBrowserJob( new CopyEntriesRunnable( parent, entriesToPaste, scope,
            new EntryExistsCopyStrategyDialogImpl(
                Display.getDefault().getActiveShell() ) ) ).execute();
    }


    // ── Han Reassigns Cargo to the Right Docking Bay ──────────────────────────
    // Han has searches on the clipboard — he clones them into whatever connection
    // the user is parked in, then pops the property dialog so they can tweak
    // the new search right away.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Clones the given {@link ISearch} objects into the currently active browser
     * connection (resolved from the selection), then opens the search properties
     * dialog if exactly one search was pasted.
     * We clone rather than move, so the originals stay wherever they came from.
     *
     * @param searches  the searches to paste; must not be null
     */
    private void pasteSearches( ISearch[] searches )
    {
        IBrowserConnection browserConnection = null;
        if ( getSelectedBrowserViewCategories().length > 0 )
        {
            browserConnection = getSelectedBrowserViewCategories()[0].getParent();
        }
        else if ( getSelectedSearches().length > 0 )
        {
            browserConnection = getSelectedSearches()[0].getBrowserConnection();
        }

        if ( browserConnection != null )
        {
            ISearch clone = null;
            for ( ISearch search : searches )
            {
                SearchParameter searchParameter = ( SearchParameter ) search.getSearchParameter().clone();
                clone = new Search( browserConnection, searchParameter );
                browserConnection.getSearchManager().addSearch( clone );
            }

            if ( searches.length == 1 )
            {
                IAdaptable element = ( IAdaptable ) clone;
                String pageId = BrowserCommonConstants.PROP_SEARCH;
                String title = clone.getName();

                PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn( getShell(), element, pageId, null,
                    null );
                if ( dialog != null )
                {
                    title = Utils.shorten( title, 30 );
                }
                dialog.getShell().setText( NLS.bind( Messages.getString( "PropertiesAction.PropertiesForX" ), title ) ); //$NON-NLS-1$
                dialog.open();
            }
        }
    }


    // ── Han Drops the Cargo on the Entry ─────────────────────────────────────
    // Han has a shipment of values and he knows exactly which entry to deliver
    // them to — he computes the diff and fires the update job.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Merges the given attribute values onto the currently selected entry by
     * computing an LDIF diff and submitting it as an {@link UpdateEntryRunnable}.
     * We resolve the real entry from cache first (bookmarks and search results
     * are proxies — we need the live entry to compute a correct diff).
     *
     * @param values  the attribute values to paste; must not be null
     */
    private void pasteValues( IValue[] values )
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
        else if ( getSelectedBookmarks().length == 1 )
        {
            entry = getSelectedBookmarks()[0].getEntry();
        }

        // always get the real entry in case it is a bookmark or search continuation
        if ( entry != null )
        {
            entry = entry.getBrowserConnection().getEntryFromCache( entry.getDn() );
        }

        if ( entry != null )
        {
            IEntry clone = new CompoundModification().cloneEntry( entry );
            new CompoundModification().createValues( clone, values );
            LdifFile diff = org.apache.directory.studio.ldapbrowser.core.utils.Utils.computeDiff( entry, clone );
            if ( diff != null )
            {
                UpdateEntryRunnable runnable = new UpdateEntryRunnable( entry,
                    diff.toFormattedString( LdifFormatParameters.DEFAULT ) );
                new StudioBrowserJob( runnable ).execute();
            }
        }

    }


    // ── Han Scans for Entries in the Cargo Hold ───────────────────────────────
    // Han checks: is there actually an entry shipment in the cargo hold that
    // matches where we're headed? Only works when exactly one destination entry
    // is selected and there are no competing items in the selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IEntry} array from the clipboard if the current
     * selection context is valid for an entry paste: exactly one entry selected,
     * and no bookmarks, searches, attributes, values, or connections competing.
     * Returns {@code null} if the conditions aren't met or the clipboard has no entries.
     *
     * @return  the entries to paste, or {@code null} if paste-entries is not applicable here
     */
    private IEntry[] getEntriesToPaste()
    {
        if ( getSelectedBookmarks().length + getSelectedSearchResults().length + getSelectedSearches().length
            + getSelectedConnections().length + getSelectedAttributes().length + getSelectedValues().length == 0
            && getSelectedEntries().length == 1 )
        {

            Object content = ClipboardUtils.getFromClipboard( EntryTransfer.getInstance() );
            if ( content instanceof IEntry[] )
            {
                IEntry[] entries = ( IEntry[] ) content;
                return entries;
            }
        }

        return null;
    }


    // ── Han Checks the Cargo Manifest for Searches ────────────────────────────
    // Han glances at the manifest — is this a search shipment? Only when we're
    // parked at a search or category node and nothing else is competing.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link ISearch} array from the clipboard if the current selection
     * is a search category or an existing search, with nothing else selected.
     * Returns {@code null} when the context doesn't call for a search paste.
     *
     * @return  the searches to paste, or {@code null} if not applicable
     */
    private ISearch[] getSearchesToPaste()
    {
        if ( getSelectedBookmarks().length + getSelectedSearchResults().length + getSelectedEntries().length
            + getSelectedConnections().length + getSelectedAttributes().length + getSelectedValues().length == 0
            && ( getSelectedSearches().length + getSelectedBrowserViewCategories().length > 0 ) )
        {
            Object content = ClipboardUtils.getFromClipboard( SearchTransfer.getInstance() );
            if ( content instanceof ISearch[] )
            {
                ISearch[] searches = ( ISearch[] ) content;
                return searches;
            }
        }

        return null;
    }


    // ── Han Checks for Value Contraband ───────────────────────────────────────
    // Han opens every compartment looking for loose values — they could be on an
    // entry, a search result, or a bookmark, as long as only one target is selected.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link IValue} array from the clipboard if exactly one entry,
     * bookmark, or search result is selected (and nothing else), so we know
     * exactly where to paste.
     * Returns {@code null} when the conditions aren't right or the clipboard
     * holds no values.
     *
     * @return  the values to paste, or {@code null} if not applicable
     */
    private IValue[] getValuesToPaste()
    {
        if ( ( getSelectedAttributes().length + getSelectedValues().length + getSelectedSearchResults().length
            + getSelectedBookmarks().length + getSelectedSearches().length + getSelectedConnections().length == 0 && ( getSelectedEntries().length == 1 ) )
            || ( getSelectedAttributes().length + getSelectedValues().length + getSelectedEntries().length
                + getSelectedSearchResults().length + getSelectedSearches().length + getSelectedConnections().length == 0 && ( getSelectedBookmarks().length == 1 ) )
            || ( getSelectedAttributes().length + getSelectedValues().length + getSelectedEntries().length
                + getSelectedBookmarks().length + getSelectedSearches().length + getSelectedConnections().length == 0 && ( getSelectedSearchResults().length == 1 ) ) )
        {
            Object content = ClipboardUtils.getFromClipboard( ValuesTransfer.getInstance() );
            if ( content instanceof IValue[] )
            {
                IValue[] values = ( IValue[] ) content;
                return values;
            }
        }

        return null;
    }

}
