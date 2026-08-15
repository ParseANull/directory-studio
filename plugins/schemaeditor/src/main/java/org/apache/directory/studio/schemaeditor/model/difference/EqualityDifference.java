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


// ── CLASS: EqualityDifference — Mace Checks The Matching Rule ────────────────
// Mace Windu scrutinises a very technical piece of evidence: the equality
// matching rule.  In LDAP, an attribute type's EQUALITY matching rule defines
// how two values of that type are compared (e.g. case-insensitive string match).
// If that matching rule was added, removed, or replaced between schema versions,
// Mace stamps an EqualityDifference — "The way we determine if two values match
// has changed, and that's a significant schema alteration."
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records that the {@code EQUALITY} matching rule OID on an LDAP AttributeType
 * changed between two schema snapshots.
 * The verdict is ADDED (rule was null, now set), REMOVED (rule was set, now
 * null), or MODIFIED (the OID changed to a different matching rule).
 * Think of it as Mace noting: "The rule for deciding what 'equal' means was
 * swapped out — that's a fundamental change to how this attribute behaves."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class EqualityDifference extends AbstractPropertyDifference
{
    // ── Mace Files An Equality-Rule Charge With Pre-Known Verdict ────────────
    // The engine already knows whether the equality OID was added, removed, or
    // changed at detection time, so we stamp it up front.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an equality-matching-rule difference with verdict pre-stamped.
     * Set old/new OID strings immediately after via {@link #setOldValue} and
     * {@link #setNewValue}.
     *
     * <p>For example — Mace files the charge:</p>
     * <pre>
     *   EqualityDifference diff =
     *       new EqualityDifference( at1, at2, DifferenceType.MODIFIED );
     *   diff.setOldValue( "caseIgnoreMatch" );
     *   diff.setNewValue( "caseExactMatch" );
     * </pre>
     *
     * @param source       the original "before" AttributeType
     * @param destination  the new "after" AttributeType
     * @param type         ADDED, REMOVED, or MODIFIED
     */
    public EqualityDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Opens The Equality Charge Without A Verdict ─────────────────────
    // Deferred-verdict variant for framework completeness.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an equality-matching-rule difference without a verdict; call
     * {@link #setType} afterwards.
     * Prefer the three-argument constructor in practice.
     *
     * <p>For example — Mace opens the sheet:</p>
     * <pre>
     *   EqualityDifference diff = new EqualityDifference( at1, at2 );
     *   diff.setType( DifferenceType.ADDED );
     *   diff.setNewValue( "caseIgnoreMatch" );
     * </pre>
     *
     * @param source       the original "before" AttributeType
     * @param destination  the new "after" AttributeType
     */
    public EqualityDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
