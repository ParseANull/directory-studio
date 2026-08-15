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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.SearchResult;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;


// ── CLASS: SearchResultEditorCursor — R2-D2 Navigating the Death Star Computer ──
// R2-D2 plugs into the Death Star's computer terminal and moves through sector
// after sector of data — he knows exactly which corridor (row) and which data
// point (column) he's at, and he can read the current value at that location.
// He also clones what he finds into a working copy before modifying anything,
// so the original is always safe.
// This cursor works the same way: it tracks the current row+column in the result
// table, maintains a working copy of the selected entry for safe editing, and
// broadcasts selection events so actions know what's currently under the cursor.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A custom table cursor for the search result editor that combines cell-level
 * navigation with selection-provider semantics and entry-change awareness.
 * We extend SWT's {@link TableCursor} to add:
 * <ul>
 *   <li>JFace {@link ISelectionProvider} so actions get typed selection events</li>
 *   <li>Working-copy management — we clone the selected entry so edits don't
 *       corrupt the live LDAP model until explicitly committed</li>
 *   <li>Entry-update listener so the table refreshes when the LDAP model changes</li>
 * </ul>
 * Think of this as R2-D2 navigating the Death Star data terminal — precise
 * position tracking plus safe copy-before-modify.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorCursor extends TableCursor implements ISelectionProvider, EntryUpdateListener
{

    /** The viewer. */
    private TableViewer viewer;

    /** The selection changes listener list. */
    private List<ISelectionChangedListener> selectionChangesListenerList;

    /** The cloned reference copy of the search result under the cursor */
    private ISearchResult referenceCopy;

    /** The cloned working copy of the search result under the cursor */
    private ISearchResult workingCopy;


    // ── R2 Plugs Into the Terminal ────────────────────────────────────────────
    // R2-D2 finds the access point, plugs in, and immediately sets up his sensors —
    // he picks the right display colors, registers for entry-update broadcasts,
    // and initializes his internal navigation and selection machinery.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Constructs the cursor on the given table viewer and wires up all listeners.
     * We paint the cursor in the system's list-selection colors, register with the
     * global event registry for entry updates, then initialize the selection-bounds
     * checker and the selection-provider machinery.
     *
     * @param viewer the JFace TableViewer this cursor navigates
     */
    public SearchResultEditorCursor( TableViewer viewer )
    {
        super( viewer.getTable(), SWT.NONE );
        this.viewer = viewer;
        this.selectionChangesListenerList = new ArrayList<ISelectionChangedListener>();

        setBackground( Display.getDefault().getSystemColor( SWT.COLOR_LIST_SELECTION ) );
        setForeground( Display.getDefault().getSystemColor( SWT.COLOR_LIST_SELECTION_TEXT ) );

        EventRegistry.addEntryUpdateListener( this, BrowserCommonActivator.getDefault().getEventRunner() );

        initSelectionChecker();
        initSelectionProvider();
    }


    // ── R2 Ensures He Doesn't Navigate Off the Edge ───────────────────────────
    // When the table has fewer columns than R2 expects, he snaps himself back to
    // the last valid column so he doesn't fall off the grid.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes a selection listener that keeps the cursor within valid column bounds.
     * If the cursor is in a column index beyond the current column count (e.g. after
     * the column set shrinks), we snap it back to the last valid column.
     */
    private void initSelectionChecker()
    {
        addSelectionListener( new SelectionListener()
        {
            public void widgetSelected( SelectionEvent e )
            {
                checkSelection();
            }


            public void widgetDefaultSelected( SelectionEvent e )
            {
                checkSelection();
            }


            private void checkSelection()
            {
                if ( viewer != null && viewer.getColumnProperties() != null
                    && viewer.getColumnProperties().length - 1 < getColumn() )
                {
                    setSelection( getRow(), viewer.getColumnProperties().length - 1 );
                }
            }
        } );
    }


    // ── R2 Sets Up His Broadcast System ───────────────────────────────────────
    // Every time R2 moves to a new sector, he broadcasts his new position so
    // every action can update its enabled state.  We fire SelectionChangedEvents
    // to every registered listener on each cursor move.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Initializes a selection listener that fires {@link SelectionChangedEvent} to all
     * registered JFace selection listeners when the cursor moves.
     * This is what makes Eclipse actions re-evaluate their enabled state as the user
     * navigates the table with arrow keys.
     */
    private void initSelectionProvider()
    {
        addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                for ( Iterator<?> it = selectionChangesListenerList.iterator(); it.hasNext(); )
                {
                    ( ( ISelectionChangedListener ) it.next() ).selectionChanged( new SelectionChangedEvent(
                        SearchResultEditorCursor.this, getSelection() ) );
                }
            }
        } );
    }


    // ── R2 Takes Control of the Terminal ──────────────────────────────────────
    // R2 asserts focus so keyboard events go to him — standard override to ensure
    // super's setFocus behavior is exposed publicly.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Requests focus for this cursor widget.
     *
     * @return {@code true} if focus was successfully set
     */
    public boolean setFocus()
    {
        return super.setFocus();
    }


    // ── R2 Unplugs From the Terminal ──────────────────────────────────────────
    // Mission complete — R2 disconnects, deregisters from the event bus, and
    // nulls his viewer reference so he doesn't hold up garbage collection.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters from the event registry and disposes this cursor.
     * Always call this before discarding the cursor to avoid listener leaks.
     */
    public void dispose()
    {
        EventRegistry.removeEntryUpdateListener( this );
        viewer = null;
        super.dispose();
    }


    // ── R2 Receives an Entry Update Broadcast ────────────────────────────────
    // The Death Star's computer notifies R2 that some sector data changed —
    // he refreshes the display so the user sees the latest state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called when any LDAP entry is modified.
     * We refresh the table viewer and redraw the cursor so the display stays
     * current with the model.
     *
     * @param event the modification event (not inspected — any change triggers a refresh)
     */
    public void entryUpdated( EntryModificationEvent event )
    {
        viewer.refresh();
        redraw();
    }


    // ── R2 Reports His Current Column Coordinate ─────────────────────────────
    // "I'm in column cn, sector 3" — R2 reads his position and translates the
    // column index back to the attribute description string.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the attribute name (column property) of the column the cursor is in.
     * Returns {@code null} if the cursor is disposed or has no valid position.
     *
     * @return the property string for the current column (e.g. "cn", "mail"), or {@code null}
     */
    public String getSelectedProperty()
    {
        if ( !isDisposed() && getRow() != null && viewer != null && viewer.getColumnProperties() != null
            && viewer.getColumnProperties().length >= getColumn() + 1 )
        {
            String property = ( String ) viewer.getColumnProperties()[getColumn()];
            return property;
        }
        return null;
    }


    // ── R2 Reads the Attribute Cluster at His Position ────────────────────────
    // R2 digs into the current sector's data node and extracts the attribute
    // hierarchy — which may include subtype attributes that group together.
    // If the attribute doesn't exist yet, he synthesizes a placeholder so the
    // editor can still create a new value.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the {@link AttributeHierarchy} for the attribute column the cursor is on.
     * If the entry doesn't have that attribute, we return a synthetic single-attribute
     * hierarchy so value editors can add a new value.
     * Returns {@code null} for the DN column or if there's no valid cursor position.
     *
     * @return the attribute hierarchy at the cursor position, or {@code null}
     */
    public AttributeHierarchy getSelectedAttributeHierarchy()
    {
        if ( !isDisposed() && getRow() != null && viewer != null && viewer.getColumnProperties() != null
            && viewer.getColumnProperties().length >= getColumn() + 1 )
        {
            ISearchResult sr = getSelectedSearchResult();
            String property = ( String ) viewer.getColumnProperties()[getColumn()];
            if ( sr != null && !BrowserUIConstants.DN.equals( property ) )
            {
                AttributeHierarchy ah = sr.getAttributeWithSubtypes( property );

                if ( ah == null )
                {
                    ah = new AttributeHierarchy( sr.getEntry(), property, new IAttribute[]
                        { new Attribute( sr.getEntry(), property ) } );
                }

                return ah;
            }
        }
        return null;
    }


    // ── R2 Retrieves the Working Copy of the Current Sector ──────────────────
    // R2 doesn't hand over the live entry to be edited — he always makes a clone
    // first.  If the cursor is still on the same row as before, he reuses the
    // existing clone; if it moved, he makes a fresh clone of the new row's entry.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the working copy of the {@link ISearchResult} under the cursor.
     * We maintain a reference copy (for computing diffs) and a working copy (for
     * in-place edits).  If the cursor has moved to a different row, we re-clone
     * the new row's entry.  We never return the live entry — always the clone.
     *
     * <p>For example — R2 clones the sector before touching it:</p>
     * <pre>
     *   originalEntry = getRow().getData().getEntry()
     *   referenceCopy = clone(originalEntry)   // for diff computation
     *   workingCopy   = clone(originalEntry)   // for in-place editing
     *   return workingCopy
     * </pre>
     *
     * @return the cloned working-copy {@link ISearchResult}, or {@code null} if no row is selected
     */
    public ISearchResult getSelectedSearchResult()
    {
        if ( !isDisposed() && getRow() != null )
        {
            Object o = getRow().getData();
            if ( o instanceof ISearchResult )
            {
                ISearchResult sr = ( ISearchResult ) o;
                if ( !sr.equals( workingCopy ) )
                {
                    IEntry entry = sr.getEntry();
                    IEntry referenceEntry = new CompoundModification().cloneEntry( entry );
                    referenceCopy = new SearchResult( referenceEntry, sr.getSearch() );
                    IEntry workingEntry = new CompoundModification().cloneEntry( entry );
                    workingCopy = new SearchResult( workingEntry, sr.getSearch() );
                }

                return workingCopy;
            }
        }
        return null;
    }


    // ── R2 Retrieves the Unmodified Reference Copy ────────────────────────────
    // After editing, someone needs to compare "what it was before" vs "what it is now"
    // to compute the LDAP modify diff.  R2 kept a pristine reference copy for this.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the unmodified reference copy of the selected search result.
     * The search result editor uses this to compute the LDAP diff between the
     * reference state and the modified working copy.
     *
     * @return the reference-copy {@link ISearchResult}, or {@code null} if no row has been visited
     */
    public ISearchResult getSelectedReferenceCopy()
    {
        return referenceCopy;
    }


    // ── R2 Discards His Working Copies ────────────────────────────────────────
    // After an edit is committed (or cancelled), R2 throws away his working copies
    // so the next selection starts fresh.  He also notifies all listeners of the
    // reset so actions re-evaluate their enabled state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Clears the working copy and reference copy, then fires a selection-changed event.
     * Call this after a LDAP modify completes to force the cursor to re-clone the
     * entry on the next {@link #getSelectedSearchResult()} call.
     */
    public void resetCopies()
    {
        referenceCopy = null;
        workingCopy = null;

        // update all actions with the fresh selection
        for ( Iterator<?> it = selectionChangesListenerList.iterator(); it.hasNext(); )
        {
            ( ( ISelectionChangedListener ) it.next() ).selectionChanged( new SelectionChangedEvent(
                SearchResultEditorCursor.this, getSelection() ) );
        }
    }


    // ── R2 Registers a New Listener on His Broadcast Channel ─────────────────
    // A new action wants to hear R2's position broadcasts — he adds it to the list.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a JFace selection-changed listener that will receive events when
     * the cursor moves.  Duplicate registrations are silently ignored.
     *
     * @param listener the listener to add; must not be null
     */
    public void addSelectionChangedListener( ISelectionChangedListener listener )
    {
        if ( !selectionChangesListenerList.contains( listener ) )
        {
            selectionChangesListenerList.add( listener );
        }
    }


    // ── R2 Reports His Current Selection to the Workbench ────────────────────
    // The workbench asks "what have you got selected?" — R2 packages up the current
    // search result, attribute hierarchy, and property name into a StructuredSelection
    // so every action can read exactly what's under the cursor.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link StructuredSelection} containing the currently selected
     * search result, attribute hierarchy, and column property — in that order,
     * omitting any that are {@code null}.
     * Actions call {@link org.apache.directory.studio.ldapbrowser.common.actions.BrowserAction#getSelectedSearchResults()}
     * and similar helpers which unwrap this selection.
     *
     * @return a non-null {@link ISelection}; may be empty if nothing is selected
     */
    public ISelection getSelection()
    {
        ISearchResult searchResult = getSelectedSearchResult();
        AttributeHierarchy ah = getSelectedAttributeHierarchy();
        String property = getSelectedProperty();

        List<Object> list = new ArrayList<Object>();
        if ( searchResult != null )
        {
            list.add( searchResult );
        }
        if ( ah != null )
        {
            list.add( ah );
        }
        if ( property != null )
        {
            list.add( property );
        }

        return new StructuredSelection( list );
    }


    // ── R2 Removes a Listener From His Broadcast Channel ─────────────────────
    // An action is being disposed — it deregisters so R2 doesn't waste cycles
    // notifying dead listeners.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Deregisters a previously added selection-changed listener.
     * If the listener was never registered, this call is a no-op.
     *
     * @param listener the listener to remove; must not be null
     */
    public void removeSelectionChangedListener( ISelectionChangedListener listener )
    {
        if ( selectionChangesListenerList.contains( listener ) )
        {
            selectionChangesListenerList.remove( listener );
        }
    }


    // ── R2 Ignores Programmatic Selection Overrides ───────────────────────────
    // The ISelectionProvider interface requires this method, but our selection is
    // always driven by the SWT cursor position — we don't support programmatic setting.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — cursor selection is driven by SWT events, not by programmatic calls.
     * We implement this to satisfy {@link ISelectionProvider} but ignore the argument.
     *
     * @param selection ignored
     */
    public void setSelection( ISelection selection )
    {
    }

}
