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


import java.math.BigInteger;

import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.studio.connection.core.jobs.StudioConnectionRunnableWithProgress;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IQuickSearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DirectoryMetadataEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.RootDSE;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;


// ── CLASS: BrowserSorter — Lando Organises Cloud City Operations ──────────────
// In The Empire Strikes Back, Lando Calrissian runs Cloud City with meticulous
// order: VIP guests go to the best suites, workers stay in their quarters,
// and troublemakers (like Han) go last. He has a priority system for everything.
// BrowserSorter is exactly that administrator: it decides which LDAP entries,
// searches, bookmarks and in-progress jobs appear in what order in the tree,
// respecting user-configured preferences (ascending/descending, leaf-first, etc.).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Determines the display order of every node in the browser tree.
 * It sorts LDAP entries, search results, searches, bookmarks and background
 * job runnables according to the user's sort preferences stored in
 * {@link BrowserPreferences}.
 * Think of this class as Lando managing Cloud City: everything has its place,
 * and the rules for who goes where come from the administrator's preferences.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BrowserSorter extends ViewerSorter
{
    /** The browser preferences, used to get the sort settings */
    private BrowserPreferences preferences;


    // ── LANDO ACCEPTS THE ADMINISTRATOR'S ROLE ────────────────────────────────
    // When Lando took over Cloud City he was handed a rulebook — who gets priority,
    // which areas are restricted, how guests are ranked. That rulebook is
    // preferences. Without it, Lando can't make a single scheduling decision.
    // We store it so every comparison we make can consult the current settings.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Constructs a new sorter that reads its ordering rules from the given
     * preferences object.
     * All sort decisions — ascending vs. descending, leaf-first vs.
     * container-first, sort limit — come from {@code preferences}, so we
     * keep a reference to it for every future comparison.
     *
     * @param preferences  The browser preferences that control how entries,
     *                     searches and bookmarks should be ordered. Must not
     *                     be {@code null}.
     */
    public BrowserSorter( BrowserPreferences preferences )
    {
        this.preferences = preferences;
    }


    // ── LANDO WIRES HIMSELF INTO THE CITY'S CONTROL SYSTEMS ──────────────────
    // Lando doesn't just have the rulebook — he plugs it into every terminal
    // in the city so that all scheduling decisions flow through him automatically.
    // Here we do the same: we attach this sorter to the JFace TreeViewer so
    // every refresh automatically uses our ordering logic.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Attaches this sorter to the given tree viewer so it controls how the
     * viewer orders its elements on every refresh.
     * After this call, the viewer will call our {@link #compare} and
     * {@link #category} methods whenever it needs to sort its children.
     *
     * @param viewer  The JFace TreeViewer to plug this sorter into.
     */
    public void connect( TreeViewer viewer )
    {
        viewer.setSorter( this );
    }


    // ── LANDO GIVES THE SORTING ORDER — BUT ONLY IF THE CROWD IS MANAGEABLE ──
    // Lando is efficient: if ten thousand guests show up at once he stops trying
    // to sort the queue — it would take forever and the city would grind to a
    // halt. He only sorts when the crowd is within reason (sortLimit). We do the
    // same: skip sorting when elements exceed the configured limit, for performance.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sorts the given array of tree elements in place, but only when sorting
     * is feasible — i.e. either no sort limit is configured (limit &lt;= 0)
     * or the number of elements is below the limit.
     * This guards us from freezing the UI when an LDAP entry has tens of
     * thousands of children.
     *
     * <p>For example — Lando at the Cloud City guest queue:</p>
     * <pre>
     *   if (guestCount &lt; cityCapacity)
     *       super.sort(viewer, guests);   // sort normally
     *   // else: too many guests — leave them in arrival order
     * </pre>
     *
     * @param viewer    The viewer requesting the sort (may be {@code null}
     *                  when called programmatically, e.g. from
     *                  {@link BrowserSearchResultPage#getChildren()}).
     * @param elements  The array to sort in place.
     */
    public void sort( final Viewer viewer, final Object[] elements )
    {
        if ( elements != null && ( preferences.getSortLimit() <= 0 || elements.length < preferences.getSortLimit() ) )
        {
            BrowserSorter.super.sort( viewer, elements );
        }
    }


    // ── LANDO CATEGORISES EACH GUEST ─────────────────────────────────────────
    // Lando assigns every arrival to a category: subentry VIPs (category 0),
    // leaf-first or container-first guests (category 1), regular citizens
    // (category 2), meta/system guests like droids or bounty hunters last
    // (category 3), and anything else (category 4). Categories are compared
    // before individual names, so all VIPs always appear before all citizens.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Assigns a numeric bucket to a tree element so that items in a lower
     * bucket always sort before items in a higher bucket, regardless of name.
     * This is how we keep subentries at the top, meta/alias entries at the
     * bottom, and leaf-first or container-first in between — all without
     * mixing categories in a single alphabetical pass.
     *
     * <p>For example — Lando at the reception desk:</p>
     * <pre>
     *   subentry  → 0  (highest priority, always first)
     *   leaf-first or container-first entries → 1
     *   normal entries → 2
     *   meta/alias/referral entries → 3  (lowest priority, always last)
     *   anything else (searches, etc.) → 4
     * </pre>
     *
     * @param element  The tree element to categorise (typically an
     *                 {@link IEntry}).
     * @return  An integer category; lower values sort earlier.
     */
    public int category( Object element )
    {
        if ( element instanceof IEntry )
        {
            IEntry entry = ( IEntry ) element;
            if ( ( entry instanceof DirectoryMetadataEntry || entry instanceof RootDSE || entry.isAlias() || entry
                .isReferral() )
                && preferences.isMetaEntriesLast() )
            {
                return 3;
            }
            else if ( entry.isSubentry() )
            {
                return 0;
            }
            else if ( !entry.hasChildren() && preferences.isLeafEntriesFirst() )
            {
                return 1;
            }
            else if ( entry.hasChildren() && preferences.isContainerEntriesFirst() )
            {
                return 1;
            }
            else
            {
                return 2;
            }
        }
        else
        {
            return 4;
        }
    }


    // ── LANDO DECIDES WHO GOES FIRST IN THE QUEUE ─────────────────────────────
    // Two guests arrive at the same time. Lando checks: are either of them
    // background-job droids (runnables)? Are they null? Searches? Bookmarks?
    // Entries? He works through his rulebook systematically until he has a
    // verdict: negative means o1 goes first, positive means o2 goes first.
    // Each type of thing has its own comparison sub-rule.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two tree elements and returns a negative integer, zero, or
     * positive integer to establish their relative order.
     * We handle many element types: background job runnables (which pin
     * "load more" nodes to the top or bottom), entries, search results,
     * searches, and bookmarks. Each type gets compared by its own rule,
     * and type categories are compared before individual names.
     *
     * <p>For example — Lando at the Cloud City queue booth:</p>
     * <pre>
     *   if (o1 is a LoadMoreJob runnable) → pin it to top or bottom
     *   if (o1 and o2 are IEntry)         → compare by category, then RDN
     *   if (o1 and o2 are ISearch)        → compare by search name
     *   if (o1 and o2 are IBookmark)      → compare by bookmark name
     * </pre>
     *
     * @param viewer  The viewer asking for the comparison (may be {@code null}).
     * @param o1      The first element.
     * @param o2      The second element.
     * @return  Negative if o1 sorts before o2, zero if equal, positive if o1
     *          sorts after o2.
     */
    public int compare( Viewer viewer, Object o1, Object o2 )
    {
        // o1 is StudioConnectionRunnableWithProgress
        if ( o1 instanceof StudioConnectionRunnableWithProgress )
        {
            StudioConnectionRunnableWithProgress runnable = ( StudioConnectionRunnableWithProgress ) o1;

            for ( Object lockedObject : runnable.getLockedObjects() )
            {
                if ( lockedObject instanceof ISearch )
                {
                    ISearch search = ( ISearch ) lockedObject;

                    if ( o1 == search.getTopSearchRunnable() )
                    {
                        return lessThanEntries();
                    }
                    else if ( o1 == search.getNextSearchRunnable() )
                    {
                        return greaterThanEntries();
                    }
                }
                else if ( lockedObject instanceof IEntry )
                {
                    IEntry entry = ( IEntry ) lockedObject;

                    if ( o1 == entry.getTopPageChildrenRunnable() )
                    {
                        return lessThanEntries();
                    }
                    else if ( o1 == entry.getNextPageChildrenRunnable() )
                    {
                        return greaterThanEntries();
                    }
                }
            }

            return lessThanEntries();
        }

        // o2 is StudioConnectionRunnableWithProgress
        if ( o2 instanceof StudioConnectionRunnableWithProgress )
        {
            StudioConnectionRunnableWithProgress runnable = ( StudioConnectionRunnableWithProgress ) o2;

            for ( Object lockedObject : runnable.getLockedObjects() )
            {
                if ( lockedObject instanceof ISearch )
                {
                    ISearch search = ( ISearch ) lockedObject;

                    if ( o2 == search.getTopSearchRunnable() )
                    {
                        return greaterThanEntries();
                    }
                    else if ( o2 == search.getNextSearchRunnable() )
                    {
                        return lessThanEntries();
                    }
                }
                else if ( lockedObject instanceof IEntry )
                {
                    IEntry entry = ( IEntry ) lockedObject;

                    if ( o2 == entry.getTopPageChildrenRunnable() )
                    {
                        return greaterThanEntries();
                    }
                    else if ( o2 == entry.getNextPageChildrenRunnable() )
                    {
                        return lessThanEntries();
                    }
                }
            }

            return greaterThanEntries();
        }

        // o1 and o2 are null
        if ( o1 == null && o2 == null )
        {
            return equal();
        }

        // o1 is null, o2 isn't
        else if ( o1 == null && o2 != null )
        {
            return lessThanEntries();
        }

        // o1 isn't null, o1 is
        else if ( o1 != null && o2 == null )
        {
            return greaterThanEntries();
        }

        // special case for quick search
        else if ( o1 instanceof IQuickSearch || o2 instanceof IQuickSearch )
        {
            if ( !( o1 instanceof IQuickSearch ) && ( o2 instanceof IQuickSearch ) )
            {
                return 1;
            }
            else if ( ( o1 instanceof IQuickSearch ) && !( o2 instanceof IQuickSearch ) )
            {
                return -1;
            }
            else
            {
                return equal();
            }
        }

        // o1 and o2 are entries
        else if ( o1 instanceof IEntry || o2 instanceof IEntry )
        {
            if ( !( o1 instanceof IEntry ) && !( o2 instanceof IEntry ) )
            {
                return equal();
            }
            else if ( !( o1 instanceof IEntry ) && ( o2 instanceof IEntry ) )
            {
                return lessThanEntries();
            }
            else if ( ( o1 instanceof IEntry ) && !( o2 instanceof IEntry ) )
            {
                return greaterThanEntries();
            }
            else
            {
                IEntry entry1 = ( IEntry ) o1;
                IEntry entry2 = ( IEntry ) o2;

                int cat1 = category( entry1 );
                int cat2 = category( entry2 );

                if ( cat1 != cat2 )
                {
                    return cat1 - cat2;
                }
                else if ( preferences.getSortEntriesBy() == BrowserCoreConstants.SORT_BY_NONE )
                {
                    return equal();
                }
                else if ( preferences.getSortEntriesBy() == BrowserCoreConstants.SORT_BY_RDN )
                {
                    return compareRdns( entry1, entry2 );
                }
                else if ( preferences.getSortEntriesBy() == BrowserCoreConstants.SORT_BY_RDN_VALUE )
                {
                    return compareRdnValues( entry1, entry2 );
                }
                else
                {
                    return equal();
                }
            }
        }

        // o1 and o2 are search results
        else if ( o1 instanceof ISearchResult || o2 instanceof ISearchResult )
        {
            if ( !( o1 instanceof ISearchResult ) && !( o2 instanceof ISearchResult ) )
            {
                return equal();
            }
            else if ( !( o1 instanceof ISearchResult ) && ( o2 instanceof ISearchResult ) )
            {
                return lessThanEntries();
            }
            else if ( ( o1 instanceof ISearchResult ) && !( o2 instanceof ISearchResult ) )
            {
                return greaterThanEntries();
            }
            else
            {
                ISearchResult sr1 = ( ISearchResult ) o1;
                ISearchResult sr2 = ( ISearchResult ) o2;

                int cat1 = category( sr1 );
                int cat2 = category( sr2 );

                if ( cat1 != cat2 )
                {
                    return cat1 - cat2;
                }
                else if ( preferences.getSortEntriesBy() == BrowserCoreConstants.SORT_BY_NONE )
                {
                    return equal();
                }
                else if ( preferences.getSortEntriesBy() == BrowserCoreConstants.SORT_BY_RDN )
                {
                    return compareRdns( sr1.getEntry(), sr2.getEntry() );
                }
                else if ( preferences.getSortEntriesBy() == BrowserCoreConstants.SORT_BY_RDN_VALUE )
                {
                    return compareRdnValues( sr1.getEntry(), sr2.getEntry() );
                }
                else
                {
                    return equal();
                }
            }
        }

        // o1 and o2 are searches
        else if ( o1 instanceof ISearch || o2 instanceof ISearch )
        {
            if ( !( o1 instanceof ISearch ) && !( o2 instanceof ISearch ) )
            {
                return equal();
            }
            else if ( !( o1 instanceof ISearch ) && ( o2 instanceof ISearch ) )
            {
                return lessThanSearches();
            }
            else if ( ( o1 instanceof ISearch ) && !( o2 instanceof ISearch ) )
            {
                return greaterThanSearches();
            }
            else
            {
                ISearch s1 = ( ISearch ) o1;
                ISearch s2 = ( ISearch ) o2;

                if ( preferences.getSortSearchesOrder() == BrowserCoreConstants.SORT_ORDER_NONE )
                {
                    return equal();
                }
                else
                {
                    return compareSearches( s1.getName(), s2.getName() );
                }
            }
        }

        // o1 and o2 are bookmarks
        else if ( o1 instanceof IBookmark || o2 instanceof IBookmark )
        {
            if ( !( o1 instanceof IBookmark ) && !( o2 instanceof IBookmark ) )
            {
                return equal();
            }
            else if ( !( o1 instanceof IBookmark ) && ( o2 instanceof IBookmark ) )
            {
                return lessThanBookmarks();
            }
            else if ( ( o1 instanceof IBookmark ) && !( o2 instanceof IBookmark ) )
            {
                return greaterThanBookmarks();
            }
            else
            {
                IBookmark b1 = ( IBookmark ) o1;
                IBookmark b2 = ( IBookmark ) o2;

                if ( preferences.getSortBookmarksOrder() == BrowserCoreConstants.SORT_ORDER_NONE )
                {
                    return equal();
                }
                else
                {
                    return compareBookmarks( b1.getName(), b2.getName() );
                }
            }
        }
        else
        {
            return equal();
        }
    }


    // ── LANDO CHECKS THE FULL NAME PLATE ──────────────────────────────────────
    // Two guests arrive: their name plates read "cn=Leia Organa" and
    // "cn=Luke Skywalker." Lando reads the full RDN string — type and value
    // together — and compares alphabetically to decide who's listed first in
    // the city register. Null RDNs get bumped to the end.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two entries by their full RDN string representation (e.g.
     * {@code "cn=Leia"} vs {@code "cn=Luke"}), case-insensitively.
     * Null RDNs are treated as greater than non-null ones so they sink to
     * the bottom of the list.
     *
     * @param entry1  The first entry to compare.
     * @param entry2  The second entry to compare.
     * @return  Negative, zero, or positive as defined by the current sort order.
     */
    private int compareRdns( IEntry entry1, IEntry entry2 )
    {
        Rdn rdn1 = entry1.getRdn();
        Rdn rdn2 = entry2.getRdn();

        if ( rdn1 == null && rdn2 == null )
        {
            return equal();
        }
        else if ( rdn1 == null && rdn2 != null )
        {
            return greaterThanEntries();
        }
        else if ( rdn1 != null && rdn2 == null )
        {
            return lessThanEntries();
        }
        else
        {
            return compareEntries( rdn1.getName(), rdn2.getName() );
        }
    }


    // ── LANDO COMPARES JUST THE GUEST'S NAME VALUE ────────────────────────────
    // Lando's priority system sometimes ignores the attribute type and only
    // looks at the value part of the name plate — "Leia" vs "Luke."  And if
    // both names are pure numbers (room numbers, maybe), he compares them
    // numerically so that "9" doesn't sort after "10" the way strings would.
    // That's this method: compare RDN values, with numeric-aware handling.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two entries by the value portion of their RDN only (e.g.
     * {@code "Leia"} vs {@code "Luke"}), case-insensitively.
     * When both values are pure digit strings (like {@code "007"} and
     * {@code "42"}) we parse and compare them as {@link BigInteger} so the
     * numeric order is correct.
     *
     * @param entry1  The first entry to compare.
     * @param entry2  The second entry to compare.
     * @return  Negative, zero, or positive as defined by the current sort order.
     */
    private int compareRdnValues( IEntry entry1, IEntry entry2 )
    {
        if ( ( entry1 == null ) && ( entry2 == null ) )
        {
            return equal();
        }
        else if ( ( entry1 != null ) && ( entry2 == null ) )
        {
            return greaterThanEntries();
        }
        else if ( ( entry1 == null ) && ( entry2 != null ) )
        {
            return lessThanEntries();
        }
        else
        {
            Rdn rdn1 = entry1.getRdn();
            Rdn rdn2 = entry2.getRdn();

            if ( ( rdn1 == null || rdn1.getName() == null || "".equals( rdn1.getName() ) ) //$NON-NLS-1$
                && ( rdn2 == null || rdn2.getName() == null || "".equals( rdn2.getName() ) ) ) //$NON-NLS-1$
            {
                return equal();
            }
            else if ( ( rdn1 == null || rdn1.getName() == null || "".equals( rdn1.getName() ) ) //$NON-NLS-1$
                && !( rdn2 == null || rdn2.getName() == null || "".equals( rdn2.getName() ) ) ) //$NON-NLS-1$
            {
                return greaterThanEntries();
            }
            else if ( !( rdn1 == null || rdn1.getName() == null || "".equals( rdn1.getName() ) ) //$NON-NLS-1$
                && ( rdn2 == null || rdn2.getName() == null || "".equals( rdn2.getName() ) ) ) //$NON-NLS-1$
            {
                return lessThanEntries();
            }

            String rdn1Value = ( String ) rdn1.getName();
            String rdn2Value = ( String ) rdn2.getName();
            if ( rdn1Value.matches( "\\d*" ) && !rdn2Value.matches( "\\d*" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                // return lessThan();
                return compareEntries( rdn1Value, rdn2Value );
            }
            else if ( !rdn1Value.matches( "\\d*" ) && rdn2Value.matches( "\\d*" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                // return greaterThan();
                return compareEntries( rdn1Value, rdn2Value );
            }
            else if ( rdn2Value.matches( "\\d*" ) && rdn2Value.matches( "\\d*" ) ) //$NON-NLS-1$ //$NON-NLS-2$
            {
                BigInteger bi1 = new BigInteger( rdn1Value );
                BigInteger bi2 = new BigInteger( rdn2Value );
                return compare( bi1, bi2 );
                // return Integer.parseInt(rdn1.getValue()) -
                // Integer.parseInt(rdn2.getValue());
            }
            else
            {
                return compareEntries( rdn1Value, rdn2Value );
            }
        }
    }


    // ── LANDO SIGNALS "THIS ONE GOES FIRST" FOR ENTRIES ──────────────────────
    // Lando checks his rulebook: if we're in ascending order, the "less than"
    // verdict is -1 (go earlier). If descending, it flips to +1 (go later).
    // This helper just hands back the right signed value so all the comparison
    // methods can stay clean and readable.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "first" signal ({@code -1} for ascending order,
     * {@code +1} for descending) for entry comparisons.
     * We flip the sign for descending so the rest of the code can just call
     * {@code lessThanEntries()} without worrying which direction we're sorting.
     *
     * @return  {@code -1} if entries sort ascending, {@code +1} if descending.
     */
    private int lessThanEntries()
    {
        return preferences.getSortEntriesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? -1 : 1;
    }


    // ── LANDO SIGNALS "THIS ONE GOES FIRST" FOR SEARCHES ─────────────────────
    // Same idea as lessThanEntries, but for the Searches section of the tree.
    // Searches have their own independent sort-order preference, so we read it
    // from the right preference key instead of the entries one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "first" signal for search comparisons, respecting the
     * searches-specific sort order preference.
     *
     * @return  {@code -1} if searches sort ascending, {@code +1} if descending.
     */
    private int lessThanSearches()
    {
        return preferences.getSortSearchesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? -1 : 1;
    }


    // ── LANDO SIGNALS "THIS ONE GOES FIRST" FOR BOOKMARKS ────────────────────
    // Same pattern again, this time for the Bookmarks section. Bookmarks have
    // their own sort order, so we consult the bookmarks preference specifically.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "first" signal for bookmark comparisons, respecting the
     * bookmarks-specific sort order preference.
     *
     * @return  {@code -1} if bookmarks sort ascending, {@code +1} if descending.
     */
    private int lessThanBookmarks()
    {
        return preferences.getSortBookmarksOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? -1 : 1;
    }


    // ── LANDO CALLS A TIE ─────────────────────────────────────────────────────
    // Both guests have the exact same priority. Lando shrugs and lets them
    // stay in whatever order they arrived. Zero means equal — don't move them.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns {@code 0} to signal that two elements are considered equal in
     * sort order and should remain in their current relative order.
     *
     * @return  Always {@code 0}.
     */
    private int equal()
    {
        return 0;
    }


    // ── LANDO SIGNALS "THIS ONE GOES LATER" FOR ENTRIES ──────────────────────
    // The mirror of lessThanEntries: +1 means "push this one down the list"
    // in ascending order, -1 in descending order (because down = earlier then).
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "last" signal ({@code +1} for ascending order,
     * {@code -1} for descending) for entry comparisons.
     *
     * @return  {@code +1} if entries sort ascending, {@code -1} if descending.
     */
    private int greaterThanEntries()
    {
        return preferences.getSortEntriesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? 1 : -1;
    }


    // ── LANDO SIGNALS "THIS ONE GOES LATER" FOR SEARCHES ─────────────────────
    // Same as greaterThanEntries but for the Searches section of the tree,
    // using the searches sort-order preference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "last" signal for search comparisons, respecting the
     * searches-specific sort order preference.
     *
     * @return  {@code +1} if searches sort ascending, {@code -1} if descending.
     */
    private int greaterThanSearches()
    {
        return preferences.getSortSearchesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? 1 : -1;
    }


    // ── LANDO SIGNALS "THIS ONE GOES LATER" FOR BOOKMARKS ────────────────────
    // Same as greaterThanEntries but for the Bookmarks section, using the
    // bookmarks sort-order preference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "last" signal for bookmark comparisons, respecting the
     * bookmarks-specific sort order preference.
     *
     * @return  {@code +1} if bookmarks sort ascending, {@code -1} if descending.
     */
    private int greaterThanBookmarks()
    {
        return preferences.getSortBookmarksOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? 1 : -1;
    }


    // ── LANDO CHECKS THE GUEST NAME BOARD — ENTRIES ───────────────────────────
    // Lando reads two name tags and decides alphabetical order. He ignores case
    // (Leia == leia), and if we're in descending mode he swaps s1 and s2 so the
    // same compareToIgnoreCase call gives us the reversed result. Clean trick.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two entry name strings case-insensitively, reversing the
     * operands when the sort order is descending so we don't need separate
     * ascending and descending code paths.
     *
     * @param s1  First entry name string.
     * @param s2  Second entry name string.
     * @return  Negative, zero, or positive depending on order and sort direction.
     * @see java.lang.String#compareToIgnoreCase(String)
     */
    private int compareEntries( String s1, String s2 )
    {
        return preferences.getSortEntriesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? s1
            .compareToIgnoreCase( s2 ) : s2.compareToIgnoreCase( s1 );
    }


    // ── LANDO CHECKS THE SEARCH NAME BOARD ────────────────────────────────────
    // Same technique as compareEntries but applied to search names.
    // Searches have their own sort-order setting, so we read that one instead.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two search name strings case-insensitively, respecting the
     * searches sort order preference.
     *
     * @param s1  First search name.
     * @param s2  Second search name.
     * @return  Negative, zero, or positive depending on order and sort direction.
     * @see java.lang.String#compareToIgnoreCase(String)
     */
    private int compareSearches( String s1, String s2 )
    {
        return preferences.getSortSearchesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? s1
            .compareToIgnoreCase( s2 ) : s2.compareToIgnoreCase( s1 );
    }


    // ── LANDO CHECKS THE BOOKMARK NAME BOARD ──────────────────────────────────
    // Same technique again for bookmark names. Bookmarks have their own
    // sort-order preference, so we use that one.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two bookmark name strings case-insensitively, respecting the
     * bookmarks sort order preference.
     *
     * @param s1  First bookmark name.
     * @param s2  Second bookmark name.
     * @return  Negative, zero, or positive depending on order and sort direction.
     * @see java.lang.String#compareToIgnoreCase(String)
     */
    private int compareBookmarks( String s1, String s2 )
    {
        return preferences.getSortBookmarksOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? s1
            .compareToIgnoreCase( s2 ) : s2.compareToIgnoreCase( s1 );
    }


    // ── LANDO READS ROOM NUMBERS ON THE LEDGER ────────────────────────────────
    // Two rooms: 9 and 10. String comparison says "10" < "9" which is wrong.
    // Lando, ever meticulous, consults the actual numeric ledger: BigInteger
    // compareTo gives the right answer. And again we swap for descending order.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link BigInteger} values numerically, respecting the
     * entries sort order preference.
     * We use BigInteger instead of int/long because LDAP attribute values can
     * be arbitrarily large — you could have an entry with
     * {@code uidNumber=99999999999999999}.
     *
     * @param bi1  First big-integer value.
     * @param bi2  Second big-integer value.
     * @return  {@code -1}, {@code 0}, or {@code 1} as defined by
     *          {@link BigInteger#compareTo(BigInteger)}, adjusted for sort order.
     * @see java.math.BigInteger#compareTo(BigInteger)
     */
    private int compare( BigInteger bi1, BigInteger bi2 )
    {
        return preferences.getSortEntriesOrder() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? bi1.compareTo( bi2 )
            : bi2.compareTo( bi1 );
    }
}
