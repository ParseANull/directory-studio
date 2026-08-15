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

package org.apache.directory.studio.ldapbrowser.core.utils;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;


// ── CLASS: AttributeComparator — LANDO SORTING THE CLOUD CITY CARGO MANIFEST ─
// Lando runs Cloud City with military precision: objectClass first, must-attributes
// next, then user attributes alphabetically, and operational attributes at the end.
// AttributeComparator embodies that ordering: it sorts IAttribute and IValue objects
// according to configurable sortBy and sortOrder preferences.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * {@link Comparator} for {@link IAttribute} and {@link IValue} objects.
 * Sorts by attribute description or value content, with objectClass and
 * mandatory attributes first, operational attributes last, and configurable
 * ascending/descending order.
 *
 * <p>Think of this as Lando sorting Cloud City's cargo manifest: critical
 * cargo first, routine cargo by name, and maintenance items last.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeComparator implements Comparator<Object>
{
    private final int sortBy;
    private final int defaultSortBy;
    private final int sortOrder;
    private final int defaultSortOrder;
    private final boolean objectClassAndMustAttributesFirst;
    private final boolean operationalAttributesLast;


    // ── Lando Initialises The Sorter With Sensible Defaults ──────────────────────
    // Without explicit configuration, Lando applies the default sort policy:
    // no explicit sort key (fall back to attribute description), ascending order,
    // objectClass and must-attributes first, and operational attributes last.
    // This no-arg constructor is the most common construction path.
    /**
     * Creates a new AttributeComparator with default sort settings:
     * sort by attribute description, ascending, objectClass and must first,
     * operational attributes last.
     */
    public AttributeComparator()
    {
        this.sortBy = BrowserCoreConstants.SORT_BY_NONE;
        this.defaultSortBy =BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION;
        this.sortOrder = BrowserCoreConstants.SORT_ORDER_NONE;
        this.defaultSortOrder = BrowserCoreConstants.SORT_ORDER_ASCENDING;
        this.objectClassAndMustAttributesFirst = true;
        this.operationalAttributesLast = true;
    }


    // ── Lando Configures The Sorter With Explicit Cloud City Policy ───────────────
    // A custom sort policy can be applied when the default ordering is not wanted.
    // sortBy and sortOrder of SORT_BY_NONE / SORT_ORDER_NONE mean "use the default".
    // The boolean flags control whether the natural-order overrides are applied.
    // This constructor is used when the UI preference store provides sort settings.
    /**
     * Creates a new AttributeComparator with explicit sort settings.
     *
     * @param sortBy the primary sort key, or {@link BrowserCoreConstants#SORT_BY_NONE}
     * @param defaultSortBy fallback sort key used when sortBy is SORT_BY_NONE
     * @param sortOrder the sort direction, or {@link BrowserCoreConstants#SORT_ORDER_NONE}
     * @param defaultSortOrder fallback sort direction used when sortOrder is SORT_ORDER_NONE
     * @param objectClassAndMustAttributesFirst whether objectClass and must attributes sort first
     * @param operationalAttributesLast whether operational attributes sort last
     */
    public AttributeComparator( int sortBy, int defaultSortBy, int sortOrder, int defaultSortOrder, boolean objectClassAndMustAttributesFirst,
        boolean operationalAttributesLast )
    {
        this.sortBy = sortBy;
        this.defaultSortBy = defaultSortBy;
        this.sortOrder = sortOrder;
        this.defaultSortOrder = defaultSortOrder;
        this.objectClassAndMustAttributesFirst = objectClassAndMustAttributesFirst;
        this.operationalAttributesLast = operationalAttributesLast;
    }


    // ── Lando Decides The Ordering Of Two Cargo Items On The Manifest ────────────
    // Lando accepts either two IAttribute or two IValue objects.
    // For values, if they share an attribute they are compared by value; otherwise
    // by attribute and then by value, depending on the sort key.
    // Mixing an attribute and a value throws ClassCastException — Han shoots first.
    /**
     * Compares two {@link IAttribute} or two {@link IValue} objects.
     *
     * @param o1 the first object
     * @param o2 the second object
     * @return a negative, zero, or positive integer as o1 is less than, equal to,
     *         or greater than o2
     * @throws ClassCastException if the arguments are not both attributes or both values
     */
    public int compare( Object o1, Object o2 )
    {
        IAttribute attribute1 = null;
        IValue value1 = null;
        if ( o1 instanceof IAttribute )
        {
            attribute1 = ( IAttribute ) o1;
        }
        else if ( o1 instanceof IValue )
        {
            value1 = ( IValue ) o1;
            attribute1 = value1.getAttribute();
        }

        IAttribute attribute2 = null;
        IValue value2 = null;
        if ( o2 instanceof IAttribute )
        {
            attribute2 = ( IAttribute ) o2;
        }
        else if ( o2 instanceof IValue )
        {
            value2 = ( IValue ) o2;
            attribute2 = value2.getAttribute();
        }

        if ( value1 != null && value2 != null )
        {
            if ( getSortByOrDefault() == BrowserCoreConstants.SORT_BY_ATTRIBUTE_DESCRIPTION )
            {
                if ( value1.getAttribute() != value2.getAttribute() )
                {
                    return this.compareAttributes( value1.getAttribute(), value2.getAttribute() );
                }
                else
                {
                    return this.compareValues( value1, value2 );
                }
            }
            else if ( getSortByOrDefault() == BrowserCoreConstants.SORT_BY_VALUE )
            {
                return this.compareValues( value1, value2 );
            }
            else
            {
                return this.equal();
            }
        }
        else if ( attribute1 != null && attribute2 != null )
        {
            return this.compareAttributes( attribute1, attribute2 );
        }
        else
        {
            throw new ClassCastException( "Can only compare two values or two attributes" );
        }
    }


    // ── Lando Sorts Two Attributes According To The Cloud City Manifest Rules ─────
    // When SORT_ORDER_NONE, structural priority rules apply first (objectClass, must,
    // operational); only then is the description string compared case-insensitively.
    // When a real sort order is set, the priority rules are bypassed entirely.
    // Returns lessThan/greaterThan adjusted for ascending or descending order.
    private int compareAttributes( IAttribute attribute1, IAttribute attribute2 )
    {
        if ( this.sortOrder == BrowserCoreConstants.SORT_ORDER_NONE )
        {
            if ( objectClassAndMustAttributesFirst )
            {
                if ( attribute1.isObjectClassAttribute() && !attribute2.isObjectClassAttribute() )
                {
                    return lessThan();
                }
                else if ( attribute2.isObjectClassAttribute() && !attribute1.isObjectClassAttribute() )
                {
                    return greaterThan();
                }

                if ( attribute1.isMustAttribute() && !attribute2.isMustAttribute() )
                {
                    return lessThan();
                }
                else if ( attribute2.isMustAttribute() && !attribute1.isMustAttribute() )
                {
                    return greaterThan();
                }
            }

            if ( operationalAttributesLast )
            {
                if ( attribute1.isOperationalAttribute() && !attribute2.isOperationalAttribute() )
                {
                    return greaterThan();
                }
                else if ( attribute2.isOperationalAttribute() && !attribute1.isOperationalAttribute() )
                {
                    return lessThan();
                }
            }
        }

        return compare( attribute1.getDescription(), attribute2.getDescription() );
    }


    // ── Lando Sorts Two Values Within A Cargo Slot ───────────────────────────────
    // Empty values sort last — a blank slot is less desirable than a filled one.
    // Non-empty values are compared by their string representation.
    // The comparison direction is adjusted by getSortOrderOrDefault.
    // Returns 0 for two empty values (equal in emptiness).
    private int compareValues( IValue value1, IValue value2 )
    {
        if ( value1.isEmpty() && value2.isEmpty() )
        {
            return equal();
        }

        if ( value1.isEmpty() && !value2.isEmpty() )
        {
            return greaterThan();
        }
        if ( !value1.isEmpty() && value2.isEmpty() )
        {
            return lessThan();
        }

        return compare( value1.getStringValue(), value2.getStringValue() );
    }

    /**
     * Gets the current sort by property or the default sort by property (from the preferences).
     */
    private int getSortByOrDefault()
    {
        if ( sortBy == BrowserCoreConstants.SORT_BY_NONE )
        {
            return defaultSortBy;
        }
        else
        {
            return sortBy;
        }
    }

    /**
     * Gets the current sort order or the default sort order (from the preferences).
     */
    private int getSortOrderOrDefault()
    {
        if ( sortOrder == BrowserCoreConstants.SORT_ORDER_NONE )
        {
            return defaultSortOrder;
        }
        else
        {
            return sortOrder;
        }
    }

    private int lessThan()
    {
        return getSortOrderOrDefault() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? -1 : 1;
    }


    private int equal()
    {
        return 0;
    }


    private int greaterThan()
    {
        return getSortOrderOrDefault() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? 1 : -1;
    }


    /**
     * Compares the two strings using the Strings's compareToIgnoreCase method, 
     * pays attention for the sort order.
     *
     * @param s1 the first string to compare
     * @param s2 the second string to compare
     * @return a negative integer, zero, or a positive integer
     * @see java.lang.String#compareToIgnoreCase(String)
     */
    private int compare( String s1, String s2 )
    {
        return getSortOrderOrDefault() == BrowserCoreConstants.SORT_ORDER_ASCENDING ? s1.compareToIgnoreCase( s2 )
            : s2.compareToIgnoreCase( s1 );
    }


    // ── Lando Flattens All Entry Values Into A Sorted Manifest ───────────────────
    // Given an entry, R2-D2 streams all its attribute values into a flat list
    // and sorts them using the default AttributeComparator settings.
    // This static helper is a convenience for callers that need a display-ready list.
    // Returns an empty list if the entry has no attributes.
    /**
     * Returns all values of the given entry as a flat sorted list, using default
     * sort settings (attribute description, ascending, objectClass first).
     *
     * @param entry the entry
     * @return the sorted list of all values
     */
    public static List<IValue> toSortedValues( IEntry entry )
    {
        return Arrays.stream( entry.getAttributes() ).flatMap( a -> Arrays.stream( a.getValues() ) )
            .sorted( new AttributeComparator() )
            .collect( Collectors.toList() );
    }

}
