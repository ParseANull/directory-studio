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

package org.apache.directory.studio.ldapbrowser.ui.editors.searchresult;


import org.apache.directory.studio.valueeditors.ValueEditorManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;


// ── CLASS: SearchResultEditorConfiguration — Palpatine Issues the Standing Orders ──
// Before Order 66 can be executed, Palpatine writes the rulebook: which units
// handle which assignments, what the default behavior is, who has authority over
// what.  The individual clone troopers just ask the rulebook when they need guidance.
// This class is that rulebook: it lazily instantiates and hands out the sorter,
// filter, content provider, label provider, cell modifier, cursor, value editor
// manager, and context menu manager — all the subsystems of the search result editor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Central factory and lifecycle holder for all search result editor subsystems.
 * Rather than each component constructing its own collaborators, they ask this
 * configuration object for them — which creates instances lazily on first request
 * and caches them for reuse.
 * Think of this as Palpatine's standing orders: each component checks the rulebook
 * to find out what its configuration is, rather than improvising.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorConfiguration
{

    /** The disposed flag. */
    private boolean disposed = false;

    /** The cursor. */
    protected SearchResultEditorCursor cursor;

    protected SearchResultEditorSorter sorter;

    protected SearchResultEditorFilter filter;

    protected SearchResultEditorContentProvider contentProvider;

    protected SearchResultEditorLabelProvider labelProvider;

    protected SearchResultEditorCellModifier cellModifier;

    protected ValueEditorManager valueEditorManager;

    protected MenuManager contextMenuManager;


    // ── Rulebook Is Drafted ───────────────────────────────────────────────────
    // Palpatine assembles the rulebook — but it's empty until the troopers ask
    // for their individual assignments.  We store the workbench part reference
    // for later use by subsystems that need site context.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new configuration for the given workbench part.
     * All subsystems are lazily created on first request.
     *
     * @param part the workbench part (the search result editor) that owns this configuration
     */
    public SearchResultEditorConfiguration( IWorkbenchPart part )
    {
        super();
    }


    // ── Rulebook Is Rescinded ─────────────────────────────────────────────────
    // Palpatine's rule is over — the rulebook is shredded, every subsystem torn down.
    // We dispose each non-null subsystem exactly once (guarded by the disposed flag)
    // and null out all references to prevent memory leaks.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Disposes all cached subsystems.
     * We guard with a {@code disposed} flag so multiple calls are safe.
     * Each non-null subsystem gets its own dispose call before we null its reference.
     */
    public void dispose()
    {
        if ( !disposed )
        {
            if ( contentProvider != null )
            {
                contentProvider.dispose();
                contentProvider = null;
            }
            if ( labelProvider != null )
            {
                labelProvider.dispose();
                labelProvider = null;
            }
            if ( cellModifier != null )
            {
                cellModifier.dispose();
                cellModifier = null;
            }
            if ( valueEditorManager != null )
            {
                valueEditorManager.dispose();
                valueEditorManager = null;
            }
            if ( contextMenuManager != null )
            {
                contextMenuManager.dispose();
                contextMenuManager = null;
            }
            if ( cursor != null )
            {
                cursor.dispose();
                cursor = null;
            }
            disposed = true;
        }
    }


    // ── Rulebook: Assign the Sort Commander ───────────────────────────────────
    // The rulebook specifies who sorts the ranks — lazily created the first time
    // someone asks.  Every subsequent call gets the same cached instance.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the sorter for the search result table, creating it on first call.
     * The sorter handles column-header clicks to sort the results alphabetically
     * by the clicked column.
     *
     * @return the shared {@link SearchResultEditorSorter} instance
     */
    public SearchResultEditorSorter getSorter()
    {
        if ( sorter == null )
        {
            sorter = new SearchResultEditorSorter();
        }
        return sorter;
    }


    // ── Rulebook: Assign the Filter Commander ─────────────────────────────────
    // The rulebook specifies who filters the ranks — lazily created and cached.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the filter for the search result table, creating it on first call.
     * The filter handles quick-filter text input to hide non-matching rows.
     *
     * @return the shared {@link SearchResultEditorFilter} instance
     */
    public SearchResultEditorFilter getFilter()
    {
        if ( filter == null )
        {
            filter = new SearchResultEditorFilter();
        }
        return filter;
    }


    // ── Rulebook: Assign the Context Menu Commander ───────────────────────────
    // The context menu is attached to the cursor widget (not the table directly)
    // so it appears wherever the cursor is.  Created once, cached.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the context menu manager for the table, creating it on first call.
     * The menu is attached to the cursor control so right-clicking near the cursor
     * shows the correct context for the selected cell.
     *
     * @param viewer the table viewer whose cursor will host the context menu
     * @return the shared {@link IMenuManager} for the context menu
     */
    public IMenuManager getContextMenuManager( TableViewer viewer )
    {
        if ( contextMenuManager == null )
        {
            contextMenuManager = new MenuManager();
            Menu menu = contextMenuManager.createContextMenu( viewer.getControl() );
            getCursor( viewer ).setMenu( menu );
        }
        return contextMenuManager;
    }


    // ── Rulebook: Assign the Content Commander ────────────────────────────────
    // The content provider feeds data rows into the table — it needs both the widget
    // (for display callbacks) and the configuration (for filter/sorter access).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the content provider for the table, creating it on first call.
     * The content provider bridges between the {@link org.apache.directory.studio.ldapbrowser.core.model.ISearch}
     * input and the rows displayed in the JFace TableViewer.
     *
     * @param mainWidget the search result editor widget; the content provider uses it to
     *                   update the info text and quick filter state
     * @return the shared {@link SearchResultEditorContentProvider} instance
     */
    public SearchResultEditorContentProvider getContentProvider( SearchResultEditorWidget mainWidget )
    {
        if ( contentProvider == null )
        {
            contentProvider = new SearchResultEditorContentProvider( mainWidget, this );
        }
        return contentProvider;
    }


    // ── Rulebook: Assign the Display Commander ────────────────────────────────
    // The label provider renders cell text and font — it needs the value editor
    // manager to format attribute values for display.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the label provider for the table, creating it on first call.
     * The label provider decides what text and font appear in each cell,
     * delegating value formatting to the {@link ValueEditorManager}.
     *
     * @param viewer the table viewer (used to resolve the value editor manager)
     * @return the shared {@link SearchResultEditorLabelProvider} instance
     */
    public SearchResultEditorLabelProvider getLabelProvider( TableViewer viewer )
    {
        if ( labelProvider == null )
        {
            labelProvider = new SearchResultEditorLabelProvider( getValueEditorManager( viewer ) );
        }
        return labelProvider;
    }


    // ── Rulebook: Assign the Edit Commander ──────────────────────────────────
    // The cell modifier gates and performs inline edits — it needs both the value
    // editor manager (to know what editors are available) and the cursor (to get the
    // working copy of the selected result).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the cell modifier for the table, creating it on first call.
     * The cell modifier implements the JFace {@link org.eclipse.jface.viewers.ICellModifier}
     * interface to gate, read, and apply inline edits to the LDAP model.
     *
     * @param viewer the table viewer (used to resolve cursor and value editor manager)
     * @return the shared {@link SearchResultEditorCellModifier} instance
     */
    public SearchResultEditorCellModifier getCellModifier( TableViewer viewer )
    {
        if ( cellModifier == null )
        {
            cellModifier = new SearchResultEditorCellModifier( getValueEditorManager( viewer ), getCursor( viewer ) );
        }
        return cellModifier;
    }


    // ── Rulebook: Assign the Cursor Commander ────────────────────────────────
    // The cursor is a custom SWT widget that sits on top of the table and tracks
    // which cell the user is "at" — it's different from the mouse cursor and
    // supports keyboard navigation within the table.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the table cursor for the given viewer, creating it on first call.
     * The cursor is a custom SWT {@code TableCursor} that tracks cell-level
     * keyboard navigation and provides selection context for all actions.
     *
     * @param viewer the table viewer to attach the cursor to
     * @return the shared {@link SearchResultEditorCursor} instance
     */
    public SearchResultEditorCursor getCursor( TableViewer viewer )
    {
        if ( cursor == null )
        {
            cursor = new SearchResultEditorCursor( viewer );
        }
        return cursor;
    }


    // ── Rulebook: Assign the Value Editor Commander ───────────────────────────
    // The value editor manager knows which editor widget handles which LDAP attribute
    // type.  It's shared across the label provider, cell modifier, and all the open-
    // editor actions.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value editor manager for the given viewer, creating it on first call.
     * The {@link ValueEditorManager} maps LDAP attribute types to cell editor widgets
     * and is shared across the label provider, cell modifier, and all open-editor actions.
     *
     * @param viewer the table viewer whose SWT Table widget hosts the cell editors
     * @return the shared {@link ValueEditorManager} instance
     */
    public ValueEditorManager getValueEditorManager( TableViewer viewer )
    {
        if ( valueEditorManager == null )
        {
            valueEditorManager = new ValueEditorManager( viewer.getTable(), true, true );
        }
        return valueEditorManager;
    }

}
