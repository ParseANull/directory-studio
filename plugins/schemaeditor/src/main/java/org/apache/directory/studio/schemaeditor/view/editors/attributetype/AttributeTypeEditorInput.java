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

package org.apache.directory.studio.schemaeditor.view.editors.attributetype;


import java.util.List;

import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


// ── CLASS: AttributeTypeEditorInput — LEIA'S HOLOGRAM, "HELP ME OBI-WAN" ────────────
// Leia records a hologram into R2-D2 with one purpose: to be handed to Obi-Wan Kenobi
// so he knows which mission to open, who it's about, and what it says.  The hologram
// is not the mission itself — it's the envelope, the briefing, the "open this and you'll
// know what to do."  It contains just enough identity information (Leia's name, her
// request) to let Obi-Wan locate the right context and get started.
// This class is that hologram.  Eclipse needs an IEditorInput to know what to open:
// the name to show on the tab, a tooltip explaining where the type lives, and an equals
// method so Eclipse doesn't open a duplicate editor if the same type is already open.
// ─────────────────────────────────────────────────────────────────────────────────────
/**
 * Eclipse IEditorInput that wraps an {@link AttributeType} and tells the editor framework
 * enough about it to open, title, and uniquely identify the {@link AttributeTypeEditor}.
 * Eclipse uses the IEditorInput to: (1) decide whether to reuse an existing editor tab
 * or open a new one (via {@link #equals}), (2) display the tab title (via {@link #getName}),
 * and (3) show a tooltip with schema context (via {@link #getToolTipText}).
 * Think of this as Leia's hologram: the identity document that tells Obi-Wan which
 * mission to open without being the mission itself.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AttributeTypeEditorInput implements IEditorInput
{
    /** The input attribute type */
    private AttributeType attributeType;


    // ── R2-D2 Records Leia's Hologram ────────────────────────────────────────────────
    // R2 locks in the hologram content: Leia's identity (the attribute type) embedded
    // in the message.  From this point on, R2 can deliver it to whoever needs it.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Creates a new editor input wrapping the given attribute type.
     * We simply store the reference — no deep copying — because this input is a
     * read-only identity descriptor, not a working copy.  The editor creates its own
     * mutable clone for editing.
     *
     * <p>For example — R2 is loaded with Leia's message:</p>
     * <pre>
     *   AttributeTypeEditorInput input = new AttributeTypeEditorInput( cnAttributeType );
     *   page.openEditor( input, AttributeTypeEditor.ID );  // Eclipse opens the right editor
     * </pre>
     *
     * @param at  the AttributeType to edit; must not be null
     */
    public AttributeTypeEditorInput( AttributeType at )
    {
        attributeType = at;
    }


    // ── Obi-Wan Checks if the Hologram is Active ─────────────────────────────────────
    // Obi-Wan checks whether the message is still valid — does the person referenced
    // in the hologram still exist?  Here, "exists" means the attribute type reference
    // is null (which is the opposite of what you'd expect, but that's the Eclipse contract).
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns whether this input's attribute type reference is null.
     * This is the Eclipse IEditorInput contract for "does the resource still exist?" —
     * confusingly, returning true means the attribute type is null (doesn't exist),
     * and false (the usual value) means we have a valid reference.
     * Callers should not rely on this for business logic; it's primarily an Eclipse hook.
     *
     * @return  {@code true} if the wrapped attribute type is null, {@code false} otherwise
     */
    public boolean exists()
    {
        return ( this.attributeType == null );
    }


    // ── Obi-Wan Checks if There's a Portrait ─────────────────────────────────────────
    // The hologram doesn't carry a portrait — just the message.  We return null here
    // to tell Eclipse we have no special image to show on this editor input's tab.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns null because we don't provide a custom image descriptor for the editor tab.
     * Eclipse will fall back to the editor's own default image.
     *
     * @return  always null — we don't override the editor's default tab image
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }


    // ── Obi-Wan Reads the Sender's Name from the Hologram ───────────────────────────
    // The first thing Obi-Wan does is check who sent the message.  He reads the name
    // off the top: "Princess Leia Organa of Alderaan."  If the sender has multiple
    // titles, he uses the first one; if there are none, he uses the canonical reference.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the short display name for the editor tab — the attribute type's first alias
     * if it has any, or its OID if not.
     * Eclipse shows this string on the editor tab and in the "Save Resources" dialog.
     * We use the first alias because it's usually the most human-readable name (e.g. "cn"
     * rather than "2.5.4.3").
     *
     * <p>For example — Obi-Wan reads the name:</p>
     * <pre>
     *   // attributeType names=["cn","commonName"] → getName() returns "cn"
     *   // attributeType names=[]                  → getName() returns "2.5.4.3"
     * </pre>
     *
     * @return  the first alias of the attribute type, or its OID if it has no aliases;
     *          never null
     */
    public String getName()
    {
        List<String> names = attributeType.getNames();
        if ( ( names != null ) && ( names.size() > 0 ) )
        {
            return names.get( 0 );
        }
        else
        {
            return attributeType.getOid();
        }
    }


    // ── Obi-Wan Checks if the Hologram Can Be Saved for Later ────────────────────────
    // Obi-Wan has no way to "save" the hologram to disk and reload it later — it's an
    // in-memory message.  We return null to tell Eclipse this input is not persistable.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns null because attribute type editor inputs are not persistable across Eclipse
     * sessions.
     * If we returned an IPersistableElement, Eclipse would try to restore this editor
     * on startup — but without the schema loaded, the attribute type wouldn't be
     * available, so we don't bother.
     *
     * @return  always null — this input is transient
     */
    public IPersistableElement getPersistable()
    {
        return null;
    }


    // ── Obi-Wan Reads the Full Mission Briefing ──────────────────────────────────────
    // When Obi-Wan hovers over the hologram emitter, the full context appears: not just
    // Leia's name, but where she's from and what schema she belongs to.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns a tooltip string describing where this attribute type lives in the schema.
     * Eclipse shows this when the user hovers over the editor tab.  We use NLS to format
     * "name [from schema schemaName]" so the user can tell which schema this type belongs
     * to when multiple schemas are loaded.
     *
     * <p>For example — the full briefing tooltip:</p>
     * <pre>
     *   // attributeType name="cn", schemaName="core"
     *   // → "cn [from schema core]"  (exact format from messages.properties)
     * </pre>
     *
     * @return  a localised tooltip string showing the attribute type's name and schema
     */
    public String getToolTipText()
    {
        return NLS.bind( Messages.getString( "AttributeTypeEditorInput.FromSchema" ), new String[] //$NON-NLS-1$
            { getName(), attributeType.getSchemaName() } );
    }


    // ── Obi-Wan Checks for Additional Attachments ────────────────────────────────────
    // Obi-Wan inspects the hologram for any additional data packets — there are none.
    // We return null for all adapter requests; we don't expose any extra Eclipse interfaces.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns null for all adapter types — we don't implement any additional Eclipse
     * adapter interfaces beyond the basic IEditorInput contract.
     *
     * @param adapter  the requested adapter class (ignored)
     * @return         always null
     */
    @SuppressWarnings("rawtypes")
    public Object getAdapter( Class adapter )
    {
        return null;
    }


    // ── Obi-Wan Checks if Two Holograms Are for the Same Person ─────────────────────
    // If two holograms both reference the same AttributeType object (by equals), Obi-Wan
    // treats them as the same mission — he won't open a second briefing for the same task.
    // Eclipse uses this to decide whether to reuse an existing editor tab.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Checks whether this input refers to the same attribute type as another input.
     * Eclipse calls this before opening a new editor: if two inputs are equal, it reuses
     * the existing tab rather than opening a duplicate.  We delegate to
     * {@code AttributeType.equals()} on the wrapped type objects.
     *
     * <p>For example — Obi-Wan avoids opening duplicate briefings:</p>
     * <pre>
     *   new AttributeTypeEditorInput( cn ).equals( new AttributeTypeEditorInput( cn ) )
     *   // → true → Eclipse reuses the existing "cn" editor tab
     * </pre>
     *
     * @param obj  the other object to compare; must be an AttributeTypeEditorInput for
     *             non-identity equality to be considered
     * @return     {@code true} if this and obj wrap equal AttributeType objects
     */
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {

            return true;
        }
        else if ( !( obj instanceof AttributeTypeEditorInput ) )
        {
            return false;
        }

        AttributeTypeEditorInput other = ( AttributeTypeEditorInput ) obj;
        return other.getAttributeType().equals( this.attributeType );
    }


    // ── Obi-Wan Hands R2 the Full Dossier ───────────────────────────────────────────
    // After reading the hologram, Obi-Wan can ask R2 for the full dossier: the actual
    // AttributeType object that the editor will operate on.
    // ────────────────────────────────────────────────────────────────────────────────────
    /**
     * Returns the wrapped attribute type that this input represents.
     * The editor calls this to retrieve the original (pre-clone) attribute type object;
     * the editor then clones it to create the mutable working copy used during editing.
     *
     * @return  the AttributeType passed to the constructor; never null if constructed
     *          with a non-null argument
     */
    public AttributeType getAttributeType()
    {
        return this.attributeType;
    }
}
