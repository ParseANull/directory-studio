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
package org.apache.directory.studio.schemaeditor.model.difference;


// ── CLASS: OrderingDifference — PALPATINE'S ATTACK SEQUENCE CHANGES ──────────
// Mace Windu is a master of Vaapad — he studies his opponent's patterns.
// Mid-duel, he realizes Palpatine has switched the ordering of his strikes;
// what used to come third now comes first.
// This class records exactly that: the ordering matching rule (how the LDAP
// server sorts and compares values of an attribute type) has changed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the ordering matching rule of an attribute type.
 * The ordering rule determines how an LDAP server sorts and range-compares
 * values — for example, whether "10" sorts before "9" lexicographically or numerically.
 * Think of this class as Mace Windu's observation that Palpatine has reordered his
 * attack pattern: the rule that governs the sequence has silently shifted.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OrderingDifference extends AbstractPropertyDifference
{
    // ── Mace Spots The New Attack Sequence With Label ─────────────────────────────
    // Mace identifies not just that the pattern changed but exactly how to
    // classify it — added, removed, or swapped for a different rule entirely.
    // We use this constructor when the diff engine already knows the verdict.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OrderingDifference with a specified difference type.
     * Use this when the engine has already determined the exact nature of the change
     * to the ordering matching rule — whether it was introduced, removed, or replaced.
     *
     * <p>For example — Mace files the classified observation:</p>
     * <pre>
     *   OrderingDifference diff =
     *       new OrderingDifference("caseIgnoreOrderingMatch", null, DifferenceType.REMOVED);
     *   // "The Chancellor no longer uses that match — it's gone."
     * </pre>
     *
     * @param source       the original ordering rule OID or name (before the change)
     * @param destination  the updated ordering rule OID or name (after the change)
     * @param type         ADDED, REMOVED, or MODIFIED — the classification of the change
     */
    public OrderingDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Senses The Shift Without Classifying It ──────────────────────────────
    // Mace knows something is off about Palpatine's sequence but defaults to
    // calling it a modification — full analysis comes later.
    // The parent class will default the type to MODIFIED.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OrderingDifference using the default difference type (MODIFIED).
     * Use this when the ordering rule has clearly changed but the engine isn't
     * yet committing to ADDED or REMOVED.
     *
     * @param source       the original ordering rule (before the change)
     * @param destination  the updated ordering rule (after the change)
     */
    public OrderingDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
