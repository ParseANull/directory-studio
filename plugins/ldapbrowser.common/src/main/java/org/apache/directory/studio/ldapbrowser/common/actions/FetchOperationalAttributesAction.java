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

import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: FetchOperationalAttributesAction — OBI-WAN SENSING THE FORCE ──────
// In the Millennium Falcon, Obi-Wan Kenobi suddenly stops mid-sentence, closes
// his eyes, and reaches out with the Force. "I felt a great disturbance in the
// Force," he says. He's perceiving information that isn't in the normal data
// stream — something hidden below the surface. Operational attributes in LDAP
// are exactly like that: they exist on every entry but the server doesn't send
// them unless you explicitly ask with a "+" wildcard. Things like createTimestamp,
// modifyTimestamp, entryUUID, and creatorsName are all operational attributes.
// This action is the toggle that tells the browser to reach out with "+" and
// bring those hidden attributes back — or stop doing so.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A checkbox action that toggles whether operational (system) attributes are
 * requested when loading an LDAP entry's attributes. Operational attributes —
 * like {@code createTimestamp}, {@code modifyTimestamp}, and {@code entryUUID}
 * — are normally suppressed by servers unless explicitly requested with a "+"
 * in the attribute list. Toggling this on re-fetches the entry with the extra
 * request included. Only available when the connection isn't already configured
 * globally to fetch operational attributes for all entries.
 * Think of this class as Obi-Wan reaching out with the Force to sense what's
 * hidden beneath the ordinary attribute listing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class FetchOperationalAttributesAction extends BrowserAction
{
    // ── OBI-WAN TAKES HIS SEAT ON THE FALCON ──────────────────────────────────
    // Nothing special needed at construction time — Obi-Wan simply arrives
    // ready to sense. The action computes its state from the selection at runtime.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new FetchOperationalAttributesAction. No arguments needed —
     * the enabled/checked states are computed dynamically from the current selection.
     */
    public FetchOperationalAttributesAction()
    {
    }


    // ── OBI-WAN'S SENSING IS A TOGGLE, NOT A ONE-SHOT MOVE ────────────────────
    // This action renders as a checkbox — click it to start sensing hidden
    // attributes; click again to stop.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@link Action#AS_CHECK_BOX}, indicating this action renders as a
     * toggle checkbox in menus. The checked state shows whether operational
     * attribute fetching is currently active for the selected entries.
     *
     * @return the SWT/JFace style constant for a checkbox action.
     */
    @Override
    public int getStyle()
    {
        return Action.AS_CHECK_BOX;
    }


    // ── OBI-WAN NAMES WHAT HE'S SENSING ───────────────────────────────────────
    // The label is fetched from the NLS message bundle so it can be localized.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action ("Fetch Operational Attributes"
     * or the localized equivalent). Retrieved from the NLS message bundle.
     *
     * @return the localized action label.
     */
    @Override
    public String getText()
    {
        return Messages.getString( "FetchOperationalAttributesAction.FetchOperationalAttributes" ); //$NON-NLS-1$
    }


    // ── OBI-WAN SENSES WITHOUT SHOWING AN ICON ────────────────────────────────
    // No icon for this action — it appears as text only in the context menu.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this action has no icon and appears as text only.
     *
     * @return null.
     */
    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── NO KEYBINDING ─────────────────────────────────────────────────────────
    // No standard Eclipse command ID is wired up for this action.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — no Eclipse command ID and no keyboard shortcut.
     *
     * @return null.
     */
    @Override
    public String getCommandId()
    {
        return null;
    }


    // ── OBI-WAN CAN SENSE ONLY WHEN THE CONNECTION ISN'T ALREADY ALWAYS SENSING
    // If the connection is already configured to always fetch operational
    // attributes for every entry, this per-entry toggle makes no sense and is
    // disabled. Also requires at least one entry to be in the selection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when at least one entry is selected and the connection
     * is NOT already configured globally to fetch operational attributes for all
     * entries. If the connection already fetches them always, this per-entry
     * toggle is redundant and is disabled.
     *
     * @return {@code true} if operational attribute fetching can be toggled.
     */
    @Override
    public boolean isEnabled()
    {
        List<IEntry> entries = getEntries();
        return !entries.isEmpty() && !entries.iterator().next().getBrowserConnection().isFetchOperationalAttributes();
    }


    // ── OBI-WAN CHECKS WHETHER HE'S ALREADY SENSING ───────────────────────────
    // The checkbox is checked when ALL selected entries already have
    // initOperationalAttributes = true. Any entry with it false unchecks the box.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when all selected entries currently have operational
     * attribute initialization enabled ({@code isInitOperationalAttributes() == true}).
     * Returns {@code false} if any entry has it disabled, or if no entries are selected.
     *
     * @return {@code true} if all selected entries are fetching operational attributes.
     */
    @Override
    public boolean isChecked()
    {
        boolean checked = true;
        List<IEntry> entries = getEntries();
        if ( entries.isEmpty() )
        {
            checked = false;
        }
        else
        {
            for ( IEntry entry : entries )
            {
                if ( !entry.isInitOperationalAttributes() )
                {
                    checked = false;
                }
            }
        }
        return checked;
    }


    // ── OBI-WAN REACHES OUT — THE HIDDEN ATTRIBUTES APPEAR ────────────────────
    // We flip the initOperationalAttributes flag on every targeted entry to the
    // opposite of the current state, then kick off a background job to re-fetch
    // the attributes. The entry editor refreshes to show the newly revealed
    // operational attributes (or hides them if we're toggling off).
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Toggles operational attribute fetching for all targeted entries and
     * re-initializes their attributes. Sets each entry's
     * {@code initOperationalAttributes} flag to the opposite of the current
     * checked state, then dispatches a background {@link InitializeAttributesRunnable}
     * to reload the entry attributes with the new setting.
     *
     * <p>For example — Obi-Wan reaching out, the Force revealing what was hidden:</p>
     * <pre>
     *   entry.setInitOperationalAttributes( !isChecked() );
     *   new StudioBrowserJob( new InitializeAttributesRunnable( entries ) ).execute();
     * </pre>
     */
    @Override
    public void run()
    {
        IEntry[] entries = getEntries().toArray( new IEntry[0] );
        boolean init = !isChecked();
        for ( IEntry entry : entries )
        {
            entry.setInitOperationalAttributes( init );
        }
        new StudioBrowserJob( new InitializeAttributesRunnable( entries ) ).execute();
    }


    // ── OBI-WAN SENSES ACROSS ALL ENTRY TYPES ─────────────────────────────────
    // We gather entries from four sources: directly selected entries, search
    // results (unwrapped to their entries), bookmarks (unwrapped to their entries),
    // and the current entry-editor input. The input is handled specially because
    // the editor may hold a cloned copy — we look up the real entry from the
    // connection cache before including it.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns all LDAP entries targeted by this action, collected from:
     * <ul>
     *   <li>Directly selected entries</li>
     *   <li>The underlying entries of selected search results</li>
     *   <li>The underlying entries of selected bookmarks</li>
     *   <li>The current entry-editor input (looked up from the connection cache
     *       to ensure we get the live object, not a clone)</li>
     * </ul>
     *
     * @return a list of all targeted entries; may be empty, never null.
     */
    protected List<IEntry> getEntries()
    {
        List<IEntry> entriesList = new ArrayList<IEntry>();
        entriesList.addAll( Arrays.asList( getSelectedEntries() ) );
        for ( ISearchResult sr : getSelectedSearchResults() )
        {
            entriesList.add( sr.getEntry() );
        }
        for ( IBookmark bm : getSelectedBookmarks() )
        {
            entriesList.add( bm.getEntry() );
        }
        if ( getInput() instanceof IEntry )
        {
            // the entry input is usually a cloned entry, lookup the real entry from connection
            IEntry input = ( IEntry ) getInput();
            IEntry entry = input.getBrowserConnection().getEntryFromCache( input.getDn() );
            if ( entry != null )
            {
                entriesList.add( entry );
            }
        }
        return entriesList;
    }

}
