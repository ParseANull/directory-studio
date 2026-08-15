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


// ── CLASS: SubstringDifference — PALPATINE CHANGES HIS VERBAL PATTERN ────────
// As Mace Windu presses Palpatine against the window, he notices the Chancellor
// has adopted a completely different pattern of speech — the substring of his
// rhetoric that used to start with "Republic" now starts with "Empire."
// This class records the analogous schema-level change: the substring matching
// rule for an attribute type has been added, removed, or replaced.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the substring matching rule of an attribute type.
 * The substring rule governs how the LDAP server handles wildcard searches
 * like {@code cn=*win*} — it determines how partial-match queries are evaluated.
 * Think of this as Mace noting that Palpatine's verbal pattern has shifted:
 * the rule governing which "substrings" of his speech match a known identity has changed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SubstringDifference extends AbstractPropertyDifference
{
    // ── Mace Classifies The New Pattern With A Label ──────────────────────────────
    // Mace can say exactly how the pattern changed — added, removed, or swapped.
    // We use this constructor when the diff engine already knows the verdict.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SubstringDifference with an explicit difference type.
     * Use this when the engine already knows whether the substring matching rule
     * was added, removed, or changed to a different rule.
     *
     * <p>For example — Mace files a specific charge:</p>
     * <pre>
     *   SubstringDifference diff =
     *       new SubstringDifference("caseIgnoreSubstringsMatch", null, DifferenceType.REMOVED);
     *   // "The Chancellor stripped out his old matching pattern entirely."
     * </pre>
     *
     * @param source       the original substring matching rule name or OID (before the change)
     * @param destination  the updated substring matching rule name or OID (after the change)
     * @param type         ADDED, REMOVED, or MODIFIED — how the rule changed
     */
    public SubstringDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Senses The Shift Without Classifying It ──────────────────────────────
    // Mace knows the pattern is different but defaults to MODIFIED —
    // deeper analysis will reveal whether it was added, removed, or swapped.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SubstringDifference using the default difference type (MODIFIED).
     * Use this when the substring rule has obviously changed but the engine
     * isn't yet committing to ADDED or REMOVED.
     *
     * @param source       the original substring rule (before the change)
     * @param destination  the updated substring rule (after the change)
     */
    public SubstringDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
