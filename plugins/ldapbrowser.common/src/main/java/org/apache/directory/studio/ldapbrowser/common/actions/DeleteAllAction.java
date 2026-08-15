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


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserCategory;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.eclipse.jface.resource.ImageDescriptor;


// ── CLASS: DeleteAllAction — GRAND MOFF TARKIN ORDERS ALDERAAN DESTROYED ─────
// Grand Moff Tarkin stands on the Death Star's bridge and issues a single order:
// "You may fire when ready." He is not deleting one rebel base — he is wiping
// the entire planet. DeleteAllAction operates the same way: where DeleteAction
// targets only the selected objects, DeleteAllAction targets EVERYTHING in the
// selected container — all children of an entry, all searches on a connection,
// or all bookmarks on a connection — not just what's highlighted.
// The base class DeleteAction handles the warning dialog and execution; this
// class just overrides the target-collection methods to return the full set.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A bulk-delete variant of {@link DeleteAction} that deletes all objects in
 * the selected container rather than just the selected objects. Depending on
 * the selection:
 * <ul>
 *   <li>An entry → deletes all its children</li>
 *   <li>A search or the Searches category → deletes all searches on the connection</li>
 *   <li>A bookmark or the Bookmarks category → deletes all bookmarks on the connection</li>
 * </ul>
 * Think of this class as Grand Moff Tarkin — not deleting one record, but
 * wiping everything in the selected container with one order.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DeleteAllAction extends DeleteAction
{
    private static final Collection<IEntry> EMPTY_ENTRIES = new HashSet<IEntry>();
    private static final ISearch[] EMPTY_SEARCHES = new ISearch[0];
    private static final IBookmark[] EMPTY_BOOKMARKS = new IBookmark[0];


    // ── TARKIN TAKES HIS PLACE ON THE BRIDGE ──────────────────────────────────
    // The default constructor — no configuration needed. Tarkin walks in and
    // is immediately ready to issue planet-scale orders.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new DeleteAllAction. No configuration required — all targeting
     * logic is handled by the overridden {@code get*()} methods.
     */
    public DeleteAllAction()
    {
    }


    // ── TARKIN ISSUES THE FINAL ORDER ─────────────────────────────────────────
    // Delegates directly to the base class, which gathers targets, shows the
    // warning dialog, and executes. The overridden get*() methods below ensure
    // that "all" objects are targeted rather than just the selected ones.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Executes the bulk delete. Delegates to {@link DeleteAction#run()}, which
     * handles the confirmation dialog and dispatches deletions. The overridden
     * {@link #getEntries()}, {@link #getSearches()}, and {@link #getBookmarks()}
     * methods below provide the full target set.
     *
     * <p>For example — Tarkin issuing the order to fire:</p>
     * <pre>
     *   super.run(); // "You may fire when ready."
     * </pre>
     */
    public void run()
    {
        super.run();
    }


    // ── TARKIN ANNOUNCES THE SCOPE OF THE DESTRUCTION ─────────────────────────
    // The label adapts to what's selected: "Delete All Child Entries", "Delete
    // All Searches", "Delete All Bookmarks", or a generic "Delete All". This
    // tells the user clearly what the full scope of the operation will be.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the menu label for this action, adapted to the current selection.
     * Shows "Delete All Child Entries", "Delete All Searches", "Delete All
     * Bookmarks", or a generic "Delete All" depending on what is selected.
     *
     * <p>For example — Tarkin's announcement depends on the target:</p>
     * <pre>
     *   // entry selected          → "Delete All Child Entries"
     *   // Searches category       → "Delete All Searches"
     *   // bookmark selected       → "Delete All Bookmarks"
     *   // nothing specific        → "Delete All"
     * </pre>
     *
     * @return the localized action label string.
     */
    public String getText()
    {
        if ( getSelectedEntries().length >= 1 )
        {
            return Messages.getString( "DeleteAllAction.DeleteAllChildEntries" ); //$NON-NLS-1$
        }
        else if ( ( getSelectedSearches().length >= 1 )
            || ( ( getSelectedBrowserViewCategories().length == 1 ) && ( getSelectedBrowserViewCategories()[0]
                .getType() == BrowserCategory.TYPE_SEARCHES ) ) )
        {
            return Messages.getString( "DeleteAllAction.DeleteAllSearches" ); //$NON-NLS-1$
        }
        else if ( ( getSelectedBookmarks().length >= 1 )
            || ( ( getSelectedBrowserViewCategories().length == 1 ) && ( getSelectedBrowserViewCategories()[0]
                .getType() == BrowserCategory.TYPE_BOOKMARKS ) ) )
        {
            return Messages.getString( "DeleteAllAction.DeleteAllBookmarks" ); //$NON-NLS-1$
        }
        else
        {
            return Messages.getString( "DeleteAllAction.DeleteAll" ); //$NON-NLS-1$
        }
    }


    // ── TARKIN HOLDS UP THE DELETE-ALL INSIGNIA ───────────────────────────────
    // Uses the custom "delete all" icon rather than the standard delete icon,
    // to visually distinguish this action from single-item delete.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "delete all" icon from the browser's image registry.
     * This is a custom icon distinct from the standard single-item delete icon.
     *
     * @return the delete-all image descriptor.
     */
    public ImageDescriptor getImageDescriptor()
    {
        return BrowserCommonActivator.getDefault().getImageDescriptor( BrowserCommonConstants.IMG_DELETE_ALL );
    }


    // ── TARKIN USES NO STANDARD KEYBINDING ────────────────────────────────────
    // DeleteAllAction has no standard Eclipse command binding — it's accessible
    // only through the menu, not a keyboard shortcut.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code null} — this action has no Eclipse command ID and is not
     * bound to a keyboard shortcut. It is accessible only through the menu.
     *
     * @return null.
     */
    public String getCommandId()
    {
        return null;
    }


    // ── TARKIN TARGETS ALL CHILDREN, NOT JUST THE SELECTED ENTRY ─────────────
    // Instead of deleting the selected entry itself, we collect all of its
    // direct children. This is the "fire on the whole planet" override — if
    // an entry is selected, we return its children; otherwise, nothing.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns all direct children of the first selected entry, rather than
     * the selected entry itself. If no entry is selected, returns an empty
     * collection. This is what makes DeleteAllAction delete everything inside
     * a container rather than just the container.
     *
     * @return a collection containing all child entries of the selected entry;
     *         empty if no entry is selected.
     */
    protected Collection<IEntry> getEntries()
    {
        if ( getSelectedEntries().length >= 1 )
        {
            Collection<IEntry> values = new HashSet<IEntry>();
            values.addAll( Arrays.asList( getSelectedEntries()[0].getChildren() ) );
            return values;
        }
        else
        {
            return EMPTY_ENTRIES;
        }
    }


    // ── TARKIN TARGETS ALL SEARCHES ON THE CONNECTION ─────────────────────────
    // Instead of deleting just the selected search, we ask the connection's
    // search manager for every search it knows about and return the full list.
    // Selecting the Searches category node also triggers this path.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns all searches on the connection associated with the first selected
     * search, or all searches for the connection behind the selected Searches
     * category node. Returns an empty array if neither applies.
     *
     * @return all searches on the relevant connection; empty array if none.
     */
    protected ISearch[] getSearches()
    {
        if ( getSelectedSearches().length >= 1 )
        {
            return getSelectedSearches()[0].getBrowserConnection().getSearchManager().getSearches().toArray(
                new ISearch[0] );
        }
        else if ( ( getSelectedBrowserViewCategories().length == 1 )
            && ( getSelectedBrowserViewCategories()[0].getType() == BrowserCategory.TYPE_SEARCHES ) )
        {
            return getSelectedBrowserViewCategories()[0].getParent().getSearchManager().getSearches().toArray(
                new ISearch[0] );
        }
        else
        {
            return EMPTY_SEARCHES;
        }
    }


    // ── TARKIN TARGETS ALL BOOKMARKS ON THE CONNECTION ────────────────────────
    // Instead of deleting just the selected bookmark, we fetch all bookmarks
    // from the connection's bookmark manager. Selecting the Bookmarks category
    // node also triggers this path.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Returns all bookmarks on the connection associated with the first selected
     * bookmark, or all bookmarks for the connection behind the selected Bookmarks
     * category node. Returns an empty array if neither applies.
     *
     * @return all bookmarks on the relevant connection; empty array if none.
     */
    protected IBookmark[] getBookmarks()
    {
        if ( getSelectedBookmarks().length >= 1 )
        {
            return getSelectedBookmarks()[0].getBrowserConnection().getBookmarkManager().getBookmarks();
        }
        else if ( ( getSelectedBrowserViewCategories().length == 1 )
            && ( getSelectedBrowserViewCategories()[0].getType() == BrowserCategory.TYPE_BOOKMARKS ) )
        {
            return getSelectedBrowserViewCategories()[0].getParent().getBookmarkManager().getBookmarks();
        }
        else
        {
            return EMPTY_BOOKMARKS;
        }
    }
}
