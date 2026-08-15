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


// ── CLASS: NonExistingMatchingRule — LEIA'S HOLOGRAM, "HELP ME OBI-WAN" ─────────────
// R2-D2 carries Leia's hologram across the galaxy: her name and message are perfectly
// preserved, but Leia herself is somewhere else entirely.  The same gap applies here.
// An attribute type definition might reference an equality matching rule by name —
// say "caseIgnoreMatch" — but if that matching rule isn't in the loaded schema, there
// is no real MatchingRule object for the editor to bind to.  This class is the
// hologram: it holds the name so the combo can display and select it without crashing,
// and it flags the name visually so the user knows the rule is unresolved.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Placeholder that represents a matching rule referenced by name in an attribute type
 * definition but not actually present in the currently loaded schema.
 * The attribute type editor uses this when the equality, ordering, or substring matching
 * rule OID/name stored in an attribute type can't be resolved to a real MatchingRule
 * object — rather than hiding the value, we show it with a warning so the user can fix
 * the schema instead of silently losing data.
 * Think of this as Leia's hologram: the name is there, the intent is clear, but the
 * real object isn't in the room.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NonExistingMatchingRule
{
    /** The None matching rule name */
    public static final String NONE = "(None)";

    /** The name */
    private String name;


    // ── R2-D2 Records the Hologram Message ──────────────────────────────────────────
    // R2-D2 records Leia's message — just her name and words, stored internally.
    // That name is all this placeholder needs; no lookup, no validation at construction.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new placeholder for a matching rule that doesn't exist in the schema.
     * We store only the name string; this is a pure carrier object for the combo input
     * list to bind its selection to when the real MatchingRule can't be found.
     * Pass {@link #NONE} as the name to represent "no matching rule selected."
     *
     * <p>For example — R2 records the unresolvable reference:</p>
     * <pre>
     *   // The AT says "equality: unknownMatchingRule" but it's not in the schema:
     *   NonExistingMatchingRule placeholder = new NonExistingMatchingRule( "unknownMatchingRule" );
     * </pre>
     *
     * @param name  the raw matching rule name or OID from the schema definition;
     *              also accepts {@link #NONE} for the "no selection" sentinel
     */
    public NonExistingMatchingRule( String name )
    {
        this.name = name;
    }


    // ── Obi-Wan Asks What Her Name Is ───────────────────────────────────────────────
    // Obi-Wan queries R2 for the sender's name — he gets back exactly what was recorded,
    // no transformation, no decoration.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw matching rule name this placeholder was constructed with.
     * This is the undecorated value from the schema — useful for comparisons and for
     * writing back to the model.  Use {@link #getDisplayName()} for user-facing strings.
     *
     * @return  the stored name exactly as passed to the constructor
     */
    public String getName()
    {
        return name;
    }


    // ── Obi-Wan Reads the Full Briefing with Context ────────────────────────────────
    // When Obi-Wan shares the message with Luke, he provides context: what the name
    // means, and a note that this person isn't physically here.  If it's the "(None)"
    // sentinel, he just says "(None)" — no drama needed.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a display-ready string for use in combo boxes and labels.
     * If this is the "no selection" sentinel the name equals {@link #NONE} and we return
     * it as-is.  Otherwise we append a warning so the user can see at a glance that the
     * referenced matching rule isn't in the loaded schema and should be corrected.
     *
     * <p>For example — Obi-Wan gives the full context:</p>
     * <pre>
     *   // name = "(None)"          → "(None)"
     *   // name = "badMatchingRule" → "badMatchingRule   (This matching rule doesnt exist)"
     * </pre>
     *
     * @return  either "(None)" or the name with a missing-rule warning annotation
     */
    public String getDisplayName()
    {
        if ( name.equals( NONE ) )
        {
            return NONE;
        }
        else
        {
            return name + "   " + "(This matching rule doesnt exist)";
        }
    }


    // ── Obi-Wan Checks if Two Holograms Are the Same Message ────────────────────────
    // Two holograms that both say "from Princess Leia" are the same message regardless
    // of how the name was pronounced when recorded.  Case doesn't matter for identity.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks equality by comparing names case-insensitively.
     * LDAP matching rule names are case-insensitive ("caseIgnoreMatch" == "CASEIGNOREMATCH"),
     * so we use equalsIgnoreCase to avoid treating logically-identical names as different
     * when deduplicating combo-input children.
     *
     * <p>For example — two spellings of the same rule name are equal:</p>
     * <pre>
     *   new NonExistingMatchingRule( "caseIgnoreMatch" )
     *       .equals( new NonExistingMatchingRule( "CASEIGNOREMATCH" ) )
     *   // → true
     * </pre>
     *
     * @param obj  the object to compare; non-NonExistingMatchingRule instances → false
     * @return     {@code true} if obj is a NonExistingMatchingRule with the same name
     *             (ignoring case)
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof NonExistingMatchingRule )
        {
            return name.equalsIgnoreCase( ( ( NonExistingMatchingRule ) obj ).getName() );
        }

        return false;
    }
}
