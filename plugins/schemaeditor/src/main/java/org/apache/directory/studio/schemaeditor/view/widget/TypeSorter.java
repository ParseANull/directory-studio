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


// ── CLASS: TypeSorter — CLONE TROOPERS EXECUTE ORDER 66 ─────────────────────
// When Order 66 goes out, every clone trooper executes it according to their
// assigned priority. The troopers closest to their targets act first (low weight);
// those still on their way act later (high weight). Each trooper's assignment is
// baked into the Imperial command structure — ADDED troopers deploy before MODIFIED
// ones, who deploy before REMOVED ones, within each property category.
// Our TypeSorter assigns the same kind of deployment weight to each PropertyDifference
// so the table presents additions first, modifications in the middle, removals last —
// mirroring the natural lifecycle of a schema change.
// ─────────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Comparator} that sorts {@link PropertyDifference} objects by change type
 * (ADDED, then MODIFIED, then REMOVED) across all property categories. Within each
 * change type, property categories are also ranked so aliases come before descriptions,
 * which come before superior references, and so on.
 * Think of it as the clone trooper deployment order: each trooper (difference) has a
 * pre-assigned execution number, and we line them up so additions happen first,
 * modifications second, and removals last.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TypeSorter implements Comparator<PropertyDifference>
{
    // ── TWO TROOPERS CHECK THEIR DEPLOYMENT NUMBERS ───────────────────────────────
    // Two clone troopers compare their execution orders: whichever has the lower
    // number deploys first. We subtract the weights and let the sign decide the order.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link PropertyDifference} objects by their type-based sort weight.
     * ADDED changes sort before MODIFIED, which sort before REMOVED, with further
     * ordering within each change type by property category.
     *
     * <p>For example — two troopers check their orders:</p>
     * <pre>
     *   AliasDifference(ADDED) weight = 1
     *   AliasDifference(REMOVED) weight = 25
     *   compare(addedAlias, removedAlias)  →  1 - 25 = -24  →  ADDED sorts first
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


    // ── READING EACH TROOPER'S DEPLOYMENT NUMBER ──────────────────────────────────
    // Imperial command assigns a unique deployment number to every trooper based on
    // their mission type and target priority. ADDED missions are in the 1-11 range
    // (strike first), MODIFIED in the 12-24 range (consolidate), REMOVED in the
    // 25-35 range (clean up). Unrecognised missions get 0 and deploy at the front.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the numeric sort weight for the given {@link PropertyDifference}. The
     * weight groups all ADDED changes in the low range (1-11), MODIFIED in the middle
     * (12-24), and REMOVED in the high range (25-35), with further ordering within
     * each range by property category. Unknown types return 0.
     *
     * <p>For example — reading the deployment roster:</p>
     * <pre>
     *   AliasDifference(ADDED)          →  1
     *   DescriptionDifference(ADDED)    →  2
     *   DescriptionDifference(MODIFIED) →  12
     *   AliasDifference(REMOVED)        →  25
     *   OptionalATDifference(REMOVED)   →  35
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
                    return 25;
                default:
                    break;
            }
        }
        else if ( diff instanceof ClassTypeDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 17;
                default:
                    break;
            }
        }
        else if ( diff instanceof CollectiveDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 20;
                default:
                    break;
            }
        }
        else if ( diff instanceof DescriptionDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 2;
                case MODIFIED:
                    return 12;
                case REMOVED:
                    return 26;
                default:
                    break;
            }
        }
        else if ( diff instanceof EqualityDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 7;
                case MODIFIED:
                    return 22;
                case REMOVED:
                    return 31;
                default:
                    break;
            }
        }
        else if ( diff instanceof MandatoryATDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 10;
                case REMOVED:
                    return 34;
                default:
                    break;
            }
        }
        else if ( diff instanceof NoUserModificationDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 21;
                default:
                    break;
            }
        }
        else if ( diff instanceof ObsoleteDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 18;
                default:
                    break;
            }
        }
        else if ( diff instanceof OptionalATDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 11;
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
                    return 8;
                case MODIFIED:
                    return 23;
                case REMOVED:
                    return 32;
                default:
                    break;
            }
        }
        else if ( diff instanceof SingleValueDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 19;
                default:
                    break;
            }
        }
        else if ( diff instanceof SubstringDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 9;
                case MODIFIED:
                    return 24;
                case REMOVED:
                    return 33;
                default:
                    break;
            }
        }
        else if ( diff instanceof SuperiorATDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 3;
                case MODIFIED:
                    return 13;
                case REMOVED:
                    return 27;
                default:
                    break;
            }
        }
        else if ( diff instanceof SuperiorOCDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 4;
                case REMOVED:
                    return 28;
                default:
                    break;
            }
        }
        else if ( diff instanceof SyntaxDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 5;
                case MODIFIED:
                    return 15;
                case REMOVED:
                    return 29;
                default:
                    break;
            }
        }
        else if ( diff instanceof SyntaxLengthDifference )
        {
            switch ( diff.getType() )
            {
                case ADDED:
                    return 6;
                case MODIFIED:
                    return 16;
                case REMOVED:
                    return 30;
                default:
                    break;
            }
        }
        else if ( diff instanceof UsageDifference )
        {
            switch ( diff.getType() )
            {
                case MODIFIED:
                    return 14;
                default:
                    break;
            }
        }

        return 0;
    }
}
