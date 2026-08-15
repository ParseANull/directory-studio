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

import org.apache.directory.studio.schemaeditor.model.Schema;


// ── CLASS: SchemaSorter — Yoda Lifting Luke's X-Wing from the Swamp ──────────
// On Dagobah, Luke's X-wing has sunk into the murky swamp — a heap of metal
// with no obvious order to it. Yoda closes his eyes, reaches out, and with the
// Force lifts the whole fighter from the chaos and sets it down neatly on the
// dry ground. What was a disordered mess is now cleanly placed.
// SchemaSorter does the same: it takes whatever order schemas happen to arrive in
// and imposes a clean, alphabetical sort on them by schema name — lifting the
// chaos into an orderly, readable list in the Schema View tree.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Sorts {@link TreeNode} objects that wrap {@link Schema} instances in ascending
 * alphabetical order by schema name.
 * When the Schema View renders its top-level list of schemas, they can arrive in
 * any order depending on how they were loaded. This comparator imposes a stable,
 * case-insensitive alphabetical order so the user always sees a predictable list.
 * Think of Yoda lifting the X-wing: the disordered swamp pile becomes a neatly
 * placed fighter — the unsorted schemas become a tidy alphabetical list.
 */
public class SchemaSorter implements Comparator<TreeNode>
{
    // ── Yoda Reaches Out and Determines Which Schema Lands First ──────────────
    // With the Force, Yoda senses the weight and position of each piece of the
    // X-wing and decides exactly where each part should be placed.
    // compare() does the same: unpack each schema's name and let them settle into order.
    // ────────────────────────────────────────────────────────────────────────────
    /**
     * Compares two {@link TreeNode} objects by the name of the {@link Schema} each wraps.
     * Both nodes must be {@link SchemaWrapper} instances for the name-based comparison to kick in.
     * If either node is not a SchemaWrapper — or if either schema is {@code null} —
     * we fall back to comparing the nodes by their {@code toString()} representations.
     *
     * <p>For example — Yoda ordering the X-wing pieces by weight, placing lighter parts first:</p>
     * <pre>
     *   sorter.compare( coreWrapper, inetOrgPersonWrapper );
     *   // "core" vs "inetorgperson" — "core" comes first alphabetically
     * </pre>
     *
     * @param o1  the first tree node to compare; expected to be a {@link SchemaWrapper}
     * @param o2  the second tree node to compare; expected to be a {@link SchemaWrapper}
     * @return    a negative integer if {@code o1} sorts before {@code o2}, zero if equal,
     *            positive if {@code o1} sorts after {@code o2}
     */
    public int compare( TreeNode o1, TreeNode o2 )
    {
        if ( ( o1 instanceof SchemaWrapper ) && ( o2 instanceof SchemaWrapper ) )
        {
            Schema s1 = ( ( SchemaWrapper ) o1 ).getSchema();
            Schema s2 = ( ( SchemaWrapper ) o2 ).getSchema();

            if ( ( s1 != null ) && ( s2 != null ) )
            {
                return s1.getSchemaName().compareToIgnoreCase( s2.getSchemaName() );
            }
        }

        // Default
        return o1.toString().compareToIgnoreCase( o2.toString() );
    }
}
