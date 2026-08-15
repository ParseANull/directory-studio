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


// ── CLASS: SyntaxDifference — PALPATINE SPEAKS IN PURE FORCE LIGHTNING ───────
// In the confrontation's climax, Palpatine stops speaking Galactic Basic entirely —
// he abandons all pretense of diplomatic language and unleashes raw Force lightning.
// His "syntax" has fundamentally changed.
// This class records the schema-level equivalent: the syntax OID of an attribute
// type (the data format it accepts, like a telephone number, an integer, or a DN)
// has been added, removed, or swapped.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to the syntax (data type) of an attribute type definition.
 * In LDAP, a syntax OID identifies what kind of data an attribute stores —
 * telephone numbers, integers, distinguished names, binary blobs, etc.
 * Changing the syntax is a major structural change that affects every entry
 * carrying that attribute.
 * Think of this as Palpatine abandoning Galactic Basic for raw Force lightning —
 * the entire communication format has changed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SyntaxDifference extends AbstractPropertyDifference
{
    // ── Mace Classifies The Syntax Shift With A Verdict ──────────────────────────
    // Mace identifies not just that Palpatine changed his language but exactly
    // how — added a new syntax OID, dropped it entirely, or swapped it.
    // We use this constructor when the diff engine has already classified the change.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SyntaxDifference with an explicit difference type.
     * Use this when the engine already knows whether the syntax OID was
     * introduced, removed, or replaced by a different one.
     *
     * <p>For example — Mace files the charge with a label:</p>
     * <pre>
     *   SyntaxDifference diff =
     *       new SyntaxDifference("1.3.6.1.4.1.1466.115.121.1.15",
     *                            "1.3.6.1.4.1.1466.115.121.1.27",
     *                            DifferenceType.MODIFIED);
     *   // "He's stopped speaking Directory String — now it's Integer. Force lightning."
     * </pre>
     *
     * @param source       the original syntax OID (before the change)
     * @param destination  the updated syntax OID (after the change)
     * @param type         ADDED, REMOVED, or MODIFIED — how the syntax changed
     */
    public SyntaxDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Senses The Syntax Shift Before Classifying ──────────────────────────
    // Mace knows the language has changed but defaults to MODIFIED —
    // the full charge sheet will emerge from deeper analysis.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SyntaxDifference using the default difference type (MODIFIED).
     * Use this when the syntax has clearly changed but the engine isn't yet
     * committing to ADDED or REMOVED.
     *
     * @param source       the original syntax OID (before the change)
     * @param destination  the updated syntax OID (after the change)
     */
    public SyntaxDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
