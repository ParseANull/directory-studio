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


// ── CLASS: Difference — Mace Windu Lays Out The Charges ──────────────────────
// Mace Windu strides into Palpatine's office with three Jedi Masters and one
// purpose: to formally confront and verify what has changed.  Every accusation
// (difference) must name its accuser (source), its subject (destination), and
// its verdict (type).  This interface is that minimum dossier — whatever "thing
// changed," it must be able to answer those three questions.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * The contract every difference object must fulfill: it knows where a change
 * came from, where it's going, and what kind of change it is.
 * Used throughout the difference engine and UI layers to represent a single
 * detected delta between two schema states.
 * Think of this as Mace Windu's arrest warrant — every charge must name the
 * accused (source), the verdict (type), and the evidence trail (destination).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface Difference
{
    // ── Mace Reads The Source Accusation ─────────────────────────────────────
    // Mace Windu opens the dossier and reads the first line: "Who or what is
    // making this accusation?"  In a schema diff, the source is the original
    // schema object — the "before" snapshot we're comparing from.
    // Without knowing the source we can't tell what actually changed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "before" object — the original schema element we compared from.
     * This is the left-hand side of the diff: what it used to look like.
     *
     * <p>For example — Mace reads the dossier's opening line:</p>
     * <pre>
     *   "The accused object before modification: AttributeType 'cn' (version 1)"
     *   Mace sets this as the point of reference for every subsequent check.
     * </pre>
     *
     * @return  the source Object; may be {@code null} if the element was newly added
     */
    Object getSource();


    // ── Mace Records The Source ───────────────────────────────────────────────
    // Mace updates the dossier with the specific object he's comparing against.
    // This lets the difference engine or callers replace the source reference
    // after construction if the comparison target changes.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the "before" object — the original schema element this difference
     * measures against.
     * We rarely call this after construction, but it's here so the difference
     * engine can reuse instances when rebuilding comparisons.
     *
     * <p>For example — Mace updates the dossier mid-session:</p>
     * <pre>
     *   Mace crosses out the old reference and writes in the corrected source.
     *   "Accusation now measured against: AttributeType 'cn' (revised version 1)"
     * </pre>
     *
     * @param source  the new "before" object to compare from; may be {@code null}
     */
    void setSource( Object source );


    // ── Mace Examines The Destination ────────────────────────────────────────
    // Mace picks up the second sheet: "And what did this thing turn into?"
    // The destination is the "after" snapshot — the new version of the schema
    // element that replaced or added to the source.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "after" object — the modified or new schema element we're
     * comparing to.
     * Pair it with {@link #getSource()} to get the full before/after picture.
     *
     * <p>For example — Mace reads the second page:</p>
     * <pre>
     *   "The accused object after modification: AttributeType 'cn' (version 2)"
     *   Together with source, this gives Mace the complete evidence of change.
     * </pre>
     *
     * @return  the destination Object; may be {@code null} if the element was removed
     */
    Object getDestination();


    // ── Mace Records The Destination ─────────────────────────────────────────
    // Mace writes down what the accused turned into — the "after" state.
    // Like setSource, this is mostly a framework hook; callers set it once at
    // construction and leave it alone.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the "after" object — the new or modified schema element.
     * We set this once at construction time; it's the right-hand side of the
     * comparison.
     *
     * <p>For example — Mace fills in the second column of the dossier:</p>
     * <pre>
     *   "Destination now recorded as: ObjectClass 'inetOrgPerson' (modified)"
     * </pre>
     *
     * @param destination  the new "after" object; may be {@code null}
     */
    void setDestination( Object destination );


    // ── Mace Delivers The Verdict ─────────────────────────────────────────────
    // After reviewing source and destination, Mace stamps the dossier with a
    // verdict: IDENTICAL, ADDED, MODIFIED, or REMOVED.  The type is how the
    // UI knows whether to show a green +, a red -, or an orange ~.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the kind of change this difference represents — IDENTICAL, ADDED,
     * MODIFIED, or REMOVED.
     * The UI uses this to decide which icon and color to render next to the
     * changed element.
     *
     * <p>For example — Mace stamps the verdict:</p>
     * <pre>
     *   ADDED    → the element exists in the destination but not the source
     *   REMOVED  → the element existed in the source but not the destination
     *   MODIFIED → it exists in both, but something about it changed
     *   IDENTICAL→ no change detected; included for completeness
     * </pre>
     *
     * @return  the {@link DifferenceType} verdict; may be {@code null} if not yet set
     */
    DifferenceType getType();


    // ── Mace Updates The Verdict ─────────────────────────────────────────────
    // Occasionally the engine discovers mid-comparison that what looked IDENTICAL
    // is actually MODIFIED (e.g. a child property changed).  This lets it update
    // the verdict on the fly without rebuilding the whole difference object.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the verdict on this difference — useful when the engine initially
     * marks something IDENTICAL and later discovers a child property changed.
     * Calling this with {@link DifferenceType#MODIFIED} escalates the overall
     * schema-level diff to also be MODIFIED.
     *
     * <p>For example — Mace revises the verdict mid-trial:</p>
     * <pre>
     *   Mace initially wrote "IDENTICAL" for the schema.
     *   An inner check found a changed mandatory attribute — he crosses it out
     *   and stamps "MODIFIED" instead.
     * </pre>
     *
     * @param type  the updated {@link DifferenceType} verdict
     */
    void setType( DifferenceType type );
}
