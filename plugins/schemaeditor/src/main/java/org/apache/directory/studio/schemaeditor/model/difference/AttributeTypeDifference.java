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


import java.util.ArrayList;
import java.util.List;


// ── CLASS: AttributeTypeDifference — Mace Windu's Full Case File ─────────────
// When Mace confronts Palpatine he doesn't just say "you changed" — he holds a
// thick folder of specific allegations: "Your aliases changed (charge 1), your
// description changed (charge 2), your syntax changed (charge 3)..."
// AttributeTypeDifference is that thick folder for an LDAP AttributeType.  It
// wraps the overall verdict (ADDED / REMOVED / MODIFIED) AND carries a list of
// PropertyDifference sub-charges that spell out exactly which fields changed.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Represents the complete difference record for a single LDAP AttributeType
 * between two schema snapshots.
 * Beyond the overall verdict (ADDED, REMOVED, MODIFIED, IDENTICAL), it holds a
 * list of {@link PropertyDifference} objects — one per property that changed —
 * so the UI can display a granular before/after table.
 * Think of it as Mace Windu's case file on a suspect: the cover says "MODIFIED,"
 * and inside are the individual charge sheets listing exactly what changed.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeDifference extends AbstractDifference
{
    /** The differences */
    private List<PropertyDifference> differences = new ArrayList<PropertyDifference>();


    // ── Mace Opens The File With The Verdict Pre-Stamped ─────────────────────
    // When the engine already knows the verdict (ADDED because this AT only
    // exists in the destination, REMOVED because it only exists in the source),
    // it stamps the verdict up front and optionally adds property sub-charges
    // later via addDifference / addDifferences.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an attribute type difference record with verdict already known.
     * Property-level details (which specific fields changed) are added afterwards
     * via {@link #addDifference} or {@link #addDifferences}.
     *
     * <p>For example — Mace opens a REMOVED case file:</p>
     * <pre>
     *   AttributeTypeDifference atDiff =
     *       new AttributeTypeDifference( oldAt, null, DifferenceType.REMOVED );
     *   // "AttributeType 'telephoneNumber' is gone — case filed, verdict: REMOVED."
     * </pre>
     *
     * @param source       the original "before" AttributeType; may be {@code null} for ADDED
     * @param destination  the new "after" AttributeType; may be {@code null} for REMOVED
     * @param type         the {@link DifferenceType} verdict
     */
    public AttributeTypeDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Mace Opens The File Without A Verdict Yet ─────────────────────────────
    // Used when the engine needs to create the record before it has finished
    // comparing all properties — the type is set later once the full review is
    // done and it's clear whether anything actually changed.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Creates an attribute type difference record without a verdict; the type is
     * determined and set later via {@link #setType}.
     * The engine initially creates these as IDENTICAL and escalates to MODIFIED
     * if the property inspection loop finds any changes.
     *
     * <p>For example — Mace opens the file before the full review:</p>
     * <pre>
     *   AttributeTypeDifference atDiff = new AttributeTypeDifference( at1, at2 );
     *   // Review begins; if any property diffs found, type → MODIFIED
     * </pre>
     *
     * @param source       the original "before" AttributeType
     * @param destination  the new "after" AttributeType
     */
    public AttributeTypeDifference( Object source, Object destination )
    {
        super( source, destination );
    }


    // ── Mace Opens The Sub-Charge Folder ─────────────────────────────────────
    // Mace reads all the individual allegations: alias changes, description
    // changes, syntax changes, etc.  This getter hands over the entire list so
    // the UI can iterate and render each one in the diff table.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Returns the list of property-level differences that make up this attribute
     * type change — one {@link PropertyDifference} per changed field.
     * The UI iterates this list to render a before/after table showing exactly
     * which fields changed and what their old and new values were.
     *
     * <p>For example — Mace reads the sub-charge folder:</p>
     * <pre>
     *   for ( PropertyDifference pd : atDiff.getDifferences() ) {
     *       // render "Description: 'old' → 'new'" in the diff UI
     *   }
     * </pre>
     *
     * @return  the mutable list of property differences; never {@code null}, may be empty
     */
    public List<PropertyDifference> getDifferences()
    {
        return differences;
    }


    // ── Mace Adds One Charge To The File ─────────────────────────────────────
    // The engine calls this each time it detects a single property change —
    // an alias added, a description changed, etc.  One call per change.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single property-level difference to this attribute type's
     * charge list.
     * We call this for each changed property detected by the engine's property
     * comparison loop.
     *
     * <p>For example — Mace adds a charge:</p>
     * <pre>
     *   atDiff.addDifference( new DescriptionDifference( at1, at2, DifferenceType.MODIFIED ) );
     * </pre>
     *
     * @param difference  the {@link PropertyDifference} to append; must not be {@code null}
     */
    public void addDifference( PropertyDifference difference )
    {
        differences.add( difference );
    }


    // ── Mace Staples A Batch Of Charges To The File ───────────────────────────
    // When the engine finishes comparing a group of properties (like all aliases)
    // it may have several differences to add at once.  This bulk-add saves
    // repeated single calls.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Appends a batch of property-level differences to this attribute type's
     * charge list in one call.
     * The engine typically calls this after comparing all aliases or all superior
     * classes at once, where the result is already a list.
     *
     * <p>For example — Mace staples the alias batch to the file:</p>
     * <pre>
     *   List&lt;PropertyDifference&gt; aliasChanges = getAliasesDifferences( at1, at2 );
     *   atDiff.addDifferences( aliasChanges );
     * </pre>
     *
     * @param differences  the list of {@link PropertyDifference} objects to add; must not be {@code null}
     */
    public void addDifferences( List<PropertyDifference> differences )
    {
        this.differences.addAll( differences );
    }


    // ── Mace Drops A Charge From The File ────────────────────────────────────
    // Rarely used in practice — but the API allows removing a specific charge
    // if the engine or tests need to undo a previously added difference.
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific property-level difference from this attribute type's
     * charge list.
     * Not commonly called at runtime — mostly useful in tests or if the engine
     * needs to reverse a tentative diff it added earlier.
     *
     * <p>For example — Mace withdraws a charge:</p>
     * <pre>
     *   atDiff.removeDifference( falseAliasDiff );
     *   // "On reflection, that alias change wasn't real — charge withdrawn."
     * </pre>
     *
     * @param difference  the {@link PropertyDifference} to remove; no-op if not present
     */
    public void removeDifference( PropertyDifference difference )
    {
        differences.remove( difference );
    }
}
