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


import java.util.Arrays;
import java.util.Comparator;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableColumn;


// ── CLASS: SearchResultEditorSorter — Lando Running Cloud City ───────────────
// Lando Calrissian runs Cloud City and keeps everything in order — tibanna gas
// miners up top, processing decks below, VIP guests in the right suites.
// He doesn't just sort randomly; he watches which column header the user clicks
// and reorganises the rows accordingly.  Click the same column again? Reverse
// the order.  Click a third time?  No sort.  That's the three-click toggle.
// This sorter is Lando: it listens to column header clicks, cycles through
// ascending → descending → none, and re-sorts the table each time.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link ViewerSorter} for the search result editor that sorts by a clicked column.
 * We register as a {@link SelectionListener} on every table column header.  When
 * the user clicks a column header we cycle through ascending → descending → none
 * and trigger a content provider refresh.  The compare method extracts the first
 * string value from the relevant attribute and does a case-insensitive comparison.
 * Think of Lando managing Cloud City: everything has its place, and he reorganises
 * on demand.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEditorSorter extends ViewerSorter implements SelectionListener
{

    /** The content provider. */
    protected SearchResultEditorContentProvider contentProvider;

    /** The search. */
    private ISearch search;

    /** The columns. */
    private TableColumn[] columns;

    /** The show Dn flag. */
    private boolean showDn;

    /** The sort property. */
    private int sortBy;

    /** The sort order. */
    private int sortOrder;


    // ── Lando Wires Up the Column Listeners ───────────────────────────────────
    // Lando takes up his post as administrator: he registers with every column
    // header so he hears about clicks.  Initial state is "no sort" — everything
    // in the order the content provider returned it.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Connects this sorter to the content provider and registers as a selection
     * listener on all current table columns.
     * Starts with no sort active ({@link BrowserCoreConstants#SORT_ORDER_NONE}).
     *
     * @param contentProvider the content provider whose viewer we'll sort
     */
    public void connect( SearchResultEditorContentProvider contentProvider )
    {
        this.contentProvider = contentProvider;

        sortBy = 0;
        sortOrder = BrowserCoreConstants.SORT_ORDER_NONE;

        columns = contentProvider.getViewer().getTable().getColumns();

        for ( TableColumn column : columns )
        {
            column.addSelectionListener( this );
        }
    }


    // ── Lando Reorganises for a New Manifest ──────────────────────────────────
    // A different search is loaded — Lando re-wires the column listeners to the
    // new set of columns.  If the current sort column no longer exists in the
    // new column set, he resets to no sort.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called when the editor's input changes to a new search.
     * Re-registers column click listeners and validates the current sort column
     * against the new search's returning attributes.  If the sort column is out
     * of bounds for the new search, we reset to no sort by cycling {@link #setSortColumn}
     * three times (ascending → descending → none).
     *
     * @param newSearch the newly active search
     * @param showDn    whether the DN column is currently visible
     */
    public void inputChanged( ISearch newSearch, boolean showDn )
    {
        this.search = newSearch;
        this.showDn = showDn;

        if ( columns != null )
        {
            for ( TableColumn column : columns )
            {
                column.removeSelectionListener( this );
            }
        }

        columns = contentProvider.getViewer().getTable().getColumns();

        for ( TableColumn column : columns )
        {
            column.addSelectionListener( this );
        }

        // check sort column
        int visibleColumns = search.getReturningAttributes().length;

        if ( showDn )
        {
            visibleColumns++;
        }

        if ( visibleColumns < sortBy + 1 )
        {
            setSortColumn( 0 );
            setSortColumn( 0 );
            setSortColumn( 0 );
        }
    }


    // ── Lando Steps Down ─────────────────────────────────────────────────────
    // Cloud City is closing — Lando removes all column listeners and clears
    // his internal state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Removes column selection listeners and clears all internal references.
     * Call this when the editor disposes.
     */
    public void dispose()
    {
        if ( columns != null )
        {
            for ( TableColumn column : columns )
            {
                if ( !column.isDisposed() )
                {
                    column.removeSelectionListener( this );
                }
            }
        }

        columns = null;
        search = null;
        contentProvider = null;
    }


    // ── Lando Ignores Double-Clicks on Column Headers ─────────────────────────
    // Default selection (double-click) on a column header does nothing here —
    // Lando only responds to single clicks.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * No-op — we only respond to single-click column header selection.
     *
     * @param e the selection event
     */
    public void widgetDefaultSelected( SelectionEvent e )
    {
    }


    // ── Lando Gets a Column Click ─────────────────────────────────────────────
    // A column header was clicked — Lando looks up which column it is and
    // delegates to setSortColumn to cycle the sort state.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Called when a table column header is clicked.
     * We find the column's index and pass it to {@link #setSortColumn(int)}.
     *
     * @param e the selection event; expected to originate from a {@link TableColumn}
     */
    public void widgetSelected( SelectionEvent e )
    {
        if ( e.widget instanceof TableColumn )
        {
            int index = contentProvider.getViewer().getTable().indexOf( ( ( TableColumn ) e.widget ) );
            setSortColumn( index );
        }
    }


    // ── Lando Cycles the Sort Order ───────────────────────────────────────────
    // Same column clicked again? Toggle the order (asc → desc → none).
    // Different column clicked? Switch to that column with ascending order.
    // Either way, update the column header arrow icon and trigger a refresh.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the sort column and order, then refreshes the table.
     * Clicking the current sort column cycles ascending → descending → none.
     * Clicking a different column sets that column ascending.
     * The column header icon (up/down arrow) is updated to reflect the new state.
     *
     * @param index the zero-based index of the clicked column
     */
    private void setSortColumn( int index )
    {
        if ( sortBy == index )
        {
            // toggle sort order
            sortOrder = sortOrder == BrowserCoreConstants.SORT_ORDER_ASCENDING ? BrowserCoreConstants.SORT_ORDER_DESCENDING
                : sortOrder == BrowserCoreConstants.SORT_ORDER_DESCENDING ? BrowserCoreConstants.SORT_ORDER_NONE
                    : BrowserCoreConstants.SORT_ORDER_ASCENDING;
        }
        else
        {
            // set new sort by
            sortBy = index;
            sortOrder = BrowserCoreConstants.SORT_ORDER_ASCENDING;
        }

        if ( sortOrder == BrowserCoreConstants.SORT_ORDER_NONE )
        {
            sortBy = BrowserCoreConstants.SORT_BY_NONE;
        }

        TableColumn[] columns = contentProvider.getViewer().getTable().getColumns();

        for ( TableColumn column : columns )
        {
            column.setImage( null );
        }

        if ( sortOrder == BrowserCoreConstants.SORT_ORDER_ASCENDING )
        {
            ( columns[index] ).setImage( BrowserCommonActivator.getDefault().getImage(
                BrowserCommonConstants.IMG_SORT_ASCENDING ) );
        }
        else if ( sortOrder == BrowserCoreConstants.SORT_ORDER_DESCENDING )
        {
            ( columns[index] ).setImage( BrowserCommonActivator.getDefault().getImage(
                BrowserCommonConstants.IMG_SORT_DESCENDING ) );
        }
        else
        {
            ( columns[index] ).setImage( null );
        }

        contentProvider.refresh();
    }


    // ── Lando Always Has an Opinion ───────────────────────────────────────────
    // isSorted() always returns true — we always run through the sort() path so
    // that our comparator gets applied (even with SORT_ORDER_NONE, which just
    // produces stable equal-comparisons for all pairs).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code true} always — we always run through the sort path.
     * With {@link BrowserCoreConstants#SORT_ORDER_NONE} the compare method
     * returns 0 for every pair, preserving the original order.
     *
     * @return {@code true}
     */
    public boolean isSorted()
    {
        // return sortOrder != SORT_ORDER_NONE;
        return true;
    }


    // ── Lando Lines Everybody Up ──────────────────────────────────────────────
    // The viewer calls sort() to re-arrange all the rows.  If we're sorted
    // (which is always), we delegate to Arrays.sort using our compare method.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Sorts the element array in place using {@link #compare(Viewer, Object, Object)}.
     * We always sort — even with SORT_ORDER_NONE — but compare() returns 0 in that
     * case so the relative order is unchanged.
     *
     * @param viewer   the table viewer (passed through to compare)
     * @param elements the array of elements to sort in place
     */
    public void sort( final Viewer viewer, Object[] elements )
    {
        if ( isSorted() )
        {
            Arrays.sort( elements, new Comparator<Object>()
            {
                public int compare( Object a, Object b )
                {
                    return SearchResultEditorSorter.this.compare( viewer, a, b );
                }
            } );
        }

    }


    // ── Lando Compares Two Residents ─────────────────────────────────────────
    // Given two search result rows, Lando looks up the value of the sort column
    // for each and compares them lexicographically (respecting sort direction).
    // Null entries go to the front (ascending) or back (descending).
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link ISearchResult} elements for sorting.
     * We extract the string value of the sort column from each result's entry
     * and compare case-insensitively.  Null entries are treated as "less than"
     * non-null entries.  For the DN column we compare normalized DN names.
     *
     * @param viewer the table viewer (not used directly)
     * @param o1     the first element
     * @param o2     the second element
     * @return negative if o1 &lt; o2, zero if equal, positive if o1 &gt; o2 (adjusted for sort direction)
     */
    public int compare( Viewer viewer, Object o1, Object o2 )
    {
        if ( search == null )
        {
            return equal();
        }

        ISearchResult sr1 = ( ISearchResult ) o1;
        ISearchResult sr2 = ( ISearchResult ) o2;

        IEntry entry1 = sr1.getEntry();
        IEntry entry2 = sr2.getEntry();

        if ( entry1 == null )
        {
            if ( entry2 == null )
            {
                return equal();
            }
            else
            {
                return lessThan();
            }
        }
        else if ( entry2 == null )
        {
            return greaterThan();
        }
        else
        {
            String attributeName;

            if ( showDn && ( sortBy == 0 ) )
            {
                attributeName = BrowserUIConstants.DN;
            }
            else if ( showDn && ( sortBy > 0 ) )
            {
                attributeName = search.getReturningAttributes()[sortBy - 1];
            }
            else
            {
                attributeName = search.getReturningAttributes()[sortBy];
            }

            if ( attributeName == BrowserUIConstants.DN )
            {
                // compare normalized names
                return compare( entry1.getDn().getNormName(), entry2.getDn().getNormName() );
            }
            else
            {
                AttributeHierarchy ah1 = entry1.getAttributeWithSubtypes( attributeName );
                AttributeHierarchy ah2 = entry2.getAttributeWithSubtypes( attributeName );

                if ( ah1 == null )
                {
                    if ( ah2 == null )
                    {
                        return equal();
                    }
                    else
                    {
                        return lessThan();
                    }
                }
                else if ( ah2 == null )
                {
                    return greaterThan();
                }
                else
                {
                    IAttribute attribute1 = ah1.getAttribute();
                    IAttribute attribute2 = ah2.getAttribute();

                    String value1 = getValue( attribute1 );
                    String value2 = getValue( attribute2 );
                    return compare( value1, value2 );
                }
            }
        }
    }


    // ── Lando Picks the First Item in the Suite ───────────────────────────────
    // Returns the first string value from an attribute, or empty string if there's
    // nothing there.  Binary values are coerced to empty string for sorting purposes.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the first string value of the given attribute for sort comparison.
     * Returns empty string if the attribute has no values.
     *
     * @param attribute the attribute to read
     * @return the first string value, or {@code ""}
     */
    private String getValue( IAttribute attribute )
    {
        if ( attribute.getValueSize() > 0 )
        {
            return attribute.getStringValue();
        }
        else
        {
            return ""; //$NON-NLS-1$
        }
    }


    // ── Lando Says "First in Line" ────────────────────────────────────────────
    // Returns the "less than" ordering value, adjusted for ascending vs descending.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the comparator value representing "o1 is less than o2",
     * accounting for the current sort direction.
     *
     * @return -1 for ascending, +1 for descending
     */
    private int lessThan()
    {
        return sortOrder == BrowserCoreConstants.SORT_ORDER_ASCENDING ? -1 : 1;
    }


    // ── Lando Says "They're the Same" ─────────────────────────────────────────
    // Returns 0 — both elements are equal from the sort's perspective.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns 0 — the two elements are considered equal for sorting purposes.
     *
     * @return 0
     */
    private int equal()
    {
        return 0;
    }


    // ── Lando Says "This One Goes First" ─────────────────────────────────────
    // Returns the "greater than" ordering value, adjusted for sort direction.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the comparator value representing "o1 is greater than o2",
     * accounting for the current sort direction.
     *
     * @return +1 for ascending, -1 for descending
     */
    private int greaterThan()
    {
        return sortOrder == BrowserCoreConstants.SORT_ORDER_ASCENDING ? 1 : -1;
    }


    // ── Lando Alphabetizes the Guest List ────────────────────────────────────
    // The actual string comparison, flipped if we're in descending order.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two strings case-insensitively, reversing the result for descending sort.
     *
     * @param s1 the first string
     * @param s2 the second string
     * @return negative, zero, or positive per sort direction
     */
    private int compare( String s1, String s2 )
    {
        return sortOrder == BrowserCoreConstants.SORT_ORDER_ASCENDING ? s1.compareToIgnoreCase( s2 ) : s2
            .compareToIgnoreCase( s1 );
    }

}
