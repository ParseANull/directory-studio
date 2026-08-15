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

import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.difference.SchemaDifference;


// ── CLASS: SchemaDifferenceSorter — MACE WINDU CONFRONTS PALPATINE ───────────
// In the Chancellor's office, Mace Windu places two combatants face to face and
// demands a verdict: who is the stronger? He does not care about feelings or context —
// he extracts the relevant fact (the schema name from source or destination),
// compares it head-to-head, and the weaker one yields. Schema A vs. Schema B:
// whoever comes first alphabetically wins, case-insensitively. No debate.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * A {@link Comparator} that sorts {@link SchemaDifference} objects alphabetically
 * by the name of the underlying schema. For ADDED, MODIFIED, and IDENTICAL differences
 * we read the destination schema name; for REMOVED we read the source schema name
 * (since the destination no longer exists). The comparison is case-insensitive.
 * Think of it as Mace Windu's confrontation: two schema names face each other and
 * the one that sorts first alphabetically wins — swift, decisive, no appeals.
 */
public class SchemaDifferenceSorter implements Comparator<Object>
{
    // ── MACE PLACES TWO SCHEMATA FACE TO FACE ────────────────────────────────────
    // Mace brings two suspects into the room, extracts the defining fact about each
    // (their schema name), and compares. The lesser name yields. Non-SchemaDifference
    // objects fall back to a plain string comparison so we never throw.
    // ─────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link SchemaDifference} objects by the name of their underlying
     * schema, case-insensitively and ascending. For REMOVED differences we use the
     * source schema because the destination is absent. Non-SchemaDifference pairs fall
     * back to a {@code toString()} comparison.
     *
     * <p>For example — Mace confronts the two suspects:</p>
     * <pre>
     *   sd1 = SchemaDifference("cosine", MODIFIED)
     *   sd2 = SchemaDifference("core",   IDENTICAL)
     *   compare(sd1, sd2)  →  positive (cosine &gt; core alphabetically)
     *   → "core" sorts first in the tree
     * </pre>
     *
     * @param o1  the first object to compare, expected to be a {@link SchemaDifference}
     * @param o2  the second object to compare, expected to be a {@link SchemaDifference}
     * @return    negative if o1 sorts before o2, positive if after, 0 if equal
     */
    public int compare( Object o1, Object o2 )
    {
        if ( ( o1 instanceof SchemaDifference ) && ( o2 instanceof SchemaDifference ) )
        {
            SchemaDifference sd1 = ( SchemaDifference ) o1;
            SchemaDifference sd2 = ( SchemaDifference ) o2;

            String name1 = ""; //$NON-NLS-1$
            String name2 = ""; //$NON-NLS-1$
            switch ( sd1.getType() )
            {
                case ADDED:
                    name1 = ( ( Schema ) sd1.getDestination() ).getSchemaName();
                    break;
                case MODIFIED:
                    name1 = ( ( Schema ) sd1.getDestination() ).getSchemaName();
                    break;
                case REMOVED:
                    name1 = ( ( Schema ) sd1.getSource() ).getSchemaName();
                    break;
                case IDENTICAL:
                    name1 = ( ( Schema ) sd1.getDestination() ).getSchemaName();
                    break;
            }

            switch ( sd2.getType() )
            {
                case ADDED:
                    name2 = ( ( Schema ) sd2.getDestination() ).getSchemaName();
                    break;
                case MODIFIED:
                    name2 = ( ( Schema ) sd2.getDestination() ).getSchemaName();
                    break;
                case REMOVED:
                    name2 = ( ( Schema ) sd2.getSource() ).getSchemaName();
                    break;
                case IDENTICAL:
                    name2 = ( ( Schema ) sd2.getDestination() ).getSchemaName();
                    break;
            }

            return name1.compareToIgnoreCase( name2 );
        }

        // Default
        return o1.toString().compareToIgnoreCase( o2.toString() );
    }
}
