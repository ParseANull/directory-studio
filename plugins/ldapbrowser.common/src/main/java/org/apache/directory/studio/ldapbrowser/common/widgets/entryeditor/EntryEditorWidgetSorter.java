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

package org.apache.directory.studio.ldapbrowser.common.widgets.entryeditor;


import java.util.Arrays;
import java.util.Comparator;

import org.apache.directory.studio.ldapbrowser.common.BrowserCommonActivator;
import org.apache.directory.studio.ldapbrowser.common.BrowserCommonConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.utils.AttributeComparator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;


// -- CLASS: EntryEditorWidgetSorter -- LANDO PRIORITIZES CLOUD CITY DOCK ASSIGNMENTS --
// Lando Calrissian runs Cloud City's docking operations from the control room.
// Every ship that requests a berth gets sorted by priority rules: VIP vessels first,
// then cargo freighters, then maintenance craft -- and within each tier, by call sign
// ascending or descending depending on which column header the dispatcher last clicked.
// This class applies exactly that logic to the entry editor table: it sorts LDAP
// attribute rows by type priority (objectClass/must first, operational last) and then
// by attribute name or value, toggling direction on column-header clicks.
// ---------------------------------------------------------------------------------
/**
 * Sorts the rows in the entry editor table according to attribute-type priority rules
 * and a user-controlled sort column and direction.
 * Implements {@link ViewerSorter} to plug into JFace, and {@link SelectionListener}
 * to respond to column-header clicks. Clicking a column header cycles through
 * ascending, descending, and no-sort for that column.
 * Think of this class as Lando in Cloud City's control room: he applies dock-assignment
 * protocols (objectClass first, operational last) and then sorts within tiers by
 * whichever column the dispatcher tapped.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EntryEditorWidgetSorter extends ViewerSorter implements SelectionListener
{

    /** The tree viewer. */
    private TreeViewer viewer;

    /** The sort property. */
    private int sortBy;

    /** The sort order. */
    private int sortOrder;

    /** The preferences. */
    private EntryEditorWidgetPreferences preferences;


    // -- LANDO SETS UP THE DOCK PROTOCOL BOOK -----------------------------------
    // Before any ships arrive, Lando reads the default sorting rules from the
    // preferences book: which column drives the default order and which direction.
    // We start with SORT_BY_NONE and SORT_ORDER_NONE so the column-header click
    // listener can establish the first active sort on demand.
    // ---------------------------------------------------------------------------------
    /**
     * Creates a new sorter that starts in "no sort" state and reads default rules from preferences.
     * The active sort is {@code SORT_BY_NONE} / {@code SORT_ORDER_NONE} until the user clicks
     * a column header. Preferences supply the fallback for when no active sort is in effect.
     *
     * <p>For example -- Lando opens the dock protocol book:</p>
     * <pre>
     *   Lando: "No active sort yet. I'll use the preference defaults until
     *            a dispatcher clicks a column header."
     * </pre>
     *
     * @param preferences  the preference wrapper that supplies default sort-by and sort-order values
     */
    public EntryEditorWidgetSorter( EntryEditorWidgetPreferences preferences )
    {
        this.sortBy = BrowserCoreConstants.SORT_BY_NONE;
        this.sortOrder = BrowserCoreConstants.SORT_ORDER_NONE;
        this.preferences = preferences;
    }


    // -- LANDO PLUGS INTO THE DOCK ROSTER TERMINAL -----------------------------
    // Lando connects to the Cloud City dock roster display (the viewer) and
    // registers himself as the sorter. He also hooks into every column header
    // so a tap on "Ship Name" or "Class" triggers his sorting logic.
    // ---------------------------------------------------------------------------------
    /**
     * Wires this sorter to the given {@link TreeViewer} and registers a click listener
     * on each column header. After this call, clicking a column header will cycle the sort.
     *
     * <p>For example -- Lando connects to the roster terminal:</p>
     * <pre>
     *   Lando plugs into the dock display. He registers as the active sorter.
     *   He adds a listener to each column header: "Attribute Description" and "Value".
     *   Now a tap on either header triggers his dock-assignment protocol.
     * </pre>
     *
     * @param viewer  the entry editor tree viewer to sort
     */
    public void connect( TreeViewer viewer )
    {
        this.viewer = viewer;
        viewer.setSorter( this );

        for ( TreeColumn column : ( ( TreeViewer ) viewer ).getTree().getColumns() )
        {
            column.addSelectionListener( this );
        }
    }


    // -- LANDO CLOSES THE DOCK FOR THE NIGHT -----------------------------------
    // The shift is over. Lando nulls his references to the viewer and preferences
    // so GC can reclaim the memory. No more sorting after this.
    // ---------------------------------------------------------------------------------
    /**
     * Releases references to the viewer and preferences when the entry editor is torn down.
     * After disposal, this sorter is inert.
     *
     * <p>For example -- Lando signs off the dock terminal:</p>
     * <pre>
     *   Lando: "That's the last ship for tonight. Closing dock control."
     *   He nulls the references. The terminal goes dark.
     * </pre>
     */
    public void dispose()
    {
        viewer = null;
        preferences = null;
    }


    // -- LANDO IGNORES THE DEFAULT DOUBLE-TAP SIGNAL ----------------------------
    // Cloud City's protocol doesn't do anything special on a double-tap of a
    // column header -- that's just someone being indecisive. We satisfy the
    // SelectionListener contract with an empty body.
    // ---------------------------------------------------------------------------------
    /**
     * No-op implementation required by {@link SelectionListener}.
     * We don't respond to default-selection events (double-clicking a column header).
     *
     * <p>For example -- Lando ignores the double-tap:</p>
     * <pre>
     *   Dispatcher double-taps "Attribute Description". Lando shrugs.
     *   "One tap is enough. Try again."
     * </pre>
     *
     * @param e  the selection event (ignored)
     */
    public void widgetDefaultSelected( SelectionEvent e )
    {
    }


    // -- LANDO CYCLES THE DOCK SORT ORDER ON A COLUMN TAP ----------------------
    // A dispatcher taps a column header on the roster display. If it's the same
    // column that's already active, Lando toggles the direction; if it's a
    // different column, he switches to that column at ascending order. He also
    // updates the sort-direction arrow icon on the tapped column and refreshes
    // the full roster display.
    // ---------------------------------------------------------------------------------
    /**
     * Responds to a column header click by updating the sort column and direction,
     * refreshing the sort-indicator icon on the column, and triggering a viewer refresh.
     * Clicking the same column cycles: ascending -> descending -> none.
     * Clicking a different column switches to that column at ascending.
     *
     * <p>For example -- dispatcher taps "Attribute Description":</p>
     * <pre>
     *   First tap  -> sort by attribute, ascending (A-Z arrow appears)
     *   Second tap -> sort by attribute, descending (Z-A arrow appears)
     *   Third tap  -> no sort (arrow disappears)
     *   Tap "Value" -> switch to value sort, ascending
     * </pre>
     *
     * @param event  the column-header selection event carrying the clicked column widget
     */
    public void widgetSelected( SelectionEvent event )
    {
        if ( ( event.widget instanceof TreeColumn ) && ( viewer != null ) )
        {
            Tree tree = viewer.getTree();
            TreeColumn treeColumn = ( ( TreeColumn ) event.widget );

            int index = tree.indexOf( treeColumn );

            switch ( index )
            {
                case EntryEditorWidgetTableMetadata.KEY_COLUMN_INDEX:
                    if ( sortBy == BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION )
                    {
                        toggleSortOrder();
                    }
                    else
                    {
                        // set new sort by
                        sortBy = BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION;
                        sortOrder = BrowserCoreConstants.SORT_ORDER_ASCENDING;
                    }

                    break;

                case EntryEditorWidgetTableMetadata.VALUE_COLUMN_INDEX:
                    if ( sortBy == BrowserCoreConstants.SORT_BY_VALUE )
                    {
                        toggleSortOrder();
                    }
                    else
                    {
                        // set new sort by
                        sortBy = BrowserCoreConstants.SORT_BY_VALUE;
                        sortOrder = BrowserCoreConstants.SORT_ORDER_ASCENDING;
                    }

                    break;

                default:;
            }

            if ( sortOrder == BrowserCoreConstants.SORT_ORDER_NONE )
            {
                sortBy = BrowserCoreConstants.SORT_BY_NONE;
            }

            for ( TreeColumn column : tree.getColumns() )
            {
                column.setImage( null );
            }

            if ( sortOrder == BrowserCoreConstants.SORT_ORDER_ASCENDING )
            {
                treeColumn.setImage( BrowserCommonActivator.getDefault().getImage(
                    BrowserCommonConstants.IMG_SORT_ASCENDING ) );
            }
            else if ( sortOrder == BrowserCoreConstants.SORT_ORDER_DESCENDING )
            {
                treeColumn.setImage( BrowserCommonActivator.getDefault().getImage(
                    BrowserCommonConstants.IMG_SORT_DESCENDING ) );
            }

            viewer.refresh();
        }
    }


    // -- LANDO ROTATES THROUGH THE THREE DIRECTION STATES ----------------------
    // After a second tap on the active column, Lando rotates the sort direction:
    // none -> ascending -> descending -> none.  This is what makes a third tap
    // on the same column clear the sort entirely.
    // ---------------------------------------------------------------------------------
    /**
     * Rotates the sort order through the three states: none -> ascending -> descending -> none.
     * Called by {@link #widgetSelected} when the user clicks the already-active column.
     *
     * <p>For example -- Lando cycles the direction wheel:</p>
     * <pre>
     *   Current: NONE       -> becomes ASCENDING  (first tap on column)
     *   Current: ASCENDING  -> becomes DESCENDING (second tap)
     *   Current: DESCENDING -> becomes NONE       (third tap, clears sort)
     * </pre>
     */
    private void toggleSortOrder()
    {
        switch ( sortOrder )
        {
            case BrowserCoreConstants.SORT_ORDER_NONE:
                sortOrder = BrowserCoreConstants.SORT_ORDER_ASCENDING;
                break;

            case BrowserCoreConstants.SORT_ORDER_ASCENDING:
                sortOrder = BrowserCoreConstants.SORT_ORDER_DESCENDING;
                break;

            case BrowserCoreConstants.SORT_ORDER_DESCENDING:
                sortOrder = BrowserCoreConstants.SORT_ORDER_NONE;
                break;
        }
    }


    // -- LANDO SORTS THE ENTIRE DOCK ROSTER ------------------------------------
    // JFace hands Lando the full list of elements and says "sort these."
    // Lando delegates to compare() for the actual pairwise ordering logic,
    // wrapping it in a Java Comparator so Arrays.sort() can do the heavy lifting.
    // ---------------------------------------------------------------------------------
    /**
     * Sorts the given array of elements in-place using our pairwise {@link #compare} logic.
     * JFace calls this instead of calling compare repeatedly itself -- we just wrap
     * compare in a {@link Comparator} and delegate to {@link Arrays#sort}.
     *
     * <p>For example -- Lando runs the full dock sort:</p>
     * <pre>
     *   Lando receives the full roster: [cn, mail, objectClass, createTimestamp]
     *   He runs the comparator over every pair and rearranges in place:
     *   Result: [objectClass, cn, mail, createTimestamp]  (must-first, operational-last)
     * </pre>
     *
     * @param viewer    the viewer requesting the sort (passed through to compare)
     * @param elements  the array of row elements to sort in-place
     */
    public void sort( final Viewer viewer, Object[] elements )
    {
        Arrays.sort( elements, new Comparator<Object>()
        {
            public int compare( Object a, Object b )
            {
                return EntryEditorWidgetSorter.this.compare( viewer, a, b );
            }
        } );

    }


    // -- LANDO COMPARES TWO DOCK ASSIGNMENTS ------------------------------------
    // Given two attribute rows, Lando delegates to AttributeComparator, passing
    // it the active sort settings (sort-by column, sort direction) plus the
    // priority rules (objectClass/must first? operational last?). The comparator
    // returns -1, 0, or +1 and Lando's roster falls into order.
    // ---------------------------------------------------------------------------------
    /**
     * Compares two elements to determine their relative sort order.
     * Delegates to {@link AttributeComparator} with the current sort-by column,
     * sort direction, and priority-grouping flags read from preferences.
     *
     * <p>For example -- Lando compares two dock requests:</p>
     * <pre>
     *   Element A: cn=John (must attribute)
     *   Element B: mail=john@co.com (may attribute)
     *   objectClassAndMustAttributesFirst=true -> cn sorts before mail
     *   Result: -1 (A comes first)
     * </pre>
     *
     * @param viewer  the viewer requesting the comparison (passed to AttributeComparator)
     * @param o1      the first element to compare
     * @param o2      the second element to compare
     * @return        negative if o1 sorts first, positive if o2 sorts first, zero if equal
     */
    public int compare( Viewer viewer, Object o1, Object o2 )
    {
        boolean objectClassAndMustAttributesFirst = preferences == null
            || preferences.isObjectClassAndMustAttributesFirst();
        boolean operationalAttributesLast = preferences == null || preferences.isOperationalAttributesLast();
        AttributeComparator comparator = new AttributeComparator( sortBy, getDefaultSortBy(), sortOrder,
            getDefaultSortOrder(), objectClassAndMustAttributesFirst, operationalAttributesLast );
        return comparator.compare( o1, o2 );
    }


    // -- LANDO READS THE DEFAULT DIRECTION FROM THE PROTOCOL BOOK ---------------
    // When no active sort is set, Lando falls back to the preference-configured
    // default direction. If preferences are null (test scenario), he defaults
    // to ascending so something sensible happens.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the fallback sort order when no active sort has been set via column-header click.
     * Reads from preferences; defaults to {@code SORT_ORDER_ASCENDING} if preferences are null.
     *
     * <p>For example -- Lando reads the default direction from the dock protocol book:</p>
     * <pre>
     *   Preferences say: ASCENDING. No active sort -> use ascending as the baseline.
     * </pre>
     *
     * @return  the default sort-order constant (NONE, ASCENDING, or DESCENDING)
     */
    private int getDefaultSortOrder()
    {
        if ( preferences == null )
        {
            return BrowserCoreConstants.SORT_ORDER_ASCENDING;
        }
        else
        {
            return preferences.getDefaultSortOrder();
        }
    }


    // -- LANDO READS THE DEFAULT COLUMN FROM THE PROTOCOL BOOK -----------------
    // When no active sort column is set, Lando checks which column the protocol
    // book says to use as the default. If preferences are null, he defaults to
    // sorting by attribute description so the table at least has some order.
    // ---------------------------------------------------------------------------------
    /**
     * Returns the fallback sort-by column when no active sort has been set via column-header click.
     * Reads from preferences; defaults to {@code SORT_BY_ATTRIBUTE_DESCRIPTION} if preferences are null.
     *
     * <p>For example -- Lando reads the default column from the protocol book:</p>
     * <pre>
     *   Preferences say: SORT_BY_ATTRIBUTE_DESCRIPTION.
     *   No active sort -> default to attribute-name order.
     * </pre>
     *
     * @return  the default sort-by constant (NONE, ATTRIBUTE_DESCRIPTION, or VALUE)
     */
    private int getDefaultSortBy()
    {
        if ( preferences == null )
        {
            return BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION;
        }
        else
        {
            return preferences.getDefaultSortBy();
        }
    }

}
