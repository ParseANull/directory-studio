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


import org.apache.directory.studio.entryeditors.EntryEditorManager;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: OpenEntryEditorAction — OPENING THE TANTIVE IV BRIDGE ─────────────
// The Tantive IV's bridge had multiple stations — navigation, weapons, comms —
// all working together to let the crew inspect and command the ship.  An entry
// editor is the same concept: multiple panels (attribute table, LDIF view, outline)
// all working together to let the user inspect and edit a single LDAP entry.
// OpenEntryEditorAction is the button that opens the bridge doors and brings that
// control deck to life for a selected entry, search result, or bookmark.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Opens the appropriate entry editor for the currently selected LDAP entry,
 * search result, or bookmark.
 * The {@link EntryEditorManager} decides which specific editor to use — the default
 * table editor, a LDIF editor, or something contributed by a third-party plugin.
 * Think of this class as the door to the Tantive IV bridge: press it and the full
 * command deck opens up for the selected entry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OpenEntryEditorAction extends BrowserAction
{

    // ── Bridge Access Card Initialised ──────────────────────────────────────────
    // Before you can open the bridge doors you need a valid access card — ours is
    // the BrowserAction context, set up by the no-arg constructor via Eclipse's
    // extension-point mechanism.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OpenEntryEditorAction.
     * Eclipse calls this when instantiating the action from a plugin.xml extension.
     * The BrowserAction parent wires up the selection listeners.
     */
    public OpenEntryEditorAction()
    {
    }


    // ── Bridge Doors Slide Open ──────────────────────────────────────────────────
    // The moment the access card is swiped, the bridge doors slide open and the crew
    // rushes in.  run() delegates to EntryEditorManager.openEntryEditor(), passing
    // the currently selected entries, search results, and bookmarks — the manager
    // picks the best editor and opens it as an Eclipse editor tab.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Opens the entry editor for the current selection.
     * We delegate to {@link EntryEditorManager#openEntryEditor(IEntry[], ISearchResult[], IBookmark[])}
     * which handles the logic of choosing and activating the right editor type.
     * If nothing is selected, nothing happens — the manager guards against empty inputs.
     */
    public void run()
    {
        EntryEditorManager entryEditorManager = BrowserUIPlugin.getDefault().getEntryEditorManager();
        entryEditorManager.openEntryEditor( getSelectedEntries(), getSelectedSearchResults(), getSelectedBookmarks() );
    }


    // ── Bridge Station Has A Name Tag ───────────────────────────────────────────
    // Each bridge station had a label: "NAVIGATION", "WEAPONS", "COMMS."  Our
    // getText() adapts its label to what's selected — "Open Search Result",
    // "Open Bookmark", or just "Open Entry" — so the menu item is always clear.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the display label for this action, adapted to the current selection.
     * The label changes depending on whether a search result, bookmark, or plain
     * entry is selected, making the menu item context-sensitive and readable.
     *
     * @return the localised label string appropriate for the current selection
     */
    public String getText()
    {
        if ( getSelectedSearchResults().length == 1
            && getSelectedBookmarks().length + getSelectedEntries().length + getSelectedBrowserViewCategories().length == 0 )
        {
            return Messages.getString( "OpenEntryEditorAction.OpenSearchResult" ); //$NON-NLS-1$
        }
        else if ( getSelectedBookmarks().length == 1
            && getSelectedSearchResults().length + getSelectedEntries().length
                + getSelectedBrowserViewCategories().length == 0 )
        {
            return Messages.getString( "OpenEntryEditorAction.OpenBookmark" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "OpenEntryEditorAction.OpenEntry" ); //$NON-NLS-1$
        }
    }


    // ── No Emblem Above The Bridge Door ─────────────────────────────────────────
    // The Tantive IV's bridge door didn't need a logo — everyone knew what it was.
    // This action has no dedicated toolbar icon, so we return null.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the image descriptor for this action's icon.
     * No dedicated icon is registered for this action; Eclipse will show text only.
     *
     * @return always null
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── No Special Clearance Code ────────────────────────────────────────────────
    // The Tantive IV's bridge used standard Rebel Alliance access codes — nothing
    // custom.  We have no registered Eclipse command ID for this action.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the Eclipse command ID for keybinding.
     * No global command is registered for "Open Entry Editor," so null is returned.
     *
     * @return always null
     */
    public String getCommandId()
    {
        return null;
    }


    // ── Bridge Is Accessible With One Crew Member ────────────────────────────────
    // The bridge needs at least one person to operate — one crew member, one
    // console.  We enable this action when exactly one selectable item (entry,
    // search result, or bookmark) is selected, or when attributes/values are
    // selected (in which case the editor opens to show that entry's detail).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Reports whether this action is available for the current selection.
     * We enable when exactly one entry, search result, or bookmark is selected,
     * or when at least one attribute or value is selected (useful for opening the
     * editor to the relevant entry from an attribute-level selection).
     *
     * @return true if a valid single-item or attribute-level selection exists
     */
    public boolean isEnabled()
    {
        return getSelectedEntries().length + getSelectedSearchResults().length + getSelectedBookmarks().length == 1
            || getSelectedAttributes().length + getSelectedValues().length > 0;
    }
}
