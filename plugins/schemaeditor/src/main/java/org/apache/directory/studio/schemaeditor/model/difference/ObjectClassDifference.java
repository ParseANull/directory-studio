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


// ── CLASS: ObjectClassDifference — MACE WINDU COMPILES THE DOSSIER ───────────
// Mace Windu and three Jedi Masters stride into Chancellor Palpatine's office
// with a full dossier of charges: every property that has changed is enumerated
// and recorded before the verdict is delivered.
// This class is that dossier — it identifies the object class being examined
// (source vs. destination) and accumulates all property-level charges against it.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Captures the full set of differences found between a source and destination object class.
 * It lives inside the {@code model.difference} package, which is used by
 * {@code DifferenceEngine} to compare two versions of a schema and report what changed.
 * Think of this class as Mace Windu's dossier — it names the suspect (the object class)
 * and collects every charge (property difference) in one place before the verdict is rendered.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectClassDifference extends AbstractDifference
{
    /** The differences */
    private List<PropertyDifference> differences = new ArrayList<PropertyDifference>();


    // ── Three Jedi Enter The Chancellor's Office ─────────────────────────────────
    // Mace Windu arrives with Kit Fisto, Saesee Tiin, and Agen Kolar — each
    // carrying a specific piece of evidence against Palpatine.
    // We create the dossier here: we know exactly which object class is on trial
    // (source vs. destination) and why the tribunal was convened (the type).
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ObjectClassDifference with an explicit difference type (ADDED, REMOVED, or MODIFIED).
     * Use this when you already know the high-level verdict — e.g., the object class was added outright.
     *
     * <p>For example — Mace names the charge before the door opens:</p>
     * <pre>
     *   ObjectClassDifference diff =
     *       new ObjectClassDifference(sourceOC, destOC, DifferenceType.ADDED);
     *   // "Chancellor Palpatine, in the name of the Galactic Senate — you are under arrest."
     * </pre>
     *
     * @param source       the original (before) object class; may be {@code null} if the class was added
     * @param destination  the updated (after) object class; may be {@code null} if the class was removed
     * @param type         the high-level verdict: ADDED, REMOVED, or MODIFIED
     */
    public ObjectClassDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Jedi Sense The Disturbance Without A Label ───────────────────────────────
    // Before the confrontation, Mace simply knows something has changed — the
    // Force whispers that Palpatine is not the same Chancellor he was.
    // We create the dossier without pre-judging the verdict; the type defaults
    // to MODIFIED and will be refined as property differences accumulate.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new ObjectClassDifference without specifying a type — the type defaults to MODIFIED.
     * Useful when the engine detects a change but needs to inspect properties before deciding the verdict.
     *
     * <p>For example — Mace opens the dossier before reading the charges:</p>
     * <pre>
     *   ObjectClassDifference diff =
     *       new ObjectClassDifference(sourceOC, destOC);
     *   // Mace senses the disturbance; specific charges come later.
     * </pre>
     *
     * @param source       the original object class before any changes
     * @param destination  the updated object class after changes were applied
     */
    public ObjectClassDifference( Object source, Object destination )
    {
        super( source, destination );
    }


    // ── Reading The Full List Of Charges ─────────────────────────────────────────
    // Mace spreads the dossier on the table — every line item of evidence against
    // Palpatine is visible at a glance.
    // This method hands callers the complete live list of property diffs so they
    // can iterate, display, or further filter without making a copy.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of property differences accumulated for this object class.
     * Callers — usually the diff reporter or a UI view — iterate this to display what changed.
     * The list is mutable; don't hold onto it across structural changes.
     *
     * @return  the list of {@link PropertyDifference} objects recorded so far; never {@code null}
     */
    public List<PropertyDifference> getDifferences()
    {
        return differences;
    }


    // ── Adding One More Charge To The Dossier ────────────────────────────────────
    // Kit Fisto slides a single evidence card across the table — one property
    // that has changed, timestamped and attributed.
    // We append that single property difference to the running list.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single property difference to this object class's change record.
     * Called by {@code DifferenceEngine} each time it finds one property that differs
     * between source and destination.
     *
     * @param difference  the specific property change to record; must not be {@code null}
     */
    public void addDifference( PropertyDifference difference )
    {
        differences.add( difference );
    }


    // ── Bulk-Filing All Charges At Once ──────────────────────────────────────────
    // Saesee Tiin drops a whole folder of evidence on the table — every charge
    // against Palpatine collected from a sub-investigation, added in one motion.
    // We batch-add a list of property differences rather than looping from outside.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a whole batch of property differences to this object class's change record.
     * Useful when a sub-engine produces a list of diffs that should all be associated
     * with this object class in one shot.
     *
     * @param differences  the collection of property changes to add; must not be {@code null}
     */
    public void addDifferences( List<PropertyDifference> differences )
    {
        this.differences.addAll( differences );
    }


    // ── Striking A Charge From The Record ────────────────────────────────────────
    // Agen Kolar realizes one piece of evidence was misattributed — he pulls
    // it from the folder so it doesn't cloud the final verdict.
    // We remove a specific property difference that no longer belongs here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific property difference from this object class's change record.
     * Called when a diff turns out to be spurious or was added in error — keeps
     * the dossier clean before the final report is generated.
     *
     * @param difference  the property change to remove; a no-op if it was never added
     */
    public void removeDifference( PropertyDifference difference )
    {
        differences.remove( difference );
    }
}
