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
package org.apache.directory.studio.templateeditor.view.preferences;


import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;

import org.apache.directory.studio.templateeditor.view.ColumnsTableViewerComparator;


// ── CLASS: ColumnViewerSortColumnUtils — PALPATINE INSTALLING SORT-ORDER TRIGGERS ─
// When Palpatine reviews his standing-orders roster, he wants to sort it by clicking
// any column header. This utility class wires up the click listeners on table and
// tree column headers. When a header is clicked, it either reverses the current sort
// (if that column is already primary) or switches to the new column in ascending
// order. The sort-direction arrow indicator in the column header is then updated.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * Static utility that adds clickable sort behaviour to columns in
 * {@link TableViewer} and {@link TreeViewer} widgets. Clicking a column header
 * toggles the sort direction if that column is already the primary sort key,
 * or makes it the new primary sort key. The sort-direction arrow on the column
 * header is updated asynchronously to keep the UI responsive.
 *
 * <p>Think of this as Palpatine's roster-sort installer:</p>
 * <pre>
 *   ColumnViewerSortColumnUtils.addSortColumn( tableViewer, titleColumn );
 *   // Now clicking the "Title" column header sorts the table by that column.
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ColumnViewerSortColumnUtils
{
    // ── ADD SORT COLUMN (TABLE): WIRE UP A TABLE COLUMN HEADER ───────────────────
    // Palpatine installs a click listener on the given table column so that
    // clicking the header triggers a resort of the table.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a click listener on the given {@code TableColumn} so that clicking
     * its header re-sorts the {@code TableViewer}. The column's zero-based index
     * is determined automatically by scanning the table's column list.
     *
     * @param tableViewer  the table viewer whose comparator will be invoked
     * @param tableColumn  the column to make sortable; does nothing if {@code null}
     */
    public static void addSortColumn( TableViewer tableViewer, TableColumn tableColumn )
    {
        if ( tableColumn == null )
        {
            return;
        }

        Table table = tableViewer.getTable();
        if ( table == null )
        {
            return;
        }

        // Looking for the column index of the table column
        for ( int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++ )
        {
            if ( tableColumn.equals( table.getColumn( columnIndex ) ) )
            {
                tableColumn.addSelectionListener( getHeaderListener( tableViewer, columnIndex ) );
            }
        }
    }


    // ── GET HEADER LISTENER (TABLE): BUILD THE CLICK HANDLER ──────────────────────
    // Palpatine's click-handler factory — builds a SelectionAdapter that calls
    // resortTable() when the column header is clicked.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a {@link SelectionListener} that calls {@link #resortTable} when the
     * column header is clicked.
     *
     * @param tableViewer  the table viewer to resort
     * @param columnIndex  the zero-based index of the column being clicked
     * @return the click listener
     */
    private static SelectionListener getHeaderListener( final TableViewer tableViewer, final int columnIndex )
    {
        return new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( tableViewer == null )
                {
                    return;
                }

                TableColumn column = ( TableColumn ) e.widget;
                resortTable( tableViewer, column, columnIndex );
            }
        };
    }


    // ── RESORT TABLE: APPLY THE NEW SORT ORDER ────────────────────────────────────
    // Palpatine's sort clerk checks whether the clicked column is already the
    // primary key (in which case the order is reversed) or a new key (in which
    // case the column switches and the order stays ascending). The table is then
    // refreshed and the direction indicator updated on the UI thread.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Applies a new sort order to the table viewer. If {@code columnIndex} is the
     * current primary sort column, the order is reversed; otherwise the primary
     * column is changed. The table is refreshed and the sort-direction arrow updated
     * asynchronously via {@link PlatformUI#getWorkbench()}.
     *
     * @param tableViewer  the viewer to resort
     * @param tableColumn  the column whose header was clicked
     * @param columnIndex  the zero-based column index
     */
    protected static void resortTable( final TableViewer tableViewer, final TableColumn tableColumn, int columnIndex )
    {
        // Getting the sorter
        ColumnsTableViewerComparator sorter = ( ColumnsTableViewerComparator ) tableViewer.getComparator();

        // Checking if sorting needs to be reversed or set to another columns
        if ( columnIndex == sorter.getColumn() )
        {
            sorter.reverseOrder();
        }
        else
        {
            sorter.setColumn( columnIndex );
        }

        // Refreshing the table and updating the direction indicator asynchronously
        PlatformUI.getWorkbench().getDisplay().asyncExec( new Runnable()
        {
            public void run()
            {
                tableViewer.refresh();
                updateDirectionIndicator( tableViewer, tableColumn );
            }
        } );
    }


    // ── UPDATE DIRECTION INDICATOR (TABLE): SHOW THE SORT ARROW ──────────────────
    // Palpatine's sort arrow is updated to show which column is primary and whether
    // the sort is ascending (up arrow) or descending (down arrow).
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Updates the sort-direction arrow indicator on the table to reflect the
     * current primary sort column and direction.
     *
     * @param tableViewer  the viewer whose table header arrow should be updated
     * @param tableColumn  the column to highlight as the primary sort key
     */
    protected static void updateDirectionIndicator( TableViewer tableViewer, TableColumn tableColumn )
    {
        tableViewer.getTable().setSortColumn( tableColumn );
        if ( ( ( ColumnsTableViewerComparator ) tableViewer.getComparator() ).getOrder() == ColumnsTableViewerComparator.ASCENDING )
        {
            tableViewer.getTable().setSortDirection( SWT.UP );
        }
        else
        {
            tableViewer.getTable().setSortDirection( SWT.DOWN );
        }
    }


    // ── ADD SORT COLUMN (TREE): WIRE UP A TREE COLUMN HEADER ─────────────────────
    // Palpatine installs a click listener on the given tree column so that clicking
    // the header triggers a resort of the tree.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Registers a click listener on the given {@code TreeColumn} so that clicking
     * its header re-sorts the {@code TreeViewer}. The column's zero-based index
     * is determined automatically by scanning the tree's column list.
     *
     * @param treeViewer  the tree viewer whose comparator will be invoked
     * @param treeColumn  the column to make sortable; does nothing if {@code null}
     */
    public static void addSortColumn( TreeViewer treeViewer, TreeColumn treeColumn )
    {
        if ( treeColumn == null )
        {
            return;
        }

        Tree tree = treeViewer.getTree();
        if ( tree == null )
        {
            return;
        }

        // Looking for the column index of the ttreeable column
        for ( int columnIndex = 0; columnIndex < tree.getColumnCount(); columnIndex++ )
        {
            if ( treeColumn.equals( tree.getColumn( columnIndex ) ) )
            {
                treeColumn.addSelectionListener( getHeaderListener( treeViewer, columnIndex ) );
            }
        }
    }


    // ── GET HEADER LISTENER (TREE): BUILD THE CLICK HANDLER ───────────────────────
    /**
     * Returns a {@link SelectionListener} that calls {@link #resortTree} when the
     * tree column header is clicked.
     *
     * @param treeViewer   the tree viewer to resort
     * @param columnIndex  the zero-based index of the column being clicked
     * @return the click listener
     */
    private static SelectionListener getHeaderListener( final TreeViewer treeViewer, final int columnIndex )
    {
        return new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                if ( treeViewer == null )
                {
                    return;
                }

                TreeColumn column = ( TreeColumn ) e.widget;
                resortTree( treeViewer, column, columnIndex );
            }
        };
    }


    // ── RESORT TREE: APPLY THE NEW SORT ORDER ─────────────────────────────────────
    /**
     * Applies a new sort order to the tree viewer. If {@code columnIndex} is the
     * current primary sort column, the order is reversed; otherwise the primary
     * column is changed. The tree is refreshed and the sort-direction arrow updated
     * asynchronously.
     *
     * @param treeViewer  the viewer to resort
     * @param treeColumn  the column whose header was clicked
     * @param columnIndex the zero-based column index
     */
    protected static void resortTree( final TreeViewer treeViewer, final TreeColumn treeColumn, int columnIndex )
    {
        // Getting the sorter
        ColumnsTableViewerComparator sorter = ( ColumnsTableViewerComparator ) treeViewer.getComparator();

        // Checking if sorting needs to be reversed or set to another columns
        if ( columnIndex == sorter.getColumn() )
        {
            sorter.reverseOrder();
        }
        else
        {
            sorter.setColumn( columnIndex );
        }

        // Refreshing the tree and updating the direction indicator asynchronously
        PlatformUI.getWorkbench().getDisplay().asyncExec( new Runnable()
        {
            public void run()
            {
                treeViewer.refresh();
                updateDirectionIndicator( treeViewer, treeColumn );
            }
        } );
    }


    // ── UPDATE DIRECTION INDICATOR (TREE): SHOW THE SORT ARROW ───────────────────
    /**
     * Updates the sort-direction arrow indicator on the tree to reflect the
     * current primary sort column and direction.
     *
     * @param treeViewer  the viewer whose tree header arrow should be updated
     * @param treeColumn  the column to highlight as the primary sort key
     */
    protected static void updateDirectionIndicator( TreeViewer treeViewer, TreeColumn treeColumn )
    {
        treeViewer.getTree().setSortColumn( treeColumn );
        if ( ( ( ColumnsTableViewerComparator ) treeViewer.getComparator() ).getColumn() == ColumnsTableViewerComparator.ASCENDING )
        {
            treeViewer.getTree().setSortDirection( SWT.UP );
        }
        else
        {
            treeViewer.getTree().setSortDirection( SWT.DOWN );
        }
    }
}
