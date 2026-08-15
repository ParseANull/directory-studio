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

package org.apache.directory.studio.ldapbrowser.common.widgets.browser;


import java.util.Arrays;

import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


// ── CLASS: BrowserEntryPage — LANDO'S CLOUD CITY NUMBERED FILING BINS ─────────
// Cloud City has thousands of docking records. Lando's filing system groups
// them into numbered bins: "[1...50]", "[51...100]", "[101...150]" — each bin
// is a manageable chunk. Nested bins are used for really huge sections.
// When you open a bin, you either get the actual docking records (leaf page)
// or more numbered sub-bins (non-leaf page).
// BrowserEntryPage is exactly that numbered bin: it holds either a slice of
// child entries or a set of nested sub-pages when folding (pagination) is on
// and a parent entry has more children than the folding threshold.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A paginated "folder" node that appears in the browser tree when an entry
 * has more children than the configured folding threshold.
 * Instead of showing hundreds of entries directly under a parent, we group
 * them into BrowserEntryPage nodes labelled "[1...50]", "[51...100]", etc.
 * Pages can be nested (a page of sub-pages) for very large sets.
 * Think of this class as one of Lando's numbered filing bins in Cloud City —
 * open it and you either get a set of records or more bins.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserEntryPage
{
    /** The tree sorter */
    private BrowserSorter sorter;

    /** The index of the first child entry in this page */
    private int first;

    /** The index of the last child entry in this page */
    private int last;

    /** The parent entry */
    private IEntry entry;

    /** The parent entry page or null if not nested */
    private BrowserEntryPage parentEntryPage;

    /** The sub pages */
    private BrowserEntryPage[] subpages;


    // ── LANDO CREATES A NEW NUMBERED BIN ─────────────────────────────────────
    // Lando stamps a new filing bin with its number range — "Bin 51 through 100"
    // — and tucks either the actual docking files or a stack of smaller sub-bins
    // inside it, depending on how many records need to fit.
    // We record the parent entry, the index range of children this page covers,
    // optional sub-pages for nested pagination, and the sorter we'll use when
    // slicing the child array.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new entry page covering a contiguous range of child entries.
     * If sub-pages are provided, this is a non-leaf page that shows nested bins
     * instead of actual entries. If sub-pages is null, this is a leaf page that
     * returns the entries from index {@code first} to {@code last}.
     *
     * <p>For example — Lando creates a numbered bin in Cloud City filing:</p>
     * <pre>
     *   Bin bin51to100 = new Bin(dockingRecords, 50, 99, null, sorter);
     *   // Leaf bin — contains actual records 51 through 100
     *
     *   Bin bin1to500 = new Bin(dockingRecords, 0, 499, subBins, sorter);
     *   // Non-leaf bin — contains 10 sub-bins of 50 each
     * </pre>
     *
     * @param entry     the parent LDAP entry whose children this page covers
     * @param first     the zero-based index of the first child in this page
     * @param last      the zero-based index of the last child in this page (inclusive)
     * @param subpages  nested pages for hierarchical pagination, or null for a leaf page
     * @param sorter    the sorter used to order children before slicing the range
     */
    public BrowserEntryPage( IEntry entry, int first, int last, BrowserEntryPage[] subpages, BrowserSorter sorter )
    {
        this.entry = entry;
        this.first = first;
        this.last = last;
        this.subpages = subpages;
        this.sorter = sorter;

        if ( subpages != null )
        {
            for ( int i = 0; i < subpages.length; i++ )
            {
                subpages[i].parentEntryPage = this;
            }
        }
    }


    // ── LANDO OPENS THE BIN AND HANDS OUT ITS CONTENTS ───────────────────────
    // Open a Cloud City filing bin and you get either the actual docking records
    // (leaf bin) or a set of smaller numbered sub-bins (non-leaf bin).
    // The content provider calls this to decide what to show when the user
    // expands a page node in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the contents of this page — either nested sub-pages or the slice
     * of actual child entries for the index range {@code [first, last]}.
     * If this is a non-leaf page, we return the sub-pages array directly.
     * If this is a leaf page, we sort the parent's full child array and extract
     * the sub-range.
     *
     * <p>For example — opening a Cloud City filing bin:</p>
     * <pre>
     *   Object[] contents = bin51to100.getChildren();
     *   // Leaf bin: returns the actual docking records 51–100
     *
     *   Object[] contents = bin1to500.getChildren();
     *   // Non-leaf bin: returns sub-bins [1..50], [51..100], ...
     * </pre>
     *
     * @return the sub-pages array if non-leaf, or the slice of IEntry children
     *         for the leaf range; null if children aren't loaded yet
     */
    public Object[] getChildren()
    {
        if ( subpages != null )
        {
            return subpages;
        }
        else
        {
            // 1. get children
            IEntry[] children = entry.getChildren();

            // 2. sort
            sorter.sort( null, children );

            // 3. extract range
            if ( children != null )
            {
                IEntry[] childrenRange = new IEntry[last - first + 1];
                for ( int i = first; i <= last; i++ )
                {
                    childrenRange[i - first] = children[i];
                }
                return childrenRange;
            }
            else
            {
                return null;
            }
        }
    }


    // ── WHAT'S THE FIRST RECORD IN THIS BIN? ─────────────────────────────────
    // Lando reads the first number stamped on the bin label to know where in
    // the filing sequence this bin begins.
    // The label provider uses this to display "[first+1 ... last+1]" in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index of the first child entry covered by this page.
     * The label provider uses this (along with {@link #getLast()}) to display
     * the range label like "[1 ... 50]" in the tree.
     *
     * <p>For example — Lando reads the first number on the Cloud City bin label:</p>
     * <pre>
     *   int startAt = bin51to100.getFirst(); // returns 50 (zero-based)
     *   display.show("[" + (startAt + 1) + "...]");
     * </pre>
     *
     * @return the zero-based index of the first entry in this page
     */
    public int getFirst()
    {
        return first;
    }


    // ── WHAT'S THE LAST RECORD IN THIS BIN? ──────────────────────────────────
    // Lando reads the last number stamped on the bin label to know where in
    // the filing sequence this bin ends.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index of the last child entry covered by this page (inclusive).
     * Used together with {@link #getFirst()} to compute the range label and to
     * detect when the page needs to be rebuilt (e.g., new children were added).
     *
     * <p>For example — Lando reads the last number on the Cloud City bin label:</p>
     * <pre>
     *   int endAt = bin51to100.getLast(); // returns 99 (zero-based)
     *   display.show("[..." + (endAt + 1) + "]");
     * </pre>
     *
     * @return the zero-based index of the last entry in this page (inclusive)
     */
    public int getLast()
    {
        return last;
    }


    // ── WHICH SHELF IS THIS BIN FROM? ────────────────────────────────────────
    // Every bin in Cloud City belongs to a specific shelf (the parent LDAP entry).
    // You can ask a bin "which shelf do you belong to?" and it'll tell you.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent LDAP entry whose children are paginated by this page.
     * This is the entry in the directory tree whose child list was too long,
     * triggering the creation of this page node.
     *
     * <p>For example — retrieving the Cloud City shelf this bin sits on:</p>
     * <pre>
     *   IEntry shelf = bin51to100.getEntry();
     *   // shelf == the "ou=pilots" LDAP entry with 1000 children
     * </pre>
     *
     * @return the parent {@link IEntry} that owns the children in this page
     */
    public IEntry getEntry()
    {
        return entry;
    }


    // ── WHICH BIN IS THE DIRECT PARENT OF THIS RECORD? ───────────────────────
    // To file a specific docking record, Lando checks every numbered sub-bin
    // recursively until he finds the one that directly contains that record.
    // The content provider uses this to resolve "which page is the parent of
    // this specific entry?" when revealing items in the tree.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Recursively finds which page directly contains the given entry.
     * Used by the content provider to answer "what is the parent of this entry?"
     * when folding is active — the parent might be a page rather than the entry's
     * LDAP parent directly.
     *
     * <p>For example — Lando finds which Cloud City sub-bin holds a specific docking record:</p>
     * <pre>
     *   BrowserEntryPage bin = masterBin.getParentOf(specificRecord);
     *   // bin == the sub-bin [51..100] that directly contains the record
     * </pre>
     *
     * @param entry   the child entry to search for within this page tree
     * @return the page that directly contains the entry, or null if not found here
     */
    public BrowserEntryPage getParentOf( IEntry entry )
    {
        if ( subpages != null )
        {
            BrowserEntryPage ep = null;
            for ( int i = 0; i < subpages.length && ep == null; i++ )
            {
                ep = subpages[i].getParentOf( entry );
            }
            return ep;
        }
        else
        {
            IEntry[] sr = ( IEntry[] ) getChildren();
            if ( sr != null && Arrays.asList( sr ).contains( entry ) )
            {
                return this;
            }
            else
            {
                return null;
            }
        }
    }


    // ── WHERE DOES THIS BIN LIVE IN THE FILING HIERARCHY? ────────────────────
    // A bin might sit directly on the main shelf (parent is the LDAP entry),
    // or inside a larger bin (parent is another page). Callers need to know
    // which so they can navigate up the tree correctly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the immediate parent of this page in the tree — either another
     * page (if nested) or the parent LDAP entry (if this is a top-level page).
     * This drives the "navigate up" behavior in the content provider.
     *
     * <p>For example — a Cloud City sub-bin reports who it lives inside:</p>
     * <pre>
     *   Object parent = bin51to100.getParent();
     *   // If nested: parent == outerBin (a BrowserEntryPage)
     *   // If top-level: parent == ou=pilots entry (an IEntry)
     * </pre>
     *
     * @return the parent {@link BrowserEntryPage} if nested, or the parent
     *         {@link IEntry} if this is a top-level page
     */
    public Object getParent()
    {
        return ( parentEntryPage != null ) ? ( Object ) parentEntryPage : ( Object ) entry;
    }


    // ── LANDO READS THE BIN'S LABEL ALOUD ────────────────────────────────────
    // Lando picks up a bin, reads the label: "Docking Records [51...100], Bin 3."
    // toString() is mainly used in debugging to see what a BrowserEntryPage
    // represents without needing to open a debugger.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a debug-friendly string representation of this page.
     * Shows the parent entry's string, the index range, and the object hash code
     * to make individual page instances distinguishable in logs.
     *
     * <p>For example — Lando reads the bin label aloud in the Cloud City filing room:</p>
     * <pre>
     *   System.out.println(bin51to100.toString());
     *   // "ou=pilots,dc=example,dc=com[50...99]1a2b3c4d"
     * </pre>
     *
     * @return a string like "parentEntry[first...last]hashCode"
     */
    public String toString()
    {
        return entry.toString() + "[" + first + "..." + last + "]" + hashCode(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
