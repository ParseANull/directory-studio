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


// ── CLASS: SingleValueDifference — MACE'S ONE DECISIVE VERDICT ───────────────
// With Palpatine pinned against the window, Mace Windu says exactly one thing:
// "He's too dangerous to be kept alive!" — a single, irreversible judgment.
// This class records that kind of moment: the SINGLE-VALUE flag on an attribute
// type has flipped (true↔false), and the change type is always MODIFIED because
// the flag can only change, never appear or disappear.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the SINGLE-VALUE constraint on an attribute type.
 * When an attribute type is single-valued, each LDAP entry may only store one
 * value for it — like a person can only have one birth date.
 * Flipping this flag has significant consequences for existing directory data,
 * which is why we track it explicitly.
 * Think of this as Mace Windu's one decisive verdict — single, clear, no overloads.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SingleValueDifference extends AbstractPropertyDifference
{
    // ── Mace Delivers The Single Verdict ─────────────────────────────────────────
    // There is only one thing to say and Mace says it plainly.
    // The type is always MODIFIED — the flag exists on every attribute type
    // and can only flip (true↔false), never be added or removed as a concept.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SingleValueDifference recording that an attribute type's SINGLE-VALUE
     * constraint has changed from one boolean to the other.
     * The type is always {@link DifferenceType#MODIFIED} — this flag can only flip value.
     *
     * <p>For example — Mace's one clear statement:</p>
     * <pre>
     *   SingleValueDifference diff = new SingleValueDifference(false, true);
     *   // "He's too dangerous to be kept alive!" — one verdict, no appeal.
     * </pre>
     *
     * @param source       the old boolean value of the SINGLE-VALUE flag (before the change)
     * @param destination  the new boolean value of the SINGLE-VALUE flag (after the change)
     */
    public SingleValueDifference( Object source, Object destination )
    {
        super( source, destination, DifferenceType.MODIFIED );
    }
}
