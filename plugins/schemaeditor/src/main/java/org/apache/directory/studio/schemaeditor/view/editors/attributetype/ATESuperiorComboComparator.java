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
package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import java.util.Comparator;
import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.studio.schemaeditor.view.editors.NonExistingAttributeType;


// ── CLASS: ATESuperiorComboComparator — LUKE AND VADER DUEL IN BESPIN'S CARBON CHAMBER
// In the Cloud City carbon-freezing chamber, Luke and Vader face off.  Vader has to
// size up his opponent — is this young Skywalker stronger or weaker than him?  He makes
// that determination in a split second, and it drives every move he makes next.
// This comparator does the same for attribute types in the "Superior Type" combo:
// given two entries (real AttributeType objects, NonExistingAttributeType placeholders,
// or one of each), it determines which one comes first alphabetically so the list
// sorts consistently for the user.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Comparator that sorts the items in the "Superior Type" combo box of the Attribute
 * Type Editor into alphabetical order by their first display name.
 * Like {@link ATEMatchingRulesComboComparator}, we handle all four combinations of
 * real {@link AttributeType} and {@link NonExistingAttributeType} placeholder objects.
 * Think of this as Vader assessing Luke: regardless of which category each combatant
 * falls into, he always produces a clear ranking.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ATESuperiorComboComparator implements Comparator<Object>
{
    // ── Vader Assesses Both Combatants in a Single Glance ───────────────────────────
    // Vader looks at Luke and sizes him up against every other Jedi he has fought —
    // real Jedi Masters and raw padawans alike — and ranks them in his threat registry.
    // We do the same: extract the first name from each entry, compare case-insensitively,
    // handle all four type combinations.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two "Superior Type" combo items by their first name, case-insensitively,
     * and returns the standard Comparator contract result.
     * Handles all four pairings: AttributeType vs AttributeType, AttributeType vs
     * NonExistingAttributeType, NonExistingAttributeType vs AttributeType, and
     * NonExistingAttributeType vs NonExistingAttributeType.
     * Returns 0 if names can't be extracted (null or empty lists) to avoid an exception
     * mid-sort.
     *
     * <p>For example — Vader's threat assessment:</p>
     * <pre>
     *   // AttributeType("cn") vs AttributeType("sn") → "cn" &lt; "sn" → negative
     *   // NonExistingAttributeType("xyz") vs AttributeType("abc") → "xyz" &gt; "abc" → positive
     * </pre>
     *
     * @param o1  the first item — either an {@link AttributeType} or a
     *            {@link NonExistingAttributeType}
     * @param o2  the second item — either an {@link AttributeType} or a
     *            {@link NonExistingAttributeType}
     * @return    negative if o1 sorts before o2, zero if equal, positive if o1 sorts after
     */
    public int compare( Object o1, Object o2 )
    {
        if ( o1 instanceof AttributeType && o2 instanceof AttributeType )
        {
            List<String> at1Names = ( ( AttributeType ) o1 ).getNames();
            List<String> at2Names = ( ( AttributeType ) o2 ).getNames();

            if ( ( at1Names != null ) && ( at2Names != null ) && ( at1Names.size() > 0 ) && ( at2Names.size() > 0 ) )
            {
                return at1Names.get( 0 ).compareToIgnoreCase( at2Names.get( 0 ) );
            }
        }
        else if ( o1 instanceof AttributeType && o2 instanceof NonExistingAttributeType )
        {
            List<String> at1Names = ( ( AttributeType ) o1 ).getNames();
            String at2Name = ( ( NonExistingAttributeType ) o2 ).getName();

            if ( ( at1Names != null ) && ( at2Name != null ) && ( at1Names.size() > 0 ) )
            {
                return at1Names.get( 0 ).compareToIgnoreCase( at2Name );
            }
        }
        else if ( o1 instanceof NonExistingAttributeType && o2 instanceof AttributeType )
        {
            String at1Name = ( ( NonExistingAttributeType ) o1 ).getName();
            List<String> at2Names = ( ( AttributeType ) o2 ).getNames();

            if ( ( at1Name != null ) && ( at2Names != null ) && ( at2Names.size() > 0 ) )
            {
                return at1Name.compareToIgnoreCase( at2Names.get( 0 ) );
            }
        }
        else if ( o1 instanceof NonExistingAttributeType && o2 instanceof NonExistingAttributeType )
        {
            String at1Name = ( ( NonExistingAttributeType ) o1 ).getName();
            String at2Name = ( ( NonExistingAttributeType ) o2 ).getName();

            if ( ( at1Name != null ) && ( at2Name != null ) )
            {
                return at1Name.compareToIgnoreCase( at2Name );
            }
        }

        return 0;
    }
}
