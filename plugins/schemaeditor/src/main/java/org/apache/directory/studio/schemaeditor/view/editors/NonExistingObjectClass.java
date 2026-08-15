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
package org.apache.directory.studio.schemaeditor.view.editors;


// ── CLASS: NonExistingObjectClass — LEIA'S HOLOGRAM, "HELP ME OBI-WAN" ──────────────
// Leia's hologram in R2-D2 names "the Rebel Alliance" and "General Kenobi" — real
// entities with real names — but neither of them is physically inside R2-D2's memory
// banks.  R2 carries the reference so the message can be delivered, not the thing itself.
// An object class definition can name a superior class — "sup: top" — or a
// schema definition might reference an object class that simply isn't loaded yet.
// This class is the hologram: it holds that name so the combo box can show it and
// let the user fix or confirm the reference, without crashing on a missing ObjectClass.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Placeholder that represents an object class referenced by name in a schema definition
 * but not actually present in the currently loaded schema.
 * Object class editors use this when a superior class or some other referenced class
 * can't be resolved to a real ObjectClass object — we preserve the name visually rather
 * than silently discarding the unresolvable reference.
 * Think of this as Leia's hologram: the name is there, the intent is clear, but the
 * real object isn't in the room.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NonExistingObjectClass
{
    public static final String NONE = "(None)"; //$NON-NLS-1$

    /** The name */
    private String name;


    // ── R2-D2 Stores the Hologram Reference ─────────────────────────────────────────
    // R2-D2 locks in Leia's message — just the name tag and the content, nothing more.
    // No lookup is attempted at this stage; we only need the name as a stable carrier.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new placeholder for an object class that doesn't exist in the schema.
     * We store the name string only — no validation, no schema lookup.
     * Pass {@link #NONE} to represent "no selection" in a combo box.
     *
     * <p>For example — R2 stores the unresolvable class reference:</p>
     * <pre>
     *   // The schema says "sup: missingClass" but missingClass isn't loaded:
     *   NonExistingObjectClass placeholder = new NonExistingObjectClass( "missingClass" );
     * </pre>
     *
     * @param name  the raw object class name from the schema definition; also accepts
     *              {@link #NONE} for the "no selection" sentinel
     */
    public NonExistingObjectClass( String name )
    {
        this.name = name;
    }


    // ── Obi-Wan Asks for the Sender's Name ──────────────────────────────────────────
    // Obi-Wan watches the hologram and asks R2, "Who sent this?"  R2 plays back the
    // exact name stored in memory — undecorated, unmodified.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw object class name this placeholder was constructed with.
     * Use this for model operations (comparisons, writing back to schema) where you
     * need the undecorated value.  Use {@link #getDisplayName()} for UI display.
     *
     * @return  the stored name exactly as passed to the constructor
     */
    public String getName()
    {
        return name;
    }


    // ── Obi-Wan Reads the Hologram Aloud with Full Context ──────────────────────────
    // Obi-Wan doesn't just mutter the name to himself — he reads it aloud to Luke with
    // enough context to understand the situation.  If it's the "(None)" sentinel, a
    // short response is all that's needed.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a display-ready string for use in combo boxes and labels.
     * Returns "(None)" unchanged for the sentinel value, or appends a warning note for
     * any real-but-missing object class name so the user can spot the problem.
     *
     * <p>For example — Obi-Wan gives the full context:</p>
     * <pre>
     *   // name = "(None)"       → "(None)"
     *   // name = "missingClass" → "missingClass   (This object class doesnt exist)"
     * </pre>
     *
     * @return  either "(None)" or the name followed by a missing-class warning annotation
     */
    public String getDisplayName()
    {
        if ( name.equals( NONE ) )
        {
            return NONE;
        }
        else
        {
            return name + "   " + "(This object class doesnt exist)";
        }
    }


    // ── Obi-Wan Checks if Two Holograms Reference the Same Entity ───────────────────
    // Two holograms that both name "the Rebel Alliance" are considered the same reference
    // regardless of how the name was capitalised in the recording.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks equality by comparing stored names case-insensitively.
     * LDAP object class names are case-insensitive, so "inetOrgPerson" and "INETORGPERSON"
     * refer to the same thing; we reflect that here to avoid duplicate entries in combo
     * input deduplication.
     *
     * <p>For example — two spellings of the same class name are equal:</p>
     * <pre>
     *   new NonExistingObjectClass( "inetOrgPerson" )
     *       .equals( new NonExistingObjectClass( "INETORGPERSON" ) )
     *   // → true
     * </pre>
     *
     * @param obj  the object to compare; non-NonExistingObjectClass instances → false
     * @return     {@code true} if obj is a NonExistingObjectClass with the same name
     *             (ignoring case)
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof NonExistingObjectClass )
        {
            return name.equalsIgnoreCase( ( ( NonExistingObjectClass ) obj ).getName() );
        }

        return false;
    }
}
