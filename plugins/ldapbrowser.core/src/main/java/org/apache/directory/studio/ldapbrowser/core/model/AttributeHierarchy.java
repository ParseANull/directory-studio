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

package org.apache.directory.studio.ldapbrowser.core.model;


import java.util.Arrays;
import java.util.Iterator;


// ── CLASS: AttributeHierarchy — LANDO RUNNING A CLOUD CITY DEPARTMENT ────────
// Lando runs Cloud City by managing a web of departments that all ultimately
// report to the same top-level function — life support, say.  When you ask
// "show me life support", Lando doesn't hand you a single panel; he hands you
// the whole department: the main panel and all the sub-consoles that fall under
// it.  Each is a real, staffed station, but they all belong to the same chain
// of command.
// An AttributeHierarchy does the same thing: it groups a top-level attribute
// description ("name") with all the concrete sub-type attributes that live
// under it in this entry (cn, sn, givenName, ...).  When the UI or an export
// routine asks for "name", we hand it the full hierarchy so it sees every
// concrete value.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A container that groups an attribute description (e.g. {@code name}) with all
 * of its sub-type {@link IAttribute} instances present on an entry
 * (e.g. {@code cn}, {@code sn}, {@code givenName}).
 *
 * <p>Example:</p>
 * <ul>
 *   <li>attributeDescription: {@code name}</li>
 *   <li>attributes: {@code cn:test1}, {@code sn:test2}, {@code givenName:test3}</li>
 * </ul>
 *
 * <p>Think of this as Lando's department roster: one function name maps to
 * many individual stations, all iterable and countable as a unit.</p>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeHierarchy implements Iterable<IAttribute>
{

    /** The entry */
    private IEntry entry;

    /** The attribute description */
    private String attributeDescription;

    /** The attributes */
    private IAttribute[] attributes;


    // ── Lando Opens A New Department Roster ───────────────────────────────────────
    // "Null entry?  Null description?  Empty staff list?  I'm not running that
    // department — those are the rules."  All three are required.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new instance of AttributeHierarchy.
     *
     * @param entry                 the entry this hierarchy belongs to; must not be {@code null}.
     * @param attributeDescription  the top-level attribute description string; must not be {@code null}.
     * @param attributes            the concrete sub-type attributes; must not be {@code null} or empty.
     * @throws IllegalArgumentException if any argument is {@code null} or the attributes array is empty.
     */
    public AttributeHierarchy( IEntry entry, String attributeDescription, IAttribute[] attributes )
    {
        if ( entry == null || attributeDescription == null || attributes == null || attributes.length < 1
            || attributes[0] == null )
        {
            throw new IllegalArgumentException( "Empty AttributeHierachie" ); //$NON-NLS-1$
        }
        this.entry = entry;
        this.attributeDescription = attributeDescription;
        this.attributes = attributes;
    }


    // ── Lando Lists All The Department Stations ───────────────────────────────────
    // "Here's the full staff list: cn-panel, sn-panel, givenName-panel."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the attributes.
     *
     * @return the attributes
     */
    public IAttribute[] getAttributes()
    {
        return attributes;
    }


    // ── Lando Checks Whether A Specific Station Is In This Department ────────────
    // "Is the sn-panel one of mine?  Let me check the roster."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether the given attribute is contained
     * in this attribute hierarchy.
     *
     * @param attribute the attribute to check
     * @return {@code true} if the attribute is in this hierarchy
     */
    public boolean contains( IAttribute attribute )
    {
        return Arrays.asList( attributes ).contains( attribute );
    }


    // ── Lando Hands Out A Roster Tour ─────────────────────────────────────────────
    // "Walk through each station in the department, one by one."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Returns an iterator over the elements of this attribute hierarchy.
     *
     * @return an iterator over the elements of this attribute hierarchy
     */
    public Iterator<IAttribute> iterator()
    {
        return Arrays.asList( attributes ).iterator();
    }


    // ── Lando Points To The Head Of The Department ───────────────────────────────
    // "The primary station — the first attribute in the array."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the first attribute.
     *
     * @return the first attribute
     */
    public IAttribute getAttribute()
    {
        return attributes[0];
    }


    // ── Lando Counts How Many Stations Are Running ───────────────────────────────
    // "We have three active panels in this department."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the number of attributes.
     *
     * @return the number of attributes
     */
    public int size()
    {
        return attributes.length;
    }


    // ── Lando Tallies The Total Crew Across All Stations ─────────────────────────
    // "Total values across all sub-type attributes in this department."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the number of all values in all attributes.
     *
     * @return the number of all values in all attributes
     */
    public int getValueSize()
    {
        int size = 0;
        for ( IAttribute attribute : attributes )
        {
            size += attribute.getValueSize();
        }
        return size;
    }


    // ── Lando Reads The Department Function Name ──────────────────────────────────
    // "The top-level function this department serves: 'name'."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the attribute description.
     *
     * @return the attribute description
     */
    public String getAttributeDescription()
    {
        return attributeDescription;
    }


    // ── Lando Identifies Which Entry The Department Belongs To ───────────────────
    // "This department is part of dc=empire,dc=net."
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Gets the entry.
     *
     * @return the entry
     */
    public IEntry getEntry()
    {
        return entry;
    }

}
