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


// ── CLASS: NonExistingSyntax — LEIA'S HOLOGRAM, "HELP ME OBI-WAN" ───────────────────
// Leia's hologram describes the Death Star plans in detail — she knows what they are,
// she references them by name — but neither the plans nor Leia are physically there
// in R2-D2's memory banks.  R2 carries the description so the mission can be understood,
// not the physical plans themselves.
// An attribute type might reference an LDAP syntax by OID — say
// "1.3.6.1.4.1.1466.115.121.1.15" — but if that syntax OID isn't in the loaded schema,
// there is no real LdapSyntax object to bind.  This class is the hologram: it holds the
// description (or OID) so the syntax combo can display and select it even when the
// real syntax object is absent, letting the user correct the reference rather than losing
// the value silently.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Placeholder that represents an LDAP syntax referenced by description or OID in an
 * attribute type definition but not actually present in the currently loaded schema.
 * The attribute type editor uses this when the syntax OID stored in an attribute type
 * can't be resolved to a real LdapSyntax object — the value is preserved and flagged
 * for the user rather than silently dropped.
 * Think of this as Leia's hologram: the description is there, the intent is clear,
 * but the real LdapSyntax object isn't in the room.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NonExistingSyntax
{
    /** The None syntax description */
    public static final String NONE = "(None)"; //$NON-NLS-1$

    /** The description */
    private String description;


    // ── R2-D2 Stores the Mission Briefing ───────────────────────────────────────────
    // R2-D2 locks in the description of the plans — just the words, not the plans
    // themselves.  No resolution attempt is made at this point; we simply store
    // the description for later display and comparison.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new placeholder for a syntax that doesn't exist in the loaded schema.
     * We store only the description string (which in practice is often the syntax OID
     * when no human-readable description is available).
     * Pass {@link #NONE} to represent "no syntax selected."
     *
     * <p>For example — R2 stores the unresolvable syntax reference:</p>
     * <pre>
     *   // AT says syntax OID "9.9.9.99.99" but we don't recognise it:
     *   NonExistingSyntax placeholder = new NonExistingSyntax( "9.9.9.99.99" );
     * </pre>
     *
     * @param description  the syntax description or OID string from the schema definition;
     *                     also accepts {@link #NONE} for the "no selection" sentinel
     */
    public NonExistingSyntax( String description )
    {
        this.description = description;
    }


    // ── Obi-Wan Asks What the Plans Are Called ───────────────────────────────────────
    // Obi-Wan queries R2: "What does Leia say the plans are?"  R2 plays back the
    // description exactly as stored — raw, undecorated.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw description string this placeholder was constructed with.
     * Use this for model operations (comparisons, writing back to the attribute type's
     * syntax OID) where the undecorated value is needed.
     * Use {@link #getDisplayName()} for user-facing display strings.
     *
     * @return  the stored description exactly as passed to the constructor
     */
    public String getDescription()
    {
        return description;
    }


    // ── Obi-Wan Reads the Briefing Aloud with Context ───────────────────────────────
    // Obi-Wan tells Luke about the plans: "She calls them 'the Death Star blueprints'
    // — but we don't have the actual plans yet, so handle with care."  If it's the
    // "(None)" sentinel, he says "(None)" and moves on with no fuss.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a display-ready string for use in combo boxes and labels.
     * Returns "(None)" for the sentinel value unchanged, or appends a warning for any
     * real-but-unresolvable syntax description so the user can spot and fix the issue.
     *
     * <p>For example — Obi-Wan delivers the full briefing with caveat:</p>
     * <pre>
     *   // description = "(None)"  → "(None)"
     *   // description = "9.9.9.1" → "9.9.9.1   (This syntax doesnt exist)"
     * </pre>
     *
     * @return  either "(None)" or the description followed by a missing-syntax warning
     */
    public String getDisplayName()
    {
        if ( description.equals( NONE ) )
        {
            return NONE;
        }
        else
        {
            return description + "   " + "(This syntax doesnt exist)";
        }
    }


    // ── Obi-Wan Checks if Two Briefings Describe the Same Plans ─────────────────────
    // Two briefings that both say "the Death Star blueprints" describe the same thing
    // regardless of how the phrase was capitalised in Leia's recording.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks equality by comparing descriptions case-insensitively.
     * LDAP syntax descriptions are generally treated case-insensitively when used as
     * identifiers; we mirror that here so combo-input deduplication works correctly
     * across differently-cased references to the same syntax.
     *
     * <p>For example — two casings of the same description are equal:</p>
     * <pre>
     *   new NonExistingSyntax( "Directory String" )
     *       .equals( new NonExistingSyntax( "DIRECTORY STRING" ) )
     *   // → true
     * </pre>
     *
     * @param obj  the object to compare; non-NonExistingSyntax instances → false
     * @return     {@code true} if obj is a NonExistingSyntax with the same description
     *             (ignoring case)
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof NonExistingSyntax )
        {
            return description.equalsIgnoreCase( ( ( NonExistingSyntax ) obj ).getDescription() );
        }

        return false;
    }
}
