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


// ── CLASS: ObsoleteDifference — PALPATINE REVEALED AS SITH LORD ──────────────
// In an instant, Palpatine drops all pretense — he ignites his lightsaber and
// reveals that the Republic's Chancellor is irrevocably obsolete as an identity.
// That single flip from "not obsolete" to "obsolete" (or vice versa) is all
// this class records: the OBSOLETE flag on an attribute type or object class
// has changed, and the change type is always MODIFIED.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the {@code OBSOLETE} flag on an attribute type or object class.
 * A schema element marked OBSOLETE is officially retired — servers should still
 * accept it for backward compatibility, but new entries shouldn't use it.
 * Think of this class as the moment Mace Windu sees Palpatine ignite his lightsaber:
 * a single, irrefutable fact that the element's status has flipped.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObsoleteDifference extends AbstractPropertyDifference
{
    // ── Palpatine Ignites His Lightsaber ─────────────────────────────────────────
    // The moment Palpatine moves, there is no ambiguity — the verdict is
    // MODIFIED, the evidence is unambiguous, and there is no overload that
    // softens the charge.
    // We hard-code DifferenceType.MODIFIED because the OBSOLETE flag can only
    // flip (true↔false); it is never added or removed as a concept.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ObsoleteDifference recording that an element's OBSOLETE flag has flipped.
     * The type is always {@link DifferenceType#MODIFIED} — the flag exists on every element
     * and can only change value, never appear or disappear.
     *
     * <p>For example — Mace sees the lightsaber; the verdict is instant:</p>
     * <pre>
     *   ObsoleteDifference diff = new ObsoleteDifference(false, true);
     *   // "He just made himself a Sith Lord." — Kit Fisto
     * </pre>
     *
     * @param source       the old boolean value of the OBSOLETE flag (before the change)
     * @param destination  the new boolean value of the OBSOLETE flag (after the change)
     */
    public ObsoleteDifference( Object source, Object destination )
    {
        super( source, destination, DifferenceType.MODIFIED );
    }
}
