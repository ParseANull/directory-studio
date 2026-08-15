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
package org.apache.directory.studio.templateeditor.view;


import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;


// ── CLASS: ColumnsTableViewerComparator — PALPATINE'S ROSTER SORT ORDER ───────────
// When Palpatine reviews his standing orders he wants them sorted — by name, by
// rank, in ascending or descending order. This comparator plays that role for our
// table and tree viewers: it knows which column is the primary sort key, which
// direction to sort in (ascending or reversed), and how to compare two rows by
// reading their column text from the associated label provider.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * JFace {@link ViewerComparator} for table and tree viewers that have named columns.
 * Sorts rows by comparing the text of a configurable primary column using a
 * case-insensitive string comparison. The sort direction can be reversed to toggle
 * between ascending and descending order.
 *
 * <p>Think of this as Palpatine sorting his standing-orders roster:</p>
 * <pre>
 *   comparator.setColumn( 0 );          // sort by the first column
 *   comparator.reverseOrder();          // flip between ascending and descending
 *   viewer.setComparator( comparator ); // apply to the viewer
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ColumnsTableViewerComparator extends ViewerComparator
{
    /** Multiplier for ascending sort order. */
    public static final int ASCENDING = 1;

    /** The associated table label provider */
    protected ITableLabelProvider labelProvider;

    /** The comparison order — {@code +1} for ascending, {@code -1} for descending */
    int order = ASCENDING;

    /** The column used to compare objects */
    int column = 0;


    // ── CONSTRUCTOR: SET UP THE ROSTER COMPARATOR ─────────────────────────────────
    // Palpatine's sort clerk is told which label provider to use for reading column
    // text. The comparator starts in ascending order on column 0.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new comparator backed by the given label provider. Starts in
     * ascending order on column 0.
     *
     * @param labelProvider  the label provider used to read column text for comparison
     */
    public ColumnsTableViewerComparator( ITableLabelProvider labelProvider )
    {
        this.labelProvider = labelProvider;
    }


    // ── COMPARE: RANK TWO ELEMENTS ────────────────────────────────────────────────
    // Palpatine's sort clerk reads the display text for both elements in the primary
    // sort column and compares them case-insensitively, then multiplies by the order
    // so the sign flips when the sort is reversed.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * {@inheritDoc}
     */
    public int compare( Viewer viewer, Object e1, Object e2 )
    {
        String s1 = labelProvider.getColumnText( e1, column );
        String s2 = labelProvider.getColumnText( e2, column );

        return s1.compareToIgnoreCase( s2 ) * order;
    }


    // ── GET COLUMN: WHICH COLUMN IS THE SORT KEY ──────────────────────────────────
    /**
     * Returns the zero-based index of the column currently used as the primary
     * sort key.
     *
     * @return the current sort column index
     */
    public int getColumn()
    {
        return column;
    }


    // ── GET ORDER: ASCENDING OR DESCENDING ────────────────────────────────────────
    /**
     * Returns the current sort order: {@link #ASCENDING} ({@code +1}) or
     * descending ({@code -1}).
     *
     * @return the current sort-order multiplier
     */
    public int getOrder()
    {
        return order;
    }


    // ── REVERSE ORDER: FLIP THE SORT DIRECTION ────────────────────────────────────
    // Palpatine's sort clerk flips the sort direction — ascending becomes descending
    // and vice versa — by negating the order multiplier.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Reverses the sort direction: ascending becomes descending, and descending
     * becomes ascending.
     */
    public void reverseOrder()
    {
        order = order * -1;
    }


    // ── SET COLUMN: CHANGE THE SORT KEY ───────────────────────────────────────────
    /**
     * Sets the zero-based column index to use as the primary sort key.
     *
     * @param column  the column index to sort by
     */
    public void setColumn( int column )
    {
        this.column = column;
    }


    // ── SET ORDER: SET THE SORT DIRECTION ─────────────────────────────────────────
    /**
     * Sets the sort order directly. Use {@link #ASCENDING} ({@code +1}) for
     * ascending or {@code -1} for descending.
     *
     * @param order  the sort-order multiplier
     */
    public void setOrder( int order )
    {
        this.order = order;
    }
}
