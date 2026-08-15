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
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;


// ── CLASS: FirstNameSorter — Mace Windu Confronting Palpatine in His Office ──
// In Palpatine's office, Mace Windu stands across from the Chancellor and makes
// a decisive judgement call — "I have fought many times, Chancellor, and always
// on the side of the Republic." He sizes up his opponent and, without hesitation,
// determines who stands where in the order of things.
// FirstNameSorter does the same: it looks at two tree nodes, extracts the first
// name from each schema element, and makes a definitive judgement about which one
// comes before the other alphabetically — no ambiguity, no deferral.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sorts {@link TreeNode} objects in ascending alphabetical order by the first name
 * of the schema element they wrap.
 * LDAP schema elements (attribute types and object classes) can have multiple names,
 * but for display purposes we sort by the first name in the list — the "primary" label.
 * This comparator is case-insensitive, so "cn" and "CN" are treated as equivalent.
 * Think of Mace Windu sizing up his opponent: a quick, firm judgement of who stands
 * first in the order, with no second-guessing.
 */
public class FirstNameSorter implements Comparator<TreeNode>
{
    // ── Mace Windu Sizes Up the Two Opponents and Declares a Verdict ─────────
    // In Palpatine's office, Mace looks from one face to the other — assessing
    // status, rank, name — and determines the order instantly: this one stands
    // to my right, that one to my left. Neither one is left without a position.
    // compare() does the same for two tree nodes: extract names, compare first names.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link TreeNode} objects by the first name of the schema element each wraps.
     * Both nodes must wrap the same type (both {@link AttributeTypeWrapper} or both
     * {@link ObjectClassWrapper}), or one of each — we handle all four combinations.
     * Falls back to {@code toString()} comparison if neither node is a recognised wrapper type.
     *
     * <p>For example — Mace Windu ordering the two figures in Palpatine's office:</p>
     * <pre>
     *   sorter.compare( cnWrapper, snWrapper );
     *   // Returns negative: "cn" comes before "sn" alphabetically
     *   sorter.compare( personWrapper, cnWrapper );
     *   // Mixed types: compares "person" vs "cn" — "cn" wins alphabetically
     * </pre>
     *
     * @param o1  the first tree node to compare; must wrap an {@link AttributeType} or {@link ObjectClass}
     * @param o2  the second tree node to compare; must wrap an {@link AttributeType} or {@link ObjectClass}
     * @return    a negative integer if {@code o1} sorts before {@code o2}, zero if equal,
     *            positive if {@code o1} sorts after {@code o2}
     */
    public int compare( TreeNode o1, TreeNode o2 )
    {
        List<String> o1Names = null;
        List<String> o2Names = null;

        if ( ( o1 instanceof AttributeTypeWrapper ) && ( o2 instanceof AttributeTypeWrapper ) )
        {
            AttributeType at1 = ( ( AttributeTypeWrapper ) o1 ).getAttributeType();
            AttributeType at2 = ( ( AttributeTypeWrapper ) o2 ).getAttributeType();

            o1Names = at1.getNames();
            o2Names = at2.getNames();
        }
        else if ( ( o1 instanceof ObjectClassWrapper ) && ( o2 instanceof ObjectClassWrapper ) )
        {
            ObjectClass oc1 = ( ( ObjectClassWrapper ) o1 ).getObjectClass();
            ObjectClass oc2 = ( ( ObjectClassWrapper ) o2 ).getObjectClass();

            o1Names = oc1.getNames();
            o2Names = oc2.getNames();
        }
        else if ( ( o1 instanceof AttributeTypeWrapper ) && ( o2 instanceof ObjectClassWrapper ) )
        {
            AttributeType at = ( ( AttributeTypeWrapper ) o1 ).getAttributeType();
            ObjectClass oc = ( ( ObjectClassWrapper ) o2 ).getObjectClass();

            o1Names = at.getNames();
            o2Names = oc.getNames();
        }
        else if ( ( o1 instanceof ObjectClassWrapper ) && ( o2 instanceof AttributeTypeWrapper ) )
        {
            ObjectClass oc = ( ( ObjectClassWrapper ) o1 ).getObjectClass();
            AttributeType at = ( ( AttributeTypeWrapper ) o2 ).getAttributeType();

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
}
