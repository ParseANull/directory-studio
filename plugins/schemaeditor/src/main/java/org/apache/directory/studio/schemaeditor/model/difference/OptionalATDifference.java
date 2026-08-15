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


// ── CLASS: OptionalATDifference — OPTIONAL WEAPONS IN PALPATINE'S ARSENAL ────
// During his duel with Mace Windu, Palpatine does not use the same optional
// techniques he once had — some defensive maneuvers have been added to his
// repertoire, others abandoned.
// This class records one such change: an optional attribute type (MAY attribute
// in LDAP speak) was added to, removed from, or swapped in an object class's
// optional list.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Records a change to one optional attribute type (a MAY attribute) in an object class.
 * Optional attributes are those an LDAP entry may carry but isn't required to — think
 * of them as the object class's non-mandatory capabilities.
 * Think of this class as Mace Windu noting which optional Force techniques Palpatine has
 * quietly added or dropped since his last assessment.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OptionalATDifference extends AbstractPropertyDifference
{
    // ── A New Technique Added To The Repertoire ───────────────────────────────────
    // Mace notices Palpatine is using a technique he's never seen before — it
    // has appeared since the last scan and has a specific classification (ADDED,
    // REMOVED, or MODIFIED).
    // We use this constructor when the diff engine already knows the verdict.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OptionalATDifference with an explicit difference type.
     * Use this when the engine has already determined whether the optional attribute
     * type was added, removed, or had its value replaced.
     *
     * <p>For example — Mace files the charge with a known category:</p>
     * <pre>
     *   OptionalATDifference diff =
     *       new OptionalATDifference("telephoneNumber", null, DifferenceType.REMOVED);
     *   // "That technique has been stripped from the arsenal."
     * </pre>
     *
     * @param source       the original optional attribute type name or object (before change)
     * @param destination  the updated optional attribute type name or object (after change)
     * @param type         ADDED, REMOVED, or MODIFIED — the nature of the change
     */
    public OptionalATDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── A Subtle Shift Without An Obvious Label ───────────────────────────────────
    // Mace senses the change but defaults to calling it a modification until
    // deeper analysis proves otherwise.
    // We create the diff without locking in a verdict — the parent class will
    // default the type to MODIFIED.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new OptionalATDifference using the default difference type (MODIFIED).
     * Use this when the engine detects a change in an optional attribute type but hasn't
     * yet classified whether it was added, removed, or simply updated.
     *
     * @param source       the original optional attribute type (before change)
     * @param destination  the updated optional attribute type (after change)
     */
    public OptionalATDifference( Object source, Object destination )
    {
        super( source, destination );
    }
}
