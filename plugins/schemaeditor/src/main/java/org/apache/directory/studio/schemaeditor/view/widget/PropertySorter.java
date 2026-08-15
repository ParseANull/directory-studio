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
package org.apache.directory.studio.schemaeditor.view.widget;


import java.util.Comparator;

import org.apache.directory.studio.schemaeditor.model.difference.AliasDifference;
import org.apache.directory.studio.schemaeditor.model.difference.ClassTypeDifference;
import org.apache.directory.studio.schemaeditor.model.difference.CollectiveDifference;
import org.apache.directory.studio.schemaeditor.model.difference.DescriptionDifference;
import org.apache.directory.studio.schemaeditor.model.difference.EqualityDifference;
import org.apache.directory.studio.schemaeditor.model.difference.MandatoryATDifference;
import org.apache.directory.studio.schemaeditor.model.difference.NoUserModificationDifference;
import org.apache.directory.studio.schemaeditor.model.difference.ObsoleteDifference;
import org.apache.directory.studio.schemaeditor.model.difference.OptionalATDifference;
import org.apache.directory.studio.schemaeditor.model.difference.OrderingDifference;
import org.apache.directory.studio.schemaeditor.model.difference.PropertyDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SingleValueDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SubstringDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SuperiorATDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SuperiorOCDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SyntaxDifference;
import org.apache.directory.studio.schemaeditor.model.difference.SyntaxLengthDifference;
import org.apache.directory.studio.schemaeditor.model.difference.UsageDifference;


// ── CLASS: PropertySorter — PALPATINE ISSUES ORDER 66 ────────────────────────
// Palpatine sits on the Imperial throne and knows exactly what role every clone
// trooper plays. When Order 66 goes out, every trooper gets a precise number that
// determines their execution priority — no ambiguity, no discussion. Alias additions
// go first; optional AT removals go last. The ranking is baked into the Sith plan.
// Our PropertySorter assigns the same kind of strict numeric priority to every type
// of PropertyDifference (aliases, descriptions, syntax, matching rules, etc.) so the
// table always presents changes in a logical, consistent property-grouped order.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Comparator} that sorts {@link PropertyDifference} objects by property
 * category, so related changes (all alias changes, then all description changes,
 * then all syntax changes, etc.) appear together in the table. Each property type
 * and change-direction combination is assigned a fixed numeric weight; the comparison
 * is simply a subtraction of those weights.
 * Think of it as Palpatine's Order 66 roster: every trooper (difference type) has a
 * pre-assigned execution number, and we line them up in strict numeric order.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PropertySorter implements Comparator<PropertyDifference>
{
    // ── PALPATINE COMPARES TWO TROOPERS' ORDERS ───────────────────────────────────
    // The Emperor checks the order numbers of two clone troopers: whichever has the
    // lower number executes first. We subtract the two weights and let the sign of
    // the result determine sorting order.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link PropertyDifference} objects by their assigned property weight.
     * Lower weight means the property type appears earlier in the sorted list.
     * The actual weights are defined in {@link #getWeight(PropertyDifference)}.
     *
     * <p>For example — the Emperor orders his troopers:</p>
     * <pre>
     *   AliasDifference(ADDED) weight = 1
     *   SyntaxDifference(ADDED) weight = 12
     *   compare(alias, syntax)  →  1 - 12 = -11  →  alias sorts first
     * </pre>
     *
     * @param diff1  the first property difference
     * @param diff2  the second property difference
     * @return       negative if diff1 sorts before diff2, positive if after, 0 if equal weight
     */
    public int compare( PropertyDifference diff1, PropertyDifference diff2 )
    {
        return getWeight( diff1 ) - getWeight( diff2 );
    }


    // ── READING EACH TROOPER'S ASSIGNED ORDER NUMBER ──────────────────────────────
    // The Emperor consults his ledger and reads off each trooper's designated number.
    // AliasDifference(ADDED) is number 1; AliasDifference(REMOVED) is number 2;
    // all the way down to OptionalATDifference(REMOVED) at 35.
    // Unrecognised types get weight 0 and sort to the very front.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the numeric sort weight for the given {@link PropertyDifference}. The
     * weight is a fixed constant that groups changes by property type and then orders
     * within the group by change direction (ADDED before MODIFIED before REMOVED,
     * roughly speaking). An unknown type returns 0.
     *
     * <p>For example — the Emperor reads the ledger:</p>
     * <pre>
     *   AliasDifference(ADDED)   →  1
     *   AliasDifference(REMOVED) →  2
     *   DescriptionDifference(ADDED) →  3
     *   ...
     *   OptionalATDifference(REMOVED) →  35
     * </pre>
     *
     * @param diff  the property difference whose weight we need
     * @return      an integer sort weight; lower means earlier in the list
     */
    private int getWeight( PropertyDifference diff )
    {
        if ( diff instanceof AliasDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 1;
                case REMOVED:
                    return 2;
                default:
                    break;
            }
        }
        else if ( diff instanceof ClassTypeDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 18;
                default:
                    break;
            }
        }
        else if ( diff instanceof CollectiveDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 21;
                default:
                    break;
            }
        }
        else if ( diff instanceof DescriptionDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 3;
                case MODIFIED:
                    return 4;
                case REMOVED:
                    return 5;
                default:
                    break;
            }
        }
        else if ( diff instanceof EqualityDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 23;
                case MODIFIED:
                    return 24;
                case REMOVED:
                    return 25;
                default:
                    break;
            }
        }
        else if ( diff instanceof MandatoryATDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 32;
                case REMOVED:
                    return 33;
                default:
                    break;
            }
        }
        else if ( diff instanceof NoUserModificationDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 22;
                default:
                    break;
            }
        }
        else if ( diff instanceof ObsoleteDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 19;
                default:
                    break;
            }
        }
        else if ( diff instanceof OptionalATDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 34;
                case REMOVED:
                    return 35;
                default:
                    break;
            }
        }
        else if ( diff instanceof OrderingDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 26;
                case MODIFIED:
                    return 27;
                case REMOVED:
                    return 28;
                default:
                    break;
            }
        }
        else if ( diff instanceof SingleValueDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 20;
                default:
                    break;
            }
        }
        else if ( diff instanceof SubstringDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 29;
                case MODIFIED:
                    return 30;
                case REMOVED:
                    return 31;
                default:
                    break;
            }
        }
        else if ( diff instanceof SuperiorATDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 6;
                case MODIFIED:
                    return 7;
                case REMOVED:
                    return 8;
                default:
                    break;
            }
        }
        else if ( diff instanceof SuperiorOCDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 9;
                case REMOVED:
                    return 10;
                default:
                    break;
            }
        }
        else if ( diff instanceof SyntaxDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 12;
                case MODIFIED:
                    return 13;
                case REMOVED:
                    return 14;
                default:
                    break;
            }
        }
        else if ( diff instanceof SyntaxLengthDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 15;
                case MODIFIED:
                    return 16;
                case REMOVED:
                    return 17;
                default:
                    break;
            }
        }
        else if ( diff instanceof UsageDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 11;
                default:
                    break;
            }
        }

        return 0;
    }
}
