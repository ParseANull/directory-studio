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


// ── CLASS: SyntaxLengthDifference — PALPATINE'S LIGHTNING GROWS LONGER ───────
// In the final moments of the confrontation, Palpatine's Force lightning grows
// longer and more powerful — the reach of his attack has changed even though
// the weapon is the same.
// This class records the schema-level equivalent: the maximum allowed length for
// an attribute type's syntax values has changed (e.g., max string length went
// from 64 to 256 characters).
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the syntax length constraint on an attribute type.
 * Many LDAP syntaxes support an optional maximum length — e.g., a directory string
 * attribute might cap values at 256 characters.
 * Think of this as Mace noting that Palpatine's Force lightning has grown longer —
 * the underlying weapon (syntax) is the same, but its reach (length) has changed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyntaxLengthDifference extends AbstractPropertyDifference
{
    // ── Mace Measures The New Reach With A Classification ────────────────────────
    // Mace notes the lightning now extends further and classifies the change:
    // the length was added for the first time, removed, or adjusted.
    // We use this constructor when the diff engine has already classified the change.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SyntaxLengthDifference with an explicit difference type.
     * Use this when the engine already knows whether the syntax length was
     * introduced, removed, or changed to a new value.
     *
     * <p>For example — Mace files the charge with a label:</p>
     * <pre>
     *   SyntaxLengthDifference diff =
     *       new SyntaxLengthDifference(64, 256, DifferenceType.MODIFIED);
     *   // "The lightning's reach quadrupled — that's a material change."
     * </pre>
     *
     * @param source       the original syntax length value (before the change)
     * @param destination  the updated syntax length value (after the change)
     * @param type         ADDED, REMOVED, or MODIFIED — how the length constraint changed
     */
    public SyntaxLengthDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Senses The Extended Reach Before Classifying ────────────────────────
    // Mace knows the reach has changed but defaults to MODIFIED —
    // a more precise verdict comes later.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SyntaxLengthDifference using the default difference type (MODIFIED).
     * Use this when the length constraint has clearly changed but the engine isn't
     * yet committing to ADDED or REMOVED.
     *
     * @param source       the original syntax length (before the change)
     * @param destination  the updated syntax length (after the change)
     */
    public SyntaxLengthDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
