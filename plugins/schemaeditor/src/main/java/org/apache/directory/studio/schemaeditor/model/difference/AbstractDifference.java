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


// ── CLASS: AbstractDifference — Mace Windu Opens The Dossier ─────────────────
// In Revenge of the Sith, Mace Windu arrives at Palpatine's office carrying a
// dossier: who is accusing whom (source), who is accused (destination), and
// what the preliminary finding is (type — ADDED, REMOVED, MODIFIED, IDENTICAL).
// This class is that dossier cover sheet — every concrete Difference subclass
// inherits these three fields and their accessors, so we don't repeat ourselves.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Base implementation of {@link Difference} that stores the source, destination,
 * and verdict type for any schema comparison result.
 * Concrete difference classes (e.g. {@link AttributeTypeDifference}) extend this
 * rather than re-implementing the same three fields over and over.
 * Think of this as Mace Windu's standard dossier cover — every charge sheet has
 * a source, a destination, and a preliminary verdict stamped on it.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AbstractDifference implements Difference
{
    /** The source Object */
    private Object source;

    /** The destination Object */
    private Object destination;

    /** The type of difference */
    private DifferenceType type;


    // ── Mace Opens The Dossier Without A Verdict ─────────────────────────────
    // Mace and three Jedi Masters enter the office.  They know the accused
    // (source) and what we expect (destination), but haven't stamped a verdict
    // yet — that comes later once the evidence is reviewed.
    // This constructor is for when the type will be set separately after more
    // detailed property checks are done.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a difference record with source and destination but no verdict yet.
     * We use this when the engine needs to create the record first and determine
     * the type ({@link DifferenceType}) in a separate pass.
     *
     * <p>For example — Mace opens the case file before delivering the verdict:</p>
     * <pre>
     *   "The accused: AttributeType 'cn' before modification."
     *   "The current state: AttributeType 'cn' after modification."
     *   (Verdict to be stamped after the full review.)
     * </pre>
     *
     * @param source       the original "before" object; may be {@code null} for ADDED diffs
     * @param destination  the new "after" object; may be {@code null} for REMOVED diffs
     */
    public AbstractDifference( Object source, Object destination )
    {
        this.source = source;
        this.destination = destination;
    }


    // ── Mace Opens The Dossier With A Verdict Already Stamped ────────────────
    // Sometimes we already know the verdict before we even enter the room — for
    // instance, if the element only exists in the source list, we know it's
    // REMOVED without any further inspection.  This constructor lets us stamp
    // the verdict at construction time to save a separate setType() call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a difference record with source, destination, and verdict all set
     * up front.
     * Use this when the {@link DifferenceType} is already known — for example,
     * when we detect an element only in the "before" list (REMOVED) or only in
     * the "after" list (ADDED).
     *
     * <p>For example — Mace arrives with the verdict already written:</p>
     * <pre>
     *   new AbstractDifference( oldSchema, null, DifferenceType.REMOVED )
     *   // "The schema 'core' is gone — verdict: REMOVED, no further review needed."
     * </pre>
     *
     * @param source       the original "before" object; may be {@code null}
     * @param destination  the new "after" object; may be {@code null}
     * @param type         the {@link DifferenceType} verdict
     */
    public AbstractDifference( Object source, Object destination, DifferenceType type )
    {
        this.source = source;
        this.destination = destination;
        this.type = type;
    }


    // ── Mace Reads The Destination ────────────────────────────────────────────
    // Mace picks up the second page: "What does this element look like now?"
    // The destination is the "after" snapshot we're comparing to.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "after" schema object — what the element looks like in the
     * version we're comparing to.
     * Pair with {@link #getSource()} to get the complete before/after picture.
     *
     * <p>For example — Mace reads the "after" column:</p>
     * <pre>
     *   Object dest = diff.getDestination();
     *   // "This is what AttributeType 'cn' became after the schema was updated."
     * </pre>
     *
     * @return  the destination object; {@code null} if the element was removed
     */
    public Object getDestination()
    {
        return destination;
    }


    // ── Mace Updates The Destination ─────────────────────────────────────────
    // Rarely needed, but the engine can replace the destination reference if
    // it builds the diff incrementally before the full "after" object is ready.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the "after" schema object on this difference record.
     * We almost never call this after construction — it's a framework hook for
     * incremental diff building.
     *
     * <p>For example — Mace crosses out the old destination and writes a new one:</p>
     * <pre>
     *   diff.setDestination( updatedAttributeType );
     * </pre>
     *
     * @param destination  the new "after" object; may be {@code null}
     */
    public void setDestination( Object destination )
    {
        this.destination = destination;
    }


    // ── Mace Reads The Source ─────────────────────────────────────────────────
    // Mace reads the first page: "What did this element look like before?"
    // The source is the original "before" snapshot we're comparing from.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the "before" schema object — the original element we're comparing
     * from.
     * This is {@code null} when the element is newly {@link DifferenceType#ADDED}
     * and didn't exist in the original schema.
     *
     * <p>For example — Mace reads the "before" column:</p>
     * <pre>
     *   Object src = diff.getSource();
     *   // "This is what AttributeType 'cn' looked like before the change."
     * </pre>
     *
     * @return  the source object; {@code null} if the element is newly added
     */
    public Object getSource()
    {
        return source;
    }


    // ── Mace Updates The Source ───────────────────────────────────────────────
    // A framework hook; rarely used after construction.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Replaces the "before" schema object on this difference record.
     * Like {@link #setDestination}, this is almost never needed after
     * construction; it exists for completeness and incremental building.
     *
     * <p>For example — Mace updates the reference point:</p>
     * <pre>
     *   diff.setSource( correctedOriginalAttributeType );
     * </pre>
     *
     * @param source  the new "before" object; may be {@code null}
     */
    public void setSource( Object source )
    {
        this.source = source;
    }


    // ── Mace Reads The Verdict ────────────────────────────────────────────────
    // Mace lifts the cover sheet and reads the stamped verdict: IDENTICAL, ADDED,
    // MODIFIED, or REMOVED.  The UI uses this to decide which icon to render.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the verdict — whether this element was added, removed, modified,
     * or unchanged since the last comparison.
     * The UI renders different icons for each verdict (green +, red -, orange ~).
     *
     * <p>For example — Mace reads the stamped verdict:</p>
     * <pre>
     *   DifferenceType verdict = diff.getType();
     *   // MODIFIED → render an orange "~" next to the schema element in the UI
     * </pre>
     *
     * @return  the {@link DifferenceType} verdict; may be {@code null} if not yet set
     */
    public DifferenceType getType()
    {
        return type;
    }


    // ── Mace Updates The Verdict ─────────────────────────────────────────────
    // The engine initially stamps IDENTICAL on parent containers and then
    // escalates them to MODIFIED if any child property turns out to have changed.
    // This is the escalation call.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Updates the verdict on this difference — most commonly used to escalate
     * an IDENTICAL parent diff to MODIFIED once a child property change is found.
     * The difference engine calls this on the containing {@link SchemaDifference}
     * and on the element-level diff to keep the whole hierarchy consistent.
     *
     * <p>For example — Mace escalates the verdict:</p>
     * <pre>
     *   // Schema was initially IDENTICAL, then a child AT changed:
     *   schemaDifference.setType( DifferenceType.MODIFIED );
     * </pre>
     *
     * @param type  the new {@link DifferenceType} verdict to stamp on this record
     */
    public void setType( DifferenceType type )
    {
        this.type = type;
    }
}
