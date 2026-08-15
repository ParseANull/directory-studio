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

import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;


// ── CLASS: BrowserSearchResultPage — Rebel Pilots' Sector Briefing ────────────
// In Return of the Jedi, the Rebel pilots receive a holographic sector grid
// before the assault on the second Death Star. Each pilot is handed only their
// slice of the battle map — a page of targets, not the whole thing. That's exactly
// what this class is: one page of a potentially massive LDAP search result set,
// holding just the slice from index `first` to index `last`.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A container for one page's worth of LDAP search results, or for nested
 * sub-pages when a result set is too large to show at once.
 * When a search returns thousands of entries the browser folds them into
 * pages so the tree doesn't become unusable — this class is one of those pages.
 * Think of this class as the sector grid each Rebel pilot holds during the
 * Death Star briefing: a bounded slice of the full battlefield.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserSearchResultPage
{

    /** The tree sorter */
    private BrowserSorter sorter;

    /** The index of the first child search result in this page */
    private int first;

    /** The index of the last child search result in this page */
    private int last;

    /** The parent search */
    private ISearch search;

    /** The parent search result page or null if not nested */
    private BrowserSearchResultPage parentSearchResultPage;

    /** The sub pages */
    private BrowserSearchResultPage[] subpages;


    // ── SECTOR ASSIGNMENT: PILOTS RECEIVE THEIR GRID ─────────────────────────
    // During the Endor briefing, Ackbar hands each Rebel pilot a sector card:
    // "Red Group, you cover entries 0–99. Gold Group, entries 100–199."
    // Each pilot knows their first target, their last target, and who their wing
    // is — and every wing knows who their squadron leader (parent page) is.
    // This constructor sets up exactly that chain of accountability.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a new page covering the search results from index {@code first}
     * to index {@code last} (inclusive), optionally nesting child sub-pages
     * underneath it when the range is still too large.
     * We set the parent reference on each sub-page here so the tree can walk
     * back up to the root later.
     *
     * <p>For example — Ackbar assigns sectors before the Endor assault:</p>
     * <pre>
     *   Red Leader:   sector [0..99],   sub-pages = [0..49], [50..99]
     *   Gold Leader:  sector [100..199], sub-pages = null (fits on one page)
     *   Each sub-page.parentSearchResultPage = their squadron page.
     * </pre>
     *
     * @param search    The LDAP search that produced these results. Every page
     *                  needs to know its parent mission so it can ask for the
     *                  full result list when the user expands it.
     * @param first     Zero-based index of the first search result on this page.
     * @param last      Zero-based index of the last search result on this page
     *                  (inclusive).
     * @param subpages  Nested child pages, or {@code null} if this is a leaf
     *                  page that shows actual search results directly.
     * @param sorter    The sorter used to order results before slicing them
     *                  into this page's range.
     */
    public BrowserSearchResultPage( ISearch search, int first, int last, BrowserSearchResultPage[] subpages,
        BrowserSorter sorter )
    {
        this.search = search;
        this.first = first;
        this.last = last;
        this.subpages = subpages;
        this.sorter = sorter;

        if ( subpages != null )
        {
            for ( int i = 0; i < subpages.length; i++ )
            {
                subpages[i].parentSearchResultPage = this;
            }
        }
    }


    // ── CONSULTING THE SECTOR GRID ────────────────────────────────────────────
    // A Rebel pilot flips open their sector card. If they're a squadron leader
    // they see a list of wing pages, not individual targets. If they're a wing
    // pilot they see the actual targets in their assigned range — already sorted
    // and sliced to just their section of the battle map.
    // This method does the same: delegates to sub-pages, or fetches, sorts and
    // slices the real search results for a leaf page.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the direct children of this page — either the nested sub-pages
     * (when this is an intermediate grouping node) or the actual
     * {@link ISearchResult} objects for this slice of the result set.
     * For leaf pages we fetch all results from the parent search, sort them,
     * then cut out just the {@code [first..last]} range to return.
     *
     * <p>For example — Wedge checks his sector card:</p>
     * <pre>
     *   if (wedge.hasSubPages())
     *       return subPages;          // "coordinate with your wings"
     *   else
     *       sort all targets, then return targets[first..last];
     * </pre>
     *
     * @return  Either the array of sub-pages or the sorted, sliced array of
     *          search results, or {@code null} if the parent search has no
     *          results yet.
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
            ISearchResult[] children = search.getSearchResults();

            // 2. sort
            sorter.sort( null, children );

            // 3. extract range
            if ( children != null )
            {
                ISearchResult[] childrenRange = new ISearchResult[last - first + 1];
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


    // ── READING THE FIRST TARGET INDEX ────────────────────────────────────────
    // Wedge glances at the top of his sector card to confirm where his
    // assigned range starts — "I've got targets beginning at index 50."
    // This is the index into the full sorted result array where our page begins.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index of the first search result included on
     * this page.
     * Useful when the parent or the tree framework needs to reconstruct
     * which slice of the full result set this page covers.
     *
     * @return  The index of the first result in this page's range.
     */
    public int getFirst()
    {
        return first;
    }


    // ── READING THE LAST TARGET INDEX ─────────────────────────────────────────
    // Wedge checks the bottom of the card: "My range ends at index 99.
    // Anything past that is Gold Group's problem." Knowing the end of the
    // range is what lets us slice the sorted result array cleanly.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the zero-based index of the last search result included on
     * this page (inclusive).
     * Together with {@link #getFirst()} this fully describes the slice of the
     * parent search's result array that this page is responsible for.
     *
     * @return  The index of the last result in this page's range.
     */
    public int getLast()
    {
        return last;
    }


    // ── IDENTIFYING THE MISSION ───────────────────────────────────────────────
    // Each sector card is stamped with the operation name — "Operation Endor."
    // Pilots need to know which search mission their targets belong to so they
    // can call back to HQ (the search object) for the full target list.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the LDAP search that owns this page.
     * The search is the source of all results that appear on this page —
     * we need it to fetch the full result list in {@link #getChildren()}.
     *
     * @return  The parent {@link ISearch} for this page.
     */
    public ISearch getSearch()
    {
        return search;
    }


    // ── LOCATING A TARGET'S HOME SECTOR ──────────────────────────────────────
    // Ackbar radios in: "Which sector is responsible for target TIE-7?"
    // If this page has sub-pages, it passes the question down the chain.
    // If this is a leaf page it checks its own target list — if TIE-7 is here,
    // this page claims responsibility and returns itself.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Finds which page within this page's hierarchy is the direct parent of
     * the given search result.
     * We walk down through sub-pages recursively until we find the leaf page
     * whose result array contains {@code searchResult}, then return that page.
     * Returns {@code null} if the result isn't within our subtree at all.
     *
     * <p>For example — Ackbar tracks a specific target to its sector:</p>
     * <pre>
     *   sector.getParentOf(tie7)
     *     → checks sub-pages one by one
     *     → leaf page finds tie7 in its result list
     *     → returns that leaf page
     * </pre>
     *
     * @param searchResult  The search result whose parent page we're looking for.
     * @return              The leaf {@link BrowserSearchResultPage} that directly
     *                      contains this result, or {@code null} if it isn't here.
     */
    public BrowserSearchResultPage getParentOf( ISearchResult searchResult )
    {
        if ( subpages != null )
        {
            BrowserSearchResultPage ep = null;
            for ( int i = 0; i < subpages.length && ep == null; i++ )
            {
                ep = subpages[i].getParentOf( searchResult );
            }
            return ep;
        }
        else
        {
            ISearchResult[] sr = ( ISearchResult[] ) getChildren();
            if ( sr != null && Arrays.asList( sr ).contains( searchResult ) )
            {
                return this;
            }
            else
            {
                return null;
            }
        }
    }


    // ── REPORTING TO THE CHAIN OF COMMAND ─────────────────────────────────────
    // "Who do I report to?" Red Five asks. If they're nested inside a squadron
    // page, they report to that page (their parent page). If they're a top-level
    // sector, they report directly to Ackbar (the search itself). This method
    // walks one step up the hierarchy and returns whoever is in charge.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the immediate parent of this page in the tree hierarchy.
     * That is either the enclosing {@link BrowserSearchResultPage} (if this is
     * a nested sub-page) or the root {@link ISearch} itself (if this is a
     * top-level page).
     * The tree framework needs this to navigate upward for things like
     * selection reveal and parent-element queries.
     *
     * @return  The parent {@link BrowserSearchResultPage} or {@link ISearch}.
     */
    public Object getParent()
    {
        return ( parentSearchResultPage != null ) ? ( Object ) parentSearchResultPage : ( Object ) search;
    }


    // ── CALLING IN YOUR SECTOR ON COMMS ───────────────────────────────────────
    // "Red Sector, targets 50 through 99, standing by." Each pilot announces
    // their sector designation on comms so everyone can track who's where.
    // We do the same: our string form is the search name plus the index range
    // plus a hash so two pages with the same range are still distinguishable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns a human-readable identifier for this page, combining the parent
     * search name, the {@code [first...last]} index range, and this object's
     * identity hash code.
     * The hash is appended so two pages with the same range (from different
     * searches) don't collide in maps or debug output.
     *
     * @return  A string like {@code "ou=people[50...99]1234567"}.
     */
    public String toString()
    {
        return search.toString() + "[" + first + "..." + last + "]" + hashCode(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
