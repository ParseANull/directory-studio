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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.ui.actions.SelectionUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserCategory;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserEntryPage;
import org.apache.directory.studio.ldapbrowser.common.widgets.browser.BrowserSearchResultPage;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.AttributeHierarchy;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Search;
import org.apache.directory.studio.ldapbrowser.core.utils.LdapFilterUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;


// ── CLASS: BrowserSelectionUtils — R2-D2 SCANNING THE BATTLEFIELD ─────────────
// On the ice plains of Hoth, R2-D2 activates his sensor suite: he scans the
// chaotic wreckage and instantly classifies every contact — that's an X-wing
// pilot (IEntry), that's a tauntaun saddle (IAttribute), that's a power cell
// (IValue), that's a rescue beacon (ISearch). He groups each type into the
// right bin so the rest of the crew knows exactly what they're working with.
// BrowserSelectionUtils is that sensor suite: given a raw JFace ISelection
// (which is just "some objects the user clicked"), it extracts and classifies
// them into typed arrays — entries, attributes, values, searches, bookmarks, etc.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Utility class for extracting strongly-typed objects from a JFace
 * {@link ISelection}. The LDAP browser's tree can contain many different types
 * of objects — entries, searches, bookmarks, attributes, values, paged nodes,
 * etc. — all mixed together in one selection event. This class provides static
 * helper methods to extract each type into its own array.
 * Think of this class as R2-D2's sensor suite: it receives a raw mixed signal
 * and classifies it into clean, typed categories.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class BrowserSelectionUtils extends SelectionUtils
{

    // ── R2 BUILDS A PROTOTYPE SEARCH FROM WHATEVER IS IN RANGE ───────────────
    // R2 scans whatever objects are in the selection and constructs a template
    // search from them: if there's a saved search already, clone it; if there's
    // an entry, use its DN as the base; if there are attribute values, build a
    // query-by-example filter from them. He sorts by most-specific type first
    // so the best candidate drives the prototype.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Derives a prototype {@link ISearch} from whatever is currently selected.
     * The strategy depends on the most specific selected type:
     * <ul>
     *   <li>ISearch → cloned directly (parameters copied)</li>
     *   <li>IEntry/ISearchResult/IBookmark → DN used as search base</li>
     *   <li>IEntry → children filter applied as the filter</li>
     *   <li>IAttribute/IValue/AttributeHierarchy → DN as base, filter
     *       built by query-by-example from the attribute values</li>
     *   <li>Connection/IBrowserConnection → first child of the root DSE as base</li>
     * </ul>
     * Used by the "Open Search" action to pre-populate a new search dialog
     * with sensible defaults based on context.
     *
     * <p>For example — R2 assembling a rescue vector from the debris field:</p>
     * <pre>
     *   ISearch proto = BrowserSelectionUtils.getExampleSearch( selection );
     *   // proto.getSearchBase() = selected entry's DN
     *   // proto.getFilter()     = filter built from selected attribute values
     * </pre>
     *
     * @param selection  the current JFace selection to derive a prototype from.
     * @return a prototype {@link ISearch} with best-effort parameters set;
     *         never null, but may have null connection if nothing useful was selected.
     */
    public static ISearch getExampleSearch( ISelection selection )
    {
        ISearch exampleSearch = new Search();
        String oldName = exampleSearch.getSearchParameter().getName();
        exampleSearch.getSearchParameter().setName( null );
        exampleSearch.setScope( SearchScope.SUBTREE );

        if ( ( selection instanceof StructuredSelection ) && !selection.isEmpty() )
        {
            Object[] objects = ( ( IStructuredSelection ) selection ).toArray();
            Comparator<Object> comparator = new Comparator<Object>()
            {
                public int compare( Object o1, Object o2 )
                {
                    if ( ( o1 instanceof IValue ) && !( o2 instanceof IValue ) )
                    {
                        return -1;
                    }
                    else if ( !( o1 instanceof IValue ) && ( o2 instanceof IValue ) )
                    {
                        return 1;
                    }
                    else if ( ( o1 instanceof IAttribute ) && !( o2 instanceof IAttribute ) )
                    {
                        return -1;
                    }
                    else if ( !( o1 instanceof IAttribute ) && ( o2 instanceof IAttribute ) )
                    {
                        return 1;
                    }
                    else if ( ( o1 instanceof AttributeHierarchy ) && !( o2 instanceof AttributeHierarchy ) )
                    {
                        return -1;
                    }
                    else if ( !( o1 instanceof AttributeHierarchy ) && ( o2 instanceof AttributeHierarchy ) )
                    {
                        return 1;
                    }
                    return 0;
                }
            };
            Arrays.sort( objects, comparator );
            Object obj = objects[0];

            if ( obj instanceof ISearch )
            {
                ISearch search = ( ISearch ) obj;
                exampleSearch = ( ISearch ) search.clone();
                exampleSearch.setName( null );
            }
            else if ( obj instanceof IEntry )
            {
                IEntry entry = ( IEntry ) obj;
                exampleSearch.setBrowserConnection( entry.getBrowserConnection() );
                exampleSearch.setSearchBase( entry.getDn() );
                exampleSearch.setFilter( entry.getChildrenFilter() );
            }
            else if ( obj instanceof ISearchResult )
            {
                ISearchResult searchResult = ( ISearchResult ) obj;
                exampleSearch.setBrowserConnection( searchResult.getEntry().getBrowserConnection() );
                exampleSearch.setSearchBase( searchResult.getEntry().getDn() );
            }
            else if ( obj instanceof IBookmark )
            {
                IBookmark bookmark = ( IBookmark ) obj;
                exampleSearch.setBrowserConnection( bookmark.getBrowserConnection() );
                exampleSearch.setSearchBase( bookmark.getDn() );
            }

            else if ( obj instanceof AttributeHierarchy || obj instanceof IAttribute || obj instanceof IValue )
            {
                IEntry entry = null;
                Set<String> filterSet = new LinkedHashSet<String>();
                for ( int i = 0; i < objects.length; i++ )
                {
                    Object object = objects[i];
                    if ( object instanceof AttributeHierarchy )
                    {
                        AttributeHierarchy ah = ( AttributeHierarchy ) object;
                        for ( IAttribute attribute : ah )
                        {
                            entry = attribute.getEntry();
                            IValue[] values = attribute.getValues();
                            for ( int v = 0; v < values.length; v++ )
                            {
                                filterSet.add( LdapFilterUtils.getFilter( values[v] ) );
                            }
                        }
                    }
                    else if ( object instanceof IAttribute )
                    {
                        IAttribute attribute = ( IAttribute ) object;
                        entry = attribute.getEntry();
                        IValue[] values = attribute.getValues();
                        for ( int v = 0; v < values.length; v++ )
                        {
                            filterSet.add( LdapFilterUtils.getFilter( values[v] ) );
                        }
                    }
                    else if ( object instanceof IValue )
                    {
                        IValue value = ( IValue ) object;
                        entry = value.getAttribute().getEntry();
                        filterSet.add( LdapFilterUtils.getFilter( value ) );
                    }
                }

                exampleSearch.setBrowserConnection( entry.getBrowserConnection() );
                exampleSearch.setSearchBase( entry.getDn() );
                StringBuffer filter = new StringBuffer();
                if ( filterSet.size() > 1 )
                {
                    filter.append( "(&" ); //$NON-NLS-1$
                    for ( Iterator<String> filterIterator = filterSet.iterator(); filterIterator.hasNext(); )
                    {
                        filter.append( filterIterator.next() );
                    }
                    filter.append( ")" ); //$NON-NLS-1$
                }
                else if ( filterSet.size() == 1 )
                {
                    filter.append( filterSet.toArray()[0] );
                }
                else
                {
                    filter.append( ISearch.FILTER_TRUE );
                }
                exampleSearch.setFilter( filter.toString() );
            }
            else if ( obj instanceof Connection )
            {
                IBrowserConnection connection = BrowserCorePlugin.getDefault().getConnectionManager()
                    .getBrowserConnection( ( Connection ) obj );
                exampleSearch.setBrowserConnection( connection );
                if ( connection.getRootDSE().getChildrenCount() > 0 )
                {
                    exampleSearch.setSearchBase( connection.getRootDSE().getChildren()[0].getDn() );
                }
                else
                {
                    exampleSearch.setSearchBase( connection.getRootDSE().getDn() );
                }
            }
            else if ( obj instanceof IBrowserConnection )
            {
                IBrowserConnection connection = ( IBrowserConnection ) obj;
                exampleSearch.setBrowserConnection( connection );
                if ( connection.getRootDSE().getChildrenCount() > 0 )
                {
                    exampleSearch.setSearchBase( connection.getRootDSE().getChildren()[0].getDn() );
                }
                else
                {
                    exampleSearch.setSearchBase( connection.getRootDSE().getDn() );
                }
            }
            else if ( obj instanceof BrowserCategory )
            {
                BrowserCategory cat = ( BrowserCategory ) obj;
                exampleSearch.setBrowserConnection( cat.getParent() );
                if ( cat.getParent().getRootDSE().getChildrenCount() > 0 )
                {
                    exampleSearch.setSearchBase( cat.getParent().getRootDSE().getChildren()[0].getDn() );
                }
                else
                {
                    exampleSearch.setSearchBase( cat.getParent().getRootDSE().getDn() );
                }
            }

        }

        exampleSearch.getSearchParameter().setName( oldName );
        return exampleSearch;
    }


    // ── R2 ISOLATES THE CATEGORY CONTACTS ─────────────────────────────────────
    // R2's sensors pick out the browser category nodes (DIT, Searches,
    // Bookmarks) from the mixed selection — like identifying the base stations
    // among all the ships on the sensor sweep.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link BrowserCategory} objects from the given selection.
     * Browser categories are the top-level grouping nodes in the LDAP browser
     * tree (DIT, Searches, Bookmarks).
     *
     * @param selection  the JFace selection to scan.
     * @return an array of BrowserCategory objects; may be empty but not null.
     */
    public static BrowserCategory[] getBrowserViewCategories( ISelection selection )
    {
        List<Object> list = getTypes( selection, BrowserCategory.class );
        return list.toArray( new BrowserCategory[list.size()] );
    }


    // ── R2 ISOLATES THE VALUE CONTACTS ────────────────────────────────────────
    // R2 picks out the individual attribute values from the scan — the finest-
    // grained LDAP objects, like specific power-cell readings from a single ship.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link IValue} objects from the given selection.
     * An IValue is a single value within a single LDAP attribute.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of IValue objects; may be empty but not null.
     */
    public static IValue[] getValues( ISelection selection )
    {
        List<Object> list = getTypes( selection, IValue.class );
        return list.toArray( new IValue[list.size()] );
    }


    // ── R2 ISOLATES THE ATTRIBUTE CONTACTS ────────────────────────────────────
    // R2 classifies the attribute-level objects — like identifying whole sensor
    // banks rather than individual readings.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link IAttribute} objects from the given selection.
     * An IAttribute represents one named field (e.g., "mail") on an LDAP entry.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of IAttribute objects; may be empty but not null.
     */
    public static IAttribute[] getAttributes( ISelection selection )
    {
        List<Object> list = getTypes( selection, IAttribute.class );
        return list.toArray( new IAttribute[list.size()] );
    }


    // ── R2 ISOLATES THE ATTRIBUTE HIERARCHY CONTACTS ──────────────────────────
    // Attribute hierarchies group related attributes — R2 picks out the grouped
    // sensor cluster contacts from the noise.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link AttributeHierarchy} objects from the given selection.
     * An AttributeHierarchy groups an attribute with its sub-type variants
     * as defined by the LDAP schema.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of AttributeHierarchy objects; may be empty but not null.
     */
    public static AttributeHierarchy[] getAttributeHierarchie( ISelection selection )
    {
        List<Object> list = getTypes( selection, AttributeHierarchy.class );
        return list.toArray( new AttributeHierarchy[list.size()] );
    }


    // ── R2 ISOLATES THE STRING PROPERTY CONTACTS ─────────────────────────────
    // Sometimes the selection contains plain String objects — property page IDs.
    // R2 filters those out separately from the richer model objects.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link String} objects from the given selection.
     * Used by the properties action to find property page IDs in the selection.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of Strings; may be empty but not null.
     */
    public static String[] getProperties( ISelection selection )
    {
        List<Object> list = getTypes( selection, String.class );
        return list.toArray( new String[list.size()] );
    }


    // ── R2 ISOLATES THE SCHEMA ATTRIBUTE TYPE CONTACTS ────────────────────────
    // AttributeType objects come from the schema browser — like the technical
    // spec sheets for each type of sensor reading.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link AttributeType} objects from the given selection.
     * AttributeType objects represent LDAP schema definitions for attribute types,
     * selected in the schema browser view.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of AttributeType objects; may be empty but not null.
     */
    public static AttributeType[] getAttributeTypeDescription( ISelection selection )
    {
        List<Object> list = getTypes( selection, AttributeType.class );
        return list.toArray( new AttributeType[list.size()] );
    }


    // ── R2 ISOLATES THE ENTRY CONTACTS ────────────────────────────────────────
    // Entries are the main directory objects — the ships themselves on the
    // sector map. R2 pulls them out from the mixed selection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link IEntry} objects from the given selection.
     * An IEntry is a single node in the LDAP directory tree, identified
     * by its DN.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of IEntry objects; may be empty but not null.
     */
    public static IEntry[] getEntries( ISelection selection )
    {
        List<Object> list = getTypes( selection, IEntry.class );
        return list.toArray( new IEntry[list.size()] );
    }


    // ── R2 ISOLATES THE BOOKMARK CONTACTS ─────────────────────────────────────
    // Bookmarks are saved coordinates — named shortcuts to entries the user
    // cares about. R2 filters them into their own category.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link IBookmark} objects from the given selection.
     * Bookmarks are named shortcuts to specific LDAP entries.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of IBookmark objects; may be empty but not null.
     */
    public static IBookmark[] getBookmarks( ISelection selection )
    {
        List<Object> list = getTypes( selection, IBookmark.class );
        return list.toArray( new IBookmark[list.size()] );
    }


    // ── R2 ISOLATES THE SEARCH RESULT CONTACTS ────────────────────────────────
    // Search results are entries that came back from a search operation —
    // like ships identified by a specific scan. R2 classifies them separately.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link ISearchResult} objects from the given selection.
     * An ISearchResult wraps an IEntry that was returned by a specific
     * LDAP search operation.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of ISearchResult objects; may be empty but not null.
     */
    public static ISearchResult[] getSearchResults( ISelection selection )
    {
        List<Object> list = getTypes( selection, ISearchResult.class );
        return list.toArray( new ISearchResult[list.size()] );
    }


    // ── R2'S CORE CLASSIFICATION ALGORITHM ────────────────────────────────────
    // This is R2's underlying sensor logic: given a selection and a type class,
    // it walks every object in the structured selection and keeps only those
    // that are instances of the requested type. Simple, fast, and reused by
    // every public "get" method above.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Generic helper that filters a structured selection to objects of the
     * given type. Returns an empty list if the selection is not structured.
     * Used internally by all the public extraction methods.
     *
     * @param selection  the JFace selection to filter.
     * @param type       the Class to filter for — only instances of this class
     *                   are included in the result.
     * @return a list of objects from the selection that are instances of {@code type};
     *         never null, may be empty.
     */
    private static List<Object> getTypes( ISelection selection, Class<?> type )
    {
        List<Object> list = new ArrayList<Object>();

        if ( selection instanceof IStructuredSelection )
        {
            IStructuredSelection structuredSelection = ( IStructuredSelection ) selection;

            for ( Object element : structuredSelection.toArray() )
            {
                if ( type.isInstance( element ) )
                {
                    list.add( element );
                }
            }
        }

        return list;
    }


    // ── R2 ISOLATES THE SAVED SEARCH CONTACTS ─────────────────────────────────
    // Saved searches are like standing patrol orders stored in the nav computer
    // — distinct from the results those patrols return.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link ISearch} objects from the given selection.
     * An ISearch is a saved LDAP search definition (not its results).
     *
     * @param selection  the JFace selection to scan.
     * @return an array of ISearch objects; may be empty but not null.
     */
    public static ISearch[] getSearches( ISelection selection )
    {
        List<Object> list = getTypes( selection, ISearch.class );
        return list.toArray( new ISearch[list.size()] );
    }


    // ── R2 ISOLATES THE ENTRY PAGE CONTACTS ───────────────────────────────────
    // Entry pages are the paging nodes shown when a parent entry has too many
    // children to display at once — like fleet page dividers on the sector map.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link BrowserEntryPage} objects from the given selection.
     * These are virtual paging nodes shown in the browser tree when an entry
     * has more children than the folding threshold allows.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of BrowserEntryPage objects; may be empty but not null.
     */
    public static BrowserEntryPage[] getBrowserEntryPages( ISelection selection )
    {
        List<Object> list = getTypes( selection, BrowserEntryPage.class );
        return list.toArray( new BrowserEntryPage[list.size()] );
    }


    // ── R2 ISOLATES THE SEARCH RESULT PAGE CONTACTS ───────────────────────────
    // Same paging concept for search results — R2 identifies the search-result
    // page dividers in the sensor sweep.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all {@link BrowserSearchResultPage} objects from the given selection.
     * These are virtual paging nodes shown in the browser tree when a search
     * returns more results than the folding threshold.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of BrowserSearchResultPage objects; may be empty but not null.
     */
    public static BrowserSearchResultPage[] getBrowserSearchResultPages( ISelection selection )
    {
        List<Object> list = getTypes( selection, BrowserSearchResultPage.class );
        return list.toArray( new BrowserSearchResultPage[list.size()] );
    }


    // ── R2 RETURNS THE FULL UNCLASSIFIED CONTACT LIST ─────────────────────────
    // Sometimes the caller just wants everything R2 found — no classification,
    // just the raw list of whatever's in the selection.
    // ──────────────────────────────────────────────────────────────────────────
    /**
     * Extracts all objects from the given selection without type filtering.
     * Returns every element in the structured selection as an Object array.
     * Useful when the caller needs to handle all types uniformly.
     *
     * @param selection  the JFace selection to scan.
     * @return an array of all selected objects; may be empty but not null.
     */
    public static Object[] getObjects( ISelection selection )
    {
        List<Object> list = getTypes( selection, Object.class );
        return list.toArray( new Object[list.size()] );
    }
}
