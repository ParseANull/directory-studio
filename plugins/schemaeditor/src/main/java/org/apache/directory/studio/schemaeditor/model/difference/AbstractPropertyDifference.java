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


// ── CLASS: AbstractPropertyDifference — Mace Notes The Specific Allegation ───
// After Mace Windu establishes the case (source, destination, verdict), he
// needs to record the exact allegation: "The description USED to be X, and it
// is NOW Y."  That's what this class adds on top of AbstractDifference — an
// old value and a new value so we can show the precise change in the UI.
// Every concrete property-level difference (AliasDifference, SyntaxDifference,
// etc.) extends this instead of AbstractDifference directly.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Extends {@link AbstractDifference} with an old value and a new value, capturing
 * the exact property change rather than just the schema objects that changed.
 * Concrete property differences (like {@link AliasDifference} or
 * {@link DescriptionDifference}) extend this to get old/new value storage for free.
 * Think of it as Mace Windu's detailed allegation sheet — beyond "this object
 * changed," it records "this specific field went from X to Y."
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractPropertyDifference extends AbstractDifference implements PropertyDifference
{
    /** The old value*/
    private Object oldValue;

    /** The new value */
    private Object newValue;


    // ── Mace Records Allegation With A Pre-Stamped Verdict ───────────────────
    // When the engine already knows whether the property was added, removed, or
    // modified (e.g. it only exists on one side), we construct with the verdict
    // already filled in.  The old/new values are set separately via setters.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a property difference with source, destination, and verdict
     * already set; old and new values are filled in via {@link #setOldValue}
     * and {@link #setNewValue} after construction.
     * Use this when the {@link DifferenceType} is determined at detection time
     * (e.g. alias only in source → REMOVED).
     *
     * <p>For example — Mace stamps the verdict before reading the detail:</p>
     * <pre>
     *   new AliasDifference( at1, at2, DifferenceType.REMOVED )
     *   // "The alias existed before but is gone — verdict pre-stamped REMOVED."
     *   diff.setOldValue( "commonName" );
     * </pre>
     *
     * @param source       the original "before" schema object
     * @param destination  the new "after" schema object
     * @param type         the {@link DifferenceType} verdict
     */
    public AbstractPropertyDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Opens The Allegation Sheet Without A Verdict ────────────────────
    // Sometimes we don't know the verdict yet at construction time — a separate
    // pass will determine it.  This constructor defers the verdict and still
    // lets us hold the source and destination references.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates a property difference with source and destination but no verdict yet.
     * The {@link DifferenceType} will be set later via {@link #setType}.
     * We use this less often — most concrete subclasses know the type immediately
     * and use the three-argument constructor instead.
     *
     * <p>For example — Mace opens the sheet before the full evidence review:</p>
     * <pre>
     *   new DescriptionDifference( at1, at2 );
     *   // Type and values TBD; set them once the evidence is checked.
     * </pre>
     *
     * @param source       the original "before" schema object
     * @param destination  the new "after" schema object
     */
    public AbstractPropertyDifference( Object source, Object destination )
    {
        super( source, destination );
    }


    // ── Mace Reads The New Value ──────────────────────────────────────────────
    // Mace reads the right column of the allegation: "And now it is Y."
    // For an ADDED property this is the only value that matters; for MODIFIED
    // both old and new are present.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value the property has in the destination (the "after" value).
     * For {@link DifferenceType#ADDED} diffs this is the only relevant value;
     * for MODIFIED it's paired with {@link #getOldValue()}.
     *
     * <p>For example — Mace reads the right column:</p>
     * <pre>
     *   Object newDesc = diff.getNewValue();
     *   // "The description is NOW: 'Common name of a person'"
     * </pre>
     *
     * @return  the new property value; {@code null} for REMOVED diffs
     */
    public Object getNewValue()
    {
        return newValue;
    }


    // ── Mace Records The New Value ────────────────────────────────────────────
    // The engine calls this immediately after constructing the diff object to
    // record what the property became in the destination schema.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the property value from the destination — the "after" state.
     * We call this right after construction when we know what the property
     * changed to (or what was newly introduced for an ADDED diff).
     *
     * <p>For example — Mace fills in the right column:</p>
     * <pre>
     *   diff.setNewValue( "Updated description text" );
     * </pre>
     *
     * @param newValue  the new property value; pass {@code null} for REMOVED diffs
     */
    public void setNewValue( Object newValue )
    {
        this.newValue = newValue;
    }


    // ── Mace Reads The Old Value ──────────────────────────────────────────────
    // Mace reads the left column: "It used to be X."  For a REMOVED property
    // this is the only value; for MODIFIED both columns are filled.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the value the property had in the source — the "before" state.
     * For {@link DifferenceType#REMOVED} diffs this is the only relevant value;
     * for MODIFIED it's paired with {@link #getNewValue()}.
     *
     * <p>For example — Mace reads the left column:</p>
     * <pre>
     *   Object oldDesc = diff.getOldValue();
     *   // "The description WAS: 'cn'"
     * </pre>
     *
     * @return  the old property value; {@code null} for ADDED diffs
     */
    public Object getOldValue()
    {
        return oldValue;
    }


    // ── Mace Records The Old Value ────────────────────────────────────────────
    // The engine calls this to record what the property was before the change,
    // giving the UI something to display in a "before/after" comparison table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Sets the property value from the source — the "before" state.
     * We call this right after construction to record the original value so the
     * UI can show a side-by-side before/after comparison.
     *
     * <p>For example — Mace fills in the left column:</p>
     * <pre>
     *   diff.setOldValue( "cn" );
     *   // "The alias 'cn' was present before — it's now gone."
     * </pre>
     *
     * @param oldValue  the old property value; pass {@code null} for ADDED diffs
     */
    public void setOldValue( Object oldValue )
    {
        this.oldValue = oldValue;
    }
}
