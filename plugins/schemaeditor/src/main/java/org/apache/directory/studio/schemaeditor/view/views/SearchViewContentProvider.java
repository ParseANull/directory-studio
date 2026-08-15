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
package org.apache.directory.studio.schemaeditor.view.views;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.studio.schemaeditor.Activator;
import org.apache.directory.studio.schemaeditor.PluginConstants;
import org.apache.directory.studio.schemaeditor.view.wrappers.TreeNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


// ── CLASS: SearchViewContentProvider — Lando Running Cloud City ───────────────
// Lando Calrissian runs Cloud City with an iron hand in a velvet glove. The city
// has miners, merchants, tourists, and administrators — all sorts of different people
// — and Lando organizes them into sectors, decides who gets priority, determines
// the display order when they show up at the gate. He doesn't care what they do
// individually; he cares about arrangement, grouping, and sequence.
// This content provider does the same with search results: it takes a raw, mixed
// list of AttributeTypes and ObjectClasses, groups them by type if that's what
// the user wants (ATs first, OCs first, or mixed), sorts within each group by
// name or OID, and optionally reverses the order — all before the table renders
// a single row. Lando would approve.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Supplies an organized list of schema objects to the Search View's table viewer.
 * The input is a raw {@code List<SchemaObject>} from the search engine. We apply
 * user-configured grouping (attribute types first, object classes first, or mixed)
 * and sorting (by first name or OID, ascending or descending) before returning the
 * final ordered array to the viewer. The two sort comparators are built once in the
 * constructor and reused on every call. Think of it as Lando managing Cloud City's
 * diverse inhabitants: grouping them by sector, deciding the display order, keeping
 * the whole operation running smoothly.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
{
    /** The preferences store */
    private IPreferenceStore store;

    /** The FirstName Sorter */
    private Comparator<SchemaObject> firstNameSorter;

    /** The OID Sorter */
    private Comparator<SchemaObject> oidSorter;


    // ── Lando Sets Up the City's Administration ───────────────────────────────
    // Before Cloud City can function, Lando builds the organizational structures:
    // the rules for how residents are grouped, the protocols for sorting people
    // into the right sector. We build the two comparators here — firstNameSorter
    // and oidSorter — so they're ready whenever getChildren is called.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new content provider and builds both sort comparators.
     * We construct the {@code firstNameSorter} (compares by first alias name, case-insensitively)
     * and {@code oidSorter} (compares by OID, case-insensitively) as anonymous classes here.
     * Both handle all four combinations of AttributeType vs. ObjectClass comparisons.
     * We also load the preference store so grouping and sort preferences are available
     * on every {@link #getChildren(Object)} call.
     *
     * <p>For example — Lando establishes city protocols:</p>
     * <pre>
     *   new SearchViewContentProvider()
     *   → builds firstNameSorter: "cn" before "sn" before "top"
     *   → builds oidSorter: "1.2.3" before "2.5.4.3"
     *   → loads preference store
     * </pre>
     */
    public SearchViewContentProvider()
    {
        store = Activator.getDefault().getPreferenceStore();

        firstNameSorter = new Comparator<SchemaObject>()
        {
            public int compare( SchemaObject o1, SchemaObject o2 )
            {
                List<String> o1Names = null;
                List<String> o2Names = null;

                if ( ( o1 instanceof AttributeType ) && ( o2 instanceof AttributeType ) )
                {
                    AttributeType at1 = ( AttributeType ) o1;
                    AttributeType at2 = ( AttributeType ) o2;

                    o1Names = at1.getNames();
                    o2Names = at2.getNames();
                }
                else if ( ( o1 instanceof ObjectClass ) && ( o2 instanceof ObjectClass ) )
                {
                    ObjectClass oc1 = ( ObjectClass ) o1;
                    ObjectClass oc2 = ( ObjectClass ) o2;

                    o1Names = oc1.getNames();
                    o2Names = oc2.getNames();
                }
                else if ( ( o1 instanceof AttributeType ) && ( o2 instanceof ObjectClass ) )
                {
                    AttributeType at = ( AttributeType ) o1;
                    ObjectClass oc = ( ObjectClass ) o2;

                    o1Names = at.getNames();
                    o2Names = oc.getNames();
                }
                else if ( ( o1 instanceof ObjectClass ) && ( o2 instanceof AttributeType ) )
                {
                    ObjectClass oc = ( ObjectClass ) o1;
                    AttributeType at = ( AttributeType ) o2;

                    o1Names = oc.getNames();
                    o2Names = at.getNames();
                }

                // Comparing the First Name
                if ( ( o1Names != null ) && ( o2Names != null ) )
                {
                    if ( ( o1Names.size() > 0 ) && ( o2Names.size() > 0 ) )
                    {
                        return o1Names.get( 0 ).compareToIgnoreCase( o2Names.get( 0 ) );
                    }
                    else if ( ( o1Names.size() == 0 ) && ( o2Names.size() > 0 ) )
                    {
                        return "".compareToIgnoreCase( o2Names.get( 0 ) ); //$NON-NLS-1$
                    }
                    else if ( ( o1Names.size() > 0 ) && ( o2Names.size() == 0 ) )
                    {
                        return o1Names.get( 0 ).compareToIgnoreCase( "" ); //$NON-NLS-1$
                    }
                }

                // Default
                return o1.toString().compareToIgnoreCase( o2.toString() );
            }
        };

        oidSorter = new Comparator<SchemaObject>()
        {
            public int compare( SchemaObject o1, SchemaObject o2 )
            {
                if ( ( o1 instanceof AttributeType ) && ( o2 instanceof AttributeType ) )
                {
                    AttributeType at1 = ( AttributeType ) o1;
                    AttributeType at2 = ( AttributeType ) o2;

                    return at1.getOid().compareToIgnoreCase( at2.getOid() );
                }
                else if ( ( o1 instanceof ObjectClass ) && ( o2 instanceof ObjectClass ) )
                {
                    ObjectClass oc1 = ( ObjectClass ) o1;
                    ObjectClass oc2 = ( ObjectClass ) o2;

                    return oc1.getOid().compareToIgnoreCase( oc2.getOid() );
                }
                else if ( ( o1 instanceof AttributeType ) && ( o2 instanceof ObjectClass ) )
                {
                    AttributeType at = ( AttributeType ) o1;
                    ObjectClass oc = ( ObjectClass ) o2;

                    return at.getOid().compareToIgnoreCase( oc.getOid() );
                }
                else if ( ( o1 instanceof ObjectClass ) && ( o2 instanceof AttributeType ) )
                {
                    ObjectClass oc = ( ObjectClass ) o1;
                    AttributeType at = ( AttributeType ) o2;

                    return oc.getOid().compareToIgnoreCase( at.getOid() );
                }

                // Default
                return o1.toString().compareToIgnoreCase( o2.toString() );
            }
        };
    }


    // ── Lando Reviews the Resident Manifest ──────────────────────────────────
    // The city's administration doesn't distinguish between "reviewing the manifest"
    // and "viewing individual residents" — the protocol is the same either way.
    // getElements delegates to getChildren because root and child logic are identical.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the elements to display in the Search View table.
     * Delegates directly to {@link #getChildren(Object)} since there's no separate
     * root-vs-children distinction for a flat list.
     *
     * @param inputElement  a {@code List<SchemaObject>} of search results
     * @return              the sorted, grouped array of {@link SchemaObject} instances
     */
    public Object[] getElements( Object inputElement )
    {
        return getChildren( inputElement );
    }


    // ── Lando Steps Away from the Console ────────────────────────────────────
    // Cloud City keeps running when Lando steps away — there's nothing here that
    // needs to be explicitly shut down. dispose() is a required no-op for the
    // IContentProvider contract.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called when this content provider is being released. Nothing to clean up here.
     */
    public void dispose()
    {
        // Nothing to do
    }


    // ── The City Adapts to a New Manifest ────────────────────────────────────
    // When a new cargo manifest arrives, Lando doesn't need to rebuild the whole
    // city — he just processes the new list when it arrives. Since we re-sort
    // on every getChildren call, we don't cache anything here.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Called when the viewer's input changes. Since we re-sort the list on every
     * {@link #getChildren(Object)} call, we have nothing to cache or clean up here.
     *
     * @param viewer    the viewer whose input changed
     * @param oldInput  the previous list of results
     * @param newInput  the new list of results
     */
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
    {
        // Nothing to do
    }


    // ── Lando Sorts the Residents into Their Sectors ─────────────────────────
    // Cloud City's sectors are organized by function: mining on level 3, tourism
    // on the upper deck, administration in the center. Lando decides the order.
    // getChildren reads the grouping preference (ATs first, OCs first, or mixed),
    // splits the raw result list accordingly, sorts within each group, optionally
    // reverses, and returns the final ordered list.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the search results in the user's preferred grouping and sort order.
     * We read three preferences: grouping mode (attribute types first, object classes
     * first, or mixed), sort field (first name or OID), and sort direction. Then we
     * partition and sort the raw {@code List<SchemaObject>} accordingly.
     *
     * <p>For example — Lando assigns sectors:</p>
     * <pre>
     *   grouping = AT_FIRST, sortBy = FIRSTNAME, order = ASCENDING
     *   input = [person(OC), cn(AT), sn(AT), inetOrgPerson(OC)]
     *   → [cn(AT), sn(AT), inetOrgPerson(OC), person(OC)]
     * </pre>
     *
     * @param parentElement  a {@code List<SchemaObject>} of raw search results
     * @return               the sorted, grouped array ready for the table to render
     */
    @SuppressWarnings("unchecked")
    public Object[] getChildren( Object parentElement )
    {
        List<SchemaObject> children = new ArrayList<SchemaObject>();

        int group = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_GROUPING );
        int sortBy = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY );
        int sortOrder = store.getInt( PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER );

        if ( parentElement instanceof List )
        {
            List<SchemaObject> searchResults = ( List<SchemaObject> ) parentElement;

            if ( group == PluginConstants.PREFS_SEARCH_VIEW_GROUPING_ATTRIBUTE_TYPES_FIRST )
            {
                List<AttributeType> attributeTypes = new ArrayList<AttributeType>();
                List<ObjectClass> objectClasses = new ArrayList<ObjectClass>();

                for ( SchemaObject searchResult : searchResults )
                {
                    if ( searchResult instanceof AttributeType )
                    {
                        attributeTypes.add( ( AttributeType ) searchResult );
                    }
                    else if ( searchResult instanceof ObjectClass )
                    {
                        objectClasses.add( ( ObjectClass ) searchResult );
                    }
                }

                // Sort by
                if ( sortBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_FIRSTNAME )
                {
                    Collections.sort( attributeTypes, firstNameSorter );
                    Collections.sort( objectClasses, firstNameSorter );
                }
                else if ( sortBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_OID )
                {
                    Collections.sort( attributeTypes, oidSorter );
                    Collections.sort( objectClasses, oidSorter );
                }

                // Sort Order
                if ( sortOrder == PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_DESCENDING )
                {
                    Collections.reverse( attributeTypes );
                    Collections.reverse( objectClasses );
                }

                children.addAll( attributeTypes );
                children.addAll( objectClasses );
            }
            else if ( group == PluginConstants.PREFS_SEARCH_VIEW_GROUPING_OBJECT_CLASSES_FIRST )
            {
                List<AttributeType> attributeTypes = new ArrayList<AttributeType>();
                List<ObjectClass> objectClasses = new ArrayList<ObjectClass>();

                for ( SchemaObject searchResult : searchResults )
                {
                    if ( searchResult instanceof AttributeType )
                    {
                        attributeTypes.add( ( AttributeType ) searchResult );
                    }
                    else if ( searchResult instanceof ObjectClass )
                    {
                        objectClasses.add( ( ObjectClass ) searchResult );
                    }
                }

                // Sort by
                if ( sortBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_FIRSTNAME )
                {
                    Collections.sort( attributeTypes, firstNameSorter );
                    Collections.sort( objectClasses, firstNameSorter );
                }
                else if ( sortBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_OID )
                {
                    Collections.sort( attributeTypes, oidSorter );
                    Collections.sort( objectClasses, oidSorter );
                }

                // Sort Order
                if ( sortOrder == PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_DESCENDING )
                {
                    Collections.reverse( attributeTypes );
                    Collections.reverse( objectClasses );
                }

                children.addAll( objectClasses );
                children.addAll( attributeTypes );
            }
            else if ( group == PluginConstants.PREFS_SEARCH_VIEW_GROUPING_MIXED )
            {
                children.addAll( searchResults );

                // Sort by
                if ( sortBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_FIRSTNAME )
                {
                    Collections.sort( children, firstNameSorter );
                }
                else if ( sortBy == PluginConstants.PREFS_SEARCH_VIEW_SORTING_BY_OID )
                {
                    Collections.sort( children, oidSorter );
                }

                // Sort Order
                if ( sortOrder == PluginConstants.PREFS_SEARCH_VIEW_SORTING_ORDER_DESCENDING )
                {
                    Collections.reverse( children );
                }
            }
        }

        return children.toArray();
    }


    // ── Trace a Resident Back to Their Sector ────────────────────────────────
    // Every resident of Cloud City belongs to a sector — Lando can trace any
    // individual back to where they belong. getParent does the same for tree
    // nodes — though in our flat table context this is rarely called with a
    // useful value.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns the parent of the given element.
     * Schema objects in the search results are flat (no parent-child nesting),
     * so this returns the parent only if the element is a {@link TreeNode} wrapper,
     * and {@code null} otherwise.
     *
     * @param element  the element whose parent to find
     * @return         the parent node, or {@code null}
     */
    public Object getParent( Object element )
    {

        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).getParent();
        }

        // Default
        return null;
    }


    // ── Does This Resident Run a Department? ─────────────────────────────────
    // Most residents of Cloud City don't have subordinates. Search results are
    // flat schema objects — no child nodes. hasChildren lets JFace know not to
    // draw expand arrows on result rows.
    // ────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether the given element has children.
     * Search results are flat schema objects, so this is almost always {@code false}.
     * JFace uses this to decide whether to draw an expand arrow on a row.
     *
     * @param element  the element to test
     * @return         {@code true} only if this is a {@link TreeNode} with children
     */
    public boolean hasChildren( Object element )
    {
        if ( element instanceof TreeNode )
        {
            return ( ( TreeNode ) element ).hasChildren();
        }

        // Default
        return false;
    }
}
