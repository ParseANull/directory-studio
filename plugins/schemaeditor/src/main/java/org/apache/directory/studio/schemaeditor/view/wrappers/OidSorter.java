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

package org.apache.directory.studio.schemaeditor.view.wrappers;


import java.util.Comparator;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;


// ── CLASS: OidSorter — R2-D2 Plugging Into the Death Star Computer ────────────
// In the Death Star detention level, R2-D2 jacks his data probe directly into
// a computer terminal — not reading names or labels, but querying the raw numeric
// identifiers stored in the system: cell block numbers, corridor codes, blast-door
// sequences. Everything is a number, sorted by numeric key, pulled from the
// underlying data store precisely and without guesswork.
// OidSorter works the same way: it bypasses human-readable names entirely and
// compares two schema elements by their Object Identifier (OID) — a dot-separated
// numeric string like "2.5.4.3" — sorting them into strict ascending order.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sorts {@link TreeNode} objects in ascending order by the OID of the schema element they wrap.
 * OIDs (Object Identifiers) are the canonical, numeric identifiers for LDAP schema elements —
 * unique across all LDAP implementations in the world. Sorting by OID gives a stable,
 * implementation-independent order that is more precise than sorting by name.
 * Think of R2 querying raw numeric codes in the Death Star terminal: fast, precise,
 * no ambiguity between names, just the numbers in order.
 */
public class OidSorter implements Comparator<TreeNode>
{
    // ── R2 Queries the Terminal and Orders Results by Numeric Code ────────────
    // R2 jacks in, issues the query, and the terminal returns a list of cell-block
    // numbers and door codes — all raw numeric identifiers, sorted ascending.
    // compare() does the same: unpack the OID from each wrapper and compare lexicographically.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link TreeNode} objects by the OID of the schema element each wraps.
     * Handles all four combinations of {@link AttributeTypeWrapper} and {@link ObjectClassWrapper}.
     * The comparison is case-insensitive, which matters for OIDs that contain letters
     * (though in practice LDAP OIDs are purely numeric with dot separators).
     * Falls back to {@code toString()} comparison if neither node is a recognised wrapper type.
     *
     * <p>For example — R2 pulling corridor codes from the Death Star terminal:</p>
     * <pre>
     *   sorter.compare( cnWrapper, snWrapper );
     *   // Compares "2.5.4.3" vs "2.5.4.4" — cn comes first numerically
     *   sorter.compare( personWrapper, cnWrapper );
     *   // Compares "2.5.6.6" vs "2.5.4.3" — cn comes first
     * </pre>
     *
     * @param o1  the first tree node; must wrap an {@link AttributeType} or {@link ObjectClass}
     * @param o2  the second tree node; must wrap an {@link AttributeType} or {@link ObjectClass}
     * @return    a negative integer if {@code o1} sorts before {@code o2}, zero if equal,
     *            positive if {@code o1} sorts after {@code o2}
     */
    public int compare( TreeNode o1, TreeNode o2 )
    {
        if ( ( o1 instanceof AttributeTypeWrapper ) && ( o2 instanceof AttributeTypeWrapper ) )
        {
            AttributeType at1 = ( ( AttributeTypeWrapper ) o1 ).getAttributeType();
            AttributeType at2 = ( ( AttributeTypeWrapper ) o2 ).getAttributeType();

            return at1.getOid().compareToIgnoreCase( at2.getOid() );
        }
        else if ( ( o1 instanceof ObjectClassWrapper ) && ( o2 instanceof ObjectClassWrapper ) )
        {
            ObjectClass oc1 = ( ( ObjectClassWrapper ) o1 ).getObjectClass();
            ObjectClass oc2 = ( ( ObjectClassWrapper ) o2 ).getObjectClass();

            return oc1.getOid().compareToIgnoreCase( oc2.getOid() );
        }
        else if ( ( o1 instanceof AttributeTypeWrapper ) && ( o2 instanceof ObjectClassWrapper ) )
        {
            AttributeType at = ( ( AttributeTypeWrapper ) o1 ).getAttributeType();
            ObjectClass oc = ( ( ObjectClassWrapper ) o2 ).getObjectClass();

            return at.getOid().compareToIgnoreCase( oc.getOid() );
        }
        else if ( ( o1 instanceof ObjectClassWrapper ) && ( o2 instanceof AttributeTypeWrapper ) )
        {
            ObjectClass oc = ( ( ObjectClassWrapper ) o1 ).getObjectClass();
            AttributeType at = ( ( AttributeTypeWrapper ) o2 ).getAttributeType();

            return oc.getOid().compareToIgnoreCase( at.getOid() );
        }

        // Default
        return o1.toString().compareToIgnoreCase( o2.toString() );
    }
}
