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


// ── CLASS: SchemaDifference — THE JEDI COUNCIL'S COMPLETE CASE FILE ──────────
// Before Mace Windu can confront Palpatine, the Jedi Council assembles a complete
// case file: every attribute-type charge and every object-class charge, organized
// into two distinct folders and handed to Mace before he walks through that door.
// This class is that case file — it holds the schema-level verdict (ADDED, REMOVED,
// MODIFIED) and the full list of attribute-type and object-class differences inside.
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Captures the complete set of differences between two versions of an LDAP schema.
 * A schema contains attribute type definitions and object class definitions, so
 * this class holds two separate lists — one for each.
 * Think of it as the Jedi Council's full case file against Chancellor Palpatine:
 * every attribute-type charge and every object-class charge, organized before
 * Mace walks through the door.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaDifference extends AbstractDifference
{
    /** The attribute types differences*/
    private List<AttributeTypeDifference> attributeTypesDifferences = new ArrayList<AttributeTypeDifference>();

    /** The object classes differences */
    private List<ObjectClassDifference> objectClassesDifferences = new ArrayList<ObjectClassDifference>();


    // ── Council Opens The Case File With A Verdict Already In Hand ────────────────
    // Yoda has already conferred with Mace — "A Sith Lord he has become."
    // The high-level verdict (ADDED, REMOVED, MODIFIED) is known before we
    // start cataloguing the individual charges.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SchemaDifference with an explicit difference type.
     * Use this when the overall verdict for the schema is already known — e.g., it was
     * newly created (ADDED) or completely removed (REMOVED).
     *
     * <p>For example — Mace receives the pre-classified file:</p>
     * <pre>
     *   SchemaDifference diff =
     *       new SchemaDifference(oldSchema, newSchema, DifferenceType.MODIFIED);
     *   // "The Council is certain: the schema has changed."
     * </pre>
     *
     * @param source       the original schema (before any changes); may be {@code null} for ADDED
     * @param destination  the updated schema (after changes); may be {@code null} for REMOVED
     * @param type         the overall verdict: ADDED, REMOVED, or MODIFIED
     */
    public SchemaDifference( Object source, Object destination, DifferenceType type )
    {
        super( source, destination, type );
    }


    // ── Council Opens The File Before Classifying ─────────────────────────────────
    // The Council knows something has changed between the two schema versions
    // but hasn't formally classified it yet — individual charges will arrive
    // and the verdict will crystallize from them.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new SchemaDifference using the default difference type (MODIFIED).
     * Use this when you know two schema versions differ but want to let the
     * accumulated property changes inform the final verdict.
     *
     * @param source       the original schema (before changes)
     * @param destination  the updated schema (after changes)
     */
    public SchemaDifference( Object source, Object destination )
    {
        super( source, destination );
    }


    // ── Reading The Attribute-Type Charges ───────────────────────────────────────
    // Mace opens the first folder — every attribute-type charge is there,
    // ready to be read aloud in the Chancellor's office.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of attribute type differences recorded for this schema.
     * Callers iterate this to enumerate which attribute types were added, removed, or changed.
     *
     * @return  the list of {@link AttributeTypeDifference} objects; never {@code null}
     */
    public List<AttributeTypeDifference> getAttributeTypesDifferences()
    {
        return attributeTypesDifferences;
    }


    // ── Filing One Attribute-Type Charge ─────────────────────────────────────────
    // Kit Fisto adds one evidence card to the attribute-type folder.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single attribute type difference to this schema's change record.
     * Called by {@code DifferenceEngine} as it works through the schema's attribute types.
     *
     * @param difference  the attribute type change to record; must not be {@code null}
     */
    public void addAttributeTypeDifference( AttributeTypeDifference difference )
    {
        attributeTypesDifferences.add( difference );
    }


    // ── Withdrawing An Attribute-Type Charge ──────────────────────────────────────
    // Saesee Tiin pulls a card from the folder — the evidence was found to be
    // misattributed and must be removed from the record.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific attribute type difference from this schema's change record.
     * Called when a diff was added in error and needs to be retracted before reporting.
     *
     * @param difference  the attribute type change to remove; a no-op if not present
     */
    public void removeAttributeTypeDifference( AttributeTypeDifference difference )
    {
        attributeTypesDifferences.remove( difference );
    }


    // ── Reading The Object-Class Charges ─────────────────────────────────────────
    // Mace opens the second folder — every object-class charge is there,
    // compiled by Agen Kolar during the overnight investigation.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the live list of object class differences recorded for this schema.
     * Callers iterate this to enumerate which object classes were added, removed, or changed.
     *
     * @return  the list of {@link ObjectClassDifference} objects; never {@code null}
     */
    public List<ObjectClassDifference> getObjectClassesDifferences()
    {
        return objectClassesDifferences;
    }


    // ── Filing One Object-Class Charge ───────────────────────────────────────────
    // Agen Kolar adds one evidence card to the object-class folder.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Appends a single object class difference to this schema's change record.
     * Called by {@code DifferenceEngine} as it works through the schema's object classes.
     *
     * @param difference  the object class change to record; must not be {@code null}
     */
    public void addObjectClassDifference( ObjectClassDifference difference )
    {
        objectClassesDifferences.add( difference );
    }


    // ── Withdrawing An Object-Class Charge ────────────────────────────────────────
    // A card is pulled from the object-class folder — it didn't belong here.
    // ────────────────────────────────────────────────────────────────────────────────
    /**
     * Removes a specific object class difference from this schema's change record.
     * Called when a diff was added in error and needs to be retracted before reporting.
     *
     * @param difference  the object class change to remove; a no-op if not present
     */
    public void removeObjectClassDifference( ObjectClassDifference difference )
    {
        objectClassesDifferences.remove( difference );
    }
}
