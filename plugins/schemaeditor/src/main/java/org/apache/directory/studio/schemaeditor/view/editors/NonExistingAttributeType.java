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


// ── CLASS: NonExistingAttributeType — LEIA'S HOLOGRAM, "HELP ME OBI-WAN" ────────────
// Princess Leia's hologram in R2-D2 is a reference to someone real — Leia is a living
// person with a name and a message — but what R2 carries is just the image, not Leia
// herself.  When Obi-Wan watches the message, he sees the name "Princess Leia Organa"
// but there is no actual person in the room.
// An attribute type editor might reference a superior attribute type by name — say
// "name" — but that type might not actually be loaded in the current schema.  This
// class is the hologram: it holds the name so the combo box can display and select it,
// even though no real AttributeType object backs it up.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Placeholder that represents an attribute type referenced by name in a schema definition
 * but not actually present in the currently loaded schema.
 * When an attribute type's superior or equality/ordering/substring matching rule refers
 * to something we can't resolve, we wrap that unresolved name in this class so the
 * combo boxes can still show it and let the user correct it — rather than silently
 * dropping the value.
 * Think of this as Leia's hologram: the name is there, the intent is clear, but the
 * real object isn't in the room.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NonExistingAttributeType
{
    public static final String NONE = "(None)"; //$NON-NLS-1$

    /** The name */
    private String name;


    // ── R2-D2 Records the Hologram Message ──────────────────────────────────────────
    // R2-D2 captures Leia's message and stores it internally — just the name and the
    // image, nothing more.  From this point on, anyone who asks R2 about the hologram
    // gets back that stored name.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new placeholder for an attribute type that doesn't exist in the schema.
     * We just store the name string; there's no lookup, no validation — this is purely
     * a carrier object so combo boxes have something to bind their selection to.
     * Pass {@link #NONE} as the name to represent "no superior type selected."
     *
     * <p>For example — R2 records the hologram:</p>
     * <pre>
     *   // The schema says "sup: attributeTypeDescription" but that AT isn't loaded:
     *   NonExistingAttributeType placeholder = new NonExistingAttributeType( "attributeTypeDescription" );
     *   // Now the combo can show it and the user can swap it for a real one.
     * </pre>
     *
     * @param name  the raw name string from the schema definition — could be a human-
     *              readable alias like "cn" or an OID; also accepts {@link #NONE}
     */
    public NonExistingAttributeType( String name )
    {
        this.name = name;
    }


    // ── Obi-Wan Asks What Her Name Is ───────────────────────────────────────────────
    // Obi-Wan watches the hologram and simply asks, "Who sent this?"  R2 plays back the
    // name exactly as recorded — no transformation, just retrieval.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the raw name string this placeholder was constructed with.
     * This is what the schema text originally said — not decorated with warning text.
     * Use {@link #getDisplayName()} if you need a user-facing string that flags the
     * missing type.
     *
     * @return  the stored name, exactly as passed to the constructor; never null
     *          if the constructor was called with a non-null argument
     */
    public String getName()
    {
        return name;
    }


    // ── Obi-Wan Reads the Full Briefing Aloud ───────────────────────────────────────
    // When Obi-Wan replays the message to Luke, he doesn't just say "Leia" — he gives
    // context: "Princess Leia Organa of Alderaan — she says she needs our help."  If
    // it's the "(None)" sentinel, he simply says "(None)" and moves on.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a display-ready string for use in combo boxes and labels.
     * If this placeholder represents the "no selection" sentinel (i.e. the name is
     * {@link #NONE}), we return "(None)" directly.  Otherwise we append a warning note
     * so the user can see at a glance that this attribute type isn't in the loaded schema
     * and needs attention.
     *
     * <p>For example — Obi-Wan gives the full briefing:</p>
     * <pre>
     *   // name = "(None)"           → getDisplayName() returns "(None)"
     *   // name = "badAttributeType" → returns "badAttributeType   (This attribute type doesnt exist)"
     * </pre>
     *
     * @return  either "(None)" or the name followed by a missing-type warning annotation
     */
    public String getDisplayName()
    {
        if ( name.equals( NONE ) )
        {
            return NONE;
        }
        else
        {
            return name + "   " + "(This attribute type doesnt exist)";
        }
    }


    // ── Obi-Wan Checks if Two Messages Are from the Same Person ─────────────────────
    // If two holograms both say "Princess Leia," they're considered the same message
    // regardless of how the name is capitalised in the recording.  Obi-Wan treats them
    // as equal because the intent is identical.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks equality by comparing the stored names case-insensitively.
     * We use case-insensitive comparison because LDAP schema is case-insensitive for
     * attribute type names; "cn" and "CN" refer to the same thing.
     * Two NonExistingAttributeType instances with the same name are considered the same
     * placeholder — this matters for deduplication in combo input lists.
     *
     * <p>For example — two holograms with the same name are treated as one:</p>
     * <pre>
     *   new NonExistingAttributeType( "cn" ).equals( new NonExistingAttributeType( "CN" ) )
     *   // → true, because LDAP name matching is case-insensitive
     * </pre>
     *
     * @param obj  the object to compare with; if it is not a NonExistingAttributeType
     *             we return false
     * @return     {@code true} if obj is a NonExistingAttributeType with the same name
     *             (ignoring case), {@code false} otherwise
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof NonExistingAttributeType )
        {
            return name.equalsIgnoreCase( ( ( NonExistingAttributeType ) obj ).getName() );
        }

        return false;
    }
}
