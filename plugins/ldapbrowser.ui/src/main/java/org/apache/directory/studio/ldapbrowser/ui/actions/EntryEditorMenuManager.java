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


import java.util.Collection;

import org.apache.directory.studio.entryeditors.EntryEditorExtension;
import org.apache.directory.studio.entryeditors.EntryEditorManager;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;


// ── CLASS: EntryEditorMenuManager — LANDO RUNS THE CLOUD CITY OPERATIONS ────
// Lando Calrissian doesn't just own Cloud City — he orchestrates everything:
// which platforms are open, which staff are on duty, who gets assigned to
// which docking bay. When Leia's ship arrives, Lando assembles the right team
// dynamically based on who's docked. This class manages the "Open With" submenu
// in the same way: every time the menu is about to open, we ask who's available
// (which entry editors can handle this entry?) and build the menu fresh.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A dynamic JFace {@link MenuManager} that populates the "Open With" submenu
 * with one action per entry editor extension capable of handling the currently
 * selected entry.
 * Because the set of applicable editors can change with every selection, we
 * rebuild the menu every time it's about to be shown — stale items are removed
 * and fresh actions are added. A "Preferences…" item is always appended at the
 * bottom as a stable exit to the entry-editor configuration page.
 * Think of this class as Lando managing Cloud City: the roster is always fresh,
 * every arrival gets the right handler, and there's always a way to change the
 * rules at the bottom of the menu.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorMenuManager extends MenuManager implements IMenuListener
{
    /** The selection provider */
    private ISelectionProvider selectionProvider;

    /** The OpenEntryEditorsPreferencePageAction */
    private OpenEntryEditorsPreferencePageAction openEntryEditorsPreferencePageAction;


    // ── Lando Opens the Cloud City Operations Centre ──────────────────────────
    // Lando steps onto the landing platform, greets the new arrivals, and
    // makes sure the operations centre is wired up to listen for incoming ships.
    // We set the selection provider (so we know who just landed) and register
    // ourselves as a menu listener so we can rebuild the docking assignments
    // before the platform opens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new {@code EntryEditorMenuManager} wired to the given selection
     * provider.
     * The menu label is localised ("Open With") and we register {@code this} as
     * a menu listener so we can repopulate actions in {@link #menuAboutToShow}
     * every time the submenu opens.
     *
     * @param selectionProvider  the JFace selection provider that tells us which
     *                           entry is currently selected; must not be null
     */
    public EntryEditorMenuManager( ISelectionProvider selectionProvider )
    {
        super( Messages.getString( "EntryEditorMenuManager.OpenWith" ) ); //$NON-NLS-1$
        this.selectionProvider = selectionProvider;
        openEntryEditorsPreferencePageAction = new OpenEntryEditorsPreferencePageAction();
        addMenuListener( this );
    }


    // ── Lando Assembles the Right Staff for Each Arrival ─────────────────────
    // Every time a ship lands, Lando clears the old crew assignments and
    // reassigns fresh staff based on who's available and who can handle this
    // particular passenger. We clear stale menu items, ask the
    // {@link EntryEditorManager} which editors can handle the selected entry,
    // and create one action per compatible editor.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Rebuilds the "Open With" submenu contents immediately before it is shown.
     * All previously added items are removed first, then a fresh action is
     * added for each {@link EntryEditorExtension} that reports it can handle
     * the currently selected entry. A separator and the preferences action are
     * always appended at the end.
     * This method is called by JFace's menu infrastructure — do not call it directly.
     *
     * @param manager  the menu manager being populated; same as {@code this}
     */
    public void menuAboutToShow( IMenuManager manager )
    {
        // As the Menu Manager is dynamic, we need to
        // remove all the previously added actions
        removeAll();

        // Getting the currently selected entry
        IEntry selectedEntry = getCurrentSelection();
        if ( selectedEntry != null )
        {
            // Getting the entry editors and creating an action for each one
            // that can handle the entry
            Collection<EntryEditorExtension> entryEditors = BrowserUIPlugin.getDefault().getEntryEditorManager()
                .getSortedEntryEditorExtensions();
            for ( EntryEditorExtension entryEditor : entryEditors )
            {
                // Verifying that the editor can handle the entry
                if ( entryEditor.getEditorInstance().canHandle( selectedEntry ) )
                {
                    // Creating the action associated with the entry editor
                    add( createAction( entryEditor ) );
                }
            }
        }

        // Separator
        add( new Separator() );

        // Preferences Action
        add( openEntryEditorsPreferencePageAction );
    }


    // ── Lando Checks Who Just Docked ──────────────────────────────────────────
    // Lando glances at the landing platform monitor — did a VIP arrive? A search
    // result? A bookmark alias? He needs to resolve the real identity of the
    // arrival before assigning staff.
    // We unwrap the first element of the structured selection to get the actual
    // {@link IEntry}, handling {@link ISearchResult} and {@link IBookmark} proxies.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Resolves the {@link IEntry} for the first item in the current structured
     * selection, unwrapping {@link ISearchResult} and {@link IBookmark} proxies.
     *
     * @return  the selected entry, or {@code null} if the selection is empty or
     *          does not contain an entry-like object
     */
    private IEntry getCurrentSelection()
    {
        StructuredSelection structuredSelection = ( StructuredSelection ) selectionProvider.getSelection();
        if ( !structuredSelection.isEmpty() )
        {
            Object selection = structuredSelection.getFirstElement();
            if ( selection instanceof IEntry )
            {
                return ( IEntry ) selection;
            }
            else if ( selection instanceof ISearchResult )
            {
                return ( ( ISearchResult ) selection ).getEntry();
            }
            else if ( selection instanceof IBookmark )
            {
                return ( ( IBookmark ) selection ).getEntry();
            }
        }

        return null;
    }


    // ── Lando Assigns a Staff Member to the New Arrival ───────────────────────
    // Lando picks the right person for the job — each entry editor extension
    // gets its own staff member (an Eclipse {@link Action}) who knows how to
    // open that specific editor for the selected entry.
    // We create an anonymous Action with the editor's name and icon, and wire
    // its {@code run()} to open that editor via the {@link EntryEditorManager}.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an Eclipse {@link IAction} that opens the given entry editor for
     * the currently selected entry when triggered.
     * The action's label and icon come from the {@link EntryEditorExtension}
     * metadata, so the menu item looks exactly as the editor declares itself.
     *
     * @param entryEditorExtension  the editor extension to wrap; must not be null
     * @return  a ready-to-add menu action that opens this editor on the current selection
     */
    private IAction createAction( final EntryEditorExtension entryEditorExtension )
    {
        Action action = new Action( entryEditorExtension.getName(), entryEditorExtension.getIcon() )
        {
            public void run()
            {
                EntryEditorManager entryEditorManager = BrowserUIPlugin.getDefault().getEntryEditorManager();
                ISelection selection = selectionProvider.getSelection();
                IEntry[] selectedEntries = BrowserSelectionUtils.getEntries( selection );
                ISearchResult[] selectedSearchResults = BrowserSelectionUtils.getSearchResults( selection );
                IBookmark[] selectedBookMarks = BrowserSelectionUtils.getBookmarks( selection );
                entryEditorManager.openEntryEditor( entryEditorExtension, selectedEntries, selectedSearchResults,
                    selectedBookMarks );
            }
        };

        return action;
    }


    // ── Lando Checks if Anyone Important Has Landed ───────────────────────────
    // Lando only opens the operations platform for real guests — he won't bother
    // if no single entry, search result, or bookmark is at the gate.
    // We return true only when exactly one entry-like item is in the selection.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} when exactly one entry, search result, or bookmark
     * is selected, indicating that the "Open With" submenu makes sense.
     * Eclipse uses this to decide whether to render the submenu at all.
     *
     * @return  {@code true} if exactly one entry-like item is selected
     */
    public boolean isVisible()
    {
        ISelection selection = selectionProvider.getSelection();

        IBookmark[] selectedBookMarks = BrowserSelectionUtils.getBookmarks( selection );
        IEntry[] selectedEntries = BrowserSelectionUtils.getEntries( selection );
        ISearchResult[] selectedSearchResults = BrowserSelectionUtils.getSearchResults( selection );

        return ( selectedSearchResults.length + selectedBookMarks.length + selectedEntries.length == 1 );
    }


    // ── Lando Confirms Cloud City Operations Are Always Dynamic ──────────────
    // Cloud City's operations change with every arrival — Lando never caches
    // a static roster. We tell JFace that this menu is always dynamic, so it
    // always calls {@link #menuAboutToShow} before rendering.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} to signal to JFace that this menu must be rebuilt
     * every time it is shown.
     * A dynamic menu always triggers {@link #menuAboutToShow} before rendering,
     * ensuring the action list stays current with the selection.
     *
     * @return  {@code true} always
     */
    public boolean isDynamic()
    {
        return true;
    }
}
